# Format des modèles .3DA (ChromeEngine)

Relevé le 2026-11-18 sur `Data\Meshes\Objects\rakietaBonus.3DA` (fusée bonus, 9920 octets, un seul objet, sans
squelette), pour donner au Missile son propre modèle. Entiers et flottants en petit-boutiste. Offsets absolus.

| Offset | Contenu |
|---|---|
| 0 | `3DA_`, puis `01 01 00 00`, `02 00 00 00` (`01` dans `kokos.3da`, `ufo.3da`), `64 00 00 00` |
| 16 | 32 matériaux de 164 octets : nom de texture (32 octets), nom de texture de reflet (32 octets, ex. `S_blachaRakieta.png`, `object_envmod.png`), puis réglages |
| 0x1490 | nombre d'objets (1), faces (72), sommets (52), offset de la table des objets (0x14E0) |
| 0x14A0 | boîte englobante : min x,y,z puis max x,y,z |
| 0x14E0 | objet : 1, nom (32 octets, `rakietaBonus`), sommets 52, faces 72, 0, 72, 0, 0, 0, UV 216, 0, 0, 2, boîte englobante, `FF FF`, offsets des blocs ci-dessous |
| 0x157C | faces : 72 × 3 index de sommets (u16) |
| 0x172C | 72 × u16 par face (tous à 0 ici : matériau ?) |
| 0x17BC | 72 × 3 index d'UV (u16) |
| 0x196C | 216 UV (2 flottants) |
| 0x202C | 52 sommets (3 flottants) ; l'axe long de la fusée est z (−40 à +38) |
| 0x229C | 52 normales (3 flottants) |
| 0x250C | pistes de position, rotation (quaternion), échelle de l'objet, une clé chacune ; elles contiennent des pointeurs mémoire sérialisés (`F8 F0 AD 00`…) et des offsets absolus vers leurs clés |
| fin | point d'attache `ogien` (type 3, boîte ±2, position −0.3, 6.4, −32.4 = arrière de la fusée) : `Rocket.AddSmokeFX` y attache la fumée |

Les textures des modèles sont cherchées par leur seul nom dans `Data\Textures` (les 298 noms des modèles de
`Meshes\Objects` y sont tous, sauf 2 absents du jeu). Un `.png` sans `.tex` devrait suffire (vrai dans pet_soccer et
pour les icônes du mod ; à confirmer pour une texture de modèle avec `missileBonus.png`).
`Data\Textures\rakietaBonus.png` (256x128) habille la fusée ; `Data\Textures\Hud\rakietaBonus.png` est une autre image.

## Ce qui est faisable
- **Changer la texture** d'un modèle (fait pour le Missile) : copier le `.3DA` et réécrire le nom de 32 octets.
  Sûr : la forme ne bouge pas.
- **Nouvelle forme pour un objet simple comme la fusée** (un objet, pas de squelette) : écrire un `.3DA` avec
  d'autres sommets, faces, UV et normales, en recalculant les comptes, la boîte englobante et tous les offsets (blocs,
  pistes, point `ogien`). Les blocs suivent un ordre fixe, donc un convertisseur OBJ → `.3DA` est envisageable : il
  servirait aussi pour un modèle fait dans Blender. Non fait ; à valider en jeu (un offset faux = plantage au chargement).
- **Modèles animés** (animaux, voitures) : squelette Biped `Bip01 *`, animations, `.colmap` : hors de portée sans
  beaucoup plus de rétro-ingénierie (même conclusion que pour pet_soccer, `new-teams.md`).
