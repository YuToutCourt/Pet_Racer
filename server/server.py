"""Serveur du multijoueur en ligne de Pet Racer : salons à code jusqu'à 6 joueurs, partie rapide classée (Elo) et
relais UDP.

    python server\\server.py [--port 7778] [--db fichier] [--fill 15] [--lag 80] [--loss 5] [--code TEST] [-v]

Le serveur ne simule rien. Un joueur crée un salon et reçoit un code de 4 lettres, les autres entrent ce code ;
ensuite le serveur recopie les paquets de course d'un jeu vers une place du salon ou vers tout le salon, donc
personne n'ouvre de port chez soi (sauf celui qui héberge le serveur).
Un seul port UDP, une boucle, aucun thread, bibliothèque standard seule. Détails dans notes/online.md.

  --lag, --jitter, --loss : simulent une mauvaise connexion sur les paquets relayés (tests).
  --code : tous les salons créés portent ce code (tests automatiques : un seul salon à la fois).
  --fill : partie rapide, secondes d'attente avant de lancer une course à moins de 6 joueurs (2 au moins).
"""
import argparse
import heapq
import itertools
import os
import random
import socket
import struct
import sys
import time

import protocol as P
from rooms import Lobby
from store import Store, clean_name


class Server:
    def __init__(self, sock, lobby, store=None, lag=0.0, jitter=0.0, loss=0.0, verbose=False):
        self.sock = sock
        self.lobby = lobby
        self.store = store
        self.lag = lag  # secondes ajoutées à chaque paquet relayé
        self.jitter = jitter
        self.loss = loss  # part des paquets relayés perdus, 0..1
        self.verbose = verbose
        self.delayed = []  # (heure d'envoi, n° d'ordre, paquet, adresse)
        self.order = itertools.count()
        self.stats = dict(recv=0, relayed=0, lost=0)
        lobby.log = self.log

    def log(self, text):
        print(time.strftime("%H:%M:%S ") + text, flush=True)

    def send(self, data, addr):
        try:
            self.sock.sendto(data, addr)
        except OSError as e:
            if self.verbose:
                self.log("envoi impossible vers %s:%d : %s" % (addr[0], addr[1], e))

    def relay(self, data, addr, now):
        """Envoie un paquet d'un joueur à un autre, avec le retard et les pertes demandés."""
        self.stats["relayed"] += 1
        if self.loss > 0 and random.random() < self.loss:
            self.stats["lost"] += 1
            return
        delay = self.lag + (random.uniform(0, self.jitter) if self.jitter > 0 else 0)
        if delay <= 0:
            self.send(data, addr)
        else:
            heapq.heappush(self.delayed, (now + delay, next(self.order), data, addr))

    def flush(self, now):
        while self.delayed and self.delayed[0][0] <= now:
            _, _, data, addr = heapq.heappop(self.delayed)
            self.send(data, addr)

    def info(self, session, seq, join_error=P.JOIN_OK):
        room = session.room
        online = len(self.lobby.sessions)
        account = dict(queue=len(self.lobby.queue))
        player = session.player
        if player is not None and room is None and self.store is not None:
            # hors salon (accueil de l'écran « En ligne ») : Elo et rang du joueur
            account.update(elo=player.elo, games=player.games, rank=self.store.rank(player), ranked=self.store.total())
        if room is None:
            return P.make_info(seq, P.STATUS_OK, join_error=join_error, online=online, **account)
        return P.make_info(seq, P.STATUS_OK, room.code, session.slot, room.host.slot, room.info_flags(), join_error,
                           online, room.names(), **account)

    def rated(self, session, seq):
        race = session.race
        if race is None:
            return P.make_rated(seq, P.RATED_NONE)
        humans = len(race.slots)
        if race.state != P.RATED_DONE or session not in race.outcome:
            return P.make_rated(seq, race.state, humans=humans)
        place, delta = race.outcome[session]
        player = session.player
        rank = self.store.rank(player) if self.store is not None else 0
        return P.make_rated(seq, P.RATED_DONE, place, humans, delta, player.elo, rank)

    def top_list(self):
        if self.store is None:
            return P.make_top_list(0, [])
        return P.make_top_list(self.store.total(), [(p.name, p.elo, p.games) for p in self.store.top(P.TOP_COUNT)])

    def login(self, client_id, name, token):
        """Le compte du joueur qui se présente. Lu dans la base une fois par jeu, pas à chaque bonjour."""
        if self.store is None:
            return None
        known = self.lobby.sessions.get(client_id)
        if known is not None and known.player is not None and known.token == token and known.player.name == name:
            return known.player
        return self.store.login(token, name)

    def handle(self, data, addr, now):
        ptype = P.read_head(data)
        if ptype is None:
            return
        self.stats["recv"] += 1
        lobby = self.lobby

        if ptype == P.PKT_RELAY:
            client_id, to_slot, inner = P.read_relay(data)
            session = lobby.sessions.get(client_id)
            if session is None or session.room is None or len(inner) > P.MAX_INNER:
                return
            lobby.touch(session, addr, now)
            out = P.make_from(session.slot, inner)
            for target in lobby.targets(session, to_slot):
                session.room.relayed += 1
                self.relay(out, target.addr, now)
            return

        if ptype not in (P.PKT_HELLO, P.PKT_CREATE, P.PKT_JOIN, P.PKT_LEAVE, P.PKT_STATE, P.PKT_QUEUE, P.PKT_RESULT,
                         P.PKT_TOP):
            return
        client_id, seq = P.read_lobby_head(data)

        if ptype == P.PKT_HELLO:
            proto, mod_version, name, token = P.read_hello(data)
            if proto != P.LOBBY_PROTO:
                self.send(P.make_info(seq, P.STATUS_BAD_PROTO), addr)
                return
            name = clean_name(name)
            session = lobby.hello(client_id, addr, name, mod_version, now, self.login(client_id, name, token), token)
            self.send(self.info(session, seq), addr)
            return

        session = lobby.sessions.get(client_id)
        if session is None:
            # Serveur relancé, ou joueur oublié après un long silence : il doit se présenter de nouveau
            self.send(P.make_info(seq, P.STATUS_UNKNOWN), addr)
            return
        lobby.touch(session, addr, now)

        if ptype == P.PKT_RESULT:
            flags, order = P.read_result(data)
            lobby.report(session, flags, order, now)
            self.send(self.rated(session, seq), addr)
            return
        if ptype == P.PKT_TOP:
            self.send(self.top_list(), addr)
            return

        join_error = P.JOIN_OK
        if ptype == P.PKT_CREATE:
            lobby.create(session)
        elif ptype == P.PKT_JOIN:
            join_error = lobby.join(session, P.read_join(data), now)
            if join_error == P.JOIN_OK:
                # l'arrivant voit tout de suite ce que montrent les autres, sans attendre leur prochain état
                for other in session.room.members():
                    if other is not session and other.state:
                        self.send(P.make_peer(other.slot, other.state), addr)
        elif ptype == P.PKT_LEAVE:
            lobby.leave(session)
        elif ptype == P.PKT_QUEUE:
            lobby.enqueue(session, now)
        elif ptype == P.PKT_STATE:
            flags, payload = P.read_state(data)
            out = P.make_peer(session.slot, payload)
            for other in lobby.set_state(session, flags, payload):
                self.relay(out, other.addr, now)
        self.send(self.info(session, seq, join_error), addr)

    def run(self):
        next_expire = next_stats = time.monotonic()
        while True:
            now = time.monotonic()
            wait = 0.5
            if self.delayed:
                wait = max(0.0, min(wait, self.delayed[0][0] - now))
            self.sock.settimeout(wait if wait > 0 else 0.0001)
            try:
                data, addr = self.sock.recvfrom(4096)
            except socket.timeout:
                data = None
            except ConnectionResetError:
                data = None  # Windows : un joueur a fermé son jeu, son port ne répond plus
            except OSError as e:
                self.log("erreur de lecture : %s" % e)
                data = None

            now = time.monotonic()
            if data is not None:
                try:
                    self.handle(data, addr, now)
                except (ValueError, IndexError, struct.error) as e:
                    if self.verbose:
                        self.log("paquet illisible de %s:%d : %s" % (addr[0], addr[1], e))
            self.flush(now)

            if now >= next_expire:
                next_expire = now + 1.0
                self.lobby.expire(now)
            if self.verbose and now >= next_stats:
                next_stats = now + 10.0
                if self.stats["recv"]:
                    self.log("10 s : %d paquets reçus, %d relayés (%d perdus exprès), %d joueurs, %d salons" % (
                        self.stats["recv"], self.stats["relayed"], self.stats["lost"],
                        len(self.lobby.sessions), len(self.lobby.rooms)))
                self.stats = dict(recv=0, relayed=0, lost=0)


