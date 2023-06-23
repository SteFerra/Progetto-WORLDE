package server.domini;


import java.io.IOException;
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

    public String getUsername(SocketChannel client){
        for (Map.Entry<String, SocketChannel> entry : utentiLoggati.entrySet()) {
            if (client.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    //chiude i socket degli utenti connessi al server
    //viene chiamato dal ServerShutdownHook
    public void chiudiSocket() throws IOException {
        for(SocketChannel socketChannel : utentiLoggati.values()){
            try{
                socketChannel.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
