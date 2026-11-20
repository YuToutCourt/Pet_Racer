import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

// [mod] Dialogue avec le serveur des salons (chantier 8, phase 3, notes/online.md ; serveur : server\protocol.py).
// Le jeu se présente (HELLO), crée un salon ou en rejoint un par son code, puis redit son état 4 fois par seconde
// (STATE, 1 fois en course) : une perte se rattrape au paquet suivant, rien n'a besoin d'être fiable ici.
// Les paquets de course de ModNet ne changent pas : envoyés à une adresse « place N » (ModAddr fictive), ModUdp les
// emballe ici dans un RELAY vers le serveur, qui les rend dans un FROM ; ils repartent alors vers ModNet.Receive
// avec l'adresse « place de l'expéditeur ». ModNet garde ainsi son étoile hôte <-> invités.
// Phase 4 : partie rapide (QUEUE redit chaque seconde jusqu'à ce que le serveur nous mette dans un salon « classé »),
// résultat de la course classée (RESULT redit jusqu'à un RATED définitif), classement (TOP -> TOP_LIST).
class ModLobby {
   static final int _MAGIC = 0x50525331; // "PRS1"
   static final int _LOBBY_PROTO = 2; // protocol.LOBBY_PROTO
   static final int _T_HELLO = 16;
   static final int _T_CREATE = 17;
   static final int _T_JOIN = 18;
   static final int _T_LEAVE = 19;
   static final int _T_STATE = 20;
   static final int _T_QUEUE = 21;
   static final int _T_RESULT = 22;
   static final int _T_TOP = 23;
   static final int _T_RELAY = 24;
   static final int _T_INFO = 32;
   static final int _T_PEER = 33;
   static final int _T_FROM = 35;
   static final int _T_RATED = 36;
   static final int _T_TOP_LIST = 37;
   static final int _STATUS_OK = 0;
   static final int _STATUS_BAD_PROTO = 1;
   static final int _STATUS_UNKNOWN = 2;
   static final int _ROOM_LOCKED = 1;
   static final int _ROOM_RANKED = 2;
   static final int _RESULT_ABANDON = 1;
   static final int _RATED_NONE = 0;
   static final int _RATED_PENDING = 1;
   static final int _RATED_DONE = 2;
   static final int _RATED_VOID = 3;
   static final int _TOP_MAX = 10; // protocol.TOP_COUNT
   static final String[] _JOIN_ERRORS = new String[]{"", "code inconnu", "salon plein", "autre version du mod", "course en cours"};
   static final String _ACCOUNT_FILE = "online.dat";

   static final int _L_OFF = 0; // pas de serveur
   static final int _L_WAIT = 1; // test automatique : attend le menu principal (profil choisi) avant de se présenter
   static final int _L_HELLO = 2;
   static final int _L_ENTER = 3; // crée ou rejoint le salon
   static final int _L_ROOM = 4;
   static final int _L_IDLE = 5; // écran « En ligne » : connecté, pas de salon (le joueur choisit)
   static final int _L_QUEUE = 6; // partie rapide : en file d'attente

   static ModUdp m_Udp = null;
   static ModAddr m_Server = null;
   static final ModBuf m_Out = new ModBuf(ModUdp._MAX_PACKET);
   static final ModBuf m_In = new ModBuf(0);
   static int m_nState = _L_OFF;
   static boolean m_bAutoMode = false; // test automatique (NetRoom de nettest.scr) : entre seul dans le salon
   static boolean m_bCreate = false; // hôte : crée le salon ; sinon le rejoint par son code
   static String m_sCode = "";
   static int m_nOnline = 0; // joueurs connectés au serveur (dernier INFO)
   static int m_nClientId = 0;
   static int m_nSeq = 0;
   static long m_lNextSend = 0L;
   static long m_lLastInfo = 0L;
   static String m_sToken = null;
   static String m_sTokenName = null;

