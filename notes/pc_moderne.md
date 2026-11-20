# Corrections PC moderne (chantier 2)

État : **fait, à valider en jeu.** Compilé, pas encore lancé (rien n'a été vu à l'écran pour ce chantier, sauf le
dérapage).

| Correctif | Fichiers |
|---|---|
| Dérapage indépendant du framerate | `Vehicle.MoveVehicle` |
| Limiteur d'images (144 par défaut, réglable dans `mods.dat`) | `ModFrameLimiter`, `ModOptions`, `RacerGame.OnFrame` |
| `System.gc()` une fois par seconde au lieu de chaque image | `RacerModule.OnFrame` |
| Pas de physique maxi 0,05 s | `RacerModule.OnFrame` |
| Aiguille du compte-tours à vitesse fixe par seconde | `KidsRotometer` |
| Anti-rebond des menus en temps | `KidsMenuModule` |
| Argument `fps=` | `RacerGame.ReadParams` |
| Bouton « gauche » du joueur 2 (menu Contrôles) | `KidsControlsMenuWindow` |
| Niveau des IA en extras et en aventure rejouée | `RaceParams.SetAILevel` |
| Ordre d'arrivée réel (place, podium), record attribué au bon temps | `Vehicle`, `RaceParams` |
| Sons : pont de pierre, menu de choix du profil | `VehicleParams`, `Data\ChoosePlayer.scr` |

## Dérapage trop fort à vitesse maxi
Symptôme : à vitesse maxi, un petit coup de volant fait glisser la voiture sur le côté, très fort.

Cause : `Vehicle.m_vTurningSlide`. Tant qu'on tourne, `AddSlideFromTurning` le fait grandir :
`60 × (angle de braquage / braquage maxi) × (vitesse / 4500)` par seconde, vers la droite de la voiture.
`DecreaseTurnSlide` le réduit de 30 par seconde. Ces deux calculs suivent bien le temps réel.
Mais `MoveVehicle` ajoutait ce vecteur **tel quel** au déplacement de chaque image, sans `× m_fFrameTime`. C'est donc un
déplacement par image, pas par seconde, et la glissade réelle est proportionnelle au nombre d'images par seconde :

| Images/s | Glissade par rapport à 30 i/s |
|---|---|
| 30 | ×1 |
| 60 | ×2 |
| 144 | ×5 |
| 300 | ×10 |

Le jeu n'a ni limite d'images ni VSync en fenêtré. En plein écran, l'écran impose souvent son rafraîchissement
(60, 144 Hz…). À l'époque, le jeu tournait vers 25–30 i/s (l'enregistrement vidéo du jeu est à 25 i/s par défaut).

Exemple à 30 i/s : au bout d'une seconde de braquage maxi à pleine vitesse, la voiture glisse à 900 unités/s pour
4500 vers l'avant (≈ 11°). À 144 i/s, elle glisse à 4300 unités/s, presque autant que vers l'avant.

Correctif (`Vehicle.MoveVehicle`) : le vecteur est multiplié par `m_fFrameTime × 30`. La glissade est celle qu'on avait
à 30 i/s, quel que soit le framerate. La constante `Vehicle._SLIDE_REF_FPS` (30) règle la force : 25 = un peu moins,
45 = 1,5 fois plus. Vaut aussi pour les IA (même code), et pour l'élan transmis lors des chocs, calculé à partir du
déplacement de l'image.

Inchangé : les pointes (`m_bTireSpikes`) suppriment toujours la glissade, et la fumée et le son de dérapage gardent
leurs seuils (ils lisent la taille du vecteur, qui ne dépendait pas du framerate).

Mesuré sur le PC de test (2560×1440 plein écran) : 420 à 550 images/s en course, donc une glissade environ 15 fois
trop forte avant le correctif.

## Limiteur d'images
Sans limite, le jeu tourne à ~500 images/s : carte graphique à fond, un cœur du processeur à fond, ventilateurs.

