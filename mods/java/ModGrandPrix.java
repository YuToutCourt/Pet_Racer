import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

// [mod] Grand Prix (chantier 6, notes/grandprix.md) : une coupe de 4 courses tirées au hasard (monde au hasard parmi
// les mondes débloqués, Royaume du Lion compris, puis une variante débloquée de ce monde, jamais deux fois la même),
// mêmes adversaires et même nombre de tours d'un bout à l'autre, points selon l'ordre d'arrivée réel (chantier 2).
// La coupe tourne en contexte « Course » (EMenuCX._RACE) : les courses se jouent exactement comme une course simple ;
// seuls quatre passages sont détournés (bouton du menu Un joueur, choix de l'animal, fin de course, podium).
// Réglages dans Data\grandprix.scr (nombre de courses, points par place) ; palmarès par profil dans grandprix.dat.
class ModGrandPrix {
   static final String _FILE = "Data/grandprix.scr";
   static final String _SAVE_FILE = "grandprix.dat";
   static final int _MAX_RACES = 8;
   static final int _CARS = 6;
   static final int _TRACKS = 31;
   static final int _WORLDS = 6;
   static final int _ROYAL_TRACK = 30;

   // réglages (Data\grandprix.scr)
   static int m_nRaces = 4;
   static int[] m_aPoints = new int[]{10, 8, 6, 4, 2, 1};

   // bouton Grand Prix pressé : la prochaine course lancée (RacerGame.RunModule) ouvre la coupe
   static boolean m_bPending = false;
   // une coupe est en cours
   static boolean m_bActive = false;

   // coupe en cours
   static int[] m_aTracks = new int[_MAX_RACES];
   static int m_iRace = 0;              // course en cours ou qui vient de se jouer (0..m_nRaces-1)
   static boolean m_bRaceDone = false;  // la course m_iRace a été jouée et comptée
   static int m_nLaps = 3;
   static int m_nCars = 0;
   static int[] m_aAnimals = new int[_CARS];    // ordre de départ : 0 = joueur
   static int[] m_aTotal = new int[_CARS];      // points cumulés, par case de m_aAnimals
   static int[] m_aRacePts = new int[_CARS];    // points de la dernière course
   static int[] m_aRacePlace = new int[_CARS];  // place à la dernière course (0 = premier)
   static int[] m_aWins = new int[_CARS];
   static boolean m_bCheated = false;           // une triche a été active pendant la coupe
   static boolean m_bAbandoned = false;         // la dernière course a été quittée par Échap
   static boolean m_bSaved = false;             // palmarès déjà enregistré pour cette coupe

   // l'aléa du jeu (RacerGame.m_Random) part d'une graine fixe : même tirage à chaque lancement. Ici, l'heure.
   static Random s_Random = new Random();

   // --- réglages ---

   static void Load() {
      if (!new File(_FILE).exists()) {
         GameObject.CrashLog("[mod] grand prix : " + _FILE + " absent, valeurs par defaut\n");
         return;
      }

      ParseKeyword k = GameObject.ParseFile(_FILE);
      if (k == null) {
         GameObject.CrashLog("[mod] grand prix : " + _FILE + " illisible, valeurs par defaut\n");
         return;
      }

      for (; k != null; k = k.GetNext()) {
         if (k.Equals("Races")) {
            int n = Math.round(k.GetFParam(0));
            if (n >= 1 && n <= _MAX_RACES) {
               m_nRaces = n;
            } else {
               GameObject.CrashLog("[mod] grand prix : Races(" + n + ") hors de 1.." + _MAX_RACES + ", ignore\n");
            }
         } else if (k.Equals("Points")) {
            for (int i = 0; i < _CARS; i++) {
               int n = Math.round(k.GetFParam(i));
               m_aPoints[i] = n >= 0 && n <= 100 ? n : 0;
            }
         }
      }

      GameObject.CrashLog("[mod] grand prix : " + _FILE + " lu, " + m_nRaces + " courses, points " + PointsText() + "\n");
   }

   static String PointsText() {
      String s = "";

      for (int i = 0; i < _CARS; i++) {
         s = s + (i > 0 ? "/" : "") + m_aPoints[i];
      }

      return s;
   }

   // --- déroulement ---

   static boolean IsActive() {
      return m_bActive;
   }

