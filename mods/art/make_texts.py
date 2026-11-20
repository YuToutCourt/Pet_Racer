# Data\Texts.scr du mod : le fichier d'origine + les textes du mod, à la suite.
# Le jeu lit ce fichier en Windows-1252 et le moteur remplace « &Clé& » par le texte dans KidsText / ModText
# (comme les crédits) : c'est le seul chemin sûr pour afficher des accents depuis le Java.
# Usage : python mods/art/make_texts.py  ->  mods/data/Data/Texts.scr
import os

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "..", "..", "game_pristine", "Data", "Texts.scr")
OUT = os.path.join(HERE, "..", "data", "Data", "Texts.scr")

# Bonus imposé : identifiants de LevelBonusTypes (0..20, 21 = Missile du mod)
BONUSES = [
    "Bombe", "Pistolet à bouchon", "Catapulte", "Turbo", "Protection", "Bidon d'huile", "Gaz hilarant",
    "Bouteille d'eau", "Banane", "Sac de guêpes", "Enclume", "Pneus à pointes", "Lance-tomates", "Pastèque",
    "Pistolet à glace", "Pistolet à eau", "Noix de coco", "Boule de neige", "Cookie", "Fusée", "OVNI",
    "Missile",
]

# Noms des animaux à l'écran, dans l'ordre des identifiants du jeu (MenuAnimal._PANTHER = 0 … _LION = 11)
ANIMALS = [
    "Panthère", "Dino", "Poulet", "Grenouille", "Tortue", "Requin", "Taureau", "Girafe", "Ours polaire", "Gorille",
    "Robot", "Lion",
]

# Circuits du Grand Prix, dans l'ordre de RacerModule.MAPNAMES : par monde, circuits 1 à 3 à l'aller (Track.map)
# puis les mêmes au retour (Track2.map) ; le n° 30 est le circuit unique du Royaume du Lion
WORLDS = ["Forêt", "Désert", "Hiver", "Tropiques", "Lune"]
TRACKS = ["%s %d%s" % (w, c + 1, " (retour)" if back else "") for w in WORLDS for back in (0, 1) for c in range(3)] \
    + ["Royaume du Lion"]
assert len(TRACKS) == 31

