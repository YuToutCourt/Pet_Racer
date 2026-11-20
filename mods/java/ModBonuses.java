import java.io.File;

// [mod] Tirage des bonus selon la place (chantier 5, notes/bonus.md), réglé par Data\bonuses.scr, lu au démarrage
// (RacerGame.OnInitialize). Une ligne par bonus : Bonus("Fusee", partout, p1, p2, p3, p4, p5, p6)
//   partout : 1 = ajouté à la liste de tous les mondes (AnimalBonusTypes.Init2 → ExtendLevel), 0 = listes d'origine
//   p1..p6  : poids de tirage du 1er au dernier (0 = jamais à cette place)
// Le tirage (LevelBonusTypes.GetRandomBonus → Pick) se fait parmi les bonus du monde, comme à l'origine, mais pondéré
// par la place de la voiture ramenée à 6 crans (en duel : 1er = cran 1, 2e = cran 6). Humains et IA.
// Fichier absent ou illisible = tirage d'origine (uniforme). Bonus sans ligne = poids _DEFAULT_WEIGHT partout.
class ModBonuses {
   static final String _FILE = "Data/bonuses.scr";
   // identifiants de LevelBonusTypes (0..20, et 21 = Missile du mod) ; un numéro marche aussi dans le fichier
   static final String[] _NAMES = new String[]{
      "Bombe", "Bouchon", "Catapulte", "Turbo", "Protection", "Huile", "Gaz", "Bouteille", "Banane", "Guepes", "Enclume",
      "Pointes", "Tomates", "Pasteque", "Glace", "Eau", "Coco", "Neige", "Cookie", "Fusee", "Ovni", "Missile"
   };
   static final int _COUNT = 22;
   static final int _PLACES = 6;
   static final float _DEFAULT_WEIGHT = 5.0F;
   static final float _MAX_WEIGHT = 1000.0F;

   static boolean s_bLoaded = false;
   static float[][] s_Weights = new float[_COUNT][_PLACES];
   static boolean[] s_bEverywhere = new boolean[_COUNT];

   static void Load() {
      if (!new File(_FILE).exists()) {
         GameObject.CrashLog("[mod] bonus : " + _FILE + " absent, tirage d'origine\n");
         return;
      }

      ParseKeyword k = GameObject.ParseFile(_FILE);
      if (k == null) {
         GameObject.CrashLog("[mod] bonus : " + _FILE + " illisible, tirage d'origine\n");
         return;
      }

      for (int i = 0; i < _COUNT; i++) {
         for (int p = 0; p < _PLACES; p++) {
            s_Weights[i][p] = _DEFAULT_WEIGHT;
         }
      }

      int nRead = 0;

      for (; k != null; k = k.GetNext()) {
         if (!k.Equals("Bonus")) {
            continue;
         }

         String sName = k.GetSParam(0);
         int id = Find(sName);
         if (id < 0) {
            GameObject.CrashLog("[mod] bonus : bonus inconnu \"" + sName + "\" ignore\n");
            continue;
         }

         s_bEverywhere[id] = k.GetFParam(1) >= 0.5F;

         for (int p = 0; p < _PLACES; p++) {
            float f = k.GetFParam(2 + p);
            if (!(f >= 0.0F) || !(f <= _MAX_WEIGHT)) { // rattrape aussi NaN
               GameObject.CrashLog("[mod] bonus : " + sName + " poids " + f + " hors de 0.." + _MAX_WEIGHT + ", " + _DEFAULT_WEIGHT + " garde\n");
               f = _DEFAULT_WEIGHT;
            }

            s_Weights[id][p] = f;
         }

         nRead++;
      }

      s_bLoaded = true;
      String sEverywhere = "";

      for (int i = 0; i < _COUNT; i++) {
         if (s_bEverywhere[i]) {
            sEverywhere = sEverywhere + " " + _NAMES[i];
         }
      }

      GameObject.CrashLog(
         "[mod] bonus : " + _FILE + " lu, " + nRead + " bonus, tirage selon la place"
            + (sEverywhere.length() > 0 ? ", ajoutes a tous les mondes :" + sEverywhere : "") + "\n"
      );
   }

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

   static String Name(int id) {
      return id >= 0 && id < _COUNT ? _NAMES[id] : "?" + id;
   }

   // Appelé par AnimalBonusTypes.Init2, juste après que le circuit a rempli sa liste : ajoute les bonus « partout »
   // (AddBonusType précharge aussi leur modèle, comme pour les bonus d'origine).
   static void ExtendLevel(LevelBonusTypes list) {
      if (!s_bLoaded || list == null || list.m_bModFixed) {
         return;
      }

      for (int i = 0; i < _COUNT; i++) {
         if (s_bEverywhere[i] && !AnimalBonusTypes.IsInList(list, i)) {
            list.AddBonusType(i);
         }
      }
   }

   // Cran de place 0..5 : la place réelle (0 = 1er) ramenée à 6 coureurs, pour qu'en duel le 2e tire comme un dernier.
   static int PlaceSlot(Vehicle v) {
      int n = RacerModule.m_Mod.m_RaceParams.m_SortTable.size();
      int place = v.m_iCurrPlace;
      if (n <= 1 || place <= 0) {
         return 0;
      }

      if (place >= n - 1) {
         return _PLACES - 1;
      }

      return (place * (_PLACES - 1) * 2 + (n - 1)) / ((n - 1) * 2); // arrondi
   }

   // Identifiant du bonus tiré parmi « types » (liste du monde, ou du monde ∩ animal), -1 = tirage d'origine.
   // Évite de redonner le même bonus deux fois de suite quand un autre est possible (comme l'original).
   static int Pick(java.util.Vector types, Vehicle v) {
      if (!s_bLoaded || types == null || types.size() == 0) {
         return -1;
      }

      int slot = PlaceSlot(v);
      int nLast = v.m_iLastBonusType;
      float fTotal = 0.0F;
      float fTotalNoLast = 0.0F;

      for (int i = 0; i < types.size(); i++) {
         float w = Weight(((BonusType)types.elementAt(i)).m_iType, slot);
         fTotal += w;
         if (((BonusType)types.elementAt(i)).m_iType != nLast) {
            fTotalNoLast += w;
         }
      }

      int id;
      if (fTotal <= 0.0F) {
         // aucun bonus du monde n'est permis à cette place : tirage uniforme plutôt que rien
         id = ((BonusType)types.elementAt(RacerGame.GetRandom(types.size()))).m_iType;
      } else {
         boolean bSkipLast = fTotalNoLast > 0.0F;
         float r = RacerGame.GetRandom() * (bSkipLast ? fTotalNoLast : fTotal);
         id = -1;

         for (int i = 0; i < types.size(); i++) {
            int t = ((BonusType)types.elementAt(i)).m_iType;
            if (bSkipLast && t == nLast) {
               continue;
            }

            float w = Weight(t, slot);
            if (w <= 0.0F) {
               continue;
            }

            id = t;
            r -= w;
            if (r < 0.0F) {
               break;
            }
         }
      }

      if (v.m_VehicleAI == null) {
         GameObject.CrashLog(
            "[mod] bonus : joueur " + (v.m_iCurrPlace + 1) + "e sur " + RacerModule.m_Mod.m_RaceParams.m_SortTable.size()
               + " (cran " + (slot + 1) + ") -> " + Name(id) + "\n"
         );
      }

      return id;
   }

   static float Weight(int id, int slot) {
      return id >= 0 && id < _COUNT ? s_Weights[id][slot] : 0.0F;
   }
}
