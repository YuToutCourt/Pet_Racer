import java.util.Random;

public class RacerGame extends Game {
   static final boolean _NOCD = true;
   static Settings settings = null;
   static RacerModule activeLevelModule = null;
   static RacerGame game = null;
   boolean m_bFinishRace = false;
   static MovieModule movieModule = null;
   static IntroModule introModule = null;
   static OutroModule outroModule = null;
   static ExtraHudModule extraHud = null;
   static final String _MENU_LEVEL_NAME = "Data/Levels/MenuLevel/Test.map";
   static final String _INTRO_LEVEL_NAME = "Data/Levels/Intro/Track_KeoZ.map";
   static final String _OUTRO_LEVEL_NAME = "Data/Levels/Outro/Track.map";
   static boolean m_bEscPressed = false;
   static GameSerializeSettings satrib = new GameSerializeSettings();
   static KidsSimpleMenuModule firstMenuModule = null;
   static final int _ACTIVE_LOGO = 0;
   static final int _ACTIVE_MENU = 1;
   static final int _ACTIVE_RACE_MODULE = 2;
   static final int _ACTIVE_INTRO = 3;
   static final int _ACTIVE_OUTRO = 4;
   int activeMenu = 0;
   static Random m_Random = new Random(123321123L);
   public boolean m_bExitDirectToMainMenu = false;
   boolean m_bEnableCheats = false;
   static boolean m_bRecordAvi = false;
   static int m_iNextAviLevel = 0;
   String m_CheatString = new String("");

   static void SetMusicVolume(float var0) {
      satrib.m_MusicVolume = var0;
   }

   static int GetResolution() {
      return satrib.m_Resolution;
   }

   public void LoadFirstGameModule() {
      movieModule.Deactivate();
      this.LoadMenuLevel();
      this.activeMenu = 1;
      firstMenuModule.Activate();
      firstMenuModule.CallDeleteMovieModule();
   }

   void CheckStringEnableCheats() {
      if (this.m_CheatString != null && !CoreLogic.m_bIsLocked) {
         GameObject.LogLine("IndexOf: " + this.m_CheatString.indexOf("PRSUPER"));
         if (this.m_CheatString.indexOf("PRSUPER") >= 0) {
            GameObject.LogLine("PRSUPER");
            this.m_bEnableCheats = true;
            this.PlayCS();
         }
      }

      this.m_CheatString = new String("");
   }

   void CheckStringCheats() {
      CoreLogic var1 = GetCoreLogic();
      if (this.m_CheatString != null) {
         // [mod] ANIMALS / TRACKS / BONUSES cochent les déblocages du menu Mods (mods.dat) au lieu de modifier la
         // progression du profil : on les retire depuis le menu.
         if (this.m_CheatString.indexOf("ANIMALS") >= 0) {
            ModOptions.m_bAllAnimals = true;
            ModOptions.Save();
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("TRACKS") >= 0) {
            ModOptions.m_bAllTracks = true;
            ModOptions.Save();
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("BONUSES") >= 0) {
            ModOptions.m_bAllExtras = true;
            ModOptions.Save();
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L11") >= 0) {
            var1.SetCheatAdventure(0);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L12") >= 0) {
            var1.SetCheatAdventure(1);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L13") >= 0) {
            var1.SetCheatAdventure(2);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L14") >= 0) {
            var1.SetCheatAdventure(3);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L15") >= 0) {
            var1.SetCheatAdventure(4);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L16") >= 0) {
            var1.SetCheatAdventure(5);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L21") >= 0) {
            var1.SetCheatAdventure(6);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L22") >= 0) {
            var1.SetCheatAdventure(7);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L23") >= 0) {
            var1.SetCheatAdventure(8);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L24") >= 0) {
            var1.SetCheatAdventure(9);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L25") >= 0) {
            var1.SetCheatAdventure(10);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L26") >= 0) {
            var1.SetCheatAdventure(11);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L31") >= 0) {
            var1.SetCheatAdventure(12);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L32") >= 0) {
            var1.SetCheatAdventure(13);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L33") >= 0) {
            var1.SetCheatAdventure(14);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L34") >= 0) {
            var1.SetCheatAdventure(15);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L35") >= 0) {
            var1.SetCheatAdventure(16);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L36") >= 0) {
            var1.SetCheatAdventure(17);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L41") >= 0) {
            var1.SetCheatAdventure(18);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L42") >= 0) {
            var1.SetCheatAdventure(19);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L43") >= 0) {
            var1.SetCheatAdventure(20);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L44") >= 0) {
            var1.SetCheatAdventure(21);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L45") >= 0) {
            var1.SetCheatAdventure(22);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L46") >= 0) {
            var1.SetCheatAdventure(23);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L51") >= 0) {
            var1.SetCheatAdventure(24);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L52") >= 0) {
            var1.SetCheatAdventure(25);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L53") >= 0) {
            var1.SetCheatAdventure(26);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L54") >= 0) {
            var1.SetCheatAdventure(27);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L55") >= 0) {
            var1.SetCheatAdventure(28);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L56") >= 0) {
            var1.SetCheatAdventure(29);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L61") >= 0) {
            var1.SetCheatAdventure(30);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L62") >= 0) {
            var1.SetCheatAdventure(31);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }

         if (this.m_CheatString.indexOf("L63") >= 0) {
            var1.SetCheatAdventure(32);
            this.PlayCS();
            this.m_CheatString = new String("");
            return;
         }
      }
   }

