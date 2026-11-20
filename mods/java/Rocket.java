// [mod] Fusée (chantier 5, notes/bonus.md). Jamais ramassable dans le jeu d'origine (absente des listes des mondes),
// elle n'avait pas été réglée. Vu en jeu : elle explose sur la cible et rien ne se passe. Elle volait comme la noix de
// coco : départ à la vitesse du tireur, +750/s², 5500 au plus, 5 s de vie, pour des voitures qui roulent à 4500 (plus
// en turbo) : elle rattrapait mal sa cible et, à la fin de ses 5 s, explosait juste derrière elle. OnMiss affiche la
// même explosion qu'un coup au but, d'où « explose sur la cible sans effet ».
// Ici : départ à _MOD_START_SPEED au moins, +_MOD_ACCELERATION/s² jusqu'à _MOD_MAX_SPEED ; les collisions avec le décor
// sont ignorées tant qu'elle suit une cible (sa trajectoire suit déjà le relief, 100 au-dessus du sol, et son modèle
// rakietaBonus.3DA est le seul projectile sans .colmap). Le journal dit comment finit chaque fusée qui concerne un humain.
//
// [mod] Missile (bonus 21, LevelBonusTypes._BONUS_MOD_MISSILE) : la même fusée (forme, animation, type d'usage 20),
// mais la cible est toujours le premier de la course (le 2e si c'est le tireur), choisie au moment où elle part,
// quelle que soit la distance. Pas de viseur pour le joueur ; l'IA le tire dès qu'elle l'a (Vehicle.AimAtEnemy).
// Plus rapide et plus longue à vivre que la fusée, pour rattraper un premier lointain ou en turbo.
// Son modèle est une copie bleue de celui de la fusée (missileBonus.3DA, mods/art/make_missile_model.py) et son icône
// est la sienne (KidsHud.ModGadgetIcon).
public class Rocket extends TrackingDevice {
   static final float _ROCKET_SPEED = 5500.0F;
   static final float _ROCKET_LIFE = 5.0F;
   static float _SCALE_BEFORE_COLLECT_ROCKET = 1.0F;
   static float _SCALE_AFTER_COLLECT_ROCKET = 1.0F;
   static final float _MOD_START_SPEED = 5000.0F;
   static final float _MOD_MAX_SPEED = 10000.0F;
   static final float _MOD_ACCELERATION = 4000.0F;
   // [mod] Missile : voitures à 4500, environ 13000 en turbo
   static final float _MOD_MISSILE_START_SPEED = 6000.0F;
   static final float _MOD_MISSILE_MAX_SPEED = 16000.0F;
   static final float _MOD_MISSILE_ACCELERATION = 8000.0F;
   static final float _MOD_MISSILE_LIFE = 20.0F;
   static final String _MOD_MISSILE_MESH = "Data\\Meshes\\Objects\\missileBonus.3da";
   float m_fModLaunchTime = 0.0F;
   boolean m_bModGroundIgnored = false;
   boolean m_bModMissile = false;

   // [mod] Missile : une fusée marquée (LevelBonusTypes.CreateBonus, case 21), avec son modèle à elle
   // (UseItem.CreateRocket, autre MeshName)
   static Rocket ModCreateMissile(Vehicle v) {
      Rocket r = (Rocket)RacerModule.m_Mod.CreateObjectNoInit("Rocket");
      r.MeshName = _MOD_MISSILE_MESH;
      r.LoadMesh(r.MeshName);
      RacerModule.m_Mod.AddControlObject(r);
      r.InitObject();
      r.m_UseType = 20;
      r.m_Parent = v;
      r.m_bModMissile = true;
      return r;
   }

   // [mod] la voiture tient un Missile pas encore tiré
   static boolean ModHoldsMissile(Vehicle v) {
      return v != null && v.m_CurrentBonus instanceof Rocket && ((Rocket)v.m_CurrentBonus).m_bModMissile && !v.m_CurrentBonus.m_bWasUsed;
   }

   // [mod] cible du Missile : la première voiture du classement qui n'est pas le tireur et n'a pas fini la course
   static Vehicle ModFindLeader(Vehicle shooter) {
      java.util.Vector t = RacerModule.m_Mod.m_RaceParams.m_SortTable;

      for (int i = 0; i < t.size(); i++) {
         Vehicle v = (Vehicle)t.elementAt(i);
         if (v != null && v != shooter && !v.m_bRaceFinished) {
            return v;
         }
      }

      return null;
   }

   public void OnMiss() {
      Vector var1 = this.GetPosition();
      this.ExecuteFX("FXRocketExplosion", var1);
      this.ModLog("ratee (" + this.ModMissCause(var1) + ")");
      // [mod] l'original ne vérifiait pas le tireur (la noix de coco, si)
      if (super.m_Parent != null) {
         super.m_Parent.PlayMissedSound();
         if (super.m_Parent.m_Character != null) {
            super.m_Parent.m_Character.MissedAnim();
         }
      }
   }

   public void OnReachTarget() {
      super.OnReachTarget();
      if (super.m_LockedObject != null) {
         Vector var1 = super.m_LockedObject.GetPosition();
         this.ExecuteFX("FXRocketExplosion", var1);
         int nPhysics = super.m_LockedObject.m_iCurrPhysics;
         super.m_LockedObject.ExplosionEffect();
         this.ModLog(
            "touche " + ModName(super.m_LockedObject) + " apres " + ModSeconds(this.GetTime() - this.m_fModLaunchTime) + " s"
               + (nPhysics == 1 || nPhysics == 2 ? ", deja en l'air ou en chute : sans effet" : "")
         );
      }
   }

