public class KidsSimpleMenuModule extends KidsMenuModule {
   static int _BlackWindow = -1;
   static int _MainMenuWindow = -1;
   static int _SettingsMenuWindow = -1;
   static int _AudioVideoMenuWindow = -1;
   static int _YesNoDialog = -1;
   static int _SinglePlayerMenuWindow = -1;
   static int _NewContinueMenuWindow = -1;
   static int _AnimalChoiceMenuWindow = -1;
   static int _LapsMenuWindow = -1;
   static int _ExtraMenuWindow = -1;
   static int _RecordsMenuWindow = -1;
   static int _CreditsMenuWindow = -1;
   static int _ControlsMenuWindow = -1;
   static int _PlacesMenuWindow = -1;
   static int _EnterNameMenuWindow = -1;
   static int _LoadingMenuWindow = -1;
   static int _BackgroundMenuWindow = -1;
   static int _CheckCDDialog = -1;
   static int _BossTauntMenuWindow = -1;
   static int _MapLandsMenuWindow = -1;
   static int _MapTracksMenuWindow = -1;
   static int _ProgressMenuWindow = -1;
   static int _FailureMenuWindow = -1;
   static int _ShowEXTRAMenuWindow = -1;
   static int _DemoScreenShotsWindow = -1;
   static int _ModsMenuWindow = -1; // [mod] écran « Mods » (KidsModsMenuWindow)
   static int _GrandPrixMenuWindow = -1; // [mod] classement du Grand Prix (KidsGrandPrixMenuWindow)
   static int _OnlineMenuWindow = -1; // [mod] écran « En ligne » (KidsOnlineMenuWindow)
   float m_LastTime = 0.0F;
   Vector m_FogColor = null;
   static MeshObject[] Billboards = new MeshObject[12];
   MenuAnimal[] m_Animals = new MenuAnimal[12];
   MeshObject[] m_Cars = new MeshObject[12];
   AnimalsList m_AnimalList = new AnimalsList(20);
   static int m_nPanther = -1;
   static int m_nDino = -1;
   static int m_nChicken = -1;
   static int m_nFrog = -1;
   static int m_nTurtle = -1;
   static int m_nShark = -1;
   static int m_nBull = -1;
   static int m_nGiraffe = -1;
   static int m_nPolarBear = -1;
   static int m_nGorilla = -1;
   static int m_nRobo = -1;
   static int m_nLion = -1;
   int m_iLoopMusicID = 0;
   String m_sLoopMusicName = null;
   float m_fMusicVolume = 0.0F;

   void CallDeleteOutroModule() {
      this.DisableCallMethod("DeleteOutroModule");
      this.CallMethodIn("DeleteOutroModule", 0.0F);
   }

   public void DeleteOutroModule() {
      RacerGame.game.DeleteOutroModule();
   }

   public void CallDeleteMovieModule() {
      this.CallMethodIn("DeleteMovieModule", 0.0F);
   }

   void CallCheckCDDialog(int var1, KidsMenuWindow var2) {
      KidsCheckCDDialog var3 = (KidsCheckCDDialog)super.m_WindowsList.WindowAt(_CheckCDDialog);
      if (var3 != null) {
         var3.DoModal(var1, var2);
      }
   }

   public void DeleteMovieModule() {
      RacerGame.game.DestroyMovieModule();
   }

   void OnDeactivate() {
      super.m_WindowsList.Hide(super.m_CurrentMenu);
      KidsMenuModule.m_CarsManager.DeactivateCarsManager();
      this.StopMusicLoop();
   }

   void ShowLoadingScreen() {
      this.StopMusicLoop();
      super.m_WindowsList.HideWindows();
      super.m_WindowsList.Show(_LoadingMenuWindow);
      super.m_Cursor.Hide();
      this.ForceRender();
   }

   MenuAnimal GetChoosenAnimal(int var1) {
      return this.m_Animals[var1];
   }

   void HideAllLevelAnimals() {
      for (int var1 = 0; var1 < this.m_Animals.length; var1++) {
         if (this.m_Animals[var1] != null) {
            this.m_Animals[var1].EnableRendering(false);
         }

         if (this.m_Cars[var1] != null) {
            this.m_Cars[var1].EnableRendering(false);
         }
      }
   }

