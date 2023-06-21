package client;


import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

//Classe del Thread che viene utilizzato per ricevere i risultati dagli altri utenti del gruppo multicast
public class MulticastClientThread extends Thread {
    final int multicastPort;
    final String multicastAddress;
    public static final int size = 1024;

    public MulticastClientThread(String multicastAddress, int multicastPort) {
        this.multicastPort = multicastPort;
        this.multicastAddress = multicastAddress;
    }

    public void run(){
        try(MulticastSocket socket = new MulticastSocket(multicastPort)){
            InetAddress gruppo = InetAddress.getByName(multicastAddress);
            if(!gruppo.isMulticastAddress()){
                throw new UnknownHostException("Indirizzo di Multicast non è valido");

            }
            socket.joinGroup(gruppo);
            while(!isInterrupted()){
                DatagramPacket packet = new DatagramPacket(new byte[size], size);
                socket.receive(packet);
                System.out.println("Messaggio arrivato dal gruppo Multicast: " + new String(packet.getData(), packet.getOffset(), packet.getLength()));
            }
        }
        catch (Exception e){
            System.out.println("Errore nel multicast: " + e.getMessage());
        }

    }
}
