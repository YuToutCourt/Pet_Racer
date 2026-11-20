import java.io.File;

// [mod] Stats des animaux (chantier 4, notes/animaux.md), lues dans Data\animals.scr au démarrage
// (RacerGame.OnInitialize) : une ligne par animal, Animal("Panthere", accélération, vitesse, virage).
// Les coefficients remplacent ceux de VehicleParams (_ANIMALS_ACCEL/SPEED/TURN_COEFS), appliqués par
// Vehicle.UpdateMaxSpeedAccel (base 500 / 4500 / 80°) et lus par l'IA (VehicleParams._ANIMALS_ACCEL_COEFS) :
// ils valent pour les humains comme pour les IA. Fichier absent, animal absent ou ligne illisible = valeurs d'origine.
// L'écran de choix (KidsAnimalChoiceMenuWindow) affiche ces valeurs en barres (GetFill).
class ModAnimals {
   static final String _FILE = "Data/animals.scr";
   // noms reconnus dans le fichier (sans accents, majuscules indifférentes), dans l'ordre des identifiants du jeu
   // (MenuAnimal._PANTHER = 0 … _LION = 11) ; un numéro 0..11 marche aussi
   static final String[] _NAMES = new String[]{
      "Panthere", "Dino", "Poulet", "Grenouille", "Tortue", "Requin", "Taureau", "Girafe", "Ours", "Gorille", "Robot", "Lion"
   };
   static final int _COUNT = 12;
   // barres de l'écran de choix, dans l'ordre d'affichage
   static final int _STAT_SPEED = 0;
   static final int _STAT_ACCEL = 1;
   static final int _STAT_TURN = 2;
   // bornes acceptées pour un coefficient (1,0 = base du jeu)
   static final float _MIN_COEF = 0.3F;
   static final float _MAX_COEF = 2.0F;
   // échelle des barres : bas à _BAR_LOW (ou 0,2 sous le plus petit coefficient), plein à 1,0 (ou au plus grand)
   static final float _BAR_LOW = 0.6F;
   static final float _BAR_HIGH = 1.0F;
   static final float _BAR_MIN_FILL = 0.04F;

   // valeurs d'origine, gardées pour le journal
   static final float[] s_OrigAccel = Copy(VehicleParams._ANIMALS_ACCEL_COEFS);
   static final float[] s_OrigSpeed = Copy(VehicleParams._ANIMALS_SPEED_COEFS);
   static final float[] s_OrigTurn = Copy(VehicleParams._ANIMALS_TURN_COEFS);

   static float[] Copy(float[] t) {
      float[] r = new float[t.length];
      System.arraycopy(t, 0, r, 0, t.length);
      return r;
   }

   static void Load() {
      if (!new File(_FILE).exists()) {
         GameObject.CrashLog("[mod] animaux : " + _FILE + " absent, valeurs d'origine\n");
         return;
      }

      ParseKeyword k = GameObject.ParseFile(_FILE);
      if (k == null) {
         GameObject.CrashLog("[mod] animaux : " + _FILE + " illisible, valeurs d'origine\n");
         return;
      }

      int nRead = 0;

      for (; k != null; k = k.GetNext()) {
         if (!k.Equals("Animal")) {
            continue;
         }

         String sName = k.GetSParam(0);
         int id = Find(sName);
         if (id < 0) {
            GameObject.CrashLog("[mod] animaux : animal inconnu \"" + sName + "\" ignore\n");
            continue;
         }

         VehicleParams._ANIMALS_ACCEL_COEFS[id] = Check(sName, k.GetFParam(1), s_OrigAccel[id]);
         VehicleParams._ANIMALS_SPEED_COEFS[id] = Check(sName, k.GetFParam(2), s_OrigSpeed[id]);
         VehicleParams._ANIMALS_TURN_COEFS[id] = Check(sName, k.GetFParam(3), s_OrigTurn[id]);
         nRead++;
      }

      String sChanged = "";

      for (int i = 0; i < _COUNT; i++) {
         if (Differs(VehicleParams._ANIMALS_ACCEL_COEFS[i], s_OrigAccel[i])
            || Differs(VehicleParams._ANIMALS_SPEED_COEFS[i], s_OrigSpeed[i])
            || Differs(VehicleParams._ANIMALS_TURN_COEFS[i], s_OrigTurn[i])) {
            sChanged = sChanged + " " + _NAMES[i] + " " + Describe(i);
         }
      }

      GameObject.CrashLog(
         "[mod] animaux : " + _FILE + " lu, " + nRead + " animal(aux)" + (sChanged.length() > 0 ? ", modifies (accel/vitesse/virage) :" + sChanged : ", valeurs d'origine") + "\n"
      );
   }

   // identifiant d'après le nom du fichier (ou un numéro), -1 si inconnu
   static int Find(String s) {
      if (s == null) {
         return -1;
      }

      s = s.trim();

      for (int i = 0; i < _COUNT; i++) {
         if (_NAMES[i].equalsIgnoreCase(s)) {
            return i;
         }
      }

      try {
         int n = Integer.parseInt(s);
         return n >= 0 && n < _COUNT ? n : -1;
      } catch (NumberFormatException e) {
         return -1;
      }
   }

   static float Check(String sName, float f, float fOrig) {
      if (!(f >= _MIN_COEF) || !(f <= _MAX_COEF)) { // rattrape aussi NaN
         GameObject.CrashLog("[mod] animaux : " + sName + " coefficient " + f + " hors de " + _MIN_COEF + ".." + _MAX_COEF + ", valeur d'origine gardee\n");
         return fOrig;
      }

      return f;
   }

   // l'analyseur du moteur peut arrondir autrement que javac : « 0.90 » lu n'est pas forcément 0.9F au bit près
   static boolean Differs(float a, float b) {
      return Math.abs(a - b) > 0.0005F;
   }

   static String Describe(int i) {
      return VehicleParams._ANIMALS_ACCEL_COEFS[i] + "/" + VehicleParams._ANIMALS_SPEED_COEFS[i] + "/" + VehicleParams._ANIMALS_TURN_COEFS[i];
   }

   static float[] Table(int nStat) {
      switch (nStat) {
         case _STAT_SPEED:
            return VehicleParams._ANIMALS_SPEED_COEFS;
         case _STAT_ACCEL:
            return VehicleParams._ANIMALS_ACCEL_COEFS;
         default:
            return VehicleParams._ANIMALS_TURN_COEFS;
      }
   }

   // remplissage d'une barre (0..1). Avec les valeurs d'origine (0,8 à 1,0) : 0,8 = moitié, 1,0 = plein.
   // L'échelle s'élargit si animals.scr sort de cet intervalle, pour que les écarts restent visibles.
   static float GetFill(int nStat, int id) {
      if (id < 0 || id >= _COUNT) {
         return 0.0F;
      }

      float[] t = Table(nStat);
      float fLow = _BAR_LOW;
      float fHigh = _BAR_HIGH;

      for (int i = 0; i < _COUNT; i++) {
         if (t[i] - 0.2F < fLow) {
            fLow = t[i] - 0.2F;
         }

         if (t[i] > fHigh) {
            fHigh = t[i];
         }
      }

      float f = (t[id] - fLow) / (fHigh - fLow);
      if (f < _BAR_MIN_FILL) {
         f = _BAR_MIN_FILL;
      }

      return f > 1.0F ? 1.0F : f;
   }
}
