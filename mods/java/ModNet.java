// [mod] Course en ligne « fantômes » (chantier 8, phase 1 ; notes/online.md).
//
// Chaque jeu simule SA voiture et envoie son état 25 fois par seconde ; les voitures des autres sont des fantômes
// (Vehicle.m_bModGhost) posés d'après ces états, avec 100 ms de retard pour interpoler entre deux paquets.
// Un hôte (slot 0 en direct, sa place du salon par le serveur) et des invités, en étoile : les invités parlent à
// l'hôte, qui relaie leurs voitures
// aux autres invités. Pas d'événement fiable en phase 1 : tout ce qui compte est redit dans chaque paquet
// (paramètres de course, départ, arrivée), une perte ne coûte rien.
//
// Paquets (ModBuf, gros-boutiste) : magique "PRN1", type, puis :
//   JOIN  invité → hôte, 4/s : identifiant, version, animal, nom, au menu ?, course chargée (n°), heure (synchro)
//   INFO  hôte → invité, en réponse : slot donné, heures (synchro), n° de course, lancée ?, circuit, tours, top
//         départ (heure de l'hôte), joueurs (animal, nom, parti ?)
//   CAR   propriétaire → hôte → autres, 25/s : slot, heure (de l'hôte), position, avant, haut, angles, vitesse,
//         braquage, tour, porte, drapeaux, temps d'arrivée
//   BYE   slot : le joueur quitte (envoyé 3 fois)
// Horloge : l'invité cale la sienne sur celle de l'hôte (aller-retour le plus court des JOIN/INFO) ; toutes les heures
// des paquets sont « heure de l'hôte » en ms sur 32 bits (écarts calculés en int, le débordement ne gêne pas).
class ModNet {
   static final int _MAGIC = 0x50524E31; // "PRN1"
   // 2 : bonus en ligne (phase 2) ; 3 : chocs entre voitures ; 4 : boîtes bonus partagées ; 5 : écran « En ligne »
   // (prêt dans JOIN, état des joueurs dans INFO, hôte à n'importe quelle place) ; 6 : IA de l'hôte (nombre et
   // niveau dans INFO, drapeau « IA » par joueur), hôte remplacé en pleine course ; 7 : partie rapide classée (phase
   // 4), triches du menu Mods coupées en ligne
   static final int _VERSION = 7;
   static final int _T_JOIN = 1;
   static final int _T_INFO = 2;
   static final int _T_CAR = 3;
   static final int _T_BYE = 4;
   static final int _MAX_PLAYERS = 6;
   static final int _NO_SLOT = 255;

   static final int _ROLE_NONE = 0;
   static final int _ROLE_HOST = 1;
   static final int _ROLE_GUEST = 2;

   static final int _ST_IDLE = 0; // pas de session
   static final int _ST_LOBBY = 1; // connectés, au menu
   static final int _ST_LOADING = 2; // course lancée, circuit en chargement
   static final int _ST_RACE = 3; // circuit actif (feux, course, arrivée)
   static final int _ST_DONE = 4; // course finie ou quittée

   static final long _JOIN_MS = 250L;
   static final long _CAR_MS = 40L;
   // silence toléré : long avant la course (chargement du circuit = plusieurs secondes sans image), court ensuite
   static final long _TIMEOUT_WAIT_MS = 30000L;
   static final long _TIMEOUT_RACE_MS = 5000L;
   // top départ = heure où tous ont chargé + ce délai (le paquet doit arriver avant)
   static final int _GO_DELAY_MS = 1500;
   // on n'attend pas plus un joueur qui charge
   static final long _READY_WAIT_MS = 60000L;
   // après la première arrivée, on attend les autres au plus ce temps avant le podium
   static final int _INTERP_DELAY_MS = 100; // retard d'affichage de départ, puis ajusté par joueur (ModNetPlayer.OnSnap)
   static final int _INTERP_MIN_MS = 60;
   static final int _INTERP_MAX_MS = 250;
   static final int _EXTRAP_MAX_MS = 300; // au-delà, le fantôme s'arrête où il est (plus de nouvelles)

   static final int _F_FINISHED = 1;
   static final int _F_TURBO = 2;
   static final int _F_SPIKES = 4;
   static final int _F_PROTECTION = 8;
   static final int _F_NOBLOCK = 16; // voiture traversable (enlevée par l'OVNI)

   static ModUdp m_Udp = null;
   static final ModBuf m_Out = new ModBuf(ModUdp._MAX_PACKET);
   static final ModBuf m_In = new ModBuf(0);
   static int m_nRole = _ROLE_NONE;
   static int m_nState = _ST_IDLE;

   // réglages du test automatique (nettest.scr, lus par ModOnline)
   static boolean m_bAuto = false;
   static boolean m_bAutoDone = false;
   static int m_nWantPlayers = 2;
   static int m_nCfgTrack = 0;
   static int m_nCfgLaps = 3;
   static int m_nCfgAnimal = 0;
   static boolean m_bCfgBonus = true;
   // [mod] IA de l'hôte (écran « En ligne ») : nombre voulu (borné aux places libres au lancement), difficulté
   // (ModAI : 0 d'origine … 5 expert ; -1 = celle du menu Mods, lue au premier usage)
   static int m_nCfgAi = _MAX_PLAYERS - 1;
   static int m_nCfgAiMode = -1;

   // invité
   static ModAddr m_HostAddr = null;
   static int m_nGuestId = 0;
   static long m_lNextJoin = 0L;

   // session
   static ModNetPlayer[] m_Players = new ModNetPlayer[_MAX_PLAYERS];
   static int m_nMySlot = _NO_SLOT;
   static int m_nHostSlot = 0; // [mod] phase 3 : place de l'hôte (celle du salon)
   static boolean m_bWantReady = false; // [mod] écran « En ligne » : l'invité s'est dit prêt
   static int m_nRaceId = 0; // course demandée par l'hôte (0 = aucune)
   static boolean m_bLaunched = false; // l'hôte a lancé la course m_nRaceId
   static int m_nTrack = 0;
   static int m_nLaps = 3;
   static boolean m_bBonus = true; // boîtes bonus dans la course (choix de l'hôte)
   static int m_nAi = 0; // IA de la course (choix de l'hôte, redit dans INFO)
   static int m_nAiMode = ModOptions._AI_AUTO; // leur difficulté, la même pour qui les reprend (ModAI.Mode)
   static boolean m_bGoKnown = false;
   static int m_nGoTime = 0; // heure de l'hôte où les feux s'allument (vert 6 s plus tard)

   // course locale
   static boolean m_bRaceOnline = false; // la course chargée est une course en ligne
   static int m_nLoadedRace = 0; // n° de la course chargée ici
   static boolean m_bLocalReady = false;
   static long m_lLaunchTime = 0L;
   static boolean m_bLightsStarted = false;
   static long m_lNextCar = 0L;
   static int m_nCarSeq = 0;
   static long m_lFirstFinish = 0L;
   static boolean m_bFinishCalled = false;

   // horloge de l'hôte vue d'ici
   static long m_lClockOffset = 0L;
   static int m_nBestRtt = Integer.MAX_VALUE;

   // statistiques pour le journal
   static int m_nCarsSent = 0;
   static int m_nCarsRecv = 0;
   static long m_lNextStats = 0L;

   static void Log(String s) {
      GameObject.CrashLog("[mod] net " + s + "\n");
   }

   static long Now() {
      return System.currentTimeMillis();
   }

   static int HostNow() {
      return (int)(Now() + m_lClockOffset);
   }

   // --- démarrage (ModOnline.Init, socket ouvert) ---

   static void Init(ModUdp udp, String sAuto, String sPeer, int nPeerPort) {
      m_Udp = udp;
      if (sAuto == null) {
         return;
      }

      m_bAuto = true;
      if (sAuto.equals("host")) {
         StartHost();
      } else if (sAuto.equals("join")) {
         StartGuest(sPeer, nPeerPort);
      } else {
         Log(": NetAuto(\"" + sAuto + "\") inconnu (host ou join)");
         m_bAuto = false;
      }
   }

   static void StartHost() {
      StartHost(0);
   }

   // [mod] phase 3 : nSlot = place de l'hôte dans le salon (0 au départ, une autre si l'hôte a été remplacé)
   static void StartHost(int nSlot) {
      if (m_nRole == _ROLE_GUEST && m_nLaps >= 1 && !IsRankedLobby()) {
         // hôte remplacé : il garde le circuit, les tours et les bonus de l'ancien hôte (vus dans ses INFO) ; pas en
         // partie rapide, dont les réglages imposés ne doivent pas devenir ceux de mes salons à code
         m_nCfgTrack = m_nTrack;
         m_nCfgLaps = m_nLaps;
         m_bCfgBonus = m_bBonus;
         m_nCfgAi = m_nAi;
         m_nCfgAiMode = m_nAiMode;
      }

      ResetSession();
      m_nRole = _ROLE_HOST;
      m_nState = _ST_LOBBY;
      m_nMySlot = nSlot;
      m_nHostSlot = nSlot;
      m_Players[nSlot] = new ModNetPlayer(nSlot);
      m_Players[nSlot].m_nAnimal = m_nCfgAnimal;
      m_Players[nSlot].m_sName = LocalName();
      m_nTrack = m_nCfgTrack;
      m_nLaps = m_nCfgLaps;
      m_bBonus = m_bCfgBonus;
      m_nAi = m_nCfgAi;
      m_nAiMode = CfgAiMode();
      Log("hote (place " + nSlot + ") : " + (m_bAuto ? "en attente de " + (m_nWantPlayers - 1) + " invite(s), " : "") + "circuit " + m_nTrack + ", " + m_nLaps + " tours");
   }

   // [mod] phase 3 : quitte la session (salon quitté, ou hôte remplacé : ModLobby relance aussitôt)
   static void StopSession() {
      if (m_nRole != _ROLE_NONE) {
         Log("session arretee (" + (m_nRole == _ROLE_HOST ? "hote" : "invite") + ")");
      }

      ResetSession();
      m_nRole = _ROLE_NONE;
      m_nState = _ST_IDLE;
      m_HostAddr = null;
      m_bWantReady = false;
   }

   static void StartGuest(String sHost, int nPort) {
      ResetSession();
      m_HostAddr = ModAddr.Resolve(sHost, nPort);
      if (m_HostAddr == null) {
         return;
      }

      m_nRole = _ROLE_GUEST;
      m_nState = _ST_LOBBY;
      m_nGuestId = (int)(Now() ^ (long)m_Udp.GetLocalPort() << 20) | 1;
      Log("invite : connexion a " + m_HostAddr);
   }

   // [mod] phase 3 : invité d'un salon du serveur, l'hôte est à la place nHostSlot (ModLobby)
   static void StartGuestRelay(int nHostSlot) {
      ResetSession();
      m_nHostSlot = nHostSlot;
      m_HostAddr = new ModAddr(nHostSlot);
      m_nRole = _ROLE_GUEST;
      m_nState = _ST_LOBBY;
      m_nGuestId = ModLobby.m_nClientId;
      Log("invite : connexion a l'hote, " + m_HostAddr);
   }

   static void ResetSession() {
      for (int i = 0; i < _MAX_PLAYERS; i++) {
         m_Players[i] = null;
      }

      m_nMySlot = _NO_SLOT;
      m_nHostSlot = 0;
      m_nRaceId = 0;
      m_bLaunched = false;
      m_bGoKnown = false;
      m_lClockOffset = 0L;
      m_nBestRtt = Integer.MAX_VALUE;
   }

   static String LocalName() {
      String s = RacerGame.GetCoreLogic() != null ? RacerGame.GetCoreLogic().m_Name : null;
      return s != null ? s : "?";
   }

   // --- à chaque image (ModOnline.Frame, menus comme course) ---

   static void Frame() {
      if (m_nRole == _ROLE_NONE || m_nState == _ST_IDLE || m_nState == _ST_DONE) {
         return;
      }

      long lNow = Now();
      if (m_nRole == _ROLE_GUEST && lNow >= m_lNextJoin && m_nState != _ST_DONE) {
         SendJoin();
         m_lNextJoin = lNow + _JOIN_MS;
      }

      CheckTimeouts(lNow);
      if (m_nState == _ST_LOBBY) {
         LobbyFrame();
      }

      if (lNow >= m_lNextStats && m_nState == _ST_RACE) {
         m_lNextStats = lNow + 10000L;
         Log("etat : " + m_nCarsSent + " envoyes, " + m_nCarsRecv + " recus, decalage d'horloge " + (int)m_lClockOffset + " ms (aller-retour " + m_nBestRtt + " ms)");
      }
   }

   static boolean AtMainMenu() {
      return RacerGame.game != null && RacerGame.game.activeMenu == 1 && RacerGame.firstMenuModule != null
         && RacerGame.firstMenuModule.m_CurrentMenu == KidsSimpleMenuModule._MainMenuWindow && !RacerModule.m_bIsDemo;
   }

   // [mod] phase 3 : l'écran « En ligne » est ouvert (attente entre deux courses)
   static boolean AtOnlineScreen() {
      return RacerGame.game != null && RacerGame.game.activeMenu == 1 && RacerGame.firstMenuModule != null
         && (RacerGame.firstMenuModule.m_CurrentMenu == KidsSimpleMenuModule._OnlineMenuWindow
            || RacerGame.firstMenuModule.m_CurrentMenu == KidsSimpleMenuModule._AnimalChoiceMenuWindow && KidsAnimalChoiceMenuWindow.m_bOnline)
         && !RacerModule.m_bIsDemo;
   }

   // où l'on attend le lancement : menu principal en test automatique (nettest.scr), écran « En ligne » sinon
   static boolean AtLobbyPlace() {
      return m_bAuto ? AtMainMenu() : AtOnlineScreen();
   }

