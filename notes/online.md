# Multijoueur en ligne (chantier 8)

Courses « fantômes » : chaque jeu simule **sa** voiture, les autres sont posées d'après le réseau ; l'hôte du salon
simule les IA qui complètent la grille ; Elo par paires d'humains. Conception : `notes/roadmap.md` section 8.
Repris de pet_soccer (`..\pet_soccer\notes\online.md`) pour la structure, mais **re-porté en Java 1.3** : le JRE du jeu
n'a ni `java.nio`, ni `InetSocketAddress`, ni `StringBuilder`, ni regex (vérifié dans son `rt.jar`).

| Phase | Contenu | État |
|---|---|---|
| 0 | Faisabilité : socket UDP 1.3, deux jeux sur un PC | validé le 2026-11-18 (aller-retour 0-1 ms, 10/10, jeu sans focus actif) |
| 1 | Course à 2 en direct, sans serveur ni bonus : fantômes, départ commun, temps d'arrivée | validé le 2026-11-18 |
| 2 | Bonus en ligne (`HIT`, `TRAP`, `GONE`, `BOOM`, OVNI) | fait, à vérifier en jeu |
| 3 | Serveur (port 7778), salons à code jusqu'à 6, écran « En ligne », IA de l'hôte | serveur fait et testé (`test_server.py`, 43 points) ; course par le serveur, boîtes partagées, spectateur validés ; écran « En ligne », courses à la suite, hôte remplacé entre deux courses, choix de l'animal validés (2026-11-20) ; IA de l'hôte et hôte remplacé en pleine course faits, à vérifier en jeu (protocole v6) |
| 4 | Partie rapide, Elo multi, classement | validé le 2026-11-20 (dialogue v2, `ModNet` v7 ; serveur : `test_server.py`, 65 points) |
| 5 | Installeur pour les amis | fait le 2026-11-20, testé sur une fausse copie du jeu (`test_installer.py`, 23 points) ; à essayer chez un ami |

## Couche réseau Java 1.3
- `ModUdp` : un `DatagramSocket` + un **fil démon** qui fait les `receive()` bloquants et range les paquets
  (`ModPacket` : adresse, octets, heure de réception) dans une file ; le fil du jeu la vide à chaque image
  (`Poll()`, jamais bloquant). Envoi direct depuis le fil du jeu. Erreurs de réception (« connection reset » de
  Windows après un envoi vers un port fermé) ignorées ; file limitée à 512 paquets.
- `ModBuf` : remplace `ByteBuffer` (gros-boutiste, comme `struct.pack(">…")` du serveur Python). Lecture au-delà de
  la fin : rend 0 et lève `m_bBad`.
- `ModAddr` : remplace `InetSocketAddress` (`Resolve(nom, port)`, `equals`, `hashCode`).
- `ModOnline` : réglages et test. `Init()` dans `RacerGame.OnInitialize`, `Frame()` dans `RacerGame.OnFrame` (avant le
  limiteur d'images).

## Réglages
- `Data\online.scr` (commun, dans `mods\data`) : `Server("adresse", port)`, 7778 par défaut (pet_soccer garde 7777).
- `nettest.scr` à la racine de chaque dossier de jeu (pas dans `mods\data`, chaque jeu a le sien) :
  `NetPort(n)` port local, `NetTest("ip", port)` pings vers cette adresse. Fichier absent = pas de socket.
  `build.ps1 -Second` les crée s'ils manquent : `game_dev` écoute sur 7781 et pingue 7782, `game_dev_b` l'inverse.
  Un `-Clean` efface celui de `game_dev` (le prochain `-Second` le recrée).

## Deux jeux sur un PC
`powershell -ExecutionPolicy Bypass -File mods\build.ps1 -Second` : `game_dev` → `game_dev_b`. `game_dev_b` garde ses
sauvegardes et journaux (`.dat`, `.cfg`, `.log`, `.txt` de la racine), son `Data\VideoSettings.scr` et son
`nettest.scr`. Au premier passage : profil et réglages copiés de `game_dev`, mais fenêtré en 1280×720.

Le plein écran empêche d'avoir une autre fenêtre : `mods\online-test.ps1` fait le build `-Second`, passe `game_dev` en
fenêtré 1280×720 (sauvegarde `Data\VideoSettings.fullscreen.scr`), lance les deux jeux, attend qu'ils soient fermés,
remet le plein écran et affiche les lignes `[mod] net` des deux `crash.log`. Options : `-NoBuild`, `-Echo` (lance
aussi `server\echo_test.py`), `-Restore` (remet seulement le plein écran après un test interrompu ; fait aussi
automatiquement au lancement suivant). Le fenêtré s'obtient en retirant la ligne `Fullscreen()` du `.scr`.

## Test de la phase 0
Paquet de test : magique `PRN0`, type (1 ping, 2 réponse), identifiant du jeu qui pingue, numéro, heure d'envoi.
Un jeu pingue chaque seconde jusqu'à 10 réponses (abandon après 120 essais) et répond aux pings des autres ; un ping
qui revient avec son propre identifiant vient de `echo_test.py`. Journal (`crash.log`, lignes `[mod] net`) : pas de
l'horloge Java, chaque réponse (aller-retour, délai entre la réception par le fil et la lecture par le jeu), puis un
bilan (réponses, aller-retour min/moy/max, images, plus grand écart entre deux images, paquets jetés, erreurs).
Le « plus grand écart entre deux images » compte aussi les chargements : ne le lire que si on est resté dans les menus.

Variante avec le serveur d'écho : `python server\echo_test.py` (port 7778) et `NetTest("127.0.0.1", 7778)`.

### À vérifier en jeu
- [ ] `mods\online-test.ps1` : les deux jeux s'ouvrent en même temps, chacun en fenêtre 1280×720 ; après fermeture,
      `game_dev` est de nouveau en plein écran.
- [ ] Le jeu qui n'a pas le focus continue à tourner (sinon il n'enverrait plus rien en course).
- [ ] Dans chaque `crash.log` : `socket UDP ouvert sur le port 7781` / `7782`, puis `test fini : 10/… reponses`.
- [ ] Pas de saccade : compteur FPS du menu Mods stable dans les deux jeux pendant le test (~15 s).

## Phase 1 — course à 2 « fantômes » (`ModNet`)
Lancement automatique depuis le menu principal, réglé par `nettest.scr` (créé par `build.ps1 -Second`) :
`game_dev` = `NetAuto("host")`, `NetPlayers(2)`, `NetTrack(0)` (Forêt 1), `NetLaps(2)`, `NetAnimal(0)` ;
`game_dev_b` = `NetAuto("join")`, `NetPeer("127.0.0.1", 7781)`, `NetAnimal(3)`. Une seule course par lancement.

