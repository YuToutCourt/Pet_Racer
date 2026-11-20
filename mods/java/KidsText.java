public class KidsText extends KidsMenuWindow {
   String m_Text = "default";
   static String[] m_Fonts = new String[]{"MSSansSerif16", "MSSansSerif18", "MSSansSerif20"};
   float r = 1.0F;
   float g = 1.0F;
   float b = 1.0F;
   float a = 1.0F;

   void ColorWhite() {
      this.r = 1.0F;
      this.g = 1.0F;
      this.b = 1.0F;
      this.a = 1.0F;
   }

   String GetText() {
      return this.m_Text;
   }

   void SetColor(float var1, float var2, float var3) {
      this.r = var1;
      this.g = var2;
      this.b = var3;
      this.a = 1.0F;
   }

   public void OnCreate() {
      super.OnCreate();
   }

   void ColorRed() {
      this.r = 1.0F;
      this.g = 0.0F;
      this.b = 0.0F;
      this.a = 1.0F;
   }

   void SetText(String var1) {
      this.SetFont(ModVideo.GetTextFont()); // [mod] taille selon la hauteur réelle de l'écran
      this.SetTextColor(this.r, this.g, this.b, this.a);
      this.SetTextPosFlags(ESpriteText.HORIZ_MIDDLE | ESpriteText.VERT_MIDDLE);
      if (var1 == null) {
         var1 = "null";
      }

      this.m_Text = var1;
      this.DisplayText(this.m_Text);
   }
}
