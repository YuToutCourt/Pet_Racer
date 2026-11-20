// [mod] Écran « En ligne » (chantier 8, phase 3, notes/online.md), ouvert par le bouton « EN LIGNE » du menu principal.
// Accueil : créer un salon, ou taper le code d'un salon (4 lettres) pour le rejoindre.
// Salon : les 6 places (nom, animal, prêt), puis les lignes « libellé < valeur > » : ton animal, prêt (invité),
// circuit, tours, boîtes bonus, IA et leur niveau (réglés par l'hôte, montrés aux invités). Les places libres que
// prendront les IA sont marquées « IA » dans la liste. OK : l'hôte lance la course quand tous les
// invités sont prêts ; l'invité se dit prêt (ou ne l'est plus). Retour : sort du salon, puis revient au menu.
// Après chaque course, le podium ramène ici (ModNet.BackToOnlineScreen) : on reste dans le salon.
// Phase 4 : l'accueil propose aussi la partie rapide (file d'attente du serveur, puis salon classé lancé tout seul :
// réglages imposés, montrés grisés) et le classement (10 premiers). En haut : mon Elo et mon rang ; en bas, après une
// course classée, ma place et ce qu'elle m'a rapporté.
// Le réseau tourne à chaque image dans ModOnline.Frame ; cet écran ne fait que l'afficher (OnlineTick, 5 fois/s).
// Textes : jetons « &Onl…& » de Data\Texts.scr (mods/art/make_texts.py) ; noms et codes en texte brut.
public class KidsOnlineMenuWindow extends KidsMenuWindow {
   static final String _TEX_BACK = "Data/Textures/Menu/b/PlanszaNiebieska.png";
   static final String _TEX_PANEL = "Data/Textures/Menu/b/OnlinePanel.png";

   // genres de ligne
   static final int _K_NONE = 0;
   static final int _K_MODE = 1; // accueil : partie rapide / créer / rejoindre / classement
   static final int _K_CODE = 2; // accueil : code du salon à rejoindre
   static final int _K_ANIMAL = 3;
   static final int _K_READY = 4; // invité
   static final int _K_TRACK = 5;
   static final int _K_LAPS = 6;
   static final int _K_BONUS = 7;
   static final int _K_AI = 8; // [mod] IA de l'hôte : combien (places libres au plus)
   static final int _K_AI_LEVEL = 9; // et leur difficulté (ModAI)
   static final String[] _LABELS = new String[]{
      "", "&OnlMode&", "&OnlCode&", "&OnlAnimal&", "&OnlReadyRow&", "&OnlTrack&", "&OnlLaps&", "&OnlBonus&", "&OnlAi&", "&OnlAiLevel&"
   };

   // accueil, ligne « Que faire ? »
   static final int _M_QUICK = 0;
   static final int _M_CREATE = 1;
   static final int _M_JOIN = 2;
   static final int _M_TOP = 3;
   static final String[] _MODE_TEXTS = new String[]{"&OnlQuick&", "&OnlCreate&", "&OnlJoin&", "&OnlTop&"};

   static final int _ROWS = 7;
   static final int _PLAYERS = ModNet._MAX_PLAYERS;
   static final int _TOP = ModLobby._TOP_MAX; // lignes de la page « Classement »
   static final int _WINDOWS = _ROWS * 4 + _PLAYERS * 3 + _TOP * 3 + 24;
   static final int _ANIMALS = 12;
   static final int _TRACKS = 31;
   static final int _MAX_LAPS = 10;
   static final int _CODE_LENGTH = 4; // server\rooms.py CODE_LENGTH

   // mise en page, en fractions de l'écran 4:3 (comme KidsModsMenuWindow)
   static final float _PANEL_W = 0.84F;
   static final float _PANEL_H = 0.84F;
   static final float _PANEL_Y = 0.0167F;
   static final float _PANEL_TEX_V = 365.0F / 512.0F;
   static final float _INFO_Y = 0.17F;
   static final float _PLAYERS_Y = 0.225F; // centre de la première place
   static final float _PLAYER_H = 0.04F;
   static final float _ROWS_Y = 0.475F; // haut de la première ligne de réglage
   static final float _ROW_H = 0.043F; // 7 lignes chez l'invité (0.05 avant les lignes des IA)
   static final float _STATUS_Y = 0.805F;
   static final float _LABEL_X = 0.12F;
   static final float _LABEL_W = 0.42F;
   static final float _LEFT_X = 0.585F;
   static final float _VALUE_X = 0.72F;
   static final float _VALUE_W = 0.2F;
   static final float _RIGHT_X = 0.855F;
   static final float _ARROW_W = 0.06F;
   static final float _ARROW_H = 0.047F;
   static final float _NAME_X = 0.12F;
   static final float _NAME_W = 0.34F;
   static final float _ANIMAL_X = 0.57F;
   static final float _STATE_X = 0.78F;
   static final float _COL_W = 0.2F;
   static final float _TOP_Y = 0.225F; // page « Classement » : centre de la première ligne
   static final float _TOP_H = 0.048F;

