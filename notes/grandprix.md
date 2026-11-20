# Grand Prix (chantier 6)

Une coupe de **4 courses tirées au hasard**, avec des points par place. Choix du 2026-11-18 : une seule coupe
(pas une par monde ni de finale), monde au hasard puis circuit au hasard, le Royaume du Lion compris.

## Déroulement
Un joueur → bouton **GRAND PRIX** (au-dessus de « Course ») → choix de l'animal → nombre de tours (3 / 5 / 7)
→ course 1 → podium → **classement de la coupe** → course 2 → … → classement final → menu Un joueur.

- **Tirage** (au lancement de la coupe, `ModGrandPrix.DrawTracks`) : pour chaque course, un monde au hasard parmi
  ceux qui ont encore un circuit débloqué pas encore pris, puis une variante au hasard dans ce monde (3 circuits ×
  aller / retour). Le Royaume du Lion (circuit n° 30, seul) compte comme un monde : il sort à peu près une fois sur
  six par course quand tout est débloqué. Jamais deux fois la même variante ; s'il y a moins de variantes débloquées
  que de courses (profil neuf : 1 circuit), on repioche dans toutes (répétitions).
  Débloqué = `CoreLogic.GetAvailableTracksCount()` : la progression du profil, ou les 31 avec « Tous les circuits » du
  menu Mods. Aléa propre (`java.util.Random` sur l'heure) : celui du jeu (`RacerGame.m_Random`) part d'une graine
  fixe et donnerait la même coupe à chaque lancement.
- **Adversaires** : les 5 tirés par le choix de l'animal (`CoreLogic.FillRandomAnimals`), gardés toute la coupe.
  Même nombre de tours pour les 4 courses.
- **Points** : 10 / 8 / 6 / 4 / 2 / 1 selon l'ordre d'arrivée réel (`CoreLogic.m_TimeList`, rempli par
  `Vehicle.FillTimeList` au chantier 2 : arrivés par rang, les autres par distance).
  Classement : points, puis nombre de victoires, puis place à la dernière course.
- **Course quittée** (Échap → quitter) : 0 point pour le joueur, classé dernier ; les IA sont classées dans leur ordre
  du moment (`RaceParams.m_SortTable`) et marquent leurs points ; la coupe passe à la course suivante.
- **Abandonner la coupe** : Retour (ou Échap) à l'écran du classement, deux fois (le premier appui demande
  confirmation, OK annule).
- **Triche** (`ModOptions.IsCheat()` à un moment de la coupe) : la coupe se joue, « Triche active : coupe non
  comptée » s'affiche, rien n'est enregistré.
- Pas de reprise après fermeture du jeu. Solo seulement.
- Records des circuits : enregistrés comme en course simple.

## Écran du classement (`KidsGrandPrixMenuWindow`)
Panneau comme l'écran Mods (titre « GRAND PRIX ») : « Course 2 / 4 » et le circuit joué, tableau des 6 voitures
(place, pilote, points de la course, total), ligne du joueur sur la barre bleue, « Prochaine course : … ».
Après la dernière : « Victoire ! Coupe d'or » / « 2e place : coupe d'argent » / « 3e place : coupe de bronze » /
« Pas de coupe cette fois », et le palmarès du profil (or, argent, bronze).
Entrée / OK : course suivante, ou retour au menu après la dernière.

## Fichiers
- `Data\grandprix.scr` : `Races(4)` (1 à 8), `Points(10, 8, 6, 4, 2, 1)`. Absent = ces valeurs.
- `game_dev\grandprix.dat` (créé à la première coupe finie) : texte, `<profil>.Played`, `.Gold`, `.Silver`, `.Bronze`.
- Java : `ModGrandPrix` (tirage, points, classement, palmarès), `KidsGrandPrixMenuWindow` (nouveaux) ;
  `KidsSinglePlayerMenuWindow` (copié, bouton), `KidsAnimalChoiceMenuWindow` (passe aux tours au lieu de la carte),
  `RacerGame` (`OnInitialize` lit le `.scr`, `RunModule` ouvre la coupe / règle la course, `EndModule` compte les
  points), `KidsPlacesMenuWindow` (podium → classement), `KidsSimpleMenuModule` (fenêtre enregistrée).
- Images : `Menu\a\GrandPrixButton(Pods).png`, `Menu\b\GPPanel.png` (`mods/art/make_grandprix.py`) ; textes `&GP…&`
  et noms des 31 circuits `&GPTrack0&`…`&GPTrack30&` (`mods/art/make_texts.py`).

## Conception
La coupe tourne en contexte « Course » (`EMenuCX._RACE` = 0) avec le drapeau `ModGrandPrix.m_bActive` : chaque course
se joue exactement comme une course simple (IA, bonus, records), seuls quatre passages sont détournés :
bouton du menu Un joueur (`m_bPending`), choix de l'animal (tours au lieu de la carte), `RacerGame.RunModule`
(`BeforeRace` : tirage à la première course, puis adversaires, tours et circuit remis avant chaque course),
fin de course (`EndModule` / podium → classement). Revenir au menu Un joueur ferme la coupe (`Reset` dans `Show`).

Journal `crash.log` : `[mod] grand prix : nouvelle coupe, … circuits 7 30 2 16`, `course 1/4, circuit …`,
`course 1/4 finie, points (animal:pts) …, joueur 2e, total 8`, `coupe finie, joueur 1er avec 34 pts`.

## À vérifier en jeu
1. Menu Un joueur : bouton « GRAND PRIX » au-dessus de « Course », survol / clic, ← → du clavier passe par lui.
2. Coupe complète de 4 courses : classement après chaque podium, points = places du podium, totaux cumulés, mêmes
   adversaires et même nombre de tours partout ; circuits variés (mondes différents, Royaume du Lion de temps en temps).
3. Échap → quitter pendant une course : classement avec « Course quittée : 0 point », la coupe continue.
4. Retour au classement : message de confirmation, second Retour → menu Un joueur ; OK annule.
5. Fin de coupe : message de coupe, palmarès ; `grandprix.dat` écrit (pas avec une triche active).
6. Course simple, aventure et extras toujours normaux après une coupe (et après une coupe abandonnée).
7. 1080p / 1440p : écran du classement centré, textes lisibles et accentués.