   // retour au menu Un joueur : plus de coupe en cours
   static void Reset() {
      if (m_bActive) {
         GameObject.CrashLog("[mod] grand prix : coupe fermee (course " + (m_iRace + 1) + "/" + m_nRaces + ")\n");
      }

      m_bPending = false;
      m_bActive = false;
   }

   // Appelé par RacerGame.RunModule avant de charger le circuit : ouvre la coupe si le bouton Grand Prix l'a
   // demandé, sinon remet les adversaires, les tours et le circuit de la course en cours.
   static void BeforeRace() {
      CoreLogic logic = RacerGame.GetCoreLogic();
      if (m_bPending) {
         m_bPending = false;
         Begin(logic);
      }

      if (!m_bActive) {
         return;
      }

      logic.SetMultiplayer(false);
      logic.SetLapsNo(m_nLaps);
      logic.m_SelectedAnimals = IntList.MakeIntListFromIntArray(m_aAnimals, m_nCars);
      logic.SetNextTrack(m_aTracks[m_iRace]);
      m_bRaceDone = false;
      m_bAbandoned = false;
      if (ModOptions.IsCheat()) {
         m_bCheated = true;
      }

      GameObject.CrashLog("[mod] grand prix : course " + (m_iRace + 1) + "/" + m_nRaces + ", circuit " + m_aTracks[m_iRace] + " ("
         + RacerModule.GetLevelFileName(m_aTracks[m_iRace]) + "), " + m_nLaps + " tours\n");
   }

   static void Begin(CoreLogic logic) {
      IntList sel = logic.GetSelectedAnimals();
      m_nCars = sel != null && sel.m_Cnt <= _CARS ? sel.m_Cnt : 0;

      for (int i = 0; i < m_nCars; i++) {
         m_aAnimals[i] = sel.GetAt(i);
         m_aTotal[i] = 0;
         m_aRacePts[i] = 0;
         m_aRacePlace[i] = i;
         m_aWins[i] = 0;
      }

      m_nLaps = logic.GetLapsNo() > 0 ? logic.GetLapsNo() : 3;
      DrawTracks(logic.GetAvailableTracksCount());
      m_iRace = 0;
      m_bRaceDone = false;
      m_bCheated = false;
      m_bSaved = false;
      m_bActive = m_nCars > 0;
      String s = "";

      for (int i = 0; i < m_nRaces; i++) {
         s = s + " " + m_aTracks[i];
      }

      GameObject.CrashLog("[mod] grand prix : nouvelle coupe, " + m_nCars + " voitures (joueur " + m_aAnimals[0] + "), circuits" + s + "\n");
   }

   // Tirage : un monde au hasard parmi ceux qui ont encore un circuit débloqué pas encore pris (le Royaume du Lion,
   // un seul circuit, compte comme un monde), puis une variante au hasard dans ce monde. Moins de variantes
   // débloquées que de courses : on recommence à piocher dans toutes (répétitions).
   static void DrawTracks(int nAvail) {
      if (nAvail < 1) {
         nAvail = 1;
      }

      if (nAvail > _TRACKS) {
         nAvail = _TRACKS;
      }

      boolean[] used = new boolean[_TRACKS];
      int[] worlds = new int[_WORLDS];
      int[] cand = new int[6];

      for (int r = 0; r < m_nRaces; r++) {
         int nWorlds = 0;

         for (int w = 0; w < _WORLDS; w++) {
            if (CountFree(w, nAvail, used, null) > 0) {
               worlds[nWorlds++] = w;
            }
         }

         if (nWorlds == 0) {
            used = new boolean[_TRACKS];
            r--;
            continue;
         }

         int w = worlds[Rand(nWorlds)];
         int n = CountFree(w, nAvail, used, cand);
         int t = cand[Rand(n)];
         used[t] = true;
         m_aTracks[r] = t;
      }
   }

   // variantes libres du monde w (débloquées et pas encore prises), copiées dans cand si non nul
   static int CountFree(int w, int nAvail, boolean[] used, int[] cand) {
      int n = 0;

      for (int t = w * 6; t < w * 6 + 6 && t < nAvail; t++) {
         if (!used[t]) {
            if (cand != null) {
               cand[n] = t;
            }

            n++;
         }
      }

      return n;
   }

   static int Rand(int n) {
      int i = (int)(s_Random.nextFloat() * n);
      return i >= n ? n - 1 : i;
   }

