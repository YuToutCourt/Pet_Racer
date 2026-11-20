import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

// [mod] Carte des circuits pour l'IA améliorée (notes/ia.md, « Carte des circuits », 2026-11-20).
// Au chargement de la course (RaceParams.SetStartPos), des rayons du moteur balaient chaque tronçon de la piste des
// IA, couloir par couloir, à hauteur de voiture : arbres (TraceTrees), objets du décor (TraceObjects : murs,
// poteaux, maisons, rambardes…) et relief trop raide (TraceTerrain). Chaque porte reçoit le couloir libre le plus
// large ; VehicleAI y garde sa trajectoire (chaque IA y choisit sa propre ligne, avec son caractère).
// Ce que les IA apprennent en course est gardé par circuit dans aimap.dat (écrit à chaque fin de course,
// RacerGame.EndModule) :
// - les murs appris : un choc contre le décor dit de quel côté était l'obstacle, le bord du couloir de ce côté recule
//   d'un pas, pour toujours (2026-11-20 : « plus je joue mieux ça sera ») ;
// - les points dangereux (freiner plus, rester au milieu du couloir), un peu oubliés à chaque course.
class ModAIMap {
   static final String _SAVE_FILE = "aimap.dat";
   static final float _LEARN_STEP = 35.0F;    // un choc : le bord du couloir passe à ça de la voiture, côté obstacle
   static final float _MIN_CORRIDOR = 40.0F;  // les murs appris laissent toujours ce couloir (centre de la voiture)
   static final float _GATE_USE = 0.85F;      // porte sans couloir mesuré : comme l'évitement de VehicleAI
   static final float _NONE = 99999.0F;       // pas de mur appris de ce côté
   static final int _LANES = 15;              // couloirs testés par porte (le 8e est le milieu)
   static final float _RAY_HEIGHT = 45.0F;    // hauteur des rayons au-dessus du sol
   static final float _CAR_HALF = 55.0F;      // demi-largeur d'une voiture : un obstacle ferme aussi les couloirs voisins
   static final float _SKY = 1500.0F;         // départ des rayons verticaux (ponts)
   static final float _WALL = 0.3F;           // Vehicle : _MAX_SLOPE_ANGLE ; plus raide = mur
   static final float _KEEP = 0.9F;           // part des dangers appris gardée d'une course à l'autre
   static final int _RETRY = 3;               // objets sans collision traversés par un rayon

   static String m_sLevel = "?";              // RacerGame.LoadLevel
   static Track m_Track = null;
   static String m_sKey = null;
   static float[] m_aLo = null;               // couloir à suivre à la porte i, décalages (> 0 vers la droite de la porte) :
   static float[] m_aHi = null;               // le couloir libre mesuré, resserré par les murs appris
   static boolean[] m_aKnown = null;          // faux : ni mesuré ni appris, la porte reste comme avant
   static float[] m_aScanLo = null;           // couloir libre mesuré par les rayons
   static float[] m_aScanHi = null;
   static boolean[] m_aScanKnown = null;
   static float[] m_aLearnLo = null;          // murs appris en course (-_NONE / _NONE : rien)
   static float[] m_aLearnHi = null;
   static float[] m_aCX = null;               // portes : milieu, axe vers la droite, largeurs
   static float[] m_aCZ = null;
   static float[] m_aRX = null;
   static float[] m_aRZ = null;
   static float[] m_aLW = null;
   static float[] m_aRW = null;
   static float[] m_aSaved = null;            // dangers appris aux courses précédentes, déjà oubliés de _KEEP
   static boolean m_bFraction = false;        // Collision.fDistance : part du rayon (vrai) ou distance
   static int m_nLearnedRace = 0;             // murs appris pendant cette course
   // bilan
   static int m_nRays;
   static int m_nTrees;
   static int m_nObjects;
   static int m_nRelief;
   static int m_nNarrowed;
   static int m_nClosed;
   static int m_nBridges;

