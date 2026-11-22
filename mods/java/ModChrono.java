import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

// [mod] « IA chrono » (notes/ia.md, « IA chrono ») : pendant l'entraînement (mods\entrainer-ia.ps1 -Chrono), les IA
// cherchent le meilleur plan de course de chaque circuit, par évolution. Le pilote reste celui de l'IA améliorée
// (VehicleAI : portes, freinage, dégagement) ; ce qui apprend, c'est son plan, porte par porte :
//   décalage  : où passer dans la porte (> 0 vers la droite), dans le couloir libre de la carte (ModAIMap)
//   limite    : vitesse de passage permise (part de la vitesse maxi, 1 = pas de limite)
//   prudence  : x prudence au lâcher d'accélérateur avant le virage (VehicleAI.ModSpeedControl), 1 = comme l'IA
// À chaque course, 6 voitures du même animal, traversables (drapeau 32 retiré : elles ne se gênent pas), sans hasard
// de conduite : deux voitures (d'autres à chaque course) roulent avec le plan de base (la référence de la course ; leur
// écart dit le bruit), jusqu'à 2 voitures revérifient les essais prometteurs de la course d'avant, les autres essaient
// une variante (un bout de piste de 3 à 9 portes modifié au hasard). Chaque essai est jugé sur son écart à la base de
// la MÊME course : le jeu ralentit quand il tourne longtemps, toutes les voitures pareil (2026-11-21, +6 à 9 % en 7 h),
// une moyenne des courses d'avant faisait paraître chaque essai plus lent. Après une amélioration, la 2e voiture de
// base roule _PREV_CHECKS courses avec la base d'avant (« ancienne ») : la nouvelle plus lente qu'elle est annulée. Note d'un plan = moyenne des tours lancés (entre deux passages de la porte du milieu de
// la piste, à l'image près, pas au 0,1 s du chrono du jeu) + _HIT_PENALTY par choc contre le décor + _BAD_PENALTY par
// remise en piste ou dégagement, + le départ (de la ligne à la porte du milieu, au 1er tour : temps par rapport à la
// médiane des voitures de la course, chocs et remises en piste pénalisés pareil) réparti sur les tours lancés ; des
// portes sautées sans remise en piste (raccourci) = échec. Une variante sur deux
// est centrée juste avant un endroit où les voitures tapent (m_aHot). Un essai meilleur que la base est revérifié une
// fois ; confirmé, il entre dans la base. Une base qui se révèle moins bonne que la précédente est annulée.
// 1re course d'un circuit : les 6 voitures roulent avec la même base (mesure du bruit : écart entre plans identiques).
// Bornes (2026-11-22) : entre deux portes, la voiture va tout droit d'une cible du plan à la suivante ; quand les portes
// sont loin, cette ligne droite peut traverser le décor que la carte (ModAIMap : couloirs parallèles aux portes) ne
// voit pas. Tropiques 1 portes 9-10 et variante 40-41 (10 km) : l'îlot d'arbres entre les deux routes, ~55 blocages
// par course ; Désert 1 variante 39-40 (14,5 km) : le bord de la route rejoint la ligne 2 km avant la porte 40, les
// voitures le frottent puis s'y coincent. Chaque choc d'une voiture près de sa ligne note l'endroit et le côté libre
// (normale du choc) ; _BORNE_ACTIVE chocs au même endroit font une borne, et tous les plans (base, essais) passent à
// _BORNE_CLEAR au moins de chaque borne, de son côté libre (décalages des deux portes du tronçon poussés, dans leur
// couloir). Gardées pour toujours dans aichrono.dat (bornes=).
// Mémoire à part : aichrono.dat (celle des IA du jeu, aimap.dat, n'est ni lue en écriture ni enregistrée en mode chrono).
class ModChrono {
   static final String _FILE = "aichrono.dat";
   static final int _MAX_CARS = 8;
   static final int _MAX_PASS = 16;
   static final float _FAIL = 9999.0F;
   // un choc coûte déjà 3 à 8 s en vrai (Forêt 1, 2026-11-21) ; la pénalité en plus préfère les plans qui n'en ont pas
   // (un plan qui a tapé et s'en est bien sorti une fois retapera)
   static final float _HIT_PENALTY = 1.0F;   // s par choc contre le décor pendant un tour lancé
   static final float _BAD_PENALTY = 3.0F;   // s par remise en piste ou dégagement (en plus du temps perdu)
   // plan de départ : 2 = avec la prudence des points dangereux appris par les IA du jeu (aimap.dat). Avant (1), le plan
   // les ignorait : Désert 1 variante, 2026-11-21, trop vite au virage de la porte 39, dans le sable et contre les
   // bords jusqu'à la porte 40, ~11 chocs par tour, tours de 90 s contre 65 s pour l'IA normale
   static final int _INIT_VERSION = 2;
   static final int _SCORE_VERSION = 3;      // notation de la base dans aichrono.dat ; plus ancienne : base remesurée
   static final float _HOT_DECAY = 0.8F;     // chocs aux portes : mémoire des dernières courses
   static final float _HOT_SHARE = 0.5F;     // part des essais centrés juste avant un endroit où les voitures tapent
   static final float _MIN_GAIN = 0.02F;     // s par tour : gain minimum pour retenir un essai
   static final float _SCREEN_K = 0.5F;      // gain > _SCREEN_K x bruit pour être revérifié
   static final float _CONFIRM_K = 0.5F;     // gain moyen (2 mesures) > _CONFIRM_K x bruit pour entrer dans la base
   static final float _UNDO_K = 1.0F;        // base nouvelle plus lente que l'ancienne de _UNDO_K x bruit : annulée
   static final int _BASE_AVG = 4;           // moyenne glissante de la base sur 4 courses
   static final int _MAX_RETEST = 2;
   static final int _SELECT_VERSION = 2;     // 2 : essais jugés contre la base de la même course ; avant : contre la
                                             // moyenne des courses d'avant (bruit gonflé par le ralentissement du jeu)
   static final int _PREV_CHECKS = 3;        // courses de contrôle d'une amélioration contre la base d'avant
   static final int _NOISE_WARM = 5;         // mesures du bruit moyennées simplement avant la moyenne glissante
   static final float _SIGMA_OFF = 0.25F;    // x demi-couloir
   static final float _SIGMA_LIM = 0.05F;    // part de la vitesse maxi
   static final float _SIGMA_CAUT = 0.25F;   // en log
   static final float _LIM_MIN = 0.4F;
   static final float _CAUT_MIN = 0.3F;
   static final float _CAUT_MAX = 2.5F;
   static final int _MAX_BORNES = 96;        // par circuit (les moins sûres remplacées)
   static final int _MAX_RACE_HITS = 600;    // chocs notés par course
   static final float _BORNE_NEAR = 250.0F;  // choc plus loin que ça de la ligne de la voiture : elle n'y était plus
   static final float _BORNE_MERGE = 200.0F; // chocs à moins de ça : la même borne
   static final float _BORNE_CLEAR = 160.0F; // la ligne passe au moins à ça de la voiture qui a tapé, côté libre
   static final int _BORNE_ACTIVE = 2;       // chocs pour qu'une borne compte

   static boolean m_bOn = false;             // ModTraining : Chrono(1) dans entrainement.scr
   static int m_nAnimal = 8;                 // Ours (stats équilibrées) ; Animal("…") dans entrainement.scr
   static java.util.Random m_Rand = new java.util.Random();