   void OnActivate() {
      this.PlayMusic(7, 1.0F);
      GameSerializeSettings var1 = RacerGame.GetGSS();
      this.m_LastTime = this.GetTime();
      super.OnActivate();
      super.m_WindowsList.Show(super.m_CurrentMenu);
      super.m_WindowsList.ProcessMouse(super.m_CurrentMenu, true);
      this.SetAudioWaveVolume((int)(var1.m_MusicVolume * 10.0F));
      this.SetAudioFXVolume((int)(var1.m_FXVolume * 10.0F));
      this.ApplyAudioSettings();
      KidsMenuModule.m_CarsManager.ActivateCarsManager();
      this.ButtonsOnTop();
   }

   void ShowBackground(boolean var1, boolean var2) {
      if (var1) {
         super.m_WindowsList.Show(_BackgroundMenuWindow);
      } else {
         super.m_WindowsList.Hide(_BackgroundMenuWindow);
      }

      if (super.m_View != -1) {
         this.ViewRenderTerrain(super.m_View, var2);
         this.ViewRenderObjects(super.m_View, var2);
         this.ViewRenderTrees(super.m_View, var2);
         this.ViewRenderGrass(super.m_View, var2);
         this.ViewRenderWater(super.m_View, var2);
         this.ViewRenderSky(super.m_View, var2);
      }
   }

   void CallDeleteIntro() {
      this.DisableCallMethod("DeleteIntroModule");
      this.CallMethodIn("DeleteIntroModule", 0.01F);
   }

   void StopMusicLoop() {
      if (this.m_iLoopMusicID != 0) {
         this.StopWave(this.m_iLoopMusicID);
         this.m_iLoopMusicID = 0;
         this.m_sLoopMusicName = null;
         this.DisableCallMethod("PlayMusicLoop");
      }
   }

   void DisableBillboard(MeshObject var1) {
      var1.EnableRendering(false);
      var1.SetCollFlags(28672);
      var1.ClearFlag(1);
      var1.ClearFlag(8);
      var1.ClearFlag(32);
   }

   void LoadFonts() {
      Module.fonts = new Fonts();
      Module.fonts.Init(this);
   }

   void SetLoopVolume(float var1) {
      if (this.m_iLoopMusicID != 0) {
         this.m_fMusicVolume = var1;
         this.SetWaveVolume(this.m_iLoopMusicID, var1);
      }
   }

   void PlayMusic(int var1, float var2) {
      this.StopMusicLoop();
      String var3 = "Data\\";
      this.m_fMusicVolume = var2;
      this.m_sLoopMusicName = var3 + CoreLogic.m_sWaveMusic[var1];
      GameObject.LogLine("MenuModule::PlayMusic: " + this.m_fMusicVolume);
      this.PlayMusicLoop();
   }

   void ShowLevelAnimal(int var1) {
      if (this.m_Animals[var1] != null) {
         this.m_Animals[var1].EnableRendering(true);
      }

      if (this.m_Cars[var1] != null) {
         this.m_Cars[var1].EnableRendering(true);
      }
   }

   MenuAnimal GetAnimal(int var1) {
      return this.m_AnimalList.GetAt(var1);
   }

   public void DeleteIntroModule() {
      RacerGame.game.DestroyIntroModule();
   }

   boolean ShowBillboard(int var1) {
      if (var1 >= 0 && var1 < Billboards.length) {
         this.HideBillboards();
         if (Billboards[var1] != null) {
            Billboards[var1].EnableRendering(true);
         }

         GameObject.LogLine("Billboard: " + Billboards[var1].GetPosition());
         return Billboards[var1] != null;
      } else {
         return false;
      }
   }

