import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

// [mod] Entraînement des IA (notes/ia.md, « Entraînement automatique ») : le jeu enchaîne tout seul des courses
// de 6 IA, sans joueur, sur tous les circuits, pour remplir la mémoire des IA (aimap.dat) sans avoir à jouer.
// Lancé par mods\entrainer-ia.ps1, qui écrit entrainement.scr à la racine du jeu le temps de l'entraînement :
//   Passes(3)              nombre de tours de l'ensemble des circuits
//   Tours(3)               tours par course
//   Vitesse(12)            accélération maxi du temps (TimerSetSpeedUp du moteur), 1 à 20 : réglée en continu pour
//                          que la physique avance par pas de _TARGET_STEP au plus (comme un joueur à 144 images/s)
//   Niveaux("3,5,4")       difficulté des IA à chaque passe, en boucle (ModAI : 2 facile .. 5 expert)
//   Circuits("0,6,30")     circuits à faire (RacerModule.MAPNAMES, 0..30) ; absent = tous
//   Son(0)                 0 = muet pendant l'entraînement
//   Chrono(1)              IA chrono (ModChrono) : les IA cherchent le meilleur plan de course, mémoire aichrono.dat
//   Animal("Ours")         IA chrono : l'animal des 6 voitures (le plan dépend de l'animal)
//   Relance(31)            le jeu se ferme toutes les 31 courses (0 = jamais) en écrivant entrainement_relance.txt ;
//                          mods\entrainer-ia.ps1 le relance et l'entraînement reprend. Le jeu ralentit quand il tourne
//                          longtemps (2026-11-21 : toutes les voitures +6 à 9 % en 7 h, images/s 1200 -> 700)
// Sans entrainement.scr : rien ne change. Course après course, entrainement_suite.txt garde la course suivante :
// un entraînement interrompu (Échap, jeu fermé, plantage) reprend là où il en était au lancement suivant.
// La démo du menu principal sert de base (RacerModule.m_bIsDemo : aucun joueur, caméra qui passe d'une voiture à
// l'autre) ; la course ne s'arrête plus à la première IA arrivée mais quand toutes le sont (ou au bout du temps
// maxi), et la mémoire est enregistrée comme après une course normale.
class ModTraining {
   static final String _FILE = "entrainement.scr";
   static final String _PROGRESS_FILE = "entrainement_suite.txt";
   static final String _RESTART_FILE = "entrainement_relance.txt"; // le jeu s'est fermé pour être relancé
   // après la première arrivée, temps de course laissé aux autres (s, temps du jeu)
   static final float _WAIT_AFTER_FIRST = 60.0F;
   // temps de course maxi par tour (s, temps du jeu) : une IA coincée pour de bon n'arrête pas tout
   static final float _MAX_TIME_PER_LAP = 150.0F;
   // attente au menu principal avant de lancer la course suivante (s, temps réel)
   static final long _MENU_WAIT_MS = 1500L;

   static boolean m_bActive = false;   // entrainement.scr présent au démarrage
   static boolean m_bRunning = false;  // entraînement en cours (faux après Échap ou à la fin)
   static int m_nPasses = 3;
   static int m_nLaps = 3;
   static int m_nSpeed = 12;                  // accélération maxi
   static int m_nRestartEvery = 0;            // Relance(n) : jeu fermé (et relancé par le script) toutes les n courses
   static float m_fSpeed = 1.0F;              // accélération en cours (RaceFrame)
   static final float _TARGET_STEP = 0.007F;  // pas de physique visé (s) : 7 ms = 144 images/s
   static final long _SPEED_EVERY_MS = 500L;  // réglage de l'accélération toutes les 0,5 s réelles
   static long m_lSpeedSince = 0L;
   static int m_nSpeedFrames = 0;
   static int[] m_aLevels = new int[]{3, 5, 4};
   static int[] m_aTracks = null;
   static boolean m_bSound = false;
   // sans bonus par défaut (2026-11-20) : un tir, une flaque ou un enlèvement envoie l'IA dans le décor ou la coince,
   // la mémoire apprenait ces chocs-là comme des murs ou des endroits dangereux. Bonus(1) : boîtes gardées.
   static boolean m_bBonus = false;

