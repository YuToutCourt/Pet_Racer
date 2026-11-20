// [mod] Bande de côté des écrans dont le cadre 4:3 est ouvert sur la 3D (choix de l'animal, podium, provocation du
// boss, tous sur MenuCharakter.png). En 16:9, le cadre est centré (ModVideo._LAYOUT_ZONE) et laissait voir le décor 3D
// sur les côtés : deux bandes, enfants du cadre, prolongent son damier jusqu'aux bords de l'écran.
// Textures : mods/art/make_side_fill.py (512×512, damier sur 512×480, à la même échelle que l'image 640×480 du cadre).
class ModSideFill extends KidsMenuWindow {
   static final String _TEX_LEFT = "Data/Textures/Menu/b/MenuCharakterLeft.png";
   static final String _TEX_RIGHT = "Data/Textures/Menu/b/MenuCharakterRight.png";
   static final float _TEX_SIZE = 512.0F;  // côté de la texture
   static final float _TEX_H = 480.0F;     // hauteur utile (comme l'image du cadre)
   static final float _FRAME_W = 640.0F;   // largeur de l'image du cadre
   boolean m_bRight = false;
   Rect m_TexRect = new Rect(0.0F, 0.0F, 1.0F, _TEX_H / _TEX_SIZE); // gardé par TextureList : modifié sur place
   int m_iTex = -1;

   // ajoute les deux bandes au cadre (à appeler dans Init, avant son RefreshSetSizeAndPos)
   static void AddTo(KidsMenuWindow wnd, KidsMenuModule mod) {
      for (int i = 0; i < 2; i++) {
         ModSideFill s = (ModSideFill)wnd.CreateObject("ModSideFill");
         s.m_bRight = i == 1;
         s.Init(mod, wnd.Add(s));
         String sTex = s.m_bRight ? _TEX_RIGHT : _TEX_LEFT;
         s.m_iTex = s.AddWindowTexture(s.AddTexture(sTex, true, true), s.m_TexRect, sTex);
         s.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
         s.Show();
      }
   }

   // la place ne dépend que de l'écran : de chaque côté, l'espace entre le cadre centré et le bord
   void SetSizeAndPos(float w, float h, float x, float y, boolean bCentered) {
      this.m_lwidth = w;
      this.m_lheight = h;
      this.m_lxpos = x;
      this.m_lypos = y;
      this.m_blpos = bCentered;
      if (this.wndParent == null) {
         return;
      }

      Vector vZone = this.wndParent.GetSize();
      int nZoneW = (int)vZone.fX;
      int nZoneH = (int)vZone.fY;
      int nScreenW = GameObject.GetScreenWidth();
      int nLeft = (nScreenW - nZoneW) / 2; // même arrondi que Window.SetSizeAndPos (_LAYOUT_ZONE)
      int nBand = this.m_bRight ? nScreenW - nZoneW - nLeft : nLeft;
      if (nZoneW <= 0 || nBand <= 0) {
         this.SetSize(new Vector(0.0F, 0.0F, 0.0F)); // écran 4:3 ou plus étroit : rien à couvrir
         return;
      }

      this.SetRelativePos(new Vector(this.m_bRight ? nZoneW : -nBand, 0.0F, 0.0F));
      this.SetSize(new Vector(nBand, nZoneH, 0.0F));

      // même échelle que le cadre : nBand pixels d'écran = nBand × 640 / nZoneW pixels d'image, côté cadre de la texture
      float fU = nBand * _FRAME_W / nZoneW / _TEX_SIZE;
      if (fU > 1.0F) {
         fU = 1.0F; // écran plus large que 32:9 : la texture s'étire
      }

      this.m_TexRect.fLeft = this.m_bRight ? 0.0F : 1.0F - fU;
      this.m_TexRect.fRight = this.m_bRight ? fU : 1.0F;
      if (this.m_iTex >= 0) {
         this.SetWindowTexture(this.m_iTex);
      }
   }
}