   // plan de base du circuit en cours
   static String m_sKey = null;
   static int m_n = 0;
   static float[] m_aOff = null;
   static float[] m_aLim = null;
   static float[] m_aCaut = null;
   static float m_fBase = 0.0F;              // tour lancé moyen de la base (s)
   static int m_nBaseEvals = 0;              // mesures dans cette moyenne (0 : base jamais mesurée)
   static float m_fNoiseVar = 0.0F;          // bruit d'une mesure (variance, s²)
   static boolean m_bNoiseKnown = false;
   static float m_fRecord = 0.0F;            // meilleur tour lancé jamais mesuré (s), 0 = aucun
   static float m_fBestBase = 0.0F;          // meilleure base mesurée (s)
   static int m_nRaces = 0;
   static int m_nGen = 0;                    // essais entrés dans la base
   static int m_nTried = 0;
   // base d'avant le dernier changement (annulation)
   static float[] m_aPrevOff = null;
   static float[] m_aPrevLim = null;
   static float[] m_aPrevCaut = null;
   static float m_fPrevBase = 0.0F;
   static boolean m_bHasPrev = false;
   static int m_nPrevChecks = 0;             // courses de contrôle faites (voiture « ancienne » : la base d'avant)
   static int m_nPrevN = 0;                  // dont mesurées (base et ancienne notées toutes les deux)
   static float m_fPrevDiff = 0.0F;          // somme des écarts base - ancienne (s)
   static int m_nNoiseN = 0;                 // mesures du bruit (écart entre les 2 voitures de base d'une course)
   // essais à revérifier : bout de piste a..b (indices circulaires) + valeurs dans ce bout
   static java.util.Vector m_Pending = new java.util.Vector();
   static float[] m_aHot = null;             // chocs récents à l'approche de chaque porte (moyenne par voiture, décroissante)
   // circuits quittés (l'entraînement change de circuit à chaque course) : essais à revérifier et base d'avant, par clé
   static java.util.Hashtable m_Mem = new java.util.Hashtable();
   // bornes du circuit en cours : tronçon (porte visée), position de la voiture au choc, côté libre (unitaire), chocs
   static int m_nBornes = 0;
   static int[] m_aBGate = new int[_MAX_BORNES];
   static float[] m_aBX = new float[_MAX_BORNES];
   static float[] m_aBZ = new float[_MAX_BORNES];
   static float[] m_aBUX = new float[_MAX_BORNES];
   static float[] m_aBUZ = new float[_MAX_BORNES];
   static int[] m_aBW = new int[_MAX_BORNES];
   static int m_nBornesNew = 0;              // bornes devenues actives à la dernière course
   static int m_nBornesFixed = 0;            // portes de la base déplacées par les bornes à la dernière préparation
   static int m_nBornesBad = 0;              // bornes que la base ne peut pas respecter (couloir trop étroit)
   // chocs de la course en cours (même forme)
   static int m_nRaceHits = 0;
   static int[] m_aHGate = new int[_MAX_RACE_HITS];
   static float[] m_aHX = new float[_MAX_RACE_HITS];
   static float[] m_aHZ = new float[_MAX_RACE_HITS];
   static float[] m_aHUX = new float[_MAX_RACE_HITS];
   static float[] m_aHUZ = new float[_MAX_RACE_HITS];

   // course en cours
   static RaceParams m_Race = null;
   static boolean m_bBuilding = false;       // base en construction : VehicleAI.ModLineOffset sans plan
   static Trial[] m_aCar = new Trial[_MAX_CARS];
   static float[][] m_aPass = new float[_MAX_CARS][_MAX_PASS]; // passages de la porte de référence
   static int[][] m_aPassHits = new int[_MAX_CARS][_MAX_PASS];
   static int[][] m_aPassBad = new int[_MAX_CARS][_MAX_PASS];  // remises en piste + dégagements
   static int[][] m_aPassGates = new int[_MAX_CARS][_MAX_PASS]; // portes passées depuis le passage d'avant
   static int[] m_nPass = new int[_MAX_CARS];
   static int[] m_nGates = new int[_MAX_CARS];
   static int[][] m_aHitAt = new int[_MAX_CARS][];               // chocs de la course, par porte visée
   static int m_iRef = 0;
   static String m_sLastSummary = "";
   static String m_sLastHistory = "";

   static final int _BASE = 0;
   static final int _RETEST = 1;
   static final int _NEW = 2;
   static final int _PREV = 3;               // la base d'avant la dernière amélioration (contrôle)
   static final String[] _ROLE_NAMES = new String[]{"base", "verif", "essai", "ancienne"};

   // une variante : le plan complet de la course + le bout de piste modifié
   static class Trial {
      int m_nRole = _BASE;
      int m_a = 0;            // bout modifié : portes a, a+1, …, a+len-1 (modulo n)
      int m_len = 0;
      float[] m_aOff;
      float[] m_aLim;
      float[] m_aCaut;
      float m_fFirst = 0.0F;  // essai revérifié : sa 1re mesure
      String m_sWhat = "";    // ce qui a changé (journal)
      float m_fScore = _FAIL;
      float m_fBestLap = 0.0F;
      int m_nLaps = 0;
      int m_nHits = 0;
      int m_nBad = 0;         // remises en piste + dégagements pendant les tours lancés
      float m_fStart = -1.0F; // départ : de la ligne (1er passage) à la porte de référence, -1 si pas mesuré
      int m_nStartHits = 0;
      int m_nStartBad = 0;
      float m_fStartCost = 0.0F; // départ par rapport aux autres voitures de la course + pénalités (s, une fois par course)
      String m_sWhy = "";
   }

   // ModTraining (ligne A de l'historique) : rôle de la voiture k dans la dernière course
   static String RoleName(int k) {
      Trial t = k >= 0 && k < _MAX_CARS ? m_aCar[k] : null;
      return t != null ? _ROLE_NAMES[t.m_nRole] : "";
   }

   // VehicleAI.ModOnObstacle (w = 1) et ModStartReverse (w = 3) : choc ou blocage en allant vers la porte i
   static void OnHit(VehicleAI ai, int i, int w) {
      int k = ai.m_Vehicle.m_Index;
      if (TrialOf(ai) == null || m_aHitAt[k] == null || i < 0 || i >= m_aHitAt[k].length) {
         return;
      }

      m_aHitAt[k][i] += w;
   }

   // VehicleAI.ModOnObstacle : choc à (x, z) en allant vers la porte b ; vNormal de l'obstacle vers la voiture (ou null)
   static void OnObstacle(VehicleAI ai, int b, float x, float z, Vector vNormal) {
      Trial t = TrialOf(ai);
      if (t == null || m_nRaceHits >= _MAX_RACE_HITS || b < 0 || b >= m_n || ModAIMap.m_aCX == null || ModAIMap.m_aCX.length != m_n) {
         return;
      }

      int a = (b - 1 + m_n) % m_n;
      float ax = PX(a, t.m_aOff[a]);
      float az = PZ(a, t.m_aOff[a]);
      float dx = PX(b, t.m_aOff[b]) - ax;
      float dz = PZ(b, t.m_aOff[b]) - az;
      float len = (float)Math.sqrt(dx * dx + dz * dz);
      if (len < 1.0F) {
         return;
      }

      dx /= len;
      dz /= len;
      float rx = -dz;
      float rz = dx;
      float s = ((x - ax) * dx + (z - az) * dz) / len;
      float lat = (x - ax) * rx + (z - az) * rz;
      if (s < 0.0F || s > 1.0F || Math.abs(lat) > _BORNE_NEAR) {
         return;
      }

      // côté libre : celui de la normale du choc (frottement contre un bord) ; de face (arbre pile sur la ligne), le
      // côté où était la voiture
      float side = 0.0F;
      if (vNormal != null) {
         float h = (float)Math.sqrt(vNormal.fX * vNormal.fX + vNormal.fZ * vNormal.fZ);
         float nr = h > 0.1F ? (vNormal.fX * rx + vNormal.fZ * rz) / h : 0.0F;
         if (Math.abs(nr) >= 0.3F) {
            side = nr > 0.0F ? 1.0F : -1.0F;
         }
      }

      if (side == 0.0F && Math.abs(lat) >= 30.0F) {
         side = lat > 0.0F ? 1.0F : -1.0F;
      }

      if (side == 0.0F) {
         return;
      }

      int h = m_nRaceHits++;
      m_aHGate[h] = b;
      m_aHX[h] = x;
      m_aHZ[h] = z;
      m_aHUX[h] = side * rx;
      m_aHUZ[h] = side * rz;
   }

   // cible du plan à la porte i, décalage o
   static float PX(int i, float o) {
      return ModAIMap.m_aCX[i] + ModAIMap.m_aRX[i] * o;
   }

   static float PZ(int i, float o) {
      return ModAIMap.m_aCZ[i] + ModAIMap.m_aRZ[i] * o;
   }

   // fin de course : les chocs rejoignent la borne la plus proche (même tronçon, même côté libre) ou en font une
   static void LearnBornes() {
      m_nBornesNew = 0;
      for (int h = 0; h < m_nRaceHits; h++) {
         int best = -1;
         float bd = _BORNE_MERGE * _BORNE_MERGE;
         for (int k = 0; k < m_nBornes; k++) {
            if (m_aBGate[k] == m_aHGate[h] && m_aBUX[k] * m_aHUX[h] + m_aBUZ[k] * m_aHUZ[h] > 0.5F) {
               float ex = m_aBX[k] - m_aHX[h];
               float ez = m_aBZ[k] - m_aHZ[h];
               if (ex * ex + ez * ez < bd) {
                  bd = ex * ex + ez * ez;
                  best = k;
               }
            }
         }

         if (best >= 0) {
            int w = m_aBW[best];
            float f = 1.0F / Math.min(w + 1, 20);
            m_aBX[best] += (m_aHX[h] - m_aBX[best]) * f;
            m_aBZ[best] += (m_aHZ[h] - m_aBZ[best]) * f;
            float ux = m_aBUX[best] + (m_aHUX[h] - m_aBUX[best]) * f;
            float uz = m_aBUZ[best] + (m_aHUZ[h] - m_aBUZ[best]) * f;
            float l = (float)Math.sqrt(ux * ux + uz * uz);
            if (l > 0.01F) {
               m_aBUX[best] = ux / l;
               m_aBUZ[best] = uz / l;
            }

            m_aBW[best] = Math.min(9999, w + 1);
            if (w + 1 == _BORNE_ACTIVE) {
               m_nBornesNew++;
            }
            continue;
         }

         int k = m_nBornes;
         if (k >= _MAX_BORNES) {
            // plein : remplace une borne d'un seul choc (jamais confirmée), sinon le choc est oublié
            k = -1;
            for (int j = 0; j < m_nBornes; j++) {
               if (m_aBW[j] < _BORNE_ACTIVE) {
                  k = j;
                  break;
               }
            }

            if (k < 0) {
               continue;
            }
         } else {
            m_nBornes++;
         }

         m_aBGate[k] = m_aHGate[h];
         m_aBX[k] = m_aHX[h];
         m_aBZ[k] = m_aHZ[h];
         m_aBUX[k] = m_aHUX[h];
         m_aBUZ[k] = m_aHUZ[h];
         m_aBW[k] = 1;
      }

      m_nRaceHits = 0;
   }

