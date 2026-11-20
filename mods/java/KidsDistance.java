public class KidsDistance extends KidsMenuWindow {
   static final float width = 0.075F;
   static final float height = 0.6F;
   static final float xpos = 0.05F;
   static final float ypos = 0.5F;
   static final float wndPointWidth = 0.5025F;
   static final float wndPointHeight = 0.075375F;
   static final float wndPointPosX = 0.5F;
   static final float wndPointPosY = 0.0F;
   static final float wndPointLowerLimiter = 0.9F;
   static final float wndPointHigherLimiter = 0.1F;
   String[] m_sBackground = new String[]{"Data/Textures/Hud/Pasek_3.png", "Data/Textures/Hud/Pasek_5.png", "Data/Textures/Hud/Pasek_7.png"};
   KidsMenuWindow[] wndPoints = null;
   static final int _MOD_MAX_LAPS = 10; // [mod] ModPasek_1.png ... ModPasek_10.png

   void SetTextures0() {
      for (int var1 = 0; var1 < this.wndPoints.length; var1++) {
         this.wndPoints[var1].SetWindowTexture(0);
      }
   }

   void InitHud(Module var1, IntList var2) {
      int var3 = var2.m_Cnt;
      CoreLogic var4 = RacerGame.GetCoreLogic();
      super.InitHud(var1);
      // [mod] une barre par nombre de tours (mods/art/make_lap_bars.py) : l'original n'en avait que pour 3 et 7
      // (Pasek_5 est une copie de Pasek_7) et prenait celle de 7 tours pour 1, 2, 4, 5, 6, 8, 9 ou 10 tours
      String sBar = this.m_sBackground[2];
      if (var4.m_Laps >= 1 && var4.m_Laps <= _MOD_MAX_LAPS) {
         sBar = "Data/Textures/Hud/ModPasek_" + var4.m_Laps + ".png";
      }

      super.m_TexturesList.AddTexture(this.AddTexture(sBar, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), sBar);

      super.m_TexturesList.SetTexture(this, 0);
      this.wndPoints = new KidsMenuWindow[var3];

      for (int var5 = 0; var5 < var3; var5++) {
         this.wndPoints[var5] = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
         this.Add(this.wndPoints[var5]);
         this.wndPoints[var5].InitHud(var1);
         this.wndPoints[var5]
            .AddWindowTexture(
               this.wndPoints[var5].AddTexture(MenuAnimal.sAnimalsFaces[var2.GetAt(var5)], true, true),
               new Rect(0.0F, 0.0F, 1.0F, 1.0F),
               MenuAnimal.sAnimalsFaces[var2.GetAt(var5)]
            );
         this.wndPoints[var5]
            .AddWindowTexture(
               this.wndPoints[var5].AddTexture(MenuAnimal.sAnimalsFacesHL[var2.GetAt(var5)], true, true),
               new Rect(0.0F, 0.0F, 1.0F, 1.0F),
               MenuAnimal.sAnimalsFacesHL[var2.GetAt(var5)]
            );
         this.wndPoints[var5].SetSizeAndPos(0.5025F, 0.075375F, 0.5F, 0.0F, true);
         this.SetDistance(0.0F, var5);
         this.wndPoints[var5].SetWindowTexture(0);
         this.wndPoints[var5].Show();
      }

      this.SetSizeAndPos(0.075F, 0.6F, 0.05F, 0.5F, true);
      this.RefreshSetSizeAndPos();
      this.SetAlpha(0.9F);
   }

   void SetDistance(float var1, int var2) {
      if (var1 < 0.0F) {
         var1 = 0.0F;
      }

      if (var1 > 1.0F) {
         var1 = 1.0F;
      }

      float var3 = 0.9F + var1 * -0.79999995F;
      this.wndPoints[var2].SetSizeAndPos(0.5025F, 0.075375F, 0.5F, var3, true);
   }

   void MoveTop(int var1) {
      this.wndPoints[var1].MoveTop();
   }

   void SetTextureHL(int var1) {
      this.wndPoints[var1].SetWindowTexture(1);
   }
}
