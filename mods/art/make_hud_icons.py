# Icônes de bonus du HUD (KidsGadget) que le jeu d'origine n'a jamais converties au format des autres.
# Rakieta.png (Fusée) est une image source de 404x219 : le moteur (textures en puissances de 2, comme toutes les
# icônes 128x128 du dossier) ne l'affiche pas, d'où une fusée sans icône. On la redresse en diagonale, on la cadre
# et on la réduit en 128x128 RGBA sur fond transparent.
#
# Missile.png (Missile, bonus du mod) : la même fusée, recolorée en bleu et argent (la Fusée est rouge et jaune), avec
# une pastille dorée « 1 » : il vise le premier. Même recoloration que la texture de son modèle (make_missile_model.py).
#
# Usage : python mods/art/make_hud_icons.py  ->  mods/data/Data/Textures/Hud/Rakieta.png, Missile.png
import colorsys
import os
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "..", "..", "game_pristine", "Data", "Textures", "Hud")
OUT = os.path.join(HERE, "..", "data", "Data", "Textures", "Hud")

SIZE = 128
MARGIN = 3  # comme Kokos.png, qui touche presque les bords

# Recoloration Fusée -> Missile : (teinte d'origine en degrés, nouvelle teinte, facteur de saturation), interpolé.
# rouge -> bleu ; orange (bords, flamme) -> bleu ciel ; jaune pâle (corps) -> argent bleuté.
MISSILE_HUES = [(0, 222, 1.0), (18, 222, 1.0), (32, 198, 0.85), (46, 212, 0.18), (80, 212, 0.18),
                (81, None, 1.0), (329, None, 1.0), (330, 222, 1.0), (360, 222, 1.0)]


def missile_color(r, g, b):
    h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
    deg = h * 360.0
    for (h0, n0, f0), (h1, n1, f1) in zip(MISSILE_HUES, MISSILE_HUES[1:]):
        if h0 <= deg <= h1:
            if n0 is None or n1 is None:
                return r, g, b
            t = (deg - h0) / float(h1 - h0)
            nh = (n0 + (n1 - n0) * t) / 360.0
            ns = s * (f0 + (f1 - f0) * t)
            nr, ng, nb = colorsys.hsv_to_rgb(nh, ns, v)
            return int(round(nr * 255)), int(round(ng * 255)), int(round(nb * 255))
    return r, g, b


def recolor_missile(img):
    img = img.convert("RGBA")
    pixels = img.get_flattened_data() if hasattr(img, "get_flattened_data") else img.getdata()
    px = [missile_color(r, g, b) + (a,) for (r, g, b, a) in pixels]
    out = Image.new("RGBA", img.size)
    out.putdata(px)
    return out


def fit_icon(src, angle):
    # pointe vers le haut à gauche, flammes en bas à droite (angle négatif = sens horaire)
    img = src.rotate(angle, resample=Image.BICUBIC, expand=True)
    img = img.crop(img.split()[3].getbbox())
    box = SIZE - 2 * MARGIN
    scale = min(box / img.width, box / img.height)
    img = img.resize((max(1, round(img.width * scale)), max(1, round(img.height * scale))), Image.LANCZOS)
    out = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    out.alpha_composite(img, ((SIZE - img.width) // 2, (SIZE - img.height) // 2))
    return out


def save(img, name, src):
    os.makedirs(OUT, exist_ok=True)
    img.save(os.path.join(OUT, name))
    print("%s : %dx%d -> %dx%d" % (name, src.width, src.height, SIZE, SIZE))


def make_icon(name, angle):
    src = Image.open(os.path.join(SRC, name)).convert("RGBA")
    save(fit_icon(src, angle), name, src)


def badge_one(size):
    # pastille dorée « 1 », dessinée 4x plus grande puis réduite (bords lisses)
    k = 4
    big = size * k
    im = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse((0, 0, big - 1, big - 1), fill=(92, 52, 8, 255))                       # contour brun
    d.ellipse((2 * k, 2 * k, big - 1 - 2 * k, big - 1 - 2 * k), fill=(226, 160, 24, 255))  # or
    d.ellipse((4 * k, 3 * k, big - 1 - 6 * k, big // 2), fill=(255, 226, 110, 255))  # reflet en haut
    d.ellipse((5 * k, 6 * k, big - 1 - 5 * k, big - 1 - 4 * k), fill=(240, 184, 40, 255))
    font = None
    for f in ("arialbd.ttf", "Arial Bold.ttf", "DejaVuSans-Bold.ttf"):
        try:
            font = ImageFont.truetype(f, int(big * 0.72))
            break
        except IOError:
            pass
    if font is None:
        font = ImageFont.load_default()
    x0, y0, x1, y1 = d.textbbox((0, 0), "1", font=font)
    tx = (big - (x1 - x0)) // 2 - x0
    ty = (big - (y1 - y0)) // 2 - y0
    for ox, oy in ((-k, 0), (k, 0), (0, -k), (0, k), (-k, -k), (k, k), (-k, k), (k, -k)):
        d.text((tx + ox, ty + oy), "1", font=font, fill=(92, 52, 8, 255))
    d.text((tx, ty), "1", font=font, fill=(255, 255, 255, 255))
    return im.resize((size, size), Image.LANCZOS)


def make_missile_icon():
    src = Image.open(os.path.join(SRC, "Rakieta.png")).convert("RGBA")
    out = fit_icon(recolor_missile(src), -60)
    b = badge_one(50)
    out.alpha_composite(b, (SIZE - b.width - 1, 1))  # en haut à droite, coin laissé libre par la fusée
    save(out, "Missile.png", src)


if __name__ == "__main__":
    make_icon("Rakieta.png", -60)
    make_missile_icon()
