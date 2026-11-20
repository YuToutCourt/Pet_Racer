# Développement du mod — Pet Racer

Moteur natif ChromeEngine (Techland, 2002, version plus ancienne que Pet Soccer) + logique de jeu en Java (JRE 1.x embarqué).
Même méthode que `..\pet_soccer`.

## Différences avec Pet Soccer
- `PetRacer.exe` est le vrai exécutable (pas de `.bum`) ; il lit `game.ini` (`JavaGameClassName("RacerGame")`).
- **Pas de `code.pak` ni de `code_inf.pak`** : les classes sont en vrac dans `game\java\` et la JVM les charge depuis
  ce dossier. Pour moder, on dépose les `.class` recompilés dans `game_dev\java\`.

## Organisation
| Dossier | Contenu |
|---|---|
| `game_pristine\` | copie intacte de `C:\Jeux\Pet Racer\game` (référence, ne jamais modifier) |
| `game_dev\` | copie de test, reçoit les mods |
| `mods\java\` | classes Java modifiées (sources, changements marqués `// [mod]`) |
| `mods\data\` | fichiers de données copiés tels quels dans `game_dev\` |
| `decompiled\` | `game_pristine\java\*.class` décompilés par Vineflower (lecture seule) |
| `tools\` | jonction vers `..\pet_soccer\tools` (JDK 8, JDK 21, Ghidra, Vineflower) |

## Build
```powershell
powershell -ExecutionPolicy Bypass -File mods\build.ps1           # build
powershell -ExecutionPolicy Bypass -File mods\build.ps1 -Run      # build + lancement
powershell -ExecutionPolicy Bypass -File mods\build.ps1 -Clean    # recopie game_dev depuis game_pristine, puis build
powershell -ExecutionPolicy Bypass -File mods\build.ps1 -Second   # build + copie dans game_dev_b (2e jeu pour l'en ligne)
powershell -ExecutionPolicy Bypass -File mods\online-test.ps1      # -Second + les deux jeux en fenêtré (notes/online.md)
powershell -ExecutionPolicy Bypass -File mods\online-test.ps1 -Screen  # + serveur, écran « En ligne » à la main
powershell -ExecutionPolicy Bypass -File mods\package.ps1 -Server adresse  # zip pour les amis (notes/online.md, phase 5)
```
1. remet `game_dev\java\` à l'identique de l'original ;
2. compile `mods\java\*.java` avec `tools\jdk8` (`-source 1.3 -target 1.3`, contre `jre\lib\rt.jar` du jeu et `game_pristine\java`) ;
3. copie les `.class` produits dans `game_dev\java\` ;
4. superpose `mods\data\` sur `game_dev\`.

**Modifier une classe** : copier `decompiled\<Classe>.java` dans `mods\java\`, l'éditer, relancer le build.

Redécompiler : `tools\jdk21\bin\java -jar tools\vineflower.jar game_pristine\java decompiled`

## Mods actuels
| Mod | Fichiers |
|---|---|
| Résolutions modernes (liste lue sur l'écran, 1080p, 1440p, 4K) et écran large (menus 4:3 centrés, HUD aux bords) | `ModVideo`, `Window`, `GameSerializeSettings`, `KidsAudioVideoMenuWindow`, `KidsSlider`, `CarsManager`, `KidsText`, `Fonts`, `KidsCursor`, `KidsDuringGameCursor`, `MovieModule`, `ModSideFill` (+ `KidsAnimalChoiceMenuWindow`, `KidsPlacesMenuWindow`, `KidsBossTauntMenuWindow`) ; `MenuAudioVideo1024.png`, `MenuCharakterLeft/Right.png` (`mods/art/`) — `notes/resolution.md` |
| PC moderne : dérapage indépendant du framerate, limiteur d'images (`mods.dat`, 144 par défaut), `System.gc()` 1×/s, pas maxi 0,05 s, compte-tours et anti-rebond des menus en temps, `fps=`, bouton J2 du menu Contrôles, niveau des IA, ordre d'arrivée et records, sons du pont de pierre et du choix du profil ; images/s dans `crash.log` | `ModFrameLimiter`, `ModOptions`, `RacerGame`, `RacerModule`, `Vehicle`, `VehicleParams`, `RaceParams`, `KidsRotometer`, `KidsMenuModule`, `KidsControlsMenuWindow` ; `Data\ChoosePlayer.scr` — `notes/pc_moderne.md` |
| Menu « Mods » (bouton engrenage du menu principal) : déblocages non destructifs (animaux, circuits, extras), compteur FPS, limite d'images, élastique des IA, triches (vitesse, bonus infinis, bonus imposé, protection), étape d'aventure ; triche = pas de record, « Triche » au podium | `ModOptions`, `KidsModsMenuWindow`, `ModText`, `KidsSimpleMenuModule`, `KidsMainMenuWindow`, `RacerGame`, `CoreLogic`, `KidsAnimalChoiceMenuWindow`, `KidsMap(Lands/Tracks)MenuWindow`, `KidsExtraMenuWindow`, `KidsHud`, `RacerModule`, `Vehicle`, `KidsPlacesMenuWindow` ; `Data\Texts.scr`, `Mods*.png` (`mods/art/make_texts.py`, `make_mods_menu.py`) — `notes/mods_menu.md` |
| Stats des animaux : `Data\animals.scr` (accélération / vitesse / virage, humains et IA) + cadre nom et 3 barres à l'écran de choix | `ModAnimals`, `RacerGame`, `KidsAnimalChoiceMenuWindow` ; `Data\animals.scr`, `Data\Texts.scr`, `Stats*.png` (`mods/art/make_animal_stats.py`) — `notes/animaux.md` |
| Bonus : Fusée jouable (icône 128x128, vitesse, ajoutée à tous les mondes), case libérée pendant turbo / pointes / protection, pistolet à eau contre la protection, journal de la bombe ; tirage selon la place (`Data\bonuses.scr`) ; turbo laissé d'origine, Rocher abandonné | `ModBonuses`, `LevelBonusTypes`, `AnimalBonusTypes`, `Rocket`, `Bomb`, `Vehicle`, `RacerModule`, `RacerGame` ; `Data\bonuses.scr`, `Hud\Rakieta.png` (`mods/art/make_hud_icons.py`) — `notes/bonus.md` |
| Grand Prix : bouton du menu Un joueur, coupe de 4 courses tirées au hasard (monde puis circuit, Royaume du Lion compris), points 10/8/6/4/2/1 (`Data\grandprix.scr`), classement après chaque course, palmarès par profil (`grandprix.dat`) | `ModGrandPrix`, `KidsGrandPrixMenuWindow`, `KidsSinglePlayerMenuWindow`, `KidsAnimalChoiceMenuWindow`, `KidsPlacesMenuWindow`, `KidsSimpleMenuModule`, `RacerGame` ; `Data\grandprix.scr`, `Data\Texts.scr`, `GrandPrixButton*.png`, `GPPanel.png` (`mods/art/make_grandprix.py`) — `notes/grandprix.md` |
| IA : trajectoire à la corde, regard vers le point suivant, freinage avant les virages, volant progressif, évitement, bonus utilisés à bon escient, caractère par IA, dégagement + remise en piste, élastique doux selon la difficulté ; « Difficulté des IA » (d'origine / auto / facile / normal / difficile / expert) dans le menu Mods ; bilan par IA dans `crash.log` | `VehicleAI`, `ModAI`, `ModOptions`, `KidsModsMenuWindow`, `RaceParams`, `RacerModule` ; `Data\Texts.scr` — `notes/ia.md` |
| Barre de progression du HUD (à gauche) : un morceau par tour de 1 à 10 tours, séparations sur les passages de ligne (l'original n'avait d'image que pour 3 et 7 tours et montrait 7 morceaux sinon, même pour 5) | `KidsDistance` ; `Hud\ModPasek_1.png` … `ModPasek_10.png` (`mods/art/make_lap_bars.py`) — `notes/online.md` |
| En ligne (chantier 8, en cours) — phase 0 : socket UDP Java 1.3 (fil lecteur), pings, `build.ps1 -Second` / `online-test.ps1` ; phase 1 : course à 2 « fantômes » en direct (horloge commune, top départ, classement aux temps) ; phase 2 : bonus en ligne (HIT, pièges recopiés, explosions, OVNI) et chocs entre voitures ; phase 3 (en cours) : serveur à salons de 6 places et relais, course à 2 par le serveur (`online-test.ps1 -Server`), boîtes bonus partagées, spectateur, écran « En ligne » (bouton du menu principal : créer / rejoindre par code, animal par l'écran classique du choix (modèle 3D, stats), prêt, circuit / tours / bonus de l'hôte, courses à la suite, hôte remplacé entre deux courses ; `online-test.ps1 -Screen`), IA de l'hôte (lignes « IA » et « Niveau des IA », protocole v6) et hôte remplacé en pleine course (un joueur reprend relais et IA) ; phase 4 : partie rapide (file d'attente du serveur, salon classé lancé seul, réglages de `Data\online.scr`), Elo par paires d'humains, classement (10 premiers), triches coupées en ligne (`ModNet` v7, dialogue v2) ; phase 5 : installeur pour les amis (`mods\package.ps1` → `build\PetRacerMod.zip`) | `ModOnline`, `ModUdp`, `ModBuf`, `ModAddr`, `ModNet`, `ModLobby`, `ModAI`, `ModOptions`, `VehicleAI`, `KidsOnlineMenuWindow`, `KidsAnimalChoiceMenuWindow`, `KidsMainMenuWindow`, `KidsSimpleMenuModule`, `KidsPlacesMenuWindow`, `BonusItem`, `KidsHud`, `RacerGame`, `RacerModule`, `RaceParams`, `Vehicle`, `ExitDuringGameWindow`, pièges `Banana` `OilCan` `WaterBottle` `Anvil` `Catapult` `FunnyGasBox`, `Bomb`, `WaterMelon` ; `Data\online.scr`, `Data\Texts.scr`, `OnlineButton*.png`, `OnlinePanel.png` (`mods/art/make_online.py`), `nettest.scr` (racine de chaque jeu), `server\` (`server.py`, `rooms.py`, `protocol.py`, `store.py`, `test_server.py`, `echo_test.py`) — `notes/online.md` |

Feuille de route complète : `notes/roadmap.md`.

## Repères
- Réglages du mod : `game_dev\mods.dat` (`FpsLimit`, `AllAnimals`, `Speed`…, voir `notes/mods_menu.md`), lu au démarrage par
  `ModOptions` (créé s'il manque), modifié par l'écran « Mods ».
- Codes de triche d'origine (`RacerGame.CheckStringCheats`) : `PRSUPER` les active, puis `ANIMALS`, `TRACKS`, `BONUSES`
  (cochent maintenant les déblocages du menu Mods), `L11`…`L63` (étape d'aventure).
- Textes à l'écran depuis le Java : jeton `&Clé&` de `Data\Texts.scr` passé à `KidsText`/`ModText` (traduit par le
  moteur). Ajouter les textes dans `mods/art/make_texts.py`.
- Accents : les polices `Data\Panel\MS Sans Serif<N>.dat` n'avaient que l'ASCII + des lettres polonaises (même le jeu
  d'origine perdait les é des crédits). `mods/art/make_fonts.py` ajoute é è ê ë à â ç î ï ô ù û ü É È Ê À Ç aux tailles
  16/18/20/26/32/40 (glyphes composés depuis la police elle-même). 32 et 40 : créées par le moteur, sources gardées
  dans `mods/art/font_src`. Autre caractère accentué à afficher → l'ajouter dans `RECIPES`.
- Fenêtres enfants d'un menu : ajoutées après une image, elles passent quand même dessous. Remonter textes et boutons
  avec `m_List.SetOnTop` dans `ButtonsOnTop` (comme `KidsControlsMenuWindow`).
- `CoreLogic.m_nAvailableTracks` (1 → 31) / `m_nAvailableAnimals` (6 → 12) : progression.
- Diagnostic : `game_dev\crash.log`. Avertissements normaux : `data/vision.scr` manquant, `lod count != mesh lod count`, sons `highlight`/`click`.
- Voix des animaux : les 12 dossiers `Data\Sounds\Voices\*` sont vides dans ce repack (installeur NSIS 2.38) ; les `[LoadWave] cannot open` des voix sont normaux.
