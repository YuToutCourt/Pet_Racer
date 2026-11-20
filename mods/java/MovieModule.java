public class MovieModule extends Module {
   MovieWindow screen = null;
   boolean bMoviePlaying = false;
   int nCurAvi = 0;
   int nAviCount;
   String[] sAviFiles = new String[]{"data/movies/tintro.avi"};
   MovieLogoWindow Splash = null;

   void MovieFinished() {
      GameObject.CrashLog("Avi finished\n");
      this.DisableCallMethod("MoveFinished");
      this.DisableCallMethod("EndLogo");
      if (this.bMoviePlaying) {
         this.screen.StopAVI();
         this.bMoviePlaying = false;
         this.nCurAvi++;
         if (this.nCurAvi < this.nAviCount) {
            this.PlayAvi(this.nCurAvi);
         } else {
            this.CallMethodIn("EndLogo", 0.0F);
         }
      }
   }

   void LoadGame() {
      RacerGame.game.LoadFirstGameModule();
   }

   void DestroyScreen() {
      if (this.screen != null) {
         this.RemoveSprite(this.screen);
         this.screen.delete();
         this.screen = null;
      }
   }

   void EndLogo() {
      this.Splash.Show();
      this.ForceRender();
      this.DestroyScreen();
      this.DisableCallMethod("MoveFinished");
      this.DisableCallMethod("EndLogo");
      this.UnregisterKeyRequest();
      this.LoadGame();
   }

   void OnCreate() {
      this.Splash = (MovieLogoWindow)this.CreateObject("MovieLogoWindow");
      this.AddSprite(this.Splash);
      this.Splash.Init(null, -1);
      this.Splash.Hide();
      this.screen = (MovieWindow)this.CreateObject("MovieWindow");
      this.AddSprite(this.screen);
      // [mod] vidéos 4:3 : centrées, sans être étirées sur un écran large
      int nZoneW = ModVideo.GetZoneWidth(GameObject.GetScreenWidth(), GameObject.GetScreenHeight());
      this.screen.SetSize(new Vector(nZoneW, GameObject.GetScreenHeight(), 0.0F));
      this.screen.Show();
      this.screen.SetModule(this);
      int var1 = this.screen.AddTexture("data/Textures/Menu/blacktxt.png");
      this.screen.SetTexture(var1, new Rect(0.0F, 0.0F, 1.0F, 1.0F));
      this.screen.SetPos(new Vector((GameObject.GetScreenWidth() - nZoneW) / 2, 0.0F, 0.0F)); // [mod]
      this.RegisterKeyRequest();
      GameObject.CrashLog("Before PlayAvi\n");
      this.PlayAvi(this.nCurAvi);
      GameObject.CrashLog("After PlayAvi\n");
      this.nAviCount = this.sAviFiles.length;
   }

   void OnInputKey(int var1, boolean var2, char var3) {
      if (!var2 && (var1 == 1 || var1 == 57)) {
         if (this.bMoviePlaying) {
            this.CallMethodIn("MovieFinished", 0.0F);
            return;
         }

         this.CallMethodIn("EndLogo", 0.0F);
      }
   }

   void PlayAvi(int var1) {
      this.bMoviePlaying = true;
      String var2 = this.sAviFiles[var1];
      this.screen.PlayAVI(var2, 0);
   }
}
