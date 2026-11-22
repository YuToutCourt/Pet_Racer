"""Plans chrono pour les IA du jeu (notes/ia.md, « Plans chrono pour les IA du jeu ») : choisit, circuit par circuit,
les plans de l'IA chrono (game_dev/aichrono.dat) qui battent vraiment l'IA experte, et les écrit dans
mods/data/Data/aiplans.dat (lu par ModPlans, donné avec le mod ; build.ps1 le copie dans game_dev).

Jugement, sur les temps réels des tours lancés (tours 2 et suivants) de l'historique de l'entraînement
(game_dev/entrainement_historique*.txt) :
  1. s'il y a des courses mixtes (mods/entrainer-ia.ps1 -Plans : une IA experte sur deux suit le plan, colonne plan=1
     des lignes A), le plan est gardé s'il y est plus rapide que les IA expertes sans plan des mêmes courses ;
  2. sinon : tours de la voiture « base » des dernières courses chrono contre ceux de l'IA experte normale, gain
     minimum --gain % (les voitures chrono roulent seules, traversables : un peu avantagées, d'où la marge).
Dans les deux cas, pas plus de --incidents remises en piste ou blocages par course.

    python mods/plans-ia.py                 # écrit mods/data/Data/aiplans.dat et affiche le tableau
    python mods/plans-ia.py --essai         # seulement le tableau
"""
import argparse
import glob
import os
import statistics
import sys
import time

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def fields(line):
    f = line.rstrip('\r\n').split('\t')
    return f[0], dict(x.split('=', 1) for x in f[1:] if '=' in x)


def flying(d):
    """médiane des tours lancés d'une ligne A (None sans tour lancé)"""
    laps = [float(x) for x in d.get('laps', '').split(',') if x.strip()]
    return statistics.median(laps[1:]) if len(laps) > 1 else None


def incidents(d):
    return int(d.get('respawn', 0) or 0) + int(d.get('stuck', 0) or 0)


def read_history(game):
    expert = {}    # circuit -> [tour lancé] des IA expertes sans plan, courses normales
    chrono = {}    # (circuit, animal) -> [(tour lancé, incidents)] de la base, courses chrono, dans l'ordre
    mixed = {}     # circuit -> {'plan': [(tour, incidents)], 'ref': [...]} courses normales avec -Plans
    files = sorted(glob.glob(os.path.join(game, 'entrainement_historique*.txt')), key=os.path.getmtime)
    for fn in files:
        race = None
        with open(fn, encoding='utf-8', errors='replace') as f:
            for line in f:
                kind, d = fields(line)
                if kind == 'R':
                    key = d.get('key', '')
                    race = (key, d.get('chrono') == '1', d.get('mode')) if ('|aller|' in key or '|retour|' in key) else None
                    continue
                if kind != 'A' or race is None:
                    continue
                key, is_chrono, mode = race
                t = flying(d)
                if t is None:
                    continue
                if is_chrono:
                    if d.get('role') == 'base':
                        chrono.setdefault((key, d.get('animal', '')), []).append((t, incidents(d)))
                    continue
                if d.get('level') != '5' and mode != 'expert':
                    continue
                if d.get('plan') == '1':
                    mixed.setdefault(key, {'plan': [], 'ref': []})['plan'].append((t, incidents(d)))
                    continue
                expert.setdefault(key, []).append(t)
                if 'plan' in d:  # course faite avec -Plans : la référence de la même course
                    mixed.setdefault(key, {'plan': [], 'ref': []})['ref'].append((t, incidents(d)))
    return expert, chrono, mixed


def read_plans(game):
    plans = {}
    fn = os.path.join(game, 'aichrono.dat')
    if not os.path.exists(fn):
        sys.exit(f'{fn} introuvable : lance d\'abord l\'IA chrono (mods\\entrainer-ia.ps1 -Chrono).')
    with open(fn, encoding='cp1252', errors='replace') as f:
        for line in f:
            if not line.strip():
                continue
            k, d = fields(line)
            key, animal = k.rsplit('|', 1)
            if 'off' in d and 'plan' in d:
                plans.setdefault(key, []).append((animal, d))
    return plans


