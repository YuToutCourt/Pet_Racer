"""Format des paquets entre le jeu et le serveur de Pet Racer (voir notes/online.md, phases 3 et 4).

Tout paquet commence par "PRS1" (4 octets) et un octet de type. Entiers en gros-boutiste, comme ModBuf côté jeu.
Une chaîne = un octet de longueur puis un octet par caractère (cp1252 / Latin-1).
À tenir à jour avec mods/java/ModLobby.java.

Les paquets de course du jeu (ModNet, magique "PRN1") ne passent jamais tels quels par le serveur : le jeu les
emballe dans un PKT_RELAY adressé à une place du salon, le serveur les rend emballés dans un PKT_FROM qui dit de
quelle place ils viennent. Le serveur ne lit ni l'intérieur des paquets de course, ni celui des PKT_STATE.
"""
import struct

MAGIC = 0x50525331  # "PRS1"
LOBBY_PROTO = 2  # version du dialogue jeu <-> serveur (ModLobby._LOBBY_PROTO) ; 2 : partie rapide, Elo, classement
MAX_PLAYERS = 6  # places d'un salon (ModNet._MAX_PLAYERS)
NO_SLOT = 255
ALL = 255  # PKT_RELAY vers tout le salon (sauf l'expéditeur)
MAX_INNER = 1480  # paquet de course emballé : le jeu lit au plus 1500 octets (ModUdp._MAX_PACKET)
MAX_STATE = 256
TOP_COUNT = 10  # lignes du classement envoyées dans PKT_TOP_LIST

# Jeu -> serveur. Tous commencent par : identifiant du jeu (int), numéro du paquet (int).
PKT_HELLO = 16  # + version du dialogue (short), version du mod (int, ModNet._VERSION), nom du joueur, jeton du compte
PKT_CREATE = 17  # crée un salon (répété jusqu'à ce que INFO le confirme)
PKT_JOIN = 18  # + code du salon (chaîne)
PKT_LEAVE = 19  # quitte le salon, ou la file d'attente de la partie rapide
PKT_STATE = 20  # + drapeaux du salon (octet, lus seulement chez l'hôte : ROOM_*) + ce que le joueur montre aux autres
#                 (animal, prêt ; chez l'hôte circuit, tours, numéro de course...), recopié sans être lu
PKT_QUEUE = 21  # partie rapide : le joueur attend (répété chaque seconde). Réponse INFO, avec un salon dès que la
#                 course est trouvée
PKT_RESULT = 22  # fin d'une course classée vue par ce jeu : drapeaux (RESULT_*), nombre, places des humains dans
#                  l'ordre d'arrivée (les partis à la fin). Répété jusqu'à un PKT_RATED définitif
PKT_TOP = 23  # demande du classement. Réponse PKT_TOP_LIST
PKT_RELAY = 24  # + place visée (octet, ALL = tout le salon) + paquet de course. Pas de réponse, pas de numéro utile.

# Serveur -> jeu
PKT_INFO = 32  # réponse à HELLO, CREATE, JOIN, LEAVE, STATE, QUEUE
PKT_PEER = 33  # + place (octet) + le dernier PKT_STATE de ce joueur
PKT_FROM = 35  # + place de l'expéditeur (octet) + paquet de course
PKT_RATED = 36  # réponse à PKT_RESULT : n° du paquet, état (RATED_*), place parmi les humains, nombre d'humains,
#                 variation d'Elo (short), Elo (short), rang (short)
PKT_TOP_LIST = 37  # réponse à PKT_TOP : nombre de classés (short), nombre de lignes, puis (nom, Elo, courses) par ligne

# PKT_INFO, état
STATUS_OK = 0
STATUS_BAD_PROTO = 1  # jeu et serveur ne parlent pas la même version
STATUS_UNKNOWN = 2  # le serveur ne connaît pas ce jeu : il doit renvoyer PKT_HELLO

# PKT_INFO, refus d'un PKT_JOIN
JOIN_OK = 0
JOIN_UNKNOWN = 1  # aucun salon avec ce code (ou salon d'une partie rapide)
JOIN_FULL = 2  # les 6 places sont prises
JOIN_VERSION = 3  # l'hôte du salon n'a pas la même version du mod
JOIN_LOCKED = 4  # l'hôte a fermé le salon (course lancée)

