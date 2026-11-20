public class RaceParams {
   static final int _AI_PLAYER = 0;
   static final int _HUMAN_PLAYER = 1;
   RacerModule m_Mod = null;
   java.util.Vector m_AITab = new java.util.Vector();
   java.util.Vector m_PlayersTab = new java.util.Vector();
   VehicleController[] m_Controllers = new VehicleController[2];
   java.util.Vector m_Vehicles = new java.util.Vector();
   java.util.Vector m_SortTable = new java.util.Vector();
   LevelBonusTypes m_AvailableBonuses = null;
   VehicleParams m_Params = null;
   Vector m_vStartPos = null;
   Vector m_vReverseStartPos = null;
   Vector m_vStartDir = null;
   int m_iNofLoops = -1;
   int m_iAILevel = 3;
   int m_NextNo = 0;
   // [mod] nombre de voitures arrivées : rang d'arrivée réel (Vehicle.m_iFinishRank)
   int m_nFinished = 0;

   // [mod] vrai si les feux sont passés au vert (IA lancées)
   boolean m_bModStarted = false;

   void AddAI(int var1) {
      this.AddModAI(var1);
   }

   // [mod] AddAI qui rend la voiture (en ligne : IA de l'hôte, ModNet.CreateGhosts)
   Vehicle AddModAI(int var1) {
      Vehicle var2 = this.AddCharacter(var1, this.m_Mod);
      var2.m_Index = this.m_NextNo++;
      this.ModAttachAI(var2);
      return var2;
   }

   VehicleAI ModAttachAI(Vehicle var2) {
      VehicleAI var3 = (VehicleAI)this.m_Mod.CreateObject("VehicleAI");
      var2.DirectSteering(true, var3);
      var3.SetVehicleAndTrack(var2, this.m_Mod.m_AITrack);
      var3.ActivateOnModule();
      this.m_AITab.addElement(var3);
      return var3;
   }

   // [mod] en ligne, hôte remplacé en course (ModNet.TakeOverAI) : le fantôme d'une IA de l'ancien hôte devient une
   // IA de ce jeu, là où il est. Avant le vert : sur la grille, lancée au vert comme les autres (StartRace) ; après :
   // elle repart tout de suite, vers le point de la piste des IA qui est devant elle (VehicleAI.ModResume).
   void ModGhostToAI(Vehicle var1) {
      var1.m_bModGhost = false;
      var1.m_bModGhostGone = false;
      var1.SetFlag(32);
      // drapeaux d'affichage du fantôme : ils n'ont pas de minuterie ici
      var1.m_bTurbo = false;
      var1.m_bTireSpikes = false;
      var1.m_bProtection = false;
      var1.DetachProtectionSound();
      VehicleAI var2 = this.ModAttachAI(var1);
      var2.SetAILevel(this.m_iAILevel);
      if (!this.m_bModStarted) {
         var1.InitTracer();
         return;
      }

      // suivi des portes à partir de la porte reçue en dernier (InitTracer repartirait de la porte 0)
      var1.UpdateCurrentGate();
      var1.CallMethodEvery("UpdateCurrentGate", Vehicle._GATES_CHECK_TIME);
      var2.ModResume();
   }

   void StartRace() {
      this.m_bModStarted = true; // [mod]
      for (int var1 = 0; var1 < this.m_AITab.size(); var1++) {
         ((VehicleAI)this.m_AITab.elementAt(var1)).Start();
      }

      for (int var2 = 0; var2 < this.m_PlayersTab.size(); var2++) {
         ((Vehicle)this.m_PlayersTab.elementAt(var2)).CalculateVehicleInput(true);
         ((Vehicle)this.m_PlayersTab.elementAt(var2)).UpdateVehicleInput(true);
      }

      this.m_Mod.StartCalculatePositions();
   }

   java.util.Vector GetRacers() {
      return this.m_Vehicles;
   }

   RaceParams(RacerModule var1) {
      this.m_Mod = var1;
   }

   Vehicle AddCharacter(int var1, Module var2) {
      Vehicle var3 = (Vehicle)var2.CreateObjectNoInit("Vehicle");
      var3.MeshName = MenuAnimal.GetCarMesh(var1);
      var3.m_iAnimalID = var1;
      var3.LoadMesh(var3.MeshName);
      var3.SetLODCoeff(PerformanceSettings._VEHICLE_LOD[PerformanceSettings.m_iCurrQuality]);
      var3.SetPosition(new Vector(Vector.Zero));
      var3.m_Params = this.m_Params;
      var3.m_vVehicleForward = new Vector(Vector.Forward);
      var3.InitObject();
      var2.AddControlObject(var3);
      VehicleCharacter var4 = (VehicleCharacter)var2.CreateObjectNoInit("VehicleCharacter");
      var4.MeshName = MenuAnimal.GetAnimalMesh(var1);
      var4.LoadMesh();
      var4.SetLODCoeff(PerformanceSettings._ANIMAL_LOD[PerformanceSettings.m_iCurrQuality]);
      var4.InitObject();
      var4.ClearFlag(4096);
      var3.SetCharacter(var4);
      var3.AttachChild(var4);
      this.m_Vehicles.addElement(var3);
      this.m_SortTable.addElement(var3);
      return var3;
   }

   // [mod] en ligne : voiture d'un autre joueur (ModNet). Ni dans m_PlayersTab ni dans m_AITab : pas de physique ni
   // de suivi des portes, mais dans m_Vehicles / m_SortTable (places, barre de distance, cible des bonus).
   // Drapeau 32 (EFlags._BLOCK_MOVE) retiré comme l'OVNI le fait : on la traverse.
   Vehicle AddGhost(int var1) {
      Vehicle var2 = this.AddCharacter(var1, this.m_Mod);
      var2.m_Index = this.m_NextNo++;
      var2.m_bModGhost = true; // bloquant ou non : ModNetPlayer.Apply
      return var2;
   }

   void SetAILevel(int var1) {
      CoreLogic var2 = RacerGame.GetCoreLogic();
      this.m_iAILevel = var1;
      if (this.m_Mod != null && this.m_Mod.IsTrackExtra()) {
         this.m_iAILevel = 4;
      }

      if (var2.GetDiffLevel() > 0) {
         this.m_iAILevel = 4;
      }

      // [mod] difficulté des IA du menu Mods (ModAI) : niveau fixe, ou celui du circuit en « auto » / « d'origine »
      this.m_iAILevel = ModAI.EffectiveLevel(this.m_iAILevel);
      // [mod] l'original passait var1 (niveau du circuit) au lieu de m_iAILevel : le niveau 4 prévu pour les
      // extras et pour l'aventure rejouée (difficulté 2) n'arrivait jamais aux IA
      for (int var3 = 0; var3 < this.m_AITab.size(); var3++) {
         VehicleAI var4 = (VehicleAI)this.m_AITab.elementAt(var3);
         var4.SetAILevel(this.m_iAILevel);
      }
   }

   void SetStartPos(Vector var1) {
      // [mod] en ligne : place sur la grille = slot du joueur, la même dans tous les jeux (pas de tirage)
      boolean bOnline = ModNet.IsRaceOnline();
      Vehicle[] aGrid = bOnline ? ModNet.GridOrder(this) : null;
      float var2 = 120.0F;
      this.m_vStartPos = new Vector(var1);
      Vector var3 = Vector.newVector(this.m_vStartPos);
      if (this.m_Mod.m_AITrack.m_TrackPoints.size() > 1) {
         TrackPoint var4 = this.m_Mod.m_AITrack.GetPoint(1);
         this.m_vStartDir = new Vector(var4.m_vPos);
         var4 = this.m_Mod.m_AITrack.GetPoint(0);
         this.m_vStartDir.Sub(var4.m_vPos);
         this.m_vStartDir.fY = 0.0F;
         this.m_vStartDir.Normalize();
         var2 = (this.m_Mod.m_AITrack.GetPoint(0).GetTrackWidth() - 100.0F) / (bOnline ? aGrid.length : this.m_AITab.size() + this.m_PlayersTab.size());
      } else {
         this.m_vStartDir = new Vector(Vector.Forward);
      }

      Vector var13 = null;
      var13 = this.m_vStartDir.Cross(Vector.Up);
      var13.Mul(var2 / 2.0F);
      this.m_vStartPos.Add(var13);
      var13.Mul(2.0F);
      int var5 = -1;
      int var6 = -1;
      int var7 = bOnline ? aGrid.length : this.m_AITab.size() + this.m_PlayersTab.size();
      int[] var8 = new int[var7];

      for (int var9 = 0; var9 < var7; var9++) {
         var8[var9] = 0;
      }

      if (!RacerModule.m_bIsDemo && !bOnline) {
         var5 = RacerGame.GetRandom(var7);
         var8[var5] = 1;
         if (RacerGame.GetCoreLogic().IsMultiplayer()) {
            do {
               var6 = RacerGame.GetRandom(var7);
            } while (var6 == var5);

            var8[var6] = 1;
         }
      }

      int var16 = 0;

      for (int var10 = 0; var10 < var8.length; var10++) {
         Vehicle var11 = null;
         if (bOnline) {
            var11 = aGrid[var10];
         } else if (var8[var10] == 1) {
            if (var10 == var5) {
               var11 = (Vehicle)this.m_PlayersTab.elementAt(0);
            }

            if (var10 == var6) {
               var11 = (Vehicle)this.m_PlayersTab.elementAt(1);
            }
         }

         if (!bOnline && var8[var10] == 0) {
            var11 = ((VehicleAI)this.m_AITab.elementAt(var16)).m_Vehicle;
            var16++;
         }

         var3.fY = this.m_Mod.TerrainHeight(var3.fX, var3.fZ);
         var11.m_vVehiclePosition.Set(var3);
         var11.SetPosition(var3);
         var11.m_vVehicleForward.Set(this.m_vStartDir);
         var11.SetDir(this.m_vStartDir);
         var3.Add(var13);
      }

      var8 = null;
      var3.Null();
      // [mod] carte du circuit pour l'IA améliorée, au chargement plutôt qu'au vert
      if (ModAI.IsImproved() && this.m_AITab.size() > 0) {
         ModAIMap.For(this.m_Mod, this.m_Mod.m_AITrack);
      }
   }

   void SetReverseStartPos(Vector var1) {
      this.m_vReverseStartPos = new Vector(var1);
   }

   void AddPlayer(int var1) {
      Vehicle var2 = this.AddCharacter(var1, this.m_Mod);
      var2.m_bIsPlayer = true;
      var2.m_Index = this.m_NextNo++;
      this.m_PlayersTab.addElement(var2);
      this.m_Controllers[this.m_PlayersTab.size() - 1].SetVehicle(var2);
      ((RacerCamera)RacerModule.m_Cams[this.m_PlayersTab.size() - 1]).SetObserved(var2);
   }
}
