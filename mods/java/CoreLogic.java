import java.io.IOException;

public class CoreLogic {
   static String _PET_RACER_CD_NAME = "PetRacer";
   static final int MUSIC_TRACK01 = 0;
   static final int MUSIC_TRACK02 = 1;
   static final int MUSIC_TRACK03 = 2;
   static final int MUSIC_TRACK04 = 3;
   static final int MUSIC_TRACK05 = 4;
   static final int MUSIC_TRACK06 = 5;
   static final int MUSIC_TRACK07 = 6;
   static final int MUSIC_MENU = 7;
   static final int MUSIC_INTRO = 8;
   static final int MUSIC_OUTRO = 9;
   static final int MUSIC_RACEWIN = 10;
   static final int MUSIC_RACELOST = 11;
   static String[] m_sWaveMusic = new String[]{
      "Music\\track01.wav",
      "Music\\track02.wav",
      "Music\\track03.wav",
      "Music\\track04.wav",
      "Music\\track05.wav",
      "Music\\track06.wav",
      "Music\\track07.wav",
      "Music\\menu.wav",
      "Music\\intro.wav",
      "Music\\outro.wav",
      "Music\\win.wav",
      "Music\\lost.wav"
   };
   int m_LastBonus = -1;
   boolean m_bBonusAdded = false;
   boolean m_bFirstPosition = false;
   boolean m_bFailedLand = false;
   int m_iCurrWaveNum = 0;
   static final int _EXTRA_AVAILABLE = 1;
   static final int _EXTRA_FIRST_PRIZE = 2;
   java.util.Vector m_Players = new java.util.Vector();
   String m_Name = null;
   static final int _EXTRA_RACES = 15;
   int[] m_ExtraAvailable = new int[15];
   static final int _MAX_DIFF_LEVEL = 1;
   int m_nAdventureDiffLevel = 0;
   boolean m_bAdventureInProgress = false;
   int m_nPassedTracks = 0;
   static final int _MAX_ANIMALS_IN_RACE = 6;
   int[] m_aCurrentAnimals = new int[6];
   int[] m_aStoredAnimals = new int[6];
   int m_AnimalsCntInRace = 6;
   int m_StoredAnimalsCntInRace = 6;
   static final int _ALL_TRACKS = 31;
   static final int _ALL_ANIMALS = 12;
   int m_nAvailableTracks = 1;
   int m_nAvailableAnimals = 6;
   static boolean m_bIsLocked = false;
   static boolean m_bPLLocked = false;
   static boolean[] m_aAnimalLock = new boolean[12];
   static boolean[] m_aTrackLock = new boolean[33];
   static boolean[] m_aLandLock = new boolean[6];
   RecordEntry[] m_aRecords = new RecordEntry[31];
   int m_SelectedAnimalID_0 = -1;
   int m_SelectedAnimalID_1 = -1;
   boolean m_bMultiplayer = false;
   IntList m_SelectedAnimals = null;
   int m_Laps = -1;
   boolean m_bReverseTrack = false;
   TimeList m_TimeList = new TimeList(9);
   boolean m_bPlayOutro = false;
   int m_nNextTrack = 0;
   boolean m_bEndAdventure = false;
   int m_TrackMusic = 0;