# Drapeaux du salon, redits dans PKT_INFO
ROOM_LOCKED = 1  # posé par l'hôte dans son PKT_STATE : plus personne n'entre (course en cours)
ROOM_RANKED = 2  # posé par le serveur : partie rapide, course classée

# PKT_RESULT, drapeaux
RESULT_ABANDON = 1  # ce joueur a quitté la course

# PKT_RATED, état de la dernière course classée du joueur
RATED_NONE = 0  # aucune course classée connue
RATED_PENDING = 1  # on attend les résultats des autres jeux
RATED_DONE = 2  # comptée : Elo mis à jour
RATED_VOID = 3  # pas comptée (classements différents, moins de 2 humains...)

_HEAD = struct.Struct(">IB")
_LOBBY_HEAD = struct.Struct(">IBii")
HEAD_SIZE = _HEAD.size
LOBBY_HEAD_SIZE = _LOBBY_HEAD.size
RELAY_HEAD_SIZE = LOBBY_HEAD_SIZE + 1


def short(n):
    return max(-32768, min(32767, int(n)))


def put_string(s):
    data = s.encode("latin-1", "replace")[:255]
    return bytes([len(data)]) + data


def get_string(data, off):
    """Renvoie (chaîne, position suivante). Lève ValueError si le paquet est tronqué."""
    if off >= len(data):
        raise ValueError("chaîne tronquée")
    end = off + 1 + data[off]
    if end > len(data):
        raise ValueError("chaîne tronquée")
    return data[off + 1:end].decode("latin-1"), end


def read_head(data):
    """Renvoie le type du paquet, ou None si ce n'est pas un paquet du jeu."""
    if len(data) < HEAD_SIZE:
        return None
    magic, ptype = _HEAD.unpack_from(data, 0)
    return ptype if magic == MAGIC else None


def read_lobby_head(data):
    """Renvoie (identifiant du jeu, numéro du paquet) d'un paquet jeu -> serveur."""
    if len(data) < LOBBY_HEAD_SIZE:
        raise ValueError("paquet tronqué")
    _, _, client_id, seq = _LOBBY_HEAD.unpack_from(data, 0)
    return client_id, seq


def read_hello(data):
    """Renvoie (version du dialogue, version du mod, nom, jeton). Un jeu d'une autre version n'est lu que jusqu'à
    sa version : la suite peut avoir un autre format."""
    proto, mod_version = struct.unpack_from(">hi", data, LOBBY_HEAD_SIZE)
    if proto != LOBBY_PROTO:
        return proto, mod_version, "", ""
    name, off = get_string(data, LOBBY_HEAD_SIZE + 6)
    token, _ = get_string(data, off)
    return proto, mod_version, name, token


def read_join(data):
    code, _ = get_string(data, LOBBY_HEAD_SIZE)
    return code


def read_state(data):
    """Renvoie (drapeaux du salon, contenu recopié aux autres)."""
    if len(data) < LOBBY_HEAD_SIZE + 1:
        raise ValueError("état tronqué")
    return data[LOBBY_HEAD_SIZE], data[LOBBY_HEAD_SIZE + 1:LOBBY_HEAD_SIZE + 1 + MAX_STATE]


def read_relay(data):
    """Renvoie (identifiant du jeu, place visée, paquet de course)."""
    if len(data) < RELAY_HEAD_SIZE:
        raise ValueError("relais tronqué")
    client_id, _ = read_lobby_head(data)
    return client_id, data[LOBBY_HEAD_SIZE], data[RELAY_HEAD_SIZE:]


def read_result(data):
    """Renvoie (drapeaux, places des humains dans l'ordre d'arrivée)."""
    if len(data) < LOBBY_HEAD_SIZE + 2:
        raise ValueError("résultat tronqué")
    flags, count = data[LOBBY_HEAD_SIZE], data[LOBBY_HEAD_SIZE + 1]
    order = list(data[LOBBY_HEAD_SIZE + 2:LOBBY_HEAD_SIZE + 2 + count])
    if len(order) != count or count > MAX_PLAYERS or any(s >= MAX_PLAYERS for s in order) or len(set(order)) != count:
        raise ValueError("résultat illisible")
    return flags, order


