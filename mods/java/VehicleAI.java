// [mod] IA des voitures. Difficulté « d'origine » (ModOptions.m_nAiMode = 0) : code de Techland intact (Orig…).
// Sinon, IA améliorée (méthodes Mod…, chantier IA, notes/ia.md) : trajectoire à la corde, regard vers le point
// suivant, freinage avant les virages serrés, volant progressif, évitement des voitures, bonus utilisés à bon
// escient, caractère propre à chaque IA, dégagement en marche arrière et remise en piste, bilan dans crash.log.
public class VehicleAI extends GameObject {
   static final int _AI_BACK_HACK_ADD_LEVEL = 2;
   static final float _AI_BACK_HACK_DISTANCE = 0.2F;
   static final int _AI_LEVEL_0 = 0;
   static final int _AI_LEVEL_1 = 1;
   static final int _AI_LEVEL_2 = 2;
   static final int _AI_LEVEL_3 = 3;
   static final int _AI_LEVEL_4 = 4;
   static final int _AI_LEVEL_5 = 5;
   static final int _MAX_AI_LEVEL_5 = 5;
   static final float[] _AI_PRECISION = new float[]{0.6F, 0.55F, 0.45F, 0.4F, 0.3F, 0.2F};
   static final float[] _AI_NOISE = new float[]{0.6F, 0.55F, 0.45F, 0.4F, 0.3F, 0.2F};
   static final float[] _AI_BONUS_COLLECT = new float[]{0.5F, 0.6F, 0.7F, 0.8F, 0.9F, 1.0F};
   static final float[] _AI_BONUS_USE_TIME = new float[]{2.0F, 1.5F, 0.8F, 0.5F, 0.3F, 0.0F};
   static final float[] _AI_TURNING_MODIFIER = new float[]{1.0F, 1.1F, 1.2F, 1.3F, 1.4F, 1.4F};
   static final float[] _AI_SLIDE_MODIFIER = new float[]{1.0F, 0.85F, 0.7F, 0.55F, 0.45F, 0.4F};
   static final float _MAX_ANGLE = 90.0F;
   static final float _TURN_PRECISION = 0.999F;
   static final float _STOP_BONUS_COLL_ANGLE = 0.8F;
   static final float _BONUS_UPDATE_TIME = 0.1F;
   static final float _USE_TURBO_ANGLE = 20.0F;
   static final float _BACKWARD_THROW_TIME = 15.0F;
   static final float _MIN_MOVE_LEN = 1000.0F;
   static final float _START_TIME = 3.0F;
   int m_iAILevel = 3;
   Track m_Track = null;
   int m_iCurrPoint = -1;
   TrackPoint m_CurrPoint = new TrackPoint();
   Vector m_vBonusPos = new Vector();
   boolean m_bCollectingBonus = false;
   Vehicle m_Vehicle = null;
   boolean m_bMoving = false;
   boolean m_bUsingBonus = false;
   Vector m_vLastPosition = new Vector();
   float m_fStopTime = 0.0F;
   boolean m_bIsGoingRound = false;
   boolean m_bIsCounting = false;
   float m_fCurrTurningLevelModifier = 1.0F;
   float m_fCurrSlideModifier = 1.0F;

   // --- [mod] IA améliorée (notes/ia.md). Réglages ci-dessous ; unités du jeu (une voiture roule à ~4500/s). ---
   static final float _MOD_RAD = 57.29578F;           // l'original multipliait par 57,926 (1 % de trop)
   // trajectoire : part de la demi-largeur utilisable ; corde à pleine force vers 40° de virage
   // [mod] 0.6 au départ : à la corde jusqu'à 60 % de la demi-largeur, l'intérieur des virages a souvent des arbres ou
   // un mur (essai en ligne du 2026-11-20 : IA souvent dans le décor, surtout en expert)
   static final float _MOD_LINE_USE = 0.5F;
   // [mod] trajectoire « large, corde, large » (2026-11-20, retour : « l'IA veut trop prendre les virages serrés ») :
   // corde modérée à la porte du virage (0,3 + 0,04 x niveau, 0,5 en expert ; avant jusqu'à 0,8), entrée par
   // l'extérieur à la porte d'avant et sortie par l'extérieur à celle d'après
   static final float _MOD_APEX = 0.3F;
   static final float _MOD_APEX_LEVEL = 0.04F;
   static final float _MOD_WIDE = 0.4F;
   // [mod] avec la carte du circuit (ModAIMap) : la trajectoire peut prendre plus de largeur, elle est gardée dans
   // le couloir libre de chaque porte
   static final float _MOD_MAP_LINE_USE = 0.65F;
   static final int _MOD_LOG_HITS = 12;              // chocs contre le décor détaillés dans crash.log par course
   // prudence au freinage selon le niveau : x(1,1 - 0,04 x niveau), de 1,1 (facile) à 0,9 (expert)
   static final float _MOD_CAUTION_LEVEL = 0.04F;
   // points dangereux appris en course (choc contre le décor, voiture coincée), communs à toutes les IA de la course :
   // à un point dangereux, moins de corde, moins de regard vers le point suivant, plus de prudence au freinage
   static final float _MOD_DANGER_HIT = 0.5F;
   static final float _MOD_DANGER_STUCK = 0.35F;
   static final float _MOD_DANGER_CAUTION = 0.8F;
   static final float _MOD_INSIDE_FULL = 0.7F;
   static final float _MOD_LANE = 0.3F;               // couloir préféré de chaque IA (en ligne droite)
   static final float _MOD_NOISE = 0.5F;              // écart au hasard = _AI_NOISE du niveau x ce facteur
   // regard vers le point suivant
   static final float _MOD_LOOK_TIME = 0.25F;         // + 0,04 s par niveau
   static final float _MOD_LOOK_BLEND = 0.45F;        // 0,6 avant : la cible glissait trop tôt à l'intérieur
   static final float _MOD_LOOK_ANGLE = 100.0F;       // plus de regard en avant à partir de ce virage (120° avant)
   // vitesse dans les virages : temps disponible / temps nécessaire pour tourner (l'original : 0,5, sans le
   // virage suivant, et jamais de frein)
   static final float _MOD_CAUTION = 0.55F;
   // [mod] 0 = jamais de frein avant les virages (2026-11-20, demande : « je voudrais qu'elle freine plus du tout ») ;
   // l'IA lâche seulement l'accélérateur (r < 1). 0,5 avant : freinait si r < 0,5 au-dessus de 45 % de la vitesse maxi
   static final float _MOD_BRAKE_RATIO = 0.0F;
   static final float _MOD_BRAKE_MIN_SPEED = 0.45F;   // fraction de la vitesse maxi
   // volant : plein braquage (donc plein dérapage) à partir de cet angle, proportionnel en dessous
   static final float _MOD_FULL_STEER = 25.0F;
   // évitement des voitures devant
   static final float _MOD_AVOID_DIST = 1400.0F;
   static final float _MOD_AVOID_WIDTH = 220.0F;
   static final float _MOD_AVOID_PUSH = 280.0F;
   static final float _MOD_AVOID_RATE = 4.0F;
   // dégagement
   static final float _MOD_STUCK_CHECK = 1.5F;
   static final float _MOD_STUCK_MOVE = 450.0F;       // l'original : 750 en 3 s (x pente x accélération animal)
   static final float _MOD_WRONG_WAY_TIME = 2.0F;
   static final float _MOD_REVERSE_MIN = 0.6F;
   static final float _MOD_REVERSE_MAX = 1.6F;
   static final int _MOD_RESPAWN_AFTER = 3;           // dégagements de suite au même point -> remise en piste
   static final float _MOD_RESPAWN_AGAIN = 60.0F;     // 2e remise au même point dans ce délai : posée après l'obstacle
   // vitesse de passage apprise (ModAIMap) : au-dessus, on lâche l'accélérateur ; 8 % au-dessus, on freine (seule
   // exception à « jamais de frein », _MOD_BRAKE_RATIO : un saut suivi d'un virage ne se passe pas autrement)
   static final float _MOD_OVER_BRAKE = 1.08F;
   static final float _MOD_PASS_RECENT = 6.0F;        // un choc compte pour une porte passée il y a moins de 6 s
   static final float _MOD_STUCK_FORGET = 15.0F;
   // bonus
   static final float _MOD_BACK_DIST = 2500.0F;
   static final float _MOD_BACK_WIDTH = 450.0F;
   static final float _MOD_TURBO_ANGLE = 12.0F;
   static final float _MOD_STRAIGHT_ANGLE = 20.0F;