   static int ActiveBornes() {
      int n = 0;
      for (int k = 0; k < m_nBornes; k++) {
         if (m_aBW[k] >= _BORNE_ACTIVE) {
            n++;
         }
      }

      return n;
   }

   // le plan (décalages off) respecte les bornes : chaque tronçon passe à _BORNE_CLEAR au moins de chaque borne, de son
   // côté libre ; les décalages des deux portes du tronçon bougent le moins possible, dans leur couloir. Renvoie le
   // nombre de bornes encore trop près (couloir trop étroit, ou deux bornes qui se contredisent)
   static int FixPlan(float[] off) {
      if (off == null || off.length != m_n || m_nBornes == 0 || ModAIMap.m_aCX == null || ModAIMap.m_aCX.length != m_n) {
         return 0;
      }

      int nBad = 0;
      for (int pass = 0; pass < 16; pass++) {
         boolean bMoved = false;
         nBad = 0;
         for (int k = 0; k < m_nBornes; k++) {
            if (m_aBW[k] < _BORNE_ACTIVE) {
               continue;
            }

            int b = m_aBGate[k];
            int a = (b - 1 + m_n) % m_n;
            float ax = PX(a, off[a]);
            float az = PZ(a, off[a]);
            float dx = PX(b, off[b]) - ax;
            float dz = PZ(b, off[b]) - az;
            float len = (float)Math.sqrt(dx * dx + dz * dz);
            if (len < 1.0F) {
               continue;
            }

            dx /= len;
            dz /= len;
            float rx = -dz;
            float rz = dx;
            float s = ((m_aBX[k] - ax) * dx + (m_aBZ[k] - az) * dz) / len;
            if (s < 0.0F || s > 1.0F) {
               continue;
            }

            float sg = m_aBUX[k] * rx + m_aBUZ[k] * rz >= 0.0F ? 1.0F : -1.0F;
            float need = _BORNE_CLEAR - ((ax - m_aBX[k]) * rx + (az - m_aBZ[k]) * rz) * sg;
            if (need <= 1.0F) {
               continue;
            }

            float ca = (1.0F - s) * (ModAIMap.m_aRX[a] * rx + ModAIMap.m_aRZ[a] * rz) * sg;
            float cb = s * (ModAIMap.m_aRX[b] * rx + ModAIMap.m_aRZ[b] * rz) * sg;
            float q = ca * ca + cb * cb;
            float oa = q > 0.0001F ? Clamp(off[a] + need * ca / q, Lo(a), Hi(a)) : off[a];
            float ob = q > 0.0001F ? Clamp(off[b] + need * cb / q, Lo(b), Hi(b)) : off[b];
            if (Math.abs(oa - off[a]) + Math.abs(ob - off[b]) < 0.5F) {
               nBad++;
               continue;
            }

            off[a] = oa;
            off[b] = ob;
            bMoved = true;
         }

         if (!bMoved) {
            break;
         }
      }

      return nBad;
   }

   static float Clamp(float v, float lo, float hi) {
      return v < lo ? lo : (v > hi ? hi : v);
   }

   static void Log(String s) {
      GameObject.CrashLog("[mod] chrono " + s + "\n");
   }

   // ------------------------------------------------------------------ conduite (VehicleAI)

   // VehicleAI.ModStart, mode chrono : le plan de cette voiture (le premier appel d'une course prépare les 6)
   static void Begin(VehicleAI ai) {
      RacerModule mod = RacerModule.m_Mod;
      if (mod == null || mod.m_RaceParams == null) {
         return;
      }

      ai.m_Vehicle.ClearFlag(32); // traversable : les voitures ne se gênent pas
      if (m_Race != mod.m_RaceParams) {
         m_Race = mod.m_RaceParams;
         try {
            PrepareRace(ai);
         } catch (Throwable e) {
            Log("erreur au depart : " + e);
            m_aOff = null;
         }
      }
   }

   static Trial TrialOf(VehicleAI ai) {
      int k = ai.m_Vehicle.m_Index;
      return m_aOff != null && RacerModule.m_Mod != null && m_Race == RacerModule.m_Mod.m_RaceParams && k >= 0 && k < _MAX_CARS ? m_aCar[k] : null;
   }

   // décalage du plan à la porte i, NaN sans plan (l'IA calcule alors le sien)
   static float Offset(VehicleAI ai, int i) {
      Trial t = m_bBuilding ? null : TrialOf(ai);
      return t != null && i >= 0 && i < t.m_aOff.length ? t.m_aOff[i] : Float.NaN;
   }

   static float Limit(VehicleAI ai, int i) {
      Trial t = TrialOf(ai);
      return t != null && i >= 0 && i < t.m_aLim.length ? t.m_aLim[i] : 1.0F;
   }

   static float Caution(VehicleAI ai, int i) {
      Trial t = TrialOf(ai);
      return t != null && i >= 0 && i < t.m_aCaut.length ? t.m_aCaut[i] : 1.0F;
   }

   // VehicleAI.ModOnGatePassed : porte i franchie à l'instant now (temps du jeu)
   static void OnGate(VehicleAI ai, int i, float now) {
      int k = ai.m_Vehicle.m_Index;
      if (TrialOf(ai) == null) {
         return;
      }

      m_nGates[k]++;
      if (i != m_iRef || m_nPass[k] >= _MAX_PASS) {
         return;
      }

      int p = m_nPass[k]++;
      m_aPass[k][p] = now;
      m_aPassHits[k][p] = ai.m_nModObstacles;
      m_aPassBad[k][p] = ai.m_nModRespawn + ai.m_nModStuck + ai.m_nModWarp; // warp : posée plus loin par le jeu
      m_aPassGates[k][p] = m_nGates[k];
      m_nGates[k] = 0;
   }

   // ------------------------------------------------------------------ préparation d'une course

