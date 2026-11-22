class RacerModule extends Module {
   static final int CHK_RACE_CHECKPOINT = 10030;
   static final int CHK_RACE_CHECKPOINT2 = 10031;
   static final int CHK_RACE_CHECKPOINT3 = 10032;
   static float _PLACES_UPDATE = 0.1F;
   static float _POWER_RACE_LOPS = 10.0F;
   static float _TIME_BEFORE_RACE = 2.0F;
   public static Camera[] m_Cams = new Camera[2];
   public static int[] m_CamsIDs = new int[2];
   static boolean m_bIsDemo = false;
   public Track m_Track = null;
   public Track m_AITrack = null;
   public BonusItem[] m_BonusTab = null;
   public RaceParams m_RaceParams = null;
   float m_fNowTime = 0.0F;
   float m_fFrameTime = 0.0F;
   float m_fLastUpdateTime = 0.0F;
   static RacerModule m_Mod = null;
   ExitDuringGameWindow m_ExitWnd = null;
   boolean m_bExitAllowed = true;
   static UseItem m_CurrBonus = null;
   Clock m_Clock = null;
   Weather m_Weather = new Weather();
   ScriptedEventList m_Events = new ScriptedEventList();
   KidsHud m_Hud = null;
   BarrelDummie dummie = null;
   static final int _KROLEWSKA = 0;
   static final int _KSIEZYC1 = 1;
   static final int _KSIEZYC2 = 2;
   static final int _KSIEZYC3 = 3;
   static final int _LAS1 = 4;
   static final int _LAS2 = 5;
   static final int _LAS3 = 6;
   static final int _PUSTYNIA1 = 7;
   static final int _PUSTYNIA2 = 8;
   static final int _PUSTYNIA3 = 9;
   static final int _TROPIK1 = 10;
   static final int _TROPIK2 = 11;
   static final int _TROPIK3 = 12;
   static final int _ZIMA1 = 13;
   static final int _ZIMA2 = 14;
   static final int _ZIMA3 = 15;
   static final int _TEST = 16;
   static final int _TESTTRACK = 17;
   static String[] MAPTEXNAMES = new String[]{
      "Data/Textures/Menu/Las1.png",
      "Data/Textures/Menu/Maps/Las2.png",
      "Data/Textures/Menu/Maps/Las3.png",
      "Data/Textures/Menu/Maps/Las1.png",
      "Data/Textures/Menu/Maps/Las2.png",
      "Data/Textures/Menu/Maps/Las3.png",
      "Data/Textures/Menu/Maps/Pustynia1.png",
      "Data/Textures/Menu/Maps/Pustynia2.png",
      "Data/Textures/Menu/Maps/Pustynia3.png",
      "Data/Textures/Menu/Maps/Pustynia1.png",
      "Data/Textures/Menu/Maps/Pustynia2.png",
      "Data/Textures/Menu/Maps/Pustynia3.png",
      "Data/Textures/Menu/Maps/Zima1.png",
      "Data/Textures/Menu/Maps/Zima2.png",
      "Data/Textures/Menu/Maps/Zima3.png",
      "Data/Textures/Menu/Maps/Zima1.png",
      "Data/Textures/Menu/Maps/Zima2.png",
      "Data/Textures/Menu/Maps/Zima3.png",
      "Data/Textures/Menu/Maps/Tropik1.png",
      "Data/Textures/Menu/Maps/Tropik2.png",
      "Data/Textures/Menu/Maps/Tropik3.png",
      "Data/Textures/Menu/Maps/Tropik1.png",
      "Data/Textures/Menu/Maps/Tropik2.png",
      "Data/Textures/Menu/Maps/Tropik3.png",
      "Data/Textures/Menu/Maps/Ksiezyc1.png",
      "Data/Textures/Menu/Maps/Ksiezyc2.png",
      "Data/Textures/Menu/Maps/Ksiezyc3.png",
      "Data/Textures/Menu/Maps/Ksiezyc1.png",
      "Data/Textures/Menu/Maps/Ksiezyc2.png",
      "Data/Textures/Menu/Maps/Ksiezyc3.png",
      "Data/Textures/Menu/Maps/Krolewska.png"
   };
   static String[] MAPNAMES = new String[]{
      "Data/Levels/Las1/Track.map",
      "Data/Levels/Las2/Track.map",
      "Data/Levels/Las3/Track.map",
      "Data/Levels/Las1/Track2.map",
      "Data/Levels/Las2/Track2.map",
      "Data/Levels/Las3/Track2.map",
      "Data/Levels/Pustynia1/Track.map",
      "Data/Levels/Pustynia2/Track.map",
      "Data/Levels/Pustynia3/Track.map",
      "Data/Levels/Pustynia1/Track2.map",
      "Data/Levels/Pustynia2/Track2.map",
      "Data/Levels/Pustynia3/Track2.map",
      "Data/Levels/Zima1/Track.map",
      "Data/Levels/Zima2/Track.map",
      "Data/Levels/Zima3/Track.map",
      "Data/Levels/Zima1/Track2.map",
      "Data/Levels/Zima2/Track2.map",
      "Data/Levels/Zima3/Track2.map",
      "Data/Levels/Tropik1/Track.map",
      "Data/Levels/Tropik2/Track.map",
      "Data/Levels/Tropik3/Track.map",
      "Data/Levels/Tropik1/Track2.map",
      "Data/Levels/Tropik2/Track2.map",
      "Data/Levels/Tropik3/Track2.map",
      "Data/Levels/Ksiezyc1/Track.map",
      "Data/Levels/Ksiezyc2/Track.map",
      "Data/Levels/Ksiezyc3/Track.map",
      "Data/Levels/Ksiezyc1/Track2.map",
      "Data/Levels/Ksiezyc2/Track2.map",
      "Data/Levels/Ksiezyc3/Track2.map",
      "Data/Levels/Krolewska/Track.map"
   };
   int m_iLoopMusicID = 0;
   String m_sLoopMusicName = null;
   float m_fMusicVolume = 0.0F;

   public void OnFrame() {
      this.m_fNowTime = this.GetTime();
      this.m_fFrameTime = this.m_fNowTime - this.m_fLastUpdateTime;
      // [mod] pas maxi : après une image très lente (chargement, alt-tab, saccade), la physique avançait d'un coup
      // de tout le temps écoulé (voiture qui saute, traverse un décor). On plafonne le pas ; le chrono de course,
      // lui, suit toujours le vrai temps (Clock).
      float fRealFrameTime = this.m_fFrameTime;
      if (this.m_fFrameTime > _MAX_FRAME_TIME) {
         this.m_fFrameTime = _MAX_FRAME_TIME;
      }

      for (int var1 = 0; var1 < this.m_RaceParams.m_PlayersTab.size(); var1++) {
         ((Vehicle)this.m_RaceParams.m_PlayersTab.elementAt(var1)).UpdateVehicle();
      }

      for (int var3 = 0; var3 < this.m_RaceParams.m_AITab.size(); var3++) {
         ((VehicleAI)this.m_RaceParams.m_AITab.elementAt(var3)).m_Vehicle.UpdateVehicle();
      }

      ModNet.RaceFrame(this); // [mod] en ligne : état envoyé, fantômes posés, top départ, attente à l'arrivée
      this.m_fLastUpdateTime = this.m_fNowTime;
      float var4 = this.m_Clock.Update();
      if (var4 > 0.0F) {
         if (this.m_Events != null) {
            while (this.m_Events.CheckAndActivate(this.m_Clock.GetMissionTime())) {
            }
         }

         float var2 = this.m_Clock.GetMissionTime();
         this.m_Weather.UpdateWeather(var2);
      }

      if (this.m_Hud != null) {
         this.m_Hud.Update(this.m_Clock.GetMissionExactTime());
      }

      // [mod] System.gc() était appelé à chaque image : 500 ramasse-miettes complets par seconde sur un PC moderne.
      // Une fois par seconde suffit (la JVM ramasse aussi d'elle-même quand il le faut).
      this.m_fGcTimer = this.m_fGcTimer + fRealFrameTime;
      if (this.m_fGcTimer >= _GC_PERIOD || this.m_fGcTimer < 0.0F) {
         this.m_fGcTimer = 0.0F;
         float fGcStart = ModFrameLimiter.Now();
         System.gc();
         float fGc = ModFrameLimiter.Now() - fGcStart;
         if (fGc > this.m_fGcWorst) {
            this.m_fGcWorst = fGc;
         }
      }

      // [mod] images par seconde moyennes de la course, dans crash.log toutes les 20 s (diagnostic du dérapage)
      if (fRealFrameTime > 0.0F && fRealFrameTime < 1.0F) {
         if (this.m_nFpsFrames == 0) {
            ModFrameLimiter.m_fWaitTotal = 0.0F;
         }

         this.m_nFpsFrames++;
         this.m_fFpsTime = this.m_fFpsTime + fRealFrameTime;
         if (fRealFrameTime > this.m_fFpsWorst) {
            this.m_fFpsWorst = fRealFrameTime;
         }

         if (this.m_fFpsTime >= 20.0F) {
            GameObject.CrashLog("[mod] course : " + (int)(this.m_nFpsFrames / this.m_fFpsTime) + " images/s en moyenne, pire image "
               + (int)(this.m_fFpsWorst * 1000.0F) + " ms, attente du limiteur " + (int)(100.0F * ModFrameLimiter.m_fWaitTotal / this.m_fFpsTime)
               + " % (" + ModOptions.GetSummary() + "), gc au pire " + ModFrameLimiter.Ms(this.m_fGcWorst) + " ms\n");
            this.m_nFpsFrames = 0;
            this.m_fFpsTime = 0.0F;
            this.m_fFpsWorst = 0.0F;
            this.m_fGcWorst = 0.0F;
            ModFrameLimiter.m_fWaitTotal = 0.0F;
         }
      }
   }

   // [mod] pas de physique maxi (s) : 0,05 s = 20 images/s ; en dessous, le jeu ralentit au lieu de sauter
   static final float _MAX_FRAME_TIME = 0.05F;
   // [mod] période du ramasse-miettes en course (s)
   static final float _GC_PERIOD = 1.0F;
   float m_fGcTimer = 0.0F;
   float m_fGcWorst = 0.0F;
   int m_nFpsFrames = 0;
   float m_fFpsTime = 0.0F;
   float m_fFpsWorst = 0.0F;

   void SetUpCameraPoints() {
      if (m_bIsDemo) {
         TrackCameraPoint.SetUpCameraPoints(this);
      }
   }

   void OnDeactivate() {
      this.StopMusicLoop();
      this.m_Hud.Hide();
      this.m_Clock.Stop();
      m_Cams[0].UnregisterKeyRequest();
      m_Cams[0].UnregisterMouseAxisRequest();
      m_Cams[0].UnregisterJoyAxisRequest();
      m_Cams[0].UnregisterJoyButtonRequest();
   }

   void StopMusicLoop() {
      if (this.m_iLoopMusicID != 0) {
         this.StopWave(this.m_iLoopMusicID);
         this.m_iLoopMusicID = 0;
         this.m_sLoopMusicName = null;
         this.DisableCallMethod("PlayMusicLoop");
      }
   }

   void DeleteBonuses() {
      GameObject[] var1 = GameObject.GetObjectsFromClass("BonusItem");

      for (int var2 = 0; var2 < var1.length; var2++) {
         ((BonusItem)var1[var2]).delete();
         var1[var2] = null;
      }

      this.m_Track.DeletePointsBonuses();
      this.m_AITrack.DeletePointsBonuses();
   }

   void PlayMusic(int var1, float var2) {
      this.StopMusicLoop();
      String var3 = "Data\\";
      this.m_fMusicVolume = var2;
      this.m_sLoopMusicName = var3 + CoreLogic.m_sWaveMusic[var1];
      GameObject.LogLine("RacerModule::PlayMusic: " + this.m_fMusicVolume);
      this.PlayMusicLoop();
   }

   static int GetMapsCnt() {
      return MAPNAMES.length;
   }

   static String GetLevelFileName(int var0) {
      return var0 >= 0 && var0 < MAPNAMES.length ? MAPNAMES[var0] : null;
   }

   void AddRot(float var1, float var2, float var3, float var4) {
      if (m_CurrBonus != null) {
         m_CurrBonus.m_XRot += var1;
         m_CurrBonus.m_YRot += var2;
         m_CurrBonus.m_ZRot += var3;
         m_CurrBonus.m_Scale += var4;
         m_CurrBonus.SetRotAndScale();
      }
   }

   void GetBonusObjects() {
      GameObject[] var1 = GameObject.GetObjectsFromClass("BonusItem");
      this.m_BonusTab = new BonusItem[var1.length];

      for (int var2 = 0; var2 < var1.length; var2++) {
         this.m_BonusTab[var2] = (BonusItem)var1[var2];
      }

      if (this.m_AITrack != null) {
         this.m_AITrack.AssignBonusObjectsToPoints();
      }
   }

   Clock GetClock() {
      if (this.m_Clock == null) {
         GameObject.CrashLog("m_Clock == null");
      }

      return this.m_Clock;
   }

   Weather GetWeather() {
      if (this.m_Weather == null) {
         GameObject.CrashLog("m_Weather == null");
      }

      return this.m_Weather;
   }

   void AddWeather(int var1, float var2) {
      this.m_Events.NewEvent(var1, var2);
   }

   public void StartCalculatePositions() {
      this.DisableCallMethod("CalcPos");
      this.CallMethodEvery("CalcPos", _PLACES_UPDATE);
   }

   public void StartRace() {
      this.DisableCallMethod("StartRace");
      this.m_Hud.StartLights();
   }

   public void StopCalculatePositions() {
      this.DisableCallMethod("CalcPos");
   }

   public void CheckForRaceEnd() {
      if (RacerGame.activeLevelModule != null) {
         if (RacerGame.game.activeMenu == 2 && RacerGame.game.m_bFinishRace) {
            RacerGame.game.EndModule();
         } else {
            this.CallMethodIn("CheckForRaceEnd", 1.0F);
         }
      }
   }

   void OnActivate() {
      this.m_Hud.Show();
      m_Cams[0].RegisterMouseAxisRequest();
      m_Cams[0].RegisterKeyRequest();
      m_Cams[0].RegisterJoyAxisRequest();
      m_Cams[0].RegisterJoyButtonRequest();
      // [mod] en ligne : les feux attendent le top départ de l'hôte (ModNet.RaceFrame)
      if (ModNet.IsRaceOnline()) {
         ModNet.OnRaceActivated();
      } else {
         this.CallMethodIn("StartRace", _TIME_BEFORE_RACE);
      }

      this.PlayMusic(RacerGame.GetCoreLogic().GetCurrWaveNum(), 1.0F);
      this.m_Hud.WrongWayHide();
   }

   RacerCamera GetRacerCamera() {
      if (m_Cams[0] == null) {
         GameObject.CrashLog("m_Camera == null");
      }

      return (RacerCamera)m_Cams[0];
   }

   void SetRot(float var1, float var2, float var3, float var4) {
      if (m_CurrBonus != null) {
         m_CurrBonus.m_XRot = var1;
         m_CurrBonus.m_YRot = var2;
         m_CurrBonus.m_ZRot = var3;
         m_CurrBonus.m_Scale = var4;
         m_CurrBonus.SetRotAndScale();
      }
   }

   void SetLoopVolume(float var1) {
      if (this.m_iLoopMusicID != 0) {
         this.m_fMusicVolume = var1;
         this.SetWaveVolume(this.m_iLoopMusicID, var1);
      }
   }

   public void OnLoadChunk(int var1, int var2, MapInputStream var3) {
      if (var1 == 10030) {
         this.m_Track = new Track();
         this.m_Track.Load(var3);
         this.m_Track.SetModule(this);
      }

      if (var1 == 10031) {
         this.m_Track.LoadBools(var3);
      }

      if (var1 == 10032) {
         this.m_Track.LoadAddLayers(var3);
         this.m_AITrack = new Track();
         this.m_AITrack.m_bLooped = this.m_Track.m_bLooped;
         this.m_AITrack.m_Mod = this.m_Track.m_Mod;
         this.m_AITrack.m_iAddLayers = this.m_Track.m_iAddLayers;

         for (int var4 = 0; var4 < this.m_Track.m_TrackPoints.size(); var4++) {
            if (this.m_Track.GetPoint(var4).m_bAIPoint) {
               TrackPoint var5 = new TrackPoint();
               var5.SetSlow(this.m_Track.GetPoint(var4));
               this.m_AITrack.AddPoint(var5);
            }
         }

         this.m_AITrack.CalculateAngles();
      }
   }

   public void CalcPos() {
      float var1 = 0.0F;
      Object var2 = null;

      for (int var3 = 0; var3 < this.m_RaceParams.m_SortTable.size() - 1; var3++) {
         var1 += ((Vehicle)this.m_RaceParams.m_SortTable.elementAt(var3)).m_fCoveredDistance;
         if (!((Vehicle)this.m_RaceParams.m_SortTable.elementAt(var3)).m_bRaceFinished) {
            for (int var4 = var3 + 1; var4 < this.m_RaceParams.m_SortTable.size(); var4++) {
               if (!((Vehicle)this.m_RaceParams.m_SortTable.elementAt(var4)).m_bRaceFinished
                  && ((Vehicle)this.m_RaceParams.m_SortTable.elementAt(var3)).m_fCoveredDistance
                     < ((Vehicle)this.m_RaceParams.m_SortTable.elementAt(var4)).m_fCoveredDistance) {
                  var2 = this.m_RaceParams.m_SortTable.elementAt(var3);
                  this.m_RaceParams.m_SortTable.setElementAt(this.m_RaceParams.m_SortTable.elementAt(var4), var3);
                  this.m_RaceParams.m_SortTable.setElementAt(var2, var4);
               }
            }
         }
      }

      var1 /= this.m_RaceParams.m_SortTable.size();
      Vehicle var8 = null;
      if (this.m_RaceParams.m_PlayersTab.size() > 0) {
         var1 = -100.0F;

         for (int var11 = 0; var11 < this.m_RaceParams.m_PlayersTab.size(); var11++) {
            var8 = (Vehicle)this.m_RaceParams.m_PlayersTab.elementAt(var11);
            if (var8.m_fCoveredDistance > var1) {
               var1 = var8.m_fCoveredDistance;
            }
         }
      }

      for (int var12 = 0; var12 < this.m_RaceParams.m_SortTable.size(); var12++) {
         var8 = (Vehicle)this.m_RaceParams.m_SortTable.elementAt(var12);
         if (var8.m_iCurrPlace > var12) {
            var8.PlayOutrunSound();
         }

         var8.m_iCurrPlace = var12;
         // [mod] IA améliorée (ModAI) : élastique doux et asymétrique selon la difficulté, en tours d'écart ; les
         // humains n'en ont plus (l'original ralentissait aussi le 2e joueur en tête en écran partagé) ; vitesse
         // de la difficulté et du caractère de chaque IA
         if (ModAI.IsImproved()) {
            if (var8.m_VehicleAI == null) {
               var8.m_fCurrMaxSpeed = var8.GetDefaultMaxSpeed();
            } else {
               float fGap = (var1 - var8.m_fCoveredDistance) * m_Mod.m_RaceParams.m_iNofLoops;
               // [mod] jamais en ligne : l'élastique ne regarderait que la voiture de l'hôte
               boolean bUp = ModOptions.m_bAiCatchUp && !ModNet.IsRaceOnline();
               float fCatch = bUp ? ModAI.CatchUp(fGap) : 0.0F;
               var8.m_fCurrMaxSpeed = var8.GetDefaultMaxSpeed() * ModAI._MODE_SPEED[ModAI.Mode()] * var8.m_VehicleAI.m_fModSpeed * (1.0F + fCatch);
               // tenue de route du joueur, quel que soit le niveau (plus de bonus de virage à la traîne)
               var8.m_VehicleAI.ModApplyHandling();
            }

            continue;
         }

         float var5 = Math.abs(var1 - var8.m_fCoveredDistance);
         if (var5 > var8.m_Params._MAX_SPEED_LEVELING_DIST) {
            var5 = 1.0F;
         } else if (var5 < var8.m_Params._MIN_SPEED_LEVELING_DIST) {
            var5 = 0.0F;
         } else {
            var5 = (var5 - var8.m_Params._MIN_SPEED_LEVELING_DIST) / (var8.m_Params._MAX_SPEED_LEVELING_DIST - var8.m_Params._MIN_SPEED_LEVELING_DIST);
         }

         if (var1 - var8.m_fCoveredDistance < 0.0F) {
            var5 *= -1.0F;
         }

         // [mod] « Rattrapage des IA » du menu Mods : sans lui, chaque voiture garde sa vitesse de base (l'original
         // accélère jusqu'à +40 % ceux qui sont derrière le premier humain et ralentit ceux qui sont devant), et les
         // IA à la traîne ne reçoivent plus le bonus de virage / dérapage.
         boolean bCatchUp = ModOptions.m_bAiCatchUp && !ModNet.IsRaceOnline(); // [mod] jamais en ligne
         if (!bCatchUp) {
            var5 = 0.0F;
         }

         var8.m_fCurrMaxSpeed = var8.GetDefaultMaxSpeed() * (1.0F + var5 * var8.m_Params._SPEED_LEVELING_COEF);
         if (var8.m_VehicleAI != null) {
            if (bCatchUp && (var1 - var8.m_fCoveredDistance) * m_Mod.m_RaceParams.m_iNofLoops > 0.2F) {
               var8.m_VehicleAI.SetSlideModifier(var8.m_VehicleAI.m_iAILevel + 2);
               var8.m_VehicleAI.SetTurningLevelModifier(var8.m_VehicleAI.m_iAILevel + 2);
            } else {
               var8.m_VehicleAI.SetSlideModifier(var8.m_VehicleAI.m_iAILevel);
               var8.m_VehicleAI.SetTurningLevelModifier(var8.m_VehicleAI.m_iAILevel);
            }
         }
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
            GameObject.LogLine("RacerModule::m_fMusicVolume: " + this.m_fMusicVolume);
            float var1 = this.GetWaveDuration(this.m_iLoopMusicID);
            this.CallMethodIn("PlayMusicLoop", var1);
         }
      }
   }

   ScriptedEventList GetEvents() {
      return this.m_Events;
   }

   void ReverseTrack() {
      if (this.m_Track != null && this.m_AITrack != null) {
         this.m_Track.ReverseTrack();
         this.m_AITrack.ReverseTrack();
         ModAIMap.OnReverse(); // [mod] carte de l'IA refaite sur les portes retournées (SetStartPos juste après)
         this.GetBonusObjects();
         this.m_RaceParams.SetStartPos(this.m_RaceParams.m_vReverseStartPos);
      }
   }

   void InitTracers() {
      for (int var1 = 0; var1 < this.m_RaceParams.m_AITab.size(); var1++) {
         ((VehicleAI)this.m_RaceParams.m_AITab.elementAt(var1))
            .m_Vehicle
            .FromUpForwardPos(Vector.Up, Vector.Right, ((VehicleAI)this.m_RaceParams.m_AITab.elementAt(var1)).m_Vehicle.GetPosition());
         ((VehicleAI)this.m_RaceParams.m_AITab.elementAt(var1)).m_Vehicle.InitTracer();
      }

      for (int var2 = 0; var2 < this.m_RaceParams.m_PlayersTab.size(); var2++) {
         ((Vehicle)this.m_RaceParams.m_PlayersTab.elementAt(var2))
            .FromUpForwardPos(Vector.Up, Vector.Right, ((Vehicle)this.m_RaceParams.m_PlayersTab.elementAt(var2)).GetPosition());
         ((Vehicle)this.m_RaceParams.m_PlayersTab.elementAt(var2)).InitTracer();
      }
   }

   void OnCreate() {
      this.m_Clock = new Clock(this);
      GameSerializeSettings var1 = RacerGame.GetGSS();
      Vector var2 = this.GetTerrainSize();
      var2.fY += 100000.0F;
      this.SetExtents(var2);
      BaseCamera.SetModule(this);
      RacerCamera var3 = null;
      if (m_bIsDemo) {
         var3 = (RacerCamera)this.CreateObjectNoInit("RacerDemoCamera");
      } else {
         var3 = (RacerCamera)this.CreateObjectNoInit("RacerCamera");
      }

      var3.SetCameraDistance(var1.m_CameraDistance0);
      m_Cams[0] = var3;
      if (RacerGame.GetCoreLogic().IsMultiplayer() && !m_bIsDemo) {
         var3 = (RacerCamera)this.CreateObjectNoInit("RacerCamera");
         var3.SetCameraDistance(var1.m_CameraDistance0);
         m_Cams[1] = var3;
      }

      this.InitFXSystem();
      this.m_fLastUpdateTime = this.GetTime();
      m_Mod = this;
      this.dummie = UseItem.CreateBarrelDummie(null);
      this.m_ExitWnd = (ExitDuringGameWindow)this.CreateObject("ExitDuringGameWindow");
      this.AddSprite(this.m_ExitWnd);
   }

   void OnInputKey(int var1, boolean var2, char var3) {
   }

   void UpdateVehiclesParamsEqual() {
      for (int var1 = 0; var1 < this.m_RaceParams.m_Vehicles.size(); var1++) {
         ((Vehicle)this.m_RaceParams.m_Vehicles.elementAt(var1)).UpdateMaxSpeedAccelEqualAll();
      }
   }

   void UpdateVehiclesParams() {
      for (int var1 = 0; var1 < this.m_RaceParams.m_Vehicles.size(); var1++) {
         ((Vehicle)this.m_RaceParams.m_Vehicles.elementAt(var1)).UpdateMaxSpeedAccel();
      }
   }

   boolean IsTrackExtra() {
      int var1 = RacerGame.firstMenuModule.GetMenuCX();
      return EMenuCX.IsExtra(var1);
   }

   static void SetDemoModule(boolean var0) {
      m_bIsDemo = var0;
   }

   public void InitModule() {
      CoreLogic var1 = RacerGame.GetCoreLogic();
      this.SetTreesLODSelectionFactor(PerformanceSettings._TREES_LOD[PerformanceSettings.m_iCurrQuality]);
      this.SetTreesVisibilityRange(PerformanceSettings._TREES_CLIP[PerformanceSettings.m_iCurrQuality]);
      this.SetObjectsVisibilityRange(
         0.8F * PerformanceSettings._OBJECTS_CLIP[PerformanceSettings.m_iCurrQuality], PerformanceSettings._OBJECTS_CLIP[PerformanceSettings.m_iCurrQuality]
      );
      m_Cams[0].InitObject();
      m_CamsIDs[0] = this.AddView(m_Cams[0], 0.0F, 0.0F, 1.0F, 1.0F);
      this.ListenerOnViewActive(m_CamsIDs[0], true);
      RacerGame.game.SetScreenshotView(this, m_CamsIDs[0]);
      if (RacerGame.GetCoreLogic().IsMultiplayer()) {
         this.SetViewSize(m_CamsIDs[0], 1.0F, 0.5F);
         this.SetViewOrigin(m_CamsIDs[0], 0.0F, 0.5F);
         m_Cams[1].InitObject();
         m_CamsIDs[1] = this.AddView(m_Cams[1], 0.0F, 0.0F, 1.0F, 0.5F);
         this.ListenerOnViewActive(m_CamsIDs[1], true);
      }

      this.GetBonusObjects();
      this.m_RaceParams = new RaceParams(this);
      if (!m_bIsDemo) {
         RacerGame.GetKb0().AttachToController(this, this.m_RaceParams, 0);
         if (RacerGame.GetCoreLogic().IsMultiplayer()) {
            RacerGame.GetKb1().AttachToController(this, this.m_RaceParams, 1);
         }
      }

      this.m_Hud = (KidsHud)this.CreateObject("KidsHud");
      this.AddSprite(this.m_Hud);
      this.m_Hud.InitHud(this, var1.IsMultiplayer());
      this.m_RaceParams.m_Params = new VehicleParams(RacerGame.GetCoreLogic().GetLapsNo());
      IntList var2 = RacerGame.GetCoreLogic().GetSelectedAnimals();
      this.m_RaceParams.m_NextNo = 0;
      byte var3 = 1;
      if (!m_bIsDemo) {
         if (var2.m_Cnt > 0) {
            this.m_RaceParams.AddPlayer(var2.GetAt(0));
         }

         if (RacerGame.GetCoreLogic().IsMultiplayer()) {
            this.m_RaceParams.AddPlayer(var2.GetAt(1));
            var3 = 2;
         }
      } else {
         var3 = 0;
      }

      // [mod] en ligne : les autres animaux sont les voitures des autres joueurs (fantômes) ; chez l'hôte, les IA
      // du salon sont de vraies IA (ModNet.CreateGhosts → RaceParams.AddModAI), des fantômes chez les autres
      if (ModNet.IsRaceOnline()) {
         ModNet.CreateGhosts(this.m_RaceParams, var2);
         var3 = (byte)var2.m_Cnt;
      }

      for (int var4 = var3; var4 < var2.m_Cnt; var4++) {
         this.m_RaceParams.AddAI(var2.GetAt(var4));
      }

      this.m_RaceParams.m_iNofLoops = RacerGame.GetCoreLogic().GetLapsNo();
      this.CallMethodIn("CheckForRaceEnd", 1.0F);
      if (m_bIsDemo) {
         RacerDemoCamera var11 = (RacerDemoCamera)m_Cams[0];
         var11.SetNextObserved();
      }

      this.SetUpCameraPoints();
      int var12 = RacerGame.firstMenuModule.GetMenuCX();
      var12 = RacerGame.GetCoreLogic().ConvertExtraToOrd(var12);
      if (var12 != -1) {
         if (var12 >= 0 && var12 <= 2) {
            this.m_RaceParams.m_AvailableBonuses = new LevelBonusTypes();
            this.m_RaceParams.m_AvailableBonuses.m_bModFixed = true; // [mod] règle d'extra : pas de bonus ajouté (ModBonuses)
            this.m_RaceParams.m_AvailableBonuses.AddBonusType(3);
            RacerGame.GetCoreLogic().SetLapsNo(3);
            this.m_RaceParams.m_iNofLoops = RacerGame.GetCoreLogic().GetLapsNo();
         }

         if (var12 >= 3 && var12 <= 5) {
            this.m_RaceParams.m_AvailableBonuses = new LevelBonusTypes();
            this.m_RaceParams.m_AvailableBonuses.m_bModFixed = true; // [mod]
            this.m_RaceParams.m_AvailableBonuses.AddBonusType(0);
            RacerGame.GetCoreLogic().SetLapsNo(3);
            this.m_RaceParams.m_iNofLoops = RacerGame.GetCoreLogic().GetLapsNo();
         }

         if (var12 >= 6 && var12 <= 8) {
            this.m_RaceParams.m_AvailableBonuses = new LevelBonusTypes();
            this.m_RaceParams.m_AvailableBonuses.m_bModFixed = true; // [mod]
            this.DeleteBonuses();
            RacerGame.GetCoreLogic().SetLapsNo(3);
            this.m_RaceParams.m_iNofLoops = RacerGame.GetCoreLogic().GetLapsNo();
         }

         if (var12 >= 9 && var12 <= 11) {
            this.m_RaceParams.m_Params._GRAVITY_ACCEL = VehicleParams._EXTRA_GRAVITY_ACCEL;
            RacerGame.GetCoreLogic().SetLapsNo(3);
            this.m_RaceParams.m_iNofLoops = RacerGame.GetCoreLogic().GetLapsNo();
         }

         if (var12 >= 12 && var12 <= 14) {
            RacerGame.GetCoreLogic().SetLapsNo(VehicleParams._POWER_RACE_LOPS);
            this.m_RaceParams.m_iNofLoops = RacerGame.GetCoreLogic().GetLapsNo();
         }
      }

      // [mod] en ligne sans bonus (choix de l'hôte, NetBonus(0)) : boîtes retirées comme l'extra « sans bonus » ;
      // entraînement des IA : sans bonus aussi, sauf Bonus(1) (ModTraining)
      if (ModNet.IsRaceOnline() && !ModNet.m_bBonus || m_bIsDemo && ModTraining.m_bRunning && !ModTraining.m_bBonus) {
         this.m_RaceParams.m_AvailableBonuses = new LevelBonusTypes();
         this.m_RaceParams.m_AvailableBonuses.m_bModFixed = true;
         this.DeleteBonuses();
      }

      GameObject[] var6 = GameObject.GetObjectsOfClass("RiverSound");
      if (var6 != null) {
         for (int var5 = 0; var5 < var6.length; var5++) {
            ((RiverSound)var6[var5]).InitSound();
         }
      }

      if (PerformanceSettings.m_iCurrQuality == PerformanceSettings._QUALITY_MIN) {
         GameObject[] var7 = GameObject.GetObjectsOfClass("Bridge");
         if (var7 != null) {
            for (int var14 = 0; var14 < var7.length; var14++) {
               ((MeshObject)var7[var14]).SetMeshVisibilityRange(9000.0F, 11000.0F);
            }
         }

         var7 = GameObject.GetObjectsOfClass("StoneBridge");
         if (var7 != null) {
            for (int var15 = 0; var15 < var7.length; var15++) {
               ((MeshObject)var7[var15]).SetMeshVisibilityRange(6000.0F, 7000.0F);
            }
         }

         GameObject var8 = GameObject.GetObjectByName("pienLas3_01");
         if (var8 != null && var8 instanceof MeshObject) {
            ((MeshObject)var8).SetMeshVisibilityRange(6000.0F, 7000.0F);
         }

         var8 = GameObject.GetObjectByName("pien03");
         if (var8 != null && var8 instanceof MeshObject) {
            ((MeshObject)var8).SetMeshVisibilityRange(6000.0F, 7000.0F);
         }

         GameObject var9 = GameObject.GetObjectByName("swiatynia01");
         if (var9 != null && var9 instanceof MeshObject) {
            ((MeshObject)var9).SetMeshVisibilityRange(8000.0F, 9000.0F);
         }

         var9 = GameObject.GetObjectByName("swiatynia02");
         if (var9 != null && var9 instanceof MeshObject) {
            ((MeshObject)var9).SetMeshVisibilityRange(8000.0F, 9000.0F);
         }
      }

      GameObject[] var21 = GameObject.GetObjectsOfClass("WaterfallSound");
      if (var21 != null) {
         for (int var16 = 0; var16 < var21.length; var16++) {
            ((WaterfallSound)var21[var16]).InitSound();
         }
      }

      GameObject[] var23 = GameObject.GetObjectsOfClass("WindSound");
      if (var23 != null) {
         for (int var17 = 0; var17 < var23.length; var17++) {
            ((WindSound)var23[var17]).InitSound();
         }
      }

      GameObject[] var25 = GameObject.GetObjectsFromClass("MultiSoundDummy");
      if (var25 != null) {
         for (int var18 = 0; var18 < var25.length; var18++) {
            ((MultiSoundDummy)var25[var18]).InitSound();
         }
      }

      GameObject[] var10 = GameObject.GetObjectsOfClass("IntroLoopSound");
      if (var23 != null) {
         for (int var19 = 0; var19 < var10.length; var19++) {
            ((IntroLoopSound)var10[var19]).InitSound();
         }
      }

      this.ActivateView(m_CamsIDs[0]);
      if (RacerGame.GetCoreLogic().IsMultiplayer()) {
         this.ActivateView(m_CamsIDs[1]);
      }

      this.m_bExitAllowed = true;
   }
}
