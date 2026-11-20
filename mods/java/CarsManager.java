public class CarsManager extends GameObject {
   static final int _NO_OF_CARS = 12;
   static final int _NO_OF_BACK_CARS = 0;
   static float _BACK_CAR_SIZE_COEF = 0.5F;
   static final int _NO_OF_LINES_LEFT = 3;
   static final int _NO_OF_LINES_RIGHT = 3;
   static final int _NO_OF_BACK_LINES_LEFT = 0;
   static final int _NO_OF_BACK_LINES_RIGHT = 0;
   static final float _MIN_LINE_SPEED = 0.4F;
   static final float _MAX_LINE_SPEED = 1.3F;
   static final float _MIN_BACK_LINE_SPEED = 0.1F;
   static final float _MAX_BACK_LINE_SPEED = 0.2F;
   static final float _MIN_LINE_DIST = 0.01F;
   static final float _MAX_LINE_DIST = 0.03F;
   static int _NOF_CARS_TYPES = 12;
   static int _CAR_1 = 0;
   static int _CAR_2 = 1;
   static int _CAR_3 = 2;
   static int _CAR_4 = 3;
   static int _CAR_5 = 4;
   static int _CAR_6 = 5;
   static int _CAR_7 = 6;
   static int _CAR_8 = 7;
   static int _CAR_9 = 8;
   static int _CAR_10 = 9;
   static int _CAR_11 = 10;
   static int _CAR_12 = 11;
   static String[] m_CarTextures = new String[]{
      "Data\\Textures\\Menu\\Cars\\autko1.png",
      "Data\\Textures\\Menu\\Cars\\autko2.png",
      "Data\\Textures\\Menu\\Cars\\autko3.png",
      "Data\\Textures\\Menu\\Cars\\autko4.png",
      "Data\\Textures\\Menu\\Cars\\autko5.png",
      "Data\\Textures\\Menu\\Cars\\autko6.png",
      "Data\\Textures\\Menu\\Cars\\autko7.png",
      "Data\\Textures\\Menu\\Cars\\autko8.png",
      "Data\\Textures\\Menu\\Cars\\autko9.png",
      "Data\\Textures\\Menu\\Cars\\autko10.png",
      "Data\\Textures\\Menu\\Cars\\autko11.png",
      "Data\\Textures\\Menu\\Cars\\autko12.png"
   };
   static float[] m_CarXSizes = new float[]{0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F};
   static float[] m_CarYSizes = new float[]{0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F};
   static boolean[] m_Rotated = new boolean[]{true, true, true, false, false, false, false, true, false, true, false, true};
   boolean m_bUpdate = false;
   static float m_CurrTime = 0.0F;
   static java.util.Vector m_Car = new java.util.Vector();
   java.util.Vector m_Lines = new java.util.Vector();
   static float m_fScreenWidth = 0.0F;
   static float m_fScreenHeight = 0.0F;

   static AnimCar GetFreeCar(boolean var0, boolean var1) {
      for (int var2 = 0; var2 < m_Car.size(); var2++) {
         AnimCar var3 = (AnimCar)m_Car.elementAt(var2);
         if (var3 != null && !var3.m_bUpdate && m_Rotated[var3.m_iCarType] == var0 && var1 == var3.m_bIsBack) {
            return var3;
         }
      }

      return null;
   }

   boolean IsInitialized() {
      return m_Car.size() != 0;
   }

   void UpdateResolution() {
      float var1 = m_fScreenWidth;
      float var2 = m_fScreenHeight;
      m_fScreenWidth = ModVideo.GetWidth(); // [mod] taille réelle (ModVideo)
      m_fScreenHeight = ModVideo.GetHeight(); // [mod]

      for (int var3 = 0; var3 < m_Car.size(); var3++) {
         AnimCar var4 = (AnimCar)m_Car.elementAt(var3);
         var4.SetCarSize(var4.m_iCarType);
         var4.SetSize(var4.GetCarSize());
      }

      for (int var5 = 0; var5 < this.m_Lines.size(); var5++) {
         ((CarLine)this.m_Lines.elementAt(var5)).Resize(var1, var2);
      }
   }

   void Hide() {
      for (int var1 = 0; var1 < m_Car.size(); var1++) {
         ((Sprite)m_Car.elementAt(var1)).Hide();
         ((Sprite)m_Car.elementAt(var1)).DetachFromParent();
      }

      this.m_bUpdate = false;
   }

   void DeactivateCarsManager() {
      this.m_bUpdate = false;

      for (int var1 = 0; var1 < m_Car.size(); var1++) {
         AnimCar var2 = (AnimCar)m_Car.elementAt(var1);
         var2.CleanUpObject();
      }
   }

   void ActivateCarsManager() {
      m_fScreenWidth = ModVideo.GetWidth(); // [mod] taille réelle (ModVideo)
      m_fScreenHeight = ModVideo.GetHeight(); // [mod]
      if (m_Car.size() > 0) {
         for (int var8 = 0; var8 < m_Car.size(); var8++) {
            AnimCar var10 = (AnimCar)m_Car.elementAt(var8);
            var10.SetUpObject();
         }
      } else {
         int var1 = 0;

         do {
            AnimCar var2 = (AnimCar)RacerGame.firstMenuModule.CreateObjectNoInit("AnimCar");
            var2.SetCarType(RacerGame.GetRandom(_NOF_CARS_TYPES));
            var2.InitObject();
            m_Car.addElement(var2);
         } while (++var1 < 12);

         for (int var3 = 0; var3 < 0; var3++) {
            AnimCar var9 = (AnimCar)RacerGame.firstMenuModule.CreateObjectNoInit("AnimCar");
            var9.SetCarType(RacerGame.GetRandom(_NOF_CARS_TYPES));
            var9.SetCarSize(m_CarXSizes[var9.m_iCarType] * _BACK_CAR_SIZE_COEF, m_CarYSizes[var9.m_iCarType] * _BACK_CAR_SIZE_COEF);
            var9.SetCarBack(true);
            var9.InitObject();
            m_Car.addElement(var9);
         }

         var1 = 0;

         do {
            this.m_Lines.addElement(new CarLine(GetRndVal(0.4F, 1.3F), var1 * 0.3F, 0.01F, 0.03F, false, false));
         } while (++var1 < 3);

         var1 = 0;

         do {
            this.m_Lines.addElement(new CarLine(GetRndVal(0.4F, 1.3F), var1 * 0.3F - 0.15F, 0.01F, 0.03F, true, false));
         } while (++var1 < 3);

         for (int var6 = 0; var6 < 0; var6++) {
            this.m_Lines.addElement(new CarLine(GetRndVal(0.1F, 0.2F), var6 * 0.25F, 0.01F, 0.03F, false, true));
         }

         for (int var7 = 0; var7 < 0; var7++) {
            this.m_Lines.addElement(new CarLine(GetRndVal(0.1F, 0.2F), var7 * 0.25F - 0.15F, 0.01F, 0.03F, true, true));
         }
      }
   }

   void Show(Sprite var1) {
      for (int var2 = 0; var2 < m_Car.size(); var2++) {
         var1.AddChild((Sprite)m_Car.elementAt(var2));
         if (((AnimCar)m_Car.elementAt(var2)).m_bUpdate) {
            ((Sprite)m_Car.elementAt(var2)).Show();
         }
      }

      this.m_bUpdate = true;
   }

   static float GetRndVal(float var0, float var1) {
      return var0 + RacerGame.GetRandom() * (var1 - var0);
   }

   public void OnFrame() {
      if (this.m_bUpdate) {
         m_CurrTime = RacerGame.firstMenuModule.GetTime();

         for (int var1 = 0; var1 < this.m_Lines.size(); var1++) {
            int var2 = RacerGame.GetRandom(this.m_Lines.size());
            int var3 = RacerGame.GetRandom(this.m_Lines.size());
            Object var4 = this.m_Lines.elementAt(var2);
            this.m_Lines.setElementAt(this.m_Lines.elementAt(var3), var2);
            this.m_Lines.setElementAt(var4, var3);
         }

         for (int var5 = 0; var5 < this.m_Lines.size(); var5++) {
            ((CarLine)this.m_Lines.elementAt(var5)).UpdateLine();
         }

         for (int var6 = 0; var6 < m_Car.size(); var6++) {
            ((AnimCar)m_Car.elementAt(var6)).UpdateCar();
         }
      }
   }
}
