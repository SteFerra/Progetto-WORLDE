package server.servizi;

import java.nio.channels.SocketChannel;
import java.util.Map;


//Classe utilizzata dal Thread logoutAutomaticoThread che rimuove periodicamente i Client che risultano
//non più connessi dall'elenco degli utenti connessi
public class logoutAutomatico implements Runnable {

    private Map<String,SocketChannel> utentiLoggati;
    private int logoutTimer;

    public logoutAutomatico(Map<String, SocketChannel> utentiLoggati, int logoutTimer) {
        this.utentiLoggati=utentiLoggati;
        this.logoutTimer=logoutTimer;
    }

    public void run(){
        try{
            while(!Thread.currentThread().isInterrupted()){
                for(Map.Entry<String, SocketChannel> elemento : utentiLoggati.entrySet()){
                    String username = elemento.getKey();
                    SocketChannel socketChannel = elemento.getValue();
                    if(!socketChannel.isConnected())
                        utentiLoggati.remove(username);
                }
                Thread.sleep(logoutTimer);
            }
        }catch (InterruptedException e){}
    }
}
