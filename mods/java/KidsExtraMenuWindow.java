public class KidsExtraMenuWindow extends KidsMenuWindow {
   int bkTex0 = -1;
   String m_TexName = "Data/Textures/Menu/b/PlanszaNiebieska.png";
   String m_TexLogoPetRacer = "Data/Textures/Menu/a/LogoPetRacer.png";
   int[] wndExtra = new int[]{-1, -1, -1, -1, -1};
   String[] wndExtraTextures = new String[]{
      "Data/Textures/Menu/a/Turbo.png",
      "Data/Textures/Menu/a/MegaTNT.png",
      "Data/Textures/Menu/a/FairPlayMenuExtra.png",
      "Data/Textures/Menu/a/LowGravity.png",
      "Data/Textures/Menu/a/PowerRace.png"
   };
   String[] wndExtraTexturesDisabled = new String[]{
      "Data/Textures/Menu/a/TurboSzary.png",
      "Data/Textures/Menu/a/MegaTNTSzary.png",
      "Data/Textures/Menu/a/FairPlayMenuExtraSzary.png",
      "Data/Textures/Menu/a/LowGravitySzary.png",
      "Data/Textures/Menu/a/PowerRaceSzary.png"
   };
   float[] wndExtraPosX = new float[]{0.60625F, 0.45625F, 0.8125F, 0.2F, 0.75F};
   float[] wndExtraPosY = new float[]{0.13333334F, 0.6666667F, 0.325F, 0.68333334F, 0.55833334F};
   float[] wndExtraWidth = new float[]{0.2F, 0.2F, 0.2F, 0.2F, 0.4F};
   float[] wndExtraHeight = new float[]{0.26666668F, 0.26666668F, 0.26666668F, 0.26666668F, 0.26666668F};
   int[] btnExtra = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
   int[] btnExtraP = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
   InitKidsButton[] btnExtraInit = new InitKidsButton[]{
      new InitKidsButton(18),
      new InitKidsButton(19),
      new InitKidsButton(20),
      new InitKidsButton(18),
      new InitKidsButton(19),
      new InitKidsButton(20),
      new InitKidsButton(18),
      new InitKidsButton(19),
      new InitKidsButton(20),
      new InitKidsButton(18),
      new InitKidsButton(19),
      new InitKidsButton(20),
      new InitKidsButton(18),
      new InitKidsButton(19),
      new InitKidsButton(20)
   };
   InitKidsButton[] btnExtraPInit = new InitKidsButton[]{
      new InitKidsButton(21),
      new InitKidsButton(22),
      new InitKidsButton(23),
      new InitKidsButton(21),
      new InitKidsButton(22),
      new InitKidsButton(23),
      new InitKidsButton(21),
      new InitKidsButton(22),
      new InitKidsButton(23),
      new InitKidsButton(21),
      new InitKidsButton(22),
      new InitKidsButton(23),
      new InitKidsButton(21),
      new InitKidsButton(22),
      new InitKidsButton(23)
   };
   float[] btnExtraPosX = new float[]{
      0.54375F, 0.63125F, 0.7125F, 0.4125F, 0.5F, 0.5828125F, 0.73125F, 0.81875F, 0.9F, 0.1125F, 0.2F, 0.2875F, 0.675F, 0.75625F, 0.84375F
   };
   float[] btnExtraPosY = new float[]{
      0.225F,
      0.24166666F,
      0.21666667F,
      0.7916667F,
      0.81666666F,
      0.7916667F,
      0.44166666F,
      0.46666667F,
      0.44166666F,
      0.8666667F,
      0.89166665F,
      0.8666667F,
      0.65833336F,
      0.68333334F,
      0.65833336F
   };
   float btnExtraWidth = 0.0625F;
   float btnExtraHeight = 0.0625F;
   float btnExtraXRatio = 1.6F;
   float btnExtraYRatio = 1.6F;
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(57);

   void ButtonsOnTop() {
      super.m_List.SetOnTop(this.btnExit);
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
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(super.m_Module.PopMenu());
      } else if (var1 == this.btnExtra[0] || var1 == this.btnExtraP[0]) {
         this.ProceedChoice(4);
      } else if (var1 == this.btnExtra[1] || var1 == this.btnExtraP[1]) {
         this.ProceedChoice(5);
      } else if (var1 == this.btnExtra[2] || var1 == this.btnExtraP[2]) {
         this.ProceedChoice(6);
      } else if (var1 == this.btnExtra[3] || var1 == this.btnExtraP[3]) {
         this.ProceedChoice(7);
      } else if (var1 == this.btnExtra[4] || var1 == this.btnExtraP[4]) {
         this.ProceedChoice(8);
      } else if (var1 == this.btnExtra[5] || var1 == this.btnExtraP[5]) {
         this.ProceedChoice(9);
      } else if (var1 == this.btnExtra[6] || var1 == this.btnExtraP[6]) {
         this.ProceedChoice(10);
      } else if (var1 == this.btnExtra[7] || var1 == this.btnExtraP[7]) {
         this.ProceedChoice(11);
      } else if (var1 == this.btnExtra[8] || var1 == this.btnExtraP[8]) {
         this.ProceedChoice(12);
      } else if (var1 == this.btnExtra[9] || var1 == this.btnExtraP[9]) {
         this.ProceedChoice(13);
      } else if (var1 == this.btnExtra[10] || var1 == this.btnExtraP[10]) {
         this.ProceedChoice(14);
      } else if (var1 == this.btnExtra[11] || var1 == this.btnExtraP[11]) {
         this.ProceedChoice(15);
      } else if (var1 == this.btnExtra[12] || var1 == this.btnExtraP[12]) {
         this.ProceedChoice(16);
      } else if (var1 == this.btnExtra[13] || var1 == this.btnExtraP[13]) {
         this.ProceedChoice(17);
      } else if (var1 == this.btnExtra[14] || var1 == this.btnExtraP[14]) {
         this.ProceedChoice(18);
      }
   }

   void ProceedChoice(int var1) {
      super.m_Module.PushMenu(super.m_WindowID);
      super.m_Module.SetMenuCX(var1);
      this.DisactivateWindow();
      super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._AnimalChoiceMenuWindow);
   }

   void ShowExtra(int var1, int var2) {
      switch (var2) {
         case 1:
            super.m_List.Show(this.btnExtra[var1]);
            super.m_List.ProcessKb(this.btnExtra[var1], false);
            super.m_List.SetEnabledButton(this.btnExtra[var1], true);
            super.m_List.ProcessKb(this.btnExtraP[var1], false);
            super.m_List.SetEnabledButton(this.btnExtraP[var1], false);
            super.m_List.Hide(this.btnExtraP[var1]);
            return;
         case 2:
            super.m_List.Show(this.btnExtraP[var1]);
            super.m_List.ProcessKb(this.btnExtraP[var1], false);
            super.m_List.SetEnabledButton(this.btnExtraP[var1], true);
            super.m_List.ProcessKb(this.btnExtra[var1], false);
            super.m_List.SetEnabledButton(this.btnExtra[var1], false);
            super.m_List.Hide(this.btnExtra[var1]);
            return;
         default:
            super.m_List.ProcessKb(this.btnExtra[var1], false);
            super.m_List.SetEnabledButton(this.btnExtra[var1], false);
            super.m_List.Hide(this.btnExtra[var1]);
            super.m_List.ProcessKb(this.btnExtraP[var1], false);
            super.m_List.SetEnabledButton(this.btnExtraP[var1], false);
            super.m_List.Hide(this.btnExtraP[var1]);
      }
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

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(this.m_TexName, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), this.m_TexName);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      KidsButton var4 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExit = this.Add(var4);
      this.btnExitInit.bMoveButton = true;
      var4.Init(super.m_Module, this, this.btnExit, this.btnExitInit);
      var4.ProcessMouse(true);

      for (int var3 = 0; var3 < this.btnExtra.length; var3++) {
         var4 = (KidsButton)this.CreateObject("KidsButton");
         this.btnExtra[var3] = this.Add(var4);
         this.btnExtraInit[var3].bMoveButton = true;
         this.btnExtraInit[var3].xpos = this.btnExtraPosX[var3];
         this.btnExtraInit[var3].ypos = this.btnExtraPosY[var3];
         this.btnExtraInit[var3].width = this.btnExtraWidth;
         this.btnExtraInit[var3].height = this.btnExtraHeight;
         this.btnExtraInit[var3].ButtonWidth = this.btnExtraXRatio;
         this.btnExtraInit[var3].ButtonHeight = this.btnExtraYRatio;
         var4.Init(super.m_Module, this, this.btnExtra[var3], this.btnExtraInit[var3]);
         var4.ProcessMouse(true);
      }

      for (int var7 = 0; var7 < this.btnExtraP.length; var7++) {
         var4 = (KidsButton)this.CreateObject("KidsButton");
         this.btnExtraP[var7] = this.Add(var4);
         this.btnExtraPInit[var7].bMoveButton = true;
         this.btnExtraPInit[var7].xpos = this.btnExtraPosX[var7];
         this.btnExtraPInit[var7].ypos = this.btnExtraPosY[var7];
         this.btnExtraPInit[var7].width = this.btnExtraWidth;
         this.btnExtraPInit[var7].height = this.btnExtraHeight;
         this.btnExtraPInit[var7].ButtonWidth = this.btnExtraXRatio;
         this.btnExtraPInit[var7].ButtonHeight = this.btnExtraYRatio;
         var4.Init(super.m_Module, this, this.btnExtraP[var7], this.btnExtraPInit[var7]);
         var4.ProcessMouse(true);
      }

      KidsMenuWindow var5 = null;

      for (int var8 = 0; var8 < this.wndExtra.length; var8++) {
         var5 = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
         this.wndExtra[var8] = this.Add(var5);
         var5.Init(super.m_Module, this.wndExtra[var8]);
         var5.AddWindowTexture(var5.AddTexture(this.wndExtraTextures[var8], true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.wndExtraTextures[var8]);
         var5.AddWindowTexture(
            var5.AddTexture(this.wndExtraTexturesDisabled[var8], true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.wndExtraTexturesDisabled[var8]
         );
         var5.SetSizeAndPos(this.wndExtraWidth[var8], this.wndExtraHeight[var8], this.wndExtraPosX[var8], this.wndExtraPosY[var8], true);
         var5.SetWindowTexture(0);
         var5.Show();
      }

      var5 = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      int var6 = this.Add(var5);
      var5.Init(super.m_Module, var6);
      var5.AddWindowTexture(var5.AddTexture(this.m_TexLogoPetRacer, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.m_TexLogoPetRacer);
      var5.SetSizeAndPos(0.4F, 0.53333336F, 0.2625F, 0.31666666F, true);
      var5.SetWindowTexture(0);
      var5.Show();
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.ProcessKb(true);
      super.m_Ctrl = new ControlWindowList(40);
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[0]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[0]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[1]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[1]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[2]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[2]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[6]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[6]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[7]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[7]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[8]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[8]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[12]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[12]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[13]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[13]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[14]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[14]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[3]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[3]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[4]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[4]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[5]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[5]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[9]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[9]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[10]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[10]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra[11]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtraP[11]));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExit));
      this.SetAlphaForAll(1.0F);
   }

   public void RefreshControls() {
      CoreLogic var1 = RacerGame.GetCoreLogic();

      for (int var2 = 0; var2 < this.btnExtra.length; var2++) {
         this.ShowExtra(var2, var1.GetExtraState(var2)); // [mod] déblocage du menu Mods
      }

      for (int var3 = 0; var3 < this.wndExtra.length; var3++) {
         // [mod] déblocage du menu Mods
         if (var1.GetExtraState(3 * var3) <= 0 && var1.GetExtraState(3 * var3 + 1) <= 0 && var1.GetExtraState(3 * var3 + 2) <= 0) {
            super.m_List.SetTexture(this.wndExtra[var3], 1);
         } else {
            super.m_List.SetTexture(this.wndExtra[var3], 0);
         }
      }
   }
}
