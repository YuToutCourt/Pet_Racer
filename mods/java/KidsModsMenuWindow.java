// [mod] Écran « Mods » (chantier 3, notes/mods_menu.md), ouvert par le bouton engrenage du menu principal.
// Une ligne par réglage : « libellé   < valeur > ». Clavier : haut / bas choisit la ligne, gauche / droite change la
// valeur, Entrée = OK, Échap = Retour. Souris : flèches, OK, Retour.
// Les valeurs ne sont appliquées qu'à OK (enregistrées dans mods.dat par ModOptions) ; Retour ne garde rien.
// Textes : jetons « &Mods…& » de Data\Texts.scr (mods/art/make_texts.py), traduits par le moteur.
public class KidsModsMenuWindow extends KidsMenuWindow {
   static final String _TEX_BACK = "Data/Textures/Menu/b/PlanszaNiebieska.png";
   static final String _TEX_PANEL = "Data/Textures/Menu/b/ModsPanel.png";
   static final String _TEX_ARROWS = "Data/Textures/Menu/a/ModsArrows.png";
   static final String _TEX_ROW = "Data/Textures/Menu/a/ModsRow.png";

   static final int _ROW_ANIMALS = 0;
   static final int _ROW_TRACKS = 1;
   static final int _ROW_EXTRAS = 2;
   static final int _ROW_FPS = 3;
   static final int _ROW_LIMIT = 4;
   static final int _ROW_CATCHUP = 5;
   static final int _ROW_AILEVEL = 6;
   static final int _ROW_SPEED = 7;
   static final int _ROW_INFINITE = 8;
   static final int _ROW_BONUS = 9;
   static final int _ROW_SHIELD = 10;
   static final int _ROW_ADVENTURE = 11;
   static final int _ROWS = 12;
   static final String[] _LABELS = new String[]{
      "&ModsAllAnimals&",
      "&ModsAllTracks&",
      "&ModsAllExtras&",
      "&ModsShowFps&",
      "&ModsFpsLimit&",
      "&ModsCatchUp&",
      "&ModsAiLevel&",
      "&ModsSpeed&",
      "&ModsInfiniteBonus&",
      "&ModsForcedBonus&",
      "&ModsShield&",
      "&ModsAdventure&"
   };
   // groupes (déblocages / affichage et course / triches / aventure) : un petit écart les sépare
   static final int[] _GROUP = new int[]{0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3};
   static final int _ADVENTURE_STAGES = 33; // codes d'origine L11…L63

   // mise en page, en fractions de l'écran 4:3 (640x480 : 1 ligne = 24 px)
   static final float _PANEL_W = 0.84F;
   static final float _PANEL_H = 0.84F;
   static final float _PANEL_Y = 0.0167F;              // bord haut
   static final float _PANEL_TEX_V = 365.0F / 512.0F;  // image utile de ModsPanel.png (make_mods_menu.py)
   static final float _ROWS_Y = 0.158F;                // haut de la première ligne (sous le titre peint)
   static final float _ROW_H = 0.048F;                 // 12 lignes (0,05 jusqu'à 11 : difficulté des IA ajoutée)
   static final float _GROUP_GAP = 0.0167F;
   static final float _LABEL_X = 0.12F;
   static final float _LABEL_W = 0.42F;
   static final float _LEFT_X = 0.585F;
   static final float _VALUE_X = 0.72F;
   static final float _VALUE_W = 0.2F;
   static final float _RIGHT_X = 0.855F;
   static final float _ARROW_W = 0.06F;
   static final float _ARROW_H = 0.055F;

   int bkTex0 = -1;
   int wndPanel = -1;
   int wndRow = -1;
   int[] ktxLabel = new int[_ROWS];
   int[] ktxValue = new int[_ROWS];
   int[] btnLeft = new int[_ROWS];
   int[] btnRight = new int[_ROWS];
   int ktxStatus = -1;
   int btnOK = -1;
   InitKidsButton btnOKInit = new InitKidsButton(40);
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(57);

   // La liste d'origine de KidsMenuWindow tient 50 fenêtres. Avec 4 fenêtres par ligne (libellé, valeur, 2 flèches),
   // la 12e ligne (Difficulté des IA) l'a remplie : le texte d'état recevait l'id -1, Init plantait et tout le
   // KidsSimpleMenuModule.OnCreate s'arrêtait (écran du choix d'animal resté affiché, CarsManager jamais créé).
   // Taille calculée d'après _ROWS pour que la prochaine ligne ne refasse pas le coup.
   KidsModsMenuWindow() {
      super.m_List = new WindowsList(_ROWS * 4 + 16);
   }

