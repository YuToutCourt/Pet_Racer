import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

// [mod] Réglages du mod, enregistrés dans mods.dat (texte « Clé=valeur », comme pet_soccer).
// Lu au démarrage (RacerGame.OnPreInitialize), modifié par l'écran « Mods » (KidsModsMenuWindow, chantier 3,
// notes/mods_menu.md) et par les codes clavier d'origine. Réglages globaux, pas par profil.
class ModOptions {
   static final String _SAVE_FILE = "mods.dat";
   // limites d'images proposées (0 = sans limite)
   static final int[] _FPS_LIMITS = new int[]{60, 120, 144, 240, 0};
   static final int _DEFAULT_FPS_LIMIT = 144;
   // vitesse des voitures humaines, en % (100 = jeu d'origine)
   static final int[] _SPEEDS = new int[]{100, 110, 125, 150};
   static final int _BONUS_RANDOM = -1;
   // bonus imposé : identifiants de LevelBonusTypes (0..21, 21 = Missile du mod), -1 = tirage normal
   static final int _BONUS_MAX = 21;

   static int m_nFpsLimit = _DEFAULT_FPS_LIMIT;
   // déblocages (ne touchent pas la progression enregistrée du profil, voir CoreLogic.GetAvailable*)
   static boolean m_bAllAnimals = false;
   static boolean m_bAllTracks = false;
   static boolean m_bAllExtras = false;
   // affichage et course
   static boolean m_bShowFps = false;
   static boolean m_bAiCatchUp = true;
   // difficulté des IA (ModAI) : 0 = IA d'origine (code de Techland intact), 1 = auto (IA améliorée, niveau du
   // circuit comme à l'origine), 2..5 = IA améliorée à niveau fixe (facile, normal, difficile, expert)
   static final int _AI_ORIGINAL = 0;
   static final int _AI_AUTO = 1;
   static final int _AI_MAX = 5;
   static int m_nAiMode = _AI_AUTO;
   // triches (humains seulement) : une triche active rend la course non comptée (records)
   static int m_nSpeed = 100;
   static boolean m_bInfiniteBonus = false;
   static int m_nForcedBonus = _BONUS_RANDOM;
   static boolean m_bShield = false;

   static void Load() {
      File f = new File(_SAVE_FILE);
      if (!f.exists()) {
         // premier lancement : on écrit le fichier avec les valeurs par défaut, pour qu'on le trouve
         Save();
         return;
      }

      try {
         BufferedReader in = new BufferedReader(new FileReader(f));

         for (String sLine = in.readLine(); sLine != null; sLine = in.readLine()) {
            int nEq = sLine.indexOf('=');
            if (nEq > 0) {
               try {
                  Set(sLine.substring(0, nEq).trim(), Integer.parseInt(sLine.substring(nEq + 1).trim()));
               } catch (NumberFormatException e) {
                  GameObject.CrashLog("[mod] mods.dat : ligne ignoree \"" + sLine + "\"\n");
               }
            }
         }

         in.close();
         GameObject.CrashLog("[mod] mods.dat charge : " + GetSummary() + "\n");
      } catch (Exception e) {
         GameObject.CrashLog("[mod] mods : lecture de " + _SAVE_FILE + " impossible : " + e + "\n");
      }
   }

   private static void Set(String sKey, int n) {
      if (sKey.equals("FpsLimit")) {
         // toute valeur de 30 à 1000 est acceptée (le menu ne propose que _FPS_LIMITS) ; 0 = sans limite
         m_nFpsLimit = n == 0 || n >= 30 && n <= 1000 ? n : _DEFAULT_FPS_LIMIT;
      } else if (sKey.equals("AllAnimals")) {
         m_bAllAnimals = n != 0;
      } else if (sKey.equals("AllTracks")) {
         m_bAllTracks = n != 0;
      } else if (sKey.equals("AllExtras")) {
         m_bAllExtras = n != 0;
      } else if (sKey.equals("ShowFps")) {
         m_bShowFps = n != 0;
      } else if (sKey.equals("AiCatchUp")) {
         m_bAiCatchUp = n != 0;
      } else if (sKey.equals("AiLevel")) {
         m_nAiMode = n >= 0 && n <= _AI_MAX ? n : _AI_AUTO;
      } else if (sKey.equals("Speed")) {
         // de 50 à 300 % à la main ; le menu ne propose que _SPEEDS
         m_nSpeed = n >= 50 && n <= 300 ? n : 100;
      } else if (sKey.equals("InfiniteBonus")) {
         m_bInfiniteBonus = n != 0;
      } else if (sKey.equals("ForcedBonus")) {
         m_nForcedBonus = n >= 0 && n <= _BONUS_MAX ? n : _BONUS_RANDOM;
      } else if (sKey.equals("Shield")) {
         m_bShield = n != 0;
      }
   }

