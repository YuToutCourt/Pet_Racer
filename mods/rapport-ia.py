"""Rapport HTML de l'entraînement des IA (notes/ia.md, « Entraînement automatique »).

Lit game_dev\\entrainement_historique.txt (écrit course après course par ModTraining) et fabrique une page autonome,
sans Internet : évolution des chocs et des temps, une fiche par circuit (carte du terrain avec la piste des IA,
points dangereux et chocs, temps du vainqueur course après course, record du joueur), détail de chaque course.

    python mods\\rapport-ia.py                 # -> build\\rapport_ia.html
    python mods\\rapport-ia.py --ouvrir        # et l'ouvre dans le navigateur
    python mods\\rapport-ia.py --direct 60     # page qui se recharge toute seule (entrainer-ia.ps1 pendant les courses)
    python mods\\rapport-ia.py --jeu game_dev_b --sortie build\\autre.html
"""
import argparse
import base64
import io
import json
import math
import os
import struct
import sys
import webbrowser
from datetime import datetime

try:
    from PIL import Image
except ImportError:  # sans Pillow : rapport sans fond de carte
    Image = None

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# RacerModule.MAPNAMES : dossier et variante de chaque numéro de circuit
WORLDS = [("Las", "Forêt"), ("Pustynia", "Désert"), ("Zima", "Hiver"), ("Tropik", "Tropiques"), ("Ksiezyc", "Lune")]


# circuits courus en sens inverse (CoreLogic.PrepareTrackBeforeRace : SetReverseTrack(true)). Pas la clé : avant
# le 2026-11-20 le « |inverse » de la clé était celui de la course précédente.
REVERSE = {3, 4, 5, 9, 10, 11, 12, 16, 17, 21, 22, 23, 27, 28, 29}