   // couleurs
   static final float[] _WHITE = new float[]{1.0F, 1.0F, 1.0F};
   static final float[] _GREY = new float[]{0.6F, 0.6F, 0.65F};
   static final float[] _YELLOW = new float[]{1.0F, 0.85F, 0.3F};
   static final float[] _GREEN = new float[]{0.45F, 1.0F, 0.45F};
   static final float[] _RED = new float[]{1.0F, 0.45F, 0.4F};

   int bkTex0 = -1;
   int wndPanel = -1;
   int wndRow = -1;
   int ktxInfo = -1;
   int ktxHelp1 = -1;
   int ktxHelp2 = -1;
   int ktxStatus = -1;
   int[] ktxName = new int[_PLAYERS];
   int[] ktxAnimal = new int[_PLAYERS];
   int[] ktxState = new int[_PLAYERS];
   int[] ktxTopName = new int[_TOP];
   int[] ktxTopElo = new int[_TOP];
   int[] ktxTopGames = new int[_TOP];
   int[] ktxLabel = new int[_ROWS];
   int[] ktxValue = new int[_ROWS];
   int[] btnLeft = new int[_ROWS];
   int[] btnRight = new int[_ROWS];
   int btnOK = -1;
   InitKidsButton btnOKInit = new InitKidsButton(40);
   int btnExit = -1;
   InitKidsButton btnExitInit = new InitKidsButton(57);

   int[] m_Kinds = new int[_ROWS];
   int m_nRows = 0;
   int m_Sel = 0;
   int m_nMode = _M_QUICK; // accueil : _M_*
   boolean m_bTopPage = false; // page « Classement » ouverte
   String m_sCode = "";
   boolean m_bNoNet = false;
   boolean m_bShown = false;

   KidsOnlineMenuWindow() {
      super.m_List = new WindowsList(_WINDOWS);
   }

   static float TopCenter(int i) {
      return _TOP_Y + _TOP_H * i;
   }

   static float RowCenter(int i) {
      return _ROWS_Y + _ROW_H * i + _ROW_H / 2.0F;
   }

   static float PlayerCenter(int i) {
      return _PLAYERS_Y + _PLAYER_H * i;
   }

   void ButtonsOnTop() {
      super.m_List.SetOnTop(this.wndRow);
      super.m_List.SetOnTop(this.ktxInfo);
      super.m_List.SetOnTop(this.ktxHelp1);
      super.m_List.SetOnTop(this.ktxHelp2);
      super.m_List.SetOnTop(this.ktxStatus);

      for (int i = 0; i < _PLAYERS; i++) {
         super.m_List.SetOnTop(this.ktxName[i]);
         super.m_List.SetOnTop(this.ktxAnimal[i]);
         super.m_List.SetOnTop(this.ktxState[i]);
      }

      for (int i = 0; i < _TOP; i++) {
         super.m_List.SetOnTop(this.ktxTopName[i]);
         super.m_List.SetOnTop(this.ktxTopElo[i]);
         super.m_List.SetOnTop(this.ktxTopGames[i]);
      }

      for (int i = 0; i < _ROWS; i++) {
         super.m_List.SetOnTop(this.ktxLabel[i]);
         super.m_List.SetOnTop(this.ktxValue[i]);
      }

      for (int i = 0; i < _ROWS; i++) {
         super.m_List.SetOnTop(this.btnLeft[i]);
         super.m_List.SetOnTop(this.btnRight[i]);
      }

      super.m_List.SetOnTop(this.btnOK);
      super.m_List.SetOnTop(this.btnExit);
   }

   // --- état du réseau ---

   static boolean IsHost() {
      return ModNet.m_nRole == ModNet._ROLE_HOST;
   }

   static boolean InRoom() {
      return ModLobby.InRoom();
   }

   // salon d'une partie rapide : réglages imposés, lancement automatique
   static boolean Ranked() {
      return ModLobby.IsRankedRoom();
   }

   // lignes voulues selon la page et le rôle
   int BuildKinds(int[] k) {
      int n = 0;
      if (this.m_bTopPage) {
         n = 0; // page « Classement » : rien à régler
      } else if (!InRoom()) {
         k[n++] = _K_MODE;
         if (this.m_nMode == _M_QUICK) {
            k[n++] = _K_ANIMAL;
         } else if (this.m_nMode == _M_JOIN) {
            k[n++] = _K_CODE;
         }
      } else if (Ranked()) {
         k[n++] = _K_ANIMAL;
         k[n++] = _K_TRACK;
         k[n++] = _K_LAPS;
         k[n++] = _K_AI_LEVEL;
      } else {
         k[n++] = _K_ANIMAL;
         if (!IsHost()) {
            k[n++] = _K_READY;
         }

         k[n++] = _K_TRACK;
         k[n++] = _K_LAPS;
         k[n++] = _K_BONUS;
         k[n++] = _K_AI;
         k[n++] = _K_AI_LEVEL;
      }

      for (int i = n; i < _ROWS; i++) {
         k[i] = _K_NONE;
      }

      return n;
   }