   void PrepareTrackBeforeRace(int var1) {
      CoreLogic var2 = RacerGame.GetCoreLogic();
      switch (var1) {
         case 0:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(1);
            break;
         case 1:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(6);
            break;
         case 2:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(0);
            break;
         case 3:
            this.SetReverseTrack(true);
            RacerGame.activeLevelModule.AddWeather(5, 5.0F);
            RacerGame.activeLevelModule.AddWeather(6, 40.0F);
            RacerGame.activeLevelModule.AddWeather(1, 120.0F);
            var2.SetCurrWaveNum(6);
            break;
         case 4:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(1);
            break;
         case 5:
            this.SetReverseTrack(true);
            RacerGame.activeLevelModule.AddWeather(1, 1.0F);
            var2.SetCurrWaveNum(6);
            break;
         case 6:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(2);
            break;
         case 7:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(0);
            break;
         case 8:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(2);
            break;
         case 9:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(1);
            break;
         case 10:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(2);
            break;
         case 11:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(2);
            break;
         case 12:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(5);
            break;
         case 13:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(1);
            break;
         case 14:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(5);
            break;
         case 15:
            this.SetReverseTrack(false);
            RacerGame.activeLevelModule.AddWeather(2, 1.0F);
            var2.SetCurrWaveNum(0);
            break;
         case 16:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(5);
            break;
         case 17:
            this.SetReverseTrack(true);
            RacerGame.activeLevelModule.AddWeather(2, 1.0F);
            var2.SetCurrWaveNum(5);
            break;
         case 18:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(3);
            break;
         case 19:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(0);
            break;
         case 20:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(3);
            break;
         case 21:
            this.SetReverseTrack(true);
            RacerGame.activeLevelModule.AddWeather(1, 1.0F);
            var2.SetCurrWaveNum(3);
            break;
         case 22:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(1);
            break;
         case 23:
            this.SetReverseTrack(true);
            RacerGame.activeLevelModule.AddWeather(1, 1.0F);
            var2.SetCurrWaveNum(3);
            break;
         case 24:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(4);
            break;
         case 25:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(1);
            break;
         case 26:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(4);
            break;
         case 27:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(0);
            break;
         case 28:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(4);
            break;
         case 29:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(4);
            break;
         case 30:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(6);
            break;
         case 31:
            this.SetReverseTrack(true);
            var2.SetCurrWaveNum(0);
            break;
         case 32:
            this.SetReverseTrack(false);
            var2.SetCurrWaveNum(3);
      }

      if (var2.IsReverseTrack()) {
         RacerModule.m_Mod.ReverseTrack();
      }
   }

   boolean UpdatePlayerSave() {
      if (this.m_Name == null) {
         return false;
      }

      PlayerSave var1 = this.FindPlayerSaveByName(this.m_Name);
      if (var1 == null) {
         return false;
      }

      var1.CopyFromLog(this);
      return true;
   }

   void AddNewTime(int var1, int var2, float var3) {
      TimeEntry var4 = new TimeEntry(var1, var2, var3);
      this.m_TimeList.Push(var4);
   }

   IntList GetSelectedAnimals() {
      return this.m_SelectedAnimals;
   }

   void StoreAnimals() {
      int var1 = 0;

      do {
         this.m_aStoredAnimals[var1] = this.m_aCurrentAnimals[var1];
      } while (++var1 < 6);

      this.m_StoredAnimalsCntInRace = this.m_AnimalsCntInRace;
   }

   void AdventureMissionSettings(int var1, int var2) {
      switch (var1) {
         case 0:
            switch (var2) {
               case 0:
            }
      }
   }

   PlayerSave FindPlayerSaveByName(String var1) {
      PlayerSave var2 = null;
      int var3 = this.m_Players.size();

      for (int var4 = 0; var4 < var3; var4++) {
         var2 = (PlayerSave)this.m_Players.elementAt(var4);
         if (var2.m_Name.equals(var1)) {
            return var2;
         }
      }

      return null;
   }

   void RestoreAnimals() {
      int var1 = 0;

      do {
         this.m_aCurrentAnimals[var1] = this.m_aStoredAnimals[var1];
      } while (++var1 < 6);

      this.m_AnimalsCntInRace = this.m_StoredAnimalsCntInRace;
   }

   void CreatePlayersForAdventure(int var1) {
      this.m_SelectedAnimalID_0 = var1;
      int var2 = 0;

      do {
         this.m_aCurrentAnimals[var2] = var2++;
      } while (var2 < 6);

      this.m_aCurrentAnimals[0] = var1;
      this.m_aCurrentAnimals[var1] = 0;
      this.m_AnimalsCntInRace = 6;
      this.m_SelectedAnimals = IntList.MakeIntListFromIntArray(this.m_aCurrentAnimals, this.m_AnimalsCntInRace);
      this.m_bAdventureInProgress = true;
      this.m_nNextTrack = 0;
      this.m_nPassedTracks = 0;
      this.m_Laps = 3;
   }

