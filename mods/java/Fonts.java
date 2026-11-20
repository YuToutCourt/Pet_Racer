public class Fonts {
   static final String FONT_MSSANSSERIF_08 = "MSSansSerif8";
   static final String FONT_MSSANSSERIF_09 = "MSSansSerif9";
   static final String FONT_MSSANSSERIF_10 = "MSSansSerif10";
   static final String FONT_MSSANSSERIF_12 = "MSSansSerif12";
   static final String FONT_MSSANSSERIF_14 = "MSSansSerif14";
   static final String FONT_MSSANSSERIF_16 = "MSSansSerif16";
   static final String FONT_MSSANSSERIF_18 = "MSSansSerif18";
   static final String FONT_MSSANSSERIF_20 = "MSSansSerif20";
   static final String FONT_MSSANSSERIF_26 = "MSSansSerif26";
   static final String FONT_MSSANSSERIF_36 = "MSSansSerif36";
   static final String FONT_TERMINAL = "FLPismoTechniczne22";
   static final String FONT_COURIER_10 = "Courier10";
   static final String FONT_COURIER_12 = "Courier12";
   static final String FONT_COURIER_14 = "Courier14";
   static java.util.Vector m_vFonts = null;
   static int m_nDefaultFont = -1;
   Module m_cModule = null;

   static void AddFont(String var0, String var1, int var2) {
      Fonts.Font var3 = new Fonts.Font();
      var3.m_sFontID = var0;
      var3.m_sTTFName = var1;
      var3.m_iHeight = var2;
      m_vFonts.addElement(var3);
   }

   static String GetDefaultFont() {
      int var0 = m_nDefaultFont;
      if (var0 < 0 || var0 >= m_vFonts.size()) {
         var0 = 0;
      }

      return m_vFonts.size() > 0 ? ((Fonts.Font)m_vFonts.elementAt(var0)).m_sFontID : null;
   }

   static int GetFontVectorInd(String var0) {
      for (int var1 = 0; var1 < m_vFonts.size(); var1++) {
         Fonts.Font var2 = (Fonts.Font)m_vFonts.elementAt(var1);
         if (var2.m_sFontID.equals(var0)) {
            return var1;
         }
      }

      return -1;
   }

   Fonts() {
      m_vFonts = new java.util.Vector();
      AddFont("MSSansSerif8", "MS Sans Serif", 8);
      AddFont("MSSansSerif10", "MS Sans Serif", 10);
      AddFont("MSSansSerif12", "MS Sans Serif", 12);
      AddFont("MSSansSerif16", "MS Sans Serif", 16);
      AddFont("MSSansSerif20", "MS Sans Serif", 20);
      AddFont("MSSansSerif26", "MS Sans Serif", 26);
      // [mod] polices pour les grandes résolutions (KidsText) ; ModVideo.GetTextFont ne les prend que si leur .dat existe
      AddFont("MSSansSerif18", "MS Sans Serif", 18);
      AddFont("MSSansSerif32", "MS Sans Serif", 32);
      AddFont("MSSansSerif40", "MS Sans Serif", 40);
      AddFont("FLPismoTechniczne22", "FL Pismo Techniczne", 22);
      AddFont("Courier10", "Courier", 10);
      AddFont("Courier12", "Courier", 12);
      AddFont("Courier14", "Courier", 14);
      this.SetDefaultFont("MSSansSerif10");
   }

   void Init(Module var1) {
      if (this.m_cModule != var1) {
         this.m_cModule = var1;
         if (this.m_cModule == null) {
            GameObject.CrashLog("[Font:AddFont] error: m_cModule==null");
         } else {
            for (int var2 = 0; var2 < m_vFonts.size(); var2++) {
               Fonts.Font var3 = (Fonts.Font)m_vFonts.elementAt(var2);
               this.m_cModule.LoadFont(var3.m_sFontID, var3.m_sTTFName, var3.m_iHeight);
            }
         }
      }
   }

   void SetDefaultFont(String var1) {
      for (int var2 = 0; var2 < m_vFonts.size(); var2++) {
         Fonts.Font var3 = (Fonts.Font)m_vFonts.elementAt(var2);
         if (var3.m_sFontID.equals(var1)) {
            m_nDefaultFont = var2;
            return;
         }
      }
   }

   static class Font {
      String m_sFontID = null;
      String m_sTTFName = null;
      int m_iHeight = -1;
   }
}
