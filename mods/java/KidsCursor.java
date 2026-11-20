public class KidsCursor extends Cursor {
   static final int _SHAPE_IDLE = 0;
   static final int _SHAPE_ACTIVE = 1;
   static final int _MAX_CURSOR_SHAPES = 2;
   TextureList m_Shapes = new TextureList();
   String m_ActiveTexName = "Data/Textures/Cursors/cursor.png";
   String m_InactiveTexName = "Data/Textures/Cursors/cursor.png";
   KidsMenuModule m_Mod = null;
   boolean m_bCursorDeactivated = false;
   int nCurentCursor = -1;
   int nCursorHeight = 64;
   int nCursorWidth = 64;

   void DeactivateCursor() {
      this.m_bCursorDeactivated = true;
      super.cursorTex.Hide();
   }

   void SetCursor(int var1) {
      if (var1 >= 0 && var1 < this.m_Shapes.GetTexturesNum()) {
         if (var1 != this.nCurentCursor) {
            this.nCurentCursor = var1;
            this.m_Shapes.SetTexture(super.cursorTex, var1);
         }
      }
   }

   void Destroy() {
      this.m_Shapes.ReleaseTextures(super.cursorTex);
      super.Destroy();
   }

   void ActivateCursor() {
      this.m_bCursorDeactivated = false;
      super.cursorTex.Show();
   }

   void Init(Module var1) {
      super.cursorTex = (Sprite)var1.CreateObject("Sprite");
      this.m_Shapes.AddTexture(super.cursorTex.AddTexture(this.m_InactiveTexName, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.m_InactiveTexName);
      this.m_Shapes.AddTexture(super.cursorTex.AddTexture(this.m_ActiveTexName, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.m_ActiveTexName);
      this.nCurentCursor = 0;
      this.m_Shapes.SetTexture(super.cursorTex, this.nCurentCursor);
      super.cursorTex.SetSize(new Vector(ModVideo.ScaleUi(this.nCursorWidth), ModVideo.ScaleUi(this.nCursorHeight), 0.0F)); // [mod] curseur à l'échelle de l'écran
      super.cursorTex.SetPos(new Vector(0.0F, 0.0F, 0.0F));
      super.cursorTex.ProcessMouse(false);
      super.cursorTex.Show();
      var1.AddSprite(super.cursorTex);
      super.cursorTex.MoveTop();
      this.InitToolTip(var1, "data\\panel\\black.png", new Rect(0.0F, 0.0F, 1.0F, 1.0F), "MSSansSerif14", 0.5F, 0.5F, 0.5F, 1.0F);
      super.vCursorSize.fX = ModVideo.ScaleUi(21); // [mod]
      super.vCursorSize.fY = ModVideo.ScaleUi(21);
      this.ProcessMouse(true);
      this.SetPos(new Vector(0.0F, 0.0F, 0.0F));
      this.SetSize(new Vector(GameObject.GetScreenWidth(), GameObject.GetScreenHeight(), 0.0F));
      var1.AddSprite(this);
      this.m_Mod = (KidsMenuModule)var1;
   }

   void OnMouseMove(float var1, float var2, int var3) {
      if (this.m_bCursorDeactivated && this.m_Mod != null) {
         this.m_Mod.ActivateCursor();
      }

      switch (this.nCurentCursor) {
         case 0:
         case 1:
            super.cursorTex.SetPos(new Vector(var1, var2, 0.0F));
            return;
      }
   }

   void OnDestroy() {
      if (super.toolTip != null) {
         super.toolTip.delete();
         super.toolTip = null;
      }
   }

   // [mod] après un changement de résolution, le curseur garde sa taille relative à l'écran
   void OnChangeResolution() {
      super.OnChangeResolution();
      if (super.cursorTex != null) {
         super.cursorTex.SetSize(new Vector(ModVideo.ScaleUi(this.nCursorWidth), ModVideo.ScaleUi(this.nCursorHeight), 0.0F));
         super.vCursorSize.fX = ModVideo.ScaleUi(21);
         super.vCursorSize.fY = ModVideo.ScaleUi(21);
      }
   }
}
