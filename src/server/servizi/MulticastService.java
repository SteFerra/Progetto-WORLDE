package server.servizi;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MulticastService {
    private final String multicastAddress;
    private final int multicastPort;
    DatagramSocket datagramSocket;
    InetAddress group;
    public List<String> arrayList;

    public MulticastService(String multicastAddress, int multicastPort){
        this.multicastAddress=multicastAddress;
        this.multicastPort=multicastPort;
        this.arrayList = new ArrayList<>();
    }

    //apertura connessione al gruppo multicast
    public void start(){
        try  {
            group = InetAddress.getByName(multicastAddress);
            if (!group.isMulticastAddress()) {
                throw new UnknownHostException("Indirizzo di Multicast non è valido");
            }
            datagramSocket = new DatagramSocket();
        }
        catch (Exception exc) {
            System.out.println("Errore server: " + exc.getMessage());
        }
    }

    public void inviaMessaggioMulticast(Set<String> parole){
        arrayList.add(parole.toString());
        String message = String.join(",", parole);
        byte[] content = message.getBytes();
        DatagramPacket packet = new DatagramPacket(content, content.length, group, multicastPort);
        // Invio il pacchetto.
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            System.out.println("Errore: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
