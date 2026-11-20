# Images du cadre de stats de l'écran de choix de l'animal (chantier 4, voir notes/animaux.md).
#   Menu/b/StatsPanel.png : panneau sombre translucide, liseré bleu (même style que ModsPanel.png).
#   Menu/a/StatsTrack.png : rail vide d'une barre (creux sombre, bord clair).
#   Menu/a/StatsSpeed.png, StatsAccel.png, StatsTurn.png : remplissage des barres (rouge, jaune, vert), coupé à droite
#       par les coordonnées de texture (comme la barre de chargement d'origine) : jamais étiré.
# Les tailles à l'écran (640x480) doivent rester d'accord avec KidsAnimalChoiceMenuWindow (_ST_*).
# Usage : python mods/art/make_animal_stats.py
import os

from PIL import Image, ImageDraw, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
OUT_A = os.path.join(HERE, "..", "data", "Data", "Textures", "Menu", "a")
OUT_B = os.path.join(HERE, "..", "data", "Data", "Textures", "Menu", "b")
S = 4  # suréchantillonnage

# panneau : 140 x 133 px en 640x480 -> image utile 256 x PANEL_TEX_H dans une texture 256x256
PANEL_W, PANEL_H = 140, 133
PANEL_TEX = 256
PANEL_TEX_H = round(PANEL_TEX * PANEL_H / PANEL_W)  # 243
# rail : 120 x 12 px -> image utile 256 x TRACK_TEX_H dans une texture 256x32
TRACK_W, TRACK_H = 120, 12
TRACK_TEX_H = round(256 * TRACK_H / TRACK_W)  # 26
# remplissages (dégradé gauche -> droite) : début, fin
FILLS = {
    "StatsSpeed.png": ((215, 40, 30), (255, 140, 40)),
    "StatsAccel.png": ((235, 150, 0), (255, 235, 70)),
    "StatsTurn.png": ((40, 160, 70), (120, 235, 120)),
}


def make_panel():
    W, H = PANEL_TEX * S, PANEL_TEX_H * S
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sh = Image.new("L", (W, H), 0)
    ImageDraw.Draw(sh).rounded_rectangle((8 * S, 10 * S, W - 4 * S, H - 2 * S), 22 * S, fill=160)
    im.paste(Image.new("RGBA", (W, H), (0, 0, 0, 255)), (0, 0), sh.filter(ImageFilter.GaussianBlur(5 * S)))
    d = ImageDraw.Draw(im)
    box = (5 * S, 5 * S, W - 8 * S, H - 8 * S)
    d.rounded_rectangle(box, 20 * S, fill=(18, 20, 34, 215), outline=(70, 170, 255, 255), width=4 * S)
    inner = (box[0] + 7 * S, box[1] + 7 * S, box[2] - 7 * S, box[3] - 7 * S)
    d.rounded_rectangle(inner, 14 * S, outline=(40, 90, 150, 255), width=1 * S)
    im = im.resize((PANEL_TEX, PANEL_TEX_H), Image.LANCZOS)
    tex = Image.new("RGBA", (PANEL_TEX, PANEL_TEX), (0, 0, 0, 0))
    tex.alpha_composite(im, (0, 0))
    return tex


def make_track():
    W, H = 256 * S, TRACK_TEX_H * S
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    r = H // 2 - 1
    d.rounded_rectangle((1, 1, W - 2, H - 2), r, fill=(6, 8, 16, 235), outline=(150, 175, 210, 255), width=3 * S)
    # ombre intérieure en haut (creux)
    m = Image.new("L", (W, H), 0)
    ImageDraw.Draw(m).rounded_rectangle((3 * S, 3 * S, W - 3 * S, H // 2), r // 2, fill=70)
    im.paste(Image.new("RGBA", (W, H), (0, 0, 0, 255)), (0, 0), m.filter(ImageFilter.GaussianBlur(2 * S)))
    im = im.resize((256, TRACK_TEX_H), Image.LANCZOS)
    tex = Image.new("RGBA", (256, 32), (0, 0, 0, 0))
    tex.alpha_composite(im, (0, 0))
    return tex


def make_fill(c0, c1):
    """256x16 opaque : dégradé horizontal, reflet clair en haut, bas plus sombre."""
    W, H = 256, 16
    im = Image.new("RGBA", (W, H))
    p = im.load()
    for x in range(W):
        t = x / (W - 1)
        base = [c0[k] * (1 - t) + c1[k] * t for k in range(3)]
        for y in range(H):
            v = y / (H - 1)
            if v < 0.45:  # reflet
                k = 0.55 * (1 - v / 0.45)
                col = [b + (255 - b) * k for b in base]
            else:  # ombre vers le bas
                k = 0.35 * (v - 0.45) / 0.55
                col = [b * (1 - k) for b in base]
            p[x, y] = tuple(int(round(c)) for c in col) + (255,)
    return im


def save(im, d, name):
    os.makedirs(d, exist_ok=True)
    path = os.path.join(d, name)
    im.save(path)
    print("->", os.path.normpath(path))


def main():
    save(make_panel(), OUT_B, "StatsPanel.png")
    save(make_track(), OUT_A, "StatsTrack.png")
    for name, (c0, c1) in FILLS.items():
        save(make_fill(c0, c1), OUT_A, name)
    print("panneau : image utile 256 x %d ; rail : 256 x %d" % (PANEL_TEX_H, TRACK_TEX_H))


if __name__ == "__main__":
    main()