   static void PrepareRace(VehicleAI ai) {
      String sKey = ModAIMap.m_sKey + "|" + ModAnimals._NAMES[m_nAnimal];
      Track tr = ai.m_Track;
      int n = tr.m_TrackPoints.size();
      if (!sKey.equals(m_sKey) || m_n != n) {
         Load(sKey, n);
      }

      if (m_aOff == null) {
         // pas encore de plan : celui que l'IA améliorée suivrait sans hasard
         m_aOff = new float[n];
         m_aLim = new float[n];
         m_aCaut = new float[n];
         m_bBuilding = true;

         for (int i = 0; i < n; i++) {
            m_aOff[i] = ai.ModLineOffset(i);
            m_aLim[i] = ModAIMap.Speed(tr, i);
            // comme VehicleAI.ModSpeedControl hors chrono : prudence x (1 + 0,8 x danger de la porte visée ou de la suivante)
            float d = Math.min(1.0F, Math.max(ModAIMap.Saved(tr, i), ModAIMap.Saved(tr, (i + 1) % n)));
            m_aCaut[i] = Math.min(_CAUT_MAX, 1.0F + VehicleAI._MOD_DANGER_CAUTION * d);
         }

         m_bBuilding = false;
         m_nBaseEvals = 0;
         Log(sKey + " : nouveau plan (trajectoire et vitesses de l'IA experte)");
      }

      // bornes : la base (et la base d'avant, contrôle) contournent les endroits où les voitures tapaient
      m_nRaceHits = 0;
      float[] before = Dup(m_aOff);
      m_nBornesBad = FixPlan(m_aOff);
      m_nBornesFixed = 0;
      StringBuffer sFixed = new StringBuffer();
      for (int i = 0; i < n; i++) {
         if (Math.abs(m_aOff[i] - before[i]) >= 1.0F) {
            m_nBornesFixed++;
            sFixed.append(sFixed.length() > 0 ? ", " : "").append(i).append(' ').append(m_aOff[i] > before[i] ? "+" : "").append(Math.round(m_aOff[i] - before[i]));
         }
      }

      if (m_bHasPrev && m_aPrevOff != null && m_aPrevOff.length == n) {
         FixPlan(m_aPrevOff);
      }

      if (m_nBornes > 0) {
         Log(sKey + " : " + ActiveBornes() + " borne(s) sur " + m_nBornes + " endroits de choc"
            + (m_nBornesFixed > 0 ? ", base deplacee aux portes " + sFixed.toString() : "") + (m_nBornesBad > 0 ? ", " + m_nBornesBad + " borne(s) impossibles a eviter" : ""));
      }

      m_iRef = n / 2;
      if (m_aHot == null || m_aHot.length != n) {
         m_aHot = new float[n];
      }

      int nCars = Math.min(_MAX_CARS, m_Race.m_AITab.size());
      for (int k = 0; k < _MAX_CARS; k++) {
         m_nPass[k] = 0;
         m_nGates[k] = 0;
         m_aCar[k] = null;
         m_aHitAt[k] = new int[n];
      }

      boolean bCalib = m_nBaseEvals == 0;
      boolean bPrev = !bCalib && m_bHasPrev && m_aPrevOff != null && m_aPrevOff.length == n && m_nPrevChecks < _PREV_CHECKS;
      int nRetest = 0;
      // la base change de voiture à chaque course (place sur la grille de départ : les voitures 3-5 faisaient un 1er
      // tour 1,4 s plus rapide et tapaient moins au départ, Forêt 1, 2026-11-21) ; les revérifications la suivent
      int kBase = nCars > 0 ? m_nRaces % nCars : 0;
      for (int j = 0; j < nCars; j++) {
         int k = (kBase + j) % nCars;
         Trial t;
         if (j == 0 || bCalib || j == 1 && !bPrev) {
            t = Copy(null);
            t.m_nRole = _BASE;
         } else if (j == 1) {
            t = new Trial();
            t.m_aOff = Dup(m_aPrevOff);
            t.m_aLim = Dup(m_aPrevLim);
            t.m_aCaut = Dup(m_aPrevCaut);
            t.m_nRole = _PREV;
            t.m_sWhat = "(base d'avant, controle " + (m_nPrevChecks + 1) + "/" + _PREV_CHECKS + ")";
         } else if (nRetest < m_Pending.size() && nRetest < _MAX_RETEST) {
            Trial p = (Trial)m_Pending.elementAt(nRetest++);
            t = Apply(p);
            t.m_nRole = _RETEST;
            t.m_fFirst = p.m_fFirst;
            t.m_sWhat = p.m_sWhat;
         } else {
            t = Mutate();
            t.m_nRole = _NEW;
            m_nTried++;
         }

         if (t.m_nRole == _RETEST || t.m_nRole == _NEW) {
            FixPlan(t.m_aOff);
         }

         m_aCar[k] = t;
      }

      StringBuffer sb = new StringBuffer();
      for (int k = 0; k < nCars; k++) {
         Trial t = m_aCar[k];
         sb.append(k > 0 ? ", " : "").append(k).append(' ').append(_ROLE_NAMES[t.m_nRole]).append(t.m_sWhat.length() > 0 ? " " + t.m_sWhat : "");
      }

      m_Pending.removeAllElements();
      Log(m_sKey + " : course " + (m_nRaces + 1) + (bCalib ? " (mesure du bruit : 6 fois la base)" : "") + ", porte de reference " + m_iRef + "/" + n
         + ", base " + (m_nBaseEvals > 0 ? ModTraining.Sec(m_fBase) + " s" : "?") + " ; voitures : " + sb.toString());
   }

   // copie de la base (ou d'un plan)
   static Trial Copy(Trial from) {
      Trial t = new Trial();
      t.m_aOff = Dup(from != null ? from.m_aOff : m_aOff);
      t.m_aLim = Dup(from != null ? from.m_aLim : m_aLim);
      t.m_aCaut = Dup(from != null ? from.m_aCaut : m_aCaut);
      return t;
   }

   static float[] Dup(float[] a) {
      float[] r = new float[a.length];
      System.arraycopy(a, 0, r, 0, a.length);
      return r;
   }

   // la base, avec le bout de piste de l'essai p
   static Trial Apply(Trial p) {
      Trial t = Copy(null);
      t.m_a = p.m_a;
      t.m_len = p.m_len;

      for (int d = 0; d < p.m_len; d++) {
         int j = (p.m_a + d) % m_n;
         t.m_aOff[j] = p.m_aOff[j];
         t.m_aLim[j] = p.m_aLim[j];
         t.m_aCaut[j] = p.m_aCaut[j];
      }

      return t;
   }

   static float Gauss() {
      return (float)m_Rand.nextGaussian();
   }

   // couloir où le plan peut passer à la porte i
   static float Lo(int i) {
      return ModAIMap.Known(i) ? ModAIMap.m_aLo[i] : -ModAIMap.m_aLW[i] * ModAIMap._GATE_USE;
   }

   static float Hi(int i) {
      return ModAIMap.Known(i) ? ModAIMap.m_aHi[i] : ModAIMap.m_aRW[i] * ModAIMap._GATE_USE;
   }

   // une variante de la base : un bout de 3 à 9 portes, modifié en douceur (cloche), trajectoire et/ou vitesse
   static Trial Mutate() {
      Trial t = Copy(null);
      int w = 1 + m_Rand.nextInt(4);
      int c = m_Rand.nextInt(m_n);
      int hot = HotGate();
      if (hot >= 0 && m_Rand.nextFloat() < _HOT_SHARE) {
         // juste avant l'endroit où les voitures tapent (le choc a lieu en allant vers la porte hot)
         c = ((hot - 1 - m_Rand.nextInt(2)) % m_n + m_n) % m_n;
      } else {
         hot = -1;
      }

      // là où les voitures tapent : grands pas, sur un bout plus long (Forêt 1 : le choc de la porte 8 revenait à
      // chaque tour, les petits pas autour ne l'ont jamais fait disparaître) ; la vitesse y est toujours baissée
      // (souvent déjà à 100 % : une hausse n'y changeait rien)
      if (hot >= 0) {
         w = 2 + m_Rand.nextInt(3);
      }

      float scale = hot >= 0 ? new float[]{1.5F, 2.5F, 4.0F}[m_Rand.nextInt(3)] : new float[]{0.5F, 1.0F, 1.5F}[m_Rand.nextInt(3)];
      int kind = m_Rand.nextInt(3); // 0 trajectoire, 1 vitesse, 2 les deux
      float go = kind != 1 ? Gauss() * _SIGMA_OFF * scale : 0.0F;
      float gl = kind != 0 ? Gauss() * _SIGMA_LIM * scale : 0.0F;
      float gc = kind != 0 ? Gauss() * _SIGMA_CAUT * scale : 0.0F;
      if (hot >= 0) {
         gl = -Math.abs(gl);
      }
      t.m_a = ((c - w) % m_n + m_n) % m_n;
      t.m_len = Math.min(m_n, 2 * w + 1);

      for (int d = -w; d <= w; d++) {
         int j = ((c + d) % m_n + m_n) % m_n;
         float wt = 0.5F * (1.0F + (float)Math.cos(Math.PI * d / (w + 1)));
         float lo = Lo(j);
         float hi = Hi(j);
         float o = t.m_aOff[j] + wt * go * 0.5F * (hi - lo);
         t.m_aOff[j] = o < lo ? lo : (o > hi ? hi : o);
         float l = t.m_aLim[j] + wt * gl;
         t.m_aLim[j] = l < _LIM_MIN ? _LIM_MIN : (l > 1.0F ? 1.0F : l);
         float k = t.m_aCaut[j] * (float)Math.exp(wt * gc);
         t.m_aCaut[j] = k < _CAUT_MIN ? _CAUT_MIN : (k > _CAUT_MAX ? _CAUT_MAX : k);
      }

      t.m_sWhat = "portes " + t.m_a + "-" + (t.m_a + t.m_len - 1) % m_n + (go != 0.0F ? " ligne " + (go > 0.0F ? "+" : "") + ModAI.F(go) : "")
         + (gl != 0.0F ? " vitesse " + (gl > 0.0F ? "+" : "") + ModAI.F(gl) : "") + (gc != 0.0F ? " prudence x" + ModAI.F((float)Math.exp(gc)) : "")
         + (hot >= 0 ? " (chocs porte " + hot + ")" : "");
      return t;
   }

   // une porte tirée au hasard, d'autant plus souvent que les voitures tapent en y allant ; -1 si aucun choc récent
   static int HotGate() {
      if (m_aHot == null || m_aHot.length != m_n) {
         return -1;
      }

      float sum = 0.0F;
      for (int i = 0; i < m_n; i++) {
         sum += m_aHot[i];
      }

      if (sum < 0.2F) {
         return -1;
      }

      float r = m_Rand.nextFloat() * sum;
      for (int i = 0; i < m_n; i++) {
         r -= m_aHot[i];
         if (r <= 0.0F) {
            return i;
         }
      }

      return m_n - 1;
   }