   static void LobbyFrame() {
      if (m_nRole == _ROLE_HOST) {
         ModNetPlayer me = m_Players[m_nMySlot];
         me.m_sName = LocalName(); // profil choisi après le démarrage
         me.m_nAnimal = m_nCfgAnimal;
         me.m_bAtMenu = AtLobbyPlace();
         me.m_bLobbyReady = true;
         if (IsRankedLobby()) {
            RankedHostFrame(); // [mod] phase 4 : partie rapide, lancée par l'hôte tout seul
            return;
         }

         if (!m_bAuto || m_bAutoDone || !AtMainMenu()) {
            return;
         }

         int nReady = 0;

         for (int i = 0; i < _MAX_PLAYERS; i++) {
            ModNetPlayer p = m_Players[i];
            if (p != null && i != m_nMySlot && !p.m_bGone && p.m_bAtMenu) {
               nReady++;
            }
         }

         if (nReady >= m_nWantPlayers - 1) {
            m_bAutoDone = true;
            Log("hote : tous au menu");
            HostLaunch();
         }
      } else {
         if (IsRankedLobby()) {
            RankedGuestFrame();
            if (m_nRole != _ROLE_GUEST) {
               return; // salon quitté
            }
         }

         if (!m_bLaunched || m_nRaceId == 0 || m_nRaceId == m_nLoadedRace || m_nMySlot == _NO_SLOT) {
            return;
         }

         if (AtLobbyPlace()) {
            Log("invite : l'hote lance la course " + m_nRaceId);
            LaunchRace();
         }
      }
   }

   // --- écran « En ligne » (KidsOnlineMenuWindow) ---
   static final int _LAUNCH_OK = 0;
   static final int _LAUNCH_ALONE = 1; // personne d'autre dans le salon
   static final int _LAUNCH_WAIT = 2; // un invité n'est pas prêt, ou pas encore revenu à l'écran
   static final int _LAUNCH_NOT_HOST = 3;

