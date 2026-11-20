// [mod] Texte d'écran (police de KidsText, taille selon la hauteur réelle) avec l'alignement au choix :
// KidsText.SetText centre toujours. Un texte « &Clé& » est traduit par le moteur depuis Data\Texts.scr
// (comme les crédits), ce qui garde les accents dans l'encodage des textes d'origine.
public class ModText extends KidsText {
   int m_nFlags = ESpriteText.HORIZ_MIDDLE | ESpriteText.VERT_MIDDLE;

   void SetAlignLeft() {
      this.m_nFlags = ESpriteText.HORIZ_LEFT | ESpriteText.VERT_MIDDLE;
   }

   void SetText(String s) {
      this.SetFont(ModVideo.GetTextFont());
      this.SetTextColor(this.r, this.g, this.b, this.a);
      this.SetTextPosFlags(this.m_nFlags);
      if (s == null) {
         s = "null";
      }

      this.m_Text = s;
      this.DisplayText(this.m_Text);
   }
}
