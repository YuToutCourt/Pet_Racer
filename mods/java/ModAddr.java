import java.net.InetAddress;

// [mod] Adresse UDP (IP + port) pour le multijoueur en ligne (chantier 8, notes/online.md).
// Le JRE 1.3 du jeu n'a pas InetSocketAddress : cette petite classe la remplace (comparable, affichable).
class ModAddr {
   InetAddress m_Ip;
   int m_nPort;
   // adresse fictive « place N d'un salon » (phase 3) : ModUdp emballe ce qui lui est envoyé pour le serveur
   // (ModLobby.SendRelay) ; ModLobby donne cette adresse aux paquets que le serveur rend. -1 = vraie adresse.
   int m_nSlot = -1;

   ModAddr(int nSlot) {
      m_nSlot = nSlot;
   }

   ModAddr(InetAddress ip, int nPort) {
      m_Ip = ip;
      m_nPort = nPort;
   }

   // résout un nom ou une IP « a.b.c.d » ; null (et une ligne au journal) si impossible
   static ModAddr Resolve(String sHost, int nPort) {
      try {
         return new ModAddr(InetAddress.getByName(sHost), nPort);
      } catch (Throwable e) {
         GameObject.CrashLog("[mod] net : adresse " + sHost + ":" + nPort + " introuvable (" + e + ")\n");
         return null;
      }
   }

   public boolean equals(Object o) {
      if (!(o instanceof ModAddr)) {
         return false;
      }

      ModAddr a = (ModAddr)o;
      if (a.m_nSlot >= 0 || m_nSlot >= 0) {
         return a.m_nSlot == m_nSlot;
      }

      return a.m_nPort == m_nPort && a.m_Ip.equals(m_Ip);
   }

   public int hashCode() {
      return m_nSlot >= 0 ? m_nSlot : m_Ip.hashCode() * 31 + m_nPort;
   }

   public String toString() {
      return m_nSlot >= 0 ? "place " + m_nSlot + " (serveur)" : m_Ip.getHostAddress() + ":" + m_nPort;
   }
}