   // ------------------------------------------------------------------ fin de course

   static float Noise() {
      return m_bNoiseKnown ? (float)Math.sqrt(m_fNoiseVar) : 0.1F;
   }

   static boolean Overlap(Trial a, Trial b) {
      for (int d = 0; d < a.m_len; d++) {
         int j = (a.m_a + d) % m_n;
         int e = (j - b.m_a + m_n) % m_n;
         if (e < b.m_len) {
            return true;
         }
      }

      return false;
   }

   // note de la voiture k : moyenne des tours lancés + pénalités (chocs, remises en piste) ; _FAIL si rien de valable
   // (avant le 2026-11-21 une remise en piste était un échec : 18 courses sur 50 sans base mesurée sur Forêt 1, le plan
   // de départ s'y remettait en piste presque à chaque fois ; maintenant elle coûte son temps + _BAD_PENALTY)
   static void Score(int k, Trial t) {
      int np = m_nPass[k];
      float sum = 0.0F;
      int nLaps = 0;
      int nHits = 0;
      int nBad = 0;
      t.m_fBestLap = 0.0F;
      t.m_fStart = -1.0F;
      t.m_fStartCost = 0.0F;
      t.m_nStartHits = np > 0 ? m_aPassHits[k][0] : 0;
      t.m_nStartBad = np > 0 ? m_aPassBad[k][0] : 0;
      if (np > 0 && ModTraining.m_fClockZero >= 0.0F && k < ModTraining._MAX_CARS && ModTraining.m_aLapCross[k][0] >= 0.0F) {
         float s = m_aPass[k][0] - ModTraining.m_fClockZero - ModTraining.m_aLapCross[k][0];
         t.m_fStart = s > 0.0F && s < 600.0F ? s : -1.0F;
      }

      for (int p = 1; p < np; p++) {
         int bad = m_aPassBad[k][p] - m_aPassBad[k][p - 1];
         // portes sautées sans remise en piste : raccourci, pas un tour
         if (bad == 0 && m_aPassGates[k][p] < m_n - 2) {
            t.m_sWhy = "portes sautees";
            t.m_fScore = _FAIL;
            return;
         }

         float lap = m_aPass[k][p] - m_aPass[k][p - 1];
         int h = m_aPassHits[k][p] - m_aPassHits[k][p - 1];
         sum += lap + _HIT_PENALTY * h + _BAD_PENALTY * bad;
         nHits += h;
         nBad += bad;
         nLaps++;
         if (bad == 0 && (t.m_fBestLap == 0.0F || lap < t.m_fBestLap)) {
            t.m_fBestLap = lap; // record : vrai temps du tour, chocs compris (aucun tour n'en était exempt)
         }
      }

      t.m_nLaps = nLaps;
      t.m_nHits = nHits;
      t.m_nBad = nBad;
      if (nLaps == 0) {
         t.m_sWhy = "aucun tour lance";
         t.m_fScore = _FAIL;
         return;
      }

      t.m_fScore = sum / nLaps;
   }

   // le départ compte aussi (Forêt 1, 2026-11-21 : l'arbre entre les portes 1 et 2 au 1er tour, ~5 s, n'était dans
   // aucune note : il ne pouvait pas s'améliorer). Par rapport à la médiane de la course (même horloge, même grille)
   static void StartCosts(int nCars) {
      float[] a = new float[nCars];
      int m = 0;
      for (int k = 0; k < nCars; k++) {
         Trial t = m_aCar[k];
         if (t != null && t.m_fScore < _FAIL && t.m_fStart >= 0.0F) {
            int i = m++;
            for (; i > 0 && a[i - 1] > t.m_fStart; i--) {
               a[i] = a[i - 1];
            }

            a[i] = t.m_fStart;
         }
      }

      float med = m == 0 ? -1.0F : (m % 2 == 1 ? a[m / 2] : 0.5F * (a[m / 2 - 1] + a[m / 2]));
      for (int k = 0; k < nCars; k++) {
         Trial t = m_aCar[k];
         if (t == null || t.m_fScore >= _FAIL || t.m_nLaps == 0) {
            continue;
         }

         t.m_fStartCost = (t.m_fStart >= 0.0F && med >= 0.0F ? t.m_fStart - med : 0.0F) + _HIT_PENALTY * t.m_nStartHits + _BAD_PENALTY * t.m_nStartBad;
         t.m_fScore += t.m_fStartCost / t.m_nLaps;
      }
   }

   // ModTraining.OnRaceEnded (course finie, pas Échap) : notes, nouvelle base, aichrono.dat, ligne d'historique
   static void OnRaceEnded(RacerModule r) {
      m_sLastHistory = "";
      if (!m_bOn || m_aOff == null || r == null || m_Race != r.m_RaceParams) {
         return;
      }

      try {
         EndRace(r);
      } catch (Throwable e) {
         Log("erreur en fin de course : " + e);
      }

      m_Race = null;
   }

