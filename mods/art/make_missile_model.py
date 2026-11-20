# Modèle 3D du Missile (bonus du mod, notes/bonus.md) : la fusée du jeu (rakietaBonus.3DA) avec sa propre texture,
# recolorée en bleu et argent comme l'icône du HUD (make_hud_icons.recolor_missile).
#
# Format .3DA (ChromeEngine, relevé sur rakietaBonus.3DA) : "3DA_", puis 32 matériaux de 164 octets à partir de
# l'octet 16, chacun avec deux noms de texture de 32 octets (texture, puis reflet). La forme (sommets, faces, UV,
# normales) et le point d'attache « ogien » (fumée) viennent après et ne changent pas : seul le nom de la texture
# du matériau 0 est remplacé. Les textures des modèles sont cherchées par leur nom dans Data\Textures.
#
# Usage : python mods/art/make_missile_model.py
#   -> mods/data/Data/Meshes/Objects/missileBonus.3DA, mods/data/Data/Textures/missileBonus.png
import os
import sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from make_hud_icons import recolor_missile  # noqa: E402

PRISTINE = os.path.join(HERE, "..", "..", "game_pristine", "Data")
OUT = os.path.join(HERE, "..", "data", "Data")

SRC_MESH = "rakietaBonus.3DA"
SRC_TEX = "rakietaBonus.png"
DST_MESH = "missileBonus.3DA"
DST_TEX = "missileBonus.png"
TEX_NAME_OFFSET = 16  # matériau 0, première texture
TEX_NAME_SIZE = 32


def make_mesh():
    data = bytearray(open(os.path.join(PRISTINE, "Meshes", "Objects", SRC_MESH), "rb").read())
    if data[:4] != b"3DA_":
        raise SystemExit("%s : en-tête 3DA_ absent" % SRC_MESH)
    field = bytes(data[TEX_NAME_OFFSET:TEX_NAME_OFFSET + TEX_NAME_SIZE])
    if field.split(b"\0")[0] != SRC_TEX.encode("ascii"):
        raise SystemExit("%s : texture inattendue %r" % (SRC_MESH, field))
    name = DST_TEX.encode("ascii")
    data[TEX_NAME_OFFSET:TEX_NAME_OFFSET + TEX_NAME_SIZE] = name + b"\0" * (TEX_NAME_SIZE - len(name))
    path = os.path.join(OUT, "Meshes", "Objects", DST_MESH)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "wb").write(bytes(data))
    print("%s : texture %s -> %s (%d octets)" % (DST_MESH, SRC_TEX, DST_TEX, len(data)))


def make_texture():
    src = Image.open(os.path.join(PRISTINE, "Textures", SRC_TEX)).convert("RGBA")
    path = os.path.join(OUT, "Textures", DST_TEX)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    recolor_missile(src).save(path)
    print("%s : %dx%d" % (DST_TEX, src.width, src.height))


if __name__ == "__main__":
    make_mesh()
    make_texture()
