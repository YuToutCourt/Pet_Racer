public class Bomb extends ThrowItem {
   static float _GRAVITY = 800.0F;
   static float _BOMB_SPEED = 5000.0F;
   static float _BOMB_RANGE = 2000.0F;
   static float _BOMB_RANGE_GROW_TIME = 0.5F;
   float m_fGrowTime = 0.0F;
   float m_fCurrRange = 0.0F;

   void UpdateExplosion() {
      float var1 = RacerModule.m_Mod.m_fNowTime - this.m_fGrowTime;
      if (!(var1 < _BOMB_RANGE_GROW_TIME)) {
         this.CallMethodIn("DeleteObj", 0.1F);
         this.m_fGrowTime = 0.0F;
      } else {
         Vector var2 = Vector.newVector();
         this.m_fCurrRange = var1 / _BOMB_RANGE_GROW_TIME * _BOMB_RANGE;
         Vehicle var3 = null;

         for (int var4 = 0; var4 < RacerModule.m_Mod.m_RaceParams.m_Vehicles.size(); var4++) {
            var3 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_Vehicles.elementAt(var4);
            // [mod] ligne de débogage d'origine retirée : une écriture dans le journal par image et par voiture
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
      this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_BOMB]);
      ModNet.OnBoom(ModNet._BOOM_BOMB, this.GetPosition()); // [mod] en ligne : explosion montrée chez les autres
      this.ExecuteFX("Explosion", this.GetPosition(), Vector.Up);
      this.EnableRendering(false);
      super.OnCollision();
   }

   void DeleteObj() {
      this.delete();
   }

   public void OnCreate() {
      super.OnCreate();
      this.SetMass(_GRAVITY);
      super.m_fSpeed = _BOMB_SPEED;
      this.SetCollFlags(16384);
      super.m_XRot = 145.0F;
      super.m_YRot = -30.0F;
      super.m_ZRot = -155.0F;
      this.SetRotAndScale();
   }

   public void OnFrame() {
      super.OnFrame();
      if (this.m_fGrowTime > 0.0F) {
         this.UpdateExplosion();
      }
   }
}