   void DeleteOutroModule() {
      if (outroModule != null) {
         outroModule.delete();
         outroModule = null;
      }
   }

   public void DestroyMovieModule() {
      if (movieModule != null) {
         movieModule.delete();
         movieModule = null;
      }
   }

   static CoreLogic GetCoreLogic() {
      return GameSerializeSettings.Logic;
   }

   void LoadLevel(int var1) {
      String var2 = RacerModule.GetLevelFileName(var1);
      ModAIMap.m_sLevel = var2; // [mod] carte du circuit pour l'IA (aimap.dat)

      try {
         GameObject.CrashLog("Loading level " + var2);
         activeLevelModule = (RacerModule)this.LoadModule(var2);
         activeLevelModule.InitModule();
      } catch (ClassCastException var3) {
         GameObject.CrashLog("Unable to load " + var2);
      }
   }

   void LoadMenuLevel() {
      String var1 = "Data/Levels/MenuLevel/Test.map";

      try {
         GameObject.CrashLog("Loading level: " + var1);
         firstMenuModule = (KidsSimpleMenuModule)this.LoadModule(var1);
         firstMenuModule.InitModule();
      } catch (ClassCastException var2) {
         GameObject.CrashLog("Unable to load: " + var1);
      }
   }

   static void SetDetails(int var0) {
      satrib.m_Details = var0;
   }

   static void SetCameraDistance(int var0, int var1) {
      satrib.m_CameraDistance0 = var1;
   }

   static KbSettings GetKb1() {
      return satrib.m_Kb1;
   }

   static int GetDetails() {
      return satrib.m_Details;
   }

   void PlayCS() {
      if (firstMenuModule != null && firstMenuModule.m_Camera != null && firstMenuModule.m_Camera.coSpeaker != null) {
         firstMenuModule.m_Camera.coSpeaker.AttachOneShotSound("Data/Sounds/bonus.wav");
      }
   }

   public void DestroyIntroModule() {
      if (introModule != null) {
         introModule.delete();
         introModule = null;
      }
   }

   String ConvertToChar(int var1) {
      switch (var1) {
         case 2:
            return "1";
         case 3:
            return "2";
         case 4:
            return "3";
         case 5:
            return "4";
         case 6:
            return "5";
         case 7:
            return "6";
         case 8:
            return "7";
         case 9:
            return "8";
         case 10:
            return "9";
         case 11:
            return "0";
         case 16:
            return "Q";
         case 17:
            return "W";
         case 18:
            return "E";
         case 19:
            return "R";
         case 20:
            return "T";
         case 21:
            return "Y";
         case 22:
            return "U";
         case 23:
            return "I";
         case 24:
            return "O";
         case 25:
            return "P";
         case 30:
            return "A";
         case 31:
            return "S";
         case 32:
            return "D";
         case 33:
            return "F";
         case 34:
            return "G";
         case 35:
            return "H";
         case 36:
            return "J";
         case 37:
            return "K";
         case 38:
            return "L";
         case 44:
            return "Z";
         case 45:
            return "X";
         case 46:
            return "C";
         case 48:
            return "B";
         case 49:
            return "N";
         case 50:
            return "M";
         case 57:
            return " ";
         default:
            return null;
      }
   }

