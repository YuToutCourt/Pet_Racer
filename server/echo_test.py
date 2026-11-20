"""Echo UDP pour le test de faisabilite (phase 0 du multijoueur en ligne de Pet Racer).

    python server\echo_test.py [port]

Renvoie chaque paquet tel quel a son expediteur. Cote jeu : NetTest("127.0.0.1", 7778) dans nettest.scr
(a la racine du jeu), puis lire les lignes « [mod] net » de crash.log.
"""
import socket
import sys

port = int(sys.argv[1]) if len(sys.argv) > 1 else 7778
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
if hasattr(socket, "SIO_UDP_CONNRESET"):
    sock.ioctl(socket.SIO_UDP_CONNRESET, False)  # Windows : un jeu ferme ne coupe pas l'echo
sock.bind(("0.0.0.0", port))
print(f"echo UDP sur le port {port}", flush=True)
while True:
    data, addr = sock.recvfrom(2048)
    print(f"{len(data)} octets de {addr[0]}:{addr[1]}", flush=True)
    sock.sendto(data, addr)
