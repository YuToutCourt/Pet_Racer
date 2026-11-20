"""Test du serveur sans le jeu : lance server.py sur un port libre et joue le rôle de plusieurs jeux.

    python server\\test_server.py

Affiche « OK » pour chaque point vérifié et sort avec le code 1 au premier échec.
"""
import os
import shutil
import socket
import struct
import subprocess
import sys
import tempfile
import time

import protocol as P

HERE = os.path.dirname(os.path.abspath(__file__))
MOD_VERSION = 7  # ModNet._VERSION
TMP = tempfile.mkdtemp(prefix="pet_racer_test_")


class Game:
    """Un faux jeu : une socket UDP et un identifiant."""

    def __init__(self, port, client_id, name, mod_version=MOD_VERSION, token=None):
        self.server = ("127.0.0.1", port)
        self.client_id = client_id
        self.name = name
        self.mod_version = mod_version
        self.token = token or "jeton%027d" % client_id
        self.seq = 0
        self.info = None
        self.pending = []  # PEER / FROM arrivés pendant qu'on attendait une réponse INFO
        self.new_socket()

    def new_socket(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.sock.bind(("127.0.0.1", 0))
        self.sock.settimeout(0.05)

    def packets(self, wait=0.3):
        """Tous les paquets reçus pendant `wait` secondes : [(type, place, contenu)] pour PEER / FROM."""
        out, self.pending = self.pending, []
        end = time.time() + wait
        while time.time() < end:
            try:
                data, _ = self.sock.recvfrom(4096)
            except (socket.timeout, ConnectionResetError):
                continue
            ptype = P.read_head(data)
            if ptype in (P.PKT_PEER, P.PKT_FROM):
                slot, body = P.read_slot_packet(data)
                out.append((ptype, slot, body))
        return out

    def drain(self):
        self.packets(0.1)

    def ask(self, ptype, body=b""):
        """Envoie un paquet de salon et renvoie la réponse du serveur (PKT_INFO décodé), ou None après 1 s."""
        self.seq += 1
        self.sock.sendto(P.make_lobby(ptype, self.client_id, self.seq, body), self.server)
        return self.wait_info()

    def wait_info(self):
        end = time.time() + 1.0
        while time.time() < end:
            try:
                data, _ = self.sock.recvfrom(4096)
            except (socket.timeout, ConnectionResetError):
                continue
            ptype = P.read_head(data)
            if ptype in (P.PKT_PEER, P.PKT_FROM):
                slot, body = P.read_slot_packet(data)
                self.pending.append((ptype, slot, body))
            elif ptype == P.PKT_INFO:
                info = P.read_info(data)
                if info["seq"] == self.seq:
                    self.info = info
                    return info
        return None

    def hello(self, proto=P.LOBBY_PROTO):
        self.seq += 1
        self.sock.sendto(P.make_hello(self.client_id, self.seq, self.name, self.mod_version, self.token, proto), self.server)
        return self.wait_info()

    def create(self):
        return self.ask(P.PKT_CREATE)

    def join(self, code):
        return self.ask(P.PKT_JOIN, P.put_string(code))

    def leave(self):
        return self.ask(P.PKT_LEAVE)

    def state(self, payload, flags=0):
        return self.ask(P.PKT_STATE, bytes([flags]) + payload)

    def relay(self, to_slot, inner):
        self.sock.sendto(P.make_relay(self.client_id, to_slot, inner), self.server)

    def queue(self):
        return self.ask(P.PKT_QUEUE)

    def wait_packet(self, want):
        """Le premier paquet de type `want` reçu dans la seconde (les autres sont gardés ou ignorés)."""
        end = time.time() + 1.0
        while time.time() < end:
            try:
                data, _ = self.sock.recvfrom(4096)
            except (socket.timeout, ConnectionResetError):
                continue
            ptype = P.read_head(data)
            if ptype in (P.PKT_PEER, P.PKT_FROM):
                self.pending.append((ptype,) + P.read_slot_packet(data))
            elif ptype == want:
                return data
        return None

    def result(self, order, flags=0):
        """Annonce la fin de la course classée ; renvoie le PKT_RATED décodé."""
        self.seq += 1
        self.sock.sendto(P.make_result(self.client_id, self.seq, order, flags), self.server)
        data = self.wait_packet(P.PKT_RATED)
        return P.read_rated(data) if data else None

    def top(self):
        self.seq += 1
        self.sock.sendto(P.make_lobby(P.PKT_TOP, self.client_id, self.seq), self.server)
        data = self.wait_packet(P.PKT_TOP_LIST)
        return P.read_top_list(data) if data else None


checks = 0


def check(what, ok):
    global checks
    checks += 1
    print(("OK    " if ok else "ECHEC ") + what, flush=True)
    if not ok:
        raise SystemExit(1)


def free_port():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(("127.0.0.1", 0))
    port = s.getsockname()[1]
    s.close()
    return port


def start_server(*args):
    port = free_port()
    proc = subprocess.Popen([sys.executable, os.path.join(HERE, "server.py"), "--host", "127.0.0.1", "--port", str(port),
                             "--db", os.path.join(TMP, "test.db")] + list(args),
                            stdout=subprocess.DEVNULL, stderr=subprocess.STDOUT)
    probe = Game(port, 999, "sonde", token="x")  # jeton refusé : la sonde n'a pas de compte
    for _ in range(30):
        if probe.hello() is not None:
            return proc, port
    proc.kill()
    raise SystemExit("le serveur ne démarre pas")


def full_room(port, count=6, base=1):
    games = [Game(port, base + i, "Joueur%d" % i) for i in range(count)]
    for g in games:
        g.hello()
    code = games[0].create()["code"]
    for g in games[1:]:
        g.join(code)
    for g in games:
        g.drain()
    return games, code


def car(slot, n):
    """Faux paquet de course (le serveur ne le lit pas)."""
    return struct.pack(">IBBi", 0x50524E31, 3, slot, n) + bytes(60)


def test_rooms(port):
    a = Game(port, 1, "Alice")
    info = a.hello()
    check("bonjour : réponse sans salon", info is not None and info["status"] == P.STATUS_OK and info["code"] == "")
    bad = Game(port, 50, "Vieux")
    check("autre version du dialogue refusée", bad.hello(proto=P.LOBBY_PROTO + 1)["status"] == P.STATUS_BAD_PROTO)
    stranger = Game(port, 51, "Inconnu")
    check("jeu inconnu : doit se présenter", stranger.create()["status"] == P.STATUS_UNKNOWN)

    info = a.create()
    code = info["code"]
    check("salon créé : code de 4 lettres, place 0, hôte", len(code) == 4 and info["my_slot"] == 0 and info["host_slot"] == 0)
    check("création répétée : même salon", a.create()["code"] == code)

    others = [Game(port, 10 + i, "Bob%d" % i) for i in range(5)]
    for i, g in enumerate(others):
        g.hello()
        info = g.join(code)
        check("invité %d entre en place %d" % (i + 1, i + 1), info["join_error"] == P.JOIN_OK and info["my_slot"] == i + 1)
    info = a.ask(P.PKT_STATE, b"\x00")
    check("6 noms dans INFO", info["names"] == ["Alice"] + ["Bob%d" % i for i in range(5)])

    late = Game(port, 20, "Tard")
    late.hello()
    check("7e joueur : salon plein", late.join(code)["join_error"] == P.JOIN_FULL)
    check("code inconnu", late.join("ZZZZ" if code != "ZZZZ" else "YYYY")["join_error"] == P.JOIN_UNKNOWN)
    old = Game(port, 21, "Vieux", mod_version=MOD_VERSION - 1)
    old.hello()
    others[4].leave()
    check("autre version du mod refusée", old.join(code)["join_error"] == P.JOIN_VERSION)
    check("place libérée : le suivant la prend", late.join(code)["my_slot"] == 5)

    for g in [a] + others[:4] + [late]:
        g.drain()
    # États recopiés
    others[1].state(b"animal=3")
    got = {g.name: g.packets(0.3) for g in [a, others[0], others[2], others[3], late]}
    check("état de la place 2 recopié aux 5 autres",
          all(pk == [(P.PKT_PEER, 2, b"animal=3")] for pk in got.values()))
    check("pas recopié à l'expéditeur", others[1].packets(0.2) == [])

    others[1].state(b"x", flags=P.ROOM_LOCKED)
    check("un invité ne ferme pas le salon", not a.state(b"hote")["locked"])
    info = a.state(b"hote", flags=P.ROOM_LOCKED)
    check("l'hôte ferme le salon", info["locked"])
    newcomer = Game(port, 22, "Nouveau")
    newcomer.hello()
    others[3].leave()
    check("salon fermé : refusé", newcomer.join(code)["join_error"] == P.JOIN_LOCKED)
    a.state(b"hote", flags=0)
    check("rouvert : il entre en place 4", newcomer.join(code)["my_slot"] == 4)
    pk = newcomer.packets(0.3)
    check("l'arrivant reçoit l'état des autres tout de suite",
          (P.PKT_PEER, 0, b"hote") in pk and (P.PKT_PEER, 2, b"x") in pk)

    members = [a, others[0], others[1], others[2], newcomer, late]  # places 0..5
    for g in members:
        g.drain()
    # Relais
    members[3].relay(0, car(3, 1))
    check("relais vers l'hôte : reçu avec la place de l'expéditeur", a.packets(0.3) == [(P.PKT_FROM, 3, car(3, 1))])
    check("les autres ne le reçoivent pas", all(g.packets(0.05) == [] for g in members[1:]))
    members[2].relay(P.ALL, car(2, 7))
    ok = all(g.packets(0.2) == [(P.PKT_FROM, 2, car(2, 7))] for i, g in enumerate(members) if i != 2)
    check("relais à tout le salon : les 5 autres", ok and members[2].packets(0.05) == [])
    members[1].relay(1, car(1, 0))
    members[1].relay(9, car(1, 0))
    check("relais vers soi ou vers une place hors salon : rien", all(g.packets(0.05) == [] for g in members))
    members[1].relay(0, bytes(P.MAX_INNER + 1))
    check("paquet trop gros : jeté", a.packets(0.2) == [])
    outsider = Game(port, 23, "Dehors")
    outsider.hello()
    outsider.relay(P.ALL, car(0, 0))
    check("joueur hors salon : rien relayé", all(g.packets(0.05) == [] for g in members))

    # Adresse qui change en route
    members[5].new_socket()
    members[5].relay(0, car(5, 2))
    check("adresse changée : relais toujours accepté", a.packets(0.3) == [(P.PKT_FROM, 5, car(5, 2))])
    a.relay(5, car(0, 3))
    check("... et réponse vers la nouvelle adresse", members[5].packets(0.3) == [(P.PKT_FROM, 0, car(0, 3))])

    # Départ de l'hôte
    info = a.leave()
    check("l'hôte sort : plus de salon pour lui", info["code"] == "")
    info = others[0].state(b"")
    check("la place 1 devient l'hôte, le salon continue", info["code"] == code and info["host_slot"] == 1 and info["names"][0] is None)
    check("nouvel arrivant en place 0", a.join(code)["my_slot"] == 0 and a.info["host_slot"] == 1)
    for g in members:
        g.leave()
    check("tous partis : salon fermé", Game(port, 24, "Après").hello() and newcomer.join(code)["join_error"] == P.JOIN_UNKNOWN)
    newcomer.leave()


def test_stale_and_timeout():
    proc, port = start_server("--stale", "1", "--timeout", "2")
    try:
        games, code = full_room(port, base=100)
        newcomer = Game(port, 110, "Nouveau")
        newcomer.hello()
        check("salon plein, tous actifs : refusé", newcomer.join(code)["join_error"] == P.JOIN_FULL)
        time.sleep(0.6)
        for g in games[:3] + games[4:]:
            g.state(b"")
        time.sleep(0.6)
        newcomer.hello()
        info = newcomer.join(code)
        check("un joueur muet depuis 1 s cède sa place", info["join_error"] == P.JOIN_OK and info["my_slot"] == 3)
        check("l'hôte n'est jamais remplacé", info["host_slot"] == 0)
        for _ in range(3):
            time.sleep(0.8)
            games[0].state(b"")
            newcomer.state(b"")
        info = games[0].state(b"")
        check("silence de 2 s : les muets sortent du salon", [n is not None for n in info["names"]] == [True, False, False, True, False, False])
    finally:
        proc.kill()


def test_takeover():
    proc, port = start_server("--takeover", "0.5")
    try:
        games, code = full_room(port, count=3, base=200)
        time.sleep(0.8)
        relaunched = Game(port, 299, "Joueur2", token=games[2].token)
        info = relaunched.hello()
        check("jeu relancé (même compte) : il retrouve sa place", info["code"] == code and info["my_slot"] == 2)
        info = games[0].state(b"")
        check("une seule fois dans le salon", sum(n is not None for n in info["names"]) == 3)
    finally:
        proc.kill()


def test_lag():
    proc, port = start_server("--lag", "150")
    try:
        games, code = full_room(port, count=2, base=300)
        start = time.time()
        games[1].relay(0, car(1, 0))
        got = []
        while not got and time.time() - start < 1.0:
            got = games[0].packets(0.02)
        delay = time.time() - start
        check("--lag 150 : paquet relayé après %.0f ms" % (delay * 1000), got and 0.14 <= delay < 0.5)
    finally:
        proc.kill()
    proc, port = start_server("--loss", "100")
    try:
        games, code = full_room(port, count=2, base=400)
        for i in range(10):
            games[1].relay(0, car(1, i))
        check("--loss 100 : rien ne passe", games[0].packets(0.3) == [])
        check("... mais le salon répond", games[1].state(b"")["code"] == code)
    finally:
        proc.kill()


def test_elo_formula():
    from store import race_deltas
    check("Elo : 3 joueurs égaux, 10 courses et plus : +16 / 0 / -16",
          race_deltas([(1000, 10, 0), (1000, 10, 1), (1000, 10, 2)]) == [16, 0, -16])
    check("Elo : premières courses, K = 48 : +24 / 0 / -24",
          race_deltas([(1000, 0, 0), (1000, 0, 1), (1000, 0, 2)]) == [24, 0, -24])
    d = race_deltas([(1000, 20, 1), (1000, 20, 1), (1000, 20, 0)])
    check("Elo : deux partis à égalité derrière (nul entre eux)", d[0] == d[1] < 0 < d[2])
    d = race_deltas([(1400, 20, 0), (1000, 20, 1)])
    check("Elo : le favori qui gagne prend peu, l'outsider perd peu", 0 < d[0] < 8 and -8 < d[1] < 0)


def queued(games, rounds=8):
    """Les jeux redisent PKT_QUEUE jusqu'à être dans un salon (ou `rounds` fois) ; renvoie leurs derniers INFO."""
    for _ in range(rounds):
        infos = [g.queue() for g in games]
        if all(i is not None and i["code"] for i in infos):
            return infos
        time.sleep(0.4)
    return [g.info for g in games]


def test_quick_match():
    db = os.path.join(TMP, "rapide.db")
    proc, port = start_server("--fill", "1", "--db", db)
    try:
        games = [Game(port, 600 + i, "Rapide%d" % i) for i in range(3)]
        for g in games:
            g.hello()
        info = games[0].queue()
        check("partie rapide : seul, on attend (pas de salon), 1 en file", info["code"] == "" and info["queue"] >= 1)
        check("accueil : Elo de départ 1000, pas encore classé", info["elo"] == 1000 and info["games"] == 0 and info["rank"] == 0)
        infos = queued(games)
        codes = set(i["code"] for i in infos)
        check("3 joueurs réunis dans un même salon classé", len(codes) == 1 and "" not in codes
              and all(i["ranked_room"] for i in infos))
        check("places 0, 1, 2 ; un seul hôte", sorted(i["my_slot"] for i in infos) == [0, 1, 2]
              and len(set(i["host_slot"] for i in infos)) == 1)
        code = infos[0]["code"]
        other = Game(port, 650, "Curieux")
        other.hello()
        check("salon de partie rapide : impossible d'y entrer par son code", other.join(code)["join_error"] == P.JOIN_UNKNOWN)
        by_slot = {i["my_slot"]: g for g, i in zip(games, infos)}
        host = by_slot[infos[0]["host_slot"]]
        check("pas de course classée avant le départ", games[0].result([0, 1, 2])["state"] == P.RATED_NONE)
        host.state(b"", P.ROOM_LOCKED)
        r = by_slot[0].result([2, 0, 1])
        check("départ : premier résultat, on attend les autres", r["state"] == P.RATED_PENDING and r["humans"] == 3)
        by_slot[1].result([2, 0, 1])
        r = by_slot[2].result([2, 0, 1])
        check("tous d'accord : course comptée, la place 2 première, +24", r["state"] == P.RATED_DONE and r["place"] == 1
              and r["delta"] == 24 and r["elo"] == 1024 and r["rank"] == 1)
        r = by_slot[1].result([2, 0, 1])
        check("résultat redit : même réponse (place 3, -24)", r["state"] == P.RATED_DONE and r["place"] == 3 and r["delta"] == -24)
        top = games[0].top()
        check("classement : 3 classés, dans l'ordre de l'Elo", top["total"] == 3
              and [row[1] for row in top["rows"]] == [1024, 1000, 976] and top["rows"][0][2] == 1)
        for g in games:
            g.leave()
        info = by_slot[1].hello()
        check("accueil : Elo et rang à jour (976, 3e)", info["elo"] == 976 and info["games"] == 1 and info["rank"] == 3)

        # deuxième course : un abandon
        infos = queued(games)
        by_slot = {i["my_slot"]: g for g, i in zip(games, infos)}
        check("on peut rejouer : nouveau salon classé", len(set(i["code"] for i in infos)) == 1 and infos[0]["code"] != code)
        by_slot[infos[0]["host_slot"]].state(b"", P.ROOM_LOCKED)
        quitter = by_slot[0]
        r = quitter.result([0], P.RESULT_ABANDON)
        check("abandon annoncé : en attente des autres", r["state"] == P.RATED_PENDING)
        quitter.leave()
        by_slot[1].result([1, 2, 0])
        r = by_slot[2].result([1, 2, 0])
        r0 = quitter.result([0], P.RESULT_ABANDON)
        check("le joueur parti est classé dernier et perd de l'Elo", r["state"] == P.RATED_DONE and r0["place"] == 3 and r0["delta"] < 0)

        # troisième course : deux joueurs qui ne voient pas le même ordre
        for g in games:
            g.leave()
        pair = games[1:]
        infos = queued(pair)
        by_slot = {i["my_slot"]: g for g, i in zip(pair, infos)}
        by_slot[infos[0]["host_slot"]].state(b"", P.ROOM_LOCKED)
        by_slot[0].result([0, 1])
        r = by_slot[1].result([1, 0])
        check("classements différents à 2 : course pas comptée", r["state"] == P.RATED_VOID)

        # quatrième : un joueur ferme son jeu sans rien dire
        for g in pair:
            g.leave()
        infos = queued(pair)
        by_slot = {i["my_slot"]: g for g, i in zip(pair, infos)}
        by_slot[infos[0]["host_slot"]].state(b"", P.ROOM_LOCKED)
        by_slot[1].leave()  # parti sans résultat (le serveur le voit sortir du salon)
        r = by_slot[0].result([0, 1])
        check("seul à finir, l'autre parti sans résultat : comptée, 1er", r["state"] == P.RATED_DONE and r["place"] == 1 and r["delta"] > 0)
    finally:
        proc.kill()

    # six joueurs : la course part sans attendre le délai
    proc, port = start_server("--fill", "60", "--db", os.path.join(TMP, "six.db"))
    try:
        six = [Game(port, 700 + i, "Six%d" % i) for i in range(7)]
        for g in six:
            g.hello()
        infos = queued(six[:6], rounds=3)
        check("6 joueurs en file : course tout de suite", len(set(i["code"] for i in infos)) == 1 and infos[0]["code"] != "")
        info = six[6].queue()
        check("le 7e attend la suivante", info["code"] == "" and info["queue"] == 1)
    finally:
        proc.kill()


def main():
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    try:
        proc, port = start_server()
        try:
            test_rooms(port)
        finally:
            proc.kill()
        test_stale_and_timeout()
        test_takeover()
        test_lag()
        test_elo_formula()
        test_quick_match()
    finally:
        shutil.rmtree(TMP, ignore_errors=True)
    print("%d points vérifiés" % checks)


if __name__ == "__main__":
    main()