   // salon, d'après le dernier INFO
   static String m_sRoom = "";
   static int m_nMySlot = 255;
   static int m_nHostSlot = 255;
   static String[] m_Names = new String[ModNet._MAX_PLAYERS];
   static int m_nLastJoinError = 0;
   static boolean m_bRanked = false; // salon d'une partie rapide (course classée)
   static boolean m_bRankedCancelled = false; // partie rapide trouvée mais jamais lancée (montré à l'accueil)
   static long m_lRoomSince = 0L; // entrée dans le salon (partie rapide : lancement automatique)

   // [mod] phase 4 : compte (redit dans chaque INFO hors salon), file d'attente
   static int m_nQueue = 0; // joueurs en file d'attente sur le serveur
   static long m_lQueueSince = 0L;
   static int m_nElo = 0;
   static int m_nGames = 0;
   static int m_nRank = 0; // 0 : pas encore classé
   static int m_nRankedTotal = 0;
   static boolean m_bAccountKnown = false;

   // résultat de ma dernière course classée
   static boolean m_bResultSending = false;
   static long m_lResultUntil = 0L;
   static long m_lNextResult = 0L;
   static int m_nResultFlags = 0;
   static int[] m_ResultOrder = new int[0];
   static int m_nRatedState = _RATED_NONE;
   static int m_nRatedPlace = 0;
   static int m_nRatedHumans = 0;
   static int m_nRatedDelta = 0;
   static int m_nRatedElo = 0;
   static int m_nRatedRank = 0;

   // classement (page « Classement » de l'écran)
   static boolean m_bWantTop = false;
   static long m_lNextTop = 0L;
   static int m_nTopCount = -1; // -1 : pas encore reçu
   static int m_nTopTotal = 0;
   static String[] m_TopNames = new String[_TOP_MAX];
   static int[] m_TopElo = new int[_TOP_MAX];
   static int[] m_TopGames = new int[_TOP_MAX];

   // statistiques
   static int m_nRelaySent = 0;
   static int m_nRelayRecv = 0;
   static boolean m_bSilentLogged = false;

   static void Log(String s) {
      GameObject.CrashLog("[mod] net salon : " + s + "\n");
   }

   // ModOnline.Init : sRoom = code du salon (NetRoom de nettest.scr), bHost = NetAuto("host")
   static void Init(ModUdp udp, String sHost, int nPort, String sRoom, boolean bHost) {
      m_Udp = udp;
      m_Server = ModAddr.Resolve(sHost, nPort);
      if (m_Server == null) {
         return;
      }

      m_bAutoMode = true;
      m_bCreate = bHost;
      m_sCode = sRoom.trim().toUpperCase();
      m_nClientId = (int)(System.currentTimeMillis() ^ (long)udp.GetLocalPort() << 16 ^ new Random().nextInt()) | 1;
      m_nState = _L_WAIT;
      Log("serveur " + m_Server + ", " + (bHost ? "cree le salon" : "rejoint le salon " + m_sCode));
   }

   static boolean IsActive() {
      return m_nState != _L_OFF;
   }

   static boolean InRoom() {
      return m_nState == _L_ROOM;
   }

   // --- écran « En ligne » (KidsOnlineMenuWindow) ---

   // ouverture de l'écran : se présente au serveur (profil déjà choisi)
   static void Connect(ModUdp udp, String sHost, int nPort) {
      if (m_nState != _L_OFF) {
         return;
      }

      m_Udp = udp;
      m_Server = ModAddr.Resolve(sHost, nPort);
      if (m_Server == null) {
         return;
      }

      if (m_nClientId == 0) {
         m_nClientId = (int)(System.currentTimeMillis() ^ (long)udp.GetLocalPort() << 16 ^ new Random().nextInt()) | 1;
      }

      m_bAutoMode = false;
      m_nLastJoinError = 0;
      m_nState = _L_HELLO;
      m_lNextSend = 0L;
      m_lLastInfo = System.currentTimeMillis();
      m_bSilentLogged = false;
      Log("connexion a " + m_Server);
   }

   static void Create() {
      if (m_nState == _L_IDLE) {
         m_bCreate = true;
         m_nLastJoinError = 0;
         m_nState = _L_ENTER;
         m_lNextSend = 0L;
      }
   }

