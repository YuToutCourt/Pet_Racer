# Images de l'écran « Mods » (chantier 3, voir notes/mods_menu.md).
#   Menu/a/ModsButton.png, ModsButtonPods.png : bouton du menu principal (engrenage + « MODS »), même style que
#       les boutons d'origine (icône 128x128, libellé blanc cerné de noir ; cerné de rouge quand on appuie).
#   Menu/a/ModsArrows.png : flèches gauche / droite des lignes de réglage (reprises de data/menu/RAMKA_log.png,
#       l'écran de choix du profil), normales en haut, surlignées en bas.
#   Menu/a/ModsRow.png : barre translucide derrière la ligne choisie.
#   Menu/b/ModsPanel.png : panneau sombre de l'écran, titre « MODS ».
# Usage : python mods/art/make_mods_menu.py
import os

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont
import math

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "..")
PRISTINE = os.path.join(ROOT, "game_pristine")
OUT_A = os.path.join(HERE, "..", "data", "Data", "Textures", "Menu", "a")
OUT_B = os.path.join(HERE, "..", "data", "Data", "Textures", "Menu", "b")
FONT = "C:/Windows/Fonts/comicbd.ttf"  # le plus proche de l'écriture BD des boutons d'origine
S = 4  # suréchantillonnage

# Panneau : doit rester d'accord avec KidsModsMenuWindow (_PANEL_*) : 0,84 x 0,80 de l'écran 640x480
PANEL_W, PANEL_H = 538, 384           # taille à l'écran en 640x480
PANEL_TEX = 512                       # texture 512x512, image utile 512 x PANEL_TEX_H
PANEL_TEX_H = round(PANEL_TEX * PANEL_H / PANEL_W)