MOD_TEXTS = [
    # écran Mods (KidsModsMenuWindow)
    ("ModsAllAnimals", "Tous les animaux"),
    ("ModsAllTracks", "Tous les circuits"),
    ("ModsAllExtras", "Toutes les courses extra"),
    ("ModsShowFps", "Compteur FPS en course"),
    ("ModsFpsLimit", "Limite d'images par seconde"),
    ("ModsNoLimit", "sans limite"),
    ("ModsCatchUp", "Rattrapage des IA (élastique)"),
    ("ModsAiLevel", "Difficulté des IA"),
    ("ModsAi0", "d'origine"),
    ("ModsAi1", "auto"),
    ("ModsAi2", "facile"),
    ("ModsAi3", "normal"),
    ("ModsAi4", "difficile"),
    ("ModsAi5", "expert"),
    ("ModsSpeed", "Vitesse (triche)"),
    ("ModsInfiniteBonus", "Bonus infinis (triche)"),
    ("ModsForcedBonus", "Bonus imposé (triche)"),
    ("ModsShield", "Protection permanente (triche)"),
    ("ModsAdventure", "Aventure : aller à l'étape"),
    ("ModsAdventureNone", "ne pas changer"),
    ("ModsYes", "oui"),
    ("ModsNo", "non"),
    ("ModsBonusRandom", "au hasard"),
    ("ModsCheatOn", "Triche active : les records ne comptent pas"),
    ("ModsCheatOff", "Aucune triche : les records comptent"),
    # podium (KidsPlacesMenuWindow)
    ("ModsCheatPodium", "Triche : record non compté"),
    # écran de choix de l'animal (KidsAnimalChoiceMenuWindow, chantier 4)
    ("StatSpeed", "Vitesse"),
    ("StatAccel", "Accélération"),
    ("StatTurn", "Maniabilité"),
    # classement du Grand Prix (KidsGrandPrixMenuWindow, chantier 6)
    ("GPHeadPlace", "Place"),
    ("GPHeadAnimal", "Pilote"),
    ("GPHeadRace", "Course"),
    ("GPHeadTotal", "Total"),
    ("GPNext", "Prochaine course :"),
    ("GPFinal0", "Victoire ! Coupe d'or"),
    ("GPFinal1", "2e place : coupe d'argent"),
    ("GPFinal2", "3e place : coupe de bronze"),
    ("GPFinal3", "Pas de coupe cette fois"),
    ("GPMedals", "Ton palmarès :"),
    ("GPCheat", "Triche active : coupe non comptée"),
    ("GPAbandoned", "Course quittée : 0 point"),
    ("GPQuitConfirm", "Retour encore une fois pour abandonner la coupe"),
    # écran « En ligne » (KidsOnlineMenuWindow, chantier 8)
    ("OnlMode", "Que faire ?"),
    ("OnlCreate", "Créer un salon"),
    ("OnlJoin", "Rejoindre un salon"),
    ("OnlCode", "Code du salon (tape-le)"),
    ("OnlAnimal", "Ton animal"),
    ("OnlReadyRow", "Prêt"),
    ("OnlTrack", "Circuit"),
    ("OnlLaps", "Tours"),
    ("OnlBonus", "Boîtes bonus"),
    ("OnlAi", "IA (places libres)"),
    ("OnlAiLevel", "Niveau des IA"),
    ("OnlAiAnimal", "au hasard"),
    ("OnlHelp1", "Crée un salon et donne son code à tes amis,"),
    ("OnlHelp2", "ou tape le code de leur salon pour le rejoindre."),
    ("OnlHost", "hôte"),
    ("OnlReady", "prêt"),
    ("OnlNotReady", "pas prêt"),
    ("OnlJoining", "arrive..."),
    ("OnlBusy", "pas revenu"),
    ("OnlNoNet", "Réseau indisponible (port UDP refusé)"),
    ("OnlNoServer", "Le serveur ne répond pas"),
    ("OnlConnecting", "Connexion au serveur..."),
    ("OnlEntering", "Entrée dans le salon..."),
    ("OnlErr1", "Code inconnu"),
    ("OnlErr2", "Salon plein"),
    ("OnlErr3", "Ce salon a une autre version du mod"),
    ("OnlErr4", "Course en cours dans ce salon : réessaie après"),
    ("OnlGo", "Tout le monde est prêt : OK pour lancer"),
    ("OnlAlone", "Seul dans le salon : donne le code à tes amis"),
    ("OnlWaitReady", "En attente : tout le monde doit être prêt"),
    ("OnlWaitHost", "Prêt ! L'hôte va lancer la course"),
    ("OnlPressReady", "Choisis ton animal, puis OK quand tu es prêt"),
    # partie rapide classée et classement (chantier 8, phase 4)
    ("OnlQuick", "Partie rapide"),
    ("OnlTop", "Classement"),
    ("OnlHelpQuick1", "Course classée contre des joueurs de ton niveau ;"),
    ("OnlHelpQuick2", "les places libres sont prises par des IA."),
    ("OnlHelpTop1", "Les meilleurs joueurs des parties rapides :"),
    ("OnlHelpTop2", "OK pour voir le classement."),
    ("OnlTopEmpty", "Personne n'est encore classé"),
    ("OnlRankedStart", "Partie rapide trouvée : départ dans quelques secondes"),
    ("OnlRankedCancel", "Partie rapide annulée : la course n'a pas pu partir"),
    ("OnlRatedWait", "Course classée : on attend le résultat des autres"),
    ("OnlRatedVoid", "Course pas comptée (résultats différents)"),
] + [("ModsBonus%d" % i, name) for i, name in enumerate(BONUSES)] \
  + [("AnimalName%d" % i, name) for i, name in enumerate(ANIMALS)] \
  + [("GPTrack%d" % i, name) for i, name in enumerate(TRACKS)]


def main():
    with open(SRC, "rb") as f:
        data = f.read()
    if not data.endswith(b"\r\n"):
        data += b"\r\n"
    # pas de ligne de commentaire : on ne sait pas si l'analyseur natif de Text.FromFile accepte « // »
    lines = []
    for key, text in MOD_TEXTS:
        assert '"' not in text, key
        lines.append('String("%s", "%s")' % (key, text))
    data += "\r\n".join(lines).encode("cp1252") + b"\r\n"
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "wb") as f:
        f.write(data)
    print("->", os.path.normpath(OUT), "(%d textes du mod)" % len(MOD_TEXTS))


if __name__ == "__main__":
    main()
