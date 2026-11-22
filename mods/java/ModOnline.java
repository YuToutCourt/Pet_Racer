import java.io.File;

// [mod] Multijoueur en ligne (chantier 8, notes/online.md) : réglages et test de faisabilité (phase 0).
//
// Réglages communs : Data\online.scr (adresse du serveur). Réglages propres à un dossier de jeu : nettest.scr à la
// racine (game_dev et game_dev_b ont chacun le leur, build.ps1 -Second ne les écrase pas) :
//   NetPort(7781)                  port UDP local (0 ou absent = au choix du système)
//   NetTest("127.0.0.1", 7782)     envoie des pings à cette adresse : l'autre jeu, ou server\echo_test.py
// Tout jeu qui a un socket ouvert répond aux pings des autres : deux jeux qui se visent l'un l'autre se testent
// mutuellement. Résultats : lignes « [mod] net » de crash.log.
// Course en ligne automatique (phase 1, ModNet), lancée depuis le menu principal :
//   NetAuto("host") ou NetAuto("join")   rôle de ce jeu
//   NetPeer("127.0.0.1", 7781)           adresse de l'hôte (invité)
//   NetPlayers(2)                        hôte : nombre de joueurs attendus avant de lancer
//   NetTrack(0) NetLaps(3)               hôte : circuit (0..30, RacerModule.MAPNAMES) et tours
//   NetAnimal(0)                         animal de ce joueur (0..11)
//   NetBonus(1)                          hôte : boîtes bonus (1, par défaut) ou course sans bonus (0)
//   NetRoom("TEST")                      phase 3 : passe par le serveur (Data\online.scr) au lieu d'aller en direct ;
//                                        l'hôte crée un salon, l'invité rejoint ce code (serveur lancé avec --code TEST)
class ModOnline {
   static final String _COMMON_FILE = "Data\\online.scr";
   static final String _LOCAL_FILE = "nettest.scr";

   // serveur (phases 3-4)
   static String m_sServerHost = "127.0.0.1";
   static int m_nServerPort = 7778;
   // [mod] phase 4 : partie rapide classée, réglages imposés (Ranked(tours, niveau des IA) de Data\online.scr) ; le
   // circuit est tiré au sort par l'hôte, boîtes bonus toujours, les places libres complétées par des IA
   static int m_nRankedLaps = 3;
   static int m_nRankedAi = 5; // ModAI : 1 auto, 2 facile, 3 normal, 4 difficile, 5 expert

   static int m_nLocalPort = 0;
   static String m_sAuto = null;
   static String m_sPeerHost = "127.0.0.1";
   static int m_nPeerPort = 7781;
   static boolean m_bWantSocket = false;
   static String m_sRoom = null;
   static ModUdp m_Udp = null;
   static final ModBuf m_Out = new ModBuf(ModUdp._MAX_PACKET);
   static final ModBuf m_In = new ModBuf(0);

   static void Log(String s) {
      GameObject.CrashLog("[mod] net " + s + "\n");
   }

   // RacerGame.OnInitialize
   static void Init() {
      LoadConfig();
      LogClock();
      if (m_bWantSocket) {
         m_Udp = ModUdp.Open(m_nLocalPort);
         if (m_Udp != null) {
            if (m_sRoom != null && m_sAuto != null) {
               ModNet.Init(m_Udp, null, null, 0); // le socket seulement : pas de course directe
               ModLobby.Init(m_Udp, m_sServerHost, m_nServerPort, m_sRoom, m_sAuto.equals("host"));
               ModNet.m_bAuto = ModLobby.IsActive(); // ModLobby lance l'hôte ou l'invité une fois dans le salon
            } else {
               ModNet.Init(m_Udp, m_sAuto, m_sPeerHost, m_nPeerPort);
            }
         }
      }
   }

   // [mod] phase 3 : écran « En ligne » ouvert. Socket ouvert s'il ne l'est pas (port au choix du système), puis
   // connexion au serveur de Data\online.scr. false : pas de réseau possible.
   static boolean OpenOnline() {
      if (m_Udp == null) {
         m_Udp = ModUdp.Open(m_nLocalPort);
         if (m_Udp == null) {
            return false;
         }

         ModNet.Init(m_Udp, null, null, 0);
      }

      ModLobby.Connect(m_Udp, m_sServerHost, m_nServerPort);
      return ModLobby.IsActive();
   }

   // RacerGame.OnFrame, à chaque image (menus, course, vidéos)
   static void Frame() {
      if (m_Udp == null) {
         return;
      }

      long lNow = System.currentTimeMillis();
      TrackFrame(lNow);

      for (int i = 0; i < 64; i++) {
         ModPacket p = m_Udp.Poll();
         if (p == null) {
            break;
         }

         m_In.Wrap(p.m_Data, p.m_nLen);
         int nMagic = m_In.GetInt();
         if (nMagic == ModNet._MAGIC) {
            ModNet.Receive(p);
         } else if (nMagic == ModLobby._MAGIC) {
            ModLobby.Receive(p);
         } else if (nMagic == _TEST_MAGIC) {
            TestReceive(p, lNow);
         }
      }

      TestFrame(lNow);
      ModLobby.Frame();
      ModNet.Frame();
   }