   boolean IsEditable(int nKind) {
      switch (nKind) {
         case _K_MODE:
            return !ModLobby.InQueue();
         case _K_READY:
            return true;
         case _K_ANIMAL:
            // invité prêt : animal figé. Partie rapide : choisi avant d'entrer dans la file (la course pourrait
            // partir pendant l'écran du choix de l'animal)
            if (!InRoom()) {
               return !ModLobby.InQueue();
            }

            return !Ranked() && (IsHost() || !ModNet.m_bWantReady);
         case _K_TRACK:
         case _K_LAPS:
         case _K_BONUS:
         case _K_AI:
         case _K_AI_LEVEL:
            return IsHost() && !Ranked();
         default:
            return false;
      }
   }

   String ValueText(int nKind) {
      switch (nKind) {
         case _K_MODE:
            return _MODE_TEXTS[this.m_nMode];
         case _K_CODE:
            String s = this.m_sCode;

            while (s.length() < _CODE_LENGTH) {
               s = s + "_";
            }

            return s;
         case _K_ANIMAL:
            return "&AnimalName" + ModNet.m_nCfgAnimal + "&";
         case _K_READY:
            return ModNet.m_bWantReady ? "&ModsYes&" : "&ModsNo&";
         case _K_TRACK:
            return "&GPTrack" + (Own() ? ModNet.m_nCfgTrack : ModNet.m_nTrack) + "&";
         case _K_LAPS:
            return String.valueOf(Own() ? ModNet.m_nCfgLaps : ModNet.m_nLaps);
         case _K_BONUS:
            return (Own() ? ModNet.m_bCfgBonus : ModNet.m_bBonus) ? "&ModsYes&" : "&ModsNo&";
         case _K_AI:
            return String.valueOf(AiShown());
         case _K_AI_LEVEL:
            return "&ModsAi" + (Own() ? ModNet.CfgAiMode() : ModNet.m_nAiMode) + "&";
         default:
            return "";
      }
   }

   // l'hôte d'un salon à code montre ses propres réglages ; sinon ceux de la course (INFO de l'hôte, ou imposés)
   static boolean Own() {
      return IsHost() && !Ranked();
   }

   static int Wrap(int n, int nMin, int nMax) {
      if (n > nMax) {
         return nMin;
      }

      return n < nMin ? nMax : n;
   }

   void Change(int nRow, int nDelta) {
      int nKind = this.m_Kinds[nRow];
      if (!this.IsEditable(nKind)) {
         return;
      }

      switch (nKind) {
         case _K_MODE:
            this.m_nMode = Wrap(this.m_nMode + nDelta, 0, _MODE_TEXTS.length - 1);
            break;
         case _K_ANIMAL:
            this.OpenAnimalChoice(); // écran classique du choix de l'animal (modèle 3D, stats)
            return;
         case _K_READY:
            ModNet.m_bWantReady = !ModNet.m_bWantReady;
            break;
         case _K_TRACK:
            ModNet.SetRace(Wrap(ModNet.m_nCfgTrack + nDelta, 0, _TRACKS - 1), ModNet.m_nCfgLaps, ModNet.m_bCfgBonus);
            break;
         case _K_LAPS:
            ModNet.SetRace(ModNet.m_nCfgTrack, Wrap(ModNet.m_nCfgLaps + nDelta, 1, _MAX_LAPS), ModNet.m_bCfgBonus);
            break;
         case _K_BONUS:
            ModNet.SetRace(ModNet.m_nCfgTrack, ModNet.m_nCfgLaps, !ModNet.m_bCfgBonus);
            break;
         case _K_AI:
            // la valeur montrée est bornée aux places libres : on part d'elle
            int nMax = ModNet.EffectiveAi(ModNet._MAX_PLAYERS);
            ModNet.SetAi(Wrap(AiShown() + nDelta, 0, nMax), ModNet.CfgAiMode());
            break;
         case _K_AI_LEVEL:
            ModNet.SetAi(AiShown(), Wrap(ModNet.CfgAiMode() + nDelta, 0, ModOptions._AI_MAX));
      }

      this.Refresh();
   }

   // IA qui rouleront si la course part maintenant (voulues, bornées aux places libres)
   static int AiShown() {
      return ModNet.EffectiveAi(Ranked() ? _PLAYERS - 1 : IsHost() ? ModNet.m_nCfgAi : ModNet.m_nAi);
   }

   // --- actions ---

