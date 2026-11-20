public class KidsSinglePlayerMenuWindow extends KidsMenuWindow {
   int bkTex0 = -1;
   String m_TexName = "Data/Textures/Menu/b/PlanszaNiebieska.png";
   String m_TexLogo = "Data/Textures/Menu/a/LogoPetRacer.png";
   int btnAdventure = -1;
   InitKidsButton btnAdventureInit = new InitKidsButton(13);
   int btnRace = -1;
   InitKidsButton btnRaceInit = new InitKidsButton(14);
   int btnExtra = -1;
   InitKidsButton btnExtraInit = new InitKidsButton(15);
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(56);
   int btnGrandPrix = -1; // [mod] bouton « GRAND PRIX » (chantier 6, ModGrandPrix)
   InitKidsButton btnGrandPrixInit = GrandPrixButtonInit();

   // [mod] bouton « GRAND PRIX » : au-dessus de « Course », dans l'escalier des boutons d'origine ; images de
   // mods/art/make_grandprix.py (256x128 comme « Aventure »)
   static InitKidsButton GrandPrixButtonInit() {
      InitKidsButton init = new InitKidsButton();
      init.sBnTexDisabled = "Data/Textures/Menu/a/GrandPrixButton.png";
      init.sBnTexEnabled = "Data/Textures/Menu/a/GrandPrixButton.png";
      init.sBnTexHighlighted = "Data/Textures/Menu/a/GrandPrixButton.png";
      init.sBnTexPushed = "Data/Textures/Menu/a/GrandPrixButtonPods.png";
      init.bMoveButton = true;
      init.xpos = 0.79F;
      init.ypos = 0.36F;
      init.width = 0.16F;
      init.height = 0.0667F;
      init.ButtonWidth = 2.5F;
      init.ButtonHeight = 4.0F;
      return init;
   }

   void ButtonsOnTop() {
      super.m_List.SetOnTop(this.btnAdventure);
      super.m_List.SetOnTop(this.btnRace);
      super.m_List.SetOnTop(this.btnExtra);
      super.m_List.SetOnTop(this.btnExit);
      super.m_List.SetOnTop(this.btnGrandPrix); // [mod]
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
      } else if (var1 == this.btnAdventure) {
         super.m_Module.PushMenu(super.m_WindowID);
         super.m_Module.SetMenuCX(1);
         this.DisactivateWindow();
         CoreLogic var2 = RacerGame.GetCoreLogic();
         if (var2.IsAdventureInProgress()) {
            super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._NewContinueMenuWindow);
         } else {
            super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._AnimalChoiceMenuWindow);
         }
      } else if (var1 == this.btnGrandPrix) {
         // [mod] Grand Prix : contexte « Course », choix de l'animal (adversaires tirés une fois), puis tours ;
         // la coupe s'ouvre au lancement de la première course (ModGrandPrix.BeforeRace)
         super.m_Module.PushMenu(super.m_WindowID);
         super.m_Module.SetMenuCX(0);
         ModGrandPrix.m_bPending = true;
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._AnimalChoiceMenuWindow);
      } else if (var1 == this.btnRace) {
         super.m_Module.PushMenu(super.m_WindowID);
         super.m_Module.SetMenuCX(0);
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._AnimalChoiceMenuWindow);
      } else if (var1 == this.btnExtra) {
         super.m_Module.PushMenu(super.m_WindowID);
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._ExtraMenuWindow);
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
      super.m_Module.SetMenuCX(-1);
      ModGrandPrix.Reset(); // [mod] revenir ici ferme la coupe (ou le Grand Prix demandé puis annulé)
      if (!KidsMenuModule.m_CarsManager.IsInitialized()) {
         KidsMenuModule.m_CarsManager.ActivateCarsManager();
      }

      KidsMenuModule.m_CarsManager.Show(this);
      this.ButtonsOnTop();
   }

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      CoreLogic var3 = RacerGame.GetCoreLogic();
      boolean var4 = !CoreLogic.m_bIsLocked;
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(this.m_TexName, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), this.m_TexName);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      KidsButton var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnAdventure = this.Add(var5);
      this.btnAdventureInit.bMoveButton = true;
      this.btnAdventureInit.xpos = 0.7F;
      this.btnAdventureInit.ypos = 0.5333F;
      this.btnAdventureInit.width = 0.1F;
      this.btnAdventureInit.height = 0.0667F;
      this.btnAdventureInit.ButtonWidth = 4.0F;
      this.btnAdventureInit.ButtonHeight = 4.0F;
      var5.Init(super.m_Module, this, this.btnAdventure, this.btnAdventureInit);
      var5.ProcessMouse(true);
      var5.Enable(var4);
      KidsMenuWindow var6 = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      int var7 = this.Add(var6);
      var6.Init(super.m_Module, var7);
      var6.AddWindowTexture(var6.AddTexture(this.m_TexLogo, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.m_TexLogo);
      var6.SetSizeAndPos(0.4F, 0.53333336F, 0.2625F, 0.31666666F, true);
      var6.SetWindowTexture(0);
      var6.Show();
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnRace = this.Add(var5);
      this.btnRaceInit.xpos = 0.75F;
      this.btnRaceInit.ypos = 0.45F;
      this.btnRaceInit.width = 0.125F;
      this.btnRaceInit.height = 0.0667F;
      this.btnRaceInit.ButtonWidth = 1.6F;
      this.btnRaceInit.ButtonHeight = 4.0F;
      this.btnRaceInit.bMoveButton = true;
      var5.Init(super.m_Module, this, this.btnRace, this.btnRaceInit);
      var5.ProcessMouse(true);
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExtra = this.Add(var5);
      this.btnExtraInit.bMoveButton = true;
      this.btnExtraInit.xpos = 0.625F;
      this.btnExtraInit.ypos = 0.6333F;
      this.btnExtraInit.width = 0.1F;
      this.btnExtraInit.height = 0.1F;
      this.btnExtraInit.ButtonWidth = 2.0F;
      this.btnExtraInit.ButtonHeight = 2.6667F;
      var5.Init(super.m_Module, this, this.btnExtra, this.btnExtraInit);
      var5.ProcessMouse(true);
      var5.Enable(var4);
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExit = this.Add(var5);
      this.btnExitInit.bMoveButton = true;
      this.btnExitInit.xpos = 0.6F;
      this.btnExitInit.ypos = 0.75F;
      this.btnExitInit.width = 0.1F;
      this.btnExitInit.height = 0.1333F;
      this.btnExitInit.ButtonWidth = 2.0F;
      this.btnExitInit.ButtonHeight = 2.0F;
      var5.Init(super.m_Module, this, this.btnExit, this.btnExitInit);
      var5.ProcessMouse(true);
      // [mod] bouton « GRAND PRIX »
      var5 = (KidsButton)this.CreateObject("KidsButton");
      this.btnGrandPrix = this.Add(var5);
      var5.Init(super.m_Module, this, this.btnGrandPrix, this.btnGrandPrixInit);
      var5.ProcessMouse(true);
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.ProcessKb(true);
      super.m_Ctrl = new ControlWindowList(6);
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnGrandPrix)); // [mod] avant « Course », comme à l'écran
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnRace));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnAdventure));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExtra));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExit));
      this.SetAlphaForAll(1.0F);
   }
}