- `RacerGame.OnFrame` (appelé par le moteur à chaque image : menus, course, vidéos) → `ModFrameLimiter.Frame()`,
  qui attend que `1/N` s se soit écoulée depuis l'image précédente.
- `N` = `FpsLimit` dans `game_dev\mods.dat` (texte `Clé=valeur`, créé au premier lancement). **144 par défaut** ;
  `0` = sans limite ; toute valeur de 30 à 1000 est acceptée. Le menu Mods (chantier 3) proposera 60 / 120 / 144 /
  240 / sans. Pour l'instant : modifier `mods.dat` jeu fermé.
- Attente : `Thread.sleep(1)` tant qu'il reste plus de temps qu'un sleep n'en prend vraiment (mesuré en continu), puis
  `Thread.yield()` jusqu'à l'heure exacte. Si Windows arrondit les sleep à 15,6 ms, on n'attend plus qu'avec yield :
  la limite reste juste, mais un cœur reste occupé.
- Horloge : au premier appel, `Calibrate` compare `GameObject.GetAbsoluteTime()` (moteur) et
  `System.currentTimeMillis()` (Java) pendant 40 ms : finesse, unité (secondes ou millisecondes), et si l'horloge
  moteur avance bien pendant une image. Garde la plus fine. Résultat dans `crash.log` :
  `[mod] limiteur : horloge moteur (moteur : pas 0.01 ms, unite s, Java : 1.00 ms), sleep(1) = 1.95 ms, limite 144 images/s`
- Garde-fous : jamais plus de 0,25 s d'attente par image (si l'horloge se bloque, on passe à l'horloge Java) ;
  après une image lente (chargement, alt-tab), on repart de maintenant au lieu de rattraper. Coupé pendant
  l'enregistrement vidéo (`record`).

## Ramasse-miettes
`RacerModule.OnFrame` appelait `System.gc()` (ramasse-miettes complet) **à chaque image** : 500 par seconde. Il est
maintenant appelé une fois par seconde en course (`_GC_PERIOD`). La JVM ramasse aussi d'elle-même quand il le faut.
Sa durée la plus longue est notée dans la ligne de diagnostic (voir plus bas).

## Pas de physique maxi
Après une image très lente (chargement, alt-tab, saccade), la physique avançait d'un coup de tout le temps écoulé :
voiture qui saute, ou traverse un décor. `m_fFrameTime` est plafonné à 0,05 s (`RacerModule._MAX_FRAME_TIME`, soit
20 images/s) : en dessous, le jeu ralentit au lieu de sauter. Le chrono de course (`Clock`) suit toujours le vrai temps.

## Compte-tours
L'aiguille (`KidsRotometer.SetRotations`) avançait d'au plus 0,1 par image : à 30 i/s, un balayage du cadran en 0,3 s ;
à 500 i/s, instantané (sauts nerveux aux passages de rapport). Maintenant 3,0 par seconde, comme à 30 i/s.

## Anti-rebond des menus
Après l'ouverture d'un écran ou un Entrée, les menus ignorent les touches pendant 2 images
(`KidsMenuModule.SetIgnoreKeys(2)`) : 67 ms à 30 i/s, 4 ms à 500 i/s. Le nombre d'images est gardé, et la durée
qu'il représentait à 30 i/s s'y ajoute (`CanIgnoreKeys`).