   // valeurs en cours d'édition : 0/1 pour les cases, index dans la liste pour limite et vitesse (-1 = valeur
   // hors liste lue dans mods.dat, gardée telle quelle), identifiant du bonus (-1 = tirage normal),
   // étape d'aventure (-1 = ne rien changer)
   int[] m_Values = new int[_ROWS];
   int m_nCustomFps = 0;
   int m_nCustomSpeed = 100;
   int m_Sel = 0;

   static float RowCenter(int i) {
      return _ROWS_Y + _ROW_H * i + _GROUP_GAP * _GROUP[i] + _ROW_H / 2.0F;
   }

   static float StatusCenter() {
      return RowCenter(_ROWS - 1) + _ROW_H * 1.2F;
   }

   // Ordre d'affichage : panneau (dessous) < barre de sélection < textes < boutons. Les enfants ajoutés après le
   // panneau passent quand même dessous : comme KidsControlsMenuWindow, on remonte tout à la main.
   void ButtonsOnTop() {
      super.m_List.SetOnTop(this.wndRow);
      for (int i = 0; i < _ROWS; i++) {
         super.m_List.SetOnTop(this.ktxLabel[i]);
         super.m_List.SetOnTop(this.ktxValue[i]);
      }

      super.m_List.SetOnTop(this.ktxStatus);
      for (int i = 0; i < _ROWS; i++) {
         super.m_List.SetOnTop(this.btnLeft[i]);
         super.m_List.SetOnTop(this.btnRight[i]);
      }

      super.m_List.SetOnTop(this.btnOK);
      super.m_List.SetOnTop(this.btnExit);
   }

   // --- valeurs ---

   int GetMin(int nRow) {
      switch (nRow) {
         case _ROW_LIMIT:
            return this.m_nCustomFps != 0 ? -1 : 0;
         case _ROW_SPEED:
            return this.m_nCustomSpeed != 0 ? -1 : 0;
         case _ROW_BONUS:
         case _ROW_ADVENTURE:
            return -1;
         default:
            return 0;
      }
   }

   int GetMax(int nRow) {
      switch (nRow) {
         case _ROW_LIMIT:
            return ModOptions._FPS_LIMITS.length - 1;
         case _ROW_SPEED:
            return ModOptions._SPEEDS.length - 1;
         case _ROW_BONUS:
            return ModOptions._BONUS_MAX;
         case _ROW_ADVENTURE:
            return _ADVENTURE_STAGES - 1;
         case _ROW_AILEVEL:
            return ModOptions._AI_MAX;
         default:
            return 1;
      }
   }

   void Change(int nRow, int nDelta) {
      int nMin = this.GetMin(nRow);
      int nMax = this.GetMax(nRow);
      int n = this.m_Values[nRow] + nDelta;
      // la liste tourne en boucle
      if (n > nMax) {
         n = nMin;
      } else if (n < nMin) {
         n = nMax;
      }

      this.m_Values[nRow] = n;
      this.RefreshRow(nRow);
      this.RefreshStatus();
   }

   String GetValueText(int nRow) {
      int n = this.m_Values[nRow];
      switch (nRow) {
         case _ROW_LIMIT:
            int nFps = n >= 0 ? ModOptions._FPS_LIMITS[n] : this.m_nCustomFps;
            return nFps > 0 ? nFps + " i/s" : "&ModsNoLimit&";
         case _ROW_SPEED:
            return (n >= 0 ? ModOptions._SPEEDS[n] : this.m_nCustomSpeed) + " %";
         case _ROW_AILEVEL:
            return "&ModsAi" + n + "&";
         case _ROW_BONUS:
            return n < 0 ? "&ModsBonusRandom&" : "&ModsBonus" + n + "&";
         case _ROW_ADVENTURE:
            if (n < 0) {
               return "&ModsAdventureNone&";
            }

            // même numérotation que les codes d'origine : L11…L16, L21…, L61…L63 ; la 6e course de chaque monde
            // et les trois du dernier sont des duels contre un boss
            int nLand = n / 6;
            int nRace = n % 6;
            return (nLand + 1) + "-" + (nRace + 1) + (nRace == 5 || nLand == 5 ? " (boss)" : "");
         default:
            return n != 0 ? "&ModsYes&" : "&ModsNo&";
      }
   }

   // la valeur de cette ligne diffère du jeu d'origine en trichant
   boolean IsCheatValue(int nRow) {
      int n = this.m_Values[nRow];
      switch (nRow) {
         case _ROW_SPEED:
            return (n >= 0 ? ModOptions._SPEEDS[n] : this.m_nCustomSpeed) != 100;
         case _ROW_INFINITE:
         case _ROW_SHIELD:
            return n != 0;
         case _ROW_BONUS:
            return n != ModOptions._BONUS_RANDOM;
         default:
            return false;
      }
   }

