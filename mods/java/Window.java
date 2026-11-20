public class Window extends Sprite {
   ContextMenu wndContextMenu = null;
   static Window wndCurrentModal = null;
   Window wndParent = null;
   Window wndRoot = null;
   Module module = null;
   java.util.Vector vChildWindows = new java.util.Vector();
   boolean bProcessingMouse = false;
   boolean bProcessingKb = false;
   boolean bProcessingJoy = false;
   String sToolTipText = null;
   float fToolTipTime = 0.5F;
   boolean bToolTipEnabled = false;
   boolean bToolTipVisible = false;
   float fMouseMoveTime = 0.0F;
   float m_lxpos = 0.0F;
   float m_lypos = 0.0F;
   float m_lwidth = 1.0F;
   float m_lheight = 1.0F;
   boolean m_blpos = false;

   void SetContextMenu(ContextMenu var1) {
      if (this.wndContextMenu != null) {
         this.DestroyMenu();
      }

      if (this.module == null) {
         GameObject.LogLine("Musisz ustawic modul, zanim zawolasz SetContextMenu dla " + this);
      } else {
         this.wndContextMenu = var1;
         if (var1 != null) {
            this.AddChildWnd(var1, this.wndRoot);
            var1.SetModal();
            var1.MoveTop();
            Cursor var2 = this.module.GetCursor();
            if (var2 != null) {
               var2.MoveTop();
            }
         }
      }
   }

   boolean HitTest(Vector var1) {
      Vector var2 = this.GetPos();
      Vector var3 = this.GetSize();
      return var2 == null || var3 == null ? false : var1.fX >= var2.fX && var1.fX < var2.fX + var3.fX && var1.fY >= var2.fY && var1.fY < var2.fY + var3.fY;
   }

   boolean CanProcessMouseBtns() {
      return wndCurrentModal == null ? true : wndCurrentModal == this.wndRoot;
   }

   void SetToolTip(String var1) {
      if (var1 == null) {
         this.bToolTipEnabled = false;
      } else {
         this.bToolTipEnabled = var1.length() > 0;
      }

      this.sToolTipText = var1;
      if (this.bToolTipVisible) {
         this.DisplayToolTip();
      }
   }

   Window SetModal() {
      Cursor var1 = null;
      this.DisableCallMethod("DisplayToolTip");
      if (this.module != null) {
         var1 = this.module.GetCursor();
      }

      if (var1 != null) {
         var1.DisplayToolTip("");
         this.bToolTipVisible = false;
      }

      Window var2 = wndCurrentModal;
      this.SetRoot(this);
      wndCurrentModal = this;
      return var2;
   }

   void RemoveChildWnd(Window var1) {
      this.vChildWindows.removeElement(var1);
   }

   void OnKeyReleased(int var1, char var2) {
   }

   void OnRBUp(float var1, float var2) {
   }

   void SetSizeAndPos(float var1, float var2, float var3, float var4, boolean var5) {
      this.m_lxpos = var3;
      this.m_lypos = var4;
      this.m_lwidth = var1;
      this.m_lheight = var2;
      this.m_blpos = var5;
      int var6;
      int var7;
      if (this.wndParent == null) {
         var6 = GameObject.GetScreenWidth();
         var7 = GameObject.GetScreenHeight();
      } else {
         Vector var8 = this.wndParent.GetSize();
         var6 = (int)var8.fX;
         var7 = (int)var8.fY;
      }

      if (var6 > 0 && var7 > 0) {
         // [mod] écran large : menus dans une zone 4:3 centrée, gadgets du HUD aux bords réels (ModVideo.Layout)
         int nMode = ModVideo.GetLayout(this);
         int nZoneW = var6;
         int nOffX = 0;
         if (nMode != ModVideo._LAYOUT_STRETCH) {
            nZoneW = ModVideo.GetZoneWidth(var6, var7);
         }

         if (nMode == ModVideo._LAYOUT_ZONE) {
            nOffX = (var6 - nZoneW) / 2;
         }

         int var12 = (int)(var1 * nZoneW);
         int var9 = (int)(var2 * var7);
         int var10;
         int var11;
         if (var5) {
            var10 = (int)(var3 * nZoneW) - var12 / 2;
            var11 = (int)(var4 * var7) - var9 / 2;
         } else {
            var10 = (int)(var3 * nZoneW);
            var11 = (int)(var4 * var7);
         }

         if (nMode == ModVideo._LAYOUT_ZONE) {
            var10 += nOffX;
         } else if (nMode == ModVideo._LAYOUT_ANCHOR) {
            var10 += ModVideo.GetAnchorShift(var6, nZoneW, var5 ? var3 : var3 + var1 / 2.0F);
         }

         if (this.wndParent == null) {
            this.SetPos(new Vector(var10, var11, 0.0F));
         } else {
            this.SetRelativePos(new Vector(var10, var11, 0.0F));
         }

         this.SetSize(new Vector(var12, var9, 0.0F));
      }
   }

   void RefreshSetSizeAndPos() {
      this.SetSizeAndPos(this.m_lwidth, this.m_lheight, this.m_lxpos, this.m_lypos, this.m_blpos);
      Window var1 = null;
      int var2 = this.vChildWindows.size();

      for (int var3 = 0; var3 < var2; var3++) {
         var1 = (Window)this.vChildWindows.elementAt(var3);
         if (var1 != null) {
            var1.RefreshSetSizeAndPos();
         }
      }
   }

   void OnButtonRB(Button var1) {
      if (var1 != null) {
         if (this.wndParent != null) {
            this.wndParent.CallOnButtonRB(var1);
         }
      }
   }

   public void SetPos(Vector var1) {
      super.SetPos(var1);
      this.DisableCallMethod("DisplayToolTip");
      Cursor var2 = null;
      if (this.module != null) {
         var2 = this.module.GetCursor();
      }

      if (var2 != null && this.bToolTipVisible) {
         var2.DisplayToolTip("");
         this.bToolTipVisible = false;
      }
   }

   void SetAlphaForAll(float var1) {
      this.SetAlpha(var1);
      Window var2 = null;
      int var3 = this.vChildWindows.size();

      for (int var4 = 0; var4 < var3; var4++) {
         var2 = (Window)this.vChildWindows.elementAt(var4);
         if (var2 != null) {
            var2.SetAlphaForAll(var1);
         }
      }
   }

   void OnButtonHighlight(Button var1) {
      if (var1 != null) {
         if (this.wndParent != null) {
            this.wndParent.CallOnButtonHighlight(var1);
         }
      }
   }

   public void Hide() {
      this.DisableCallMethod("DisplayToolTip");
      Cursor var1 = null;
      if (this.module != null) {
         var1 = this.module.GetCursor();
      }

      if (var1 != null && this.bToolTipVisible) {
         var1.DisplayToolTip("");
         this.bToolTipVisible = false;
      }

      super.ProcessMouse(false);
      super.ProcessKb(false);
      super.ProcessJoy(false);
      super.Hide();
   }

   public void ProcessKb(boolean var1) {
      super.ProcessKb(var1);
      this.bProcessingKb = var1;
   }

   public void RestoreTextures() {
   }

   void SetRoot(Window var1) {
      this.wndRoot = var1;
      int var2 = this.vChildWindows.size();

      for (int var4 = 0; var4 < var2; var4++) {
         Window var3 = (Window)this.vChildWindows.elementAt(var4);
         if (var3 != null) {
            var3.SetRoot(var1);
         }
      }
   }

   void OnButtonDoubleClick(Button var1) {
      if (var1 != null) {
         if (this.wndParent != null) {
            this.wndParent.CallOnButtonDoubleClick(var1);
         }
      }
   }

   Vector GetLogicalPos(Vector var1) {
      Vector var2 = this.GetPos();
      Vector var3 = this.GetSize();
      float var4 = var1.fX - var2.fX;
      if (var3.fX != 0.0F) {
         var4 /= var3.fX;
      } else {
         var4 = 0.0F;
      }

      float var5 = var1.fY - var2.fY;
      if (var3.fY != 0.0F) {
         var5 /= var3.fY;
      } else {
         var5 = 0.0F;
      }

      return new Vector(var4, var5, 0.0F);
   }

   ContextMenu CreateContextMenu(float var1, float var2) {
      return null;
   }

   void OnMouseMove(float var1, float var2, int var3) {
      if (this.bToolTipEnabled) {
         Cursor var4 = null;
         if (this.module != null) {
            var4 = this.module.GetCursor();
         }

         if (var4 != null && this.bToolTipVisible) {
            var4.DisplayToolTip("");
            this.bToolTipVisible = false;
         }

         this.DisableCallMethod("DisplayToolTip");
         this.CallMethodIn("DisplayToolTip", this.fToolTipTime);
      }
   }

   public native void CallOnButtonHighlight(Button var1);

   void OnButton(Button var1) {
      if (var1 != null) {
         if (this.wndParent != null) {
            this.wndParent.CallOnButton(var1);
         }
      }
   }

   boolean IsCursorInWindow() {
      if (!this.CanProcessMouseMove()) {
         return false;
      }

      Vector var1 = this.GetPos();
      Vector var2 = this.GetSize();
      Vector var3 = this.GetMousePos();
      return var3.fX >= var1.fX && var3.fX <= var1.fX + var2.fX && var3.fZ >= var1.fY && var3.fZ <= var1.fY + var2.fY;
   }

   void AddChildWnd(Window var1, Window var2) {
      if (var1.wndParent != null) {
         var1.wndParent.RemoveChildWnd(var1);
      }

      this.vChildWindows.addElement(var1);
      var1.SetRoot(var2);
      var1.wndParent = this;
   }

   public native void CallOnButtonRB(Button var1);

   void SetModule(Module var1) {
      this.module = var1;
   }

   void DestroyMenu() {
      if (this.wndContextMenu != null) {
         this.wndContextMenu.Destroy();
         this.RemoveChildWnd(this.wndContextMenu);
         this.module.RemoveSprite(this.wndContextMenu);
         this.wndContextMenu.delete();
         this.wndContextMenu = null;
         this.ClearModal();
      }
   }

   void OnMouseLeave(float var1, float var2) {
      if (this.bToolTipEnabled) {
         this.DisableCallMethod("DisplayToolTip");
         Cursor var3 = null;
         if (this.module != null) {
            var3 = this.module.GetCursor();
         }

         if (var3 != null && this.bToolTipVisible) {
            var3.DisplayToolTip("");
            this.bToolTipVisible = false;
         }
      }
   }

   void Destroy() {
      this.ReleaseTextures();
      Window var1 = null;

      while (this.vChildWindows.size() > 0) {
         var1 = (Window)this.vChildWindows.elementAt(0);
         this.vChildWindows.removeElementAt(0);
         var1.Destroy();
         var1.delete();
         var1 = null;
      }

      this.DeleteToolTip();
      if (this.module != null) {
         this.module.RemoveSprite(this);
      } else {
         this.GetModule().RemoveSprite(this);
      }
   }

   public void ProcessMouse(boolean var1) {
      super.ProcessMouse(var1);
      this.bProcessingMouse = var1;
   }

   void ClearModal() {
      this.DisableCallMethod("DisplayToolTip");
      Cursor var1 = null;
      if (this.module != null) {
         var1 = this.module.GetCursor();
      }

      if (var1 != null) {
         var1.DisplayToolTip("");
         this.bToolTipVisible = false;
      }

      wndCurrentModal = null;
   }

   void DisplayToolTip() {
      Cursor var1 = null;
      if (this.module != null) {
         var1 = this.module.GetCursor();
      }

      if (var1 != null) {
         if (this.sToolTipText != null) {
            if (this.sToolTipText.length() > 0) {
               if (this.IsVisible()) {
                  var1.DisplayToolTip(this.sToolTipText);
                  this.bToolTipVisible = true;
                  return;
               }

               this.bToolTipVisible = false;
               return;
            }

            this.bToolTipVisible = false;
            return;
         }

         this.bToolTipVisible = false;
      }
   }

   void DeleteToolTip() {
      this.DisableCallMethod("DisplayToolTip");
      Cursor var1 = null;
      if (this.module != null) {
         var1 = this.module.GetCursor();
      }

      if (var1 != null && this.bToolTipVisible) {
         var1.DisplayToolTip("");
         this.bToolTipVisible = false;
      }
   }

   Vector GetCenterPos() {
      Vector var1 = this.GetPos();
      Vector var2 = this.GetSize();
      return new Vector(var1.fX + var2.fX / 2.0F, var1.fY + var2.fY / 2.0F, var1.fZ + var2.fZ / 2.0F);
   }

   boolean CanProcessMouseMove() {
      return wndCurrentModal == null ? true : wndCurrentModal == this.wndRoot;
   }

   public void ProcessJoy(boolean var1) {
      super.ProcessJoy(var1);
      this.bProcessingJoy = var1;
   }

   public native void CallOnButtonDoubleClick(Button var1);

   public void Show() {
      super.ProcessMouse(this.bProcessingMouse);
      super.ProcessKb(this.bProcessingKb);
      super.ProcessJoy(this.bProcessingJoy);
      super.Show();
   }

   public void ReleaseTextures() {
   }

   void OnCreate() {
      this.SetFont("MSSansSerif12");
      this.wndRoot = this;
   }

   public native void CallOnButton(Button var1);
}
