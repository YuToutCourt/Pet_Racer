// [mod] en ligne (ModNet, chantier 8 phase 2) : piège recopié chez les autres joueurs (TRAP), retiré partout quand
// une voiture le déclenche (TRAP_GONE) ; un fantôme ne le déclenche jamais (c'est le jeu de son joueur qui décide).
public class Banana extends ThrowItem {
   int m_iModNetId = 0;
   static float _BANANA_SPEED = 300.0F;
   boolean m_bWaitingForObject = false;
   boolean m_bDeactivate = false;

   public void OnCollisionFullInfo(ControlObject var1, Vector var2, Vector var3) {
      if (!this.m_bDeactivate) {
         if (!this.m_bWaitingForObject) {
            this.ModLand(var3);
            ModNet.OnTrapLanded(this, UseItem._USE_BANANA);
         } else {
            TrackObject var4 = (TrackObject)var1;
            if (var4.m_iClassID == TrackObject._CLASS_VEHICLE) {
               Vehicle var5 = (Vehicle)var1;
               if (!var5.m_bProtection && !var5.m_bModGhost) {
                  ModNet.OnTrapTriggered(this);
                  this.SetCollHandler(0);
                  var5.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_BANANA]);
                  if (((TrackObject)var1).m_iClassID == TrackObject._CLASS_VEHICLE) {
                     ((Vehicle)var1).RotateVehicleVertical();
                  }

                  this.ExecuteFX(var5, "FXBanana", Vector.Zero);
                  this.ExecuteParticleSystem("BananaExplosion", this.GetPosition(), Vector.Up);
                  super.m_bHitSomebody = true;
                  super.OnCollision();
                  this.m_bDeactivate = true;
                  this.CallMethodIn("DeleteObj", 0.5F);
               }
            }
         }
      }
   }

   void ModLand(Vector var3) {
      this.m_bWaitingForObject = true;
      this.SetPhysicsActive(false);
      this.SetFlag(32);
      this.SetFlag(4096);
      this.DestroyObjectIn(RacerModule.m_Mod.m_fNowTime, 60.0F);
      super.m_XRot = -80.0F;
      super.m_YRot = 0.0F;
      super.m_ZRot = -40.0F;
      this.SetRotAndScale();
   }

   void DeleteObj() {
      this.delete();
   }

   public void OnCreate() {
      super.OnCreate();
      super.m_iClassID = TrackObject._CLASS_BANANA;
      this.ClearFlag(4);
      this.ClearFlag(2);
      this.SetCollFlags(131072);
      this.SetCollHandler(768);
      this.SetCollFlags(16384);
      this.SetCollFlags(8192);
      super.m_fSpeed = _BANANA_SPEED;
      super.m_XRot = -75.0F;
      super.m_YRot = 0.0F;
      super.m_ZRot = -40.0F;
      this.SetRotAndScale();
   }
}