   static void Join(String sCode) {
      if (m_nState == _L_IDLE) {
         m_bCreate = false;
         m_sCode = sCode.trim().toUpperCase();
         m_nLastJoinError = 0;
         m_nState = _L_ENTER;
         m_lNextSend = 0L;
      }
   }

   // [mod] phase 4 : partie rapide. Le serveur réunit des joueurs d'Elo proches et nous met dans un salon classé
   static void Queue() {
      if (m_nState == _L_IDLE) {
         m_nState = _L_QUEUE;
         m_nLastJoinError = 0;
         m_lQueueSince = System.currentTimeMillis();
         m_lNextSend = 0L;
         m_nRatedState = _RATED_NONE; // le résultat de la course d'avant n'est plus affiché
         m_bRankedCancelled = false;
         Log("partie rapide : en file d'attente");
      }
   }

   static boolean InQueue() {
      return m_nState == _L_QUEUE;
   }

   static void CancelQueue() {
      if (m_nState == _L_QUEUE) {
         SendLobby(_T_LEAVE);
         m_nState = _L_IDLE;
         m_lNextSend = 0L;
         Log("partie rapide : file d'attente quittee");
      }
   }

   // Retour dans le salon : on en sort, la session de course s'arrête (le serveur donne l'hôte à un autre)
   static void LeaveRoom() {
      CancelQueue();
      if (m_nState == _L_ROOM || m_nState == _L_ENTER) {
         Leave();
         ModNet.StopSession();
         m_nState = _L_IDLE;
         m_lNextSend = 0L;
      }
   }

   // [mod] phase 4 : fin de ma course classée (podium, ou course quittée) ; redit chaque seconde jusqu'à la réponse
   // définitive du serveur, même après la sortie du salon. order : places des humains arrivés, dans l'ordre
   static void SendResult(boolean bAbandon, int[] order) {
      if (m_bResultSending || m_nState == _L_OFF) {
         return;
      }

      m_bResultSending = true;
      m_nResultFlags = bAbandon ? _RESULT_ABANDON : 0;
      m_ResultOrder = order;
      m_lNextResult = 0L;
      m_lResultUntil = System.currentTimeMillis() + 180000L;
      m_nRatedState = _RATED_PENDING;
      String s = "";

      for (int i = 0; i < order.length; i++) {
         s = s + " " + order[i];
      }

      Log("course classee : " + (bAbandon ? "abandon" : "ordre d'arrivee des humains" + s));
      FrameResult(System.currentTimeMillis());
   }

   static void FrameResult(long lNow) {
      if (!m_bResultSending || lNow < m_lNextResult || m_nState == _L_OFF || m_nState == _L_HELLO || m_nState == _L_WAIT) {
         return;
      }

      if (lNow > m_lResultUntil) {
         m_bResultSending = false;
         Log("course classee : pas de reponse definitive du serveur, abandon de l'envoi");
         return;
      }

      m_lNextResult = lNow + 1000L;
      BeginLobby(_T_RESULT);
      m_Out.PutByte(m_nResultFlags);
      m_Out.PutByte(m_ResultOrder.length);

      for (int i = 0; i < m_ResultOrder.length; i++) {
         m_Out.PutByte(m_ResultOrder[i]);
      }

      m_Udp.Send(m_Server, m_Out);
   }

   // une course classée pas encore partie se lance-t-elle seule ? (ModNet, hôte et invités)
   static boolean IsRankedRoom() {
      return m_nState == _L_ROOM && m_bRanked;
   }

   // Retour sur la page d'accueil de l'écran : plus de serveur (il nous oublie après son délai)
   static void Disconnect() {
      LeaveRoom();
      if (m_nState != _L_OFF) {
         Log("deconnexion");
      }

      m_nState = _L_OFF;
   }

