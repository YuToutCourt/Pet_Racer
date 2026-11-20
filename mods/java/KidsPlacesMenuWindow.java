public class KidsPlacesMenuWindow extends KidsMenuWindow {
   static final int _BONUS = 0;
   static final int _NOTFIRST = 1;
   int bkTex0 = -1;
   boolean m_bShowEXTRAWindow = false;
   int m_LastBonus = -1;
   String m_TexName = "Data/Textures/Menu/b/MenuCharakter.png";
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(45);
   Vector[] m_AnimalsPos = new Vector[]{new Vector(2130.0F, 96.0F, 1931.0F), new Vector(2066.0F, 81.0F, 1868.0F), new Vector(2194.0F, 66.0F, 1996.0F)};
   Vector m_CameraPos = new Vector(2426.0F, 350.0F, 1646.0F);
   MenuAnimal[] m_Animals = null;
   int wndExtraText = -1;
   float ExtraTextPosX = 0.7625F;
   float ExtraTextPosY = 0.16666667F;
   float ExtraTextWidth = 0.4F;
   float ExtraTextHeight = 0.26666668F;
   String ExtraTextBonusTex = "Data/Textures/Menu/a/WygralesDodatkowy.png";
   String ExtraTextNoFirstTex = "Data/Textures/Menu/a/NiestetyNieZdobyles.png";
   boolean m_bFirst = false;
   int ktxCheat = -1; // [mod] « Triche : record non compté » (menu Mods)

   void ButtonsOnTop() {
      super.m_List.SetOnTop(this.ktxCheat); // [mod] texte au-dessus des images (cf. menu Mods)
      super.m_List.SetOnTop(this.btnExit);
   }

   public void HideAnimals() {
      if (this.m_Animals != null) {
         for (int var1 = 0; var1 < this.m_Animals.length; var1++) {
            if (this.m_Animals[var1] != null) {
               this.m_Animals[var1].ResetAnim();
               this.m_Animals[var1].EnableRendering(false);
               this.m_Animals[var1] = null;
            }
         }

         this.m_Animals = null;
      }
   }

   void SetDirToCamera(MenuAnimal var1) {
      MenuCamera var2 = super.m_Module.m_Camera;
      Vector var3 = var2.GetForwardVector();
      var3.fY = 0.0F;
      var1.SetDir(var3);
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
         this.EndWindow();
      }
   }

   public void BeforeEndWindow() {
      if (this.m_bFirst) {
         ((KidsSimpleMenuModule)super.m_Module).PlayMusic(10, 1.0F);
      } else {
         ((KidsSimpleMenuModule)super.m_Module).PlayMusic(11, 1.0F);
      }

      for (int var1 = 0; var1 < this.m_Animals.length; var1++) {
         if (this.m_Animals[var1] != null) {
            switch (var1) {
               case 0:
                  this.m_Animals[var1].PlayAnim("", "joy", -1);
                  this.m_Animals[var1].PlaySoundJoy();
                  break;
               case 1:
                  this.m_Animals[var1].PlayAnim("", "whistle", -1);
                  break;
               case 2:
                  this.m_Animals[var1].PlayAnim("", "stomp", -1);
            }
         }
      }
   }

   public void Hide() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      if (var1.m_TimeList != null) {
         var1.m_TimeList.Clear();
      }

      this.HideAnimals();
      super.Hide();
      this.ProcessKb(false);
      var1.m_bBonusAdded = false;
      var1.m_bFirstPosition = false;
      var1.m_bFailedLand = false;
      ((KidsSimpleMenuModule)super.m_Module).PlayMusic(7, 1.0F);
      super.m_Module.DeactivateMenuCamera();
   }

   public void Show() {
      super.m_Module.SetIgnoreKeys(2);
      this.HideAnimals();
      super.Show();
      this.ProcessKb(true);
      ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, true);
      CoreLogic var1 = RacerGame.GetCoreLogic();
      this.m_Animals = new MenuAnimal[3];
      TimeList var2 = var1.GetTimeList();
      Vector var5 = new Vector(this.m_AnimalsPos[0]);
      var5.Sub(this.m_CameraPos);
      var5.Normalize();
      var5.Negate();
      super.m_Module.m_Camera.InitObject();
      super.m_Module.m_Camera.FromForwardUpPos(var5, new Vector(0.0F, 1.0F, 0.0F), this.m_CameraPos);
      super.m_Module.ActivateMenuCamera();
      int var6 = 3;
      if (var2.GetCnt() < 3) {
         var6 = var2.GetCnt();
      }

      this.m_Animals = new MenuAnimal[3];

      for (int var7 = 0; var7 < var6; var7++) {
         TimeEntry var3 = var2.GetAt(var7);
         MenuAnimal var4 = ((KidsSimpleMenuModule)super.m_Module).GetAnimal(var3.m_AnimalID);
         var4.SetPosition(this.m_AnimalsPos[var7]);
         this.SetDirToCamera(var4);
         var4.EnableRendering(true);
         var4.PlayConstantIdle();
         this.m_Animals[var7] = var4;
      }

      this.SetExtraPicture(-1);
      if (super.m_Module.GetMenuCX() == 1) {
         this.m_bShowEXTRAWindow = false;
         if (var1.m_bBonusAdded) {
            this.m_bShowEXTRAWindow = true;
            this.m_LastBonus = var1.m_LastBonus;
         } else if (!var1.m_bFirstPosition) {
            this.SetExtraPicture(1);
         }
      }

      this.m_bFirst = false;
      if (var2.GetAt(0).m_AnimalID == var1.m_SelectedAnimals.GetAt(0)) {
         this.m_bFirst = true;
      }

      if (super.m_Module.GetMenuCX() == 3 && var2.GetAt(0).m_AnimalID == var1.m_SelectedAnimals.GetAt(1)) {
         this.m_bFirst = true;
      }

      ((KidsSimpleMenuModule)super.m_Module).StopMusicLoop();
      this.DisableCallMethod("BeforeEndWindow");
      this.CallMethodIn("BeforeEndWindow", 1.0F);
      // [mod] avertissement de triche (KidsMenuWindow.Show vient de réafficher tous les enfants)
      if (this.ktxCheat >= 0) {
         if (ModOptions.IsCheat()) {
            ModText t = (ModText)super.m_List.WindowAt(this.ktxCheat);
            t.SetColor(1.0F, 0.6F, 0.2F);
            t.SetText("&ModsCheatPodium&");
            super.m_List.SetOnTop(this.ktxCheat);
         } else {
            super.m_List.Hide(this.ktxCheat);
         }
      }
   }

   public void EndWindow() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      this.DisableCallMethod("BeforeEndWindow");
      int var2 = super.m_Module.GetMenuCX();
      TimeEntry var3 = RacerGame.GetCoreLogic().GetNewRecordPlayer();
      // [mod] une triche du menu Mods (vitesse, bonus, protection) rend la course non comptée : pas de record
      if (ModOptions.IsCheat()) {
         if ((var2 == 0 || var2 == 1) && var3 != null) {
            GameObject.CrashLog("[mod] triche active : record non enregistre\n");
         }

         var3 = null;
      }

      if ((var2 == 0 || var2 == 1) && var3 != null) {
         TimeEntry var4 = var1.GetNewRecordPlayer();
         if (var4 != null) {
            String var5 = new String(var1.m_Name);
            RecordEntry var6 = new RecordEntry(var5, var4.m_Time);
            var1.SetNewRecord(var6);
         }
      }

      GameSerializeSettings var7 = RacerGame.GetGSS();
      var7.Save();
      if (var2 == 1) {
         super.m_Module.ClearStack();
         if (var1.GetEndAdventure()) {
            var1.SetEndAdventure(false);
            if (var1.CanPlayOutro()) {
               this.DisactivateWindow();
               super.m_Module.SetMenuCX(-1);
               OutroModule.m_Winner = var1.m_aCurrentAnimals[0];
               RacerGame.game.LoadOutroModule();
               RacerGame.game.activeMenu = 4;
               RacerGame.outroModule.Activate();
            } else {
               this.DisactivateWindow();
               super.m_Module.SetMenuCX(-1);
               super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._MainMenuWindow);
            }
         } else if (!var1.m_bFirstPosition && var1.m_bFailedLand) {
            this.DisactivateWindow();
            super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._FailureMenuWindow);
         } else if (var1.m_bFirstPosition && var1.m_nPassedTracks % 6 == 0) {
            KidsProgressMenuWindow var9 = (KidsProgressMenuWindow)super.m_Module.GetWindowAt(KidsSimpleMenuModule._ProgressMenuWindow);
            var9.m_FinishedLand = var1.m_nPassedTracks / 6 - 1;
            this.DisactivateWindow();
            if (this.m_bShowEXTRAWindow) {
               this.m_bShowEXTRAWindow = false;
               this.ShowEXTRAWindow(KidsSimpleMenuModule._ProgressMenuWindow, this.m_LastBonus);
            } else {
               super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._ProgressMenuWindow);
            }
         } else {
            this.DisactivateWindow();
            super.m_Module.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
            super.m_Module.PushMenu(KidsSimpleMenuModule._SinglePlayerMenuWindow);
            super.m_Module.PushMenu(KidsSimpleMenuModule._MapLandsMenuWindow);
            KidsMapTracksMenuWindow var8 = (KidsMapTracksMenuWindow)super.m_Module.GetWindowAt(KidsSimpleMenuModule._MapTracksMenuWindow);
            var8.m_CurrentLand = var1.m_nPassedTracks / 6;
            if (this.m_bShowEXTRAWindow) {
               this.m_bShowEXTRAWindow = false;
               this.ShowEXTRAWindow(KidsSimpleMenuModule._MapTracksMenuWindow, this.m_LastBonus);
            } else {
               super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._MapTracksMenuWindow);
            }
         }
      } else if (var2 == 0 && ModNet.BackToOnlineScreen()) {
         // [mod] en ligne : retour au salon pour la course suivante
         this.DisactivateWindow();
         super.m_Module.ClearStack();
         super.m_Module.SetMenuCX(-1);
         super.m_Module.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._OnlineMenuWindow);
      } else if (var2 == 0 && ModGrandPrix.IsActive()) {
         // [mod] Grand Prix : classement de la coupe (points déjà comptés par RacerGame.EndModule)
         this.DisactivateWindow();
         super.m_Module.ClearStack();
         super.m_Module.SetMenuCX(0);
         super.m_Module.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
         super.m_Module.PushMenu(KidsSimpleMenuModule._SinglePlayerMenuWindow);
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._GrandPrixMenuWindow);
      } else if (var2 == 0) {
         this.DisactivateWindow();
         super.m_Module.ClearStack();
         super.m_Module.SetMenuCX(0);
         super.m_Module.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
         super.m_Module.PushMenu(KidsSimpleMenuModule._SinglePlayerMenuWindow);
         super.m_Module.PushMenu(KidsSimpleMenuModule._AnimalChoiceMenuWindow);
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._MapLandsMenuWindow);
      } else if (var2 != 4
         && var2 != 5
         && var2 != 6
         && var2 != 7
         && var2 != 8
         && var2 != 9
         && var2 != 10
         && var2 != 11
         && var2 != 12
         && var2 != 13
         && var2 != 14
         && var2 != 15
         && var2 != 16
         && var2 != 17
         && var2 != 18) {
         this.DisactivateWindow();
         super.m_Module.ClearStack();
         super.m_Module.SetMenuCX(-1);
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._MainMenuWindow);
      } else {
         this.DisactivateWindow();
         super.m_Module.ClearStack();
         super.m_Module.SetMenuCX(-1);
         super.m_Module.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
         super.m_Module.PushMenu(KidsSimpleMenuModule._SinglePlayerMenuWindow);
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._ExtraMenuWindow);
      }
   }

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(this.m_TexName, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), this.m_TexName);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      ModSideFill.AddTo(this, var1); // [mod] écran large : damier prolongé sur les côtés au lieu du décor 3D
      KidsButton var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExit = this.Add(var3);
      this.btnExitInit.bMoveButton = true;
      this.btnExitInit.xpos = 0.5F;
      this.btnExitInit.ypos = 0.86667F;
      this.btnExitInit.width = 0.1484F;
      this.btnExitInit.height = 0.13334F;
      this.btnExitInit.ButtonWidth = 1.3473F;
      this.btnExitInit.ButtonHeight = 2.0F;
      var3.Init(super.m_Module, this, this.btnExit, this.btnExitInit);
      var3.ProcessMouse(true);
      KidsMenuWindow var4 = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      this.wndExtraText = this.Add(var4);
      var4.Init(super.m_Module, this.wndExtraText);
      var4.AddWindowTexture(var4.AddTexture(this.ExtraTextBonusTex, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.ExtraTextBonusTex);
      var4.AddWindowTexture(var4.AddTexture(this.ExtraTextNoFirstTex, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.ExtraTextNoFirstTex);
      var4.SetSizeAndPos(this.ExtraTextWidth, this.ExtraTextHeight, this.ExtraTextPosX, this.ExtraTextPosY, true);
      var4.SetWindowTexture(0);
      var4.Hide();
      // [mod] « Triche : record non compté », en bas à gauche sur le damier (rempli dans Show)
      ModText ktx = (ModText)this.CreateObject("ModText");
      this.ktxCheat = this.Add(ktx);
      ktx.Init(super.m_Module, this.ktxCheat);
      ktx.SetAlignLeft();
      ktx.SetSizeAndPos(0.4F, 0.06F, 0.03F, 0.91F, false);
      ktx.Hide();
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.ProcessKb(true);
      super.m_Ctrl = new ControlWindowList(5);
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExit));
      this.SetAlphaForAll(1.0F);
   }

   void ShowEXTRAWindow(int var1, int var2) {
      KidsShowExtraMenuWindow var3 = (KidsShowExtraMenuWindow)super.m_Module.GetWindowAt(KidsSimpleMenuModule._ShowEXTRAMenuWindow);
      var3.m_NextWindowToShow = var1;
      var3.m_ExtraPictureToShow = var2;
      super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._ShowEXTRAMenuWindow);
   }

   void SetExtraPicture(int var1) {
      if (var1 >= 0 && this.wndExtraText > -1) {
         super.m_List.Show(this.wndExtraText);
         super.m_List.SetTexture(this.wndExtraText, var1);
      } else {
         super.m_List.Hide(this.wndExtraText);
      }
   }
}