   static void Save() {
      try {
         FileWriter out = new FileWriter(_SAVE_FILE);
         out.write("FpsLimit=" + m_nFpsLimit + "\r\n");
         out.write("AllAnimals=" + B(m_bAllAnimals) + "\r\n");
         out.write("AllTracks=" + B(m_bAllTracks) + "\r\n");
         out.write("AllExtras=" + B(m_bAllExtras) + "\r\n");
         out.write("ShowFps=" + B(m_bShowFps) + "\r\n");
         out.write("AiCatchUp=" + B(m_bAiCatchUp) + "\r\n");
         out.write("AiLevel=" + m_nAiMode + "\r\n");
         out.write("Speed=" + m_nSpeed + "\r\n");
         out.write("InfiniteBonus=" + B(m_bInfiniteBonus) + "\r\n");
         out.write("ForcedBonus=" + m_nForcedBonus + "\r\n");
         out.write("Shield=" + B(m_bShield) + "\r\n");
         out.close();
         GameObject.CrashLog("[mod] mods.dat enregistre : " + GetSummary() + "\n");
      } catch (Exception e) {
         GameObject.CrashLog("[mod] mods : ecriture de " + _SAVE_FILE + " impossible : " + e + "\n");
      }
   }

   static int B(boolean b) {
      return b ? 1 : 0;
   }

   // une triche de course est active : la course ne compte pas (records ; plus tard Grand Prix et Elo).
   // Les déblocages, l'affichage, la limite d'images et le rattrapage des IA ne sont pas des triches.
   static boolean IsCheat() {
      return m_nSpeed != 100 || m_bInfiniteBonus || m_nForcedBonus != _BONUS_RANDOM || m_bShield;
   }

   // multiplicateur de vitesse (et d'accélération) d'une voiture : seules les voitures humaines trichent, jamais en
   // ligne (chantier 8, phase 4)
   static float GetSpeedMul(Vehicle v) {
      return IsCheater(v) && m_nSpeed != 100 ? m_nSpeed / 100.0F : 1.0F;
   }

   // la voiture profite des triches de bonus (humains seulement ; aucune triche dans une course en ligne)
   static boolean IsCheater(Vehicle v) {
      return v != null && v.m_bIsPlayer && !ModNet.IsRaceOnline();
   }

   static int Find(int[] t, int n) {
      for (int i = 0; i < t.length; i++) {
         if (t[i] == n) {
            return i;
         }
      }

      return -1;
   }

   static String GetSummary() {
      String s = "limite " + (m_nFpsLimit > 0 ? m_nFpsLimit + " images/s" : "aucune");
      if (m_bAllAnimals || m_bAllTracks || m_bAllExtras) {
         s = s + ", debloque" + (m_bAllAnimals ? " animaux" : "") + (m_bAllTracks ? " circuits" : "") + (m_bAllExtras ? " extras" : "");
      }

      if (m_bShowFps) {
         s = s + ", compteur FPS";
      }

      if (!m_bAiCatchUp) {
         s = s + ", sans rattrapage des IA";
      }

      s = s + ", IA " + ModAI._MODE_NAMES[m_nAiMode];

      if (IsCheat()) {
         s = s + ", TRICHE (vitesse " + m_nSpeed + " %" + (m_bInfiniteBonus ? ", bonus infinis" : "")
            + (m_nForcedBonus != _BONUS_RANDOM ? ", bonus impose " + m_nForcedBonus : "") + (m_bShield ? ", protection" : "") + ")";
      }

      return s;
   }
}