   static float GetFXVolume() {
      return satrib.m_FXVolume;
   }

   public void OnPreInitialize(String[] var1) {
      if (!Text.FromFile("data/texts.scr")) {
         GameObject.CrashLog("Missing or corrupt file: texts.scr!!!!");
      }

      ModOptions.Load(); // [mod] réglages du mod (mods.dat)
      this.ReadParams(var1);
      this.SetGameName(Text.Get("&GameName&"));
      game = this;
      settings = new Settings();
      settings.LoadSettings();
      AnimalBonusTypes.Init();
      if (var1 != null) {
         this.ReadExecuteParams(var1);
      }

      Vector.InitOptimizer(20, 20);
   }

   public void OnInitialize() {
      super.OnInitialize();
      ModAnimals.Load(); // [mod] stats des animaux (Data\animals.scr), avant toute voiture
      ModBonuses.Load(); // [mod] tirage des bonus selon la place (Data\bonuses.scr)
      ModGrandPrix.Load(); // [mod] Grand Prix : nombre de courses et points (Data\grandprix.scr)
      ModOnline.Init(); // [mod] en ligne : reglages et socket UDP (Data\online.scr, nettest.scr)
      this.RegisterKeyRequest();
      this.SetDefaultSoundRanges(1000.0F, 4000.0F);
      this.LoadSoundTemplates("Data/sounds.ini");
      if (satrib.m_bFirstTime) {
         PerformanceSettings.SetQuality(this.GetCPUTime(), this.GetVideoTime(), this.GetMemTime());
         satrib.m_Details = PerformanceSettings.m_iCurrQuality;
      }

      satrib.SetCurrentSettings();
      movieModule = (MovieModule)this.CreateObject("MovieModule");
      this.activeMenu = 0;
      movieModule.Activate();
      this.TimersStart();
      this.TimersDefaultFrozen(false);
   }

   // [mod] appelé par le moteur à chaque image (menus, course, vidéos) : réseau puis limiteur d'images
   public void OnFrame() {
      super.OnFrame();
      ModOnline.Frame(); // [mod] en ligne : paquets recus
      ModFrameLimiter.Frame();
   }

   void ReadParams(String[] var1) {
      String var2 = "OUTPUT.avi";
      float var3 = 25.0F;
      if (var1.length > 0) {
         for (int var4 = 0; var4 < var1.length; var4++) {
            if (var1[var4].startsWith("record")) {
               m_bRecordAvi = true;
            }

            if (var1[var4].startsWith("avi=")) {
               var2 = var1[var4].substring(var1[var4].indexOf(61) + 1);
            }

            if (var1[var4].startsWith("fps=")) {
               // [mod] l'original lisait « =25 » + « 1 » (parenthèse mal placée) : exception au lancement avec fps=
               try {
                  var3 = Float.valueOf(var1[var4].substring(var1[var4].indexOf(61) + 1).trim()).floatValue();
               } catch (NumberFormatException var5) {
                  GameObject.CrashLog("[mod] argument " + var1[var4] + " ignore\n");
               }
            }
         }
      }

      if (m_bRecordAvi) {
         GameObject.LogLine("Enabling recording");
         Game.EnableRecordingAVI(var2, var3);
         Game.EnableInputRecording("default.inp", var3);
      }
   }

   void EndRaceAfterEsc() {
      int var1 = firstMenuModule.GetMenuCX();
      if (var1 != 1 && var1 != 0) {
         this.m_bExitDirectToMainMenu = true;
      }

      m_bEscPressed = true;
      this.EndModule();
   }