   static int CanLaunch() {
      if (m_nRole != _ROLE_HOST || m_nState != _ST_LOBBY) {
         return _LAUNCH_NOT_HOST;
      }

      int nOthers = 0;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && i != m_nMySlot && !p.m_bGone && !p.m_bAI) {
            nOthers++;
            if (!p.m_bAtMenu || !p.m_bLobbyReady) {
               return _LAUNCH_WAIT;
            }
         }
      }

      return nOthers == 0 ? _LAUNCH_ALONE : _LAUNCH_OK;
   }

   // l'hôte lance la course (bouton OK de l'écran, ou test automatique)
   static void HostLaunch() {
      m_nRaceId = (int)Now() & 0x7FFFFFFF | 1;
      AddAIPlayers(); // avant m_bLaunched : le même INFO donne la course et ses IA
      m_bLaunched = true;
      Log("hote : course " + m_nRaceId + " lancee");
      LaunchRace();
   }

   // --- [mod] IA de l'hôte (phase 3) ---
   // Les places libres du salon (au plus m_nAi) reçoivent une IA : un « joueur » de la session (m_bAI), sans adresse,
   // que le jeu de l'hôte fait rouler (VehicleAI, RaceParams.AddModAI) et dont il envoie l'état comme le sien. Chez les
   // invités, c'est un fantôme comme les autres ; leurs bonus et chocs sur elle partent vers l'hôte (RouteEvent).

   static int CfgAiMode() {
      if (m_nCfgAiMode < 0 || m_nCfgAiMode > ModOptions._AI_MAX) {
         m_nCfgAiMode = ModOptions.m_nAiMode; // la difficulté du menu Mods, au départ
      }

      return m_nCfgAiMode;
   }

   // humains de la session (moi compris)
   static int CountHumans() {
      int n = 0;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         if (m_Players[i] != null && !m_Players[i].m_bAI && !m_Players[i].m_bGone) {
            n++;
         }
      }

      return n;
   }

   static int CountAI() {
      int n = 0;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         if (m_Players[i] != null && m_Players[i].m_bAI) {
            n++;
         }
      }

      return n;
   }

   // place libre pour une IA : ni joueur de la session, ni membre du salon pas encore arrivé ici
   static boolean IsFreeForAI(int i) {
      return m_Players[i] == null && (ModLobby.m_Names == null || i >= ModLobby.m_Names.length || ModLobby.m_Names[i] == null);
   }

   // nombre d'IA réellement mises au lancement (écran « En ligne » : la valeur montrée à l'hôte)
   static int EffectiveAi() {
      return EffectiveAi(m_nCfgAi);
   }

   static int EffectiveAi(int nWish) {
      int nFree = 0;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         if (IsFreeForAI(i)) {
            nFree++;
         }
      }

      return Math.max(0, Math.min(nWish, nFree));
   }

   static void SetAi(int nCount, int nMode) {
      m_nCfgAi = Math.max(0, Math.min(_MAX_PLAYERS - 1, nCount));
      m_nCfgAiMode = Math.max(0, Math.min(ModOptions._AI_MAX, nMode));
      if (m_nRole == _ROLE_HOST && m_nState == _ST_LOBBY) {
         m_nAi = m_nCfgAi;
         m_nAiMode = m_nCfgAiMode;
      }
   }

   static void AddAIPlayers() {
      RemoveAIPlayers();
      boolean bRanked = IsRankedLobby(); // partie rapide : la grille complétée à 6, niveau de Data\online.scr
      m_nAiMode = bRanked ? ModOnline.m_nRankedAi : CfgAiMode();
      int nWant = EffectiveAi(bRanked ? _MAX_PLAYERS - 1 : m_nCfgAi);
      boolean[] bUsed = new boolean[KidsOnlineMenuWindow._ANIMALS];

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && p.m_nAnimal >= 0 && p.m_nAnimal < bUsed.length) {
            bUsed[p.m_nAnimal] = true;
         }
      }

      int n = 0;

      for (int i = 0; i < _MAX_PLAYERS && n < nWant; i++) {
         if (!IsFreeForAI(i)) {
            continue;
         }

         ModNetPlayer p = new ModNetPlayer(i);
         p.m_bAI = true;
         p.m_bAtMenu = true;
         p.m_bLobbyReady = true;
         p.m_nAnimal = RandomFreeAnimal(bUsed);
         bUsed[p.m_nAnimal] = true;
         p.m_sName = "IA";
         m_Players[i] = p;
         n++;
      }

      if (n > 0) {
         Log("hote : " + n + " IA en places libres, difficulte " + ModAI._MODE_NAMES[m_nAiMode]);
      }
   }

   // un animal qu'aucun autre n'a, au hasard (tous pris : n'importe lequel)
   static int RandomFreeAnimal(boolean[] bUsed) {
      int nFree = 0;

      for (int a = 0; a < bUsed.length; a++) {
         if (!bUsed[a]) {
            nFree++;
         }
      }

      if (nFree == 0) {
         return RacerGame.GetRandom(bUsed.length);
      }

      int k = RacerGame.GetRandom(nFree);

      for (int a = 0; a < bUsed.length; a++) {
         if (!bUsed[a] && k-- == 0) {
            return a;
         }
      }

      return 0;
   }

   static void RemoveAIPlayers() {
      for (int i = 0; i < _MAX_PLAYERS; i++) {
         if (m_Players[i] != null && m_Players[i].m_bAI) {
            m_Players[i] = null;
         }
      }
   }

   // l'animal de ce joueur (écran « En ligne »)
   static void SetMyAnimal(int nAnimal) {
      m_nCfgAnimal = nAnimal;
      if (m_nMySlot != _NO_SLOT && m_Players[m_nMySlot] != null && m_nRole == _ROLE_HOST) {
         m_Players[m_nMySlot].m_nAnimal = nAnimal;
      }
   }

   // l'hôte règle la course (écran « En ligne ») : redit aux invités dans chaque INFO
   static void SetRace(int nTrack, int nLaps, boolean bBonus) {
      m_nCfgTrack = nTrack;
      m_nCfgLaps = nLaps;
      m_bCfgBonus = bBonus;
      if (m_nRole == _ROLE_HOST && m_nState == _ST_LOBBY) {
         m_nTrack = nTrack;
         m_nLaps = nLaps;
         m_bBonus = bBonus;
      }
   }

   // Lance la course comme le bouton OK de l'écran des tours (KidsLapsMenuWindow), depuis le menu principal.
   // Animaux : le joueur local d'abord (RaceParams.AddPlayer), puis les autres slots dans l'ordre (fantômes).
   static void LaunchRace() {
      CoreLogic cl = RacerGame.GetCoreLogic();
      int n = 0;
      int[] aAnimals = new int[_MAX_PLAYERS];
      aAnimals[n++] = m_Players[m_nMySlot].m_nAnimal;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && i != m_nMySlot) {
            p.m_Ghost = null;
            p.m_AIVehicle = null;
            p.ResetRace();
            p.m_bReady = p.m_bAI; // une IA n'a rien à charger (HostCheckGo)
            aAnimals[n++] = p.m_nAnimal;
         }
      }

      m_Players[m_nMySlot].ResetRace();
      ResetEvents();
      cl.SetMultiplayer(false);
      cl.SetNextTrack(m_nTrack);
      cl.SetLapsNo(m_nLaps);
      cl.m_SelectedAnimals = IntList.MakeIntListFromIntArray(aAnimals, n);
      m_bRaceOnline = true;
      m_bRankedRace = IsRankedLobby();
      m_bRankedReported = false;
      KidsAnimalChoiceMenuWindow.m_bOnline = false;
      m_nLoadedRace = m_nRaceId;
      m_nState = _ST_LOADING;
      m_bLocalReady = false;
      m_bLightsStarted = false;
      m_bFinishCalled = false;
      m_lFirstFinish = 0L;
      m_lMyFinish = 0L;
      m_nSpecSlot = _NO_SLOT;
      m_lLaunchTime = Now();
      if (m_nRole == _ROLE_HOST) {
         m_bGoKnown = false;
      }

      Log("course " + m_nRaceId + " : circuit " + m_nTrack + " (" + RacerModule.GetLevelFileName(m_nTrack) + "), " + m_nLaps + " tours, " + (m_bBonus ? "avec" : "sans") + " bonus, " + n + " voitures dont " + CountAI() + " IA (" + ModAI._MODE_NAMES[m_nAiMode] + "), je suis le slot " + m_nMySlot);
      KidsSimpleMenuModule m = RacerGame.firstMenuModule;
      Window w = m.GetWindowAt(m.m_CurrentMenu);
      m.ClearStack();
      m.SetMenuCX(0);
      m.ShowBackground(false, true);
      if (w instanceof KidsMenuWindow) {
         ((KidsMenuWindow)w).DisactivateWindow();
      }

      if (w instanceof KidsMainMenuWindow) {
         // la démo du menu principal (90 s sans toucher au menu) partirait au retour, à la place du podium
         w.DisableCallMethod("RunDemo");
      }

      m.m_CurrentMenu = -1;
      RacerGame.game.RunModule();
   }

   static void CheckTimeouts(long lNow) {
      long lLimit = m_nState == _ST_RACE && m_bLightsStarted ? _TIMEOUT_RACE_MS : _TIMEOUT_WAIT_MS;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         // une IA se tait quand son hôte se tait : c'est l'hôte qu'on surveille (HostLeftRace)
         if (p != null && i != m_nMySlot && !p.m_bGone && !p.m_bAI && p.m_lLastHeard != 0L && lNow - p.m_lLastHeard > lLimit) {
            Log("joueur " + i + " (" + p.m_sName + ") muet depuis " + (lNow - p.m_lLastHeard) + " ms : parti");
            if (m_nRole == _ROLE_HOST && m_nState == _ST_LOBBY) {
               m_Players[i] = null; // entre deux courses : place libérée, il reviendra s'il parle encore
            } else {
               SetGone(p);
            }
         }
      }
   }

   static void SetGone(ModNetPlayer p) {
      p.m_bGone = true;
      Vehicle v = p.m_Ghost;
      if (v != null && !v.m_bModGhostGone) {
         v.m_bModGhostGone = true;
         v.ClearFlag(32);
         if (!v.m_bRaceFinished) {
            // voiture retirée de la course : invisible, muette, classée derrière (Vehicle.FillTimeList)
            v.EnableRendering(false);
            v.m_Character.EnableRendering(false);
            v.SetSoundVolume(v.m_iNofEngineSound, 0.0F);
         }
      }

      // l'hôte parti, l'invité ne reçoit plus personne (étoile)
      if (m_nRole == _ROLE_GUEST && p.m_nSlot == m_nHostSlot) {
         if (CanTakeOver()) {
            HostLeftRace(p); // [mod] par le serveur : un autre joueur prend son rôle, la course continue
            return;
         }

         for (int i = 0; i < _MAX_PLAYERS; i++) {
            if (m_Players[i] != null && i != m_nMySlot && !m_Players[i].m_bGone) {
               SetGone(m_Players[i]);
            }
         }
      }
   }

   // --- [mod] hôte remplacé en pleine course (phase 3) ---
   // Par le serveur seulement (chacun peut joindre chacun par sa place) et pendant une course (entre deux courses,
   // c'est le salon qui donne l'hôte : ModLobby.SyncNet). Le joueur restant de plus petite place prend le rôle :
   // tous font le même choix avec ce qu'ils savent (BYE de l'hôte, ou 5 s de silence). Il relaie voitures et
   // événements, répond aux JOIN et fait rouler les IA de l'ancien hôte à partir de leurs fantômes. Après la course,
   // ModLobby.SyncNet remet tout le monde d'accord avec le salon.
   static int m_nTakeovers = 0;

   // pas après le podium lancé (m_bFinishCalled) : tous les humains sont arrivés, l'hôte part vers son podium et se
   // tait, ce n'est pas un départ
   static boolean CanTakeOver() {
      return !m_bAuto && ModLobby.InRoom() && m_bRaceOnline && (m_nState == _ST_LOADING || m_nState == _ST_RACE) && m_nMySlot != _NO_SLOT
         && !m_bFinishCalled;
   }

   static void HostLeftRace(ModNetPlayer old) {
      int nNew = _NO_SLOT;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && i != old.m_nSlot && !p.m_bGone && !p.m_bAI) {
            nNew = i;
            break;
         }
      }

      if (nNew == _NO_SLOT) {
         return;
      }

      m_nTakeovers++;
      Log("l'hote (place " + old.m_nSlot + ", " + old.m_sName + ") a quitte la course : " + (nNew == m_nMySlot ? "je prends son role" : "la place " + nNew + " prend son role"));
      if (nNew == m_nMySlot) {
         BecomeRaceHost();
         return;
      }

      m_nHostSlot = nNew;
      m_HostAddr = new ModAddr(nNew);
      ModNetPlayer h = m_Players[nNew];
      h.ResetLink(); // nouveau lien d'événements ; ce que l'ancien hôte n'avait pas confirmé est perdu
      h.m_lLastHeard = Now();
      m_lNextJoin = 0L;
   }

   static void BecomeRaceHost() {
      long lNow = Now();
      m_nRole = _ROLE_HOST;
      m_nHostSlot = m_nMySlot;
      m_HostAddr = null;
      // l'écran « En ligne » montrera les réglages de cette course si je reste hôte du salon (pas ceux, imposés,
      // d'une partie rapide)
      if (!m_bRankedRace) {
         m_nCfgTrack = m_nTrack;
         m_nCfgLaps = m_nLaps;
         m_bCfgBonus = m_bBonus;
         m_nCfgAi = m_nAi;
         m_nCfgAiMode = m_nAiMode;
      }
      m_bLaunched = true;
      m_Players[m_nMySlot].m_bReady = m_bLocalReady;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p == null || i == m_nMySlot) {
            continue;
         }

         p.ResetLink();
         if (!p.m_bAI) {
            p.m_Addr = new ModAddr(i); // par le serveur
            p.m_nGuestId = 0; // inconnu ici : reconnu par sa place à son premier JOIN (HostOnJoin)
            p.m_lLastHeard = lNow;
            p.m_bReady = p.m_bReady || m_bGoKnown;
         }
      }

      TakeOverAI();
   }

   // les fantômes des IA deviennent des IA de ce jeu (pas encore créés : CreateGhosts le fera, je suis l'hôte)
   static void TakeOverAI() {
      RacerModule mod = RacerModule.m_Mod;
      int n = 0;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p == null || !p.m_bAI || p.m_Ghost == null || mod == null || mod.m_RaceParams == null) {
            continue;
         }

         Vehicle v = p.m_Ghost;
         p.m_Ghost = null;
         p.m_AIVehicle = v;
         if (v.m_bRaceFinished) {
            continue; // arrivée : elle reste où elle est, son temps est connu
         }

         mod.m_RaceParams.ModGhostToAI(v);
         n++;
      }

      if (n > 0) {
         Log("je reprends " + n + " IA (" + ModAI._MODE_NAMES[m_nAiMode] + ")");
      }
   }

   // --- réception (ModOnline.Frame) ---

   static void Receive(ModPacket pk) {
      m_In.Wrap(pk.m_Data, pk.m_nLen);
      m_In.GetInt(); // magique, déjà vérifié
      int nType = m_In.GetByte();
      if (nType == _T_JOIN && m_nRole == _ROLE_HOST) {
         HostOnJoin(pk);
      } else if (nType == _T_INFO && m_nRole == _ROLE_GUEST) {
         GuestOnInfo(pk);
      } else if (nType == _T_CAR) {
         OnCar(pk);
      } else if (nType == _T_EVT) {
         OnEvents(pk);
      } else if (nType == _T_BYE) {
         int nSlot = m_In.GetByte();
         if (!m_In.m_bBad && nSlot < _MAX_PLAYERS && m_Players[nSlot] != null && nSlot != m_nMySlot && !m_Players[nSlot].m_bGone) {
            Log("joueur " + nSlot + " (" + m_Players[nSlot].m_sName + ") quitte");
            SetGone(m_Players[nSlot]);
            if (m_nRole == _ROLE_HOST) {
               Forward(pk, nSlot);
            }
         }
      }
   }

   static void BeginPacket(int nType) {
      m_Out.Clear();
      m_Out.PutInt(_MAGIC);
      m_Out.PutByte(nType);
   }

   static void SendJoin() {
      ModNetPlayer me = m_nMySlot != _NO_SLOT ? m_Players[m_nMySlot] : null;
      BeginPacket(_T_JOIN);
      m_Out.PutInt(m_nGuestId);
      m_Out.PutShort(_VERSION);
      m_Out.PutByte(m_nCfgAnimal);
      m_Out.PutString(LocalName());
      m_Out.PutByte(AtLobbyPlace() ? 1 : 0);
      m_Out.PutInt(m_bLocalReady ? m_nLoadedRace : 0);
      m_Out.PutInt((int)Now());
      m_Out.PutByte(me != null && me.m_bFinished ? 1 : 0);
      m_Out.PutByte(m_bAuto || m_bWantReady ? 1 : 0); // [mod] v5 : prêt (écran « En ligne »)
      m_Udp.Send(m_HostAddr, m_Out);
   }

   static void HostOnJoin(ModPacket pk) {
      int nGuestId = m_In.GetInt();
      int nVersion = m_In.GetShort();
      int nAnimal = m_In.GetByte();
      String sName = m_In.GetString();
      boolean bAtMenu = m_In.GetByte() != 0;
      int nReadyRace = m_In.GetInt();
      int nSyncT = m_In.GetInt();
      m_In.GetByte(); // arrivé (lu dans les états de course)
      boolean bLobbyReady = m_In.GetByte() != 0;
      if (m_In.m_bBad) {
         return;
      }

      if (nVersion != _VERSION) {
         Log("hote : invite " + pk.m_From + " en version " + nVersion + " (moi " + _VERSION + ") : refuse");
         SendInfo(pk.m_From, _NO_SLOT, nSyncT);
         return;
      }

      ModNetPlayer p = null;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         if (m_Players[i] != null && i != m_nMySlot && m_Players[i].m_nGuestId == nGuestId) {
            p = m_Players[i];
            break;
         }
      }

      // [mod] hôte pris en pleine course : je ne connais pas encore son identifiant, je le reconnais à sa place
      int nFromSlot = pk.m_From.m_nSlot;
      if (p == null && nFromSlot >= 0 && nFromSlot < _MAX_PLAYERS && nFromSlot != m_nMySlot && m_Players[nFromSlot] != null
         && !m_Players[nFromSlot].m_bAI && m_Players[nFromSlot].m_nGuestId == 0) {
         p = m_Players[nFromSlot];
         p.m_nGuestId = nGuestId;
      }

      if (p == null) {
         // nouvel invité : seulement avant le lancement d'une course
         int nSlot = _NO_SLOT;
         int nWanted = pk.m_From.m_nSlot; // par le serveur : même place que dans le salon si elle est libre
         if (m_nState == _ST_LOBBY && nWanted >= 0 && nWanted < _MAX_PLAYERS && nWanted != m_nMySlot && m_Players[nWanted] == null) {
            nSlot = nWanted;
         } else if (m_nState == _ST_LOBBY && nWanted < 0) {
            for (int i = 0; i < _MAX_PLAYERS; i++) {
               if (m_Players[i] == null && i != m_nMySlot) {
                  nSlot = i;
                  break;
               }
            }
         }

         if (nSlot == _NO_SLOT) {
            SendInfo(pk.m_From, _NO_SLOT, nSyncT);
            return;
         }

         p = new ModNetPlayer(nSlot);
         p.m_nGuestId = nGuestId;
         p.m_Addr = pk.m_From;
         m_Players[nSlot] = p;
         Log("hote : " + sName + " (" + pk.m_From + ", animal " + nAnimal + ") rejoint en slot " + nSlot);
      }

      if (p.m_bGone) {
         // parti de cette course (BYE, silence) : il revient à la suivante. En attendant, réponse quand même, pour
         // qu'il ne nous croie pas partis pendant que la course se finit ici
         SendInfo(pk.m_From, p.m_nSlot, nSyncT);
         return;
      }

      p.m_Addr = pk.m_From;
      p.m_lLastHeard = pk.m_lTime;
      p.m_nAnimal = nAnimal;
      p.m_sName = sName;
      p.m_bAtMenu = bAtMenu;
      p.m_bLobbyReady = bLobbyReady;
      if (nReadyRace != 0 && nReadyRace == m_nRaceId && !p.m_bReady) {
         p.m_bReady = true;
         Log("hote : " + sName + " a charge la course");
      }

      SendInfo(pk.m_From, p.m_nSlot, nSyncT);
   }

   static void SendInfo(ModAddr to, int nSlot, int nSyncT) {
      BeginPacket(_T_INFO);
      m_Out.PutShort(_VERSION);
      m_Out.PutByte(nSlot);
      m_Out.PutInt(nSyncT);
      m_Out.PutInt(HostNow());
      m_Out.PutInt(m_nRaceId);
      m_Out.PutByte(m_bLaunched ? 1 : 0);
      m_Out.PutByte(m_nTrack);
      m_Out.PutByte(m_nLaps);
      m_Out.PutByte(m_bBonus ? 1 : 0);
      m_Out.PutByte(m_nAi); // v6
      m_Out.PutByte(m_nAiMode); // v6
      m_Out.PutByte(m_bGoKnown ? 1 : 0);
      m_Out.PutInt(m_nGoTime);
      int nCount = 0;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         if (m_Players[i] != null) {
            nCount++;
         }
      }

      m_Out.PutByte(nCount);

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null) {
            m_Out.PutByte(i);
            m_Out.PutByte(p.m_nAnimal);
            m_Out.PutString(p.m_sName);
            // v5 : 1 parti, 2 prêt, 4 à l'écran « En ligne » (montrés aux invités) ; v6 : 8 IA de l'hôte
            m_Out.PutByte((p.m_bGone ? 1 : 0) | (p.m_bLobbyReady ? 2 : 0) | (p.m_bAtMenu ? 4 : 0) | (p.m_bAI ? 8 : 0));
         }
      }

      m_Udp.Send(to, m_Out);
   }

   static void GuestOnInfo(ModPacket pk) {
      if (!pk.m_From.equals(m_HostAddr)) {
         return;
      }

      int nVersion = m_In.GetShort();
      int nSlot = m_In.GetByte();
      int nSyncT = m_In.GetInt();
      int nHostTime = m_In.GetInt();
      int nRaceId = m_In.GetInt();
      boolean bLaunched = m_In.GetByte() != 0;
      int nTrack = m_In.GetByte();
      int nLaps = m_In.GetByte();
      boolean bBonus = m_In.GetByte() != 0;
      int nAi = m_In.GetByte();
      int nAiMode = m_In.GetByte();
      boolean bGoKnown = m_In.GetByte() != 0;
      int nGoTime = m_In.GetInt();
      int nCount = m_In.GetByte();
      if (m_In.m_bBad) {
         return;
      }

      if (nSlot == _NO_SLOT || nVersion != _VERSION) {
         if (m_nMySlot == _NO_SLOT && pk.m_lTime >= m_lNextRefusedLog) {
            m_lNextRefusedLog = pk.m_lTime + 5000L;
            Log("invite : refuse par l'hote (complet, course en cours ou version " + nVersion + ")");
         }

         return;
      }

      // horloge : on garde l'échantillon à l'aller-retour le plus court ; il vieillit d'1 ms par échantillon pour
      // qu'une dérive finisse par être corrigée
      int nRtt = (int)pk.m_lTime - nSyncT;
      if (nRtt >= 0 && nRtt < 2000) {
         if (m_nBestRtt != Integer.MAX_VALUE) {
            m_nBestRtt++;
         }

         if (nRtt <= m_nBestRtt) {
            m_nBestRtt = nRtt;
            m_lClockOffset = (long)nHostTime + nRtt / 2 - pk.m_lTime;
         }
      }

      if (m_nMySlot == _NO_SLOT) {
         Log("invite : accepte en slot " + nSlot + " (aller-retour " + nRtt + " ms)");
      }

      m_nMySlot = nSlot;
      boolean[] bSeen = new boolean[_MAX_PLAYERS];

      for (int k = 0; k < nCount; k++) {
         int i = m_In.GetByte();
         int nAnimal = m_In.GetByte();
         String sName = m_In.GetString();
         int nFlags = m_In.GetByte();
         boolean bGone = (nFlags & 1) != 0;
         if (m_In.m_bBad || i >= _MAX_PLAYERS) {
            return;
         }

         bSeen[i] = true;
         ModNetPlayer p = m_Players[i];
         boolean bAI = (nFlags & 8) != 0;
         if (p == null || p.m_bAI != bAI && m_nState == _ST_LOBBY) {
            p = new ModNetPlayer(i);
            m_Players[i] = p;
         }

         p.m_bAI = bAI;
         p.m_nAnimal = nAnimal;
         p.m_sName = sName;
         p.m_bLobbyReady = (nFlags & 2) != 0;
         p.m_bAtMenu = (nFlags & 4) != 0;
         if (bGone && !p.m_bGone && i != m_nMySlot) {
            SetGone(p);
         } else if (!bGone && p.m_bGone && m_nState == _ST_LOBBY) {
            p.m_bGone = false; // revenu pour la course suivante
         }

         if (i == m_nHostSlot) {
            p.m_lLastHeard = pk.m_lTime;
         }
      }

      // avant le lancement, la liste de l'hôte fait foi (un invité parti libère sa place)
      if (m_nState == _ST_LOBBY) {
         for (int i = 0; i < _MAX_PLAYERS; i++) {
            if (!bSeen[i]) {
               m_Players[i] = null;
            }
         }
      }

      m_nTrack = nTrack;
      m_nLaps = nLaps;
      m_bBonus = bBonus;
      m_nAi = nAi;
      m_nAiMode = nAiMode <= ModOptions._AI_MAX ? nAiMode : ModOptions._AI_AUTO;
      if (nRaceId != m_nRaceId) {
         m_nRaceId = nRaceId;
         m_bGoKnown = false;
      }

      m_bLaunched = bLaunched;
      if (bGoKnown && !m_bGoKnown && nRaceId == m_nLoadedRace) {
         m_bGoKnown = true;
         m_nGoTime = nGoTime;
         Log("invite : top depart dans " + (nGoTime - HostNow()) + " ms");
      }
   }

   static long m_lNextRefusedLog = 0L;

   // l'hôte renvoie la voiture d'un invité aux autres invités
   static void Forward(ModPacket pk, int nFromSlot) {
      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && i != nFromSlot && i != m_nMySlot && !p.m_bGone && p.m_Addr != null) {
            m_Udp.Send(p.m_Addr, pk.m_Data, pk.m_nLen);
         }
      }
   }

   static void OnCar(ModPacket pk) {
      int nSlot = m_In.GetByte();
      int nRace = m_In.GetInt();
      if (m_In.m_bBad || nSlot >= _MAX_PLAYERS || nSlot == m_nMySlot) {
         return;
      }

      ModNetPlayer p = m_Players[nSlot];
      if (p == null || p.m_bGone) {
         return;
      }

      if (m_nRole == _ROLE_HOST) {
         if (!pk.m_From.equals(p.m_Addr)) {
            return;
         }

         p.m_lLastHeard = pk.m_lTime;
         Forward(pk, nSlot);
      } else if (!pk.m_From.equals(m_HostAddr)) {
         return;
      } else {
         p.m_lLastHeard = pk.m_lTime;
         m_Players[m_nHostSlot].m_lLastHeard = pk.m_lTime;
      }

      if (nRace != m_nLoadedRace || m_nState != _ST_RACE) {
         return;
      }

      ModNetSnap s = p.NextSnap();
      if (!s.Read(m_In) || m_In.m_bBad) {
         p.DropSnap();
         return;
      }

      m_nCarsRecv++;
      p.OnSnap(s, HostNow());
      // l'arrivée compte tout de suite (le classement se fait sur les temps, pas sur l'ordre de réception)
      if ((s.m_nFlags & _F_FINISHED) != 0 && !p.m_bFinished) {
         p.m_bFinished = true;
         p.m_fFinishTime = s.m_fFinishTime;
         Vehicle v = p.m_Ghost;
         if (v != null && !v.m_bRaceFinished) {
            v.m_bRaceFinished = true;
            v.m_fFinishClockRaceTime = s.m_fFinishTime;
            v.m_iCurrLoop = m_nLaps;
            OnSomeoneFinished();
            Log("joueur " + nSlot + " (" + p.m_sName + ") arrive en " + s.m_fFinishTime + " s");
         }
      }
   }

   // --- course (RacerModule) ---

   static boolean IsRaceOnline() {
      return m_bRaceOnline && !RacerModule.m_bIsDemo;
   }

   // RacerModule.InitModule : les animaux 1..n-1 de CoreLogic sont les autres slots, dans l'ordre
   static void CreateGhosts(RaceParams rp, IntList animals) {
      int k = 1;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && i != m_nMySlot && k < animals.m_Cnt) {
            if (p.m_bAI && m_nRole == _ROLE_HOST) {
               // [mod] IA de l'hôte : une vraie IA ici (m_AITab), un fantôme chez les autres
               p.m_AIVehicle = rp.AddModAI(animals.GetAt(k++));
               p.m_AIVehicle.m_iModNetSlot = i;
               Log("IA " + i + " : bloquante " + p.m_AIVehicle.IsSetFlag(32) + ", rayon " + p.CarRadius());
               continue;
            }

            p.m_Ghost = rp.AddGhost(animals.GetAt(k++));
            p.m_Ghost.m_iModNetSlot = i;
            if (p.m_bGone) {
               SetGone(p);
            }
         }
      }

      Vehicle me = (Vehicle)rp.m_PlayersTab.elementAt(0);
      me.m_iModNetSlot = m_nMySlot;
      Vector e = me.GetEllipsoid();
      Log("chocs : ma voiture bloquante " + me.IsSetFlag(32) + ", ellipsoide " + e.fX + " x " + e.fY + " x " + e.fZ);
   }

   // RaceParams.SetStartPos : place sur la grille = slot (même grille dans tous les jeux)
   static Vehicle[] GridOrder(RaceParams rp) {
      Vehicle[] a = new Vehicle[_MAX_PLAYERS];
      int n = 0;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         if (i == m_nMySlot) {
            a[n++] = (Vehicle)rp.m_PlayersTab.elementAt(0);
         } else if (m_Players[i] != null && m_Players[i].Car() != null) {
            a[n++] = m_Players[i].Car();
         }
      }

      Vehicle[] r = new Vehicle[n];
      System.arraycopy(a, 0, r, 0, n);
      return r;
   }

   // RacerModule.OnActivate : circuit chargé et affiché, on attend le top départ
   static void OnRaceActivated() {
      m_nState = _ST_RACE;
      m_bLocalReady = true;
      m_lNextCar = 0L;
      m_nCarsSent = 0;
      m_nCarsRecv = 0;
      if (m_nRole == _ROLE_HOST) {
         m_Players[m_nMySlot].m_bReady = true;
      }

      Log("course chargee en " + (Now() - m_lLaunchTime) + " ms (" + RacerModule.m_Mod.m_RaceParams.m_iNofLoops + " tours), en attente du top depart");
   }

   static Vehicle LocalVehicle() {
      RacerModule mod = RacerModule.m_Mod;
      return mod != null && mod.m_RaceParams != null && mod.m_RaceParams.m_PlayersTab.size() > 0 ? (Vehicle)mod.m_RaceParams.m_PlayersTab.elementAt(0) : null;
   }

   // RacerModule.OnFrame, avant la physique
   static void RaceFrame(RacerModule mod) {
      if (m_nState != _ST_RACE) {
         return;
      }

      long lNow = Now();
      if (m_nRole == _ROLE_HOST && !m_bGoKnown) {
         HostCheckGo(lNow);
      }

      if (m_bGoKnown && !m_bLightsStarted && HostNow() - m_nGoTime >= 0) {
         m_bLightsStarted = true;
         Log("feux allumes avec " + (HostNow() - m_nGoTime) + " ms de retard sur le top (vert dans 6 s)");
         mod.StartRace();
      }

      Vehicle me = LocalVehicle();
      if (me != null && lNow >= m_lNextCar) {
         m_lNextCar = lNow + _CAR_MS;
         SendCar(me, m_nMySlot);
         if (m_nRole == _ROLE_HOST) {
            // [mod] mes IA : leur état part comme le mien
            for (int i = 0; i < _MAX_PLAYERS; i++) {
               ModNetPlayer p = m_Players[i];
               if (p != null && p.m_bAI && p.m_AIVehicle != null) {
                  SendCar(p.m_AIVehicle, i);
               }
            }
         }

         FlushBumps();
         SendEvents();
      }

      int nHostNow = HostNow();

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && p.m_Ghost != null && !p.m_bGone) {
            p.Apply(nHostNow, mod, me);
         }
      }

      HostAIContacts(mod); // [mod] mes IA contre les fantômes des invités

      if (me != null && me.m_bRaceFinished && !m_bFinishCalled) {
         boolean bAll = true;

         for (int i = 0; i < _MAX_PLAYERS; i++) {
            ModNetPlayer p = m_Players[i];
            if (p != null && i != m_nMySlot && !p.m_bGone && !p.m_bFinished && !p.m_bAI) {
               bAll = false;
            }
         }

         // tous les humains arrivés (ou partis : silence de 5 s, BYE) ; en attendant, spectateur. Les IA pas encore
         // arrivées sont classées à la distance, comme hors ligne (Vehicle.FillTimeList)
         if (bAll) {
            m_bFinishCalled = true;
            Log("podium (tous arrives)");
            ReportRanked(false); // [mod] phase 4 : partie rapide, ordre d'arrivée des humains au serveur
            SpectateStop(mod, me);
            me.FinishRace();
         } else {
            SpectateFrame(mod, lNow);
         }
      }
   }

   static void HostCheckGo(long lNow) {
      boolean bAll = true;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && !p.m_bGone && !p.m_bReady) {
            bAll = false;
         }
      }

      if (bAll || lNow - m_lLaunchTime > _READY_WAIT_MS) {
         m_bGoKnown = true;
         m_nGoTime = HostNow() + _GO_DELAY_MS;
         Log("hote : " + (bAll ? "tous charges" : "attente du chargement depassee") + ", top depart dans " + _GO_DELAY_MS + " ms");
      }
   }

   // état d'une voiture de ce jeu : la mienne, ou une IA (hôte)
   static void SendCar(Vehicle me, int nSlot) {
      BeginPacket(_T_CAR);
      m_Out.PutByte(nSlot);
      m_Out.PutInt(m_nLoadedRace);
      m_Out.PutInt(HostNow());
      me.GetPosition(m_vTmp);
      PutVector(m_vTmp);
      me.GetForwardVector(m_vTmp);
      PutVector(m_vTmp);
      me.GetUpVector(m_vTmp);
      PutVector(m_vTmp);
      m_Out.PutFloat(me.m_fCurrVehicleHAngle);
      m_Out.PutFloat(me.m_fCurrVehicleVAngle);
      m_Out.PutFloat(me.m_fCurrSpeed);
      m_Out.PutFloat(me.m_fCurrDirAngle);
      m_Out.PutByte(me.m_iCurrLoop + 1);
      m_Out.PutShort(me.m_iCurrTrackPoint);
      int nFlags = 0;
      if (me.m_bRaceFinished) {
         nFlags |= _F_FINISHED;
      }

      if (me.m_bTurbo) {
         nFlags |= _F_TURBO;
      }

      if (me.m_bTireSpikes) {
         nFlags |= _F_SPIKES;
      }

      if (me.m_bProtection) {
         nFlags |= _F_PROTECTION;
      }

      if (!me.IsSetFlag(32)) {
         nFlags |= _F_NOBLOCK;
      }

      m_Out.PutByte(nFlags);
      m_Out.PutFloat(me.m_fFinishClockRaceTime);
      if (m_nRole == _ROLE_HOST) {
         for (int i = 0; i < _MAX_PLAYERS; i++) {
            ModNetPlayer p = m_Players[i];
            if (p != null && i != m_nMySlot && !p.m_bGone && p.m_Addr != null) {
               m_Udp.Send(p.m_Addr, m_Out);
            }
         }
      } else {
         m_Udp.Send(m_HostAddr, m_Out);
      }

      m_nCarsSent++;
   }

   static final Vector m_vTmp = new Vector();

   static void PutVector(Vector v) {
      m_Out.PutFloat(v.fX);
      m_Out.PutFloat(v.fY);
      m_Out.PutFloat(v.fZ);
   }

   // Vehicle.UpdateCurrentGate, quand la voiture locale passe la ligne pour la dernière fois
   static void OnLocalFinished(Vehicle me) {
      if (m_nMySlot != _NO_SLOT && m_Players[m_nMySlot] != null) {
         m_Players[m_nMySlot].m_bFinished = true;
      }

      Log("arrivee en " + me.m_fFinishClockRaceTime + " s");
      m_lMyFinish = Now();
      OnSomeoneFinished();
   }

   // [mod] Vehicle.UpdateCurrentGate : une IA de ce jeu (hôte) passe la ligne ; les autres le voient dans son état
   static void OnAIFinished(Vehicle v) {
      int nSlot = v.m_iModNetSlot;
      if (nSlot >= 0 && nSlot < _MAX_PLAYERS && m_Players[nSlot] != null) {
         m_Players[nSlot].m_bFinished = true;
         m_Players[nSlot].m_fFinishTime = v.m_fFinishClockRaceTime;
      }

      Log("IA " + nSlot + " (" + Rocket.ModName(v) + ") arrive en " + v.m_fFinishClockRaceTime + " s");
      OnSomeoneFinished();
   }

   // --- spectateur : arrivé, on regarde les autres en attendant le podium ---
   static final long _SPEC_DELAY_MS = 3000L; // passage de la ligne vu de sa voiture, avant de regarder ailleurs
   static long m_lMyFinish = 0L;
   static int m_nSpecSlot = _NO_SLOT; // joueur regardé (_NO_SLOT : ma voiture)
   static long m_lSpecFinished = 0L; // heure où le joueur regardé a fini

   static boolean IsWaitingOthers() {
      Vehicle me = m_nState == _ST_RACE && IsRaceOnline() ? LocalVehicle() : null;
      return me != null && me.m_bRaceFinished && !m_bFinishCalled;
   }

   // le joueur suivant ou précédent à regarder (bRacing : seulement ceux qui roulent encore)
   static int NextSpecSlot(int nFrom, int nDir, boolean bRacing) {
      for (int k = 1; k <= _MAX_PLAYERS; k++) {
         int i = ((nFrom == _NO_SLOT ? (nDir > 0 ? _MAX_PLAYERS - 1 : 0) : nFrom) + nDir * k + 2 * _MAX_PLAYERS) % _MAX_PLAYERS;
         ModNetPlayer p = m_Players[i];
         // de lui-même, le spectateur suit les humains ; les flèches passent aussi par les IA
         if (i != m_nMySlot && p != null && p.Car() != null && !p.m_bGone && (!bRacing || !p.m_bFinished && !p.m_bAI)) {
            return i;
         }
      }

      return _NO_SLOT;
   }

   static void Spectate(RacerModule mod, int nSlot) {
      RacerCamera cam = mod.GetRacerCamera();
      if (cam == null || nSlot == m_nSpecSlot) {
         return;
      }

      cam.SetObserved(m_Players[nSlot].Car());
      m_nSpecSlot = nSlot;
      m_lSpecFinished = 0L;
      Log("spectateur : je regarde le joueur " + nSlot + " (" + m_Players[nSlot].m_sName + ")");
   }

   static void SpectateStop(RacerModule mod, Vehicle me) {
      RacerCamera cam = mod.GetRacerCamera();
      if (m_nSpecSlot != _NO_SLOT && cam != null) {
         cam.SetObserved(me);
      }

      m_nSpecSlot = _NO_SLOT;
   }

   // après 3 s, la caméra suit un joueur qui roule encore ; 3 s après son arrivée, le suivant
   static void SpectateFrame(RacerModule mod, long lNow) {
      if (lNow - m_lMyFinish < _SPEC_DELAY_MS) {
         return;
      }

      ModNetPlayer cur = m_nSpecSlot != _NO_SLOT ? m_Players[m_nSpecSlot] : null;
      if (cur != null && cur.m_bFinished && m_lSpecFinished == 0L) {
         m_lSpecFinished = lNow;
      }

      if (cur == null || cur.m_bGone || cur.Car() == null || cur.m_bFinished && lNow - m_lSpecFinished > _SPEC_DELAY_MS) {
         int n = NextSpecSlot(m_nSpecSlot, 1, true);
         if (n != _NO_SLOT) {
            Spectate(mod, n);
         }
      }
   }

   // RacerGame.OnInputKey : gauche / droite changent de joueur regardé
   static void OnKey(int nKey, boolean bDown) {
      if (!bDown || !IsWaitingOthers()) {
         return;
      }

      RacerModule mod = RacerModule.m_Mod;
      if (nKey == EKey._LEFT || nKey == EKey._RIGHT) {
         int n = NextSpecSlot(m_nSpecSlot, nKey == EKey._RIGHT ? 1 : -1, false);
         if (n != _NO_SLOT) {
            m_lMyFinish = 0L; // choix du joueur : plus d'attente de 3 s
            Spectate(mod, n);
         }
      }
   }

   // KidsHud : ligne du bas pendant l'attente (null : rien)
   static String HudText() {
      if (!IsWaitingOthers()) {
         return null;
      }

      int nLeft = 0;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && i != m_nMySlot && !p.m_bGone && !p.m_bFinished && !p.m_bAI) {
            nLeft++;
         }
      }

      String s = m_nSpecSlot != _NO_SLOT ? "Tu regardes " + m_Players[m_nSpecSlot].m_sName : nLeft + " joueur(s) encore en course";
      return s + "   -   gauche / droite : changer de joueur";
   }

   // Vehicle.FinishRace : hors ligne, le podium arrive 16 s après la ligne ; en ligne, ce temps court depuis ma
   // propre arrivée (l'attente des autres en a déjà mangé une partie), 4 s au moins
   static float StopTimeAfterRace(float fDefault) {
      if (!IsRaceOnline() || m_lMyFinish == 0L) {
         return fDefault;
      }

      return Math.max(4.0F, fDefault - (Now() - m_lMyFinish) / 1000.0F);
   }

   static void OnSomeoneFinished() {
      if (m_lFirstFinish == 0L) {
         m_lFirstFinish = Now();
      }

      RerankFinished();
   }

   // Rang d'arrivée d'après les temps (chaque jeu mesure depuis son feu vert, réglé sur le même top) et non d'après
   // l'ordre où les arrivées sont connues ici.
   static void RerankFinished() {
      RacerModule mod = RacerModule.m_Mod;
      if (mod == null || mod.m_RaceParams == null) {
         return;
      }

      RaceParams rp = mod.m_RaceParams;
      java.util.Vector sort = rp.m_SortTable;
      Vehicle[] fin = new Vehicle[sort.size()];
      int n = 0;

      for (int i = 0; i < sort.size(); i++) {
         Vehicle v = (Vehicle)sort.elementAt(i);
         if (v.m_bRaceFinished) {
            int k = n++;

            while (k > 0 && fin[k - 1].m_fFinishClockRaceTime > v.m_fFinishClockRaceTime) {
               fin[k] = fin[k - 1];
               k--;
            }

            fin[k] = v;
         }
      }

      java.util.Vector rest = new java.util.Vector();

      for (int i = 0; i < sort.size(); i++) {
         Vehicle v = (Vehicle)sort.elementAt(i);
         if (!v.m_bRaceFinished) {
            rest.addElement(v);
         }
      }

      sort.removeAllElements();

      for (int i = 0; i < n; i++) {
         fin[i].m_iFinishRank = i;
         sort.addElement(fin[i]);
      }

      for (int i = 0; i < rest.size(); i++) {
         sort.addElement(rest.elementAt(i));
      }

      for (int i = 0; i < sort.size(); i++) {
         ((Vehicle)sort.elementAt(i)).m_iCurrPlace = i;
      }

      rp.m_nFinished = n;
   }

   // ------------------------------------------------------------------ bonus en ligne (phase 2)
   // Événements fiables, par lien (invité <-> hôte) : chaque lien numérote ce qu'il envoie, l'autre bout applique dans
   // l'ordre et renvoie le dernier numéro appliqué ; tout ce qui n'est pas confirmé est redit avec chaque état (25/s).
   // L'hôte relaie aux autres invités ce qui ne lui est pas destiné.
   // Contenu d'un événement : genre, slot d'origine, slot visé (255 = tous), puis :
   //   HIT  effet                       un fantôme a été touché chez un autre : l'effet s'applique ici
   //   TRAP n°, type, position          piège posé (banane, huile, bouteille, enclume, catapulte, gaz) : copie ici
   //   GONE n°                          piège déclenché quelque part : retiré ici
   //   BOOM genre, position             explosion (bombe, pastèque) : on la montre
   //   BUMP poussée (x, y, z), vitesse   ma voiture a heurté ton fantôme : ta part du choc (Vehicle.OnCollisionFullInfoMove)

   static final int _T_EVT = 5;
   static final int _EV_HIT = 1;
   static final int _EV_TRAP = 2;
   static final int _EV_GONE = 3;
   static final int _EV_BOOM = 4;
   static final int _EV_BUMP = 5;
   static final int _EV_PICK = 6; // boîte bonus ramassée ici : prise aussi chez les autres
   static final int _ALL = 255;
   static final int _EVT_BUDGET = 1200; // octets d'événements par paquet

   static final int _HIT_STARS = 1; // bouchon, noix de coco, boule de neige
   static final int _HIT_WASPS = 2;
   static final int _HIT_TOMATO = 3; // tomate, cookie
   static final int _HIT_EXPLOSION = 4; // bombe, pastèque, fusée, missile
   static final int _HIT_FREEZE = 5;
   static final int _HIT_WATER = 6;
   static final int _HIT_ROCK = 7;
   static final int _HIT_MOLE = 8;
   static final int _HIT_UFO = 9;
   static final String[] _HIT_NAMES = new String[]{"?", "etoiles", "guepes", "tomate", "explosion", "glace", "eau", "rocher", "taupe", "ovni"};

   static final int _BOOM_BOMB = 1;
   static final int _BOOM_WATERMELON = 2;

   // un même fantôme touché plusieurs fois de suite par la même chose (la bombe teste le rayon à chaque image) :
   // un seul HIT par seconde et par effet
   static final long _HIT_REPEAT_MS = 1000L;
   static long[] m_aLastHit = new long[_MAX_PLAYERS * 16];

   // pièges posés ici ou recopiés : n° -> ModNetTrap
   static java.util.Hashtable m_Traps = new java.util.Hashtable();
   static int m_nTrapSerial = 0;
   // au-delà, le piège a pu disparaître de lui-même (durée de vie 60 s, gaz 15 s) : on n'y touche plus
   static final long _TRAP_SAFE_MS = 50000L;
   static final long _GAS_SAFE_MS = 12000L;
   static int m_nEventsSent = 0;
   static int m_nEventsRecv = 0;

   static boolean EventsOn() {
      return IsRaceOnline() && m_nState == _ST_RACE && m_nMySlot != _NO_SLOT;
   }

   // lien vers le slot visé : l'invité passe toujours par l'hôte
   static ModNetPlayer LinkTo(int nSlot) {
      if (m_nRole == _ROLE_GUEST) {
         return m_Players[m_nHostSlot];
      }

      return nSlot >= 0 && nSlot < _MAX_PLAYERS ? m_Players[nSlot] : null;
   }

   // nouvel événement d'ici (origine = mon slot)
   static void Emit(byte[] ev, int nTarget) {
      if (nTarget == _ALL) {
         if (m_nRole == _ROLE_GUEST) {
            Queue(m_Players[m_nHostSlot], ev);
         } else {
            for (int i = 0; i < _MAX_PLAYERS; i++) {
               Queue(m_Players[i], ev); // sauf moi (Queue)
            }
         }
      } else {
         Queue(LinkTo(nTarget), ev);
      }

      m_nEventsSent++;
   }

   static void Queue(ModNetPlayer link, byte[] ev) {
      // une IA n'est pas un lien : ce qui la vise est appliqué par l'hôte (RouteEvent)
      if (link != null && !link.m_bGone && !link.m_bAI && link.m_nSlot != m_nMySlot) {
         link.m_EvOut.addElement(ev);
      }
   }

   // [mod] voiture que ce jeu fait rouler pour cette place : la mienne, ou une IA (hôte)
   static Vehicle LocalCar(int nSlot) {
      if (nSlot == m_nMySlot) {
         return LocalVehicle();
      }

      if (m_nRole == _ROLE_HOST && nSlot >= 0 && nSlot < _MAX_PLAYERS && m_Players[nSlot] != null && m_Players[nSlot].m_bAI) {
         Vehicle v = m_Players[nSlot].m_AIVehicle;
         return v != null && !v.m_bModGhost ? v : null;
      }

      return null;
   }

   static ModBuf m_Ev = new ModBuf(256);

   static void BeginEvent(int nKind, int nTarget) {
      BeginEvent(nKind, m_nMySlot, nTarget);
   }

   static void BeginEvent(int nKind, int nOrigin, int nTarget) {
      m_Ev.Clear();
      m_Ev.PutByte(nKind);
      m_Ev.PutByte(nOrigin);
      m_Ev.PutByte(nTarget);
   }

   static byte[] EndEvent() {
      byte[] b = new byte[m_Ev.Length()];
      System.arraycopy(m_Ev.m_Data, 0, b, 0, b.length);
      return b;
   }

   static void PutVec(ModBuf b, Vector v) {
      b.PutFloat(v.fX);
      b.PutFloat(v.fY);
      b.PutFloat(v.fZ);
   }

   // Vehicle (StopAndStars, ExplosionEffect…) : un fantôme vient d'être touché ici
   static void HitGhost(Vehicle v, int nEffect) {
      if (!EventsOn() || v.m_iModNetSlot < 0 || v.m_bModGhostGone) {
         return;
      }

      long lNow = Now();
      int k = v.m_iModNetSlot * 16 + nEffect;
      if (lNow - m_aLastHit[k] < _HIT_REPEAT_MS) {
         return;
      }

      m_aLastHit[k] = lNow;
      BeginEvent(_EV_HIT, v.m_iModNetSlot);
      m_Ev.PutByte(nEffect);
      Emit(EndEvent(), v.m_iModNetSlot);
      Log("bonus : " + _HIT_NAMES[nEffect] + " sur le joueur " + v.m_iModNetSlot);
   }

   // pièges : n° et durée de vie selon la classe
   static int TrapId(UseItem t) {
      if (t instanceof Banana) {
         return ((Banana)t).m_iModNetId;
      } else if (t instanceof OilCan) {
         return ((OilCan)t).m_iModNetId;
      } else if (t instanceof WaterBottle) {
         return ((WaterBottle)t).m_iModNetId;
      } else if (t instanceof Anvil) {
         return ((Anvil)t).m_iModNetId;
      } else if (t instanceof Catapult) {
         return ((Catapult)t).m_iModNetId;
      } else {
         return t instanceof FunnyGasBox ? ((FunnyGasBox)t).m_iModNetId : 0;
      }
   }

   static void SetTrapId(UseItem t, int nId) {
      if (t instanceof Banana) {
         ((Banana)t).m_iModNetId = nId;
      } else if (t instanceof OilCan) {
         ((OilCan)t).m_iModNetId = nId;
      } else if (t instanceof WaterBottle) {
         ((WaterBottle)t).m_iModNetId = nId;
      } else if (t instanceof Anvil) {
         ((Anvil)t).m_iModNetId = nId;
      } else if (t instanceof Catapult) {
         ((Catapult)t).m_iModNetId = nId;
      } else if (t instanceof FunnyGasBox) {
         ((FunnyGasBox)t).m_iModNetId = nId;
      }
   }

   // un piège posé ici vient de se poser (ou le nuage de gaz d'apparaître) : copie chez les autres
   static void OnTrapLanded(UseItem t, int nType) {
      if (!EventsOn() || TrapId(t) != 0) {
         return; // hors ligne, ou c'est une copie
      }

      int nId = (m_nMySlot + 1) << 24 | ++m_nTrapSerial & 0xFFFFFF;
      SetTrapId(t, nId);
      m_Traps.put(new Integer(nId), new ModNetTrap(t, nType, Now()));
      BeginEvent(_EV_TRAP, _ALL);
      m_Ev.PutInt(nId);
      m_Ev.PutByte(nType);
      PutVec(m_Ev, t.GetPosition());
      Emit(EndEvent(), _ALL);
   }

   // une voiture d'ici déclenche un piège (original ou copie) : il disparaît partout
   static void OnTrapTriggered(UseItem t) {
      int nId = TrapId(t);
      if (nId == 0 || !IsRaceOnline()) {
         return;
      }

      m_Traps.remove(new Integer(nId));
      if (EventsOn()) {
         BeginEvent(_EV_GONE, _ALL);
         m_Ev.PutInt(nId);
         Emit(EndEvent(), _ALL);
      }
   }

   static void OnBoom(int nKind, Vector pos) {
      if (!EventsOn()) {
         return;
      }

      BeginEvent(_EV_BOOM, _ALL);
      m_Ev.PutByte(nKind);
      PutVec(m_Ev, pos);
      Emit(EndEvent(), _ALL);
   }

   // --- boîtes bonus : chaque jeu a les siennes, celle que je ramasse disparaît aussi chez les autres ---
   static final float _PICK_MATCH_DIST = 200.0F; // boîte reconnue à sa position d'origine (même circuit partout)

   // BonusItem.OnCollisionObject, voiture de ce jeu
   static void OnBoxTaken(BonusItem box) {
      if (!EventsOn()) {
         return;
      }

      BeginEvent(_EV_PICK, _ALL);
      PutVec(m_Ev, box.m_vDefaultPos);
      Emit(EndEvent(), _ALL);
   }

   static void TakeBox(Vector pos) {
      RacerModule mod = RacerModule.m_Mod;
      if (mod == null || mod.m_BonusTab == null) {
         return;
      }

      BonusItem best = null;
      float fBest = _PICK_MATCH_DIST * _PICK_MATCH_DIST;

      for (int i = 0; i < mod.m_BonusTab.length; i++) {
         BonusItem b = mod.m_BonusTab[i];
         if (b != null && b.m_vDefaultPos != null) {
            float dx = b.m_vDefaultPos.fX - pos.fX;
            float dy = b.m_vDefaultPos.fY - pos.fY;
            float dz = b.m_vDefaultPos.fZ - pos.fZ;
            float d = dx * dx + dy * dy + dz * dz;
            if (d < fBest) {
               fBest = d;
               best = b;
            }
         }
      }

      if (best != null && !best.m_fWasCollected) {
         best.m_fWasCollected = true;
         best.AfterCollectingBonus(); // s'envole et revient 10 s plus tard, comme ramassée ici
         m_nBoxesTaken++;
      }
   }

   static int m_nBoxesTaken = 0;

   // --- chocs entre voitures ---
   // Le fantôme bloque ma voiture comme une voiture locale (drapeau 32 du moteur) : c'est ma voiture qui le heurte,
   // Vehicle.OnCollisionFullInfoMove calcule le choc pour les deux, comme dans le jeu d'origine. Ma part s'applique
   // ici ; la part du fantôme est cumulée puis envoyée à son joueur (BUMP) avec l'état suivant, qui l'applique à sa
   // vraie voiture. Si les deux jeux voient le contact en même temps, chacun garde sa propre part (comme
   // m_bGotCollision du jeu d'origine, qui ne traite un choc qu'une fois).
   static final long _BUMP_SELF_MS = 150L;
   // [mod] dernier contact vu ici, par paire (voiture de ce jeu, fantôme) : [place locale * 6 + place du fantôme]
   static long[] m_aContact = new long[_MAX_PLAYERS * _MAX_PLAYERS];
   static int m_nContacts = 0;
   static int m_nBumpsSent = 0;
   static int m_nBumpsRecv = 0;
   static int m_nBumpsSkipped = 0;
   static int m_nUnstuck = 0;

   // Vehicle.OnCollisionFullInfoMove : une voiture d'ici (la mienne, ou une IA de l'hôte) heurte un fantôme ;
   // push = poussée à ajouter à son m_vCollisionVector, fKeep = part de vitesse qu'il garde
   static void BumpGhost(Vehicle hitter, Vehicle ghost, Vector push, float fKeep) {
      int nSlot = ghost.m_iModNetSlot;
      if (!EventsOn() || nSlot < 0 || nSlot >= _MAX_PLAYERS || m_Players[nSlot] == null || ghost.m_bModGhostGone) {
         return;
      }

      int nFrom = hitter != null && hitter.m_iModNetSlot >= 0 && hitter.m_iModNetSlot < _MAX_PLAYERS ? hitter.m_iModNetSlot : m_nMySlot;
      m_aEngineContact[nFrom * _MAX_PLAYERS + nSlot] = Now();
      m_aContactFrames[nSlot]++;
      m_nContacts++;
      AddBump(nFrom, nSlot, push, fKeep);
   }

   static void AddBump(int nFrom, int nSlot, Vector push, float fKeep) {
      ModNetPlayer p = m_Players[nSlot];
      m_aContact[nFrom * _MAX_PLAYERS + nSlot] = Now();
      p.m_vBump.Add(push);
      p.m_fBumpKeep *= fKeep;
      p.m_nBumpFrom = nFrom;
      p.m_bBumpPending = true;
   }

   // [mod] Chocs IA de l'hôte <-> fantômes des invités. Essai du 2026-11-20 : le moteur ne signalait jamais de contact
   // entre une IA et le fantôme d'un invité (0 dans le journal de l'hôte), les IA passaient au travers des invités.
   // L'hôte teste donc lui-même, à chaque image, le recouvrement des deux voitures (rayon de l'ellipsoïde, dans le
   // plan) ; s'il y en a un et que le moteur n'a rien dit pour cette paire, il fait le choc comme l'original
   // (OnCollisionFullInfoMove) : chaque voiture repoussée le long de la normale du contact, ralentie si elle fonçait
   // dans l'autre ; la part du fantôme part chez son joueur (BUMP).
   static long[] m_aEngineContact = new long[_MAX_PLAYERS * _MAX_PLAYERS];
   static int[] m_aContactFrames = new int[_MAX_PLAYERS]; // images de contact par place visée (journal)
   static int m_nAIContacts = 0;
   static final float _AI_CONTACT_PUSH = 8.0F; // poussée de séparation (par unité d'enfoncement, par seconde)

   static void HostAIContacts(RacerModule mod) {
      float dt = mod.m_fFrameTime;
      if (m_nRole != _ROLE_HOST || !EventsOn() || dt <= 0.0F) {
         return;
      }

      long lNow = Now();

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer a = m_Players[i];
         Vehicle av = a != null && a.m_bAI ? a.m_AIVehicle : null;
         if (av == null || av.m_bModGhost || !av.IsSetFlag(32)) {
            continue;
         }

         for (int j = 0; j < _MAX_PLAYERS; j++) {
            ModNetPlayer g = m_Players[j];
            Vehicle gv = g != null && !g.m_bAI && !g.m_bGone ? g.m_Ghost : null;
            if (gv == null || gv.m_bModGhostGone || !gv.IsSetFlag(32) || lNow - m_aEngineContact[i * _MAX_PLAYERS + j] < 100L) {
               continue;
            }

            float nx = av.m_vVehiclePosition.fX - gv.m_vVehiclePosition.fX;
            float nz = av.m_vVehiclePosition.fZ - gv.m_vVehiclePosition.fZ;
            float d = (float)Math.sqrt(nx * nx + nz * nz);
            float rr = a.CarRadius() + g.CarRadius();
            float dy = av.m_vVehiclePosition.fY - gv.m_vVehiclePosition.fY;
            if (d >= rr || d < 0.001F || dy > rr || -dy > rr) {
               continue;
            }

            nx /= d; // du fantôme vers l'IA
            nz /= d;
            float vax = av.m_vVehicleForward.fX * av.m_fCurrSpeed;
            float vaz = av.m_vVehicleForward.fZ * av.m_fCurrSpeed;
            float closing = (g.m_vVel.fX - vax) * nx + (g.m_vVel.fZ - vaz) * nz; // > 0 : ils se rapprochent
            float imp = Math.max(closing, 0.0F) * av.m_Params._OBJ_HIT_SUPR * dt + (rr - d) * _AI_CONTACT_PUSH;
            float slow = av.m_Params._OBJ_HIT_SPEED_SUPR * dt;
            if (slow > 1.0F) {
               slow = 1.0F;
            }

            // part de l'IA
            if (av.m_bTurbo) {
               av.DisableTurbo();
            }

            av.m_vCollisionVector.fX += nx * imp;
            av.m_vCollisionVector.fZ += nz * imp;
            if (av.m_vCollisionVector.Len() > av.m_fCollCoef) {
               av.m_vCollisionVector.Normalize();
               av.m_vCollisionVector.Mul(av.m_fCollCoef);
            }

            float fa = av.m_vVehicleForward.fX * nx + av.m_vVehicleForward.fZ * nz;
            if (fa < -0.3F && av.m_fCurrSpeed > 0.0F) {
               av.m_fCurrSpeed -= slow * av.m_fCurrSpeed; // l'IA fonçait dans le fantôme
            }

            if (mod.m_fNowTime - av.m_fLastCollisionSound > av.m_Params._TIME_BETWEEN_HIT_SOUNDS) {
               av.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_CRASH_CAR]);
               av.m_fLastCollisionSound = mod.m_fNowTime;
            }

            // part du fantôme, envoyée à son joueur
            m_vTmp.Set(Vector.Zero);
            m_vTmp.fX = -nx * imp;
            m_vTmp.fZ = -nz * imp;
            float gl = (float)Math.sqrt(g.m_vVel.fX * g.m_vVel.fX + g.m_vVel.fZ * g.m_vVel.fZ);
            float fg = gl > 1.0F ? -(g.m_vVel.fX * nx + g.m_vVel.fZ * nz) / gl : 0.0F;
            AddBump(i, j, m_vTmp, fg > 0.3F ? 1.0F - slow : 1.0F); // l'invité fonçait dans l'IA : il ralentit
            m_aContactFrames[j]++;
            m_nAIContacts++;
         }
      }
   }

   static void FlushBumps() {
      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && p.m_bBumpPending) {
            p.m_bBumpPending = false;
            if (EventsOn() && !p.m_bGone) {
               BeginEvent(_EV_BUMP, p.m_nBumpFrom, i);
               PutVec(m_Ev, p.m_vBump);
               m_Ev.PutFloat(p.m_fBumpKeep);
               Emit(EndEvent(), i);
               m_nBumpsSent++;
            }

            p.m_vBump.Set(Vector.Zero);
            p.m_fBumpKeep = 1.0F;
         }
      }
   }

   // la part d'une voiture de ce jeu (place nTarget : moi, ou une IA de l'hôte) dans un choc vu chez un autre joueur
   static void ApplyBump(int nOrigin, int nTarget, Vector push, float fKeep) {
      Vehicle me = LocalCar(nTarget);
      if (me == null || nOrigin >= _MAX_PLAYERS || m_Players[nOrigin] == null) {
         return;
      }

      m_nBumpsRecv++;
      if (Now() - m_aContact[nTarget * _MAX_PLAYERS + nOrigin] < _BUMP_SELF_MS) {
         m_nBumpsSkipped++; // j'ai déjà traité ce contact ici
         return;
      }

      if (me.m_bTurbo) {
         me.DisableTurbo();
      }

      me.m_vCollisionVector.Add(push);
      if (me.m_vCollisionVector.Len() > me.m_fCollCoef) {
         me.m_vCollisionVector.Normalize();
         me.m_vCollisionVector.Mul(me.m_fCollCoef);
      }

      if (fKeep < 0.0F) {
         fKeep = 0.0F;
      }

      if (fKeep < 1.0F) {
         me.m_fCurrSpeed *= fKeep;
      }

      if (RacerModule.m_Mod.m_fNowTime - me.m_fLastCollisionSound > me.m_Params._TIME_BETWEEN_HIT_SOUNDS) {
         me.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_CRASH_CAR]);
         me.m_fLastCollisionSound = RacerModule.m_Mod.m_fNowTime;
      }
   }

   // à chaque état envoyé : événements non confirmés + accusé, sur chaque lien qui en a
   static void SendEvents() {
      if (m_nRole == _ROLE_GUEST) {
         SendEvents(m_Players[m_nHostSlot], m_HostAddr);
      } else {
         for (int i = 0; i < _MAX_PLAYERS; i++) {
            ModNetPlayer p = m_Players[i];
            if (p != null && i != m_nMySlot && !p.m_bGone && p.m_Addr != null) {
               SendEvents(p, p.m_Addr);
            }
         }
      }
   }

   static void SendEvents(ModNetPlayer link, ModAddr to) {
      if (link == null || link.m_bGone || to == null || link.m_EvOut.isEmpty() && !link.m_bAckDirty) {
         return;
      }

      BeginPacket(_T_EVT);
      m_Out.PutByte(m_nMySlot);
      m_Out.PutInt(m_nLoadedRace);
      m_Out.PutInt(link.m_nEvIn);
      m_Out.PutInt(link.m_nEvOutFirst);
      int nCountPos = m_Out.Length();
      m_Out.PutByte(0);
      int n = 0;

      for (int i = 0; i < link.m_EvOut.size() && n < 255; i++) {
         byte[] ev = (byte[])link.m_EvOut.elementAt(i);
         if (m_Out.Length() + ev.length + 1 > _EVT_BUDGET) {
            break;
         }

         m_Out.PutByte(ev.length);

         for (int j = 0; j < ev.length; j++) {
            m_Out.PutByte(ev[j]);
         }

         n++;
      }

      m_Out.m_Data[nCountPos] = (byte)n;
      m_Udp.Send(to, m_Out);
      link.m_bAckDirty = false;
   }

   static void OnEvents(ModPacket pk) {
      int nFrom = m_In.GetByte();
      int nRace = m_In.GetInt();
      int nAck = m_In.GetInt();
      int nFirst = m_In.GetInt();
      int n = m_In.GetByte();
      if (m_In.m_bBad || nFrom >= _MAX_PLAYERS || nRace != m_nLoadedRace || !IsRaceOnline()) {
         return;
      }

      ModNetPlayer link = m_nRole == _ROLE_GUEST ? m_Players[m_nHostSlot] : m_Players[nFrom];
      ModAddr expected = m_nRole == _ROLE_GUEST ? m_HostAddr : link != null ? link.m_Addr : null;
      if (link == null || link.m_bGone || expected == null || !pk.m_From.equals(expected)) {
         return;
      }

      link.m_lLastHeard = pk.m_lTime;
      // accusé : ce que l'autre a appliqué ne sera plus redit
      while (!link.m_EvOut.isEmpty() && nAck - link.m_nEvOutFirst >= 0) {
         link.m_EvOut.removeElementAt(0);
         link.m_nEvOutFirst++;
      }

      for (int k = 0; k < n; k++) {
         int nLen = m_In.GetByte();
         if (m_In.m_bBad || m_In.Remaining() < nLen) {
            return;
         }

         int nSeq = nFirst + k;
         int nStart = m_In.m_nPos;
         m_In.m_nPos += nLen;
         if (nSeq - link.m_nEvIn <= 0) {
            link.m_bAckDirty = true; // déjà appliqué : l'accusé s'est perdu, on le redit
         } else if (nSeq == link.m_nEvIn + 1) {
            link.m_nEvIn = nSeq;
            link.m_bAckDirty = true;
            byte[] ev = new byte[nLen];
            System.arraycopy(m_In.m_Data, nStart, ev, 0, nLen);
            RouteEvent(ev, link);
         }
      }
   }

   // l'hôte relaie ce qui ne lui est pas destiné ; chacun applique ce qui le concerne
   static void RouteEvent(byte[] ev, ModNetPlayer fromLink) {
      if (ev.length < 3) {
         return;
      }

      int nOrigin = ev[1] & 255;
      int nTarget = ev[2] & 255;
      // [mod] une de mes IA est visée : l'effet s'applique ici, comme pour ma voiture
      boolean bLocal = nTarget == m_nMySlot || nTarget < _MAX_PLAYERS && LocalCar(nTarget) != null;
      if (m_nRole == _ROLE_HOST) {
         if (nTarget == _ALL) {
            for (int i = 0; i < _MAX_PLAYERS; i++) {
               if (i != nOrigin && m_Players[i] != fromLink) {
                  Queue(m_Players[i], ev); // sauf moi et les IA (Queue)
               }
            }
         } else if (!bLocal) {
            if (nTarget < _MAX_PLAYERS) {
               Queue(m_Players[nTarget], ev);
            }

            return;
         }
      }

      if (nTarget == _ALL || bLocal) {
         m_nEventsRecv++;
         ApplyEvent(ev);
      }
   }

   static ModBuf m_EvIn = new ModBuf(0);

   static void ApplyEvent(byte[] ev) {
      ModBuf b = m_EvIn;
      b.Wrap(ev, ev.length);
      int nKind = b.GetByte();
      int nOrigin = b.GetByte();
      int nTarget = b.GetByte();
      Vehicle from = nOrigin < _MAX_PLAYERS && m_Players[nOrigin] != null ? m_Players[nOrigin].m_Ghost : null;
      if (nKind == _EV_HIT) {
         ApplyHit(b.GetByte(), nOrigin, from, nTarget);
      } else if (nKind == _EV_TRAP) {
         int nId = b.GetInt();
         int nType = b.GetByte();
         Vector pos = new Vector();
         pos.fX = b.GetFloat();
         pos.fY = b.GetFloat();
         pos.fZ = b.GetFloat();
         if (!b.m_bBad) {
            CreateTrapCopy(nId, nType, pos, from);
         }
      } else if (nKind == _EV_GONE) {
         RemoveTrap(b.GetInt());
      } else if (nKind == _EV_BUMP) {
         Vector push = new Vector();
         push.fX = b.GetFloat();
         push.fY = b.GetFloat();
         push.fZ = b.GetFloat();
         float fKeep = b.GetFloat();
         if (!b.m_bBad) {
            ApplyBump(nOrigin, nTarget, push, fKeep);
         }
      } else if (nKind == _EV_PICK) {
         Vector pos = new Vector();
         pos.fX = b.GetFloat();
         pos.fY = b.GetFloat();
         pos.fZ = b.GetFloat();
         if (!b.m_bBad) {
            TakeBox(pos);
         }
      } else if (nKind == _EV_BOOM) {
         int nBoom = b.GetByte();
         Vector pos = new Vector();
         pos.fX = b.GetFloat();
         pos.fY = b.GetFloat();
         pos.fZ = b.GetFloat();
         Vehicle me = LocalVehicle();
         Vehicle fx = from != null ? from : me;
         if (!b.m_bBad && fx != null) {
            if (nBoom == _BOOM_WATERMELON) {
               fx.ExecuteFX("FXWaterMelonExplosion", pos, Vector.Up);
            } else {
               fx.ExecuteFX("Explosion", pos, Vector.Up);
            }
         }
      }
   }

   // effet d'un bonus d'un autre joueur sur une voiture de ce jeu (la mienne, ou une IA de l'hôte) ; la protection
   // fait foi ici
   static void ApplyHit(int nEffect, int nOrigin, Vehicle from, int nTarget) {
      Vehicle me = LocalCar(nTarget);
      if (me == null || me.m_bRaceFinished || nEffect <= 0 || nEffect >= _HIT_NAMES.length) {
         return;
      }

      String sOn = nTarget == m_nMySlot ? "" : " sur mon IA " + nTarget;
      if (nEffect != _HIT_UFO && me.m_bProtection) {
         Log("bonus : " + _HIT_NAMES[nEffect] + " du joueur " + nOrigin + sOn + " arrete par la protection");
         return;
      }

      Log("bonus : " + _HIT_NAMES[nEffect] + " du joueur " + nOrigin + sOn);
      switch (nEffect) {
         case _HIT_STARS:
            me.StopAndStars();
            break;
         case _HIT_WASPS:
            me.StoppedByWaspBag();
            break;
         case _HIT_TOMATO:
            me.StoppedByTomatoOrCookie();
            break;
         case _HIT_EXPLOSION:
            me.ExplosionEffect();
            break;
         case _HIT_FREEZE:
            me.FreezeVehicle();
            break;
         case _HIT_WATER:
            me.WaterVehicle();
            break;
         case _HIT_ROCK:
            me.FallingRock();
            break;
         case _HIT_MOLE:
            me.StoppedByMole();
            break;
         case _HIT_UFO:
            // l'OVNI arrive du ciel sur ma voiture (il teste lui-même la protection)
            UFOObject u = UseItem.CreateUFOObject(from != null ? from : me);
            u.Init(me);
      }
   }

   static void CreateTrapCopy(int nId, int nType, Vector pos, Vehicle from) {
      Vehicle parent = from != null && !from.m_bModGhostGone ? from : LocalVehicle();
      if (parent == null || m_Traps.containsKey(new Integer(nId))) {
         return;
      }

      UseItem t = null;
      switch (nType) {
         case UseItem._USE_BANANA:
            t = UseItem.CreateBanana(parent);
            break;
         case UseItem._USE_OILCAN:
            t = UseItem.CreateOilCan(parent);
            break;
         case UseItem._USE_WATERBOTTLE:
            t = UseItem.CreateWaterBottle(parent);
            break;
         case UseItem._USE_ANVIL:
            t = UseItem.CreateAnvil(parent);
            break;
         case UseItem._USE_CATAPULT:
            t = UseItem.CreateCatapult(parent);
            break;
         case UseItem._USE_FUNNYGAS:
            t = UseItem.CreateFunnyGasBox(parent);
            break;
         default:
            return;
      }

      SetTrapId(t, nId);
      t.SetPosition(pos);
      if (t instanceof Banana) {
         ((Banana)t).ModLand(Vector.Up);
      } else if (t instanceof OilCan) {
         ((OilCan)t).ModLand(Vector.Up);
      } else if (t instanceof WaterBottle) {
         ((WaterBottle)t).ModLand(Vector.Up);
      } else if (t instanceof Anvil) {
         ((Anvil)t).ModLand(Vector.Up);
      } else if (t instanceof Catapult) {
         ((Catapult)t).ModLand(Vector.Up);
      } else if (t instanceof FunnyGasBox) {
         ((FunnyGasBox)t).FireFX();
      }

      m_Traps.put(new Integer(nId), new ModNetTrap(t, nType, Now()));
   }

   static void RemoveTrap(int nId) {
      ModNetTrap tr = (ModNetTrap)m_Traps.remove(new Integer(nId));
      if (tr == null) {
         return;
      }

      long lAge = Now() - tr.m_lTime;
      if (lAge < (tr.m_nType == UseItem._USE_FUNNYGAS ? _GAS_SAFE_MS : _TRAP_SAFE_MS)) {
         tr.m_Item.DeleteTraces();
         tr.m_Item.delete();
      }
   }

   static void ResetEvents() {
      m_Traps.clear();
      m_nTrapSerial = 0;
      m_nEventsSent = 0;
      m_nEventsRecv = 0;
      m_nContacts = 0;
      m_nBumpsSent = 0;
      m_nBumpsRecv = 0;
      m_nBumpsSkipped = 0;
      m_nUnstuck = 0;
      m_nBoxesTaken = 0;

      for (int i = 0; i < m_aLastHit.length; i++) {
         m_aLastHit[i] = 0L;
      }

      for (int i = 0; i < m_aContact.length; i++) {
         m_aContact[i] = 0L;
         m_aEngineContact[i] = 0L;
      }

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         m_aContactFrames[i] = 0;
      }

      m_nAIContacts = 0;

      m_nTakeovers = 0;
   }

   // RacerGame.EndModule : course finie ou quittée ; la session s'arrête (phase 1 : une course par lancement)
   static void OnRaceEnded(boolean bQuit) {
      if (!m_bRaceOnline) {
         return;
      }

      m_bRaceOnline = false;
      Log("fin de course" + (bQuit ? " (quittee)" : "") + " : " + m_nCarsSent + " etats envoyes, " + m_nCarsRecv + " recus, " + m_nEventsSent + " evenements envoyes, " + m_nEventsRecv + " recus, " + m_nBoxesTaken + " boites bonus prises par les autres");
      Log("chocs : " + m_nContacts + " images de contact avec un fantome, " + m_nBumpsSent + " chocs envoyes, " + m_nBumpsRecv + " recus (" + m_nBumpsSkipped + " ignores, deja vus ici), " + m_nUnstuck + " images de chevauchement");
      String sPer = "";

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         if (m_aContactFrames[i] > 0) {
            ModNetPlayer p = m_Players[i];
            sPer = sPer + (sPer.length() > 0 ? ", " : "") + "place " + i + (p != null && p.m_bAI ? " (IA)" : "") + " " + m_aContactFrames[i];
         }
      }

      if (sPer.length() > 0 || m_nAIContacts > 0) {
         Log("chocs : images de contact par voiture visee : " + (sPer.length() > 0 ? sPer : "aucune") + " ; dont " + m_nAIContacts + " vus par le test IA / fantome de l'hote");
      }

      if (m_nTakeovers > 0) {
         Log("hote remplace " + m_nTakeovers + " fois pendant la course (je finis " + (m_nRole == _ROLE_HOST ? "hote" : "invite") + ")");
      }
      m_Traps.clear();
      if (!m_bAuto && ModLobby.InRoom()) {
         // [mod] écran « En ligne » : on reste dans le salon pour la course suivante. Course quittée : les autres
         // retirent ma voiture (BYE), la leur continue
         if (bQuit) {
            SendBye();
         }

         BackToLobby();
         if (m_bRankedRace) {
            // [mod] partie rapide : une course par salon. Course quittée = abandon (dernier) ; puis retour à l'accueil
            // de l'écran, où s'affiche le résultat quand le serveur l'a décidé
            ReportRanked(bQuit);
            m_bRankedRace = false;
            ModLobby.LeaveRoom();
         }

         return;
      }

      SendBye();
      ModLobby.Leave(); // test automatique (nettest.scr) : une course par lancement, on libère le salon
      m_nState = _ST_DONE;
   }

   // --- [mod] phase 4 : partie rapide classée ---
   // Salon créé par le serveur (ModLobby.IsRankedRoom) : réglages imposés (Data\online.scr, circuit tiré au sort par
   // l'hôte), invités prêts d'office, l'hôte lance seul. À la fin, chaque jeu dit au serveur l'ordre d'arrivée des
   // humains (ModLobby.SendResult) ; c'est lui qui compte l'Elo.
   static boolean m_bRankedRace = false; // la course chargée est une course classée
   static boolean m_bRankedReported = false;
   static String m_sRankedRoom = "";
   static int m_nRankedTrack = 0;

   static boolean IsRankedLobby() {
      return !m_bAuto && ModLobby.IsRankedRoom();
   }

   // hôte d'un salon classé, entre son arrivée et le lancement : réglages imposés, lancement automatique
   static void RankedHostFrame() {
      if (!ModLobby.m_sRoom.equals(m_sRankedRoom)) {
         m_sRankedRoom = ModLobby.m_sRoom;
         m_nRankedTrack = RacerGame.GetRandom(KidsOnlineMenuWindow._TRACKS);
         Log("partie rapide : circuit " + m_nRankedTrack + " tire au sort, " + ModOnline.m_nRankedLaps + " tours, IA " + ModAI._MODE_NAMES[ModOnline.m_nRankedAi]);
      }

      m_nTrack = m_nRankedTrack;
      m_nLaps = ModOnline.m_nRankedLaps;
      m_bBonus = true;
      m_nAi = _MAX_PLAYERS - 1;
      m_nAiMode = ModOnline.m_nRankedAi;
      long lWaited = System.currentTimeMillis() - ModLobby.m_lRoomSince;
      boolean bAllHere = true;

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         if (ModLobby.m_Names[i] != null && i != m_nMySlot && m_Players[i] == null) {
            bAllHere = false; // pas encore arrivé dans la session
         }
      }

      // quelques secondes pour voir contre qui on court ; un joueur qui n'arrive pas n'est attendu que 20 s
      if (CanLaunch() == _LAUNCH_OK && AtLobbyPlace() && lWaited > 4000L && (bAllHere || lWaited > 20000L)) {
         Log("partie rapide : lancement (" + CountHumans() + " joueurs)");
         HostLaunch();
      } else if (lWaited > 45000L) {
         Log("partie rapide : course impossible a lancer (" + CanLaunch() + "), salon quitte");
         ModLobby.m_bRankedCancelled = true;
         ModLobby.LeaveRoom();
      }
   }

   // invité d'un salon classé : prêt d'office ; hôte qui ne lance pas en 60 s (parti ?) : on s'en va
   static void RankedGuestFrame() {
      m_bWantReady = true;
      if (!m_bLaunched && System.currentTimeMillis() - ModLobby.m_lRoomSince > 60000L) {
         Log("partie rapide : l'hote ne lance pas la course, salon quitte");
         ModLobby.m_bRankedCancelled = true;
         ModLobby.LeaveRoom();
      }
   }

   // fin de ma course classée (podium : tous les humains sont arrivés ; ou course quittée = abandon)
   static void ReportRanked(boolean bAbandon) {
      if (!m_bRankedRace || m_bRankedReported || m_nMySlot == _NO_SLOT) {
         return;
      }

      m_bRankedReported = true;
      if (bAbandon) {
         ModLobby.SendResult(true, new int[]{m_nMySlot});
         return;
      }

      RerankFinished();
      RacerModule mod = RacerModule.m_Mod;
      Vehicle me = LocalVehicle();
      int[] tmp = new int[_MAX_PLAYERS];
      int n = 0;
      if (mod != null && mod.m_RaceParams != null) {
         java.util.Vector sort = mod.m_RaceParams.m_SortTable;

         for (int k = 0; k < sort.size() && n < _MAX_PLAYERS; k++) {
            Vehicle v = (Vehicle)sort.elementAt(k);
            int nSlot = SlotOfCar(v, me);
            if (v.m_bRaceFinished && nSlot != _NO_SLOT && (nSlot == m_nMySlot || !m_Players[nSlot].m_bAI)) {
               tmp[n++] = nSlot;
            }
         }
      }

      int[] order = new int[n];
      System.arraycopy(tmp, 0, order, 0, n);
      ModLobby.SendResult(false, order);
   }

   static int SlotOfCar(Vehicle v, Vehicle me) {
      if (v == me) {
         return m_nMySlot;
      }

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p != null && i != m_nMySlot && p.Car() == v) {
            return i;
         }
      }

      return _NO_SLOT;
   }

   // [mod] après le podium ou une course quittée : retour à l'écran « En ligne » plutôt qu'au menu de la course simple
   static boolean BackToOnlineScreen() {
      return !m_bAuto && !ModLobby.m_bAutoMode && ModLobby.IsActive();
   }

   // [mod] phase 3 : retour à l'attente du salon entre deux courses
   static void BackToLobby() {
      m_nState = _ST_LOBBY;
      m_bLocalReady = false;
      m_bWantReady = false;
      m_bLightsStarted = false;
      if (m_nRole == _ROLE_HOST) {
         m_bLaunched = false; // l'invité ne relance pas : son n° de course chargée est déjà celui-ci
         m_bGoKnown = false;
      }

      for (int i = 0; i < _MAX_PLAYERS; i++) {
         ModNetPlayer p = m_Players[i];
         if (p == null) {
            continue;
         }

         if (p.m_bAI) {
            m_Players[i] = null; // les IA ne vivent que le temps d'une course (AddAIPlayers au lancement suivant)
            continue;
         }

         p.m_Ghost = null;
         p.ResetRace();
         p.m_bLobbyReady = false;
         if (p.m_bGone && i != m_nMySlot && m_nRole == _ROLE_HOST) {
            m_Players[i] = null; // parti pendant la course : s'il est encore là, son JOIN le fait revenir
         }
      }

      Log("retour au salon (course " + m_nRaceId + " finie)");
   }

   static void SendBye() {
      if (m_nMySlot == _NO_SLOT) {
         return;
      }

      BeginPacket(_T_BYE);
      m_Out.PutByte(m_nMySlot);

      for (int k = 0; k < 3; k++) {
         if (m_nRole == _ROLE_HOST) {
            for (int i = 0; i < _MAX_PLAYERS; i++) {
               ModNetPlayer p = m_Players[i];
               if (p != null && i != m_nMySlot && !p.m_bGone && p.m_Addr != null) {
                  m_Udp.Send(p.m_Addr, m_Out);
               }
            }
         } else {
            m_Udp.Send(m_HostAddr, m_Out);
         }
      }
   }
}

