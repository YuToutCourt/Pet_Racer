"""Prépare les captures d'écran du README (docs/screens/*.jpg).

Usage :
  python mods\\art\\captures.py                 # prend les 7 dernières captures de Images\\Captures d'écran,
                                               # dans l'ordre où elles ont été prises (voir NOMS)
  python mods\\art\\captures.py fichier.png menu  # convertit une seule capture sous un nom donné

Chaque image est réduite à 1600 px de large au plus et enregistrée en JPEG (qualité 85).
"""
import os
import sys
from pathlib import Path

from PIL import Image

# Ordre dans lequel prendre les captures en jeu.
NOMS = ["course", "menu", "online", "animaux", "grandprix", "missile", "mods"]
LARGEUR_MAX = 1600

RACINE = Path(__file__).resolve().parents[2]
SORTIE = RACINE / "docs" / "screens"


def dossier_captures():
    images = Path(os.environ.get("USERPROFILE", "~")).expanduser() / "Pictures"
    for nom in ("Screenshots", "Captures d'écran", "Captures d’écran"):
        if (images / nom).is_dir():
            return images / nom
    sys.exit("Dossier des captures introuvable : donne le fichier en argument.")


def convertir(source, nom):
    im = Image.open(source).convert("RGB")
    if im.width > LARGEUR_MAX:
        im = im.resize((LARGEUR_MAX, round(im.height * LARGEUR_MAX / im.width)), Image.LANCZOS)
    SORTIE.mkdir(parents=True, exist_ok=True)
    cible = SORTIE / (nom + ".jpg")
    im.save(cible, quality=85, optimize=True, progressive=True)
    print(f"{Path(source).name} -> {cible.relative_to(RACINE)} ({im.width}x{im.height}, {cible.stat().st_size // 1024} Ko)")


def main():
    if len(sys.argv) == 3:
        convertir(sys.argv[1], sys.argv[2])
        return
    dossier = dossier_captures()
    captures = sorted(dossier.glob("*.png"), key=lambda p: p.stat().st_mtime)[-len(NOMS):]
    if len(captures) < len(NOMS):
        sys.exit(f"Il faut {len(NOMS)} captures dans {dossier}, trouvé {len(captures)}.")
    for source, nom in zip(captures, NOMS):
        convertir(source, nom)


if __name__ == "__main__":
    main()
