# Bonus (chantier 5)

Correctifs des bonus, tirage selon la place (`Data\bonuses.scr`) et Fusée rendue jouable. Choix retenus le
2026-11-18 : le **turbo reste tel quel** (×1,7 compté deux fois, soit environ ×2,9 en vrai : c'est le jeu d'origine) ;
le **Rocher n'est pas fait** (bonus inachevé, voir plus bas).

## Fusée
Codée par Techland mais jamais ramassable : absente des listes de bonus des mondes (seulement dans les listes par
animal, et le tirage prend ce qui est dans les deux). Vue en jeu avec le bonus imposé du menu Mods : pas d'icône, et
elle « explose sur la cible sans effet ».

- **Icône** : `Hud\Rakieta.png` était l'image source, en 404x219. Les icônes qui marchent sont en 128x128 (puissances
  de 2), le moteur n'affichait rien. Nouvelle icône 128x128, fusée en diagonale (`mods/art/make_hud_icons.py`).
- **Pas d'effet** : la fusée volait comme la noix de coco (`TrackingDevice`) : départ à la vitesse du tireur, +750/s²,
  5500 au plus, 5 s de vie, pour des voitures qui roulent à 4500 (×2,9 en turbo, plus avec un animal rapide). Elle
  rattrapait mal sa cible et, au bout de ses 5 s, explosait juste derrière elle. `OnMiss` (raté) joue la même
  explosion que `OnReachTarget` (touché), d'où l'impression d'un coup au but sans effet.
  Corrigé dans `Rocket` (mod) : départ à 5000 au moins, +4000/s², 10000 au plus ; tant qu'elle suit une cible, les
  contacts avec le décor sont ignorés (sa trajectoire suit déjà le relief, 100 au-dessus du sol, et `rakietaBonus.3DA`
  est le seul projectile sans `.colmap`). Effet au but inchangé : `Vehicle.ExplosionEffect`, la voiture saute en
  tournant comme avec la bombe, puis 2 s d'arrêt.
- Le tireur n'était pas vérifié dans `OnMiss` (la noix de coco le fait) : ajouté.
- **Journal** (`crash.log`), pour chaque fusée tirée par ou sur un humain :
  `[mod] fusee : lancee par joueur sur IA Lion a 1840`, puis `touche IA Lion apres 0.6 s` ou
  `ratee (temps ecoule, a 230 de IA Lion)` / `ratee (sans cible, fin de course)` / `ratee (joueur protege)`.
  Sans cible verrouillée (viseur absent), elle file tout droit 2 s puis explose : c'est normal.
- Ajoutée à tous les mondes par `bonuses.scr` (`partout = 1`), sauf les courses extra à règle (turbo seul, bombes
  seules, sans bonus), marquées par `LevelBonusTypes.m_bModFixed` dans `RacerModule.InitModule`.