// un joueur de la session (local ou distant)
class ModNetPlayer {
   static final int _SNAPS = 16;
   int m_nSlot;
   int m_nGuestId = 0;
   ModAddr m_Addr = null;
   int m_nAnimal = 0;
   String m_sName = "?";
   boolean m_bAtMenu = false; // à l'endroit où l'on attend la course (écran « En ligne », ou menu principal en test)
   boolean m_bLobbyReady = false; // [mod] phase 3 : s'est dit prêt (l'hôte l'est toujours)
   boolean m_bReady = false; // course chargée
   boolean m_bGone = false;
   long m_lLastHeard = 0L;
   Vehicle m_Ghost = null;
   // [mod] IA de l'hôte : pas d'adresse ni de lien ; chez l'hôte, sa voiture est m_AIVehicle (VehicleAI), chez les
   // autres un fantôme (m_Ghost)
   boolean m_bAI = false;
   Vehicle m_AIVehicle = null;
   boolean m_bFinished = false;
   float m_fFinishTime = 0.0F;
   // états reçus, rangés dans un anneau (le plus récent en m_nHead - 1)
   ModNetSnap[] m_Snaps = new ModNetSnap[_SNAPS];
   int m_nHead = 0;
   int m_nCount = 0;
   // lien d'événements fiables avec ce joueur (hôte : un par invité ; invité : celui de l'hôte)
   java.util.Vector m_EvOut = new java.util.Vector();
   int m_nEvOutFirst = 1; // n° du premier événement de m_EvOut
   int m_nEvIn = 0; // dernier n° appliqué venant de lui
   boolean m_bAckDirty = false;
   int m_nPrevFlags = 0;
   // affichage : retard d'interpolation propre à ce joueur (ms)
   int m_nDelay = ModNet._INTERP_DELAY_MS;
   // chocs : part du fantôme cumulée jusqu'au prochain envoi, et la voiture d'ici qui l'a heurté (ModNet.m_aContact)
   Vector m_vBump = new Vector();
   float m_fBumpKeep = 1.0F;
   boolean m_bBumpPending = false;
   int m_nBumpFrom = 0;
   Vector m_vVel = new Vector();
   Vector m_vTmp = new Vector();
   float m_fRadius = 0.0F; // ellipsoïde du fantôme (le plus petit rayon horizontal)