   boolean m_bMod = false;
   boolean m_bModLevelSet = false;
   // caractère de l'IA, tiré au départ
   float m_fModSkill = 1.0F;   // x écart au hasard (0,85 à 1,15)
   float m_fModLane = 0.0F;    // -1 (gauche) .. 1 (droite)
   float m_fModAggro = 0.5F;   // 0 = garde ses bonus, 1 = les lance vite
   float m_fModSpeed = 1.0F;   // x vitesse maxi (0,985 à 1 : jamais plus que le joueur)
   float m_fModApexK = 1.0F;   // x corde (0,75 à 1,25) : chaque IA a sa façon de prendre les virages
   float m_fModWideK = 1.0F;   // x entrée / sortie larges (0,5 à 1,5)
   // [mod] plan chrono du circuit (ModPlans, IA expertes) : ligne, vitesses et prudence ; null = trajectoire calculée
   ModPlans.Plan m_ModPlan = null;
   float m_fModPlanK = 1.0F;   // x vitesses du plan (animal entraîné -> animal de cette IA)
   // porte visée : centre, axe vers la droite (unitaire), demi-largeurs, décalage de la cible
   float m_fModGateCX = 0.0F;
   float m_fModGateCZ = 0.0F;
   float m_fModGateRX = 1.0F;
   float m_fModGateRZ = 0.0F;
   float m_fModGateLW = 0.0F;
   float m_fModGateRW = 0.0F;
   float m_fModGateOff = 0.0F;
   int m_iModNext = -1;
   boolean m_bModNextValid = false;
   float m_fModNextX = 0.0F;
   float m_fModNextZ = 0.0F;
   float m_fModNextOff = 0.0F;
   float[] m_fModTmp = new float[2];
   float m_fModAvoid = 0.0F;
   float m_fModAngle = 0.0F;
   // dégagement : 0 = roule, 1 = marche arrière
   int m_iModState = 0;
   float m_fModStateTime = 0.0F;
   float m_fModCheckTime = 0.0F;
   float m_fModCheckX = 0.0F;
   float m_fModCheckZ = 0.0F;
   float m_fModWrongSince = -1.0F;
   int m_iModStuckPoint = -1;
   int m_iModStuckRepeat = 0;
   float m_fModLastStuck = -100.0F;
   // bonus
   float m_fModBonusSince = -1.0F;
   boolean m_bModHadBonus = false;
   // bilan (crash.log)
   boolean m_bModBraking = false;
   boolean m_bModAvoiding = false;
   boolean m_bModLogged = false;
   int m_nModStuck = 0;
   int m_nModRespawn = 0;
   int m_nModWarp = 0;                // remises en piste du jeu vers l'avant (zone de remise en piste touchée)
   int m_iModRespawnPoint = -1;
   // [mod] vitesses de passage apprises (ModAIMap.m_aSpeed) : vitesse (part de la vitesse maxi) et heure du dernier
   // passage à chaque porte
   float[] m_aModPassSpeed = null;
   float[] m_aModPassTime = null;
   boolean m_bModOverSpeed = false;
   float m_fModRespawnAt = -100.0F;
   int m_nModBrakes = 0;
   int m_nModAvoid = 0;
   int m_nModBonusGot = 0;
   int m_nModBonusUsed = 0;
   float m_fModOffTrack = 0.0F;
   int m_nModObstacles = 0;
   float m_fModLastObstacle = -100.0F;
   static float[] m_aModDanger = null;
   static Object m_ModDangerRace = null;
   static int m_nModHitLogs = 0;

   void TurnLeft() {
      this.m_Vehicle.TurnLeft();
   }

   float GetSlideModifier() {
      return this.m_fCurrSlideModifier;
   }