   static int m_nNext = 0;             // course suivante, 0 .. Total() - 1
   static int m_nDone = 0;             // courses finies depuis ce lancement
   static boolean m_bInRace = false;   // une course d'entraînement est lancée
   static boolean m_bEnding = false;   // fin de course demandée (toutes arrivées ou temps maxi)
   static boolean m_bSpeedSet = false;
   static float m_fRaceStart = -1.0F;  // temps du module de course au début de la course
   static float m_fFirstFinish = -1.0F;
   static int m_nFinished = 0;
   static long m_lRaceRealStart = 0L;
   static long m_lMenuSince = 0L;
   static long m_lTrainingStart = 0L;
   static int m_nFrames = 0;

   static void Log(String s) {
      GameObject.CrashLog("[mod] entrainement " + s + "\n");
   }

   // RacerGame.OnInitialize, avant ModOnline.Init (pas de réseau pendant l'entraînement)
   static void Init() {
      if (!new File(_FILE).exists()) {
         return;
      }

      for (ParseKeyword k = GameObject.ParseFile(_FILE); k != null; k = k.GetNext()) {
         if (k.Equals("Passes")) {
            m_nPasses = Math.max(1, Math.min(50, k.GetIParam(0)));
         }

         if (k.Equals("Tours")) {
            m_nLaps = Math.max(1, Math.min(10, k.GetIParam(0)));
         }

         if (k.Equals("Vitesse")) {
            m_nSpeed = Math.max(1, Math.min(20, k.GetIParam(0)));
         }

         if (k.Equals("Relance")) {
            m_nRestartEvery = Math.max(0, Math.min(1000, k.GetIParam(0)));
         }

         if (k.Equals("Son")) {
            m_bSound = k.GetIParam(0) != 0;
         }

         if (k.Equals("Bonus")) {
            m_bBonus = k.GetIParam(0) != 0;
         }

         if (k.Equals("Chrono")) {
            ModChrono.m_bOn = k.GetIParam(0) != 0;
         }

         if (k.Equals("Animal")) {
            int a = ModAnimals.Find(k.GetSParam(0));
            if (a >= 0) {
               ModChrono.m_nAnimal = a;
            }
         }

         if (k.Equals("Niveaux")) {
            int[] a = ParseInts(k.GetSParam(0), ModOptions._AI_AUTO, ModOptions._AI_MAX);
            if (a.length > 0) {
               m_aLevels = a;
            }
         }

         if (k.Equals("Circuits")) {
            int[] a = ParseInts(k.GetSParam(0), 0, RacerModule.GetMapsCnt() - 1);
            if (a.length > 0) {
               m_aTracks = a;
            }
         }
      }

      if (ModChrono.m_bOn) {
         m_bBonus = false; // [mod] IA chrono : jamais de bonus (un tir ou un piège fausserait le chrono d'un plan)
      }

      if (m_aTracks == null) {
         m_aTracks = new int[RacerModule.GetMapsCnt()];

         for (int i = 0; i < m_aTracks.length; i++) {
            m_aTracks[i] = i;
         }
      }

      m_nNext = ReadProgress();
      if (m_nNext >= Total()) {
         m_nNext = 0;
      }

      m_bActive = true;
      m_bRunning = true;
      m_lTrainingStart = System.currentTimeMillis();
      String sLevels = "";

      for (int i = 0; i < m_aLevels.length; i++) {
         sLevels = sLevels + (i > 0 ? ", " : "") + ModAI._MODE_NAMES[m_aLevels[i]];
      }

      Log(": " + m_nPasses + " passe(s) de " + m_aTracks.length + " circuits = " + Total() + " courses de " + m_nLaps + " tours, temps jusqu'a x" + m_nSpeed
         + ", difficultes " + sLevels + (m_bSound ? "" : ", muet") + (m_bBonus ? "" : ", sans bonus") + (m_nNext > 0 ? " ; reprise a la course " + (m_nNext + 1) : "")
         + (ModChrono.m_bOn ? " ; IA CHRONO, animal " + ModAnimals._NAMES[ModChrono.m_nAnimal] + ", memoire " + ModChrono._FILE : "")
         + (m_nRestartEvery > 0 ? " ; jeu relance toutes les " + m_nRestartEvery + " courses" : ""));
   }