   static void EndRace(RacerModule r) {
      int nCars = Math.min(_MAX_CARS, r.m_RaceParams.m_AITab.size());
      boolean bCalib = m_nBaseEvals == 0;
      StringBuffer sCars = new StringBuffer();
      StringBuffer sLog = new StringBuffer();
      float fBaseNow = _FAIL;
      float fPrevNow = _FAIL;
      boolean bPrevRan = false;
      float fSum = 0.0F;
      float fSum2 = 0.0F;
      int nBase = 0;
      int[] nHitAt = new int[m_n];
      int nCounted = 0;

      for (int k = 0; k < nCars; k++) {
         if (m_aCar[k] != null) {
            Score(k, m_aCar[k]);
         }
      }

      int nHitsNoted = m_nRaceHits;
      LearnBornes();
      StartCosts(nCars);
      for (int k = 0; k < nCars; k++) {
         Trial t = m_aCar[k];
         if (t == null) {
            continue;
         }

         if (t.m_fBestLap > 0.0F && (m_fRecord == 0.0F || t.m_fBestLap < m_fRecord)) {
            m_fRecord = t.m_fBestLap;
         }

         if (t.m_nRole == _BASE && t.m_fScore < _FAIL) {
            fSum += t.m_fScore;
            fSum2 += t.m_fScore * t.m_fScore;
            nBase++;
         }

         if (t.m_nRole == _PREV) {
            bPrevRan = true;
            if (t.m_fScore < _FAIL) {
               fPrevNow = t.m_fScore;
            }
         }

         sCars.append(k > 0 ? ";" : "").append(_ROLE_NAMES[t.m_nRole]).append(':').append(t.m_fScore < _FAIL ? ModTraining.Sec(t.m_fScore) : "x").append(':')
            .append(t.m_nLaps).append(':').append(t.m_nHits).append(':').append(t.m_nBad).append(':').append(ModTraining.Sec(t.m_fStartCost));
         for (int i = 0; m_aHitAt[k] != null && i < m_aHitAt[k].length && i < m_n; i++) {
            nHitAt[i] += m_aHitAt[k][i];
         }

         nCounted++;
         sLog.append(k > 0 ? ", " : "").append(k).append(' ').append(_ROLE_NAMES[t.m_nRole]).append(' ')
            .append(t.m_fScore < _FAIL ? ModTraining.Sec(t.m_fScore) : "echec (" + t.m_sWhy + ")").append(t.m_nBad > 0 ? " (" + t.m_nBad + " remise(s) en piste)" : "");
      }

      // endroits où les voitures tapent (essais suivants centrés là une fois sur deux, ModChrono.Mutate)
      StringBuffer sHot = new StringBuffer();
      for (int i = 0; i < m_n; i++) {
         m_aHot[i] = _HOT_DECAY * m_aHot[i] + (nCounted > 0 ? (float)nHitAt[i] / nCounted : 0.0F);
         sHot.append(i > 0 ? "," : "").append(Math.round(m_aHot[i] * 100.0F));
      }

      if (nBase > 0) {
         fBaseNow = fSum / nBase; // les voitures de la base (2 hors 1re course, 1 pendant un contrôle)
      }

      String sDecision = "";
      if (bCalib) {
         if (nBase >= 2) {
            float mean = fSum / nBase;
            float var = Math.max(0.0F, fSum2 / nBase - mean * mean) * nBase / (nBase - 1);
            m_fNoiseVar = var;
            m_nNoiseN = nBase - 1;
            m_bNoiseKnown = true;
            m_fBase = mean;
            m_nBaseEvals = 1;
            sDecision = "bruit mesure : ecart-type " + ModTraining.Sec((float)Math.sqrt(var)) + " s entre " + nBase + " voitures identiques";
         } else if (nBase == 1) {
            m_fBase = fSum;
            m_nBaseEvals = 1;
            sDecision = "une seule base mesuree, bruit pas encore connu";
         } else {
            sDecision = "base en echec partout : nouvelle mesure a la course suivante";
         }
      } else {
         // 2 voitures de base : variance d'une mesure = (a - b)² / 2
         sDecision = Select(nCars, fBaseNow, nBase >= 2 ? Math.max(0.0F, fSum2 - fSum * fSum / nBase) * nBase / (nBase - 1) : -1.0F, bPrevRan, fPrevNow);
      }

      if (m_nBaseEvals > 0 && (m_fBestBase == 0.0F || m_fBase < m_fBestBase)) {
         m_fBestBase = m_fBase;
      }

      m_nRaces++;
      Save();
      String sNoise = m_bNoiseKnown ? ModTraining.Sec(Noise()) : "?";
      Log(m_sKey + " : course " + m_nRaces + " finie ; " + sLog.toString() + " ; " + sDecision + " ; base " + ModTraining.Sec(m_fBase) + " s (" + m_nBaseEvals
         + " mesures), record " + ModTraining.Sec(m_fRecord) + " s, bruit " + sNoise + " s, " + m_nGen + " ameliorations sur " + m_nTried + " essais"
         + " ; bornes " + ActiveBornes() + "/" + m_nBornes + (m_nBornesNew > 0 ? " (" + m_nBornesNew + " nouvelle(s), " + nHitsNoted + " chocs notes)" : ""));

      // historique (rapport HTML) : la base, sa trajectoire, ses vitesses
      StringBuffer sLine = new StringBuffer();
      StringBuffer sLim = new StringBuffer();
      for (int i = 0; i < m_n; i++) {
         float x = ModAIMap.m_aCX[i] + ModAIMap.m_aRX[i] * m_aOff[i];
         float z = ModAIMap.m_aCZ[i] + ModAIMap.m_aRZ[i] * m_aOff[i];
         sLine.append(i > 0 ? ";" : "").append(Math.round(x)).append(',').append(Math.round(z));
         sLim.append(i > 0 ? "," : "").append(Math.round(m_aLim[i] * 100.0F)).append(':').append(Math.round(m_aCaut[i] * 100.0F));
      }

      StringBuffer sBornes = new StringBuffer();
      for (int k = 0; k < m_nBornes; k++) {
         if (m_aBW[k] >= _BORNE_ACTIVE) {
            sBornes.append(sBornes.length() > 0 ? ";" : "").append(m_aBGate[k]).append(',').append(Math.round(m_aBX[k])).append(',')
               .append(Math.round(m_aBZ[k])).append(',').append(m_aBW[k]);
         }
      }

      m_sLastHistory = "C\tkey=" + m_sKey + "\tanimal=" + ModAnimals._NAMES[m_nAnimal] + "\tchrono_race=" + m_nRaces + "\tcalib=" + (bCalib ? 1 : 0)
         + "\tscore=" + _SCORE_VERSION
         + "\tbase=" + ModTraining.Sec(m_fBase) + "\tbase_now=" + (fBaseNow < _FAIL ? ModTraining.Sec(fBaseNow) : "") + "\trecord=" + ModTraining.Sec(m_fRecord)
         + "\tnoise=" + sNoise + "\tgen=" + m_nGen + "\ttried=" + m_nTried + "\tdecision=" + ModTraining.Clean(sDecision) + "\tcars=" + sCars.toString()
         + "\tline=" + sLine.toString() + "\tplan=" + sLim.toString() + "\thot=" + sHot.toString()
         // bornes : porte visée, position, chocs ; base déplacée par les bornes à la préparation de cette course
         + "\tbornes=" + sBornes.toString() + "\tbornes_new=" + m_nBornesNew + "\tbornes_fixed=" + m_nBornesFixed + "\tbornes_bad=" + m_nBornesBad
         + "\tbornes_hits=" + nHitsNoted + "\r\n";
   }

   // une mesure du bruit (variance d'une mesure, s²) : moyenne simple au début, glissante ensuite
   static void NoiseSample(float var) {
      if (m_nNoiseN < _NOISE_WARM) {
         m_fNoiseVar = (m_fNoiseVar * m_nNoiseN + var) / (m_nNoiseN + 1);
         m_nNoiseN++;
      } else {
         m_fNoiseVar = 0.85F * m_fNoiseVar + 0.15F * var;
      }

      m_bNoiseKnown = true;
   }

   // course normale : base mise à jour, contrôle de la dernière amélioration, essais revérifiés confirmés ou non,
   // nouveaux essais retenus. Tout se compare à la base de CETTE course (fBaseNow) : voir l'en-tête
   static String Select(int nCars, float fBaseNow, float fPairVar, boolean bPrevRan, float fPrevNow) {
      StringBuffer sb = new StringBuffer();
      // 1. bruit : écart entre les 2 voitures de base (même plan, même course : seul le hasard les sépare)
      if (fPairVar >= 0.0F) {
         NoiseSample(fPairVar);
      }

      // la base mesurée cette course ; sa moyenne glissante sert au journal, au rapport, et de repli si la base échoue
      if (fBaseNow < _FAIL) {
         int m = Math.min(m_nBaseEvals, _BASE_AVG - 1);
         m_fBase = (m_fBase * m + fBaseNow) / (m + 1);
         m_nBaseEvals++;
      } else {
         sb.append("base en echec cette course (essais compares a la moyenne des courses d'avant) ; ");
      }

      float noise = Noise();
      float fRef = fBaseNow < _FAIL ? fBaseNow : m_fBase;

      // 2. contrôle de la dernière amélioration : base contre base d'avant, dans la même course
      if (bPrevRan && fBaseNow < _FAIL) {
         m_nPrevChecks++;
         if (fPrevNow < _FAIL) {
            m_fPrevDiff += fBaseNow - fPrevNow;
            m_nPrevN++;
         }

         if (m_nPrevChecks >= _PREV_CHECKS) {
            m_bHasPrev = false;
            if (m_nPrevN == 0) {
               sb.append("derniere amelioration tenue (base d'avant en echec a chaque controle) ; ");
            } else {
               float d = m_fPrevDiff / m_nPrevN;
               if (d > _UNDO_K * noise) {
                  m_aOff = m_aPrevOff;
                  m_aLim = m_aPrevLim;
                  m_aCaut = m_aPrevCaut;
                  m_fBase -= d;
                  m_nBaseEvals = 2;
                  m_nGen = Math.max(0, m_nGen - 1);
                  sb.append("derniere amelioration annulee (+").append(ModTraining.Sec(d)).append(" s par rapport a la base d'avant sur ").append(m_nPrevN)
                     .append(" course(s)) ; ");
                  return sb.toString(); // essais de cette course mesurés contre une autre base : oubliés
               }

               sb.append("derniere amelioration tenue (").append(d > 0.0F ? "+" : "").append(ModTraining.Sec(d)).append(" s par rapport a la base d'avant sur ")
                  .append(m_nPrevN).append(" course(s)) ; ");
            }
         }
      }

      float thrScreen = Math.max(_MIN_GAIN, _SCREEN_K * noise);
      float thrConfirm = Math.max(_MIN_GAIN, _CONFIRM_K * noise);
      // 3. essais revérifiés : sur la 2e mesure seule. La 1re a été choisie parce qu'elle était la meilleure de la
      // course (souvent un essai qui a juste évité l'accident à +8 s de la porte 8, Forêt 1) : la compter dans la
      // moyenne faisait entrer des essais chanceux, pas meilleurs (2026-11-21 : 10 « améliorations », aucun gain)
      java.util.Vector ok = new java.util.Vector();
      for (int k = 0; k < nCars; k++) {
         Trial t = m_aCar[k];
         if (t != null && t.m_nRole == _RETEST) {
            if (t.m_fScore < fRef - thrConfirm) {
               Insert(ok, t);
            } else {
               sb.append("essai ").append(t.m_sWhat).append(" pas confirme (").append(t.m_fScore < _FAIL ? ModTraining.Sec(t.m_fScore) + " s, " + Gain(t, fRef) : "echec")
                  .append(" la 2e fois) ; ");
            }
         }
      }

      // 4. confirmés : le meilleur entre dans la base, puis les autres qui touchent d'autres portes
      if (ok.size() > 0) {
         m_aPrevOff = Dup(m_aOff);
         m_aPrevLim = Dup(m_aLim);
         m_aPrevCaut = Dup(m_aCaut);
         m_fPrevBase = fRef;
         m_bHasPrev = true;
         m_nPrevChecks = 0;
         m_nPrevN = 0;
         m_fPrevDiff = 0.0F;
         java.util.Vector in = new java.util.Vector();
         float fGain = 0.0F;
         for (int i = 0; i < ok.size(); i++) {
            Trial t = (Trial)ok.elementAt(i);
            boolean bFree = true;
            for (int j = 0; j < in.size(); j++) {
               bFree &= !Overlap(t, (Trial)in.elementAt(j));
            }

            if (bFree) {
               in.addElement(t);
               for (int d = 0; d < t.m_len; d++) {
                  int j = (t.m_a + d) % m_n;
                  m_aOff[j] = t.m_aOff[j];
                  m_aLim[j] = t.m_aLim[j];
                  m_aCaut[j] = t.m_aCaut[j];
               }

               fGain += fRef - t.m_fScore;
               m_nGen++;
               sb.append("AMELIORATION ").append(t.m_sWhat).append(" : ").append(ModTraining.Sec(t.m_fScore)).append(" s contre ")
                  .append(ModTraining.Sec(fRef)).append(" pour la base de la course ; ");
            }
         }

         // les gains de plusieurs bouts ne s'additionnent pas toujours : moitié du gain estimé, la base sera
         // remesurée aux courses suivantes (et contrôlée contre celle d'avant)
         m_fBase = fRef - 0.5F * fGain;
         m_nBaseEvals = 1;
      }

      // 5. nouveaux essais prometteurs : revérifiés à la course suivante (s'ils ne touchent pas un bout qui vient de
      // changer)
      // (la base vient de changer : ces mesures-là ne s'y comparent plus)
      java.util.Vector cand = new java.util.Vector();
      for (int k = 0; k < nCars && ok.size() == 0; k++) {
         Trial t = m_aCar[k];
         if (t != null && t.m_nRole == _NEW && t.m_fScore < fRef - thrScreen) {
            Insert(cand, t);
         }
      }

      for (int i = 0; i < cand.size() && m_Pending.size() < _MAX_RETEST; i++) {
         Trial t = (Trial)cand.elementAt(i);
         t.m_fFirst = t.m_fScore;
         m_Pending.addElement(t);
         sb.append("essai ").append(t.m_sWhat).append(" prometteur (").append(ModTraining.Sec(t.m_fScore)).append(" s, ").append(Gain(t, fRef))
            .append("), revu a la course suivante ; ");
      }

      return sb.length() > 0 ? sb.toString() : "rien de mieux";
   }