   // ligne « Ton animal » (flèches ou clic) : l'écran de choix habituel, en mode « en ligne », puis retour ici
   void OpenAnimalChoice() {
      KidsAnimalChoiceMenuWindow.m_bOnline = true;
      super.m_Module.PushMenu(super.m_WindowID);
      this.DisactivateWindow();
      super.m_Module.ActivateMenuWindow(KidsSimpleMenuModule._AnimalChoiceMenuWindow);
   }

   void OnOK() {
      if (this.m_bNoNet) {
         return;
      }

      if (!InRoom()) {
         if (ModLobby.m_nState != ModLobby._L_IDLE || this.m_bTopPage) {
            return; // pas encore connecté, déjà en train d'entrer, en file d'attente, ou classement affiché
         }

         if (this.m_nMode == _M_QUICK) {
            ModLobby.Queue();
         } else if (this.m_nMode == _M_CREATE) {
            ModLobby.Create();
         } else if (this.m_nMode == _M_TOP) {
            this.m_bTopPage = true;
            ModLobby.m_bWantTop = true;
            ModLobby.m_lNextTop = 0L;
            this.m_Sel = 0;
         } else if (this.m_sCode.length() == _CODE_LENGTH) {
            ModLobby.Join(this.m_sCode);
         }
      } else if (Ranked()) {
         return; // partie rapide : l'hôte lance tout seul
      } else if (IsHost()) {
         if (ModNet.CanLaunch() == ModNet._LAUNCH_OK) {
            ModNet.HostLaunch(); // la fenêtre est fermée par ModNet.LaunchRace
            return;
         }
      } else if (ModNet.m_nMySlot != ModNet._NO_SLOT) {
         ModNet.m_bWantReady = !ModNet.m_bWantReady;
      }

      this.Refresh();
   }

   void OnBack() {
      if (this.m_bTopPage) {
         this.m_bTopPage = false; // retour à l'accueil
         ModLobby.m_bWantTop = false;
         this.Refresh();
         return;
      }

      if (ModLobby.InQueue()) {
         ModLobby.CancelQueue();
         this.Refresh();
         return;
      }

      if (InRoom() || ModLobby.m_nState == ModLobby._L_ENTER) {
         ModLobby.LeaveRoom(); // retour à l'accueil de l'écran
         this.m_Sel = 0;
         this.Refresh();
         return;
      }

      ModLobby.Disconnect();
      this.DisactivateWindow();
      super.m_Module.ActivateMenuWindow(super.m_Module.PopMenu());
   }

   void TypeChar(char c) {
      if (InRoom() || ModLobby.InQueue() || this.m_bTopPage) {
         return;
      }

      if (c >= 'a' && c <= 'z') {
         c = (char)(c - 'a' + 'A');
      }

      if (c < 'A' || c > 'Z') {
         return;
      }

      if (this.m_nMode != _M_JOIN) {
         this.m_nMode = _M_JOIN;
         this.m_sCode = "";
      }

      if (this.m_sCode.length() >= _CODE_LENGTH) {
         this.m_sCode = "";
      }

      this.m_sCode = this.m_sCode + c;
      this.Refresh();
      if (this.m_nRows > 1) {
         this.Select(1);
      }
   }

   // --- affichage ---

   ModText TextAt(int id) {
      return (ModText)super.m_List.WindowAt(id);
   }

   // texte et couleur, seulement s'ils changent (rafraîchi 5 fois/s)
   void Put(int id, String s, float[] c) {
      ModText t = this.TextAt(id);
      if (t == null) {
         return;
      }

      if (s.equals(t.m_Text) && t.r == c[0] && t.g == c[1] && t.b == c[2]) {
         return;
      }

      t.SetColor(c[0], c[1], c[2]);
      t.SetText(s);
   }

   // fenêtres montrées / cachées par Refresh : état connu, pour ne toucher qu'à ce qui change (-1 : inconnu, après Show)
   int[] m_Visible = new int[_WINDOWS];

   void ShowWnd(int id, boolean b) {
      Window w = super.m_List.WindowAt(id);
      if (w == null || id >= this.m_Visible.length || this.m_Visible[id] == (b ? 1 : 0)) {
         return;
      }

      this.m_Visible[id] = b ? 1 : 0;

      if (b) {
         w.Show();
      } else {
         w.Hide();
      }

      super.m_List.ProcessMouse(id, b);
   }

   void Select(int nRow) {
      if (this.m_nRows == 0) {
         return;
      }

      this.m_Sel = (nRow + this.m_nRows) % this.m_nRows;
      KidsMenuWindow w = (KidsMenuWindow)super.m_List.WindowAt(this.wndRow);
      w.SetSizeAndPos(0.8F, _ROW_H * 1.15F, 0.5F, RowCenter(this.m_Sel), true);
   }

