// [mod] Limiteur d'images (chantier 2, notes/pc_moderne.md).
// Le jeu n'a ni limite ni VSync : sur un PC moderne il tourne à 400-500 images/s, carte graphique et un cœur du
// processeur à fond. Frame() est appelé une fois par image (RacerGame.OnFrame) et attend que 1/N s se soit écoulée
// depuis l'image précédente (N = ModOptions.m_nFpsLimit, 0 = sans limite).
// Attente : Thread.sleep(1) tant qu'il reste plus de temps qu'un sleep n'en prend vraiment, puis Thread.yield()
// jusqu'à l'heure exacte. Si Windows arrondit les sleep à 15,6 ms, on n'attend plus qu'avec yield (plus précis,
// mais un cœur reste occupé).
class ModFrameLimiter {
   static final int _CLOCK_NONE = 0;
   static final int _CLOCK_ENGINE = 1;
   static final int _CLOCK_JAVA = 2;
   // une horloge plus grossière que ça ne sert pas à limiter (s)
   static final float _MAX_CLOCK_STEP = 0.002F;
   // jamais plus d'un quart de seconde d'attente par image, quoi qu'il arrive
   static final long _GUARD_MS = 250L;
   static int m_nClock = _CLOCK_NONE;
   static long m_lJavaBase = 0L;
   static float m_fLast = -1.0F;
   // durée réelle mesurée d'un Thread.sleep(1) (s)
   static float m_fSleepCost = 0.002F;
   // statistiques pour crash.log (RacerModule) : temps passé à attendre
   static float m_fWaitTotal = 0.0F;

   // Horloge du limiteur, en secondes. Choisie au premier appel (Calibrate).
   static float Now() {
      if (m_nClock == _CLOCK_NONE) {
         Calibrate();
      }

      return m_nClock == _CLOCK_ENGINE ? GameObject.GetAbsoluteTime() * m_fEngineScale : (System.currentTimeMillis() - m_lJavaBase) / 1000.0F;
   }

   // unité de GetAbsoluteTime() : 1 si elle compte en secondes, 0,001 en millisecondes (mesuré par Calibrate)
   static float m_fEngineScale = 1.0F;