   // « -0.35 s sur la base » : écart d'un essai à la base de la course
   static String Gain(Trial t, float fRef) {
      float d = t.m_fScore - fRef;
      return (d > 0.0F ? "+" : "") + ModTraining.Sec(d) + " s sur la base";
   }

   // rangé par note croissante
   static void Insert(java.util.Vector v, Trial t) {
      int i = 0;
      while (i < v.size() && ((Trial)v.elementAt(i)).m_fScore <= t.m_fScore) {
         i++;
      }

      v.insertElementAt(t, i);
   }

   // ------------------------------------------------------------------ aichrono.dat
   // une ligne par circuit, sens, portes et animal : « clé<TAB>champ=valeur<TAB>… »
   //   off=décalages (arrondis) ; plan=limite%:prudence% par porte ; base, evals, noise (variance x 10000), record…

   static void Load(String sKey, int n) {
      if (m_sKey != null && m_aOff != null) {
         // circuit quitté : ce qui n'est pas dans aichrono.dat attend en mémoire le retour sur ce circuit
         m_Mem.put(m_sKey + "|" + m_n, new Object[]{m_Pending.clone(), m_aPrevOff, m_aPrevLim, m_aPrevCaut, new Float(m_fPrevBase), new Boolean(m_bHasPrev),
            new Integer(m_nPrevChecks), new Integer(m_nPrevN), new Float(m_fPrevDiff)});
      }

      m_sKey = sKey;
      m_n = n;
      m_aOff = null;
      m_aLim = null;
      m_aCaut = null;
      m_fBase = 0.0F;
      m_nBaseEvals = 0;
      m_fNoiseVar = 0.0F;
      m_bNoiseKnown = false;
      m_fRecord = 0.0F;
      m_fBestBase = 0.0F;
      m_nRaces = 0;
      m_nGen = 0;
      m_nTried = 0;
      m_bHasPrev = false;
      m_nPrevChecks = 0;
      m_nPrevN = 0;
      m_fPrevDiff = 0.0F;
      m_nNoiseN = 0;
      m_Pending.removeAllElements();
      m_aHot = null;
      m_nBornes = 0;
      m_nRaceHits = 0;
      String s = FindLine(sKey);
      if (s == null) {
         return;
      }

      ReadBornes(Field(s, "bornes"), n);

      float[][] base = ReadPlan(s, "", n);
      if (base == null) {
         return;
      }

      m_aOff = base[0];
      m_aLim = base[1];
      m_aCaut = base[2];
      m_fBase = FloatField(s, "base");
      m_nBaseEvals = (int)FloatField(s, "evals");
      m_fNoiseVar = FloatField(s, "noise") / 10000.0F;
      m_bNoiseKnown = FloatField(s, "noise_known") > 0.0F;
      m_nNoiseN = (int)FloatField(s, "noise_n");
      if (FloatField(s, "select") < _SELECT_VERSION) {
         // bruit d'avant : mesuré contre la moyenne des courses d'avant, gonflé par le ralentissement du jeu. Gardé en
         // attendant, remplacé dès la 1re mesure entre les 2 voitures de base
         m_nNoiseN = 0;
         Log(sKey + " : essais juges contre la base de la meme course, bruit remesure entre les 2 voitures de base");
      }
      m_fRecord = FloatField(s, "record");
      m_fBestBase = FloatField(s, "best_base");
      m_nRaces = (int)FloatField(s, "races");
      m_nGen = (int)FloatField(s, "gen");
      m_nTried = (int)FloatField(s, "tried");
      m_aHot = Floats(Field(s, "hot"), n, 0.01F);
      if (FloatField(s, "score") < _SCORE_VERSION) {
         // notée avant (chocs à 0,3 s, remise en piste = échec) : plan gardé, base et bruit remesurés (6 fois la base)
         m_nBaseEvals = 0;
         m_fNoiseVar = 0.0F;
         m_bNoiseKnown = false;
         m_fBestBase = 0.0F; // (le record reste : c'est un vrai temps de tour, pas une note)
         Log(sKey + " : nouvelle notation des plans, base remesuree a la prochaine course");
      }

      Object[] mem = (Object[])m_Mem.get(sKey + "|" + n);
      if (mem != null) {
         java.util.Vector v = (java.util.Vector)mem[0];
         for (int i = 0; i < v.size(); i++) {
            m_Pending.addElement(v.elementAt(i));
         }

         m_aPrevOff = (float[])mem[1];
         m_aPrevLim = (float[])mem[2];
         m_aPrevCaut = (float[])mem[3];
         m_fPrevBase = ((Float)mem[4]).floatValue();
         m_bHasPrev = ((Boolean)mem[5]).booleanValue();
         if (mem.length >= 9) {
            m_nPrevChecks = ((Integer)mem[6]).intValue();
            m_nPrevN = ((Integer)mem[7]).intValue();
            m_fPrevDiff = ((Float)mem[8]).floatValue();
         }
      } else {
         // jeu relancé (mods\entrainer-ia.ps1 le relance toutes les -Relance courses) : contrôle en cours et essais à
         // revérifier repris de aichrono.dat
         float[][] pp = Field(s, "prev_checks") != null ? ReadPlan(s, "prev_", n) : null;
         if (pp != null) {
            m_aPrevOff = pp[0];
            m_aPrevLim = pp[1];
            m_aPrevCaut = pp[2];
            m_fPrevBase = FloatField(s, "prev_base");
            m_nPrevChecks = (int)FloatField(s, "prev_checks");
            m_nPrevN = (int)FloatField(s, "prev_n");
            m_fPrevDiff = FloatField(s, "prev_diff");
            m_bHasPrev = true;
         }

         int np = (int)FloatField(s, "pend");
         for (int k = 0; k < np && k < _MAX_RETEST; k++) {
            String p = "p" + k + "_";
            float[][] q = ReadPlan(s, p, n);
            Trial t = new Trial();
            t.m_a = (int)FloatField(s, p + "a");
            t.m_len = (int)FloatField(s, p + "len");
            if (q == null || t.m_a < 0 || t.m_a >= n || t.m_len <= 0 || t.m_len > n) {
               continue;
            }

            t.m_aOff = q[0];
            t.m_aLim = q[1];
            t.m_aCaut = q[2];
            t.m_fFirst = FloatField(s, p + "first");
            String w = Field(s, p + "what");
            t.m_sWhat = w != null ? w : "";
            m_Pending.addElement(t);
         }
      }

      if (m_nGen == 0 && FloatField(s, "init") < _INIT_VERSION) {
         // plan de départ d'avant, rien d'appris dessus : refait (avec les points dangereux), base remesurée
         m_aOff = null;
         m_aLim = null;
         m_aCaut = null;
         m_nBaseEvals = 0;
         m_fBestBase = 0.0F;
         m_bHasPrev = false;
         m_Pending.removeAllElements();
         Log(sKey + " : plan de depart refait (prudence aux points dangereux)");
      }

      Log(sKey + " : plan repris (" + m_nRaces + " courses, base " + ModTraining.Sec(m_fBase) + " s, record " + ModTraining.Sec(m_fRecord) + " s"
         + (m_Pending.size() > 0 ? ", " + m_Pending.size() + " essai(s) a revoir" : "") + ")");
   }

