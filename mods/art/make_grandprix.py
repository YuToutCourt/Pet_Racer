# Images du Grand Prix (chantier 6, voir notes/grandprix.md).
#   Menu/a/GrandPrixButton.png, GrandPrixButtonPods.png : bouton « GRAND PRIX » du menu Un joueur, même style que
#       « Course » / « Aventure » (lettres BD blanches cernées de noir, cernées de rouge quand on appuie, posées
#       en vague et un peu penchées), 256x128 comme « Aventure ».
#   Menu/b/GPPanel.png : panneau du classement, comme celui de l'écran Mods, titre « GRAND PRIX ».
# Usage : python mods/art/make_grandprix.py
import math
import os

from PIL import Image, ImageDraw, ImageFont

from make_mods_menu import FONT, OUT_A, OUT_B, S, make_panel, save

TEXT = "GRAND PRIX"


def make_button(pressed):
    W, H = 256 * S, 128 * S
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    font = ImageFont.truetype(FONT, 33 * S)
    outline = (230, 0, 0, 255) if pressed else (0, 0, 0, 255)
    stroke = 5 * S
    # largeur de chaque lettre pour les poser une à une, en vague, comme les libellés d'origine
    widths = [font.getlength(c) * 0.97 for c in TEXT]
    x = (W - sum(widths)) / 2
    for i, c in enumerate(TEXT):
        if c == " ":
            x += widths[i]
            continue
        cell = Image.new("RGBA", (70 * S, 90 * S), (0, 0, 0, 0))
        ImageDraw.Draw(cell).text((35 * S, 45 * S), c, font=font, anchor="mm", fill=(255, 255, 255, 255),
                                  stroke_width=stroke, stroke_fill=outline)
        cell = cell.rotate(-8 + 10 * math.sin(i * 1.3), resample=Image.BICUBIC)
        y = H / 2 - 45 * S + 7 * S * math.sin(i * 0.9) + (i - len(TEXT) / 2) * 1.6 * S
        im.alpha_composite(cell, (int(x + widths[i] / 2 - 35 * S), int(y)))
        x += widths[i]
    return im.resize((256, 128), Image.LANCZOS)


def main():
    save(make_button(False), OUT_A, "GrandPrixButton.png")
    save(make_button(True), OUT_A, "GrandPrixButtonPods.png")
    save(make_panel("GRAND PRIX"), OUT_B, "GPPanel.png")


if __name__ == "__main__":
    main()
