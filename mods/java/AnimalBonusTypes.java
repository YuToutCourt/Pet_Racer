public class AnimalBonusTypes {
   static LevelBonusTypes[] animalsbonuses = new LevelBonusTypes[12];
   static LevelBonusTypes[] ab = new LevelBonusTypes[12];

   static LevelBonusTypes GetBonuses(int var0) {
      return ab[var0];
   }

   static boolean IsInList(LevelBonusTypes var0, int var1) {
      for (int var3 = 0; var3 < var0.m_Types.size(); var3++) {
         BonusType var2 = (BonusType)var0.m_Types.elementAt(var3);
         if (var1 == var2.m_iType) {
            return true;
         }
      }

      return false;
   }

   // Appelé par chaque circuit (Las1.InitModule …) juste après avoir rempli sa liste de bonus
   static void Init2(LevelBonusTypes var0) {
      ModBonuses.ExtendLevel(var0); // [mod] bonus « partout » de Data\bonuses.scr (la Fusée par défaut)

      for (int var1 = 0; var1 < ab.length; var1++) {
         ab[var1] = GetIntersection(var0, var1);
      }
   }

   static LevelBonusTypes GetIntersection(LevelBonusTypes var0, int var1) {
      if (var1 >= 0 && var1 < animalsbonuses.length) {
         LevelBonusTypes var2 = new LevelBonusTypes();
         int var3 = 0;
         LevelBonusTypes var4 = animalsbonuses[var1];
         if (var0 != null && var4 != null) {
            for (int var6 = 0; var6 < var0.m_Types.size(); var6++) {
               int var5 = ((BonusType)var0.m_Types.elementAt(var6)).m_iType;
               // [mod] les bonus ajoutés par le mod (Missile, 21) ne sont dans aucune liste d'animal : toujours permis
               if (IsInList(var4, var5) || var5 >= LevelBonusTypes._BONUS_MOD_MISSILE) {
                  var2.AddBonusTypeNoCreate(var5);
                  var3++;
               }
            }
         }

         if (var3 == 0) {
            var2 = null;
         }

         return var2;
      } else {
         return null;
      }
   }

   static void Init() {
      LevelBonusTypes var0 = null;
      animalsbonuses[0] = new LevelBonusTypes();
      var0 = animalsbonuses[0];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[1] = new LevelBonusTypes();
      var0 = animalsbonuses[1];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[2] = new LevelBonusTypes();
      var0 = animalsbonuses[2];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[3] = new LevelBonusTypes();
      var0 = animalsbonuses[3];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[4] = new LevelBonusTypes();
      var0 = animalsbonuses[4];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[5] = new LevelBonusTypes();
      var0 = animalsbonuses[5];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[6] = new LevelBonusTypes();
      var0 = animalsbonuses[6];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[7] = new LevelBonusTypes();
      var0 = animalsbonuses[7];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[8] = new LevelBonusTypes();
      var0 = animalsbonuses[8];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[9] = new LevelBonusTypes();
      var0 = animalsbonuses[9];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[10] = new LevelBonusTypes();
      var0 = animalsbonuses[10];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
      animalsbonuses[11] = new LevelBonusTypes();
      var0 = animalsbonuses[11];
      var0.AddBonusTypeNoCreate(10);
      var0.AddBonusTypeNoCreate(8);
      var0.AddBonusTypeNoCreate(0);
      var0.AddBonusTypeNoCreate(2);
      var0.AddBonusTypeNoCreate(16);
      var0.AddBonusTypeNoCreate(18);
      var0.AddBonusTypeNoCreate(1);
      var0.AddBonusTypeNoCreate(14);
      var0.AddBonusTypeNoCreate(6);
      var0.AddBonusTypeNoCreate(5);
      var0.AddBonusTypeNoCreate(4);
      var0.AddBonusTypeNoCreate(19);
      var0.AddBonusTypeNoCreate(17);
      var0.AddBonusTypeNoCreate(11);
      var0.AddBonusTypeNoCreate(12);
      var0.AddBonusTypeNoCreate(3);
      var0.AddBonusTypeNoCreate(20);
      var0.AddBonusTypeNoCreate(9);
      var0.AddBonusTypeNoCreate(7);
      var0.AddBonusTypeNoCreate(13);
      var0.AddBonusTypeNoCreate(15);
   }
}
