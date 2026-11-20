public class KidsAnimalChoiceMenuWindow extends KidsMenuWindow {
   static final float _ROTATE_SPEED = 25.0F;
   static final float _SWAP_TIME = 1.0F;
   static final float _MIN_CAMERA_DIST = 225.0F;
   static final float _MAX_CAMERA_DIST = 500.0F;
   String m_TexName = "Data/Textures/Menu/b/MenuCharakter.png";
   static final float _TIME_BETWEEN_IDLES = 5.0F;
   static final float _MIN_TIME_BETWEEN_SPEACH = 4.0F;
   static final float _MAX_TIME_BETWEEN_SPEACH = 8.0F;
   float m_TimeofLastSpeechEnd = 0.0F;
   float m_TimeofNextSpeech = 0.0F;
   static final float _TIME_TO_FIRST_SPEACH = 1.5F;
   int bkTex0 = -1;
   int btnPrev = -1;
   InitKidsButton btnPrevInit = new InitKidsButton(43);
   int btnNext = -1;
   InitKidsButton btnNextInit = new InitKidsButton(44);
   int wnd12 = -1;
   static final float wnd12Width = 0.14F;
   static final float wnd12Height = 0.18F;
   static final float wnd12PosX = 0.1F;
   static final float wnd12PosY = 0.88F;
   static final String wnd12_tex1 = "Data/Textures/Menu/a/1MenuCharakter.png";
   static final String wnd12_tex2 = "Data/Textures/Menu/a/2MenuCharakter.png";
   int tex1 = -1;
   int tex2 = -1;
   int btnOK = -1;
   InitKidsButton btnOKInit = new InitKidsButton(45);
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(57);
   IntList m_AnimalList = null;
   int m_Choice = 0;
   float m_LastTime = -1.0F;
   float t = 0.0F;
   static final float _TIME_IDLE = 5.0F;
   float m_TimeToIdle = 5.0F;
   MenuAnimal m_AnimalForPlayingChooseSound = null;
   boolean m_bLockWindow = false;
   // [mod] chantier 4 (notes/animaux.md) : cadre en haut à droite, nom de l'animal et 3 barres (vitesse, accélération,
   // maniabilité) d'après ModAnimals (Data\animals.scr). Images : mods/art/make_animal_stats.py.
   // Mise en page en fractions de l'écran 4:3 ; entre parenthèses, les pixels en 640x480.
   static final String _TEX_STATS_PANEL = "Data/Textures/Menu/b/StatsPanel.png";
   static final String _TEX_STATS_TRACK = "Data/Textures/Menu/a/StatsTrack.png";
   static final String[] _TEX_STATS_FILL = new String[]{
      "Data/Textures/Menu/a/StatsSpeed.png", "Data/Textures/Menu/a/StatsAccel.png", "Data/Textures/Menu/a/StatsTurn.png"
   };
   static final String[] _STATS_LABELS = new String[]{"&StatSpeed&", "&StatAccel&", "&StatTurn&"};
   static final float _ST_X = 494.0F / 640.0F;         // cadre : bord gauche (494), entre le hublot 3D et le bord
   static final float _ST_Y = 6.0F / 480.0F;           // bord haut (6)
   static final float _ST_W = 140.0F / 640.0F;         // 140 px
   static final float _ST_H = 133.0F / 480.0F;         // 133 px : finit au-dessus de la flèche « suivant »
   static final float _ST_TEX_V = 243.0F / 256.0F;     // image utile de StatsPanel.png
   static final float _ST_PAD = 10.0F / 640.0F;        // marge intérieure gauche / droite (10)
   static final float _ST_NAME_Y = 12.0F / 480.0F;     // nom : 12..34
   static final float _ST_NAME_H = 22.0F / 480.0F;
   static final float _ST_ROW_Y = 36.0F / 480.0F;      // 1re ligne : libellé 36..52, barre 52..64
   static final float _ST_ROW_H = 33.0F / 480.0F;
   static final float _ST_LABEL_H = 16.0F / 480.0F;
   static final float _ST_BAR_H = 12.0F / 480.0F;
   static final float _ST_TRACK_V = 26.0F / 32.0F;     // image utile de StatsTrack.png
   static final float _ST_FILL_IN_X = 2.0F / 640.0F;   // remplissage : 2 px à l'intérieur du rail
   static final float _ST_FILL_IN_Y = 2.0F / 480.0F;
   int wndStatsPanel = -1;
   int ktxStatsName = -1;
   int[] ktxStatsLabel = new int[3];
   int[] wndStatsTrack = new int[3];
   int[] wndStatsFill = new int[3];
   Rect[] m_StatsFillRect = new Rect[3]; // gardés par la liste de textures : modifiés sur place (comme la barre de chargement)
   // [mod] chantier 8 : ouvert depuis l'écran « En ligne » (ligne « Ton animal ») : les 12 animaux, déblocage ou non ;
   // OK donne l'animal au salon (ModNet.SetMyAnimal), OK et Retour ramènent à l'écran « En ligne ».
   static boolean m_bOnline = false;

   boolean AllAnimals() {
      return m_bOnline || RacerGame.GetCoreLogic().GetAvailableAnimalsCount() == 12;
   }

   // retour à l'écran « En ligne » (empilé par KidsOnlineMenuWindow.OpenAnimalChoice)
   void BackToOnline() {
      m_bOnline = false;
      this.DisactivateWindow();
      super.m_Module.ActivateMenuWindow(super.m_Module.PopMenu());
   }

   public void StopAnimForAll() {
      int var2 = 0;

      do {
         MenuAnimal var1 = ((KidsSimpleMenuModule)super.m_Module).GetChoosenAnimal(var2);
         if (var1 != null) {
            var1.PlayAnim("stop", 1);
         }
      } while (++var2 < 12);
   }

   void ButtonsOnTop() {
      super.m_List.SetOnTop(this.btnPrev);
      super.m_List.SetOnTop(this.btnNext);
      super.m_List.SetOnTop(this.btnOK);
      super.m_List.SetOnTop(this.btnExit);
   }

   boolean CanChangeWindow() {
      return super.m_Module.m_Camera.m_ActionType == 1;
   }

   void ShowPodium(boolean var1) {
      MeshObject var2 = (MeshObject)GameObject.GetObjectByName("podium01");
      var2.EnableRendering(var1);
      var2 = (MeshObject)GameObject.GetObjectByName("flaga01");
      var2.EnableRendering(var1);
      var2 = (MeshObject)GameObject.GetObjectByName("flaga02");
      var2.EnableRendering(var1);
   }

   public void FirstAnim() {
      this.LaunchIdle(true);
      this.DisableCallMethod("Anim");
      this.DisableCallMethod("FirstAnim");
      this.CallMethodIn("Anim", 5.0F);
   }

   void SetButtonsState() {
      KidsButton var1 = null;
      CoreLogic var2 = RacerGame.GetCoreLogic();
      boolean var3 = this.AllAnimals(); // [mod] déblocage du menu Mods, ou écran « En ligne »
      var1 = (KidsButton)super.m_List.WindowAt(this.btnPrev);
      if (var1 != null) {
         var1.Enable(this.m_Choice > 0 || var3);
      }

      var1 = (KidsButton)super.m_List.WindowAt(this.btnNext);
      if (var1 != null && this.m_AnimalList != null) {
         var1.Enable(this.m_Choice < this.m_AnimalList.m_Cnt - 1 || var3);
      }

      super.m_Module.SetCursor(0);
   }

   public void OnKeyReleased(int var1, char var2) {
      if (this.CanProcessMouseMove() && !super.m_Module.CanIgnoreKeys() && !this.m_bLockWindow) {
         if (var1 == 28) {
            this.ButtonPushed(this.btnOK);
         } else if (var1 == 203) {
            this.ButtonPushed(this.btnPrev);
         } else if (var1 == 205) {
            this.ButtonPushed(this.btnNext);
         } else if (var1 == 1) {
            this.ButtonPushed(this.btnExit);
         }
      }
   }

   public void ButtonPushed(int var1) {
      this.SetButtonsState();
      if (!this.m_bLockWindow) {
         CoreLogic var2 = RacerGame.GetCoreLogic();
         if (var2 != null) {
            super.ButtonPushed(var1);
            if (var1 == this.btnExit && m_bOnline) {
               this.BackToOnline(); // [mod] chantier 8 : animal inchangé
            } else if (var1 == this.btnExit) {
               int var4 = super.m_Module.PopMenu();
               if (var4 == KidsSimpleMenuModule._AnimalChoiceMenuWindow) {
                  var2.SelectAnimal(1, -1);
                  this.Show();
               } else {
                  var2.SelectAnimal(-1, -1);
                  this.DisactivateWindow();
                  super.m_Module.ActivateMenuWindow(var4);
               }
            } else if (var1 == this.btnOK) {
               this.m_bLockWindow = true;
               this.DisableCallMethod("Anim");
               this.DisableCallMethod("FirstAnim");
               this.m_AnimalForPlayingChooseSound = this.GetAnimalAt(this.m_Choice);
               float var3 = 1.0F;
               if (this.m_AnimalForPlayingChooseSound != null) {
                  var3 = this.m_AnimalForPlayingChooseSound.GetChoosenSoundLen();
               }

               this.PlayChoosenSound(super.m_Module.m_Camera.GetPosition());
               this.DisableCallMethod("NextWindowAfterChoose");
               this.CallMethodIn("NextWindowAfterChoose", var3);
            } else if (var1 == this.btnPrev) {
               this.PreviousAnimal();
            } else if (var1 == this.btnNext) {
               this.NextAnimal();
            }
         }
      }
   }

   public void PlayChoosenSound(Vector var1) {
      if (this.m_AnimalForPlayingChooseSound != null) {
         this.m_AnimalForPlayingChooseSound.PlaySoundChoosen(var1);
         this.m_AnimalForPlayingChooseSound = null;
      }
   }

   void LaunchIdle(boolean var1) {
      MenuAnimal var2 = this.GetAnimalAt(this.m_Choice);
      if (var2 != null) {
         float var3 = this.GetTime();
         if (var1) {
            if (this.m_TimeofLastSpeechEnd < var3) {
               this.m_TimeofNextSpeech = var3 + RacerGame.GetRandom(4.0F, 8.0F);
               this.m_TimeofLastSpeechEnd = var3 + var2.LaunchChooseAnimalIdle(true);
               return;
            }
         } else if (this.m_TimeofNextSpeech < var3 && this.m_TimeofLastSpeechEnd < var3) {
            this.m_TimeofNextSpeech = var3 + RacerGame.GetRandom(4.0F, 8.0F);
            this.m_TimeofLastSpeechEnd = var3 + var2.LaunchChooseAnimalIdle(true);
            return;
         }

         var2.LaunchChooseAnimalIdle(false);
      }
   }

   public void NextWindowAfterChoose() {
      this.m_bLockWindow = false;
      this.DisableCallMethod("NextWindowAfterChoose");
      if (m_bOnline) {
         // [mod] chantier 8 : animal choisi pour la course en ligne
         ModNet.SetMyAnimal(this.m_AnimalList.GetAt(this.m_Choice));
         this.BackToOnline();
         return;
      }

      CoreLogic var1 = RacerGame.GetCoreLogic();
      if (var1 != null) {
         IntList var2 = this.m_AnimalList;
         switch (super.m_Module.GetMenuCX()) {
            case 0:
            case 1:
               var1.SelectAnimal(0, var2.GetAt(this.m_Choice));
               if (super.m_Module.GetMenuCX() == 0) {
                  var1.FillRandomAnimals(super.m_Module.GetMenuCX(), 6);
               } else {
                  var1.CreatePlayersForAdventure(var2.GetAt(this.m_Choice));
               }

               this.DisactivateWindow();
               super.m_Module.PushMenu(super.m_WindowID);
               if (ModGrandPrix.m_bPending) {
                  // [mod] Grand Prix : pas de choix de circuit (tirés au hasard), on passe au nombre de tours
                  super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._LapsMenuWindow);
                  return;
               }

               super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._MapLandsMenuWindow);
               return;
            case 2:
            default:
               return;
            case 3:
               if (super.m_Module.GetMenu() == KidsSimpleMenuModule._AnimalChoiceMenuWindow) {
                  var1.SelectAnimal(1, var2.GetAt(this.m_Choice));
                  this.m_AnimalForPlayingChooseSound = this.GetAnimalAt(this.m_Choice);
                  var1.FillRandomAnimals(super.m_Module.GetMenuCX(), 6);
                  this.DisactivateWindow();
                  super.m_Module.PushMenu(super.m_WindowID);
                  super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._MapLandsMenuWindow);
                  return;
               }

               var1.SelectAnimal(0, var2.GetAt(this.m_Choice));
               this.m_AnimalForPlayingChooseSound = this.GetAnimalAt(this.m_Choice);
               super.m_Module.PushMenu(super.m_WindowID);
               this.Show();
               return;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
               var1.SelectAnimal(0, var2.GetAt(this.m_Choice));
               this.m_AnimalForPlayingChooseSound = this.GetAnimalAt(this.m_Choice);
               var1.FillRandomAnimals(super.m_Module.GetMenuCX(), 6);
               KidsBossTauntMenuWindow var3 = (KidsBossTauntMenuWindow)super.m_Module.GetWindowAt(KidsSimpleMenuModule._BossTauntMenuWindow);
               switch (super.m_Module.GetMenuCX()) {
                  case 4:
                  case 5:
                  case 6:
                     var3.m_Boss = 4;
                     break;
                  case 7:
                  case 8:
                  case 9:
                     var3.m_Boss = 2;
                     break;
                  case 10:
                  case 11:
                  case 12:
                     var3.m_Boss = 1;
                     break;
                  case 13:
                  case 14:
                  case 15:
                     var3.m_Boss = 0;
                     break;
                  case 16:
                  case 17:
                  case 18:
                     var3.m_Boss = 5;
               }

               var3.m_TauntType = 2;
               super.m_Module.PushMenu(super.m_WindowID);
               this.DisactivateWindow();
               super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._BossTauntMenuWindow);
         }
      }
   }

   MenuAnimal GetAnimalAt(int var1) {
      return ((KidsSimpleMenuModule)super.m_Module).GetChoosenAnimal(this.m_AnimalList.GetAt(var1));
   }

   // [mod] chantier 4 : cadre des stats ---------------------------------------------------------------------------

   int AddStatsPicture(String sTex, Rect r, float w, float h, float x, float y) {
      KidsMenuWindow wnd = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      int id = this.Add(wnd);
      wnd.Init(super.m_Module, id);
      wnd.AddWindowTexture(wnd.AddTexture(sTex, true, true), r, sTex);
      wnd.SetWindowTexture(0);
      wnd.SetSizeAndPos(w, h, x, y, false);
      wnd.Show();
      return id;
   }

   int AddStatsText(float w, float h, float x, float y, boolean bLeft) {
      ModText t = (ModText)this.CreateObject("ModText");
      int id = this.Add(t);
      t.Init(super.m_Module, id);
      if (bLeft) {
         t.SetAlignLeft();
      }

      t.SetSizeAndPos(w, h, x, y, false);
      t.Show();
      return id;
   }

   void InitStats() {
      this.wndStatsPanel = this.AddStatsPicture(_TEX_STATS_PANEL, new Rect(0.0F, 0.0F, 1.0F, _ST_TEX_V), _ST_W, _ST_H, _ST_X, _ST_Y);
      this.ktxStatsName = this.AddStatsText(_ST_W, _ST_NAME_H, _ST_X, _ST_NAME_Y, false);
      float fLeft = _ST_X + _ST_PAD;
      float fWidth = _ST_W - 2.0F * _ST_PAD;

      for (int i = 0; i < 3; i++) {
         float fTop = _ST_ROW_Y + _ST_ROW_H * i;
         this.ktxStatsLabel[i] = this.AddStatsText(fWidth, _ST_LABEL_H, fLeft, fTop, true);
         this.wndStatsTrack[i] = this.AddStatsPicture(_TEX_STATS_TRACK, new Rect(0.0F, 0.0F, 1.0F, _ST_TRACK_V), fWidth, _ST_BAR_H, fLeft, fTop + _ST_LABEL_H);
         this.m_StatsFillRect[i] = new Rect(0.0F, 0.0F, 1.0F, 1.0F);
         this.wndStatsFill[i] = this.AddStatsPicture(
            _TEX_STATS_FILL[i], this.m_StatsFillRect[i], fWidth - 2.0F * _ST_FILL_IN_X, _ST_BAR_H - 2.0F * _ST_FILL_IN_Y, fLeft + _ST_FILL_IN_X, fTop + _ST_LABEL_H + _ST_FILL_IN_Y
         );
      }
   }

   // ordre d'affichage : cadre < rails < remplissages < textes (les enfants ajoutés après passent sinon dessous)
   void StatsOnTop() {
      super.m_List.SetOnTop(this.wndStatsPanel);

      for (int i = 0; i < 3; i++) {
         super.m_List.SetOnTop(this.wndStatsTrack[i]);
      }

      for (int i = 0; i < 3; i++) {
         super.m_List.SetOnTop(this.wndStatsFill[i]);
      }

      super.m_List.SetOnTop(this.ktxStatsName);

      for (int i = 0; i < 3; i++) {
         super.m_List.SetOnTop(this.ktxStatsLabel[i]);
      }
   }

   // nom et barres de l'animal affiché (m_Choice) ; appelé par Show, NextAnimal et PreviousAnimal
   void RefreshStats() {
      if (this.m_AnimalList == null || this.wndStatsPanel < 0 || this.m_Choice < 0 || this.m_Choice >= this.m_AnimalList.m_Cnt) {
         return;
      }

      int id = this.m_AnimalList.GetAt(this.m_Choice);
      ModText t = (ModText)super.m_List.WindowAt(this.ktxStatsName);
      t.ColorWhite();
      t.SetText(id >= 0 && id < ModAnimals._COUNT ? "&AnimalName" + id + "&" : "");
      float fLeft = _ST_X + _ST_PAD + _ST_FILL_IN_X;
      float fWidth = _ST_W - 2.0F * _ST_PAD - 2.0F * _ST_FILL_IN_X;

      for (int i = 0; i < 3; i++) {
         t = (ModText)super.m_List.WindowAt(this.ktxStatsLabel[i]);
         t.ColorWhite();
         t.SetText(_STATS_LABELS[i]);
         float fFill = ModAnimals.GetFill(i, id);
         KidsMenuWindow wnd = (KidsMenuWindow)super.m_List.WindowAt(this.wndStatsFill[i]);
         // la barre raccourcit et l'image est coupée d'autant à droite : le dégradé n'est jamais écrasé
         wnd.SetSizeAndPos(fWidth * fFill, _ST_BAR_H - 2.0F * _ST_FILL_IN_Y, fLeft, _ST_ROW_Y + _ST_ROW_H * i + _ST_LABEL_H + _ST_FILL_IN_Y, false);
         this.m_StatsFillRect[i].fRight = fFill;
         wnd.SetTextureCoords(this.m_StatsFillRect[i]);
      }
   }

   void PreviousAnimal() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      this.NextIdle();
      IntList var2 = this.m_AnimalList;
      int var3 = this.m_Choice;
      if (this.m_Choice <= 0 && !this.AllAnimals()) { // [mod] déblocage du menu Mods, ou écran « En ligne »
         this.m_Choice = 0;
         this.CheckAvailableOK();
      } else {
         MenuAnimal var5 = this.GetAnimalAt(this.m_Choice);
         if (!this.CanChangeWindow()) {
            super.m_Module.m_Camera.FastSettings(var5, 25.0F, var5.m_ChooseAnimalRadius, var5.m_ChooseAnimalHeight, var5.m_ChooseAnimalAddHeight);
            ((KidsSimpleMenuModule)super.m_Module).HideAllLevelAnimals();
            int var8 = ((KidsSimpleMenuModule)super.m_Module).GetLevelAnimalByRef(var5);
            if (var8 >= 0) {
               ((KidsSimpleMenuModule)super.m_Module).ShowLevelAnimal(var8);
            }

            this.CheckAvailableOK();
         } else {
            this.m_Choice += -1;
            if (this.m_Choice < 0) {
               this.m_Choice = var2.m_Cnt - 1;
            }

            var5 = this.GetAnimalAt(this.m_Choice);
            MenuAnimal var6 = this.GetAnimalAt(var3);
            super.m_Module
               .m_Camera
               .SlowSettings(
                  var6, var5, 1.0F, 25.0F, var5.m_ChooseAnimalRadius, var5.m_ChooseAnimalHeight, var6.m_ChooseAnimalAddHeight, var5.m_ChooseAnimalAddHeight
               );
            ((KidsSimpleMenuModule)super.m_Module).HideAllLevelAnimals();
            int var4 = ((KidsSimpleMenuModule)super.m_Module).GetLevelAnimalByRef(var6);
            if (var4 >= 0) {
               ((KidsSimpleMenuModule)super.m_Module).ShowLevelAnimal(var4);
            }

            var4 = ((KidsSimpleMenuModule)super.m_Module).GetLevelAnimalByRef(var5);
            if (var4 >= 0) {
               ((KidsSimpleMenuModule)super.m_Module).ShowLevelAnimal(var4);
            }

            this.SetButtonsState();
            this.CheckAvailableOK();
         }
      }

      this.RefreshStats(); // [mod] chantier 4
   }

   void NextAnimal() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      this.NextIdle();
      IntList var2 = this.m_AnimalList;
      int var4 = this.m_Choice;
      if (this.m_Choice >= var2.m_Cnt - 1 && !this.AllAnimals()) { // [mod] déblocage du menu Mods, ou écran « En ligne »
         this.m_Choice = var2.m_Cnt - 1;
         this.CheckAvailableOK();
      } else {
         MenuAnimal var5 = this.GetAnimalAt(this.m_Choice);
         if (!this.CanChangeWindow()) {
            GameObject.CrashLog("ostre przejscie\n");
            super.m_Module.m_Camera.FastSettings(var5, 25.0F, var5.m_ChooseAnimalRadius, var5.m_ChooseAnimalHeight, var5.m_ChooseAnimalAddHeight);
            ((KidsSimpleMenuModule)super.m_Module).HideAllLevelAnimals();
            int var8 = ((KidsSimpleMenuModule)super.m_Module).GetLevelAnimalByRef(var5);
            if (var8 >= 0) {
               ((KidsSimpleMenuModule)super.m_Module).ShowLevelAnimal(var8);
            }

            this.CheckAvailableOK();
         } else {
            this.m_Choice++;
            if (this.m_Choice >= var2.m_Cnt) {
               this.m_Choice = 0;
            }

            var5 = this.GetAnimalAt(this.m_Choice);
            MenuAnimal var6 = this.GetAnimalAt(var4);
            super.m_Module
               .m_Camera
               .SlowSettings(
                  var6, var5, 1.0F, 25.0F, var5.m_ChooseAnimalRadius, var5.m_ChooseAnimalHeight, var6.m_ChooseAnimalAddHeight, var5.m_ChooseAnimalAddHeight
               );
            GameObject.CrashLog("plynne przejscie\n");
            ((KidsSimpleMenuModule)super.m_Module).HideAllLevelAnimals();
            int var3 = ((KidsSimpleMenuModule)super.m_Module).GetLevelAnimalByRef(var6);
            if (var3 >= 0) {
               ((KidsSimpleMenuModule)super.m_Module).ShowLevelAnimal(var3);
            }

            var3 = ((KidsSimpleMenuModule)super.m_Module).GetLevelAnimalByRef(var5);
            if (var3 >= 0) {
               ((KidsSimpleMenuModule)super.m_Module).ShowLevelAnimal(var3);
            }

            this.SetButtonsState();
            this.CheckAvailableOK();
         }
      }

      this.RefreshStats(); // [mod] chantier 4
   }

   public void Hide() {
      super.m_Module.m_Camera.StopForestLoop();
      super.m_Module.m_Camera.ClearSettings();
      this.DisableCallMethod("Anim");
      this.DisableCallMethod("FirstAnim");
      this.ShowPodium(true);
      ((KidsSimpleMenuModule)super.m_Module).m_AnimalList.EnableRenderingForAll(false);
      super.Hide();
      this.ProcessKb(false);
      ((KidsSimpleMenuModule)super.m_Module).HideAllLevelAnimals();
      super.m_Module.DeactivateMenuCamera();
   }

   void NextIdle() {
      this.DisableCallMethod("FirstAnim");
      this.DisableCallMethod("Anim");
      this.CallMethodIn("FirstAnim", 1.5F);
   }

   void CheckAvailableOK() {
      if (this.m_Choice >= 0) {
         CoreLogic var1 = RacerGame.GetCoreLogic();
         MenuAnimal var2 = this.GetAnimalAt(this.m_Choice);
         boolean var3 = m_bOnline || !var1.IsAnimalLocked(var2.m_AnimalKind); // [mod] en ligne : tous jouables
         KidsButton var4 = (KidsButton)super.m_List.WindowAt(this.btnOK);
         if (var4 != null) {
            var4.Enable(var3);
         }
      }
   }

   public void Show() {
      super.m_Module.SetIgnoreKeys(2);
      super.Show();
      this.ProcessKb(true);
      CoreLogic var1 = RacerGame.GetCoreLogic();
      this.ShowPodium(false);
      ((KidsSimpleMenuModule)super.m_Module).m_AnimalList.EnableRenderingForAll(false);
      this.m_Choice = 0;
      if (super.m_Module.GetMenu() == KidsSimpleMenuModule._AnimalChoiceMenuWindow) {
         super.m_List.SetTexture(this.wnd12, this.tex2);
         if (var1 != null) {
            this.m_AnimalList = var1.GetAvailableAnimals(3, true);
         }
      } else {
         super.m_List.SetTexture(this.wnd12, this.tex1);
         if (var1 != null) {
            this.m_AnimalList = var1.GetAvailableAnimals(super.m_Module.GetMenuCX(), false);
         }
      }

      if (super.m_Module.GetMenuCX() == 3 && !m_bOnline) {
         super.m_List.Show(this.wnd12);
      } else {
         super.m_List.Hide(this.wnd12);
      }

      if (m_bOnline) {
         // [mod] chantier 8 : les 12 animaux, en commençant par celui déjà choisi dans le salon
         this.m_AnimalList = new IntList(12);

         for (int i = 0; i < 12; i++) {
            this.m_AnimalList.Add(i);
         }

         this.m_Choice = ModNet.m_nCfgAnimal >= 0 && ModNet.m_nCfgAnimal < 12 ? ModNet.m_nCfgAnimal : 0;
      }

      super.m_Module.m_Camera.InitObject();
      super.m_Module.ActivateMenuCamera();
      ((KidsSimpleMenuModule)super.m_Module).HideAllLevelAnimals();
      MenuAnimal var2 = this.GetAnimalAt(this.m_Choice);
      super.m_Module.m_Camera.FastSettings(var2, 25.0F, var2.m_ChooseAnimalRadius, var2.m_ChooseAnimalHeight, var2.m_ChooseAnimalAddHeight);
      int var3 = ((KidsSimpleMenuModule)super.m_Module).GetLevelAnimalByRef(var2);
      if (var3 >= 0) {
         ((KidsSimpleMenuModule)super.m_Module).ShowLevelAnimal(var3);
      }

      ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, true);
      this.SetButtonsState();
      this.StopAnimForAll();
      this.NextIdle();
      super.m_Module.m_Camera.PlayForestLoop();
      if (var2 != null) {
         this.PlayChoosenSound(var2.GetPosition());
      }

      this.CheckAvailableOK();
      this.StatsOnTop(); // [mod] chantier 4
      this.RefreshStats();
   }

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(this.m_TexName, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), this.m_TexName);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      ModSideFill.AddTo(this, var1); // [mod] écran large : damier prolongé sur les côtés au lieu du décor 3D
      KidsButton var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnPrev = this.Add(var3);
      this.btnPrevInit.bMoveButton = true;
      this.btnPrevInit.xpos = 0.1F;
      this.btnPrevInit.ypos = 0.433334F;
      this.btnPrevInit.width = 0.14375F;
      this.btnPrevInit.height = 0.19166F;
      this.btnPrevInit.ButtonWidth = 1.3913F;
      this.btnPrevInit.ButtonHeight = 1.3913F;
      var3.Init(super.m_Module, this, this.btnPrev, this.btnPrevInit);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnNext = this.Add(var3);
      this.btnNextInit.bMoveButton = true;
      this.btnNextInit.xpos = 0.9F;
      this.btnNextInit.ypos = 0.433334F;
      this.btnNextInit.width = 0.14375F;
      this.btnNextInit.height = 0.19166F;
      this.btnNextInit.ButtonWidth = 1.3913F;
      this.btnNextInit.ButtonHeight = 1.3913F;
      var3.Init(super.m_Module, this, this.btnNext, this.btnNextInit);
      var3.ProcessMouse(true);
      KidsMenuWindow var4 = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      this.wnd12 = this.Add(var4);
      var4.Init(super.m_Module, this.wnd12);
      this.tex1 = var4.AddWindowTexture(
         var4.AddTexture("Data/Textures/Menu/a/1MenuCharakter.png", true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), "Data/Textures/Menu/a/1MenuCharakter.png"
      );
      this.tex2 = var4.AddWindowTexture(
         var4.AddTexture("Data/Textures/Menu/a/2MenuCharakter.png", true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), "Data/Textures/Menu/a/2MenuCharakter.png"
      );
      var4.SetSizeAndPos(0.14F, 0.18F, 0.1F, 0.88F, true);
      var4.SetWindowTexture(this.tex1);
      var4.Show();
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnOK = this.Add(var3);
      this.btnOKInit.bMoveButton = true;
      this.btnOKInit.xpos = 0.5F;
      this.btnOKInit.ypos = 0.86667F;
      this.btnOKInit.width = 0.1484F;
      this.btnOKInit.height = 0.13334F;
      this.btnOKInit.ButtonWidth = 1.3473F;
      this.btnOKInit.ButtonHeight = 2.0F;
      var3.Init(super.m_Module, this, this.btnOK, this.btnOKInit);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExit = this.Add(var3);
      this.btnExitInit.bMoveButton = true;
      var3.Init(super.m_Module, this, this.btnExit, this.btnExitInit);
      var3.ProcessMouse(true);
      this.InitStats(); // [mod] chantier 4 : cadre des stats en haut à droite
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.SetAlphaForAll(1.0F);
      this.SetButtonsState();
      this.ProcessKb(true);
   }

   public void Anim() {
      this.LaunchIdle(false);
      this.DisableCallMethod("Anim");
      this.DisableCallMethod("FirstAnim");
      this.CallMethodIn("Anim", 5.0F);
   }

   void ChangeCameraDistance(float var1) {
      MenuCamera var2 = super.m_Module.m_Camera;
      if (var2 != null) {
         float var3 = var2.GetCameraDistance();
         if (!(var3 <= 0.0F)) {
            var3 += var1;
            if (var3 < 225.0F) {
               var3 = 225.0F;
            }

            if (var3 > 500.0F) {
               var3 = 500.0F;
            }

            var2.SetCameraDistance(var3);
         }
      }
   }
}
