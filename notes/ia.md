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
   (angle actuel + virage de la porte). Elle lâche l'accélérateur, ou freine vraiment si c'est très en dessous
   (au-dessus de 45 % de la vitesse maxi).
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