   void AddBonus(int var1) {
      if (var1 >= 0 && var1 < this.m_ExtraAvailable.length) {
         this.m_bBonusAdded = true;
         this.m_LastBonus = var1;
         if (this.m_ExtraAvailable[var1] < 1) {
            this.m_ExtraAvailable[var1] = 1;
         }
      }
   }

   boolean IsExtraFirstPrize(int var1) {
      return this.m_ExtraAvailable[var1] >= 2;
   }

   void SetNextTrack(int var1) {
      if (var1 < 0) {
         var1 = 0;
      }

      if (var1 >= 31) {
         var1 = 30;
      }

      this.m_nNextTrack = var1;
   }

   void AddNewAnimal(int var1) {
      if (var1 > this.m_nAvailableAnimals) {
         this.m_nAvailableAnimals = var1;
      }

      this.m_nAvailableAnimals = var1;
      if (this.m_nAvailableAnimals > 12) {
         this.m_nAvailableAnimals = 12;
      }
   }

   void SetLapsNo(int var1) {
      this.m_Laps = var1;
   }

   void IncreaseDiffLevel() {
      this.m_nAdventureDiffLevel++;
      if (this.m_nAdventureDiffLevel > 1) {
         this.m_nAdventureDiffLevel = 1;
      }
   }

   void MakeProgressAfterAdventureRace() {
      this.m_bFailedLand = false;
      if (RacerGame.firstMenuModule.GetMenuCX() == 1) {
         if (this.m_TimeList != null && this.m_TimeList.GetCnt() >= 1) {
            boolean var1 = false;
            if (this.m_TimeList.GetAt(0).m_AnimalID == this.GetSelectedAnimals().GetAt(0)) {
               var1 = true;
            }

            this.m_bFirstPosition = var1;
            if (var1) {
               switch (this.m_nPassedTracks) {
                  case 0:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 1:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddBonus(0);
                     return;
                  case 2:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 3:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddBonus(3);
                     return;
                  case 4:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddNewAnimal(7);
                     this.StoreAnimals();
                     this.InsertNewBossOnly(6);
                     return;
                  case 5:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.RestoreAnimals();
                     this.InsertNewBoss(6);
                     this.AddBonus(6);
                     return;
                  case 6:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 7:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddBonus(9);
                     return;
                  case 8:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 9:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddBonus(1);
                     return;
                  case 10:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.StoreAnimals();
                     this.InsertNewBossOnly(7);
                     return;
                  case 11:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddNewAnimal(8);
                     this.RestoreAnimals();
                     this.InsertNewBoss(7);
                     this.AddBonus(12);
                     return;
                  case 12:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 13:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddBonus(7);
                     return;
                  case 14:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 15:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddBonus(10);
                     return;
                  case 16:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.StoreAnimals();
                     this.InsertNewBossOnly(8);
                     return;
                  case 17:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddNewAnimal(9);
                     this.RestoreAnimals();
                     this.InsertNewBoss(8);
                     this.AddBonus(4);
                     return;
                  case 18:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 19:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddBonus(11);
                     return;
                  case 20:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 21:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddBonus(13);
                     return;
                  case 22:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.StoreAnimals();
                     this.InsertNewBossOnly(9);
                     return;
                  case 23:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddNewAnimal(10);
                     this.RestoreAnimals();
                     this.InsertNewBoss(9);
                     this.AddBonus(2);
                     return;
                  case 24:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 25:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddBonus(8);
                     return;
                  case 26:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 27:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddBonus(5);
                     return;
                  case 28:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.StoreAnimals();
                     this.InsertNewBossOnly(10);
                     return;
                  case 29:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.AddNewAnimal(11);
                     this.AddBonus(14);
                     this.InsertNewBoss(11);
                     return;
                  case 30:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 31:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     return;
                  case 32:
                     this.m_nPassedTracks++;
                     this.AddNewTrack(this.m_nPassedTracks);
                     this.IncreaseDiffLevel();
                     this.m_nPassedTracks = 0;
                     this.m_nNextTrack = 0;
                     this.m_bAdventureInProgress = false;
                     this.AddNewAnimal(12);
                     this.m_bPlayOutro = true;
                     this.SetEndAdventure(true);
                     return;
               }
            } else {
               switch (this.m_nPassedTracks) {
                  case 0:
                  case 1:
                  case 2:
                  case 3:
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
                  case 19:
                  case 20:
                  case 21:
                  case 22:
                  case 23:
                  case 24:
                  case 25:
                  case 26:
                  case 27:
                  case 28:
                  case 29:
                  case 30:
                  case 31:
                  case 32:
               }
            }
         }
      }
   }