   // "3,5,4" -> {3, 5, 4} (sans String.split : JRE 1.3) ; les valeurs hors de [min, max] sont ignorées
   static int[] ParseInts(String s, int nMin, int nMax) {
      int[] a = new int[64];
      int n = 0;
      int cur = -1;

      for (int i = 0; i <= (s == null ? 0 : s.length()); i++) {
         char c = s != null && i < s.length() ? s.charAt(i) : ',';
         if (c >= '0' && c <= '9') {
            cur = (cur < 0 ? 0 : cur * 10) + (c - '0');
         } else {
            if (cur >= nMin && cur <= nMax && n < a.length) {
               a[n++] = cur;
            }

            cur = -1;
         }
      }

      int[] r = new int[n];
      System.arraycopy(a, 0, r, 0, n);
      return r;
   }

   static int Total() {
      return m_nPasses * m_aTracks.length;
   }

   static int Pass(int nRace) {
      return nRace / m_aTracks.length;
   }

   static int TrackOf(int nRace) {
      return m_aTracks[nRace % m_aTracks.length];
   }

   // ModAI.Mode : difficulté de la passe en cours
   static int AiMode() {
      return m_aLevels[Pass(m_nNext) % m_aLevels.length];
   }

   static int ReadProgress() {
      try {
         File f = new File(_PROGRESS_FILE);
         if (!f.exists()) {
            return 0;
         }

         BufferedReader r = new BufferedReader(new FileReader(f));
         String s = r.readLine();
         r.close();
         return s != null ? Math.max(0, Integer.parseInt(s.trim())) : 0;
      } catch (Exception e) {
         return 0;
      }
   }

   static void WriteProgress() {
      try {
         File f = new File(_PROGRESS_FILE);
         if (m_nNext >= Total()) {
            f.delete();
            return;
         }

         FileWriter w = new FileWriter(f);
         w.write(m_nNext + "\r\n");
         w.close();
      } catch (Exception e) {
         Log(": " + _PROGRESS_FILE + " non ecrit (" + e + ")");
      }
   }

   static void Mute() {
      if (!m_bSound) {
         RacerGame.game.SetAudioWaveVolume(0);
         RacerGame.game.SetAudioFXVolume(0);
         RacerGame.game.ApplyAudioSettings();
      }
   }

   static void RestoreSound() {
      GameSerializeSettings g = RacerGame.GetGSS();
      RacerGame.game.SetAudioWaveVolume((int)(g.m_MusicVolume * 10.0F));
      RacerGame.game.SetAudioFXVolume((int)(g.m_FXVolume * 10.0F));
      RacerGame.game.ApplyAudioSettings();
   }

   // accélération du temps à appliquer au module de course (ExitDuringGameWindow la remet après « Quitter ? »)
   static float TimeScale() {
      return m_bRunning && m_bInRace ? m_fSpeed : 1.0F;
   }

   // RacerGame.OnFrame, à chaque image
   static void Frame() {
      if (!m_bRunning || RacerGame.game == null) {
         return;
      }

      if (RacerGame.game.activeMenu == 3) {
         RacerGame.game.EndIntro(); // pas d'intro
         return;
      }

      if (m_bInRace) {
         RaceFrame();
         return;
      }

      boolean bAtMenu = RacerGame.game.activeMenu == 1 && RacerGame.firstMenuModule != null
         && RacerGame.firstMenuModule.m_CurrentMenu == KidsSimpleMenuModule._MainMenuWindow && !RacerModule.m_bIsDemo;
      if (!bAtMenu) {
         m_lMenuSince = 0L;
         return;
      }

      long lNow = System.currentTimeMillis();
      if (m_lMenuSince == 0L) {
         m_lMenuSince = lNow;
         Mute();
         return;
      }

      if (lNow - m_lMenuSince < _MENU_WAIT_MS) {
         return;
      }

      m_lMenuSince = 0L;
      if (m_nNext >= Total()) {
         Finish();
      } else if (m_nRestartEvery > 0 && m_nDone >= m_nRestartEvery) {
         Restart();
      } else {
         Launch();
      }
   }