   ModNetPlayer(int nSlot) {
      m_nSlot = nSlot;

      for (int i = 0; i < _SNAPS; i++) {
         m_Snaps[i] = new ModNetSnap();
      }
   }

   // la voiture de ce joueur dans la course d'ici (fantôme, ou IA que ce jeu fait rouler)
   Vehicle Car() {
      return m_Ghost != null ? m_Ghost : m_AIVehicle;
   }

   // rayon de son ellipsoïde dans le plan (SetEllipsoid : rayon, hauteur, décalage), pour le test IA / fantôme
   float m_fCarRadius = 0.0F;
   Vehicle m_RadiusOf = null;

   float CarRadius() {
      Vehicle v = Car();
      if (v != null && v != m_RadiusOf) {
         m_RadiusOf = v;
         m_fCarRadius = v.GetEllipsoid().fX;
      }

      return m_fCarRadius;
   }

   // lien d'événements remis à zéro (hôte remplacé en course : nouveau lien des deux côtés)
   void ResetLink() {
      m_EvOut.removeAllElements();
      m_nEvOutFirst = 1;
      m_nEvIn = 0;
      m_bAckDirty = false;
   }

   void ResetRace() {
      ResetLink();
      m_nPrevFlags = 0;
      m_nDelay = ModNet._INTERP_DELAY_MS;
      m_vBump.Set(Vector.Zero);
      m_fBumpKeep = 1.0F;
      m_bBumpPending = false;
      m_nBumpFrom = 0;
      m_bReady = false;
      m_bFinished = false;
      m_fFinishTime = 0.0F;
      m_nHead = 0;
      m_nCount = 0;
   }

