package server.servizi;

import java.nio.channels.SocketChannel;
import java.util.Map;

//Classe utilizzata dal Thread logoutAutomaticoThread che rimuove periodicamente i Client che risultano
//non più connessi dall'elenco degli utenti connessi, avviene quando un client si disconnette in modo anomalo
public class logoutAutomatico implements Runnable {

    private Map<String,SocketChannel> utentiLoggati;
    private int logoutTimer;

    public logoutAutomatico(Map<String, SocketChannel> utentiLoggati, int logoutTimer) {
        this.utentiLoggati=utentiLoggati;
        this.logoutTimer=logoutTimer;
    }

    public void run(){
        try{
            while(!Thread.currentThread().isInterrupted()){     //fino a quando il Thread non viene interrotto controlla periodicamente tutti i client se sono connessi
                for(Map.Entry<String, SocketChannel> elemento : utentiLoggati.entrySet()){  //ed elimina quelli disconnessi
                    String username = elemento.getKey();
                    SocketChannel socketChannel = elemento.getValue();
                    if(!socketChannel.isConnected()) {
                        utentiLoggati.remove(username); //ed elimina quelli disconnessi dalla lista.
                    }
                }
                Thread.sleep(logoutTimer);
            }
        }catch (InterruptedException e){}
    }
}