   // la session de course doit suivre le salon : hôte si c'est ma place, sinon invité de la place de l'hôte.
   // Jamais pendant une course : l'hôte parti en course est remplacé par ModNet lui-même (ModNet.HostLeftRace), et
   // l'accord avec le salon se refait au retour à l'écran.
   static void SyncNet() {
      if (m_nState != _L_ROOM || m_nMySlot >= ModNet._MAX_PLAYERS || m_nHostSlot >= ModNet._MAX_PLAYERS) {
         return;
      }

      if (ModNet.m_nState == ModNet._ST_LOADING || ModNet.m_nState == ModNet._ST_RACE) {
         return;
      }

      boolean bHost = m_nMySlot == m_nHostSlot;
      if (bHost) {
         if (ModNet.m_nRole != ModNet._ROLE_HOST || ModNet.m_nMySlot != m_nMySlot) {
            ModNet.StartHost(m_nMySlot);
         }
      } else if (ModNet.m_nRole != ModNet._ROLE_GUEST || ModNet.m_nHostSlot != m_nHostSlot || ModNet.m_nState == ModNet._ST_DONE) {
         ModNet.StartGuestRelay(m_nHostSlot);
      }
   }

   // ModOnline.Frame, à chaque image
   static void Frame() {
      if (m_nState == _L_OFF) {
         return;
      }

      long lNow = System.currentTimeMillis();
      if (m_nState == _L_WAIT) {
         if (!ModNet.AtMainMenu()) {
            return;
         }

         m_nState = _L_HELLO;
         m_lLastInfo = lNow;
      }

      if (lNow - m_lLastInfo > 6000L && !m_bSilentLogged) {
         m_bSilentLogged = true;
         Log("le serveur ne repond plus depuis 6 s");
      }

      SyncNet();
      FrameResult(lNow);
      if (m_bWantTop && lNow >= m_lNextTop && m_nState != _L_HELLO) {
         SendLobby(_T_TOP);
         m_lNextTop = lNow + 5000L;
      }

      if (lNow < m_lNextSend) {
         return;
      }

      if (m_nState == _L_HELLO) {
         SendHello();
         m_lNextSend = lNow + 1000L;
      } else if (m_nState == _L_IDLE) {
         SendHello(); // rien d'autre à dire : le serveur ne nous oublie pas, et redit le nombre de joueurs en ligne
         m_lNextSend = lNow + 2000L;
      } else if (m_nState == _L_QUEUE) {
         SendLobby(_T_QUEUE);
         m_lNextSend = lNow + 1000L;
      } else if (m_nState == _L_ENTER) {
         if (m_bCreate) {
            SendLobby(_T_CREATE);
         } else {
            BeginLobby(_T_JOIN);
            m_Out.PutString(m_sCode);
            m_Udp.Send(m_Server, m_Out);
         }

         m_lNextSend = lNow + (m_nLastJoinError != 0 ? 2000L : 250L);
      } else {
         SendState();
         m_lNextSend = lNow + (ModNet.m_nState == ModNet._ST_RACE ? 1000L : 250L);
      }
   }

   static void BeginLobby(int nType) {
      m_Out.Clear();
      m_Out.PutInt(_MAGIC);
      m_Out.PutByte(nType);
      m_Out.PutInt(m_nClientId);
      m_Out.PutInt(++m_nSeq);
   }

   static void SendLobby(int nType) {
      BeginLobby(nType);
      m_Udp.Send(m_Server, m_Out);
   }

   static void SendHello() {
      String sName = ModNet.LocalName();
      BeginLobby(_T_HELLO);
      m_Out.PutShort(_LOBBY_PROTO);
      m_Out.PutInt(ModNet._VERSION);
      m_Out.PutString(sName);
      m_Out.PutString(Token(sName));
      m_Udp.Send(m_Server, m_Out);
   }

   // hôte : salon fermé aux arrivants dès que la course est lancée. Contenu recopié aux autres : vide pour l'instant
   // (l'écran « En ligne » y mettra animal, prêt, circuit…)
   static void SendState() {
      boolean bLock = m_nMySlot == m_nHostSlot && (ModNet.m_nState == ModNet._ST_LOADING || ModNet.m_nState == ModNet._ST_RACE || ModNet.m_nState == ModNet._ST_DONE);
      BeginLobby(_T_STATE);
      m_Out.PutByte(bLock ? _ROOM_LOCKED : 0);
      m_Udp.Send(m_Server, m_Out);
   }