   void Refresh() {
      if (!this.m_bShown) {
         return;
      }

      // lignes de réglage
      int[] k = new int[_ROWS];
      int n = this.BuildKinds(k);
      this.m_nRows = n;
      this.m_Kinds = k;

      for (int i = 0; i < _ROWS; i++) {
         boolean bUsed = i < n;
         boolean bEdit = bUsed && this.IsEditable(k[i]);
         this.Put(this.ktxLabel[i], bUsed ? _LABELS[k[i]] : "", bUsed ? _WHITE : _WHITE);
         this.Put(this.ktxValue[i], bUsed ? this.ValueText(k[i]) : "", bEdit || k[i] == _K_CODE ? _WHITE : _GREY);
         this.ShowWnd(this.btnLeft[i], bEdit);
         this.ShowWnd(this.btnRight[i], bEdit);
      }

      if (this.m_Sel >= n) {
         this.m_Sel = 0;
      }

      this.ShowWnd(this.wndRow, n > 0);
      this.Select(this.m_Sel);
      this.RefreshInfo();
      this.RefreshPlayers();
      this.RefreshStatus();
   }

   // « 1er », « 2e »... (texte brut : sans accent)
   static String Nth(int n) {
      return n == 1 ? "1er" : n + "e";
   }

   // mon Elo, d'après le dernier INFO du serveur hors salon
   static String AccountText() {
      if (ModLobby.m_nGames == 0 || ModLobby.m_nRank == 0) {
         return "Ton Elo : " + ModLobby.m_nElo + " (pas encore de partie rapide)";
      }

      return "Ton Elo : " + ModLobby.m_nElo + "  -  " + Nth(ModLobby.m_nRank) + " sur " + ModLobby.m_nRankedTotal;
   }

   void RefreshInfo() {
      if (this.m_bTopPage) {
         this.Put(this.ktxInfo, ModLobby.m_nTopCount < 0 ? "Classement" : "Classement des parties rapides (" + ModLobby.m_nTopTotal + " joueurs)", _YELLOW);
         this.Put(this.ktxHelp1, "", _WHITE);
         this.Put(this.ktxHelp2, "", _WHITE);
      } else if (InRoom()) {
         this.Put(this.ktxInfo, Ranked() ? "Partie rapide" : "Salon  " + ModLobby.m_sRoom, _YELLOW);
         this.Put(this.ktxHelp1, "", _WHITE);
         this.Put(this.ktxHelp2, "", _WHITE);
      } else {
         if (ModLobby.m_bAccountKnown) {
            this.Put(this.ktxInfo, AccountText(), _YELLOW);
         } else {
            this.Put(this.ktxInfo, ModOnline.m_sServerHost + " : " + ModOnline.m_nServerPort, _GREY);
         }

         boolean bQuick = this.m_nMode == _M_QUICK;
         boolean bTop = this.m_nMode == _M_TOP;
         this.Put(this.ktxHelp1, bQuick ? "&OnlHelpQuick1&" : bTop ? "&OnlHelpTop1&" : "&OnlHelp1&", _WHITE);
         this.Put(this.ktxHelp2, bQuick ? "&OnlHelpQuick2&" : bTop ? "&OnlHelpTop2&" : "&OnlHelp2&", _WHITE);
      }
   }

   // page « Classement » : les 10 premiers (nom, Elo, courses classées) ; ailleurs, lignes vides
   void RefreshTop() {
      for (int i = 0; i < _TOP; i++) {
         boolean bRow = this.m_bTopPage && i < ModLobby.m_nTopCount && ModLobby.m_TopNames[i] != null;
         String sName = bRow ? ModLobby.m_TopNames[i] : "";
         float[] c = bRow && sName.equals(ModNet.LocalName()) ? _YELLOW : _WHITE;
         this.Put(this.ktxTopName[i], bRow ? (i + 1) + ". " + sName : "", c);
         this.Put(this.ktxTopElo[i], bRow ? String.valueOf(ModLobby.m_TopElo[i]) : "", c);
         this.Put(this.ktxTopGames[i], bRow ? ModLobby.m_TopGames[i] + (ModLobby.m_TopGames[i] > 1 ? " courses" : " course") : "", _GREY);
      }
   }