def judge(key, animal, expert, chrono, mixed, a):
    """(gardé, gain %, source, détail)"""
    m = mixed.get(key)
    if m and len(m['plan']) >= a.mixtes and len(m['ref']) >= a.mixtes:
        p = statistics.median(t for t, _ in m['plan'])
        r = statistics.median(t for t, _ in m['ref'])
        inc = sum(i for _, i in m['plan']) / len(m['plan'])
        gain = (r - p) / r * 100
        ok = gain >= a.gain_mixtes and inc <= a.incidents
        return ok, gain, 'mixtes', f'{len(m["plan"])} IA avec plan {p:.2f} s / {len(m["ref"])} sans {r:.2f} s, {inc:.1f} incident(s)/course'
    runs = chrono.get((key, animal), [])[-a.dernieres:]
    ref = expert.get(key, [])
    if len(runs) < 4 or len(ref) < 6:
        return False, 0.0, 'peu', f'{len(runs)} mesure(s) chrono, {len(ref)} IA expertes : pas assez pour juger'
    p = statistics.median(t for t, _ in runs)
    r = statistics.median(ref)
    inc = sum(i for _, i in runs) / len(runs)
    gain = (r - p) / r * 100
    ok = gain >= a.gain and inc <= a.incidents
    return ok, gain, 'chrono', f'base chrono {p:.2f} s ({len(runs)} courses) / IA experte {r:.2f} s ({len(ref)}), {inc:.1f} incident(s)/course'


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--jeu', default=os.path.join(ROOT, 'game_dev'), help='dossier du jeu entraîné (game_dev)')
    ap.add_argument('--sortie', default=os.path.join(ROOT, 'mods', 'data', 'Data', 'aiplans.dat'))
    ap.add_argument('--gain', type=float, default=2.0, help='gain minimum (%%) contre l\'IA experte, mesures chrono')
    ap.add_argument('--gain-mixtes', type=float, default=0.5, help='gain minimum (%%) dans les courses mixtes')
    ap.add_argument('--incidents', type=float, default=1.0, help='remises en piste + blocages par course, au plus')
    ap.add_argument('--dernieres', type=int, default=10, help='mesures chrono de la base prises (les plus récentes)')
    ap.add_argument('--mixtes', type=int, default=6, help='IA de chaque côté pour juger sur les courses mixtes')
    ap.add_argument('--essai', action='store_true', help='affiche le tableau sans écrire le fichier')
    a = ap.parse_args()

    expert, chrono, mixed = read_history(a.jeu)
    plans = read_plans(a.jeu)
    kept = []
    print(f'{"circuit":34} {"gain":>7}  {"source":7} decision')
    for key in sorted(plans):
        best = None
        for animal, d in plans[key]:
            ok, gain, src, why = judge(key, animal, expert, chrono, mixed, a)
            if best is None or (ok, gain) > (best[0], best[1]):
                best = (ok, gain, src, why, animal, d)
        ok, gain, src, why, animal, d = best
        name = key.replace('Data/Levels/', '')
        print(f'{name:34} {gain:+6.1f}%  {src:7} {"GARDE " if ok else "non   "} {why}')
        if ok:
            kept.append((key, animal, gain, src, d))

    print(f'\n{len(kept)} plan(s) gardé(s) sur {len(plans)} circuit(s)')
    if a.essai:
        return
    lines = [f'# Plans chrono pour les IA expertes (mods/plans-ia.py, {time.strftime("%Y-%m-%d %H:%M")}) : '
             'un circuit par ligne, lu par ModPlans. Refait par mods\\package.ps1 -AiMap.']
    for key, animal, gain, src, d in kept:
        lines.append(f'{key}\tanimal={animal}\tgain={gain:.1f}\tsource={src}\tgen={d.get("gen", "0")}\toff={d["off"]}\tplan={d["plan"]}')
    os.makedirs(os.path.dirname(a.sortie), exist_ok=True)
    with open(a.sortie, 'w', encoding='ascii', newline='\r\n') as f:
        f.write('\n'.join(lines) + '\n')
    print(f'écrit : {os.path.relpath(a.sortie, ROOT)}')


if __name__ == '__main__':
    main()
