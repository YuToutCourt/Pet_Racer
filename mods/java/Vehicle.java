public class Vehicle extends TrackObject {
   final float _FALL_DOWN_TEST_COEF = 0.2F;
   float _HALF_WHEEL_SIZE = 0.0F;
   static final float _CORRECTION = 0.01F;
   // [mod] framerate auquel le dérapage d'origine est reproduit (voir MoveVehicle, notes/pc_moderne.md)
   static final float _SLIDE_REF_FPS = 30.0F;
   static int _GATES_F_SEARCH_RANGE = 3;
   static int _GATES_B_SEARCH_RANGE = 2;
   static float _AIM_PRECISION = 30.0F;
   static float _GATES_CHECK_TIME = 0.1F;
   static float _AIM_UPDATE_TIME = 0.1F;
   static float _CHECK_GO_UPDATE_TIME = 0.1F;
   static float _CHECK_FOR_GO_ANIM = 0.1F;
   static final int _CAUSE_NOTHING = -1;
   static final int _CAUSE_GAS = 0;
   static final int _CAUSE_WASP_BAG = 1;
   static final int _CAUSE_WATER = 2;
   static final int _CAUSE_STARS = 3;
   static final int _CAUSE_TOMATO_OR_COOKIE = 4;
   static final int _CAUSE_RACE_END = 5;
   static final int _PHYSICS_NORMAL = 0;
   static final int _PHYSICS_EXPLOSION = 1;
   static final int _PHYSICS_FALL_UNDER_GROUND = 2;
   static final int _PHYSICS_SLIDE = 3;
   static final int _PHYSICS_FREEZE = 4;
   static final int _PHYSICS_UFO = 5;
   static final float _NEUTRAL_REV_ACC_COEF = 2.0F;
   static final float _NEUTRAL_REV_COEF = 1.0F;
   static final float _MIN_TIME_BTW_TREE_COLL = 0.2F;
   static final float _MAX_TREE_COLL_TIME = 2.0F;
   static final float _MODIFY_SPEED_TIME = 0.1F;
   static final float _TURBO_SOUND_ADD = 1.0F;
   static final float _WATER_POOL_CHECK_TIME = 0.5F;
   static final float _BRIDGE_CHECK_TIME = 0.5F;
   static final float _TIME_BTW_ACCEL_SOUNDS = 0.5F;
   static final float _MIN_DIST_BTW_CARS = 300.0F;
   static final float _ACCELERATION_MOD = 3.0F;
   static final float _TIME_BTW_FX_WATER_SPLASH = 0.5F;
   static final float _ENGINE_SOUND_VOLUME = 0.35F;
   static final float _ENGINE_VISIBLE_SOUND_VOLUME = 0.7F;
   static final int _MAX_SIMULTANEOUSLY_PLAYING_SOUNDS = 2;
   static final float _AVG_TIME_OF_PLAYING_SOUND = 3.0F;
   static final float _MAX_SOUND_DISTANCE = 5000.0F;
   VehicleParams m_Params = null;
   float m_fFlyingTime = 0.0F;
   float m_fLandingTime = 0.0F;
   boolean m_bEnableBack = true;
   Vector m_vSlideVector = new Vector();
   float m_fAccelerate = 0.0F;
   float m_fBrake = 0.0F;
   float m_fTurnLeft = 0.0F;
   float m_fTurnRight = 0.0F;
   float m_fCurrSpeed = 0.0F;
   float m_fCurrGravity = 0.0F;
   float m_fCurrDirAngle = 0.0F;
   boolean m_bIsFlyingOnFrame = false;
   int[] m_Wheels = new int[]{-1, -1, -1, -1};
   int[] m_WheelsBase = new int[]{-1, -1, -1, -1};
   Vector[] vWheelTmp = new Vector[4];
   float[] m_Springs = new float[4];
   float[] m_SpringsNew = new float[4];
   float m_fWheelRotationSpeed = 0.0F;
   boolean m_bFlying = false;
   boolean m_bLanding = false;
   Vector m_vTurningSlide = new Vector();
   float m_fCurrVehicleHAngle = 0.0F;
   float m_fCurrVehicleVAngle = 0.0F;
   Vector m_vVehicleNormal = null;
   Vector m_vVehicleForward = null;
   Vector m_vVehicleRight = null;
   Vector m_vVehicleTForward = null;
   Vector m_vVehicleTNormal = null;
   Vector m_vVehiclePosition = null;
   Vector m_vCollisionVector = null;
   Vector m_vMoveDir = null;
   boolean m_bGotCollision = false;
   float m_fLastAccelFXTime = 0.0F;
   float m_fLastBrakeFXTime = 0.0F;
   float m_fLastSlideFXTime = 0.0F;
   int m_iNofEngineSound = 0;
   VehicleCharacter m_Character = null;
   int m_iCurrTrackPoint = -1;
   boolean m_bIsOnTrack = true;
   UseItem m_CurrentBonus = null;
   int m_iCurrPhysics = 0;
   float m_fAddTurnAngle = 0.0F;
   boolean m_bTurbo = false;
   boolean m_bTireSpikes = false;
   boolean m_bProtection = false;
   int m_iCurrLoop = -1;
   float m_fBrakePressTime = 0.0F;
   int m_iNofOffRoadSound = 0;
   int m_iNofRoadSound = 0;
   int m_iNofProtectionSound = 0;
   boolean m_bRaceFinished = false;
   float m_fLastCollisionSound = 0.0F;
   float m_fCollectBonusTime = 0.0F;
   boolean m_bGetInput = false;
   boolean m_bUpdateInput = false;
   int m_iCurrPlace = -1;
   float m_fCoveredDistance = 0.0F;
   CollInfo m_collInfo = new CollInfo();
   float m_fLastLookAroundTime = 0.0F;
   int m_iAnimalID = -1;
   float m_fAimAngle = 0.0F;
   Vector m_vAimVector = new Vector();
   float m_fAngleToTurn = 0.0F;
   boolean m_bDirectSteer = false;
   VehicleAI m_VehicleAI = null;
   int m_iStopCause = -1;
   Vector m_vSlidePhysicsDir = new Vector();
   float m_fCurrRevolution = 0.0F;
   float m_fBeforeMoveSpeed = 0.0F;
   float m_fLastTalkTime = 3.0F;
   boolean m_bIsPlayer = false;
   float m_fRespawnTime = 0.0F;
   Vehicle m_LockedObject = null;
   boolean m_bWrongWay = false;
   float m_fInAccelerate = 0.0F;
   float m_fInBrake = 0.0F;
   float m_fInTurnLeft = 0.0F;
   float m_fInTurnRight = 0.0F;
   boolean m_bStopped = false;
   int m_iAIUpdatePointNo = -1;
   int m_iAIUpdatePointForwardNo = -1;
   boolean m_bCanUseBonus = true;
   float m_fCurrMaxSpeed = 0.0F;
   float m_fCollCoef = 0.0F;
   float m_fDefaultMaxSpeed = 0.0F;
   float m_fDefaultMaxTurnAngle = 0.0F;
   float m_fDefaultMaxAccel = 0.0F;
   float m_fDefaultMinAccel = 0.0F;
   float m_fLastTreeCollTime = 0.0F;
   float m_fTreeCollTime = 0.0F;
   boolean m_bMustRespawn = false;
   float m_fCurrSpeedModifier = 0.0F;
   int m_Index = -1;
   boolean m_bInWaterPool = false;
   float m_fRandomStartCoef = 0.0F;
   int m_iBridgeSound = 0;
   float m_fLastAccelTime = 0.0F;
   float m_fLastAccelPressTime = 0.0F;
   float m_fLastRightPressTime = 0.0F;
   float m_fLastLeftPressTime = 0.0F;
   boolean m_bWasTakenByUFO = false;
   boolean m_bPlayBrakeSound = false;
   int m_iNofWaterPoolSound = 0;
   float m_fFinishClockRaceTime = 0.0F;
   float m_fFinishTimeForCam = 0.0F;
   float m_fSlopeAccelCoef = 1.0F;
   boolean m_bIsObservedByCamera = false;
   float m_fLastSplashTime = 0.0F;
   float m_fLastTurnSlideValue = 0.0F;
   boolean m_bSlideIncreasing = false;
   int m_iNofSlideSound = 0;
   String m_HornSound = "Data/Sounds/horn.wav";
   static int m_iNofCurrPlayingSounds = 0;
   int m_iLastBonusType = -1;

   void SetCharacter(VehicleCharacter var1) {
      this.m_Character = var1;
   }

   void AlignSprings() {
      int var1 = 0;

      do {
         if (this.m_Springs[var1] < this.m_SpringsNew[var1]) {
            this.m_Springs[var1] = this.m_Springs[var1] + this.m_Params._SPRING_TIME_MOVE_COEF * RacerModule.m_Mod.m_fFrameTime;
            if (this.m_Springs[var1] > this.m_SpringsNew[var1]) {
               this.m_Springs[var1] = this.m_SpringsNew[var1];
            }
         } else {
            this.m_Springs[var1] = this.m_Springs[var1] - this.m_Params._SPRING_TIME_MOVE_COEF * RacerModule.m_Mod.m_fFrameTime;
            if (this.m_Springs[var1] < this.m_SpringsNew[var1]) {
               this.m_Springs[var1] = this.m_SpringsNew[var1];
            }
         }

         if (this.m_Springs[var1] < this.m_Params._SPRING_MIN) {
            this.m_Springs[var1] = this.m_Params._SPRING_MIN;
         }

         if (this.m_Springs[var1] > this.m_Params._SPRING_MAX) {
            this.m_Springs[var1] = this.m_Params._SPRING_MAX;
         }
      } while (++var1 < 4);
   }

   void AttachRoadSound(String var1) {
      if (this.m_iNofRoadSound == 0) {
         this.m_iNofRoadSound = this.AttachSound(var1);
      }
   }

   void AttachProtectionSound(String var1) {
      this.m_iNofProtectionSound = this.AttachSound(var1);
   }

   void PlayHitSound() {
      if (this.CanPlayTalkSound()) {
         if (!(RacerGame.GetRandom() > this.m_Params._PROB_PLAY_HIT_SOUND)) {
            if (this.CanAttachOneShotSound()) {
               this.AttachOneShotSound(MenuAnimal._HIT_SOUNDS[this.m_iAnimalID], VehicleParams._MIN_TALK_RANGE, VehicleParams._MAX_TALK_RANGE);
            }
         }
      }
   }

   float GetMaxSpeed() {
      return this.m_fCurrMaxSpeed;
   }

   float GetDefaultMaxSpeed() {
      return this.m_fDefaultMaxSpeed;
   }

   void AttachWaterSound(String var1) {
      if (this.m_iNofWaterPoolSound == 0) {
         this.m_iNofWaterPoolSound = this.AttachSound(var1);
      }
   }

   void PlayBrakeSound() {
      if (this.m_bIsOnTrack) {
         this.PlaySound(VehicleParams._CAR_SOUNDS[VehicleParams._BRAKE_ROAD]);
      } else {
         this.PlaySound(VehicleParams._CAR_SOUNDS[VehicleParams._BRAKE_OFF_ROAD]);
      }
   }

   void StopSlideSound() {
      if (this.m_iNofSlideSound != 0) {
         this.DetachSound(this.m_iNofSlideSound);
      }

      this.m_iNofSlideSound = 0;
   }

   void PlayAccelSound() {
      if (RacerModule.m_Mod.m_fNowTime > this.m_fLastAccelTime + 0.5F) {
         this.m_fLastAccelTime = RacerModule.m_Mod.m_fNowTime;
         if (!this.m_bIsOnTrack) {
            this.PlaySound(VehicleParams._CAR_SOUNDS[VehicleParams._ACCEL_OFF_ROAD]);
            return;
         }

         this.PlaySound(VehicleParams._CAR_SOUNDS[VehicleParams._ACCEL_ROAD]);
      }
   }

   public void ReturnToMenu() {
      this.DisableCallMethod("DisableTurbo");
      this.DisableCallMethod("DisableProtection");
      this.DisableCallMethod("EnableBack");
      this.DisableCallMethod("CheckForNearPlayers");
      this.DisableCallMethod("UpdateCurrentGate");
      this.DisableCallMethod("CheckForStop");
      this.DisableCallMethod("CheckIfCanGoAfterBrake");
      this.DisableCallMethod("ThrowGunAway");
      this.DisableCallMethod("DisableTireSpikes");
      this.DisableCallMethod("AimAtEnemy");
      this.DisableCallMethod("CheckForWaterPool");
      this.DisableCallMethod("CheckForBridge");
      RacerGame.game.m_bFinishRace = true;
      RacerModule.m_Mod.SetViewSize(RacerModule.m_CamsIDs[0], 1.0F, 1.0F);
      RacerModule.m_Mod.SetViewOrigin(RacerModule.m_CamsIDs[0], 0.0F, 0.0F);
      if (RacerGame.GetCoreLogic().IsMultiplayer()) {
         RacerModule.m_Mod.DeactivateView(RacerModule.m_CamsIDs[1]);
         RacerModule.m_Mod.RemoveView(RacerModule.m_CamsIDs[1]);
      }
   }

   public void RemoveSound() {
      m_iNofCurrPlayingSounds += -1;
      if (m_iNofCurrPlayingSounds < 0) {
         m_iNofCurrPlayingSounds = 0;
      }
   }

   void Respawn() {
      this.ZeroRespawnParams();
      this.PlayRespawnSound();
      this.m_iCurrTrackPoint = RacerModule.m_Mod.m_Track.GetPrevPointNum(this.m_iCurrTrackPoint);
      this.m_iCurrTrackPoint = RacerModule.m_Mod.m_Track.GetPrevPointNum(this.m_iCurrTrackPoint);
      if (this.m_iCurrTrackPoint != -1) {
         if (RacerModule.m_Mod.m_Track.GetPointPos(this.m_iCurrTrackPoint) != null) {
            Vector var1 = new Vector(RacerModule.m_Mod.m_Track.GetPointPos(this.m_iCurrTrackPoint));
            var1 = this.RandomProperPos(var1);
            this.m_vVehiclePosition.Set(var1);
            Vector var2 = new Vector(this.m_vVehiclePosition);
            var2.fY += 10000.0F;
            this.m_vVehiclePosition.fY = RacerModule.m_Mod.TerrainHeight(this.m_vVehiclePosition.fX, this.m_vVehiclePosition.fZ) - 12000.0F;
            this.TestTrack(var2, this.m_vVehiclePosition);
            this.m_vVehiclePosition.Set(this.m_collInfo.vEnd);
            this.m_vVehiclePosition.fY = this.m_vVehiclePosition.fY + this.m_Params._RESPAWN_HEIGHT;
            this.m_iCurrTrackPoint = RacerModule.m_Mod.m_Track.GetNextPointNum(this.m_iCurrTrackPoint);
            if (this.m_iCurrTrackPoint == -1) {
               return;
            }

            var1.Set(RacerModule.m_Mod.m_Track.GetPointPos(this.m_iCurrTrackPoint));
            var1.Sub(this.m_vVehiclePosition);
            var1.fY = 0.0F;
            var1.Normalize();
            this.m_vVehicleForward.Set(var1);
            this.m_vVehicleNormal.Set(Vector.Up);
         }

         this.m_iAIUpdatePointNo = this.m_iCurrTrackPoint;

         while (!RacerModule.m_Mod.m_Track.IsPointVisibleByAI(this.m_iAIUpdatePointNo)) {
            this.m_iAIUpdatePointNo = RacerModule.m_Mod.m_Track.GetNextPointNum(this.m_iAIUpdatePointNo);
         }

         this.m_fRespawnTime = RacerModule.m_Mod.m_fNowTime;
         if (this.m_iCurrTrackPoint == 0) {
            this.m_iCurrLoop += -1;
         }
      }
   }

   void AccelerateRelease() {
      this.m_fInAccelerate = 0.0F;
      if (!this.m_bTurbo) {
         if (this.m_bGetInput) {
            this.m_fAccelerate = 0.0F;
         }
      }
   }

   boolean MustRespawn() {
      Vector var1 = Vector.newVector();
      Vector var2 = new Vector();
      Vector var3 = Vector.newVector(this.m_vVehiclePosition);
      var3.fY = 0.0F;
      int var4 = RacerModule.m_Mod.m_Track.GetPrevPointNum(this.m_iCurrTrackPoint);
      if (var4 == -1) {
         var1.Null();
         var3.Null();
         return false;
      }

      var1.Set(RacerModule.m_Mod.m_Track.GetPointPos(this.m_iCurrTrackPoint));
      var1.Sub(RacerModule.m_Mod.m_Track.GetPointPos(var4));
      float var5 = var1.Len();
      var2.Set(var3);
      var2.Sub(RacerModule.m_Mod.m_Track.GetPointPos(var4));
      boolean var6 = false;
      if (var1.Cross(var2).fY > 0.0F) {
         var6 = true;
      }

      var1.Normalize();
      var2 = VehicleAI.ProjVector(RacerModule.m_Mod.m_Track.GetPointPos(var4), var3, var1);
      var5 = var2.Len() / var5;
      var2.Add(RacerModule.m_Mod.m_Track.GetPointPos(var4));
      var2.Sub(var3);
      float var7 = var2.Len();
      byte var8 = 0;
      if (RacerModule.m_Mod.m_Track.m_iAddLayers == 2) {
         var8 = 1;
      }

      if (var6) {
         var1.Set(RacerModule.m_Mod.m_Track.GetPoint(this.m_iCurrTrackPoint).GetLayerPoint(var8).m_vLeft);
         var1.Sub(RacerModule.m_Mod.m_Track.GetPoint(var4).GetLayerPoint(var8).m_vLeft);
         var1.Mul(var5);
         var1.Add(RacerModule.m_Mod.m_Track.GetPoint(var4).GetLayerPoint(var8).m_vLeft);
      } else {
         var1.Set(RacerModule.m_Mod.m_Track.GetPoint(this.m_iCurrTrackPoint).GetLayerPoint(var8).m_vRight);
         var1.Sub(RacerModule.m_Mod.m_Track.GetPoint(var4).GetLayerPoint(var8).m_vRight);
         var1.Mul(var5);
         var1.Add(RacerModule.m_Mod.m_Track.GetPoint(var4).GetLayerPoint(var8).m_vRight);
      }

      var2.Add(var3);
      var3.Null();
      var2.Sub(var1);
      var1.Null();
      return var7 > var2.Len();
   }

   void DecreaseVehicleVAngle() {
      if (this.m_fCurrVehicleVAngle >= 0.0F) {
         this.m_fCurrVehicleVAngle = this.m_fCurrVehicleVAngle - RacerModule.m_Mod.m_fFrameTime * this.m_Params._VERT_DEV_ACCEL;
         if (this.m_fCurrVehicleVAngle < 0.0F) {
            this.m_fCurrVehicleVAngle = 0.0F;
            return;
         }
      } else {
         this.m_fCurrVehicleVAngle = this.m_fCurrVehicleVAngle + RacerModule.m_Mod.m_fFrameTime * this.m_Params._VERT_DEV_ACCEL;
         if (this.m_fCurrVehicleVAngle > 0.0F) {
            this.m_fCurrVehicleVAngle = 0.0F;
         }
      }
   }

   void DecreaseVehicleHAngle() {
      if (this.m_fCurrVehicleHAngle >= 0.0F) {
         this.m_fCurrVehicleHAngle = this.m_fCurrVehicleHAngle - RacerModule.m_Mod.m_fFrameTime * this.m_Params._HORIZ_DEV_ACCEL;
         if (this.m_fCurrVehicleHAngle < 0.0F) {
            this.m_fCurrVehicleHAngle = 0.0F;
            return;
         }
      } else {
         this.m_fCurrVehicleHAngle = this.m_fCurrVehicleHAngle + RacerModule.m_Mod.m_fFrameTime * this.m_Params._HORIZ_DEV_ACCEL;
         if (this.m_fCurrVehicleHAngle > 0.0F) {
            this.m_fCurrVehicleHAngle = 0.0F;
         }
      }
   }

   float GetDefaultMaxTurnAngle() {
      float var1 = this.m_fDefaultMaxTurnAngle;
      if (this.m_VehicleAI != null) {
         var1 *= this.m_VehicleAI.GetTurningLevelModifier();
      }

      return var1;
   }

   public void GoAfterUFO() {
      // [mod] en ligne : le fantôme suit le réseau
      if (this.m_bModGhost) {
         return;
      }

      this.m_bWasTakenByUFO = false;
      this.SetFlag(256);
      this.m_iCurrPhysics = 0;
      this.m_fRespawnTime = RacerModule.m_Mod.m_fNowTime;
      this.PlayRespawnSound();
      this.Go();
   }

   void DecreaseTurning() {
      if (this.m_fTurnRight == 0.0F && this.m_fTurnLeft == 0.0F && this.m_fCurrDirAngle != 0.0F) {
         float var1;
         if (this.m_fCurrDirAngle < 0.0F) {
            var1 = this.m_fCurrDirAngle + this.m_Params._DECREASE_TURNING_SPEED * RacerModule.m_Mod.m_fFrameTime;
         } else {
            var1 = this.m_fCurrDirAngle - this.m_Params._DECREASE_TURNING_SPEED * RacerModule.m_Mod.m_fFrameTime;
         }

         if (var1 * this.m_fCurrDirAngle < 0.0F) {
            var1 = 0.0F;
         }

         this.m_fCurrDirAngle = var1;
      }
   }

   void DecreaseSlide() {
      float var1 = this.m_vSlideVector.Len() - this.m_Params._SLIDE_TIME_COEF * RacerModule.m_Mod.m_fFrameTime;
      if (var1 < 0.0F) {
         var1 = 0.0F;
      }

      this.m_vSlideVector.Normalize();
      this.m_vSlideVector.Mul(var1);
   }

   public void ThrowGunAway() {
      this.m_CurrentBonus.m_bWasUsed = true;
      this.m_Character.ThrowItem(false);
      this.m_CurrentBonus.Throw(this, false);
   }

   void BrakeAnalog(float var1) {
      this.m_fInBrake = var1;
      this.m_fInAccelerate = 0.0F;
      if (!this.m_bTurbo) {
         if (this.m_bGetInput) {
            this.m_fBrake = var1;
            this.m_fAccelerate = 0.0F;
         }
      }
   }

   void UpdateExplosionPhysics() {
      this.m_bFlying = true;
      this.SetEngineNeutralFreq();
      Vector var1 = Vector.newVector(Vector.Up);
      this.m_fCurrGravity = this.m_fCurrGravity + this.m_Params._GRAVITY_ACCEL * RacerModule.m_Mod.m_fFrameTime;
      var1.Mul(RacerModule.m_Mod.m_fFrameTime * this.m_fCurrGravity);
      Vector var2 = new Vector(this.m_vVehiclePosition);
      Vector var3 = new Vector(var2);
      var3.Add(var1);
      var1.Null();
      this.TestTrack(var2, var3);
      var3.Set(this.m_collInfo.vEnd);
      if (this.m_collInfo.coll != null) {
         if (Vector.Up.Dot(this.m_collInfo.coll.vNormal) < this.m_Params._MAX_SLOPE_ANGLE) {
            this.SetBounceVector(this.m_collInfo.coll.vNormal);
         }

         var3.fY += 0.01F;
         this.m_Character.PlayHitAnim();
         this.m_fCurrGravity = 0.0F;
         this.m_iCurrPhysics = 0;
         this.InputsRelease();
         this.UpdateVehicleInput(false);
         this.m_bStopped = true;
         this.m_bCanUseBonus = false;
         this.CallMethodIn("Go", 2.0F);
      }

      this.m_vVehiclePosition.Set(var3);
      if (this.m_fAddTurnAngle > 0.0F) {
         float var4 = RacerModule.m_Mod.m_fFrameTime * this.m_Params._HIT_ROTATION_SPEED;
         this.m_fAddTurnAngle -= var4;
         if (this.m_fAddTurnAngle < 0.0F) {
            var4 += this.m_fAddTurnAngle;
            this.m_fAddTurnAngle = 0.0F;
         }

         this.m_vVehicleNormal = this.RotateVector(this.m_vVehicleNormal, var4, this.m_vVehicleRight);
         this.m_vVehicleForward = this.RotateVector(this.m_vVehicleForward, var4, this.m_vVehicleRight);
      }

      this.DecreaseVehicleHAngle();
      this.DecreaseVehicleVAngle();
      this.m_Character.SetVAngle(this.m_fCurrVehicleVAngle, RacerModule.m_Mod.m_fFrameTime);
      this.m_Character.SetHAngle(-this.m_fCurrVehicleHAngle, RacerModule.m_Mod.m_fFrameTime);
      this.FromUpForwardPos(this.m_vVehicleNormal, this.m_vVehicleForward, this.m_vVehiclePosition);
   }

   void SetEngineFreq() {
      this.m_fCurrRevolution = this.CalcCurrRevolution();
      if (this.m_iNofEngineSound != 0) {
         float var1 = this.m_fCurrRevolution;
         var1 *= this.m_Params._MAX_SOUND_FREQ - this.m_Params._MIN_SOUND_FREQ;
         var1 += this.m_Params._MIN_SOUND_FREQ;
         this.SetSoundFreq(this.m_iNofEngineSound, var1);
      }
   }

   void CalculateSprings() {
      Vector var1 = Vector.newVector(this.m_vVehicleTNormal);
      var1.Mul(2.0F * this.m_Params._SUSPENSION_HEIGHT);
      int var2 = 0;

      do {
         this.vWheelTmp[var2].Set(this.GetElementPos(this.m_WheelsBase[var2]));
         this.vWheelTmp[var2].Add(var1);
      } while (++var2 < 4);

      var1.Negate();
      var1.Mul(2.0F);
      Vector var4 = null;
      int var3 = 0;

      do {
         var4 = new Vector(var1);
         var4.Add(this.vWheelTmp[var3]);
         this.TestTrack(this.vWheelTmp[var3], var4);
         var4.Set(this.m_collInfo.vEnd);
         if (this.m_collInfo.coll != null) {
            var4.Sub(this.vWheelTmp[var3]);
            this.m_SpringsNew[var3] = 2.0F * this.m_Params._SUSPENSION_HEIGHT + this._HALF_WHEEL_SIZE - var4.Len();
         } else {
            this.m_SpringsNew[var3] = this.m_Params._SPRING_NORMAL;
         }
      } while (++var3 < 4);

      var1.Null();
   }

   void FallingRock() {
      // [mod] en ligne : fantôme touché, l'effet est appliqué chez son joueur (ModNet)
      if (this.m_bModGhost) {
         ModNet.HitGhost(this, ModNet._HIT_ROCK);
         return;
      }

      this.m_bStopped = true;
      this.m_bCanUseBonus = false;
      this.InputsRelease();
      this.UpdateVehicleInput(false);
      this.ZeroVehicleParams();
      this.CallMethodIn("GoAfterRock", this.m_Params.WAIT_AFTER_ROCK_TIME);
   }

   void TurnRight() {
      if (this.m_fInTurnRight == 0.0F) {
         this.m_fLastRightPressTime = RacerModule.m_Mod.m_fNowTime;
      }

      this.m_fInTurnRight = 1.0F;
      this.m_fInTurnLeft = 0.0F;
      if (this.m_bGetInput) {
         this.m_fTurnRight = 1.0F;
         this.m_fTurnLeft = 0.0F;
      }
   }

   public void GoAfterRock() {
      this.DisableCallMethod("GoAfterRock");
      this.m_fRespawnTime = RacerModule.m_Mod.m_fNowTime;
      this.Go();
   }

   void UpdateVehicleInput(boolean var1) {
      this.m_bGetInput = var1;
      if (var1) {
         this.RestoreKbState();
      }
   }

   void CalculateVehicleInput(boolean var1) {
      this.m_bUpdateInput = var1;
   }

   void TurnLeft() {
      if (this.m_fInTurnLeft == 0.0F) {
         this.m_fLastLeftPressTime = RacerModule.m_Mod.m_fNowTime;
      }

      this.m_fInTurnLeft = 1.0F;
      this.m_fInTurnRight = 0.0F;
      if (this.m_bGetInput) {
         this.m_fTurnLeft = 1.0F;
         this.m_fTurnRight = 0.0F;
      }
   }

   float CalcCurrRevolution() {
      float var1 = this.m_fCurrSpeed;
      if (!(var1 > 0.0F)) {
         float var4;
         return var4 = var1 / this.m_Params._MIN_SPEED;
      }

      var1 /= this.m_fCurrMaxSpeed;

      for (int var2 = 1; var2 < this.m_Params._GEAR_SHIFT.length; var2++) {
         if (var1 <= this.m_Params._GEAR_SHIFT[var2]) {
            return (var1 - this.m_Params._GEAR_SHIFT[var2 - 1] + this.m_Params._GEAR_SHIFT_MOD[var2 - 1])
               / (this.m_Params._GEAR_SHIFT[var2] - this.m_Params._GEAR_SHIFT[var2 - 1] + this.m_Params._GEAR_SHIFT_MOD[var2 - 1]);
         }
      }

      return var1 > this.m_Params._GEAR_SHIFT[this.m_Params._GEAR_SHIFT.length - 1] ? 1.0F : 0.0F;
   }

   void CalculateSlopeAffect() {
      this.m_fSlopeAccelCoef = 1.0F - this.m_vVehicleForward.fY;
   }

   void InputsRelease() {
      this.m_fAccelerate = 0.0F;
      this.m_fBrake = 0.0F;
      this.m_fTurnLeft = 0.0F;
      this.m_fTurnRight = 0.0F;
   }

   void ChangeBonusSize(boolean var1) {
      this.ChangeBonusSize(var1, true);
   }

   void ChangeBonusSize(boolean var1, boolean var2) {
      if (this.m_CurrentBonus.m_UseType != 3 && this.m_CurrentBonus.m_UseType != 2 && this.m_CurrentBonus.m_UseType != 6) {
         this.m_CurrentBonus.m_bShowAfterScaleChange = var2;
         if (var1) {
            if (this.m_CurrentBonus.m_UseType == 4) {
               this.m_CurrentBonus.m_Scale = CorkPistol._SCALE_BEFORE_COLLECT_CORK;
               this.m_CurrentBonus.m_DestinationSize = CorkPistol._SCALE_AFTER_COLLECT_CORK;
            } else if (this.m_CurrentBonus.m_UseType == 16) {
               this.m_CurrentBonus.m_Scale = WaterPistol._SCALE_BEFORE_COLLECT_WATERPISTOL;
               this.m_CurrentBonus.m_DestinationSize = WaterPistol._SCALE_AFTER_COLLECT_WATERPISTOL;
            } else if (this.m_CurrentBonus.m_UseType == 21) {
               this.m_CurrentBonus.m_Scale = FallingRock._SCALE_BEFORE_COLLECT_FALLINGROCK;
               this.m_CurrentBonus.m_DestinationSize = FallingRock._SCALE_AFTER_COLLECT_FALLINGROCK;
            } else if (this.m_CurrentBonus.m_UseType == 22) {
               this.m_CurrentBonus.m_Scale = UFO._SCALE_BEFORE_COLLECT_UFO;
               this.m_CurrentBonus.m_DestinationSize = UFO._SCALE_AFTER_COLLECT_UFO;
            } else if (this.m_CurrentBonus.m_UseType == 15) {
               this.m_CurrentBonus.m_Scale = FreezePistol._SCALE_BEFORE_COLLECT_FREEZEPISTOL;
               this.m_CurrentBonus.m_DestinationSize = FreezePistol._SCALE_AFTER_COLLECT_FREEZEPISTOL;
            } else if (this.m_CurrentBonus.m_UseType == 17) {
               this.m_CurrentBonus.m_Scale = 0.3F;
               this.m_CurrentBonus.m_DestinationSize = 0.8F;
            } else if (this.m_CurrentBonus.m_UseType == 18) {
               this.m_CurrentBonus.m_Scale = SnowBall._SCALE_BEFORE_COLLECT_SNOWBALL;
               this.m_CurrentBonus.m_DestinationSize = SnowBall._SCALE_AFTER_COLLECT_SNOWBALL;
            } else if (this.m_CurrentBonus.m_UseType == 20) {
               this.m_CurrentBonus.m_Scale = Rocket._SCALE_BEFORE_COLLECT_ROCKET;
               this.m_CurrentBonus.m_DestinationSize = Rocket._SCALE_AFTER_COLLECT_ROCKET;
            } else if (this.m_CurrentBonus.m_UseType == 11) {
               this.m_CurrentBonus.m_Scale = WaterBottle._SCALE_BEFORE_COLLECT_BOTTLE;
               this.m_CurrentBonus.m_DestinationSize = WaterBottle._SCALE_AFTER_COLLECT_BOTTLE;
            } else if (this.m_CurrentBonus.m_UseType == 7) {
               this.m_CurrentBonus.m_Scale = FunnyGas._SCALE_BEFORE_COLLECT_GAS;
               this.m_CurrentBonus.m_DestinationSize = FunnyGas._SCALE_AFTER_COLLECT_GAS;
            } else if (this.m_CurrentBonus.m_UseType == 12) {
               this.m_CurrentBonus.m_Scale = Anvil._SCALE_BEFORE_COLLECT_ANVIL;
               this.m_CurrentBonus.m_DestinationSize = Anvil._SCALE_AFTER_COLLECT_ANVIL;
            } else {
               this.m_CurrentBonus.m_Scale = 0.5F;
               this.m_CurrentBonus.m_DestinationSize = 1.5F;
            }

            this.m_CurrentBonus.m_DeltaSize = 3.0F;
            this.m_CurrentBonus.m_bChangeSize = true;
         } else {
            if (this.m_CurrentBonus.m_UseType == 4) {
               this.m_CurrentBonus.m_Scale = CorkPistol._SCALE_AFTER_COLLECT_CORK;
               this.m_CurrentBonus.m_DestinationSize = CorkPistol._SCALE_BEFORE_COLLECT_CORK;
            } else if (this.m_CurrentBonus.m_UseType == 16) {
               this.m_CurrentBonus.m_Scale = WaterPistol._SCALE_AFTER_COLLECT_WATERPISTOL;
               this.m_CurrentBonus.m_DestinationSize = WaterPistol._SCALE_BEFORE_COLLECT_WATERPISTOL;
            } else if (this.m_CurrentBonus.m_UseType == 21) {
               this.m_CurrentBonus.m_Scale = FallingRock._SCALE_AFTER_COLLECT_FALLINGROCK;
               this.m_CurrentBonus.m_DestinationSize = FallingRock._SCALE_BEFORE_COLLECT_FALLINGROCK;
            } else if (this.m_CurrentBonus.m_UseType == 22) {
               this.m_CurrentBonus.m_Scale = UFO._SCALE_AFTER_COLLECT_UFO;
               this.m_CurrentBonus.m_DestinationSize = UFO._SCALE_BEFORE_COLLECT_UFO;
            } else if (this.m_CurrentBonus.m_UseType == 15) {
               this.m_CurrentBonus.m_Scale = FreezePistol._SCALE_AFTER_COLLECT_FREEZEPISTOL;
               this.m_CurrentBonus.m_DestinationSize = FreezePistol._SCALE_BEFORE_COLLECT_FREEZEPISTOL;
            } else if (this.m_CurrentBonus.m_UseType == 17) {
               this.m_CurrentBonus.m_Scale = 0.8F;
               this.m_CurrentBonus.m_DestinationSize = 0.3F;
            } else if (this.m_CurrentBonus.m_UseType == 18) {
               this.m_CurrentBonus.m_Scale = SnowBall._SCALE_AFTER_COLLECT_SNOWBALL;
               this.m_CurrentBonus.m_DestinationSize = SnowBall._SCALE_BEFORE_COLLECT_SNOWBALL;
            } else if (this.m_CurrentBonus.m_UseType == 20) {
               this.m_CurrentBonus.m_Scale = Rocket._SCALE_AFTER_COLLECT_ROCKET;
               this.m_CurrentBonus.m_DestinationSize = Rocket._SCALE_BEFORE_COLLECT_ROCKET;
            } else if (this.m_CurrentBonus.m_UseType == 11) {
               this.m_CurrentBonus.m_Scale = WaterBottle._SCALE_AFTER_COLLECT_BOTTLE;
               this.m_CurrentBonus.m_DestinationSize = WaterBottle._SCALE_BEFORE_COLLECT_BOTTLE;
            } else if (this.m_CurrentBonus.m_UseType == 7) {
               this.m_CurrentBonus.m_Scale = FunnyGas._SCALE_AFTER_COLLECT_GAS;
               this.m_CurrentBonus.m_DestinationSize = FunnyGas._SCALE_BEFORE_COLLECT_GAS;
            } else if (this.m_CurrentBonus.m_UseType == 12) {
               this.m_CurrentBonus.m_Scale = Anvil._SCALE_AFTER_COLLECT_ANVIL;
               this.m_CurrentBonus.m_DestinationSize = Anvil._SCALE_BEFORE_COLLECT_ANVIL;
            } else {
               this.m_CurrentBonus.m_Scale = 1.5F;
               this.m_CurrentBonus.m_DestinationSize = 0.5F;
            }

            this.m_CurrentBonus.m_DeltaSize = -3.0F;
            this.m_CurrentBonus.m_bChangeSize = true;
         }
      }
   }

   public void OnCollisionFullInfoMove(ControlObject var1, Vector var2, Vector var3, Vector var4) {
      float var5 = 0.0F;
      if (this.m_iCurrPhysics != 1) {
         int var6 = TrackObject._CLASS_UNKNOWN;
         TrackObject var7 = null;
         if (var1 == null) {
            var6 = TrackObject._CLASS_TREE;
            var5 = RacerModule.m_Mod.m_fNowTime;
         } else {
            var7 = (TrackObject)var1;
            var6 = var7.m_iClassID;
            if (var6 == TrackObject._CLASS_TREE) {
               var5 = RacerModule.m_Mod.m_fNowTime;
            }
         }

         if (var6 != TrackObject._CLASS_WATER_POOL) {
            if (var6 == TrackObject._CLASS_VEHICLE) {
               Vehicle var18 = (Vehicle)var1;
               if (this.m_bTurbo) {
                  this.DisableTurbo();
               }

               if (this.m_bGotCollision) {
                  this.m_bGotCollision = false;
               } else {
                  if (RacerModule.m_Mod.m_fNowTime - this.m_fLastCollisionSound > this.m_Params._TIME_BETWEEN_HIT_SOUNDS) {
                     this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_CRASH_CAR]);
                     this.m_fLastCollisionSound = RacerModule.m_Mod.m_fNowTime;
                  }

                  this.m_vMoveDir.Mul(1.0F / RacerModule.m_Mod.m_fFrameTime);
                  var18.m_vMoveDir.Mul(1.0F / RacerModule.m_Mod.m_fFrameTime);
                  var18.m_bGotCollision = true;
                  Vector var19 = Vector.newVector(var3);
                  var19.Negate();
                  Vector var20 = Vector.newVector(var2);
                  var20.Add(this.m_vMoveDir);
                  var20.Set(VehicleAI.ProjVector(var2, var20, var19));
                  var19.Null();
                  Vector var21 = Vector.newVector(var2);
                  var21.Add(var18.m_vMoveDir);
                  var21.Set(VehicleAI.ProjVector(var2, var21, var3));
                  Vector var12 = Vector.newVector(var20);
                  var12.Negate();
                  var12.Add(this.m_vMoveDir);
                  Vector var13 = Vector.newVector(var21);
                  var13.Negate();
                  var13.Add(var18.m_vMoveDir);
                  Vector var14 = Vector.newVector();
                  float fModGhostKeep = 1.0F; // [mod] en ligne : part de vitesse gardée par un fantôme heurté
                  if (var21.Len2() != 0.0F && !(var21.Dot(var20) > 0.0F)) {
                     var21.Add(var20);
                     var20.Set(var21);
                     float var22 = this.m_Params._OBJ_HIT_SPEED_SUPR * RacerModule.m_Mod.m_fFrameTime;
                     if (var22 > 1.0F) {
                        var22 = 1.0F;
                     }

                     this.m_fCurrSpeed = this.m_fCurrSpeed - var22 * this.m_fCurrSpeed;
                     var18.m_fCurrSpeed = var18.m_fCurrSpeed - var22 * var18.m_fCurrSpeed;
                     fModGhostKeep = 1.0F - var22;
                  } else {
                     var14.Set(var20);
                     var20.Set(var21);
                     var21.Set(var14);
                     float var15 = this.m_Params._OBJ_HIT_SPEED_SUPR * RacerModule.m_Mod.m_fFrameTime;
                     if (var15 > 1.0F) {
                        var15 = 1.0F;
                     }

                     this.m_fCurrSpeed = this.m_fCurrSpeed - var15 * this.m_fCurrSpeed;
                  }

                  var14.Set(var20);
                  var20.Null();
                  var14.Add(var12);
                  var12.Null();
                  var14.Mul(this.m_Params._OBJ_HIT_SUPR * RacerModule.m_Mod.m_fFrameTime);
                  this.m_vCollisionVector.Add(var14);
                  if (this.m_vCollisionVector.Len() > this.m_fCollCoef) {
                     this.m_vCollisionVector.Normalize();
                     this.m_vCollisionVector.Mul(this.m_fCollCoef);
                  }

                  var14.Set(var21);
                  var21.Null();
                  var14.Add(var13);
                  var13.Null();
                  var14.Mul(this.m_Params._OBJ_HIT_SUPR * RacerModule.m_Mod.m_fFrameTime);
                  if (var18.m_bModGhost) {
                     // [mod] en ligne : la part du fantôme part chez son joueur (ModNet.BumpGhost)
                     ModNet.BumpGhost(this, var18, var14, fModGhostKeep);
                  }

                  var18.m_vCollisionVector.Add(var14);
                  var14.Null();
                  if (var18.m_vCollisionVector.Len() > this.m_fCollCoef) {
                     var18.m_vCollisionVector.Normalize();
                     var18.m_vCollisionVector.Mul(this.m_fCollCoef);
                  }

                  this.m_vMoveDir.Set(var4);
                  var18.m_vMoveDir.Mul(RacerModule.m_Mod.m_fFrameTime);
               }
            } else if (var6 == TrackObject._CLASS_TREE || var6 == TrackObject._CLASS_ANVIL && ((Anvil)var7).m_bSolid) {
               if (this.m_VehicleAI != null) {
                  this.m_VehicleAI.ModOnObstacle(var3); // [mod] l'IA retient ce point et le côté de l'obstacle
               }

               if (var6 == TrackObject._CLASS_ANVIL && ((Anvil)var7).m_bSolid) {
                  this.m_iStopCause = 3;
                  this.StopByObstacle();
               } else if (this.m_fCurrSpeed > this.m_fCurrMaxSpeed * 0.5F) {
                  this.m_Character.PlayHitAnim();
               }

               if (RacerModule.m_Mod.m_fNowTime - this.m_fLastCollisionSound > this.m_Params._TIME_BETWEEN_HIT_SOUNDS) {
                  this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_CRASH]);
                  this.m_fLastCollisionSound = RacerModule.m_Mod.m_fNowTime;
               }

               if (this.m_bTurbo) {
                  this.DisableTurbo();
               }

               if (var5 > 0.0F) {
                  if (this.m_fLastTreeCollTime + 0.2F > RacerModule.m_Mod.m_fNowTime) {
                     this.m_fTreeCollTime = this.m_fTreeCollTime + (RacerModule.m_Mod.m_fNowTime - this.m_fLastTreeCollTime);
                  } else {
                     this.m_fTreeCollTime = 0.0F;
                  }

                  if (this.m_fTreeCollTime > 2.0F) {
                     this.m_fTreeCollTime = 0.0F;
                     this.m_bMustRespawn = true;
                  }

                  this.m_fLastTreeCollTime = var5;
               }

               if (var4.Len2() > 0.0F) {
                  Vector var8 = null;
                  this.m_vMoveDir.Set(var4);
                  Vector var9 = Vector.newVector(this.m_vMoveDir);
                  var9.Normalize();
                  Vector var10 = Vector.newVector(Vector.Up);
                  if (var3.Dot(var10) < 0.95F) {
                     var3.fY = 0.0F;
                     var3.Normalize();
                  } else {
                     var3.Set(this.m_vMoveDir);
                     var3.fY = 0.0F;
                     var3.Negate();
                     var3.Normalize();
                  }

                  float var11 = 180.0F - (float)Math.acos(var3.Dot(var9)) * 57.29F;
                  if (var3.Cross(var9).fY < 0.0F) {
                     var11 *= -1.0F;
                  }

                  var9.Null();
                  var8 = this.RotateVector(var3, var11, var10);
                  var10.Null();
                  var8.Mul(this.m_fBeforeMoveSpeed);
                  var8.Mul(this.m_Params._TREE_HIT_SUPR);
                  this.m_vCollisionVector.Add(var8);
                  if (this.m_vCollisionVector.Len() > this.m_fCollCoef) {
                     this.m_vCollisionVector.Normalize();
                     this.m_vCollisionVector.Mul(this.m_fCollCoef);
                  }
               }

               this.m_fCurrSpeed = 0.0F;
            } else if (var6 == TrackObject._CLASS_RESPAWN) {
               this.RespawnForward();
            } else {
               if (this.m_VehicleAI != null && var6 != TrackObject._CLASS_POLE && var3 != null && Vector.Up.Dot(var3) < this.m_Params._MAX_SLOPE_ANGLE) {
                  // [mod] mur, maison, pont : l'IA retient ce point et le côté de l'obstacle
                  this.m_VehicleAI.ModOnObstacle(var3);
               }

               if (var6 == TrackObject._CLASS_POLE) {
                  this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_BREAK_WOOD]);
                  this.ExecuteFX(this, "FXHit", Vector.Zero);
                  this.m_fCurrSpeed = this.m_fCurrSpeed * this.m_Params._POLE_HIT_COEF;
               }
            }
         }
      }
   }

   void StopByObstacle() {
      this.InputsRelease();
      this.UpdateVehicleInput(false);
      this.SystemBrake();
      this.m_bCanUseBonus = false;
      if (this.m_iStopCause != 5 && !this.m_bRaceFinished) {
         if (this.m_CurrentBonus != null && !this.m_CurrentBonus.m_bWasUsed) {
            this.ChangeBonusSize(false, false);
            this.m_Character.HideItem(this.m_iStopCause);
            this.CallMethodIn("GoAfterAnim", _CHECK_FOR_GO_ANIM);
         } else {
            this.PlayStopAnim();
         }

         this.CheckForStop();
      } else {
         this.CheckForStop();
      }
   }

   public void FinishRace() {
      if (!RacerModule.m_bIsDemo) {
         RacerModule.m_Mod.m_bExitAllowed = false;
      }

      RacerModule.m_Mod.StopCalculatePositions();
      this.FillTimeList();
      // [mod] démo du menu : aucun joueur, elementAt(0) plantait
      if (!RacerGame.GetCoreLogic().IsMultiplayer() && RacerModule.m_Mod.m_RaceParams.m_PlayersTab.size() > 0) {
         Vehicle var4 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_PlayersTab.elementAt(0);
         if (var4.m_iCurrPlace == 0) {
            RacerModule.m_Mod.PlayMusic(10, 1.0F);
         } else {
            RacerModule.m_Mod.PlayMusic(11, 1.0F);
         }
      }

      this.CallMethodIn("ReturnToMenu", ModNet.StopTimeAfterRace(this.m_Params._STOP_TIME_AFTER_RACE)); // [mod]
   }

   // [mod] rang d'arrivée réel (0 = premier), -1 = pas encore arrivé
   int m_iFinishRank = -1;
   // [mod] en ligne (ModNet) : voiture d'un autre joueur posée d'après le réseau, sans physique ; slot du joueur ;
   // joueur parti (voiture cachée, classée derrière)
   boolean m_bModGhost = false;
   boolean m_bModGhostGone = false;
   int m_iModNetSlot = -1;

   // [mod] Appelé quand la voiture passe la ligne pour la dernière fois. La place (m_iCurrPlace) vient du classement
   // à la distance, refait toutes les 0,1 s par RacerModule.CalcPos, qui ne déplace plus une voiture arrivée : une
   // voiture qui doublait juste avant la ligne pouvait finir avec la place de celle qu'elle venait de passer
   // (HUD, musique de victoire). On la range à son rang d'arrivée dans la table de tri.
   void SetFinishRank() {
      RaceParams var1 = RacerModule.m_Mod.m_RaceParams;
      this.m_iFinishRank = var1.m_nFinished++;
      java.util.Vector var2 = var1.m_SortTable;
      int var3 = var2.indexOf(this);
      if (var3 >= 0 && var3 != this.m_iFinishRank && this.m_iFinishRank < var2.size()) {
         var2.removeElementAt(var3);
         var2.insertElementAt(this, this.m_iFinishRank);
      }

      for (int var4 = 0; var4 < var2.size(); var4++) {
         ((Vehicle)var2.elementAt(var4)).m_iCurrPlace = var4;
      }
   }

   // [mod] Remplit la liste des temps (podium, records, progression de l'aventure) dans l'ordre d'arrivée réel :
   // les voitures arrivées par rang, puis les autres par distance parcourue (temps extrapolé, comme l'original).
   // TimeList.Push trie par temps et garde l'ordre d'insertion à temps égal : on insère dans l'ordre voulu avec des
   // temps qui ne décroissent jamais.
   // Premier argument d'AddNewTime (TimeEntry.m_AnimalPos) : l'original y mettait la place, et
   // CoreLogic.GetNewRecordPlayer prend l'entrée 0 pour le joueur : une IA gagnante offrait son temps au record du
   // joueur. On y met 0 pour l'humain le mieux classé, 1 pour le second (2 joueurs), 10 et plus pour les IA.
   void FillTimeList() {
      RaceParams var1 = RacerModule.m_Mod.m_RaceParams;
      CoreLogic var2 = RacerGame.GetCoreLogic();
      var2.ClearTimeList();
      int var3 = var1.m_Vehicles.size();
      Vehicle[] var4 = new Vehicle[var3];
      int var5 = 0;

      for (int var6 = 0; var6 < var1.m_nFinished; var6++) {
         for (int var7 = 0; var7 < var3; var7++) {
            Vehicle var8 = (Vehicle)var1.m_Vehicles.elementAt(var7);
            if (var8 != null && var8.m_iFinishRank == var6) {
               var4[var5++] = var8;
            }
         }
      }

      int var9 = var5;

      for (int var10 = 0; var10 < var3; var10++) {
         Vehicle var11 = (Vehicle)var1.m_Vehicles.elementAt(var10);
         if (var11 != null && var11.m_iFinishRank < 0) {
            int var12 = var5++;

            // [mod] joueur en ligne parti : derrière tous les autres
            while (var12 > var9 && ModRankDistance(var4[var12 - 1]) < ModRankDistance(var11)) {
               var4[var12] = var4[var12 - 1];
               var12--;
            }

            var4[var12] = var11;
         }
      }

      float var13 = 0.0F;
      int var14 = 0;

      for (int var15 = 0; var15 < var5; var15++) {
         Vehicle var16 = var4[var15];
         float var17 = var16.CalculateRaceTime() / var1.m_iNofLoops;
         if (!(var17 >= var13)) {
            var17 = var13;
         }

         var13 = var17;
         int var18 = var1.m_PlayersTab.indexOf(var16) >= 0 ? var14++ : 10 + var15;
         var2.AddNewTime(var18, var16.m_iAnimalID, var17);
      }
   }

   // [mod] distance pour le classement des non-arrivés : un joueur en ligne parti passe derrière
   float ModRankDistance() {
      return this.m_bModGhostGone ? -1.0F : this.m_fCoveredDistance;
   }

   static float ModRankDistance(Vehicle v) {
      return v.ModRankDistance();
   }

   public void WinRace() {
      Vehicle var1 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_Vehicles.elementAt(0);
      RacerModule.m_Mod.m_bExitAllowed = false;
      this.m_bRaceFinished = true;
      this.m_iStopCause = 5;
      this.StopByObstacle();
      this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_RACE_FINISHED]);
      RacerModule.m_Mod.StopCalculatePositions();
      RacerGame.GetCoreLogic().ClearTimeList();
      RacerGame.GetCoreLogic().AddNewTime(0, var1.m_iAnimalID, var1.CalculateRaceTime() / RacerModule.m_Mod.m_RaceParams.m_iNofLoops);

      for (int var2 = 1; var2 < RacerModule.m_Mod.m_RaceParams.m_Vehicles.size(); var2++) {
         var1 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_Vehicles.elementAt(var2);
         RacerGame.GetCoreLogic().AddNewTime(var2, var1.m_iAnimalID, var1.CalculateRaceTime() / RacerModule.m_Mod.m_RaceParams.m_iNofLoops);
      }

      // [mod] démo du menu : aucun joueur, elementAt(0) plantait
      if (!RacerGame.GetCoreLogic().IsMultiplayer() && RacerModule.m_Mod.m_RaceParams.m_PlayersTab.size() > 0) {
         Vehicle var4 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_PlayersTab.elementAt(0);
         if (var4.m_iCurrPlace == 0) {
            RacerModule.m_Mod.PlayMusic(10, 1.0F);
         } else {
            RacerModule.m_Mod.PlayMusic(11, 1.0F);
         }
      }

      this.CallMethodIn("ReturnToMenu", this.m_Params._STOP_TIME_AFTER_RACE);
   }

   float GetCurrAccel(float var1) {
      if (var1 > 1.0F) {
         var1 = 1.0F;
      }

      if (var1 < 0.0F) {
         var1 = 0.0F;
      }

      float var2 = var1 - 1.0F;
      var2 *= var2;
      return (this.m_fDefaultMinAccel + var2 * (this.m_fDefaultMaxAccel - this.m_fDefaultMinAccel)) * this.m_fRandomStartCoef;
   }

   public void DisableRandomAccel() {
      this.m_fRandomStartCoef = 1.0F;
   }

   void DisableTurbo() {
      this.DisableCallMethod("DisableTurbo");
      this.ModEndActiveBonus(2); // [mod] le turbo n'est plus forcément dans la case (ModParkActiveBonus)
      this.m_bTurbo = false;
      this.RestoreKbState();
   }

   float CalculateRaceTime() {
      return this.m_fFinishClockRaceTime > 0.0F
         ? this.m_fFinishClockRaceTime
         : RacerModule.m_Mod.GetClock().GetMissionTime()
            + (1.0F - this.m_fCoveredDistance) * RacerModule.m_Mod.GetClock().GetMissionTime() / this.m_fCoveredDistance;
   }

   void UpdateVehicleCharacter() {
      this.m_Character.SetVAngle(this.m_fCurrVehicleVAngle, RacerModule.m_Mod.m_fFrameTime);
      this.m_Character.SetHAngle(-this.m_fCurrVehicleHAngle, RacerModule.m_Mod.m_fFrameTime);
      this.m_Character.AimUsingAngle(this.m_fAimAngle);
   }

   public void CheckForStopByUFO() {
      this.m_bStopped = true;
      this.DisableCallMethod("CheckForStopByUFO");
      if (this.m_fCurrSpeed <= 0.0F) {
         this.m_iCurrPhysics = 5;
         this.ClearFlag(256);
         this.m_fBrake = 0.0F;
      } else {
         this.CallMethodIn("CheckForStopByUFO", _CHECK_GO_UPDATE_TIME);
      }
   }

   void UpdateSpringsAndWheels() {
      this.CalculateSprings();
      this.AlignSprings();
      this.UpdateVehicleWheels();
   }

   void SlowDown() {
      float var1 = this.m_Params._SLIDE_SPEED_COEF * this.m_vSlideVector.Len() / this.m_Params._MAX_BOUNCE_RANGE;
      if (this.m_fCurrSpeed >= 0.0F) {
         this.m_fCurrSpeed = this.m_fCurrSpeed - this.m_fCurrSpeed * RacerModule.m_Mod.m_fFrameTime * var1;
         if (this.m_fCurrSpeed < 0.0F) {
            this.m_fCurrSpeed = 0.0F;
            return;
         }
      } else {
         this.m_fCurrSpeed = this.m_fCurrSpeed - this.m_fCurrSpeed * RacerModule.m_Mod.m_fFrameTime * var1;
         if (this.m_fCurrSpeed > 0.0F) {
            this.m_fCurrSpeed = 0.0F;
         }
      }
   }

   void FallDown() {
      Vector var1 = new Vector(this.m_vVehicleNormal);
      var1.Mul(this.m_Params._SUSPENSION_HEIGHT);
      var1.Add(this.m_vVehiclePosition);
      Vector var2 = new Vector(this.m_vVehiclePosition);
      var2.Sub(var1);
      var2.Mul(2.2F);
      var2.Add(var1);
      if (this.m_bLanding) {
         this.m_fLandingTime = this.m_fLandingTime - RacerModule.m_Mod.m_fFrameTime;
         if (this.m_fLandingTime <= 0.0F) {
            this.m_fLandingTime = 0.0F;
            this.m_fFlyingTime = 0.0F;
            this.m_bFlying = false;
            this.m_bLanding = false;
            return;
         }
      }

      this.TestTrack(var1, var2);
      var2.Set(this.m_collInfo.vEnd);
      if (this.m_collInfo.coll != null) {
         if (!this.m_bLanding) {
            this.m_bLanding = true;
            this.m_fLandingTime = this.m_Params._MAX_LANDING_TIME;
            this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_FALL]);
         }

         Vector var3 = this.CalculateNewNormal();
         if (var3.Len2() == 0.0F) {
            this.AlignToUp();
         } else {
            var3.Mul(this.m_Params._GROUND_FLY_ALIGN_INERTIA * RacerModule.m_Mod.m_fFrameTime);
            var3.Add(this.m_vVehicleNormal);
            var3.Normalize();
            this.m_vVehicleNormal.Set(var3);
            this.SetVehiclePosAndOrientation();
         }
      } else {
         this.AlignToUp();
      }
   }

   void Brake() {
      this.m_fInBrake = 1.0F;
      this.m_fInAccelerate = 0.0F;
      if (!this.m_bTurbo) {
         if (this.m_bGetInput) {
            this.m_fBrake = 1.0F;
            this.m_fAccelerate = 0.0F;
            if (!this.m_bIsFlyingOnFrame) {
               if (this.m_fCurrSpeed > 0.0F && RacerModule.m_Mod.m_fNowTime - this.m_fBrakePressTime > this.m_Params._TIME_BETWEEN_BRAKE_SOUNDS) {
                  this.PlayBrakeSound();
                  this.m_fBrakePressTime = RacerModule.m_Mod.m_fNowTime;
               }
            }
         }
      }
   }

   void SystemBrake() {
      if (this.m_bTurbo) {
         this.DisableTurbo();
      }

      this.m_fBrake = 1.0F;
      this.m_fAccelerate = 0.0F;
      if (RacerModule.m_Mod.m_fNowTime - this.m_fBrakePressTime > this.m_Params._TIME_BETWEEN_BRAKE_SOUNDS) {
         this.m_bPlayBrakeSound = true;
         this.m_fBrakePressTime = RacerModule.m_Mod.m_fNowTime;
      }
   }

   void DriveInIce() {
      this.m_iCurrPhysics = 2;
      this.m_bCanUseBonus = false;
      this.CallMethodIn("RespawnAfterFalling", this.m_Params._FALL_TIME);
   }

   public void CheckForBridge() {
      if (this.m_fCurrSpeed != 0.0F && this.m_bIsObservedByCamera) {
         Vector var1 = new Vector(this.m_vVehiclePosition);
         var1.fY += 30.0F;
         Vector var2 = new Vector(var1);
         var2.fY -= 100.0F;
         Collision var3 = this.TraceObjects(var1, var2);
         if (var3 != null) {
            TrackObject var4 = (TrackObject)var3.Obstacle;
            if (var4.m_iClassID == TrackObject._CLASS_BRIDGE) {
               this.AttachBridgeSound(VehicleParams._SOUNDS[VehicleParams._SND_BRIDGE]);
               return;
            }

            if (var4.m_iClassID == TrackObject._CLASS_STONE_BRIDGE) {
               this.AttachBridgeSound(VehicleParams._SOUNDS[VehicleParams._SND_STONE_BRIDGE]);
               return;
            }
         }

         this.DetachBridgeSound();
      } else {
         this.DetachBridgeSound();
      }
   }

   void AttachEngineSound() {
      if (this.m_iNofEngineSound == 0) {
         this.m_iNofEngineSound = this.AttachSound(MenuAnimal.GetCarEngine(this.m_iAnimalID), 900.0F, 5000.0F);
         this.SetLowEngineVolume();
      }
   }

   void AttachBridgeSound(String var1) {
      if (this.m_iBridgeSound == 0) {
         this.DetachOffRoadSound();
         this.DetachRoadSound();
         this.m_iBridgeSound = this.AttachSound(var1);
      }
   }

   void AttachOffRoadSound(String var1) {
      if (this.m_iNofOffRoadSound == 0) {
         this.m_iNofOffRoadSound = this.AttachSound(var1);
      }
   }

   void StoppedByWaspBag() {
      // [mod] en ligne : fantôme touché, l'effet est appliqué chez son joueur (ModNet)
      if (this.m_bModGhost) {
         ModNet.HitGhost(this, ModNet._HIT_WASPS);
         return;
      }

      this.m_iStopCause = 1;
      this.StopByObstacle();
   }

   void PlayThrowSound() {
      if (this.CanPlayTalkSound()) {
         if (!(RacerGame.GetRandom() > this.m_Params._PROB_PLAY_THROW_SOUND)) {
            if (this.CanAttachOneShotSound()) {
               int var1 = this.m_iAnimalID * MenuAnimal._NUM_OF_THROW_SOUNDS_PER_ANIMAL + GameObject.RandomInt(MenuAnimal._NUM_OF_THROW_SOUNDS_PER_ANIMAL);
               this.AttachOneShotSound(MenuAnimal._THROW_SOUNDS[var1], VehicleParams._MIN_TALK_RANGE, VehicleParams._MAX_TALK_RANGE);
            }
         }
      }
   }

   void PlayMissedSound() {
      if (this.CanPlayTalkSound()) {
         if (!(RacerGame.GetRandom() > this.m_Params._PROB_PLAY_MISSED_SOUND)) {
            if (this.CanAttachOneShotSound()) {
               this.AttachOneShotSound(MenuAnimal._MISSED_SOUNDS[this.m_iAnimalID], VehicleParams._MIN_TALK_RANGE, VehicleParams._MAX_TALK_RANGE);
            }
         }
      }
   }

   boolean CanPlayTalkSound() {
      if (RacerModule.m_Mod.m_fNowTime - this.m_fLastTalkTime < VehicleParams._MIN_TIME_BETW_TALK) {
         return false;
      }

      this.m_fLastTalkTime = RacerModule.m_Mod.m_fNowTime;
      return true;
   }

   void PlayRespawnSound() {
      if (((RacerCamera)RacerModule.m_Cams[0]).m_Observed == this) {
         this.PlayWave(VehicleParams._SOUNDS[VehicleParams._SND_RESPAWN]);
      }

      if (RacerModule.m_Cams[1] != null && ((RacerCamera)RacerModule.m_Cams[1]).m_Observed == this) {
         this.PlayWave(VehicleParams._SOUNDS[VehicleParams._SND_RESPAWN]);
      }
   }

   void PlaySlideSound() {
      if (!this.m_bIsObservedByCamera) {
         this.StopSlideSound();
      } else if (this.m_iNofSlideSound == 0) {
         if (!this.m_bIsOnTrack) {
            this.m_iNofSlideSound = this.AttachSound(VehicleParams._CAR_SOUNDS[VehicleParams._SLIDE_OFF_ROAD]);
         } else {
            this.m_iNofSlideSound = this.AttachSound(VehicleParams._CAR_SOUNDS[VehicleParams._SLIDE_ROAD]);
         }
      }
   }

   void BrakeRelease() {
      this.m_fInBrake = 0.0F;
      if (!this.m_bTurbo) {
         if (this.m_bGetInput) {
            this.m_fBrake = 0.0F;
         }
      }
   }

   public void StoppedByTomatoOrCookie() {
      // [mod] en ligne : fantôme touché, l'effet est appliqué chez son joueur (ModNet)
      if (this.m_bModGhost) {
         ModNet.HitGhost(this, ModNet._HIT_TOMATO);
         return;
      }

      this.m_iStopCause = 4;
      this.StopByObstacle();
   }

   void ExecuteSmokeFxs() {
      if (this.m_bTurbo) {
         this.FourWheelsSmokeFX();
         this.TurboFire();
      }

      if (!this.m_bFlying) {
         if (this.m_fAccelerate > 0.0F) {
            if (this.m_fCurrSpeed > 0.0F && this.m_fCurrSpeed < this.m_fCurrMaxSpeed / 20.0F) {
               this.TwoWheelsSmokeFX();
               if (!this.m_bIsFlyingOnFrame && RacerModule.m_Mod.m_fNowTime - this.m_fLastAccelPressTime < 0.1F) {
                  this.PlayAccelSound();
               }
            }

            if (this.m_fCurrSpeed < 0.0F) {
               this.FourWheelsSmokeFX();
            }
         }

         if (this.m_fBrake > 0.0F) {
            if (this.m_fCurrSpeed < 0.0F && this.m_fCurrSpeed > this.m_Params._MIN_SPEED / 20.0F) {
               this.TwoWheelsSmokeFX();
               if (!this.m_bIsFlyingOnFrame && RacerModule.m_Mod.m_fNowTime - this.m_fLastAccelPressTime < 0.1F) {
                  this.PlayAccelSound();
               }
            }

            if (this.m_fCurrSpeed > 0.0F) {
               this.FourWheelsSmokeFX();
            }
         }
      }
   }

   public void CheckForNearPlayers() {
      boolean var1 = false;
      Vector var2 = Vector.newVector();

      for (int var3 = 0; var3 < RacerModule.m_Mod.m_RaceParams.m_Vehicles.size(); var3++) {
         var2.Set(((Vehicle)RacerModule.m_Mod.m_RaceParams.m_Vehicles.elementAt(var3)).m_vVehiclePosition);
         var2.Sub(this.m_vVehiclePosition);
         var2.fY = 0.0F;
         if (var2.Len() < this.m_Params._MIN_LOOK_DISTANCE) {
            this.m_Character.LookAround();
            var1 = true;
         }
      }

      var2.Null();
      if (!var1 && RacerGame.GetRandom() < 0.2F) {
         this.m_Character.LookAround();
      }

      this.CallMethodIn(
         "CheckForNearPlayers",
         this.m_Params._MIN_TIME_BETWEEN_LOOK + (this.m_Params._MAX_TIME_BETWEEN_LOOK - this.m_Params._MIN_TIME_BETWEEN_LOOK) * RacerGame.GetRandom()
      );
   }

   void StopAim() {
      this.m_fAimAngle = 0.0F;
      this.m_LockedObject = null;
      this.DisableCallMethod("AimAtEnemy");
   }

   public void AimAtEnemy() {
      Object var1 = null;
      Vehicle var2 = null;
      Vector var3 = Vector.newVector();
      float var4 = -1.0F;
      float var5 = -1.0F;
      this.m_LockedObject = null;
      // [mod] Missile : il vise tout seul le premier. Pas de viseur pour le joueur (il serait sur la voiture devant, ou
      // hors de l'écran) ; l'IA, qui n'attend qu'une cible verrouillée pour tirer (VehicleAI.UseBonus), l'a tout de suite.
      if (Rocket.ModHoldsMissile(this)) {
         var3.Null();
         this.m_fAimAngle = 0.0F;
         this.m_vAimVector.Set(this.m_vVehicleForward);
         if (this.m_VehicleAI != null) {
            this.m_LockedObject = Rocket.ModFindLeader(this);
         }

         this.CallMethodIn("AimAtEnemy", _AIM_UPDATE_TIME);
         return;
      }

      for (int var6 = 0; var6 < RacerModule.m_Mod.m_RaceParams.m_Vehicles.size(); var6++) {
         var1 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_Vehicles.elementAt(var6);
         if (var1 != this && !((Vehicle)var1).m_bProtection) {
            var3.Set(((Vehicle)var1).m_vVehiclePosition);
            var3.Sub(this.m_vVehiclePosition);
            var3.fY = 0.0F;
            if (!(var3.Len() > this.m_Params._MIN_AIM_DISTANCE)) {
               var3.Normalize();
               var5 = var3.Dot(this.m_vVehicleForward);
               if (var5 > var4) {
                  var4 = var5;
                  this.m_vAimVector.Set(var3);
                  var2 = (Vehicle)var1;
               }
            }
         }
      }

      var3.Null();
      if (var4 > this.m_Character._MAX_ROTATE_COS_ANGLE) {
         this.m_fAimAngle = (float)Math.acos(var4) * 57.29F;
         if (this.m_vVehicleForward.Cross(this.m_vAimVector).fY < 0.0F) {
            this.m_fAimAngle *= -1.0F;
         }

         float var9 = this.m_fAimAngle - this.m_Character.m_fCurrAimAngle;
         if (var9 >= -_AIM_PRECISION && var9 < _AIM_PRECISION) {
            this.m_LockedObject = var2;
         }
      } else {
         this.m_fAimAngle = 0.0F;
         this.m_vAimVector.Set(Vector.Zero);
      }

      this.CallMethodIn("AimAtEnemy", _AIM_UPDATE_TIME);
   }

   public void GoAfterAnim() {
      this.DisableCallMethod("GoAfterAnim");
      if (this.m_Character.m_bStateFinished) {
         this.m_Character.UnhideItem();
         if (this.m_CurrentBonus != null && !this.m_CurrentBonus.m_bWasUsed) {
            this.ChangeBonusSize(true, true);
         }

         this.CallMethodIn("Go", this.m_Params.WAIT_AFTER_BONUS_TIME);
      } else {
         this.CallMethodIn("GoAfterAnim", _CHECK_FOR_GO_ANIM);
      }
   }

   void StoppedByMole() {
      // [mod] en ligne : fantôme touché, l'effet est appliqué chez son joueur (ModNet)
      if (this.m_bModGhost) {
         ModNet.HitGhost(this, ModNet._HIT_MOLE);
         return;
      }

      this.m_iCurrPhysics = 2;
      this.m_bCanUseBonus = false;
      this.CallMethodIn("RespawnAfterFalling", this.m_Params._FALL_TIME);
   }

   void TestTrack(Vector var1, Vector var2) {
      float var3 = this.TerrainHeight(var1.fX, var1.fZ);
      if (var1.fY < var3) {
         var1.fY = var3 + 3.0F;
      }

      this.m_collInfo.coll = this.TraceTerrain(var1, var2);
      this.m_collInfo.vEnd.Set(var2);
      Collision var4 = this.TraceObjects(var1, var2);
      if (var4 != null) {
         TrackObject var5 = (TrackObject)var4.Obstacle;
         if (var5.m_iClassID == TrackObject._CLASS_PER_FACE
            || var5.m_iClassID == TrackObject._CLASS_BRIDGE
            || var5.m_iClassID == TrackObject._CLASS_STONE_BRIDGE) {
            this.m_collInfo.coll = var4;
            this.m_collInfo.vEnd.Set(var2);
         }
      }
   }

   void UpdateCurrDistance() {
      if (this.m_iCurrLoop < 0) {
         this.m_fCoveredDistance = 0.0F;
      } else if (this.m_iCurrLoop == RacerModule.m_Mod.m_RaceParams.m_iNofLoops) {
         this.m_fCoveredDistance = 1.0F;
      } else {
         float var1 = 0.0F;
         if (this.m_iCurrTrackPoint == 0) {
            if (this.m_iCurrLoop == -1) {
               var1 = 0.0F;
            } else {
               var1 = RacerModule.m_Mod.m_Track.m_TrackPoints.size() - 1.0F;
            }
         } else {
            var1 = this.m_iCurrTrackPoint - 1.0F;
         }

         this.m_fCoveredDistance = var1 / RacerModule.m_Mod.m_Track.m_TrackPoints.size() / RacerModule.m_Mod.m_RaceParams.m_iNofLoops;
         if (this.m_iCurrLoop > -1) {
            this.m_fCoveredDistance = this.m_fCoveredDistance + (float)this.m_iCurrLoop / RacerModule.m_Mod.m_RaceParams.m_iNofLoops;
         }

         Vector var2 = RacerModule.m_Mod.m_Track.GetPointPos(this.m_iCurrTrackPoint);
         Vector var3 = Vector.newVector(var2);
         var3.Sub(this.m_vVehiclePosition);
         var3.fY = 0.0F;
         float var4 = var3.Len();
         var3.Set(this.m_vVehiclePosition);
         var3.Sub(RacerModule.m_Mod.m_Track.GetPointPos(RacerModule.m_Mod.m_Track.GetPrevPointNum(this.m_iCurrTrackPoint)));
         var3.fY = 0.0F;
         float var5 = var3.Len();
         var3.Null();
         this.m_fCoveredDistance = this.m_fCoveredDistance
            + var5 / (var5 + var4) / (RacerModule.m_Mod.m_Track.m_TrackPoints.size() * RacerModule.m_Mod.m_RaceParams.m_iNofLoops);
      }
   }

   public void Unfreeze() {
      this.ExecuteParticleSystem("IceBreak", this.GetPosition(), Vector.Up);
      this.DisableCallMethod("Unfreeze");
      this.Go();
   }

   public void OnCreate() {
      super.m_iClassID = TrackObject._CLASS_VEHICLE;
      this.SetLighting(true);
      this.SetMeshMaterialAmbient(0.4F, 0.4F, 0.4F);
      this.SetPhysicsType(0);
      this.SetCollAction(0);
      this.SetCollFlags(4096);
      this.SetCollHandler(1024);
      this.ClearFlag(8);
      this.SetFlag(256);
      this.ClearFlag(4096);
      this.m_fCurrGravity = 0.0F;
      this.m_Wheels[0] = this.GetElementID("kolo_PP");
      this.m_Wheels[1] = this.GetElementID("kolo_LP");
      this.m_Wheels[2] = this.GetElementID("kolo_LT");
      this.m_Wheels[3] = this.GetElementID("kolo_PT");
      this.m_WheelsBase[0] = this.GetElementID("kolobase_PP");
      this.m_WheelsBase[1] = this.GetElementID("kolobase_LP");
      this.m_WheelsBase[2] = this.GetElementID("kolobase_LT");
      this.m_WheelsBase[3] = this.GetElementID("kolobase_PT");
      Vector var1 = null;
      var1 = this.GetElementPos(this.m_Wheels[0]);
      var1.Sub(this.GetPosition());
      this._HALF_WHEEL_SIZE = var1.fY;
      this.m_vVehicleNormal = new Vector(Vector.Up);
      this.m_vVehicleRight = this.GetRightVector();
      this.m_vVehicleTForward = new Vector(this.m_vVehicleForward);
      this.m_vVehicleTNormal = new Vector(this.m_vVehicleNormal);
      this.m_vVehiclePosition = this.GetPosition();
      int var2 = 0;

      do {
         this.vWheelTmp[var2] = new Vector(0.0F, 0.0F, 0.0F);
      } while (++var2 < 4);

      this.m_vCollisionVector = new Vector();
      this.m_vMoveDir = new Vector(this.GetForwardVector());
      this.UpdateMaxSpeedAccel();
      this.AttachEngineSound();
      this.SetSoundVolume(this.m_iNofEngineSound, 0.35F);
      this.SetEngineFreq();
      this.CallMethodIn(
         "CheckForNearPlayers",
         this.m_Params._MIN_TIME_BETWEEN_LOOK + (this.m_Params._MAX_TIME_BETWEEN_LOOK - this.m_Params._MIN_TIME_BETWEEN_LOOK) * RacerGame.GetRandom()
      );
      Vector var6 = Vector.newVector();
      Vector var3 = Vector.newVector();
      this.GetMeshExtentsInWorld(var6, var3);
      var3.Sub(var6);
      this.SetBlobMapSize(var3.fX * 0.9F);
      var6.Null();
      var3.Null();
      this.CallMethodEvery("CheckForWaterPool", 0.5F);
      // [mod] en ligne : pas de départ aléatoire (×0,6 à ×1,4 pendant 5 s), la course doit être la même pour tous
      this.m_fRandomStartCoef = ModNet.IsRaceOnline() ? 1.0F : 1.4F - RacerGame.GetRandom(0.8F);
      this.CallMethodIn("DisableRandomAccel", this.m_Params._DISABLE_ACCEL_TIME);
      if (GameObject.GetObjectsOfClass("Bridge").length > 0) {
         this.CallMethodEvery("CheckForBridge", 0.5F);
      }

      Vector var4 = this.GetEllipsoid();
      var4.Mul(0.8F);
      this.SetEllipsoid(var4.fX, var4.fY, var4.fZ);
   }

   void RestoreKbState() {
      this.m_fAccelerate = this.m_fInAccelerate;
      this.m_fBrake = this.m_fInBrake;
      this.m_fTurnLeft = this.m_fInTurnLeft;
      this.m_fTurnRight = this.m_fInTurnRight;
   }

   void SetEngineFreqMax() {
      if (this.m_iNofEngineSound != 0) {
         this.SetSoundFreq(this.m_iNofEngineSound, this.m_Params._MAX_SOUND_FREQ);
      }
   }

   void UpdateFallUnderGroundPhysics() {
      this.m_bFlying = true;
      this.SetEngineNeutralFreq();
      Vector var1 = Vector.newVector(Vector.Up);
      this.m_fCurrGravity = this.m_fCurrGravity + this.m_Params._GRAVITY_ACCEL * RacerModule.m_Mod.m_fFrameTime;
      var1.Mul(RacerModule.m_Mod.m_fFrameTime * this.m_fCurrGravity);
      this.m_vVehiclePosition.Add(var1);
      var1.Null();
      this.SetPosition(this.m_vVehiclePosition);
   }

   void AccelVAngle(boolean var1) {
      if (this.m_fCurrSpeed == 0.0F) {
         this.DecreaseVehicleVAngle();
      } else {
         float var2 = this.m_fCurrSpeed;
         if (var1) {
            this.m_fCurrVehicleVAngle = this.m_fCurrVehicleVAngle - RacerModule.m_Mod.m_fFrameTime * this.m_Params._VERT_DEV_ACCEL;
            if (var2 < 0.0F) {
               var2 = 0.0F;
            }

            var2 = -this.m_Params._MAX_VERT_DEV * (1.0F - var2 / this.m_fCurrMaxSpeed);
            if (this.m_fCurrVehicleVAngle < var2) {
               this.m_fCurrVehicleVAngle = var2;
               return;
            }
         } else {
            this.m_fCurrVehicleVAngle = this.m_fCurrVehicleVAngle + RacerModule.m_Mod.m_fFrameTime * this.m_Params._VERT_DEV_ACCEL;
            if (var2 > 0.0F) {
               var2 = 0.0F;
            }

            var2 = this.m_Params._MAX_VERT_DEV * (1.0F - var2 / this.m_Params._MIN_SPEED);
            if (this.m_fCurrVehicleVAngle > var2) {
               this.m_fCurrVehicleVAngle = var2;
            }
         }
      }
   }

   float GetSlideModifier() {
      float var1 = 1.0F;
      if (this.m_VehicleAI != null) {
         var1 *= this.m_VehicleAI.GetSlideModifier();
      }

      return var1;
   }

   void TurnLeftAnalog(float var1) {
      if (this.m_fInTurnLeft == 0.0F) {
         this.m_fLastLeftPressTime = RacerModule.m_Mod.m_fNowTime;
      }

      this.m_fInTurnRight = 0.0F;
      this.m_fInTurnLeft = var1;
      if (this.m_bGetInput) {
         this.m_fTurnRight = 0.0F;
         this.m_fTurnLeft = var1;
      }
   }

   void Accelerate() {
      if (this.m_fInAccelerate == 0.0F) {
         this.m_fLastAccelPressTime = RacerModule.m_Mod.m_fNowTime;
      }

      this.m_fInAccelerate = 1.0F;
      this.m_fInBrake = 0.0F;
      if (!this.m_bTurbo) {
         if (this.m_bGetInput) {
            this.m_fAccelerate = 1.0F;
            this.m_fBrake = 0.0F;
            if (!this.m_bIsFlyingOnFrame) {
               if (this.m_fCurrSpeed < 0.0F && RacerModule.m_Mod.m_fNowTime - this.m_fBrakePressTime > this.m_Params._TIME_BETWEEN_BRAKE_SOUNDS) {
                  this.m_bPlayBrakeSound = true;
                  this.m_fBrakePressTime = RacerModule.m_Mod.m_fNowTime;
               }
            }
         }
      }
   }

   void SystemAccelerate() {
      if (this.m_fInAccelerate == 0.0F) {
         this.m_fLastAccelPressTime = RacerModule.m_Mod.m_fNowTime;
      }

      this.m_fAccelerate = 1.0F;
      this.m_fBrake = 0.0F;
      if (this.m_fCurrSpeed < 0.0F && RacerModule.m_Mod.m_fNowTime - this.m_fBrakePressTime > this.m_Params._TIME_BETWEEN_BRAKE_SOUNDS) {
         this.m_bPlayBrakeSound = true;
         this.m_fBrakePressTime = RacerModule.m_Mod.m_fNowTime;
      }
   }

   void TurnRightAnalog(float var1) {
      if (this.m_fInTurnRight == 0.0F) {
         this.m_fLastRightPressTime = RacerModule.m_Mod.m_fNowTime;
      }

      this.m_fInTurnLeft = 0.0F;
      this.m_fInTurnRight = var1;
      if (this.m_bGetInput) {
         this.m_fTurnLeft = 0.0F;
         this.m_fTurnRight = var1;
      }
   }

   void DecreaseTurnSlide() {
      float var1 = this.m_vTurningSlide.Len() - this.m_Params._SLIDE_SUPRESS * RacerModule.m_Mod.m_fFrameTime;
      if (var1 < 0.0F) {
         var1 = 0.0F;
      }

      this.m_vTurningSlide.Normalize();
      this.m_vTurningSlide.Mul(var1);
   }

   void SetEngineNeutralFreq() {
      if (this.m_fInAccelerate > 0.0F) {
         this.m_fCurrRevolution = this.m_fCurrRevolution + 2.0F * RacerModule.m_Mod.m_fFrameTime;
         if (this.m_fCurrRevolution > this.m_fInAccelerate) {
            this.m_fCurrRevolution = this.m_fInAccelerate;
         }
      } else {
         this.m_fCurrRevolution = this.m_fCurrRevolution - 1.0F * RacerModule.m_Mod.m_fFrameTime;
         if (this.m_fCurrRevolution < 0.0F) {
            this.m_fCurrRevolution = 0.0F;
         }
      }

      if (this.m_iNofEngineSound != 0) {
         float var1 = this.m_fCurrRevolution;
         var1 *= this.m_Params._MAX_SOUND_FREQ - this.m_Params._MIN_SOUND_FREQ;
         var1 += this.m_Params._MIN_SOUND_FREQ;
         if (this.m_bTurbo) {
            var1 *= this.m_Params._TURBO_SOUND_MULTIPLIER;
         }

         this.SetSoundFreq(this.m_iNofEngineSound, var1);
      }
   }

   void RespawnForward() {
      this.ZeroRespawnParams();
      this.PlayRespawnSound();
      Vector var1 = RacerModule.m_Mod.m_Track.GetPointPos(this.m_iCurrTrackPoint);
      if (var1 == null) {
         GameObject.CrashLog("Nie ma takiego punktu! m_iCurrTrackPoint=" + this.m_iCurrTrackPoint + "\n");
      } else {
         Vector var2 = Vector.newVector(var1);
         this.m_vVehiclePosition.Set(var2);
         Vector var3 = new Vector(this.m_vVehiclePosition);
         var3.fY += 10000.0F;
         this.m_vVehiclePosition.fY = RacerModule.m_Mod.TerrainHeight(this.m_vVehiclePosition.fX, this.m_vVehiclePosition.fZ) - 12000.0F;
         this.TestTrack(var3, this.m_vVehiclePosition);
         this.m_vVehiclePosition.Set(this.m_collInfo.vEnd);
         this.m_vVehiclePosition.fY = this.m_vVehiclePosition.fY + this.m_Params._RESPAWN_HEIGHT;
         this.m_iCurrTrackPoint = RacerModule.m_Mod.m_Track.GetNextPointNum(this.m_iCurrTrackPoint);
         if (this.m_iCurrTrackPoint == -1) {
            return;
         }

         var2.Set(RacerModule.m_Mod.m_Track.GetPointPos(this.m_iCurrTrackPoint));
         var2.Sub(this.m_vVehiclePosition);
         var2.fY = 0.0F;
         var2.Normalize();
         this.m_vVehicleForward.Set(var2);
         this.m_vVehicleNormal.Set(Vector.Up);
         var2.Null();
      }

      this.m_iAIUpdatePointForwardNo = this.m_iCurrTrackPoint;

      while (!RacerModule.m_Mod.m_Track.IsPointVisibleByAI(this.m_iAIUpdatePointForwardNo)) {
         this.m_iAIUpdatePointForwardNo = RacerModule.m_Mod.m_Track.GetNextPointNum(this.m_iAIUpdatePointForwardNo);
      }

      this.m_fRespawnTime = RacerModule.m_Mod.m_fNowTime;
      if (this.m_iCurrTrackPoint == 1) {
         this.m_iCurrLoop++;
      }
   }

   void UpdateMovementParams() {
      if (this.m_bUpdateInput) {
         boolean var1 = false;
         if (this.m_fAccelerate > 0.0F) {
            var1 = true;
            if (this.m_fFlyingTime < this.m_Params._REAL_FLY_TIME_CONTROL && this.m_bEnableBack) {
               if (this.m_fCurrSpeed < 0.0F) {
                  this.m_fCurrSpeed = this.m_fCurrSpeed + this.m_Params._BRAKE_COEF * RacerModule.m_Mod.m_fFrameTime;
                  if (this.m_fCurrSpeed > 0.0F) {
                     this.m_fCurrSpeed = 0.0F;
                     this.m_bEnableBack = false;
                     this.CallMethodIn("EnableBack", this.m_Params._TIME_TO_GOING_BACK);
                  }
               } else if ((!this.m_bIsOnTrack || this.m_bInWaterPool) && this.m_fCurrSpeed > this.m_Params._OUT_OF_TRACK_SPEED) {
                  if (this.m_fCurrSpeed > this.m_Params._OUT_OF_TRACK_SPEED) {
                     this.m_fCurrSpeed = this.m_fCurrSpeed - this.m_Params._SLOW_COEF * RacerModule.m_Mod.m_fFrameTime;
                     if (this.m_fCurrSpeed < this.m_Params._OUT_OF_TRACK_SPEED) {
                        this.m_fCurrSpeed = this.m_Params._OUT_OF_TRACK_SPEED;
                     }
                  }
               } else {
                  float var3;
                  if (this.m_bTurbo) {
                     var3 = this.m_fAccelerate * this.m_fCurrMaxSpeed * this.m_Params._TURBO_MULTIPLIER;
                  } else {
                     var3 = this.m_fAccelerate * this.m_fCurrMaxSpeed;
                  }

                  var3 += this.m_fCurrSpeedModifier;
                  if (this.m_fCurrSpeed < var3) {
                     this.m_fCurrSpeed = this.m_fCurrSpeed
                        + this.GetCurrAccel(this.m_fCurrSpeed / var3) * this.m_fSlopeAccelCoef * RacerModule.m_Mod.m_fFrameTime;
                     if (this.m_fCurrSpeed > var3) {
                        this.m_fCurrSpeed = var3;
                        this.m_fCurrSpeedModifier *= -1.0F;
                     }
                  } else {
                     this.m_fCurrSpeed = this.m_fCurrSpeed - this.m_fDefaultMaxAccel * RacerModule.m_Mod.m_fFrameTime;
                     if (this.m_fCurrSpeed < var3) {
                        this.m_fCurrSpeed = var3;
                        this.m_fCurrSpeedModifier *= -1.0F;
                     }
                  }
               }
            } else if (this.m_fCurrSpeed < 0.0F) {
               this.m_fCurrSpeed = this.m_fCurrSpeed + this.m_Params._FLY_SLOW_COEF * RacerModule.m_Mod.m_fFrameTime;
               if (this.m_fCurrSpeed > 0.0F) {
                  this.m_fCurrSpeed = 0.0F;
               }
            } else {
               this.m_fCurrSpeed = this.m_fCurrSpeed - this.m_Params._FLY_SLOW_COEF * RacerModule.m_Mod.m_fFrameTime;
               if (this.m_fCurrSpeed < 0.0F) {
                  this.m_fCurrSpeed = 0.0F;
               }
            }
         }

         if (this.m_fBrake > 0.0F && this.m_fFlyingTime < this.m_Params._REAL_FLY_TIME_CONTROL && this.m_bEnableBack) {
            var1 = true;
            if (this.m_fCurrSpeed > 0.0F) {
               this.m_fCurrSpeed = this.m_fCurrSpeed - this.m_Params._BRAKE_COEF * RacerModule.m_Mod.m_fFrameTime;
               if (this.m_fCurrSpeed < 0.0F) {
                  this.m_fCurrSpeed = 0.0F;
                  this.m_bEnableBack = false;
                  this.CallMethodIn("EnableBack", this.m_Params._TIME_TO_GOING_BACK);
               }
            } else {
               this.m_Character.LookAround();
               if (this.m_fCurrSpeed > this.m_fBrake * this.m_Params._MIN_SPEED) {
                  float var2 = this.m_fBrake * this.m_Params._MIN_SPEED;
                  this.m_fCurrSpeed = this.m_fCurrSpeed - this.GetDefaultMaxAcceleration() * RacerModule.m_Mod.m_fFrameTime;
                  if (this.m_fCurrSpeed < var2) {
                     this.m_fCurrSpeed = var2;
                  }
               }
            }
         }

         if (!var1) {
            if (this.m_fCurrSpeed < 0.0F) {
               this.m_fCurrSpeed = this.m_fCurrSpeed + this.m_Params._SLOW_COEF * RacerModule.m_Mod.m_fFrameTime;
               if (this.m_fCurrSpeed > 0.0F) {
                  this.m_fCurrSpeed = 0.0F;
               }
            } else {
               this.m_fCurrSpeed = this.m_fCurrSpeed - this.m_Params._SLOW_COEF * RacerModule.m_Mod.m_fFrameTime;
               if (this.m_fCurrSpeed < 0.0F) {
                  this.m_fCurrSpeed = 0.0F;
               }
            }
         }

         if (this.m_fTurnLeft > 0.0F) {
            float var4 = this.m_fTurnLeft * this.GetDefaultMaxTurnAngle();
            this.m_fCurrDirAngle = this.m_fCurrDirAngle - this.m_Params._TURNING_SPEED * RacerModule.m_Mod.m_fFrameTime;
            if (this.m_fCurrDirAngle < -var4) {
               this.m_fCurrDirAngle = -var4;
            }
         }

         if (this.m_fTurnRight > 0.0F) {
            float var5 = this.m_fTurnRight * this.GetDefaultMaxTurnAngle();
            this.m_fCurrDirAngle = this.m_fCurrDirAngle + this.m_Params._TURNING_SPEED * RacerModule.m_Mod.m_fFrameTime;
            if (this.m_fCurrDirAngle > var5) {
               this.m_fCurrDirAngle = var5;
            }
         }
      }
   }

   void ZeroParams() {
      this.m_fFlyingTime = 0.0F;
      this.m_fLandingTime = 0.0F;
      this.m_bEnableBack = true;
      this.m_vSlideVector.Set(Vector.Zero);
      this.m_fCurrDirAngle = 0.0F;
      this.m_bIsFlyingOnFrame = false;
      this.m_fWheelRotationSpeed = 0.0F;
      this.m_bLanding = false;
      this.m_vTurningSlide.Set(Vector.Zero);
      this.m_vCollisionVector.Set(Vector.Zero);
      this.m_bGotCollision = false;
   }

   void ZeroVehicleParams() {
      this.m_fCurrGravity = 0.0F;
      this.m_fCurrSpeed = 0.0F;
      this.m_vSlideVector.Set(Vector.Zero);
      this.m_vTurningSlide.Set(Vector.Zero);
   }

   float GetDefaultMaxAcceleration() {
      return this.m_fDefaultMaxAccel;
   }

   void UpdateMaxSpeedAccelEqualAll() {
      float fCheat = ModOptions.GetSpeedMul(this); // [mod] triche « vitesse » du menu Mods (humains seulement)
      this.m_fDefaultMaxSpeed = this.m_Params._MAX_SPEED * fCheat;
      this.m_fDefaultMaxTurnAngle = this.m_Params._MAX_TURN_ANGLE * VehicleParams._ANIMALS_TURN_COEFS[this.m_iAnimalID];
      this.m_fDefaultMaxAccel = this.m_Params._ACCELERATION_COEF * VehicleParams._ANIMALS_ACCEL_COEFS[this.m_iAnimalID] * fCheat;
      this.m_fDefaultMinAccel = this.m_fDefaultMaxAccel / 3.0F;
      this.m_fCollCoef = this.m_fDefaultMaxSpeed / 5.0F;
      this.m_fCurrMaxSpeed = this.m_fDefaultMaxSpeed;
      this.m_fCurrSpeedModifier = this.m_fCurrMaxSpeed * 0.03F;
   }

   void SetVehicleAngles() {
      if (!this.m_bFlying && !this.m_bIsFlyingOnFrame) {
         if (this.m_fAccelerate > 0.0F) {
            this.AccelVAngle(true);
         }

         if (this.m_fBrake > 0.0F) {
            this.AccelVAngle(false);
         }

         if (this.m_fAccelerate == 0.0F && this.m_fBrake == 0.0F) {
            this.DecreaseVehicleVAngle();
         }
      }

      if (!this.m_bFlying) {
         if (this.m_fTurnLeft > 0.0F) {
            this.TurnRightHAngle(false);
         } else if (this.m_fTurnRight > 0.0F) {
            this.TurnRightHAngle(true);
         } else {
            this.DecreaseVehicleHAngle();
         }
      } else {
         this.DecreaseVehicleHAngle();
      }
   }

   void InitTracer() {
      if (RacerModule.m_Mod.m_Track.m_TrackPoints.size() != 0) {
         this.m_iCurrTrackPoint = 0;
         if (RacerModule.m_Mod.m_Track.m_TrackPoints.size() < 6) {
            GameObject.LogLine("Trasa ma prawdopodobnie za malo punktow !!!");
         } else {
            this.UpdateCurrentGate();
            this.CallMethodEvery("UpdateCurrentGate", _GATES_CHECK_TIME);
         }
      }
   }

   Vector CalculateNewNormal() {
      Vector var1 = Vector.newVector(this.m_vVehicleNormal);
      var1.Mul(this.m_Params._SUSPENSION_HEIGHT);
      int var2 = 0;

      do {
         this.vWheelTmp[var2].Set(this.GetElementPos(this.m_Wheels[var2]));
         this.vWheelTmp[var2].Add(var1);
      } while (++var2 < 4);

      var1.Negate();
      var1.Mul(2.0F);
      Vector var5 = null;
      int var3 = 0;

      do {
         var5 = new Vector(var1);
         var5.Add(this.vWheelTmp[var3]);
         this.TestTrack(this.vWheelTmp[var3], var5);
         if (this.m_collInfo.coll != null) {
            this.vWheelTmp[var3].Set(this.m_collInfo.coll.vNormal);
         } else {
            this.vWheelTmp[var3].Set(Vector.Zero);
         }
      } while (++var3 < 4);

      var1.Null();
      Vector var8 = new Vector(this.vWheelTmp[3]);
      int var4 = 0;

      do {
         var8.Add(this.vWheelTmp[var4]);
      } while (++var4 < 3);

      var8.Normalize();
      if (var8.Len2() == 0.0F) {
         Vector var9 = new Vector(this.m_vVehiclePosition);
         var9.fY = var9.fY + this.m_Params._SUSPENSION_HEIGHT;
         var5 = new Vector(var9);
         var5.fY = var5.fY - 2.0F * this.m_Params._SUSPENSION_HEIGHT;
         this.TestTrack(var9, var5);
         if (this.m_collInfo.coll != null) {
            var8.Set(this.m_collInfo.coll.vNormal);
         } else {
            var8.Set(Vector.Up);
         }
      }

      return var8;
   }

   void AddSlideFromTurning() {
      Vector var1 = Vector.newVector(this.m_vVehicleRight);
      float var2 = this.m_fCurrSpeed;
      if (this.m_fCurrSpeed >= 0.0F) {
         var2 /= this.m_Params._MAX_SPEED;
      } else {
         var2 /= -this.m_Params._MAX_SPEED;
      }

      var1.fY = 0.0F;
      var1.Normalize();
      var1.Mul(
         RacerModule.m_Mod.m_fFrameTime * this.m_Params._SLIDE_COEF * this.m_fCurrDirAngle / this.GetDefaultMaxTurnAngle() * this.GetSlideModifier() * var2
      );
      this.m_vTurningSlide.Add(var1);
      var1.Null();
   }

   void MoveVehicle() {
      if (this.m_iCurrPhysics == 3) {
         this.m_vMoveDir.Set(this.m_vSlidePhysicsDir);
      } else {
         this.m_vMoveDir.Set(this.m_vVehicleForward);
      }

      this.m_vMoveDir.Mul(RacerModule.m_Mod.m_fFrameTime * this.m_fCurrSpeed);
      if (this.m_bTurbo) {
         this.m_vMoveDir.Mul(this.m_Params._TURBO_MULTIPLIER);
      }

      Vector var1 = Vector.newVector(this.m_vSlideVector);
      var1.Mul(RacerModule.m_Mod.m_fFrameTime);
      this.m_vMoveDir.Add(var1);
      if (!this.m_bTireSpikes) {
         // [mod] m_vTurningSlide (dérapage dans les virages) était ajouté tel quel à chaque image : un déplacement
         // par image et non par seconde. À 144 images/s la voiture glissait 5 fois plus qu'à 30, à 300 images/s
         // 10 fois plus. On le ramène à ce qu'il donnait à _SLIDE_REF_FPS images/s, quel que soit le framerate.
         var1.Set(this.m_vTurningSlide);
         var1.Mul(RacerModule.m_Mod.m_fFrameTime * _SLIDE_REF_FPS);
         this.m_vMoveDir.Add(var1);
      }

      var1.Set(this.m_vCollisionVector);
      var1.Mul(RacerModule.m_Mod.m_fFrameTime);
      this.m_vMoveDir.Add(var1);
      var1.Null();
      Vector var2 = new Vector(this.m_vVehicleNormal);
      var2.Mul(this.m_Params._SUSPENSION_HEIGHT);
      var2.Add(this.m_vVehiclePosition);
      Vector var3 = new Vector(var2);
      var3.Add(this.m_vMoveDir);
      if (!var2.IsEqual(var3)) {
         this.TestTrack(var2, var3);
         var3.Set(this.m_collInfo.vEnd);
      } else {
         this.m_collInfo.coll = null;
      }

      if (this.m_collInfo.coll != null) {
         if (Vector.Up.Dot(this.m_collInfo.coll.vNormal) < this.m_Params._MAX_SLOPE_ANGLE) {
            this.SetBounceVector(this.m_collInfo.coll.vNormal);
            if (this.m_VehicleAI != null) {
               this.m_VehicleAI.ModOnObstacle(this.m_collInfo.coll.vNormal); // [mod] mur ou pente raide
            }
         } else {
            var3.fY += 0.01F;
         }

         this.m_fCurrGravity = 0.0F;
      } else {
         Vector var4 = Vector.newVector();
         if (Math.abs(this.m_fCurrSpeed) < 10.0F) {
            var4.Set(this.m_vVehicleNormal);
         } else {
            var4.Set(Vector.Up);
         }

         this.m_fCurrGravity = this.m_fCurrGravity + this.m_Params._GRAVITY_ACCEL * RacerModule.m_Mod.m_fFrameTime;
         var4.Mul(RacerModule.m_Mod.m_fFrameTime * this.m_fCurrGravity);
         var2.Set(var3);
         Vector var5 = Vector.newVector();
         var5.Set(this.m_vVehicleNormal);
         var5.Negate();
         var5.Mul(this.m_Params._SUSPENSION_HEIGHT);
         var3.Add(var5);
         var5.Null();
         var3.Add(var4);
         var4.Null();
         this.TestTrack(var2, var3);
         var3.Set(this.m_collInfo.vEnd);
         if (this.m_collInfo.coll != null) {
            if (Vector.Up.Dot(this.m_collInfo.coll.vNormal) < this.m_Params._MAX_SLOPE_ANGLE) {
               this.SetBounceVector(this.m_collInfo.coll.vNormal);
            }

            var3.fY += 0.01F;
            this.m_fCurrGravity = 0.0F;
         } else {
            this.m_bIsFlyingOnFrame = true;
         }
      }

      this.m_vMoveDir.Set(var3);
      this.m_vMoveDir.Sub(this.m_vVehiclePosition);
      if (this.m_vMoveDir.Len2() > 0.0F) {
         this.m_fBeforeMoveSpeed = this.m_vMoveDir.Len();
         this.MoveByEllipsoid(this.m_vMoveDir);
         this.m_vMoveDir.Mul(0.99F);
         this.m_vVehiclePosition.Add(this.m_vMoveDir);
      }

      if (this.m_bMustRespawn) {
         this.Respawn();
         this.m_bMustRespawn = false;
      }
   }

   void SlowTurningVehicle() {
      if (this.m_fCurrSpeed > 0.0F) {
         this.m_fCurrSpeed = this.m_fCurrSpeed - this.m_Params._TURNING_SLOW_COEF * RacerModule.m_Mod.m_fFrameTime;
         if (this.m_fCurrSpeed < 0.0F) {
            this.m_fCurrSpeed = 0.0F;
            return;
         }
      } else {
         this.m_fCurrSpeed = this.m_fCurrSpeed + this.m_Params._TURNING_SLOW_COEF * RacerModule.m_Mod.m_fFrameTime;
         if (this.m_fCurrSpeed > 0.0F) {
            this.m_fCurrSpeed = 0.0F;
         }
      }
   }

   boolean CollectBonus() {
      // [mod] turbo, pointes et protection en cours ne gardent plus la case (ModParkActiveBonus)
      if (this.m_CurrentBonus != null && !this.ModParkActiveBonus()) {
         return false;
      }

      // [mod] triche « bonus imposé » du menu Mods (humains seulement), sinon tirage d'origine
      LevelBonusTypes bonuses = RacerModule.m_Mod.m_RaceParams.m_AvailableBonuses;
      if (ModOptions.m_nForcedBonus != ModOptions._BONUS_RANDOM && ModOptions.IsCheater(this)) {
         this.m_CurrentBonus = bonuses.CreateBonus(ModOptions.m_nForcedBonus, this);
      } else {
         this.m_CurrentBonus = bonuses.GetRandomBonus(this);
      }

      if (this.m_CurrentBonus == null) {
         return false; // [mod] circuit sans bonus : rien à donner (l'original plantait ici)
      }

      this.m_fCollectBonusTime = RacerModule.m_Mod.m_fNowTime;
      this.m_CurrentBonus.m_bIsVisibleOnHud = true;
      RacerModule.m_CurrBonus = this.m_CurrentBonus;
      this.ChangeBonusSize(true);
      switch (this.m_CurrentBonus.m_UseType) {
         case 0:
            this.ExecuteFX(this, "FXCollectBomb", Vector.Zero);
            break;
         case 1:
            this.ExecuteFX(this, "FXCollectCatapult", Vector.Zero);
            break;
         case 2:
            this.ExecuteFX(this, "FXCollectTurbo", Vector.Zero);
            break;
         case 3:
            this.ExecuteFX(this, "FXCollectProtection", Vector.Zero);
            break;
         case 4:
            this.ExecuteFX(this, "FXCollectCorkPistol", Vector.Zero);
            break;
         case 5:
            this.ExecuteFX(this, "FXCollectOilCan", Vector.Zero);
            break;
         case 6:
            this.ExecuteFX(this, "FXCollectTireSpikes", Vector.Zero);
            break;
         case 7:
            this.ExecuteFX(this, "FXCollectFunnyGas", Vector.Zero);
            break;
         case 8:
            this.ExecuteFX(this, "FXCollectWaspBag", Vector.Zero);
         case 9:
         default:
            break;
         case 10:
            this.ExecuteFX(this, "FXCollectBanana", Vector.Zero);
            break;
         case 11:
            this.ExecuteFX(this, "FXCollectWaterBottle", Vector.Zero);
            break;
         case 12:
            this.ExecuteFX(this, "FXCollectAnvil", Vector.Zero);
            break;
         case 13:
            this.ExecuteFX(this, "FXCollectTomatoLauncher", Vector.Zero);
            break;
         case 14:
            this.ExecuteFX(this, "FXCollectWaterMelon", Vector.Zero);
            break;
         case 15:
            this.ExecuteFX(this, "FXCollectFreezePistol", Vector.Zero);
            break;
         case 16:
            this.ExecuteFX(this, "FXCollectWaterPistol", Vector.Zero);
            break;
         case 17:
            this.ExecuteFX(this, "FXCollectCoconut", Vector.Zero);
            break;
         case 18:
            this.ExecuteFX(this, "FXCollectSnowBall", Vector.Zero);
            break;
         case 19:
            this.ExecuteFX(this, "FXCollectCookie", Vector.Zero);
            break;
         case 20:
            this.ExecuteFX(this, "FXCollectRocket", Vector.Zero);
            break;
         case 21:
            this.ExecuteFX(this, "FXCollectFallingRock", Vector.Zero);
            break;
         case 22:
            this.ExecuteFX(this, "FXCollectUFO", Vector.Zero);
      }

      if (this.m_CurrentBonus.m_UseType == 0
         || this.m_CurrentBonus.m_UseType == 14
         || this.m_CurrentBonus.m_UseType == 4
         || this.m_CurrentBonus.m_UseType == 13
         || this.m_CurrentBonus.m_UseType == 8
         || this.m_CurrentBonus.m_UseType == 15
         || this.m_CurrentBonus.m_UseType == 16
         || this.m_CurrentBonus.m_UseType == 17
         || this.m_CurrentBonus.m_UseType == 20
         || this.m_CurrentBonus.m_UseType == 18
         || this.m_CurrentBonus.m_UseType == 19
         || this.m_CurrentBonus.m_UseType == 21
         || this.m_CurrentBonus.m_UseType == 22) {
         this.StartAim();
      }

      if (this.m_CurrentBonus.m_UseType == 0
         || this.m_CurrentBonus.m_UseType == 14
         || this.m_CurrentBonus.m_UseType == 17
         || this.m_CurrentBonus.m_UseType == 18
         || this.m_CurrentBonus.m_UseType == 20
         || this.m_CurrentBonus.m_UseType == 19) {
         ThrowItem var3 = (ThrowItem)this.m_CurrentBonus;
         var3.AttachToRHand();
         var3.SetPosition(Vector.Zero);
         this.m_Character.TakeRightItem(true);
         return true;
      } else if (this.m_CurrentBonus.m_UseType == 1
         || this.m_CurrentBonus.m_UseType == 5
         || this.m_CurrentBonus.m_UseType == 11
         || this.m_CurrentBonus.m_UseType == 10
         || this.m_CurrentBonus.m_UseType == 12) {
         ThrowItem var2 = (ThrowItem)this.m_CurrentBonus;
         var2.AttachToRHand();
         var2.SetPosition(Vector.Zero);
         this.m_Character.TakeRightItem(false);
         return true;
      } else if (this.m_CurrentBonus.m_UseType == 7
         || this.m_CurrentBonus.m_UseType == 4
         || this.m_CurrentBonus.m_UseType == 8
         || this.m_CurrentBonus.m_UseType == 13
         || this.m_CurrentBonus.m_UseType == 15
         || this.m_CurrentBonus.m_UseType == 16
         || this.m_CurrentBonus.m_UseType == 21
         || this.m_CurrentBonus.m_UseType == 22) {
         ThrowItem var1 = (ThrowItem)this.m_CurrentBonus;
         var1.AttachToLHand();
         var1.SetPosition(Vector.Zero);
         this.m_Character.TakeLeftItem();
         return true;
      } else if (this.m_CurrentBonus.m_UseType == 2) {
         this.m_CurrentBonus.m_bIsVisibleOnHud = true;
         return true;
      } else if (this.m_CurrentBonus.m_UseType == 6) {
         this.m_CurrentBonus.m_bIsVisibleOnHud = true;
         return true;
      } else if (this.m_CurrentBonus.m_UseType == 3) {
         this.m_CurrentBonus.AttachToCenter();
         this.m_CurrentBonus.SetPosition(Vector.Zero);
         return true;
      } else {
         return false;
      }
   }

   void UseBonus() {
      if (this.m_CurrentBonus == null) {
         this.PlayHornSound();
      } else if (!this.m_CurrentBonus.m_bWasUsed) {
         if (this.m_bCanUseBonus) {
            this.m_CurrentBonus.m_bWasUsed = true;
            if (this.m_CurrentBonus.m_UseType == 1
               || this.m_CurrentBonus.m_UseType == 5
               || this.m_CurrentBonus.m_UseType == 11
               || this.m_CurrentBonus.m_UseType == 10
               || this.m_CurrentBonus.m_UseType == 12) {
               this.m_CurrentBonus.Throw(this, false);
               this.m_Character.ThrowItem(false);
               this.PlayThrowSound();
            }

            if (this.m_CurrentBonus.m_UseType == 17
               || this.m_CurrentBonus.m_UseType == 18
               || this.m_CurrentBonus.m_UseType == 20
               || this.m_CurrentBonus.m_UseType == 19) {
               TrackingDevice var1 = (TrackingDevice)this.m_CurrentBonus;
               this.m_Character.ThrowItem(true);
               float var2 = 5000.0F;
               float var3 = 5.0F;
               switch (this.m_CurrentBonus.m_UseType) {
                  case 17:
                     var2 = 5500.0F;
                     var3 = 5.0F;
                     break;
                  case 18:
                     var2 = 5500.0F;
                     var3 = 5.0F;
                     break;
                  case 19:
                     var2 = 5500.0F;
                     var3 = 5.0F;
                     break;
                  case 20:
                     var2 = 5500.0F;
                     var3 = 5.0F;
                     ((Rocket)this.m_CurrentBonus).AddSmokeFX();
               }

               var1.FakeThrow(RacerModule.m_Mod, this, this.m_LockedObject, this.m_fCurrSpeed, var2, var3);
               // [mod] le Missile n'a pas de cible verrouillée chez le joueur (il la choisit au départ) : son de lancer quand même
               if (this.m_LockedObject != null || var1 instanceof Rocket && ((Rocket)var1).m_bModMissile) {
                  this.PlayThrowSound();
               }
            }

            if (this.m_CurrentBonus.m_UseType == 7) {
               this.m_Character.UseLeftHandItem();
               this.ExecuteFX(this.m_CurrentBonus, "FXSpray", Vector.Zero);
               FunnyGasBox var4 = UseItem.CreateFunnyGasBox(this);
               var4.FireFX();
               ModNet.OnTrapLanded(var4, UseItem._USE_FUNNYGAS); // [mod] en ligne : nuage recopié chez les autres
               this.DisableCallMethod("ThrowGunAway");
               this.CallMethodIn("ThrowGunAway", 0.5F);
               this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_SPRAY]);
               this.PlayThrowSound();
            }

            if (this.m_CurrentBonus.m_UseType == 8) {
               this.m_Character.UseLeftHandItem();
               WaspBagBox var5 = UseItem.CreateWaspBagBox(this);
               var5.AttachToParentAfterHit(2.0F);
               var5.FireFX();
               var5.Init(RacerModule.m_Mod, this, this.m_LockedObject, this.m_fCurrSpeed, 5500.0F, 10.0F);
               this.DisableCallMethod("ThrowGunAway");
               this.CallMethodIn("ThrowGunAway", 1.0F);
               if (this.m_LockedObject != null) {
                  this.PlayThrowSound();
               }
            }

            if (this.m_CurrentBonus.m_UseType == 4) {
               this.m_Character.UseLeftHandItem();
               this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_CORK_GUN]);
               CorkPistolBullet var6 = UseItem.CreateCorkPistolBullet();
               this.ExecuteFX(this, "FXCorkPistol", Vector.Zero);
               var6.InitPosition(this.m_CurrentBonus);
               var6.Init(RacerModule.m_Mod, this, this.m_LockedObject, this.m_fCurrSpeed, 5500.0F, 3.0F);
               this.DisableCallMethod("ThrowGunAway");
               this.CallMethodIn("ThrowGunAway", 1.0F);
               if (this.m_LockedObject != null) {
                  this.PlayThrowSound();
               }
            }

            if (this.m_CurrentBonus.m_UseType == 13) {
               this.m_Character.UseLeftHandItem();
               Vector var7 = this.m_CurrentBonus.GetPosition();
               this.ExecuteFX("FXTomatoLauncher", var7);
               Tomato var11 = UseItem.CreateTomato();
               var11.InitPosition(this.m_CurrentBonus);
               var11.Init(RacerModule.m_Mod, this, this.m_LockedObject, this.m_fCurrSpeed, 5500.0F, 5.0F);
               this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_CORK_GUN]);
               this.DisableCallMethod("ThrowGunAway");
               this.CallMethodIn("ThrowGunAway", 1.0F);
               if (this.m_LockedObject != null) {
                  this.PlayThrowSound();
               }
            }

            if (this.m_CurrentBonus.m_UseType == 15) {
               this.m_Character.UseLeftHandItem();
               Vector var8 = this.m_CurrentBonus.GetPosition();
               Vector var12 = this.m_CurrentBonus.GetForwardVector();
               var12.Normalize();
               var12.Mul(30.0F);
               var8.Add(var12);
               Vector var14 = Vector.newVector();
               if (this.m_LockedObject != null) {
                  var14.Set(this.m_LockedObject.GetPosition());
                  var14.fY += 80.0F;
                  var14.Sub(this.m_CurrentBonus.GetPosition());
                  var14.Normalize();
                  this.ExecuteFX(this.m_LockedObject, "FXFreezePistolSplash", Vector.Zero);
               } else {
                  var14.Set(this.m_CurrentBonus.GetRightVector());
                  var14.Negate();
               }

               this.m_CurrentBonus.FireParticles("FreezePistolShoot", var14);
               var14.Null();
               if (this.m_LockedObject != null && !this.m_LockedObject.m_bProtection) {
                  this.m_LockedObject.FreezeVehicle();
               }

               this.DisableCallMethod("ThrowGunAway");
               this.CallMethodIn("ThrowGunAway", 1.0F);
               this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_FREEZE_PISTOL]);
               if (this.m_LockedObject != null) {
                  this.PlayThrowSound();
               }
            }

            if (this.m_CurrentBonus.m_UseType == 16) {
               this.m_Character.UseLeftHandItem();
               Vector var9 = this.m_CurrentBonus.GetPosition();
               Vector var13 = this.m_CurrentBonus.GetForwardVector();
               var13.Normalize();
               var13.Mul(30.0F);
               var9.Add(var13);
               Vector var15 = Vector.newVector();
               if (this.m_LockedObject != null) {
                  var15.Set(this.m_LockedObject.GetPosition());
                  var15.fY += 80.0F;
                  var15.Sub(this.m_CurrentBonus.GetPosition());
                  var15.Normalize();
                  this.ExecuteFX(this.m_LockedObject, "FXWaterPistolSplash", Vector.Zero);
               } else {
                  var15.Set(this.m_CurrentBonus.GetRightVector());
                  var15.Negate();
               }

               this.m_CurrentBonus.FireParticles("WaterPistolShoot", var15);
               var15.Null();
               this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_WATER_GUN]);
               // [mod] la protection compte aussi contre le pistolet à eau (le pistolet à glace la testait déjà)
               if (this.m_LockedObject != null && !this.m_LockedObject.m_bProtection) {
                  this.m_LockedObject.WaterVehicle();
               }

               this.DisableCallMethod("ThrowGunAway");
               this.CallMethodIn("ThrowGunAway", 1.0F);
               if (this.m_LockedObject != null) {
                  this.PlayThrowSound();
               }
            }

            if (this.m_CurrentBonus.m_UseType == 21) {
               if (this.m_LockedObject != null) {
               }

               if (this.m_LockedObject != null) {
                  this.m_LockedObject.FallingRock();
               }

               this.DisableCallMethod("ThrowGunAway");
               this.CallMethodIn("ThrowGunAway", 1.0F);
               this.PlayThrowSound();
            }

            if (this.m_CurrentBonus.m_UseType == 22) {
               this.m_Character.UseLeftHandItem();
               this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_UFO_BUTTON]);
               this.DisableCallMethod("ThrowGunAway");
               this.CallMethodIn("ThrowGunAway", 1.0F);
               this.PlayThrowSound();
               UFOObject var10 = UseItem.CreateUFOObject(this);
               var10.Init(this.m_LockedObject);
               // [mod] en ligne : l'enlèvement se joue chez le joueur visé, qui lance son propre OVNI
               if (this.m_LockedObject != null && this.m_LockedObject.m_bModGhost) {
                  ModNet.HitGhost(this.m_LockedObject, ModNet._HIT_UFO);
               }
            }

            if (this.m_CurrentBonus.m_UseType == 0 || this.m_CurrentBonus.m_UseType == 14) {
               this.m_Character.ThrowItem(true);
               ((ThrowItem)this.m_CurrentBonus).m_vParentVehicle = this;
               if (this.m_vAimVector.Len2() > 0.0F) {
                  this.m_CurrentBonus.Throw(this.m_fCurrSpeed, this.m_vAimVector, true);
                  this.PlayThrowSound();
               } else {
                  this.m_CurrentBonus.Throw(this, true);
               }
            }

            if (this.m_CurrentBonus.m_UseType == 2) {
               this.m_CurrentBonus.EnableOnHud();
               this.SystemAccelerate();
               this.m_bTurbo = true;
               this.DisableCallMethod("DisableTurbo");
               this.CallMethodIn("DisableTurbo", this.m_Params._DISABLE_TURBO_TIME);
            }

            if (this.m_CurrentBonus.m_UseType == 6) {
               this.m_CurrentBonus.EnableOnHud();
               this.m_bTireSpikes = true;
               this.DisableCallMethod("DisableTireSpikes");
               this.CallMethodIn("DisableTireSpikes", this.m_Params._DISABLE_TIRESPIKES_TIME);
            }

            if (this.m_CurrentBonus.m_UseType == 3) {
               this.m_CurrentBonus.EnableOnHud();
               this.m_bProtection = true;
               this.DetachProtectionSound(); // [mod] une 2e protection pendant la 1re ne double plus le son
               this.AttachProtectionSound("Data\\Sounds\\protect.wav");
               this.DisableCallMethod("DisableProtection");
               this.ExecuteFX(this, "FXProtection", Vector.Zero);
               this.CallMethodIn("DisableProtection", this.m_Params._DISABLE_PROTECTION_TIME);
            }
         }
      }
   }

   float GetCurrDistance() {
      this.UpdateCurrDistance();
      return this.m_fCoveredDistance;
   }

   void SetEngineFreqTurbo() {
      this.m_fCurrRevolution = this.m_fCurrRevolution + 1.0F * RacerModule.m_Mod.m_fFrameTime;
      if (this.m_fCurrRevolution > 1.0F) {
         this.m_fCurrRevolution = 1.0F;
      }

      if (this.m_iNofEngineSound != 0) {
         this.SetSoundFreq(this.m_iNofEngineSound, this.m_fCurrRevolution * this.m_Params._MAX_SOUND_FREQ * this.m_Params._TURBO_SOUND_MULTIPLIER);
      }
   }

   public void UpdateCurrentGate() {
      boolean var1 = false;
      int var2 = this.m_iCurrTrackPoint;
      Vector var3 = this.GetPosition();
      if (!RacerModule.m_Mod.m_Track.BetweenGates(var3, this.m_iCurrTrackPoint)) {
         int var4 = RacerModule.m_Mod.m_Track.GetNextPointNum(this.m_iCurrTrackPoint);
         int var5 = RacerModule.m_Mod.m_Track.GetPrevPointNum(this.m_iCurrTrackPoint);

         for (int var6 = 0; !var1 && var6 < 2; var6++) {
            for (int var7 = 0; !var1 && var7 < _GATES_F_SEARCH_RANGE; var7++) {
               if (RacerModule.m_Mod.m_Track.BetweenGates(var3, var4)) {
                  this.m_iCurrTrackPoint = var4;
                  var1 = true;
               } else {
                  var4 = RacerModule.m_Mod.m_Track.GetNextPointNum(var4);
               }
            }

            for (int var10 = 0; !var1 && var10 < _GATES_B_SEARCH_RANGE; var10++) {
               if (RacerModule.m_Mod.m_Track.BetweenGates(var3, var5)) {
                  this.m_iCurrTrackPoint = var5;
                  var1 = true;
               } else {
                  var5 = RacerModule.m_Mod.m_Track.GetPrevPointNum(var5);
               }
            }
         }

         if (!var1) {
            this.m_iCurrTrackPoint = -1;
            this.m_bIsOnTrack = false;
            return;
         }

         int var9 = RacerModule.m_Mod.m_Track.m_TrackPoints.size();
         if ((var2 > var9 - 5 || var2 == 0) && this.m_iCurrTrackPoint > 0 && this.m_iCurrTrackPoint < 5) {
            this.m_iCurrLoop++;
            if (this.m_iCurrLoop == RacerModule.m_Mod.m_RaceParams.m_iNofLoops && !this.m_bRaceFinished) {
               // [mod] temps exact (GetMissionTime n'avance que par pas de 0,1 s : deux arrivées serrées avaient le
               // même temps) et rang d'arrivée réel, qui fait foi pour la place, le podium et les records
               this.m_fFinishClockRaceTime = RacerModule.m_Mod.GetClock().GetMissionExactTime();
               this.SetFinishRank();
               this.m_fFinishTimeForCam = RacerModule.m_Mod.m_fNowTime;
               this.m_bRaceFinished = true;
               this.m_iStopCause = 5;
               this.StopByObstacle();
               this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_RACE_FINISHED]);
               // [mod] en ligne : classement sur les temps de tous, podium quand les autres sont arrivés (ModNet)
               if (ModNet.IsRaceOnline()) {
                  if (this.m_VehicleAI != null) {
                     ModNet.OnAIFinished(this); // IA de l'hôte
                  } else {
                     ModNet.OnLocalFinished(this);
                  }

                  return;
               }

               if (!RacerModule.m_bIsDemo) {
                  Vehicle var11 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_PlayersTab.elementAt(0);
                  if (RacerGame.GetCoreLogic().IsMultiplayer()) {
                     Vehicle var8 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_PlayersTab.elementAt(1);
                     if (var11.m_bRaceFinished && var8.m_bRaceFinished) {
                        this.FinishRace();
                     }

                     return;
                  }

                  if (var11.m_bRaceFinished) {
                     this.FinishRace();
                  }

                  return;
               }

               this.FinishRace();
               return;
            }
         }

         if (var2 > 0 && var2 < 5 && (this.m_iCurrTrackPoint > var9 - 5 || this.m_iCurrTrackPoint == 0)) {
            this.m_iCurrLoop += -1;
         }
      }

      if (RacerModule.m_Mod.m_Track.m_iAddLayers > 0 && this.MustRespawn()) {
         this.Respawn();
      } else {
         if (RacerModule.m_Mod.m_Track.m_iAddLayers == 1) {
            this.m_bIsOnTrack = RacerModule.m_Mod.m_Track.InsideGateAI(this.GetPosition(), this.m_iCurrTrackPoint);
         }

         if (RacerModule.m_Mod.m_Track.m_iAddLayers == 2) {
            this.m_bIsOnTrack = RacerModule.m_Mod.m_Track.InsideGateTrack(this.GetPosition(), this.m_iCurrTrackPoint);
         }

         this.PlayRoadSound();
         this.CheckForWrongWay();
      }
   }

   void SetBounceVector(Vector var1) {
      Vector var2 = Vector.newVector(Vector.Up);
      var2.Negate();
      var2.Add(var1);
      var2.Normalize();
      var2.Mul(this.m_Params._MAX_BOUNCE_RANGE);
      this.m_vSlideVector.Add(var2);
      this.m_vSlideVector.Normalize();
      this.m_vSlideVector.Mul(this.m_Params._MAX_BOUNCE_RANGE);
      var2.Null();
   }

   void SupressCollVector() {
      float var1 = this.m_vCollisionVector.Len() - this.m_Params._COLL_SUPRESS * RacerModule.m_Mod.m_fFrameTime;
      if (var1 < 0.0F) {
         var1 = 0.0F;
      }

      this.m_vCollisionVector.Normalize();
      this.m_vCollisionVector.Mul(var1);
   }

   void UpdateSlidePhysics() {
      if (this.m_fCurrSpeed == 0.0F) {
         this.m_iCurrPhysics = 0;
      }

      this.SetEngineNeutralFreq();
      this.m_bIsFlyingOnFrame = false;
      this.DecreaseTurning();
      this.TurnSlideSmokeFX();
      if (this.m_vSlideVector.Len2() > 0.0F) {
         this.SlowDown();
         this.DecreaseSlide();
      }

      if (this.m_vCollisionVector.Len2() > 0.0F) {
         this.SupressCollVector();
      }

      this.MoveVehicle();
      this.SlowTurningVehicle();
      this.DecreaseVehicleVAngle();
      this.DecreaseVehicleHAngle();
      this.UpdateVehicleCharacter();
      if (this.m_fAddTurnAngle > 0.0F) {
         float var1 = RacerModule.m_Mod.m_fFrameTime * this.m_Params._SLIDE_ROTATION_SPEED;
         this.m_fAddTurnAngle -= var1;
         if (this.m_fAddTurnAngle < 0.0F) {
            var1 += this.m_fAddTurnAngle;
            this.m_fAddTurnAngle = 0.0F;
         }

         this.m_vVehicleRight = this.RotateVector(this.m_vVehicleRight, var1, this.m_vVehicleNormal);
         this.m_vVehicleForward = this.RotateVector(this.m_vVehicleForward, var1, this.m_vVehicleNormal);
      }

      if (this.m_bFlying) {
         GameObject.LogLine("Spadamy");
         this.FallDown();
      } else if (!this.m_bIsFlyingOnFrame) {
         this.m_fFlyingTime = 0.0F;
         Vector var2 = this.CalculateNewNormal();
         if (var2.Len2() == 0.0F) {
            this.SetVehiclePosAndOrientation();
         } else {
            var2.Mul(this.m_Params._GROUND_ALIGN_INERTIA * RacerModule.m_Mod.m_fFrameTime);
            var2.Add(this.m_vVehicleNormal);
            var2.Normalize();
            this.m_vVehicleNormal.Set(var2);
            this.SetVehiclePosAndOrientation();
         }
      } else {
         this.AlignToUp();
         this.m_fFlyingTime = this.m_fFlyingTime + RacerModule.m_Mod.m_fFrameTime;
         if (this.m_fFlyingTime > this.m_Params._REAL_FLY_TIME) {
            if (!this.m_bFlying) {
               this.m_Character.PlayHandShakeAnim();
            }

            this.m_bFlying = true;
         }
      }
   }

   void SetLowEngineVolume() {
      if (this.m_iNofEngineSound != 0) {
         this.SetSoundVolume(this.m_iNofEngineSound, 0.35F);
      } else {
         GameObject.LogLine("SetLowEngineVolume nie ma numerka");
      }
   }

   void DisableTireSpikes() {
      this.ModEndActiveBonus(6); // [mod]
      this.m_bTireSpikes = false;
   }

   void UFO() {
      // [mod] en ligne : l'OVNI est envoyé à son joueur dès le lancer (UseBonus), celui-ci n'est que visuel
      if (this.m_bModGhost) {
         return;
      }

      this.InputsRelease();
      this.UpdateVehicleInput(false);
      this.SystemBrake();
      this.m_bCanUseBonus = false;
      this.m_bWasTakenByUFO = true;
      this.CheckForStopByUFO();
   }

   void SetEngineFreqMin() {
      if (this.m_iNofEngineSound != 0) {
         this.SetSoundFreq(this.m_iNofEngineSound, this.m_Params._MIN_SOUND_FREQ);
      }
   }

   void UpdateVehicleWheels() {
      Vector var1 = Vector.newVector();
      float var2 = this.m_fCurrVehicleHAngle / 57.29F;
      Vector var3 = Vector.newVector((float)Math.sin(var2), (float)Math.cos(var2), 0.0F);
      int var4 = 0;

      do {
         var1.Set(this.GetElementLocalPos(this.m_WheelsBase[var4]));
         var1.fY = var1.fY + this.m_Springs[var4];
         this.FromUpForwardPosElement(this.m_Wheels[var4], var3, Vector.Forward, var1);
      } while (++var4 < 4);

      var3.Null();
      var1.Null();
      this.RotateElement(this.m_Wheels[1], Vector.Up, -this.m_fCurrDirAngle / 2.0F);
      this.RotateElement(this.m_Wheels[0], Vector.Up, -this.m_fCurrDirAngle / 2.0F);
      boolean var6 = false;
      if (!this.m_bFlying) {
         if (this.m_fAccelerate > 0.0F && this.m_fCurrSpeed < 0.0F) {
            var6 = true;
         }

         if (this.m_fBrake > 0.0F && this.m_fCurrSpeed > 0.0F) {
            var6 = true;
         }
      }

      if (!var6) {
         this.m_fWheelRotationSpeed = this.m_fWheelRotationSpeed + this.m_fCurrSpeed * this.m_Params._WHEEL_MOVE_SPEED_COEF;
         int var5 = 0;

         do {
            this.RotateElement(this.m_Wheels[var5], Vector.Right, this.m_fWheelRotationSpeed);
         } while (++var5 < 4);
      }
   }

   void SetVehiclePosAndOrientation() {
      this.m_vVehicleNormal.Normalize();
      this.m_vVehicleRight = this.m_vVehicleNormal.Cross(this.m_vVehicleForward);
      this.m_vVehicleRight.Normalize();
      this.m_vVehicleForward = this.m_vVehicleRight.Cross(this.m_vVehicleNormal);
      this.m_vVehicleForward.Normalize();
      this.m_vVehicleTForward.Set(this.m_vVehicleForward);
      this.m_vVehicleTNormal.Set(this.m_vVehicleNormal);
      this.m_vVehicleTForward = this.RotateVector(this.m_vVehicleTForward, this.m_fCurrVehicleVAngle, this.m_vVehicleRight);
      this.m_vVehicleTNormal = this.RotateVector(this.m_vVehicleTNormal, this.m_fCurrVehicleVAngle, this.m_vVehicleRight);
      this.m_vVehicleTNormal = this.RotateVector(this.m_vVehicleTNormal, this.m_fCurrVehicleHAngle, this.m_vVehicleTForward);
      this.FromUpForwardPos(this.m_vVehicleTNormal, this.m_vVehicleTForward, this.m_vVehiclePosition);
      this.UpdateSpringsAndWheels();
   }

   void ExplosionEffect() {
      // [mod] en ligne : fantôme touché, l'effet est appliqué chez son joueur (ModNet)
      if (this.m_bModGhost) {
         ModNet.HitGhost(this, ModNet._HIT_EXPLOSION);
         return;
      }

      if (this.m_iCurrPhysics != 1 && this.m_iCurrPhysics != 2) {
         this.m_bCanUseBonus = false;
         this.m_iCurrPhysics = 1;
         this.ZeroParams();
         this.m_fCurrSpeed = 0.0F;
         this.m_fCurrGravity = this.m_Params._EXPLOSION_ACCEL;
         this.m_fAddTurnAngle = this.m_Params._NUM_OF_V_TURNS * 360.0F;
      }
   }

   void TurnSlideSmokeFX() {
      if (RacerModule.m_Mod.m_fNowTime - this.m_fLastSlideFXTime > this.m_Params._TIME_BTW_SMOKES) {
         this.m_fLastSlideFXTime = RacerModule.m_Mod.m_fNowTime;
         int var1 = 0;

         do {
            this.ExecuteFX("Smoke", this.GetElementPos(this.m_Wheels[var1]), this.m_vVehicleNormal);
         } while (++var1 < 4);
      }
   }

   public void Go() {
      this.DisableCallMethod("Go");
      this.UpdateVehicleInput(true);
      this.m_bStopped = false;
      this.m_bCanUseBonus = true;
   }

   void DetachRoadSound() {
      if (this.m_iNofRoadSound != 0) {
         this.DetachSound(this.m_iNofRoadSound);
      }

      this.m_iNofRoadSound = 0;
   }

   void DetachProtectionSound() {
      if (this.m_iNofProtectionSound != 0) {
         this.DetachSound(this.m_iNofProtectionSound);
      }

      this.m_iNofProtectionSound = 0;
   }

   void DetachWaterSound() {
      if (this.m_iNofWaterPoolSound != 0) {
         this.DetachSound(this.m_iNofWaterPoolSound);
      }

      this.m_iNofWaterPoolSound = 0;
   }

   void ZeroRespawnParams() {
      this.m_fFlyingTime = 0.0F;
      this.m_fLandingTime = 0.0F;
      this.m_bEnableBack = true;
      this.m_vSlideVector.Set(Vector.Zero);
      this.m_fCurrSpeed = 0.0F;
      this.m_fCurrGravity = 0.0F;
      this.m_fCurrDirAngle = 0.0F;
      this.m_bIsFlyingOnFrame = false;
      this.m_fWheelRotationSpeed = 0.0F;
      this.m_bFlying = false;
      this.m_bLanding = false;
      this.m_vTurningSlide.Set(Vector.Zero);
      this.m_fCurrVehicleHAngle = 0.0F;
      this.m_fCurrVehicleVAngle = 0.0F;
      this.m_vCollisionVector.Set(Vector.Zero);
      this.m_vMoveDir.Set(Vector.Zero);
      this.m_bGotCollision = false;
   }

   boolean CanAttachOneShotSound() {
      if (!RacerModule.m_bIsDemo) {
         Vehicle var1 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_PlayersTab.elementAt(0);
         if (var1.m_vVehiclePosition.Distance(this.m_vVehiclePosition) > 5000.0F) {
            if (!RacerGame.GetCoreLogic().IsMultiplayer()) {
               return false;
            }

            Vehicle var2 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_PlayersTab.elementAt(1);
            if (var2.m_vVehiclePosition.Distance(this.m_vVehiclePosition) > 5000.0F) {
               return false;
            }
         }
      }

      if (m_iNofCurrPlayingSounds < 2) {
         this.AddSound();
         return true;
      } else {
         return false;
      }
   }

   void AlignToUp() {
      Vector var1 = Vector.newVector(Vector.Up);
      var1.Mul(this.m_Params._UP_FLY_ALIGN_INERTIA * RacerModule.m_Mod.m_fFrameTime);
      var1.Add(this.m_vVehicleNormal);
      var1.Normalize();
      this.m_vVehicleNormal.Set(var1);
      var1.Null();
      this.SetVehiclePosAndOrientation();
   }

   void TurnVehicle() {
      float var1 = 1.0F;
      if (this.m_fFlyingTime > this.m_Params._REAL_FLY_TIME_CONTROL) {
         var1 = this.m_Params._FLY_MANOEUVRE_COEF;
      }

      Vector var2 = new Vector(this.m_vVehicleRight);
      if (this.m_fCurrSpeed < 0.0F) {
         var2.Negate();
      }

      var2 = this.m_vVehicleForward.Cross(var2);
      this.m_vVehicleForward = this.RotateVector(this.m_vVehicleForward, -this.m_fCurrDirAngle * RacerModule.m_Mod.m_fFrameTime * var1, var2);
   }

   public void WaterVehicle() {
      // [mod] en ligne : fantôme touché, l'effet est appliqué chez son joueur (ModNet)
      if (this.m_bModGhost) {
         ModNet.HitGhost(this, ModNet._HIT_WATER);
         return;
      }

      this.m_iStopCause = 2;
      this.StopByObstacle();
   }

   public void CheckForStop() {
      this.m_bStopped = true;
      this.DisableCallMethod("CheckForStop");
      if (this.m_fCurrSpeed <= 0.0F) {
         this.m_fBrake = 0.0F;
      } else {
         this.CallMethodIn("CheckForStop", _CHECK_GO_UPDATE_TIME);
      }
   }

   void RotateVehicleVertical() {
      // [mod] en ligne : les pièges ne touchent pas les fantômes (copie du piège chez son joueur)
      if (this.m_bModGhost) {
         return;
      }

      this.m_iCurrPhysics = 3;
      this.m_vSlidePhysicsDir.Set(this.m_vVehicleForward);
      this.m_vSlidePhysicsDir.fY = 0.0F;
      this.m_vSlidePhysicsDir.Normalize();
      this.m_fAddTurnAngle = this.m_Params._NUM_OF_H_TURNS * 360.0F;
   }

   public void PlayStopAnim() {
      if (this.m_iStopCause == 0) {
         this.m_Character.PlayGasLaughAnim();
      }

      if (this.m_iStopCause == 1) {
         this.m_Character.PlayWaspAnim();
      }

      if (this.m_iStopCause == 2) {
         this.m_Character.PlayShakeAnim();
      }

      if (this.m_iStopCause == 4) {
         this.m_Character.PlayShakeAnim();
      }

      if (this.m_iStopCause == 3) {
         this.m_Character.PlayHitAnim();
      }

      this.CallMethodIn("GoAfterAnim", _CHECK_FOR_GO_ANIM);
   }

   void DirectSteering(boolean var1, VehicleAI var2) {
      this.m_bDirectSteer = var1;
      if (var2 == null) {
         GameObject.LogLine("Blad: nie zostalo podane AI, ktore steruje tym pojazdem");
      }

      this.m_VehicleAI = var2;
   }

   public void RespawnAfterFalling() {
      this.UpdateVehicleInput(true);
      this.m_iCurrPhysics = 0;
      this.m_bCanUseBonus = true;
      this.m_vVehiclePosition.fY = RacerModule.m_Mod.TerrainHeight(this.m_vVehiclePosition.fX, this.m_vVehiclePosition.fZ) + this.m_Params._RESPAWN_HEIGHT;
      this.SetPosition(this.m_vVehiclePosition);
      this.ZeroVehicleParams();
   }

   void StoppedByFunnyGas() {
      // [mod] en ligne : les pièges ne touchent pas les fantômes (copie du piège chez son joueur)
      if (this.m_bModGhost) {
         return;
      }

      this.m_iStopCause = 0;
      this.StopByObstacle();
   }

   void TurnLeftRelease() {
      this.m_fInTurnLeft = 0.0F;
      if (this.m_bGetInput) {
         this.m_fTurnLeft = 0.0F;
      }
   }

   void TurnRightRelease() {
      this.m_fInTurnRight = 0.0F;
      if (this.m_bGetInput) {
         this.m_fTurnRight = 0.0F;
      }
   }

   int GetCurrentPlace() {
      return this.m_iCurrPlace;
   }

   void UpdateVehiclePhysics() {
      this.m_bIsFlyingOnFrame = false;
      if (this.m_bTurbo) {
         this.SetEngineFreqTurbo();
      } else if (!this.m_bGetInput) {
         this.SetEngineNeutralFreq();
      } else if (!this.m_bFlying) {
         this.SetEngineFreq();
      } else {
         this.SetEngineNeutralFreq();
      }

      if (this.m_fCurrDirAngle != 0.0F) {
         this.DecreaseTurning();
      }

      if (this.m_fCurrDirAngle != 0.0F && this.m_fCurrSpeed != 0.0F && !this.m_bFlying) {
         this.AddSlideFromTurning();
      }

      if (this.m_vTurningSlide.Len2() > 0.0F) {
         this.DecreaseTurnSlide();
         float var1 = this.m_vTurningSlide.Len();
         if (var1 > this.m_Params._SLIDE_SMOKE_COEF) {
            this.TurnSlideSmokeFX();
            if (this.m_fCurrSpeed > this.m_fCurrMaxSpeed / 3.0F) {
               if (var1 > this.m_fLastTurnSlideValue) {
                  this.m_bSlideIncreasing = true;
               } else {
                  this.m_bSlideIncreasing = false;
               }
            } else {
               this.m_bSlideIncreasing = false;
            }
         } else {
            this.m_bSlideIncreasing = false;
         }

         this.m_fLastTurnSlideValue = var1;
      } else {
         this.m_bSlideIncreasing = false;
      }

      if (this.m_bSlideIncreasing) {
         this.PlaySlideSound();
      } else {
         this.StopSlideSound();
      }

      this.CalculateSlopeAffect();
      if (this.m_fCurrSpeed != 0.0F) {
         if (this.m_bDirectSteer) {
            this.TurnVehicleUsingAngle();
         } else if (this.m_fCurrDirAngle != 0.0F) {
            this.TurnVehicle();
         }
      }

      if (this.m_vSlideVector.Len2() > 0.0F) {
         this.SlowDown();
         this.DecreaseSlide();
      }

      if (this.m_vCollisionVector.Len2() > 0.0F) {
         this.SupressCollVector();
      }

      this.MoveVehicle();
      this.ExecuteSmokeFxs();
      this.SetVehicleAngles();
      this.UpdateVehicleCharacter();
      if (this.m_bPlayBrakeSound) {
         if (!this.m_bIsFlyingOnFrame) {
            this.PlayBrakeSound();
         }

         this.m_bPlayBrakeSound = false;
      }

      if (this.m_bFlying) {
         this.FallDown();
         this.DecreaseVehicleVAngle();
      } else if (!this.m_bIsFlyingOnFrame) {
         this.m_fFlyingTime = 0.0F;
         Vector var2 = this.CalculateNewNormal();
         if (var2.Len2() == 0.0F) {
            this.SetVehiclePosAndOrientation();
         } else {
            var2.Mul(this.m_Params._GROUND_ALIGN_INERTIA * RacerModule.m_Mod.m_fFrameTime);
            var2.Add(this.m_vVehicleNormal);
            var2.Normalize();
            this.m_vVehicleNormal.Set(var2);
            this.SetVehiclePosAndOrientation();
         }
      } else {
         this.AlignToUp();
         this.m_fFlyingTime = this.m_fFlyingTime + RacerModule.m_Mod.m_fFrameTime;
         if (this.m_fFlyingTime > this.m_Params._REAL_FLY_TIME) {
            if (!this.m_bFlying) {
               this.m_Character.PlayHandShakeAnim();
            }

            this.m_bFlying = true;
         }
      }
   }

   void AccelerateAnalog(float var1) {
      if (this.m_fInAccelerate == 0.0F && var1 > 0.0F) {
         this.m_fLastAccelPressTime = RacerModule.m_Mod.m_fNowTime;
      }

      this.m_fInAccelerate = var1;
      this.m_fInBrake = 0.0F;
      if (!this.m_bTurbo) {
         if (this.m_bGetInput) {
            this.m_fAccelerate = var1;
            this.m_fBrake = 0.0F;
         }
      }
   }

   int GetCurrentLap() {
      if (this.m_iCurrLoop == RacerModule.m_Mod.m_RaceParams.m_iNofLoops) {
         return this.m_iCurrLoop - 1;
      } else {
         return this.m_iCurrLoop < 0 ? 0 : this.m_iCurrLoop;
      }
   }

   float GetRevolution() {
      return this.m_fCurrRevolution;
   }

   void CheckForWrongWay() {
      if (this.m_iCurrPhysics != 0) {
         this.m_bWrongWay = false;
      } else {
         int var1 = RacerModule.m_Mod.m_Track.GetPrevPointNum(this.m_iCurrTrackPoint);
         if (var1 == -1) {
            this.m_bWrongWay = false;
         } else {
            Vector var2 = Vector.newVector(RacerModule.m_Mod.m_Track.GetPointPos(this.m_iCurrTrackPoint));
            var2.Sub(RacerModule.m_Mod.m_Track.GetPointPos(var1));
            var2.fY = 0.0F;
            var2.Normalize();
            Vector var3 = Vector.newVector(this.m_vVehicleForward);
            var3.fY = 0.0F;
            var3.Normalize();
            if (var3.Dot(var2) < -0.4F) {
               this.m_bWrongWay = true;
            } else {
               this.m_bWrongWay = false;
            }

            var3.Null();
            var2.Null();
         }
      }
   }

   void DisableProtection() {
      this.ModEndActiveBonus(3); // [mod] (remplace DetachFromCenter, qui supprimait le bonus de la case quel qu'il soit)
      this.m_bProtection = false;
      this.DetachProtectionSound();
   }

   void DetachEngineSound() {
      if (this.m_iNofEngineSound != 0) {
         this.DetachSound(this.m_iNofEngineSound);
      }

      this.m_iNofEngineSound = 0;
   }

   void DetachBridgeSound() {
      if (this.m_iBridgeSound != 0) {
         this.DetachSound(this.m_iBridgeSound);
         if (this.m_bIsObservedByCamera) {
            this.PlayRoadSound();
         }
      }

      this.m_iBridgeSound = 0;
   }

   void DetachOffRoadSound() {
      if (this.m_iNofOffRoadSound != 0) {
         this.DetachSound(this.m_iNofOffRoadSound);
      }

      this.m_iNofOffRoadSound = 0;
   }

   void PlayOutrunSound() {
      if (this.CanPlayTalkSound()) {
         if (!(RacerGame.GetRandom() > this.m_Params._PROB_PLAY_OUTRUN_SOUND)) {
            if (this.CanAttachOneShotSound()) {
               int var1 = this.m_iAnimalID * MenuAnimal._NUM_OF_OUTRUN_SOUNDS_PER_ANIMAL + GameObject.RandomInt(MenuAnimal._NUM_OF_OUTRUN_SOUNDS_PER_ANIMAL);
               this.AttachOneShotSound(MenuAnimal._OUTRUN_SOUNDS[var1], VehicleParams._MIN_TALK_RANGE, VehicleParams._MAX_TALK_RANGE);
            }
         }
      }
   }

   void PlayRoadSound() {
      if (this.m_bFlying || this.m_fCurrSpeed == 0.0F || !this.m_bIsObservedByCamera) {
         this.DetachRoadSound();
         this.DetachOffRoadSound();
      } else if (this.m_bIsOnTrack && !this.m_bWrongWay) {
         this.DetachOffRoadSound();
         this.AttachRoadSound(VehicleParams._CAR_SOUNDS[VehicleParams._MOVE_ROAD]);
      } else {
         this.DetachRoadSound();
         this.AttachOffRoadSound(VehicleParams._CAR_SOUNDS[VehicleParams._MOVE_OFF_ROAD]);
      }
   }

   void TurboFire() {
      this.ExecuteFX(this, "FXTurbo", Vector.Zero);
   }

   void PlayHornSound() {
      RacerModule var1 = RacerGame.GetRacerModule();
      if (var1 != null) {
         RacerCamera var2 = (RacerCamera)RacerModule.m_Cams[0];
         if (var2 != null && var2.coSpeaker != null && this.m_HornSound != null) {
            var2.coSpeaker.AttachOneShotSound(this.m_HornSound);
         }
      }
   }

   public void AddSound() {
      m_iNofCurrPlayingSounds++;
      this.CallMethodIn("RemoveSound", 3.0F);
   }

   void TwoWheelsSmokeFX() {
      if (RacerModule.m_Mod.m_fNowTime - this.m_fLastAccelFXTime > this.m_Params._TIME_BTW_SMOKES) {
         this.m_fLastAccelFXTime = RacerModule.m_Mod.m_fNowTime;
         this.ExecuteFX("Smoke", this.GetElementPos(this.m_Wheels[0]), this.m_vVehicleNormal);
         this.ExecuteFX("Smoke", this.GetElementPos(this.m_Wheels[1]), this.m_vVehicleNormal);
      }
   }

   void FourWheelsSmokeFX() {
      if (RacerModule.m_Mod.m_fNowTime - this.m_fLastBrakeFXTime > this.m_Params._TIME_BTW_SMOKES) {
         this.m_fLastBrakeFXTime = RacerModule.m_Mod.m_fNowTime;
         this.ExecuteFX("Smoke", this.GetElementPos(this.m_Wheels[0]), this.m_vVehicleNormal);
         this.ExecuteFX("Smoke", this.GetElementPos(this.m_Wheels[1]), this.m_vVehicleNormal);
         this.ExecuteFX("Smoke", this.GetElementPos(this.m_Wheels[2]), this.m_vVehicleNormal);
         this.ExecuteFX("Smoke", this.GetElementPos(this.m_Wheels[3]), this.m_vVehicleNormal);
      }
   }

   void EnableBack() {
      this.DisableCallMethod("EnableBack");
      this.m_bEnableBack = true;
   }

   void StartAim() {
      this.DisableCallMethod("AimAtEnemy");
      this.CallMethodIn("AimAtEnemy", _AIM_UPDATE_TIME);
   }

   public void StopAndStars() {
      // [mod] en ligne : fantôme touché, l'effet est appliqué chez son joueur (ModNet)
      if (this.m_bModGhost) {
         ModNet.HitGhost(this, ModNet._HIT_STARS);
         return;
      }

      this.m_iStopCause = 3;
      this.StopByObstacle();
   }

   Vector RandomProperPos(Vector var1) {
      boolean var2 = false;
      boolean var3 = false;
      Vector var4 = Vector.newVector();

      while (!var2) {
         for (int var5 = 0; var5 < RacerModule.m_Mod.m_RaceParams.m_Vehicles.size(); var5++) {
            Vehicle var6 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_Vehicles.elementAt(var5);
            if (this != var6) {
               var4.Set(var6.m_vVehiclePosition);
               var4.Sub(var1);
               var4.fY = 0.0F;
               if (var4.Len() < 300.0F) {
                  var3 = true;
                  break;
               }
            }
         }

         if (!var3) {
            break;
         }

         Vector var7 = Vector.newVector(2.0F * RacerGame.GetRandom() - 1.0F, 0.0F, 2.0F * RacerGame.GetRandom() - 1.0F);
         var7.Normalize();
         var7.Mul(450.0F);
         var1.Add(var7);
         var3 = false;
         var7.Null();
      }

      var4.Null();
      return var1;
   }

   void TurnVehicleUsingAngle() {
      if (this.m_fAngleToTurn != 0.0F) {
         Vector var1 = new Vector(this.m_vVehicleRight);
         if (this.m_fCurrSpeed < 0.0F) {
            var1.Negate();
         }

         var1 = this.m_vVehicleForward.Cross(var1);
         float var2 = RacerModule.m_Mod.m_fFrameTime * this.GetDefaultMaxTurnAngle();
         if (this.m_fFlyingTime > this.m_Params._REAL_FLY_TIME_CONTROL) {
            var2 *= this.m_Params._FLY_MANOEUVRE_COEF;
         }

         if (this.m_fAngleToTurn > 0.0F) {
            this.m_fAngleToTurn -= var2;
            if (this.m_fAngleToTurn < 0.0F) {
               var2 += this.m_fAngleToTurn;
               this.m_fAngleToTurn = 0.0F;
            }
         } else {
            this.m_fAngleToTurn += var2;
            if (this.m_fAngleToTurn > 0.0F) {
               var2 -= this.m_fAngleToTurn;
               this.m_fAngleToTurn = 0.0F;
            }

            var2 *= -1.0F;
         }

         this.m_vVehicleForward = this.RotateVector(this.m_vVehicleForward, -var2, var1);
      }
   }

   void TurnRightHAngle(boolean var1) {
      if (this.m_fCurrSpeed == 0.0F) {
         this.DecreaseVehicleHAngle();
      } else {
         if (var1) {
            this.m_fCurrVehicleHAngle = this.m_fCurrVehicleHAngle
               - Math.abs(this.m_fCurrSpeed) / this.m_fCurrMaxSpeed * RacerModule.m_Mod.m_fFrameTime * this.m_Params._HORIZ_DEV_ACCEL;
            if (this.m_fCurrVehicleHAngle < -this.m_Params._MAX_HORIZ_DEV) {
               this.m_fCurrVehicleHAngle = -this.m_Params._MAX_HORIZ_DEV;
               return;
            }
         } else {
            this.m_fCurrVehicleHAngle = this.m_fCurrVehicleHAngle
               + Math.abs(this.m_fCurrSpeed) / this.m_fCurrMaxSpeed * RacerModule.m_Mod.m_fFrameTime * this.m_Params._HORIZ_DEV_ACCEL;
            if (this.m_fCurrVehicleHAngle > this.m_Params._MAX_HORIZ_DEV) {
               this.m_fCurrVehicleHAngle = this.m_Params._MAX_HORIZ_DEV;
            }
         }
      }
   }

   // [mod] triches de bonus du menu Mods (humains seulement), appelé à chaque image par UpdateVehicle :
   // - protection permanente : le drapeau que testent tous les projectiles et pièges reste levé (sans bulle visible) ;
   // - bonus infinis : la case vide se remplit 1 s après usage (ou dès le départ), si le circuit a des bonus.
   static final float _INFINITE_BONUS_DELAY = 1.0F;
   float m_fModBonusEmptySince = -1.0F;

   void UpdateModCheats() {
      if (!ModOptions.IsCheater(this)) {
         return;
      }

      if (ModOptions.m_bShield) {
         this.m_bProtection = true;
      }

      if (ModOptions.m_bInfiniteBonus && (this.m_CurrentBonus == null || this.ModIsActiveSelfBonus(this.m_CurrentBonus)) && this.m_bGetInput
         && !this.m_bRaceFinished) {
         LevelBonusTypes bonuses = RacerModule.m_Mod.m_RaceParams.m_AvailableBonuses;
         if (bonuses == null || bonuses.m_Types.size() == 0) {
            return;
         }

         float fNow = RacerModule.m_Mod.m_fNowTime;
         if (this.m_fModBonusEmptySince < 0.0F || fNow < this.m_fModBonusEmptySince) {
            this.m_fModBonusEmptySince = fNow;
         } else if (fNow - this.m_fModBonusEmptySince >= _INFINITE_BONUS_DELAY) {
            this.m_fModBonusEmptySince = -1.0F;
            this.CollectBonus();
         }
      } else {
         this.m_fModBonusEmptySince = -1.0F;
      }
   }

   void UpdateVehicle() {
      this.UpdateModCheats(); // [mod]
      if (!(RacerModule.m_Mod.m_fFrameTime < 1.0E-4F)) {
         if (this.m_iCurrPhysics == 1) {
            this.UpdateExplosionPhysics();
         } else if (this.m_iCurrPhysics == 2) {
            this.UpdateFallUnderGroundPhysics();
         } else if (this.m_iCurrPhysics == 3) {
            this.UpdateSlidePhysics();
         } else if (this.m_iCurrPhysics != 4) {
            if (this.m_iCurrPhysics != 5) {
               this.UpdateMovementParams();
               this.UpdateVehiclePhysics();
            }
         }
      }
   }

   public void FreezeVehicle() {
      // [mod] en ligne : fantôme touché, l'effet est appliqué chez son joueur (ModNet)
      if (this.m_bModGhost) {
         ModNet.HitGhost(this, ModNet._HIT_FREEZE);
         return;
      }

      this.InputsRelease();
      this.UpdateVehicleInput(false);
      this.SystemBrake();
      this.m_bCanUseBonus = false;
      this.CallMethodIn("Unfreeze", this.m_Params._FREEZE_TIME);
      this.CheckForStop();
   }

   public void CheckForWaterPool() {
      Vector var1 = new Vector(this.m_vVehicleForward);
      var1.Normalize();
      Vector var2 = new Vector(var1);
      var1.Mul(50.0F);
      var1.fY += 150.0F;
      var1.Add(this.m_vVehiclePosition);
      var2.Mul(150.0F);
      var2.Add(this.m_vVehiclePosition);
      Collision var3 = this.TraceObjects(var1, var2);
      if (var3 != null) {
         TrackObject var4 = (TrackObject)var3.Obstacle;
         if (var4.m_iClassID == TrackObject._CLASS_WATER_POOL) {
            if (!this.m_bInWaterPool) {
               this.ExecuteFX(this, "FXWaterSplash", Vector.Zero);
               this.PlaySound(VehicleParams._SOUNDS[VehicleParams._SND_TO_WATER]);
            }

            this.m_bInWaterPool = true;
            if (this.m_fCurrSpeed != 0.0F) {
               this.AttachWaterSound(VehicleParams._SOUNDS[VehicleParams._SND_IN_WATER]);
               return;
            }

            this.DetachWaterSound();
            return;
         }
      }

      this.m_bInWaterPool = false;
      this.DetachWaterSound();
   }

   void UpdateMaxSpeedAccel() {
      // [mod] triche « vitesse » du menu Mods (humains seulement) : vitesse maxi et accélération, pas le virage.
      // Appelé à la création de la voiture (pas encore marquée humaine) puis par chaque circuit après l'ajout des
      // joueurs (RacerModule.UpdateVehiclesParams) : c'est ce second appel qui compte.
      float fCheat = ModOptions.GetSpeedMul(this);
      this.m_fDefaultMaxSpeed = this.m_Params._MAX_SPEED * VehicleParams._ANIMALS_SPEED_COEFS[this.m_iAnimalID] * fCheat;
      this.m_fDefaultMaxTurnAngle = this.m_Params._MAX_TURN_ANGLE * VehicleParams._ANIMALS_TURN_COEFS[this.m_iAnimalID];
      this.m_fDefaultMaxAccel = this.m_Params._ACCELERATION_COEF * VehicleParams._ANIMALS_ACCEL_COEFS[this.m_iAnimalID] * fCheat;
      this.m_fDefaultMinAccel = this.m_fDefaultMaxAccel / 3.0F;
      this.m_fCollCoef = this.m_fDefaultMaxSpeed / 5.0F;
      this.m_fCurrMaxSpeed = this.m_fDefaultMaxSpeed;
      this.m_fCurrSpeedModifier = this.m_fCurrMaxSpeed * 0.03F;
   }

   void SetHighEngineVolume() {
      if (this.m_iNofEngineSound != 0) {
         this.SetSoundVolume(this.m_iNofEngineSound, 0.7F);
      } else {
         GameObject.LogLine("SetHighEngineVolume nie ma numerka");
      }
   }

   // [mod] Bonus « sur soi » (chantier 5, notes/bonus.md). À l'origine, turbo (type d'usage 2), pointes (6) et protection
   // (3) restaient dans la case pendant tout leur effet (10 s) : CollectBonus refusait toute boîte. Désormais, au
   // ramassage suivant, l'objet de l'effet en cours est mis de côté ici ; son icône s'arrête de clignoter et l'effet
   // continue. Disable* retrouvent l'objet dans la case ou de côté (ModEndActiveBonus).
   UseItem m_ModParkedTurbo = null;
   UseItem m_ModParkedSpikes = null;
   UseItem m_ModParkedProtection = null;

   boolean ModIsActiveSelfBonus(UseItem b) {
      return b != null && b.m_bWasUsed && (b.m_UseType == 2 || b.m_UseType == 6 || b.m_UseType == 3);
   }

   // libère la case si elle ne contient qu'un bonus sur soi déjà activé ; false si elle contient autre chose
   boolean ModParkActiveBonus() {
      UseItem b = this.m_CurrentBonus;
      if (!this.ModIsActiveSelfBonus(b)) {
         return false;
      }

      // un plus ancien du même type (2e turbo pris pendant le 1er) est fini tout de suite : sa minuterie a été relancée
      UseItem old = this.ModGetParked(b.m_UseType);
      if (old != null && old != b) {
         this.ModFinishItem(old);
      }

      this.ModSetParked(b.m_UseType, b);
      b.DisableOnHud();
      this.m_CurrentBonus = null;
      return true;
   }

   // fin de l'effet : l'objet est soit dans la case (rien ramassé depuis), soit de côté
   void ModEndActiveBonus(int nType) {
      UseItem parked = this.ModGetParked(nType);
      this.ModSetParked(nType, null);
      if (parked != null) {
         this.ModFinishItem(parked);
      }

      if (this.m_CurrentBonus != null && this.m_CurrentBonus.m_UseType == nType && this.m_CurrentBonus.m_bWasUsed) {
         UseItem b = this.m_CurrentBonus;
         this.m_CurrentBonus = null;
         if (b != parked) {
            this.ModFinishItem(b);
         }
      }
   }

   void ModFinishItem(UseItem b) {
      b.DisableOnHud();
      if (b.m_UseType == 3) {
         // ce que faisait UseItem.DetachFromCenter, mais sur cet objet-là et pas sur le contenu de la case
         b.DetachFromParentAndKeepWorldPos();
         b.EnableRendering(false);
         b.delete();
      }
   }

   UseItem ModGetParked(int nType) {
      return nType == 2 ? this.m_ModParkedTurbo : (nType == 6 ? this.m_ModParkedSpikes : (nType == 3 ? this.m_ModParkedProtection : null));
   }

   void ModSetParked(int nType, UseItem b) {
      if (nType == 2) {
         this.m_ModParkedTurbo = b;
      } else if (nType == 6) {
         this.m_ModParkedSpikes = b;
      } else if (nType == 3) {
         this.m_ModParkedProtection = b;
      }
   }
}