   static void LoadConfig() {
      if (new File(_COMMON_FILE).exists()) {
         for (ParseKeyword k = GameObject.ParseFile(_COMMON_FILE); k != null; k = k.GetNext()) {
            if (k.Equals("Server")) {
               m_sServerHost = k.GetSParam(0);
               m_nServerPort = k.GetIParam(1);
            }

            if (k.Equals("Ranked")) {
               m_nRankedLaps = Math.max(1, Math.min(10, k.GetIParam(0)));
               m_nRankedAi = Math.max(ModOptions._AI_AUTO, Math.min(ModOptions._AI_MAX, k.GetIParam(1)));
            }
         }
      }

      if (!new File(_LOCAL_FILE).exists()) {
         Log(": pas de " + _LOCAL_FILE + ", pas de socket");
         return;
      }

      for (ParseKeyword k = GameObject.ParseFile(_LOCAL_FILE); k != null; k = k.GetNext()) {
         if (k.Equals("NetPort")) {
            m_nLocalPort = Math.max(0, Math.min(65535, k.GetIParam(0)));
            m_bWantSocket = true;
         }

         if (k.Equals("NetAuto")) {
            m_sAuto = k.GetSParam(0);
            m_bWantSocket = true;
         }

         if (k.Equals("NetPeer")) {
            m_sPeerHost = k.GetSParam(0);
            m_nPeerPort = k.GetIParam(1);
         }

         if (k.Equals("NetPlayers")) {
            ModNet.m_nWantPlayers = Math.max(2, Math.min(ModNet._MAX_PLAYERS, k.GetIParam(0)));
         }

         if (k.Equals("NetTrack")) {
            ModNet.m_nCfgTrack = Math.max(0, Math.min(30, k.GetIParam(0)));
         }

         if (k.Equals("NetLaps")) {
            ModNet.m_nCfgLaps = Math.max(1, Math.min(10, k.GetIParam(0)));
         }

         if (k.Equals("NetBonus")) {
            ModNet.m_bCfgBonus = k.GetIParam(0) != 0;
         }

         if (k.Equals("NetAnimal")) {
            ModNet.m_nCfgAnimal = Math.max(0, Math.min(11, k.GetIParam(0)));
         }

         if (k.Equals("NetRoom")) {
            m_sRoom = k.GetSParam(0);
         }
         if (k.Equals("NetTest")) {
            m_sTestHost = k.GetSParam(0);
            m_nTestPort = k.GetIParam(1);
            m_bWantSocket = true;
         }
      }

      Log(": " + _LOCAL_FILE + " port local " + m_nLocalPort + (m_sTestHost != null ? ", test vers " + m_sTestHost + ":" + m_nTestPort : "")
         + (m_sAuto != null ? ", course auto " + m_sAuto + (m_sRoom != null ? " par le serveur " + m_sServerHost + ":" + m_nServerPort + ", salon " + m_sRoom : m_sAuto.equals("join") ? " vers " + m_sPeerHost + ":" + m_nPeerPort : "") : ""));
   }

   // pas de l'horloge Java (System.currentTimeMillis) : 1 ms si le moteur a réglé la minuterie de Windows, sinon ~16
   static void LogClock() {
      long lStart = System.currentTimeMillis();
      long lPrev = lStart;
      long lMinStep = 1000L;
      int nSteps = 0;

      for (long l = lStart; l - lStart < 50L; l = System.currentTimeMillis()) {
         if (l != lPrev) {
            lMinStep = Math.min(lMinStep, l - lPrev);
            lPrev = l;
            nSteps++;
         }
      }

      Log(": horloge Java, pas de " + lMinStep + " ms (" + nSteps + " pas en 50 ms)");
   }

   // ------------------------------------------------------------------ test de faisabilité (phase 0)
   // Paquet : magique "PRN0", type (1 ping, 2 réponse), identifiant du jeu qui a pingé, numéro, heure d'envoi.
   // Un ping qui revient avec notre identifiant = renvoyé tel quel par echo_test.py ; un ping d'un autre identifiant
   // = l'autre jeu nous teste, on lui répond.