def make_info(seq, status, code="", my_slot=NO_SLOT, host_slot=NO_SLOT, room_flags=0, join_error=JOIN_OK, online=0,
              names=None, queue=0, elo=0, games=0, rank=0, ranked=0):
    """names : liste de MAX_PLAYERS noms, None pour une place vide. queue : joueurs qui attendent une partie rapide ;
    elo, games, rank : le compte du joueur (rank 0 = pas encore classé) ; ranked : nombre de joueurs classés."""
    out = _HEAD.pack(MAGIC, PKT_INFO) + struct.pack(">iB", seq, status) + put_string(code)
    out += struct.pack(">BBBBh", my_slot, host_slot, room_flags, join_error, short(online))
    for name in (names or [None] * MAX_PLAYERS):
        out += b"\x00" if name is None else b"\x01" + put_string(name)
    out += struct.pack(">hhhhh", short(queue), short(elo), short(games), short(rank), short(ranked))
    return out


def make_peer(slot, payload):
    return _HEAD.pack(MAGIC, PKT_PEER) + bytes([slot]) + payload


def make_from(slot, inner):
    return _HEAD.pack(MAGIC, PKT_FROM) + bytes([slot]) + inner


def make_rated(seq, state, place=0, humans=0, delta=0, elo=0, rank=0):
    return _HEAD.pack(MAGIC, PKT_RATED) + struct.pack(">iBBBhhh", seq, state, place, humans, short(delta), short(elo),
                                                      short(rank))


def make_top_list(total, rows):
    """rows : [(nom, Elo, courses)]"""
    out = _HEAD.pack(MAGIC, PKT_TOP_LIST) + struct.pack(">hB", short(total), len(rows))
    for name, elo, games in rows:
        out += put_string(name) + struct.pack(">hh", short(elo), short(games))
    return out


# --- côté jeu : sert aux tests (server/test_server.py) ---

def make_lobby(ptype, client_id, seq, body=b""):
    return _LOBBY_HEAD.pack(MAGIC, ptype, client_id, seq) + body


def make_hello(client_id, seq, name, mod_version, token="", proto=LOBBY_PROTO):
    return make_lobby(PKT_HELLO, client_id, seq, struct.pack(">hi", proto, mod_version) + put_string(name) + put_string(token))


def make_relay(client_id, to_slot, inner):
    return make_lobby(PKT_RELAY, client_id, 0, bytes([to_slot]) + inner)


def make_result(client_id, seq, order, flags=0):
    return make_lobby(PKT_RESULT, client_id, seq, bytes([flags, len(order)]) + bytes(order))


def read_info(data):
    seq, status = struct.unpack_from(">iB", data, HEAD_SIZE)
    if status != STATUS_OK:
        return dict(seq=seq, status=status)
    code, off = get_string(data, HEAD_SIZE + 5)
    my_slot, host_slot, room_flags, join_error, online = struct.unpack_from(">BBBBh", data, off)
    off += 6
    names = []
    for _ in range(MAX_PLAYERS):
        present = data[off]
        off += 1
        if present:
            name, off = get_string(data, off)
            names.append(name)
        else:
            names.append(None)
    queue, elo, games, rank, ranked = struct.unpack_from(">hhhhh", data, off)
    return dict(seq=seq, status=status, code=code, my_slot=my_slot, host_slot=host_slot,
                locked=bool(room_flags & ROOM_LOCKED), ranked_room=bool(room_flags & ROOM_RANKED),
                join_error=join_error, online=online, names=names, queue=queue, elo=elo, games=games, rank=rank,
                ranked=ranked)


def read_rated(data):
    seq, state, place, humans, delta, elo, rank = struct.unpack_from(">iBBBhhh", data, HEAD_SIZE)
    return dict(seq=seq, state=state, place=place, humans=humans, delta=delta, elo=elo, rank=rank)


def read_top_list(data):
    total, count = struct.unpack_from(">hB", data, HEAD_SIZE)
    off = HEAD_SIZE + 3
    rows = []
    for _ in range(count):
        name, off = get_string(data, off)
        elo, games = struct.unpack_from(">hh", data, off)
        off += 4
        rows.append((name, elo, games))
    return dict(total=total, rows=rows)


def read_slot_packet(data):
    """PKT_PEER ou PKT_FROM : renvoie (place, contenu)."""
    return data[HEAD_SIZE], data[HEAD_SIZE + 1:]
