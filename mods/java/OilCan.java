// [mod] en ligne (ModNet, chantier 8 phase 2) : piège recopié chez les autres joueurs (TRAP), retiré partout quand
// une voiture le déclenche (TRAP_GONE) ; un fantôme ne le déclenche jamais (c'est le jeu de son joueur qui décide).
public class OilCan extends ThrowItem {
   int m_iModNetId = 0;
   static float _OILCAN_SPEED = 700.0F;
   boolean m_bWaitingForObject = false;
   boolean m_bDeactivate = false;
   int m_nTrace = -1;

   public void OnCollisionFullInfo(ControlObject var1, Vector var2, Vector var3) {
      if (!this.m_bDeactivate) {
         if (!this.m_bWaitingForObject) {
            this.ModLand(var3);
            ModNet.OnTrapLanded(this, UseItem._USE_OILCAN);
         } else {
            TrackObject var4 = (TrackObject)var1;
            if (var4.m_iClassID == TrackObject._CLASS_VEHICLE) {
               Vehicle var5 = (Vehicle)var1;
               if (!var5.m_bProtection && !var5.m_bModGhost) {
                  ModNet.OnTrapTriggered(this);
                  this.SetCollHandler(0);
                  this.ExecuteFX(var1, "FXOilSplash", Vector.Zero);
                  this.ExecuteParticleSystem("Oil", this.GetPosition(), Vector.Up);
                  var5.RotateVehicleVertical();
                  var5.PlayBrakeSound();
                  super.m_bHitSomebody = true;
                  super.OnCollision();
                  this.m_bDeactivate = true;
                  this.DeleteTraces();
                  this.CallMethodIn("DeleteObj", 0.1F);
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
      super.m_XRot = -5.0F;
      super.m_YRot = 50.0F;
      super.m_ZRot = -95.0F;
      super.m_Scale = 3.0F;
      this.SetRotAndScale();
      this.DestroyObjectIn(RacerModule.m_Mod.m_fNowTime, 60.0F);
      if (this.m_nTrace == -1) {
         Vector var6 = this.GetPosition();
         var6.fY = this.TerrainHeight(var6.fX, var6.fZ);
         this.m_nTrace = this.ExecuteFXTrace("FXOilCanLeak", var6, var3);
      }

      this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_CAN]);
   }

   void DeleteObj() {
      this.DeleteTraces();
      this.delete();
   }

   public void DeleteTraces() {
      if (this.m_nTrace != -1) {
         this.DeleteFXTrace(this.m_nTrace);
         this.m_nTrace = -1;
      }
   }

   public void OnCreate() {
      super.OnCreate();
      super.m_iClassID = TrackObject._CLASS_OILCAN;
      this.ClearFlag(4);
      this.ClearFlag(2);
      this.SetCollFlags(131072);
      this.SetCollHandler(768);
      this.SetCollFlags(16384);
      this.SetCollFlags(8192);
      super.m_fSpeed = _OILCAN_SPEED;
      super.m_XRot = -5.0F;
      super.m_YRot = 50.0F;
      super.m_ZRot = -95.0F;
      super.m_Scale = 3.0F;
      this.SetRotAndScale();
   }
}