   // la carte de cette piste, calculée au premier appel
   static void For(RacerModule mod, Track t) {
      if (t == null || mod == null || t == m_Track && m_aLo != null) {
         return;
      }

      m_Track = t;
      long t0 = System.currentTimeMillis();
      int n = t.m_TrackPoints.size();
      m_aLo = new float[n];
      m_aHi = new float[n];
      m_aKnown = new boolean[n];
      m_aScanLo = new float[n];
      m_aScanHi = new float[n];
      m_aScanKnown = new boolean[n];
      m_aCX = new float[n];
      m_aCZ = new float[n];
      m_aRX = new float[n];
      m_aRZ = new float[n];
      m_aLW = new float[n];
      m_aRW = new float[n];
      m_nLearnedRace = 0;
      m_sKey = m_sLevel + (RacerGame.GetCoreLogic().IsReverseTrack() ? "|inverse" : "") + "|" + n;
      m_aSaved = LoadDanger(m_sKey, n);
      LoadWalls(m_sKey, n);
      m_nRays = m_nTrees = m_nObjects = m_nRelief = m_nNarrowed = m_nClosed = m_nBridges = 0;

      for (int i = 0; i < n; i++) {
         TrackPoint p = t.GetPoint(i);
         m_aCX[i] = p.m_vPos.fX;
         m_aCZ[i] = p.m_vPos.fZ;
         float x = p.m_vRight.fX - p.m_vPos.fX;
         float z = p.m_vRight.fZ - p.m_vPos.fZ;
         float l = (float)Math.sqrt(x * x + z * z);
         m_aRX[i] = l > 0.001F ? x / l : 1.0F;
         m_aRZ[i] = l > 0.001F ? z / l : 0.0F;
         m_aLW[i] = p.m_fLWidth;
         m_aRW[i] = p.m_fRWidth;
      }

      if (n >= 2) {
         try {
            Build(mod, t, n);
         } catch (Throwable e) {
            // carte incomplète : l'IA conduit comme avant aux portes qui restent inconnues
            GameObject.CrashLog("[mod] IA carte : erreur " + e + "\n");
         }
      }

      int nSaved = 0;
      int nWalls = 0;
      for (int i = 0; i < n; i++) {
         Combine(i);
         if (m_aSaved[i] > 0.0F) {
            nSaved++;
         }

         if (HasWall(i)) {
            nWalls++;
         }
      }

      GameObject.CrashLog(
         "[mod] IA carte " + m_sKey + " : " + n + " portes, " + m_nRays + " rayons (distance " + (m_bFraction ? "en part" : "absolue")
            + "), couloirs fermes par arbres " + m_nTrees + ", objets " + m_nObjects + ", relief " + m_nRelief + " ; portes resserrees "
            + m_nNarrowed + ", sans couloir libre " + m_nClosed + ", sur pont " + m_nBridges + " ; memoire : murs appris a " + nWalls
            + " portes, points dangereux " + nSaved + " ; en " + (System.currentTimeMillis() - t0) + " ms\n"
      );
   }

