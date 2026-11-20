public class LevelBonusTypes {
   static final int _BONUS_UNKNOWN = -1;
   static final int _BONUS_BOMB = 0;
   static final int _BONUS_CORKPISTOL = 1;
   static final int _BONUS_CATAPULT = 2;
   static final int _BONUS_TURBO = 3;
   static final int _BONUS_PROTECTION = 4;
   static final int _BONUS_OILCAN = 5;
   static final int _BONUS_FUNNYGAS = 6;
   static final int _BONUS_WATERBOTTLE = 7;
   static final int _BONUS_BANANA = 8;
   static final int _BONUS_WASPBAG = 9;
   static final int _BONUS_ANVIL = 10;
   static final int _BONUS_TIRESPIKES = 11;
   static final int _BONUS_TOMATOLAUNCHER = 12;
   static final int _BONUS_WATERMELON = 13;
   static final int _BONUS_FREEZEPISTOL = 14;
   static final int _BONUS_WATERPISTOL = 15;
   static final int _BONUS_COCONUT = 16;
   static final int _BONUS_SNOWBALL = 17;
   static final int _BONUS_COOKIE = 18;
   static final int _BONUS_ROCKET = 19;
   static final int _BONUS_UFO = 20;
   static final int _BONUS_DUMMIE = 21; // inutilisé dans le jeu d'origine
   // [mod] Missile : une fusée qui vise toujours le premier (Rocket.m_bModMissile)
   static final int _BONUS_MOD_MISSILE = 21;
   java.util.Vector m_Types = new java.util.Vector();
   // [mod] liste imposée par une règle de course extra (turbo seul, bombes seules, sans bonus) : ModBonuses.ExtendLevel
   // n'y ajoute rien
   boolean m_bModFixed = false;

   UseItem CreateBonus(int var1, Vehicle var2) {
      switch (var1) {
         case 0:
            return UseItem.CreateBomb(var2);
         case 1:
            return UseItem.CreateCorkPistol(var2);
         case 2:
            return UseItem.CreateCatapult(var2);
         case 3:
            return UseItem.CreateTurbo(var2);
         case 4:
            return UseItem.CreateProtection(var2);
         case 5:
            return UseItem.CreateOilCan(var2);
         case 6:
            return UseItem.CreateFunnyGas(var2);
         case 7:
            return UseItem.CreateWaterBottle(var2);
         case 8:
            return UseItem.CreateBanana(var2);
         case 9:
            return UseItem.CreateWaspBag(var2);
         case 10:
            return UseItem.CreateAnvil(var2);
         case 11:
            return UseItem.CreateTireSpikes(var2);
         case 12:
            return UseItem.CreateTomatoLauncher(var2);
         case 13:
            return UseItem.CreateWaterMelon(var2);
         case 14:
            return UseItem.CreateFreezePistol(var2);
         case 15:
            return UseItem.CreateWaterPistol(var2);
         case 16:
            return UseItem.CreateCoconut(var2);
         case 17:
            return UseItem.CreateSnowBall(var2);
         case 18:
            return UseItem.CreateCookie(var2);
         case 19:
            return UseItem.CreateRocket(var2);
         case 20:
            return UseItem.CreateUFO(var2);
         case _BONUS_MOD_MISSILE:
            return Rocket.ModCreateMissile(var2);
         default:
            return null;
      }
   }

   void AddBonusTypeNoCreate(int var1) {
      this.m_Types.addElement(new BonusType(var1));
   }

   UseItem GetRandomBonus(Vehicle var1) {
      if (this.m_Types.size() == 0) {
         return null;
      }

      LevelBonusTypes var2 = AnimalBonusTypes.GetBonuses(var1.m_iAnimalID);
      // [mod] tirage pondéré par la place (Data\bonuses.scr, ModBonuses), parmi la même liste que l'original
      java.util.Vector modTypes = var2 != null && var2.m_Types != null && var2.m_Types.size() > 0 ? var2.m_Types : this.m_Types;
      int modId = ModBonuses.Pick(modTypes, var1);
      if (modId >= 0) {
         var1.m_iLastBonusType = modId;
         return this.CreateBonus(modId, var1);
      }

      if (var2 != null && var2.m_Types != null && var2.m_Types.size() > 0) {
         java.util.Vector var3 = var2.m_Types;
         if (var3.size() > 1) {
            int var4 = RacerGame.GetRandom(var3.size());
            if (var1.m_iLastBonusType == ((BonusType)var3.elementAt(var4)).m_iType) {
               if (--var4 < 0) {
                  var4 = var3.size() - 1;
               }
            }

            var1.m_iLastBonusType = ((BonusType)var3.elementAt(var4)).m_iType;
            return this.CreateBonus(var1.m_iLastBonusType, var1);
         } else {
            return this.CreateBonus(((BonusType)var3.elementAt(0)).m_iType, var1);
         }
      } else {
         return this.CreateBonus(((BonusType)this.m_Types.elementAt(RacerGame.GetRandom(this.m_Types.size()))).m_iType, var1);
      }
   }

   void AddBonusType(int var1) {
      this.m_Types.addElement(new BonusType(var1));
      UseItem var2 = this.CreateBonus(var1, null);
      if (var2 != null) {
         var2.SetFirst();
      }
   }
}