   boolean IsCheat() {
      return this.IsCheatValue(_ROW_SPEED) || this.IsCheatValue(_ROW_INFINITE) || this.IsCheatValue(_ROW_BONUS) || this.IsCheatValue(_ROW_SHIELD);
   }

   void LoadValues() {
      this.m_Values[_ROW_ANIMALS] = ModOptions.B(ModOptions.m_bAllAnimals);
      this.m_Values[_ROW_TRACKS] = ModOptions.B(ModOptions.m_bAllTracks);
      this.m_Values[_ROW_EXTRAS] = ModOptions.B(ModOptions.m_bAllExtras);
      this.m_Values[_ROW_FPS] = ModOptions.B(ModOptions.m_bShowFps);
      this.m_Values[_ROW_LIMIT] = ModOptions.Find(ModOptions._FPS_LIMITS, ModOptions.m_nFpsLimit);
      this.m_nCustomFps = this.m_Values[_ROW_LIMIT] < 0 ? ModOptions.m_nFpsLimit : 0;
      this.m_Values[_ROW_CATCHUP] = ModOptions.B(ModOptions.m_bAiCatchUp);
      this.m_Values[_ROW_AILEVEL] = ModOptions.m_nAiMode;
      this.m_Values[_ROW_SPEED] = ModOptions.Find(ModOptions._SPEEDS, ModOptions.m_nSpeed);
      this.m_nCustomSpeed = this.m_Values[_ROW_SPEED] < 0 ? ModOptions.m_nSpeed : 0;
      this.m_Values[_ROW_INFINITE] = ModOptions.B(ModOptions.m_bInfiniteBonus);
      this.m_Values[_ROW_BONUS] = ModOptions.m_nForcedBonus;
      this.m_Values[_ROW_SHIELD] = ModOptions.B(ModOptions.m_bShield);
      this.m_Values[_ROW_ADVENTURE] = -1;
   }

   void OnOK() {
      ModOptions.m_bAllAnimals = this.m_Values[_ROW_ANIMALS] != 0;
      ModOptions.m_bAllTracks = this.m_Values[_ROW_TRACKS] != 0;
      ModOptions.m_bAllExtras = this.m_Values[_ROW_EXTRAS] != 0;
      ModOptions.m_bShowFps = this.m_Values[_ROW_FPS] != 0;
      int n = this.m_Values[_ROW_LIMIT];
      ModOptions.m_nFpsLimit = n >= 0 ? ModOptions._FPS_LIMITS[n] : this.m_nCustomFps;
      ModOptions.m_bAiCatchUp = this.m_Values[_ROW_CATCHUP] != 0;
      ModOptions.m_nAiMode = this.m_Values[_ROW_AILEVEL];
      n = this.m_Values[_ROW_SPEED];
      ModOptions.m_nSpeed = n >= 0 ? ModOptions._SPEEDS[n] : this.m_nCustomSpeed;
      ModOptions.m_bInfiniteBonus = this.m_Values[_ROW_INFINITE] != 0;
      ModOptions.m_nForcedBonus = this.m_Values[_ROW_BONUS];
      ModOptions.m_bShield = this.m_Values[_ROW_SHIELD] != 0;
      ModOptions.Save();
      n = this.m_Values[_ROW_ADVENTURE];
      if (n >= 0) {
         // comme les codes L11…L63 : aventure du profil en cours placée à cette étape, puis enregistrée
         RacerGame.GetCoreLogic().SetCheatAdventure(n);
         RacerGame.GetGSS().Save();
         RacerGame.game.PlayCS();
         GameObject.CrashLog("[mod] menu Mods : aventure placee a l'etape " + (n / 6 + 1) + "-" + (n % 6 + 1) + "\n");
      }
   }

   // --- affichage ---

   ModText TextAt(int id) {
      return (ModText)super.m_List.WindowAt(id);
   }

   void RefreshRow(int nRow) {
      // tout le texte de l'écran en blanc (demande du joueur) : la ligne choisie se voit à la barre bleue
      ModText t = this.TextAt(this.ktxLabel[nRow]);
      t.ColorWhite();
      t.SetText(_LABELS[nRow]);
      t = this.TextAt(this.ktxValue[nRow]);
      t.ColorWhite();
      t.SetText(this.GetValueText(nRow));
   }

   void RefreshStatus() {
      ModText t = this.TextAt(this.ktxStatus);
      t.ColorWhite();
      t.SetText(this.IsCheat() ? "&ModsCheatOn&" : "&ModsCheatOff&");
   }

   void Select(int nRow) {
      int nOld = this.m_Sel;
      this.m_Sel = (nRow + _ROWS) % _ROWS;
      if (nOld != this.m_Sel) {
         this.RefreshRow(nOld);
      }

      this.RefreshRow(this.m_Sel);
      KidsMenuWindow w = (KidsMenuWindow)super.m_List.WindowAt(this.wndRow);
      w.SetSizeAndPos(0.8F, _ROW_H * 1.15F, 0.5F, RowCenter(this.m_Sel), true);
   }

