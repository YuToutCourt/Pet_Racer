public class KidsMapLandsMenuWindow extends KidsMenuWindow {
   int bkTex0 = -1;
   String m_TexName = "Data/Textures/Menu/b/PlanszaNiebieska.png";
   String m_TexLogo = "Data/Textures/Menu/a/LogoPetRacer.png";
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(57);
   int[] btnLands = new int[]{-1, -1, -1, -1, -1, -1};
   InitKidsButton[] btnLandsInit = new InitKidsButton[]{
      new InitKidsButton(58), new InitKidsButton(59), new InitKidsButton(60), new InitKidsButton(61), new InitKidsButton(62), new InitKidsButton(63)
   };
   float[] btnLandsPosX = new float[]{0.59375F, 0.775F, 0.71875F, 0.6F, 0.375F, 0.14375F};
   float[] btnLandsPosY = new float[]{0.13333334F, 0.30833334F, 0.51666665F, 0.73333335F, 0.69166666F, 0.78333336F};
   float btnLandsWidth = 0.1171875F;
   float btnLandsHeight = 0.15625F;
   float[] btnLandsXRatio = new float[]{1.7066667F, 1.7066667F, 3.4133334F, 1.7066667F, 1.7066667F, 1.7066667F};
   float btnLandsYRatio = 1.7066667F;

   void ButtonsOnTop() {
   }

   void SetDisabledButtons() {
      for (int var1 = 0; var1 < this.btnLands.length; var1++) {
         if (var1 >= 0 && var1 < this.btnLands.length) {
            super.m_List.SetEnabledButton(this.btnLands[var1], false);
         }
      }
   }

   void SetStateForOthers() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      int var2 = (var1.GetAvailableTracksCount() - 1) / 6; // [mod] déblocage du menu Mods
      this.SetDisabledButtons();
      this.SetEnabledButtons(0, var2);
   }

   public void OnKeyReleased(int var1, char var2) {
      if (this.CanProcessMouseMove() && !super.m_Module.CanIgnoreKeys()) {
         if (var1 == 203) {
            super.m_Module.DeactivateCursor();
            super.m_Ctrl.Previous();
         } else if (var1 == 205) {
            super.m_Module.DeactivateCursor();
            super.m_Ctrl.Next();
         } else if (var1 == 1) {
            this.ButtonPushed(this.btnExit);
         }
      }
   }

   public void ButtonPushed(int var1) {
      super.ButtonPushed(var1);
      if (var1 == this.btnExit) {
         switch (super.m_Module.GetMenuCX()) {
            case 1:
               this.DisactivateWindow();
               super.m_Module.ClearStack();
               super.m_Module.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
               super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._SinglePlayerMenuWindow);
               return;
            default:
               this.DisactivateWindow();
               super.m_Module.ActivateMenuWindow(super.m_Module.PopMenu());
         }
      } else {
         for (int var2 = 0; var2 < this.btnLands.length; var2++) {
            if (var1 == this.btnLands[var2]) {
               KidsMapTracksMenuWindow var3 = (KidsMapTracksMenuWindow)super.m_Module.GetWindowAt(KidsSimpleMenuModule._MapTracksMenuWindow);
               var3.m_CurrentLand = var2;
               super.m_Module.PushMenu(super.m_WindowID);
               this.DisactivateWindow();
               super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._MapTracksMenuWindow);
               return;
            }
         }
      }
   }

   void SetStateForAdventure() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      int var2 = var1.m_nPassedTracks / 6;
      this.SetDisabledButtons();
      this.SetEnabledButtons(var2, var2);
   }

   public void Hide() {
      super.Hide();
      this.ProcessKb(false);
   }

   public void Show() {
      super.m_Module.SetIgnoreKeys(2);
      super.Show();
      this.ProcessKb(true);
      ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, false);
      this.RefreshControls();
      if (!KidsMenuModule.m_CarsManager.IsInitialized()) {
         KidsMenuModule.m_CarsManager.ActivateCarsManager();
      }

      KidsMenuModule.m_CarsManager.Show(this);
      this.ButtonsOnTop();
   }

   void SetEnabledButtons(int var1, int var2) {
      CoreLogic var3 = RacerGame.GetCoreLogic();

      for (int var4 = var1; var4 <= var2; var4++) {
         if (var4 >= 0 && var4 < this.btnLands.length && !var3.IsLandLocked(var4)) {
            super.m_List.SetEnabledButton(this.btnLands[var4], true);
         }
      }
   }

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(this.m_TexName, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), this.m_TexName);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      KidsButton var4 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExit = this.Add(var4);
      this.btnExitInit.bMoveButton = true;
      var4.Init(super.m_Module, this, this.btnExit, this.btnExitInit);
      var4.ProcessMouse(true);

      for (int var3 = 0; var3 < this.btnLands.length; var3++) {
         var4 = (KidsButton)this.CreateObject("KidsButton");
         this.btnLands[var3] = this.Add(var4);
         this.btnLandsInit[var3].bMoveButton = true;
         this.btnLandsInit[var3].xpos = this.btnLandsPosX[var3];
         this.btnLandsInit[var3].ypos = this.btnLandsPosY[var3];
         this.btnLandsInit[var3].width = this.btnLandsWidth;
         this.btnLandsInit[var3].height = this.btnLandsHeight;
         this.btnLandsInit[var3].ButtonWidth = this.btnLandsXRatio[var3];
         this.btnLandsInit[var3].ButtonHeight = this.btnLandsYRatio;
         var4.Init(super.m_Module, this, this.btnLands[var3], this.btnLandsInit[var3]);
         var4.ProcessMouse(true);
      }

      KidsMenuWindow var5 = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      int var6 = this.Add(var5);
      var5.Init(super.m_Module, var6);
      var5.AddWindowTexture(var5.AddTexture(this.m_TexLogo, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.m_TexLogo);
      var5.SetSizeAndPos(0.4F, 0.53333336F, 0.2625F, 0.31666666F, true);
      var5.SetWindowTexture(0);
      var5.Show();
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.ProcessKb(true);
      super.m_Ctrl = new ControlWindowList(10);

      for (int var7 = 0; var7 < this.btnLands.length; var7++) {
         super.m_Ctrl.Push(super.m_List.WindowAt(this.btnLands[var7]));
      }

      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExit));
      this.SetAlphaForAll(1.0F);
   }

   void RefreshControls() {
      if (super.m_Module.GetMenuCX() == 1) {
         this.SetStateForAdventure();
      } else {
         this.SetStateForOthers();
      }
   }
}