   static void Build(RacerModule mod, Track t, int n) {
      float[] cx = m_aCX;
      float[] cz = m_aCZ;
      float[] rx = m_aRX;
      float[] rz = m_aRZ;
      float[] lw = m_aLW;
      float[] rw = m_aRW;
      float[] gy = new float[n];
      boolean[] bTerrain = new boolean[n];

      // Collision.fDistance : en part du rayon ou en distance ? Un rayon vertical vers le sol tranche.
      float y0 = mod.TerrainHeight(cx[0], cz[0]);
      Collision c0 = mod.TraceTerrain(new Vector(cx[0], y0 + _SKY, cz[0]), new Vector(cx[0], y0 - 100.0F, cz[0]));
      m_bFraction = c0 != null && c0.fDistance <= 1.0001F;

      // hauteur du sol à chaque porte : le terrain, ou un pont si la piste y passe (le plus proche de la porte d'avant)
      for (int i = 0; i < n; i++) {
         float yT = mod.TerrainHeight(cx[i], cz[i]);
         gy[i] = i == 0 ? yT : Ground(mod, cx[i], cz[i], yT, gy[i - 1]);
         bTerrain[i] = Math.abs(gy[i] - yT) < 10.0F;
         if (!bTerrain[i]) {
            m_nBridges++;
         }
      }

      // tronçon i -> suivant, couloir k : bloqué ?
      boolean[][] seg = new boolean[n][];
      Vector a = new Vector();
      Vector b = new Vector();

      for (int i = 0; i < n; i++) {
         int j = t.GetNextPointNum(i);
         if (j == -1) {
            continue;
         }

         seg[i] = new boolean[_LANES];

         for (int k = 0; k < _LANES; k++) {
            float u = (float)k / (_LANES - 1);
            float oi = -lw[i] + (lw[i] + rw[i]) * u;
            float oj = -lw[j] + (lw[j] + rw[j]) * u;
            float ax = cx[i] + rx[i] * oi;
            float az = cz[i] + rz[i] * oi;
            float bx = cx[j] + rx[j] * oj;
            float bz = cz[j] + rz[j] * oj;
            a.Set(ax, (bTerrain[i] ? mod.TerrainHeight(ax, az) : gy[i]) + _RAY_HEIGHT, az);
            b.Set(bx, (bTerrain[j] ? mod.TerrainHeight(bx, bz) : gy[j]) + _RAY_HEIGHT, bz);
            int w = Blocked(mod, a, b);
            seg[i][k] = w != 0;
            if (w == 1) {
               m_nTrees++;
            } else if (w == 2) {
               m_nObjects++;
            } else if (w == 3) {
               m_nRelief++;
            }
         }
      }

      // couloir libre de chaque porte : libre à l'arrivée et au départ, élargi de la demi-largeur d'une voiture
      boolean[] free = new boolean[_LANES];

      for (int i = 0; i < n; i++) {
         int pr = t.GetPrevPointNum(i);
         float w = lw[i] + rw[i];
         float step = w / (_LANES - 1);
         int r = step > 1.0F ? Math.round(_CAR_HALF / step) : 0;

         for (int k = 0; k < _LANES; k++) {
            free[k] = true;
         }

         for (int k = 0; k < _LANES; k++) {
            boolean bHit = seg[i] != null && seg[i][k] || pr != -1 && seg[pr] != null && seg[pr][k];
            if (bHit) {
               for (int m = k - r; m <= k + r; m++) {
                  if (m >= 0 && m < _LANES) {
                     free[m] = false;
                  }
               }
            }
         }

         // le plus long passage libre ; à égalité, le plus près du milieu
         int best = -1;
         int bestLen = 0;
         int bestDist = 999;
         int k = 0;

         while (k < _LANES) {
            if (!free[k]) {
               k++;
            } else {
               int s = k;

               while (k < _LANES && free[k]) {
                  k++;
               }

               int len = k - s;
               int mid = s + k - 1;
               int dist = Math.abs(mid - (_LANES - 1));
               if (len > bestLen || len == bestLen && dist < bestDist) {
                  best = s;
                  bestLen = len;
                  bestDist = dist;
               }
            }
         }

         if (best < 0) {
            m_nClosed++;
            m_aScanKnown[i] = false;
         } else {
            m_aScanKnown[i] = true;
            m_aScanLo[i] = -lw[i] + step * best;
            m_aScanHi[i] = -lw[i] + step * (best + bestLen - 1);
            if (bestLen < _LANES) {
               m_nNarrowed++;
            }
         }
      }
   }

   // distance au point touché
   static float Dist(Collision c, float len) {
      return m_bFraction ? c.fDistance * len : c.fDistance;
   }

   // sol sous (x, z) : le terrain ou une surface d'objet (pont, route), celle qui est le plus près de la porte d'avant
   static float Ground(RacerModule mod, float x, float z, float yT, float yPrev) {
      float best = yT;
      Vector a = new Vector(x, yT + _SKY, z);
      Vector b = new Vector(x, yT - 20.0F, z);

      for (int k = 0; k < _RETRY; k++) {
         float len = a.fY - b.fY;
         if (len <= 1.0F) {
            break;
         }

         Collision c = mod.TraceObjects(a, b);
         if (c == null) {
            break;
         }

         float y = a.fY - Dist(c, len);
         if (c.vNormal != null && Vector.Up.Dot(c.vNormal) > 0.7F && IsSurface(c.Obstacle) && Math.abs(y - yPrev) < Math.abs(best - yPrev)) {
            best = y;
         }

         a.fY = y - 5.0F;
      }

      return best;
   }

