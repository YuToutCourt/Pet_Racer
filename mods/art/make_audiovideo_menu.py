# Fond du menu Audio/Vidéo sans les libellés peints « 640/480 800/600 1024/768 » (voir notes/resolution.md).
# À leur place : une plaque sombre arrondie, sur laquelle KidsAudioVideoMenuWindow écrit la résolution choisie
# (ModVideo garde une liste de résolutions bien plus longue que les 3 d'origine).
# Usage : python mods/art/make_audiovideo_menu.py
#   -> mods/data/Data/Textures/Menu/b/MenuAudioVideo1024.png
import os

from PIL import Image, ImageDraw, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
NAME = os.path.join("Data", "Textures", "Menu", "b", "MenuAudioVideo1024.png")
SRC = os.path.join(HERE, "..", "..", "game_pristine", NAME)
OUT = os.path.join(HERE, "..", "data", NAME)

# plaque, en pixels de la texture (l'image utile fait 640x480 en haut à gauche d'une texture 1024x512).
# Doit rester d'accord avec ModVideo._LABEL_RECT (fractions de 640x480).
PLATE = (410, 232, 604, 264)
RADIUS = 12
S = 4  # suréchantillonnage pour des bords lisses


def main():
    im = Image.open(SRC).convert("RGBA")
    x0, y0, x1, y1 = PLATE
    w, h = (x1 - x0) * S, (y1 - y0) * S

    # ombre douce, puis plaque opaque : les libellés d'origine disparaissent sous la plaque
    shadow = Image.new("L", (w + 16 * S, h + 16 * S), 0)
    ImageDraw.Draw(shadow).rounded_rectangle((8 * S, 10 * S, 8 * S + w, 10 * S + h), RADIUS * S, fill=150)
    shadow = shadow.filter(ImageFilter.GaussianBlur(4 * S)).resize(((w + 16 * S) // S, (h + 16 * S) // S), Image.LANCZOS)
    black = Image.new("RGBA", shadow.size, (0, 0, 0, 255))
    im.paste(black, (x0 - 8, y0 - 8), shadow)

    plate = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(plate)
    d.rounded_rectangle((0, 0, w - 1, h - 1), RADIUS * S, fill=(12, 14, 30, 255), outline=(255, 255, 255, 255), width=2 * S)
    plate = plate.resize((x1 - x0, y1 - y0), Image.LANCZOS)
    im.alpha_composite(plate, (x0, y0))

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    im.save(OUT)
    print("->", os.path.normpath(OUT))


if __name__ == "__main__":
    main()
