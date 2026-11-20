import java.io.IOException;

public class GameSerializeSettings {
   static final int _SP_VER = 9;
   static String _PROGRESS_FILE_NAME = "progress.cfg";
   static CoreLogic Logic = new CoreLogic();
   KbSettings m_Kb0 = new KbSettings(0);
   KbSettings m_Kb1 = new KbSettings(1);
   int m_Details = 1;
   int m_CameraDistance0 = 2;
   float m_MusicVolume = 0.25F;
   float m_FXVolume = 0.7F;
   int m_Resolution = 0;
   boolean m_bFirstTime = false;

   void SetCurrentSettings() {
      Settings var1 = RacerGame.settings;
      // [mod] la taille vient de VideoSettings.scr, plus de l'index 0..2 de progress.cfg (ModVideo)
      ModVideo.ApplyStartup(var1);

      byte var2;
      switch (this.m_Details) {
         case 0:
            var1.nTextureQuality = 2;
            var2 = 2;
            PerformanceSettings.SetCurrQuality(PerformanceSettings._QUALITY_MIN);
            break;
         case 1:
            var1.nTextureQuality = 1;
            var2 = 4;
            PerformanceSettings.SetCurrQuality(PerformanceSettings._QUALITY_MED);
            break;
         default:
            var1.nTextureQuality = 0;
            var2 = 5;
            PerformanceSettings.SetCurrQuality(PerformanceSettings._QUALITY_MAX);
      }

      if (!RacerGame.m_bRecordAvi) {
         RacerGame.game.SetTextureQuality(var2);
      }

      RacerGame.game.SetAudioWaveVolume((int)(this.m_MusicVolume * 10.0F));
      RacerGame.game.SetAudioFXVolume((int)(this.m_FXVolume * 10.0F));
      if (!RacerGame.m_bRecordAvi) {
         RacerGame.game.ApplyVideoSettings();
      }

      RacerGame.game.ApplyAudioSettings();
   }

   GameSerializeSettings() {
      this.SetDefaultSettings();
      this.Load();
   }

   void Load() {
      KidsStream var1 = new KidsStream(_PROGRESS_FILE_NAME);
      if (var1.OpenStream(1)) {
         if (!this.LoadSettings(var1)) {
            this.m_bFirstTime = true;
            this.SetDefaultSettings();
            return;
         }
      } else {
         this.m_bFirstTime = true;
         this.SetDefaultSettings();
      }
   }

   void SetDefaultSettings() {
      this.m_Kb0 = new KbSettings(0);
      this.m_Kb1 = new KbSettings(1);
      this.m_Details = 2;
      this.m_CameraDistance0 = 2;
      this.m_MusicVolume = 0.25F;
      this.m_FXVolume = 0.7F;
      this.m_Resolution = 0;
      Logic.SetDefault();
   }

   boolean SaveSettings(KidsStream var1) {
      try {
         var1.out.writeInt(9);
         if (!this.m_Kb0.Save(var1)) {
            return false;
         }

         if (!this.m_Kb1.Save(var1)) {
            return false;
         }

         var1.out.writeInt(this.m_Details);
         var1.out.writeInt(this.m_CameraDistance0);
         var1.out.writeFloat(this.m_MusicVolume);
         var1.out.writeFloat(this.m_FXVolume);
         var1.out.writeInt(this.m_Resolution);
         Logic.Save(var1);
         var1.WriteMarker(1005);
         var1.out.flush();
         return true;
      } catch (IOException var2) {
         GameObject.CrashLog("GameSerializeSettings::SaveSettings() - IOException");
         return false;
      }
   }

   boolean LoadSettings(KidsStream var1) {
      try {
         if (var1.in.readInt() != 9) {
            return false;
         }

         if (!this.m_Kb0.Load(var1)) {
            return false;
         }

         if (!this.m_Kb1.Load(var1)) {
            return false;
         }

         this.m_Details = var1.in.readInt();
         this.m_CameraDistance0 = var1.in.readInt();
         this.m_MusicVolume = var1.in.readFloat();
         this.m_FXVolume = var1.in.readFloat();
         this.m_Resolution = var1.in.readInt();
         if (!Logic.Load(var1)) {
            return false;
         }

         if (!var1.ReadMarker(1005)) {
            return false;
         }
      } catch (IOException var2) {
         GameObject.CrashLog("GameSerializeSettings::SaveSettings() - IOException");
      }

      return true;
   }

   boolean IsFirstTime() {
      return this.m_bFirstTime;
   }

   void Save() {
      KidsStream var1 = new KidsStream(_PROGRESS_FILE_NAME);
      if (var1.OpenStream(0)) {
         if (!this.SaveSettings(var1)) {
            GameObject.LogLine("Nie udalo sie zapisac ustawien!!!");
            return;
         }
      } else {
         GameObject.LogLine("Nie udalo sie zapisac ustawien!!!");
      }
   }
}