def open_socket(host, port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    if sys.platform == "win32":
        # Sans cela, un envoi vers un port fermé fait échouer les lectures suivantes (WSAECONNRESET)
        try:
            sock.ioctl(getattr(socket, "SIO_UDP_CONNRESET", 0x9800000C), False)
        except (OSError, ValueError):
            pass
    sock.bind((host, port))
    return sock


def main():
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")  # journal lisible même redirigé vers un fichier
    ap = argparse.ArgumentParser(description="Serveur du multijoueur en ligne de Pet Racer (salons à 6, relais UDP).")
    ap.add_argument("--db", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "players.db"),
                    help="fichier SQLite des comptes (players.db à côté de server.py par défaut)")
    ap.add_argument("--host", default="0.0.0.0", help="adresse d'écoute (0.0.0.0 : toutes)")
    ap.add_argument("--port", type=int, default=7778, help="port UDP (7778 par défaut)")
    ap.add_argument("--lag", type=float, default=0, help="retard ajouté à chaque paquet relayé, en ms")
    ap.add_argument("--jitter", type=float, default=0, help="retard aléatoire en plus, de 0 à N ms")
    ap.add_argument("--loss", type=float, default=0, help="pourcentage de paquets relayés perdus")
    ap.add_argument("--code", default=None, help="code imposé aux salons créés (tests : un seul salon)")
    ap.add_argument("--timeout", type=float, default=30, help="secondes de silence avant d'oublier un joueur")
    ap.add_argument("--stale", type=float, default=10, help="secondes de silence avant qu'un joueur d'un salon plein cède sa place")
    ap.add_argument("--takeover", type=float, default=3, help="secondes de silence avant qu'un jeu relancé reprenne la place de l'ancien")
    ap.add_argument("--fill", type=float, default=15, help="partie rapide : attente avant une course à moins de 6 joueurs, en s")
    ap.add_argument("-v", "--verbose", action="store_true", help="statistiques toutes les 10 s, paquets illisibles")
    args = ap.parse_args()

    code = args.code.strip().upper() if args.code else None
    store = Store(args.db)
    lobby = Lobby(fixed_code=code, timeout=args.timeout, stale=args.stale, takeover_after=args.takeover, store=store,
                  fill=args.fill)
    sock = open_socket(args.host, args.port)
    server = Server(sock, lobby, store, args.lag / 1000.0, args.jitter / 1000.0, args.loss / 100.0, args.verbose)
    extras = []
    if args.lag or args.jitter:
        extras.append("retard %g ms + 0 à %g ms" % (args.lag, args.jitter))
    if args.loss:
        extras.append("pertes %g %%" % args.loss)
    if code:
        extras.append("code imposé %s" % code)
    server.log("serveur Pet Racer sur le port UDP %d%s" % (args.port, " (" + ", ".join(extras) + ")" if extras else ""))
    server.log("comptes : %s" % args.db)
    try:
        server.run()
    except KeyboardInterrupt:
        server.log("arrêt")
    finally:
        store.close()


if __name__ == "__main__":
    main()