   public void OnLoadingProgress(float var1) {
      if (firstMenuModule != null) {
         KidsLoadingMenuWindow var2 = (KidsLoadingMenuWindow)firstMenuModule.m_WindowsList.WindowAt(KidsSimpleMenuModule._LoadingMenuWindow);
         if (var2 != null) {
            if (this.activeMenu == 2) {
               var2.Update(var1);
            }
         }
      }
   }

   static float GetRandom() {
      return m_Random.nextFloat();
   }

   static int GetRandom(int var0) {
      return Math.round(GetRandom() * (var0 - 1));
   }

   static int GetRandom(int var0, int var1) {
      return var0 + GetRandom(var1 - var0 + 1);
   }

   static float GetRandom(float var0, float var1) {
      return var0 + GetRandom() * (var1 - var0);
   }

   static float GetRandom(float var0) {
      return var0 * GetRandom();
   }

   static void SaveAttrib() {
      satrib.Save();
   }

   void EndOutro() {
      if (outroModule != null) {
         outroModule.SplashScreen();
      }

      firstMenuModule.ClearStack();
      firstMenuModule.m_CurrentMenu = KidsSimpleMenuModule._MainMenuWindow;
      firstMenuModule.SetMenuCX(-1);
      if (outroModule != null) {
         outroModule.Deactivate();
      }

      this.activeMenu = 1;
      firstMenuModule.Activate();
      firstMenuModule.CallDeleteOutroModule();
   }

   public void RunModule() {
      GameObject.CrashLog("RunModule\n");
      if (RacerModule.m_bIsDemo) {
         CoreLogic var1 = GetCoreLogic();
         var1.m_SelectedAnimals = new IntList(6);
         var1.m_SelectedAnimals.Push(0);
         var1.m_SelectedAnimals.Push(1);
         var1.m_SelectedAnimals.Push(2);
         var1.m_SelectedAnimals.Push(3);
         var1.m_SelectedAnimals.Push(4);
         var1.m_SelectedAnimals.Push(5);
         var1.m_Laps = 3;
      }

      if (!RacerModule.m_bIsDemo) {
         ModGrandPrix.BeforeRace(); // [mod] Grand Prix : ouvre la coupe, ou règle la course suivante
      }

      firstMenuModule.ShowLoadingScreen();
      firstMenuModule.StopMusicLoop();
      CoreLogic var5 = GetCoreLogic();
      int var2 = firstMenuModule.GetMenuCX();
      if (EMenuCX.IsExtra(var2)) {
         var5.SetTrackBeforeExtra(var2);
      }

      extraHud = (ExtraHudModule)this.CreateObject("ExtraHudModule");
      this.activeMenu = 2;
      int var3 = 0;
      if (RacerModule.m_bIsDemo) {
         if (m_bRecordAvi) {
            GameObject.LogLine("Recording...");
            this.LoadLevel(m_iNextAviLevel);
            switch (m_iNextAviLevel) {
               case 0:
               case 1:
               case 6:
               case 7:
               case 12:
               case 13:
               case 18:
               case 19:
               case 24:
               case 25:
                  m_iNextAviLevel++;
                  break;
               case 2:
               case 8:
               case 14:
               case 20:
                  m_iNextAviLevel += 4;
                  break;
               case 26:
                  this.StopAVIRecording();
                  m_bRecordAvi = false;
                  var3 = (int)GetRandom(1.0F, 4.0F);
                  this.LoadLevel(var3);
            }
         } else {
            var3 = (int)GetRandom(1.0F, 4.0F);
            this.LoadLevel(var3);
         }
      } else {
         this.LoadLevel(var5.GetNextTrack());
      }

      if (EMenuCX.IsExtra(var2)) {
         int var4 = var5.ConvertExtraToOrd(var2);
         var5.PrepareTrackBeforeExtra(var4);
      } else if (RacerModule.m_bIsDemo) {
         var5.PrepareTrackBeforeRace(var3);
      } else {
         var5.PrepareTrackBeforeRace(var5.GetNextTrack());
      }

      var5.ClearTimeList();
      activeLevelModule.Activate();
      extraHud.Activate();
      firstMenuModule.m_Cursor.Show();
      firstMenuModule.m_WindowsList.Hide(KidsSimpleMenuModule._LoadingMenuWindow);
      firstMenuModule.Deactivate();
      firstMenuModule.ReleaseAllWindowsTextures();
   }

