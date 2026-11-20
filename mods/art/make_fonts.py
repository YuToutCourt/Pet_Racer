# Polices du moteur (Data\Panel\MS Sans Serif<N>.dat) avec les lettres accentuées du français.
#
# Le moteur dessine le texte octet par octet : l'octet sert d'indice dans la table de glyphes de la police.
# Les polices n'ont que l'ASCII + une liste fixe de « lettres polonaises » (ę ó ą ś ł ż ź ć ń ...) :
#   - 16/18/20/26 (fichiers de 2001, game_pristine) : glyphes polonais (Windows-1250) ;
#   - 32/40 (créées par le moteur au premier lancement en 1440p / 4K, copiées dans mods/art/font_src) : mêmes
#     cases, mais dessinées par Windows en Windows-1252 (ê ó ä ñ Ê Ó ...).
# Il manque donc é è à ç ... : le texte « élastique » s'affichait « lastique ». On fabrique ces lettres à partir des
# glyphes de la police elle-même (même dessin) : lettre de base + accent pris sur une lettre qui l'a déjà
# (aigu de ó/Ó, tréma de ä, circonflexe de ê ou aigu + grave, grave = aigu retourné, cédille = virgule).
# Les nouvelles cases vont dans la place libre de la texture (agrandie en hauteur si besoin).
#
# Format .dat : nom (64 octets), largeur, hauteur, hauteur de ligne (int32) ; 256 × (avance int32, u0 v0 u1 v1
# float32) ; int32 (= 1) ; pixels RGBA largeur × hauteur. Case d'un glyphe = avance × hauteur de ligne.
#
# Usage : python mods/art/make_fonts.py [dossier_aperçu]  ->  mods/data/Data/Panel/MS Sans Serif<N>.dat
import os
import struct
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
PRISTINE = os.path.join(HERE, "..", "..", "game_pristine", "Data", "Panel")
ENGINE = os.path.join(HERE, "font_src")
OUT = os.path.join(HERE, "..", "data", "Data", "Panel")

# (taille, dossier source, cases spéciales déjà en Windows-1252)
FONTS = [(16, PRISTINE, False), (18, PRISTINE, False), (20, PRISTINE, False), (26, PRISTINE, False),
         (32, ENGINE, True), (40, ENGINE, True)]

# octet Windows-1252 -> (lettre de base, accent)
RECIPES = [
    ("é", "e", "acute"), ("è", "e", "grave"), ("ê", "e", "circ"), ("ë", "e", "diaer"),
    ("à", "a", "grave"), ("â", "a", "circ"), ("ç", "c", "cedil"),
    ("î", "i", "circ"), ("ï", "i", "diaer"), ("ô", "o", "circ"),
    ("ù", "u", "grave"), ("û", "u", "circ"), ("ü", "u", "diaer"),
    ("É", "E", "acute"), ("È", "E", "grave"), ("Ê", "E", "circ"), ("À", "A", "grave"), ("Ç", "C", "cedil"),
]
# cases occupées par une lettre polonaise qu'aucun texte français n'utilise (ę, Ę) : on les remplace
REPLACEABLE = (0xEA, 0xCA)

EMPTY = (0, 0, 0, 0)


def code(ch):
    return ch.encode("cp1252")[0]


class Font:
    def __init__(self, path):
        d = open(path, "rb").read()
        self.name = d[:64]
        self.W, self.H, self.LH = struct.unpack("<3i", d[64:76])
        self.ents = [list(struct.unpack("<i4f", d[76 + c * 20:96 + c * 20])) for c in range(256)]
        self.flag = d[76 + 5120:80 + 5120]
        self.img = Image.frombytes("RGBA", (self.W, self.H), d[80 + 5120:])
        self.free_y = max(round(e[4] * self.H) for e in self.ents if e[0] > 0) + 1
        self.free_x = 0

    def has(self, c):
        return self.ents[c][0] > 0

    def cell(self, c):
        adv, u0, v0, u1, v1 = self.ents[c]
        x0, y0 = round(u0 * self.W), round(v0 * self.H)
        return self.img.crop((x0, y0, x0 + adv, y0 + self.LH))

    def grow(self):
        h = self.H * 2
        img = Image.new("RGBA", (self.W, h), EMPTY)
        img.paste(self.img, (0, 0))
        for e in self.ents:
            if e[0] > 0:
                e[2] = e[2] * self.H / h
                e[4] = e[4] * self.H / h
        self.img, self.H = img, h

    def put(self, c, glyph):
        adv = glyph.size[0]
        if self.free_x + adv > self.W:
            self.free_x = 0
            self.free_y += self.LH + 1
        while self.free_y + self.LH > self.H:
            self.grow()
        x, y = self.free_x, self.free_y
        self.img.paste(glyph, (x, y))
        self.ents[c] = [adv, x / self.W, y / self.H, (x + adv) / self.W, (y + self.LH) / self.H]
        self.free_x += adv + 1

    def save(self, path):
        out = bytearray(self.name)
        out += struct.pack("<3i", self.W, self.H, self.LH)
        for e in self.ents:
            out += struct.pack("<i4f", *e)
        out += self.flag
        out += self.img.tobytes()
        with open(path, "wb") as f:
            f.write(out)


