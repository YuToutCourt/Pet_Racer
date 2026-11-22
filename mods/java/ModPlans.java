import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

// [mod] Plans chrono pour les IA du jeu (notes/ia.md, « Plans chrono pour les IA du jeu », 2026-11-22) : les IA
// expertes (course simple, Grand Prix, en ligne, remplissage des parties classées) suivent le plan de course que l'IA
// chrono a trouvé sur le circuit (ModChrono, aichrono.dat) : décalage dans chaque porte, vitesse de passage permise,
// prudence avant le virage. Seulement les plans qui battent l'IA experte : mods\plans-ia.py les choisit (temps réels
// des tours lancés, historique de l'entraînement) et les écrit dans Data\aiplans.dat, donné avec le mod.
// Le reste de la conduite ne change pas : évitement des autres voitures, bonus, dégagement, points dangereux de la
// course en cours (une IA qui s'est plantée là fait ralentir les suivantes). Chaque IA garde un petit couloir à elle
// autour de la ligne du plan (_SPREAD), sinon les 5 expertes se suivraient pile sur la même ligne.
// Vitesses : le plan les donne en part de la vitesse maxi de l'animal entraîné (l'Ours) ; elles sont converties pour
// chaque animal, pour la même vitesse réelle de passage (un Lion va 11 % plus vite que l'Ours en ligne droite, pas dans
// le virage). Royal (vitesse égale pour tous) : pas de conversion.
// Pas de plan : IA chrono (elle a les siens), autre niveau qu'expert, difficulté « d'origine », circuit sans plan
// validé, nombre de portes différent. Entraînement normal : pas de plan, sauf Plans(1) dans entrainement.scr
// (mods\entrainer-ia.ps1 -Plans), qui en donne à une voiture sur deux pour comparer dans les mêmes courses.
class ModPlans {
   static final String _FILE = "Data/aiplans.dat";
   static final int _LEVEL = 5;               // niveau VehicleAI de l'IA experte (celui de l'entraînement chrono)
   static final float _SPREAD = 40.0F;        // x couloir de l'IA (-1..1) : écart à la ligne du plan

   static int m_nTraining = 0;                // entraînement : 0 = aucun plan, 1 = une voiture sur deux, 2 = toutes
   static java.util.Hashtable m_Plans = null; // clé ModAIMap.m_sKey (circuit|sens|portes) -> Plan

   static class Plan {
      String m_sKey;
      int m_nAnimal = 8;                      // animal entraîné (vitesses en part de sa vitesse maxi)
      float[] m_aOff;
      float[] m_aLim;
      float[] m_aCaut;
      String m_sGain = "";
   }

   // VehicleAI.ModStart : le plan de cette IA, ou null
   static Plan For(VehicleAI ai) {
      if (ModChrono.m_bOn || !ModAI.IsImproved() || ai.m_iAILevel != _LEVEL || ModAIMap.m_sKey == null || ai.m_Track == null) {
         return null;
      }

      if (ModTraining.m_bRunning) {
         // comparaison dans les mêmes courses : la voiture avec plan change de place sur la grille d'une course à l'autre
         if (m_nTraining == 0 || m_nTraining == 1 && (ModTraining.m_nNext + ai.m_Vehicle.m_Index) % 2 == 0) {
            return null;
         }
      }

      Load();
      Plan p = (Plan)m_Plans.get(ModAIMap.m_sKey);
      return p != null && p.m_aOff.length == ai.m_Track.m_TrackPoints.size() ? p : null;
   }

   // x part de la vitesse maxi du plan, pour la voiture v : même vitesse réelle que l'animal entraîné
   static float SpeedK(Plan p, Vehicle v) {
      float[] c = VehicleParams._ANIMALS_SPEED_COEFS;
      int a = v.m_iAnimalID;
      if (a < 0 || a >= c.length || p.m_nAnimal < 0 || p.m_nAnimal >= c.length || c[a] <= 0.0F) {
         return 1.0F;
      }

      // Royal : UpdateMaxSpeedAccelEqualAll, la même vitesse maxi pour tous les animaux
      if (Math.abs(v.m_fDefaultMaxSpeed - v.m_Params._MAX_SPEED * c[a]) > 1.0F) {
         return 1.0F;
      }

      return c[p.m_nAnimal] / c[a];
   }

   static void Load() {
      if (m_Plans != null) {
         return;
      }

      m_Plans = new java.util.Hashtable();
      File f = new File(_FILE);
      if (!f.exists()) {
         GameObject.CrashLog("[mod] plans : pas de " + _FILE + " (les IA expertes calculent leur trajectoire)\n");
         return;
      }

      StringBuffer bad = new StringBuffer();
      try {
         BufferedReader in = new BufferedReader(new FileReader(f));
         for (String s = in.readLine(); s != null; s = in.readLine()) {
            if (s.length() == 0 || s.startsWith("#") || s.startsWith("/")) {
               continue;
            }

            Plan p = Parse(s);
            if (p != null) {
               m_Plans.put(p.m_sKey, p);
            } else {
               bad.append(bad.length() > 0 ? ", " : "").append(s.length() > 40 ? s.substring(0, 40) : s);
            }
         }

         in.close();
      } catch (Exception e) {
         GameObject.CrashLog("[mod] plans : lecture de " + _FILE + " impossible : " + e + "\n");
      }

      GameObject.CrashLog("[mod] plans : " + m_Plans.size() + " circuit(s) avec un plan chrono pour les IA expertes" + (bad.length() > 0 ? " ; lignes illisibles : " + bad : "") + "\n");
   }

   // « clé<TAB>animal=Ours<TAB>off=…<TAB>plan=limite%:prudence%,…<TAB>gain=… » (mods\plans-ia.py)
   static Plan Parse(String s) {
      int t = s.indexOf('\t');
      if (t <= 0) {
         return null;
      }

      Plan p = new Plan();
      p.m_sKey = s.substring(0, t);
      int n = Gates(p.m_sKey);
      float[][] q = n > 0 ? ModChrono.ReadPlan(s, "", n) : null;
      if (q == null) {
         return null;
      }

      p.m_aOff = q[0];
      p.m_aLim = q[1];
      p.m_aCaut = q[2];
      String a = ModChrono.Field(s, "animal");
      int id = a != null ? ModAnimals.Find(a) : -1;
      if (id >= 0) {
         p.m_nAnimal = id;
      }

      String g = ModChrono.Field(s, "gain");
      p.m_sGain = g != null ? g : "";
      return p;
   }

   // nombre de portes : dernier champ de la clé circuit|sens|portes
   static int Gates(String sKey) {
      int b = sKey.lastIndexOf('|');
      try {
         return b >= 0 ? Integer.parseInt(sKey.substring(b + 1).trim()) : 0;
      } catch (NumberFormatException e) {
         return 0;
      }
   }
}
