// [mod] en ligne (ModNet, chantier 8 phase 2) : piège recopié chez les autres joueurs (TRAP), retiré partout quand
// une voiture le déclenche (TRAP_GONE) ; un fantôme ne le déclenche jamais (c'est le jeu de son joueur qui décide).
public class FunnyGasBox extends ThrowItem {
   int m_iModNetId = 0;
   static float _FUNNY_GAS_LIFE_TIME = 15.0F;
   boolean m_bDeactivate = false;

   public void FireFX() {
      this.ExecuteFX(this, "FXFunnyGas", Vector.Zero);
      this.SetScale(new Vector(5.0F, 5.0F, 5.0F));
      this.EnableRendering(false);
   }

   public void OnCollisionFullInfo(ControlObject var1, Vector var2, Vector var3) {
      if (!this.m_bDeactivate) {
         if (var1 != null) {
            TrackObject var4 = (TrackObject)var1;
            if (var4.m_iClassID != TrackObject._CLASS_VEHICLE) {
               return;
            }

            Vehicle var5 = (Vehicle)var1;
            if (var5.m_bProtection || var5.m_bModGhost) {
               return;
            }

            ModNet.OnTrapTriggered(this);

            this.SetCollHandler(0);
            this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_FUNNYGAS]);
            if (((TrackObject)var1).m_iClassID == TrackObject._CLASS_VEHICLE) {
               ((Vehicle)var1).StoppedByFunnyGas();
            }

            this.ExecuteFX(var5, "FXHiHi", Vector.Zero);
            super.m_bHitSomebody = true;
            super.OnCollision();
            this.m_bDeactivate = true;
            this.CallMethodIn("DeleteObj", 0.3F);
            this.ClearFlag(32);
         }
      }
   }

   void DeleteObj() {
      this.delete();
   }

   public void OnCreate() {
      super.OnCreate();
      super.m_iClassID = TrackObject._CLASS_UNKNOWN;
      this.ClearFlag(4);
      this.ClearFlag(2);
      this.SetCollFlags(131072);
      this.SetCollHandler(768);
      this.SetCollFlags(16384);
      this.SetCollFlags(8192);
      super.m_fSpeed = 0.0F;
      this.DestroyObjectIn(RacerModule.m_Mod.m_fNowTime, _FUNNY_GAS_LIFE_TIME);
      this.SetFlag(32);
      this.SetPhysicsActive(false);
   }
}