   public void UseBonus() {
      if (this.m_bMod) {
         this.ModUseBonus();
         return;
      }

      this.DisableCallMethod("UseBonus");
      switch (this.m_Vehicle.m_CurrentBonus.m_UseType) {
         case 0:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 1:
            this.CallMethodIn("ThrowBackward", RacerGame.GetRandom() * 15.0F);
            return;
         case 2:
            if (Math.abs(this.m_CurrPoint.m_fAngle) < 20.0F) {
               this.m_Vehicle.UseBonus();
               this.m_bUsingBonus = false;
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 3:
            this.m_Vehicle.UseBonus();
            this.m_bUsingBonus = false;
            return;
         case 4:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 5:
            this.CallMethodIn("ThrowBackward", RacerGame.GetRandom() * 15.0F);
            return;
         case 6:
            this.m_Vehicle.UseBonus();
            this.m_bUsingBonus = false;
            return;
         case 7:
            this.CallMethodIn("ThrowBackward", RacerGame.GetRandom() * 15.0F);
            return;
         case 8:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 9:
         default:
            return;
         case 10:
            this.CallMethodIn("ThrowBackward", RacerGame.GetRandom() * 15.0F);
            return;
         case 11:
            this.CallMethodIn("ThrowBackward", RacerGame.GetRandom() * 15.0F);
            return;
         case 12:
            this.CallMethodIn("ThrowBackward", RacerGame.GetRandom() * 15.0F);
            return;
         case 13:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 14:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 15:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 16:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 17:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 18:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 19:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 20:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 21:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
               return;
            }

            this.CallMethodIn("UseBonus", 0.1F);
            return;
         case 22:
            if (this.m_Vehicle.m_LockedObject != null) {
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel]);
            } else {
               this.CallMethodIn("UseBonus", 0.1F);
            }
      }
   }

   void ChooseNextPoint() {
      if (this.m_bMod) {
         this.ModChooseNextPoint();
         return;
      }

      this.m_CurrPoint.SetFast(this.m_Track.GetPoint(this.m_iCurrPoint));
      this.m_CurrPoint.SetGateWidth(_AI_PRECISION[this.m_iAILevel]);
      this.AddNoise(_AI_NOISE[this.m_iAILevel]);
      if (this.m_Vehicle.m_CurrentBonus == null) {
         this.m_bCollectingBonus = false;
         if (this.m_CurrPoint.m_Bonuses != null) {
            if (RacerGame.GetRandom() <= _AI_BONUS_COLLECT[this.m_iAILevel]) {
               int var1 = RacerGame.GetRandom(this.m_CurrPoint.m_Bonuses.size());
               boolean var2 = false;
               if (!((BonusItem)this.m_CurrPoint.m_Bonuses.elementAt(var1)).m_fWasCollected) {
                  var2 = true;
               }

               for (int var3 = 0; var3 < var1 && !var2; var3++) {
                  if (!((BonusItem)this.m_CurrPoint.m_Bonuses.elementAt(var3)).m_fWasCollected) {
                     var1 = var3;
                     var2 = true;
                  }
               }

               for (int var4 = var1 + 1; var4 < this.m_CurrPoint.m_Bonuses.size() && !var2; var4++) {
                  if (!((BonusItem)this.m_CurrPoint.m_Bonuses.elementAt(var4)).m_fWasCollected) {
                     var1 = var4;
                     var2 = true;
                  }
               }

               if (!var2) {
                  return;
               }

               this.m_bCollectingBonus = true;
               this.m_vBonusPos.Set(((BonusItem)this.m_CurrPoint.m_Bonuses.elementAt(var1)).m_vDefaultPos);
            }

            this.m_Vehicle.Accelerate();
         }
      }
   }

   public void StopGoRound() {
      this.DisableCallMethod("StopGoRound");
      this.m_bIsGoingRound = false;
   }

   public void ThrowBackward() {
      this.DisableCallMethod("UseBonus");
      if (this.m_Vehicle.m_bIsOnTrack) {
         this.m_Vehicle.UseBonus();
         this.m_bUsingBonus = false;
      } else {
         this.CallMethodIn("ThrowBackward", RacerGame.GetRandom() * 15.0F);
      }
   }

   void Stop() {
      this.m_bMoving = false;
   }

   static float PointFromLineDist(Vector var0, Vector var1, Vector var2) {
      Vector var3 = Vector.newVector(var0);
      var3.Sub(var1);
      float var4 = var2.Dot(var3);
      var4 *= -var4;
      var4 = (float)Math.sqrt(++var4);
      var4 *= var3.Len();
      var3.Null();
      return var4;
   }

   void AddNoise(float var1) {
      var1 *= RacerGame.GetRandom();
      Vector var2 = Vector.newVector();
      if (RacerGame.GetRandom() < 0.5F) {
         var2.Set(this.m_CurrPoint.m_vRight);
         var2.Sub(this.m_CurrPoint.m_vPos);
         var2.Normalize();
         var2.Mul(this.m_CurrPoint.m_fRWidth * var1);
      } else {
         var2.Set(this.m_CurrPoint.m_vLeft);
         var2.Sub(this.m_CurrPoint.m_vPos);
         var2.Normalize();
         var2.Mul(this.m_CurrPoint.m_fLWidth * var1);
      }

      this.m_CurrPoint.m_vPos.Add(var2);
      var2.Null();
   }

   void SetAILevel(int var1) {
      if (var1 >= 0 && var1 <= 5) {
         this.m_bModLevelSet = true;
         this.m_iAILevel = var1;
         this.m_Vehicle.m_Params._MIN_SPEED_LEVELING_DIST = 0.001F * (this.m_iAILevel + 1);
         this.m_Vehicle.m_Params._MAX_SPEED_LEVELING_DIST = 0.01F * (this.m_iAILevel + 1);
         this.SetTurningLevelModifier(this.m_iAILevel);
         this.SetSlideModifier(this.m_iAILevel);
      }
   }

   void SlowOnCurve() {
      Vector var1 = this.m_Vehicle.GetPosition();
      var1.fY = 0.0F;
      Vector var2 = Vector.newVector(this.m_CurrPoint.m_vPos);
      var2.Sub(var1);
      float var3 = var2.Len();
      var2.Normalize();
      Vector var4 = Vector.newVector(this.m_Vehicle.GetForwardVector());
      var4.fY = 0.0F;
      var4.Normalize();
      float var5 = var4.Dot(var2);
      var2.Null();
      var4.Null();
      if (this.m_Vehicle.m_fCurrSpeed > this.m_Vehicle.GetMaxSpeed() * 0.1F
         && var3 / this.m_Vehicle.m_fCurrSpeed < Math.abs(Math.acos(var5)) * 28.645F / this.m_Vehicle.GetDefaultMaxTurnAngle()) {
         this.m_Vehicle.AccelerateRelease();
      } else {
         this.m_Vehicle.Accelerate();
      }
   }

   void SetTurningLevelModifier(int var1) {
      if (var1 < 0) {
         var1 = 0;
      }

      if (var1 > 5) {
         var1 = 5;
      }

      this.m_fCurrTurningLevelModifier = _AI_TURNING_MODIFIER[var1];
   }

   public void UseTrackingBonus() {
      this.DisableCallMethod("UseTrackingBonus");
      this.m_fModBonusSince = -1.0F;
      this.m_Vehicle.UseBonus();
      this.m_bUsingBonus = false;
   }

   void SetVehicleAndTrack(Vehicle var1, Track var2) {
      this.m_Track = var2;
      this.m_Vehicle = var1;
   }

   void TurnRight() {
      this.m_Vehicle.TurnRight();
   }

   float GetTurningLevelModifier() {
      return this.m_fCurrTurningLevelModifier;
   }

   void SetSlideModifier(int var1) {
      if (var1 < 0) {
         var1 = 0;
      }

      if (var1 > 5) {
         var1 = 5;
      }

      this.m_fCurrSlideModifier = _AI_SLIDE_MODIFIER[var1];
   }

   static float ProjVectorLen(Vector var0, Vector var1, Vector var2) {
      Vector var3 = Vector.newVector(var1);
      var3.Sub(var0);
      float var4 = var3.Len();
      var3.Normalize();
      var4 *= var3.Dot(var2);
      var3.Null();
      return var4;
   }

   public void GoRound() {
      this.DisableCallMethod("GoRound");
      this.m_Vehicle.Accelerate();
      this.m_Vehicle.TurnLeftRelease();
      this.m_Vehicle.TurnRightRelease();
      this.CallMethodIn("StopGoRound", 0.5F);
   }

   static Vector ProjVector(Vector var0, Vector var1, Vector var2) {
      Vector var3 = Vector.newVector(var1);
      var3.Sub(var0);
      float var4 = var3.Len();
      var3.Normalize();
      Vector var5 = new Vector(var2);
      var5.Mul(var3.Dot(var2) * var4);
      var3.Null();
      return var5;
   }

   void Start() {
      this.m_bMod = ModAI.IsImproved();
      if (this.m_bMod) {
         this.ModStart();
         return;
      }

      this.m_iCurrPoint = 1;
      this.m_CurrPoint.SetFast(this.m_Track.GetPoint(this.m_iCurrPoint));
      this.m_CurrPoint.SetGateWidth(_AI_PRECISION[this.m_iAILevel]);
      this.AddNoise(_AI_NOISE[this.m_iAILevel]);
      this.m_bMoving = true;
      this.m_Vehicle.CalculateVehicleInput(true);
      this.m_Vehicle.UpdateVehicleInput(true);
      this.m_Vehicle.Accelerate();
   }

   public void OnFrame() {
      if (this.m_bMod) {
         this.ModOnFrame();
         return;
      }

      if (this.m_bMoving && this.m_Vehicle.m_iCurrPhysics == 0) {
         if (!this.m_bIsGoingRound) {
            if (this.m_Vehicle.m_iCurrLoop == RacerModule.m_Mod.m_RaceParams.m_iNofLoops) {
               this.Stop();
            } else {
               if (!this.m_bUsingBonus && this.m_Vehicle.m_CurrentBonus != null && !this.m_Vehicle.m_CurrentBonus.m_bWasUsed) {
                  this.m_bUsingBonus = true;
                  this.CallMethodIn("UseBonus", 0.1F);
               }

               if (this.m_Vehicle.m_iAIUpdatePointNo != -1) {
                  Vector var1 = Vector.newVector(RacerModule.m_Mod.m_Track.GetPointPos(this.m_Vehicle.m_iAIUpdatePointNo));
                  Vector var2 = Vector.newVector(this.m_Track.GetPointPos(this.m_iCurrPoint));
                  if (var1.fX != var2.fX || var1.fZ != var2.fZ) {
                     this.m_iCurrPoint = this.m_Track.GetPrevPointNum(this.m_iCurrPoint);
                  }

                  var1.Null();
                  var2.Null();
                  this.ChooseNextPoint();
                  this.m_Vehicle.m_iAIUpdatePointNo = -1;
               }

               if (this.m_Vehicle.m_iAIUpdatePointForwardNo != -1) {
                  Vector var11 = Vector.newVector(RacerModule.m_Mod.m_Track.GetPointPos(this.m_Vehicle.m_iAIUpdatePointForwardNo));
                  Vector var13 = Vector.newVector(this.m_Track.GetPointPos(this.m_iCurrPoint));
                  boolean var3 = false;
                  if (var11.fX != var13.fX || var11.fZ != var13.fZ) {
                     var3 = true;
                     this.m_iCurrPoint = this.m_Track.GetNextPointNum(this.m_iCurrPoint);
                  }

                  if (var3) {
                     this.ChooseNextPoint();
                  }

                  this.m_Vehicle.m_iAIUpdatePointForwardNo = -1;
                  var11.Null();
                  var13.Null();
               }

               Vector var12 = this.m_Vehicle.GetPosition();
               var12.fY = 0.0F;
               Vector var14 = this.m_Vehicle.GetForwardVector();
               var14.fY = 0.0F;
               var14.Normalize();
               Vector var15 = Vector.newVector(this.m_CurrPoint.m_vPos);
               var15.Sub(var12);
               var15.Normalize();
               Vector var4 = Vector.newVector(this.m_CurrPoint.m_vRight);
               var4.Sub(this.m_CurrPoint.m_vPos);
               var4.Normalize();
               if (var4.Cross(var15).fY < 0.0F) {
                  this.m_iCurrPoint = this.m_Track.GetNextPointNum(this.m_iCurrPoint);
                  if (this.m_iCurrPoint != -1) {
                     this.ChooseNextPoint();
                     var15.Null();
                     var4.Null();
                  } else {
                     this.Stop();
                     var15.Null();
                     var4.Null();
                  }
               } else {
                  var15.Null();
                  var4.Null();
                  Vector var5 = Vector.newVector(this.m_CurrPoint.m_vPos);
                  var5.Sub(var12);
                  var5.fY = 0.0F;
                  var5.Normalize();
                  if (this.m_bCollectingBonus) {
                     Vector var6 = Vector.newVector(this.m_vBonusPos);
                     var6.Sub(var12);
                     var6.fY = 0.0F;
                     var6.Normalize();
                     float var7 = var5.Dot(var6);
                     if (var7 < 0.8F) {
                        this.m_bCollectingBonus = false;
                     } else {
                        var5.Set(var6);
                     }

                     var6.Null();
                  }

                  float var16 = var5.Dot(var14);
                  this.SlowOnCurve();
                  if (var16 < 0.999F) {
                     var16 = (float)Math.acos(var16) * 57.926F;
                     if (var14.Cross(var5).fY > 0.0F) {
                        var16 *= -1.0F;
                        this.TurnLeft();
                     } else {
                        this.TurnRight();
                     }

                     this.m_Vehicle.m_fAngleToTurn = var16;
                  } else {
                     this.m_Vehicle.TurnLeftRelease();
                     this.m_Vehicle.TurnRightRelease();
                     this.m_Vehicle.m_fAngleToTurn = 0.0F;
                  }

                  var5.Null();
                  if (!this.m_Vehicle.m_bStopped && !this.m_bIsGoingRound && this.m_Vehicle.m_iCurrPhysics == 0 && !this.m_bIsCounting) {
                     this.m_vLastPosition.Set(this.m_Vehicle.m_vVehiclePosition);
                     this.m_fStopTime = RacerModule.m_Mod.m_fNowTime;
                     this.m_bIsCounting = true;
                  }

                  boolean var18 = false;
                  if (this.m_bIsCounting && RacerModule.m_Mod.m_fNowTime > this.m_fStopTime + 3.0F) {
                     Vector var8 = Vector.newVector(this.m_Vehicle.m_vVehiclePosition);
                     var8.Sub(this.m_vLastPosition);
                     var8.fY = 0.0F;
                     if (var8.Len() < 1000.0F * this.m_Vehicle.m_fSlopeAccelCoef * VehicleParams._ANIMALS_ACCEL_COEFS[this.m_Vehicle.m_iAnimalID] * 0.75F) {
                        var18 = true;
                     }

                     this.m_bIsCounting = false;
                     var8.Null();
                  }

                  if (var18) {
                     this.m_bIsGoingRound = true;
                     int var19 = this.m_Track.GetPrevPointNum(this.m_iCurrPoint);
                     if (var19 != -1) {
                        Vector var9 = Vector.newVector(this.m_CurrPoint.m_vPos);
                        var9.Sub(this.m_Track.GetPointPos(var19));
                        var9.fY = 0.0F;
                        var9.Normalize();
                        Vector var10 = Vector.newVector(this.m_Vehicle.m_vVehiclePosition);
                        var10.Sub(this.m_Track.GetPointPos(var19));
                        var10.fY = 0.0F;
                        var10.Normalize();
                        if (var9.Cross(var10).fY > 0.0F) {
                           this.m_Vehicle.TurnRight();
                           this.m_Vehicle.m_fAngleToTurn = 90.0F;
                        } else {
                           this.m_Vehicle.TurnLeft();
                           this.m_Vehicle.m_fAngleToTurn = -90.0F;
                        }

                        var9.Null();
                        var10.Null();
                     } else if (RacerGame.GetRandom() < 0.5F) {
                        this.m_Vehicle.TurnLeft();
                        this.m_Vehicle.m_fAngleToTurn = -90.0F;
                     } else {
                        this.m_Vehicle.TurnRight();
                        this.m_Vehicle.m_fAngleToTurn = 90.0F;
                     }

                     this.m_Vehicle.Brake();
                     this.CallMethodIn("GoRound", 2.0F);
                  }
               }
            }
         }
      }
   }

   // ======================= [mod] IA améliorée =======================

   void ModStart() {
      if (!this.m_bModLevelSet && ModAI.IsFixedLevel()) {
         // circuit qui ne règle pas le niveau : la difficulté choisie s'applique quand même
         this.SetAILevel(ModAI.EffectiveLevel(this.m_iAILevel));
      }

      this.m_fModSkill = 0.85F + 0.3F * RacerGame.GetRandom();
      this.m_fModLane = 2.0F * RacerGame.GetRandom() - 1.0F;
      this.m_fModAggro = RacerGame.GetRandom();
      this.m_fModSpeed = 1.0F - RacerGame.GetRandom() * 0.015F;
      this.m_fModApexK = 0.75F + 0.5F * RacerGame.GetRandom();
      this.m_fModWideK = 0.5F + RacerGame.GetRandom();
      if (ModChrono.m_bOn) {
         // [mod] IA chrono : pas de hasard de conduite, les écarts de chrono viennent du plan (ModChrono)
         this.m_fModSkill = 1.0F;
         this.m_fModLane = 0.0F;
         this.m_fModAggro = 0.0F;
         this.m_fModSpeed = 1.0F;
         this.m_fModApexK = 1.0F;
         this.m_fModWideK = 1.0F;
      }

      this.ModApplyHandling();
      this.m_bModNextValid = false;
      this.m_iModNext = -1;
      this.m_fModAvoid = 0.0F;
      this.m_iModState = 0;
      this.m_iCurrPoint = 1;
      if (ModChrono.m_bOn) {
         ModChrono.Begin(this); // [mod] IA chrono : plan de cette voiture, voitures traversables
      }

      this.m_ModPlan = ModPlans.For(this); // [mod] plan chrono validé du circuit (IA expertes)
      this.m_fModPlanK = this.m_ModPlan != null ? ModPlans.SpeedK(this.m_ModPlan, this.m_Vehicle) : 1.0F;
      this.ModChooseNextPoint();
      this.m_bMoving = true;
      this.m_Vehicle.CalculateVehicleInput(true);
      this.m_Vehicle.UpdateVehicleInput(true);
      this.m_Vehicle.Accelerate();
      float now = RacerModule.m_Mod.m_fNowTime;
      // premier contrôle « coincée » un peu plus tard : la voiture part arrêtée
      this.ModResetStuckCheck(now + 1.5F);
      GameObject.CrashLog(
         "[mod] " + Rocket.ModName(this.m_Vehicle) + " : difficulte " + ModAI._MODE_NAMES[ModAI.Mode()] + " (niveau " + this.m_iAILevel
            + "), precision x" + ModAI.F(this.m_fModSkill) + ", couloir " + ModAI.F(this.m_fModLane) + ", agressivite "
            + ModAI.F(this.m_fModAggro) + ", vitesse x" + ModAI.F(this.m_fModSpeed * ModAI._MODE_SPEED[ModAI.Mode()]) + ", virage x"
            + ModAI.F(this.m_fCurrTurningLevelModifier) + ", derapage x" + ModAI.F(this.m_fCurrSlideModifier) + ", corde x"
            + ModAI.F(this.m_fModApexK) + ", largeur x" + ModAI.F(this.m_fModWideK)
            + (this.m_ModPlan != null ? ", PLAN CHRONO (gain " + this.m_ModPlan.m_sGain + " %, vitesses x" + ModAI.F(this.m_fModPlanK) + ")" : "") + "\n"
      );
   }

   // [mod] IA améliorée : la tenue de route du joueur (ModAI._AI_TURN / _AI_SLIDE), à tous les niveaux ; le niveau
   // ne joue que sur la conduite (trajectoire, précision, freinage, bonus)
   void ModApplyHandling() {
      this.m_fCurrTurningLevelModifier = ModAI._AI_TURN;
      this.m_fCurrSlideModifier = ModAI._AI_SLIDE;
   }

   // [mod] en ligne, hôte remplacé en course (RaceParams.ModGhostToAI) : Start() vise le point 1, celui du départ ;
   // ici, la voiture est déjà en pleine course. On vise le point de la piste des IA le plus proche, ou le suivant si
   // la voiture l'a déjà passé.
   void ModResume() {
      this.Start();
      int n = this.m_Track.m_TrackPoints.size();
      if (n < 2) {
         return;
      }

      float x = this.m_Vehicle.m_vVehiclePosition.fX;
      float z = this.m_Vehicle.m_vVehiclePosition.fZ;
      int best = 0;
      float fBest = Float.MAX_VALUE;

      for (int i = 0; i < n; i++) {
         Vector p = this.m_Track.GetPointPos(i);
         float dx = p.fX - x;
         float dz = p.fZ - z;
         float d = dx * dx + dz * dz;
         if (d < fBest) {
            fBest = d;
            best = i;
         }
      }

      int next = this.m_Track.GetNextPointNum(best);
      if (next >= 0) {
         Vector a = this.m_Track.GetPointPos(best);
         Vector b = this.m_Track.GetPointPos(next);
         if ((b.fX - a.fX) * (x - a.fX) + (b.fZ - a.fZ) * (z - a.fZ) > 0.0F) {
            best = next; // déjà passé : le suivant
         }
      }

      this.m_iCurrPoint = best;
      if (this.m_bMod) {
         this.m_bModNextValid = false;
         this.m_iModNext = -1;
         this.ModChooseNextPoint();
         this.ModResetStuckCheck(RacerModule.m_Mod.m_fNowTime + 1.5F);
      } else {
         this.m_CurrPoint.SetFast(this.m_Track.GetPoint(this.m_iCurrPoint));
         this.m_CurrPoint.SetGateWidth(_AI_PRECISION[this.m_iAILevel]);
         this.AddNoise(_AI_NOISE[this.m_iAILevel]);
      }

      GameObject.CrashLog("[mod] " + Rocket.ModName(this.m_Vehicle) + " : reprise en course, point " + best + " de " + n + "\n");
   }

   void ModResetStuckCheck(float fTime) {
      this.m_fModCheckTime = fTime;
      this.m_fModCheckX = this.m_Vehicle.m_vVehiclePosition.fX;
      this.m_fModCheckZ = this.m_Vehicle.m_vVehiclePosition.fZ;
      this.m_fModWrongSince = -1.0F;
   }

   // axe « droite » unitaire d'une porte, dans m_fModTmp
   void ModRightOf(TrackPoint p) {
      float x = p.m_vRight.fX - p.m_vPos.fX;
      float z = p.m_vRight.fZ - p.m_vPos.fZ;
      float l = (float)Math.sqrt(x * x + z * z);
      if (l > 0.001F) {
         this.m_fModTmp[0] = x / l;
         this.m_fModTmp[1] = z / l;
      } else {
         this.m_fModTmp[0] = 1.0F;
         this.m_fModTmp[1] = 0.0F;
      }
   }

   // sens du virage au point i, -1..1 : > 0 = la piste tourne du côté « droite » de la porte (la corde y est)
   float ModTurnSide(int i, float rx, float rz) {
      int n = this.m_Track.GetNextPointNum(i);
      if (n == -1) {
         return 0.0F;
      }

      Vector p = this.m_Track.GetPointPos(i);
      Vector q = this.m_Track.GetPointPos(n);
      float d2x = q.fX - p.fX;
      float d2z = q.fZ - p.fZ;
      float l = (float)Math.sqrt(d2x * d2x + d2z * d2z);
      if (l < 0.001F) {
         return 0.0F;
      }

      d2x /= l;
      d2z /= l;
      float d1x = d2x;
      float d1z = d2z;
      int pr = this.m_Track.GetPrevPointNum(i);
      if (pr != -1) {
         Vector o = this.m_Track.GetPointPos(pr);
         d1x = p.fX - o.fX;
         d1z = p.fZ - o.fZ;
         l = (float)Math.sqrt(d1x * d1x + d1z * d1z);
         if (l > 0.001F) {
            d1x /= l;
            d1z /= l;
         } else {
            d1x = d2x;
            d1z = d2z;
         }
      }

      float t = ((d2x - d1x) * rx + (d2z - d1z) * rz) / _MOD_INSIDE_FULL;
      return t < -1.0F ? -1.0F : (t > 1.0F ? 1.0F : t);
   }

   // décalage latéral de la cible à la porte i (> 0 vers la droite de la porte) : corde modérée au virage, extérieur
   // avant et après (entrée et sortie larges), couloir de l'IA en ligne droite, un peu de hasard selon le niveau
   float ModLineOffset(int i) {
      if (ModChrono.m_bOn) {
         // [mod] IA chrono : le décalage du plan que cette voiture essaie
         float o = ModChrono.Offset(this, i);
         if (o == o) {
            return o;
         }
      }

      if (this.m_ModPlan != null && i >= 0 && i < this.m_ModPlan.m_aOff.length) {
         // [mod] plan chrono (ModPlans) + le petit couloir de cette IA, dans le couloir libre de la carte ; point
         // dangereux relevé pendant la course : vers le milieu du couloir, comme sans plan
         float po = ModAIMap.Clamp(i, this.m_ModPlan.m_aOff[i] + this.m_fModLane * ModPlans._SPREAD);
         float pm = ModAIMap.Mid(i);
         return pm + (po - pm) * (1.0F - this.ModDanger(i));
      }

      TrackPoint p = this.m_Track.GetPoint(i);
      this.ModRightOf(p);
      float rx = this.m_fModTmp[0];
      float rz = this.m_fModTmp[1];
      float side = this.ModTurnSide(i, rx, rz);
      float q = _MOD_APEX + _MOD_APEX_LEVEL * this.m_iAILevel;
      // virages des portes voisines, vus dans l'axe de celle-ci : on s'écarte de leur côté intérieur
      float around = 0.0F;
      int n = this.m_Track.GetNextPointNum(i);
      if (n != -1) {
         around += this.ModTurnSide(n, rx, rz);
      }

      n = this.m_Track.GetPrevPointNum(i);
      if (n != -1) {
         around += this.ModTurnSide(n, rx, rz);
      }

      if (around > 1.0F) {
         around = 1.0F;
      } else if (around < -1.0F) {
         around = -1.0F;
      }

      float straight = 1.0F - Math.abs(side);
      float f = this.m_fModLane * _MOD_LANE * straight * (1.0F - Math.abs(around)) + side * q * this.m_fModApexK
         - around * _MOD_WIDE * this.m_fModWideK * straight;
      if (!ModChrono.m_bOn) {
         f += (2.0F * RacerGame.GetRandom() - 1.0F) * _AI_NOISE[this.m_iAILevel] * _MOD_NOISE * this.m_fModSkill;
      }

      if (f > 1.0F) {
         f = 1.0F;
      } else if (f < -1.0F) {
         f = -1.0F;
      }

      // carte du circuit : dans le couloir libre de la porte ; point dangereux : vers le milieu de ce couloir
      float use = ModAIMap.Known(i) ? _MOD_MAP_LINE_USE : _MOD_LINE_USE;
      float off = f > 0.0F ? f * p.m_fRWidth * use : f * p.m_fLWidth * use;
      off = ModAIMap.Clamp(i, off);
      float mid = ModAIMap.Mid(i);
      return mid + (off - mid) * (1.0F - this.ModDanger(i));
   }

   void ModChooseNextPoint() {
      TrackPoint p = this.m_Track.GetPoint(this.m_iCurrPoint);
      this.m_CurrPoint.SetFast(p);
      this.ModRightOf(p);
      this.m_fModGateRX = this.m_fModTmp[0];
      this.m_fModGateRZ = this.m_fModTmp[1];
      this.m_fModGateCX = p.m_vPos.fX;
      this.m_fModGateCZ = p.m_vPos.fZ;
      this.m_fModGateLW = p.m_fLWidth;
      this.m_fModGateRW = p.m_fRWidth;
      // le point visé en regardant devant devient la cible : même décalage, pas de saut
      float off = this.m_bModNextValid && this.m_iModNext == this.m_iCurrPoint ? this.m_fModNextOff : this.ModLineOffset(this.m_iCurrPoint);
      this.m_fModGateOff = off;
      this.m_CurrPoint.m_vPos.fX = this.m_fModGateCX + this.m_fModGateRX * off;
      this.m_CurrPoint.m_vPos.fZ = this.m_fModGateCZ + this.m_fModGateRZ * off;
      this.m_CurrPoint.m_vPos.fY = 0.0F;
      this.m_iModNext = this.m_Track.GetNextPointNum(this.m_iCurrPoint);
      this.m_bModNextValid = this.m_iModNext != -1;
      if (this.m_bModNextValid) {
         TrackPoint n = this.m_Track.GetPoint(this.m_iModNext);
         this.m_fModNextOff = this.ModLineOffset(this.m_iModNext);
         this.ModRightOf(n);
         this.m_fModNextX = n.m_vPos.fX + this.m_fModTmp[0] * this.m_fModNextOff;
         this.m_fModNextZ = n.m_vPos.fZ + this.m_fModTmp[1] * this.m_fModNextOff;
      }

      // bonus : la boîte la plus proche de la trajectoire (l'original en tirait une au hasard)
      if (this.m_Vehicle.m_CurrentBonus == null) {
         this.m_bCollectingBonus = false;
         if (p.m_Bonuses != null && RacerGame.GetRandom() <= _AI_BONUS_COLLECT[this.m_iAILevel]) {
            BonusItem best = null;
            float fBest = 1.0E9F;

            for (int k = 0; k < p.m_Bonuses.size(); k++) {
               BonusItem b = (BonusItem)p.m_Bonuses.elementAt(k);
               if (!b.m_fWasCollected && b.m_vDefaultPos != null) {
                  float lat = (b.m_vDefaultPos.fX - this.m_fModGateCX) * this.m_fModGateRX + (b.m_vDefaultPos.fZ - this.m_fModGateCZ) * this.m_fModGateRZ - off;
                  if (lat < 0.0F) {
                     lat = -lat;
                  }

                  if (lat < fBest) {
                     fBest = lat;
                     best = b;
                  }
               }
            }

            if (best != null) {
               this.m_bCollectingBonus = true;
               this.m_vBonusPos.Set(best.m_vDefaultPos);
            }
         }
      }
   }

   void ModOnFrame() {
      if (!this.m_bMoving || this.m_Vehicle.m_iCurrPhysics != 0) {
         return;
      }

      Vehicle v = this.m_Vehicle;
      if (v.m_iCurrLoop == RacerModule.m_Mod.m_RaceParams.m_iNofLoops) {
         this.Stop();
         this.ModLogResult();
         return;
      }

      float now = RacerModule.m_Mod.m_fNowTime;
      float dt = RacerModule.m_Mod.m_fFrameTime;
      if (!v.m_bIsOnTrack) {
         this.m_fModOffTrack += dt;
      }

      boolean bHas = v.m_CurrentBonus != null;
      if (bHas && !this.m_bModHadBonus) {
         this.m_nModBonusGot++;
         this.m_fModBonusSince = now;
      }

      this.m_bModHadBonus = bHas;
      if (!this.m_bUsingBonus && v.m_CurrentBonus != null && !v.m_CurrentBonus.m_bWasUsed) {
         this.m_bUsingBonus = true;
         this.CallMethodIn("UseBonus", 0.1F);
      }

      // remise en piste (chute, sortie) : comme l'original, on recale le point visé
      if (v.m_iAIUpdatePointNo != -1) {
         Vector a = RacerModule.m_Mod.m_Track.GetPointPos(v.m_iAIUpdatePointNo);
         Vector b = this.m_Track.GetPointPos(this.m_iCurrPoint);
         if (a != null && b != null && (a.fX != b.fX || a.fZ != b.fZ)) {
            int n = this.m_Track.GetPrevPointNum(this.m_iCurrPoint);
            if (n != -1) {
               this.m_iCurrPoint = n;
            }
         }

         this.m_bModNextValid = false;
         this.ModChooseNextPoint();
         v.m_iAIUpdatePointNo = -1;
         this.m_iModState = 0;
         this.ModResetStuckCheck(now + 1.5F);
      }

      if (v.m_iAIUpdatePointForwardNo != -1) {
         Vector a = RacerModule.m_Mod.m_Track.GetPointPos(v.m_iAIUpdatePointForwardNo);
         Vector b = this.m_Track.GetPointPos(this.m_iCurrPoint);
         if (a != null && b != null && (a.fX != b.fX || a.fZ != b.fZ)) {
            int n = this.m_Track.GetNextPointNum(this.m_iCurrPoint);
            if (n != -1) {
               this.m_iCurrPoint = n;
               this.ModChooseNextPoint();
            }
         }

         v.m_iAIUpdatePointForwardNo = -1;
      }

      float cx = v.m_vVehiclePosition.fX;
      float cz = v.m_vVehiclePosition.fZ;
      float tx = this.m_CurrPoint.m_vPos.fX - cx;
      float tz = this.m_CurrPoint.m_vPos.fZ - cz;
      // porte franchie (même test que l'original : la cible est passée derrière la ligne de la porte)
      if (this.m_fModGateRZ * tx - this.m_fModGateRX * tz < 0.0F) {
         this.ModOnGatePassed(this.m_iCurrPoint, now);
         this.m_iCurrPoint = this.m_Track.GetNextPointNum(this.m_iCurrPoint);
         if (this.m_iCurrPoint != -1) {
            this.ModChooseNextPoint();
         } else {
            this.Stop();
         }

         return;
      }

      float fx = v.m_vVehicleForward.fX;
      float fz = v.m_vVehicleForward.fZ;
      float l = (float)Math.sqrt(fx * fx + fz * fz);
      if (l > 0.001F) {
         fx /= l;
         fz /= l;
      }

      float d0 = (float)Math.sqrt(tx * tx + tz * tz);
      float speed = v.m_fCurrSpeed;
      // regard vers le point suivant : en approchant de la porte, la cible glisse vers la suivante
      float ax = this.m_CurrPoint.m_vPos.fX;
      float az = this.m_CurrPoint.m_vPos.fZ;
      if (this.m_bModNextValid && speed > 0.0F) {
         float fLook = speed * (_MOD_LOOK_TIME + 0.04F * this.m_iAILevel);
         if (d0 < fLook) {
            float a = Math.abs(this.m_CurrPoint.m_fAngle);
            if (a > _MOD_LOOK_ANGLE) {
               a = _MOD_LOOK_ANGLE;
            }

            float blend = (1.0F - d0 / fLook) * _MOD_LOOK_BLEND * (1.0F - a / _MOD_LOOK_ANGLE) * (1.0F - this.ModDanger(this.m_iCurrPoint));
            ax += (this.m_fModNextX - ax) * blend;
            az += (this.m_fModNextZ - az) * blend;
         }
      }

      this.ModAvoid(cx, cz, fx, fz, speed, dt);
      ax += this.m_fModGateRX * this.m_fModAvoid;
      az += this.m_fModGateRZ * this.m_fModAvoid;
      float dx = ax - cx;
      float dz = az - cz;
      l = (float)Math.sqrt(dx * dx + dz * dz);
      if (l > 0.001F) {
         dx /= l;
         dz /= l;
      }

      if (this.m_bCollectingBonus) {
         float bx = this.m_vBonusPos.fX - cx;
         float bz = this.m_vBonusPos.fZ - cz;
         l = (float)Math.sqrt(bx * bx + bz * bz);
         if (l > 0.001F) {
            bx /= l;
            bz /= l;
         }

         if (dx * bx + dz * bz < (this.m_iAILevel >= 3 ? 0.75F : 0.8F)) {
            this.m_bCollectingBonus = false;
         } else {
            dx = bx;
            dz = bz;
         }
      }

      if (this.ModStuck(now, cx, cz, fx, fz, dx, dz)) {
         return;
      }

      float dot = fx * dx + fz * dz;
      float ang = 0.0F;
      if (dot < 0.9995F) {
         ang = (float)Math.acos(dot < -1.0F ? -1.0F : dot) * _MOD_RAD;
         // volant progressif : seul l'effet de dérapage dépend du braquage, la direction tourne de l'angle demandé
         float k = ang / _MOD_FULL_STEER;
         if (k > 1.0F) {
            k = 1.0F;
         } else if (k < 0.2F) {
            k = 0.2F;
         }

         if (fz * dx - fx * dz > 0.0F) {
            ang = -ang;
            v.TurnLeftAnalog(k);
         } else {
            v.TurnRightAnalog(k);
         }

         v.m_fAngleToTurn = ang;
      } else {
         v.TurnLeftRelease();
         v.TurnRightRelease();
         v.m_fAngleToTurn = 0.0F;
      }

      this.m_fModAngle = ang;
      this.ModSpeedControl(d0, Math.abs(ang), speed);
   }

   // lâcher l'accélérateur ou freiner si le virage qui vient ne passe pas à cette vitesse
   void ModSpeedControl(float d0, float a0, float speed) {
      Vehicle v = this.m_Vehicle;
      float vmax = v.GetMaxSpeed();
      float turn = v.GetDefaultMaxTurnAngle();
      float r = 99.0F;
      float caution = _MOD_CAUTION * (1.1F - _MOD_CAUTION_LEVEL * this.m_iAILevel) * (1.0F + _MOD_DANGER_CAUTION * Math.max(this.ModDanger(this.m_iCurrPoint), this.ModDanger(this.m_iModNext)));
      if (ModChrono.m_bOn) {
         caution *= ModChrono.Caution(this, this.m_iCurrPoint); // [mod] IA chrono : prudence du plan à cette porte
      } else if (this.m_ModPlan != null && this.m_iCurrPoint >= 0 && this.m_iCurrPoint < this.m_ModPlan.m_aCaut.length) {
         caution *= this.m_ModPlan.m_aCaut[this.m_iCurrPoint]; // [mod] plan chrono (ModPlans) : prudence apprise
      }
      if (speed > vmax * 0.15F && !v.m_bTurbo && turn > 0.0F) {
         float need = a0 / turn * caution;
         if (need > 0.0001F) {
            r = d0 / speed / need;
         }

         if (this.m_bModNextValid) {
            float sx = this.m_fModNextX - this.m_CurrPoint.m_vPos.fX;
            float sz = this.m_fModNextZ - this.m_CurrPoint.m_vPos.fZ;
            float seg = (float)Math.sqrt(sx * sx + sz * sz);
            need = (a0 + Math.abs(this.m_CurrPoint.m_fAngle)) / turn * caution;
            if (need > 0.0001F && (d0 + 0.5F * seg) / speed / need < r) {
               r = (d0 + 0.5F * seg) / speed / need;
            }
         }
      }

      // [mod] vitesse de passage apprise à la porte visée (et à la suivante quand elle est proche)
      float lim = this.ModSpeedLimit(this.m_iCurrPoint);
      if (this.m_bModNextValid && d0 < 1200.0F) {
         lim = Math.min(lim, this.ModSpeedLimit(this.m_iModNext));
      }

      this.m_bModOverSpeed = lim < 1.0F && speed > lim * vmax;
      if (this.m_bModOverSpeed && r >= 1.0F) {
         r = 0.99F;
      }

      boolean bOverBrake = lim < 1.0F && speed > lim * vmax * _MOD_OVER_BRAKE && !v.m_bTurbo;
      if ((r < _MOD_BRAKE_RATIO || bOverBrake) && speed > vmax * _MOD_BRAKE_MIN_SPEED && v.m_bIsOnTrack) {
         if (!this.m_bModBraking) {
            this.m_nModBrakes++;
            this.m_bModBraking = true;
         }

         v.Brake();
      } else {
         if (this.m_bModBraking) {
            v.BrakeRelease();
            this.m_bModBraking = false;
         }

         if (r < 1.0F) {
            v.AccelerateRelease();
         } else {
            v.Accelerate();
         }
      }
   }

   // vitesse de passage permise à la porte i : apprise (aimap.dat), ou celle du plan en IA chrono
   float ModSpeedLimit(int i) {
      if (!ModChrono.m_bOn && this.m_ModPlan != null && i >= 0 && i < this.m_ModPlan.m_aLim.length) {
         // [mod] plan chrono (ModPlans) : même vitesse réelle de passage que l'animal entraîné
         float l = this.m_ModPlan.m_aLim[i];
         return l >= 1.0F ? 1.0F : Math.min(1.0F, l * this.m_fModPlanK);
      }

      return ModChrono.m_bOn ? ModChrono.Limit(this, i) : ModAIMap.Speed(this.m_Track, i);
   }

   // se décaler si une voiture plus lente est juste devant, dans le même couloir
   void ModAvoid(float cx, float cz, float fx, float fz, float speed, float dt) {
      if (ModChrono.m_bOn) {
         this.m_fModAvoid = 0.0F; // [mod] IA chrono : les voitures se traversent
         return;
      }

      float want = 0.0F;
      float fBest = _MOD_AVOID_DIST;
      java.util.Vector cars = RacerModule.m_Mod.m_RaceParams.m_Vehicles;

      for (int i = 0; i < cars.size(); i++) {
         Vehicle o = (Vehicle)cars.elementAt(i);
         if (o != this.m_Vehicle && !o.m_bRaceFinished) {
            float rx = o.m_vVehiclePosition.fX - cx;
            float rz = o.m_vVehiclePosition.fZ - cz;
            float ahead = rx * fx + rz * fz;
            if (ahead > 0.0F && ahead < fBest) {
               float lat = rx * this.m_fModGateRX + rz * this.m_fModGateRZ;
               if (Math.abs(lat) < _MOD_AVOID_WIDTH && (ahead < 300.0F || o.m_fCurrSpeed < speed * 0.97F)) {
                  fBest = ahead;
                  float push = _MOD_AVOID_PUSH * (0.5F + 0.5F * (1.0F - ahead / _MOD_AVOID_DIST));
                  // on garde le côté déjà pris (pas de va-et-vient quand l'autre est pile devant)
                  int side;
                  if (this.m_fModAvoid > 20.0F) {
                     side = 1;
                  } else if (this.m_fModAvoid < -20.0F) {
                     side = -1;
                  } else {
                     side = lat >= 0.0F ? -1 : 1;
                  }

                  want = side * push;
               }
            }
         }
      }

      if (want != 0.0F) {
         // rester sur la piste : sinon passer de l'autre côté, sinon s'arrêter au bord
         float lo = -this.m_fModGateLW * 0.85F;
         float hi = this.m_fModGateRW * 0.85F;
         if (ModAIMap.Known(this.m_iCurrPoint)) {
            // [mod] carte du circuit : pas d'évitement vers un arbre ou un mur
            lo = ModAIMap.m_aLo[this.m_iCurrPoint];
            hi = ModAIMap.m_aHi[this.m_iCurrPoint];
         }
         float t = this.m_fModGateOff + want;
         if (t < lo || t > hi) {
            float alt = this.m_fModGateOff - want;
            if (alt >= lo && alt <= hi) {
               want = -want;
            } else {
               want = (t < lo ? lo : hi) - this.m_fModGateOff;
            }
         }

         if (!this.m_bModAvoiding) {
            this.m_nModAvoid++;
            this.m_bModAvoiding = true;
         }
      } else {
         this.m_bModAvoiding = false;
      }

      float k = dt * _MOD_AVOID_RATE;
      if (k > 1.0F) {
         k = 1.0F;
      }

      this.m_fModAvoid += (want - this.m_fModAvoid) * k;
   }

   // coincée (presque immobile) ou à contresens : marche arrière en tournant le nez vers la cible, puis repart ;
   // 3 fois de suite au même point -> remise en piste. Renvoie vrai quand elle a réglé les commandes elle-même.
   boolean ModStuck(float now, float cx, float cz, float fx, float fz, float dx, float dz) {
      Vehicle v = this.m_Vehicle;
      if (v.m_bStopped) {
         // arrêtée par un bonus : rien à dégager
         this.m_iModState = 0;
         this.ModResetStuckCheck(now + _MOD_STUCK_CHECK);
         return false;
      }

      float dot = fx * dx + fz * dz;
      if (this.m_iModState == 1) {
         float t = now - this.m_fModStateTime;
         float ang = (float)Math.acos(dot > 1.0F ? 1.0F : (dot < -1.0F ? -1.0F : dot)) * _MOD_RAD;
         if (t > _MOD_REVERSE_MAX || t > _MOD_REVERSE_MIN && ang < 30.0F) {
            this.m_iModState = 0;
            v.BrakeRelease();
            v.Accelerate();
            this.ModResetStuckCheck(now + 2.5F);
            return false;
         }

         v.Brake();
         v.TurnLeftRelease();
         v.TurnRightRelease();
         if (fz * dx - fx * dz > 0.0F) {
            ang = -ang;
         }

         // en marche arrière, Vehicle.TurnVehicleUsingAngle inverse le sens de rotation : on inverse l'angle
         v.m_fAngleToTurn = v.m_fCurrSpeed < 0.0F ? -ang : 0.0F;
         return true;
      }

      if (dot < -0.2F && v.m_fCurrSpeed > 0.0F) {
         if (this.m_fModWrongSince < 0.0F) {
            this.m_fModWrongSince = now;
         } else if (now - this.m_fModWrongSince > _MOD_WRONG_WAY_TIME) {
            return this.ModStartReverse(now, "a contresens");
         }
      } else {
         this.m_fModWrongSince = -1.0F;
      }

      if (now >= this.m_fModCheckTime + _MOD_STUCK_CHECK) {
         float mx = cx - this.m_fModCheckX;
         float mz = cz - this.m_fModCheckZ;
         float moved = (float)Math.sqrt(mx * mx + mz * mz);
         float thr = _MOD_STUCK_MOVE * v.m_fSlopeAccelCoef * VehicleParams._ANIMALS_ACCEL_COEFS[v.m_iAnimalID];
         this.m_fModCheckTime = now;
         this.m_fModCheckX = cx;
         this.m_fModCheckZ = cz;
         if (moved < thr && now - v.m_fRespawnTime > 2.0F) {
            return this.ModStartReverse(now, "coincee");
         }
      }

      return false;
   }

   // porte i franchie : vitesse notée ; la porte d'il y a deux portes est passée sans choc depuis -> limite remontée
   void ModOnGatePassed(int i, float now) {
      int n = this.m_Track.m_TrackPoints.size();
      if (this.m_aModPassSpeed == null || this.m_aModPassSpeed.length != n) {
         this.m_aModPassSpeed = new float[n];
         this.m_aModPassTime = new float[n];
         for (int j = 0; j < n; j++) {
            this.m_aModPassTime[j] = -100.0F;
         }
      }

      if (i < 0 || i >= n) {
         return;
      }

      float vmax = this.m_Vehicle.GetMaxSpeed();
      this.m_aModPassSpeed[i] = vmax > 0.0F ? Math.abs(this.m_Vehicle.m_fCurrSpeed) / vmax : 0.0F;
      this.m_aModPassTime[i] = now;
      if (ModChrono.m_bOn) {
         ModChrono.OnGate(this, i, now); // [mod] IA chrono : temps des tours lancés ; rien d'appris dans aimap.dat
         return;
      }

      int k = this.m_Track.GetPrevPointNum(this.m_Track.GetPrevPointNum(i));
      if (k >= 0 && k < n && now - this.m_aModPassTime[k] < _MOD_PASS_RECENT * 2.0F && this.m_fModLastObstacle < this.m_aModPassTime[k]) {
         ModAIMap.RelaxSpeed(this.m_Track, k);
      }
   }

   // Vehicle.Respawn pose la voiture à la porte (courante - 2) : on avance la porte courante de 3 pour qu'elle
   // reparte de la porte d'après, vers la suivante. Jamais par-dessus la ligne d'arrivée (le compte des tours).
   boolean ModRespawnAhead() {
      Vehicle v = this.m_Vehicle;
      Track t = RacerModule.m_Mod.m_Track;
      int k = v.m_iCurrTrackPoint;

      for (int j = 0; j < 3; j++) {
         int nx = t.GetNextPointNum(k);
         if (nx <= k) {
            return false;
         }

         k = nx;
      }

      v.m_iCurrTrackPoint = k;
      return true;
   }

   boolean ModStartReverse(float now, String sWhy) {
      this.m_nModStuck++;
      if (ModChrono.m_bOn) {
         ModChrono.OnHit(this, this.m_iCurrPoint, 3); // [mod] IA chrono : blocage, endroit à retravailler
      }

      this.ModMarkDanger(_MOD_DANGER_STUCK);
      if (this.m_iModStuckPoint == this.m_iCurrPoint && now - this.m_fModLastStuck < _MOD_STUCK_FORGET) {
         this.m_iModStuckRepeat++;
      } else {
         this.m_iModStuckPoint = this.m_iCurrPoint;
         this.m_iModStuckRepeat = 1;
      }

      this.m_fModLastStuck = now;
      if (this.m_iModStuckRepeat >= _MOD_RESPAWN_AFTER) {
         this.m_iModStuckRepeat = 0;
         this.m_nModRespawn++;
         // [mod] déjà remise en piste ici : Vehicle.Respawn la reposerait 2 portes avant, sur le même obstacle, en boucle
         // jusqu'à la fin de la course (variantes en sens inverse : 39 remises en une course). On la pose après.
         boolean bAgain = this.m_iModRespawnPoint == this.m_iCurrPoint && now - this.m_fModRespawnAt < _MOD_RESPAWN_AGAIN;
         boolean bAhead = bAgain && this.ModRespawnAhead();
         this.m_iModRespawnPoint = this.m_iCurrPoint;
         this.m_fModRespawnAt = now;
         this.m_Vehicle.m_bMustRespawn = true;
         this.ModResetStuckCheck(now + 2.5F);
         Vector p = this.m_Vehicle.m_vVehiclePosition;
         GameObject.CrashLog(
            "[mod] " + Rocket.ModName(this.m_Vehicle) + " : " + sWhy + " au point " + this.m_iCurrPoint + ", remise en piste"
               + (bAhead ? " apres l'obstacle" : (bAgain ? " (pres de la ligne : au meme endroit)" : "")) + ", position " + Math.round(p.fX) + ","
               + Math.round(p.fY) + "," + Math.round(p.fZ) + " (sol " + Math.round(RacerModule.m_Mod.TerrainHeight(p.fX, p.fZ)) + ")\n"
         );
         return false;
      }

      this.m_iModState = 1;
      this.m_fModStateTime = now;
      this.m_fModWrongSince = -1.0F;
      return true;
   }

   // bonus : projectiles dès qu'une cible est verrouillée (délai du niveau, plus court si agressive) ; pièges
   // lâchés quand une voiture suit de près ; turbo gardé pour une ligne droite ; protection et pointes tout de suite
   void ModUseBonus() {
      this.DisableCallMethod("UseBonus");
      Vehicle v = this.m_Vehicle;
      UseItem b = v.m_CurrentBonus;
      if (b == null || b.m_bWasUsed) {
         this.m_bUsingBonus = false;
         return;
      }

      float now = RacerModule.m_Mod.m_fNowTime;
      if (this.m_fModBonusSince < 0.0F) {
         this.m_fModBonusSince = now;
      }

      float held = now - this.m_fModBonusSince;
      boolean go;
      switch (b.m_UseType) {
         case 1:
         case 5:
         case 7:
         case 10:
         case 11:
         case 12:
            go = v.m_bIsOnTrack && (this.ModCarBehind() || held > 6.0F + 8.0F * (1.0F - this.m_fModAggro));
            break;
         case 2:
            float a = Math.abs(this.m_fModAngle);
            go = v.m_bIsOnTrack && (a < _MOD_TURBO_ANGLE && this.ModStraightAhead() || held > 10.0F && a < _USE_TURBO_ANGLE);
            break;
         case 3:
         case 6:
         case 9:
            go = true;
            break;
         default:
            if (v.m_LockedObject != null) {
               this.m_nModBonusUsed++;
               this.CallMethodIn("UseTrackingBonus", _AI_BONUS_USE_TIME[this.m_iAILevel] * (1.3F - 0.6F * this.m_fModAggro));
               return;
            }

            go = false;
      }

      if (go) {
         v.UseBonus();
         this.m_bUsingBonus = false;
         this.m_fModBonusSince = -1.0F;
         this.m_nModBonusUsed++;
      } else {
         this.CallMethodIn("UseBonus", 0.2F);
      }
   }

   boolean ModCarBehind() {
      Vehicle v = this.m_Vehicle;
      float fx = v.m_vVehicleForward.fX;
      float fz = v.m_vVehicleForward.fZ;
      float l = (float)Math.sqrt(fx * fx + fz * fz);
      if (l > 0.001F) {
         fx /= l;
         fz /= l;
      }

      java.util.Vector cars = RacerModule.m_Mod.m_RaceParams.m_Vehicles;

      for (int i = 0; i < cars.size(); i++) {
         Vehicle o = (Vehicle)cars.elementAt(i);
         if (o != v && !o.m_bRaceFinished) {
            float rx = o.m_vVehiclePosition.fX - v.m_vVehiclePosition.fX;
            float rz = o.m_vVehiclePosition.fZ - v.m_vVehiclePosition.fZ;
            float back = -(rx * fx + rz * fz);
            float lat = Math.abs(rx * fz - rz * fx);
            if (back > 0.0F && back < _MOD_BACK_DIST && lat < _MOD_BACK_WIDTH) {
               return true;
            }
         }
      }

      return false;
   }

   boolean ModStraightAhead() {
      if (Math.abs(this.m_CurrPoint.m_fAngle) >= _MOD_STRAIGHT_ANGLE) {
         return false;
      }

      return !this.m_bModNextValid || Math.abs(this.m_Track.GetPoint(this.m_iModNext).m_fAngle) < _MOD_STRAIGHT_ANGLE;
   }

   // bilan de la course dans crash.log, une fois à l'arrivée
   void ModLogResult() {
      if (this.m_bModLogged) {
         return;
      }

      this.m_bModLogged = true;
      Vehicle v = this.m_Vehicle;
      int nRank = v.m_iFinishRank >= 0 ? v.m_iFinishRank + 1 : v.m_iCurrPlace + 1;
      GameObject.CrashLog(
         "[mod] " + Rocket.ModName(v) + " arrivee " + nRank + "e : hors piste " + ModAI.F(this.m_fModOffTrack) + " s, freinages " + this.m_nModBrakes
            + ", evitements " + this.m_nModAvoid + ", coincee " + this.m_nModStuck + " fois, remise en piste " + this.m_nModRespawn + ", bonus "
            + this.m_nModBonusGot + " ramasses / " + this.m_nModBonusUsed + " utilises, chocs contre le decor " + this.m_nModObstacles
            + ", points dangereux connus " + ModDangerCount() + "\n"
      );
   }

   // [mod] fin de course (RacerGame.EndModule, arrivée ou Échap) : ce que les IA ont appris, dans aimap.dat
   static void ModSaveMemory() {
      if (ModChrono.m_bOn) {
         return; // [mod] IA chrono : la mémoire des IA du jeu (aimap.dat) n'est pas touchée
      }

      if (RacerModule.m_Mod == null || ModAIMap.m_Track == null || ModAIMap.m_Track != RacerModule.m_Mod.m_AITrack) {
         return; // pas de carte pour ce circuit (pas d'IA améliorée)
      }

      RaceParams rp = RacerModule.m_Mod.m_RaceParams;
      ModAIMap.SaveRace(m_ModDangerRace == rp ? m_aModDanger : null);
   }

   // --- [mod] points dangereux appris en course ---

   // la table de la course en cours (refaite à chaque nouvelle course)
   static float[] ModDangerTable(Track t) {
      RaceParams rp = RacerModule.m_Mod != null ? RacerModule.m_Mod.m_RaceParams : null;
      int n = t != null ? t.m_TrackPoints.size() : 0;
      if (m_aModDanger == null || m_ModDangerRace != rp || m_aModDanger.length != n) {
         // [mod] repart des dangers appris aux courses d'avant sur ce circuit (aimap.dat)
         m_aModDanger = ModAIMap.StartDanger(t);
         m_ModDangerRace = rp;
         m_nModHitLogs = 0;
      }

      return m_aModDanger;
   }

   // appris aux courses d'avant (ModAIMap) + relevé pendant celle-ci
   float ModDanger(int i) {
      if (ModChrono.m_bOn) {
         return 0.0F; // [mod] IA chrono : le plan décide seul (pas de points dangereux, qui changent en course)
      }

      float[] a = ModDangerTable(this.m_Track);
      if (this.m_ModPlan != null) {
         // [mod] plan chrono (ModPlans) : les dangers appris sont déjà dans sa prudence ; seulement ceux de la course
         return i >= 0 && i < a.length ? Math.min(1.0F, a[i]) : 0.0F;
      }

      return i >= 0 && i < a.length ? Math.min(1.0F, ModAIMap.Saved(this.m_Track, i) + a[i]) : 0.0F;
   }

   static int ModDangerCount() {
      int n = 0;

      for (int i = 0; m_aModDanger != null && i < m_aModDanger.length; i++) {
         if (ModAIMap.Saved(ModAIMap.m_Track, i) + m_aModDanger[i] >= 0.1F) {
            n++;
         }
      }

      return n;
   }

   // le point visé (là où ça s'est mal passé) et le précédent (d'où l'on arrivait trop serré). La table est commune
   // à toutes les IA : un incident compte pour 1/(nombre d'IA), une porte n'atteint 1 que si toutes s'y plantent
   // (2026-11-20 : à +0,5 par choc et par IA, tout le circuit était « dangereux » en une course).
   void ModMarkDanger(float f) {
      float[] a = ModDangerTable(this.m_Track);
      int nAI = RacerModule.m_Mod != null ? RacerModule.m_Mod.m_RaceParams.m_AITab.size() : 1;
      f /= Math.max(1, nAI);
      int i = this.m_iCurrPoint;
      if (i >= 0 && i < a.length) {
         a[i] = Math.min(1.0F, a[i] + f);
         int pr = this.m_Track.GetPrevPointNum(i);
         if (pr >= 0 && pr < a.length) {
            a[pr] = Math.min(1.0F, a[pr] + f * 0.6F);
         }
      }
   }

   // Vehicle : choc contre un arbre, une enclume (OnCollisionFullInfoMove), un mur ou une maison (rebond sur une
   // surface raide). vNormal : normale du choc (de l'obstacle vers la voiture), null si inconnue.
   void ModOnObstacle(Vector vNormal) {
      if (!this.m_bMod) {
         return;
      }

      float now = RacerModule.m_Mod.m_fNowTime;
      if (now - this.m_fModLastObstacle < 0.5F) {
         return;
      }

      this.m_fModLastObstacle = now;
      this.m_nModObstacles++;
      int i = this.m_iCurrPoint;
      float x = this.m_Vehicle.m_vVehiclePosition.fX;
      float z = this.m_Vehicle.m_vVehiclePosition.fZ;
      ModTraining.OnHit(x, z); // [mod] entraînement : position du choc, pour la carte du rapport
      String sLearn = "";
      if (ModChrono.m_bOn) {
         // [mod] IA chrono : rien d'appris en course (couloirs, vitesses et dangers identiques pour les 6 voitures) ;
         // le choc coûte du temps au plan (ModChrono._HIT_PENALTY) ; ModChrono note l'endroit (essais centrés là)
         ModChrono.OnHit(this, i, 1);
         ModChrono.OnObstacle(this, i, x, z, vNormal); // bornes : où la ligne du plan mène dans le décor
         this.m_bModNextValid = false;
         this.ModChooseNextPoint();
         return;
      }

      if (vNormal != null && i >= 0 && ModAIMap.m_Track == this.m_Track) {
         // [mod] mur appris (aimap.dat, pour toujours) : le bord du couloir recule du côté de l'obstacle, un pas
         // entier à la porte la plus proche de la voiture, un demi à l'autre (le choc est entre les deux)
         int pr = this.m_Track.GetPrevPointNum(i);
         TrackPoint pi = this.m_Track.GetPoint(i);
         float di = (x - pi.m_vPos.fX) * (x - pi.m_vPos.fX) + (z - pi.m_vPos.fZ) * (z - pi.m_vPos.fZ);
         float dp = di;
         if (pr >= 0) {
            TrackPoint pp = this.m_Track.GetPoint(pr);
            dp = (x - pp.m_vPos.fX) * (x - pp.m_vPos.fX) + (z - pp.m_vPos.fZ) * (z - pp.m_vPos.fZ);
         }

         float fStep = ModAIMap._LEARN_STEP;
         if (ModAIMap.Learn(i, x, z, vNormal.fX, vNormal.fZ, di <= dp ? fStep : fStep * 0.5F)) {
            sLearn = sLearn + " " + i;
         }

         if (pr >= 0 && ModAIMap.Learn(pr, x, z, vNormal.fX, vNormal.fZ, di <= dp ? fStep * 0.5F : fStep)) {
            sLearn = sLearn + " " + pr;
         }
      }

      if (m_nModHitLogs < _MOD_LOG_HITS && i >= 0) {
         // pour régler la carte : où était la voiture dans la porte, ce que la carte disait du couloir, ce qui est appris
         m_nModHitLogs++;
         float lat = (x - this.m_fModGateCX) * this.m_fModGateRX + (z - this.m_fModGateCZ) * this.m_fModGateRZ;
         float side = vNormal == null ? 0.0F : -(vNormal.fX * this.m_fModGateRX + vNormal.fZ * this.m_fModGateRZ);
         GameObject.CrashLog(
            "[mod] IA " + Rocket.ModName(this.m_Vehicle) + " : choc contre le decor avant le point " + i + ", voiture a " + Math.round(lat)
               + " du milieu, obstacle " + (vNormal == null ? "?" : (side > 0.1F ? "a droite" : (side < -0.1F ? "a gauche" : "devant")))
               + ", cible " + Math.round(this.m_fModGateOff) + ", porte -" + Math.round(this.m_fModGateLW) + ".." + Math.round(this.m_fModGateRW)
               + (ModAIMap.Known(i) ? ", couloir " + Math.round(ModAIMap.m_aLo[i]) + ".." + Math.round(ModAIMap.m_aHi[i]) : ", porte pas cartographiee")
               + (sLearn.length() > 0 ? ", mur appris aux portes" + sLearn : "") + "\n"
         );
      }

      // [mod] trop vite à l'approche : les deux portes d'avant passeront moins vite (sauts, virages serrés)
      if (i >= 0 && ModAIMap.m_Track == this.m_Track && this.m_aModPassSpeed != null) {
         int g1 = this.m_Track.GetPrevPointNum(i);
         int g2 = g1 >= 0 ? this.m_Track.GetPrevPointNum(g1) : -1;
         String sCut = "";
         if (g1 >= 0 && g1 < this.m_aModPassSpeed.length && now - this.m_aModPassTime[g1] < _MOD_PASS_RECENT
            && ModAIMap.CutSpeed(this.m_Track, g1, this.m_aModPassSpeed[g1], ModAIMap._SPEED_CUT)) {
            sCut = sCut + " " + g1 + ":" + Math.round(ModAIMap.m_aSpeed[g1] * 100.0F) + "%";
         }

         if (g2 >= 0 && g2 < this.m_aModPassSpeed.length && now - this.m_aModPassTime[g2] < _MOD_PASS_RECENT * 1.5F
            && ModAIMap.CutSpeed(this.m_Track, g2, this.m_aModPassSpeed[g2], ModAIMap._SPEED_CUT2)) {
            sCut = sCut + " " + g2 + ":" + Math.round(ModAIMap.m_aSpeed[g2] * 100.0F) + "%";
         }

         if (sCut.length() > 0 && m_nModHitLogs < _MOD_LOG_HITS) {
            GameObject.CrashLog("[mod] IA " + Rocket.ModName(this.m_Vehicle) + " : trop vite avant le point " + i + ", vitesse apprise aux portes" + sCut + "\n");
         }
      }

      this.ModMarkDanger(_MOD_DANGER_HIT);
      // nouvelle cible tout de suite, plus au milieu
      this.m_bModNextValid = false;
      this.ModChooseNextPoint();
   }
}