   void SetCheatAllTracks() {
      this.m_nAvailableTracks = 31;
   }

   void ClearTimeList() {
      this.m_TimeList.Clear();
   }

   TimeList GetTimeList() {
      return this.m_TimeList;
   }

   void SetLocked() {
      if (m_bIsLocked) {
         this.m_nAvailableTracks = 31;
         this.m_nAvailableAnimals = 12;
      }
   }

   void SetCurrWaveNum(int var1) {
      this.m_iCurrWaveNum = var1;
   }

   void FillRandomAnimals(int var1, int var2) {
      int var3 = var2 - 1;
      if (var1 == 3) {
         var3--;
      }

      IntList var4 = new IntList(var2);
      IntList var5 = null;
      var5 = new IntList(this.GetAvailableAnimalsCount()); // [mod] déblocage du menu Mods

      for (int var6 = 0; var6 < this.GetAvailableAnimalsCount(); var6++) {
         if (this.m_SelectedAnimalID_0 != var6 && (var1 != 3 || this.m_SelectedAnimalID_1 != var6)) {
            var5.Add(var6);
         }
      }

      for (int var10 = 0; var10 < var5.m_Cnt; var10++) {
         int var7 = RacerGame.GetRandom(0, 100) % var5.m_Cnt;
         int var8 = RacerGame.GetRandom(0, 100) % var5.m_Cnt;
         var5.SwapElems(var7, var8);
      }

      var4.Add(this.m_SelectedAnimalID_0);
      if (var1 == 3) {
         var4.Add(this.m_SelectedAnimalID_1);
      }

      for (int var11 = 0; var11 < var3; var11++) {
         var4.Add(var5.Pop());
      }

      this.m_SelectedAnimals = var4;
   }

   void SetCheatAllAnimals() {
      this.m_nAvailableAnimals = 12;
   }

   boolean _CompareTimeEntries(TimeEntry var1, int var2) {
      return var1 == null ? false : var1.m_Time < this.m_aRecords[var2].m_Time;
   }

   static int ConvertFromLapsToSlider(int var0) {
      switch (var0) {
         case 3:
            return 0;
         case 4:
         case 6:
         default:
            return 0;
         case 5:
            return 1;
         case 7:
            return 2;
      }
   }

   boolean IsLandLocked(int var1) {
      return var1 >= 0 && var1 < m_aLandLock.length ? m_aLandLock[var1] : true;
   }

   int GetCurrWaveNum() {
      return this.m_iCurrWaveNum;
   }

   public void SetDefault() {
      this.m_Players.removeAllElements();
      this.m_Name = "default";
      int var1 = 0;

      do {
         this.m_ExtraAvailable[var1] = 0;
      } while (++var1 < 15);

      this.m_nAdventureDiffLevel = 0;
      this.m_bAdventureInProgress = false;
      this.m_nPassedTracks = 0;
      var1 = 0;

      do {
         this.m_aCurrentAnimals[var1] = var1;
         this.m_aStoredAnimals[var1] = var1++;
      } while (var1 < 6);

      this.m_AnimalsCntInRace = 6;
      this.m_StoredAnimalsCntInRace = 6;
      this.m_nAvailableTracks = 1;
      this.m_nAvailableAnimals = 6;
      var1 = 0;

      do {
         this.m_aRecords[var1] = new RecordEntry();
      } while (++var1 < 31);
   }

   void PrepareForAdventure() {
      if (this.m_bAdventureInProgress) {
         this.m_SelectedAnimals = IntList.MakeIntListFromIntArray(this.m_aCurrentAnimals, this.m_AnimalsCntInRace);
         this.m_Laps = 3;
         this.SetNextTrack(this.m_nPassedTracks);
      }
   }

