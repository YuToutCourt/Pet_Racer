# Menu « Mods » (chantier 3)

Bouton engrenage « MODS » au menu principal (en bas à gauche du logo) → écran `KidsModsMenuWindow`
(`KidsSimpleMenuModule._ModsMenuWindow`). Réglages **globaux** (pas par profil), enregistrés par OK dans
`game_dev\mods.dat` ; Retour ne garde rien.

## Écran
Une ligne par réglage : « libellé  < valeur > ».
- Clavier : ↑ ↓ choisit la ligne (barre bleue), ← → change la valeur (la liste tourne en boucle),
  Entrée = OK, Échap = Retour.
- Souris : flèches de chaque ligne, OK, Retour. Cliquer une flèche choisit aussi sa ligne.
- Tout le texte est blanc (demandé après le premier essai : plus de jaune / orange / gris). En bas,
  « Triche active : les records ne comptent pas » ou « Aucune triche : les records comptent ».
- Textes : jetons `&Mods…&` ajoutés à `Data\Texts.scr` (`mods/art/make_texts.py`). Le moteur traduit lui-même les
  `&clé&` dans `DisplayText` (comme les crédits) : les accents passent par l'encodage d'origine (Windows-1252), pas
  par une conversion Java → natif dont on ne connaît pas le comportement. Premier essai : textes ternes (sous le
  panneau semi-transparent, corrigé par `ButtonsOnTop`) et accents absents (la police n'avait pas é, à… : ajoutés
  par `mods/art/make_fonts.py`).
- Images (`mods/art/make_mods_menu.py`) : `ModsButton.png` / `ModsButtonPods.png` (engrenage, libellé BD comme les
  boutons d'origine), `ModsArrows.png` (flèches reprises de `data/menu/RAMKA_log.png`, l'écran du profil),
  `ModsRow.png` (barre de sélection), `ModsPanel.png` (panneau, titre « MODS »). Fond : `PlanszaNiebieska.png`
  (comme Options). Noms nouveaux : aucun cache `.tex` d'origine ne peut les masquer.

## Réglages

| Groupe | Réglage | Valeurs | Effet | Branché sur |
|---|---|---|---|---|
| Déblocages | Tous les animaux | non / oui | 12 animaux au choix et dans le tirage des adversaires | `CoreLogic.GetAvailableAnimalsCount` |
| Déblocages | Tous les circuits | non / oui | 31 circuits en course simple | `CoreLogic.GetAvailableTracksCount` |
| Déblocages | Toutes les courses extra | non / oui | les 15 extras ouverts (le « premier prix » gagné reste affiché) | `CoreLogic.GetExtraState` |
| Affichage | Compteur FPS en course | non / oui | moyenne sur 0,5 s, en haut à droite (collé au bord en 16:9) | `KidsHud.UpdateFps` |
| Affichage | Limite d'images | 60 / 120 / 144 / 240 / sans | limiteur du chantier 2, effet immédiat | `ModFrameLimiter` |
| Course | Rattrapage des IA (élastique) | oui / non | non = chaque voiture garde sa vitesse de base | `RacerModule.CalcPos` |
| Course | Difficulté des IA | d'origine / auto / facile / normal / difficile / expert | IA améliorée ou d'origine, niveau fixe (`notes/ia.md`) | `ModAI`, `VehicleAI`, `RaceParams.SetAILevel` |
| Triche | Vitesse | 100 / 110 / 125 / 150 % | vitesse maxi **et** accélération des humains (pas le virage) | `Vehicle.UpdateMaxSpeedAccel(EqualAll)` |
| Triche | Bonus infinis | non / oui | case vide → nouveau bonus 1 s plus tard (dès le départ), sur les circuits à bonus | `Vehicle.UpdateModCheats` |
| Triche | Bonus imposé | au hasard / 21 bonus | chaque boîte (et chaque recharge) donne ce bonus | `Vehicle.CollectBonus` |
| Triche | Protection permanente | non / oui | drapeau `m_bProtection` toujours levé (sans bulle visible) | `Vehicle.UpdateModCheats` |
| Aventure | Aller à l'étape | ne pas changer / 1-1 … 6-3 | appliqué à OK sur le profil en cours, puis enregistré | `CoreLogic.SetCheatAdventure` |

- **Déblocages non destructifs** : `m_nAvailableAnimals`, `m_nAvailableTracks`, `m_ExtraAvailable` gardent la vraie
  progression du profil (enregistrée par `PlayerSave`). Les menus et le tirage lisent les accesseurs de `CoreLogic`.
  Décocher l'option rend le profil tel qu'il était. Lecteurs modifiés : `CoreLogic` (tirage des adversaires, liste des
  animaux, `IsExtraAvailable`, `GetAvailableMaps`), `KidsAnimalChoiceMenuWindow`, `KidsMapLandsMenuWindow`,
  `KidsMapTracksMenuWindow`, `KidsExtraMenuWindow`.