def track_info(i):
    """numéro (0..30) -> (dossier, nom affiché, nom du monde)"""
    if i == 30:
        return "Krolewska", "Royal", "Royal"
    w, name = WORLDS[i // 6]
    n = i % 3 + 1
    variant = (i % 6) >= 3
    return f"{w}{n}", f"{name} {n}" + (" · variante" if variant else ""), name


def num(s, default=None):
    try:
        return float(s)
    except (TypeError, ValueError):
        return default


def parse_history(path):
    sessions, records, geoms, races = [], {}, {}, []
    with open(path, "r", encoding="cp1252", errors="replace") as f:
        for line in f:
            parts = line.rstrip("\r\n").split("\t")
            if len(parts) < 2:
                continue
            kind = parts[0]
            kv = {}
            for p in parts[1:]:
                k, _, v = p.partition("=")
                kv[k] = v
            if kind == "S":
                sessions.append(kv)
            elif kind == "P":
                t = num(kv.get("time"))
                if t:
                    records[int(kv["track"])] = {"time": t, "name": kv.get("name", "")}
            elif kind == "G":
                gates = []
                for g in kv.get("gates", "").split(";"):
                    c = g.split(",")
                    if len(c) == 3:
                        gates.append((float(c[0]), float(c[1]), float(c[2])))
                size = [float(x) for x in kv.get("size", "0,0,0").split(",")]
                g = {"track": int(kv["track"]), "size": size, "gates": gates}
                # les vieux historiques ont des portes à plat (y = 0) : on garde la géométrie qui a des hauteurs
                has_y = any(y for _, y, _ in gates)
                old = geoms.get(kv["key"])
                if old is None or has_y or not any(y for _, y, _ in old["gates"]):
                    geoms[kv["key"]] = g
            elif kind == "R":
                kv["cars_list"] = []
                races.append(kv)
            elif kind == "A" and races and races[-1].get("race") == kv.get("race"):
                races[-1]["cars_list"].append(kv)
            elif kind == "C" and races:  # IA chrono (ModChrono) : plan de base après cette course
                races[-1]["chrono_line"] = kv
    return sessions, records, geoms, races


# ---------------------------------------------------------------------------------------------- carte du terrain
# Le terrain (Data\Levels\<monde>\<monde>_Colour.png) n'a pas de repère connu : on cherche l'orientation et l'origine
# qui collent le mieux à la hauteur des portes de la piste (même circuit dans <monde>_Height.png). Si rien ne colle
# (corrélation trop faible), la carte est dessinée sans fond.

def corr(a, b):
    n = len(a)
    if n < 5:
        return -1.0
    ma, mb = sum(a) / n, sum(b) / n
    va = sum((x - ma) ** 2 for x in a)
    vb = sum((y - mb) ** 2 for y in b)
    if va <= 0 or vb <= 0:
        return -1.0
    return sum((x - ma) * (y - mb) for x, y in zip(a, b)) / math.sqrt(va * vb)


TRANSFORMS = [  # (u, v) dans [0,1]² -> (colonne, ligne) dans [0,1]²
    lambda u, v: (u, v), lambda u, v: (1 - u, v), lambda u, v: (u, 1 - v), lambda u, v: (1 - u, 1 - v),
    lambda u, v: (v, u), lambda u, v: (1 - v, u), lambda u, v: (v, 1 - u), lambda u, v: (1 - v, 1 - u),
]


def calibrate(geom, height):
    sx, _, sz = geom["size"]
    if sx <= 0 or sz <= 0 or height is None:
        return None
    w, h = height.size
    px = height.load()
    ys = [g[1] for g in geom["gates"]]
    best = None
    for ox in (0.0, -sx / 2):
        for oz in (0.0, -sz / 2):
            for ti, t in enumerate(TRANSFORMS):
                hs, inside = [], 0
                for x, _, z in geom["gates"]:
                    c, r = t((x - ox) / sx, (z - oz) / sz)
                    if 0 <= c <= 1 and 0 <= r <= 1:
                        inside += 1
                    c = min(max(c, 0.0), 1.0)
                    r = min(max(r, 0.0), 1.0)
                    hs.append(px[int(round(c * (w - 1))), int(round(r * (h - 1)))])
                if inside < 0.95 * len(geom["gates"]):
                    continue
                k = corr(ys, hs)
                if best is None or k > best[0]:
                    best = (k, ox, oz, ti)
    if best is None or best[0] < 0.6:
        return None
    return {"corr": best[0], "ox": best[1], "oz": best[2], "t": best[3]}


def build_map(geom, game_dir, races_of_key, line=None, bornes=None):
    """coordonnées de la piste et des chocs dans le repère de l'image, fond recadré en data URI"""
    folder, _, _ = track_info(geom["track"])
    base = os.path.join(game_dir, "Data", "Levels", folder)
    colour = height = None
    if Image is not None:
        try:
            height = Image.open(os.path.join(base, f"{folder}_Height.png")).convert("L")
            colour = Image.open(os.path.join(base, f"{folder}_Colour.png")).convert("RGB")
        except OSError:
            colour = height = None
    cal = calibrate(geom, height)
    sx, _, sz = geom["size"]

    if cal is not None:
        W, H = colour.size
        t = TRANSFORMS[cal["t"]]

        def to_px(x, z):
            c, r = t((x - cal["ox"]) / sx, (z - cal["oz"]) / sz)
            return c * W, r * H
    else:
        def to_px(x, z):  # sans fond : vue de dessus, z vers le haut
            return x, -z

    gates = [to_px(x, z) for x, _, z in geom["gates"]]
    xs = [p[0] for p in gates]
    ys = [p[1] for p in gates]
    span = max(max(xs) - min(xs), max(ys) - min(ys), 1.0)
    m = span * 0.08
    x0, y0, x1, y1 = min(xs) - m, min(ys) - m, max(xs) + m, max(ys) + m
    # carré, centré sur la piste
    cx, cy, half = (x0 + x1) / 2, (y0 + y1) / 2, max(x1 - x0, y1 - y0) / 2
    x0, y0, x1, y1 = cx - half, cy - half, cx + half, cy + half
    img = None
    if cal is not None:
        W, H = colour.size
        cx0, cy0 = max(0, int(x0)), max(0, int(y0))
        cx1, cy1 = min(W, int(math.ceil(x1))), min(H, int(math.ceil(y1)))
        crop = colour.crop((cx0, cy0, cx1, cy1))
        scale = 520.0 / max(crop.size)
        if scale < 1:
            crop = crop.resize((max(1, int(crop.size[0] * scale)), max(1, int(crop.size[1] * scale))), Image.LANCZOS)
        buf = io.BytesIO()
        crop.save(buf, "JPEG", quality=78)
        img = {"src": "data:image/jpeg;base64," + base64.b64encode(buf.getvalue()).decode("ascii"),
               "x": cx0, "y": cy0, "w": cx1 - cx0, "h": cy1 - cy0}

    hits = []
    for r in races_of_key:
        pts = []
        for h in r.get("hits", "").split(";"):
            c = h.split(",")
            if len(c) == 2:
                px_, py_ = to_px(float(c[0]), float(c[1]))
                pts.append([round(px_, 1), round(py_, 1)])
        hits.append(pts)
    pts_line = None
    if line:  # IA chrono : trajectoire du plan de base (x, z par porte)
        pts_line = [[round(a, 1), round(b, 1)] for a, b in (to_px(x, z) for x, z in line)]
    pts_bornes = None
    if bornes:  # IA chrono : endroits où la ligne droite du plan menait dans le décor (porte, x, z, chocs)
        pts_bornes = []
        for g, x, z, w in bornes:
            a, b = to_px(x, z)
            pts_bornes.append([round(a, 1), round(b, 1), g, w])
    return {
        "line": pts_line,
        "bornes": pts_bornes,
        "vb": [round(x0, 1), round(y0, 1), round(x1 - x0, 1), round(y1 - y0, 1)],
        "gates": [[round(p[0], 1), round(p[1], 1)] for p in gates],
        "img": img,
        "hits": hits,
        "corr": round(cal["corr"], 2) if cal else None,
    }


# ---------------------------------------------------------------------------------------------- données de la page

def read_records(folder):
    """records du menu Records d'un dossier de jeu (progress.cfg, CoreLogic.Load : marqueur 1100 + 100000, puis 31 fois
    nom UTF + temps float). Le temps du jeu est la course entière divisée par le nombre de tours (Vehicle :
    AddNewTime(… CalculateRaceTime() / tours)), départ compris : pas un meilleur tour."""
    try:
        d = open(os.path.join(folder, "progress.cfg"), "rb").read()
    except OSError:
        return {}
    p = d.find(struct.pack(">i", 101100))
    if p < 0:
        return {}
    p += 4
    out = {}
    try:
        for i in range(31):
            n = struct.unpack(">H", d[p:p + 2])[0]
            name = d[p + 2:p + 2 + n].decode("utf-8", "replace")
            p += 2 + n
            t = struct.unpack(">f", d[p:p + 4])[0]
            p += 4
            if 0 < t < 100000:
                out[i] = {"time": round(t, 2), "name": name}
    except (struct.error, IndexError):
        return {}
    return out


def player_records(game_dir, records):
    """les records de l'historique (au lancement de l'entraînement), mis à jour en direct par les sauvegardes de
    game_dev et de game_joueur (mods\\jouer.ps1 : on joue là pendant que les IA s'entraînent dans game_dev)"""
    out = dict(records)
    root = os.path.dirname(os.path.abspath(game_dir))
    for folder in (game_dir, os.path.join(root, "game_joueur")):
        for i, r in read_records(folder).items():
            if i not in out or r["time"] < out[i]["time"]:
                out[i] = r
    return out


def build_data(game_dir, path=None):
    path = path or os.path.join(game_dir, "entrainement_historique.txt")
    if not os.path.exists(path):
        return {"races": [], "tracks": [], "sessions": [], "empty": True}
    sessions, records, geoms, races = parse_history(path)
    records = player_records(game_dir, records)
    chrono = build_chrono(races, geoms, game_dir, records)
    # les courses de l'IA chrono (6 fois le même animal, sans hasard) ne comptent pas dans les courbes des IA du jeu
    races = [r for r in races if r.get("chrono") != "1"]

    out_races = []
    for idx, r in enumerate(races):
        cars = []
        for a in r["cars_list"]:
            cars.append({
                "animal": a.get("animal", "?"), "rank": int(num(a.get("rank"), 0)), "finished": a.get("finished") == "1",
                "time": num(a.get("time")), "best": num(a.get("best")),
                "laps": [num(x) for x in a.get("laps", "").split(",") if x],
                "off": num(a.get("off"), 0), "stuck": int(num(a.get("stuck"), 0)), "respawn": int(num(a.get("respawn"), 0)),
                "hits": int(num(a.get("hits"), 0)), "bonus": int(num(a.get("bonus"), 0)),
            })
        # tours impossibles (moins de la moitié du tour médian de la course) : recul par-dessus la ligne compté comme
        # un tour par les anciens historiques (0:11.78 sur Forêt 1, 2026-11-20) ; on les écarte du meilleur tour
        all_laps = sorted(x for c in cars for x in c["laps"] if x)
        if all_laps:
            med = all_laps[len(all_laps) // 2]
            for c in cars:
                c["laps"] = [x for x in c["laps"] if x and x >= 0.5 * med]
                c["best"] = min(c["laps"]) if c["laps"] else None
        cars.sort(key=lambda c: c["rank"])
        laps = int(num(r.get("laps"), 3))
        n = max(1, len(cars))
        win = next((c for c in cars if c["finished"] and c["rank"] == 1), None)
        bests = [c["best"] for c in cars if c["best"]]
        hits_total = sum(c["hits"] for c in cars)
        out_races.append({
            "i": idx + 1, "date": r.get("date", ""), "race": int(num(r.get("race"), 0)), "total": int(num(r.get("total"), 0)),
            "pass": int(num(r.get("pass"), 1)), "track": int(num(r.get("track"), 0)), "key": r.get("key", ""),
            "mode": r.get("mode", "?"), "laps": laps, "finished": int(num(r.get("finished"), 0)), "cars": len(cars),
            "game": num(r.get("game"), 0), "real": num(r.get("real"), 0),
            "walls": int(num(r.get("walls"), 0)), "danger": int(num(r.get("danger"), 0)), "lessons": int(num(r.get("lessons"), 0)),
            "dangers": [int(x) for x in r.get("dangers", "").split(",") if x.strip().lstrip("-").isdigit()],
            "hits": hits_total, "hitsPerLap": hits_total / (n * laps),
            "stuck": sum(c["stuck"] for c in cars),
            "winTime": win["time"] if win else None, "winner": win["animal"] if win else None,
            "bestLap": min(bests) if bests else None,
            "carsList": cars,
        })

    by_key = {}
    for r in out_races:
        by_key.setdefault(r["key"], []).append(r)

    tracks = []
    for key, runs in by_key.items():
        tnum = runs[0]["track"]
        folder, name, world = track_info(tnum)
        geom = geoms.get(key)
        raw_runs = [races[r["i"] - 1] for r in runs]
        tracks.append({
            "key": key, "track": tnum, "name": name, "world": world, "reverse": tnum in REVERSE,
            "runs": [r["i"] for r in runs],
            "record": records.get(tnum),
            "map": build_map(geom, game_dir, raw_runs) if geom and geom["gates"] else None,
        })
    tracks.sort(key=lambda t: (t["track"], t["key"]))
    done = {t["track"] for t in tracks}
    missing = [{"track": i, "name": track_info(i)[1]} for i in range(31) if i not in done]

    last = sessions[-1] if sessions else {}
    chrono_done = {t["track"] for t in chrono}
    return {
        "chrono": chrono,
        "chronoMissing": [track_info(i)[1] for i in range(31) if i not in chrono_done] if chrono else [],
        "races": out_races, "tracks": tracks, "missing": missing, "sessions": sessions,
        "planned": int(num(last.get("passes"), 0) * num(last.get("tracks"), 0)) if last else 0,
        "generated": datetime.now().strftime("%d/%m/%Y %H:%M"),
        "empty": not out_races and not chrono,
    }


def chrono_runs(r, cars):
    """temps officiels du jeu (lignes A) de chaque voiture d'une course chrono : tours 1, 2, 3 et course entière.
    Voiture k = ordre des lignes A (champ car= depuis le 2026-11-21) ; son plan (base, vérif, essai) vient de la ligne C."""
    runs = []
    for idx, a in enumerate(r["cars_list"]):
        k = int(num(a.get("car"), idx))
        laps = [num(x) for x in a.get("laps", "").split(",") if x]
        runs.append({
            "car": k, "role": a.get("role") or (cars[k]["role"] if k < len(cars) else "?"),
            "laps": laps, "time": num(a.get("time")) if a.get("finished") == "1" else None,
            "hits": int(num(a.get("hits"), 0)), "respawn": int(num(a.get("respawn"), 0)), "stuck": int(num(a.get("stuck"), 0)),
        })
    # tours impossibles (recul par-dessus la ligne compté comme un tour) : écartés, comme pour les IA du jeu
    all_laps = sorted(x for c in runs for x in c["laps"] if x)
    if all_laps:
        med = all_laps[len(all_laps) // 2]
        for c in runs:
            if any(x and x < 0.5 * med for x in c["laps"]):
                c["laps"], c["time"] = [], None
    runs.sort(key=lambda c: c["car"])
    return runs


def build_chrono(races, geoms, game_dir, records):
    """IA chrono : une fiche par circuit, sens et animal (clé de aichrono.dat), course chrono après course chrono"""
    by_key, raw_by_key = {}, {}
    for r in races:
        c = r.get("chrono_line")
        if r.get("chrono") != "1" or not c:
            continue
        cars = []
        for e in c.get("cars", "").split(";"):
            f = e.split(":")
            if len(f) >= 4:
                cars.append({"role": f[0], "score": num(f[1]), "laps": int(num(f[2], 0)), "hits": int(num(f[3], 0)),
                             "bad": int(num(f[4], 0)) if len(f) > 4 else 0})
        key = c.get("key", "?")
        raw_by_key.setdefault(key, []).append(r)
        by_key.setdefault(key, []).append({
            "n": int(num(c.get("chrono_race"), 0)), "date": r.get("date", ""), "calib": c.get("calib") == "1",
            "score": int(num(c.get("score"), 1)),
            "base": num(c.get("base")), "baseNow": num(c.get("base_now")), "record": num(c.get("record")) or None,
            "noise": num(c.get("noise")), "gen": int(num(c.get("gen"), 0)), "tried": int(num(c.get("tried"), 0)),
            "decision": c.get("decision", ""), "cars": cars, "runs": chrono_runs(r, cars), "track": int(num(r.get("track"), 0)),
            "mapKey": r.get("key", ""), "real": num(r.get("real"), 0), "animal": c.get("animal", "?"),
            "race": int(num(r.get("race"), 0)), "total": int(num(r.get("total"), 0)), "laps": int(num(r.get("laps"), 3)),
            "_line": c.get("line", ""), "_plan": c.get("plan", ""), "_bornes": c.get("bornes", ""),
        })
    out = []
    for key, pts in by_key.items():
        last = pts[-1]
        line = []
        for g in last["_line"].split(";"):
            f = g.split(",")
            if len(f) == 2:
                line.append((float(f[0]), float(f[1])))
        plan = []
        for g in last["_plan"].split(","):
            f = g.split(":")
            if len(f) == 2:
                plan.append([int(num(f[0], 100)), int(num(f[1], 100))])
        bornes = []
        for g in last["_bornes"].split(";"):
            f = g.split(",")
            if len(f) == 4:
                bornes.append((int(num(f[0], 0)), float(f[1]), float(f[2]), int(num(f[3], 0))))
        geom = geoms.get(last["mapKey"])
        # chocs des 10 dernières courses (les 6 voitures) : où le plan tape encore
        mp = build_map(geom, game_dir, raw_by_key[key][-10:], line, bornes) if geom and geom["gates"] else None
        for p in pts:
            p.pop("_line", None)
            p.pop("_plan", None)
            p.pop("_bornes", None)
        _, name, world = track_info(last["track"])
        rec = records.get(last["track"])
        out.append({"key": key, "name": name, "world": world, "track": last["track"], "animal": last["animal"],
                    "reverse": last["track"] in REVERSE, "points": pts, "plan": plan, "map": mp,
                    "playerRecord": rec})
    out.sort(key=lambda t: (t["track"], t["key"]))
    return out


def main():
    ap = argparse.ArgumentParser(description="Rapport HTML de l'entraînement des IA")
    ap.add_argument("--jeu", default=os.path.join(ROOT, "game_dev"))
    ap.add_argument("--sortie", default=os.path.join(ROOT, "build", "rapport_ia.html"))
    ap.add_argument("--historique", default=None, help="autre fichier d'historique (essais)")
    ap.add_argument("--ouvrir", action="store_true")
    ap.add_argument("--direct", type=int, default=0, help="rechargement automatique toutes les N secondes")
    a = ap.parse_args()

    data = build_data(a.jeu, a.historique)
    template = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "rapport-ia.html"), encoding="utf-8").read()
    payload = json.dumps(data, ensure_ascii=False, separators=(",", ":")).replace("</", "<\\/")
    refresh = f'<meta http-equiv="refresh" content="{a.direct}">' if a.direct > 0 else ""
    page = template.replace("<!--REFRESH-->", refresh).replace("/*DATA*/null", payload)
    os.makedirs(os.path.dirname(os.path.abspath(a.sortie)), exist_ok=True)
    tmp = a.sortie + ".tmp"
    with open(tmp, "w", encoding="utf-8") as f:
        f.write(page)
    os.replace(tmp, a.sortie)  # le navigateur ne lit jamais une page à moitié écrite
    n = len(data["races"]) + sum(len(t["points"]) for t in data["chrono"])
    maps = sum(1 for t in data["tracks"] if t["map"] and t["map"]["img"])
    print(f"[rapport] {a.sortie} : {n} courses, {len(data['tracks'])} circuits ({maps} avec fond de carte)")
    if a.ouvrir:
        webbrowser.open("file:///" + os.path.abspath(a.sortie).replace("\\", "/"))


if __name__ == "__main__":
    sys.exit(main())
