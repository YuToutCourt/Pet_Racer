"""Comptes des joueurs, Elo en ligne et historique des courses classées, dans un fichier SQLite (repris de pet_soccer).

Un compte = un jeton secret tiré par le jeu à son premier passage en ligne (online.dat). Pas de mot de passe : qui
présente le jeton est le joueur. Le serveur ne garde que l'empreinte du jeton.

Elo multijoueur (phase 4) : dans une course à N humains, chaque paire de joueurs compte comme un duel gagné par celui
arrivé devant (deux joueurs partis tous les deux : nul). Variation d'un joueur = K / (N - 1) x somme de ses écarts
au résultat attendu ; K = 48 pendant les 10 premières courses, puis 32 (formule de pet_soccer, ModElo). Les IA ne
comptent pas.
"""
import hashlib
import math
import sqlite3
import time

ELO_START = 1000
ELO_K = 32.0
PLACEMENT_RACES = 10  # premières courses : l'Elo bouge plus vite
PLACEMENT_K = 48.0
NAME_MAX = 16

_SCHEMA = """
CREATE TABLE IF NOT EXISTS players (
    id INTEGER PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    elo INTEGER NOT NULL,
    games INTEGER NOT NULL DEFAULT 0,
    peak INTEGER NOT NULL,
    created REAL NOT NULL,
    last_seen REAL NOT NULL
);
CREATE TABLE IF NOT EXISTS races (
    id INTEGER PRIMARY KEY,
    played REAL NOT NULL,
    humans INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS race_players (
    race INTEGER NOT NULL,
    player INTEGER NOT NULL,
    place INTEGER NOT NULL,
    abandon INTEGER NOT NULL,
    elo_before INTEGER NOT NULL,
    delta INTEGER NOT NULL
);
"""
_COLUMNS = "id, name, elo, games, peak, wins"


def expected(elo, other):
    return 1.0 / (1.0 + math.pow(10.0, (other - elo) / 400.0))


def race_deltas(entries):
    """entries : [(Elo, courses jouées, rang)] ; rang 0 = premier, deux rangs égaux = nul entre eux.
    Renvoie la variation d'Elo de chacun, dans le même ordre."""
    n = len(entries)
    out = []
    for i, (elo, games, rank) in enumerate(entries):
        total = 0.0
        for j, (other, _, other_rank) in enumerate(entries):
            if i == j:
                continue
            score = 1.0 if rank < other_rank else (0.5 if rank == other_rank else 0.0)
            total += score - expected(elo, other)
        k = PLACEMENT_K if games < PLACEMENT_RACES else ELO_K
        out.append(int(math.floor(k * total / (n - 1) + 0.5)) if n > 1 else 0)
    return out


def clean_name(name):
    name = "".join(c for c in name if ord(c) >= 32).strip()[:NAME_MAX]
    return name or "?"


def valid_token(token):
    return 16 <= len(token) <= 64 and token.isalnum() and token.isascii()


class Player:
    def __init__(self, row):
        self.id, self.name, self.elo, self.games, self.peak, self.wins = row

    def __repr__(self):
        return "%s#%d (%d)" % (self.name, self.id, self.elo)


class Store:
    def __init__(self, path):
        self.db = sqlite3.connect(path)
        self.db.executescript(_SCHEMA)
        columns = [row[1] for row in self.db.execute("PRAGMA table_info(players)")]
        if "wins" not in columns:  # base de la phase 3
            self.db.execute("ALTER TABLE players ADD COLUMN wins INTEGER NOT NULL DEFAULT 0")
        self.db.commit()
        self.players = {}  # numéro du compte -> Player : un seul objet par compte, partagé par ses sessions

    def close(self):
        self.db.close()

    def login(self, token, name):
        """Le compte de ce jeton, créé s'il n'existe pas. None si le jeton n'a pas la bonne forme."""
        if not valid_token(token):
            return None
        digest = hashlib.sha256(token.encode("ascii")).hexdigest()
        name = clean_name(name)
        now = time.time()
        row = self.db.execute("SELECT %s FROM players WHERE token = ?" % _COLUMNS, (digest,)).fetchone()
        if row is None:
            cur = self.db.execute(
                "INSERT INTO players (token, name, elo, peak, created, last_seen) VALUES (?, ?, ?, ?, ?, ?)",
                (digest, name, ELO_START, ELO_START, now, now))
            row = (cur.lastrowid, name, ELO_START, 0, ELO_START, 0)
        player = self.players.get(row[0])
        if player is None:
            player = self.players[row[0]] = Player(row)
        player.name = name
        self.db.execute("UPDATE players SET name = ?, last_seen = ? WHERE id = ?", (name, now, player.id))
        self.db.commit()
        return player

    def record_race(self, ranking):
        """Compte une course classée. ranking : [(Player, rang, parti)] (rang 0 = premier ; les partis ont tous le
        même rang, derrière). Renvoie la variation d'Elo de chacun, dans le même ordre."""
        deltas = race_deltas([(p.elo, p.games, rank) for p, rank, _ in ranking])
        cur = self.db.execute("INSERT INTO races (played, humans) VALUES (?, ?)", (time.time(), len(ranking)))
        race_id = cur.lastrowid
        for (player, rank, gone), delta in zip(ranking, deltas):
            self.db.execute("INSERT INTO race_players (race, player, place, abandon, elo_before, delta) VALUES (?, ?, ?, ?, ?, ?)",
                            (race_id, player.id, rank, int(gone), player.elo, delta))
            player.elo += delta
            player.games += 1
            player.wins += rank == 0 and not gone
            player.peak = max(player.peak, player.elo)
            self.db.execute("UPDATE players SET elo = ?, games = ?, peak = ?, wins = ? WHERE id = ?",
                            (player.elo, player.games, player.peak, player.wins, player.id))
        self.db.commit()
        return deltas

    # --- classement : les joueurs qui ont au moins une course classée ---

    def top(self, count):
        rows = self.db.execute(
            "SELECT %s FROM players WHERE games > 0 ORDER BY elo DESC, games DESC, id LIMIT ?" % _COLUMNS, (count,))
        return [Player(row) for row in rows]

    def total(self):
        return self.db.execute("SELECT COUNT(*) FROM players WHERE games > 0").fetchone()[0]

    def rank(self, player):
        """Rang du joueur à partir de 1, ou 0 s'il n'a pas encore de course classée."""
        if player.games == 0:
            return 0
        return 1 + self.db.execute(
            "SELECT COUNT(*) FROM players WHERE games > 0 AND (elo > ? OR (elo = ? AND (games > ? OR (games = ? AND id < ?))))",
            (player.elo, player.elo, player.games, player.games, player.id)).fetchone()[0]