   // case de m_aAnimals d'un animal, -1 si absent
   static int Slot(int nAnimal) {
      for (int i = 0; i < m_nCars; i++) {
         if (m_aAnimals[i] == nAnimal) {
            return i;
         }
      }

      return -1;
   }

   // Course finie normalement (RacerGame.EndModule) : points d'après CoreLogic.m_TimeList, déjà rempli dans l'ordre
   // d'arrivée (Vehicle.FillTimeList). Une voiture absente de la liste prend la dernière place.
   static void OnRaceFinished() {
      if (!m_bActive || m_bRaceDone) {
         return;
      }

      TimeList list = RacerGame.GetCoreLogic().GetTimeList();
      int[] order = new int[_CARS];
      int n = 0;

      for (int i = 0; list != null && i < list.GetCnt() && n < m_nCars; i++) {
         TimeEntry e = list.GetAt(i);
         int s = e != null ? Slot(e.m_AnimalID) : -1;
         if (s >= 0 && !Contains(order, n, s)) {
            order[n++] = s;
         }
      }

      n = AppendMissing(order, n, -1);
      Score(order, n, false);
   }

   // Échap → quitter en pleine course (RacerGame.EndModule, avant la suppression du circuit) : les IA sont classées
   // dans leur ordre du moment (RaceParams.m_SortTable, refait toutes les 0,1 s), le joueur prend la dernière place.
   static void OnRaceAbandoned(RacerModule mod) {
      if (!m_bActive || m_bRaceDone) {
         return;
      }

      int[] order = new int[_CARS];
      int n = 0;
      if (mod != null && mod.m_RaceParams != null) {
         java.util.Vector v = mod.m_RaceParams.m_SortTable;

         for (int i = 0; i < v.size() && n < m_nCars; i++) {
            Vehicle car = (Vehicle)v.elementAt(i);
            int s = car != null ? Slot(car.m_iAnimalID) : -1;
            if (s > 0 && !Contains(order, n, s)) {
               order[n++] = s;
            }
         }
      }

      n = AppendMissing(order, n, 0);
      order[n++] = 0;
      Score(order, n, true);
   }

   static boolean Contains(int[] t, int n, int v) {
      for (int i = 0; i < n; i++) {
         if (t[i] == v) {
            return true;
         }
      }

      return false;
   }

   // ajoute les cases pas encore classées (sauf nSkip), dans l'ordre de départ
   static int AppendMissing(int[] order, int n, int nSkip) {
      for (int s = 0; s < m_nCars; s++) {
         if (s != nSkip && !Contains(order, n, s)) {
            order[n++] = s;
         }
      }

      return n;
   }

   static void Score(int[] order, int n, boolean bAbandoned) {
      String s = "";

      for (int p = 0; p < n; p++) {
         int slot = order[p];
         int pts = p < m_aPoints.length ? m_aPoints[p] : 0;
         if (bAbandoned && slot == 0) {
            pts = 0;
         }

         m_aRacePlace[slot] = p;
         m_aRacePts[slot] = pts;
         m_aTotal[slot] += pts;
         if (p == 0) {
            m_aWins[slot]++;
         }

         s = s + " " + m_aAnimals[slot] + ":" + pts;
      }

      m_bRaceDone = true;
      m_bAbandoned = bAbandoned;
      if (ModOptions.IsCheat()) {
         m_bCheated = true;
      }

      GameObject.CrashLog("[mod] grand prix : course " + (m_iRace + 1) + "/" + m_nRaces + (bAbandoned ? " abandonnee" : " finie")
         + ", points (animal:pts)" + s + ", joueur " + (m_aRacePlace[0] + 1) + "e, total " + m_aTotal[0] + "\n");
      if (IsOver()) {
         int nPlace = PlayerRank();
         GameObject.CrashLog("[mod] grand prix : coupe finie, joueur " + (nPlace + 1) + "e avec " + m_aTotal[0] + " pts"
            + (m_bCheated ? " (triche, non enregistre)" : "") + "\n");
         SaveResult(nPlace);
      }
   }

   // la dernière course a été jouée
   static boolean IsOver() {
      return m_bActive && m_bRaceDone && m_iRace >= m_nRaces - 1;
   }