   boolean IsReverseTrack() {
      return this.m_bReverseTrack;
   }

   void SetReverseTrack(boolean var1) {
      this.m_bReverseTrack = var1;
   }

   int GetNextTrack() {
      return this.m_nNextTrack;
   }

   int GetLapsNo() {
      return this.m_Laps;
   }

   boolean Load(KidsStream var1) {
      try {
         if (!var1.ReadMarker(1100)) {
            return false;
         }

         int var2 = 0;

         do {
            this.m_aRecords[var2] = new RecordEntry();
            this.m_aRecords[var2].m_Name = var1.in.readUTF();
            this.m_aRecords[var2].m_Time = var1.in.readFloat();
         } while (++var2 < 31);

         PlayerSave var3 = null;
         this.m_Players.removeAllElements();
         int var4 = var1.in.readInt();

         for (int var6 = 0; var6 < var4; var6++) {
            var3 = new PlayerSave();
            if (!var3.Load(var1)) {
               return false;
            }

            this.m_Players.addElement(var3);
         }

         return var1.ReadMarker(1104);
      } catch (IOException var5) {
         GameObject.LogLine("CoreLogic::Save() - IOException");
         return false;
      }
   }

   void SetNewRecord(RecordEntry var1) {
      if (this.m_nNextTrack >= 0 && this.m_nNextTrack < this.m_aRecords.length) {
         this.m_aRecords[this.m_nNextTrack] = var1;
      }
   }

   void InsertNewBossOnly(int var1) {
      this.m_AnimalsCntInRace = 2;
      this.m_aCurrentAnimals[1] = var1;
   }

   boolean IsAdventureInProgress() {
      return this.m_bAdventureInProgress;
   }

   int ConvertExtraToOrd(int var1) {
      switch (var1) {
         case 4:
            return 0;
         case 5:
            return 1;
         case 6:
            return 2;
         case 7:
            return 3;
         case 8:
            return 4;
         case 9:
            return 5;
         case 10:
            return 6;
         case 11:
            return 7;
         case 12:
            return 8;
         case 13:
            return 9;
         case 14:
            return 10;
         case 15:
            return 11;
         case 16:
            return 12;
         case 17:
            return 13;
         case 18:
            return 14;
         default:
            return -1;
      }
   }

   boolean CanPlayOutro() {
      if (this.m_bPlayOutro) {
         this.m_bPlayOutro = false;
         return true;
      } else {
         return false;
      }
   }

   void RemovePlayer(PlayerSave var1) {
      int var2 = this.m_Players.indexOf(var1);
      if (var2 != -1) {
         this.m_Players.removeElementAt(var2);
      }
   }

   TimeEntry _FindTimeByPlayer(int var1) {
      TimeEntry var2 = null;

      for (int var3 = 0; var3 < this.m_TimeList.GetCnt(); var3++) {
         var2 = this.m_TimeList.GetAt(var3);
         if (var2.m_AnimalPos == var1) {
            return var2;
         }
      }

      return null;
   }

   void SetMultiplayer(boolean var1) {
      this.m_bMultiplayer = var1;
   }

   boolean IsMultiplayer() {
      return this.m_bMultiplayer;
   }

   IntList GetAvailableAnimals(int var1, boolean var2) {
      IntList var3 = null;
      switch (var1) {
         case 0:
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
            var3 = new IntList(this.GetAvailableAnimalsCount()); // [mod] déblocage du menu Mods

            for (int var5 = 0; var5 < this.GetAvailableAnimalsCount(); var5++) {
               var3.Add(var5);
            }
            break;
         case 1:
            var3 = new IntList(6);
            var3.Add(0);
            var3.Add(1);
            var3.Add(2);
            var3.Add(3);
            var3.Add(4);
            var3.Add(5);
         case 2:
         default:
            break;
         case 3:
            var3 = new IntList(this.GetAvailableAnimalsCount()); // [mod] déblocage du menu Mods

            for (int var4 = 0; var4 < this.GetAvailableAnimalsCount(); var4++) {
               if (!var2 || this.m_SelectedAnimalID_0 != var4) {
                  var3.Add(var4);
               }
            }
      }

      return var3;
   }