## Petits bugs
- **`fps=`** en argument de lancement (`RacerGame.ReadParams`, sert à l'enregistrement vidéo) : parenthèse mal placée,
  le jeu lisait « =25 » + « 1 » et levait une exception. Corrigé, une valeur illisible est ignorée.
- **Menu Contrôles** (`KidsControlsMenuWindow`) : copier-coller, la taille du bouton « gauche » du joueur 2 était écrite
  dans celle du joueur 1. Le bouton du joueur 2 gardait sa taille par défaut (0,16 × 0,10 au lieu de 0,078 × 0,058).
- **Niveau des IA** (`RaceParams.SetAILevel`) : le niveau 4 prévu pour les **extras** et pour l'**aventure rejouée**
  (difficulté 2, après la fin de l'aventure) était calculé puis jamais transmis aux IA, qui gardaient le niveau du
  circuit (0 à 3). Maintenant transmis : IA plus précises, plus rapides à ramasser et utiliser les bonus
  (`VehicleAI._AI_*`). **Les extras deviennent plus difficiles**, comme prévu à l'origine. Course simple et
  première aventure : inchangées.
- **Ordre d'arrivée** (`Vehicle.SetFinishRank`, `FillTimeList`) :
  - le temps d'arrivée était pris sur `Clock.GetMissionTime`, qui n'avance que par pas de 0,1 s : deux arrivées
    serrées avaient le même temps, et le podium les départageait par l'ordre de création (le joueur d'abord).
    Maintenant `GetMissionExactTime` et un rang d'arrivée réel (`RaceParams.m_nFinished`).
  - la place (`m_iCurrPlace` : HUD, musique de victoire) vient du classement à la distance, refait toutes les 0,1 s
    (`RacerModule.CalcPos`), qui ne déplace plus une voiture arrivée : une voiture qui doublait juste avant la ligne
    pouvait garder la place de celle qu'elle venait de passer. Elle est maintenant rangée à son rang d'arrivée.
  - le podium (`TimeList`) est rempli dans l'ordre d'arrivée, puis les voitures pas arrivées par distance parcourue
    (temps extrapolé, comme l'original).
  - **record** : `CoreLogic.GetNewRecordPlayer` prend l'entrée n° 0 pour le joueur, mais l'original y mettait la
    *place*. Quand une IA gagnait en battant le record du circuit, son temps était enregistré au nom du joueur.
    Maintenant n° 0 = humain le mieux classé, n° 1 = le second humain (à 2), IA à partir de 10.
    Le record est donc le temps du joueur, gagnant ou non.
  Prépare le Grand Prix (points selon l'ordre d'arrivée) et le multijoueur.
- **Sons** : `VehicleParams` visait `stone_bridge.wav`, le fichier s'appelle `bridge_stone.wav` (bruit des roues sur
  les ponts de pierre). `Data\ChoosePlayer.scr` (écran de choix du profil) visait `data/sounds/click` et
  `highlight` sans dossier ni extension → `Data/Sounds/Demo/CLICK.WAV` et `highlight.wav`.

## Vérifié, rien à corriger
- **Soulèvement `fY += 0.01`** au contact du sol (`Vehicle`, 3 endroits) : ce n'est pas un cumul. La voiture est
  replacée à chaque image au point de contact + 0,01 (marge pour ne pas rester dans le sol), puis la gravité la
  ramène au sol à l'image suivante. Même résultat à tout framerate.
- `m_vMoveDir.Mul(0.99F)` en fin de déplacement : retire 1 % de chaque déplacement, donc 1 % de la vitesse, à tout
  framerate.
- Caméra (`RacerCamera`, `_CAM_DIR_DELAY / m_fFrameTime`) et tête du pilote (`VehicleCharacter.SetVAngle`) : déjà en
  temps réel.

## Diagnostic : images par seconde
`RacerModule.OnFrame` écrit toutes les 20 s de course une ligne dans `crash.log` :
`[mod] course : 144 images/s en moyenne, pire image 21 ms, attente du limiteur 62 % (limite 144 images/s), gc au pire 0.85 ms`
« attente du limiteur » = part du temps passée à attendre : la marge qui reste au PC.

## À vérifier en jeu
- `crash.log` : la ligne `[mod] limiteur : horloge …` au démarrage, puis en course ≈ 144 images/s.
- Conduite identique avec `FpsLimit=60`, `144` et `0` (même circuit, même animal, temps au tour comparable).
- Alt-tab en course : pas de téléportation au retour.
- Compte-tours : l'aiguille balaie au lieu de sauter.
- Menu Contrôles : le bouton « gauche » du joueur 2 a la même taille que celui du joueur 1.
- Une arrivée serrée : place affichée, podium et musique d'accord.
