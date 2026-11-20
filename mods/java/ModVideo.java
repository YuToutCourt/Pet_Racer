// [mod] Résolutions modernes : n'importe quelle taille d'écran au lieu de 640×480 / 800×600 / 1024×768 (voir notes/resolution.md)
//
// La taille de référence est celle de Data\VideoSettings.scr (lue par le moteur avant le Java) : le jeu ne la
// remplace plus par l'index de progress.cfg. L'index d'origine (0, 1, 2) reste tenu à jour comme « classe de taille »
// pour le code qui s'en sert encore (polices de KidsText, curseur du menu Audio/Vidéo).
class ModVideo {
   // taille demandée au moteur (elle ne change à l'écran qu'après ApplyVideoSettings)
   static int m_nWidth = 640;
   static int m_nHeight = 480;

   // appelé par GameSerializeSettings.SetCurrentSettings, au démarrage : garde la taille de VideoSettings.scr
   static void ApplyStartup(Settings s) {
      int w = GameObject.GetScreenWidth();
      int h = GameObject.GetScreenHeight();
      if (w < 640 || h < 480) {
         w = 640;
         h = 480;
      }

      Request(s, w, h);
      GameObject.CrashLog("[mod] video : " + w + "x" + h + " (classe " + s.nResolution + ")\n");
   }

   // toute demande de changement de résolution passe par ici
   static void Request(Settings s, int w, int h) {
      m_nWidth = w;
      m_nHeight = h;
      s.nResolution = GetSizeClass(h);
      RacerGame.SetResolution(s.nResolution);
      RacerGame.game.SetResolution(w, h);
   }

   // 0 = petit (≤ 480 lignes), 1 = moyen (≤ 600), 2 = grand : indexe les tables d'origine à 3 cases
   static int GetSizeClass(int h) {
      if (h < 600) {
         return 0;
      }

      return h < 768 ? 1 : 2;
   }

   // --- liste des résolutions du menu Audio/Vidéo ---
   // plaque peinte par mods/art/make_audiovideo_menu.py (pixels 410..604 × 232..264 de l'image 640×480)
   static final float _LABEL_W = 194.0F / 640.0F;
   static final float _LABEL_H = 32.0F / 480.0F;
   static final float _LABEL_X = 507.0F / 640.0F;
   static final float _LABEL_Y = 248.0F / 480.0F;
   static final int _MAX_MODES = 16;
   // rapports largeur/hauteur gardés quand l'écran propose trop de modes : 4:3, 5:4, 16:10, 16:9, 21:9
   static final float[] _ASPECTS = new float[]{1.3333F, 1.25F, 1.6F, 1.7778F, 2.3333F, 2.3889F};
   // modes de secours si le moteur n'en donne aucun
   static final int[] _FALLBACK = new int[]{640, 480, 800, 600, 1024, 768, 1280, 720, 1280, 960, 1366, 768, 1600, 900, 1920, 1080, 2560, 1440};
   static int[] m_tModes = null; // w0, h0, w1, h1… triés
   static int m_nModes = 0;

