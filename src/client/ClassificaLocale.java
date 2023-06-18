package client;

import condivisi.ClassificaData;
import condivisi.interfacce.INotifyRanking;
import condivisi.interfacce.INotifyRankingUpdate;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

//Questa classe è utilizzata per mantenere salvata la classifica in locale
public class ClassificaLocale {

    private HashMap<String, Double> classifica;
    private final String classificaName;
    private final String classificaHost;
    private final int classificaPort;

    //Interfacce chiamate per la callback
    private INotifyRanking server;
    private INotifyRankingUpdate stubCallback;
    private String username;

    public ClassificaLocale(String classificasvcHost, String classificasvcName, int classificasvcPort){
        this.classifica = new HashMap<>();
        this.classificaHost = classificasvcHost;
        this.classificaName = classificasvcName;
        this.classificaPort = classificasvcPort;
    }

    public void inizializza() throws RemoteException, NotBoundException{
        Registry registry = LocateRegistry.getRegistry(classificaHost, classificaPort);
        server = (INotifyRanking) registry.lookup(classificaName);
    }

    //Registrazione della Callback per la notifica dell'aggiornamento della classifica
    public void registrazioneCallback(String username){
        if(stubCallback != null){
            System.out.println("La Registrazione alla Callback è già stata effettuata");
            return;
        }
        try{
            System.out.println("\t" + "Registrazione alla Callback in corso");
            var callbackObj = new NotificaAggClassificaImpl(this);
            var stub = (INotifyRankingUpdate) UnicastRemoteObject.exportObject(callbackObj, 0);
            server.registraCallback(username, stub);
            System.out.println("\t" + "Registrazione completata");
            stubCallback = stub;
            this.username=username;
        }catch (Exception e){
            System.err.println("Eccezione del Client: " + e.getMessage());
        }
    }

    public void deregistrazioneCallback(){
        if(stubCallback == null){
            System.out.println("\t" + "Hai già effettuato la cancellazione della registrazione alla Callback");
        }
        try{
            if(server!=null && stubCallback != null){
                System.out.println("\t" + "Sto effettuando la cancellazione della registrazione alla Callback");
                server.cancRegistrazioneCallback(username, stubCallback);
                stubCallback=null;
                System.out.println("\t" + "Cancellazione completata");
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    //aggiorna la classifica salvata in locale dalla classifica inviata dal Server
    void aggiornaClassificaLocale(ClassificaData classificaData){
        this.classifica = classificaData.getClassifica();
    }

    public HashMap<String, Double> StampaClassifica(){
        return this.classifica;
    }
}
