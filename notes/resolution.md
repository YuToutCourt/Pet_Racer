# Résolutions modernes et écran large (chantier 1)

État : **fait, à valider en jeu.** Vu en 1920×1080 et 1440×1080 fenêtrés (menus, choix de l'animal, course).
La liste du menu Audio/Vidéo, le curseur agrandi, les vidéos centrées et les grandes polices n'ont pas encore été vus à l'écran.

## Ce qui bloquait
- La limite 640×480 / 800×600 / 1024×768 n'existait que dans le Java. Le moteur accepte n'importe quelle taille
  (`GameObject.SetResolution(w,h)` puis `ApplyVideoSettings()`).
- La liste était recopiée 4 fois (`Settings`, `KidsAudioVideoMenuWindow` ×2, `GameSerializeSettings`).
- Au démarrage, `GameSerializeSettings.SetCurrentSettings` réappliquait l'index de `progress.cfg` et écrasait la
  taille de `Data\VideoSettings.scr`. Tout index inconnu redonnait 640×480.

## Ce que fait le mod (`ModVideo`)
- **Taille de référence** : celle de `VideoSettings.scr`, que le moteur lit et écrit lui-même. `progress.cfg` ne
  l'écrase plus. L'index d'origine (0, 1, 2) reste tenu à jour comme « classe de taille » (< 600, < 768, au-delà)
  pour le vieux code qui s'en sert.
- **Liste du menu Audio/Vidéo** : modes réels de l'écran, lus par `EnumResolution(i, liste)`. Le 2e paramètre est
  un numéro de liste de 0 à 3 (une par profondeur de couleur), **pas** le nombre de bits : analysé dans
  `ChromeEngine.dll` (`FUN_10155c70`). Les 4 listes sont réunies, puis filtrées : tailles courantes
  (hauteurs 480 … 2160, rapports 4:3, 5:4, 16:10, 16:9, 21:9), la plus grande et la taille actuelle, 16 au plus.
  Liste de secours si le moteur ne rend rien. Lignes `[mod] video` dans `crash.log`.
- Le curseur « Résolution » a un cran par taille. Les libellés peints `640/480 800/600 1024/768` sont remplacés
  par une plaque sombre (`mods/art/make_audiovideo_menu.py` → `MenuAudioVideo1024.png`) où s'écrit la taille choisie,
  mise à jour pendant qu'on fait glisser le curseur.
- `KidsSlider` : au clavier (haut / bas), la fenêtre est maintenant prévenue du changement. Avant, régler la
  résolution ou les volumes au clavier n'était pas pris en compte.

## Écran large (`Window.SetSizeAndPos`, `ModVideo.GetLayout`)
Les menus et le HUD sont placés en fractions 0..1 de l'écran, pensées pour du 4:3 : en 16:9 tout s'élargissait.

| Fenêtre | Placement |
|---|---|
| Écran de menu (fenêtre racine) | zone 4:3 centrée (largeur = hauteur × 4/3) |
| Fond noir derrière les menus, fondus, HUD | tout l'écran, comme avant |
| Gadget du HUD (enfant de `KidsHud`) | taille 4:3 ; centre < 0,4 → collé au bord gauche, > 0,6 → au bord droit, sinon centré |
| Fenêtre dans une fenêtre | relative à son parent, comme avant |

- Sur les côtés des menus, on voit ce qu'il y a derrière : les voitures en filigrane du menu principal.
- Choix de l'animal, podium et provocation du boss : leur cadre `MenuCharakter.png` est ouvert sur la 3D, et le décor
  apparaissait sur les côtés. `ModSideFill` y ajoute deux bandes (enfants du cadre, placées hors de lui) qui
  prolongent le damier jusqu'au bord de l'écran, assombri vers l'extérieur. Aux bords du cadre, le damier est
  régulier (colonnes de 64 px, limite pile au bord) : `mods/art/make_side_fill.py` reprend les lignes de la colonne du
  bord et ajoute des colonnes de 64 px. Textures 512×512 à l'échelle du cadre, de quoi couvrir jusqu'au 32:9.
- `ModVideo.m_bWide = false` remet tout étiré (option possible du menu Mods).
- Vidéos d'intro (`MovieModule`) : 4:3 centrées.
- Curseur du menu et de la course : 64 px pensés pour 768 lignes, agrandi au-delà (`ModVideo.ScaleUi`).
- Polices de `KidsText` : 16/18/20 px d'origine, puis 26 px dès 1000 lignes, 32 px en 1440p et 40 px en 4K si le
  `.dat` existe dans `Data\Panel` (`ModVideo.GetTextFont`). `MSSansSerif18` est maintenant déclarée dans `Fonts`
  (le jeu la demandait sans l'avoir déclarée). Le moteur crée lui-même `MS Sans Serif32.dat` et `40.dat` au premier
  lancement (`Tworze font … 256x256` / `512x256` dans `crash.log`) ; depuis le chantier 3, le mod les fournit avec les
  accents français (`mods/art/make_fonts.py`, sources dans `mods/art/font_src`).
- `CarsManager` (voitures du menu) prend la taille réelle au lieu de la table à 3 cases.

## 3D
Rien à corriger. Le moteur garde le champ vertical et élargit l'image en 16:9 : la voiture a la même taille en
1440×1080 et en 1920×1080, on voit seulement plus de décor sur les côtés. Les roues restent rondes.

## Fichiers
`ModVideo` (nouveau), `GameSerializeSettings`, `KidsAudioVideoMenuWindow`, `KidsSlider`, `Window`, `CarsManager`,
`KidsText`, `Fonts`, `KidsCursor`, `KidsDuringGameCursor`, `MovieModule`, `ModSideFill`, `KidsAnimalChoiceMenuWindow`,
`KidsPlacesMenuWindow`, `KidsBossTauntMenuWindow` ; `mods/art/make_audiovideo_menu.py`, `mods/art/make_side_fill.py`.

## Fenêtré
Retirer la ligne `Fullscreen()` de `Data\VideoSettings.scr` lance le jeu dans une fenêtre (pratique pour les essais).
Pas encore de bouton : prévu dans le menu Mods.

## Reste à faire / à vérifier
- Valider en plein écran 1080p et 1440p : menu Audio/Vidéo (liste, plaque, texte), changement de résolution
  depuis le menu, relance du jeu (la taille doit rester).
- Écran partagé à 2 en 16:9 : deux bandes très larges (le moteur élargit l'image, rien n'est déformé).
  Le partage vertical a été écarté : il réduirait beaucoup le champ de vision.
- Premier lancement du mod : le jeu démarre encore à la taille de `VideoSettings.scr` (640×480 d'origine).
  Idée : prendre d'office la taille de l'écran.
- Sous-titres (`Module.dontcallShowSubtitle`) : code mort, rien à faire.