   static void Leave() {
      if (m_nState == _L_ROOM) {
         for (int i = 0; i < 3; i++) {
            SendLobby(_T_LEAVE);
         }

         Log("quitte le salon " + m_sRoom + " (" + m_nRelaySent + " paquets de course envoyes, " + m_nRelayRecv + " recus)");
      }

      m_nState = _L_OFF;
      ClearRoom();
   }

   static void ClearRoom() {
      m_sRoom = "";
      m_nMySlot = 255;
      m_nHostSlot = 255;
      m_bRanked = false;
      m_Names = new String[ModNet._MAX_PLAYERS];
   }

   // ModUdp.Send vers une adresse « place N » : emballé pour le serveur
   static boolean SendRelay(int nSlot, byte[] data, int nLen) {
      if (m_nState != _L_ROOM || nLen > ModUdp._MAX_PACKET - 20) {
         return false;
      }

      BeginLobby(_T_RELAY);
      m_Out.PutByte(nSlot);
      System.arraycopy(data, 0, m_Out.m_Data, m_Out.m_nPos, nLen);
      m_Out.m_nPos += nLen;
      m_nRelaySent++;
      return m_Udp.Send(m_Server, m_Out);
   }

   // ModOnline.Frame, paquet « PRS1 » (magique déjà lue)
   static void Receive(ModPacket pk) {
      if (m_nState == _L_OFF || !pk.m_From.equals(m_Server)) {
         return;
      }

      m_In.Wrap(pk.m_Data, pk.m_nLen);
      m_In.GetInt();
      int nType = m_In.GetByte();
      if (nType == _T_INFO) {
         OnInfo(pk);
      } else if (nType == _T_FROM) {
         int nSlot = m_In.GetByte();
         if (m_In.m_bBad || m_nState != _L_ROOM || nSlot >= ModNet._MAX_PLAYERS) {
            return;
         }

         ModPacket inner = new ModPacket();
         inner.m_From = new ModAddr(nSlot);
         inner.m_nLen = m_In.Remaining();
         inner.m_Data = new byte[inner.m_nLen];
         System.arraycopy(pk.m_Data, m_In.m_nPos, inner.m_Data, 0, inner.m_nLen);
         inner.m_lTime = pk.m_lTime;
         m_nRelayRecv++;
         m_In.Wrap(inner.m_Data, inner.m_nLen);
         if (m_In.GetInt() == ModNet._MAGIC) {
            ModNet.Receive(inner);
         }
      } else if (nType == _T_RATED) {
         OnRated();
      } else if (nType == _T_TOP_LIST) {
         OnTopList();
      }
      // _T_PEER : pas lu (l'écran « En ligne » montre ce que dit l'hôte dans les INFO de ModNet)
   }

   static void OnRated() {
      m_In.GetInt(); // n° du paquet
      int nState = m_In.GetByte();
      int nPlace = m_In.GetByte();
      int nHumans = m_In.GetByte();
      int nDelta = m_In.GetShort();
      int nElo = m_In.GetShort();
      int nRank = m_In.GetShort();
      if (m_In.m_bBad || !m_bResultSending) {
         return;
      }

      m_nRatedState = nState;
      if (nState == _RATED_PENDING) {
         return; // on redit le résultat jusqu'à la décision
      }

      m_bResultSending = false;
      m_nRatedPlace = nPlace;
      m_nRatedHumans = nHumans;
      m_nRatedDelta = nDelta;
      m_nRatedElo = nElo;
      m_nRatedRank = nRank;
      if (nState == _RATED_DONE) {
         m_nElo = nElo;
         m_nRank = nRank;
         Log("course classee comptee : " + nPlace + "e sur " + nHumans + ", Elo " + (nDelta >= 0 ? "+" : "") + nDelta + " -> " + nElo + ", rang " + nRank);
      } else {
         Log("course classee pas comptee (" + (nState == _RATED_VOID ? "resultats differents ou trop peu de joueurs" : "inconnue du serveur") + ")");
      }
   }

