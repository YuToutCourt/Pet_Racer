// [mod] Classement du Grand Prix (chantier 6, notes/grandprix.md), affiché après le podium de chaque course de la
// coupe (KidsPlacesMenuWindow.EndWindow) ou après une course quittée par Échap (RacerGame.EndModule).
// Course jouée, tableau des 6 voitures (place, animal, points de la course, total), course suivante.
// Entrée / OK = course suivante (ou retour au menu Un joueur après la dernière). Échap / Retour = abandonner la
// coupe, à confirmer par un second appui.
// Textes : jetons « &GP…& », « &AnimalName…& » de Data\Texts.scr (mods/art/make_texts.py) ; chaque texte à l'écran
// est soit un jeton seul, soit du texte sans accent (on ne sait pas si le moteur traduit un jeton au milieu d'un texte).
public class KidsGrandPrixMenuWindow extends KidsMenuWindow {
   static final String _TEX_BACK = "Data/Textures/Menu/b/PlanszaNiebieska.png";
   static final String _TEX_PANEL = "Data/Textures/Menu/b/GPPanel.png";
   static final String _TEX_ROW = "Data/Textures/Menu/a/ModsRow.png";
   static final int _ROWS = 6;

   // mise en page, en fractions de l'écran 4:3 (même panneau que l'écran Mods)
   static final float _PANEL_W = 0.84F;
   static final float _PANEL_H = 0.84F;
   static final float _PANEL_Y = 0.0167F;
   static final float _PANEL_TEX_V = 365.0F / 512.0F;  // image utile de GPPanel.png (make_grandprix.py)
   static final float _TEXT_H = 0.05F;
   static final float _RACE_Y = 0.19F;
   static final float _HEAD_Y = 0.26F;
   static final float _ROWS_Y = 0.32F;
   static final float _ROW_H = 0.06F;
   static final float _FOOT_Y = 0.71F;
   static final float _STATUS_Y = 0.78F;
   static final float _LEFT_X = 0.12F;
   static final float _PLACE_X = 0.17F;
   static final float _ANIMAL_X = 0.25F;
   static final float _RACE_PTS_X = 0.64F;
   static final float _TOTAL_X = 0.79F;

   int bkTex0 = -1;
   int wndPanel = -1;
   int wndRow = -1;
   int ktxRace = -1;
   int ktxTrack = -1;
   int[] ktxHead = new int[4];
   int[] ktxPlace = new int[_ROWS];
   int[] ktxAnimal = new int[_ROWS];
   int[] ktxRacePts = new int[_ROWS];
   int[] ktxTotal = new int[_ROWS];
   int ktxFootLabel = -1;
   int ktxFootValue = -1;
   int ktxFinal = -1;
   int ktxStatusLabel = -1;
   int ktxStatusValue = -1;
   int ktxStatus = -1;
   int btnOK = -1;
   InitKidsButton btnOKInit = new InitKidsButton(40);
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(57);
   boolean m_bConfirmQuit = false;

   void ButtonsOnTop() {
      super.m_List.SetOnTop(this.wndRow);
      super.m_List.SetOnTop(this.ktxRace);
      super.m_List.SetOnTop(this.ktxTrack);

      for (int i = 0; i < this.ktxHead.length; i++) {
         super.m_List.SetOnTop(this.ktxHead[i]);
      }

      for (int i = 0; i < _ROWS; i++) {
         super.m_List.SetOnTop(this.ktxPlace[i]);
         super.m_List.SetOnTop(this.ktxAnimal[i]);
         super.m_List.SetOnTop(this.ktxRacePts[i]);
         super.m_List.SetOnTop(this.ktxTotal[i]);
      }

      super.m_List.SetOnTop(this.ktxFootLabel);
      super.m_List.SetOnTop(this.ktxFootValue);
      super.m_List.SetOnTop(this.ktxFinal);
      super.m_List.SetOnTop(this.ktxStatusLabel);
      super.m_List.SetOnTop(this.ktxStatusValue);
      super.m_List.SetOnTop(this.ktxStatus);
      super.m_List.SetOnTop(this.btnOK);
      super.m_List.SetOnTop(this.btnExit);
   }

   static float RowCenter(int i) {
      return _ROWS_Y + _ROW_H * i;
   }

   // --- affichage ---

   ModText TextAt(int id) {
      return (ModText)super.m_List.WindowAt(id);
   }

   void SetText(int id, String s) {
      if (s == null) {
         super.m_List.Hide(id);
         return;
      }

      super.m_List.Show(id);
      ModText t = this.TextAt(id);
      t.ColorWhite();
      t.SetText(s);
   }

