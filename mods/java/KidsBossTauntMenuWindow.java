public class KidsBossTauntMenuWindow extends KidsMenuWindow {
   int bkTex0 = -1;
   String m_TexName = "Data/Textures/Menu/b/MenuCharakter.png";
   int m_TauntType = -1;
   int m_Boss = -1;
   Vector[] CameraPos = new Vector[]{
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F),
      new Vector(3672.0F, 600.0F, 2802.0F)
   };
   Vector BossPos = new Vector(3870.0F, 486.0F, 2801.0F);
   Vector vUp = new Vector(0.0F, 1.0F, 0.0F);
   MenuAnimal m_BossMesh = null;
   float CameraUp = 40.0F;

   void ButtonsOnTop() {
   }

   void SetCameraAndBoss(ControlObject var1, int var2) {
      Vector var3 = this.CameraPos[var2];
      Vector var4 = new Vector(this.BossPos);
      var4.fY = var4.fY + this.CameraUp;
      var4.Sub(var3);
      var4.Normalize();
      var4.Negate();
      super.m_Module.m_Camera.FromForwardUpPos(var4, this.vUp, var3);
      var1.SetPosition(this.BossPos);
      this.SetDirToCamera(var1);
   }

   void SetDirToCamera(ControlObject var1) {
      MenuCamera var2 = super.m_Module.m_Camera;
      Vector var3 = var2.GetForwardVector();
      var3.fY = 0.0F;
      var1.SetDir(var3);
   }

   public void OnKeyReleased(int var1, char var2) {
      if (this.CanProcessMouseMove() && !super.m_Module.CanIgnoreKeys()) {
         if (var1 == 1) {
            this.EndSequence();
         }
      }
   }

   public void ButtonPushed(int var1) {
      super.ButtonPushed(var1);
   }

   public void TimeSequence() {
      this.DisableCallMethod("TimeSequence");
      this.DisableCallMethod("PlayTaunt");
      this.CallMethodIn("PlayTaunt", 1.0F);
   }

   public void Hide() {
      ((KidsSimpleMenuModule)super.m_Module).HideBillboards();
      if (this.m_BossMesh != null) {
         this.m_BossMesh.EnableRendering(false);
         this.m_BossMesh = null;
      }

      super.Hide();
      this.ProcessKb(false);
      if (((KidsSimpleMenuModule)super.m_Module).m_FogColor != null) {
         super.m_Module
            .FogSetColor(
               ((KidsSimpleMenuModule)super.m_Module).m_FogColor.fX,
               ((KidsSimpleMenuModule)super.m_Module).m_FogColor.fY,
               ((KidsSimpleMenuModule)super.m_Module).m_FogColor.fZ
            );
      }

      super.m_Module.DeactivateMenuCamera();
   }

   public void Show() {
      super.m_Module.DeactivateMenuCamera();
      super.m_Module.ActivateMenuCamera();
      super.m_Module.SetIgnoreKeys(2);
      super.Show();
      this.ProcessKb(true);
      ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, false);
      if (super.m_Module.m_View != -1) {
         super.m_Module.ViewRenderObjects(super.m_Module.m_View, true);
      }

      ((KidsSimpleMenuModule)super.m_Module).ShowBillboard(this.m_Boss);
      this.m_BossMesh = null;
      if (this.m_Boss >= 0) {
         this.m_BossMesh = ((KidsSimpleMenuModule)super.m_Module).GetAnimal(this.m_Boss);
      }

      int var1 = 0;

      do {
         MenuAnimal var2 = ((KidsSimpleMenuModule)super.m_Module).GetAnimal(var1);
         var2.ClearFlag(256);
      } while (++var1 < 11);

      if (this.m_BossMesh != null) {
         this.m_BossMesh.ClearFlag(256);
         this.SetCameraAndBoss(this.m_BossMesh, this.m_Boss);
         this.m_BossMesh.EnableRendering(true);
         this.m_BossMesh.SetPhysicsActive(false);
         if (this.m_BossMesh.m_BackGroundColor != null) {
            super.m_Module.FogSetColor(this.m_BossMesh.m_BackGroundColor.fX, this.m_BossMesh.m_BackGroundColor.fY, this.m_BossMesh.m_BackGroundColor.fZ);
         }
      }

      ((KidsSimpleMenuModule)super.m_Module).StopMusicLoop();
      this.TimeSequence();
   }

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(this.m_TexName, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), this.m_TexName);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      ModSideFill.AddTo(this, var1); // [mod] écran large : damier prolongé sur les côtés au lieu du décor 3D
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.ProcessKb(true);
      this.SetAlphaForAll(1.0F);
   }

   public void PlayTaunt() {
      this.DisableCallMethod("PlayTaunt");
      float var1 = 0.5F;
      switch (this.m_TauntType) {
         case 0:
            if (this.m_Boss >= 0 && this.m_BossMesh != null) {
               this.m_BossMesh.PlayAnim("", "talk", 5);
               var1 += this.m_BossMesh.PlaySoundInvite(1);
            }
            break;
         case 1:
            if (this.m_Boss >= 0 && this.m_BossMesh != null) {
               this.m_BossMesh.PlayAnim("", "talk", 3);
               var1 += this.m_BossMesh.PlaySoundInvite(0);
            }
            break;
         case 2:
            if (this.m_Boss >= 0 && this.m_BossMesh != null) {
               this.m_BossMesh.PlayAnim("", "talk", 4);
               var1 += this.m_BossMesh.PlaySoundInvite(1);
            }
      }

      this.CallMethodIn("EndSequence", var1);
   }

   void RefreshControls() {
   }

   public void EndSequence() {
      this.DisableCallMethod("TimeSequence");
      this.DisableCallMethod("PlayTaunt");
      this.DisableCallMethod("EndSequence");
      if (this.m_BossMesh != null) {
         this.m_BossMesh.SetFlag(256);
      }

      int var1 = 0;

      do {
         MenuAnimal var2 = ((KidsSimpleMenuModule)super.m_Module).GetAnimal(var1);
         var2.SetFlag(256);
      } while (++var1 < 11);

      super.m_Module.ClearStack();
      this.DisactivateWindow();
      super.m_Module.m_CurrentMenu = -1;
      RacerGame.game.RunModule();
   }
}