Déroulement :
1. L'invité envoie `JOIN` 4 fois/s ; l'hôte répond `INFO` (slot, paramètres, joueurs, heures pour caler l'horloge :
   l'invité garde l'échantillon à l'aller-retour le plus court).
2. Quand tous sont au menu principal, l'hôte lance (`ModNet.LaunchRace`, comme le OK de l'écran des tours : contexte
   « Course », animaux = le sien puis les autres slots) ; l'invité lance en voyant « lancée » dans `INFO`.
3. Circuit chargé (`RacerModule.OnActivate`) : plus de `CallMethodIn("StartRace", 2 s)`, on attend. L'hôte, quand tous
   ont chargé, fixe le top (heure de l'hôte + 1,5 s) ; chaque jeu allume les feux à ce top (`ModNet.RaceFrame`),
   vert 6 s après comme d'habitude.
4. Course : chaque jeu envoie l'état de sa voiture 25 fois/s (`CAR`, 79 octets : position, avant/haut réels de l'objet,
   angles du pilote, vitesse, braquage, tour, porte, drapeaux, temps d'arrivée) ; l'hôte relaie aux autres invités.
   Fantôme posé avec 100 ms de retard, interpolé entre deux états (`ModNetPlayer.Apply` : `FromUpForwardPos`, ressorts
   et roues, pilote, son du moteur).
5. Arrivée : temps exact de chaque jeu (depuis son vert), redit dans chaque `CAR`. Rang par les temps
   (`ModNet.RerankFinished`), pas par l'ordre de réception. Le podium attend que tous soient arrivés, au plus 30 s
   après la première arrivée ; les non-arrivés sont classés à la distance (temps extrapolé), un joueur parti en dernier.
6. Fin (`RacerGame.EndModule`) ou Échap → Oui : `BYE` ×3. Chez les autres, la voiture d'un joueur parti disparaît.
   Silence : 30 s avant le vert (chargements), 5 s ensuite.

Coupé en ligne : rattrapage (`RacerModule.CalcPos`), départ aléatoire ×0,6-1,4 (`Vehicle.OnCreate`), bonus (boîtes
retirées comme l'extra « sans bonus »), ralenti de la pause (`ExitDuringGameWindow`, copiée dans `mods\java`).
Pas encore coupé : les triches du menu Mods (phase 4).

Fantôme (`RaceParams.AddGhost`) : hors `m_PlayersTab` / `m_AITab` (ni physique, ni portes, ni IA), dans `m_Vehicles` /
`m_SortTable` (places, barre de distance) ; drapeau 32 (`EFlags._BLOCK_MOVE`) retiré comme l'OVNI. Grille : place =
slot (`ModNet.GridOrder` dans `RaceParams.SetStartPos`), la même dans tous les jeux.

Journal (`[mod] net`) : connexion, lancement, chargement, top (« feux allumés avec N ms de retard »), état toutes les
10 s (envoyés / reçus, décalage d'horloge), arrivées, podium, fin.

### À vérifier en jeu (phase 1)
- [ ] `mods\online-test.ps1` : les deux jeux arrivent au menu principal et la course se lance seule dans les deux
      (choisir le profil si l'écran de profil s'affiche ; la démo du menu bloque le lancement tant qu'elle tourne).
- [ ] Les deux circuits chargent, les feux s'allument ensemble (« retard » de quelques ms dans les deux journaux).
- [ ] Le fantôme roule de façon fluide (roues, pilote, son), au bon endroit ; on le traverse sans choc.
- [ ] Place (1er / 2e) et barre de distance justes dans les deux jeux.
- [ ] Arrivée : le premier attend l'autre, puis même podium et mêmes temps dans les deux jeux.
- [ ] Échap pendant la course : la course continue derrière la fenêtre ; « Oui » : chez l'autre, la voiture disparaît
      et il finit seul.

## Phase 2 — bonus en ligne
Principe : **le tireur décide** si un projectile touche (il le fait voler chez lui vers le fantôme), **la victime
applique** l'effet chez elle, après avoir vérifié sa protection. Les pièges posés sont **recopiés** chez tous, et
c'est le jeu de la voiture qui roule dessus qui décide.

Événements fiables (`EVT`, `ModNet` : numérotés par lien invité ↔ hôte, accusés, redits avec chaque état jusqu'à
confirmation, relayés par l'hôte) :

| Événement | Envoyé par | Effet chez les autres |
|---|---|---|
| `HIT` effet | `Vehicle` quand un **fantôme** est touché : `StopAndStars` (bouchon, noix de coco, boule de neige), `StoppedByWaspBag`, `StoppedByTomatoOrCookie` (tomate, cookie), `ExplosionEffect` (bombe, pastèque, fusée, missile), `FreezeVehicle`, `WaterVehicle`, `FallingRock`, `StoppedByMole` ; un seul par seconde et par effet (la bombe teste son rayon à chaque image) | la même méthode sur la voiture locale, sauf protection |
| `HIT` OVNI | `Vehicle.UseBonus` dès le lancer sur un fantôme | la victime lance son propre `UFOObject` sur elle (l'OVNI du tireur n'est que visuel) |
| `TRAP` n°, type, position | banane, huile, bouteille, enclume, catapulte à l'atterrissage (`ModLand`), gaz à l'apparition | copie posée au même endroit, déjà « atterrie » |
| `GONE` n° | la voiture locale déclenche un piège (original ou copie) | copie retirée |
| `BOOM` genre, position | explosion de bombe ou de pastèque | effet visuel |

Côté fantôme : un piège ne le déclenche jamais (`!m_bModGhost` dans les classes de pièges, copiées dans `mods\java`),
`RotateVehicleVertical` / `StoppedByFunnyGas` / `UFO` / `GoAfterUFO` ne font rien ; bulle de protection (effet + son)
et son du turbo d'après ses drapeaux.
`NetBonus(0)` dans le `nettest.scr` de l'hôte : course sans bonus (transmis à l'invité).

### Chocs entre voitures (protocole version 3)
Premier essai de la phase 2 : bonus OK, mais les voitures se traversaient (le drapeau 32 `_BLOCK_MOVE` du moteur,
qui fait qu'un objet arrête les autres, était retiré des fantômes). Maintenant :
- le fantôme **bloque** ma voiture comme une voiture locale ; `Vehicle.OnCollisionFullInfoMove` calcule le choc
  d'origine pour les deux voitures : ma part s'applique ici, celle du fantôme (poussée + vitesse perdue) est cumulée
  puis envoyée à son joueur (`BUMP`, `ModNet.BumpGhost` / `ApplyBump`), qui l'applique à sa vraie voiture avec le
  bruit du choc. Si les deux jeux voient le contact (moins de 150 ms d'écart), chacun ne garde que sa propre part ;
- le fantôme est **affiché au présent** : interpolé entre deux états (retard ajusté par joueur, 60 à 250 ms selon
  l'âge des paquets) puis prolongé jusqu'à maintenant avec sa vitesse (calculée à la réception). Avant, il était
  montré 100 ms en retard : côte à côte, chacun se voyait devant, et on aurait heurté une voiture déjà partie ;
- traversable quand sa voiture l'est (enlevée par l'OVNI, drapeau `NOBLOCK`), quand le joueur est parti, ou s'il
  s'est retrouvé sur ma voiture (centres plus proches que son ellipsoïde : le moteur pourrait m'y coincer).
Journal : `chocs : ma voiture bloquante …, ellipsoide …` au départ, et en fin de course
`chocs : N images de contact avec un fantome, N chocs envoyes, N recus (N ignores…), N images de chevauchement`.

Limites connues : on ne voit pas voler les projectiles des autres (seulement l'effet quand ils touchent, et les
explosions) ; le fantôme ne joue pas les animations de son pilote (lancer, coup reçu) ; chaque jeu a ses propres
boîtes bonus (un fantôme n'en ramasse pas). Pièges : retirés partout seulement dans leurs 50 premières secondes
(gaz : 12), après le jeu les supprime de lui-même.

### À vérifier en jeu (phase 2)
Astuce : le « bonus imposé » du menu Mods (dans les deux jeux) permet d'essayer chaque bonus à la suite.
- [ ] Projectiles sur le fantôme (bouchon, guêpes, tomate, noix de coco / boule de neige / cookie, fusée, missile,
      pistolets à glace et à eau) : l'effet arrive chez l'autre, avec une ligne `bonus : … du joueur N` dans son journal.
- [ ] Protection chez la victime : `arrete par la protection`, rien ne se passe ; la bulle se voit sur le fantôme.
- [ ] Pièges (banane, huile, bouteille, enclume, catapulte, gaz) : visibles chez l'autre au même endroit ; il glisse
      ou explose en roulant dessus ; le piège disparaît alors dans les deux jeux. Le fantôme passe dessus sans rien.
- [ ] Bombe / pastèque près de l'autre : explosion visible chez lui, il est soufflé.
- [ ] OVNI : l'autre est enlevé puis reposé chez lui, et on le voit disparaître / réapparaître chez soi.
- [ ] Turbo de l'autre : son du moteur plus aigu.
- [x] Bonus ramassés et lancés sur l'autre (premier essai : « ça marche bien »).
- [x] Chocs (essai du 2026-11-20 : « le choc marche bien ») : on ne traverse plus l'autre ; en le percutant, les deux voitures sont secouées / ralenties, dans les
      deux jeux, avec le bruit du choc. Pas de voiture coincée l'une dans l'autre.
- [ ] Côte à côte : le fantôme est au bon endroit (on ne se voit plus tous les deux devant), sans saccades.

## Phase 3 — serveur et salons (`server\`)
Réécrit d'après celui de pet_soccer (salons à 2, Elo, file d'attente, liaison directe), **pour 6 places**. Rien de la
file d'attente ni de l'Elo pour l'instant (phase 4), pas de liaison directe : tout passe par le relais.

```powershell
python server\server.py                     # port UDP 7778, comptes dans server\players.db
python server\server.py --lag 80 --loss 5   # mauvaise connexion simulée sur les paquets relayés
python server\test_server.py                # vérifie le serveur sans le jeu (43 points)
```
Options : `--code TEST` (code imposé, un salon à la fois), `--timeout` (30 s, joueur oublié), `--stale` (10 s, un muet
cède sa place dans un salon plein ; jamais l'hôte), `--takeover` (3 s, un jeu relancé avec le même compte reprend sa
place), `--jitter`, `-v`.

Salon : 6 places numérotées 0 à 5. Le créateur prend la place 0 et devient l'hôte ; un arrivant prend la plus petite
place libre et la garde. **L'hôte qui part ne ferme pas le salon** : la plus petite place restante devient l'hôte
(une course en cours peut continuer chez les autres). Salon vide = fermé. L'hôte peut fermer le salon aux arrivants
(drapeau `ROOM_LOCKED` de son `STATE`, pendant une course).

Paquets (`server\protocol.py`, magique `PRS1`, à tenir à jour avec `mods/java/ModLobby.java`) :

| Paquet | Sens | Contenu |
|---|---|---|
| 16 `HELLO` | jeu → serveur | version du dialogue (1), version du mod (`ModNet._VERSION`), nom du profil, jeton du compte |
| 17 `CREATE`, 18 `JOIN` + code, 19 `LEAVE` | jeu → serveur | répétés jusqu'à ce que `INFO` le confirme |
| 20 `STATE` | jeu → serveur | drapeaux du salon (lus chez l'hôte seulement) + ce que le joueur montre aux autres (animal, prêt ; chez l'hôte : circuit, tours, bonus, course lancée), recopié sans être lu |
| 24 `RELAY` | jeu → serveur | place visée (255 = tout le salon) + un paquet de course `PRN1` de `ModNet`. Pas de réponse. |
| 32 `INFO` | serveur → jeu | code, ma place, place de l'hôte, drapeaux, refus d'entrée (inconnu, plein, autre version, fermé), joueurs connectés, les 6 noms |
| 33 `PEER` | serveur → jeu | place + dernier `STATE` de ce joueur (envoyé aussi d'un coup à l'arrivant) |
| 35 `FROM` | serveur → jeu | place de l'expéditeur + le paquet de course |

Côté jeu, première étape (faite) : la course à 2 de la phase 1 passe par le serveur.
- `ModLobby` (Java 1.3) : attend le menu principal (profil choisi), se présente (`HELLO`, jeton du profil dans
  `online.dat` à la racine du jeu, une ligne `profil;jeton`), crée le salon (hôte) ou le rejoint par son code
  (réessai toutes les 2 s si refusé), puis redit son `STATE` 4 fois/s (1 fois/s en course ; salon fermé aux
  arrivants dès que l'hôte lance la course). Serveur relancé (`UNKNOWN`) : il se représente.
- `ModNet` ne change presque pas : en salon, l'invité vise l'hôte à l'adresse fictive « place N » (`ModAddr(slot)`,
  `ModNet.StartGuestRelay`), `ModUdp.Send` emballe tout envoi vers une telle adresse dans un `RELAY`
  (`ModLobby.SendRelay`), et `ModLobby` rend les `FROM` à `ModNet.Receive` avec l'adresse « place de l'expéditeur ».
  L'étoile hôte ↔ invités reste ; l'hôte donne à l'invité sa place du salon comme numéro de joueur.
- Fin de course (`BYE`) : le jeu quitte le salon (`LEAVE` ×3). Une course par lancement, comme en phase 1.
- Réglage : `NetRoom("TEST")` dans `nettest.scr` (avec `NetAuto("host")` ou `("join")`) ; l'adresse du serveur vient
  de `Data\online.scr`. Sans `NetRoom`, course en direct comme avant.
- Test : `mods\online-test.ps1 -Server` lance `server\server.py --code TEST -v` dans sa fenêtre, ajoute
  `NetRoom("TEST")` aux deux `nettest.scr` le temps du test (remis ensuite, ou au lancement suivant).
- Journal : lignes `[mod] net salon : …` (connexion, salon, places, entrée refusée, serveur muet, départ avec le
  nombre de paquets relayés) ; côté serveur, sa fenêtre (`-v` : bilan toutes les 10 s).

Suite (plus bas) : IA de l'hôte pour compléter la grille, hôte remplacé **pendant** une course.

Journal : le « decalage d'horloge » de l'invité s'affichait énorme (−1 795 296 329 728 ms) parce que l'heure de l'hôte
voyage sur 32 bits ; le calcul (`HostNow`, ramené à 32 bits) était juste. Affiché ramené à 32 bits depuis la phase 3.

### Essai du 2026-11-20 par le serveur, et corrections
Course jouée de bout en bout par le serveur (feux à 1-2 ms près, ~25 états/s, aller-retour 1 ms, chocs et bonus
relayés, départ de l'hôte vu par l'invité, serveur qui passe l'hôte à la place 1). Défauts vus et corrigés :
- **Pas de podium, puis « une cinématique »** : c'était la démo du menu principal. `KidsMainMenuWindow.Show` programme
  `RunDemo` 90 s plus tard ; la course en ligne part du menu sans l'annuler, la minuterie se réveille au retour et
  la démo remplace le podium. `ModNet.LaunchRace` annule maintenant `RunDemo`. Au passage, la démo plantait dans
  `Vehicle.FinishRace` (aucun joueur, `m_PlayersTab.elementAt(0)`, défaut d'origine) : protégé.
- **Attente des autres = rien à faire** : après son arrivée, **mode spectateur**. 3 s après la ligne, la caméra suit
  un joueur encore en course (`RacerCamera.SetObserved` sur son fantôme), puis le suivant 3 s après son arrivée ;
  flèches gauche / droite : changer de joueur. Ligne d'aide en bas de l'écran (`KidsHud.m_Spec`, `ModNet.HudText`).
  Choix du 2026-11-20 (essai validé) : **on attend que tous soient arrivés**, sans limite de temps ni touche pour
  passer (un joueur parti ou muet 5 s ne compte plus). Le podium arrive ensuite 16 s après sa propre arrivée comme
  hors ligne, mais 4 s au moins après la fin de l'attente (`ModNet.StopTimeAfterRace`).
  Le fantôme a maintenant son vecteur « droite » à jour (caméra d'arrivée).
- **Boîtes bonus non partagées** : chaque jeu a ses boîtes, une boîte prise par l'un restait chez l'autre, qui pouvait
  la prendre aussi. Événement `PICK` (position d'origine de la boîte, `ModNet.OnBoxTaken` depuis `BonusItem`, copié
  dans `mods\java`) : chez les autres, la boîte la plus proche (200 au plus) s'envole et revient 10 s plus tard
  comme si elle avait été prise sur place. Deux joueurs qui la touchent dans la même fraction de seconde l'ont
  tous les deux. `ModNet._VERSION` = 4.
- Journal : `spectateur : je regarde le joueur N (nom)`, `podium (tous arrives)`, et dans `fin de course : …` le nombre
  de boîtes prises par les autres.

### À vérifier en jeu (phase 3, course par le serveur)
- [x] `mods\online-test.ps1 -Server` : la fenêtre du serveur s'ouvre (`serveur Pet Racer sur le port UDP 7778`),
      les deux jeux s'y présentent (`bonjour …`), salon `TEST` créé, l'invité entre en place 1.
- [x] La course se lance seule et se joue comme en direct (fantômes, bonus, chocs, arrivée ; podium : voir ci-dessous).
- [x] Journaux : `[mod] net salon : salon TEST : place …`, puis `quitte le salon TEST (N paquets … envoyes, N recus)`.
- [x] Après fermeture, les deux `nettest.scr` sont remis (pas de ligne `NetRoom`).
- [x] Podium affiché dans les deux jeux, sans démo qui part à la place.
- [x] Arrivé le premier : 3 s après, la caméra suit l'autre, ligne d'aide en bas (« ça marche bien »).
- [x] Plus d'Espace : le podium attend que tous soient arrivés (essai du 2026-11-20 : « tout marche »).
- [x] Boîte bonus prise par l'un : elle s'envole aussi chez l'autre, qui ne peut plus la prendre ; elle revient
      ~10 s plus tard dans les deux jeux (essai du 2026-11-20 : « tout marche »).

## Phase 3 — écran « En ligne » (protocole version 5)
Bouton **EN LIGNE** (globe) au menu principal, à gauche de MODS → `KidsOnlineMenuWindow`, même mise en page que
l'écran Mods (panneau `OnlinePanel.png`, lignes « libellé < valeur > », flèches, OK / Retour ; images de
`mods/art/make_online.py`, textes `&Onl…&` de `make_texts.py`). L'écran ne fait qu'afficher (rafraîchi 5 fois/s) :
le réseau tourne dans `ModOnline.Frame`. À l'ouverture, `ModOnline.OpenOnline` ouvre le socket s'il ne l'est pas
(port de `NetPort`, sinon au choix du système) et `ModLobby.Connect` se présente au serveur de `Data\online.scr`.

- **Accueil** : « Que faire ? < Créer un salon / Rejoindre un salon > » ; taper des lettres passe en « Rejoindre » et
  remplit le code (4 lettres, Retour arrière efface). OK : crée ou rejoint. Ligne d'état : connexion, nombre de
  joueurs en ligne, ou la raison d'un refus (code inconnu, plein, autre version, course en cours). Retour : menu
  principal (on quitte le serveur).
- **Salon** : code en haut, les 6 places (nom, animal, hôte / prêt / pas prêt / pas revenu / arrive…), puis
  « Ton animal » (flèches ou clic : ouvre l'écran classique du choix de l'animal, modèle 3D et barres de stats, en
  mode « en ligne » — `KidsAnimalChoiceMenuWindow.m_bOnline` : les 12, pour tous ; OK donne l'animal au salon, OK et
  Retour ramènent ici ; figé quand l'invité est prêt), « Prêt » (invité), « Circuit » (les 31, aller et
  retour, Royaume du Lion), « Tours » (1 à 10), « Boîtes bonus » : réglés par l'hôte, grisés chez les invités.
  OK : l'invité se dit prêt (ou plus) ; l'hôte lance quand tous les invités sont prêts et revenus à l'écran.
  Retour : sort du salon (accueil).
- **Courses à la suite** : après le podium (ou une course quittée), retour à cet écran, toujours dans le salon
  (`ModNet.BackToLobby` : les « prêt » sont remis à non, un joueur parti pendant la course est oublié par l'hôte et
  revient par son JOIN). Course quittée : `BYE` aux autres, comme avant ; l'hôte continue de répondre à ce joueur
  (sinon il le croirait parti) pendant que la course se finit chez lui. Le salon est fermé aux arrivants pendant
  chaque course.
- **Hôte remplacé entre deux courses** : `ModLobby.SyncNet` fait suivre le salon à `ModNet` (hôte si c'est ma place,
  sinon invité de la place de l'hôte) ; l'hôte n'est plus forcément en place 0 (`ModNet.m_nHostSlot`). Le nouvel
  hôte garde le circuit, les tours et les bonus de l'ancien (`ModNet.StartHost`). Pendant une
  course : voir « Hôte remplacé en pleine course » plus bas.
- Protocole `ModNet` v5 : `JOIN` + « prêt » ; dans `INFO`, un octet d'état par joueur (1 parti, 2 prêt, 4 à l'écran).
- Le test automatique (`nettest.scr` avec `NetAuto`) marche comme avant : menu principal, une course, puis on quitte.

Test : `mods\online-test.ps1 -Screen` lance `server\server.py -v` (codes au hasard) et retire `NetAuto` / `NetPeer` /
`NetRoom` / `NetTest` des deux `nettest.scr` le temps du test (remis ensuite).

### À vérifier en jeu (écran « En ligne ») — validé le 2026-11-20
- [x] Bouton EN LIGNE (globe) à gauche de MODS, il réagit à la souris et aux flèches.
- [x] Jeu A : l'écran s'ouvre, « N joueurs en ligne » ; Créer un salon → code de 4 lettres en haut, « Seul dans le
      salon ».
- [x] Jeu B : taper le code (clavier AZERTY : les bonnes lettres ?), OK → B apparaît dans les deux listes ; un mauvais
      code → « Code inconnu ».
- [x] Animal changé dans B : visible chez A ; circuit / tours / bonus changés par A : visibles (grisés) chez B.
- [x] B fait OK → « prêt » chez A, « Tout le monde est prêt » ; A fait OK → la course part dans les deux jeux, avec
      les bons animaux, circuit, tours et bonus.
- [x] Après le podium : retour à l'écran dans les deux jeux, toujours dans le salon, B « pas prêt » ; deuxième course.
- [x] B quitte une course (Échap → Oui) : retour à l'écran chez B, A finit seul ; ensuite B peut repartir.
- [x] A fait Retour (sort du salon) : B devient l'hôte (réglages de l'hôte chez B) ; A peut revenir avec le code.

Essai du 2026-11-20 : « ça marche » (tous les points ci-dessus). Deux retours, traités :
- Choix de l'animal par « < animal > » trop pauvre → écran classique (voir « Salon » plus haut).
- « 2 tours demandés, 7 joués ? » : le `crash.log` des deux jeux dit 1 tour pour la seule course gardée (quittée
  avant la ligne) ; la chaîne écran → `INFO` → `CoreLogic.SetLapsNo` → `m_iNofLoops` ne change pas le nombre. Le
  journal « course chargee » donne maintenant les tours réellement pris par la course, à regarder au prochain essai.
  Trouvé en route : l'hôte remplaçant repartait sur ses propres réglages (circuit 0, 3 tours) → corrigé.
  Fin de l'histoire (retour du joueur) : le nombre de tours était bon, c'est la barre de progression à gauche qui
  était découpée en 7. `KidsDistance` n'a d'image que pour 3 tours (`Pasek_3`) et 7 tours (`Pasek_7`, `Pasek_5` en
  est une copie, bug d'origine) et prend celle de 7 pour toute autre valeur. Corrigé pour tout le jeu (solo, extras à
  10 tours, en ligne) : `ModPasek_1.png` … `ModPasek_10.png` (`mods/art/make_lap_bars.py`, recoloration de `Pasek_3`),
  un morceau par tour, séparations là où l'icône passe la ligne (centre de l'icône de 0.9 au départ à 0.1 à l'arrivée).

### À vérifier en jeu (choix de l'animal en ligne, barre des tours) — validé le 2026-11-20
- [x] Ligne « Ton animal », flèche ou clic : l'écran classique s'ouvre sur l'animal déjà choisi, les 12 défilent
      (même non débloqués), barres de stats à jour.
- [x] OK : son de l'animal, retour au salon avec le nouvel animal, visible chez l'autre jeu. Retour : rien ne change.
- [x] Pendant que B choisit, A le voit « pas prêt » (pas « pas revenu ») et ne peut pas lancer.
- [x] Au retour, l'écran « En ligne » est normal (fond, voitures qui défilent, lignes).
- [x] Journal : « course chargee en … ms (N tours) » avec N = la valeur de la ligne « Tours ».
- [x] Barre de progression à gauche : N morceaux pour N tours (1, 2, 5…), l'icône change de morceau à chaque
      passage de ligne.
- [x] Hôte remplacé : B garde le circuit / les tours / les bonus de A.

## Phase 3 — IA de l'hôte et hôte remplacé en course (protocole version 6)
Fait le 2026-11-20. Premier essai le même jour (2 joueurs + 4 IA expert) : « tout a l'air de fonctionner », sauf
les chocs invité / IA et les IA souvent dans le décor — corrigés, voir « Corrections après le premier essai ».

### IA de l'hôte
- Écran « En ligne » : deux lignes de plus, réglées par l'hôte et grisées chez les invités. **IA (places libres)** :
  combien d'IA, 0 à 5, bornée aux places libres du salon (5 au départ = la grille complétée à 6) ; **Niveau des IA** :
  d'origine / auto / facile / normal / difficile / expert, la difficulté du menu Mods au départ (changer la ligne ne
  touche pas à `mods.dat`). Les places libres qui recevront une IA sont marquées « N. IA — au hasard » dans la liste.
  Lignes plus serrées (0,043 au lieu de 0,05) pour tenir les 7 lignes de l'invité.
- Au lancement (`ModNet.HostLaunch` → `AddAIPlayers`) : chaque IA devient un « joueur » de la session à une place
  libre (`ModNetPlayer.m_bAI`, sans adresse ni lien d'événements), avec un animal au hasard qu'aucun autre n'a, nommé
  « IA ». `INFO` les donne aux invités (drapeau 8 par joueur, plus le nombre et le niveau des IA). Elles disparaissent
  au retour à l'écran (`BackToLobby`).
- Chez l'hôte : vraies IA (`RaceParams.AddModAI` depuis `ModNet.CreateGhosts`, dans `m_AITab`), sur la grille à leur
  place comme les joueurs, lancées au vert. Leur état part 25 fois/s comme celui de l'hôte (`SendCar(voiture, place)`).
  Chez les invités : des fantômes comme les autres.
- Bonus et chocs : un invité qui touche le fantôme d'une IA envoie `HIT` / `BUMP` à sa place ; l'hôte, qui voit que
  cette place est une de ses IA (`ModNet.LocalCar`), l'applique à l'IA (protection comprise) au lieu de relayer.
  Les IA visent aussi les fantômes des invités, et leurs chocs contre eux partent avec la place de l'IA comme
  origine. Le « contact déjà vu ici » des chocs est tenu par paire de voitures (`m_aContact`), plus par joueur.
  Pièges, boîtes et explosions des IA passent comme ceux de l'hôte.
- Difficulté : `ModAI.Mode()` prend celle du salon en course en ligne (`ModNet.m_nAiMode`), la même dans tous les
  jeux. Élastique coupé en ligne aussi pour l'IA améliorée (il ne regardait que la voiture de l'hôte).
- Arrivée : `Vehicle` → `ModNet.OnAIFinished` ; classement aux temps comme pour les joueurs. **Le podium n'attend que
  les humains** : une IA pas encore arrivée est classée à la distance, comme hors ligne. Le spectateur suit de
  lui-même les humains encore en course ; les flèches passent aussi par les IA. La ligne d'aide ne compte que les
  humains.
- Lancer seul avec des IA reste refusé (« Seul dans le salon ») : ce serait une course hors ligne.

### Hôte remplacé en pleine course
Avant : l'hôte parti (Échap → Oui, jeu fermé), chaque invité retirait tout le monde (étoile cassée) et finissait seul.
Maintenant, par le serveur seulement (`ModNet.CanTakeOver` : salon, course chargée ou en cours, podium pas encore
lancé — à la fin, l'hôte qui part vers son podium se tait, ce n'est pas un départ) :
- Dès le `BYE` de l'hôte ou 5 s de silence (30 s pendant le chargement), chacun choisit le même remplaçant : le joueur
  restant (ni parti, ni IA) de plus petite place (`ModNet.HostLeftRace`). Les invités lui parlent à l'adresse
  « place N » (le serveur relaie), avec un lien d'événements neuf (ce que l'ancien hôte n'avait pas confirmé est
  perdu).
- Le remplaçant (`BecomeRaceHost`) : rôle d'hôte pour la suite de la course ; l'horloge commune est gardée (il était
  déjà calé sur l'ancien hôte) ; il reconnaît chaque invité par sa place à son premier `JOIN`, relaie voitures et
  événements, et fixe le top départ si l'ancien hôte est parti pendant le chargement.
- Ses IA (`TakeOverAI`) : chaque fantôme d'IA devient une IA de ce jeu, là où il est (`RaceParams.ModGhostToAI`).
  Avant le vert : sur la grille, lancée au vert comme les autres. Après : suivi des portes à partir de la dernière
  reçue, et l'IA vise le point de la piste des IA le plus proche devant elle (`VehicleAI.ModResume` ; `Start` vise le
  point 1, celui du départ : elle aurait fait demi-tour). Une IA déjà arrivée reste où elle est. Si le remplaçant
  part à son tour, le suivant reprend de la même façon.
- Après la course, `ModLobby.SyncNet` remet tout le monde d'accord avec le salon (hôte du salon = celui du serveur :
  l'ancien hôte s'il est resté dans le salon, sinon la plus petite place, en général le remplaçant).
- Limites : l'ancien hôte qui a seulement quitté la course reste hôte du salon, à l'écran « En ligne », et son salon
  se rouvre aux arrivants pendant que les autres finissent ; en course directe (`nettest.scr` sans `NetRoom`), pas de
  remplaçant (comme avant).

Journal : `hote : N IA en places libres, difficulte …`, `course … : N voitures dont N IA (…)`,
`IA N (IA Lion) arrive en … s`, `bonus : … du joueur N sur mon IA M`, `l'hote (place N, nom) a quitte la course : je
prends son role` / `la place N prend son role`, `je reprends N IA (…)`, `[mod] IA Lion : reprise en course, point P
de N`, et en fin de course `hote remplace N fois pendant la course (je finis hote / invite)`.
`mods\online-test.ps1` affiche maintenant aussi les lignes `[mod] IA …` des deux journaux.

### À vérifier en jeu (IA de l'hôte, hôte remplacé) — `mods\online-test.ps1 -Screen`
- [x] Écran du salon : lignes « IA (places libres) » et « Niveau des IA » sous « Boîtes bonus », lisibles, flèches
      bien placées, rien ne chevauche la ligne d'état ni OK / Retour (invité : 7 lignes).
- [x] Hôte : « IA » va de 0 à 4 à deux joueurs (repart à 0 après 4) ; les places marquées « IA — au hasard » suivent
      la valeur, chez l'hôte et chez l'invité (grisé).
- [x] Course à 2 + 4 IA : 6 voitures sur la grille dans les deux jeux, aux mêmes places ; les IA roulent normalement
      chez l'hôte et sont fluides chez l'invité ; mêmes places au classement dans les deux jeux.
- [x] L'invité touche une IA (bouchon, tomate…) : elle s'arrête dans les deux jeux (`… sur mon IA N` chez l'hôte).
      IA protégée : le tir est arrêté.
- [x] Une IA tire sur l'invité ou pose un piège devant lui : l'effet arrive chez l'invité.
- [ ] L'invité pousse une IA : elle est secouée chez l'hôte ; une IA pousse l'invité : il est secoué chez lui.
      (premier essai : l'invité passait au travers des IA ; refait, à revérifier)
- [ ] Arrivée : le podium n'attend pas les IA en retard ; même ordre dans les deux jeux. « Niveau des IA » facile
      puis expert : écart visible.
- [x] Retour à l'écran : plus d'IA parmi les joueurs, seulement les places « IA » prévues.
- [ ] L'hôte quitte en pleine course (Échap → Oui) : chez l'invité, la voiture de l'hôte disparaît, les IA continuent
      (journal : `je prends son role`, `je reprends 4 IA`, `reprise en course`) et la course va jusqu'au podium.
- [ ] Même chose pendant les feux : les IA partent au vert chez l'invité.
- [ ] L'hôte ferme son jeu en pleine course : l'invité reprend après ~5 s, pareil.
- [ ] Après une course reprise : retour à l'écran dans les deux jeux, l'hôte du salon est de nouveau celui du
      serveur, une nouvelle course part normalement.

### Corrections après le premier essai (2026-11-20)
- **Invités sans choc avec les IA.** Journal de l'essai : chez l'hôte, `0 images de contact avec un fantome` sur toute
  la course ; le moteur ne signalait jamais le contact entre une IA et le fantôme d'un invité (la voiture de l'hôte,
  elle, le voit ; cause dans le moteur pas trouvée). Une IA qui ne réagit pas chez l'hôte arrive sur l'invité,
  son fantôme lui rentre dedans et il passe au travers (règle anti-coincement). L'hôte teste maintenant lui-même, à
  chaque image, le recouvrement de chaque IA avec chaque fantôme d'invité (`ModNet.HostAIContacts`, rayon de
  l'ellipsoïde dans le plan) quand le moteur n'a rien dit pour la paire depuis 100 ms : les deux voitures sont
  repoussées le long de la normale du contact (vitesse de rapprochement × `_OBJ_HIT_SUPR`, plus une poussée de
  séparation), ralenties si elles fonçaient dans l'autre, bruit du choc ; la part de l'invité part en `BUMP` (il
  l'ignore s'il a déjà traité ce contact lui-même).
- Journal : au départ chez l'hôte `IA N : bloquante …, rayon …` ; en fin de course `chocs : images de contact par
  voiture visee : place 2 (IA) 40, place 0 12 … ; dont N vus par le test IA / fantome de l'hote`.
- **IA souvent dans le décor** : voir `notes/ia.md`, « Corrections après l'essai en ligne ».

## Phase 4 — partie rapide, Elo, classement (dialogue v2, `ModNet` v7)
Fait le 2026-11-20 (avec graphify), repris de la file d'attente de pet_soccer et étendu à 6 joueurs.

### Écran « En ligne »
- Accueil : « Que faire ? < **Partie rapide** / Créer un salon / Rejoindre un salon / **Classement** > ». En haut, mon
  Elo et mon rang (« Ton Elo : 1016 - 3e sur 12 », ou « pas encore de partie rapide »), redits par le serveur dans
  chaque `INFO` hors salon.
- Partie rapide : ligne « Ton animal » (choisi **avant** d'entrer dans la file : la course pourrait sinon partir
  pendant l'écran du choix de l'animal), OK = file d'attente (« Recherche d'adversaires : N en attente (12 s) »,
  Retour = on en sort). Course trouvée → salon classé : liste des joueurs (« IA — au hasard » dans les places libres),
  circuit / tours / niveau des IA grisés, « Partie rapide trouvée : départ dans quelques secondes ».
- Après la course : retour à l'accueil (un salon classé = une course), où la ligne du bas dit « Partie rapide : 2e
  sur 3, Elo +16 (1016) » (ou « on attend le résultat des autres », « pas comptée »). OK relance une recherche.
- Classement : OK ouvre la page des 10 premiers (nom, Elo, courses ; moi en jaune), mon rang en bas, rafraîchie
  toutes les 5 s ; Retour revient à l'accueil.

### Course classée
- Le serveur réunit jusqu'à 6 joueurs de la file d'Elo proches (écart accepté 150 + 10 par seconde d'attente, même
  version du mod, comptes différents) : tout de suite à 6, sinon quand le plus ancien attend depuis 15 s
  (`--fill`) et qu'ils sont au moins 2. Hôte tiré au sort, places 0.. dans l'ordre. Le salon n'a pas de code à
  donner (`JOIN` refusé comme « code inconnu »).
- Jeu de l'hôte (`ModNet.RankedHostFrame`) : circuit tiré au sort (les 31), tours et niveau des IA de
  `Ranked(tours, niveau)` dans `Data\online.scr` (3 tours, expert depuis le 2026-11-20 : la mémoire des IA est entraînée en expert), boîtes bonus, grille complétée à 6 par des IA.
  Lancement seul 4 s après l'arrivée dans le salon quand tous les joueurs sont là (20 s au plus pour un absent) ;
  rien après 45 s → salon quitté (« Partie rapide annulée »). Les invités sont prêts d'office et partent si rien ne
  se lance en 60 s. Ces réglages ne deviennent jamais ceux des salons à code (hôte remplacé, reprise de course).
- Triches du menu Mods coupées dans **toute** course en ligne (`ModOptions.IsCheater` / `GetSpeedMul`), classée ou non.
- Résultat : au podium (tous les humains arrivés), chaque jeu envoie au serveur l'ordre d'arrivée des humains
  qu'il a vu (`ModNet.ReportRanked` → `ModLobby.SendResult`, paquet `RESULT`, redit chaque seconde jusqu'à une
  réponse `RATED` définitive, même après la sortie du salon). Course quittée (Échap → Oui) : `RESULT` « abandon ».
- Serveur (`rooms.Race`) : la course commence quand l'hôte ferme le salon (`STATE` verrouillé) ; ses joueurs sont
  les membres du salon à ce moment. Elle est tranchée quand chacun a envoyé son résultat ou est parti sans (sortie
  du salon, silence de 30 s), au plus tard 90 s après le premier « arrivé » (20 min en tout). Les partis sont
  classés derniers, à égalité entre eux. L'ordre des autres doit être le même chez tous ; sinon celui de la
  majorité stricte, sinon course pas comptée. Moins de 2 humains : pas comptée.
- Elo (`store.race_deltas`) : chaque paire d'humains = un duel (nul entre deux partis), variation = K / (N - 1) ×
  somme des écarts au résultat attendu ; K = 48 pendant les 10 premières courses, puis 32. Les IA ne comptent pas.
  Base `players.db` : colonne `wins` ajoutée d'elle-même aux bases de la phase 3, tables `races` / `race_players`.

Paquets ajoutés (`server\protocol.py`, `LOBBY_PROTO` = 2) :

| Paquet | Sens | Contenu |
|---|---|---|
| 21 `QUEUE` | jeu → serveur | redit chaque seconde tant qu'on attend (`LEAVE` pour sortir de la file) ; réponse `INFO` |
| 22 `RESULT` | jeu → serveur | drapeaux (1 abandon), nombre, places des humains arrivés dans l'ordre ; réponse `RATED` |
| 23 `TOP` | jeu → serveur | réponse `TOP_LIST` |
| 36 `RATED` | serveur → jeu | état (aucune, en attente, comptée, pas comptée), place parmi les humains, nombre d'humains, variation, Elo, rang |
| 37 `TOP_LIST` | serveur → jeu | nombre de classés, puis les 10 premiers (nom, Elo, courses) |
| `INFO` | | + drapeau de salon 2 (classé), joueurs en file, et hors salon : Elo, courses, rang, nombre de classés |

Test : `mods\online-test.ps1 -Screen` (serveur lancé avec `--fill 5` : à deux, la course part 5 s après).
Journal : `partie rapide : en file d'attente`, `partie rapide trouvee apres N s`, `salon XXXX (partie rapide,
classee)`, chez l'hôte `circuit N tire au sort…` puis `lancement`, à la fin `course classee : ordre d'arrivee des
humains …` et `course classee comptee : 1e sur 2, Elo +24 -> 1024, rang 1` ; côté serveur `partie rapide …`,
`course classée … : départ`, `annonce l'ordre …`, puis le bilan avec les variations d'Elo.

### À vérifier en jeu (phase 4) — `mods\online-test.ps1 -Screen`
- [ ] Accueil : la ligne « Que faire ? » passe par Partie rapide, Créer un salon, Rejoindre un salon, Classement ;
      les deux lignes d'aide changent ; en haut « Ton Elo : 1000 (pas encore de partie rapide) ».
- [ ] Partie rapide dans les deux jeux (animal choisi avant) : « Recherche d'adversaires », puis ~5 s plus tard le
      salon classé dans les deux jeux, 4 places « IA », réglages grisés ; la course part seule quelques secondes après.
- [ ] Course : 6 voitures, triches du menu Mods sans effet (vitesse, bonus imposé…) si on les avait laissées.
- [ ] Après le podium : retour à l'accueil dans les deux jeux, « Partie rapide : 1er sur 2, Elo +24 (1024) » chez le
      gagnant, « 2e sur 2, Elo -24 (976) » chez l'autre ; en haut l'Elo et le rang à jour.
- [ ] Classement : les deux joueurs, dans l'ordre, moi en jaune ; Retour revient à l'accueil.
- [ ] Une partie rapide où l'un quitte la course (Échap → Oui) : il est classé dernier, l'autre finit et gagne de l'Elo.
- [ ] Retour pendant la recherche : on sort de la file (« 0 en attente » chez l'autre jeu).
- [ ] Salons à code : rien n'a changé (créer, rejoindre, IA, réglages de l'hôte).
- [x] Validé par l'utilisateur le 2026-11-20 (« tout marche »).

## Phase 5 — installeur pour les amis
Repris de pet_soccer (`..\pet_soccer\mods\installer`), plus simple : pas de `code.pak` à refaire, les classes sont
copiées dans `java\` du jeu comme le fait `build.ps1`.

```powershell
powershell -ExecutionPolicy Bypass -File mods\package.ps1 -Server ton.adresse   # fabrique build\PetRacerMod.zip
powershell -ExecutionPolicy Bypass -File mods\package.ps1                       # sans adresse : demandée à l'ami
python mods\installer\test_installer.py                                         # essai sur une fausse copie du jeu
```
- `package.ps1` relance le build, puis met dans `build\PetRacerMod.zip` (moins de 1 Mo) : `fichiers\java\*.class`
  (les classes compilées), `fichiers\Data\…` (tout `mods\data`), l'installeur (`Installer.bat`, `Desinstaller.bat`,
  `install.ps1`, `LISEZMOI.txt`, de `mods\installer`), `mod.txt` (version en ligne `ModNet._VERSION`, date) et
  `originaux.txt` (SHA-256 des fichiers de `game_pristine` que le mod remplace). `-Server` / `-Port` (7778) écrivent
  l'adresse dans le `online.scr` du paquet. Refuse de partir avec un fichier personnel (`.dat`/`.cfg` hors de
  `Data\`, `.log`, `.db`, `nettest.scr`, `VideoSettings.scr`). `-NoBuild` : prend `build\classes` tel quel.
- `install.ps1` (PowerShell 5, sans rien à installer) :
  - dossier du jeu : `-Game`, sinon le registre (clé de désinstallation « Pet Racer » : l'installeur d'origine y
    note `C:\Jeux\uninstall Pet Racer.exe`, le jeu est dans `<dossier>\Pet Racer\game`), sinon `C:\Jeux`,
    `C:\Games`, Program Files (avec ou sans `Techland`), sinon une fenêtre de choix. Un dossier parent est accepté
    (il cherche `game`, `Pet Racer\game`). Jeu trouvé seul : il demande confirmation.
  - jeu ouvert : refus. Dossier en lecture seule (Program Files) : se relance en administrateur.
  - version du jeu : compare les originaux à `originaux.txt` ; différence = avertissement, on peut continuer.
  - sauvegarde les originaux remplacés dans `<jeu>\mod_backup` (une seule fois), liste de ce que le mod a posé dans
    `mod_backup\fichiers_du_mod.txt`. Mise à jour : ce qu'une version précédente avait posé et que la nouvelle n'a
    plus est remis d'origine ou retiré.
  - adresse du serveur : `-Server`, sinon celle du paquet, sinon celle déjà réglée (mise à jour), sinon demandée
    (Entrée = plus tard).
  - mémoire des IA : `Data\aimap_depart.dat` du paquet copiée en `aimap.dat` si le jeu n'en a pas (jamais écrasée,
    laissée à la désinstallation ; `package.ps1 -AiMap` la reprend de `game_dev\aimap.dat`, voir `notes/ia.md`).
  - `-Uninstall` (`Desinstaller.bat`) : remet les originaux, retire le reste, efface `mod_backup`. Les `*.dat` de la
    racine (profils, `mods.dat`, `online.dat`, `grandprix.dat`) et `progress.cfg` ne sont jamais touchés.
- `test_installer.py` : fausse copie dans `build\installer-test\Pet Racer\game` (exe, tout `java\` d'origine,
  données remplacées, fausses sauvegardes) ; installation (dossier parent donné), `java\` identique à
  `game_dev\java`, données, adresse, `mod_backup`, sauvegardes intactes ; mise à jour ; mise à jour vers un paquet
  sans `ModNet.class` ni `RacerGame.class` ; mémoire des IA de départ copiée, jamais écrasée, laissée à la
  désinstallation ; désinstallation = dossier identique octet par octet à avant (hors `aimap.dat`) ;
  avertissement si un original a été modifié ; mauvais dossier refusé.

### Reste à faire pour jouer par Internet
Héberger `server\server.py` (port UDP 7778) : sur ton PC avec le port UDP 7778 redirigé sur la box vers ce PC
(adresse fixe ou nom DuckDNS si l'IP de la box change), ou sur un VPS. Puis `package.ps1 -Server <adresse>` et
envoyer `build\PetRacerMod.zip`. Les amis n'ont aucun port à ouvrir (tout passe par le relais du serveur). Celui qui
héberge sur son PC joue avec l'adresse publique aussi (ou `127.0.0.1` dans son propre `online.scr`, si sa box ne
gère pas le « hairpin NAT »).

### À vérifier chez un ami
- [ ] Le zip décompressé, `Installer.bat` : jeu trouvé (ou choisi), adresse demandée, « Mod installé ».
- [ ] Le jeu démarre, menus du mod présents, écran « En ligne » : connexion au serveur.
- [ ] `Desinstaller.bat` : le jeu d'origine revient.