   void SetSelectedAnimals(IntList var1) {
      this.m_SelectedAnimals = var1;
   }

   static int ConvertFromSliderToLaps(int var0) {
      switch (var0) {
         case 0:
            return 3;
         case 1:
            return 5;
         case 2:
            return 7;
         default:
            return 3;
      }
   }

   void Save(KidsStream var1) {
      this.UpdatePlayerSave();

      try {
         var1.WriteMarker(1100);
         int var2 = 0;

         do {
            var1.out.writeUTF(this.m_aRecords[var2].m_Name);
            var1.out.writeFloat(this.m_aRecords[var2].m_Time);
         } while (++var2 < 31);

         var1.out.writeInt(this.m_Players.size());
         PlayerSave var3 = null;
         int var4 = this.m_Players.size();

         for (int var6 = 0; var6 < var4; var6++) {
            var3 = (PlayerSave)this.m_Players.elementAt(var6);
            var3.Save(var1);
         }

         var1.WriteMarker(1104);
         var1.out.flush();
      } catch (IOException var5) {
         GameObject.LogLine("CoreLogic::Save() - IOException");
      }
   }

   boolean IsExtraAvailable(int var1) {
      return this.GetExtraState(var1) > 0; // [mod] déblocage du menu Mods
   }

   TimeEntry GetNewRecordPlayer() {
      TimeEntry var1 = null;
      TimeEntry var2 = this._FindTimeByPlayer(0);
      if (this._CompareTimeEntries(var2, this.m_nNextTrack)) {
         var1 = var2;
      }

      if (this.m_bMultiplayer) {
         TimeEntry var3 = this._FindTimeByPlayer(1);
         if (var3 != null && var3.m_Time < var2.m_Time) {
            var1 = var3;
         }
      }

      return var1;
   }

   void UpdatePlayer(String var1) {
      PlayerSave var2 = null;
      if (var1 != null) {
         var2 = this.FindPlayerSaveByName(var1);
      } else {
         var1 = "DEFAULT";
      }

      if (var2 == null) {
         var2 = new PlayerSave();
         var2.SetDefault();
         var2.m_Name = new String(var1);
         this.m_Players.addElement(var2);
      }

      if (var2 != null) {
         var2.CopyToLog(this);
      }
   }

   boolean IsTrackLocked(int var1) {
      return var1 >= 0 && var1 < m_aTrackLock.length ? m_aTrackLock[var1] : true;
   }

   boolean IsAnimalLocked(int var1) {
      return var1 >= 0 && var1 < m_aAnimalLock.length ? m_aAnimalLock[var1] : true;
   }

   void SelectAnimal(int var1, int var2) {
      switch (var1) {
         case 0:
            this.m_SelectedAnimalID_0 = var2;
            this.m_SelectedAnimalID_1 = -1;
            return;
         case 1:
            this.m_SelectedAnimalID_1 = var2;
            return;
         default:
            this.m_SelectedAnimalID_0 = -1;
            this.m_SelectedAnimalID_1 = -1;
      }
   }

   CoreLogic() {
      this.SetDefault();
   }

   void SetEndAdventure(boolean var1) {
      this.m_bEndAdventure = true;
   }

   void PrepareTrackBeforeExtra(int var1) {
      CoreLogic var2 = RacerGame.GetCoreLogic();
      switch (var1) {
         case 0:
            var2.SetCurrWaveNum(1);
            return;
         case 1:
            var2.SetCurrWaveNum(2);
            return;
         case 2:
            var2.SetCurrWaveNum(1);
            return;
         case 3:
            var2.SetCurrWaveNum(0);
            return;
         case 4:
            var2.SetCurrWaveNum(5);
            return;
         case 5:
            var2.SetCurrWaveNum(4);
            return;
         case 6:
            var2.SetCurrWaveNum(1);
            return;
         case 7:
            var2.SetCurrWaveNum(5);
            return;
         case 8:
            var2.SetCurrWaveNum(4);
            return;
         case 9:
            var2.SetCurrWaveNum(2);
            return;
         case 10:
            var2.SetCurrWaveNum(5);
            return;
         case 11:
            var2.SetCurrWaveNum(4);
            return;
         case 12:
            var2.SetCurrWaveNum(2);
            return;
         case 13:
            var2.SetCurrWaveNum(3);
            return;
         case 14:
            var2.SetCurrWaveNum(4);
            return;
      }
   }

