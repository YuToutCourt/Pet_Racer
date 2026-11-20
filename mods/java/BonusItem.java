// [mod] copie de decompiled/BonusItem.java : la boîte ramassée ici disparaît aussi chez les autres joueurs
// en ligne (ModNet.OnBoxTaken / TakeBox, chantier 8).
public class BonusItem extends TrackObject {
   static float _BONUS_MOVE_OSC = 80.0F;
   static float _BONUS_MOVE_SPEED = 50.0F;
   static float _EFFECT_DISTANCE = 500.0F;
   static float _RESTORE_EFFECT_TIME = 10.0F;
   static float _EFFECT_UP_SPEED = 2000.0F;
   static float _HORIZ_TURN_SPEED = 30.0F;
   boolean m_fWasCollected = false;
   TrackPoint m_TrackPoint = null;
   Vector m_vDefaultPos = null;
   float m_fCurrMove = 0.0F;
   float m_fCurrDir = 1.0F;
   boolean m_bMoveEffect = true;
   boolean m_bTakeEffect = false;

   public void OnFrame() {
      if (this.m_bMoveEffect) {
         if (this.m_fCurrMove > _BONUS_MOVE_OSC) {
            this.m_fCurrMove = _BONUS_MOVE_OSC;
            this.m_fCurrDir = -1.0F;
         }

         if (this.m_fCurrMove < 0.0F) {
            this.m_fCurrMove = 0.0F;
            this.m_fCurrDir = 1.0F;
         }

         this.m_fCurrMove = this.m_fCurrMove + this.m_fCurrDir * _BONUS_MOVE_SPEED * RacerModule.m_Mod.m_fFrameTime;
         Vector var1 = Vector.newVector(this.m_vDefaultPos);
         var1.fY = var1.fY + this.m_fCurrMove;
         this.SetPosition(var1);
         var1.Null();
         this.SetDir(this.RotateVector(this.GetForwardVector(), _HORIZ_TURN_SPEED * RacerModule.m_Mod.m_fFrameTime, Vector.Up));
      }

      if (this.m_bTakeEffect) {
         Vector var2 = this.GetPosition();
         var2.fY = var2.fY + _EFFECT_UP_SPEED * RacerModule.m_Mod.m_fFrameTime;
         this.SetPosition(var2);
         if (var2.fY - this.m_vDefaultPos.fY > _EFFECT_DISTANCE) {
            this.EnableRendering(false);
            this.m_bTakeEffect = false;
            this.CallMethodIn("RestoreBonus", _RESTORE_EFFECT_TIME);
         }
      }
   }

   void RestoreBonus() {
      this.SetPosition(this.m_vDefaultPos);
      this.EnableRendering(true);
      this.m_fWasCollected = false;
      this.SetCollHandler(512);
      this.SetFlag(32);
      this.m_bMoveEffect = true;
   }

   void AfterCollectingBonus() {
      this.SetCollHandler(0);
      this.ClearFlag(32);
      this.m_bTakeEffect = true;
      this.m_bMoveEffect = false;
   }

   public void OnCreate() {
      super.m_iClassID = TrackObject._CLASS_BONUS;
      this.SetPhysicsType(0);
      this.SetCollType(0);
      this.SetCollFlags(131072);
      this.SetCollHandler(512);
      this.m_vDefaultPos = this.GetPosition();
      this.SetEllipsoid(50.0F, 300.0F, -100.0F);
   }

   public void OnCollisionObject(ControlObject var1) {
      Vehicle var2 = (Vehicle)var1;
      if (!this.m_fWasCollected && var2.CollectBonus()) {
         this.m_fWasCollected = true;
         this.AfterCollectingBonus();
         ModNet.OnBoxTaken(this); // [mod] en ligne : boîte prise aussi chez les autres
         if (((RacerCamera)RacerModule.m_Cams[0]).m_Observed == var2) {
            this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_BONUS]);
         }
      }
   }
}
