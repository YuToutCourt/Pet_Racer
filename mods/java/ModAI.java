// [mod] IA améliorée (chantier IA, notes/ia.md) : réglages par difficulté et élastique.
// La conduite elle-même est dans VehicleAI (branches « Mod… ») ; le niveau 0..5 de VehicleAI reste celui de
// Techland (précision, ramassage et délai des bonus, virage, dérapage), la difficulté choisit ce niveau.
// Difficulté « d'origine » (ModOptions._AI_ORIGINAL) : VehicleAI, RaceParams et CalcPos font exactement comme le
// jeu de base.
class ModAI {
   static final String[] _MODE_NAMES = new String[]{"d'origine", "auto", "facile", "normal", "difficile", "expert"};
   // niveau VehicleAI imposé par la difficulté ; -1 = celui que donne le circuit (0..3, 4 en extra / aventure rejouée)
   static final int[] _MODE_LEVEL = new int[]{-1, -1, 0, 2, 4, 5};
   // vitesse maxi des IA par difficulté (avant élastique et caractère). Choix du 2026-11-20 : une IA forte conduit
   // mieux, elle ne va pas plus vite ; au-dessus de « facile », la même vitesse que le joueur (avant : x1,03 en
   // difficile, x1,06 en expert)
   static final float[] _MODE_SPEED = new float[]{1.0F, 1.0F, 0.95F, 1.0F, 1.0F, 1.0F};
   // élastique : gain maxi d'une IA derrière le premier humain, perte maxi d'une IA devant lui
   // (l'original : +40 % / -40 %, pleine force dès 1 à 5 % de la course d'écart). Plus aucun gain en difficile et
   // expert (avant +25 % : « elles vont juste plus vite pour te battre »), un petit en auto / facile / normal.
   static final float[] _MODE_BOOST = new float[]{0.4F, 0.1F, 0.1F, 0.08F, 0.0F, 0.0F};
   static final float[] _MODE_SLOW = new float[]{0.4F, 0.2F, 0.3F, 0.15F, 0.05F, 0.0F};
   // tenue de route de l'IA améliorée, à tous les niveaux : x vitesse de rotation et x dérapage. 1 = exactement la
   // voiture du joueur (l'original donnait aux IA jusqu'à x1,4 en rotation et x0,4 en dérapage selon le niveau, et
   // 2 niveaux de plus à la traîne)
   static final float _AI_TURN = 1.0F;
   static final float _AI_SLIDE = 1.0F;
   // écart (en tours) où l'élastique commence, et où il est à pleine force
   static final float _CATCHUP_MIN_LAPS = 0.02F;
   static final float _CATCHUP_FULL_LAPS = 0.25F;

   static boolean IsImproved() {
      return Mode() != ModOptions._AI_ORIGINAL;
   }

   static int Mode() {
      // [mod] en ligne : la difficulté choisie par l'hôte du salon (ModNet.m_nAiMode), la même dans tous les jeux, y
      // compris chez celui qui reprend les IA si l'hôte part en course
      // [mod] entraînement des IA (ModTraining) : la difficulté de la passe en cours
      int n = ModTraining.m_bRunning ? ModTraining.AiMode() : ModNet.IsRaceOnline() ? ModNet.m_nAiMode : ModOptions.m_nAiMode;
      return n >= 0 && n < _MODE_NAMES.length ? n : ModOptions._AI_AUTO;
   }

   static boolean IsFixedLevel() {
      return _MODE_LEVEL[Mode()] >= 0;
   }

   // niveau VehicleAI à appliquer, à partir de celui que demande le circuit
   static int EffectiveLevel(int nTrackLevel) {
      int n = _MODE_LEVEL[Mode()];
      return n >= 0 ? n : nTrackLevel;
   }

   static float Clamp01(float f) {
      return f < 0.0F ? 0.0F : (f > 1.0F ? 1.0F : f);
   }

   // courbe douce 0..1 (pas d'à-coup quand l'écart franchit un seuil)
   static float Smooth(float t) {
      t = Clamp01(t);
      return t * t * (3.0F - 2.0F * t);
   }

   // élastique de l'IA améliorée : fraction de vitesse en plus (> 0) ou en moins (< 0).
   // fGapLaps > 0 : l'IA est derrière le premier humain de fGapLaps tours.
   static float CatchUp(float fGapLaps) {
      if (ModTraining.m_bRunning) {
         return 0.0F; // [mod] entraînement : pas d'humain, l'écart serait mesuré à la moyenne des IA
      }

      int m = Mode();
      float a = fGapLaps < 0.0F ? -fGapLaps : fGapLaps;
      float t = Smooth((a - _CATCHUP_MIN_LAPS) / (_CATCHUP_FULL_LAPS - _CATCHUP_MIN_LAPS));
      return fGapLaps > 0.0F ? _MODE_BOOST[m] * t : -_MODE_SLOW[m] * t;
   }

   static String F(float f) {
      // une décimale ou deux, sans String.format (JRE 1.3)
      int n = Math.round(f * 100.0F);
      String s = n < 0 ? "-" : "";
      n = Math.abs(n);
      return s + n / 100 + "." + (n % 100 < 10 ? "0" : "") + n % 100;
   }
}