   void SetCheatAdventure(int var1) {
      if (var1 >= 0 && var1 <= 32) {
         this.m_nAdventureDiffLevel = 0;
         this.m_bAdventureInProgress = true;
         this.m_nPassedTracks = var1;
         this.m_SelectedAnimalID_0 = 1;
         this.m_SelectedAnimalID_1 = -1;
         this.m_Laps = 3;
         switch (var1) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
               this.m_AnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 0;
               this.m_aCurrentAnimals[2] = 2;
               this.m_aCurrentAnimals[3] = 3;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 0;
               this.m_aCurrentAnimals[2] = 2;
               this.m_aCurrentAnimals[3] = 3;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               return;
            case 5:
               this.m_AnimalsCntInRace = 2;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 0;
               this.m_aCurrentAnimals[2] = 2;
               this.m_aCurrentAnimals[3] = 3;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               return;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
               this.m_AnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 2;
               this.m_aCurrentAnimals[3] = 3;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 2;
               this.m_aCurrentAnimals[3] = 3;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               return;
            case 11:
               this.m_AnimalsCntInRace = 2;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 7;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 2;
               this.m_aCurrentAnimals[3] = 3;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               return;
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
               this.m_AnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 7;
               this.m_aCurrentAnimals[3] = 3;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 7;
               this.m_aCurrentAnimals[3] = 3;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               return;
            case 17:
               this.m_AnimalsCntInRace = 2;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 8;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 7;
               this.m_aCurrentAnimals[3] = 3;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               return;
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
               this.m_AnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 7;
               this.m_aCurrentAnimals[3] = 8;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 7;
               this.m_aCurrentAnimals[3] = 8;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               return;
            case 23:
               this.m_AnimalsCntInRace = 2;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 9;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 7;
               this.m_aCurrentAnimals[3] = 8;
               this.m_aCurrentAnimals[4] = 4;
               this.m_aCurrentAnimals[5] = 5;
               return;
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
               this.m_AnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 7;
               this.m_aCurrentAnimals[3] = 8;
               this.m_aCurrentAnimals[4] = 9;
               this.m_aCurrentAnimals[5] = 5;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 7;
               this.m_aCurrentAnimals[3] = 8;
               this.m_aCurrentAnimals[4] = 9;
               this.m_aCurrentAnimals[5] = 5;
               return;
            case 29:
               this.m_AnimalsCntInRace = 2;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 10;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 7;
               this.m_aCurrentAnimals[3] = 8;
               this.m_aCurrentAnimals[4] = 9;
               this.m_aCurrentAnimals[5] = 5;
               return;
            case 30:
            case 31:
            case 32:
               this.m_AnimalsCntInRace = 2;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 11;
               this.m_StoredAnimalsCntInRace = 6;
               this.m_aCurrentAnimals[0] = 1;
               this.m_aCurrentAnimals[1] = 6;
               this.m_aCurrentAnimals[2] = 7;
               this.m_aCurrentAnimals[3] = 8;
               this.m_aCurrentAnimals[4] = 9;
               this.m_aCurrentAnimals[5] = 10;
               return;
         }
      } else {
         GameObject.LogLine("Bledna trasa");
      }
   }

   void InsertNewBoss(int var1) {
      if (var1 == 11) {
         this.m_AnimalsCntInRace = 2;
         this.m_aCurrentAnimals[1] = var1;
      } else {
         this.m_AnimalsCntInRace = 6;
         int var2 = this._FindAnimalToLeave(var1);
         if (var2 >= 0) {
            this.m_aCurrentAnimals[var2] = var1;
         }
      }
   }

   boolean GetEndAdventure() {
      return this.m_bEndAdventure;
   }

   void AddBonusFirstPrize(int var1) {
      if (var1 >= 0 && var1 < this.m_ExtraAvailable.length) {
         this.m_ExtraAvailable[var1] = 2;
      }
   }

   void SetTrackBeforeExtra(int var1) {
      CoreLogic var2 = RacerGame.GetCoreLogic();
      switch (var1) {
         case 4:
            var2.SetNextTrack(0);
            return;
         case 5:
            var2.SetNextTrack(8);
            return;
         case 6:
            var2.SetNextTrack(22);
            return;
         case 7:
            var2.SetNextTrack(2);
            return;
         case 8:
            var2.SetNextTrack(16);
            return;
         case 9:
            var2.SetNextTrack(26);
            return;
         case 10:
            var2.SetLapsNo(3);
            var2.SetNextTrack(4);
            return;
         case 11:
            var2.SetLapsNo(3);
            var2.SetNextTrack(12);
            return;
         case 12:
            var2.SetLapsNo(3);
            var2.SetNextTrack(24);
            return;
         case 13:
            var2.SetNextTrack(6);
            return;
         case 14:
            var2.SetNextTrack(14);
            return;
         case 15:
            var2.SetNextTrack(18);
            return;
         case 16:
            var2.SetNextTrack(10);
            return;
         case 17:
            var2.SetNextTrack(20);
            return;
         case 18:
            var2.SetNextTrack(28);
            return;
      }
   }

   int GetDiffLevel() {
      return this.m_nAdventureDiffLevel;
   }

   void AddNewTrack(int var1) {
      if (var1 >= this.m_nAvailableTracks - 1) {
         this.m_nAvailableTracks = var1 + 1;
      }

      if (this.m_nAvailableTracks > 31) {
         this.m_nAvailableTracks = 31;
      }
   }

   void CheckExtraPrize() {
      int var1 = this.ConvertExtraToOrd(RacerGame.firstMenuModule.GetMenuCX());
      if (var1 >= 0 && this.m_TimeList.GetAt(0).m_AnimalID == this.GetSelectedAnimals().GetAt(0)) {
         this.AddBonusFirstPrize(var1);
      }
   }

   void SetCheatAllBonusesFirstPrize() {
      for (int var1 = 0; var1 < this.m_ExtraAvailable.length; var1++) {
         this.m_ExtraAvailable[var1] = 2;
      }
   }

   void SetCheatAllBonuses() {
      for (int var1 = 0; var1 < this.m_ExtraAvailable.length; var1++) {
         this.m_ExtraAvailable[var1] = 1;
      }
   }

   int GetAvailableMaps() {
      return this.GetAvailableTracksCount(); // [mod] déblocage du menu Mods
   }

   // [mod] Déblocages du menu Mods (ModOptions) : ce que voient les menus et le tirage des adversaires.
   // Les champs m_nAvailable* / m_ExtraAvailable gardent la vraie progression du profil (enregistrée par PlayerSave) :
   // décocher l'option rend le profil tel qu'il était.
   int GetAvailableAnimalsCount() {
      return ModOptions.m_bAllAnimals ? 12 : this.m_nAvailableAnimals;
   }

   int GetAvailableTracksCount() {
      return ModOptions.m_bAllTracks ? 31 : this.m_nAvailableTracks;
   }

   // 0 = fermée, 1 = ouverte, 2 = ouverte et gagnée (premier prix)
   int GetExtraState(int i) {
      int n = this.m_ExtraAvailable[i];
      return ModOptions.m_bAllExtras && n < 1 ? 1 : n;
   }

   int _FindAnimalToLeave(int var1) {
      int var2 = RacerGame.GetRandom(0, 100) % 5;
      int var3 = 0;

      for (int var4 = 1; var4 < this.m_AnimalsCntInRace; var4++) {
         if (this.m_aCurrentAnimals[var4] == var1) {
            return -1;
         }
      }

      while (var3 < 20) {
         if (var2 < 1) {
            var2 = 1;
         }

         if (var2 > 5) {
            var2 = 5;
         }

         if (this.m_aCurrentAnimals[var2] < 6) {
            return var2;
         }

         var2++;
         var3++;
      }

      if (var2 < 1) {
         var2 = 1;
      }

      if (var2 > 5) {
         var2 = 5;
      }

      return var2;
   }
}