   static void OnTopList() {
      int nTotal = m_In.GetShort();
      int nCount = Math.min(_TOP_MAX, m_In.GetByte());
      String[] names = new String[_TOP_MAX];
      int[] elo = new int[_TOP_MAX];
      int[] games = new int[_TOP_MAX];

      for (int i = 0; i < nCount; i++) {
         names[i] = m_In.GetString();
         elo[i] = m_In.GetShort();
         games[i] = m_In.GetShort();
      }

      if (m_In.m_bBad) {
         return;
      }

      m_nTopTotal = nTotal;
      m_TopNames = names;
      m_TopElo = elo;
      m_TopGames = games;
      m_nTopCount = nCount;
   }

   static void OnInfo(ModPacket pk) {
      int nSeq = m_In.GetInt();
      int nStatus = m_In.GetByte();
      if (m_In.m_bBad) {
         return;
      }

      m_lLastInfo = pk.m_lTime;
      if (m_bSilentLogged) {
         m_bSilentLogged = false;
         Log("le serveur repond de nouveau");
      }

      if (nStatus == _STATUS_BAD_PROTO) {
         Log("le serveur n'a pas la meme version du dialogue (" + _LOBBY_PROTO + " ici) : abandon");
         m_nState = _L_OFF;
         return;
      }

      if (nStatus == _STATUS_UNKNOWN) {
         if (m_nState != _L_HELLO) {
            Log("le serveur ne nous connait plus (relance ?) : on se presente de nouveau");
            m_nState = _L_HELLO;
            m_lNextSend = 0L;
         }

         return;
      }

      String sCode = m_In.GetString();
      int nMySlot = m_In.GetByte();
      int nHostSlot = m_In.GetByte();
      int nRoomFlags = m_In.GetByte();
      int nJoinError = m_In.GetByte();
      int nOnline = m_In.GetShort();
      String[] names = new String[ModNet._MAX_PLAYERS];

      for (int i = 0; i < ModNet._MAX_PLAYERS; i++) {
         names[i] = m_In.GetByte() != 0 ? m_In.GetString() : null;
      }

      // [mod] phase 4 : file d'attente, et mon compte (seulement hors salon)
      int nQueue = m_In.GetShort();
      int nElo = m_In.GetShort();
      int nGames = m_In.GetShort();
      int nRank = m_In.GetShort();
      int nRankedTotal = m_In.GetShort();
      if (m_In.m_bBad) {
         return;
      }

      m_nOnline = nOnline;
      m_nQueue = nQueue;
      if (sCode.length() == 0 && nElo > 0) {
         m_nElo = nElo;
         m_nGames = nGames;
         m_nRank = nRank;
         m_nRankedTotal = nRankedTotal;
         m_bAccountKnown = true;
      }

      boolean bRanked = (nRoomFlags & _ROOM_RANKED) != 0;
      if (bRanked && (m_nState == _L_IDLE || m_nState == _L_HELLO)) {
         // course trouvée alors que le joueur avait quitté la file : on n'y va pas
         SendLobby(_T_LEAVE);
         return;
      }

      if (m_nState == _L_QUEUE && sCode.length() == 0) {
         return; // toujours en attente
      }
      if (m_nState == _L_HELLO) {
         Log("connecte au serveur (" + nOnline + " joueur(s) en ligne)");
         m_nState = m_bAutoMode ? _L_ENTER : _L_IDLE;
         m_lNextSend = 0L;
         if (sCode.length() == 0) {
            return;
         }
      }

      if (m_nState == _L_IDLE && sCode.length() == 0) {
         return;
      }

      if (m_nState == _L_ENTER && nJoinError != 0) {
         if (nJoinError != m_nLastJoinError) {
            Log("entree refusee dans " + m_sCode + " : " + (nJoinError < _JOIN_ERRORS.length ? _JOIN_ERRORS[nJoinError] : "" + nJoinError));
         }

         m_nLastJoinError = nJoinError;
         if (!m_bAutoMode) {
            m_nState = _L_IDLE; // l'écran affiche la raison ; le joueur réessaie s'il veut
            return;
         }
      }

      if (sCode.length() == 0) {
         if (m_nState == _L_ROOM) {
            Log("plus dans le salon " + m_sRoom);
            ClearRoom();
            if (m_bAutoMode) {
               m_nState = _L_ENTER;
            } else {
               ModNet.StopSession();
               m_nState = _L_IDLE;
            }
         }

         return;
      }

      if (m_nState != _L_ROOM) {
         if (bRanked) {
            Log("partie rapide trouvee apres " + (System.currentTimeMillis() - m_lQueueSince) / 1000L + " s d'attente");
         }

         m_nState = _L_ROOM;
         m_nLastJoinError = 0;
         m_lNextSend = 0L;
         m_nRelaySent = 0;
         m_nRelayRecv = 0;
         m_lRoomSince = System.currentTimeMillis();
         Log("salon " + sCode + (bRanked ? " (partie rapide, classee)" : "") + " : place " + nMySlot + ", hote en place " + nHostSlot);
      }

      m_bRanked = bRanked;

      if (nHostSlot != m_nHostSlot && m_nHostSlot != 255 && m_sRoom.equals(sCode)) {
         Log("salon " + sCode + " : l'hote est maintenant en place " + nHostSlot);
      }

      for (int i = 0; i < ModNet._MAX_PLAYERS; i++) {
         if (names[i] != null && m_Names[i] == null && i != nMySlot) {
            Log("salon " + sCode + " : " + names[i] + " en place " + i);
         } else if (names[i] == null && m_Names[i] != null && i != m_nMySlot) {
            Log("salon " + sCode + " : " + m_Names[i] + " (place " + i + ") sort");
         }
      }

      m_sRoom = sCode;
      m_nMySlot = nMySlot;
      m_nHostSlot = nHostSlot;
      m_Names = names;
      // l'hôte, entre deux courses : un joueur sorti du salon libère tout de suite sa place de course
      if (ModNet.m_nRole == ModNet._ROLE_HOST && ModNet.m_nState == ModNet._ST_LOBBY) {
         for (int i = 0; i < ModNet._MAX_PLAYERS; i++) {
            if (names[i] == null && i != ModNet.m_nMySlot && ModNet.m_Players[i] != null) {
               ModNet.m_Players[i] = null;
            }
         }
      }

      SyncNet(); // ModNet lancé (hôte ou invité), ou relancé si l'hôte a changé
   }

