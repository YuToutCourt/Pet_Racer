"""Essai de l'installeur sur une fausse copie du jeu : installe, met à jour, désinstalle, et compare.

    powershell -ExecutionPolicy Bypass -File mods/package.ps1
    python mods/installer/test_installer.py
"""
import filecmp
import hashlib
import os
import shutil
import subprocess
import sys
import zipfile

root = os.path.abspath(sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
pristine = os.path.join(root, "game_pristine")
dev = os.path.join(root, "game_dev")
data = os.path.join(root, "mods", "data")
pkg_zip = os.path.join(root, "build", "PetRacerMod.zip")
work = os.path.join(root, "build", "installer-test")
install_root = os.path.join(work, "Pet Racer")  # un espace dans le chemin, comme une vraie installation
game = os.path.join(install_root, "game")        # l'installeur d'origine met le jeu dans « Pet Racer\game »
pkg = os.path.join(work, "PetRacerMod")
checks = 0
sys.stdout.reconfigure(errors="replace")


def check(what, ok):
    global checks
    checks += 1
    print(("OK    " if ok else "ECHEC ") + what)
    if not ok:
        raise SystemExit(1)


def sha(path):
    with open(path, "rb") as f:
        return hashlib.sha256(f.read()).hexdigest()


def rel_files(top):
    out = []
    for d, _, files in os.walk(top):
        out += [os.path.relpath(os.path.join(d, f), top) for f in files]
    return sorted(out)


def snapshot(top):
    return {f: sha(os.path.join(top, f)) for f in rel_files(top)}


def run(*args, game_arg=None):
    cmd = ["powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", os.path.join(pkg, "install.ps1"),
           "-Game", game_arg or game, "-Quiet"] + list(args)
    res = subprocess.run(cmd, capture_output=True)
    return res.returncode, res.stdout.decode("cp850", "replace") + res.stderr.decode("cp850", "replace")


shutil.rmtree(work, ignore_errors=True)
os.makedirs(game)
with zipfile.ZipFile(pkg_zip) as z:
    names = sorted(z.namelist())
    z.extractall(work)  # comme l'ami qui décompresse le zip
mod_data = rel_files(data)
mod_files = rel_files(os.path.join(pkg, "fichiers"))
replaced_data = [f for f in mod_data if os.path.exists(os.path.join(pristine, f))]

# la fausse copie : l'exe, tout java\ d'origine, les données que le mod remplace, et des sauvegardes du joueur
shutil.copy2(os.path.join(pristine, "PetRacer.exe"), game)
shutil.copytree(os.path.join(pristine, "java"), os.path.join(game, "java"))
for f in replaced_data:
    os.makedirs(os.path.dirname(os.path.join(game, f)), exist_ok=True)
    shutil.copy2(os.path.join(pristine, f), os.path.join(game, f))
for save in ("progress.cfg", "mods.dat", "online.dat", "grandprix.dat"):
    with open(os.path.join(game, save), "w") as f:
        f.write("sauvegarde du joueur")
before = snapshot(game)

check("le paquet ne contient ni sauvegarde, ni compte, ni journal, ni nettest.scr",
      not [n for n in names if (n.lower().endswith((".dat", ".cfg")) and "/Data/" not in n)
           or n.lower().endswith((".log", ".db")) or "nettest" in n.lower()])
check("le paquet a l'installeur, le LISEZMOI et les empreintes des originaux",
      all("PetRacerMod/" + n in names for n in ("Installer.bat", "Desinstaller.bat", "install.ps1", "LISEZMOI.txt",
                                                "mod.txt", "originaux.txt")))
with open(os.path.join(pkg, "install.ps1"), "rb") as f:
    check("install.ps1 en UTF-8 avec BOM (accents lisibles par PowerShell 5)", f.read(3) == b"\xef\xbb\xbf")

code, out = run("-Server", "exemple.duckdns.org", "-Port", "7780", game_arg=install_root)  # dossier parent donné
print(out.strip())
check("installation réussie (dossier « Pet Racer » donné, « game » trouvé dedans)", code == 0)
check("pas d'avertissement de version : les originaux sont ceux de game_pristine", "ATTENTION" not in out)
check("java\\ identique à celui de game_dev (%d classes)" % len(rel_files(os.path.join(game, "java"))),
      snapshot(os.path.join(game, "java")) == snapshot(os.path.join(dev, "java")))
same = [f for f in mod_data if f != os.path.join("Data", "online.scr")
        and filecmp.cmp(os.path.join(game, f), os.path.join(data, f), False)]
check("les %d fichiers de données du mod sont en place" % (len(mod_data) - 1), len(same) == len(mod_data) - 1)
with open(os.path.join(game, "Data", "online.scr"), "rb") as f:
    online = f.read()
with open(os.path.join(data, "Data", "online.scr"), "rb") as f:
    source = f.read()
check("adresse du serveur écrite dans online.scr, le reste du fichier (fins de ligne comprises) inchangé",
      online != source and online == source.replace(b'Server("127.0.0.1", 7778)', b'Server("exemple.duckdns.org", 7780)'))
backup = os.path.join(game, "mod_backup")
replaced = [f for f in mod_files if f in before]
check("les %d fichiers d'origine remplacés (classes et données) sont gardés dans mod_backup" % len(replaced),
      all(sha(os.path.join(backup, f)) == before[f] for f in replaced)
      and rel_files(backup) == sorted(replaced + ["fichiers_du_mod.txt"]))
check("les sauvegardes du joueur ne sont pas touchées",
      all(sha(os.path.join(game, s)) == before[s] for s in ("progress.cfg", "mods.dat", "online.dat", "grandprix.dat")))
ai_start = os.path.join(data, "Data", "aimap_depart.dat")
ai_map = os.path.join(game, "aimap.dat")
check("pas encore de mémoire des IA : celle de départ du mod est copiée en aimap.dat",
      os.path.exists(ai_start) and filecmp.cmp(ai_map, ai_start, False) and "aimap.dat" not in before)

code, out = run()  # mise à jour : même paquet, sans redonner l'adresse
check("mise à jour réussie", code == 0)
with open(os.path.join(game, "Data", "online.scr"), "rb") as f:
    check("mise à jour : l'adresse du serveur déjà réglée est gardée", b'Server("exemple.duckdns.org", 7780)' in f.read())
check("mise à jour : mod_backup garde les originaux, pas les fichiers du mod",
      all(sha(os.path.join(backup, f)) == before[f] for f in replaced)
      and rel_files(backup) == sorted(replaced + ["fichiers_du_mod.txt"]))

# nouvelle version du mod qui n'a plus une classe ajoutée ni une classe remplacée : l'une part, l'autre est remise
added_cls = os.path.join("java", "ModNet.class")
replaced_cls = os.path.join("java", "RacerGame.class")
check("le paquet ajoute ModNet.class et remplace RacerGame.class", added_cls not in before and replaced_cls in before)
for f in (added_cls, replaced_cls):
    shutil.move(os.path.join(pkg, "fichiers", f), os.path.join(work, os.path.basename(f)))
code, out = run()
check("mise à jour vers un paquet plus petit réussie", code == 0 and "2 fichier(s) de l'ancienne version" in out)
check("classe que le mod n'a plus : retirée ; classe qu'il ne remplace plus : remise d'origine",
      not os.path.exists(os.path.join(game, added_cls)) and sha(os.path.join(game, replaced_cls)) == before[replaced_cls]
      and not os.path.exists(os.path.join(backup, replaced_cls)))
for f in (added_cls, replaced_cls):
    shutil.move(os.path.join(work, os.path.basename(f)), os.path.join(pkg, "fichiers", f))
code, out = run()
check("réinstallation du paquet complet : java\\ de nouveau identique à game_dev",
      code == 0 and snapshot(os.path.join(game, "java")) == snapshot(os.path.join(dev, "java")))

# jeu d'une autre édition : un fichier remplacé différent -> avertissement (sans question en -Quiet)
code, out = run("-Uninstall")
check("désinstallation réussie", code == 0)
print(out.strip())
after = snapshot(game)
check("mémoire des IA laissée en place à la désinstallation", after.pop("aimap.dat", None) == sha(ai_start))
check("le reste du dossier du jeu est revenu exactement comme avant (%d fichiers)" % len(after), after == before)
with open(ai_map, "w") as f:
    f.write("appris chez l'ami")
learned = sha(ai_map)
code, out = run("-Uninstall")
check("désinstaller deux fois : refusé proprement", code == 1 and "pas install" in out)

texts = os.path.join(game, "Data", "Texts.scr")
with open(texts, "ab") as f:
    f.write(b"\r\n/* modifie */\r\n")
code, out = run()
check("jeu modifié à la main : l'installation prévient (ATTENTION … Texts.scr)", code == 0 and "ATTENTION" in out and "Texts.scr" in out)
check("mémoire des IA de l'ami : jamais écrasée par celle de départ", sha(ai_map) == learned)
code, out = run("-Uninstall")
with open(texts, "rb") as f:
    check("…et la désinstallation remet le fichier tel qu'il était", code == 0 and f.read().endswith(b"/* modifie */\r\n"))

code, out = run(game_arg=os.path.join(work, "pas un jeu"))
check("mauvais dossier : refusé proprement", code == 1 and "n'est pas dans" in out)

shutil.rmtree(work, ignore_errors=True)
print("%d vérifications réussies" % checks)