   ModNetSnap NextSnap() {
      ModNetSnap s = m_Snaps[m_nHead];
      m_nHead = (m_nHead + 1) % _SNAPS;
      if (m_nCount < _SNAPS) {
         m_nCount++;
      }

      return s;
   }

   void DropSnap() {
      m_nHead = (m_nHead + _SNAPS - 1) % _SNAPS;
      m_nCount--;
   }

   ModNetSnap Snap(int iAge) {
      return m_Snaps[(m_nHead - 1 - iAge + _SNAPS * 2) % _SNAPS];
   }

   // nouvel état reçu (déjà rangé en Snap(0)) : sa vitesse d'après le précédent, et le retard d'affichage
   // nécessaire pour toujours avoir deux états autour de l'heure affichée
   void OnSnap(ModNetSnap s, int nHostNow) {
      s.m_vVel.Set(Vector.Zero);
      if (m_nCount > 1) {
         ModNetSnap prev = Snap(1);
         int dt = s.m_nTime - prev.m_nTime;
         if (dt > 0 && dt < 500) {
            s.m_vVel.Set(s.m_vPos);
            s.m_vVel.Sub(prev.m_vPos);
            s.m_vVel.Mul(1000.0F / dt);
            if (s.m_vVel.Len() > 20000.0F) {
               s.m_vVel.Set(Vector.Zero); // téléportation (remise en piste)
            }
         }
      }

      int nAge = nHostNow - s.m_nTime;
      if (nAge < 0) {
         nAge = 0;
      }

      int nWant = nAge + (int)ModNet._CAR_MS + 20;
      if (nWant > m_nDelay) {
         m_nDelay = nWant;
      } else {
         m_nDelay--;
      }

      if (m_nDelay < ModNet._INTERP_MIN_MS) {
         m_nDelay = ModNet._INTERP_MIN_MS;
      }

      if (m_nDelay > ModNet._INTERP_MAX_MS) {
         m_nDelay = ModNet._INTERP_MAX_MS;
      }
   }