# --- petites opérations sur les glyphes (images RGBA d'une case) ---

def ink(img):
    """boîte de l'encre (x0, y0, x1, y1 inclus) ou None"""
    w, h = img.size
    px = img.load()
    xs = [x for x in range(w) for y in range(h) if px[x, y] != EMPTY]
    ys = [y for y in range(h) for x in range(w) if px[x, y] != EMPTY]
    return (min(xs), min(ys), max(xs), max(ys)) if xs else None


def center_x(img):
    b = ink(img)
    return (b[0] + b[2]) / 2.0


def rows_above(img, y_limit):
    """garde seulement les lignes au-dessus de y_limit (exclu)"""
    out = img.copy()
    px = out.load()
    for y in range(y_limit, img.size[1]):
        for x in range(img.size[0]):
            px[x, y] = EMPTY
    return out


def rows_from(img, y_limit):
    out = img.copy()
    px = out.load()
    for y in range(0, y_limit):
        for x in range(img.size[0]):
            px[x, y] = EMPTY
    return out


def over(dst, src, x, y):
    """colle les pixels d'encre de src sur dst en (x, y), coupés au bord"""
    dp, sp = dst.load(), src.load()
    for sy in range(src.size[1]):
        for sx in range(src.size[0]):
            p = sp[sx, sy]
            tx, ty = x + sx, y + sy
            if p != EMPTY and 0 <= tx < dst.size[0] and 0 <= ty < dst.size[1]:
                dp[tx, ty] = p


class Accent:
    """accent rogné + écart vertical au haut de la lettre + décalage horizontal par rapport à son centre"""

    def __init__(self, img, gap, dx):
        self.img, self.gap, self.dx = img, gap, dx

    @staticmethod
    def take(f, accented, base):
        """accent = ce que la lettre accentuée a au-dessus du haut de la lettre de base"""
        a, b = f.cell(code(accented)), f.cell(code(base))
        top = ink(b)[1]
        only = rows_above(a, top)
        bx = ink(only)
        assert bx, (accented, base)
        img = only.crop((bx[0], bx[1], bx[2] + 1, bx[3] + 1))
        return Accent(img, top - bx[3] - 1, (bx[0] + bx[2]) / 2.0 - center_x(b))

    def mirrored(self):
        return Accent(self.img.transpose(Image.FLIP_LEFT_RIGHT), self.gap, -self.dx)

    @staticmethod
    def circumflex(acute):
        """^ = aigu (montant) à gauche + grave (descendant) à droite, sommets joints"""
        w, h = acute.img.size
        img = Image.new("RGBA", (2 * w - 1, h), EMPTY)
        over(img, acute.img, 0, 0)
        over(img, acute.img.transpose(Image.FLIP_LEFT_RIGHT), w - 1, 0)
        return Accent(img, acute.gap, 0.0)

    def fit(self, width):
        """accent trop large pour une lettre étroite (î, ï) : tréma resserré, sinon rogné au centre"""
        w, h = self.img.size
        if w <= width:
            return self.img
        px = self.img.load()
        cols = [x for x in range(w) if any(px[x, y] != EMPTY for y in range(h))]
        groups = []
        for x in cols:
            if groups and x == groups[-1][1] + 1:
                groups[-1][1] = x
            else:
                groups.append([x, x])
        if len(groups) == 2:
            (l0, l1), (r0, r1) = groups
            wl, wr = l1 - l0 + 1, r1 - r0 + 1
            space = max(1, width - wl - wr)
            img = Image.new("RGBA", (wl + space + wr, h), EMPTY)
            img.paste(self.img.crop((l0, 0, l1 + 1, h)), (0, 0))
            img.paste(self.img.crop((r0, 0, r1 + 1, h)), (wl + space, 0))
            if img.size[0] <= width:
                return img
            self.img = img
            w = img.size[0]
        cut = (w - width) // 2
        return self.img.crop((cut, 0, cut + width, h))


