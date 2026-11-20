// [mod] en ligne (ModNet, chantier 8 phase 2) : piège recopié chez les autres joueurs (TRAP), retiré partout quand
// une voiture le déclenche (TRAP_GONE) ; un fantôme ne le déclenche jamais (c'est le jeu de son joueur qui décide).
public class Anvil extends ThrowItem {
   int m_iModNetId = 0;
   static float _ANVIL_SPEED = 300.0F;
   static float _SCALE_BEFORE_COLLECT_ANVIL = 0.3F;
   static float _SCALE_AFTER_COLLECT_ANVIL = 0.8F;
   boolean m_bWaitingForObject = false;
   boolean m_bSolid = true;

   public void OnCollisionFullInfo(ControlObject var1, Vector var2, Vector var3) {
      if (!this.m_bWaitingForObject) {
         this.ModLand(var3);
         ModNet.OnTrapLanded(this, UseItem._USE_ANVIL);
      } else {
         TrackObject var4 = (TrackObject)var1;
         if (var4.m_iClassID == TrackObject._CLASS_VEHICLE) {
            Vehicle var5 = (Vehicle)var1;
            if (!var5.m_bProtection && !var5.m_bModGhost) {
               ModNet.OnTrapTriggered(this);
               this.SetCollHandler(0);
               this.ClearFlag(32);
               this.EnableRendering(false);
               this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_DRIVEINANVIL]);
               super.m_bHitSomebody = true;
               super.OnCollision();
               this.CallMethodIn("DeleteObj", 0.5F);
            }
         }
      }
   }

   void ModLand(Vector var3) {
      this.m_bWaitingForObject = true;
      this.SetPhysicsActive(false);
      this.SetFlag(32);
      this.SetFlag(4096);
      this.ExecuteFX(this, "FXAnvil", Vector.Zero);
      this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_ANVIL]);
      super.m_XRot = -35.0F;
      super.m_YRot = -85.0F;
      super.m_ZRot = -30.0F;
      this.SetRotAndScale();
   }

   public void OnCreate() {
      super.OnCreate();
      super.m_iClassID = TrackObject._CLASS_ANVIL;
      this.ClearFlag(4);
      this.ClearFlag(2);
      this.SetCollFlags(131072);
      this.SetCollHandler(768);
      this.SetCollFlags(16384);
      super.m_fSpeed = _ANVIL_SPEED;
      super.m_XRot = -105.0F;
      super.m_YRot = -10.0F;
      super.m_ZRot = -30.0F;
      this.SetRotAndScale();
   }
}