   void LoadIntroModule() {
      String var1 = "Data/Levels/Intro/Track_KeoZ.map";

      try {
         GameObject.CrashLog("Loading level: " + var1);
         introModule = (IntroModule)this.LoadModule(var1);
         introModule.InitModule();
      } catch (ClassCastException var2) {
         GameObject.CrashLog("Unable to load: " + var1);
      }
   }

   static float GetMusicVolume() {
      return satrib.m_MusicVolume;
   }

   static RacerModule GetRacerModule() {
      if (activeLevelModule == null) {
         GameObject.CrashLog("activeLevelModule == NULL!!!");
      }

      return activeLevelModule;
   }

   static KbSettings GetKb0() {
      return satrib.m_Kb0;
   }

   static void SetFXVolume(float var0) {
      satrib.m_FXVolume = var0;
   }

   static GameSerializeSettings GetGSS() {
      return satrib;
   }

   static int GetCameraDistance(int var0) {
      return satrib.m_CameraDistance0;
   }

   static void SetResolution(int var0) {
      satrib.m_Resolution = var0;
   }

   void OnInputKey(int var1, boolean var2, char var3) {
      super.OnInputKey(var1, var2, var3);
      ModNet.OnKey(var1, var2); // [mod] en ligne, arrivé : spectateur
      if (firstMenuModule != null && firstMenuModule.m_CurrentMenu == KidsSimpleMenuModule._MainMenuWindow) {
         if (var1 == 42 || var1 == 54) {
            if (var2) {
               this.m_CheatString = new String("");
               return;
            } else {
               this.CheckStringEnableCheats();
               return;
            }
         }

         if (this.m_CheatString == null) {
            this.m_CheatString = new String("");
         }

         if (this.m_CheatString.length() > 50) {
            this.m_CheatString = new String("");
         }

         String var4 = this.ConvertToChar(var1);
         if (var4 != null && !var2) {
            this.m_CheatString = this.m_CheatString.concat(var4);
            if (this.m_bEnableCheats) {
               this.CheckStringCheats();
            }
         }
      }

      if (var1 == 1 && !var2) {
         switch (this.activeMenu) {
            case 1:
            default:
               break;
            case 2:
               if (activeLevelModule.m_bExitAllowed) {
                  activeLevelModule.m_ExitWnd.ShowExitWnd();
                  return;
               }
               break;
            case 3:
               this.EndIntro();
               return;
         }
      }
   }

   void EndIntro() {
      if (introModule != null) {
         introModule.SplashScreen();
         introModule.Deactivate();
      }

      this.LoadMenuLevel();
      this.activeMenu = 1;
      firstMenuModule.Activate();
      firstMenuModule.CallDeleteIntro();
   }

   void LoadOutroModule() {
      String var1 = "Data/Levels/Outro/Track.map";

      try {
         GameObject.CrashLog("Loading level: " + var1);
         if (firstMenuModule != null) {
            firstMenuModule.Deactivate();
         }

         outroModule = (OutroModule)this.LoadModule(var1);
         outroModule.InitModule();
      } catch (ClassCastException var2) {
         GameObject.CrashLog("Unable to load: " + var1);
      }
   }