   static void Launch() {
      int nTrack = TrackOf(m_nNext);
      Log("course " + (m_nNext + 1) + "/" + Total() + " (passe " + (Pass(m_nNext) + 1) + ") : " + RacerModule.GetLevelFileName(nTrack) + ", difficulte "
         + ModAI._MODE_NAMES[AiMode()]);
      KidsSimpleMenuModule m = RacerGame.firstMenuModule;
      Window w = m.GetWindowAt(m.m_CurrentMenu);
      if (w != null) {
         w.DisableCallMethod("RunDemo"); // la démo du menu (90 s sans toucher à rien) ne doit pas partir en plus
      }

      m_bInRace = true;
      m_bEnding = false;
      m_bSpeedSet = false;
      m_fRaceStart = -1.0F;
      m_fFirstFinish = -1.0F;
      m_nFinished = 0;
      m_nFrames = 0;
      m_fClockZero = -1.0F;
      m_sbHits = new StringBuffer();
      m_nHits = 0;

      for (int i = 0; i < m_aLapN.length; i++) {
         m_aLapN[i] = 0;

         for (int j = 0; j < _MAX_LAPS; j++) {
            m_aLapCross[i][j] = -1.0F;
         }
      }

      m_lRaceRealStart = System.currentTimeMillis();
      CoreLogic c = RacerGame.GetCoreLogic();
      RacerModule.SetDemoModule(true);
      c.m_bMultiplayer = false;
      RacerGame.game.RunModule();
   }

   // RacerGame.RunModule, démo : circuit, tours et animaux de la course d'entraînement (6 animaux tirés parmi 12,
   // chacun avec ses stats, pour que les IA n'apprennent pas toujours avec les mêmes voitures)
   static int PrepareRace(CoreLogic c) {
      boolean[] bUsed = new boolean[12];
      c.m_SelectedAnimals = new IntList(6);

      for (int i = 0; i < 6; i++) {
         if (ModChrono.m_bOn) {
            c.m_SelectedAnimals.Push(ModChrono.m_nAnimal); // [mod] IA chrono : 6 fois le même animal, chronos comparables
            continue;
         }

         int a;
         do {
            a = RacerGame.GetRandom(12);
         } while (bUsed[a]);

         bUsed[a] = true;
         c.m_SelectedAnimals.Push(a);
      }

      c.m_Laps = m_nLaps;
      return TrackOf(m_nNext);
   }

   static void RaceFrame() {
      RacerModule r = RacerGame.activeLevelModule;
      if (r == null || RacerGame.game.activeMenu != 2 || r.m_RaceParams == null) {
         return;
      }

      long lNow = System.currentTimeMillis();
      if (!m_bSpeedSet) {
         m_bSpeedSet = true;
         m_fSpeed = Math.min(3.0F, (float)m_nSpeed); // départ prudent, puis selon les images/s réelles
         r.TimerSetSpeedUp(m_fSpeed);
         m_lSpeedSince = lNow;
         m_nSpeedFrames = 0;
         Mute();
      }

      // accélération = pas visé / durée réelle d'une image (moyenne sur 0,5 s), entre 1 et m_nSpeed : plus le PC
      // affiche d'images, plus le temps peut aller vite sans que la conduite change
      m_nSpeedFrames++;
      if (lNow - m_lSpeedSince >= _SPEED_EVERY_MS) {
         float fReal = (lNow - m_lSpeedSince) / 1000.0F / m_nSpeedFrames;
         float f = Math.max(1.0F, Math.min((float)m_nSpeed, _TARGET_STEP / fReal));
         if (Math.abs(f - m_fSpeed) > 0.05F * m_fSpeed) {
            m_fSpeed = f;
            r.TimerSetSpeedUp(m_fSpeed);
         }

         m_lSpeedSince = lNow;
         m_nSpeedFrames = 0;
      }

      m_nFrames++;
      if (m_fRaceStart < 0.0F) {
         m_fRaceStart = r.m_fNowTime;
      }

      // départ du chrono de course (feu vert) : les temps de l'historique comptent en temps du jeu à partir de là
      float fMission = r.GetClock().GetMissionExactTime();
      if (m_fClockZero < 0.0F && fMission > 0.0F) {
         m_fClockZero = r.m_fNowTime - fMission;
         m_fMissionZero = fMission;
      }

      if (m_bEnding) {
         return;
      }

      float t = r.m_fNowTime - m_fRaceStart;
      boolean bLate = m_fFirstFinish >= 0.0F && r.m_fNowTime - m_fFirstFinish > _WAIT_AFTER_FIRST;
      if (bLate || t > _MAX_TIME_PER_LAP * m_nLaps + 20.0F) {
         Log(": " + (bLate ? "attente des dernieres IA depassee" : "temps de course maxi depasse") + ", " + m_nFinished + "/" + r.m_RaceParams.m_Vehicles.size()
            + " arrivees");
         EndRace(null);
      }
   }