   void Refresh() {
      int nRace = ModGrandPrix.m_iRace;
      boolean bOver = ModGrandPrix.IsOver();
      this.SetText(this.ktxRace, "Course " + (nRace + 1) + " / " + ModGrandPrix.m_nRaces);
      this.SetText(this.ktxTrack, "&GPTrack" + ModGrandPrix.m_aTracks[nRace] + "&");
      this.SetText(this.ktxHead[0], "&GPHeadPlace&");
      this.SetText(this.ktxHead[1], "&GPHeadAnimal&");
      this.SetText(this.ktxHead[2], "&GPHeadRace&");
      this.SetText(this.ktxHead[3], "&GPHeadTotal&");
      int[] t = ModGrandPrix.GetStandings();
      int nPlayerRow = -1;

      for (int i = 0; i < _ROWS; i++) {
         if (i >= t.length) {
            this.SetText(this.ktxPlace[i], null);
            this.SetText(this.ktxAnimal[i], null);
            this.SetText(this.ktxRacePts[i], null);
            this.SetText(this.ktxTotal[i], null);
            continue;
         }

         int s = t[i];
         if (s == 0) {
            nPlayerRow = i;
         }

         // ex aequo complet (points, victoires, place) : même place affichée
         int nPlace = i;

         while (nPlace > 0 && !ModGrandPrix.Better(t[nPlace - 1], s)) {
            nPlace--;
         }

         this.SetText(this.ktxPlace[i], String.valueOf(nPlace + 1));
         this.SetText(this.ktxAnimal[i], "&AnimalName" + ModGrandPrix.m_aAnimals[s] + "&");
         this.SetText(this.ktxRacePts[i], "+" + ModGrandPrix.m_aRacePts[s]);
         this.SetText(this.ktxTotal[i], String.valueOf(ModGrandPrix.m_aTotal[s]));
      }

      // barre bleue sur la ligne du joueur
      KidsMenuWindow w = (KidsMenuWindow)super.m_List.WindowAt(this.wndRow);
      if (nPlayerRow >= 0) {
         super.m_List.Show(this.wndRow);
         w.SetSizeAndPos(0.8F, _ROW_H * 1.05F, 0.5F, RowCenter(nPlayerRow), true);
      } else {
         super.m_List.Hide(this.wndRow);
      }

      if (bOver) {
         int nRank = ModGrandPrix.PlayerRank();
         this.SetText(this.ktxFootLabel, null);
         this.SetText(this.ktxFootValue, null);
         this.SetText(this.ktxFinal, "&GPFinal" + (nRank < 3 ? nRank : 3) + "&");
      } else {
         this.SetText(this.ktxFinal, null);
         this.SetText(this.ktxFootLabel, "&GPNext&");
         this.SetText(this.ktxFootValue, "&GPTrack" + ModGrandPrix.m_aTracks[nRace + 1] + "&");
      }

      this.RefreshStatus();
   }

   // ligne du bas : confirmation d'abandon, triche, course quittée, ou palmarès en fin de coupe
   void RefreshStatus() {
      String sCentered = null;
      String sLabel = null;
      String sValue = null;
      if (this.m_bConfirmQuit) {
         sCentered = "&GPQuitConfirm&";
      } else if (ModGrandPrix.m_bCheated) {
         sCentered = "&GPCheat&";
      } else if (ModGrandPrix.IsOver()) {
         sLabel = "&GPMedals&";
         sValue = ModGrandPrix.GetMedals(0) + " or,  " + ModGrandPrix.GetMedals(1) + " argent,  " + ModGrandPrix.GetMedals(2) + " bronze";
      } else if (ModGrandPrix.m_bAbandoned) {
         sCentered = "&GPAbandoned&";
      }

      this.SetText(this.ktxStatus, sCentered);
      this.SetText(this.ktxStatusLabel, sLabel);
      this.SetText(this.ktxStatusValue, sValue);
   }

   // --- entrées ---

   public void OnKeyReleased(int var1, char var2) {
      if (this.CanProcessMouseMove() && !super.m_Module.CanIgnoreKeys()) {
         if (var1 == 28 || var1 == 156) {
            this.ButtonPushed(this.btnOK);
         } else if (var1 == 1) {
            this.ButtonPushed(this.btnExit);
         } else if (var1 == 203) {
            super.m_Module.DeactivateCursor();
            super.m_Ctrl.Previous();
         } else if (var1 == 205) {
            super.m_Module.DeactivateCursor();
            super.m_Ctrl.Next();
         }
      }
   }

   public void ButtonPushed(int var1) {
      super.ButtonPushed(var1);
      if (var1 == this.btnOK) {
         if (this.m_bConfirmQuit) {
            // OK après un premier Retour : on ne quitte pas
            this.m_bConfirmQuit = false;
            this.RefreshStatus();
            return;
         }

         if (ModGrandPrix.NextRace()) {
            this.StartNextRace();
         } else {
            this.BackToMenu();
         }
      } else if (var1 == this.btnExit) {
         if (ModGrandPrix.IsOver() || this.m_bConfirmQuit) {
            if (!ModGrandPrix.IsOver()) {
               GameObject.CrashLog("[mod] grand prix : coupe abandonnee par le joueur\n");
            }

            this.BackToMenu();
         } else {
            this.m_bConfirmQuit = true;
            this.RefreshStatus();
         }
      }
   }

