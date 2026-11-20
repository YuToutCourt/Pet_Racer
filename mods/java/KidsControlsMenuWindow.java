public class KidsControlsMenuWindow extends KidsMenuWindow {
   int bkTex0 = -1;
   String m_TexName = "Data/Textures/Menu/b/MenuControls.png";
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(47);
   int btnFIRE11 = -1;
   InitKidsButton btnFIRE11Init = new InitKidsButton(34);
   int btnUPARROW1 = -1;
   InitKidsButton btnUPARROW1Init = new InitKidsButton(30);
   int btnDOWNARROW1 = -1;
   InitKidsButton btnDOWNARROW1Init = new InitKidsButton(31);
   int btnLEFTARROW1 = -1;
   InitKidsButton btnLEFTARROW1Init = new InitKidsButton(32);
   int btnRIGHTARROW1 = -1;
   InitKidsButton btnRIGHTARROW1Init = new InitKidsButton(33);
   int btnFIRE12 = -1;
   InitKidsButton btnFIRE12Init = new InitKidsButton(52);
   int btnUPARROW2 = -1;
   InitKidsButton btnUPARROW2Init = new InitKidsButton(48);
   int btnDOWNARROW2 = -1;
   InitKidsButton btnDOWNARROW2Init = new InitKidsButton(49);
   int btnLEFTARROW2 = -1;
   InitKidsButton btnLEFTARROW2Init = new InitKidsButton(50);
   int btnRIGHTARROW2 = -1;
   InitKidsButton btnRIGHTARROW2Init = new InitKidsButton(51);
   int sldKBJOYPAD1 = -1;
   InitKidsSlider sldKBJOYPAD1Init = new InitKidsSlider(6);
   int sldKBJOYPAD2 = -1;
   InitKidsSlider sldKBJOYPAD2Init = new InitKidsSlider(6);
   static final float _TEXTWIDTH = 0.12F;
   static final float _TEXTHEIGHT = 0.07F;
   static final float _LEFT1 = 0.1F;
   static final float _LEFT2 = 0.6F;
   static final float _TOP = 0.25F;
   static final float _TOP_DIST = 0.125F;
   static final float _SLIDER_LEFT_DIST = 0.1F;
   static final float _TEXT_DIST = 0.15F;
   static final float _JOY_DIST = 0.15F;
   int ktxFire11 = -1;
   static final float ktxFire11Width = 0.12F;
   static final float ktxFire11Height = 0.07F;
   static final float ktxFire11PosX = 0.3125F;
   static final float ktxFire11PosY = 0.25833333F;
   int ktxUp1 = -1;
   static final float ktxUp1Width = 0.12F;
   static final float ktxUp1Height = 0.07F;
   static final float ktxUp1PosX = 0.33125F;
   static final float ktxUp1PosY = 0.38333333F;
   int ktxDown1 = -1;
   static final float ktxDown1Width = 0.12F;
   static final float ktxDown1Height = 0.07F;
   static final float ktxDown1PosX = 0.33125F;
   static final float ktxDown1PosY = 0.50208336F;
   int ktxLeft1 = -1;
   static final float ktxLeft1Width = 0.12F;
   static final float ktxLeft1Height = 0.07F;
   static final float ktxLeft1PosX = 0.3203125F;
   static final float ktxLeft1PosY = 0.6375F;
   int ktxRight1 = -1;
   static final float ktxRight1Width = 0.12F;
   static final float ktxRight1Height = 0.07F;
   static final float ktxRight1PosX = 0.303125F;
   static final float ktxRight1PosY = 0.775F;
   int ktxFire12 = -1;
   static final float ktxFire12Width = 0.12F;
   static final float ktxFire12Height = 0.07F;
   static final float ktxFire12PosX = 0.6875F;
   static final float ktxFire12PosY = 0.25833333F;
   int ktxUp2 = -1;
   static final float ktxUp2Width = 0.12F;
   static final float ktxUp2Height = 0.07F;
   static final float ktxUp2PosX = 0.66875F;
   static final float ktxUp2PosY = 0.38333333F;
   int ktxDown2 = -1;
   static final float ktxDown2Width = 0.12F;
   static final float ktxDown2Height = 0.07F;
   static final float ktxDown2PosX = 0.671875F;
   static final float ktxDown2PosY = 0.50208336F;
   int ktxLeft2 = -1;
   static final float ktxLeft2Width = 0.12F;
   static final float ktxLeft2Height = 0.07F;
   static final float ktxLeft2PosX = 0.6796875F;
   static final float ktxLeft2PosY = 0.63958335F;
   int ktxRight2 = -1;
   static final float ktxRight2Width = 0.12F;
   static final float ktxRight2Height = 0.07F;
   static final float ktxRight2PosX = 0.6953125F;
   static final float ktxRight2PosY = 0.775F;
   int m_Pressed = -1;

   void ButtonsOnTop() {
      super.m_List.SetOnTop(this.btnFIRE11);
      super.m_List.SetOnTop(this.btnFIRE12);
      super.m_List.SetOnTop(this.btnUPARROW1);
      super.m_List.SetOnTop(this.btnUPARROW2);
      super.m_List.SetOnTop(this.btnLEFTARROW1);
      super.m_List.SetOnTop(this.btnLEFTARROW2);
      super.m_List.SetOnTop(this.btnRIGHTARROW1);
      super.m_List.SetOnTop(this.btnRIGHTARROW2);
      super.m_List.SetOnTop(this.btnDOWNARROW1);
      super.m_List.SetOnTop(this.btnDOWNARROW2);
      super.m_List.SetOnTop(this.sldKBJOYPAD1);
      super.m_List.SetOnTop(this.sldKBJOYPAD2);
      super.m_List.SetOnTop(this.ktxFire11);
      super.m_List.SetOnTop(this.ktxFire12);
      super.m_List.SetOnTop(this.ktxUp1);
      super.m_List.SetOnTop(this.ktxUp2);
      super.m_List.SetOnTop(this.ktxDown1);
      super.m_List.SetOnTop(this.ktxDown2);
      super.m_List.SetOnTop(this.ktxLeft1);
      super.m_List.SetOnTop(this.ktxLeft2);
      super.m_List.SetOnTop(this.ktxRight1);
      super.m_List.SetOnTop(this.ktxRight2);
      super.m_List.SetOnTop(this.btnExit);
   }

   public void OnKeyReleased(int var1, char var2) {
      if (this.CanProcessMouseMove() && !super.m_Module.CanIgnoreKeys()) {
         if (this.m_Pressed >= 0) {
            if (var1 == 1) {
               this.m_Pressed = -1;
               this.RefreshControls();
            } else {
               KbSettings var3 = RacerGame.GetKb0();
               KbSettings var4 = RacerGame.GetKb1();
               String var5 = KbSettings.GetKeyString(var1);
               if (var5 != null) {
                  if (this.m_Pressed <= 5) {
                     var3.SetKey(var1, this.m_Pressed, var4);
                  } else if (this.m_Pressed <= 11) {
                     var4.SetKey(var1, this.m_Pressed - 6, var3);
                  }

                  this.m_Pressed = -1;
                  this.RefreshControls();
               }
            }
         } else if (var1 == 203) {
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
      if (this.m_Pressed < 0) {
         super.ButtonPushed(var1);
         if (var1 == this.btnExit) {
            this.m_Pressed = -1;
            this.DisactivateWindow();
            super.m_Module.ActivateMenuWindow(super.m_Module.PopMenu());
            RacerGame.SaveAttrib();
         } else if (var1 == this.btnUPARROW1) {
            this.m_Pressed = 0;
            this.RefreshControls();
         } else if (var1 == this.btnDOWNARROW1) {
            this.m_Pressed = 1;
            this.RefreshControls();
         } else if (var1 == this.btnLEFTARROW1) {
            this.m_Pressed = 2;
            this.RefreshControls();
         } else if (var1 == this.btnRIGHTARROW1) {
            this.m_Pressed = 3;
            this.RefreshControls();
         } else if (var1 == this.btnFIRE11) {
            this.m_Pressed = 4;
            this.RefreshControls();
         } else if (var1 == this.btnUPARROW2) {
            this.m_Pressed = 6;
            this.RefreshControls();
         } else if (var1 == this.btnDOWNARROW2) {
            this.m_Pressed = 7;
            this.RefreshControls();
         } else if (var1 == this.btnLEFTARROW2) {
            this.m_Pressed = 8;
            this.RefreshControls();
         } else if (var1 == this.btnRIGHTARROW2) {
            this.m_Pressed = 9;
            this.RefreshControls();
         } else if (var1 == this.btnFIRE12) {
            this.m_Pressed = 10;
            this.RefreshControls();
         } else {
            byte var2 = -1;
            this.RefreshControls();
         }
      }
   }

   public void SliderChanged(int var1, float var2, float var3) {
      super.SliderChanged(var1, var2, var3);
      this.m_Pressed = -1;
      KbSettings var4 = RacerGame.GetKb0();
      KbSettings var5 = RacerGame.GetKb1();
      KidsSlider var6 = (KidsSlider)super.m_List.WindowAt(this.sldKBJOYPAD1);
      KidsSlider var7 = (KidsSlider)super.m_List.WindowAt(this.sldKBJOYPAD2);
      if (var1 == this.sldKBJOYPAD1) {
         var4.m_Device = var6.GetValueDiscrete();
      }

      if (var1 == this.sldKBJOYPAD2) {
         var5.m_Device = 2 - var7.GetValueDiscrete();
      }

      this.RefreshControls();
   }

   public void OnInputJoyButton(int var1, int var2, boolean var3) {
      if ((this.m_Pressed == 4 || this.m_Pressed == 5 || this.m_Pressed == 10 || this.m_Pressed == 11) && var3) {
         KbSettings var4 = RacerGame.GetKb0();
         KbSettings var5 = RacerGame.GetKb1();
         String var6 = KbSettings.GetJoyButtonString(var2, 0, false);
         if (var6 == null) {
            return;
         }

         if (this.m_Pressed == 4 || this.m_Pressed == 5) {
            var4.SetJoyButton(var2, this.m_Pressed - 4, var5, var1);
         }

         if (this.m_Pressed == 10 || this.m_Pressed == 11) {
            var5.SetJoyButton(var2, this.m_Pressed - 10, var4, var1);
         }

         this.m_Pressed = -1;
         this.RefreshControls();
      }
   }

   public void Hide() {
      super.Hide();
      this.ProcessKb(false);
   }

   public void Show() {
      super.m_Module.SetIgnoreKeys(2);
      super.Show();
      this.RefreshControls();
      this.ProcessKb(true);
      this.ProcessJoy(true);
      ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, false);
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
      KidsButton var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnFIRE11 = this.Add(var3);
      this.btnFIRE11Init.xpos = 0.184375F;
      this.btnFIRE11Init.ypos = 0.23958333F;
      this.btnFIRE11Init.width = 0.08437F;
      this.btnFIRE11Init.height = 0.05833F;
      this.btnFIRE11Init.ButtonWidth = 1.1852F;
      this.btnFIRE11Init.ButtonHeight = 2.2857F;
      var3.Init(super.m_Module, this, this.btnFIRE11, this.btnFIRE11Init);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnUPARROW1 = this.Add(var3);
      this.btnUPARROW1Init.xpos = 0.21875F;
      this.btnUPARROW1Init.ypos = 0.39583334F;
      this.btnUPARROW1Init.width = 0.09375F;
      this.btnUPARROW1Init.height = 0.05F;
      this.btnUPARROW1Init.ButtonWidth = 1.14285F;
      this.btnUPARROW1Init.ButtonHeight = 2.6667F;
      var3.Init(super.m_Module, this, this.btnUPARROW1, this.btnUPARROW1Init);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnDOWNARROW1 = this.Add(var3);
      this.btnDOWNARROW1Init.xpos = 0.2109375F;
      this.btnDOWNARROW1Init.ypos = 0.5104167F;
      this.btnDOWNARROW1Init.width = 0.09687F;
      this.btnDOWNARROW1Init.height = 0.05F;
      this.btnDOWNARROW1Init.ButtonWidth = 2.0645F;
      this.btnDOWNARROW1Init.ButtonHeight = 2.6667F;
      var3.Init(super.m_Module, this, this.btnDOWNARROW1, this.btnDOWNARROW1Init);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnLEFTARROW1 = this.Add(var3);
      this.btnLEFTARROW1Init.xpos = 0.2015625F;
      this.btnLEFTARROW1Init.ypos = 0.65416664F;
      this.btnLEFTARROW1Init.width = 0.0781F;
      this.btnLEFTARROW1Init.height = 0.0583F;
      this.btnLEFTARROW1Init.ButtonWidth = 2.56F;
      this.btnLEFTARROW1Init.ButtonHeight = 2.2857F;
      var3.Init(super.m_Module, this, this.btnLEFTARROW1, this.btnLEFTARROW1Init);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnRIGHTARROW1 = this.Add(var3);
      this.btnRIGHTARROW1Init.xpos = 0.159375F;
      this.btnRIGHTARROW1Init.ypos = 0.79375F;
      this.btnRIGHTARROW1Init.width = 0.1F;
      this.btnRIGHTARROW1Init.height = 0.05833F;
      this.btnRIGHTARROW1Init.ButtonWidth = 2.0F;
      this.btnRIGHTARROW1Init.ButtonHeight = 2.2857F;
      var3.Init(super.m_Module, this, this.btnRIGHTARROW1, this.btnRIGHTARROW1Init);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnFIRE12 = this.Add(var3);
      this.btnFIRE12Init.xpos = 0.81875F;
      this.btnFIRE12Init.ypos = 0.24583334F;
      this.btnFIRE12Init.width = 0.08437F;
      this.btnFIRE12Init.height = 0.05833F;
      this.btnFIRE12Init.ButtonWidth = 1.1852F;
      this.btnFIRE12Init.ButtonHeight = 2.2857F;
      var3.Init(super.m_Module, this, this.btnFIRE12, this.btnFIRE12Init);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnUPARROW2 = this.Add(var3);
      this.btnUPARROW2Init.xpos = 0.78125F;
      this.btnUPARROW2Init.ypos = 0.38541666F;
      this.btnUPARROW2Init.width = 0.09375F;
      this.btnUPARROW2Init.height = 0.05F;
      this.btnUPARROW2Init.ButtonWidth = 1.14285F;
      this.btnUPARROW2Init.ButtonHeight = 2.6667F;
      var3.Init(super.m_Module, this, this.btnUPARROW2, this.btnUPARROW2Init);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnDOWNARROW2 = this.Add(var3);
      this.btnDOWNARROW2Init.xpos = 0.7890625F;
      this.btnDOWNARROW2Init.ypos = 0.5104167F;
      this.btnDOWNARROW2Init.width = 0.09687F;
      this.btnDOWNARROW2Init.height = 0.05F;
      this.btnDOWNARROW2Init.ButtonWidth = 2.0645F;
      this.btnDOWNARROW2Init.ButtonHeight = 2.6667F;
      var3.Init(super.m_Module, this, this.btnDOWNARROW2, this.btnDOWNARROW2Init);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnLEFTARROW2 = this.Add(var3);
      this.btnLEFTARROW2Init.xpos = 0.8046875F;
      this.btnLEFTARROW2Init.ypos = 0.65416664F;
      // [mod] l'original réécrivait ici la taille du bouton du joueur 1 (copier-coller) : celui du joueur 2
      // gardait sa taille par défaut
      this.btnLEFTARROW2Init.width = 0.0781F;
      this.btnLEFTARROW2Init.height = 0.0583F;
      this.btnLEFTARROW2Init.ButtonWidth = 2.56F;
      this.btnLEFTARROW2Init.ButtonHeight = 2.2857F;
      var3.Init(super.m_Module, this, this.btnLEFTARROW2, this.btnLEFTARROW2Init);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnRIGHTARROW2 = this.Add(var3);
      this.btnRIGHTARROW2Init.xpos = 0.8265625F;
      this.btnRIGHTARROW2Init.ypos = 0.79583335F;
      this.btnRIGHTARROW2Init.width = 0.1F;
      this.btnRIGHTARROW2Init.height = 0.05833F;
      this.btnRIGHTARROW2Init.ButtonWidth = 2.0F;
      this.btnRIGHTARROW2Init.ButtonHeight = 2.2857F;
      var3.Init(super.m_Module, this, this.btnRIGHTARROW2, this.btnRIGHTARROW2Init);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExit = this.Add(var3);
      this.btnExitInit.bMoveButton = true;
      this.btnExitInit.xpos = 0.5F;
      this.btnExitInit.ypos = 0.908334F;
      this.btnExitInit.width = 0.25F;
      this.btnExitInit.height = 0.133334F;
      this.btnExitInit.ButtonWidth = 1.6F;
      this.btnExitInit.ButtonHeight = 1.0F;
      var3.Init(super.m_Module, this, this.btnExit, this.btnExitInit);
      var3.ProcessMouse(true);
      KidsSlider var4 = (KidsSlider)this.CreateObject("KidsSlider");
      this.sldKBJOYPAD1 = this.Add(var4);
      this.sldKBJOYPAD1Init.xpos = 0.3375F;
      this.sldKBJOYPAD1Init.ypos = 0.06667F;
      this.sldKBJOYPAD1Init.width = 0.3F;
      this.sldKBJOYPAD1Init.height = 0.133334F;
      this.sldKBJOYPAD1Init.sliderwidth = 0.29629F;
      this.sldKBJOYPAD1Init.sliderheight = 1.0F;
      this.sldKBJOYPAD1Init.slidery = 0.5F;
      var4.Init(super.m_Module, this, this.sldKBJOYPAD1, this.sldKBJOYPAD1Init);
      var4.ProcessMouse(true);
      var4.SetDiscretisation(3);
      var4.SetBorders(0.1F, 0.9F);
      var4 = (KidsSlider)this.CreateObject("KidsSlider");
      this.sldKBJOYPAD2 = this.Add(var4);
      this.sldKBJOYPAD2Init.xpos = 0.659375F;
      this.sldKBJOYPAD2Init.ypos = 0.06667F;
      this.sldKBJOYPAD2Init.width = 0.3F;
      this.sldKBJOYPAD2Init.height = 0.133334F;
      this.sldKBJOYPAD2Init.sliderwidth = 0.29629F;
      this.sldKBJOYPAD2Init.sliderheight = 1.0F;
      this.sldKBJOYPAD2Init.slidery = 0.5F;
      var4.Init(super.m_Module, this, this.sldKBJOYPAD2, this.sldKBJOYPAD2Init);
      var4.ProcessMouse(true);
      var4.SetDiscretisation(3);
      var4.SetBorders(0.1F, 0.9F);
      KidsText var5 = (KidsText)this.CreateObject("KidsText");
      this.ktxUp1 = this.Add(var5);
      var5.Init(super.m_Module, this.ktxUp1);
      var5.SetSizeAndPos(0.12F, 0.07F, 0.33125F, 0.38333333F, true);
      var5.Show();
      var5 = (KidsText)this.CreateObject("KidsText");
      this.ktxDown1 = this.Add(var5);
      var5.Init(super.m_Module, this.ktxDown1);
      var5.SetSizeAndPos(0.12F, 0.07F, 0.33125F, 0.50208336F, true);
      var5.Show();
      var5 = (KidsText)this.CreateObject("KidsText");
      this.ktxLeft1 = this.Add(var5);
      var5.Init(super.m_Module, this.ktxLeft1);
      var5.SetSizeAndPos(0.12F, 0.07F, 0.3203125F, 0.6375F, true);
      var5.Show();
      var5 = (KidsText)this.CreateObject("KidsText");
      this.ktxRight1 = this.Add(var5);
      var5.Init(super.m_Module, this.ktxRight1);
      var5.SetSizeAndPos(0.12F, 0.07F, 0.303125F, 0.775F, true);
      var5.Show();
      var5 = (KidsText)this.CreateObject("KidsText");
      this.ktxFire11 = this.Add(var5);
      var5.Init(super.m_Module, this.ktxFire11);
      var5.SetSizeAndPos(0.12F, 0.07F, 0.3125F, 0.25833333F, true);
      var5.Show();
      var5 = (KidsText)this.CreateObject("KidsText");
      this.ktxUp2 = this.Add(var5);
      var5.Init(super.m_Module, this.ktxUp2);
      var5.SetSizeAndPos(0.12F, 0.07F, 0.66875F, 0.38333333F, true);
      var5.Show();
      var5 = (KidsText)this.CreateObject("KidsText");
      this.ktxDown2 = this.Add(var5);
      var5.Init(super.m_Module, this.ktxDown2);
      var5.SetSizeAndPos(0.12F, 0.07F, 0.671875F, 0.50208336F, true);
      var5.Show();
      var5 = (KidsText)this.CreateObject("KidsText");
      this.ktxLeft2 = this.Add(var5);
      var5.Init(super.m_Module, this.ktxLeft2);
      var5.SetSizeAndPos(0.12F, 0.07F, 0.6796875F, 0.63958335F, true);
      var5.Show();
      var5 = (KidsText)this.CreateObject("KidsText");
      this.ktxRight2 = this.Add(var5);
      var5.Init(super.m_Module, this.ktxRight2);
      var5.SetSizeAndPos(0.12F, 0.07F, 0.6953125F, 0.775F, true);
      var5.Show();
      var5 = (KidsText)this.CreateObject("KidsText");
      this.ktxFire12 = this.Add(var5);
      var5.Init(super.m_Module, this.ktxFire12);
      var5.SetSizeAndPos(0.12F, 0.07F, 0.6875F, 0.25833333F, true);
      var5.Show();
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.ProcessMouse(true);
      this.ProcessKb(true);
      super.m_Ctrl = new ControlWindowList(20);
      super.m_Ctrl.Push(super.m_List.WindowAt(this.sldKBJOYPAD1));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnFIRE11));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnUPARROW1));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnDOWNARROW1));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnLEFTARROW1));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnRIGHTARROW1));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.sldKBJOYPAD2));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnFIRE12));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnUPARROW2));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnDOWNARROW2));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnLEFTARROW2));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnRIGHTARROW2));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExit));
      this.SetAlphaForAll(1.0F);
   }

   void RefreshControls() {
      KbSettings var1 = RacerGame.GetKb0();
      KbSettings var2 = RacerGame.GetKb1();
      KidsText var3 = (KidsText)super.m_List.WindowAt(this.ktxUp1);
      if (this.m_Pressed == 0) {
         var3.ColorRed();
      } else {
         var3.ColorWhite();
      }

      if (var1.m_Device == 0) {
         var3.SetText(KbSettings.GetKeyString(var1.GetUp()));
      } else {
         var3.SetText(" ");
      }

      var3 = (KidsText)super.m_List.WindowAt(this.ktxDown1);
      if (this.m_Pressed == 1) {
         var3.ColorRed();
      } else {
         var3.ColorWhite();
      }

      if (var1.m_Device == 0) {
         var3.SetText(KbSettings.GetKeyString(var1.GetDown()));
      } else {
         var3.SetText(" ");
      }

      var3 = (KidsText)super.m_List.WindowAt(this.ktxLeft1);
      if (this.m_Pressed == 2) {
         var3.ColorRed();
      } else {
         var3.ColorWhite();
      }

      if (var1.m_Device == 0) {
         var3.SetText(KbSettings.GetKeyString(var1.GetLeft()));
      } else {
         var3.SetText(" ");
      }

      var3 = (KidsText)super.m_List.WindowAt(this.ktxRight1);
      if (this.m_Pressed == 3) {
         var3.ColorRed();
      } else {
         var3.ColorWhite();
      }

      if (var1.m_Device == 0) {
         var3.SetText(KbSettings.GetKeyString(var1.GetRight()));
      } else {
         var3.SetText(" ");
      }

      var3 = (KidsText)super.m_List.WindowAt(this.ktxFire11);
      if (this.m_Pressed == 4) {
         var3.ColorRed();
      } else {
         var3.ColorWhite();
      }

      if (var1.m_Device == 0) {
         var3.SetText(KbSettings.GetKeyString(var1.GetFire1()));
      } else {
         var3.SetText(KbSettings.GetJoyButtonString(var1.GetFire1Joy(), var1.m_JoyNumber, var1.m_Device == 2));
      }

      var3 = (KidsText)super.m_List.WindowAt(this.ktxUp2);
      if (this.m_Pressed == 6) {
         var3.ColorRed();
      } else {
         var3.ColorWhite();
      }

      if (var2.m_Device == 0) {
         var3.SetText(KbSettings.GetKeyString(var2.GetUp()));
      } else {
         var3.SetText(" ");
      }

      var3 = (KidsText)super.m_List.WindowAt(this.ktxDown2);
      if (this.m_Pressed == 7) {
         var3.ColorRed();
      } else {
         var3.ColorWhite();
      }

      if (var2.m_Device == 0) {
         var3.SetText(KbSettings.GetKeyString(var2.GetDown()));
      } else {
         var3.SetText(" ");
      }

      var3 = (KidsText)super.m_List.WindowAt(this.ktxLeft2);
      if (this.m_Pressed == 8) {
         var3.ColorRed();
      } else {
         var3.ColorWhite();
      }

      if (var2.m_Device == 0) {
         var3.SetText(KbSettings.GetKeyString(var2.GetLeft()));
      } else {
         var3.SetText(" ");
      }

      var3 = (KidsText)super.m_List.WindowAt(this.ktxRight2);
      if (this.m_Pressed == 9) {
         var3.ColorRed();
      } else {
         var3.ColorWhite();
      }

      if (var2.m_Device == 0) {
         var3.SetText(KbSettings.GetKeyString(var2.GetRight()));
      } else {
         var3.SetText(" ");
      }

      var3 = (KidsText)super.m_List.WindowAt(this.ktxFire12);
      if (this.m_Pressed == 10) {
         var3.ColorRed();
      } else {
         var3.ColorWhite();
      }

      if (var2.m_Device == 0) {
         var3.SetText(KbSettings.GetKeyString(var2.GetFire1()));
      } else {
         var3.SetText(KbSettings.GetJoyButtonString(var2.GetFire1Joy(), var2.m_JoyNumber, var2.m_Device == 2));
      }

      KidsSlider var4 = (KidsSlider)super.m_List.WindowAt(this.sldKBJOYPAD1);
      var4.SetValueDiscrete(var1.m_Device);
      var4.RefreshPos();
      var4 = (KidsSlider)super.m_List.WindowAt(this.sldKBJOYPAD2);
      var4.SetValueDiscrete(2 - var2.m_Device);
      var4.RefreshPos();
   }
}