   // Vehicle, ligne d'arrivée franchie par une IA en démo d'entraînement : la course finit quand toutes sont là
   static void OnCarFinished(Vehicle v) {
      RacerModule r = RacerModule.m_Mod;
      m_nFinished++;
      if (m_fFirstFinish < 0.0F) {
         m_fFirstFinish = r.m_fNowTime;
      }

      if (m_nFinished >= r.m_RaceParams.m_Vehicles.size()) {
         EndRace(v);
      }
   }

   static void EndRace(Vehicle v) {
      if (m_bEnding) {
         return;
      }

      m_bEnding = true;
      RacerModule r = RacerModule.m_Mod;
      if (v == null && r.m_RaceParams.m_Vehicles.size() > 0) {
         v = (Vehicle)r.m_RaceParams.m_Vehicles.elementAt(0);
      }

      if (v != null) {
         v.FinishRace(); // podium sauté (démo) : retour au menu principal après _STOP_TIME_AFTER_RACE
      } else {
         RacerGame.game.m_bFinishRace = true;
      }
   }

   // RacerGame.EndModule, après l'enregistrement de la mémoire. bEsc : course quittée par Échap = arrêt de
   // l'entraînement (la même course sera refaite à la reprise)
   static void OnRaceEnded(boolean bEsc) {
      if (!m_bRunning || !m_bInRace) {
         return;
      }

      m_bInRace = false;
      RacerModule r = RacerGame.activeLevelModule;
      float fGame = r != null && m_fRaceStart >= 0.0F ? r.m_fNowTime - m_fRaceStart : 0.0F;
      long lReal = System.currentTimeMillis() - m_lRaceRealStart;
      if (bEsc) {
         m_bRunning = false;
         RestoreSound();
         Log(": arrete par Echap a la course " + (m_nNext + 1) + "/" + Total() + " (elle sera refaite au prochain lancement avec " + _FILE + ")");
         return;
      }

      Log("course " + (m_nNext + 1) + "/" + Total() + " finie : " + m_nFinished + " arrivees, " + Min(fGame) + " de course en " + Min(lReal / 1000.0F) + " reelles (x"
         + (lReal > 0L ? Sec(fGame * 1000.0F / lReal) : "?") + "), " + (lReal > 0L ? (int)(m_nFrames * 1000L / lReal) : 0) + " images/s, pas de physique "
         + (m_nFrames > 0 ? ModFrameLimiter.Ms(fGame / m_nFrames) : "?") + " ms");
      ModChrono.OnRaceEnded(r); // [mod] IA chrono : notes des plans, nouvelle base, aichrono.dat
      WriteHistory(r, fGame, lReal);
      m_nNext++;
      m_nDone++;
      WriteProgress();
   }

   // ------------------------------------------------------------------ historique (rapport HTML)
   // entrainement_historique.txt, ajouté course après course (jamais effacé par le jeu), lu par mods\rapport-ia.py.
   // Une ligne par enregistrement, champs « clé=valeur » séparés par des tabulations :
   //   S  début d'un entraînement (date, réglages)
   //   P  record du joueur sur un circuit (menu Records), au premier départ
   //   G  géométrie d'un circuit (portes de la piste des IA x,y,z ; taille du terrain), une fois par lancement
   //   R  une course (circuit, difficulté, arrivées, mémoire après la course, points dangereux, chocs x,z)
   //   A  une IA dans cette course (place, temps, tours, hors piste, coincée, chocs…)
   // Temps en secondes de jeu depuis le feu vert (le temps accéléré ne change rien).
   static final String _HISTORY_FILE = "entrainement_historique.txt";
   static final int _MAX_CARS = 8;
   static final int _MAX_LAPS = 12;
   static final int _MAX_HITS = 400;
   static float m_fClockZero = -1.0F;
   static float m_fMissionZero = 0.0F;
   static float[][] m_aLapCross = new float[_MAX_CARS][_MAX_LAPS];
   static int[] m_aLapN = new int[_MAX_CARS];
   static StringBuffer m_sbHits = new StringBuffer();
   static int m_nHits = 0;
   static boolean m_bHeaderDone = false;
   static java.util.Vector m_GeomDone = new java.util.Vector();