   static boolean IsSurface(ControlObject o) {
      if (!(o instanceof TrackObject)) {
         return false;
      }

      int k = ((TrackObject)o).m_iClassID;
      return k == TrackObject._CLASS_PER_FACE || k == TrackObject._CLASS_BRIDGE || k == TrackObject._CLASS_STONE_BRIDGE;
   }

   // premier obstacle entre a et b : 0 aucun, 1 arbre, 2 objet, 3 relief
   static int Blocked(RacerModule mod, Vector a, Vector b) {
      m_nRays++;
      if (mod.TraceTrees(a, b) != null) {
         return 1;
      }

      float dx = b.fX - a.fX;
      float dy = b.fY - a.fY;
      float dz = b.fZ - a.fZ;
      float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (len > 1.0F) {
         // les objets sans collision (déclencheurs, panneaux, bonus) arrêtent le rayon : on repart juste derrière
         Vector s = new Vector(a.fX, a.fY, a.fZ);
         float done = 0.0F;

         for (int k = 0; k < _RETRY; k++) {
            Collision c = mod.TraceObjects(s, b);
            if (c == null) {
               break;
            }

            if (IsObstacle(c)) {
               return 2;
            }

            done += Dist(c, len - done) + 10.0F;
            if (done >= len) {
               break;
            }

            float f = done / len;
            s.Set(a.fX + dx * f, a.fY + dy * f, a.fZ + dz * f);
         }
      }

      Collision c = mod.TraceTerrain(a, b);
      if (c != null && c.vNormal != null && Vector.Up.Dot(c.vNormal) < _WALL) {
         return 3;
      }

      return 0;
   }

   // ce qui arrête une voiture (Vehicle.OnCollisionFullInfoMove) : arbres, poteaux, murs et objets du décor raides ;
   // pas les bonus, les voitures, les déclencheurs ni le sol
   static boolean IsObstacle(Collision c) {
      if (!(c.Obstacle instanceof TrackObject)) {
         return false;
      }

      int k = ((TrackObject)c.Obstacle).m_iClassID;
      if (k == TrackObject._CLASS_TREE || k == TrackObject._CLASS_POLE) {
         return true;
      }

      if (k == TrackObject._CLASS_PER_FACE || k == TrackObject._CLASS_BRIDGE || k == TrackObject._CLASS_STONE_BRIDGE
         || k == TrackObject._CLASS_UNKNOWN) {
         return c.vNormal == null || Vector.Up.Dot(c.vNormal) < _WALL;
      }

      return false;
   }

   // --- pour VehicleAI ---

   static boolean Known(int i) {
      return m_aKnown != null && i >= 0 && i < m_aKnown.length && m_aKnown[i];
   }

   static float Clamp(int i, float off) {
      if (!Known(i)) {
         return off;
      }

      return off < m_aLo[i] ? m_aLo[i] : (off > m_aHi[i] ? m_aHi[i] : off);
   }

   static float Mid(int i) {
      return Known(i) ? 0.5F * (m_aLo[i] + m_aHi[i]) : 0.0F;
   }

   static boolean HasWall(int i) {
      return m_aLearnLo[i] > -_NONE || m_aLearnHi[i] < _NONE;
   }

   // couloir de la porte i sans les murs appris : le couloir libre mesuré, ou la porte
   static float BaseLo(int i) {
      return m_aScanKnown[i] ? m_aScanLo[i] : -m_aLW[i] * _GATE_USE;
   }

   static float BaseHi(int i) {
      return m_aScanKnown[i] ? m_aScanHi[i] : m_aRW[i] * _GATE_USE;
   }

   // couloir à suivre = couloir mesuré, resserré par les murs appris, jamais moins de _MIN_CORRIDOR
   static void Combine(int i) {
      float lo = BaseLo(i);
      float hi = BaseHi(i);
      if (m_aLearnLo[i] > lo) {
         lo = m_aLearnLo[i];
      }

      if (m_aLearnHi[i] < hi) {
         hi = m_aLearnHi[i];
      }

      if (hi - lo < _MIN_CORRIDOR) {
         float c = 0.5F * (lo + hi);
         lo = c - 0.5F * _MIN_CORRIDOR;
         hi = c + 0.5F * _MIN_CORRIDOR;
      }

      m_aLo[i] = lo;
      m_aHi[i] = hi;
      m_aKnown[i] = m_aScanKnown[i] || HasWall(i);
   }