   static void BuildModes() {
      if (m_tModes != null) {
         return;
      }

      int[] t = new int[512];
      int n = 0;
      StringBuffer sLog = new StringBuffer();

      // EnumResolution(i, liste) rend "LxH" pour le i-ème mode d'une des 4 listes du moteur (une par profondeur
      // de couleur : 15, 16, 24, 32 bits selon la carte), ou "" après le dernier (ChromeEngine.dll, FUN_10155c70).
      // On réunit les 4 listes.
      for (int nList = 0; nList < 4; nList++) {
         for (int i = 0; i < 256 && n < 256; i++) {
            String s = null;
            try {
               s = RacerGame.game.EnumResolution(i, nList);
            } catch (Throwable e) {
               GameObject.CrashLog("[mod] video : EnumResolution(" + i + ", " + nList + ") : " + e + "\n");
               break;
            }

            int x = s == null ? -1 : s.indexOf('x');
            if (x <= 0) {
               break;
            }

            try {
               int w = Integer.parseInt(s.substring(0, x).trim());
               int h = Integer.parseInt(s.substring(x + 1).trim());
               if (w >= 640 && h >= 480 && Find(t, n, w, h) < 0) {
                  t[2 * n] = w;
                  t[2 * n + 1] = h;
                  n++;
                  sLog.append(s).append(' ');
               }
            } catch (NumberFormatException e) {
               break;
            }
         }
      }

      GameObject.CrashLog("[mod] video : modes de l'ecran : " + sLog + "\n");
      if (n == 0) {
         for (int i = 0; i < _FALLBACK.length; i += 2) {
            t[n * 2] = _FALLBACK[i];
            t[n * 2 + 1] = _FALLBACK[i + 1];
            n++;
         }
      }

      // trop de modes : on garde les tailles courantes (et toujours le plus grand, l'écran natif, et la taille actuelle)
      if (n > _MAX_MODES) {
         int[] u = new int[2 * n];
         int m = 0;
         int nBig = 0;
         for (int i = 1; i < n; i++) {
            if ((long)t[2 * i] * t[2 * i + 1] > (long)t[2 * nBig] * t[2 * nBig + 1]) {
               nBig = i;
            }
         }

         for (int i = 0; i < n; i++) {
            boolean bCurrent = t[2 * i] == m_nWidth && t[2 * i + 1] == m_nHeight;
            if (i == nBig || bCurrent || IsCommonSize(t[2 * i], t[2 * i + 1])) {
               u[2 * m] = t[2 * i];
               u[2 * m + 1] = t[2 * i + 1];
               m++;
            }
         }

         t = u;
         n = m;
      }

      // tri par hauteur puis largeur
      for (int i = 0; i < n; i++) {
         for (int j = i + 1; j < n; j++) {
            if (t[2 * j + 1] < t[2 * i + 1] || t[2 * j + 1] == t[2 * i + 1] && t[2 * j] < t[2 * i]) {
               int a = t[2 * i];
               int b = t[2 * i + 1];
               t[2 * i] = t[2 * j];
               t[2 * i + 1] = t[2 * j + 1];
               t[2 * j] = a;
               t[2 * j + 1] = b;
            }
         }
      }

      // encore trop : on garde les plus grands
      int nFirst = n > _MAX_MODES ? n - _MAX_MODES : 0;
      m_nModes = n - nFirst;
      m_tModes = new int[2 * m_nModes];
      System.arraycopy(t, 2 * nFirst, m_tModes, 0, 2 * m_nModes);
      StringBuffer sList = new StringBuffer();
      for (int i = 0; i < m_nModes; i++) {
         sList.append(GetModeName(i)).append(' ');
      }

      GameObject.CrashLog("[mod] video : liste du menu : " + sList + "\n");
   }

   // hauteurs des tailles d'écran courantes (720p, 1080p, 1440p, 4K…)
   static final int[] _HEIGHTS = new int[]{480, 600, 720, 768, 864, 900, 960, 1024, 1050, 1080, 1200, 1440, 1536, 1600, 2160};

   static boolean IsCommonSize(int w, int h) {
      boolean bHeight = false;
      for (int i = 0; i < _HEIGHTS.length; i++) {
         if (_HEIGHTS[i] == h) {
            bHeight = true;
         }
      }

      if (!bHeight) {
         return false;
      }

      float r = (float)w / h;
      for (int i = 0; i < _ASPECTS.length; i++) {
         if (Math.abs(r - _ASPECTS[i]) < 0.02F) {
            return true;
         }
      }

      return false;
   }

   static int Find(int[] t, int n, int w, int h) {
      for (int i = 0; i < n; i++) {
         if (t[2 * i] == w && t[2 * i + 1] == h) {
            return i;
         }
      }

      return -1;
   }

   static int GetModeCount() {
      BuildModes();
      return m_nModes;
   }

   static String GetModeName(int i) {
      return m_tModes[2 * i] + " x " + m_tModes[2 * i + 1];
   }