   void RefreshPlayers() {
      this.RefreshTop();
      if (this.m_bTopPage) {
         for (int i = 0; i < _PLAYERS; i++) {
            this.Put(this.ktxName[i], "", _WHITE);
            this.Put(this.ktxAnimal[i], "", _WHITE);
            this.Put(this.ktxState[i], "", _WHITE);
         }

         return;
      }

      boolean bRoom = InRoom();
      int nAi = bRoom ? AiShown() : 0; // les premières places libres seront des IA (ModNet.AddAIPlayers)

      for (int i = 0; i < _PLAYERS; i++) {
         String sName = bRoom ? ModLobby.m_Names[i] : null;
         if (sName == null) {
            boolean bAi = nAi > 0 && ModNet.IsFreeForAI(i);
            if (bAi) {
               nAi--;
            }

            this.Put(this.ktxName[i], bRoom ? (i + 1) + "." + (bAi ? " IA" : "") : "", _GREY);
            this.Put(this.ktxAnimal[i], bAi ? "&OnlAiAnimal&" : "", _GREY);
            this.Put(this.ktxState[i], "", _GREY);
            continue;
         }

         boolean bMe = i == ModLobby.m_nMySlot;
         ModNetPlayer p = ModNet.m_Players[i];
         this.Put(this.ktxName[i], (i + 1) + ". " + sName, bMe ? _YELLOW : _WHITE);
         int nAnimal = bMe ? ModNet.m_nCfgAnimal : p != null ? p.m_nAnimal : -1;
         this.Put(this.ktxAnimal[i], nAnimal >= 0 ? "&AnimalName" + nAnimal + "&" : "", _WHITE);
         String sState;
         float[] c;
         if (i == ModLobby.m_nHostSlot) {
            sState = "&OnlHost&";
            c = _YELLOW;
         } else if (Ranked()) {
            // partie rapide : tous prêts d'office ; on montre seulement qui est arrivé
            sState = bMe || p != null && p.m_bAtMenu ? "&OnlReady&" : p == null ? "&OnlJoining&" : "&OnlBusy&";
            c = bMe || p != null && p.m_bAtMenu ? _GREEN : _GREY;
         } else if (bMe) {
            sState = ModNet.m_bWantReady ? "&OnlReady&" : "&OnlNotReady&";
            c = ModNet.m_bWantReady ? _GREEN : _RED;
         } else if (p == null) {
            sState = "&OnlJoining&";
            c = _GREY;
         } else if (!p.m_bAtMenu) {
            sState = "&OnlBusy&";
            c = _GREY;
         } else if (p.m_bLobbyReady) {
            sState = "&OnlReady&";
            c = _GREEN;
         } else {
            sState = "&OnlNotReady&";
            c = _RED;
         }

         this.Put(this.ktxState[i], sState, c);
      }
   }

   void RefreshStatus() {
      String s;
      float[] c = _WHITE;
      int nState = ModLobby.m_nState;
      if (this.m_bNoNet) {
         s = "&OnlNoNet&";
         c = _RED;
      } else if (nState == ModLobby._L_OFF) {
         s = "&OnlNoServer&";
         c = _RED;
      } else if (ModLobby.m_bSilentLogged) {
         s = "&OnlNoServer&";
         c = _RED;
      } else if (nState == ModLobby._L_HELLO) {
         s = "&OnlConnecting&";
      } else if (nState == ModLobby._L_ENTER) {
         s = "&OnlEntering&";
      } else if (this.m_bTopPage) {
         s = ModLobby.m_nTopCount < 0 ? "&OnlConnecting&" : ModLobby.m_nTopCount == 0 ? "&OnlTopEmpty&" : AccountText();
         c = ModLobby.m_nTopCount > 0 ? _YELLOW : _GREY;
      } else if (nState == ModLobby._L_QUEUE) {
         long lSecs = (System.currentTimeMillis() - ModLobby.m_lQueueSince) / 1000L;
         s = "Recherche d'adversaires : " + ModLobby.m_nQueue + " en attente (" + lSecs + " s)";
         c = _GREEN;
      } else if (nState == ModLobby._L_IDLE) {
         int e = ModLobby.m_nLastJoinError;
         int r = ModLobby.m_nRatedState;
         if (e > 0 && e <= 4) {
            s = "&OnlErr" + e + "&";
            c = _RED;
         } else if (r == ModLobby._RATED_DONE) {
            // résultat de ma dernière course classée
            int d = ModLobby.m_nRatedDelta;
            s = "Partie rapide : " + Nth(ModLobby.m_nRatedPlace) + " sur " + ModLobby.m_nRatedHumans + ", Elo " + (d >= 0 ? "+" : "") + d + " (" + ModLobby.m_nRatedElo + ")";
            c = d >= 0 ? _GREEN : _RED;
         } else if (r == ModLobby._RATED_PENDING) {
            s = "&OnlRatedWait&";
         } else if (r == ModLobby._RATED_VOID) {
            s = "&OnlRatedVoid&";
            c = _GREY;
         } else if (ModLobby.m_bRankedCancelled) {
            s = "&OnlRankedCancel&";
            c = _GREY;
         } else {
            s = ModLobby.m_nOnline + (ModLobby.m_nOnline > 1 ? " joueurs" : " joueur") + " en ligne";
            c = _GREY;
         }
      } else if (ModNet.m_nMySlot == ModNet._NO_SLOT || ModNet.m_nRole == ModNet._ROLE_NONE) {
         s = "&OnlEntering&";
      } else if (Ranked()) {
         s = "&OnlRankedStart&";
         c = _GREEN;
      } else if (IsHost()) {
         int r = ModNet.CanLaunch();
         if (r == ModNet._LAUNCH_OK) {
            s = "&OnlGo&";
            c = _GREEN;
         } else if (r == ModNet._LAUNCH_ALONE) {
            s = "&OnlAlone&";
         } else {
            s = "&OnlWaitReady&";
         }
      } else if (ModNet.m_bWantReady) {
         s = "&OnlWaitHost&";
         c = _GREEN;
      } else {
         s = "&OnlPressReady&";
      }

      this.Put(this.ktxStatus, s, c);
   }

