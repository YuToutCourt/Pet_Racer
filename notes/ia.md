# IA des voitures (chantier IA)

Demandé le 2026-11-18 : améliorer l'IA (pilotage, bonus, élastique, dégagement) et pouvoir choisir sa difficulté.
Réglage **« Difficulté des IA »** dans l'écran Mods (`mods.dat` : `AiLevel`).

| Valeur | `AiLevel` | Effet |
|---|---|---|
| d'origine | 0 | code de Techland intact (`VehicleAI` d'origine, élastique d'origine) |
| auto (défaut) | 1 | IA améliorée, niveau donné par le circuit comme à l'origine (0 à 3, 4 en extra / aventure rejouée) |
| facile | 2 | IA améliorée, niveau 0, vitesse ×0,95 |
| normal | 3 | niveau 2, vitesse ×1 |
| difficile | 4 | niveau 4, vitesse ×1 |
| expert | 5 | niveau 5 (jamais utilisé par le jeu d'origine), vitesse ×1 |

Depuis le 2026-11-20, l'IA améliorée a la voiture du joueur à tous les niveaux (vitesse, rotation, dérapage) : le
niveau ne change que la conduite. Voir « IA forte, pas plus rapide » en bas.

Ce n'est pas une triche : les records et le Grand Prix comptent quelle que soit la difficulté.

## Comment l'IA d'origine conduisait (`decompiled/VehicleAI.java`)
- Vise le point suivant de la piste de l'IA (`RacerModule.m_AITrack`, points `m_bAIPoint`), décalé au hasard à gauche ou
  à droite (`AddNoise`). Elle ne regarde jamais plus loin.
- Direction « directe » (`Vehicle.m_bDirectSteer`, `TurnVehicleUsingAngle`) : le nez tourne de l'angle demandé à la
  vitesse de virage maxi. `TurnLeft` / `TurnRight` (tout ou rien) ne servent qu'au dérapage et à l'animation.
- `SlowOnCurve` lâche l'accélérateur si l'angle à rattraper est trop grand, sans jamais freiner. L'angle du virage
  suivant (`TrackPoint.m_fAngle`) est ignoré. La conversion en degrés utilise 57,926 au lieu de 57,296.
- Bonus : projectiles tirés dès qu'une cible est verrouillée ; pièges lâchés derrière après 0 à 15 s au hasard,
  même si personne ne suit ; turbo dès que l'angle est inférieur à 20° ; boîte à ramasser choisie au hasard.
- Coincée (moins de 750 parcourus en 3 s) : braque à 90°, freine 2 s (donc recule), repart.
- Niveau 0 à 5 (`_AI_PRECISION`, `_AI_NOISE`, `_AI_BONUS_COLLECT`, `_AI_BONUS_USE_TIME`, `_AI_TURNING_MODIFIER`,
  `_AI_SLIDE_MODIFIER`), donné par chaque circuit (`Las1.InitModule` → `RaceParams.SetAILevel`).

## IA améliorée (`mods/java/VehicleAI.java`, méthodes `Mod…`)
Tous les réglages sont des constantes `_MOD_…` en tête de `VehicleAI`.
1. **Trajectoire** (`ModLineOffset`) : dans un virage, la cible se décale vers la corde (jusqu'à 60 % de la
   demi-largeur, plus franchement à haut niveau). Le sens du virage est calculé géométriquement, sans dépendre des
   conventions gauche / droite du jeu. En ligne droite, chaque IA garde son couloir. Il reste un peu de hasard
   (moitié de celui d'origine).
2. **Regard devant** : en approchant d'une porte, la cible glisse vers la suivante (0,25 s + 0,04 s par niveau). Le
   glissement est moindre dans les virages serrés.
3. **Freinage** (`ModSpeedControl`) : compare le temps restant avant la porte au temps nécessaire pour tourner
   (angle actuel + virage de la porte). Elle lâche l'accélérateur, sans jamais freiner (2026-11-20 :
   `_MOD_BRAKE_RATIO` = 0 ; avant, 0,5 = freinage réel si très en dessous, au-dessus de 45 % de la vitesse maxi).
4. **Volant progressif** : `TurnLeftAnalog` / `TurnRightAnalog` proportionnels à l'angle (plein à 25°), donc moins
   de dérapage parasite sur les petites corrections. Conversion en degrés corrigée.
5. **Bonus** (`ModUseBonus`) :
   - pièges (catapulte, huile, gaz, banane, bouteille, enclume) lâchés quand une voiture suit à moins de 2500, ou
     après 6 à 14 s selon le caractère ;
   - turbo gardé pour une ligne droite (angle inférieur à 12°, virages des 2 portes suivantes inférieurs à 20°),
     ou après 10 s ;
   - projectiles comme à l'origine, délai du niveau ×0,7 à ×1,3 selon le caractère ;
   - boîte ramassée : la plus proche de la trajectoire.
6. **Caractère** (tiré au départ, écrit dans `crash.log`) : hasard ×0,85 à ×1,15, couloir préféré, agressivité
   avec les bonus, vitesse ±1,5 %.
7. **Difficulté** : voir le tableau plus haut (`ModAI`, `RaceParams.SetAILevel`, et `VehicleAI.ModStart` pour un
   circuit qui ne règle pas le niveau).
8. **Élastique** (`RacerModule.CalcPos` → `ModAI.CatchUp`) : progressif entre 0,02 et 0,25 tour d'écart avec le
   premier humain. Derrière : jusqu'à +15 % (facile) ou +25 %. Devant : jusqu'à −30 % (facile), −20 % (auto,
   normal), −10 % (difficile), −5 % (expert). L'original faisait +40 % / −40 % et atteignait sa pleine force dès
   1 à 5 % de la course. Les humains n'ont plus d'élastique : l'original ralentissait aussi le 2e joueur en écran
   partagé. « Rattrapage des IA : non » le coupe toujours. (Valeurs revues le 2026-11-20, voir en bas : plus de
   gain en difficile / expert, plus de bonus de virage à la traîne.)
9. **Évitement** (`ModAvoid`) : une voiture plus lente à moins de 1400 devant, dans le même couloir (à moins de
   220 de côté), décale la cible de 140 à 280. L'IA garde le côté choisi et reste à 85 % de la largeur de piste ;
   sinon elle passe de l'autre côté.
10. **Dégagement** (`ModStuck`) :
    - presque immobile pendant 1,5 s, ou à contresens pendant 2 s : marche arrière de 0,6 à 1,6 s en tournant le
      nez vers la cible ;
    - 3 fois au même point en moins de 15 s : remise en piste (`Vehicle.m_bMustRespawn`, comme après une chute).

## Journal (`crash.log`)
- Au départ : `[mod] IA Lion : difficulte normal (niveau 2), precision x0.93, couloir 0.40, agressivite 0.70, vitesse x1.01`
- À l'arrivée : `[mod] IA Lion arrivee 3e : hors piste 2.10 s, freinages 12, evitements 5, coincee 1 fois, remise en piste 0, bonus 4 ramasses / 4 utilises`
- Remise en piste : `[mod] IA Lion : coincee au point 37, remise en piste`
- `mods.dat charge : … IA auto`

## Fichiers
`VehicleAI` (nouveau dans `mods/java`), `ModAI` (nouveau), `ModOptions` (`AiLevel`), `KidsModsMenuWindow` (ligne
« Difficulté des IA », hauteur de ligne 0,05 → 0,048 pour 12 lignes), `RaceParams.SetAILevel`, `RacerModule.CalcPos` ;
`Data\Texts.scr` (`ModsAiLevel`, `ModsAi0`…`ModsAi5`, `mods/art/make_texts.py`).

## À vérifier en jeu
1. Écran Mods : la ligne « Difficulté des IA » s'affiche entre « Rattrapage » et « Vitesse », les 12 lignes et le
   bandeau de triche tiennent au-dessus de OK / Retour, et ← → fait défiler d'origine / auto / facile / normal /
   difficile / expert.
2. Course en « auto » : les IA prennent la corde, ne zigzaguent plus, freinent avant les épingles, se doublent
   sans se rentrer dedans.
3. Pièges : une IA lâche son piège quand le joueur la suit de près. Turbo : déclenché en ligne droite.
4. Facile, puis expert, sur le même circuit : écart nettement différent.
5. « d'origine » : comportement du jeu de base (zigzag, pas de frein).
6. Envoyer `crash.log` après quelques courses : les bilans `[mod] IA …` disent ce qu'il faut régler (beaucoup de
   « coincee », de temps hors piste, ou de freinages = constantes `_MOD_…` à ajuster).

