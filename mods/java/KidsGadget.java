// [mod] icône du Missile (bonus du mod) en 22e position (_GADGET_MOD_MISSILE), choisie par KidsHud.ModGadgetIcon.
// Image : mods/art/make_hud_icons.py.
public class KidsGadget extends KidsMenuWindow {
   static String[] m_GadgetTexturesNames = new String[]{
      "Data/Textures/Hud/Turbo.png",
      "Data/Textures/Hud/Ochrona.png",
      "Data/Textures/Hud/TireSpikes.png",
      "Data/Textures/Hud/Kowadlo.png",
      "Data/Textures/Hud/Banan.png",
      "Data/Textures/Hud/Dynamit.png",
      "Data/Textures/Hud/Katapulta.png",
      "Data/Textures/Hud/Kokos.png",
      "Data/Textures/Hud/Ciacho.png",
      "Data/Textures/Hud/PistoletKorek.png",
      "Data/Textures/Hud/PistoletZamrazajacy.png",
      "Data/Textures/Hud/Spray.png",
      "Data/Textures/Hud/Puszka.png",
      "Data/Textures/Hud/Rakieta.png",
      "Data/Textures/Hud/sniezka.png",
      "Data/Textures/Hud/Bazuka.png",
      "Data/Textures/Hud/ufo.png",
      "Data/Textures/Hud/worek.png",
      "Data/Textures/Hud/Butla.png",
      "Data/Textures/Hud/Arbuz.png",
      "Data/Textures/Hud/PistoletWoda.png",
      "Data/Textures/Hud/Missile.png" // [mod]
   };
   static int _GADGET_NONE = -1;
   static int _GADGET_TURBO = 0;
   static int _GADGET_PROTECTION = 1;
   static int _GADGET_TIRESPIKES = 2;
   static int _GADGET_ANVIL = 3;
   static int _GADGET_BANANA = 4;
   static int _GADGET_BOMB = 5;
   static int _GADGET_CATAPULT = 6;
   static int _GADGET_COCONUT = 7;
   static int _GADGET_COOKIE = 8;
   static int _GADGET_CORKPISTOL = 9;
   static int _GADGET_FREEZEPISTOL = 10;
   static int _GADGET_FUNNYGAS = 11;
   static int _GADGET_OILCAN = 12;
   static int _GADGET_ROCKET = 13;
   static int _GADGET_SNOWBALL = 14;
   static int _GADGET_TOMATOLAUNCHER = 15;
   static int _GADGET_UFO = 16;
   static int _GADGET_WASPBAG = 17;
   static int _GADGET_WATERBOTTLE = 18;
   static int _GADGET_WATERMELON = 19;
   static int _GADGET_WATERPISTOL = 20;
   static int _GADGET_MOD_MISSILE = 21; // [mod]
   static final float width = 0.1F;
   static final float height = 0.12F;
   static final float xpos = 0.1F;
   static final float ypos = 0.9F;
   static final float widthmulti = 0.05F;
   static final float heightmulti = 0.06F;
   static final float xpos0 = 0.1F;
   static final float ypos0 = 0.45F;
   static final float xpos1 = 0.1F;
   static final float ypos1 = 0.95F;

   void InitHud(Module var1, int var2, boolean var3) {
      super.InitHud(var1);

      for (int var4 = 0; var4 < m_GadgetTexturesNames.length; var4++) {
         super.m_TexturesList
            .AddTexture(this.AddTexture(m_GadgetTexturesNames[var4], true, true), new Rect(0.0F, 0.0F, 1.0F, 1.0F), m_GadgetTexturesNames[var4]);
      }

      if (var3) {
         if (var2 == 0) {
            this.SetSizeAndPos(0.05F, 0.06F, 0.1F, 0.45F, true);
         } else {
            this.SetSizeAndPos(0.05F, 0.06F, 0.1F, 0.95F, true);
         }
      } else {
         this.SetSizeAndPos(0.1F, 0.12F, 0.1F, 0.9F, true);
      }

      this.RefreshSetSizeAndPos();
      this.SetAlpha(1.0F);
   }

   void ShowGadget(int var1) {
      if (var1 >= 0 && var1 < m_GadgetTexturesNames.length) {
         this.Show();
         super.m_TexturesList.SetTexture(this, var1);
      } else {
         this.Hide();
      }
   }
}
