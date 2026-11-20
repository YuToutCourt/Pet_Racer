"""Joueurs connectés, salons à code (jusqu'à 6 places) et partie rapide classée. Aucun réseau ici : server.py s'en
occupe.

Un salon a 6 places numérotées 0 à 5. Son créateur prend la place 0 et devient l'hôte ; chaque arrivant prend la
plus petite place libre et la garde tant qu'il reste. Les places servent d'adresse au relais : un jeu envoie un
paquet de course « à la place 3 » ou « à tout le salon », le serveur le rend en disant de quelle place il vient.

Hôte parti : le salon ne ferme pas, l'hôte devient le joueur de plus petite place (la course en cours peut
continuer chez les autres). Le salon ferme quand il est vide.

Partie rapide (phase 4) : le joueur attend dans une file (PKT_QUEUE redit chaque seconde). Le serveur réunit jusqu'à
6 joueurs d'Elo proches (l'écart accepté grandit avec l'attente) et crée pour eux un salon « classé » sans code à
donner ; la course part dès 6 joueurs, ou quand le plus ancien attend depuis `fill` secondes et qu'ils sont au moins
2. Le jeu de l'hôte la lance tout seul (réglages imposés, IA dans les places libres). La course commence quand l'hôte
ferme le salon : les joueurs présents à ce moment sont ceux de la course classée (Race). Chacun annonce à la fin
l'ordre d'arrivée des humains qu'il a vu (PKT_RESULT) ; quand tous l'ont dit (ou sont partis), le serveur compte la
course si les classements concordent (sinon celui de la majorité, ou course annulée).
"""
import random
from collections import Counter

import protocol as P

# Lettres d'un code de salon : sans I ni O, trop proches de 1 et 0 quand on dicte le code
CODE_LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ"
CODE_LENGTH = 4


class Session:
    """Un jeu connecté au serveur."""

    def __init__(self, client_id, addr, name, mod_version, now):
        self.client_id = client_id
        self.addr = addr
        self.name = name
        self.mod_version = mod_version
        self.last_seen = now
        self.room = None
        self.slot = P.NO_SLOT
        self.token = ""
        self.player = None  # compte du joueur (store.Player), None sans jeton valable
        self.state = b""  # dernier PKT_STATE, recopié aux arrivants
        self.queue_since = None  # partie rapide : heure d'entrée dans la file
        self.last_queue = 0.0
        self.race = None  # dernière course classée de ce joueur (Race), gardée après sa sortie du salon

    def __repr__(self):
        return "%s (%s:%d)" % (self.name, self.addr[0], self.addr[1])


class Room:
    def __init__(self, code, host, ranked=False):
        self.code = code
        self.slots = [None] * P.MAX_PLAYERS
        self.slots[0] = host
        self.host = host
        self.flags = 0  # P.ROOM_LOCKED, posé par l'hôte
        self.ranked = ranked  # partie rapide
        self.race = None  # course classée (Race) : une seule par salon de partie rapide
        self.relayed = 0  # paquets de course recopiés

    def info_flags(self):
        return self.flags | (P.ROOM_RANKED if self.ranked else 0)

    def members(self):
        return [s for s in self.slots if s is not None]

    def free_slot(self):
        for i, s in enumerate(self.slots):
            if s is None:
                return i
        return None

    def names(self):
        return [s.name if s is not None else None for s in self.slots]


class Race:
    """Course classée : les joueurs du salon au moment où l'hôte l'a lancée, et ce que chacun en dit."""

    def __init__(self, room, now):
        self.code = room.code
        self.slots = {s.slot: s for s in room.members() if s.player is not None}
        self.started = now
        self.first_finish = None  # premier résultat « arrivé » reçu
        self.reports = {}  # Session -> (parti, places dans l'ordre d'arrivée)
        self.left = set()  # sortis du salon (ou du serveur) sans avoir donné de résultat
        self.state = P.RATED_PENDING
        self.outcome = {}  # Session -> (place parmi les humains, à partir de 1 ; variation d'Elo)

    def sessions(self):
        return list(self.slots.values())

    def slot_of(self, session):
        for slot, s in self.slots.items():
            if s is session:
                return slot
        return None