   // rafraîchi 5 fois par seconde tant que l'écran est ouvert (le réseau change sans qu'on touche à rien)
   public void OnlineTick() {
      if (!this.m_bShown) {
         return;
      }

      this.Refresh();
      this.CallMethodIn("OnlineTick", 0.2F);
   }

   // --- entrées ---

   public void OnKeyReleased(int var1, char var2) {
      if (this.CanProcessMouseMove() && !super.m_Module.CanIgnoreKeys()) {
         if (var1 == 200) {
            super.m_Module.DeactivateCursor();
            this.Select(this.m_Sel - 1);
         } else if (var1 == 208) {
            super.m_Module.DeactivateCursor();
            this.Select(this.m_Sel + 1);
         } else if (var1 == 203) {
            super.m_Module.DeactivateCursor();
            this.Change(this.m_Sel, -1);
         } else if (var1 == 205) {
            super.m_Module.DeactivateCursor();
            this.Change(this.m_Sel, 1);
         } else if (var1 == 28 || var1 == 156) {
            this.ButtonPushed(this.btnOK);
         } else if (var1 == 1) {
            this.ButtonPushed(this.btnExit);
         } else if (var1 == 14) {
            // retour arrière : efface la dernière lettre du code
            if (!InRoom() && this.m_sCode.length() > 0) {
               this.m_sCode = this.m_sCode.substring(0, this.m_sCode.length() - 1);
               this.Refresh();
            }
         } else {
            this.TypeChar(var2);
         }
      }
   }

   public void ButtonPushed(int var1) {
      super.ButtonPushed(var1);
      if (var1 == this.btnExit) {
         this.OnBack();
      } else if (var1 == this.btnOK) {
         this.OnOK();
      } else {
         for (int i = 0; i < _ROWS; i++) {
            if (var1 == this.btnLeft[i] || var1 == this.btnRight[i]) {
               this.Select(i);
               this.Change(i, var1 == this.btnLeft[i] ? -1 : 1);
               return;
            }
         }
      }
   }

   public void Hide() {
      this.m_bShown = false;
      this.DisableCallMethod("OnlineTick");
      super.Hide();
      this.ProcessKb(false);
   }

   public void Show() {
      super.m_Module.SetIgnoreKeys(2);
      super.Show();
      this.ProcessKb(true);
      ((KidsSimpleMenuModule)super.m_Module).ShowBackground(false, false);
      if (!KidsMenuModule.m_CarsManager.IsInitialized()) {
         KidsMenuModule.m_CarsManager.ActivateCarsManager();
      }

      KidsMenuModule.m_CarsManager.Show(this);
      // connexion au serveur à l'ouverture (déjà faite si l'on revient d'une course)
      this.m_bNoNet = !ModOnline.OpenOnline();
      this.m_bShown = true;

      for (int i = 0; i < this.m_Visible.length; i++) {
         this.m_Visible[i] = -1; // super.Show vient de tout montrer
      }

      this.Refresh();
      this.ButtonsOnTop();
      this.DisableCallMethod("OnlineTick");
      this.CallMethodIn("OnlineTick", 0.2F);
   }

   // --- construction ---

   int AddPicture(String sTex, Rect r, float w, float h, float x, float y) {
      KidsMenuWindow wnd = (KidsMenuWindow)this.CreateObject("KidsMenuWindow");
      int id = this.Add(wnd);
      wnd.Init(super.m_Module, id);
      wnd.AddWindowTexture(wnd.AddTexture(sTex, true, true), r, sTex);
      wnd.SetWindowTexture(0);
      wnd.SetSizeAndPos(w, h, x, y, true);
      wnd.Show();
      return id;
   }

   int AddText(float w, float h, float x, float y, boolean bCentered, boolean bLeft) {
      ModText t = (ModText)this.CreateObject("ModText");
      int id = this.Add(t);
      t.Init(super.m_Module, id);
      if (bLeft) {
         t.SetAlignLeft();
      }

      t.SetSizeAndPos(w, h, x, y, bCentered);
      t.Show();
      return id;
   }

