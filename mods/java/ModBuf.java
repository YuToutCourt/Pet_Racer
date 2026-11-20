// [mod] Tampon d'octets pour les paquets réseau (chantier 8, notes/online.md).
// Remplace java.nio.ByteBuffer, absent du JRE 1.3 du jeu. Gros-boutiste, comme struct.pack(">…") du serveur Python.
// Écriture : Clear(), Put…(), puis Length() octets de m_Data à envoyer.
// Lecture : Wrap(octets, longueur), puis Get…() ; une lecture au-delà de la fin rend 0 et lève m_bBad.
class ModBuf {
   byte[] m_Data;
   int m_nPos;
   int m_nLen;
   boolean m_bBad;

   ModBuf(int nSize) {
      m_Data = new byte[nSize];
   }

   void Clear() {
      m_nPos = 0;
      m_nLen = 0;
      m_bBad = false;
   }

   void Wrap(byte[] data, int nLen) {
      m_Data = data;
      m_nPos = 0;
      m_nLen = nLen;
      m_bBad = false;
   }

   int Length() {
      return m_nPos;
   }

   int Remaining() {
      return m_nLen - m_nPos;
   }

   // --- écriture (sans contrôle : le tampon est dimensionné pour le plus gros paquet) ---

   void PutByte(int v) {
      m_Data[m_nPos++] = (byte)v;
   }

   void PutShort(int v) {
      m_Data[m_nPos++] = (byte)(v >> 8);
      m_Data[m_nPos++] = (byte)v;
   }

   void PutInt(int v) {
      m_Data[m_nPos++] = (byte)(v >> 24);
      m_Data[m_nPos++] = (byte)(v >> 16);
      m_Data[m_nPos++] = (byte)(v >> 8);
      m_Data[m_nPos++] = (byte)v;
   }

   void PutLong(long v) {
      PutInt((int)(v >> 32));
      PutInt((int)v);
   }

   void PutFloat(float v) {
      PutInt(Float.floatToIntBits(v));
   }

   // chaîne courte : longueur sur un octet puis caractères Latin-1 (noms de joueurs, codes de salon)
   void PutString(String s) {
      int n = Math.min(s.length(), 255);
      PutByte(n);

      for (int i = 0; i < n; i++) {
         PutByte(s.charAt(i));
      }
   }

   // --- lecture ---

   boolean Need(int n) {
      if (m_nPos + n > m_nLen) {
         m_bBad = true;
         m_nPos = m_nLen;
         return false;
      }

      return true;
   }

   // octet non signé (0..255)
   int GetByte() {
      return Need(1) ? m_Data[m_nPos++] & 255 : 0;
   }

   // court signé (-32768..32767)
   int GetShort() {
      if (!Need(2)) {
         return 0;
      }

      int v = m_Data[m_nPos] << 8 | m_Data[m_nPos + 1] & 255;
      m_nPos += 2;
      return (short)v;
   }

   int GetInt() {
      if (!Need(4)) {
         return 0;
      }

      int v = (m_Data[m_nPos] & 255) << 24 | (m_Data[m_nPos + 1] & 255) << 16 | (m_Data[m_nPos + 2] & 255) << 8 | m_Data[m_nPos + 3] & 255;
      m_nPos += 4;
      return v;
   }

   long GetLong() {
      long hi = GetInt();
      long lo = GetInt() & 0xFFFFFFFFL;
      return hi << 32 | lo;
   }

   float GetFloat() {
      return Float.intBitsToFloat(GetInt());
   }

   String GetString() {
      int n = GetByte();
      if (!Need(n)) {
         return "";
      }

      StringBuffer s = new StringBuffer(n);

      for (int i = 0; i < n; i++) {
         s.append((char)(m_Data[m_nPos++] & 255));
      }

      return s.toString();
   }
}
