// [mod] en ligne (ModNet, chantier 8 phase 2) : piège recopié chez les autres joueurs (TRAP), retiré partout quand
// une voiture le déclenche (TRAP_GONE) ; un fantôme ne le déclenche jamais (c'est le jeu de son joueur qui décide).
public class Catapult extends ThrowItem {
   int m_iModNetId = 0;
   static float _CATAPULT_RANGE = 100.0F;
   static float _CATAPULT_SPEED = 700.0F;
   boolean m_bWaitingForObject = false;
   int m_iNofAnim = -1;

   public void OnAnimEnd(int var1) {
      if (var1 == this.m_iNofAnim) {
         this.StopAnim("");
         this.CallMethodIn("DeleteObj", 0.1F);
      }
   }

   public void OnCollisionFullInfo(ControlObject var1, Vector var2, Vector var3) {
      if (!this.m_bWaitingForObject) {
         this.ModLand(var3);
         ModNet.OnTrapLanded(this, UseItem._USE_CATAPULT);
      } else {
         TrackObject var4 = (TrackObject)var1;
         if (var4.m_iClassID == TrackObject._CLASS_VEHICLE) {
            Vehicle var5 = (Vehicle)var1;
            if (!var5.m_bProtection && !var5.m_bModGhost) {
               ModNet.OnTrapTriggered(this);
               this.SetCollHandler(0);
               this.ExecuteFX("FXCatapult", this.GetPosition());
               this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_CATAPULT]);
               this.m_iNofAnim = this.PlayAnim("", "rozprezenie", 1);
               var5.ExplosionEffect();
               super.m_bHitSomebody = true;
               super.OnCollision();
            }
         }
      }
   }

   void ModLand(Vector var3) {
      this.m_bWaitingForObject = true;
      this.SetPhysicsActive(false);
      this.SetFlag(32);
      this.SetFlag(4096);
      this.AlignToGround();
      super.m_XRot = 0.0F;
      super.m_YRot = -5.0F;
      super.m_ZRot = 50.0F;
      super.m_Scale = 3.0F;
      this.SetRotAndScale();
      this.DestroyObjectIn(RacerModule.m_Mod.m_fNowTime, 60.0F);
   }

   void DeleteObj() {
      this.delete();
   }

   public void OnCreate() {
      super.OnCreate();
      super.m_iClassID = TrackObject._CLASS_CATAPULT;
      this.ClearFlag(4);
      this.ClearFlag(2);
      this.SetCollFlags(131072);
      this.SetCollFlags(16384);
      this.SetCollFlags(8192);
      this.SetCollHandler(768);
      super.m_fSpeed = _CATAPULT_SPEED;
      this.InitAnimSeq("rozprezenie", 0, 39);
      super.m_XRot = 160.0F;
      super.m_YRot = -60.0F;
      super.m_ZRot = -140.0F;
      this.SetRotAndScale();
   }
}