   void RefreshAll() {
      for (int i = 0; i < _ROWS; i++) {
         this.RefreshRow(i);
      }

      this.Select(this.m_Sel);
      this.RefreshStatus();
   }

   // --- entrées ---

   public void OnKeyReleased(int var1, char var2) {
      if (this.CanProcessMouseMove() && !super.m_Module.CanIgnoreKeys()) {
         if (var1 == 200) {
            super.m_Module.DeactivateCursor();
            this.Select(this.m_Sel - 1);
         } else if (var1 == 208) {
            super.m_Module.DeactivateCursor();
            this.Select(this.m_Sel + 1);
         } else if (var1 == 203) {
            super.m_Module.DeactivateCursor();
            this.Change(this.m_Sel, -1);
         } else if (var1 == 205) {
            super.m_Module.DeactivateCursor();
            this.Change(this.m_Sel, 1);
         } else if (var1 == 28 || var1 == 156) {
            this.ButtonPushed(this.btnOK);
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
         this.OnOK();
         this.DisactivateWindow();
         super.m_Module.ActivateMenuWindow(super.m_Module.PopMenu());
      } else {
         for (int i = 0; i < _ROWS; i++) {
            if (var1 == this.btnLeft[i] || var1 == this.btnRight[i]) {
               this.Select(i);
               this.Change(i, var1 == this.btnLeft[i] ? -1 : 1);
               return;
            }
         }
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
      if (!KidsMenuModule.m_CarsManager.IsInitialized()) {
         KidsMenuModule.m_CarsManager.ActivateCarsManager();
      }

      KidsMenuModule.m_CarsManager.Show(this);
      this.LoadValues();
      this.m_Sel = 0;
      this.RefreshAll();
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

   int AddText(float w, float h, float x, float y, boolean bCentered, boolean bLeft) {
      ModText t = (ModText)this.CreateObject("ModText");
      int id = this.Add(t);
      t.Init(super.m_Module, id);
      if (bLeft) {
         t.SetAlignLeft();
      }

      t.SetSizeAndPos(w, h, x, y, bCentered);
      t.Show();
      return id;
   }

   int AddArrow(int nRow, boolean bRight) {
      InitKidsButton init = new InitKidsButton();
      init.sBnTexDisabled = _TEX_ARROWS;
      init.sBnTexEnabled = _TEX_ARROWS;
      init.sBnTexHighlighted = _TEX_ARROWS;
      init.sBnTexPushed = _TEX_ARROWS;
      float u = bRight ? 0.5F : 0.0F;
      init.rBnTexDisabled = new Rect(u, 0.0F, u + 0.5F, 0.5F);
      init.rBnTexEnabled = new Rect(u, 0.0F, u + 0.5F, 0.5F);
      init.rBnTexHighlighted = new Rect(u, 0.5F, u + 0.5F, 1.0F);
      init.rBnTexPushed = new Rect(u, 0.5F, u + 0.5F, 1.0F);
      init.xpos = bRight ? _RIGHT_X : _LEFT_X;
      init.ypos = RowCenter(nRow);
      init.width = _ARROW_W;
      init.height = _ARROW_H;
      init.bMoveButton = false;
      KidsButton b = (KidsButton)this.CreateObject("KidsButton");
      int id = this.Add(b);
      b.Init(super.m_Module, this, id, init);
      b.ProcessMouse(true);
      return id;
   }

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(_TEX_BACK, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), _TEX_BACK);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      this.wndPanel = this.AddPicture(_TEX_PANEL, new Rect(0.0F, 0.0F, 1.0F, _PANEL_TEX_V), _PANEL_W, _PANEL_H, 0.5F, _PANEL_Y + _PANEL_H / 2.0F);
      this.wndRow = this.AddPicture(_TEX_ROW, new Rect(0.0F, 0.0F, 1.0F, 1.0F), 0.8F, _ROW_H * 1.15F, 0.5F, RowCenter(0));

      for (int i = 0; i < _ROWS; i++) {
         float y = RowCenter(i);
         this.ktxLabel[i] = this.AddText(_LABEL_W, _ROW_H, _LABEL_X, y - _ROW_H / 2.0F, false, true);
         this.ktxValue[i] = this.AddText(_VALUE_W, _ROW_H, _VALUE_X, y, true, false);
         this.btnLeft[i] = this.AddArrow(i, false);
         this.btnRight[i] = this.AddArrow(i, true);
      }

      this.ktxStatus = this.AddText(0.8F, _ROW_H, 0.5F, StatusCenter(), true, false);
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
      this.LoadValues();
      this.RefreshAll();
   }
}