## Corrections après l'essai en ligne du 2026-11-20
Retour : « les IA prennent souvent les murs / arbres ». Journal (4 IA expert, Forêt 1) : jusqu'à 9 « coincée » par IA
et par course, trois IA coincées plusieurs fois au même point (2), 3,5 s hors piste pour l'une. Un choc contre un
arbre ou un mur met la vitesse à 0 (`Vehicle.OnCollisionFullInfoMove`), d'où les arrêts.
- **Moins de corde** : l'intérieur des virages a souvent des arbres ou un mur. `_MOD_LINE_USE` 0,6 → 0,5 ; part de
  corde `0,35 + 0,1 × niveau` plafonnée à 0,8 (avant `0,35 + 0,13 × niveau`, 1,0 en expert).
- **Points dangereux appris en course**, communs à toutes les IA de la course (`VehicleAI.m_aModDanger`, refait à
  chaque course) : un choc contre le décor (`ModOnObstacle`, au plus un toutes les 0,5 s) ajoute 0,5 au point visé
  et 0,3 au précédent ; une voiture coincée ajoute 0,35 / 0,21 (plafond 1). À un point dangereux : la cible revient
  vers le milieu de la porte (décalage × (1 − danger)), le regard vers le point suivant diminue d'autant, et la
  prudence au freinage augmente (jusqu'à × 1,8). Après un choc, la cible est recalculée tout de suite.
- Bilan dans `crash.log` : `… chocs contre le decor N, points dangereux connus N` (voir « Carte des circuits »).

À vérifier : mêmes circuit et difficulté qu'à l'essai (Forêt 1, expert), hors ligne puis en ligne ; les IA doivent
nettement moins finir dans les arbres, surtout au 2e et 3e tour ; « coincee » et « chocs contre le decor » plus bas
dans les bilans. Si elles deviennent trop lentes dans certains virages : baisser `_MOD_DANGER_CAUTION`.

## IA forte, pas plus rapide (2026-11-20)
Retour : « l'IA veut trop prendre les virages serrés, ça ne marche pas ; elles sont trop faciles à battre ; en expert
elles vont juste plus vite pour te battre, j'aimerais qu'elle soit forte, pas plus rapide ».
D'où venait la vitesse en expert : vitesse ×1,06, élastique jusqu'à +25 % derrière le premier humain, et surtout la
tenue de route d'origine des IA (`_AI_TURNING_MODIFIER` ×1,4 en rotation, `_AI_SLIDE_MODIFIER` ×0,4 en dérapage au
niveau 5, et 2 niveaux de plus à la traîne).
- **Même voiture que le joueur** : rotation et dérapage ×1 à tous les niveaux (`ModAI._AI_TURN`, `_AI_SLIDE`,
  appliqués par `VehicleAI.ModApplyHandling` au départ et à chaque image dans `RacerModule.CalcPos`) ; vitesse ×1 de
  normal à expert, ×0,95 en facile ; caractère 0,985 à 1 (jamais au-dessus). Le mode « d'origine » ne change pas.
- **Élastique** : plus aucun gain en difficile et expert ; derrière, +10 % (auto, facile) / +8 % (normal) ; devant,
  −20 % (auto), −30 % (facile), −15 % (normal), −5 % (difficile), 0 (expert). Plus de bonus de virage à la traîne.