   // --- compte : un jeton par profil dans online.dat (« profil;jeton » par ligne), comme pet_soccer ---

   static String Token(String sName) {
      if (m_sToken != null && sName.equals(m_sTokenName)) {
         return m_sToken;
      }

      m_sTokenName = sName;
      m_sToken = null;
      StringBuffer all = new StringBuffer();

      try {
         if (new File(_ACCOUNT_FILE).exists()) {
            BufferedReader r = new BufferedReader(new FileReader(_ACCOUNT_FILE));

            for (String line = r.readLine(); line != null; line = r.readLine()) {
               int k = line.lastIndexOf(';');
               if (k > 0 && line.substring(0, k).equals(sName)) {
                  m_sToken = line.substring(k + 1).trim();
               }

               if (line.trim().length() > 0) {
                  all.append(line).append("\r\n");
               }
            }

            r.close();
         }
      } catch (Throwable e) {
         Log("lecture de " + _ACCOUNT_FILE + " impossible (" + e + ")");
      }

      if (m_sToken == null) {
         String sChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
         Random rnd = new Random(System.currentTimeMillis() ^ (long)m_nClientId << 7);
         StringBuffer t = new StringBuffer();

         for (int i = 0; i < 32; i++) {
            t.append(sChars.charAt(rnd.nextInt(sChars.length())));
         }

         m_sToken = t.toString();
         all.append(sName).append(';').append(m_sToken).append("\r\n");

         try {
            FileWriter w = new FileWriter(_ACCOUNT_FILE);
            w.write(all.toString());
            w.close();
            Log("compte cree pour le profil " + sName + " (" + _ACCOUNT_FILE + ")");
         } catch (Throwable e) {
            Log("ecriture de " + _ACCOUNT_FILE + " impossible (" + e + ")");
         }
      }

      return m_sToken;
   }
}