   static boolean Recording() {
      return m_bRunning && m_bInRace && RacerModule.m_bIsDemo && RacerModule.m_Mod != null;
   }

   // Vehicle : ligne de départ franchie (m_iCurrLoop vient d'augmenter ; -1 -> 0 au départ, puis un par tour)
   static void OnLap(Vehicle v) {
      if (!Recording() || m_fClockZero < 0.0F) {
         return;
      }

      // rangé au numéro de tour du jeu, pas au nombre de passages : une IA qui recule par-dessus la ligne (marche
      // arrière pour se dégager) perd un tour (Vehicle) puis le reprend en repassant ; on garde alors ce dernier
      // passage (le 1er « tour » de 11 s du 2026-11-20). Le départ (tour 0) garde le premier.
      int i = v.m_Index;
      int j = v.m_iCurrLoop;
      if (i >= 0 && i < _MAX_CARS && j >= 0 && j < _MAX_LAPS) {
         if (j > 0 || m_aLapCross[i][0] < 0.0F) {
            m_aLapCross[i][j] = RacerModule.m_Mod.m_fNowTime - m_fClockZero;
         }

         for (int l = j + 1; l < _MAX_LAPS; l++) {
            m_aLapCross[i][l] = -1.0F; // passages d'avant le recul : plus valables
         }

         m_aLapN[i] = j + 1;
      }
   }

   // VehicleAI.ModOnObstacle : choc contre le décor
   static void OnHit(float x, float z) {
      if (!Recording() || m_nHits >= _MAX_HITS) {
         return;
      }

      if (m_nHits > 0) {
         m_sbHits.append(';');
      }

      m_sbHits.append(Math.round(x)).append(',').append(Math.round(z));
      m_nHits++;
   }

   static String Sec(float f) {
      return String.valueOf(Math.round(f * 100.0F) / 100.0F);
   }

   static String Now() {
      return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
   }