   static final int _TEST_MAGIC = 0x50524E30; // "PRN0"
   static final int _TEST_PING = 1;
   static final int _TEST_PONG = 2;
   static final int _TEST_WANTED = 10; // réponses voulues
   static final int _TEST_MAX_SENT = 120; // abandon après 2 minutes
   static String m_sTestHost = null;
   static int m_nTestPort = 0;
   static ModAddr m_TestAddr = null;
   static int m_nTestId = 0;
   static int m_nTestSent = 0;
   static int m_nTestReplies = 0;
   static long m_lTestNext = 0L;
   static long m_lTestLastSend = 0L;
   static long m_lRttMin = Long.MAX_VALUE;
   static long m_lRttMax = 0L;
   static long m_lRttSum = 0L;
   static long m_lQueueWaitMax = 0L;
   static int m_nPingsAnswered = 0;
   static boolean m_bTestDone = false;
   // images pendant le test, et plus grand écart entre deux images (un fil qui gênerait le jeu se verrait ici)
   static int m_nFrames = 0;
   static long m_lLastFrame = 0L;
   static long m_lFrameGapMax = 0L;

   static void TrackFrame(long lNow) {
      if (m_lLastFrame != 0L) {
         m_lFrameGapMax = Math.max(m_lFrameGapMax, lNow - m_lLastFrame);
      }

      m_lLastFrame = lNow;
      m_nFrames++;
   }

   static void TestFrame(long lNow) {
      if (m_sTestHost == null || m_bTestDone) {
         return;
      }

      if (m_TestAddr == null) {
         m_TestAddr = ModAddr.Resolve(m_sTestHost, m_nTestPort);
         if (m_TestAddr == null) {
            m_bTestDone = true;
            return;
         }

         m_nTestId = (int)(lNow ^ (long)m_Udp.GetLocalPort() << 16) | 1;
         m_lTestNext = lNow;
         Log("test : pings vers " + m_TestAddr + " (identifiant " + m_nTestId + ")");
      }

      if (m_nTestReplies >= _TEST_WANTED || m_nTestSent >= _TEST_MAX_SENT) {
         if (lNow >= m_lTestLastSend + 2000L) {
            TestSummary();
         }

         return;
      }

      if (lNow >= m_lTestNext) {
         m_Out.Clear();
         m_Out.PutInt(_TEST_MAGIC);
         m_Out.PutByte(_TEST_PING);
         m_Out.PutInt(m_nTestId);
         m_Out.PutInt(m_nTestSent);
         m_Out.PutLong(lNow);
         m_Udp.Send(m_TestAddr, m_Out);
         m_nTestSent++;
         m_lTestLastSend = lNow;
         m_lTestNext = lNow + 1000L;
      }
   }

   static void TestReceive(ModPacket p, long lNow) {
      int nType = m_In.GetByte();
      int nId = m_In.GetInt();
      int nSeq = m_In.GetInt();
      long lSent = m_In.GetLong();
      if (m_In.m_bBad) {
         return;
      }

      if (nType == _TEST_PING && nId != m_nTestId) {
         // l'autre jeu nous teste : réponse avec le même contenu
         m_Out.Clear();
         m_Out.PutInt(_TEST_MAGIC);
         m_Out.PutByte(_TEST_PONG);
         m_Out.PutInt(nId);
         m_Out.PutInt(nSeq);
         m_Out.PutLong(lSent);
         m_Udp.Send(p.m_From, m_Out);
         if (m_nPingsAnswered++ == 0) {
            Log("test : premier ping recu de " + p.m_From + ", on repond");
         }

         return;
      }

      if (nId != m_nTestId || m_bTestDone) {
         return;
      }

      long lRtt = p.m_lTime - lSent;
      long lWait = lNow - p.m_lTime; // temps passé dans la file avant que le jeu le lise
      m_nTestReplies++;
      m_lRttMin = Math.min(m_lRttMin, lRtt);
      m_lRttMax = Math.max(m_lRttMax, lRtt);
      m_lRttSum += lRtt;
      m_lQueueWaitMax = Math.max(m_lQueueWaitMax, lWait);
      Log("test : reponse " + nSeq + " de " + p.m_From + (nType == _TEST_PING ? " (echo)" : " (jeu)") + " en " + lRtt + " ms, lue " + lWait + " ms apres reception");
   }

   static void TestSummary() {
      m_bTestDone = true;
      String sRtt = m_nTestReplies > 0 ? "aller-retour " + m_lRttMin + "/" + m_lRttSum / m_nTestReplies + "/" + m_lRttMax + " ms (min/moy/max)" : "aucune reponse";
      Log("test fini : " + m_nTestReplies + "/" + m_nTestSent + " reponses, " + sRtt + ", attente en file " + m_lQueueWaitMax + " ms au plus, " + m_nFrames + " images, plus grand ecart entre images " + m_lFrameGapMax + " ms, recus " + m_Udp.m_nReceived + ", jetes " + m_Udp.m_nDropped + ", erreurs de reception " + m_Udp.m_nRecvErrors + ", pings de l'autre jeu " + m_nPingsAnswered);
   }
}