   // Mesure les deux horloges disponibles et garde la plus fine. GetAbsoluteTime() (moteur) est normalement
   // précise, mais on vérifie qu'elle avance bien pendant une image (et pas seulement d'une image à l'autre),
   // et dans quelle unité (comparée à l'horloge Java sur 40 ms).
   static void Calibrate() {
      m_lJavaBase = System.currentTimeMillis();
      long lEnd = m_lJavaBase + 40L;
      float fPrev = GameObject.GetAbsoluteTime();
      float fEngineStart = fPrev;
      float fEngineStep = 1.0E9F;
      int nEngineChanges = 0;
      long lPrevMs = m_lJavaBase;
      long lJavaStep = 1000000L;
      int nJavaChanges = 0;

      for (long lNow = m_lJavaBase; lNow < lEnd; lNow = System.currentTimeMillis()) {
         float f = GameObject.GetAbsoluteTime();
         if (f != fPrev) {
            if (f - fPrev < fEngineStep) {
               fEngineStep = f - fPrev;
            }

            fPrev = f;
            nEngineChanges++;
         }

         if (lNow != lPrevMs) {
            if (lNow - lPrevMs < lJavaStep) {
               lJavaStep = lNow - lPrevMs;
            }

            lPrevMs = lNow;
            nJavaChanges++;
         }
      }

      float fJavaStep = nJavaChanges > 0 ? lJavaStep / 1000.0F : 1.0F;
      float fJavaElapsed = (lPrevMs - m_lJavaBase) / 1000.0F;
      float fRatio = fJavaElapsed > 0.0F ? (fPrev - fEngineStart) / fJavaElapsed : 0.0F;
      String sUnit = "?";
      if (fRatio > 0.5F && fRatio < 2.0F) {
         m_fEngineScale = 1.0F;
         sUnit = "s";
      } else if (fRatio > 500.0F && fRatio < 2000.0F) {
         m_fEngineScale = 0.001F;
         sUnit = "ms";
      } else {
         nEngineChanges = 0;
      }

      fEngineStep *= m_fEngineScale;
      if (nEngineChanges >= 2 && (fEngineStep <= _MAX_CLOCK_STEP || fEngineStep < fJavaStep)) {
         m_nClock = _CLOCK_ENGINE;
      } else {
         m_nClock = _CLOCK_JAVA;
      }

      // coût réel d'un sleep(1), mesuré avec l'horloge retenue
      float fCost = 0.0F;

      for (int i = 0; i < 3; i++) {
         float fBefore = Now();

         try {
            Thread.sleep(1L);
         } catch (InterruptedException e) {
         }

         float fSlept = Now() - fBefore;
         if (fSlept > fCost) {
            fCost = fSlept;
         }
      }

      m_fSleepCost = fCost > 0.001F ? fCost : 0.001F;
      GameObject.CrashLog(
         "[mod] limiteur : horloge "
            + (m_nClock == _CLOCK_ENGINE ? "moteur" : "Java")
            + " (moteur : "
            + (nEngineChanges >= 2 ? "pas " + Ms(fEngineStep) + " ms, unite " + sUnit : "inutilisable, rapport " + fRatio)
            + ", Java : "
            + Ms(fJavaStep)
            + " ms), sleep(1) = "
            + Ms(m_fSleepCost)
            + " ms, "
            + ModOptions.GetSummary()
            + "\n"
      );
      if (m_nClock == _CLOCK_JAVA && fJavaStep > _MAX_CLOCK_STEP) {
         GameObject.CrashLog("[mod] limiteur : aucune horloge assez fine, la limite sera approximative\n");
      }
   }

   // appelé une fois par image par RacerGame.OnFrame
   static void Frame() {
      int nLimit = ModOptions.m_nFpsLimit;
      if (nLimit <= 0 || RacerGame.m_bRecordAvi) {
         m_fLast = -1.0F;
         return;
      }

      float fPeriod = 1.0F / nLimit;
      float fNow = Now();
      if (m_fLast < 0.0F || fNow < m_fLast) {
         m_fLast = fNow;
         return;
      }

      float fTarget = m_fLast + fPeriod;
      float fStart = fNow;
      long lGuard = System.currentTimeMillis() + _GUARD_MS;

      while (fNow < fTarget) {
         if (fTarget - fNow > m_fSleepCost) {
            float fBefore = fNow;

            try {
               Thread.sleep(1L);
            } catch (InterruptedException e) {
            }

            fNow = Now();
            float fSlept = fNow - fBefore;
            // garde le pire sleep vu, oublié lentement
            m_fSleepCost = fSlept > m_fSleepCost ? fSlept : m_fSleepCost * 0.99F + fSlept * 0.01F;
         } else {
            Thread.yield();
            fNow = Now();
         }

         if (System.currentTimeMillis() > lGuard) {
            // l'horloge n'avance pas : on passe à l'horloge Java plutôt que de bloquer le jeu
            if (m_nClock == _CLOCK_ENGINE) {
               m_nClock = _CLOCK_JAVA;
               GameObject.CrashLog("[mod] limiteur : horloge moteur bloquee, passage a l'horloge Java\n");
            }

            m_fLast = -1.0F;
            return;
         }
      }

      m_fWaitTotal = m_fWaitTotal + (fNow - fStart);
      // cadence régulière ; après une image lente (chargement, alt-tab), on repart de maintenant au lieu de rattraper
      m_fLast = fNow - fTarget > fPeriod ? fNow : fTarget;
   }

   static String Ms(float f) {
      int n = Math.round(f * 100000.0F);
      return n / 100 + "." + n / 10 % 10 + n % 10;
   }
}
