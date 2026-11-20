# Feuille de route — Pet Racer

## Contexte
Pet Racer (Techland 2002, ChromeEngine + Java) est modé comme Pet Soccer : classes recompilées dans `mods\java\`
(marques `// [mod]`), données dans `mods\data\`, `mods\build.ps1` → `game_dev\`. Le test « vitesse x1,5 » a prouvé
que la recompilation marche. Objectif : un mod complet pour PC moderne + contenu + multijoueur en ligne avec Elo,
en réutilisant au maximum ce qui existe dans `..\pet_soccer` (menu Mods, Elo, serveur Python, réseau UDP).

Coût : ★ = quelques heures, ★★ = une session, ★★★ = plusieurs sessions, ★★★★ = gros chantier.

## Ordre proposé
| # | Chantier | Coût | Dépend de |
|---|---|---|---|
| 1 | ~~Résolutions modernes + écran large (menus 4:3 centrés, HUD aux bords)~~ — fait, à valider (`notes/resolution.md`) | ★★★ | — |
| 2 | ~~Corrections PC moderne (limiteur d'images, pas maxi, petits bugs)~~ — fait, validé (`notes/pc_moderne.md`) | ★★ | — |
| 3 | ~~Menu « Mods » (déblocages, FPS, triches)~~ — fait, à valider (`notes/mods_menu.md`) | ★★ | 2 (limiteur) |
| 4 | ~~Stats des animaux (`animals.scr`) + barres au choix du perso~~ — fait, à valider (`notes/animaux.md`) | ★★ | — |
| 5 | ~~Bonus : correctifs, tirage selon la place, fusée réactivée~~ — fait, à valider (`notes/bonus.md`) ; rocher abandonné | ★★ | 4 |
| 6 | ~~Grand Prix : une coupe de 4 courses au hasard, points par place~~ — fait, à valider (`notes/grandprix.md`) | ★★★ | 2 (ordre d'arrivée) |
| 7 | Manette (DirectInput : gâchettes, croix, menus) + remappage | ★★ | — | (SKIP CAR J'AI PAS ENCORE DE MANETTE POUR TESTER)
| 8 | Multijoueur en ligne : fantômes, IA de l'hôte, Elo — en cours, phases 0 et 1 validées, phase 2 (bonus + chocs) faite, chocs validés, phase 3 en cours : serveur, course par le serveur, boîtes partagées validés ; écran « En ligne », courses à la suite, choix de l'animal et barre de progression à N tours validés ; IA de l'hôte et hôte remplacé en pleine course faits, à valider ; phase 4 (partie rapide, Elo multi, classement) faite, à valider (`notes/online.md`) | ★★★★ | tout le reste figé |
| — | ~~Sons : `highlight` / `click` (fichiers dans `Sounds\Demo\`)~~ — fait avec le chantier 2 | ★ | — (priorité basse) |

Chaque chantier : sa note `notes\<chantier>.md` (comme pet_soccer), une ligne dans `notes\development.md`,
build par `mods\build.ps1`, essai en jeu, puis validation par toi avant le suivant.
Infra commune : `ModOptions` + `mods.dat` (texte `Clé=valeur`, repris de pet_soccer), lus au démarrage dans
`RacerGame.OnPreInitialize` — posé au chantier 2 (clé `FpsLimit`). Journal : `GameObject.CrashLog("[mod] …")` direct,
sans accents (pas de classe `ModLog`).

## 1. Résolutions modernes + écran large
Constats :
- La limite 4:3 est **seulement dans le Java** : le moteur prend n'importe quelle taille
  (`GameObject.SetResolution(w,h)` + `ApplyVideoSettings()`, `EnumResolution`, `IsResolutionPresent` dispo mais
  inutilisés).
- La liste est recopiée 4 fois : `Settings.GetResolutionWidth/Height` + `LoadSettings`,
  `KidsAudioVideoMenuWindow` (2 `switch`), `GameSerializeSettings` (démarrage : tout index ≠ 1/2 → 640×480).
- `progress.cfg` (index de résolution) **écrase** `VideoSettings.scr` au lancement.
- HUD et menus en coordonnées 0–1 étirées sur l'écran : en 16:9 tout s'élargit (cadrans ovales, fonds étirés).
- Pièges : `KidsText.m_Fonts = {16,18,20}` indexé par la résolution (index ≥ 3 = plantage) ;
  `CarsManager`/`AnimCar`/`CarLine` prennent la taille dans la table (résolution inconnue = largeur 0) ;
  sous-titres en pixels 1024×768 (`Module`) ; curseur 64×64 px ; polices bitmap à taille fixe (petites en 1440p).

Plan :
1. `ModVideo` (nouvelle classe) : **une seule** table de résolutions, construite depuis `EnumResolution` (modes
   réels de l'écran) + liste de secours (640×480 … 2560×1440, 4:3/16:9/16:10/21:9). Remplace les 4 `switch`.
   La résolution est gardée en largeur×hauteur (dans `mods.dat`), plus en index ; `GameSerializeSettings` ne
   l'écrase plus.
2. Menu Audio/Vidéo : le curseur à 3 crans devient une ligne « < 1920×1080 > » (les libellés sont peints dans
   `MenuAudioVideo1024.png` → nouveau fond ou texte par-dessus).
3. Écran large — règle unique dans `Window.SetSizeAndPos` : les fenêtres de menu et le HUD sont placés dans une
   **zone 4:3 centrée** (largeur = hauteur × 4/3), sauf les éléments marqués « plein écran » (fonds, fondus).
   Les fonds de menu sont étirés ou complétés par des bandes ; les gadgets du HUD (vitesse, compte-tours, place,
   tours) sont ancrés aux bords réels de l'écran pour ne pas rester serrés au centre.
4. Polices : `KidsText` et `Fonts` choisissent la taille d'après la hauteur réelle (16/18/20 → 24/28 en 1080p+).
5. ~~Caméra~~ : vérifié, rien à faire. Le moteur garde le champ vertical et élargit l'image en 16:9.
6. ~~Écran partagé vertical~~ : écarté, il réduirait le champ de vision ; on garde le partage haut / bas.

Premier essai (petit) : `GameSerializeSettings` ne réapplique plus l'index de `progress.cfg`, et
`Resolution(1920,1080)` dans `VideoSettings.scr` → voir le rendu 3D, le HUD étiré et le FOV avant de tout refaire.

## 2. Corrections PC moderne — fait, validé (détails : `notes/pc_moderne.md`)
La physique des voitures est en Java et suit bien le temps réel (~60 calculs `× m_fFrameTime` dans `Vehicle`).
Pas de jeu qui accélère à 300 FPS. Les défauts trouvés, tous traités :

| Défaut | Où | Correctif |
|---|---|---|
| Pas de limite d'images, pas de VSync | moteur | `ModFrameLimiter` appelé par `RacerGame.OnFrame`, `FpsLimit` dans `mods.dat` (144 par défaut) |
| `System.gc()` à chaque image | `RacerModule.OnFrame` | une fois par seconde |
| Pas de pas maxi | `RacerModule.OnFrame` | `m_fFrameTime` plafonné à 0,05 s |
| Dérapage ajouté tel quel à chaque image | `Vehicle.MoveVehicle` | `× m_fFrameTime × 30` |
| Aiguille du compte-tours `MAX_DELTA = 0.1` par image | `KidsRotometer` | 3,0 par seconde |
| Anti-rebond des touches compté en images | `KidsMenuModule` | images + durée équivalente à 30 i/s |
| `fps=` en argument de lancement | `RacerGame.ReadParams` | analyse corrigée |
| Bouton « gauche » du joueur 2 (copier-coller) | `KidsControlsMenuWindow` | taille du bon bouton |
| Niveau 4 des IA jamais transmis (extras, aventure rejouée) | `RaceParams.SetAILevel` | `m_iAILevel` transmis |
| Arrivées à 0,1 s près, place figée, record d'une IA donné au joueur | `Vehicle`, `RaceParams` | temps exact, rang d'arrivée réel (`Vehicle.m_iFinishRank`, sert au Grand Prix et au multi) |
| Sons `stone_bridge.wav`, `highlight` / `click` | `VehicleParams`, `ChoosePlayer.scr` | bons chemins |

Constats corrigés en route : le soulèvement `fY += 0.01` au contact du sol n'est pas un cumul (marge remise à chaque
image), rien à faire. Le podium était déjà presque dans l'ordre d'arrivée (temps de course, pas temps au tour) ;
le vrai défaut était le pas de 0,1 s et le record.
Laissés : textures manquantes (`Robociki.jpg`, `oko.jpg`), voix des animaux absentes de ce repack.

## 3. Menu « Mods » — fait, à valider (détails : `notes/mods_menu.md`)
Réalisé en fenêtre Java (lignes « libellé < valeur > », clavier et souris), pas en `UserInterface` + `.scr` ; bouton
engrenage dessiné ; déblocages non destructifs (accesseurs de `CoreLogic`) ; textes par jetons `&Mods…&` de `Texts.scr`.
Plan d'origine ci-dessous.

Codes d'origine (`RacerGame.CheckStringCheats`) : Maj + `PRSUPER`, puis `ANIMALS`, `TRACKS`, `BONUSES`,
`L11`…`L63` (saut d'étape d'aventure). Pas de compteur FPS ni de mode debug dans le jeu.

- Menus construits en Java, boutons en images seules. Écran `KidsModsMenuWindow extends KidsMenuWindow`
  (modèle : `KidsSinglePlayerMenuWindow`), enregistré dans `KidsSimpleMenuModule.OnCreate` (`_ModsMenuWindow`).
  Contenu en texte sans nouvelle image : `UserInterface` chargé depuis `Data\Mods.scr` (comme
  `KidsEnterNameMenuWindow` / `ChoosePlayer.scr`, qui gère boutons texte et polices).
- Bouton « Mods » au menu principal (`KidsMainMenuWindow.Init`, `m_Ctrl` a encore 4 places) : une nouvelle image
  de bouton, ou un `KidsButton` avec texte par-dessus.
- Réglages, enregistrés dans `mods.dat` :

| Cadre | Réglage | Branché sur |
|---|---|---|
| Déblocages | Tous les animaux / circuits / extras | `CoreLogic` (comme `ANIMALS`, `TRACKS`, `BONUSES`) |
| Affichage | Compteur FPS | `GetLastFrameTime()` → `KidsText` dans `KidsHud.Update` |
| Affichage | Limite d'images 60 / 120 / 144 / 240 / sans | limiteur du chantier 2 |
| Course | Rattrapage des IA (élastique) oui / non | `RacerModule.CalcPos` |
| Triche | Vitesse 100 / 110 / 125 / 150 % | `Vehicle.UpdateMaxSpeedAccel` (humains seulement) |
| Triche | Bonus infinis, bonus imposé, protection permanente | `Vehicle.CollectBonus` / `UseBonus` |
| Triche | Étape d'aventure (remplace `L11`…`L63`) | `CoreLogic.SetCheatAdventure` |

- Une triche active = course non comptée (records, Grand Prix, Elo), « Triche » affiché au podium. Les codes
  clavier restent comme raccourcis et écrivent aussi `mods.dat`.

## 4. Stats des animaux visibles et éditables — fait, à valider (détails : `notes/animaux.md`)
Réalisé comme prévu : `ModAnimals` lit `Data\animals.scr` dans `RacerGame.OnInitialize` ; cadre nom + 3 barres en
haut à droite de l'écran de choix (nouvelles images `Stats*.png`, pas `progress.png` qui porte « CHARGEMENT »).
Plan d'origine ci-dessous.

Aujourd'hui tout est en dur : coefficients `VehicleParams` (accélération / vitesse / virage, base 4500 / 500 / 80°),
appliqués par `Vehicle.UpdateMaxSpeedAccel`. Exemples : Panthère 0.9/1.0/0.8, Grenouille 0.85/0.85/1.0,
Robot 0.8/0.9/1.0. L'écran de choix (`KidsAnimalChoiceMenuWindow`) n'affiche aucun texte.

- `Data\animals.scr` (nouveau) : une ligne par animal `Animal("Panthere", accel, vitesse, virage)`, valeurs
  d'origine par défaut. Lu par `ModAnimals.Load` (`GameObject.ParseFile`) au démarrage → remplit les tableaux
  de `VehicleParams`. Fichier absent = valeurs d'origine. Vaut pour humains et IA.
- Écran de choix : nom de l'animal + 3 barres (Vitesse, Accélération, Maniabilité) dans la bande libre en haut à
  droite du cadre. Barres sur le modèle de `KidsLoadingMenuWindow` (`Menu/b/progress.png`, `SetTextureCoords`),
  texte `KidsText`. Rafraîchies dans `Show`, `NextAnimal`, `PreviousAnimal`.
- Les réglages par monde (Désert : accélération 390, Hiver : 300, Lune : gravité, Royal : tout égal) restent ;
  les barres montrent les valeurs de base.

## 5. Bonus : correctifs, rééquilibrage, bonus cachés — fait, à valider (détails : `notes/bonus.md`)
Réalisé : Fusée jouable (icône 404x219 refaite en 128x128 ; trop lente pour rattraper sa cible, elle explosait
derrière elle au bout de 5 s : départ 5000, 10000 au plus) et ajoutée à tous les mondes ; case libérée pendant
turbo / pointes / protection ; pistolet à eau contre la protection ; journal de la bombe ; tirage pondéré par la place
dans `Data\bonuses.scr` (poids par place + « partout », sans les durées ni rayons prévus). Choix du 2026-11-18 : le
turbo double (≈ ×2,9) reste tel quel ; le Rocher (inachevé : ni numéro de bonus, ni rocher qui tombe, ni icône) est
abandonné. Plan d'origine ci-dessous.

Bugs trouvés :
- **Turbo ×1,7 appliqué deux fois** (vitesse cible `Vehicle:1740` **et** pas de déplacement `:1980`) → ≈ ×2,9.
- **Fusée** (ID 19) : codée mais dans aucune liste de niveau → jamais ramassable. **Rocher** (21) et **Taupe** :
  codés, inaccessibles.
- Turbo, Pointes, Protection bloquent la case bonus pendant 10 s (on ne peut rien ramasser).
- Pistolet à eau : ignore la protection.
- Tirage uniforme, sans tenir compte de la place (les listes par animal sont identiques pour les 12).

Plan :
1. `Data\bonuses.scr` : par bonus, durée / vitesse / rayon / durée de vie du piège, et poids de tirage selon la place
   (1er → surtout pièges posés, derniers → fusée, OVNI, turbo). Valeurs d'origine par défaut.
2. `LevelBonusTypes.GetRandomBonus` : tirage pondéré par la place (`m_iCurrPlace`) au lieu d'uniforme.
3. Correctifs : turbo une seule fois, case libérée dès l'activation des bonus « sur soi », protection testée par le
   pistolet à eau.
4. Réactiver Fusée (ajoutée aux listes de tous les mondes) et Rocher (projectile instantané déjà codé,
   `Vehicle.FallingRock`) : deux « nouveaux » bonus sans nouvelle image si leurs icônes existent
   (`FXTexCollectFallingRock` existe). Vu en jeu (2026-11-18, bonus imposé du menu Mods) : la Fusée donnée
   directement ne marche pas, il faudra trouver ce qui lui manque, pas seulement l'ajouter aux listes.
5. Vrais nouveaux bonus (idées : aimant qui aspire vers le 1er, bouclier renvoyeur, éclair qui touche tous ceux
   devant) : après, s'il reste de l'envie. Nouvelle icône HUD nécessaire.

## 6. Grand Prix à points — fait, à valider (détails : `notes/grandprix.md`)
Choix du 2026-11-18, à la place du plan ci-dessous : **une seule coupe de 4 courses**, chacune dans un monde tiré au
hasard puis sur un circuit (aller ou retour) tiré au hasard dans ce monde, Royaume du Lion compris ; pas de coupe par
monde ni de finale. Points 10/8/6/4/2/1, course quittée = 0 point et la coupe continue, pas de reprise après
fermeture du jeu. Réalisé en contexte « Course » + drapeau (`ModGrandPrix`), sans nouveau contexte `EMenuCX`.
Plan d'origine ci-dessous.

Il n'y a ni coupe ni points : l'aventure enchaîne 33 étapes en duel de boss, les extras sont des courses seules.

- Nouveau mode, bouton à côté d'Aventure/Course (contexte de menu dédié dans `EMenuCX`).
- Coupes décrites dans `Data\grandprix.scr` : nom + liste de circuits (index de `RacerModule.MAPNAMES`, aller ou
  retour) + nombre de tours. Défaut : une coupe par monde (Forêt, Désert, Hiver, Tropique, Lune) de 4 circuits, plus
  une coupe finale à 6.
- Points par place (6 coureurs) : 10 / 8 / 6 / 4 / 2 / 1, réglables dans le `.scr`. Les mêmes 5 adversaires sur toute
  la coupe (`CoreLogic.FillRandomAnimals` une seule fois).
- Accroche : `RacerGame.EndModule` (après chaque course) → points d'après l'ordre d'arrivée réel (chantier 2), puis
  écran « classement de la coupe » (nouvelle fenêtre, texte `KidsText`) → course suivante ou podium final.
- Sauvegarde : `grandprix.dat` (coupe en cours, points, trophée or/argent/bronze par coupe et par profil), sans
  toucher `progress.cfg`.

## 7. Manette moderne + touches remappables
Existant : le moteur passe par DirectInput (`OnInputJoyAxis`, `OnInputJoyButton`). Une manette Xbox y apparaît
déjà comme joystick générique ; `KbSettings.AttachToController` lit stick X/Y + un bouton « tir ». Le clavier est
déjà remappable (`KidsControlsMenuWindow`), sauvé dans `progress.cfg`.

Manque, et plan :
1. **Gâchettes** : en DirectInput, la manette Xbox met LT/RT sur l'axe Z (combinées). Lire l'axe Z → accélérer /
   freiner aux gâchettes (`InputAnalog` existe déjà, ajouter l'axe et une zone morte).
2. **Croix** et boutons : A = bonus, B = frein, Start = pause, croix = direction (POV non lu aujourd'hui).
3. **Menus à la manette** : traduire croix/stick + A/B en flèches/Entrée/Échap (`KidsMenuModule.OnInputKey`).
4. **Remappage** complet manette (pas seulement « tir ») : actions → bouton/axe, écrit dans `controls.dat` (texte,
   à côté de `mods.dat`) au lieu du binaire `progress.cfg`.
5. Vrai XInput (vibrations, LT/RT séparées) : impossible en Java 1.3 sans DLL native → hors champ, DirectInput
   suffit pour jouer.

## 8. Multijoueur en ligne : courses « fantômes » + Elo
Ton idée tient : chaque jeu simule **sa** voiture, les autres sont des fantômes placés à partir des positions reçues.
Aucun hôte n'a autorité. La physique des voitures est **toute en Java** (`Vehicle.OnCreate` coupe la physique du
moteur), ce qui rend le fantôme simple à faire.

### Fantômes
- Créés par `RaceParams.AddCharacter` mais **absents** de `m_PlayersTab` / `m_AITab` (`RacerModule.OnFrame`) :
  jamais de `UpdateVehicle`.
- Chaque frame : `m_vVehiclePosition` / `Forward` / `Normal` interpolés → `SetVehiclePosAndOrientation()` ;
  pose du pilote (`m_Character.SetVAngle`/`SetHAngle`) ; `m_iCurrLoop`, `m_iCurrTrackPoint`, `m_bProtection`,
  `m_bRaceFinished` venus du réseau.
- `ClearFlag(32)` : pas de collision voiture/fantôme (même astuce que `UFOObject`). Ils ne ramassent pas les boîtes
  (pas de `MoveByEllipsoid`).
- Restent dans `m_Vehicles` / `m_SortTable` : visés par `AimAtEnemy`, comptés dans les places et la barre de distance.
- À couper en ligne : rattrapage de vitesse de `RacerModule.CalcPos` (±40 %), multiplicateur de départ aléatoire
  0,6–1,4 (`Vehicle`, `VehicleParams`), pause par ralentissement du temps (`ExitDuringGameWindow`).

### Projectiles : qui décide quoi
| Catégorie | Bonus | Réseau |
|---|---|---|
| Sur soi | Turbo, Protection, Pointes | drapeaux dans l'état (la protection compte pour les autres) |
| Pièges posés | Catapulte, Huile, Banane, Bouteille, Enclume, Gaz | événement `TRAP(id, type, pos)` ; celui qui roule dessus subit l'effet chez lui et envoie `TRAP_GONE` |
| Visés / à tête chercheuse | Bouchon, Guêpes, Tomate, Noix de coco, Boule de neige, Cookie, Fusée | le tireur fait voler le projectile chez lui ; touche un fantôme → `HIT(cible, effet)`, la victime applique l'effet |
| Instantanés | Pistolet à glace, Pistolet à eau, Rocher | `HIT` direct |
| OVNI | — | `HIT(UFO)` : l'enlèvement tourne chez la victime |
| Zone | Bombe, Pastèque | `BOOM(pos)` : chaque jeu teste sa propre voiture |

Les autres voient aussi le projectile (événement `SHOT`) pour l'effet visuel. La victime vérifie sa protection
avant d'appliquer un `HIT` : c'est elle qui fait foi.

### Transport (JRE 1.3 : pas de `java.nio`)
- `DatagramSocket` + `byte[]` à la place de `DatagramChannel` / `ByteBuffer`. Lecture non bloquante :
  fil démon qui ne fait que remplir une file, vidée par le fil principal dans `RacerGame.OnFrame`.
- Repris de pet_soccer : serveur Python (salons, relais, liaison directe, comptes, Elo SQLite), `ModLobby`
  (à porter en 1.3). Salons à étendre de 2 à **jusqu'à 6 joueurs** (`rooms.py`), Elo multijoueur (chaque paire
  de joueurs compte comme un duel, K divisé par le nombre d'adversaires).
- Paquets : état de sa voiture 20–30/s (position, direction, vitesse, tour, porte, drapeaux) ; événements fiables
  (`SHOT`, `HIT`, `TRAP`, `TRAP_GONE`, `BOOM`, `FINISH(temps exact)`), même mécanisme d'accusé que `ModNet`.
- Départ commun : le serveur donne un top (« départ dans N ms ») ; les feux attendent ce top au lieu des 8 s
  locales (`KidsStartLights`). Temps d'arrivée : `Clock.GetMissionExactTime` (le temps d'origine est arrondi à 0,1 s).
- Classement final : par temps d'arrivée annoncé ; un joueur qui ne finit pas en N s après le premier est classé
  derrière. Chaque jeu calcule pareil, le serveur vérifie que les temps annoncés concordent.
- Chaque voiture a un **propriétaire** (le jeu qui la simule) : un `HIT` est toujours envoyé à lui.

### IA pour compléter la grille (choix retenu)
- Les places vides (jusqu'à 6) sont des IA **simulées par l'hôte du salon**, dans son `m_AITab` comme hors ligne,
  et diffusées comme n'importe quelle voiture : les autres jeux les voient en fantômes.
- Un projectile d'un invité qui touche une IA → `HIT` envoyé à l'hôte, qui l'applique à son IA. Les IA de l'hôte
  visent aussi les fantômes des invités (`AimAtEnemy` voit déjà tout `m_Vehicles`) → `HIT` vers l'invité visé.
- L'hôte diffuse donc 1 + nIA voitures (≈ 6 × 40 octets, 20/s : rien à craindre).
- Rattrapage (`CalcPos`) coupé en ligne : sinon les IA de l'hôte se règleraient sur lui seul.
- Hôte parti en course : ~~ses IA s'arrêtent net~~ (choix du 2026-11-20) le joueur restant de plus petite place
  reprend son rôle (relais, réponses aux JOIN) et fait rouler les IA à partir de leurs fantômes ; la course continue.
- Niveau des IA : au choix de l'hôte dans un salon à code (ligne « Niveau des IA », la difficulté du menu Mods au
  départ) ; fixe en partie classée (réglé dans `online.scr`, phase 4).

### Elo
- Formule de `ModElo` / `store.elo_delta` (1000 au départ, K = 48 puis 32). Course à N **humains** : chaque paire
  compte comme un duel gagné par celui arrivé devant, somme des écarts × K / (N − 1). Abandon = dernier.
  Les IA ne comptent pas (on ne regarde que l'ordre des humains entre eux) ; une course à 1 humain + IA n'est pas classée.
- Seules les parties rapides comptent (comme pet_soccer) ; salons à code jamais classés.

### Phases
| # | Phase |
|---|---|
| 0 | Faisabilité : `DatagramSocket` dans la JVM 1.3 du jeu (fil de lecture), deux jeux sur un PC (`build.ps1 -Second` → `game_dev_b`) |
| 1 | Course à 2 en direct sans serveur, **sans bonus** (règles « FairPlay » des extras), fantômes, départ commun, temps d'arrivée |
| 2 | Bonus en ligne (`SHOT`, `HIT`, `TRAP`, `BOOM`, OVNI) |
| 3 | Serveur + salons à code jusqu'à 6, écran « en ligne », choix animal / circuit / tours, IA de l'hôte pour compléter |
| 4 | Partie rapide, Elo multi, classement |
| 5 | Installeur pour les amis (plus simple que pet_soccer : pas de `code.pak`, on copie dans `java\`) |

En ligne, coupés : triches du menu Mods, rattrapage, départ aléatoire, pause qui fige le temps (échap ouvre le menu
sans arrêter la course).

## Fichiers principaux touchés
- Infra : `RacerGame` (démarrage, `OnFrame`), nouveaux `ModOptions`, `ModLog`, `ModVideo`, `ModAnimals`,
  `ModBonuses`, `ModGrandPrix`, `ModNet`, `ModLobby`.
- Résolution : `Settings`, `GameSerializeSettings`, `KidsAudioVideoMenuWindow`, `Window`, `KidsText`, `Fonts`,
  `CarsManager`/`AnimCar`/`CarLine`, `Module` (sous-titres), `RacerCamera`, `RacerModule` (écran partagé).
- Course : `RacerModule`, `Vehicle`, `VehicleParams`, `RaceParams`, `LevelBonusTypes`, `KidsHud`, `KidsRotometer`,
  `KidsMenuModule`, `KidsStartLights`, `Clock`.
- Menus : `KidsSimpleMenuModule`, `KidsMainMenuWindow`, `KidsAnimalChoiceMenuWindow`, nouvelles fenêtres Mods,
  Grand Prix, En ligne.
- Données : `mods\data\Data\{Mods,animals,bonuses,grandprix,online}.scr`.
- Serveur : copie adaptée de `..\pet_soccer\server\` dans `pet_racer\server\` (salons à 6, Elo multi).
- À réutiliser de pet_soccer : `ModOptions`/`MenuMods` (logique `mods.dat`), `ModElo` (formule), `ModLobby` et
  `ModNet` (structure, accusés, liaison directe ; à réécrire sans `nio`), `server\*.py`, `package.ps1`.

## Vérification
- Chaque chantier : `powershell -ExecutionPolicy Bypass -File mods\build.ps1 -Run`, puis lignes `[mod]` dans
  `game_dev\crash.log`.
- Résolution : lancer en 1920×1080 et 2560×1440 (16:9) et 1024×768 (4:3) ; menus, HUD, écran partagé, choix du perso,
  sous-titres, défilé de voitures du menu ; changer de résolution depuis le menu puis relancer.
- FPS : compteur FPS du menu Mods ; même circuit, même animal, temps au tour comparable avec limite 60 et sans
  limite ; alt-tab en course sans téléportation.
- Stats / bonus : modifier `animals.scr` et `bonuses.scr`, relancer, vérifier les barres et le comportement en course ;
  fusée et rocher ramassables.
- Grand Prix : coupe complète, points cumulés, sauvegarde reprise après fermeture du jeu.
- Manette : manette Xbox en jeu (gâchettes, croix, A/B) et dans les menus ; remappage gardé après relance.
- En ligne : deux jeux sur ce PC (`game_dev` + `game_dev_b`) puis tests serveur repris de `server\test_server.py`
  (salons à 6, Elo multi), mauvaise connexion simulée (`--lag`, `--loss`), puis une vraie course par Internet.
