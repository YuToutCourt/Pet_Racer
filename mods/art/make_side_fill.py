# Bandes de côté des écrans à cadre 4:3 ouvert sur la 3D (choix de l'animal, podium, provocation du boss).
# En 16:9, le cadre MenuCharakter.png est centré et laissait voir le décor 3D sur les côtés : ces bandes prolongent
# son damier jusqu'au bord de l'écran, en l'assombrissant vers l'extérieur (voir notes/resolution.md, ModSideFill).
#
# Aux bords du cadre, le damier est régulier : colonnes de 64 px, une limite de colonne pile au bord de l'image,
# lignes d'environ 68,5 px. On reprend les limites de lignes lues sur la colonne du bord, et on ajoute des colonnes
# de 64 px en alternant les couleurs.
#
# Usage : python mods/art/make_side_fill.py
#   -> mods/data/Data/Textures/Menu/b/MenuCharakterLeft.png et MenuCharakterRight.png
import os

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
DIR = os.path.join("Data", "Textures", "Menu", "b")
SRC = os.path.join(HERE, "..", "..", "game_pristine", DIR, "MenuCharakter.png")
OUT = os.path.join(HERE, "..", "data", DIR)

TEX = 512          # texture 512x512 ; damier sur 512x480 (ModSideFill._TEX_W / _TEX_H)
H = 480            # hauteur utile de l'image du cadre
CELL = 64          # largeur d'une colonne du damier aux bords du cadre
PURPLE = (128, 54, 127)
BLACK = (0, 0, 0)
FADE = 256         # distance (px) où l'assombrissement est complet
DARK = 0.55        # part de luminosité retirée au bout de FADE
S = 4              # suréchantillonnage pour des limites de cases lisses


def is_purple(c):
    return c[0] + c[2] > 150


def smoothstep(t):
    t = max(0.0, min(1.0, t))
    return t * t * (3 - 2 * t)


def make(frame, edge_x, right):
    px = frame.load()
    edge = [is_purple(px[edge_x, y]) for y in range(H)]
    big = Image.new("RGB", (TEX * S, H * S))
    out = big.load()
    for X in range(TEX * S):
        # distance au bord du cadre, en pixels de l'image du cadre
        d = (X + 0.5) / S if right else (TEX * S - X - 0.5) / S
        flip = int(d // CELL) % 2 == 0  # la colonne collée au cadre est de la couleur inverse
        k = 1.0 - DARK * smoothstep(d / FADE)
        purple = tuple(int(c * k) for c in PURPLE)
        for Y in range(H * S):
            p = edge[min(H - 1, int(Y / S))] != flip
            out[X, Y] = purple if p else BLACK

    tex = Image.new("RGBA", (TEX, TEX), (0, 0, 0, 255))
    tex.paste(big.resize((TEX, H), Image.LANCZOS), (0, 0))
    return tex


def main():
    frame = Image.open(SRC).convert("RGB")
    os.makedirs(OUT, exist_ok=True)
    for name, edge_x, right in (("MenuCharakterLeft.png", 0, False), ("MenuCharakterRight.png", 639, True)):
        path = os.path.join(OUT, name)
        make(frame, edge_x, right).save(path)
        print("->", os.path.normpath(path))


if __name__ == "__main__":
    main()
