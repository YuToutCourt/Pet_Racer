public class KidsMainMenuWindow extends KidsMenuWindow {
   static float _DEMO_START_TIME = 90.0F;
   int bkTex0 = -1;
   String m_TexName = "Data/Textures/Menu/b/MenuMain.png";
   String m_TexLogoPetRacer = "Data/Textures/Menu/a/LogoPetRacer.png";
   float m_DemoTime = _DEMO_START_TIME;
   int btnSinglePlayer = -1;
   InitKidsButton btnSinglePlayerInit = new InitKidsButton(0);
   int btnSettings = -1;
   InitKidsButton btnSettingsInit = new InitKidsButton(1);
   int btnMultiPlayer = -1;
   InitKidsButton btnMultiPlayerInit = new InitKidsButton(2);
   int btnRecords = -1;
   InitKidsButton btnRecordsInit = new InitKidsButton(4);
   int btnCredits = -1;
   InitKidsButton btnCreditsInit = new InitKidsButton(5);
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(6);
   int btnMods = -1; // [mod] bouton engrenage « MODS » (KidsModsMenuWindow)
   InitKidsButton btnModsInit = ModsButtonInit();
   int btnOnline = -1; // [mod] bouton « EN LIGNE » (KidsOnlineMenuWindow, chantier 8)
   InitKidsButton btnOnlineInit = OnlineButtonInit();
   boolean m_bPerformTests = false;
   float m_fLastUpdateTime = 0.0F;
   static final int _MODAL_EXIT = 0;

   void ButtonsOnTop() {
      super.m_List.SetOnTop(this.btnSinglePlayer);
      super.m_List.SetOnTop(this.btnMultiPlayer);
      super.m_List.SetOnTop(this.btnSettings);
      super.m_List.SetOnTop(this.btnRecords);
      super.m_List.SetOnTop(this.btnCredits);
      super.m_List.SetOnTop(this.btnExit);
      super.m_List.SetOnTop(this.btnMods); // [mod]
      super.m_List.SetOnTop(this.btnOnline); // [mod]
   }

   // [mod] bouton « EN LIGNE » : à gauche de « MODS », images de mods/art/make_online.py
   static InitKidsButton OnlineButtonInit() {
      InitKidsButton init = ModsButtonInit();
      init.sBnTexDisabled = "Data/Textures/Menu/a/OnlineButton.png";
      init.sBnTexEnabled = "Data/Textures/Menu/a/OnlineButton.png";
      init.sBnTexHighlighted = "Data/Textures/Menu/a/OnlineButton.png";
      init.sBnTexPushed = "Data/Textures/Menu/a/OnlineButtonPods.png";
      init.xpos = 0.1F;
      return init;
   }

   // [mod] bouton « MODS » : mêmes réglages que les boutons d'origine, images de mods/art/make_mods_menu.py
   static InitKidsButton ModsButtonInit() {
      InitKidsButton init = new InitKidsButton();
      init.sBnTexDisabled = "Data/Textures/Menu/a/ModsButton.png";
      init.sBnTexEnabled = "Data/Textures/Menu/a/ModsButton.png";
      init.sBnTexHighlighted = "Data/Textures/Menu/a/ModsButton.png";
      init.sBnTexPushed = "Data/Textures/Menu/a/ModsButtonPods.png";
      init.bMoveButton = true;
      init.xpos = 0.3F;
      init.ypos = 0.85F;
      init.width = 0.1F;
      init.height = 0.13333334F;
      init.ButtonWidth = 2.0F;
      init.ButtonHeight = 2.0F;
      return init;
   }

   public void OnKeyReleased(int var1, char var2) {
      if (this.CanProcessMouseMove() && !super.m_Module.CanIgnoreKeys()) {
         this.DisableCallMethod("RunDemo");
         this.CallMethodIn("RunDemo", _DEMO_START_TIME);
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
      this.DisableCallMethod("RunDemo");
      super.ButtonPushed(var1);
      if (var1 == this.btnExit) {
         this.DisableCallMethod("RunDemo");
         ((KidsSimpleMenuModule)super.m_Module).CallYesNoDialog(0, this);
      } else if (var1 == this.btnSettings) {
         super.m_Module.PushMenu(super.m_WindowID);
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._SettingsMenuWindow);
      } else if (var1 == this.btnSinglePlayer) {
         super.m_Module.PushMenu(super.m_WindowID);
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._SinglePlayerMenuWindow);
      } else if (var1 == this.btnMultiPlayer) {
         super.m_Module.PushMenu(super.m_WindowID);
         super.m_Module.SetMenuCX(3);
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._AnimalChoiceMenuWindow);
      } else if (var1 == this.btnRecords) {
         super.m_Module.PushMenu(super.m_WindowID);
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._RecordsMenuWindow);
      } else if (var1 == this.btnCredits) {
         super.m_Module.PushMenu(super.m_WindowID);
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._CreditsMenuWindow);
      } else if (var1 == this.btnMods) {
         // [mod] écran « Mods »
         super.m_Module.PushMenu(super.m_WindowID);
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._ModsMenuWindow);
      } else if (var1 == this.btnOnline) {
         // [mod] écran « En ligne »
         super.m_Module.PushMenu(super.m_WindowID);
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._OnlineMenuWindow);
      }
   }

   public void ModalMessage(int var1, int var2, int var3) {
      this.DisableCallMethod("RunDemo");
      switch (var1) {
         case 0:
            if (var3 != 1) {
               this.DisableCallMethod("RunDemo");
               this.CallMethodIn("RunDemo", _DEMO_START_TIME);
               return;
            } else {
               RacerGame.GetGSS().Save();
               GameObject[] var4 = GameObject.GetObjectsFromClass("MeshObject");

               for (int var5 = 0; var5 < var4.length; var5++) {
                  var4[var5].delete();
                  var4[var5] = null;
               }

               if (CoreLogic.m_bIsLocked) {
                  super.m_Module.ClearStack();
                  this.DisactivateWindow();
                  super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._DemoScreenShotsWindow);
                  return;
               } else {
                  GameObject.ExitGame();
                  return;
               }
            }
      }
   }

   public void Hide() {
      super.Hide();
      this.ProcessKb(false);
   }

   public void RunDemo() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      if (!CoreLogic.m_bIsLocked) {
         this.m_DemoTime = _DEMO_START_TIME;
         RacerModule.SetDemoModule(true);
         var1.m_bMultiplayer = false;
         RacerGame.game.RunModule();
      }
   }

   public void Show() {
      super.m_Module.SetIgnoreKeys(2);
      super.Show();
      this.ProcessKb(true);
      super.m_Module.ClearStack();
      super.m_Module.SetMenuCX(-1);
      ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, false);
      this.DisableCallMethod("RunDemo");
      CoreLogic var1 = RacerGame.GetCoreLogic();
      if (this.m_DemoTime > 0.0F && !CoreLogic.m_bIsLocked) {
         this.CallMethodIn("RunDemo", this.m_DemoTime);
      }

      if (!KidsMenuModule.m_CarsManager.IsInitialized()) {
         KidsMenuModule.m_CarsManager.ActivateCarsManager();
      }

      KidsMenuModule.m_CarsManager.Show(this);
      this.ButtonsOnTop();
      RacerGame.GetCoreLogic().SetLocked();
   }

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      CoreLogic var3 = RacerGame.GetCoreLogic();
      boolean var4 = !CoreLogic.m_bIsLocked;
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(this.m_TexName, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), this.m_TexName);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      KidsButton var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnSinglePlayer = this.Add(var5);
      this.btnSinglePlayerInit.bMoveButton = true;
      this.btnSinglePlayerInit.xpos = 0.55F;
      this.btnSinglePlayerInit.ypos = 0.85F;
      this.btnSinglePlayerInit.width = 0.1F;
      this.btnSinglePlayerInit.height = 0.13333334F;
      this.btnSinglePlayerInit.ButtonWidth = 2.0F;
      this.btnSinglePlayerInit.ButtonHeight = 2.0F;
      var5.Init(super.m_Module, this, this.btnSinglePlayer, this.btnSinglePlayerInit);
      var5.ProcessMouse(true);
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnSettings = this.Add(var5);
      this.btnSettingsInit.bMoveButton = true;
      this.btnSettingsInit.xpos = 0.625F;
      this.btnSettingsInit.ypos = 0.53333336F;
      this.btnSettingsInit.width = 0.1F;
      this.btnSettingsInit.height = 0.13333334F;
      this.btnSettingsInit.ButtonWidth = 2.0F;
      this.btnSettingsInit.ButtonHeight = 2.0F;
      var5.Init(super.m_Module, this, this.btnSettings, this.btnSettingsInit);
      var5.ProcessMouse(true);
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnMultiPlayer = this.Add(var5);
      this.btnMultiPlayerInit.bMoveButton = true;
      this.btnMultiPlayerInit.xpos = 0.48125F;
      this.btnMultiPlayerInit.ypos = 0.65833336F;
      this.btnMultiPlayerInit.width = 0.1F;
      this.btnMultiPlayerInit.height = 0.13333334F;
      this.btnMultiPlayerInit.ButtonWidth = 2.0F;
      this.btnMultiPlayerInit.ButtonHeight = 2.0F;
      var5.Init(super.m_Module, this, this.btnMultiPlayer, this.btnMultiPlayerInit);
      var5.ProcessMouse(true);
      var5.Enable(var4);
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnRecords = this.Add(var5);
      this.btnRecordsInit.bMoveButton = true;
      this.btnRecordsInit.xpos = 0.85F;
      this.btnRecordsInit.ypos = 0.6666667F;
      this.btnRecordsInit.width = 0.1F;
      this.btnRecordsInit.height = 0.13333334F;
      this.btnRecordsInit.ButtonWidth = 2.0F;
      this.btnRecordsInit.ButtonHeight = 2.0F;
      var5.Init(super.m_Module, this, this.btnRecords, this.btnRecordsInit);
      var5.ProcessMouse(true);
      var5.Enable(var4);
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnCredits = this.Add(var5);
      this.btnCreditsInit.bMoveButton = true;
      this.btnCreditsInit.xpos = 0.75F;
      this.btnCreditsInit.ypos = 0.5F;
      this.btnCreditsInit.width = 0.1375F;
      this.btnCreditsInit.height = 0.15F;
      this.btnCreditsInit.ButtonWidth = 1.4545455F;
      this.btnCreditsInit.ButtonHeight = 1.7777778F;
      var5.Init(super.m_Module, this, this.btnCredits, this.btnCreditsInit);
      var5.ProcessMouse(true);
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExit = this.Add(var5);
      this.btnExitInit.bMoveButton = true;
      this.btnExitInit.xpos = 0.775F;
      this.btnExitInit.ypos = 0.8666667F;
      this.btnExitInit.width = 0.1F;
      this.btnExitInit.height = 0.13333334F;
      this.btnExitInit.ButtonWidth = 2.0F;
      this.btnExitInit.ButtonHeight = 2.0F;
      var5.Init(super.m_Module, this, this.btnExit, this.btnExitInit);
      var5.ProcessMouse(true);
      // [mod] bouton « MODS », en bas à gauche du logo (place libre du fond)
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnMods = this.Add(var5);
      var5.Init(super.m_Module, this, this.btnMods, this.btnModsInit);
      var5.ProcessMouse(true);
      // [mod] bouton « EN LIGNE », à gauche de « MODS »
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnOnline = this.Add(var5);
      var5.Init(super.m_Module, this, this.btnOnline, this.btnOnlineInit);
      var5.ProcessMouse(true);
      KidsMenuWindow var6 = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      int var7 = this.Add(var6);
      var6.Init(super.m_Module, var7);
      var6.AddWindowTexture(var6.AddTexture(this.m_TexLogoPetRacer, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.m_TexLogoPetRacer);
      var6.SetSizeAndPos(0.4F, 0.53333336F, 0.2625F, 0.31666666F, true);
      var6.SetWindowTexture(0);
      var6.Show();
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.ProcessKb(true);
      super.m_Ctrl = new ControlWindowList(10);
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnOnline)); // [mod] tout à gauche
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnMods)); // [mod] à gauche d'« un joueur »
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnSinglePlayer));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnMultiPlayer));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnSettings));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnCredits));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnRecords));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExit));
      this.SetAlphaForAll(1.0F);
      if (RacerGame.m_bRecordAvi) {
         GameObject.LogLine("Recording AVI active...");
         _DEMO_START_TIME = 1.0F;
         this.m_DemoTime = _DEMO_START_TIME;
      }
   }

   void OnMouseMove(float var1, float var2, int var3) {
      this.DisableCallMethod("RunDemo");
      this.CallMethodIn("RunDemo", _DEMO_START_TIME);
   }
}
