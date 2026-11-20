# Images de l'écran « En ligne » (chantier 8, phase 3, voir notes/online.md).
#   Menu/a/OnlineButton.png, OnlineButtonPods.png : bouton du menu principal (globe + « EN LIGNE »), même style que
#       le bouton « MODS » (icône 128x128, libellé blanc cerné de noir ; globe rouge et cerné de rouge quand on appuie).
#   Menu/b/OnlinePanel.png : panneau de l'écran, comme celui de l'écran Mods, titre « EN LIGNE ».
# Usage : python mods/art/make_online.py
import math
import os

from PIL import Image, ImageDraw, ImageFilter

from make_mods_menu import OUT_A, OUT_B, S, make_panel, outlined_text, save, shadow_of


def globe(size, light, dark, line, outline):
    """Globe : disque en dégradé, méridiens et parallèles, contour noir épais."""
    W = size * S
    c = W / 2
    r = W / 2 - 5 * S
    mask = Image.new("L", (W, W), 0)
    ImageDraw.Draw(mask).ellipse((c - r, c - r, c + r, c + r), fill=255)

    grad = Image.new("RGBA", (W, W))
    gp = grad.load()
    for y in range(W):
        for x in range(W):
            t = min(1.0, max(0.0, (x * 0.5 + y * 0.8) / (W * 1.3)))
            gp[x, y] = tuple(int(light[k] * (1 - t) + dark[k] * t) for k in range(3)) + (255,)
    body = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    body.paste(grad, (0, 0), mask)

    # méridiens (ellipses de plus en plus étroites) et parallèles, coupés au disque
    grid = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    d = ImageDraw.Draw(grid)
    w = int(2.2 * S)
    for k in (0.0, 0.45, 0.8):
        rx = r * k
        if rx < 1:
            d.line((c, c - r, c, c + r), fill=line, width=w)
        else:
            d.ellipse((c - rx, c - r, c + rx, c + r), outline=line, width=w)
    for k in (-0.55, 0.0, 0.55):
        y = c + r * k
        half = r * math.sqrt(1 - k * k)
        d.line((c - half, y, c + half, y), fill=line, width=w)
    clipped = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    clipped.paste(grid, (0, 0), mask)
    body.alpha_composite(clipped)

    # contour : disque dilaté en noir, sous le corps
    edge = mask.filter(ImageFilter.MaxFilter(4 * S + 1))
    out = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    out.paste(Image.new("RGBA", (W, W), outline), (0, 0), edge)
    out.alpha_composite(body)

    # reflet clair en haut à gauche
    hl = Image.new("L", (W, W), 0)
    ImageDraw.Draw(hl).ellipse((c - r * 0.75, c - r * 0.85, c - r * 0.05, c - r * 0.25), fill=110)
    hl = hl.filter(ImageFilter.GaussianBlur(4 * S))
    out.paste(Image.new("RGBA", (W, W), (255, 255, 255, 255)), (0, 0), hl)
    return out.resize((size, size), Image.LANCZOS)


def make_button(pressed):
    im = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    if pressed:
        g = globe(84, (255, 160, 150), (190, 20, 20), (120, 0, 0, 255), (120, 0, 0, 255))
    else:
        g = globe(84, (150, 220, 255), (20, 80, 170), (230, 245, 255, 255), (0, 0, 0, 255))
    layer = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    layer.alpha_composite(g, (22, 6))
    im.alpha_composite(shadow_of(layer))
    im.alpha_composite(layer)
    outline = (230, 0, 0, 255) if pressed else (0, 0, 0, 255)
    t = outlined_text((256, 96), "EN LIGNE", 17, outline, stroke=3)
    im.alpha_composite(t, ((128 - t.width) // 2 + 6, 128 - t.height - 22))
    return im


def main():
    save(make_button(False), OUT_A, "OnlineButton.png")
    save(make_button(True), OUT_A, "OnlineButtonPods.png")
    save(make_panel("EN LIGNE"), OUT_B, "OnlinePanel.png")


if __name__ == "__main__":
    main()