   // [mod] vitesse de départ et maximale de la fusée (le tireur passe 5500 et sa propre vitesse, comme pour la noix de coco)
   void Init(Module var1, Vehicle var2, Vehicle var3, float var4, float var5, float var6) {
      if (this.m_bModMissile) {
         // [mod] Missile : le premier au moment du départ, pas la voiture du viseur
         var3 = ModFindLeader(var2);
         super.Init(var1, var2, var3, var4 > _MOD_MISSILE_START_SPEED ? var4 : _MOD_MISSILE_START_SPEED, _MOD_MISSILE_MAX_SPEED, _MOD_MISSILE_LIFE);
         this.m_fModLaunchTime = this.GetTime();
         if (var3 != null && var2 != null) {
            this.ModLog("lance par " + ModName(var2) + " (" + (var2.m_iCurrPlace + 1) + "e) sur "
               + (var3.m_iCurrPlace == 0 ? "le premier " : "le " + (var3.m_iCurrPlace + 1) + "e ") + ModName(var3) + " a "
               + ModDistance(var2.GetPosition(), var3.GetPosition()));
         } else {
            this.ModLog("lance par " + ModName(var2) + " sans premier a viser");
         }

         return;
      }

      super.Init(var1, var2, var3, var4 > _MOD_START_SPEED ? var4 : _MOD_START_SPEED, _MOD_MAX_SPEED, var6);
      this.m_fModLaunchTime = this.GetTime();
      if (var3 != null && var2 != null) {
         this.ModLog("lancee par " + ModName(var2) + " sur " + ModName(var3) + " a " + ModDistance(var2.GetPosition(), var3.GetPosition()));
      } else {
         this.ModLog("lancee par " + ModName(var2) + " sans cible verrouillee");
      }
   }

   // [mod] accélération propre à la fusée (TrackingDevice._ACCELERATION = 750 vaut pour tous les projectiles)
   void Accelerate(float var1) {
      if (super.m_fCurrSpeed < super.m_fSpeed) {
         super.m_fCurrSpeed = super.m_fCurrSpeed + var1 * (this.m_bModMissile ? _MOD_MISSILE_ACCELERATION : _MOD_ACCELERATION);
         if (super.m_fCurrSpeed > super.m_fSpeed) {
            super.m_fCurrSpeed = super.m_fSpeed;
         }
      }
   }

   // [mod] décor ignoré pendant la poursuite d'une cible ; sans cible, comportement d'origine (explose au contact)
   public void OnCollisionFullInfo(ControlObject var1, Vector var2, Vector var3) {
      if (var1 == null && super.m_LockedObject != null && super.m_bWasInit && !super.m_bDestroyed) {
         if (!this.m_bModGroundIgnored) {
            this.m_bModGroundIgnored = true;
            this.ModLog("contact avec le decor ignore");
         }

         return;
      }

      super.OnCollisionFullInfo(var1, var2, var3);
   }

   public void AddSmokeFX() {
      this.ExecuteFX(this, "ogien", "FXRocketSmoke", Vector.Zero);
   }

   public void OnCreate() {
      super.OnCreate();
      super.m_fSpeed = 5500.0F;
      super.m_fFlyRotationSpeed = TrackingDevice._FLY_ROTATION_SPEED;
      super.m_XRot = -45.0F;
      super.m_YRot = 220.0F;
      super.m_ZRot = 35.0F;
      this.SetRotAndScale();
   }

   // --- [mod] journal : seulement les fusées tirées par un humain ou sur un humain ---
   String ModMissCause(Vector vPos) {
      if (super.m_LockedObject == null) {
         return super.m_Life <= 0.0F ? "sans cible, fin de course" : "sans cible, decor touche";
      }

      if (super.m_LockedObject.m_bProtection) {
         return ModName(super.m_LockedObject) + " protege";
      }

      return (super.m_Life <= 0.0F ? "temps ecoule" : "?") + ", a " + ModDistance(vPos, super.m_LockedObject.GetPosition()) + " de "
         + ModName(super.m_LockedObject);
   }

   void ModLog(String s) {
      boolean bHuman = super.m_Parent != null && super.m_Parent.m_VehicleAI == null
         || super.m_LockedObject != null && super.m_LockedObject.m_VehicleAI == null;
      if (bHuman) {
         GameObject.CrashLog((this.m_bModMissile ? "[mod] missile : " : "[mod] fusee : ") + s + "\n");
      }
   }

   static String ModName(Vehicle v) {
      if (v == null) {
         return "?";
      }

      if (v.m_VehicleAI == null) {
         return "joueur";
      }

      return "IA " + (v.m_iAnimalID >= 0 && v.m_iAnimalID < ModAnimals._COUNT ? ModAnimals._NAMES[v.m_iAnimalID] : "" + v.m_iAnimalID);
   }

   static String ModDistance(Vector a, Vector b) {
      float dx = a.fX - b.fX;
      float dz = a.fZ - b.fZ;
      return "" + Math.round(Math.sqrt(dx * dx + dz * dz));
   }

   static String ModSeconds(float f) {
      return "" + Math.round(f * 10.0F) / 10.0F;
   }
}
