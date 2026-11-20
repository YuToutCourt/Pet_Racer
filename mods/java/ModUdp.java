import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Vector;

// [mod] Socket UDP du multijoueur en ligne (chantier 8, notes/online.md).
// Le JRE 1.3 du jeu n'a pas java.nio (pas de DatagramChannel non bloquant) : un fil démon fait les receive()
// bloquants et range les paquets dans une file ; le fil du jeu la vide à chaque image avec Poll(), sans jamais
// attendre. Envoi direct depuis le fil du jeu (send() sur UDP ne bloque pas en pratique).
class ModUdp implements Runnable {
   static final int _MAX_PACKET = 1500;
   // au-delà, les paquets reçus sont jetés (le jeu ne vide plus la file : chargement, fenêtre déplacée…)
   static final int _MAX_QUEUE = 512;

   DatagramSocket m_Sock;
   Thread m_Thread;
   volatile boolean m_bOpen;
   final Vector m_Queue = new Vector();
   // compteurs pour le journal
   int m_nReceived;
   int m_nDropped;
   int m_nRecvErrors;
   int m_nSendErrors;

   // nPort = 0 : port choisi par le système. null si le port est pris ou le socket refusé (déjà journalisé).
   static ModUdp Open(int nPort) {
      ModUdp u = new ModUdp();

      try {
         u.m_Sock = new DatagramSocket(nPort);
         u.m_bOpen = true;
         u.m_Thread = new Thread(u, "ModUdp");
         u.m_Thread.setDaemon(true);
         u.m_Thread.start();
         GameObject.CrashLog("[mod] net : socket UDP ouvert sur le port " + u.m_Sock.getLocalPort() + "\n");
         return u;
      } catch (Throwable e) {
         GameObject.CrashLog("[mod] net : impossible d'ouvrir le port UDP " + nPort + " (" + e + ")\n");
         if (u.m_Sock != null) {
            u.m_Sock.close();
         }

         return null;
      }
   }

   int GetLocalPort() {
      return m_Sock == null ? 0 : m_Sock.getLocalPort();
   }

   boolean Send(ModAddr to, ModBuf b) {
      return Send(to, b.m_Data, b.Length());
   }

   boolean Send(ModAddr to, byte[] data, int nLen) {
      if (!m_bOpen || to == null) {
         return false;
      }

      if (to.m_nSlot >= 0) {
         return ModLobby.SendRelay(to.m_nSlot, data, nLen); // [mod] phase 3 : par le serveur
      }

      try {
         m_Sock.send(new DatagramPacket(data, nLen, to.m_Ip, to.m_nPort));
         return true;
      } catch (Throwable e) {
         if (m_nSendErrors++ < 5) {
            GameObject.CrashLog("[mod] net : envoi vers " + to + " refuse (" + e + ")\n");
         }

         return false;
      }
   }

   // paquet suivant de la file, ou null : ne bloque jamais
   ModPacket Poll() {
      synchronized (m_Queue) {
         if (m_Queue.isEmpty()) {
            return null;
         }

         ModPacket p = (ModPacket)m_Queue.elementAt(0);
         m_Queue.removeElementAt(0);
         return p;
      }
   }

   void Close() {
      m_bOpen = false;
      if (m_Sock != null) {
         m_Sock.close(); // débloque le receive() du fil
      }
   }

   // fil démon : reçoit, copie, range. Sous Windows, un envoi vers un port fermé fait échouer le receive() suivant
   // (« connection reset ») : on l'ignore et on continue ; une rafale d'erreurs fait une petite pause.
   public void run() {
      byte[] buf = new byte[_MAX_PACKET];
      int nErrorsInRow = 0;

      while (m_bOpen) {
         DatagramPacket p = new DatagramPacket(buf, buf.length);

         try {
            m_Sock.receive(p);
            nErrorsInRow = 0;
         } catch (Throwable e) {
            if (!m_bOpen) {
               break;
            }

            m_nRecvErrors++;
            if (++nErrorsInRow > 20) {
               try {
                  Thread.sleep(10L);
               } catch (InterruptedException ie) {
               }
            }

            continue;
         }

         ModPacket pk = new ModPacket();
         pk.m_From = new ModAddr(p.getAddress(), p.getPort());
         pk.m_nLen = p.getLength();
         pk.m_Data = new byte[pk.m_nLen];
         System.arraycopy(buf, 0, pk.m_Data, 0, pk.m_nLen);
         pk.m_lTime = System.currentTimeMillis();

         synchronized (m_Queue) {
            if (m_Queue.size() >= _MAX_QUEUE) {
               m_nDropped++;
            } else {
               m_Queue.addElement(pk);
               m_nReceived++;
            }
         }
      }
   }
}

// un paquet reçu (rangé par le fil de ModUdp)
class ModPacket {
   ModAddr m_From;
   byte[] m_Data;
   int m_nLen;
   // heure de réception (ms, System.currentTimeMillis) : plus juste que l'heure où le jeu le lit
   long m_lTime;
}
