package client;

import condivisi.interfacce.INotifyRanking;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

//Questa classe è utilizzata per mantenere salvata la classifica in locale
public class ClassificaLocale {

    private HashMap<String, Integer> classifica;
    private final String classificaName;
    private final String classificaHost;
    private final int classificaPort;

    //Interfacce chiamate per la callback
    private INotifyRanking server;

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


    public HashMap<String, Integer> StampaClassifica(){
        return this.classifica;
    }
}
