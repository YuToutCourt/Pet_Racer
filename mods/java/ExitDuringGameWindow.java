public class ExitDuringGameWindow extends Window {
   float _WINDOW_SIZE_X = 0.5F;
   float _WINDOW_SIZE_Y = 0.5F;
   float _BUTTON_SIZE_X = 0.1F;
   float _BUTTON_SIZE_Y = 0.1F;
   float _YES_POS_X = 0.25F;
   float _YES_POS_Y = 0.8F;
   float _NO_POS_X = 0.75F;
   float _NO_POS_Y = 0.8F;
   String m_BlackTexName = "Data/Textures/Menu/blacktxt.png";
   int m_iBlackTexNum = 0;
   String m_TexName = "Data/Textures/Menu/Quit.png";
   int m_iTexNum = 0;
   Button m_YesBtn = null;
   Button m_NoBtn = null;
   Window m_QuitWindow = null;
   KidsDuringGameCursor m_Cursor = null;

   void OnButton(Button var1) {
      super.OnButton(var1);
      if (var1 == this.m_YesBtn) {
         this.HideExitWnd();
         RacerGame.game.EndRaceAfterEsc();
      }

      if (var1 == this.m_NoBtn) {
         this.HideExitWnd();
      }
   }

   // [mod] en ligne (ModNet) : la fenêtre « Quitter ? » ne fige plus le temps, la course continue chez les autres
   void ShowExitWnd() {
      this.UpdateWindowSizePos();
      this.Show();
      this.m_Cursor.ActivateCursor();
      if (!ModNet.IsRaceOnline()) {
         this.GetModule().TimerSetSpeedUp(1.0E-5F);
      }
   }

   void HideExitWnd() {
      this.m_Cursor.DeactivateCursor();
      this.Hide();
      if (!ModNet.IsRaceOnline()) {
         this.GetModule().TimerSetSpeedUp(ModTraining.TimeScale()); // [mod] entraînement des IA : temps accéléré
      }
   }

   void UpdateWindowSizePos() {
      float var1 = GameObject.GetScreenWidth();
      float var2 = GameObject.GetScreenHeight();
      float var3 = var1 * this._WINDOW_SIZE_X;
      float var4 = var2 * this._WINDOW_SIZE_Y;
      float var5 = (var1 - var3) / 2.0F;
      float var6 = (var2 - var4) / 2.0F;
      this.SetPos(new Vector(0.0F, 0.0F, 0.0F));
      this.SetSize(new Vector(var1, var2, 0.0F));
      this.m_QuitWindow.SetRelativePos(new Vector(var5, var6, 0.0F));
      this.m_QuitWindow.SetSize(new Vector(var3, var4, 0.0F));
      this.m_YesBtn
         .SetRelativePos(
            new Vector(this._YES_POS_X * var3 - this._BUTTON_SIZE_X * var1 / 2.0F, this._YES_POS_Y * var4 - this._BUTTON_SIZE_Y * var2 / 2.0F, 0.0F)
         );
      this.m_YesBtn.SetSize(new Vector(this._BUTTON_SIZE_X * var1, this._BUTTON_SIZE_Y * var2, 0.0F));
      this.m_NoBtn
         .SetRelativePos(new Vector(this._NO_POS_X * var3 - this._BUTTON_SIZE_X * var1 / 2.0F, this._NO_POS_Y * var4 - this._BUTTON_SIZE_Y * var2 / 2.0F, 0.0F));
      this.m_NoBtn.SetSize(new Vector(this._BUTTON_SIZE_X * var1, this._BUTTON_SIZE_Y * var2, 0.0F));
   }

   void OnCreate() {
      super.OnCreate();
      super.module = this.GetModule();
      this.ProcessMouse(true);
      float var1 = GameObject.GetScreenWidth();
      float var2 = GameObject.GetScreenHeight();
      float var3 = var1 * this._WINDOW_SIZE_X;
      float var4 = var2 * this._WINDOW_SIZE_Y;
      float var5 = (var1 - var3) / 2.0F;
      float var6 = (var2 - var4) / 2.0F;
      this.m_iBlackTexNum = this.AddTexture(this.m_BlackTexName, true, true);
      this.SetTexture(this.m_iBlackTexNum, new Rect(0.0F, 0.0F, 1.0F, 1.0F));
      this.SetPos(new Vector(0.0F, 0.0F, 0.0F));
      this.SetSize(new Vector(var1, var2, 0.0F));
      this.SetAlpha(0.2F);
      this.m_QuitWindow = (Window)this.CreateObject("Window");
      this.m_iTexNum = this.AddTexture(this.m_TexName, true, true);
      this.m_QuitWindow.SetTexture(this.m_iTexNum, new Rect(0.0F, 0.0F, 1.0F, 1.0F));
      this.AddChild(this.m_QuitWindow);
      this.AddChildWnd(this.m_QuitWindow, this);
      this.m_QuitWindow.SetRelativePos(new Vector(var5, var6, 0.0F));
      this.m_QuitWindow.SetSize(new Vector(var3, var4, 0.0F));
      this.m_QuitWindow.ProcessMouse(true);
      this.m_QuitWindow.Show();
      this.m_YesBtn = (Button)this.CreateObject("Button");
      this.m_QuitWindow.AddChild(this.m_YesBtn);
      this.m_QuitWindow.AddChildWnd(this.m_YesBtn, this);
      this.m_YesBtn
         .Init(
            new UIElement(
               0,
               0,
               new Vector(this._YES_POS_X * var3 - this._BUTTON_SIZE_X * var1 / 2.0F, this._YES_POS_Y * var4 - this._BUTTON_SIZE_Y * var2 / 2.0F, 0.0F),
               new Vector(this._BUTTON_SIZE_X * var1, this._BUTTON_SIZE_Y * var2, 0.0F),
               "Data/Textures/Menu/a/OKMenuQuit.png",
               new Rect(0.0F, 0.0F, 1.0F, 1.0F),
               "Data/Textures/Menu/a/OKMenuQuitPods.png",
               new Rect(0.0F, 0.0F, 1.0F, 1.0F),
               "",
               new Rect(),
               "",
               new Rect(),
               1,
               ""
            ),
            super.module
         );
      this.m_YesBtn.ProcessMouse(true);
      this.m_NoBtn = (Button)this.CreateObject("Button");
      this.m_QuitWindow.AddChild(this.m_NoBtn);
      this.m_QuitWindow.AddChildWnd(this.m_NoBtn, this);
      this.m_NoBtn
         .Init(
            new UIElement(
               1,
               0,
               new Vector(this._NO_POS_X * var3 - this._BUTTON_SIZE_X * var1 / 2.0F, this._NO_POS_Y * var4 - this._BUTTON_SIZE_Y * var2 / 2.0F, 0.0F),
               new Vector(this._BUTTON_SIZE_X * var1, this._BUTTON_SIZE_Y * var2, 0.0F),
               "Data/Textures/Menu/a/NoMenuQuit.png",
               new Rect(0.0F, 0.0F, 1.0F, 1.0F),
               "Data/Textures/Menu/a/NoMenuQuitPods.png",
               new Rect(0.0F, 0.0F, 1.0F, 1.0F),
               "",
               new Rect(),
               "",
               new Rect(),
               1,
               ""
            ),
            super.module
         );
      this.m_NoBtn.ProcessMouse(true);
      this.m_Cursor = (KidsDuringGameCursor)this.CreateObject("KidsDuringGameCursor");
      this.m_Cursor.Init(this.GetModule());
      this.m_Cursor.SetCursor(0);
      this.m_Cursor.DeactivateCursor();
   }
}