   // Choc d'une IA en (x, z) contre le décor ; (nx, nz) : la normale du choc, qui part de l'obstacle vers la voiture.
   // Le bord du couloir de la porte i, du côté de l'obstacle, recule à fStep de la voiture (sans laisser moins de
   // _MIN_CORRIDOR). Vrai si la porte a appris quelque chose.
   static boolean Learn(int i, float x, float z, float nx, float nz, float fStep) {
      if (m_aKnown == null || i < 0 || i >= m_aKnown.length) {
         return false;
      }

      float h = (float)Math.sqrt(nx * nx + nz * nz);
      if (h < 0.001F) {
         return false;
      }

      // > 0 : l'obstacle est à droite de la voiture (la normale pointe vers la gauche de la porte)
      float s = -(nx * m_aRX[i] + nz * m_aRZ[i]) / h;
      if (Math.abs(s) < 0.1F) {
         return false; // choc pile de face : on ne sait pas de quel côté passer, les points dangereux s'en chargent
      }

      float lat = (x - m_aCX[i]) * m_aRX[i] + (z - m_aCZ[i]) * m_aRZ[i];
      if (lat < -m_aLW[i] * 1.5F - 50.0F || lat > m_aRW[i] * 1.5F + 50.0F) {
         return false; // loin de cette porte (raccourci, voiture éjectée) : rien à apprendre ici
      }

      if (s > 0.0F) {
         float b = Math.min(lat, m_aHi[i]) - fStep;
         float floor = Math.max(BaseLo(i), m_aLearnLo[i]) + _MIN_CORRIDOR;
         if (b < floor) {
            b = floor;
         }

         if (!(b < m_aLearnHi[i] - 1.0F)) {
            return false;
         }

         m_aLearnHi[i] = b;
      } else {
         float b = Math.max(lat, m_aLo[i]) + fStep;
         float ceil = Math.min(BaseHi(i), m_aLearnHi[i]) - _MIN_CORRIDOR;
         if (b > ceil) {
            b = ceil;
         }

         if (!(b > m_aLearnLo[i] + 1.0F)) {
            return false;
         }

         m_aLearnLo[i] = b;
      }

      Combine(i);
      m_nLearnedRace++;
      return true;
   }

   // table des dangers d'une nouvelle course : ceux des courses d'avant, un peu oubliés
   static float[] StartDanger(Track t) {
      For(RacerModule.m_Mod, t);
      int n = t != null ? t.m_TrackPoints.size() : 0;
      float[] a = new float[n];

      for (int i = 0; m_Track == t && m_aSaved != null && i < n && i < m_aSaved.length; i++) {
         a[i] = m_aSaved[i];
      }

      return a;
   }

   // --- aimap.dat : deux lignes par circuit et par sens ---
   // « circuit|sens|portes=d0,d1,… » : dangers en centièmes
   // « circuit|sens|portes|murs=i:gauche:droite,… » : murs appris (décalages arrondis, x = rien de ce côté)

   static String LoadLine(String sKey) {
      File f = new File(_SAVE_FILE);
      if (!f.exists()) {
         return null;
      }

      String r = null;

      try {
         BufferedReader in = new BufferedReader(new FileReader(f));

         for (String s = in.readLine(); s != null; s = in.readLine()) {
            int e = s.indexOf('=');
            if (e > 0 && s.substring(0, e).equals(sKey)) {
               r = s.substring(e + 1);
            }
         }

         in.close();
      } catch (Exception e) {
         GameObject.CrashLog("[mod] IA carte : lecture de " + _SAVE_FILE + " impossible : " + e + "\n");
      }

      return r;
   }

   static float[] LoadDanger(String sKey, int n) {
      float[] a = new float[n];
      String v = LoadLine(sKey);
      int i = 0;
      int p = 0;

      while (v != null && p <= v.length() && i < n) {
         int q = v.indexOf(',', p);
         if (q < 0) {
            q = v.length();
         }

         try {
            a[i] = Integer.parseInt(v.substring(p, q).trim()) / 100.0F * _KEEP;
         } catch (NumberFormatException ex) {
            a[i] = 0.0F;
         }

         i++;
         p = q + 1;
      }

      return a;
   }