   public void PlayMusicLoop() {
      if (this.m_sLoopMusicName == null) {
         GameObject.LogLine("Nazwa muzyki == null");
      } else {
         if (this.m_iLoopMusicID != 0) {
            this.StopWave(this.m_iLoopMusicID);
            this.m_iLoopMusicID = 0;
         }

         this.m_iLoopMusicID = this.PlayWave(this.m_sLoopMusicName);
         if (this.m_iLoopMusicID == 0) {
            GameObject.LogLine("Nie udalo sie odpalic dzwieku:" + this.m_sLoopMusicName);
         } else {
            this.SetLoopVolume(this.m_fMusicVolume);
            GameObject.LogLine("MenuModule::m_fMusicVolume: " + this.m_fMusicVolume);
            float var1 = this.GetWaveDuration(this.m_iLoopMusicID);
            this.CallMethodIn("PlayMusicLoop", var1);
         }
      }
   }

   void CallYesNoDialog(int var1, KidsMenuWindow var2) {
      KidsYesNoDialog var3 = (KidsYesNoDialog)super.m_WindowsList.WindowAt(_YesNoDialog);
      if (var3 != null) {
         var3.DoModal(var1, var2);
      }
   }

   void AddBillboards() {
      MeshObject var1 = null;
      GameObject[] var2 = GameObject.GetObjectsOfClass("MeshObject");

      for (int var3 = 0; var3 < var2.length; var3++) {
         if (var2[var3].GetName().compareTo("blb_las") == 0) {
            GameObject.LogLine("Znaleziony billboard: LAS");
            var1 = (MeshObject)var2[var3];
            Billboards[0] = var1;
            Billboards[1] = var1;
            Billboards[2] = var1;
            Billboards[3] = var1;
            Billboards[4] = var1;
            Billboards[5] = var1;
            Billboards[6] = var1;
            this.DisableBillboard(var1);
         }

         if (var2[var3].GetName().compareTo("blb_pustynia") == 0) {
            GameObject.LogLine("Znaleziony billboard: PUSTYNIA");
            var1 = (MeshObject)var2[var3];
            Billboards[7] = var1;
            this.DisableBillboard(var1);
         }

         if (var2[var3].GetName().compareTo("blb_zima") == 0) {
            GameObject.LogLine("Znaleziony billboard: ZIMA");
            var1 = (MeshObject)var2[var3];
            Billboards[8] = var1;
            this.DisableBillboard(var1);
         }

         if (var2[var3].GetName().compareTo("blb_dzungla") == 0) {
            GameObject.LogLine("Znaleziony billboard: DZUNGLA");
            var1 = (MeshObject)var2[var3];
            Billboards[9] = var1;
            this.DisableBillboard(var1);
         }

         if (var2[var3].GetName().compareTo("blb_ksiezyc") == 0) {
            GameObject.LogLine("Znaleziony billboard: KSIEZYC");
            var1 = (MeshObject)var2[var3];
            Billboards[10] = var1;
            this.DisableBillboard(var1);
         }

         if (var2[var3].GetName().compareTo("blb_krolestwo") == 0) {
            GameObject.LogLine("Znaleziony billboard: KROLESTWO");
            var1 = (MeshObject)var2[var3];
            Billboards[11] = var1;
            this.DisableBillboard(var1);
         }
      }
   }