   // mode de la liste le plus proche de la taille actuelle
   static int GetCurrentMode() {
      BuildModes();
      int nBest = 0;
      long nBestDiff = Long.MAX_VALUE;
      for (int i = 0; i < m_nModes; i++) {
         long d = Math.abs((long)m_tModes[2 * i] - m_nWidth) + Math.abs((long)m_tModes[2 * i + 1] - m_nHeight);
         if (d < nBestDiff) {
            nBestDiff = d;
            nBest = i;
         }
      }

      return nBest;
   }

   static void RequestMode(Settings s, int i) {
      BuildModes();
      if (i < 0) {
         i = 0;
      }

      if (i >= m_nModes) {
         i = m_nModes - 1;
      }

      Request(s, m_tModes[2 * i], m_tModes[2 * i + 1]);
      GameObject.CrashLog("[mod] video : menu -> " + GetModeName(i) + "\n");
   }

   // --- écran large (Window.SetSizeAndPos) ---
   // les coordonnées 0..1 du jeu ont été pensées pour du 4:3 ; en 16:9 elles s'étiraient en largeur
   static final int _LAYOUT_STRETCH = 0; // comme le jeu d'origine : fraction de la largeur du parent
   static final int _LAYOUT_ZONE = 1;    // écran de menu : zone 4:3 centrée (bandes sur les côtés)
   static final int _LAYOUT_ANCHOR = 2;  // gadget du HUD : taille 4:3, collé au bord d'écran le plus proche
   static boolean m_bWide = true;        // false = tout étiré comme avant

   static int GetLayout(Window w) {
      if (!m_bWide) {
         return _LAYOUT_STRETCH;
      }

      if (w.wndParent == null) {
         // le HUD et les fonds noirs (fondus, fond derrière les menus) couvrent tout l'écran
         if (w instanceof KidsHud || w instanceof KidsBackgroundMenuWindow || w instanceof KidsBlackMenuWindow) {
            return _LAYOUT_STRETCH;
         }

         return _LAYOUT_ZONE;
      }

      return w.wndParent instanceof KidsHud ? _LAYOUT_ANCHOR : _LAYOUT_STRETCH;
   }

   // largeur de la zone 4:3 pour un parent de w × h (jamais plus large que lui)
   static int GetZoneWidth(int w, int h) {
      int z = h * 4 / 3;
      return z < w ? z : w;
   }

   // décalage horizontal d'un gadget du HUD dont le centre est à fCenter (0..1 dans la zone 4:3) :
   // à gauche il reste collé au bord gauche, à droite au bord droit, au milieu il est centré
   static int GetAnchorShift(int w, int nZoneW, float fCenter) {
      if (fCenter < 0.4F) {
         return 0;
      }

      return fCenter > 0.6F ? w - nZoneW : (w - nZoneW) / 2;
   }

   // --- tailles en pixels pensées pour 1024×768 (curseur, polices) ---
   static int ScaleUi(int nPx) {
      int h = GameObject.GetScreenHeight();
      return h <= 768 ? nPx : nPx * h / 768;
   }

   // police de KidsText : 16/18/20 px d'origine (640/800/1024), plus grande au-delà si son .dat existe
   static String GetTextFont() {
      int h = GameObject.GetScreenHeight();
      if (h < 600) {
         return "MSSansSerif16";
      } else if (h < 768) {
         return "MSSansSerif18";
      } else if (h < 1000) {
         return "MSSansSerif20";
      } else if (h >= 2000 && HasFont(40)) {
         return "MSSansSerif40";
      } else if (h >= 1400 && HasFont(32)) {
         return "MSSansSerif32";
      }

      return "MSSansSerif26";
   }

   static boolean HasFont(int nSize) {
      return new java.io.File("Data/Panel/MS Sans Serif" + nSize + ".dat").exists();
   }

   static int GetWidth() {
      return m_nWidth;
   }

   static int GetHeight() {
      return m_nHeight;
   }
}
