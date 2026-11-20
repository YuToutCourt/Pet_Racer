// [mod] en ligne (ModNet) : l'explosion est montrée chez les autres joueurs (BOOM) ; les fantômes touchés le sont
// par Vehicle.ExplosionEffect (HIT vers leur joueur)
public class WaterMelon extends ThrowItem {
   static float _GRAVITY = 700.0F;
   static float _WATERMELON_SPEED = 5000.0F;
   static float _WATERMELON_RANGE = 2000.0F;
   static float _WATERMELON_RANGE_GROW_TIME = 0.5F;
   float m_fGrowTime = 0.0F;
   float m_fCurrRange = 0.0F;

   void UpdateExplosion() {
      float var1 = RacerModule.m_Mod.m_fNowTime - this.m_fGrowTime;
      if (!(var1 < _WATERMELON_RANGE_GROW_TIME)) {
         this.CallMethodIn("DeleteObj", 0.1F);
         this.m_fGrowTime = 0.0F;
      } else {
         Vector var2 = Vector.newVector();
         this.m_fCurrRange = var1 / _WATERMELON_RANGE_GROW_TIME * _WATERMELON_RANGE;
         Vehicle var3 = null;

         for (int var4 = 0; var4 < RacerModule.m_Mod.m_RaceParams.m_Vehicles.size(); var4++) {
            var3 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_Vehicles.elementAt(var4);
            if (super.m_vParentVehicle == null || super.m_vParentVehicle != var3) {
               var2.Set(var3.m_vVehiclePosition);
               var2.Sub(this.GetPosition());
               var2.fY = 0.0F;
               if (var2.Len() < this.m_fCurrRange && !var3.m_bProtection) {
                  var3.ExplosionEffect();
                  super.m_bHitSomebody = true;
               }
            }
         }

         var2.Null();
      }
   }

   public void OnCollision() {
      this.SetCollHandler(0);
      this.SetPhysicsActive(false);
      this.m_fGrowTime = RacerModule.m_Mod.m_fNowTime;
      this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_WATERMELON]);
      ModNet.OnBoom(ModNet._BOOM_WATERMELON, this.GetPosition());
      this.ExecuteFX("FXWaterMelonExplosion", this.GetPosition(), Vector.Up);
      this.ExecuteParticleSystem("WaterMelonExplosion", this.GetPosition(), Vector.Up);
      this.EnableRendering(false);
      super.OnCollision();
   }

   void DeleteObj() {
      this.delete();
   }

   public void OnCreate() {
      super.OnCreate();
      this.SetMass(_GRAVITY);
      super.m_fSpeed = _WATERMELON_SPEED;
      this.SetCollFlags(16384);
      super.m_XRot = 55.0F;
      super.m_YRot = -70.0F;
      super.m_ZRot = -140.0F;
      this.SetRotAndScale();
   }

   public void OnFrame() {
      super.OnFrame();
      if (this.m_fGrowTime > 0.0F) {
         this.UpdateExplosion();
      }
   }
}