class Lobby:
    def __init__(self, fixed_code=None, timeout=30.0, stale=10.0, takeover_after=3.0, max_rooms=500, rng=None,
                 store=None, fill=15.0, queue_range=150.0, queue_grow=10.0, queue_stale=4.0, report_wait=90.0,
                 race_max=1200.0):
        self.sessions = {}  # identifiant du jeu -> Session
        self.by_player = {}  # numéro du compte -> sa dernière Session
        self.rooms = {}  # code -> Room
        self.fixed_code = fixed_code
        self.timeout = timeout  # silence après lequel un joueur est oublié
        self.stale = stale  # silence après lequel un joueur d'un salon plein peut céder sa place à un arrivant
        self.takeover_after = takeover_after  # silence après lequel un jeu relancé reprend la place de l'ancien
        self.max_rooms = max_rooms
        self.rng = rng or random.Random()
        self.store = store
        self.queue = []  # sessions qui attendent une partie rapide, de la plus ancienne à la plus récente
        self.fill = fill  # attente du plus ancien avant de lancer une course à moins de 6
        self.queue_range = queue_range  # écart d'Elo accepté au départ...
        self.queue_grow = queue_grow  # ... plus tant par seconde d'attente
        self.queue_stale = queue_stale  # un joueur qui ne redit pas PKT_QUEUE depuis tant de secondes n'attend plus
        self.report_wait = report_wait  # après le premier « arrivé », délai laissé aux autres jeux pour leur résultat
        self.race_max = race_max  # durée au-delà de laquelle une course classée est tranchée avec ce qu'on sait
        self.races = []  # courses classées pas encore tranchées
        self.now = 0.0
        self.log = lambda text: None

    # --- joueurs ---

    def hello(self, client_id, addr, name, mod_version, now, player=None, token=""):
        self.now = now
        session = self.sessions.get(client_id)
        if session is None:
            session = Session(client_id, addr, name, mod_version, now)
            self.sessions[client_id] = session
            self.log("bonjour %r, mod v%d%s" % (session, mod_version, ", compte %r" % player if player else ""))
            old = self.by_player.get(player.id) if player is not None else None
            if old is not None and old.client_id in self.sessions and now - old.last_seen >= self.takeover_after:
                self.takeover(old, session)
        session.name = name
        session.mod_version = mod_version
        session.token = token
        session.player = player
        if player is not None:
            self.by_player[player.id] = session
        self.touch(session, addr, now)
        return session

    def touch(self, session, addr, now):
        """Le joueur vient de parler. Son adresse peut changer en route (box, 4G) : on suit la dernière."""
        self.now = now
        session.last_seen = now
        if session.addr != addr:
            session.addr = addr

    def forget(self, session):
        self.unqueue(session, "parti")
        self.sessions.pop(session.client_id, None)
        if session.player is not None and self.by_player.get(session.player.id) is session:
            del self.by_player[session.player.id]

    def drop(self, session, why):
        self.leave(session, why)
        self.forget(session)
        self.log("parti : %r (%s)" % (session, why))

    def takeover(self, old, new):
        """Le jeu d'un joueur a été relancé (nouvel identifiant, même compte) : il retrouve sa place."""
        room = old.room
        if room is not None:
            room.slots[old.slot] = new
            if room.host is old:
                room.host = new
            new.room, new.slot = room, old.slot
            old.room, old.slot = None, P.NO_SLOT
            self.log("salon %s : %r reprend la place %d" % (room.code, new, new.slot))
        race = old.race
        if race is not None:
            # la course classée suit le joueur dans son jeu relancé
            for slot, s in list(race.slots.items()):
                if s is old:
                    race.slots[slot] = new
            if old in race.reports:
                race.reports[new] = race.reports.pop(old)
            race.left.discard(old)
            if old in race.outcome:
                race.outcome[new] = race.outcome.pop(old)
            new.race = race
        self.forget(old)

    def expire(self, now):
        self.now = now
        for session in [s for s in self.sessions.values() if now - s.last_seen > self.timeout]:
            self.drop(session, "silence de %d s" % self.timeout)
        for session in [s for s in self.queue if now - s.last_queue > self.queue_stale]:
            self.unqueue(session, "ne la redit plus")
        self.group(now)
        for race in list(self.races):
            self.check_race(race, now)

    # --- salons ---

    def new_code(self):
        if self.fixed_code:
            return None if self.fixed_code in self.rooms else self.fixed_code
        for _ in range(200):
            code = "".join(self.rng.choice(CODE_LETTERS) for _ in range(CODE_LENGTH))
            if code not in self.rooms:
                return code
        return None

    def create(self, session):
        """Crée un salon dont le joueur est l'hôte. Renvoie le salon, ou None s'il n'y a plus de place."""
        room = session.room
        if room is not None and room.host is session:
            return room  # demande répétée (ou hôte qui recrée : il garde son salon)
        self.leave(session, "crée un autre salon")
        if len(self.rooms) >= self.max_rooms:
            return None
        code = self.new_code()
        if code is None:
            return None
        room = Room(code, session)
        self.rooms[code] = room
        session.room, session.slot = room, 0
        self.log("salon %s créé par %r" % (code, session))
        return room

    def join(self, session, code, now):
        """Fait entrer le joueur dans le salon. Renvoie un code P.JOIN_*.

        Salon plein : un joueur (pas l'hôte) muet depuis `stale` secondes cède sa place. Son jeu a sans doute
        planté, et un jeu sans compte ne peut pas reprendre sa place tout seul (voir takeover).
        """
        code = code.strip().upper()
        room = self.rooms.get(code)
        if room is not None and session.room is room:
            return P.JOIN_OK  # demande répétée
        if room is None or room.ranked:
            return P.JOIN_UNKNOWN  # un salon de partie rapide n'a pas de code à donner
        if room.flags & P.ROOM_LOCKED:
            return P.JOIN_LOCKED
        if room.host.mod_version != session.mod_version:
            return P.JOIN_VERSION
        slot = room.free_slot()
        if slot is None:
            silent = [s for s in room.members() if s is not room.host and now - s.last_seen >= self.stale]
            if not silent:
                return P.JOIN_FULL
            gone = min(silent, key=lambda s: s.last_seen)
            self.leave(gone, "muet depuis %d s, remplacé" % (now - gone.last_seen))
            slot = room.free_slot()
        self.leave(session, "rejoint un autre salon")
        room.slots[slot] = session
        session.room, session.slot = room, slot
        session.state = b""
        self.log("salon %s : %r entre en place %d (%d joueurs)" % (code, session, slot, len(room.members())))
        return P.JOIN_OK

    def leave(self, session, why="quitte"):
        """Sort le joueur de son salon. Si c'était l'hôte, la plus petite place restante le devient ; un salon
        vide ferme."""
        self.unqueue(session, why)
        room = session.room
        if room is None:
            return
        room.slots[session.slot] = None
        session.room, session.slot = None, P.NO_SLOT
        session.state = b""
        race = room.race
        if race is not None and race.state == P.RATED_PENDING and race.slot_of(session) is not None \
                and session not in race.reports:
            race.left.add(session)
            self.log("course classée %s : %r part sans résultat (%s)" % (race.code, session.player, why))
            self.check_race(race, self.now)
        rest = room.members()
        if not rest:
            del self.rooms[room.code]
            self.log("salon %s fermé (%r : %s), %d paquets de course relayés" % (room.code, session, why, room.relayed))
            return
        self.log("salon %s : %r sort (%s)" % (room.code, session, why))
        if room.host is session:
            room.host = rest[0]
            self.log("salon %s : %r devient l'hôte (place %d)" % (room.code, room.host, room.host.slot))

    def set_state(self, session, flags, payload):
        """PKT_STATE. Renvoie les joueurs à qui le recopier."""
        session.state = payload
        room = session.room
        if room is None:
            return []
        if room.host is session and room.flags != flags:
            room.flags = flags
            self.log("salon %s : %s" % (room.code, "fermé aux arrivants" if flags & P.ROOM_LOCKED else "ouvert"))
            if room.ranked and flags & P.ROOM_LOCKED and room.race is None:
                self.start_race(room)
        return [s for s in room.members() if s is not session]

    def targets(self, session, to_slot):
        """Destinataires d'un PKT_RELAY : la place visée, ou tout le salon sauf l'expéditeur."""
        room = session.room
        if room is None:
            return []
        if to_slot == P.ALL:
            return [s for s in room.members() if s is not session]
        if to_slot < P.MAX_PLAYERS:
            target = room.slots[to_slot]
            if target is not None and target is not session:
                return [target]
        return []

    # --- partie rapide : file d'attente ---

    def enqueue(self, session, now):
        """PKT_QUEUE : le joueur attend une partie rapide. Redit chaque seconde tant qu'il attend."""
        self.now = now
        if session.player is None:
            return
        room = session.room
        if room is not None:
            if room.ranked and room.race is None:
                return  # course déjà trouvée : le jeu ne le sait pas encore
            self.leave(session, "cherche une partie rapide")
        session.last_queue = now
        if session not in self.queue:
            session.queue_since = now
            self.queue.append(session)
            self.log("file d'attente : %r entre (%d en attente)" % (session.player, len(self.queue)))
        self.group(now)

    def unqueue(self, session, why="quitte"):
        if session in self.queue:
            self.queue.remove(session)
            session.queue_since = None
            self.log("file d'attente : %r sort (%s), %d en attente" % (session.player, why, len(self.queue)))

    def accepted_gap(self, session, now):
        """Écart d'Elo que ce joueur accepte : il grandit sans limite tant qu'il attend."""
        return self.queue_range + self.queue_grow * max(0.0, now - session.queue_since)

    def group(self, now):
        """Réunit des joueurs de la file. On part du plus ancien et on lui ajoute les plus proches de son Elo (même
        version du mod, autre compte, écart dans ce que l'un des deux accepte), 5 au plus. La course part si elle
        est pleine, ou si le plus ancien attend depuis `fill` secondes et qu'ils sont au moins 2."""
        waiting = [s for s in self.queue if now - s.last_queue <= self.queue_stale and s.room is None]
        used = set()
        for first in waiting:
            if first in used:
                continue
            others = []
            for s in waiting:
                if s is first or s in used or s.mod_version != first.mod_version:
                    continue
                if any(s.player is o.player for o in [first] + others):
                    continue
                gap = abs(first.player.elo - s.player.elo)
                if gap <= max(self.accepted_gap(first, now), self.accepted_gap(s, now)):
                    others.append(s)
            others.sort(key=lambda s: abs(first.player.elo - s.player.elo))
            group = [first] + others[:P.MAX_PLAYERS - 1]
            if len(group) < 2 or len(group) < P.MAX_PLAYERS and now - first.queue_since < self.fill:
                continue
            if not self.make_ranked_room(group, now):
                return
            used.update(group)

    def make_ranked_room(self, group, now):
        if len(self.rooms) >= self.max_rooms:
            return False
        code = None
        for _ in range(200):
            c = "".join(self.rng.choice(CODE_LETTERS) for _ in range(CODE_LENGTH))
            if c not in self.rooms:
                code = c
                break
        if code is None:
            return False
        waited = now - group[0].queue_since
        # l'hôte fait rouler les IA et relaie tout : tiré au sort
        host = self.rng.choice(group)
        members = [host] + [s for s in group if s is not host]
        room = Room(code, host, ranked=True)
        self.rooms[code] = room
        for slot, s in enumerate(members):
            self.unqueue(s, "course trouvée")
            room.slots[slot] = s
            s.room, s.slot = room, slot
            s.state = b""
        elos = [s.player.elo for s in members]
        self.log("partie rapide %s : %d joueurs (%s), hôte %r, Elo de %d à %d, après %d s d'attente" % (
            code, len(members), ", ".join(repr(s.player) for s in members), host.player, min(elos), max(elos), waited))
        return True

    # --- courses classées ---

    def start_race(self, room):
        race = Race(room, self.now)
        room.race = race
        for s in race.sessions():
            s.race = race
        self.races.append(race)
        self.log("course classée %s : départ, %d humains (%s)" % (
            race.code, len(race.slots), ", ".join("%d %r" % (slot, s.player) for slot, s in sorted(race.slots.items()))))
        if len(race.slots) < 2:
            self.settle(race, self.now)

    def report(self, session, flags, order, now):
        """PKT_RESULT. Renvoie la course classée du joueur (pour la réponse), ou None."""
        self.now = now
        race = session.race
        if race is None:
            return None
        if race.state == P.RATED_PENDING and session not in race.reports and race.slot_of(session) is not None:
            gone = bool(flags & P.RESULT_ABANDON)
            race.reports[session] = (gone, [slot for slot in order if slot in race.slots])
            race.left.discard(session)
            if not gone and race.first_finish is None:
                race.first_finish = now
            self.log("course classée %s : %r %s" % (race.code, session.player, "abandonne" if gone else
                     "annonce l'ordre " + " ".join(str(slot) for slot in race.reports[session][1])))
            self.check_race(race, now)
        return race

    def check_race(self, race, now):
        if race.state != P.RATED_PENDING:
            return
        waiting = [s for s in race.sessions() if s not in race.reports and s not in race.left]
        if waiting:
            late = race.first_finish is not None and now - race.first_finish > self.report_wait
            if not late and now - race.started < self.race_max:
                return
        self.settle(race, now)

    def settle(self, race, now):
        """Tranche la course : les joueurs sans résultat ou qui l'ont quittée sont classés derrière, à égalité ;
        l'ordre des autres doit être le même chez tous (sinon celui de la majorité, ou course annulée)."""
        if race in self.races:
            self.races.remove(race)
        finishers = [s for s in race.sessions() if s in race.reports and not race.reports[s][0]]
        gone = [s for s in race.sessions() if s not in finishers]
        fin_slots = set(race.slot_of(s) for s in finishers)
        orders = Counter(tuple(slot for slot in race.reports[s][1] if slot in fin_slots) for s in finishers)
        why = None
        order = None
        if len(race.slots) < 2:
            why = "moins de 2 humains"
        elif not finishers:
            why = "personne n'a fini"
        else:
            full = [(o, n) for o, n in orders.most_common() if set(o) == fin_slots]
            if not full:
                why = "classements incomplets"
            elif len(orders) > 1 and full[0][1] * 2 <= len(finishers):
                why = "classements différents : " + " / ".join(" ".join(map(str, o)) for o in orders)
            else:
                order = full[0][0]
        if why is not None:
            race.state = P.RATED_VOID
            self.log("course classée %s : pas comptée (%s)" % (race.code, why))
            return
        if len(orders) > 1:
            self.log("course classée %s : classements différents, celui de la majorité est gardé" % race.code)
        ranking = [(race.slots[slot], i, False) for i, slot in enumerate(order)]
        ranking += [(s, len(order), True) for s in gone]
        if self.store is not None:
            deltas = self.store.record_race([(s.player, rank, g) for s, rank, g in ranking])
        else:
            deltas = [0] * len(ranking)
        for (s, rank, g), delta in zip(ranking, deltas):
            race.outcome[s] = (rank + 1, delta)
        race.state = P.RATED_DONE
        self.log("course classée %s : %s" % (race.code, ", ".join(
            "%d. %r %+d%s" % (rank + 1, s.player, delta, " (parti)" if g else "")
            for (s, rank, g), delta in zip(ranking, deltas))))