def compose(base, accent):
    out = base.copy()
    b = ink(base)
    img = accent.fit(base.size[0])
    w, h = img.size
    y1 = b[1] - accent.gap - 1
    y0 = max(0, y1 - (h - 1))
    x0 = int(round((b[0] + b[2]) / 2.0 + accent.dx - (w - 1) / 2.0))
    x0 = max(0, min(x0, base.size[0] - w))
    over(out, img, x0, y0)
    return out


def cedilla(f, base):
    """virgule de la police, centrée sous la lettre et accrochée à son bas"""
    comma = f.cell(code(","))
    bc = ink(comma)
    img = comma.crop((bc[0], bc[1], bc[2] + 1, bc[3] + 1))
    out = base.copy()
    b = ink(base)
    w = img.size[0]
    x0 = int(round((b[0] + b[2]) / 2.0 - (w - 1) / 2.0))
    over(out, img, x0, b[3])
    return out


def build(size, src, native):
    f = Font(os.path.join(src, "MS Sans Serif%d.dat" % size))
    acute = {"lc": Accent.take(f, "ó", "o"), "uc": Accent.take(f, "Ó", "O")}
    grave = {k: v.mirrored() for k, v in acute.items()}
    if native:  # vrai circonflexe dessiné par Windows dans ê / Ê
        circ = {"lc": Accent.take(f, "ê", "e"), "uc": Accent.take(f, "Ê", "E")}
    else:
        circ = {k: Accent.circumflex(v) for k, v in acute.items()}
    diaer = {"lc": Accent.take(f, "ä", "a")}
    top_x = ink(f.cell(code("o")))[1]  # haut des minuscules : sert à ôter le point du i

    added, kept = [], []
    for ch, base_ch, kind in RECIPES:
        c = code(ch)
        if f.has(c):
            if native or c not in REPLACEABLE:
                kept.append(ch)
                continue
        case = "uc" if base_ch.isupper() else "lc"
        base = f.cell(code(base_ch))
        if base_ch == "i":
            base = rows_from(base, top_x)  # i sans point
        if kind == "cedil":
            glyph = cedilla(f, base)
        else:
            glyph = compose(base, {"acute": acute, "grave": grave, "circ": circ, "diaer": diaer}[kind][case])
        f.put(c, glyph)
        added.append(ch)

    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, "MS Sans Serif%d.dat" % size)
    f.save(path)
    print("-> %s (%dx%d) ajoutées : %s%s" % (os.path.normpath(path), f.W, f.H, "".join(added),
                                            ("  déjà là : " + "".join(kept)) if kept else ""))
    return f


def preview(fonts, folder):
    """texte d'essai rendu comme le moteur (case après case), sur le bleu du panneau"""
    lines = ["Rattrapage des IA (élastique)", "Bonus imposé : Pastèque, Fusée, guêpes",
             "Aventure : aller à l'étape   ça île naïf où Noël",
             "ÉTÉ À Ça Être  Responsable développement"]
    for size, f in fonts:
        w = max(sum(f.ents[b][0] for b in s.encode("cp1252")) for s in lines) + 16
        img = Image.new("RGBA", (w, f.LH * len(lines) + 16), (20, 30, 70, 255))
        y = 8
        for s in lines:
            x = 8
            for b in s.encode("cp1252"):
                if f.has(b):
                    img.alpha_composite(f.cell(b), (x, y))
                x += f.ents[b][0]
            y += f.LH
        scale = 3 if size <= 20 else 2
        img.resize((img.size[0] * scale, img.size[1] * scale), Image.NEAREST).save(
            os.path.join(folder, "font_preview_%d.png" % size))


def main():
    fonts = [(size, build(size, src, native)) for size, src, native in FONTS]
    if len(sys.argv) > 1:
        preview(fonts, sys.argv[1])


if __name__ == "__main__":
    main()
