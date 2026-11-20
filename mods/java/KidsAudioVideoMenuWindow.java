public class KidsAudioVideoMenuWindow extends KidsMenuWindow {
   int bkTex0 = -1;
   String m_TexName = "Data/Textures/Menu/b/MenuAudioVideo1024.png";
   String m_TexLogoPetRacer = "Data/Textures/Menu/a/LogoPetRacer.png";
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(57);
   int btnOK = -1;
   InitKidsButton btnOKInit = new InitKidsButton(40);
   int sldDetails = -1;
   InitKidsSlider sldDetailsInit = new InitKidsSlider(1);
   int sldCamera0 = -1;
   InitKidsSlider sldCamera0Init = new InitKidsSlider(2);
   int sldMusic = -1;
   InitKidsSlider sldMusicInit = new InitKidsSlider(3);
   int sldFX = -1;
   InitKidsSlider sldFXInit = new InitKidsSlider(4);
   int sldRes = -1;
   InitKidsSlider sldResInit = new InitKidsSlider(5);
   boolean bChangeResolution = false;
   int ktxRes = -1; // [mod] résolution choisie, écrite sur la plaque du fond (ModVideo)

   void ButtonsOnTop() {
      super.m_List.SetOnTop(this.sldDetails);
      super.m_List.SetOnTop(this.sldCamera0);
      super.m_List.SetOnTop(this.sldMusic);
      super.m_List.SetOnTop(this.sldFX);
      super.m_List.SetOnTop(this.sldRes);
      super.m_List.SetOnTop(this.btnOK);
      super.m_List.SetOnTop(this.btnExit);
   }

   public void ChangeGameSettings() {
      if (this.bChangeResolution) {
         ((KidsSimpleMenuModule)super.m_Module).StopMusicLoop();
      }

      Settings var1 = RacerGame.settings;
      int var2 = 0;
      var2 = ((KidsSlider)super.m_List.WindowAt(this.sldRes)).GetValueDiscrete();
      if (this.bChangeResolution) {
         // [mod] le curseur parcourt la liste de ModVideo (modes réels de l'écran) au lieu de 640/800/1024
         ModVideo.RequestMode(var1, var2);
         KidsMenuModule.m_CarsManager.UpdateResolution();
      }

      var2 = ((KidsSlider)super.m_List.WindowAt(this.sldDetails)).GetValueDiscrete();
      byte var3;
      switch (var2) {
         case 0:
            var1.nTextureQuality = 2;
            var3 = 2;
            PerformanceSettings.SetCurrQuality(PerformanceSettings._QUALITY_MIN);
            break;
         case 1:
            var1.nTextureQuality = 1;
            var3 = 4;
            PerformanceSettings.SetCurrQuality(PerformanceSettings._QUALITY_MED);
            break;
         default:
            var1.nTextureQuality = 0;
            var3 = 5;
            PerformanceSettings.SetCurrQuality(PerformanceSettings._QUALITY_MAX);
      }

      RacerGame.SetDetails(var2);
      RacerGame.game.SetTextureQuality(var3);
      RacerGame.SetCameraDistance(0, ((KidsSlider)super.m_List.WindowAt(this.sldCamera0)).GetValueDiscrete());
      float var4 = ((KidsSlider)super.m_List.WindowAt(this.sldMusic)).m_Value;
      RacerGame.SetMusicVolume(var4);
      RacerGame.game.SetAudioWaveVolume((int)(var4 * 10.0F));
      var4 = ((KidsSlider)super.m_List.WindowAt(this.sldFX)).m_Value;
      RacerGame.SetFXVolume(var4);
      RacerGame.game.SetAudioFXVolume((int)(var4 * 10.0F));
      if (this.bChangeResolution) {
         this.ApplyVideoSettings();
         super.m_Module.OnChangeResolution();
      }

      this.ApplyAudioSettings();
      RacerGame.settings.SaveSettings();
      RacerGame.SaveAttrib();
      this.RefreshControls();
      if (this.bChangeResolution) {
         ((KidsSimpleMenuModule)super.m_Module).PlayMusic(7, 1.0F);
      }
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
      } else if (var1 == this.btnOK) {
         this.DisactivateWindow();
         this.ChangeGameSettings();
         super.m_Module.ActivateMenuWindow(super.m_Module.PopMenu());
      }
   }

   // [mod] le texte de la résolution suit le curseur pendant qu'on le fait glisser
   public void SliderChanging(int var1, float var2) {
      super.SliderChanging(var1, var2);
      if (var1 == this.sldRes) {
         this.RefreshResText();
      }
   }

   public void SliderChanged(int var1, float var2, float var3) {
      super.SliderChanged(var1, var2, var3);
      if (var1 == this.sldDetails || var1 == this.sldRes) {
         this.bChangeResolution = true;
      }

      if (var1 == this.sldRes) {
         this.RefreshResText(); // [mod]
      }

      if (var1 == this.sldMusic) {
         float var4 = ((KidsSlider)super.m_List.WindowAt(this.sldMusic)).m_Value;
         RacerGame.SetMusicVolume(var4);
         RacerGame.game.SetAudioWaveVolume((int)(var4 * 10.0F));
      }

      if (var1 == this.sldFX) {
         float var5 = ((KidsSlider)super.m_List.WindowAt(this.sldFX)).m_Value;
         RacerGame.SetFXVolume(var5);
         RacerGame.game.SetAudioFXVolume((int)(var5 * 10.0F));
      }
   }

   public void Hide() {
      super.Hide();
      this.ProcessKb(false);
   }

   public void Show() {
      super.m_Module.SetIgnoreKeys(2);
      this.bChangeResolution = false;
      super.Show();
      this.ProcessKb(true);
      ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, false);
      this.RefreshControls();
      if (!KidsMenuModule.m_CarsManager.IsInitialized()) {
         KidsMenuModule.m_CarsManager.ActivateCarsManager();
      }

      KidsMenuModule.m_CarsManager.Show(this);
   }

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(this.m_TexName, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), this.m_TexName);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      KidsSlider var3 = (KidsSlider)this.CreateObject("KidsSlider");
      this.sldDetails = this.Add(var3);
      this.sldDetailsInit.xpos = 0.78125F;
      this.sldDetailsInit.ypos = 0.075F;
      this.sldDetailsInit.width = 0.3125F;
      this.sldDetailsInit.height = 0.13334F;
      this.sldDetailsInit.sliderwidth = 0.32F;
      this.sldDetailsInit.sliderheight = 1.0F;
      this.sldDetailsInit.slidery = 0.5F;
      var3.Init(super.m_Module, this, this.sldDetails, this.sldDetailsInit);
      var3.ProcessMouse(true);
      var3.SetDiscretisation(3);
      var3.SetBorders(0.1F, 0.9F);
      var3 = (KidsSlider)this.CreateObject("KidsSlider");
      this.sldCamera0 = this.Add(var3);
      this.sldCamera0Init.xpos = 0.78125F;
      this.sldCamera0Init.ypos = 0.266667F;
      this.sldCamera0Init.width = 0.3125F;
      this.sldCamera0Init.height = 0.13334F;
      this.sldCamera0Init.sliderwidth = 0.32F;
      this.sldCamera0Init.sliderheight = 1.0F;
      this.sldCamera0Init.slidery = 0.5F;
      var3.Init(super.m_Module, this, this.sldCamera0, this.sldCamera0Init);
      var3.ProcessMouse(true);
      var3.SetDiscretisation(3);
      var3.SetBorders(0.1F, 0.9F);
      var3 = (KidsSlider)this.CreateObject("KidsSlider");
      this.sldMusic = this.Add(var3);
      this.sldMusicInit.xpos = 0.35F;
      this.sldMusicInit.ypos = 0.60834F;
      this.sldMusicInit.width = 0.3125F;
      this.sldMusicInit.height = 0.13334F;
      this.sldMusicInit.sliderwidth = 0.32F;
      this.sldMusicInit.sliderheight = 1.0F;
      this.sldMusicInit.slidery = 0.5F;
      var3.Init(super.m_Module, this, this.sldMusic, this.sldMusicInit);
      var3.ProcessMouse(true);
      var3.SetBorders(0.1F, 0.9F);
      var3 = (KidsSlider)this.CreateObject("KidsSlider");
      this.sldFX = this.Add(var3);
      this.sldFXInit.xpos = 0.3F;
      this.sldFXInit.ypos = 0.8F;
      this.sldFXInit.width = 0.3125F;
      this.sldFXInit.height = 0.13334F;
      this.sldFXInit.sliderwidth = 0.32F;
      this.sldFXInit.sliderheight = 1.0F;
      this.sldFXInit.slidery = 0.5F;
      var3.Init(super.m_Module, this, this.sldFX, this.sldFXInit);
      var3.ProcessMouse(true);
      var3.SetBorders(0.1F, 0.9F);
      var3 = (KidsSlider)this.CreateObject("KidsSlider");
      this.sldRes = this.Add(var3);
      this.sldResInit.xpos = 0.78125F;
      this.sldResInit.ypos = 0.46667F;
      this.sldResInit.width = 0.3125F;
      this.sldResInit.height = 0.13334F;
      this.sldResInit.sliderwidth = 0.32F;
      this.sldResInit.sliderheight = 1.0F;
      this.sldResInit.slidery = 0.5F;
      var3.Init(super.m_Module, this, this.sldRes, this.sldResInit);
      var3.ProcessMouse(true);
      var3.SetDiscretisation(Math.max(2, ModVideo.GetModeCount())); // [mod] un cran par résolution
      var3.SetBorders(0.1F, 0.9F);
      KidsButton var4 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExit = this.Add(var4);
      this.btnExitInit.bMoveButton = true;
      var4.Init(super.m_Module, this, this.btnExit, this.btnExitInit);
      var4.ProcessMouse(true);
      var3.SetBorders(0.1F, 0.9F);
      var4 = (KidsButton)this.CreateObject("KidsButton");
      this.btnOK = this.Add(var4);
      this.btnOKInit.bMoveButton = true;
      this.btnOKInit.xpos = 0.6875F;
      this.btnOKInit.ypos = 0.7F;
      this.btnOKInit.width = 0.14375F;
      this.btnOKInit.height = 0.1916F;
      this.btnOKInit.ButtonWidth = 1.0F;
      this.btnOKInit.ButtonHeight = 1.8823F;
      var4.Init(super.m_Module, this, this.btnOK, this.btnOKInit);
      var4.ProcessMouse(true);
      // [mod] résolution écrite sur la plaque peinte à la place des libellés 640/480 800/600 1024/768
      KidsText ktx = (KidsText)this.CreateObject("KidsText");
      this.ktxRes = this.Add(ktx);
      ktx.Init(super.m_Module, this.ktxRes);
      ktx.SetSizeAndPos(ModVideo._LABEL_W, ModVideo._LABEL_H, ModVideo._LABEL_X, ModVideo._LABEL_Y, true);
      ktx.Show();
      KidsMenuWindow var5 = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      int var6 = this.Add(var5);
      var5.Init(super.m_Module, var6);
      var5.AddWindowTexture(var5.AddTexture(this.m_TexLogoPetRacer, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.m_TexLogoPetRacer);
      var5.SetSizeAndPos(0.4F, 0.53333336F, 0.2625F, 0.31666666F, true);
      var5.SetWindowTexture(0);
      var5.Show();
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.SetAlphaForAll(1.0F);
      this.ProcessKb(true);
      super.m_Ctrl = new ControlWindowList(10);
      super.m_Ctrl.Push(super.m_List.WindowAt(this.sldDetails));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.sldCamera0));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.sldRes));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.sldMusic));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.sldFX));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnOK));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExit));
      this.RefreshControls();
   }

   void RefreshControls() {
      KidsSlider var1 = (KidsSlider)super.m_List.WindowAt(this.sldDetails);
      var1.SetValueDiscrete(RacerGame.GetDetails());
      var1.RefreshPos();
      var1 = (KidsSlider)super.m_List.WindowAt(this.sldCamera0);
      var1.SetValueDiscrete(RacerGame.GetCameraDistance(0));
      var1.RefreshPos();
      var1 = (KidsSlider)super.m_List.WindowAt(this.sldMusic);
      var1.SetSliderByValue(RacerGame.GetMusicVolume());
      var1.RefreshPos();
      var1 = (KidsSlider)super.m_List.WindowAt(this.sldFX);
      var1.SetSliderByValue(RacerGame.GetFXVolume());
      var1.RefreshPos();
      var1 = (KidsSlider)super.m_List.WindowAt(this.sldRes);
      var1.SetValueDiscrete(ModVideo.GetCurrentMode()); // [mod] position dans la liste de ModVideo
      var1.RefreshPos();
      this.RefreshSetSizeAndPos();
      this.RefreshResText();
   }

   // [mod] affiche la résolution sous le curseur
   void RefreshResText() {
      if (this.ktxRes < 0) {
         return;
      }

      int i = ((KidsSlider)super.m_List.WindowAt(this.sldRes)).GetValueDiscrete();
      ((KidsText)super.m_List.WindowAt(this.ktxRes)).SetText(ModVideo.GetModeName(Math.max(0, Math.min(ModVideo.GetModeCount() - 1, i))));
   }

   public int GetGameSettings() {
      Settings var1 = RacerGame.settings;
      byte var2 = 0;
      switch (var1.nResolution) {
         case 0:
            var2 = 0;
            break;
         case 1:
            var2 = 1;
            break;
         case 2:
            var2 = 2;
      }

      byte var3 = var2;
      switch (var1.nTextureQuality) {
         case 0:
            var2 = 2;
            break;
         case 1:
            boolean var5 = true;
            break;
         case 2:
            boolean var4 = false;
      }

      this.RefreshControls();
      return var3;
   }
}