- **Trajectoire « large, corde, large »** (`ModLineOffset`) : corde modérée à la porte du virage (0,3 + 0,04 × niveau,
  avant jusqu'à 0,8), et la porte d'avant / d'après se place à l'extérieur du virage voisin (`_MOD_WIDE` 0,4) : entrée
  et sortie larges au lieu de couper l'intérieur tout le long.
- **Regard devant** plus court dans les virages : `_MOD_LOOK_BLEND` 0,6 → 0,45, nul à partir de 100° (avant 120°) ;
  c'est lui qui faisait glisser la cible à l'intérieur avant la porte.
- **Le niveau joue sur le freinage** : prudence ×1,1 (facile) à ×0,9 (expert), freinage plus tardif et plus juste à
  haut niveau. Restent par niveau : précision de la ligne, ramassage et délai des bonus, regard devant.
- Journal au départ : `… vitesse x1.00, virage x1.00, derapage x1.00`.

À vérifier : expert sur Forêt 1, hors ligne puis en ligne ; les IA ne doivent plus remonter « toutes seules » en
ligne droite, mais rester dures à battre grâce à des virages propres. Réglages si besoin : trop lentes en virage →
baisser `_MOD_CAUTION` ou monter un peu `ModAI._AI_TURN` (1,1) ; encore dans le décor → baisser `_MOD_APEX` /
`_MOD_WIDE` ; trop faciles → `_MOD_CAUTION_LEVEL` plus grand.

## Carte des circuits (2026-11-20)
Idée du joueur : « une sorte de mapping de toutes les maps, avec les objets, et un peu de hasard pour que chaque IA
ne fasse pas la même chose ». Faite automatiquement, sans rien régler à la main par circuit (`mods/java/ModAIMap.java`).
- **Calcul au chargement** (`RaceParams.SetStartPos`, seulement s'il y a des IA améliorées) : pour chaque tronçon de
  la piste des IA (porte → porte suivante), 15 couloirs en travers, un rayon par couloir à 45 au-dessus du sol :
  - arbres : `TraceTrees` (les arbres du moteur arrivent dans `Vehicle.OnCollisionFullInfoMove` sans objet, traités
    comme arbres) ;
  - objets : `TraceObjects`, arbre / poteau toujours, mur (`PER_FACE`, pont, objet sans classe) s'il est plus raide
    que `_MAX_SLOPE_ANGLE` (0,3) ; les bonus, voitures, déclencheurs et le sol ne comptent pas (le rayon repart
    derrière, 3 fois au plus) ;
  - relief : `TraceTerrain`, seulement une pente plus raide que 0,3.
  Sol de chaque porte : le terrain, ou une surface de pont si la piste y passe (la plus proche de la porte d'avant).
- **Couloir libre** de chaque porte : couloirs libres à l'arrivée et au départ, chaque obstacle fermant aussi les
  couloirs voisins sur une demi-largeur de voiture (55) ; on garde le plus long passage libre (à égalité, le plus
  près du milieu). Rien de libre : la porte reste comme avant (« sans couloir libre » dans le bilan).
- **Utilisation** (`VehicleAI.ModLineOffset`) : avec la carte, la trajectoire peut prendre 65 % de la demi-largeur
  (50 % sans) et elle est ramenée dans le couloir libre ; à un point dangereux, elle va vers le milieu du couloir
  libre (plus le milieu de la porte, qui peut être un arbre). L'évitement des voitures reste aussi dans ce couloir.
- **Hasard par IA** : en plus du couloir préféré et de la précision, chaque IA tire au départ sa façon de prendre
  les virages : corde ×0,75 à ×1,25, entrée / sortie larges ×0,5 à ×1,5.
- **Mémoire par circuit** (`aimap.dat`, à côté de `mods.dat`), deux lignes par circuit, sens et nombre de portes.
  Écrite à chaque fin de course (`RacerGame.EndModule` → `VehicleAI.ModSaveMemory`), à l'arrivée comme sur Échap :
  avant le 2026-11-20 elle ne l'était qu'à l'arrivée d'une IA, donc jamais quand le joueur gagnait de loin.
  - **Murs appris** (`Las1.map|84|murs=12:x:140,13:-60:x,…` : porte, bord gauche, bord droit, x = rien) : à chaque
    choc contre le décor, la normale du choc dit de quel côté était l'obstacle ; le bord du couloir de ce côté passe à
    35 de la voiture (pas entier à la porte la plus proche, demi à l'autre). Jamais oubliés : plus on court, plus le
    couloir de chaque endroit piégeux est juste. Il reste toujours au moins 40 de couloir. Choc pile de face : rien
    d'appris (on ne sait pas de quel côté passer), le point dangereux s'en charge.
  - **Points dangereux** (`Las1.map|84=0,0,35,…`, en centièmes) : repris ×0,9 en début de course (s'oublient peu à
    peu, puisqu'ils font freiner).
  - Chocs comptés (2026-11-20) : arbres et enclumes comme avant, plus les murs, maisons et ponts (objets raides) et les
    rebonds sur le relief raide (`Vehicle.SetBounceVector`) — avant, un mur n'apprenait rien à l'IA.
- Journal :
  - au chargement : `[mod] IA carte Las1.map|84 : 84 portes, 1260 rayons (distance absolue), couloirs fermes par
    arbres N, objets N, relief N ; portes resserrees N, sans couloir libre N, sur pont N ; memoire : murs appris a N
    portes, points dangereux N ; en N ms` ;
  - au départ de chaque IA : `… corde x1.12, largeur x0.80` ;
  - les 12 premiers chocs contre le décor de la course : `[mod] IA Lion : choc contre le decor avant le point 12,
    voiture a -180 du milieu, obstacle a gauche, cible -120, porte -300..280, couloir -90..280, mur appris aux portes
    12 11` ;
  - fin de course : `[mod] IA carte Las1.map|84 : memoire enregistree, murs appris a N portes (N lecons cette
    course), points dangereux N`.

À vérifier : la ligne `IA carte` apparaît, en peu de ms, avec des couloirs fermés (si tout est à 0, les rayons ne
voient pas le décor) ; la ligne `memoire enregistree` à chaque fin de course ; les chocs disent `obstacle a gauche /
a droite` et `mur appris` ; en courant 5 à 10 fois le même circuit, `murs appris a N portes` monte puis se stabilise et
les chocs du bilan baissent. Si des IA se mettent à éviter un endroit sans raison (couloir trop serré) : effacer la
ligne `|murs` du circuit dans `aimap.dat`.
Réglages : `ModAIMap._LANES`, `_RAY_HEIGHT`, `_CAR_HALF`, `_KEEP`, `_LEARN_STEP`, `_MIN_CORRIDOR` ;
`VehicleAI._MOD_MAP_LINE_USE`. Effacer `aimap.dat` remet la mémoire à zéro.

**Mémoire de départ pour les amis** : `mods\data\Data\aimap_depart.dat`, une copie de `game_dev\aimap.dat` faite par
`mods\package.ps1 -AiMap`. L'installeur la copie en `aimap.dat` seulement si le jeu de l'ami n'en a pas encore ; ensuite
elle n'est jamais écrasée, et la désinstallation la laisse en place (comme les sauvegardes). Pour la mettre à jour :
courir dans `game_dev` sur beaucoup de circuits, puis `package.ps1 -AiMap` (et la pousser sur git).

## Entraînement automatique (2026-11-20)
Idée du joueur : « faire jouer des IA contre des IA sur tous les circuits, comme ça je n'ai pas à jouer » pour remplir
`aimap_depart.dat`. `mods\entrainer-ia.ps1` (classe `ModTraining`) :
- écrit `game_dev\entrainement.scr` (Passes, Tours, Vitesse, Niveaux, Circuits, Son), passe game_dev en fenêtré
  1280×720, lance le jeu et attend ; à la fin, retire `entrainement.scr` et remet les réglages vidéo ;
- le jeu saute l'intro, et depuis le menu principal lance la démo d'origine (6 IA, aucun joueur) sur le circuit
  suivant : tous les circuits (31) dans l'ordre, autant de passes que demandé, difficulté de la passe en boucle
  (normal, expert, difficile par défaut), 6 animaux tirés au hasard parmi 12 à chaque course ;
- temps accéléré (`TimerSetSpeedUp`, ×3 par défaut ; remis après la fenêtre « Quitter ? »), son coupé, pas de
  réseau (`ModOnline.Init` sauté), pas d'élastique (`ModAI.CatchUp` = 0) ;
- la course ne s'arrête plus à la première IA arrivée (démo d'origine) mais quand les 6 le sont, ou 60 s de jeu après
  la première, ou 150 s de jeu par tour ; la mémoire est enregistrée (`ModSaveMemory`, d'habitude pas en démo) ;
- `game_dev\entrainement_suite.txt` garde la course suivante : Échap (arrête l'entraînement, le jeu reste ouvert),
  jeu fermé ou plantage → relancer le script reprend là (`-Nouveau` pour repartir de zéro) ; effacé à la fin, et le
  jeu se ferme tout seul ;
- journal : `[mod] entrainement course 5/93 (passe 1) : Data/Levels/…, difficulte expert`, puis `… finie : 6 arrivees,
  2 min 40 s de course en 0 min 55 s reelles, N images/s, pas de physique N ms`.
Ensuite : `mods\package.ps1 -AiMap` (ou `entrainer-ia.ps1 -Package`) copie `game_dev\aimap.dat` dans
`mods\data\Data\aimap_depart.dat`.

À vérifier : d'abord court (`-Circuits 0 -Passes 1`) : intro sautée, course lancée seule, caméra de la démo, temps
accéléré, la course va jusqu'à la 6e arrivée, retour au menu puis fermeture du jeu ; `memoire enregistree` dans le
journal. Si les IA tapent beaucoup plus le décor qu'en vitesse normale (pas de physique trop grand) : `-Vitesse 2`.

### Historique et rapport HTML (2026-11-20)
Demande : « save les records des IA sur chaque circuit pour voir l'évolution, dans un HTML sympa avec images et
graphiques ».
- `ModTraining.WriteHistory` ajoute à `game_dev\entrainement_historique.txt` (jamais effacé) après chaque course
  d'entraînement : `S` réglages, `P` records du joueur (menu Records), `G` géométrie de la piste des IA (portes x,y,z,
  taille du terrain), `R` la course (difficulté, arrivées, murs appris / points dangereux après la course, danger de
  chaque porte, positions des chocs), `A` chaque IA (place, temps, tours, meilleur tour, chocs, coincée, hors
  piste…). Temps en secondes de jeu depuis le feu vert, mesurés sur `m_fNowTime` (le passage de ligne est pris
  dans `Vehicle`, juste après `m_iCurrLoop++`) ; `clock=` dit si le chrono du jeu suit le temps accéléré (1).
- `mods\rapport-ia.py` (+ gabarit `mods\rapport-ia.html`) → `build\rapport_ia.html`, une page autonome, sans
  Internet : tuiles (courses, temps simulé, chocs par IA et par tour 1re → dernière course, temps du vainqueur,
  mémoire), deux graphiques course par course (chocs, temps du vainqueur en % du meilleur du circuit, moyenne par
  passe), une fiche par circuit (carte, meilleur temps IA, record du joueur, courbe du vainqueur) et une fenêtre de
  détail (carte avec curseur course par course, IA de la course, toutes les courses du circuit). Lien direct :
  `rapport_ia.html#circuit-6`.
- Carte : fond = `Data\Levels\<monde>\<monde>_Colour.png`. Le repère du terrain n'est pas connu : le script essaie
  les 8 orientations × 2 origines et garde celle où la hauteur des portes colle le mieux à `<monde>_Height.png`
  (corrélation ≥ 0,6), sinon piste seule sur fond uni. Vérifié sur un faux historique tracé sur les vrais reliefs
  (orientation retrouvée sur les 4 circuits essayés, corrélation 0,92 à 1).
- `entrainer-ia.ps1` ouvre le rapport au départ et le refait toutes les 30 s pendant l'entraînement (la page se
  recharge seule), puis une dernière fois à la fin ; `-SansRapport` pour s'en passer.
À vérifier sur un vrai entraînement : fonds de carte bien alignés sur la piste (sinon : ligne `corr` à regarder
dans les données), `clock=1` dans les lignes R, temps des IA comparables au record du joueur (si le record a été
fait avec un autre nombre de tours, la comparaison ne vaut pas).

## Premier vrai entraînement : corrections (2026-11-20)

71 courses (passes normal / expert / difficile) : pas d'évolution visible, et des IA bloquées sur les variantes.
Diagnostic dans `game_dev\entrainement_historique.txt` et `crash.log` :

- **Dangers saturés** : la table des points dangereux est commune aux 6 IA ; chaque choc y ajoutait +0,5 (coincée
  +0,35), plafonné à 1, et la mémoire n'en oubliait que 10 % par course. Dès la 1re course, presque toutes les portes
  étaient à 100 % : les IA roulaient partout au milieu du couloir en freinant, plus rien à apprendre.
  → v2 : un incident compte pour 1/(nombre d'IA) ; mémoire = moyenne glissante (`ModAIMap._KEEP` 0,6 : ancienne × 0,6
  + course × 0,4), une porte sans incident redescend ; en course, danger = mémoire + relevé de la course.
  Nouvelle clé `circuit|sens|portes|dangers` dans aimap.dat : les anciennes lignes de dangers sont ignorées et
  retirées au prochain enregistrement, **les murs appris sont gardés**.
- **Remise en piste en boucle** (surtout les variantes, en sens inverse : 39 remises en une course sur Las2
  Track2, toujours aux portes 10-11 et 20-21) : `Vehicle.Respawn` repose la voiture 2 portes avant, elle revient sur
  le même obstacle. → 2e remise au même point en moins de 60 s : posée après l'obstacle (`VehicleAI.ModRespawnAhead`,
  jamais par-dessus la ligne). La ligne « remise en piste » de crash.log donne la position et la hauteur du sol, pour
  comprendre ces obstacles (pente infranchissable dans ce sens ?).
- **Cartes du rapport sans fond** : les portes ont y = 0, la corrélation avec le relief ne trouvait rien. → la ligne
  G de l'historique prend la hauteur du sol (`TerrainHeight`) ; le rapport préfère une géométrie avec hauteurs.
- Fausse piste écartée : le temps ×3 accélère bien la physique (pas de 3,8 ms, comme ~260 images/s).
- `entrainer-ia.ps1` : `-Niveaux expert` par défaut (courbe lisible) ; `-NouvelHistorique` range l'ancien historique.

### Courses retour et tours impossibles (2026-11-20, suite)

- **Carte de l'IA fausse en course retour** : `RacerGame.LoadLevel` → `InitModule` → `SetStartPos` calcule la carte
  (`ModAIMap.For`) sur les portes dans le sens aller ; `CoreLogic.PrepareTrackBeforeRace` retourne ensuite le
  circuit (`RacerModule.ReverseTrack`), et `For` ne refaisait rien (même objet Track). Porte i de la carte = ancienne
  porte i, ailleurs sur la piste → trajectoires absurdes et blocages sur les 15 circuits retour (3-5, 9-12, 16-17,
  21-23, 27-29). → `ModAIMap.OnReverse()` dans `RacerModule.ReverseTrack` : carte refaite au `SetStartPos` qui suit.
- **Sens de la clé** : `CoreLogic.IsReverseTrack()` lu au chargement = sens de la course précédente. → `m_bReversed`
  (faux dans `LoadLevel`, vrai dans `OnReverse`). Clés v2 `niveau|aller|n` / `niveau|retour|n` ; tout l'ancien
  aimap.dat (v1) est ignoré et retiré au prochain enregistrement : mémoire à refaire (murs v1 appris sur une carte
  fausse ou rangés sous le mauvais sens).
- **Meilleur tour IA de 0:11.78** : une IA qui recule par-dessus la ligne perd un tour (`Vehicle`) puis le reprend ;
  `ModTraining.OnLap` comptait chaque passage. → passages rangés au numéro de tour du jeu (`m_iCurrLoop`) ; le rapport
  écarte aussi les tours < 50 % du tour médian de la course (anciens historiques).
- Rapport : « course retour » d'après le numéro de circuit (`REVERSE`), plus d'après la clé.

### Accélération automatique (2026-11-20)

Le temps réel d'une course, c'est la course elle-même (chargement ~2 s). À ×3 et ~1 000 images/s, la physique
avançait par pas de 2,8 ms, bien plus fin qu'un joueur à 144 images/s (7 ms). `ModTraining.RaceFrame` règle
maintenant `TimerSetSpeedUp` toutes les 0,5 s réelles : accélération = 7 ms (`_TARGET_STEP`) / durée réelle d'une
image, entre 1 et `Vitesse` (défaut 12, maxi 20), départ à ×3. Sur ce PC : ~×7, soit ~2,3 fois plus rapide qu'avant,
sans conduite différente de celle d'un joueur à 144 images/s. crash.log et la ligne R (`speed=`) donnent
l'accélération moyenne réelle de chaque course. (Remplace le « ×3 par défaut » et le conseil `-Vitesse 2` plus haut.)

### Entraînement sans bonus (2026-11-20)

Par défaut, plus de boîtes bonus pendant l'entraînement (`RacerModule.InitModule`, même chemin que l'en ligne sans
bonus : `DeleteBonuses`). Un tir, une flaque ou un enlèvement envoyait l'IA dans le décor ou la coinçait, et la
mémoire apprenait ces chocs comme des murs ou des endroits dangereux. `entrainer-ia.ps1 -Bonus` (Bonus(1) dans
entrainement.scr) les garde.

### Vitesses de passage et chocs de face (2026-11-21)

Retours du joueur sur les 4 circuits encore difficiles après l'entraînement :
- Hiver 3 (aller et retour) : un saut (ou une pente qui fait sauter) suivi d'un virage ; tout le monde y arrive à
  fond et finit dans le décor (« même un joueur s'il ne le sait pas »).
- Forêt 3 retour : un virage serré avec un arbre (trop vite = arbre) ; un passage plein de rochers, plusieurs
  chemins possibles, les IA en prennent souvent un.
- Tropiques 1 : deux chemins qui se rejoignent avec un arbre au milieu ; un virage pris trop large contre un arbre.

Deux manques de l'IA :
- **pas de mémoire de vitesse** : elle ne freine jamais (choix du 2026-11-20) et ne lâche l'accélérateur que d'après
  l'angle du virage, sans savoir qu'à cet endroit elle arrive trop vite. → `ModAIMap.m_aSpeed` : vitesse de passage
  permise par porte (part de la vitesse maxi de l'animal), dans aimap.dat (`clé|vitesses=i:pourcent,…`, seulement les
  portes limitées). Choc contre le décor à plus de 55 % de la vitesse maxi : la porte passée juste avant descend à
  92 % de la vitesse qu'on y avait, celle d'encore avant à 96 % (mini 50 %). Chaque passage propre (pas de choc
  jusqu'à deux portes plus loin) la remonte de 0,6 point. En conduite (`ModSpeedControl`) : au-dessus de la limite de
  la porte visée (et de la suivante à moins de 1 200), accélérateur lâché ; 8 % au-dessus, **frein** (seule exception
  au « jamais de frein »). crash.log : « trop vite avant le point i, vitesse apprise aux portes g:85% » ; bilan
  « vitesses limitees a N portes » ; historique : `speeds=` / `cuts=`.
- **choc de face jamais appris** (`Learn` : « on ne sait pas de quel côté passer ») → on passe du côté où le
  couloir a le plus de place, à 3 pas (105) de la voiture au moment du choc.

## IA chrono (2026-11-21)

Idée du joueur : une IA qui optimise sa trajectoire par évolution, à part de l'IA experte du multi. Le pilote reste
celui de l'IA améliorée (portes, freinage, dégagement) ; ce qui apprend, c'est son **plan de course** par circuit, sens
et animal, porte par porte (`mods/java/ModChrono.java`) :
- **décalage** : où passer dans la porte, borné au couloir libre de la carte (`ModAIMap.m_aLo/m_aHi`) ;
- **limite** : vitesse de passage permise (part de la vitesse maxi, 1 = aucune) ;
- **prudence** : × la prudence au lâcher d'accélérateur avant le virage (`ModSpeedControl`), 0,3 à 2,5.
Plan de départ = ce que l'IA experte ferait sans hasard (ligne « large, corde, large » + vitesses apprises d'aimap.dat).

Vérifications faites avant :
- **Chocs entre IA** : le drapeau 32 (`_BLOCK_MOVE`) retiré à chaque voiture (déjà fait pour les fantômes en ligne et
  l'OVNI) → les 6 voitures se traversent. L'évitement (`ModAvoid`) est coupé.
- **Écarts de chrono** : le chrono au tour du jeu n'avance que toutes les 0,1 s (`Vehicle.UpdateCurrentGate`, appelé
  toutes les `_GATES_CHECK_TIME`) → trop grossier. Le temps est pris au passage des portes de l'IA (`ModOnGatePassed`,
  à chaque image). Le départ au hasard (×0,6-1,4) est coupé après le départ (`DisableRandomAccel`). En mode chrono,
  pas de hasard de conduite : précision, couloir, corde, largeur, vitesse ×1, pas de bruit de trajectoire, pas de
  points dangereux (ils changent pendant la course), rien d'appris en course (murs, vitesses) : seul le plan diffère
  d'une voiture à l'autre. Il reste le pas de physique, qui varie avec les images/s. **La 1re course d'un circuit fait
  rouler 6 fois la même base** : l'écart-type entre ces 6 voitures est le bruit d'une mesure (journal et rapport).

Une course chrono (6 fois le même animal, `-Animal`, Ours par défaut) :
- voiture 0 : la base (mesurée à chaque course, moyenne glissante sur 4 courses) ;
- jusqu'à 2 voitures : revérification des essais prometteurs de la course d'avant ;
- les autres : une variante, un bout de 3 à 9 portes modifié en cloche (trajectoire, vitesse ou les deux, 3 tailles
  de pas).
Note = moyenne des tours lancés, d'un passage de la porte du milieu de la piste au suivant (2 tours lancés sur 3), +0,3 s
par choc contre le décor ; remise en piste, dégagement ou portes sautées = échec. Un essai meilleur que la base de plus
de max(0,02 s ; ½ bruit) est revérifié ; la moyenne des deux mesures meilleure de max(0,02 ; ½ bruit) → il entre dans la
base (plusieurs à la fois s'ils touchent des portes différentes). Si la nouvelle base, remesurée 3 fois, est plus lente
que l'ancienne de plus d'un bruit : annulée.

Essai hors du jeu (`ModChrono` réel + faux circuit de 40 portes, meilleure ligne et vitesses tirées au hasard, chocs si
trop vite, bruit 0,08 s puis 0,4 s) : vrai tour de la base 66,4 → 50,2 s en 300 courses (91 améliorations sur 1 329
essais) ; avec 5 fois plus de bruit, 66,4 → 54,7 s, pas d'emballement. Relecture d'aichrono.dat OK.

Fichiers :
- `game_dev\aichrono.dat` : une ligne par `circuit|sens|portes|animal`, champs `clé=valeur` séparés par des
  tabulations (`off=` décalages, `plan=` limite%:prudence% par porte, base, record, bruit…). **aimap.dat n'est ni
  modifié ni enregistré en mode chrono** (`ModSaveMemory` sauté).
- crash.log : `[mod] chrono <clé> : course N, porte de reference …, voitures : 0 base, 1 essai portes 12-16 ligne +0.31…`
  puis `… finie ; 0 base 31.52, 1 essai 31.40, … ; essai … prometteur / AMELIORATION … ; base …, record …, bruit …`.
- historique : ligne `chrono=1` dans R, et une ligne `C` (base, mesure, record, bruit, décision, trajectoire de la base
  en x,z, plan). Le rapport HTML a une section « IA chrono » (carte avec la trajectoire et les vitesses limitées, courbe
  de la base et du record, décisions) ; ces courses ne comptent plus dans les courbes des IA normales.

Lancer : `powershell -ExecutionPolicy Bypass -File mods\entrainer-ia.ps1 -Chrono` (Forêt 1, 100 courses, expert, Ours) ;
`-Circuits 0,6 -Passes 300 -Animal Lion` pour d'autres réglages. Réglages : `ModChrono._SIGMA_*` (taille des essais),
`_HIT_PENALTY`, `_SCREEN_K` / `_CONFIRM_K` (prudence de l'adoption).

À vérifier au premier essai : ligne `bruit mesure` de la 1re course (s'il dépasse ~0,3 s, il faudra plus de tours par
course ou plusieurs mesures par essai) ; les voitures se traversent bien ; `AMELIORATION` apparaît dans les 10-20
premières courses ; la base baisse dans le rapport. Ensuite, si les records battent nettement l'IA experte : donner ces
plans à l'IA du jeu.

### IA chrono : 1er essai réel et corrections (2026-11-21)
50 courses sur Forêt 1 (Ours, ~2 min par course chargement compris). Constats :
- courses 1-18 : presque toutes les voitures remises en piste (le plan de départ se jetait dans le décor vers les
  portes 18-26) → notées « échec », base jamais mesurée. Une amélioration là (vitesse -0,09, prudence ×0,67) a tout
  débloqué : course de la base 3:45 → 3:08 (-14,6 %).
- record resté à 0 : seuls les tours sans choc comptaient, aucun tour n'en avait.
- bruit 2-3 s : un même plan fait 57,5 s ou 65,5 s au tour selon qu'il tape une fois de plus (un choc coûte 3-8 s).
Corrections : remise en piste / dégagement = temps perdu + 3 s (plus un échec) ; choc 1 s ; record = vrai meilleur
tour ; une variante sur deux est centrée juste avant les portes où les voitures tapent (`hot=` dans aichrono.dat) ;
notation version 2 (`score=2`) : un vieux plan est gardé mais sa base remesurée (6 fois) ; essais à revérifier gardés
en mémoire par circuit (l'entraînement change de circuit à chaque course). Lignes A : `car=` et `role=`.
Rapport : tuiles globales, évolution de tous les circuits (% des 5 premières courses), une fiche par circuit avec
meilleurs tours 1/2/3 et meilleure course (chronos officiels du jeu), détail au clic (#chrono-<n>). Temps en m:ss,cc
au-delà d'une minute. `entrainer-ia.ps1 -Chrono -Circuits tous` : les 31 circuits, 20 passes par défaut (~20 h).

### IA chrono : analyse des 100 courses de Forêt 1 et 2e réglage (2026-11-21)
Depuis la course 20, plus aucun progrès (tour lancé de la base 59,2 → 60,1 s) malgré 10 « améliorations » : un tour
fait 57-58 s ou 65-66 s (accident +8 s, ~1 tour sur 4), et 2,9 chocs par voiture et par course à la porte 8 (~1 par
tour), 1,1 à la porte 1 (départ). Les « améliorations » étaient des essais qui avaient juste évité l'accident.
Changements : validation d'un essai sur sa 2e mesure seule (plus de moyenne avec la 1re, choisie parce que chanceuse) ;
essais centrés sur un endroit de chocs : bout de 5 à 9 portes, pas ×1,5 à ×4, vitesse toujours baissée ; la base change
de voiture à chaque course (grille : voitures 3-5 1,4 s plus rapides au 1er tour) ; `-Chrono` = 8 tours par défaut
(7 tours lancés par mesure). Faux circuit, même bruit : 66,4 → 50,3 s en 300 courses (57,6 s avant), base estimée juste.
Rapport : la courbe suit le tour lancé moyen de la base (comparable entre 3 et 8 tours), fiches : tour 1, meilleur
tour lancé, meilleure course du nombre de tours actuel.

### IA chrono : le départ compte dans la note (2026-11-21, après 23 courses de 8 tours sur Forêt 1)

Bilan des 23 courses à 8 tours : tour lancé de la base 59,5 → ~57 s, record 52,99 s, bruit 3 → 1,2 s, chocs pendant
les tours lancés 7-10 → 1-3 par voiture et par course ; l'arbre de la porte 8 a presque disparu (chaleur 29 → 2,6).
Reste l'arbre entre les portes 1 et 2 (351 chocs sur 601, x ≈ 12100, z ≈ 6370) : 115 de ces chocs au départ, surtout
les voitures 0-2 de la grille (1 choc à chaque départ, 2 dans ~25 % des départs : tour 1 à 71,5 s au lieu de 66,3 s).
Or la note ne comptait que les tours lancés (porte 21 → porte 21) : le départ (ligne → porte 21 au 1er tour) n'y
était pas, rien ne poussait à corriger ce choc. Maintenant (notation 3) : coût du départ = temps ligne → porte 21
moins la médiane des voitures de la course + 1 s par choc + 3 s par remise en piste, divisé par le nombre de tours
lancés et ajouté à la note (un arbre au départ ≈ +0,85 s). 6e champ de cars= dans la ligne C : ce coût. Base
remesurée une fois (6 x la base) ; le record est gardé.

### Entraînement : reprise et remises en piste du jeu (2026-11-21)

- `-Chrono -Circuits tous -Passes 10` lancé juste après un Forêt 1 seul arrêté à la course 3 : reprise à la course 4 =
  circuit n° 3, Forêt 1, 2 et 3 sautés à la 1re passe. entrainer-ia.ps1 garde maintenant les réglages
  (game_dev\entrainement_suite_reglages.txt : chrono, circuits, tours, niveaux, animal ; pas -Passes) et ignore la
  reprise s'ils ont changé.
- Forêt 1 · variante (1re course, 6 x la base) : des tours à 47 s au milieu de tours à 54 s, sur 4 voitures sur 6
  (record chrono 47,02 s, joueur 51,97 s). Pas de raccourci par les portes (aucun saut de 2-3 portes ne gagne > 4000).
  Piste : Vehicle.RespawnForward (zone de remise en piste du jeu touchée : voiture posée au point suivant), pas
  comptée jusqu'ici. Compteur VehicleAI.m_nModWarp (warp= dans les lignes A), compté comme une remise en piste dans
  la note chrono (tour exclu du record). À vérifier au prochain passage sur ce circuit.
- Désert 1 variante (1re course) : ~11 chocs par tour, 160-250 s hors piste par voiture, tours de 88-100 s (IA normale :
  63-67 s). Presque tous les chocs entre les portes 39 et 40 (virage serré à la 39 puis 14 500 de ligne droite). Le
  plan de départ de l'IA chrono ignorait les points dangereux appris (aimap.dat : 0,65 à la 39, 0,74 à la 40) que
  l'IA normale applique en prudence (x 1 + 0,8 x danger). Plan de départ v2 (init=2 dans aichrono.dat) : prudence
  initiale = 1 + 0,8 x max(danger porte, danger suivante). Les plans sans amélioration (gen=0) d'avant sont refaits
  et remesurés ; Forêt 1 (déjà 18 améliorations) garde le sien.

### IA chrono : le jeu ralentit quand il tourne longtemps (2026-11-21, 298 courses en 7 h 49 sur les 31 circuits)

- Bilan : 47 améliorations sur 1809 essais, 11 circuits sans aucune, 177 « rien de mieux ». Gains nets là où ça tapait :
  Désert 3 1:35 → 59,6 s (surtout le plan de départ v2), Désert 2 1:00,4 → 49,0 s, Forêt 1 variante 56,9 → 49,0 s.
- Dérive : les 6 voitures (base et essais) ralentissent ensemble, plan inchangé et sans choc : Royal 27,95 → 30,9 s,
  Lune 2 49,2 → 53,9 s, Forêt 2 variante 39,2 → 41,6 s (+6 à 9 %). Les sessions de 50 courses du matin (~35 min) n'ont
  aucune dérive (Forêt 1 58,0 s de bout en bout). Même temps : images/s 1200 → 700, pas de physique 6,8 → 10,6 ms
  (cassure vers la course 170, alors que la dérive est régulière). Cause pas prouvée : précision de l'horloge du
  moteur (GetTime, un float qui grandit) ou usure du jeu. Lignes R : getTime0/getTime1 (horloge du module au départ
  et à l'arrivée), uptime (min réelles depuis le lancement), launch_race (courses depuis le lancement) pour trancher.
- Effet : Select comparait les essais à m_fBase, moyenne glissante des courses d'avant (plus rapides) : chaque essai
  paraissait ~1 s plus lent ; le bruit (écart base du jour / moyenne) était gonflé aussi (Royal 0,97 s alors que les 6
  voitures d'une course sont à 0,1-0,2 s). L'annulation (base nouvelle vs ancienne, à des courses d'écart) aussi.
- Corrigé (select=2 dans aichrono.dat) :
  - 2 voitures de base par course ; chaque essai est jugé sur son écart à leur moyenne, dans la MÊME course ;
  - bruit = variance entre les 2 voitures de base (moyenne simple sur 5 mesures, puis glissante) ; l'ancien est
    gardé jusqu'à la 1re mesure ;
  - après une amélioration, la 2e voiture de base roule 3 courses avec la base d'avant (rôle « ancienne ») : la
    nouvelle plus lente qu'elle de plus d'un bruit est annulée ;
  - entrainer-ia.ps1 -Relance 31 (défaut) : le jeu se ferme toutes les 31 courses (entrainement_relance.txt) et le
    script le relance ; contrôle en cours et essais à revérifier sont maintenant dans aichrono.dat (prev_*, pend,
    p0_*, p1_*), plus seulement en mémoire (perdus à chaque relance sinon).
- Pièges pas réglés : Tropiques 1 variante porte 41 (~55 coincées par course), Tropiques 1 porte 10 (+40-60 s quand
  ça coince), Désert 1 variante porte 40 (~10 chocs par tour). Les variantes de quelques portes n'en sortent pas.

### IA chrono : les trois pièges, bornes (2026-11-22)

- Cause commune : entre deux portes, la voiture va tout droit d'une cible du plan à la suivante. Ces trois tronçons
  sont très longs, et la ligne droite traverse le décor. La carte (ModAIMap) ne teste que des couloirs parallèles
  (même part de la largeur aux deux portes), jamais la vraie ligne du plan.
  - Tropiques 1, portes 9 → 10 (10 km), et sa variante, portes 40 → 41 : même tronçon, l'îlot d'arbres entre deux
    routes. Les chocs tombent pile sur la ligne du plan, à 5 km de la porte 9 (aller) et à 2 km de la porte 40
    (variante).
  - Désert 1 variante, portes 39 → 40 (14,5 km) : les chocs dessinent une droite qui rejoint la ligne du plan vers
    11-12 km. C'est le bord de la route : les voitures le frottent puis se coincent au bout. Le couloir de la porte
    39 est tenu à 410..450 par les murs appris des IA normales (aimap.dat).
- Corrigé (ModChrono, bornes) :
  - Chaque choc d'une voiture à moins de 250 de sa ligne note la porte visée, la position et le côté libre (normale du
    choc, ou le côté de la voiture si le choc est de face).
  - 2 chocs à moins de 200 l'un de l'autre font une borne, gardée pour toujours (aichrono.dat, bornes=).
  - Tous les plans (base, base d'avant, essais, revérifications) passent à 160 au moins de chaque borne, du côté
    libre : les décalages des deux portes du tronçon bougent le moins possible, dans leur couloir.
  - Historique, ligne C : bornes (porte, x, z, chocs), bornes_new, bornes_fixed (portes de la base déplacées),
    bornes_bad (impossibles à respecter), bornes_hits. Rapport : losanges sur la carte.
- Le premier passage sur chaque circuit apprend les bornes, le suivant les contourne. Simulation hors jeu sur les
  chocs de Désert 1 variante : porte 40 de +84 à environ −52, la 39 reste bloquée par son couloir. Si les voitures
  frottent plus loin, de nouvelles bornes s'ajoutent à la course d'après.
