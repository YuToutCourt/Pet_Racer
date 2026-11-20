# Barres de progression du HUD (KidsDistance, à gauche de l'écran), une par nombre de tours de 1 à 10.
# Le jeu d'origine n'a que Pasek_3.png et Pasek_7.png (Pasek_5.png est une copie de Pasek_7) et prend celle de
# 7 tours pour toute autre valeur : une course en 1, 2, 5 ou 10 tours affichait 7 morceaux.
#
# On repart de Pasek_3 : on efface ses deux séparations, on recolore chaque morceau en ne changeant que la teinte
# (les trois couleurs d'origine ont la même saturation et la même luminosité, le relief est gardé), puis on trace
# les nouvelles séparations comme les originales (une ligne sombre, une ligne de transition).
#
# Les séparations tombent sur les passages de ligne : l'icône d'un animal est centrée sur 0.9 de la hauteur au
# départ et sur 0.1 à l'arrivée (KidsDistance.SetDistance), le tour k se termine donc à 0.9 - 0.8 * k / n.
# Les noms sont nouveaux (ModPasek_N.png) : aucun cache .tex d'origine ne peut les masquer.
#
# Usage : python mods/art/make_lap_bars.py  ->  mods/data/Data/Textures/Hud/ModPasek_1.png ... ModPasek_10.png
import colorsys
import os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "..", "..", "game_pristine", "Data", "Textures", "Hud", "Pasek_3.png")
OUT = os.path.join(HERE, "..", "data", "Data", "Textures", "Hud")

MAX_LAPS = 10
# séparations de Pasek_3 (ligne sombre + transition) et la ligne propre qui les remplace
OLD_SEPARATORS = [((172, 173), 171), ((341, 342), 343)]
# teintes en degrés, du premier tour (en bas) au dernier (en haut) : rouge, bleu, cyan comme Pasek_3, puis des
# couleurs bien distinctes de leurs voisines
HUES = [356, 220, 176, 127, 298, 50, 248, 25, 190, 330]
DARK = (0.5, 0.42)        # séparation : facteurs de saturation et de luminosité (ligne 172 de Pasek_3)
TRANSITION = (0.9, 0.85)  # ligne suivante, à peine assombrie (ligne 173)


def hsv(p):
    r, g, b, a = p
    h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
    return h, s, v, a


def rgb(h, s, v, a):
    r, g, b = colorsys.hsv_to_rgb(h, min(1.0, s), min(1.0, v))
    return int(round(r * 255)), int(round(g * 255)), int(round(b * 255)), a


def clean_base():
    img = Image.open(SRC).convert("RGBA")
    w, _ = img.size
    for rows, good in OLD_SEPARATORS:
        for y in rows:
            for x in range(w):
                img.putpixel((x, y), img.getpixel((x, good)))
    return img


def make_bar(base, laps):
    w, h = base.size
    out = Image.new("RGBA", (w, h))
    # ligne où se termine chaque tour (du bas vers le haut)
    ends = [int(round((0.9 - 0.8 * k / laps) * h)) for k in range(1, laps)]
    for y in range(h):
        lap = sum(1 for e in ends if y < e)  # 0 = premier tour (en bas)
        hue = HUES[lap % len(HUES)] / 360.0
        if y in ends:
            fs, fv = DARK
        elif y - 1 in ends:
            fs, fv = TRANSITION
        else:
            fs, fv = 1.0, 1.0
        for x in range(w):
            p = base.getpixel((x, y))
            if p[3] == 0:
                out.putpixel((x, y), p)
                continue
            _, s, v, a = hsv(p)
            out.putpixel((x, y), rgb(hue, s * fs, v * fv, a))
    return out


def main():
    base = clean_base()
    os.makedirs(OUT, exist_ok=True)
    for laps in range(1, MAX_LAPS + 1):
        path = os.path.join(OUT, "ModPasek_%d.png" % laps)
        make_bar(base, laps).save(path)
        print(path)


if __name__ == "__main__":
    main()
