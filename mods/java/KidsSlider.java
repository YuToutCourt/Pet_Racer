public class KidsSlider extends KidsMenuWindow {
   int bkTex0 = -1;
   String m_LBUpSoundName = "Data/Sounds/Demo/highlight.wav";
   static final int _DISABLED = 0;
   static final int _ENABLED = 1;
   static final int _HIGHLIGHTED = 2;
   int m_State = 1;
   int[] bkTex = new int[]{-1, -1, -1};
   int[] bnTex = new int[]{-1, -1, -1};
   float m_LeftBorder = 0.25F;
   float m_RightBorder = 0.75F;
   float m_Value = 0.0F;
   float m_OldValue = 0.0F;
   boolean _bMouseDrag = false;
   int sliderid = -1;
   int m_Discretisation = 0;

   public void FakeLeave() {
      if (this.m_State != 0) {
         if (this.m_State != 1) {
            this.SetSliderState(1);
         }

         if (this._bMouseDrag) {
            this._bMouseDrag = false;
            this.m_Value = this.m_OldValue;
            this.SetSliderByValue(this.m_Value);
            if (super.m_TargetWindow != null) {
               super.m_TargetWindow.SliderChanging(super.m_WindowID, this.m_Value);
               super.m_TargetWindow.SliderChanged(super.m_WindowID, this.m_OldValue, this.m_Value);
            }
         }

         super.m_bEnableKeyFocus = false;
      }
   }

   void SetSliderByValue(float var1) {
      if (var1 < 0.0F) {
         var1 = 0.0F;
      }

      if (var1 > 1.0F) {
         var1 = 1.0F;
      }

      this.m_Value = var1;
      if (this.m_Discretisation > 1) {
         int var2 = this.GetValueDiscrete();
         if (var2 >= 0) {
            this.SetValueDiscrete(var2);
         }
      }

      float var4 = this.m_LeftBorder + this.m_Value * (this.m_RightBorder - this.m_LeftBorder);
      Window var3 = super.m_List.WindowAt(0);
      if (var3 != null) {
         var3.SetSizeAndPos(var3.m_lwidth, var3.m_lheight, var4, var3.m_lypos, var3.m_blpos);
      }
   }

   boolean SetSliderState(int var1) {
      if (this.m_State == 0) {
         return false;
      }

      if (var1 == 2) {
         this.m_State = var1;
      } else {
         this.m_State = 1;
      }

      this.SetWindowTexture(this.bkTex[this.m_State]);
      super.m_List.SetTexture(this.sliderid, this.bnTex[this.m_State]);
      return true;
   }

   boolean IsEnabled() {
      return this.m_State != 0 && this.CanProcessMouseMove();
   }

   public void OnKeyReleased(int var1, char var2) {
      if (this.CanProcessMouseMove() && !super.m_Module.CanIgnoreKeys() && super.m_bEnableKeyFocus) {
         float fOld = this.m_Value; // [mod] au clavier aussi, la fenêtre est prévenue du changement
         this.OnKeyReleasedOrig(var1);
         if (this.m_Value != fOld && super.m_TargetWindow != null) {
            super.m_TargetWindow.SliderChanging(super.m_WindowID, this.m_Value);
            super.m_TargetWindow.SliderChanged(super.m_WindowID, fOld, this.m_Value);
         }
      }
   }

   void OnKeyReleasedOrig(int var1) {
      {
         if (var1 == 200) {
            if (this.m_Discretisation >= 2) {
               int var4 = this.GetValueDiscrete() - 1;
               if (var4 < 0) {
                  var4 = 0;
               }

               this.SetValueDiscrete(var4);
            } else {
               this.m_Value -= 0.05F;
               if (this.m_Value < 0.0F) {
                  this.m_Value = 0.0F;
               }
            }

            this.RefreshPos();
         } else if (var1 == 208) {
            if (this.m_Discretisation >= 2) {
               int var3 = this.GetValueDiscrete() + 1;
               if (var3 >= this.m_Discretisation) {
                  var3 = this.m_Discretisation - 1;
               }

               this.SetValueDiscrete(var3);
            } else {
               this.m_Value += 0.05F;
               if (this.m_Value >= 1.0F) {
                  this.m_Value = 1.0F;
               }
            }

            this.RefreshPos();
         }
      }
   }

   void OnLBUp(float var1, float var2) {
      if (this._bMouseDrag && this.m_State != 0 && this.CanProcessMouseMove()) {
         this.SetSliderByValue(this.m_Value);
         if (super.m_TargetWindow != null) {
            super.m_TargetWindow.SliderChanging(super.m_WindowID, this.m_Value);
            super.m_TargetWindow.SliderChanged(super.m_WindowID, this.m_OldValue, this.m_Value);
            super.m_TargetWindow.m_Module.PlayCameraSound(this.m_LBUpSoundName);
         }
      }

      this._bMouseDrag = false;
   }

   float GetSliderValueByPos(Vector var1) {
      Vector var2 = this.GetLogicalPos(var1);
      float var3 = 0.0F;
      if (var2.fX <= this.m_LeftBorder) {
         var3 = 0.0F;
      } else if (var2.fX >= this.m_RightBorder) {
         var3 = 1.0F;
      } else {
         var3 = (var2.fX - this.m_LeftBorder) / (this.m_RightBorder - this.m_LeftBorder);
      }

      return var3;
   }

   public void OnMouseLeave(float var1, float var2) {
      if (this.m_State != 0) {
         super.OnMouseLeave(var1, var2);
         if (this.m_State != 1) {
            this.SetSliderState(1);
         }

         super.m_Module.SetCursor(0);
         if (this._bMouseDrag) {
            this._bMouseDrag = false;
            this.m_Value = this.m_OldValue;
            this.SetSliderByValue(this.m_Value);
            if (super.m_TargetWindow != null) {
               super.m_TargetWindow.SliderChanging(super.m_WindowID, this.m_Value);
               super.m_TargetWindow.SliderChanged(super.m_WindowID, this.m_OldValue, this.m_Value);
            }
         }

         super.m_bEnableKeyFocus = false;
      }
   }

   public void Enable(boolean var1) {
      if (var1) {
         this.m_State = 1;
         this.SetWindowTexture(this.bkTex[1]);
         super.m_List.SetTexture(this.sliderid, this.bnTex[1]);
      } else {
         this.m_State = 0;
         this.SetWindowTexture(this.bkTex[0]);
         super.m_List.SetTexture(this.sliderid, this.bnTex[0]);
      }
   }

   public boolean DeactivateKeyFocus() {
      if (super.m_bEnableKeyFocus) {
         this.FakeLeave();
         return true;
      } else {
         return false;
      }
   }

   void SetValueDiscrete(int var1) {
      if (this.m_Discretisation > 1) {
         this.m_Value = var1 / (this.m_Discretisation - 1.0F);
      }
   }

   public void MouseEntered(int var1) {
   }

   void SetBorders(float var1, float var2) {
      if (var1 > var2) {
         float var3 = var1;
         var1 = var2;
         var2 = var3;
      }

      if (var1 < 0.0F) {
         var1 = 0.0F;
      }

      if (var1 > 1.0F) {
         var1 = 1.0F;
      }

      if (var2 < 0.0F) {
         var2 = 0.0F;
      }

      if (var2 > 1.0F) {
         var2 = 1.0F;
      }

      this.m_LeftBorder = var1;
      this.m_RightBorder = var2;
   }

   void RefreshPos() {
      this.SetSliderByValue(this.m_Value);
   }

   public void MouseLeft(int var1) {
   }

   public boolean FakeEnter() {
      if (this.m_State != 0 && this.CanProcessMouseMove()) {
         if (this.m_State != 2) {
            this.SetSliderState(2);
         }

         super.m_bEnableKeyFocus = true;
         return true;
      } else {
         return false;
      }
   }

   void OnLBDown(float var1, float var2) {
      if (!this._bMouseDrag && this.m_State != 0) {
         this._bMouseDrag = true;
         this.m_OldValue = this.m_Value;
         Vector var3 = super.m_Module.m_Cursor.cursorTex.GetPos();
         float var4 = this.GetSliderValueByPos(var3);
         if (this.m_Value != var4) {
            this.SetSliderByValue(var4);
            if (super.m_TargetWindow != null) {
               super.m_TargetWindow.SliderChanging(super.m_WindowID, this.m_Value);
            }
         }
      }
   }

   void OnMouseEnter(float var1, float var2) {
      if (this.m_State != 0 && this.CanProcessMouseMove()) {
         if (this.m_State != 2) {
            this.SetSliderState(2);
         }

         super.m_Module.SetCursor(1);
         super.m_bEnableKeyFocus = false;
      }
   }

   int GetValueDiscrete() {
      if (this.m_Discretisation <= 1) {
         return -1;
      }

      int var1 = (int)(this.m_Value * this.m_Discretisation);
      if (var1 >= this.m_Discretisation) {
         var1 = this.m_Discretisation - 1;
      }

      return var1;
   }

   void Init(KidsMenuModule var1, KidsMenuWindow var2, int var3, InitKidsSlider var4) {
      super.Init(var1, var3);
      super.m_TargetWindow = var2;
      this.bkTex[0] = this.AddWindowTexture(this.AddTexture(var4.sBkTexDisabled, true, true), var4.rBkTexDisabled, var4.sBkTexDisabled);
      this.bkTex[1] = this.AddWindowTexture(this.AddTexture(var4.sBkTexEnabled, true, true), var4.rBkTexEnabled, var4.sBkTexEnabled);
      this.bkTex[2] = this.AddWindowTexture(this.AddTexture(var4.sBkTexHighlighted, true, true), var4.rBkTexHighlighted, var4.sBkTexHighlighted);
      KidsMenuWindow var5 = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      this.sliderid = this.Add(var5);
      this.bnTex[0] = var5.AddWindowTexture(var5.AddTexture(var4.sBnTexDisabled, true, true), var4.rBnTexDisabled, var4.sBnTexDisabled);
      this.bnTex[1] = var5.AddWindowTexture(var5.AddTexture(var4.sBnTexEnabled, true, true), var4.rBnTexEnabled, var4.sBnTexEnabled);
      this.bnTex[2] = var5.AddWindowTexture(var5.AddTexture(var4.sBnTexHighlighted, true, true), var4.rBnTexHighlighted, var4.sBnTexHighlighted);
      var5.Init(var1, this, this.sliderid);
      float var6 = this.m_LeftBorder + this.m_Value * (this.m_RightBorder - this.m_LeftBorder);
      var5.SetSizeAndPos(var4.sliderwidth, var4.sliderheight, var6, var4.slidery, true);
      var5.Show();
      this.SetSizeAndPos(var4.width, var4.height, var4.xpos, var4.ypos, var4.bpos);
      this.RefreshSetSizeAndPos();
      this.Enable(true);
      this.SetSliderState(1);
      this.ActivateWindow();
      this.SetAlphaForAll(1.0F);
   }

   void OnMouseMove(float var1, float var2, int var3) {
      if (this.m_State != 0 && this.CanProcessMouseMove()) {
         if (var3 != 1 && this._bMouseDrag) {
            super.m_TargetWindow.SliderChanging(super.m_WindowID, this.m_Value);
            super.m_TargetWindow.SliderChanged(super.m_WindowID, this.m_OldValue, this.m_Value);
            this._bMouseDrag = false;
         }

         if (this._bMouseDrag) {
            Vector var4 = super.m_Module.m_Cursor.cursorTex.GetPos();
            float var5 = this.GetSliderValueByPos(var4);
            this.SetSliderByValue(var5);
            if (super.m_TargetWindow != null) {
               super.m_TargetWindow.SliderChanging(super.m_WindowID, this.m_Value);
            }
         }
      }
   }

   void SetDiscretisation(int var1) {
      if (var1 >= 0) {
         this.m_Discretisation = var1;
      }
   }
}