   public void OnCreate() {
      super.OnCreate();
      this.m_FogColor = this.FogGetColor();
      this.LoadFonts();
      KidsLoadingMenuWindow var1 = null;
      Vector var2 = new Vector(18960.0F, 7843.0F, 15296.0F);
      MenuAnimal var3 = MenuAnimal.CreateAndInit(0, var2, this);
      m_nPanther = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3 = MenuAnimal.CreateAndInit(1, var2, this);
      m_nDino = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3 = MenuAnimal.CreateAndInit(2, var2, this);
      m_nChicken = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3 = MenuAnimal.CreateAndInit(3, var2, this);
      m_nFrog = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3 = MenuAnimal.CreateAndInit(4, var2, this);
      m_nShark = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3 = MenuAnimal.CreateAndInit(5, var2, this);
      m_nShark = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3 = MenuAnimal.CreateAndInit(6, var2, this);
      m_nBull = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3 = MenuAnimal.CreateAndInit(7, var2, this);
      m_nGiraffe = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3 = MenuAnimal.CreateAndInit(8, var2, this);
      m_nPolarBear = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3 = MenuAnimal.CreateAndInit(9, var2, this);
      m_nGorilla = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3 = MenuAnimal.CreateAndInit(10, var2, this);
      m_nRobo = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      var3.ClearFlag(4096);
      var3 = MenuAnimal.CreateAndInit(11, var2, this);
      m_nLion = this.m_AnimalList.AddAnimal(var3);
      var3.EnableRendering(false);
      this.AddAnimals();
      this.AddBillboards();
      var1 = (KidsLoadingMenuWindow)this.CreateObject("KidsLoadingMenuWindow");
      this.AddSprite(var1);
      _LoadingMenuWindow = super.m_WindowsList.Add(var1);
      var1.Init(this, _LoadingMenuWindow);
      KidsBlackMenuWindow var5 = (KidsBlackMenuWindow)this.CreateObject("KidsBlackMenuWindow");
      this.AddSprite(var5);
      _BlackWindow = super.m_WindowsList.Add(var5);
      var5.Init(this, _BlackWindow);
      KidsYesNoDialog var6 = (KidsYesNoDialog)this.CreateObject("KidsYesNoDialog");
      this.AddSprite(var6);
      _YesNoDialog = super.m_WindowsList.Add(var6);
      var6.Init(this, _YesNoDialog);
      KidsCheckCDDialog var7 = (KidsCheckCDDialog)this.CreateObject("KidsCheckCDDialog");
      this.AddSprite(var7);
      _CheckCDDialog = super.m_WindowsList.Add(var7);
      var7.Init(this, _CheckCDDialog);
      KidsMainMenuWindow var8 = (KidsMainMenuWindow)this.CreateObject("KidsMainMenuWindow");
      this.AddSprite(var8);
      _MainMenuWindow = super.m_WindowsList.Add(var8);
      var8.Init(this, _MainMenuWindow);
      KidsSettingsMenuWindow var9 = (KidsSettingsMenuWindow)this.CreateObject("KidsSettingsMenuWindow");
      this.AddSprite(var9);
      _SettingsMenuWindow = super.m_WindowsList.Add(var9);
      var9.Init(this, _SettingsMenuWindow);
      KidsAudioVideoMenuWindow var10 = (KidsAudioVideoMenuWindow)this.CreateObject("KidsAudioVideoMenuWindow");
      this.AddSprite(var10);
      _AudioVideoMenuWindow = super.m_WindowsList.Add(var10);
      var10.Init(this, _AudioVideoMenuWindow);
      KidsSinglePlayerMenuWindow var11 = (KidsSinglePlayerMenuWindow)this.CreateObject("KidsSinglePlayerMenuWindow");
      this.AddSprite(var11);
      _SinglePlayerMenuWindow = super.m_WindowsList.Add(var11);
      var11.Init(this, _SinglePlayerMenuWindow);
      KidsNewContinueMenuWindow var12 = (KidsNewContinueMenuWindow)this.CreateObject("KidsNewContinueMenuWindow");
      this.AddSprite(var12);
      _NewContinueMenuWindow = super.m_WindowsList.Add(var12);
      var12.Init(this, _NewContinueMenuWindow);
      KidsAnimalChoiceMenuWindow var13 = (KidsAnimalChoiceMenuWindow)this.CreateObject("KidsAnimalChoiceMenuWindow");
      this.AddSprite(var13);
      _AnimalChoiceMenuWindow = super.m_WindowsList.Add(var13);
      var13.Init(this, _AnimalChoiceMenuWindow);
      KidsMapLandsMenuWindow var14 = (KidsMapLandsMenuWindow)this.CreateObject("KidsMapLandsMenuWindow");
      this.AddSprite(var14);
      _MapLandsMenuWindow = super.m_WindowsList.Add(var14);
      var14.Init(this, _MapLandsMenuWindow);
      KidsMapTracksMenuWindow var15 = (KidsMapTracksMenuWindow)this.CreateObject("KidsMapTracksMenuWindow");
      this.AddSprite(var15);
      _MapTracksMenuWindow = super.m_WindowsList.Add(var15);
      var15.Init(this, _MapTracksMenuWindow);
      KidsLapsMenuWindow var16 = (KidsLapsMenuWindow)this.CreateObject("KidsLapsMenuWindow");
      this.AddSprite(var16);
      _LapsMenuWindow = super.m_WindowsList.Add(var16);
      var16.Init(this, _LapsMenuWindow);
      KidsExtraMenuWindow var17 = (KidsExtraMenuWindow)this.CreateObject("KidsExtraMenuWindow");
      this.AddSprite(var17);
      _ExtraMenuWindow = super.m_WindowsList.Add(var17);
      var17.Init(this, _ExtraMenuWindow);
      KidsRecordsMenuWindow var18 = (KidsRecordsMenuWindow)this.CreateObject("KidsRecordsMenuWindow");
      this.AddSprite(var18);
      _RecordsMenuWindow = super.m_WindowsList.Add(var18);
      var18.Init(this, _RecordsMenuWindow);
      KidsCreditsMenuWindow var19 = (KidsCreditsMenuWindow)this.CreateObject("KidsCreditsMenuWindow");
      this.AddSprite(var19);
      _CreditsMenuWindow = super.m_WindowsList.Add(var19);
      var19.Init(this, _CreditsMenuWindow);
      KidsControlsMenuWindow var20 = (KidsControlsMenuWindow)this.CreateObject("KidsControlsMenuWindow");
      this.AddSprite(var20);
      _ControlsMenuWindow = super.m_WindowsList.Add(var20);
      var20.Init(this, _ControlsMenuWindow);
      KidsPlacesMenuWindow var21 = (KidsPlacesMenuWindow)this.CreateObject("KidsPlacesMenuWindow");
      this.AddSprite(var21);
      _PlacesMenuWindow = super.m_WindowsList.Add(var21);
      var21.Init(this, _PlacesMenuWindow);
      KidsEnterNameMenuWindow var22 = (KidsEnterNameMenuWindow)this.CreateObject("KidsEnterNameMenuWindow");
      this.AddSprite(var22);
      _EnterNameMenuWindow = super.m_WindowsList.Add(var22);
      var22.Init(this, _EnterNameMenuWindow);
      KidsBossTauntMenuWindow var23 = (KidsBossTauntMenuWindow)this.CreateObject("KidsBossTauntMenuWindow");
      this.AddSprite(var23);
      _BossTauntMenuWindow = super.m_WindowsList.Add(var23);
      var23.Init(this, _BossTauntMenuWindow);
      KidsProgressMenuWindow var24 = (KidsProgressMenuWindow)this.CreateObject("KidsProgressMenuWindow");
      this.AddSprite(var24);
      _ProgressMenuWindow = super.m_WindowsList.Add(var24);
      var24.Init(this, _ProgressMenuWindow);
      KidsFailureMenuWindow var25 = (KidsFailureMenuWindow)this.CreateObject("KidsFailureMenuWindow");
      this.AddSprite(var25);
      _FailureMenuWindow = super.m_WindowsList.Add(var25);
      var25.Init(this, _FailureMenuWindow);
      KidsShowExtraMenuWindow var26 = (KidsShowExtraMenuWindow)this.CreateObject("KidsShowExtraMenuWindow");
      this.AddSprite(var26);
      _ShowEXTRAMenuWindow = super.m_WindowsList.Add(var26);
      var26.Init(this, _ShowEXTRAMenuWindow);
      // [mod] écran « Mods » (chantier 3), ouvert par le bouton engrenage du menu principal
      KidsModsMenuWindow wndMods = (KidsModsMenuWindow)this.CreateObject("KidsModsMenuWindow");
      this.AddSprite(wndMods);
      _ModsMenuWindow = super.m_WindowsList.Add(wndMods);
      wndMods.Init(this, _ModsMenuWindow);
      // [mod] classement du Grand Prix (chantier 6), après chaque course de la coupe
      KidsGrandPrixMenuWindow wndGP = (KidsGrandPrixMenuWindow)this.CreateObject("KidsGrandPrixMenuWindow");
      this.AddSprite(wndGP);
      _GrandPrixMenuWindow = super.m_WindowsList.Add(wndGP);
      wndGP.Init(this, _GrandPrixMenuWindow);
      // [mod] écran « En ligne » (chantier 8, phase 3) : salon, animal, prêt, réglages de l'hôte
      KidsOnlineMenuWindow wndOnline = (KidsOnlineMenuWindow)this.CreateObject("KidsOnlineMenuWindow");
      this.AddSprite(wndOnline);
      _OnlineMenuWindow = super.m_WindowsList.Add(wndOnline);
      wndOnline.Init(this, _OnlineMenuWindow);
      if (CoreLogic.m_bIsLocked) {
         KidsScreenShots var27 = (KidsScreenShots)this.CreateObject("KidsScreenShots");
         this.AddSprite(var27);
         _DemoScreenShotsWindow = super.m_WindowsList.Add(var27);
         var27.Init(this, _DemoScreenShotsWindow);
      }

      KidsBackgroundMenuWindow var28 = (KidsBackgroundMenuWindow)this.CreateObject("KidsBackgroundMenuWindow");
      this.AddSprite(var28);
      _BackgroundMenuWindow = super.m_WindowsList.Add(var28);
      var28.Init(this, _BackgroundMenuWindow);
      super.m_Camera.InitObject();
      this.HideAllWindows();
      super.m_CurrentMenu = _EnterNameMenuWindow;
      KidsMenuModule.m_CarsManager = (CarsManager)this.CreateObject("CarsManager");
      KidsMenuModule.m_CarsManager.ActivateOnModule();
   }