def outlined_text(size, text, px, outline, fill=(255, 255, 255, 255), stroke=None):
    """Texte BD : remplissage blanc, contour épais. Renvoie une image RGBA ajustée au texte."""
    font = ImageFont.truetype(FONT, px)
    stroke = stroke or max(2, px // 6)
    im = Image.new("RGBA", size, (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.text((size[0] // 2, size[1] // 2), text, font=font, anchor="mm", fill=fill, stroke_width=stroke, stroke_fill=outline)
    return im.crop(im.getbbox())


def gear(size, teeth, r_out, r_in, r_hole, light, dark, outline):
    """Engrenage métallique (dégradé clair -> foncé, contour noir), dessiné en grand puis réduit."""
    W = size * S
    cx = cy = W / 2
    pts = []
    n = teeth * 4
    for i in range(n):
        a = 2 * math.pi * (i - 0.5) / n
        r = r_out if (i % 4) in (1, 2) else r_in
        pts.append((cx + r * S * math.cos(a), cy + r * S * math.sin(a)))
    mask = Image.new("L", (W, W), 0)
    md = ImageDraw.Draw(mask)
    md.polygon(pts, fill=255)
    md.ellipse((cx - r_hole * S, cy - r_hole * S, cx + r_hole * S, cy + r_hole * S), fill=0)

    # dégradé diagonal + reflet
    grad = Image.new("RGBA", (W, W))
    gp = grad.load()
    for y in range(W):
        for x in range(0, W, 1):
            t = min(1.0, max(0.0, (x * 0.4 + y * 0.9) / (W * 1.3)))
            gp[x, y] = tuple(int(light[k] * (1 - t) + dark[k] * t) for k in range(3)) + (255,)
    body = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    body.paste(grad, (0, 0), mask)

    # contour : masque dilaté, en noir, sous le corps
    edge = mask.filter(ImageFilter.MaxFilter(4 * S + 1))
    out = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    out.paste(Image.new("RGBA", (W, W), outline), (0, 0), edge)
    out.alpha_composite(body)

    # petit reflet clair en haut à gauche
    hl = Image.new("L", (W, W), 0)
    ImageDraw.Draw(hl).ellipse((cx - r_in * S * 0.85, cy - r_in * S * 0.95, cx + r_in * S * 0.2, cy - r_in * S * 0.1), fill=90)
    hl = ImageChops.multiply(hl, mask).filter(ImageFilter.GaussianBlur(3 * S))
    out.paste(Image.new("RGBA", (W, W), (255, 255, 255, 255)), (0, 0), hl)
    return out.resize((size, size), Image.LANCZOS)


def shadow_of(im, offset=(4, 5), blur=4, alpha=150):
    a = im.split()[3].point(lambda v: alpha if v > 0 else 0)
    sh = Image.new("RGBA", im.size, (0, 0, 0, 0))
    sh.putalpha(a.filter(ImageFilter.GaussianBlur(blur)))
    out = Image.new("RGBA", im.size, (0, 0, 0, 0))
    out.alpha_composite(sh, offset)
    return out


def make_button(pressed):
    im = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    if pressed:
        g = gear(92, 8, 40, 31, 12, (255, 150, 150), (200, 20, 20), (120, 0, 0, 255))
    else:
        g = gear(92, 8, 40, 31, 12, (225, 232, 240), (70, 85, 110), (0, 0, 0, 255))
    layer = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    layer.alpha_composite(g, (18, 4))
    im.alpha_composite(shadow_of(layer))
    im.alpha_composite(layer)
    outline = (230, 0, 0, 255) if pressed else (0, 0, 0, 255)
    # libellé à la taille de ceux d'origine (« OPTIONS », « CREDITS » : ~11 px de haut sur 128)
    t = outlined_text((256, 96), "MODS", 17, outline, stroke=3)
    im.alpha_composite(t, ((128 - t.width) // 2 + 10, 128 - t.height - 22))
    return im


def make_arrows():
    """Atlas 128x64 : en haut flèche gauche (0..64) et droite (64..128), en bas les mêmes surlignées."""
    src = Image.open(os.path.join(PRISTINE, "data", "menu", "RAMKA_log.png")).convert("RGBA")
    # boîtes relevées sur l'alpha de la planche (des points isolés traînent juste à côté)
    left = src.crop((2, 440, 35, 471))
    right = src.crop((107, 440, 140, 471))
    left = left.crop(left.getbbox())
    right = right.crop(right.getbbox())
    atlas = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    for i, a in enumerate((left, right)):
        a = a.resize((28, 26), Image.LANCZOS)
        cell = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
        cell.alpha_composite(a, ((64 - 28) // 2, 3))
        atlas.alpha_composite(cell, (64 * i, 0))
        # surlignée : teinte orangée + halo
        r, g, b, al = a.split()
        lum = Image.merge("RGB", (r, g, b)).convert("L")
        hot = Image.merge("RGBA", (lum.point(lambda v: min(255, 120 + v)), lum.point(lambda v: min(255, 60 + v * 3 // 4)),
                                   lum.point(lambda v: v // 4), al))
        glow = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
        ga = Image.new("L", (64, 32), 0)
        ga.paste(al, ((64 - 28) // 2, 3))
        glow.paste(Image.new("RGBA", (64, 32), (255, 220, 120, 255)), (0, 0), ga.filter(ImageFilter.GaussianBlur(2.5)))
        cell = glow
        cell.alpha_composite(hot, ((64 - 28) // 2, 3))
        atlas.alpha_composite(cell, (64 * i, 32))
    return atlas


def make_row():
    """Barre 256x32 : bleu clair translucide, bords arrondis et fondus."""
    W, H = 256 * S, 32 * S
    m = Image.new("L", (W, H), 0)
    ImageDraw.Draw(m).rounded_rectangle((6 * S, 4 * S, W - 6 * S, H - 4 * S), 10 * S, fill=110)
    m = m.filter(ImageFilter.GaussianBlur(3 * S)).resize((256, 32), Image.LANCZOS)
    im = Image.new("RGBA", (256, 32), (90, 170, 255, 255))
    im.putalpha(m)
    return im


def make_panel(title="MODS"):
    """Panneau 512x512 (image utile 512 x PANEL_TEX_H) : fond sombre translucide, liseré bleu, titre (« MODS »)."""
    W, H = PANEL_TEX * S, PANEL_TEX_H * S
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sh = Image.new("L", (W, H), 0)
    ImageDraw.Draw(sh).rounded_rectangle((10 * S, 14 * S, W - 6 * S, H - 2 * S), 26 * S, fill=170)
    im.paste(Image.new("RGBA", (W, H), (0, 0, 0, 255)), (0, 0), sh.filter(ImageFilter.GaussianBlur(6 * S)))
    d = ImageDraw.Draw(im)
    box = (6 * S, 8 * S, W - 10 * S, H - 8 * S)
    d.rounded_rectangle(box, 24 * S, fill=(18, 20, 34, 225), outline=(70, 170, 255, 255), width=4 * S)
    inner = (box[0] + 7 * S, box[1] + 7 * S, box[2] - 7 * S, box[3] - 7 * S)
    d.rounded_rectangle(inner, 18 * S, outline=(40, 90, 150, 255), width=1 * S)
    im = im.resize((PANEL_TEX, PANEL_TEX_H), Image.LANCZOS)
    title = outlined_text((512, 128), title, 40, (0, 0, 0, 255), stroke=5)
    # le panneau est étiré de 512 à 538 px de large en 640x480 : le titre est compressé d'autant pour rester droit
    title = title.resize((round(title.width * PANEL_TEX / PANEL_W), title.height), Image.LANCZOS)
    im.alpha_composite(title, ((PANEL_TEX - title.width) // 2, 14))
    tex = Image.new("RGBA", (PANEL_TEX, PANEL_TEX), (0, 0, 0, 0))
    tex.alpha_composite(im, (0, 0))
    return tex


def save(im, d, name):
    os.makedirs(d, exist_ok=True)
    p = os.path.join(d, name)
    im.save(p)
    print("->", os.path.normpath(p))


def main():
    save(make_button(False), OUT_A, "ModsButton.png")
    save(make_button(True), OUT_A, "ModsButtonPods.png")
    save(make_arrows(), OUT_A, "ModsArrows.png")
    save(make_row(), OUT_A, "ModsRow.png")
    save(make_panel(), OUT_B, "ModsPanel.png")
    print("panneau : image utile 512 x", PANEL_TEX_H)


if __name__ == "__main__":
    main()