   // passe à la course suivante (écran du classement) ; faux si la coupe est finie
   static boolean NextRace() {
      if (!m_bActive || m_iRace >= m_nRaces - 1) {
         return false;
      }

      m_iRace++;
      m_bRaceDone = false;
      return true;
   }

   // classement : cases de m_aAnimals triées par points, puis victoires, puis place à la dernière course
   static int[] GetStandings() {
      int[] t = new int[m_nCars];

      for (int i = 0; i < m_nCars; i++) {
         int j = i;

         while (j > 0 && Better(i, t[j - 1])) {
            t[j] = t[j - 1];
            j--;
         }

         t[j] = i;
      }

      return t;
   }

   static boolean Better(int a, int b) {
      if (m_aTotal[a] != m_aTotal[b]) {
         return m_aTotal[a] > m_aTotal[b];
      }

      if (m_aWins[a] != m_aWins[b]) {
         return m_aWins[a] > m_aWins[b];
      }

      return m_aRacePlace[a] < m_aRacePlace[b];
   }

   // place du joueur au classement (0 = premier)
   static int PlayerRank() {
      int[] t = GetStandings();

      for (int i = 0; i < t.length; i++) {
         if (t[i] == 0) {
            return i;
         }
      }

      return m_nCars - 1;
   }

   // --- palmarès (grandprix.dat) : « profil.Played=n », « profil.Gold=n », « .Silver », « .Bronze » ---

   static final String[] _MEDALS = new String[]{"Gold", "Silver", "Bronze"};

   static String ProfileName() {
      String s = RacerGame.GetCoreLogic().m_Name;
      return s != null ? s.replace('=', '_').replace('\n', '_').replace('\r', '_') : "default";
   }

   static java.util.Vector ReadLines() {
      java.util.Vector v = new java.util.Vector();
      File f = new File(_SAVE_FILE);
      if (!f.exists()) {
         return v;
      }

      try {
         BufferedReader in = new BufferedReader(new FileReader(f));

         for (String sLine = in.readLine(); sLine != null; sLine = in.readLine()) {
            if (sLine.indexOf('=') > 0) {
               v.addElement(sLine);
            }
         }

         in.close();
      } catch (Exception e) {
         GameObject.CrashLog("[mod] grand prix : lecture de " + _SAVE_FILE + " impossible : " + e + "\n");
      }

      return v;
   }

   static int GetValue(java.util.Vector v, String sKey) {
      for (int i = 0; i < v.size(); i++) {
         String s = (String)v.elementAt(i);
         int nEq = s.lastIndexOf('=');
         if (s.substring(0, nEq).equals(sKey)) {
            try {
               return Integer.parseInt(s.substring(nEq + 1).trim());
            } catch (NumberFormatException e) {
               return 0;
            }
         }
      }

      return 0;
   }

   static void SetValue(java.util.Vector v, String sKey, int n) {
      for (int i = 0; i < v.size(); i++) {
         String s = (String)v.elementAt(i);
         if (s.substring(0, s.lastIndexOf('=')).equals(sKey)) {
            v.setElementAt(sKey + "=" + n, i);
            return;
         }
      }

      v.addElement(sKey + "=" + n);
   }

   // médailles du profil en cours : or, argent, bronze
   static int GetMedals(int nMedal) {
      return GetValue(ReadLines(), ProfileName() + "." + _MEDALS[nMedal]);
   }

   static void SaveResult(int nPlace) {
      if (m_bSaved || m_bCheated) {
         return;
      }

      m_bSaved = true;
      java.util.Vector v = ReadLines();
      String sName = ProfileName();
      SetValue(v, sName + ".Played", GetValue(v, sName + ".Played") + 1);
      if (nPlace >= 0 && nPlace < _MEDALS.length) {
         String sKey = sName + "." + _MEDALS[nPlace];
         SetValue(v, sKey, GetValue(v, sKey) + 1);
      }

      try {
         FileWriter out = new FileWriter(_SAVE_FILE);

         for (int i = 0; i < v.size(); i++) {
            out.write((String)v.elementAt(i) + "\r\n");
         }

         out.close();
         GameObject.CrashLog("[mod] grand prix : palmares enregistre dans " + _SAVE_FILE + "\n");
      } catch (Exception e) {
         GameObject.CrashLog("[mod] grand prix : ecriture de " + _SAVE_FILE + " impossible : " + e + "\n");
      }
   }
}