   static void WriteHistory(RacerModule r, float fGame, long lReal) {
      if (r == null || r.m_RaceParams == null) {
         return;
      }

      StringBuffer sb = new StringBuffer();
      int nTrack = TrackOf(m_nNext);
      String sKey = ModAIMap.m_sKey != null ? ModAIMap.m_sKey : RacerModule.GetLevelFileName(nTrack);
      if (!m_bHeaderDone) {
         m_bHeaderDone = true;
         sb.append("S\tdate=").append(Now()).append("\tpasses=").append(m_nPasses).append("\tlaps=").append(m_nLaps).append("\tspeed=").append(m_nSpeed)
            .append("\ttracks=").append(m_aTracks.length).append("\r\n");
         CoreLogic c = RacerGame.GetCoreLogic();

         for (int i = 0; c != null && c.m_aRecords != null && i < c.m_aRecords.length; i++) {
            RecordEntry e = c.m_aRecords[i];
            if (e != null && e.m_Time < RecordEntry._TIME_LIMIT) {
               sb.append("P\ttrack=").append(i).append("\ttime=").append(Sec(e.m_Time)).append("\tname=").append(Clean(e.m_Name)).append("\r\n");
            }
         }
      }

      Track t = r.m_AITrack;
      if (t != null && !m_GeomDone.contains(sKey)) {
         m_GeomDone.addElement(sKey);
         Vector vSize = r.GetTerrainSize();
         sb.append("G\tkey=").append(sKey).append("\ttrack=").append(nTrack).append("\tsize=").append(Math.round(vSize.fX)).append(',')
            .append(Math.round(vSize.fY)).append(',').append(Math.round(vSize.fZ)).append("\tgates=");

         for (int i = 0; i < t.m_TrackPoints.size(); i++) {
            Vector p = ((TrackPoint)t.m_TrackPoints.elementAt(i)).m_vPos;
            // les portes sont à plat (y = 0) : hauteur du sol, pour caler la carte du rapport sur le relief
            float y = p.fY != 0.0F ? p.fY : r.TerrainHeight(p.fX, p.fZ);
            sb.append(i > 0 ? ";" : "").append(Math.round(p.fX)).append(',').append(Math.round(y)).append(',').append(Math.round(p.fZ));
         }

         sb.append("\r\n");
      }

      // horloge de course : temps du chrono / temps du jeu depuis le feu vert (1 = le chrono suit le temps accéléré)
      float fMission = r.GetClock().GetMissionExactTime() - m_fMissionZero;
      float fSinceGreen = m_fClockZero >= 0.0F ? r.m_fNowTime - m_fClockZero - m_fMissionZero : 0.0F;
      StringBuffer sd = new StringBuffer();
      float[] d = ModAIMap.m_aSaved; // mémoire des dangers après cette course

      for (int i = 0; d != null && i < d.length; i++) {
         sd.append(i > 0 ? "," : "").append(Math.round(d[i] * 100.0F));
      }

      sb.append("R\tdate=").append(Now()).append("\trace=").append(m_nNext + 1).append("\ttotal=").append(Total()).append("\tpass=").append(Pass(m_nNext) + 1)
         .append("\ttrack=").append(nTrack).append("\tkey=").append(sKey).append("\tmode=").append(ModAI._MODE_NAMES[AiMode()]).append("\tlaps=")
         .append(m_nLaps).append("\tspeed=").append(lReal > 0L ? Sec(fGame * 1000.0F / lReal) : "?").append("\tfinished=").append(m_nFinished).append("\tcars=")
         .append(r.m_RaceParams.m_Vehicles.size()).append("\tgame=").append(Sec(fGame)).append("\treal=").append(Sec(lReal / 1000.0F))
         .append("\tclock=").append(fSinceGreen > 1.0F ? Sec(fMission / fSinceGreen) : "?").append("\twalls=").append(ModAIMap.m_nLastWalls)
         .append("\tdanger=").append(ModAIMap.m_nLastDanger).append("\tlessons=").append(ModAIMap.m_nLastLessons).append("\tspeeds=").append(ModAIMap.m_nLastSpeeds)
         .append("\tcuts=").append(ModAIMap.m_nLastSpeedCuts).append("\tchrono=").append(ModChrono.m_bOn ? 1 : 0).append("\tdangers=")
         .append(sd.toString()).append("\thits=").append(m_sbHits.toString())
         // depuis le lancement du jeu : courses, minutes réelles, et l'horloge du module de course (GetTime) au départ et
         // à l'arrivée (un float : plus il est grand, moins il est précis ; ralentissement du jeu, 2026-11-21)
         .append("\tlaunch_race=").append(m_nDone + 1).append("\tuptime=").append(Sec((System.currentTimeMillis() - m_lTrainingStart) / 60000.0F))
         .append("\tgetTime0=").append(m_fRaceStart).append("\tgetTime1=").append(r.m_fNowTime).append("\r\n");

      for (int i = 0; i < r.m_RaceParams.m_Vehicles.size(); i++) {
         Vehicle v = (Vehicle)r.m_RaceParams.m_Vehicles.elementAt(i);
         VehicleAI a = v.m_VehicleAI;
         int k = v.m_Index;
         int nRank = v.m_iFinishRank >= 0 ? v.m_iFinishRank + 1 : v.m_iCurrPlace + 1;
         StringBuffer sl = new StringBuffer();
         float fBest = 0.0F;
         float fFinish = 0.0F;

         // tour n = passage n - passage n-1 (le passage 0 est le départ, juste après la ligne)
         // (un passage manquant, -1 : tour compté par une remise en piste sans franchir la ligne, ce tour est omis)
         for (int j = 1; k >= 0 && k < _MAX_CARS && j < m_aLapN[k]; j++) {
            if (m_aLapCross[k][j] < 0.0F || m_aLapCross[k][j - 1] < 0.0F) {
               continue;
            }

            float fLap = m_aLapCross[k][j] - m_aLapCross[k][j - 1];
            sl.append(sl.length() > 0 ? "," : "").append(Sec(fLap));
            if (fBest == 0.0F || fLap < fBest) {
               fBest = fLap;
            }

            fFinish = m_aLapCross[k][j];
         }

         sb.append("A\trace=").append(m_nNext + 1).append("\ttrack=").append(nTrack).append("\tanimal=")
            .append(v.m_iAnimalID >= 0 && v.m_iAnimalID < ModAnimals._COUNT ? ModAnimals._NAMES[v.m_iAnimalID] : "" + v.m_iAnimalID).append("\trank=")
            .append(nRank).append("\tfinished=").append(v.m_bRaceFinished ? 1 : 0).append("\ttime=").append(v.m_bRaceFinished ? Sec(fFinish) : "")
            .append("\tlaps=").append(sl.toString()).append("\tbest=").append(fBest > 0.0F ? Sec(fBest) : "").append("\tcar=").append(k);
         if (ModChrono.m_bOn) {
            sb.append("\trole=").append(ModChrono.RoleName(k)); // [mod] IA chrono : plan de cette voiture (base, verif, essai)
         }

         if (a != null) {
            sb.append("\toff=").append(Sec(a.m_fModOffTrack)).append("\tstuck=").append(a.m_nModStuck).append("\trespawn=").append(a.m_nModRespawn).append("\twarp=").append(a.m_nModWarp)
               .append("\thits=").append(a.m_nModObstacles).append("\tbrakes=").append(a.m_nModBrakes).append("\tavoid=").append(a.m_nModAvoid)
               .append("\tbonus=").append(a.m_nModBonusUsed).append("\tlevel=").append(a.m_iAILevel);
         }

         sb.append("\r\n");
      }

      sb.append(ModChrono.m_sLastHistory); // [mod] IA chrono : ligne C (plan de base, chronos), vide sinon
      try {
         FileWriter w = new FileWriter(_HISTORY_FILE, true);
         w.write(sb.toString());
         w.close();
      } catch (Exception e) {
         Log(": " + _HISTORY_FILE + " non ecrit (" + e + ")");
      }
   }