   static void LoadWalls(String sKey, int n) {
      m_aLearnLo = new float[n];
      m_aLearnHi = new float[n];

      for (int i = 0; i < n; i++) {
         m_aLearnLo[i] = -_NONE;
         m_aLearnHi[i] = _NONE;
      }

      String v = LoadLine(sKey + "|murs");
      int p = 0;

      while (v != null && p < v.length()) {
         int q = v.indexOf(',', p);
         if (q < 0) {
            q = v.length();
         }

         String e = v.substring(p, q).trim();
         int c1 = e.indexOf(':');
         int c2 = c1 < 0 ? -1 : e.indexOf(':', c1 + 1);
         if (c2 > 0) {
            try {
               int i = Integer.parseInt(e.substring(0, c1));
               String lo = e.substring(c1 + 1, c2);
               String hi = e.substring(c2 + 1);
               if (i >= 0 && i < n) {
                  if (!lo.equals("x")) {
                     m_aLearnLo[i] = Integer.parseInt(lo);
                  }

                  if (!hi.equals("x")) {
                     m_aLearnHi[i] = Integer.parseInt(hi);
                  }
               }
            } catch (NumberFormatException ex) {
            }
         }

         p = q + 1;
      }
   }

   // fin de course (RacerGame.EndModule, arrivée ou Échap) : la mémoire de ce circuit dans aimap.dat.
   // d : les dangers de la course, null si aucune IA améliorée n'a couru (on garde alors ceux d'avant)
   static void SaveRace(float[] d) {
      if (m_sKey == null || m_aLo == null) {
         return;
      }

      if (d == null || d.length != m_aLo.length) {
         d = m_aSaved;
      }

      StringBuffer sb = new StringBuffer();
      int nDanger = 0;

      for (int i = 0; i < d.length; i++) {
         if (i > 0) {
            sb.append(',');
         }

         int v = Math.round(d[i] * 100.0F);
         sb.append(v);
         if (v > 0) {
            nDanger++;
         }
      }

      StringBuffer sw = new StringBuffer();
      int nWalls = 0;

      for (int i = 0; i < m_aLearnLo.length; i++) {
         if (HasWall(i)) {
            if (nWalls > 0) {
               sw.append(',');
            }

            nWalls++;
            sw.append(i).append(':');
            sw.append(m_aLearnLo[i] > -_NONE ? String.valueOf(Math.round(m_aLearnLo[i])) : "x").append(':');
            sw.append(m_aLearnHi[i] < _NONE ? String.valueOf(Math.round(m_aLearnHi[i])) : "x");
         }
      }

      String kD = m_sKey;
      String kW = m_sKey + "|murs";
      StringBuffer all = new StringBuffer();

      try {
         File f = new File(_SAVE_FILE);
         if (f.exists()) {
            BufferedReader in = new BufferedReader(new FileReader(f));

            for (String s = in.readLine(); s != null; s = in.readLine()) {
               int e = s.indexOf('=');
               String k = e > 0 ? s.substring(0, e) : "";
               if (!k.equals(kD) && !k.equals(kW) && s.trim().length() > 0) {
                  all.append(s).append("\r\n");
               }
            }

            in.close();
         }

         all.append(kD).append('=').append(sb.toString()).append("\r\n");
         if (nWalls > 0) {
            all.append(kW).append('=').append(sw.toString()).append("\r\n");
         }

         FileWriter out = new FileWriter(_SAVE_FILE);
         out.write(all.toString());
         out.close();
         GameObject.CrashLog(
            "[mod] IA carte " + m_sKey + " : memoire enregistree, murs appris a " + nWalls + " portes (" + m_nLearnedRace
               + " lecons cette course), points dangereux " + nDanger + "\n"
         );
      } catch (Exception e) {
         GameObject.CrashLog("[mod] IA carte : ecriture de " + _SAVE_FILE + " impossible : " + e + "\n");
      }

      m_nLearnedRace = 0;
   }
}
