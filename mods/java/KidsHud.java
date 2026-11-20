public class KidsHud extends KidsMenuWindow {
   KidsGadget m_Gadget0 = null;
   KidsPlace m_Place0 = null;
   KidsPlaceText m_PlaceText0 = null;
   KidsLaps m_Lap0 = null;
   KidsLapsText m_LapsText0 = null;
   KidsLargePlace m_LargePlace0 = null;
   KidsSpeed m_Speed0 = null;
   KidsRotometer m_Rotations0 = null;
   KidsWrongWay m_WrongWay0 = null;
   KidsGadget m_Gadget1 = null;
   KidsPlace m_Place1 = null;
   KidsPlaceText m_PlaceText1 = null;
   KidsLaps m_Lap1 = null;
   KidsLapsText m_LapsText1 = null;
   KidsLargePlace m_LargePlace1 = null;
   KidsSpeed m_Speed1 = null;
   KidsRotometer m_Rotations1 = null;
   KidsWrongWay m_WrongWay1 = null;
   KidsDistance m_Distance = null;
   KidsStartLights m_Lights = null;
   Sight[] m_Sight = new Sight[2];
   boolean m_bMulti = false;
   // [mod] compteur d'images par seconde (menu Mods, ModOptions.m_bShowFps), en haut à droite, collé au bord
   static final float _FPS_PERIOD = 0.5F;
   ModText m_Fps = null;
   float m_fFpsStart = -1.0F;
   int m_nFpsFrames = 0;

   // [mod] moyenne sur une demi-seconde, mesurée avec l'horloge du limiteur d'images
   void UpdateFps() {
      if (this.m_Fps == null || !ModOptions.m_bShowFps) {
         return;
      }

      float fNow = ModFrameLimiter.Now();
      if (this.m_fFpsStart < 0.0F || fNow < this.m_fFpsStart) {
         this.m_fFpsStart = fNow;
         this.m_nFpsFrames = 0;
         return;
      }

      this.m_nFpsFrames++;
      float fElapsed = fNow - this.m_fFpsStart;
      if (fElapsed >= _FPS_PERIOD) {
         this.m_Fps.SetText(Math.round(this.m_nFpsFrames / fElapsed) + " FPS");
         this.m_fFpsStart = fNow;
         this.m_nFpsFrames = 0;
      }
   }

   // [mod] KidsMenuWindow.Show réaffiche tous les enfants : le compteur reste caché s'il n'est pas demandé
   public void Show() {
      super.Show();
      if (this.m_Fps != null && !ModOptions.m_bShowFps) {
         this.m_Fps.Hide();
      }

      if (this.m_Spec != null && this.m_sSpec == null) {
         this.m_Spec.Hide();
      }
   }

   // [mod] en ligne, arrivé en attendant les autres : qui on regarde (ModNet.HudText), en bas au centre
   ModText m_Spec = null;
   String m_sSpec = null;

   void UpdateSpec() {
      if (this.m_Spec == null) {
         return;
      }

      String s = ModNet.HudText();
      if (s == null ? this.m_sSpec != null : !s.equals(this.m_sSpec)) {
         this.m_sSpec = s;
         if (s == null) {
            this.m_Spec.Hide();
         } else {
            this.m_Spec.SetText(s);
            this.m_Spec.Show();
         }
      }
   }

   void Update(float var1) {
      this.UpdateFps(); // [mod]
      this.UpdateSpec(); // [mod]
      this.SetTime(var1);
      this.m_Sight[0].Update(var1);
      if (this.m_bMulti) {
         this.m_Sight[1].Update(var1);
      }

      Vehicle var2 = null;
      Vehicle var3 = null;
      java.util.Vector var4 = RacerModule.m_Mod.m_RaceParams.GetRacers();
      this.m_Distance.SetTextures0();
      if (var4 != null) {
         var2 = ((RacerCamera)RacerModule.m_Cams[0]).m_Observed;
         if (var2 != null) {
            this.ShowPlace(var2.GetCurrentPlace(), 0);
            this.ShowLap(var2.GetCurrentLap(), 0);
            if (var2.m_bRaceFinished) {
               this.ShowLargePlace(var2.GetCurrentPlace(), 0);
            }

            int var5 = ModGadgetIcon(((RacerCamera)RacerModule.m_Cams[0]).GetCurrentGadgetIcon(), var2); // [mod]
            this.ShowGadget(var5, 0);
            if (var2.m_fCurrSpeed >= 0.0F) {
               this.SetSpeed(var2.m_fCurrSpeed / var2.GetMaxSpeed(), 0);
            } else {
               this.SetSpeed(var2.m_fCurrSpeed / var2.m_Params._MIN_SPEED, 0);
            }

            this.SetRotations(var2.GetRevolution(), 0);
            if (var2.m_Index >= 0) {
               this.m_Distance.SetTextureHL(var2.m_Index);
            }

            if (var2.m_bWrongWay) {
               this.m_WrongWay0.Show();
            } else {
               this.m_WrongWay0.Hide();
            }

            if (var2.m_bRaceFinished) {
               this.m_Lights.ShowFinish(RacerModule.m_Mod.m_fNowTime - var2.m_fFinishTimeForCam, 0);
            }
         }

         if (this.m_bMulti) {
            var3 = ((RacerCamera)RacerModule.m_Cams[1]).m_Observed;
            if (var3 != null) {
               this.ShowPlace(var3.GetCurrentPlace(), 1);
               this.ShowLap(var3.GetCurrentLap(), 1);
               if (var3.m_bRaceFinished) {
                  this.ShowLargePlace(var3.GetCurrentPlace(), 1);
               }

               this.ShowGadget(ModGadgetIcon(((RacerCamera)RacerModule.m_Cams[1]).GetCurrentGadgetIcon(), var3), 1); // [mod]
               if (var3.m_fCurrSpeed >= 0.0F) {
                  this.SetSpeed(var3.m_fCurrSpeed / var3.GetMaxSpeed(), 1);
               } else {
                  this.SetSpeed(var3.m_fCurrSpeed / var3.m_Params._MIN_SPEED, 1);
               }

               this.SetRotations(var3.GetRevolution(), 1);
               if (var3.m_Index >= 0) {
                  this.m_Distance.SetTextureHL(var3.m_Index);
               }

               if (var3.m_bWrongWay) {
                  this.m_WrongWay1.Show();
               } else {
                  this.m_WrongWay1.Hide();
               }

               if (var3.m_bRaceFinished) {
                  this.m_Lights.ShowFinish(RacerModule.m_Mod.m_fNowTime - var3.m_fFinishTimeForCam, 1);
               }
            }
         }

         for (int var10 = 0; var10 < var4.size(); var10++) {
            this.m_Distance.SetDistance(((Vehicle)var4.elementAt(var10)).GetCurrDistance(), var10);
         }

         Object var6 = null;

         for (int var11 = var4.size() - 1; var11 >= 0; var11--) {
            var6 = (Vehicle)RacerModule.m_Mod.m_RaceParams.m_SortTable.elementAt(var11);
            if (((Vehicle)var6).m_Index >= 0) {
               this.m_Distance.MoveTop(((Vehicle)var6).m_Index);
            }
         }

         var2 = ((RacerCamera)RacerModule.m_Cams[0]).m_Observed;
         if (var2 != null) {
            this.m_Distance.MoveTop(var2.m_Index);
         }
      }
   }

   void InitHud(Module var1, boolean var2) {
      super.InitHud(var1);
      CoreLogic var3 = RacerGame.GetCoreLogic();
      this.m_bMulti = var2;
      this.m_WrongWay0 = (KidsWrongWay)this.CreateObject("KidsWrongWay");
      this.Add(this.m_WrongWay0);
      this.m_WrongWay0.InitHud(var1, 0, var2);
      this.m_Gadget0 = (KidsGadget)this.CreateObject("KidsGadget");
      this.Add(this.m_Gadget0);
      this.m_Gadget0.InitHud(var1, 0, var2);
      this.m_Place0 = (KidsPlace)this.CreateObject("KidsPlace");
      this.Add(this.m_Place0);
      this.m_Place0.InitHud(var1, 0, var2);
      this.m_PlaceText0 = (KidsPlaceText)this.CreateObject("KidsPlaceText");
      this.Add(this.m_PlaceText0);
      this.m_PlaceText0.InitHud(var1, 0, var2);
      this.m_Lap0 = (KidsLaps)this.CreateObject("KidsLaps");
      this.Add(this.m_Lap0);
      this.m_Lap0.InitHud(var1, 0, var2);
      this.m_LapsText0 = (KidsLapsText)this.CreateObject("KidsLapsText");
      this.Add(this.m_LapsText0);
      this.m_LapsText0.InitHud(var1, 0, var2);
      this.m_LargePlace0 = (KidsLargePlace)this.CreateObject("KidsLargePlace");
      this.Add(this.m_LargePlace0);
      this.m_LargePlace0.InitHud(var1, 0, var2);
      this.m_LargePlace0.Hide();
      this.m_LargePlace0.m_Hud = this;
      this.m_Speed0 = (KidsSpeed)this.CreateObject("KidsSpeed");
      this.Add(this.m_Speed0);
      this.m_Speed0.InitHud(var1, 0, var2);
      this.m_Rotations0 = (KidsRotometer)this.CreateObject("KidsRotometer");
      this.Add(this.m_Rotations0);
      this.m_Rotations0.InitHud(var1, 0, var2);
      if (!var2) {
         this.m_Sight[0] = new Sight((RacerCamera)RacerModule.m_Cams[0], -1);
      } else {
         this.m_Sight[0] = new Sight((RacerCamera)RacerModule.m_Cams[0], 0);
         this.m_Sight[1] = new Sight((RacerCamera)RacerModule.m_Cams[1], 1);
      }

      if (var2) {
         this.m_WrongWay1 = (KidsWrongWay)this.CreateObject("KidsWrongWay");
         this.Add(this.m_WrongWay1);
         this.m_WrongWay1.InitHud(var1, 1, var2);
         this.m_Gadget1 = (KidsGadget)this.CreateObject("KidsGadget");
         this.Add(this.m_Gadget1);
         this.m_Gadget1.InitHud(var1, 1, var2);
         this.m_Place1 = (KidsPlace)this.CreateObject("KidsPlace");
         this.Add(this.m_Place1);
         this.m_Place1.InitHud(var1, 1, var2);
         this.m_PlaceText1 = (KidsPlaceText)this.CreateObject("KidsPlaceText");
         this.Add(this.m_PlaceText1);
         this.m_PlaceText1.InitHud(var1, 1, var2);
         this.m_Lap1 = (KidsLaps)this.CreateObject("KidsLaps");
         this.Add(this.m_Lap1);
         this.m_Lap1.InitHud(var1, 1, var2);
         this.m_LapsText1 = (KidsLapsText)this.CreateObject("KidsLapsText");
         this.Add(this.m_LapsText1);
         this.m_LapsText1.InitHud(var1, 1, var2);
         this.m_LargePlace1 = (KidsLargePlace)this.CreateObject("KidsLargePlace");
         this.Add(this.m_LargePlace1);
         this.m_LargePlace1.InitHud(var1, 1, var2);
         this.m_LargePlace1.Hide();
         this.m_LargePlace1.m_Hud = this;
         this.m_Speed1 = (KidsSpeed)this.CreateObject("KidsSpeed");
         this.Add(this.m_Speed1);
         this.m_Speed1.InitHud(var1, 1, var2);
         this.m_Rotations1 = (KidsRotometer)this.CreateObject("KidsRotometer");
         this.Add(this.m_Rotations1);
         this.m_Rotations1.InitHud(var1, 1, var2);
      }

      this.m_Distance = (KidsDistance)this.CreateObject("KidsDistance");
      this.Add(this.m_Distance);
      this.m_Distance.InitHud(var1, var3.GetSelectedAnimals());
      this.m_Lights = (KidsStartLights)this.CreateObject("KidsStartLights");
      this.Add(this.m_Lights);
      this.m_Lights.InitHud(var1);
      // [mod] compteur FPS
      this.m_Fps = (ModText)this.CreateObject("ModText");
      this.Add(this.m_Fps);
      this.m_Fps.InitHud(var1);
      this.m_Fps.SetColor(1.0F, 1.0F, 0.6F);
      this.m_Fps.SetSizeAndPos(0.14F, 0.05F, 0.92F, 0.035F, true);
      this.m_Fps.SetText("");
      this.m_Spec = (ModText)this.CreateObject("ModText");
      this.Add(this.m_Spec);
      this.m_Spec.InitHud(var1);
      this.m_Spec.SetColor(1.0F, 1.0F, 1.0F);
      this.m_Spec.SetSizeAndPos(0.9F, 0.05F, 0.5F, 0.8F, true);
      this.m_Spec.SetText("");
      this.m_Spec.Hide();
      if (ModOptions.m_bShowFps) {
         this.m_Fps.Show();
      } else {
         this.m_Fps.Hide();
      }
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.m_WrongWay0.Hide();
      if (this.m_WrongWay1 != null) {
         this.m_WrongWay1.Hide();
      }
   }

   void SetSpeed(float var1, int var2) {
      if (var2 == 0) {
         if (this.m_Speed0 != null) {
            this.m_Speed0.SetSpeed(var1);
            return;
         }
      } else if (this.m_Speed1 != null) {
         this.m_Speed1.SetSpeed(var1);
      }
   }

   public void WrongWayHide() {
      this.m_WrongWay0.Hide();
      if (this.m_WrongWay1 != null) {
         this.m_WrongWay1.Hide();
      }
   }

   void ShowPlace(int var1, int var2) {
      if (var2 == 0) {
         if (this.m_Place0 != null) {
            this.m_Place0.ShowPlace(var1);
            return;
         }
      } else if (this.m_Place1 != null) {
         this.m_Place1.ShowPlace(var1);
      }
   }

   public void SetTime(float var1) {
      ExtraHudModule var2 = RacerGame.extraHud;
      if (var2 != null && var2.m_Timer != null) {
         var2.m_Timer.SetTime(var1);
      }
   }

   // [mod] le Missile est une Fusée (même type d'usage, donc même icône pour RacerCamera) : on lui donne la sienne
   static int ModGadgetIcon(int icon, Vehicle v) {
      if (icon == KidsGadget._GADGET_ROCKET && v != null && v.m_CurrentBonus instanceof Rocket && ((Rocket)v.m_CurrentBonus).m_bModMissile) {
         return KidsGadget._GADGET_MOD_MISSILE;
      }

      return icon;
   }

   void ShowGadget(int var1, int var2) {
      if (var2 == 0) {
         this.m_Gadget0.ShowGadget(var1);
      } else {
         this.m_Gadget1.ShowGadget(var1);
      }
   }

   void SetRotations(float var1, int var2) {
      if (var2 == 0) {
         if (this.m_Rotations0 != null) {
            this.m_Rotations0.SetRotations(var1);
            return;
         }
      } else if (this.m_Rotations1 != null) {
         this.m_Rotations1.SetRotations(var1);
      }
   }

   void ShowLargePlace(int var1, int var2) {
      if (var2 == 0) {
         if (this.m_LargePlace0 != null) {
            this.m_LargePlace0.ShowPlace(var1);
            return;
         }
      } else if (this.m_LargePlace1 != null) {
         this.m_LargePlace1.ShowPlace(var1);
      }
   }

   public void StartLights() {
      this.m_Lights.StartLights();
   }

   void ShowLap(int var1, int var2) {
      if (var2 == 0) {
         if (this.m_Lap0 != null) {
            this.m_Lap0.ShowLap(var1);
            return;
         }
      } else if (this.m_Lap1 != null) {
         this.m_Lap1.ShowLap(var1);
      }
   }

   boolean IsLargeNumberLocked(int var1) {
      if (this.m_Lights != null) {
         return var1 == 0 ? this.m_Lights.m_bLockLargeNumber0 : this.m_Lights.m_bLockLargeNumber1;
      } else {
         return false;
      }
   }
}