   public void EndModule() {
      CoreLogic var1 = GetCoreLogic();
      this.m_bFinishRace = false;
      boolean var2 = m_bEscPressed;
      m_bEscPressed = false;
      ModNet.OnRaceEnded(var2); // [mod] en ligne : fin de session (BYE)
      if (!RacerModule.m_bIsDemo && ModAI.IsImproved()) {
         VehicleAI.ModSaveMemory(); // [mod] ce que les IA ont appris sur ce circuit (aimap.dat), arrivée ou Échap
      }
      if (!RacerModule.m_bIsDemo && !var2) {
         var1.MakeProgressAfterAdventureRace();
         var1.CheckExtraPrize();
      }

      // [mod] Grand Prix : points de la course, avant que le circuit soit supprimé (course quittée : ordre du
      // moment) et que le podium vide la liste des temps (course finie)
      boolean bGrandPrix = ModGrandPrix.IsActive() && !RacerModule.m_bIsDemo;
      if (bGrandPrix) {
         if (var2) {
            ModGrandPrix.OnRaceAbandoned(activeLevelModule);
         } else {
            ModGrandPrix.OnRaceFinished();
         }
      }

      var1.m_bFailedLand = false;
      this.activeMenu = 1;
      if (activeLevelModule != null) {
         activeLevelModule.Deactivate();
         firstMenuModule.m_LevelModuleToDelete = activeLevelModule;
         firstMenuModule.DeleteRacerModule();
         extraHud.ShowSplash();
         extraHud.ForceRender();
         extraHud.Deactivate();
         extraHud.delete();
         extraHud = null;
      }

      if (RacerModule.m_bIsDemo) {
         this.m_bExitDirectToMainMenu = true;
         RacerModule.SetDemoModule(false);
      }

      if (this.m_bExitDirectToMainMenu) {
         firstMenuModule.ClearStack();
         firstMenuModule.m_CurrentMenu = KidsSimpleMenuModule._MainMenuWindow;
         firstMenuModule.SetMenuCX(-1);
      } else if (!var2) {
         firstMenuModule.m_CurrentMenu = KidsSimpleMenuModule._PlacesMenuWindow;
      } else {
         int var3 = firstMenuModule.GetMenuCX();
         firstMenuModule.ClearStack();
         var1.ClearTimeList();
         if (ModNet.BackToOnlineScreen() && !RacerModule.m_bIsDemo) {
            // [mod] course en ligne quittée : retour au salon
            firstMenuModule.SetMenuCX(-1);
            firstMenuModule.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
            firstMenuModule.m_CurrentMenu = KidsSimpleMenuModule._OnlineMenuWindow;
         } else if (bGrandPrix) {
            // [mod] course quittée en Grand Prix : classement de la coupe, qui continue
            firstMenuModule.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
            firstMenuModule.PushMenu(KidsSimpleMenuModule._SinglePlayerMenuWindow);
            firstMenuModule.m_CurrentMenu = KidsSimpleMenuModule._GrandPrixMenuWindow;
         } else if (var3 == 1) {
            firstMenuModule.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
            firstMenuModule.PushMenu(KidsSimpleMenuModule._SinglePlayerMenuWindow);
            firstMenuModule.PushMenu(KidsSimpleMenuModule._MapLandsMenuWindow);
            KidsMapTracksMenuWindow var4 = (KidsMapTracksMenuWindow)firstMenuModule.GetWindowAt(KidsSimpleMenuModule._MapTracksMenuWindow);
            var4.m_CurrentLand = var1.m_nPassedTracks / 6;
            firstMenuModule.m_CurrentMenu = KidsSimpleMenuModule._MapTracksMenuWindow;
         } else if (var3 == 0) {
            firstMenuModule.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
            firstMenuModule.PushMenu(KidsSimpleMenuModule._SinglePlayerMenuWindow);
            firstMenuModule.PushMenu(KidsSimpleMenuModule._AnimalChoiceMenuWindow);
            firstMenuModule.m_CurrentMenu = KidsSimpleMenuModule._MapLandsMenuWindow;
         } else if (var3 == 2
            || var3 == 4
            || var3 == 5
            || var3 == 6
            || var3 == 7
            || var3 == 8
            || var3 == 9
            || var3 == 10
            || var3 == 11
            || var3 == 12
            || var3 == 13
            || var3 == 14
            || var3 == 15
            || var3 == 16
            || var3 == 17
            || var3 == 18) {
            firstMenuModule.PushMenu(KidsSimpleMenuModule._MainMenuWindow);
            firstMenuModule.PushMenu(KidsSimpleMenuModule._SinglePlayerMenuWindow);
            firstMenuModule.m_CurrentMenu = KidsSimpleMenuModule._ExtraMenuWindow;
         }
      }

      this.m_bExitDirectToMainMenu = false;
      firstMenuModule.RestoreAllWindowsTextures();
      firstMenuModule.Activate();
   }
}
