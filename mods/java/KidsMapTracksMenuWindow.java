public class KidsMapTracksMenuWindow extends KidsMenuWindow {
   int bkTex0 = -1;
   String m_TexName = "Data/Textures/Menu/b/PlanszaNiebieska.png";
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(57);
   int m_CurrentLand = -1;
   int wndBoss = -1;
   static float wndBossWidth = 0.4F;
   static float wndBossHeight = 0.53333336F;
   static float wndBossPosX = 0.2625F;
   static float wndBossPosY = 0.31666666F;
   static String[] BossesTextures = new String[]{
      "Data/Textures/Menu/a/LogoByczyBor.png",
      "Data/Textures/Menu/a/LogoSucheZiemieZyrafy.png",
      "Data/Textures/Menu/a/LogoMisiaZimowisko.png",
      "Data/Textures/Menu/a/LogoDzikaGorylaDzungla.png",
      "Data/Textures/Menu/a/LogoKsiezycSzalonegoRobota.png",
      "Data/Textures/Menu/a/LogoKrolestwoLwa.png"
   };
   float[] btnPosX = new float[]{
      0.675F,
      0.8125F,
      0.81875F,
      0.64375F,
      0.39375F,
      0.14375F,
      0.675F,
      0.8125F,
      0.81875F,
      0.64375F,
      0.39375F,
      0.14375F,
      0.675F,
      0.8125F,
      0.81875F,
      0.64375F,
      0.39375F,
      0.14375F,
      0.675F,
      0.8125F,
      0.81875F,
      0.64375F,
      0.39375F,
      0.14375F,
      0.675F,
      0.8125F,
      0.81875F,
      0.64375F,
      0.39375F,
      0.14375F,
      0.66875F,
      0.725F,
      0.3875F
   };
   float[] btnPosY = new float[]{
      0.13333334F,
      0.31666666F,
      0.59166664F,
      0.8F,
      0.76666665F,
      0.81666666F,
      0.13333334F,
      0.31666666F,
      0.59166664F,
      0.8F,
      0.76666665F,
      0.81666666F,
      0.13333334F,
      0.31666666F,
      0.59166664F,
      0.8F,
      0.76666665F,
      0.81666666F,
      0.13333334F,
      0.31666666F,
      0.59166664F,
      0.8F,
      0.76666665F,
      0.81666666F,
      0.13333334F,
      0.31666666F,
      0.59166664F,
      0.8F,
      0.76666665F,
      0.81666666F,
      0.2F,
      0.60833335F,
      0.8F
   };
   float btnWidth = 0.1171875F;
   float btnHeight = 0.15625F;
   float btnXRatio = 1.7066667F;
   float btnYRatio = 1.7066667F;
   int[] btnTracks = new int[]{
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
   };
   InitKidsButton[] btnTracksInit = new InitKidsButton[]{
      new InitKidsButton(64),
      new InitKidsButton(65),
      new InitKidsButton(66),
      new InitKidsButton(67),
      new InitKidsButton(68),
      new InitKidsButton(69),
      new InitKidsButton(70),
      new InitKidsButton(71),
      new InitKidsButton(72),
      new InitKidsButton(73),
      new InitKidsButton(74),
      new InitKidsButton(75),
      new InitKidsButton(76),
      new InitKidsButton(77),
      new InitKidsButton(78),
      new InitKidsButton(79),
      new InitKidsButton(80),
      new InitKidsButton(81),
      new InitKidsButton(82),
      new InitKidsButton(83),
      new InitKidsButton(84),
      new InitKidsButton(85),
      new InitKidsButton(86),
      new InitKidsButton(87),
      new InitKidsButton(88),
      new InitKidsButton(89),
      new InitKidsButton(90),
      new InitKidsButton(91),
      new InitKidsButton(92),
      new InitKidsButton(93),
      new InitKidsButton(94),
      new InitKidsButton(95),
      new InitKidsButton(96)
   };

   void ButtonsOnTop() {
   }

   void SetDisabledButtons() {
      for (int var1 = 0; var1 < this.btnTracks.length; var1++) {
         if (var1 >= 0 && var1 < this.btnTracks.length) {
            super.m_List.SetEnabledButton(this.btnTracks[var1], false);
         }
      }
   }

   void SetStateForOthers() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      int var2 = 5;
      int nTracks = var1.GetAvailableTracksCount(); // [mod] déblocage du menu Mods
      if (nTracks / 6 <= this.m_CurrentLand) {
         var2 = (nTracks - 1) % 6;
      }

      this.SetDisabledButtons();
      this.HideButtons();
      this.ShowButtons(this.m_CurrentLand);
      this.SetEnabledButtons(6 * this.m_CurrentLand, 6 * this.m_CurrentLand + var2);
      super.m_List.SetEnabledButton(this.btnTracks[31], false);
      super.m_List.SetEnabledButton(this.btnTracks[32], false);
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
      CoreLogic var2 = RacerGame.GetCoreLogic();
      super.ButtonPushed(var1);
      if (var1 == this.btnExit) {
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(super.m_Module.PopMenu());
      } else {
         for (int var3 = 0; var3 < this.btnTracks.length; var3++) {
            if (var1 == this.btnTracks[var3]) {
               var2.SetNextTrack(var3);
               var2.SetMultiplayer(super.m_Module.GetMenuCX() == 3);
               switch (super.m_Module.GetMenuCX()) {
                  case 0:
                  case 3:
                     super.m_Module.PushMenu(super.m_WindowID);
                     this.DisactivateWindow();
                     if (CoreLogic.m_bPLLocked) {
                        var2.SetLapsNo(3);
                        super.m_Module.ClearStack();
                        ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, true);
                        super.m_Module.m_CurrentMenu = -1;
                        RacerGame.game.RunModule();
                        return;
                     }

                     super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._LapsMenuWindow);
                     break;
                  case 1:
                     super.m_Module.ClearStack();
                     this.DisactivateWindow();
                     int var4 = var3 % 6;
                     if (var4 != 0 && var4 != 5) {
                        super.m_Module.m_CurrentMenu = -1;
                        RacerGame.game.RunModule();
                     } else if (var4 == 0) {
                        KidsBossTauntMenuWindow var5 = (KidsBossTauntMenuWindow)super.m_Module.GetWindowAt(KidsSimpleMenuModule._BossTauntMenuWindow);
                        var5.m_Boss = this.m_CurrentLand + 6;
                        var5.m_TauntType = 0;
                        super.m_Module.PushMenu(super.m_WindowID);
                        this.DisactivateWindow();
                        super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._BossTauntMenuWindow);
                     } else {
                        KidsBossTauntMenuWindow var6 = (KidsBossTauntMenuWindow)super.m_Module.GetWindowAt(KidsSimpleMenuModule._BossTauntMenuWindow);
                        var6.m_Boss = this.m_CurrentLand + 6;
                        var6.m_TauntType = 1;
                        super.m_Module.PushMenu(super.m_WindowID);
                        this.DisactivateWindow();
                        super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._BossTauntMenuWindow);
                     }
                  case 2:
               }
            }
         }
      }
   }

   void SetStateForAdventure() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      int var2 = var1.m_nPassedTracks;
      this.SetDisabledButtons();
      this.HideButtons();
      this.ShowButtons(this.m_CurrentLand);
      this.SetEnabledButtons(var2, var2);
   }

   void HideButtons() {
      for (int var1 = 0; var1 < this.btnTracks.length; var1++) {
         super.m_List.Hide(this.btnTracks[var1]);
         super.m_List.ProcessKb(this.btnTracks[var1], false);
      }
   }

   public void Hide() {
      super.Hide();
      this.ProcessKb(false);
   }

   public void Show() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      super.m_Module.SetIgnoreKeys(2);
      super.Show();
      this.ProcessKb(true);
      ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, false);
      KidsMenuWindow var2 = (KidsMenuWindow)super.m_List.WindowAt(this.wndBoss);
      if (var2 != null) {
         var2.SetWindowTexture(this.m_CurrentLand);
      }

      if (super.m_Module.GetMenuCX() == 1) {
         this.SetStateForAdventure();
      } else {
         this.SetStateForOthers();
      }

      if (var1 != null) {
         if (super.m_Module.GetMenuCX() == 1) {
            var1.PrepareForAdventure();
         } else {
            var1.SetNextTrack(0);
         }

         var1.SetLapsNo(3);
      }

      if (!KidsMenuModule.m_CarsManager.IsInitialized()) {
         KidsMenuModule.m_CarsManager.ActivateCarsManager();
      }

      KidsMenuModule.m_CarsManager.Show(this);
      this.ButtonsOnTop();
   }

   void ShowButtons(int var1) {
      switch (var1) {
         case 0:
            int var7 = 0;

            do {
               super.m_List.Show(this.btnTracks[var1 * 6 + var7]);
               super.m_List.ProcessKb(this.btnTracks[var1 * 6 + var7], true);
            } while (++var7 < 6);

            return;
         case 1:
            int var6 = 0;

            do {
               super.m_List.Show(this.btnTracks[var1 * 6 + var6]);
               super.m_List.ProcessKb(this.btnTracks[var1 * 6 + var6], true);
            } while (++var6 < 6);

            return;
         case 2:
            int var5 = 0;

            do {
               super.m_List.Show(this.btnTracks[var1 * 6 + var5]);
               super.m_List.ProcessKb(this.btnTracks[var1 * 6 + var5], true);
            } while (++var5 < 6);

            return;
         case 3:
            int var4 = 0;

            do {
               super.m_List.Show(this.btnTracks[var1 * 6 + var4]);
               super.m_List.ProcessKb(this.btnTracks[var1 * 6 + var4], true);
            } while (++var4 < 6);

            return;
         case 4:
            int var3 = 0;

            do {
               super.m_List.Show(this.btnTracks[var1 * 6 + var3]);
               super.m_List.ProcessKb(this.btnTracks[var1 * 6 + var3], true);
            } while (++var3 < 6);

            return;
         case 5:
            int var2 = 0;

            do {
               super.m_List.Show(this.btnTracks[var1 * 6 + var2]);
               super.m_List.ProcessKb(this.btnTracks[var1 * 6 + var2], true);
            } while (++var2 < 3);

            return;
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

      for (int var3 = 0; var3 < this.btnTracks.length; var3++) {
         var4 = (KidsButton)this.CreateObject("KidsButton");
         this.btnTracks[var3] = this.Add(var4);
         this.btnTracksInit[var3].bMoveButton = true;
         this.btnTracksInit[var3].xpos = this.btnPosX[var3];
         this.btnTracksInit[var3].ypos = this.btnPosY[var3];
         this.btnTracksInit[var3].width = this.btnWidth;
         this.btnTracksInit[var3].height = this.btnHeight;
         this.btnTracksInit[var3].ButtonWidth = this.btnXRatio;
         this.btnTracksInit[var3].ButtonHeight = this.btnYRatio;
         var4.Init(super.m_Module, this, this.btnTracks[var3], this.btnTracksInit[var3]);
         var4.ProcessMouse(true);
      }

      KidsMenuWindow var5 = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      this.wndBoss = this.Add(var5);
      var5.Init(super.m_Module, this.wndBoss);

      for (int var6 = 0; var6 < BossesTextures.length; var6++) {
         var5.AddWindowTexture(var5.AddTexture(BossesTextures[var6], true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), BossesTextures[var6]);
      }

      var5.SetSizeAndPos(wndBossWidth, wndBossHeight, wndBossPosX, wndBossPosY, true);
      var5.SetWindowTexture(0);
      var5.Show();
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.ProcessKb(true);
      super.m_Ctrl = new ControlWindowList(40);

      for (int var7 = 0; var7 < this.btnTracks.length; var7++) {
         super.m_Ctrl.Push(super.m_List.WindowAt(this.btnTracks[var7]));
      }

      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExit));
      this.SetAlphaForAll(1.0F);
   }

   void SetEnabledButtons(int var1, int var2) {
      CoreLogic var3 = RacerGame.GetCoreLogic();

      for (int var4 = var1; var4 <= var2; var4++) {
         if (var4 >= 0 && var4 < this.btnTracks.length && !var3.IsTrackLocked(var4)) {
            super.m_List.SetEnabledButton(this.btnTracks[var4], true);
         }
      }
   }

   void RefreshControls() {
   }
}