- L'IA savait déjà s'en servir (`VehicleAI`, type d'usage 20), comme de tout projectile à tête chercheuse.

## Missile (nouveau bonus du mod, n° 21)
Demandé le 2026-11-18 : un bonus qui vise le premier de la course, quelle que soit la distance. C'est **la Fusée**
(même forme, même animation, type d'usage 20), marquée `Rocket.m_bModMissile`. Essayé en jeu le 2026-11-18 : tous les
tirs du journal touchent (cible à 12 000 – 48 000, 1,3 à 4 s).
- **Icône** (2026-11-18) : `Hud\Missile.png`, la fusée de la Fusée recolorée bleu et argent (flamme bleutée) avec une
  pastille dorée « 1 » (`mods/art/make_hud_icons.py`). 22e image de `KidsGadget` (`_GADGET_MOD_MISSILE` = 21) ;
  `RacerCamera.GetCurrentGadgetIcon` ne voit qu'une fusée (type d'usage 20), `KidsHud.ModGadgetIcon` la remplace.
- **Modèle** (2026-11-18) : `Meshes\Objects\missileBonus.3DA`, copie de `rakietaBonus.3DA` dont seul le nom de texture
  change (`missileBonus.png`, même recoloration que l'icône) : `mods/art/make_missile_model.py`. Chargé par
  `Rocket.ModCreateMissile` (copie de `UseItem.CreateRocket` avec un autre `MeshName`). Une vraie forme à lui
  demanderait d'écrire un `.3DA` : voir `notes/format_3da.md`.
- **Cible** : choisie au moment où elle part (`Rocket.Init` → `ModFindLeader`) : la première voiture du classement
  (`RaceParams.m_SortTable`) qui n'est pas le tireur et n'a pas fini la course. Le tireur est 1er → elle vise le 2e.
  Elle vole en ligne droite vers la cible, 100 au-dessus du sol, à travers le décor (comme la Fusée qui suit une cible).
- **Vitesse** : départ 6000, +8000/s², 16000 au plus (une voiture en turbo fait environ 13000) ; 20 s de vie.
- **Effet** : celui de la Fusée (`Vehicle.ExplosionEffect` : saut en tournant, 2 s d'arrêt). La protection l'arrête.
- **Viseur** : aucun chez le joueur (`Vehicle.AimAtEnemy`) : il serait sur la voiture de devant, pas sur la cible.
  L'IA, qui ne tire un projectile que quand elle a une cible verrouillée, reçoit le premier comme cible tout de suite.
- **Tirage** : `Bonus("Missile", 1, 0, 0, 1, 3, 5, 6)` dans `bonuses.scr` (tous les mondes, jamais aux 2 premières
  places, plus rare que la Fusée). Les listes par animal ne le contiennent pas : `AnimalBonusTypes.GetIntersection`
  laisse passer tout numéro ≥ 21.
- **Menu Mods** : « Missile » en dernier choix du bonus imposé (`ModOptions._BONUS_MAX` = 21, `make_texts.py`).
- **Journal** : `[mod] missile : lance par joueur (5e) sur le premier IA Lion a 8200`, puis `touche IA Lion apres 1.9 s`
  ou `ratee (…)` ; aussi quand c'est le joueur qui est visé. Tireur en tête : `… (1e) sur le 2e IA Lion …`.
- Plus tard : une forme à lui (`notes/format_3da.md`) ; éventuellement une explosion qui touche aussi les voitures
  proches du premier.

## Correctifs
| Défaut | Où | Correctif |
|---|---|---|
| Turbo, Pointes, Protection gardaient la case bonus pendant tout leur effet (10 s) : on passait dans les boîtes sans rien prendre | `Vehicle.CollectBonus` | au ramassage suivant, l'effet en cours est mis de côté (`ModParkActiveBonus`) : il continue, son icône cesse de clignoter, le nouveau bonus prend la case. `DisableTurbo` / `DisableTireSpikes` / `DisableProtection` retrouvent l'objet dans la case ou de côté (`ModEndActiveBonus`) au lieu d'effacer la case quel que soit son contenu |
| 2e protection pendant la 1re : son attaché deux fois | `Vehicle.UseBonus` | ancien son détaché d'abord |
| Pistolet à eau : ignorait la protection (le pistolet à glace la testait) | `Vehicle.UseBonus` | même test |
| Bombe : une ligne `m_vParentVehicle: …` écrite dans le journal par image et par voiture pendant l'explosion | `Bomb.UpdateExplosion` | ligne retirée |
| Fusée : pas d'icône, pas d'effet, tireur non vérifié | `Rocket`, `Hud\Rakieta.png` | voir plus haut |

Triche « bonus infinis » : une case occupée par un turbo, des pointes ou une protection déjà activés compte comme vide.

## Tirage selon la place : Data\bonuses.scr
```
!Bonus(s,f,f,f,f,f,f,f)
Bonus("Fusee", 1, 0, 1, 3, 6, 9, 10)    // partout, poids du 1er … 6e
```
- **Fichier lu par le jeu : `game_dev\Data\bonuses.scr`.** Source : `mods\data\Data\bonuses.scr` (recopiée par
  `mods\build.ps1`, qui écrase une modification faite directement dans `game_dev`).
- Lu au lancement (`RacerGame.OnInitialize` → `ModBonuses.Load`). Journal :
  `[mod] bonus : Data/bonuses.scr lu, 21 bonus, tirage selon la place, ajoutes a tous les mondes : Fusee`.
- Tirage (`LevelBonusTypes.GetRandomBonus` → `ModBonuses.Pick`) parmi les bonus du monde en cours, comme à
  l'origine, mais pondéré par la place. Celle-ci est ramenée à 6 crans : en duel (aventure), le 1er tire avec p1 et
  le 2e avec p6. Toujours pas deux fois de suite le même bonus quand un autre est possible. Humains et IA.
- `partout = 1` : ajouté à la liste de chaque monde (`AnimalBonusTypes.Init2` → `ModBonuses.ExtendLevel`, qui
  précharge aussi le modèle comme pour les bonus d'origine).
- Noms : Bombe, Bouchon, Catapulte, Turbo, Protection, Huile, Gaz, Bouteille, Banane, Guepes, Enclume, Pointes,
  Tomates, Pasteque, Glace, Eau, Coco, Neige, Cookie, Fusee, Ovni (ou le numéro 0…20 de `LevelBonusTypes`).
- Bonus sans ligne : poids 5 partout. Poids hors 0…1000 : 5, avec une ligne au journal. Fichier absent ou illisible :
  tirage d'origine (uniforme, pas de Fusée).
- Chaque bonus ramassé par un humain est noté : `[mod] bonus : joueur 5e sur 6 (cran 5) -> Fusee`.

Valeurs livrées : devant, surtout des pièges à laisser derrière soi (les projectiles ne visent qu'une voiture devant,
dans un cône de ±60° et à moins de 3000) ; derrière, Fusée, OVNI, turbo et projectiles.

| Famille | Bonus | 1er | 2e | 3e | 4e | 5e | 6e |
|---|---|---|---|---|---|---|---|
| Pièges | Catapulte, Huile, Gaz, Bouteille, Banane, Enclume | 10 | 8 | 6 | 4 | 3 | 2 |
| Sur soi | Protection | 6 | 6 | 5 | 4 | 3 | 3 |
| | Pointes | 4 | 4 | 4 | 4 | 4 | 4 |
| | Turbo | 1 | 3 | 5 | 7 | 9 | 10 |
| Projectiles | Bouchon, Guêpes, Tomates, Coco, Neige, Cookie, Glace, Eau | 1 | 6 | 8 | 8 | 8 | 8 |
| Zone | Bombe, Pastèque | 2 | 6 | 7 | 7 | 7 | 7 |
| Forts | Fusée (partout), OVNI (Lune seulement) | 0 | 1 | 3 | 6 | 9 | 10 |
| | Missile (partout, bonus du mod) | 0 | 0 | 1 | 3 | 5 | 6 |

Listes d'origine des mondes : partout Bombe, Bouchon, Catapulte, Turbo, Protection, Huile, Gaz, Cookie ; Forêt +
Guêpes, Tomates ; Désert + Pastèque, Coco ; Tropique + Banane, Eau ; Hiver + Bouteille, Enclume, Pointes, Glace,
Neige ; Lune + Pointes, OVNI ; Royal : rien de plus. Les listes par animal (`AnimalBonusTypes`) sont identiques pour
les 12 et contiennent tout : elles ne filtrent rien.

## Rocher : abandonné
Bonus commencé puis laissé par Techland (type d'usage 21, classe `FallingRock`). Sur le modèle de l'OVNI : une
télécommande en main gauche (`pilot.3da`), on vise, on appuie, la cible est bloquée 3 s (`Vehicle.FallingRock`,
`WAIT_AFTER_ROCK_TIME`). Il manque l'essentiel : aucun numéro dans `LevelBonusTypes` (impossible à donner), aucun
rocher qui tombe (`FXFallingRock` est vide), pas d'icône HUD (une image source `Hud\Glaz.png` 265x167 existe), pas de
son. L'IA sait l'utiliser. Le finir = environ une session ; non retenu.

## Fichiers
`ModBonuses` (nouveau), `LevelBonusTypes`, `AnimalBonusTypes`, `Rocket`, `Bomb` (copiés de `decompiled\`), `Vehicle`,
`RacerModule`, `RacerGame`, `ModOptions`, `KidsGadget`, `KidsHud` (Missile) ; `Data\bonuses.scr`, `Data\Texts.scr`
(`mods/art/make_texts.py`), `Textures\Hud\Rakieta.png` et `Missile.png` (`mods/art/make_hud_icons.py`),
`Meshes\Objects\missileBonus.3DA` et `Textures\missileBonus.png` (`mods/art/make_missile_model.py`).

## Plus tard
- En ligne (chantier 8) : `bonuses.scr` doit être le même chez tous (ou imposé en partie classée), comme `animals.scr`.
- Les autres projectiles à tête chercheuse (noix de coco, boule de neige, cookie, bouchon, tomate) ont toujours les
  réglages lents d'origine : s'ils ratent souvent leur cible, même remède que la fusée.

## À vérifier en jeu
1. `crash.log` au lancement : `[mod] bonus : Data/bonuses.scr lu, 22 bonus, …, ajoutes a tous les mondes : Fusee Missile`.
2. Fusée (menu Mods → bonus imposé « Fusée », ou en course normale en étant derrière) : icône à la place habituelle des icônes de bonus, viseur sur une voiture devant, tir → la fusée rattrape la cible, qui saute en tournant puis repart 2 s après.
   `crash.log` : `[mod] fusee : lancee par joueur sur …` puis `touche …`. Si c'est `ratee (…)`, me donner la ligne.
3. Prendre un turbo, l'activer, passer dans une boîte pendant l'effet : nouveau bonus dans la case, le turbo continue
   jusqu'au bout. Pareil avec la protection (la bulle reste, puis disparaît à la fin) et les pointes (Lune, Hiver).
   Un nouveau bonus pris pendant l'effet ne disparaît pas quand l'effet se termine.
4. Deux turbos de suite (bonus imposé Turbo + bonus infinis) : le 2e prolonge le 1er, rien ne se bloque.
5. Tirage : une course en tête puis une en queue de peloton ; les lignes `[mod] bonus : joueur Ne sur 6 -> …`
   montrent surtout des pièges devant, des fusées / turbos / projectiles derrière. La Fusée sort aussi chez les IA.
6. Pistolet à eau (Tropique) sur une voiture protégée : sans effet.
7. Bombe : plus de lignes `m_vParentVehicle` dans `crash.log`.
8. Courses extra « turbo seul » / « bombes seules » : toujours rien d'autre (pas de Fusée).
9. Missile : menu Mods → bonus imposé « Missile », course en étant derrière. En main : pas de viseur. Tir → la
   fusée part vers le premier, même loin et hors de vue ; il saute en tournant. `crash.log` :
   `[mod] missile : lance par joueur (Ne) sur le premier …` puis `touche …`. Si c'est `ratee (temps ecoule…)`, me
   donner la ligne (premier trop loin pour 20 s ?). En tête : il vise le 2e.
10. Missile chez l'IA : en queue de peloton, une IA qui l'a le tire vite ; ligne `[mod] missile : … sur le premier joueur`
    quand c'est le joueur qui est en tête.
11. Missile, icône et modèle : en main, icône bleue avec la pastille « 1 » (la Fusée garde la sienne, rouge et jaune) ;
    la fusée en main et en vol est bleue et argent. `TexError.log` (dans `game_dev`) ne doit pas contenir
    `missileBonus` ni `Missile.png`.
