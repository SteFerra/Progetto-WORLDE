package server.domini;


import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//classe che rappresenta gli utenti che hanno effettuato la login
//vengono salvati in un HashMap<String, SocketChannel>
public class utentiConnessi {

    public Map<String, SocketChannel> utentiLoggati = new ConcurrentHashMap<>();

    public boolean èConnesso (String username){
        if(utentiLoggati.containsKey(username)) return true;
        return false;
    }


}