   // pose le fantôme : interpolation entre les deux états qui encadrent l'heure nHostNow - m_nDelay (lisse), puis
   // prolongée jusqu'à maintenant avec sa vitesse, pour que le fantôme soit là où l'autre joueur est vraiment
   // (sinon chacun se voit devant l'autre et on heurte une voiture qui n'est plus là)
   void Apply(int nHostNow, RacerModule mod, Vehicle me) {
      if (m_nCount == 0) {
         return;
      }

      int nRender = nHostNow - m_nDelay;

      ModNetSnap a = Snap(0);
      ModNetSnap b = null;

      for (int k = 0; k < m_nCount; k++) {
         ModNetSnap s = Snap(k);
         if (s.m_nTime - nRender <= 0) {
            a = s;
            break;
         }

         b = s;
         a = s; // plus vieux que tout : on prend le plus ancien
      }

      float f = 0.0F;
      if (b != null && b != a && b.m_nTime - a.m_nTime > 0) {
         f = (float)(nRender - a.m_nTime) / (float)(b.m_nTime - a.m_nTime);
         if (f < 0.0F) {
            f = 0.0F;
         }

         if (f > 1.0F) {
            f = 1.0F;
         }
      } else {
         b = a;
      }

      Vehicle v = m_Ghost;
      Lerp(v.m_vVehiclePosition, a.m_vPos, b.m_vPos, f);
      Lerp(m_vVel, a.m_vVel, b.m_vVel, f);
      float fAhead = (nHostNow - (a.m_nTime + f * (b.m_nTime - a.m_nTime))) / 1000.0F;
      if (fAhead < 0.0F) {
         fAhead = 0.0F;
      }

      if (fAhead > ModNet._EXTRAP_MAX_MS / 1000.0F) {
         fAhead = ModNet._EXTRAP_MAX_MS / 1000.0F;
      }

      m_vTmp.Set(m_vVel);
      m_vTmp.Mul(fAhead);
      v.m_vVehiclePosition.Add(m_vTmp);
      // déplacement de l'image, lu par le calcul du choc (Vehicle.OnCollisionFullInfoMove)
      v.m_vMoveDir.Set(m_vVel);
      v.m_vMoveDir.Mul(mod.m_fFrameTime);
      Lerp(v.m_vVehicleForward, a.m_vFwd, b.m_vFwd, f);
      v.m_vVehicleForward.Normalize();
      Lerp(v.m_vVehicleNormal, a.m_vUp, b.m_vUp, f);
      v.m_vVehicleNormal.Normalize();
      v.m_vVehicleRight = v.m_vVehicleNormal.Cross(v.m_vVehicleForward); // caméra du spectateur (RacerCamera)
      v.m_vVehicleRight.Normalize();
      v.m_vVehicleTForward.Set(v.m_vVehicleForward);
      v.m_vVehicleTNormal.Set(v.m_vVehicleNormal);
      v.FromUpForwardPos(v.m_vVehicleNormal, v.m_vVehicleForward, v.m_vVehiclePosition);
      v.m_fCurrVehicleHAngle = a.m_fH + (b.m_fH - a.m_fH) * f;
      v.m_fCurrVehicleVAngle = a.m_fV + (b.m_fV - a.m_fV) * f;
      v.m_fCurrSpeed = a.m_fSpeed + (b.m_fSpeed - a.m_fSpeed) * f;
      v.m_fCurrDirAngle = a.m_fDir + (b.m_fDir - a.m_fDir) * f;
      ModNetSnap d = f < 0.5F ? a : b;
      v.m_iCurrLoop = d.m_nLoop;
      if (d.m_nTrackPoint >= 0) {
         v.m_iCurrTrackPoint = d.m_nTrackPoint;
      }

      v.m_bTurbo = (d.m_nFlags & ModNet._F_TURBO) != 0;
      v.m_bTireSpikes = (d.m_nFlags & ModNet._F_SPIKES) != 0;
      v.m_bProtection = (d.m_nFlags & ModNet._F_PROTECTION) != 0;
      // bulle de protection : effet et son quand elle apparaît, son coupé quand elle disparaît
      if (v.m_bProtection && (m_nPrevFlags & ModNet._F_PROTECTION) == 0) {
         v.ExecuteFX(v, "FXProtection", Vector.Zero);
         v.DetachProtectionSound();
         v.AttachProtectionSound("Data\\Sounds\\protect.wav");
      } else if (!v.m_bProtection && (m_nPrevFlags & ModNet._F_PROTECTION) != 0) {
         v.DetachProtectionSound();
      }

      m_nPrevFlags = d.m_nFlags;
      // le fantôme bloque comme une vraie voiture, sauf si la sienne est traversable (OVNI) ; s'il est arrivé sur
      // ma voiture (téléportation, erreur de prévision), on le rend traversable le temps de se dégager, sinon le
      // moteur pourrait coincer ma voiture dedans
      boolean bBlock = (d.m_nFlags & ModNet._F_NOBLOCK) == 0;
      if (bBlock && me != null) {
         if (m_fRadius <= 0.0F) {
            Vector e = v.GetEllipsoid();
            m_fRadius = e.fX < e.fZ ? e.fX : e.fZ;
         }

         float r = m_fRadius;
         m_vTmp.Set(me.m_vVehiclePosition);
         m_vTmp.Sub(v.m_vVehiclePosition);
         if (m_vTmp.Len() < r) {
            bBlock = false;
            ModNet.m_nUnstuck++;
         }
      }

      if (bBlock) {
         v.SetFlag(32);
      } else {
         v.ClearFlag(32);
      }
      if (mod.m_fFrameTime > 0.0F) {
         v.UpdateSpringsAndWheels();
         v.UpdateVehicleCharacter();
         if (v.m_bTurbo) {
            v.SetEngineFreqTurbo();
         } else {
            v.SetEngineFreq();
         }
      }
   }

