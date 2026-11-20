# Stats des animaux (chantier 4)

Les 12 animaux n'ont que 3 caractéristiques, en dur dans `VehicleParams` : des coefficients d'accélération, de vitesse
et de virage appliqués à la base du jeu par `Vehicle.UpdateMaxSpeedAccel` (accélération 500, vitesse maxi 4500,
braquage 80°). Ils sont maintenant lus dans `Data\animals.scr` et affichés en barres à l'écran de choix.

## Data\animals.scr
```
!Animal(s,f,f,f)
Animal("Panthere", 0.90, 1.00, 0.80)    // accélération, vitesse, virage
```
- **Fichier lu par le jeu : `game_dev\Data\animals.scr`.** La source est `mods\data\Data\animals.scr` : après l'avoir
  modifiée, relancer `mods\build.ps1` (qui la recopie dans `game_dev`), sinon le jeu garde l'ancienne version.
  Modifier directement `game_dev\Data\animals.scr` marche aussi : depuis le 2026-11-18 le build ne l'écrase plus
  s'il est plus récent que la source (ligne `[attention]`), mais un `-Clean` le perdrait : recopier dans `mods\data`.
- Lu une fois au lancement (`RacerGame.OnInitialize` → `ModAnimals.Load`, analyseur du moteur `GameObject.ParseFile`),
  avant toute voiture. Remplit les tableaux `VehicleParams._ANIMALS_ACCEL/SPEED/TURN_COEFS`.
- Vaut pour **toutes** les voitures, humains et IA (l'IA lit aussi le coefficient d'accélération pour ses virages,
  `VehicleAI:515`). Les triches du menu Mods (vitesse) se multiplient par-dessus.
- Noms reconnus (majuscules indifférentes) : Panthere, Dino, Poulet, Grenouille, Tortue, Requin, Taureau, Girafe, Ours,
  Gorille, Robot, Lion ; ou le numéro 0…11. Valeurs acceptées 0,3 à 2,0.
- Fichier absent, animal absent, nom inconnu, valeur hors bornes → valeur d'origine, et une ligne `[mod] animaux : …`
  dans `crash.log`. Le journal liste aussi les animaux modifiés (`modifies (accel/vitesse/virage) : …`).
- Les réglages par monde restent : Désert accélération 390, Hiver 300, Royal (`UpdateMaxSpeedAccelEqualAll`) vitesse
  égale pour tous (accélération et virage gardent les coefficients).

Valeurs d'origine (= le fichier livré) :

| Animal | Accél. | Vitesse | Virage |
|---|---|---|---|
| Panthère | 0,90 | 1,00 | 0,80 |
| Dino | 0,90 | 0,80 | 0,90 |
| Poulet | 0,90 | 0,90 | 0,95 |
| Grenouille | 0,85 | 0,85 | 1,00 |
| Tortue | 0,90 | 0,80 | 1,00 |
| Requin | 0,95 | 0,90 | 0,85 |
| Taureau | 0,90 | 1,00 | 0,90 |
| Girafe | 0,95 | 0,95 | 0,80 |
| Ours polaire | 0,90 | 0,90 | 0,90 |
| Gorille | 0,90 | 0,90 | 0,90 |
| Robot | 0,80 | 0,90 | 1,00 |
| Lion | 0,90 | 1,00 | 0,80 |

## Écran de choix
- Cadre en haut à droite (bande de damier libre entre le hublot 3D et le bord, au-dessus de la flèche « suivant ») :
  nom de l'animal, puis Vitesse / Accélération / Maniabilité, chacune avec sa barre (rouge, jaune, vert). Mis à jour à
  l'ouverture et à chaque animal suivant / précédent ; même chose pour le joueur 2 en écran partagé.
- Échelle : 0,8 = barre à moitié, 1,0 = pleine (`ModAnimals.GetFill`). Si `animals.scr` sort de 0,8…1,0, l'échelle
  s'agrandit (plein = plus grand coefficient, vide = plus petit − 0,2) pour que les écarts restent visibles.
- La barre raccourcit et sa texture est coupée d'autant (`SetTextureCoords`, comme la barre de chargement) : le
  dégradé n'est jamais écrasé.
- Textes : jetons `&AnimalName0&`…`&AnimalName11&`, `&StatSpeed&`, `&StatAccel&`, `&StatTurn&` (`make_texts.py`).
- Images (`mods/art/make_animal_stats.py`) : `Menu/b/StatsPanel.png` (même style que le panneau Mods),
  `Menu/a/StatsTrack.png`, `StatsSpeed.png`, `StatsAccel.png`, `StatsTurn.png`. Mise en page : constantes `_ST_*` de
  `KidsAnimalChoiceMenuWindow`, en pixels 640x480 dans les commentaires.

## Plus tard
- En ligne (chantier 8) : tous les jeux doivent avoir les mêmes valeurs ; la partie classée devra imposer celles
  d'origine (ou comparer un résumé de `animals.scr` au salon).
- Éditer `animals.scr` n'est pas compté comme une triche (les records restent comptés) : les adversaires du même
  animal ont les mêmes valeurs.

## Fichiers
`ModAnimals` (nouveau), `RacerGame`, `KidsAnimalChoiceMenuWindow` ; `Data\animals.scr`, `Data\Texts.scr`,
`Textures\Menu\a\Stats*.png`, `Textures\Menu\b\StatsPanel.png`.

## À vérifier en jeu
1. Course simple → choix de l'animal : cadre en haut à droite, nom accentué (« Panthère », « Ours polaire »), 3 libellés
   (« Accélération », « Maniabilité »), barres différentes d'un animal à l'autre (Panthère : vitesse pleine, virage à
   moitié ; Robot : l'inverse).
2. ← → et flèches à la souris : nom et barres suivent ; le cadre ne cache ni la flèche « suivant » ni l'animal.
3. En 1080p / 1440p et en 4:3 : cadre à sa place (dans le cadre 4:3), texte lisible.
4. Deux joueurs : le cadre suit aussi au choix du joueur 2.
5. `crash.log` : `[mod] animaux : Data/animals.scr lu, 12 animal(aux), valeurs d'origine`.
6. Essai d'édition : mettre `Animal("Tortue", 0.90, 1.50, 1.00)` dans `mods\data\Data\animals.scr`, relancer le build
   puis le jeu (ou modifier `game_dev\Data\animals.scr` directement) → `crash.log` : `modifies (accel/vitesse/virage) :
   Tortue 0.9/1.5/1.0`, barre de vitesse de la Tortue pleine (les
   autres baissent), Tortue nettement plus rapide en course ; une valeur `5.0` → ignorée avec une ligne au journal.
