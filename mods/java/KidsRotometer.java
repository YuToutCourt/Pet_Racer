class KidsRotometer extends KidsMenuWindow {
   static final float MAX_DELTA = 0.1F;
   static final float width = 0.10050001F;
   static final float height = 0.118590005F;
   static final float xpos = 0.833F;
   static final float ypos = 0.939F;
   static final float widthmulti = 0.125F;
   static final float heightmulti = 0.15F;
   static final float xpos0 = 0.828F;
   static final float ypos0 = 0.424F;
   static final float xpos1 = 0.828F;
   static final float ypos1 = 0.924F;
   static final float wndArrowWidth = 0.8925F;
   static final float wndArrowHeight = 1.05F;
   static final float wndArrowPosX = 0.66F;
   static final float wndArrowPosY = 0.68F;
   String m_sBackground = "Data/Textures/Hud/Roto_ba.png";
   String m_sArrowTex = "Data/Textures/Hud/Speed_ar.png";
   KidsMenuWindow wndArrow = null;
   float m_OldRotation = 0.0F;

   void InitHud(Module var1, int var2, boolean var3) {
      super.InitHud(var1);
      super.m_TexturesList.AddTexture(this.AddTexture(this.m_sBackground, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.m_sBackground);
      if (this.wndArrow == null) {
         this.wndArrow = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
         this.Add(this.wndArrow);
         this.wndArrow.InitHud(var1);
         this.wndArrow.AddWindowTexture(this.wndArrow.AddTexture(this.m_sArrowTex, true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), this.m_sArrowTex);
         this.wndArrow.SetSizeAndPos(0.8925F, 1.05F, 0.66F, 0.68F, true);
         this.wndArrow.SetWindowTexture(0);
         this.wndArrow.Show();
      }

      if (var3) {
         if (var2 == 0) {
            this.SetSizeAndPos(0.125F, 0.15F, 0.828F, 0.424F, true);
         } else {
            this.SetSizeAndPos(0.125F, 0.15F, 0.828F, 0.924F, true);
         }
      } else {
         this.SetSizeAndPos(0.10050001F, 0.118590005F, 0.833F, 0.939F, true);
      }

      this.RefreshSetSizeAndPos();
      super.m_TexturesList.SetTexture(this, 0);
      this.SetAlpha(1.0F);
   }

   // [mod] l'aiguille avançait d'au plus MAX_DELTA par image : à 500 images/s elle suivait les changements de
   // vitesse instantanément (sauts nerveux aux passages de rapport). On garde la vitesse qu'elle avait à 30 images/s.
   static final float _MAX_DELTA_PER_SECOND = 3.0F;

   void SetRotations(float var1) {
      float var2 = var1 - this.m_OldRotation;
      float var3 = 0.0F;
      float fMax = RacerModule.m_Mod != null ? _MAX_DELTA_PER_SECOND * RacerModule.m_Mod.m_fFrameTime : 0.1F;
      if (var2 >= -fMax && var2 <= fMax) {
         var3 = var1;
      } else if (var2 > 0.0F) {
         var3 = this.m_OldRotation + fMax;
      } else {
         var3 = this.m_OldRotation - fMax;
      }

      if (var3 < 0.0F) {
         var3 = 0.0F;
      }

      if (var3 > 1.0F) {
         var3 = 1.0F;
      }

      this.m_OldRotation = var3;
      float var4 = 140.0F * var3 - 120.0F;
      this.wndArrow.SetRotation(var4);
   }
}