   // plan complet d'une ligne de aichrono.dat : <p>off=décalages, <p>plan=limite%:prudence% ; {off, lim, caut} ou null
   static float[][] ReadPlan(String s, String p, int n) {
      float[] off = Floats(Field(s, p + "off"), n, 1.0F);
      String plan = Field(s, p + "plan");
      if (off == null || plan == null) {
         return null;
      }

      float[] lim = new float[n];
      float[] caut = new float[n];
      int a = 0;
      for (int i = 0; i < n; i++) {
         int q = plan.indexOf(',', a);
         if (q < 0) {
            q = plan.length();
         }

         String e = a < plan.length() ? plan.substring(a, q) : "";
         int c = e.indexOf(':');
         try {
            lim[i] = c > 0 ? Integer.parseInt(e.substring(0, c)) / 100.0F : 1.0F;
            caut[i] = c > 0 ? Integer.parseInt(e.substring(c + 1)) / 100.0F : 1.0F;
         } catch (NumberFormatException ex) {
            return null;
         }

         a = q + 1;
      }

      return new float[][]{off, lim, caut};
   }

   static void AppendPlan(StringBuffer line, String p, float[] off, float[] lim, float[] caut) {
      line.append('\t').append(p).append("off=");
      for (int i = 0; i < m_n; i++) {
         line.append(i > 0 ? "," : "").append(Math.round(off[i]));
      }

      line.append('\t').append(p).append("plan=");
      for (int i = 0; i < m_n; i++) {
         line.append(i > 0 ? "," : "").append(Math.round(lim[i] * 100.0F)).append(':').append(Math.round(caut[i] * 100.0F));
      }
   }

   // bornes=porte:x:z:ux:uz:chocs;… (aichrono.dat)
   static void ReadBornes(String v, int n) {
      m_nBornes = 0;
      int p = 0;
      while (v != null && p < v.length() && m_nBornes < _MAX_BORNES) {
         int q = v.indexOf(';', p);
         if (q < 0) {
            q = v.length();
         }

         String e = v.substring(p, q);
         p = q + 1;
         int[] a = new int[6];
         int c = 0;
         int f = 0;
         try {
            for (; f < 6 && c <= e.length(); f++) {
               int d = e.indexOf(':', c);
               if (d < 0) {
                  d = e.length();
               }

               a[f] = Integer.parseInt(e.substring(c, d).trim());
               c = d + 1;
            }
         } catch (NumberFormatException ex) {
            continue;
         }

         float l = (float)Math.sqrt((float)a[3] * a[3] + (float)a[4] * a[4]);
         if (f < 6 || a[0] < 0 || a[0] >= n || l < 1.0F) {
            continue;
         }

         int k = m_nBornes++;
         m_aBGate[k] = a[0];
         m_aBX[k] = a[1];
         m_aBZ[k] = a[2];
         m_aBUX[k] = a[3] / l;
         m_aBUZ[k] = a[4] / l;
         m_aBW[k] = Math.max(1, a[5]);
      }
   }

   static String FindLine(String sKey) {
      File f = new File(_FILE);
      if (!f.exists()) {
         return null;
      }

      String r = null;
      try {
         BufferedReader in = new BufferedReader(new FileReader(f));
         for (String s = in.readLine(); s != null; s = in.readLine()) {
            if (s.startsWith(sKey + "\t")) {
               r = s;
            }
         }

         in.close();
      } catch (Exception e) {
         Log("lecture de " + _FILE + " impossible : " + e);
      }

      return r;
   }

   static String Field(String s, String name) {
      int p = s.indexOf("\t" + name + "=");
      if (p < 0) {
         return null;
      }

      p += name.length() + 2;
      int q = s.indexOf('\t', p);
      return s.substring(p, q < 0 ? s.length() : q);
   }

   static float FloatField(String s, String name) {
      String v = Field(s, name);
      try {
         return v != null ? Float.valueOf(v.trim()).floatValue() : 0.0F;
      } catch (NumberFormatException e) {
         return 0.0F;
      }
   }

   static float[] Floats(String v, int n, float scale) {
      if (v == null) {
         return null;
      }

      float[] a = new float[n];
      int p = 0;
      for (int i = 0; i < n; i++) {
         if (p > v.length()) {
            return null;
         }

         int q = v.indexOf(',', p);
         if (q < 0) {
            q = v.length();
         }

         try {
            a[i] = Integer.parseInt(v.substring(p, q).trim()) * scale;
         } catch (NumberFormatException e) {
            return null;
         }

         p = q + 1;
      }

      return a;
   }

   static void Save() {
      StringBuffer line = new StringBuffer(m_sKey);
      line.append("\tgates=").append(m_n).append("\traces=").append(m_nRaces).append("\tgen=").append(m_nGen).append("\ttried=").append(m_nTried)
         .append("\tbase=").append(ModTraining.Sec(m_fBase)).append("\tevals=").append(m_nBaseEvals).append("\tbest_base=").append(ModTraining.Sec(m_fBestBase))
         .append("\trecord=").append(ModTraining.Sec(m_fRecord)).append("\tnoise=").append(Math.round(m_fNoiseVar * 10000.0F)).append("\tnoise_known=")
         .append(m_bNoiseKnown ? 1 : 0).append("\tnoise_n=").append(m_nNoiseN).append("\tselect=").append(_SELECT_VERSION).append("\tscore=").append(_SCORE_VERSION).append("\tinit=").append(_INIT_VERSION).append("\tdate=").append(ModTraining.Now());
      AppendPlan(line, "", m_aOff, m_aLim, m_aCaut);
      line.append("\thot=");
      for (int i = 0; i < m_n; i++) {
         line.append(i > 0 ? "," : "").append(m_aHot != null && i < m_aHot.length ? Math.round(m_aHot[i] * 100.0F) : 0);
      }

      if (m_nBornes > 0) {
         // porte:x:z:côté x 1000:côté z x 1000:chocs
         line.append("\tbornes=");
         for (int k = 0; k < m_nBornes; k++) {
            line.append(k > 0 ? ";" : "").append(m_aBGate[k]).append(':').append(Math.round(m_aBX[k])).append(':').append(Math.round(m_aBZ[k])).append(':')
               .append(Math.round(m_aBUX[k] * 1000.0F)).append(':').append(Math.round(m_aBUZ[k] * 1000.0F)).append(':').append(m_aBW[k]);
         }
      }

      // ce qui n'était gardé qu'en mémoire (m_Mem) : le jeu est relancé régulièrement pendant l'entraînement
      if (m_bHasPrev && m_aPrevOff != null && m_aPrevOff.length == m_n) {
         line.append("\tprev_base=").append(ModTraining.Sec(m_fPrevBase)).append("\tprev_checks=").append(m_nPrevChecks).append("\tprev_n=").append(m_nPrevN)
            .append("\tprev_diff=").append(ModTraining.Sec(m_fPrevDiff));
         AppendPlan(line, "prev_", m_aPrevOff, m_aPrevLim, m_aPrevCaut);
      }

      line.append("\tpend=").append(m_Pending.size());
      for (int k = 0; k < m_Pending.size(); k++) {
         Trial t = (Trial)m_Pending.elementAt(k);
         String p = "p" + k + "_";
         line.append('\t').append(p).append("a=").append(t.m_a).append('\t').append(p).append("len=").append(t.m_len).append('\t').append(p).append("first=")
            .append(ModTraining.Sec(t.m_fFirst)).append('\t').append(p).append("what=").append(ModTraining.Clean(t.m_sWhat));
         AppendPlan(line, p, t.m_aOff, t.m_aLim, t.m_aCaut);
      }

      StringBuffer all = new StringBuffer();
      try {
         File f = new File(_FILE);
         if (f.exists()) {
            BufferedReader in = new BufferedReader(new FileReader(f));
            for (String s = in.readLine(); s != null; s = in.readLine()) {
               if (s.trim().length() > 0 && !s.startsWith(m_sKey + "\t")) {
                  all.append(s).append("\r\n");
               }
            }

            in.close();
         }

         all.append(line.toString()).append("\r\n");
         FileWriter out = new FileWriter(_FILE);
         out.write(all.toString());
         out.close();
      } catch (Exception e) {
         Log("ecriture de " + _FILE + " impossible : " + e);
      }
   }
}