   void HideBillboards() {
      for (int var1 = 0; var1 < Billboards.length; var1++) {
         if (Billboards[var1] != null) {
            Billboards[var1].EnableRendering(false);
         }
      }
   }

   int GetLevelAnimalByRef(MenuAnimal var1) {
      for (int var2 = 0; var2 < this.m_Animals.length; var2++) {
         if (this.m_Animals[var2] == var1) {
            return var2;
         }
      }

      return -1;
   }

   void AddAnimals() {
      GameObject[] var1 = GameObject.GetObjectsOfClass("MenuAnimal");

      for (int var2 = 0; var2 < var1.length; var2++) {
         if (var1[var2].GetName().compareTo("MenuPantera") == 0) {
            this.m_Animals[0] = (MenuAnimal)var1[var2];
            this.m_Animals[0].m_AnimalKind = 0;
            this.m_Animals[0].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[0];
            this.m_Animals[0].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[0];
            this.m_Animals[0].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[0];
            this.m_Animals[0].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 0);
         }

         if (var1[var2].GetName().compareTo("MenuDino") == 0) {
            this.m_Animals[1] = (MenuAnimal)var1[var2];
            this.m_Animals[1].m_AnimalKind = 1;
            this.m_Animals[1].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[1];
            this.m_Animals[1].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[1];
            this.m_Animals[1].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[1];
            this.m_Animals[1].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 1);
         }