   static void Lerp(Vector out, Vector a, Vector b, float f) {
      out.fX = a.fX + (b.fX - a.fX) * f;
      out.fY = a.fY + (b.fY - a.fY) * f;
      out.fZ = a.fZ + (b.fZ - a.fZ) * f;
   }
}

// un état de voiture reçu
class ModNetSnap {
   int m_nTime;
   Vector m_vPos = new Vector();
   Vector m_vFwd = new Vector();
   Vector m_vUp = new Vector();
   float m_fH;
   float m_fV;
   float m_fSpeed;
   float m_fDir;
   int m_nLoop;
   int m_nTrackPoint;
   int m_nFlags;
   float m_fFinishTime;
   Vector m_vVel = new Vector(); // calculée à la réception (ModNetPlayer.OnSnap)

   boolean Read(ModBuf b) {
      m_nTime = b.GetInt();
      GetVector(b, m_vPos);
      GetVector(b, m_vFwd);
      GetVector(b, m_vUp);
      m_fH = b.GetFloat();
      m_fV = b.GetFloat();
      m_fSpeed = b.GetFloat();
      m_fDir = b.GetFloat();
      m_nLoop = b.GetByte() - 1;
      m_nTrackPoint = b.GetShort();
      m_nFlags = b.GetByte();
      m_fFinishTime = b.GetFloat();
      return !b.m_bBad;
   }

   static void GetVector(ModBuf b, Vector v) {
      v.fX = b.GetFloat();
      v.fY = b.GetFloat();
      v.fZ = b.GetFloat();
   }
}

// un piège posé ou recopié (ModNet.m_Traps)
class ModNetTrap {
   UseItem m_Item;
   int m_nType;
   long m_lTime;

   ModNetTrap(UseItem item, int nType, long lTime) {
      m_Item = item;
      m_nType = nType;
      m_lTime = lTime;
   }
}
