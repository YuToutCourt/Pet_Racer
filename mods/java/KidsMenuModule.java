public class KidsMenuModule extends Module {
   MenuCamera m_Camera = null;
   int m_View = -1;
   int m_IgnoreKeys = 0;
   KidsCursor m_Cursor = null;
   static CarsManager m_CarsManager = null;
   MenuStack m_MenuStack = new MenuStack();
   WindowsList m_WindowsList = new WindowsList(50);
   int m_CurrentMenu = -1;
   RacerModule m_LevelModuleToDelete = null;

   void OnChangeResolution() {
      this.m_Cursor.OnChangeResolution();
      this.m_Cursor.SetCursor(0);
      this.ActivateMenuCamera();
   }

   public void OnFrame() {
      if (this.m_IgnoreKeys > 0) {
         this.m_IgnoreKeys += -1;
      }
   }

   void ButtonsOnTop() {
      KidsMenuWindow var1 = null;

      for (int var2 = 0; var2 < this.m_WindowsList.m_Cnt; var2++) {
         var1 = (KidsMenuWindow)this.m_WindowsList.WindowAt(var2);
         if (var1 != null) {
            var1.ButtonsOnTop();
         }
      }
   }

   void PushMenu(int var1) {
      this.m_MenuStack.Push(var1);
   }

   int PopMenu() {
      return this.m_MenuStack.Pop();
   }

   int GetMenu() {
      return this.m_MenuStack.Get();
   }

   void ClearStack() {
      this.m_MenuStack.ClearStack();
   }

   void OnDeactivate() {
      this.UnregisterKeyRequest();
      this.DeactivateMenuCamera();
   }

   void OnActivate() {
      this.RegisterKeyRequest();
      this.ActivateMenuCamera();
   }

   int GetMenuCX() {
      return this.m_MenuStack.m_Context;
   }

   void SetCursor(int var1) {
      this.m_Cursor.SetCursor(var1);
   }

   void DeactivateMenuCamera() {
      if (this.m_View != -1) {
         this.DeactivateView(this.m_View);
         this.RemoveView(this.m_View);
         this.m_View = -1;
      }
   }

   void DeactivateCursor() {
      this.m_Cursor.DeactivateCursor();
   }

   Window GetWindowAt(int var1) {
      return var1 >= 0 && var1 < this.m_WindowsList.m_Cnt ? this.m_WindowsList.WindowAt(var1) : null;
   }

   void Destroy() {
      this.m_Cursor.Destroy();
      this.m_Cursor = null;
      this.m_WindowsList.Destroy(null);
   }

   void RestoreAllWindowsTextures() {
      for (int var1 = 0; var1 < this.m_WindowsList.m_Cnt; var1++) {
         KidsMenuWindow var2 = (KidsMenuWindow)this.m_WindowsList.WindowAt(var1);
         if (var2 != null) {
            var2.RestoreTextures();
         }
      }
   }

   void ActivateMenuWindow(int var1) {
      if (var1 >= 0 && var1 < this.m_WindowsList.m_Cnt) {
         if (var1 != this.m_CurrentMenu) {
            KidsMenuWindow var2 = (KidsMenuWindow)this.m_WindowsList.m_aList[var1];
            if (var2 != null) {
               var2.ActivateWindow();
               this.m_CurrentMenu = var1;
            }

            this.SetCursor(0);
         }
      }
   }

   // [mod] Les menus ignorent les touches pendant « var1 » images après l'ouverture d'un écran ou un Entrée, pour
   // qu'une même pression n'agisse pas aussi sur l'écran suivant. 2 images = 67 ms à 30 images/s, mais 4 ms à
   // 500 images/s. On garde le nombre d'images et on ajoute la durée qu'il représentait à 30 images/s.
   static final float _IGNORE_KEYS_FRAME_TIME = 1.0F / 30.0F;
   float m_fIgnoreKeysUntil = -1.0F;

   void SetIgnoreKeys(int var1) {
      this.m_IgnoreKeys = var1;
      this.m_fIgnoreKeysUntil = var1 > 0 ? ModFrameLimiter.Now() + var1 * _IGNORE_KEYS_FRAME_TIME : -1.0F;
   }

   boolean CanIgnoreKeys() {
      if (this.m_IgnoreKeys > 0) {
         return true;
      }

      if (this.m_fIgnoreKeysUntil >= 0.0F) {
         float fNow = ModFrameLimiter.Now();
         // horloge revenue en arrière (ne devrait pas arriver) : on ne bloque pas les menus
         if (fNow < this.m_fIgnoreKeysUntil && this.m_fIgnoreKeysUntil - fNow < 1.0F) {
            return true;
         }

         this.m_fIgnoreKeysUntil = -1.0F;
      }

      return false;
   }

   void ActivateMenuCamera() {
      this.DeactivateMenuCamera();
      this.m_View = this.AddView(this.m_Camera, 0.0F, 0.0F, 1.0F, 1.0F);
      this.ActivateView(this.m_View);
      this.ListenerOnViewActive(this.m_View, true);
   }

   void PlayCameraSound(String var1) {
      if (this.m_Camera != null && this.m_Camera.coSpeaker != null && var1 != null) {
         this.m_Camera.coSpeaker.AttachOneShotSound(var1);
      }
   }

   void ActivateCursor() {
      KidsMenuWindow var1 = null;
      int var2 = this.m_WindowsList.m_Cnt;

      for (int var3 = 0; var3 < var2; var3++) {
         var1 = (KidsMenuWindow)this.m_WindowsList.WindowAt(var3);
         if (var1 != null) {
            var1.DeactivateKeyFocus();
         }
      }

      this.m_Cursor.ActivateCursor();
   }

   void HideAllWindows() {
      this.m_WindowsList.HideWindows();
   }

   void DeleteRacerModule() {
      this.DeleteAllSounds();
      this.CallMethodIn("DeleteModule", 0.1F);
   }

   void ReleaseAllWindowsTextures() {
      for (int var1 = 0; var1 < this.m_WindowsList.m_Cnt; var1++) {
         KidsMenuWindow var2 = (KidsMenuWindow)this.m_WindowsList.WindowAt(var1);
         if (var2 != null) {
            var2.ReleaseTextures();
         }
      }
   }

   public void OnCreate() {
      this.m_Cursor = (KidsCursor)this.CreateObject("KidsCursor");
      this.m_Cursor.Init(this);
      this.m_Camera = (MenuCamera)this.CreateObject("MenuCamera");
      this.m_Camera.Init(this);
   }

   void OnInputKey(int var1, boolean var2, char var3) {
      if (!var2) {
         switch (var1) {
            case 0:
         }
      }
   }

   void SetMenuCX(int var1) {
      this.m_MenuStack.m_Context = var1;
   }

   public void InitModule() {
      this.m_Camera.InitObject();
      this.m_Camera.Init(this);
   }

   void DeleteModule() {
      if (this.m_LevelModuleToDelete != null) {
         this.m_LevelModuleToDelete.delete();
         this.m_LevelModuleToDelete = null;
      }
   }
}