         if (var1[var2].GetName().compareTo("MenuKurczak") == 0) {
            this.m_Animals[2] = (MenuAnimal)var1[var2];
            this.m_Animals[2].m_AnimalKind = 2;
            this.m_Animals[2].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[2];
            this.m_Animals[2].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[2];
            this.m_Animals[2].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[2];
            this.m_Animals[2].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 2);
         }

         if (var1[var2].GetName().compareTo("MenuZaba") == 0) {
            this.m_Animals[3] = (MenuAnimal)var1[var2];
            this.m_Animals[3].m_AnimalKind = 3;
            this.m_Animals[3].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[3];
            this.m_Animals[3].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[3];
            this.m_Animals[3].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[3];
            this.m_Animals[3].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 3);
         }

         if (var1[var2].GetName().compareTo("MenuZolw") == 0) {
            this.m_Animals[4] = (MenuAnimal)var1[var2];
            this.m_Animals[4].m_AnimalKind = 4;
            this.m_Animals[4].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[4];
            this.m_Animals[4].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[4];
            this.m_Animals[4].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[4];
            this.m_Animals[4].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 4);
         }

         if (var1[var2].GetName().compareTo("MenuRekin") == 0) {
            this.m_Animals[5] = (MenuAnimal)var1[var2];
            this.m_Animals[5].m_AnimalKind = 5;
            this.m_Animals[5].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[5];
            this.m_Animals[5].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[5];
            this.m_Animals[5].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[5];
            this.m_Animals[5].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 5);
         }

         if (var1[var2].GetName().compareTo("MenuByk") == 0) {
            this.m_Animals[6] = (MenuAnimal)var1[var2];
            this.m_Animals[6].m_AnimalKind = 6;
            this.m_Animals[6].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[6];
            this.m_Animals[6].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[6];
            this.m_Animals[6].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[6];
            this.m_Animals[6].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 6);
         }

         if (var1[var2].GetName().compareTo("MenuZyrafa") == 0) {
            this.m_Animals[7] = (MenuAnimal)var1[var2];
            this.m_Animals[7].m_AnimalKind = 7;
            this.m_Animals[7].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[7];
            this.m_Animals[7].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[7];
            this.m_Animals[7].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[7];
            this.m_Animals[7].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 7);
         }

         if (var1[var2].GetName().compareTo("MenuMis") == 0) {
            this.m_Animals[8] = (MenuAnimal)var1[var2];
            this.m_Animals[8].m_AnimalKind = 8;
            this.m_Animals[8].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[8];
            this.m_Animals[8].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[8];
            this.m_Animals[8].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[8];
            this.m_Animals[8].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 8);
         }

         if (var1[var2].GetName().compareTo("MenuGoryl") == 0) {
            this.m_Animals[9] = (MenuAnimal)var1[var2];
            this.m_Animals[9].m_AnimalKind = 9;
            this.m_Animals[9].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[9];
            this.m_Animals[9].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[9];
            this.m_Animals[9].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[9];
            this.m_Animals[9].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 9);
         }

         if (var1[var2].GetName().compareTo("MenuRobocik") == 0) {
            this.m_Animals[10] = (MenuAnimal)var1[var2];
            this.m_Animals[10].m_AnimalKind = 10;
            this.m_Animals[10].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[10];
            this.m_Animals[10].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[10];
            this.m_Animals[10].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[10];
            this.m_Animals[10].SetLighting(true);
            this.m_Animals[10].ClearFlag(4096);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 10);
         }

         if (var1[var2].GetName().compareTo("MenuLew") == 0) {
            this.m_Animals[11] = (MenuAnimal)var1[var2];
            this.m_Animals[11].m_AnimalKind = 11;
            this.m_Animals[11].m_ChooseAnimalRadius = MenuAnimal.CAMERARADIUS[11];
            this.m_Animals[11].m_ChooseAnimalHeight = MenuAnimal.CAMERAHEIGHT[11];
            this.m_Animals[11].m_ChooseAnimalAddHeight = MenuAnimal.CAMERADIRHEIGHT[11];
            this.m_Animals[11].SetLighting(true);
            MenuAnimal.InitAnimIdle((MenuAnimal)var1[var2], 11);
         }
      }

      var1 = GameObject.GetObjectsFromClass("MeshObject");

      for (int var4 = 0; var4 < var1.length; var4++) {
         if (var1[var4].GetName().compareTo("pantera_sam01") == 0) {
            this.m_Cars[0] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("dino_sam01") == 0) {
            this.m_Cars[1] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("kurczak_sam01") == 0) {
            this.m_Cars[2] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("zaba_sam01") == 0) {
            this.m_Cars[3] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("zolw_sam01") == 0) {
            this.m_Cars[4] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("rekin_sam01") == 0) {
            this.m_Cars[5] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("byk_sam01") == 0) {
            this.m_Cars[6] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("zyrafa_sam01") == 0) {
            this.m_Cars[7] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("mis_sam01") == 0) {
            this.m_Cars[8] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("goryl_sam01") == 0) {
            this.m_Cars[9] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("robocik_sam01") == 0) {
            this.m_Cars[10] = (MeshObject)var1[var4];
         }

         if (var1[var4].GetName().compareTo("lew_sam01") == 0) {
            this.m_Cars[11] = (MeshObject)var1[var4];
         }
      }
   }

   public void InitModule() {
      super.InitModule();
   }

   public void OnFrame() {
      super.OnFrame();
      float var1 = this.GetTime();
      float var2 = var1 - this.m_LastTime;
      this.m_LastTime = var1;
      super.m_Camera.Update(var2);
   }
}