- **Élastique d'origine** (`CalcPos`, toutes les 0,1 s) : la référence est l'humain le plus avancé ; une voiture derrière
  lui va jusqu'à +40 % de vitesse maxi, une devant jusqu'à −40 % (pleine force à 1 % de la course d'écart). Les IA
  à la traîne de plus de 0,2 tour gagnent aussi 2 niveaux de virage / dérapage. Ça vaut aussi pour le 2e humain en
  écran partagé. Le premier humain n'est jamais touché : le couper ne change pas ses temps.
- **Étape d'aventure** : même numérotation que les codes `L11`…`L63` (33 étapes : 5 mondes × 6 dont un boss, puis
  3 duels). Comme le code d'origine, elle remet la difficulté à 0 et l'animal du joueur à 1.
- Les triches ne touchent que les voitures humaines (`Vehicle.m_bIsPlayer`), donc les deux en multijoueur local.

## Course non comptée
`ModOptions.IsCheat()` : vitesse ≠ 100 %, bonus infinis, bonus imposé ou protection. Alors :
- pas de record (`KidsPlacesMenuWindow.EndWindow`, ligne `[mod] triche active : record non enregistre` dans
  `crash.log`) ; la progression d'aventure et les extras gagnés comptent toujours ;
- « Triche : record non compté » en bas à gauche du podium.
Plus tard : Grand Prix (chantier 6) et Elo (chantier 8) liront le même `IsCheat()`.
Les déblocages, le compteur FPS, la limite d'images, l'élastique et l'étape d'aventure ne sont pas des triches.

## Codes clavier d'origine
Toujours là (`RacerGame.CheckStringCheats`, Maj + `PRSUPER` puis le code, au menu principal) :
`ANIMALS`, `TRACKS`, `BONUSES` cochent maintenant les déblocages du menu (et écrivent `mods.dat`) au lieu de
modifier le profil ; `L11`…`L63` inchangés.

## mods.dat
Texte, `Clé=valeur` : `FpsLimit`, `AllAnimals`, `AllTracks`, `AllExtras`, `ShowFps`, `AiCatchUp`, `AiLevel` (0 = d'origine … 5 = expert, 1 par défaut), `Speed`,
`InfiniteBonus`, `ForcedBonus` (−1 = au hasard, 0…20 = `LevelBonusTypes`), `Shield`. Clé absente = valeur par défaut ;
une valeur hors liste écrite à la main (ex. `FpsLimit=75`, `Speed=200`) est gardée et affichée telle quelle par le menu.

## Fichiers
`ModOptions`, `KidsModsMenuWindow`, `ModText`, `KidsSimpleMenuModule`, `KidsMainMenuWindow`, `RacerGame`, `CoreLogic`,
`KidsAnimalChoiceMenuWindow`, `KidsMapLandsMenuWindow`, `KidsMapTracksMenuWindow`, `KidsExtraMenuWindow`, `KidsHud`,
`RacerModule`, `Vehicle`, `KidsPlacesMenuWindow` ; `Data\Texts.scr`, `Textures\Menu\a\Mods*.png`,
`Textures\Menu\b\ModsPanel.png`, `Data\Panel\MS Sans Serif{16,18,20,26,32,40}.dat` (générés par
`mods/art/make_texts.py`, `mods/art/make_mods_menu.py` et `mods/art/make_fonts.py`).

## À vérifier en jeu
1. Menu principal : bouton « MODS » visible, survol / clic, ← → du clavier passe par lui (avant « Un joueur »).
2. Écran Mods : textes accentués corrects (« élastique », « Bonus imposé »), pas de `&Mods…&` brut à l'écran ;
   ↑ ↓ ← →, Entrée, Échap ; flèches à la souris ; Retour ne garde rien, OK écrit `mods.dat`.
3. Tous les animaux / circuits / extras : visibles au choix, puis décocher → progression d'origine revenue.
4. Compteur FPS en course, limite 60 puis « sans limite » sans relancer.
5. Vitesse 150 % : la voiture du joueur va nettement plus vite, pas les IA.
6. Bonus imposé (ex. OVNI) + bonus infinis : un nouveau bonus 1 s après chaque usage. « Fusée » ne marche pas
   (vu en jeu le 2026-11-18 ; jamais ramassable dans le jeu d'origine) : à reprendre au chantier 5.
7. Protection permanente : les projectiles des IA n'ont plus d'effet.
8. Triche active → podium avec « Triche : record non compté », pas de nouveau record.
9. Aventure → 3-1 : « Continuer l'aventure » repart au monde 3.

## Piège : 50 fenêtres maximum par écran
`KidsMenuWindow.m_List` = `new WindowsList(50)` ; au-delà, `Add` renvoie -1 sans rien dire. L'écran Mods utilise
4 fenêtres par ligne : à 12 lignes (ajout de « Difficulté des IA ») il atteignait 50, le texte d'état prenait l'id -1,
`Init` plantait (NPE dans `RefreshStatus`) et tout `KidsSimpleMenuModule.OnCreate` s'arrêtait → au lancement, l'écran
du choix d'animal restait affiché par-dessus le reste, `CarsManager` jamais créé (NPE dans `KidsMainMenuWindow.Show`).
Corrigé 2026-11-18 : constructeur de `KidsModsMenuWindow` qui remplace la liste par `WindowsList(_ROWS * 4 + 16)`.
Tout nouvel écran avec beaucoup d'éléments doit faire de même.