   int AddArrow(int nRow, boolean bRight) {
      InitKidsButton init = new InitKidsButton();
      init.sBnTexDisabled = KidsModsMenuWindow._TEX_ARROWS;
      init.sBnTexEnabled = KidsModsMenuWindow._TEX_ARROWS;
      init.sBnTexHighlighted = KidsModsMenuWindow._TEX_ARROWS;
      init.sBnTexPushed = KidsModsMenuWindow._TEX_ARROWS;
      float u = bRight ? 0.5F : 0.0F;
      init.rBnTexDisabled = new Rect(u, 0.0F, u + 0.5F, 0.5F);
      init.rBnTexEnabled = new Rect(u, 0.0F, u + 0.5F, 0.5F);
      init.rBnTexHighlighted = new Rect(u, 0.5F, u + 0.5F, 1.0F);
      init.rBnTexPushed = new Rect(u, 0.5F, u + 0.5F, 1.0F);
      init.xpos = bRight ? _RIGHT_X : _LEFT_X;
      init.ypos = RowCenter(nRow);
      init.width = _ARROW_W;
      init.height = _ARROW_H;
      init.bMoveButton = false;
      KidsButton b = (KidsButton)this.CreateObject("KidsButton");
      int id = this.Add(b);
      b.Init(super.m_Module, this, id, init);
      b.ProcessMouse(true);
      return id;
   }

   void Init(KidsMenuModule var1, int var2) {
      super.Init(var1, var2);
      this.bkTex0 = super.m_TexturesList.AddTexture(this.AddTexture(_TEX_BACK, true, true), new Rect(0.0F, 0.0F, 0.625F, 0.9375F), _TEX_BACK);
      super.m_TexturesList.SetTexture(this, this.bkTex0);
      this.wndPanel = this.AddPicture(_TEX_PANEL, new Rect(0.0F, 0.0F, 1.0F, _PANEL_TEX_V), _PANEL_W, _PANEL_H, 0.5F, _PANEL_Y + _PANEL_H / 2.0F);
      this.wndRow = this.AddPicture(KidsModsMenuWindow._TEX_ROW, new Rect(0.0F, 0.0F, 1.0F, 1.0F), 0.8F, _ROW_H * 1.15F, 0.5F, RowCenter(0));
      this.ktxInfo = this.AddText(0.8F, _PLAYER_H, 0.5F, _INFO_Y, true, false);
      this.ktxHelp1 = this.AddText(0.8F, _PLAYER_H, 0.5F, PlayerCenter(1), true, false);
      this.ktxHelp2 = this.AddText(0.8F, _PLAYER_H, 0.5F, PlayerCenter(2), true, false);

      for (int i = 0; i < _PLAYERS; i++) {
         float y = PlayerCenter(i);
         this.ktxName[i] = this.AddText(_NAME_W, _PLAYER_H, _NAME_X, y - _PLAYER_H / 2.0F, false, true);
         this.ktxAnimal[i] = this.AddText(_COL_W, _PLAYER_H, _ANIMAL_X, y, true, false);
         this.ktxState[i] = this.AddText(_COL_W, _PLAYER_H, _STATE_X, y, true, false);
      }

      for (int i = 0; i < _TOP; i++) {
         float y = TopCenter(i);
         this.ktxTopName[i] = this.AddText(_NAME_W + 0.1F, _TOP_H, _NAME_X, y - _TOP_H / 2.0F, false, true);
         this.ktxTopElo[i] = this.AddText(_COL_W, _TOP_H, _ANIMAL_X, y, true, false);
         this.ktxTopGames[i] = this.AddText(_COL_W, _TOP_H, _STATE_X, y, true, false);
      }

      for (int i = 0; i < _ROWS; i++) {
         float y = RowCenter(i);
         this.ktxLabel[i] = this.AddText(_LABEL_W, _ROW_H, _LABEL_X, y - _ROW_H / 2.0F, false, true);
         this.ktxValue[i] = this.AddText(_VALUE_W, _ROW_H, _VALUE_X, y, true, false);
         this.btnLeft[i] = this.AddArrow(i, false);
         this.btnRight[i] = this.AddArrow(i, true);
      }

      this.ktxStatus = this.AddText(0.8F, _ROW_H, 0.5F, _STATUS_Y, true, false);
      KidsButton var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnOK = this.Add(var3);
      this.btnOKInit.bMoveButton = true;
      this.btnOKInit.xpos = 0.62F;
      this.btnOKInit.ypos = 0.915F;
      this.btnOKInit.width = 0.12F;
      this.btnOKInit.height = 0.16F;
      var3.Init(super.m_Module, this, this.btnOK, this.btnOKInit);
      var3.ProcessMouse(true);
      var3 = (KidsButton)this.CreateObject("KidsButton");
      this.btnExit = this.Add(var3);
      this.btnExitInit.bMoveButton = true;
      this.btnExitInit.xpos = 0.82F;
      this.btnExitInit.ypos = 0.915F;
      var3.Init(super.m_Module, this, this.btnExit, this.btnExitInit);
      var3.ProcessMouse(true);
      this.SetSizeAndPos(1.0F, 1.0F, 0.0F, 0.0F, false);
      this.RefreshSetSizeAndPos();
      this.SetAlphaForAll(1.0F);
      this.ProcessKb(true);
      super.m_Ctrl = new ControlWindowList(4);
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnOK));
      super.m_Ctrl.Push(super.m_List.WindowAt(this.btnExit));
   }
}