   void StartNextRace() {
      // comme KidsLapsMenuWindow (OK) : la course part sur CoreLogic.GetNextTrack(), réglé par ModGrandPrix.BeforeRace
      super.m_Module.ClearStack();
      ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, true);
      this.DisactivateWindow();
      super.m_Module.m_CurrentMenu = -1;
      RacerGame.game.RunModule();
   }

   void BackToMenu() {
      ModGrandPrix.Reset();
      this.DisactivateWindow();
      super.m_Module.ClearStack();
      super.m_Module.SetMenuCX(-1);
      super.m_Module.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
      super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._SinglePlayerMenuWindow);
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
      if (!KidsMenuModule.m_CarsManager.IsInitialized()) {
         KidsMenuModule.m_CarsManager.ActivateCarsManager();
      }

      KidsMenuModule.m_CarsManager.Show(this);
      this.m_bConfirmQuit = false;
      this.Refresh();
      this.ButtonsOnTop();
   }

   // --- construction ---

   int AddPicture(String sTex, Rect r, float w, float h, float x, float y) {
      KidsMenuWindow wnd = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      int id = this.Add(wnd);
      wnd.Init(super.m_Module, id);
      wnd.AddWindowTexture(wnd.AddTexture(sTex, true, true), r, sTex);
      wnd.SetWindowTexture(0);
      wnd.SetSizeAndPos(w, h, x, y, true);
      wnd.Show();
      return id;
   }

   // texte centré sur (x, y), ou aligné à gauche à partir de x
   int AddText(float w, float x, float y, boolean bLeft) {
      ModText t = (ModText)this.CreateObject("ModText");
      int id = this.Add(t);
      t.Init(super.m_Module, id);
      if (bLeft) {
         t.SetAlignLeft();
         t.SetSizeAndPos(w, _TEXT_H, x, y - _TEXT_H / 2.0F, false);
      } else {
         t.SetSizeAndPos(w, _TEXT_H, x, y, true);
      }

      t.Show();
      return id;
   }

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(_TEX_BACK, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), _TEX_BACK);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      this.wndPanel = this.AddPicture(_TEX_PANEL, new Rect(0.0F, 0.0F, 1.0F, _PANEL_TEX_V), _PANEL_W, _PANEL_H, 0.5F, _PANEL_Y + _PANEL_H / 2.0F);
      this.wndRow = this.AddPicture(_TEX_ROW, new Rect(0.0F, 0.0F, 1.0F, 1.0F), 0.8F, _ROW_H * 1.05F, 0.5F, RowCenter(0));
      this.ktxRace = this.AddText(0.3F, _LEFT_X, _RACE_Y, true);
      this.ktxTrack = this.AddText(0.45F, 0.42F, _RACE_Y, true);
      this.ktxHead[0] = this.AddText(0.12F, _PLACE_X, _HEAD_Y, false);
      this.ktxHead[1] = this.AddText(0.3F, _ANIMAL_X, _HEAD_Y, true);
      this.ktxHead[2] = this.AddText(0.16F, _RACE_PTS_X, _HEAD_Y, false);
      this.ktxHead[3] = this.AddText(0.16F, _TOTAL_X, _HEAD_Y, false);

      for (int i = 0; i < _ROWS; i++) {
         float y = RowCenter(i);
         this.ktxPlace[i] = this.AddText(0.12F, _PLACE_X, y, false);
         this.ktxAnimal[i] = this.AddText(0.3F, _ANIMAL_X, y, true);
         this.ktxRacePts[i] = this.AddText(0.16F, _RACE_PTS_X, y, false);
         this.ktxTotal[i] = this.AddText(0.16F, _TOTAL_X, y, false);
      }

      this.ktxFootLabel = this.AddText(0.35F, _LEFT_X, _FOOT_Y, true);
      this.ktxFootValue = this.AddText(0.4F, 0.48F, _FOOT_Y, true);
      this.ktxFinal = this.AddText(0.8F, 0.5F, _FOOT_Y, false);
      this.ktxStatusLabel = this.AddText(0.35F, _LEFT_X, _STATUS_Y, true);
      this.ktxStatusValue = this.AddText(0.4F, 0.48F, _STATUS_Y, true);
      this.ktxStatus = this.AddText(0.8F, 0.5F, _STATUS_Y, false);
      KidsButton var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnOK = this.Add(var3);
      this.btnOKInit.bMoveButton = true;
      this.btnOKInit.xpos = 0.62F;
      this.btnOKInit.ypos = 0.915F;
      this.btnOKInit.width = 0.12F;
      this.btnOKInit.height = 0.16F;
      var3.Init(super.m_Module, this, this.btnOK, this.btnOKInit);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExit = this.Add(var3);
      this.btnExitInit.bMoveButton = true;
      this.btnExitInit.xpos = 0.82F;
      this.btnExitInit.ypos = 0.915F;
      var3.Init(super.m_Module, this, this.btnExit, this.btnExitInit);
      var3.ProcessMouse(true);
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.SetAlphaForAll(1.0F);
      this.ProcessKb(true);
      super.m_Ctrl = new ControlWindowList(4);
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnOK));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExit));
   }
}
