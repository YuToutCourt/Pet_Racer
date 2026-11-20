public class VehicleParams {
   static float _CAMERA_FOV = 95.0F;
   static float _CAMERA_CLIP_NEAR = 50.0F;
   static float _CAMERA_CLIP_FAR = 15000.0F;
   static int _POWER_RACE_LOPS = 10;
   static float _EXTRA_GRAVITY_ACCEL = -1300.0F;
   static float[] _ANIMALS_ACCEL_COEFS = new float[]{0.9F, 0.9F, 0.9F, 0.85F, 0.9F, 0.95F, 0.9F, 0.95F, 0.9F, 0.9F, 0.8F, 0.9F};
   static float[] _ANIMALS_SPEED_COEFS = new float[]{1.0F, 0.8F, 0.9F, 0.85F, 0.8F, 0.9F, 1.0F, 0.95F, 0.9F, 0.9F, 0.9F, 1.0F};
   static float[] _ANIMALS_TURN_COEFS = new float[]{0.8F, 0.9F, 0.95F, 1.0F, 1.0F, 0.85F, 0.9F, 0.8F, 0.9F, 0.9F, 1.0F, 0.8F};
   float _MAX_SPEED = 4500.0F;
   float _MIN_SPEED = -1300.0F;
   float _ACCELERATION_COEF = 500.0F;
   float _BRAKE_COEF = 2000.0F;
   float _SLOW_COEF = 900.0F;
   float _FLY_SLOW_COEF = 0.0F;
   float _SUSPENSION_HEIGHT = 40.0F;
   float _SPRING_MIN = -10.0F;
   float _SPRING_MAX = 10.0F;
   float _SPRING_NORMAL = -20.0F;
   float _TURNING_SPEED = 700.0F;
   float _MAX_TURN_ANGLE = 80.0F;
   float _DECREASE_TURNING_SPEED = 360.0F;
   float _GRAVITY_ACCEL = -2700.0F;
   float _EXPLOSION_ACCEL = 3000.0F;
   float _GROUND_ALIGN_INERTIA = 30.0F;
   float _GROUND_FLY_ALIGN_INERTIA = 10.0F;
   float _UP_FLY_ALIGN_INERTIA = 3.0F;
   float _MAX_SLOPE_ANGLE = 0.3F;
   float _MAX_BOUNCE_RANGE = 2000.0F;
   float _SLIDE_TIME_COEF = 2000.0F;
   float _SLIDE_SPEED_COEF = 2.0F;
   float _TIME_TO_GOING_BACK = 0.3F;
   float _REAL_FLY_TIME = 0.2F;
   float _REAL_FLY_TIME_CONTROL = 0.1F;
   float _MAX_LANDING_TIME = 0.2F;
   float _WHEEL_MOVE_SPEED_COEF = 0.3F;
   float _SPRING_TIME_MOVE_COEF = 250.0F;
   float _SLIDE_COEF = 60.0F;
   float _SLIDE_SMOKE_COEF = this._SLIDE_COEF / 10.0F;
   float _SLIDE_SUPRESS = 30.0F;
   float _MAX_VERT_DEV = 10.0F;
   float _MAX_HORIZ_DEV = 10.0F;
   float _VERT_DEV_ACCEL = 100.0F;
   float _HORIZ_DEV_ACCEL = 100.0F;
   float _COLL_SUPRESS = 800.0F;
   float _OBJ_HIT_SUPR = 15.0F;
   float _OBJ_HIT_SPEED_SUPR = 2.0F;
   float _TREE_HIT_SUPR = 15.0F;
   float _TIME_BTW_SMOKES = 0.03F;
   float _MIN_SOUND_FREQ = 0.5F;
   float _MAX_SOUND_FREQ = 2.0F;
   float _OUT_OF_TRACK_SPEED = this._MAX_SPEED / 5.0F;
   float _NUM_OF_V_TURNS = 9.0F;
   float _NUM_OF_H_TURNS = 8.0F;
   float _HIT_ROTATION_SPEED = 1500.0F;
   float _SLIDE_ROTATION_SPEED = 600.0F;
   float _MAX_TIME_OUTSIDE = 3.0F;
   float _TURBO_MULTIPLIER = 1.7F;
   float _TURBO_SOUND_MULTIPLIER = 1.7F;
   float _DISABLE_TURBO_TIME = 10.0F;
   float _DISABLE_TIRESPIKES_TIME = 10.0F;
   float _DISABLE_PROTECTION_TIME = 10.0F;
   float _TIME_BETWEEN_BRAKE_SOUNDS = 1.0F;
   float _TIME_BETWEEN_SLIDE_SOUNDS = 0.6F;
   float _TIME_BETWEEN_HIT_SOUNDS = 1.0F;
   float _MIN_TIME_BETWEEN_LOOK = 4.0F;
   float _MAX_TIME_BETWEEN_LOOK = 20.0F;
   float _MIN_LOOK_DISTANCE = 1000.0F;
   float _MIN_AIM_DISTANCE = 3000.0F;
   float _POLE_HIT_COEF = 0.8F;
   float _FALL_TIME = 2.0F;
   float _RESPAWN_HEIGHT = 200.0F;
   float _TURNING_SLOW_COEF = 1000.0F;
   float _FREEZE_TIME = 5.0F;
   float[] _CAM_SINGLE_ANGLE = new float[]{15.0F, 20.0F, 25.0F};
   float[] _CAM_SINGLE_HEIGHT = new float[]{250.0F, 150.0F, 100.0F};
   float[] _CAM_SINGLE_BACK = new float[]{320.0F, 160.0F, 110.0F};
   float[] _CAM_MULTI_ANGLE = new float[]{5.0F, 5.0F, 15.0F};
   float[] _CAM_MULTI_HEIGHT = new float[]{170.0F, 130.0F, 100.0F};
   float[] _CAM_MULTI_BACK = new float[]{520.0F, 270.0F, 170.0F};
   static float _CAM_DEST_FINISH_ANGLE = 40.0F;
   float _STOP_TIME_AFTER_RACE = 16.0F;
   float _PROB_PLAY_OUTRUN_SOUND = 0.8F;
   float _PROB_PLAY_THROW_SOUND = 0.8F;
   float _PROB_PLAY_MISSED_SOUND = 0.8F;
   float _PROB_PLAY_HIT_SOUND = 0.8F;
   float _UFO_MOVE_CAM_TIME = 1.0F;
   static float[] _CAM_SINGLE_ANIMAL_BACK = new float[]{30.0F, 50.0F, 0.0F, 0.0F, 20.0F, 30.0F, 100.0F, 80.0F, 100.0F, 100.0F, 80.0F, 100.0F};
   static float[] _CAM_MULTI_ANIMAL_BACK = new float[]{30.0F, 50.0F, 0.0F, 0.0F, 20.0F, 30.0F, 100.0F, 80.0F, 100.0F, 100.0F, 80.0F, 100.0F};
   float[] _CAM_MAX_BACK_COEF = new float[]{5.0F, 5.0F, 5.0F};
   float[] _CAM_MAX_HEIGHT_COEF = new float[]{5.0F, 5.0F, 5.0F};
   float[] _CAM_MAX_ANGLE_COEF = new float[]{5.0F, 5.0F, 5.0F};
   float[] _GEAR_SHIFT = new float[]{0.0F, 0.1F, 0.25F, 0.45F, 0.65F, 1.0F};
   float[] _GEAR_SHIFT_MOD = new float[]{0.0F, 0.1F, 0.15F, 0.2F, 0.3F, 0.4F};
   float WAIT_AFTER_BONUS_TIME = 0.5F;
   float WAIT_AFTER_ROCK_TIME = 3.0F;
   float _CAM_ANGLE_DELTA = 10.0F;
   float _CAM_MOVE_DELTA = 50.0F;
   float _RESPAWN_ZOOM_CAM_TIME = 2.0F;
   float _RESPAWN_MOVE_CAM_TIME = 1.0F;
   static float _SIGHT_MOVE_SPEED = 2.5F;
   float _SPEED_LEVELING_COEF = 0.4F;
   float _MIN_SPEED_LEVELING_DIST = 0.001F;
   float _MAX_SPEED_LEVELING_DIST = 0.01F;
   float _FLY_MANOEUVRE_COEF = 0.3F;
   static final float _BONUS_LIFE_TIME = 60.0F;
   Vector vEnginePos = new Vector(0.0F, 10.0F, -50.0F);
   static float _MIN_TIME_BETW_TALK = 5.0F;
   static float _MIN_TALK_RANGE = 1000.0F;
   static float _MAX_TALK_RANGE = 5000.0F;
   float _DISABLE_ACCEL_TIME = 5.0F;
   float _FINISH_CAM_ROT_SPEED = 60.0F;
   static String[] _SOUNDS = new String[]{
      "Data\\sounds\\break_road.wav",
      "Data\\Sounds\\crash_car.wav",
      "Data\\Sounds\\crash.wav",
      "Data\\Sounds\\respawn.wav",
      "Data\\Sounds\\bomb.wav",
      "Data\\Sounds\\bonus.wav",
      "Data\\Sounds\\spring.wav",
      "Data\\Sounds\\turbo.wav",
      "Data\\Sounds\\off_road.wav",
      "Data\\Sounds\\fly.wav",
      "Data\\Sounds\\meta.wav",
      "Data\\Sounds\\finish.wav",
      "Data\\Sounds\\brake_asphalt.wav",
      "Data\\Sounds\\finish.wav",
      "Data\\Sounds\\finish.wav",
      "Data\\Sounds\\brake_asphalt.wav",
      "Data\\Sounds\\finish.wav",
      "Data\\Sounds\\finish.wav",
      "Data\\Sounds\\wasp.wav",
      "Data\\Sounds\\melon.wav",
      "Data\\Sounds\\gun.wav",
      "Data\\Sounds\\spray.wav",
      "Data\\Sounds\\can.wav",
      "Data\\Sounds\\water_gun.wav",
      "Data\\Sounds\\coconut.wav",
      "Data\\Sounds\\water_bottle.wav",
      "Data\\Sounds\\heavy.wav",
      "Data\\Sounds\\pie.wav",
      "Data\\Sounds\\ice_break.wav",
      "Data\\Sounds\\ice_gun.wav",
      "Data\\Sounds\\break_wood.wav",
      "Data\\Sounds\\slide_road.wav",
      "Data\\Sounds\\bridge.wav",
      "Data\\Sounds\\accel.wav",
      "Data\\Sounds\\fall.wav",
      "Data\\Sounds\\in_water.wav",
      "Data\\Sounds\\water_contact.wav",
      "Data\\Sounds\\ufo.wav",
      "Data\\Sounds\\ufo_beem.wav",
      "Data\\Sounds\\ufo_button.wav",
      "Data\\Sounds\\bridge_stone.wav" // [mod] le fichier s'appelle bridge_stone.wav (l'original visait stone_bridge.wav)
   };
   static int _SND_BRAKE_ROAD = 0;
   static int _SND_CRASH_CAR = 1;
   static int _SND_CRASH = 2;
   static int _SND_RESPAWN = 3;
   static int _SND_BOMB = 4;
   static int _SND_BONUS = 5;
   static int _SND_CATAPULT = 6;
   static int _SND_TURBO = 7;
   static int _SND_OFF_ROAD = 8;
   static int _SND_FLY = 9;
   static int _SND_RACE_FINISHED = 10;
   static int _SND_OILCAN = 11;
   static int _SND_BANANA = 12;
   static int _SND_TRAPPEDBYMOLE = 13;
   static int _SND_FUNNYGAS = 14;
   static int _SND_SLIDEONWATER = 15;
   static int _SND_DRIVEINANVIL = 16;
   static int _SND_DRIVEINICE = 17;
   static int _SND_WASPBAG = 18;
   static int _SND_WATERMELON = 19;
   static int _SND_CORK_GUN = 20;
   static int _SND_SPRAY = 21;
   static int _SND_CAN = 22;
   static int _SND_WATER_GUN = 23;
   static int _SND_COCONUT = 24;
   static int _SND_WATER = 25;
   static int _SND_ANVIL = 26;
   static int _SND_PIE = 27;
   static int _SND_ICE_BREAK = 28;
   static int _SND_FREEZE_PISTOL = 29;
   static int _SND_BREAK_WOOD = 30;
   static int _SND_SLIDE_ROAD = 31;
   static int _SND_BRIDGE = 32;
   static int _SND_ACCEL = 33;
   static int _SND_FALL = 34;
   static int _SND_IN_WATER = 35;
   static int _SND_TO_WATER = 36;
   static int _SND_UFO = 37;
   static int _SND_UFO_BEAM = 38;
   static int _SND_UFO_BUTTON = 39;
   static int _SND_STONE_BRIDGE = 40;
   static String[] _CAR_SOUNDS = new String[]{
      "Data\\sounds\\move_asphalt.wav",
      "Data\\sounds\\move_gravel.wav",
      "Data\\sounds\\move_snow.wav",
      "Data\\sounds\\move_sand.wav",
      "Data\\sounds\\move_litter.wav",
      "Data\\sounds\\brake_asphalt.wav",
      "Data\\sounds\\brake_gravel.wav",
      "Data\\sounds\\brake_snow.wav",
      "Data\\sounds\\brake_sand.wav",
      "Data\\sounds\\brake_litter.wav",
      "Data\\sounds\\slide_asphalt.wav",
      "Data\\sounds\\slide_gravel.wav",
      "Data\\sounds\\accel_asphalt.wav",
      "Data\\sounds\\accel_gravel.wav",
      "Data\\sounds\\accel_snow.wav",
      "Data\\sounds\\accel_sand.wav",
      "Data\\sounds\\accel_litter.wav"
   };
   static int _SND_MOVE_ASPHALT = 0;
   static int _SND_MOVE_GRAVEL = 1;
   static int _SND_MOVE_SNOW = 2;
   static int _SND_MOVE_SAND = 3;
   static int _SND_MOVE_LITTER = 4;
   static int _SND_BRAKE_ASPHALT = 5;
   static int _SND_BRAKE_GRAVEL = 6;
   static int _SND_BRAKE_SNOW = 7;
   static int _SND_BRAKE_SAND = 8;
   static int _SND_BRAKE_LITTER = 9;
   static int _SND_CURVE_ASPHALT = 10;
   static int _SND_CURVE_GRAVEL = 11;
   static int _SND_ACCEL_ASPHALT = 12;
   static int _SND_ACCEL_GRAVEL = 13;
   static int _SND_ACCEL_SNOW = 14;
   static int _SND_ACCEL_SAND = 15;
   static int _SND_ACCEL_LITTER = 16;
   static int _MOVE_ROAD = 0;
   static int _MOVE_OFF_ROAD = 0;
   static int _BRAKE_ROAD = 0;
   static int _BRAKE_OFF_ROAD = 0;
   static int _SLIDE_ROAD = 0;
   static int _SLIDE_OFF_ROAD = 0;
   static int _ACCEL_ROAD = 0;
   static int _ACCEL_OFF_ROAD = 0;

   VehicleParams(int var1) {
      this._MIN_SPEED_LEVELING_DIST /= var1;
      this._MAX_SPEED_LEVELING_DIST /= var1;
   }

   static void SetTrackSounds(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      _MOVE_ROAD = var0;
      _MOVE_OFF_ROAD = var1;
      _BRAKE_ROAD = var2;
      _BRAKE_OFF_ROAD = var3;
      _SLIDE_ROAD = var4;
      _SLIDE_OFF_ROAD = var5;
      _ACCEL_ROAD = var6;
      _ACCEL_OFF_ROAD = var7;
   }
}
