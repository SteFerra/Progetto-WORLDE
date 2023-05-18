package server;

// configurazione del server, in caso il file di config non sia leggibile, il server utilizza delle impostazioni predefinite.

import condivisi.ConfigReader;

import java.io.FileNotFoundException;
import java.io.IOException;

public class WordleServerConfig {

    public String registrazioneHost = "localhost";
    public int registrazionePort = 7777;
    public String registrazioneServiceName = "WORDLE-REGSERVER";

    public String host = "localhost";
    public int port = 7778;

    //configurazione per RMI callback sulla classifica degli utenti
    public String classificaSvcHost ="localhost";
    public String classificaSvcName ="rankingNotifyServer";
    public int classificaSvcPort = 7779;

    public int multicastPort = 44444;
    public String multicastAddress ="239.255.32.32";

    public void LoadConfig(String filename){
        ConfigReader cfgReader = new ConfigReader(filename);

        try{
            cfgReader.LoadConfiguration();
        }catch (FileNotFoundException e){
            System.out.println("Il File di Configurazione non è stato trovato. Utilizzo i valori predefiniti. ");
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            //Comando REGISTER
            registrazioneHost = cfgReader.getStringValue("registrazionehost", registrazioneHost);
            registrazionePort = cfgReader.getIntValue("registrazioneport", registrazionePort);
            registrazioneServiceName = cfgReader.getStringValue("registrazioneservicename", registrazioneServiceName);

            //Connessione al Server TCP
            host = cfgReader.getStringValue("server", host);
            port = cfgReader.getIntValue("tcpport", port);

            //RMI callback
            classificaSvcHost = cfgReader.getStringValue("classificasvchost", classificaSvcHost);
            classificaSvcName = cfgReader.getStringValue("classificasvcname", classificaSvcName);
            classificaSvcPort = cfgReader.getIntValue("classificasvcport", classificaSvcPort);

            //Multicast
            multicastPort = cfgReader.getIntValue("multiport", multicastPort);
            multicastAddress = cfgReader.getStringValue("multicast", multicastAddress);
        }catch (NumberFormatException e){
            System.out.println("Errore nei dati. Utilizzo dei valori predefiniti.");
        }
    }
}