   // pas de tabulation ni de retour à la ligne dans un champ
   static String Clean(String s) {
      if (s == null) {
         return "";
      }

      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         sb.append(c == '\t' || c == '\r' || c == '\n' ? ' ' : c);
      }

      return sb.toString();
   }

   // le jeu se ferme pour repartir neuf (mods\entrainer-ia.ps1 voit entrainement_relance.txt et le relance) ; la
   // course suivante est déjà dans entrainement_suite.txt
   static void Restart() {
      m_bRunning = false;
      RestoreSound();
      try {
         FileWriter w = new FileWriter(_RESTART_FILE);
         w.write((m_nNext + 1) + "\r\n");
         w.close();
      } catch (Exception e) {
         Log(": " + _RESTART_FILE + " non ecrit (" + e + ") ; le jeu reste ouvert");
         return;
      }

      Log(": " + m_nDone + " courses depuis le lancement, en " + Min((System.currentTimeMillis() - m_lTrainingStart) / 1000.0F)
         + " ; le jeu se ferme pour etre relance (course suivante " + (m_nNext + 1) + "/" + Total() + ")");
      GameObject.ExitGame();
   }

   static void Finish() {
      m_bRunning = false;
      RestoreSound();
      Log(": termine, " + m_nDone + " courses en " + Min((System.currentTimeMillis() - m_lTrainingStart) / 1000.0F) + " ; le jeu se ferme");
      GameObject.ExitGame();
   }

   // RacerGame.OnInputKey : Échap arrête l'entraînement tout de suite, en course (pas de fenêtre « Quitter ? ») comme
   // au menu entre deux courses ; le jeu reste ouvert, comme d'habitude
   static boolean OnKey(int nKey, boolean bDown) {
      if (!m_bRunning || nKey != 1) {
         return false;
      }

      if (!bDown) {
         if (m_bInRace) {
            if (RacerGame.game.activeMenu == 2 && RacerGame.activeLevelModule != null) {
               RacerGame.game.EndRaceAfterEsc();
            }
         } else {
            m_bRunning = false;
            RestoreSound();
            Log(": arrete par Echap avant la course " + (m_nNext + 1) + "/" + Total());
         }
      }

      return true;
   }

   static String Min(float s) {
      int n = Math.round(s);
      return n / 60 + " min " + (n % 60 < 10 ? "0" : "") + n % 60 + " s";
   }
}
