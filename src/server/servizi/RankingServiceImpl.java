package server.servizi;

import condivisi.interfacce.INotifyRanking;
import condivisi.interfacce.INotifyRankingUpdate;
import server.admin.RankingAdmin;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

//classe per gestire il servizio di aggiornamento e notifica della Classifica
//Implementa l'interfaccia INotifyRanking
public class RankingServiceImpl extends RemoteObject implements INotifyRanking {

    //Array dei client registrati alla callback
    private final ArrayList<INotifyRankingUpdate> clients;
    //HashMap contenente callback con relativo username
    private final HashMap<INotifyRankingUpdate, String> clientHashMap;

    private final RankingAdmin rankingAdmin;

    public RankingServiceImpl(RankingAdmin rankingAdmin) throws RemoteException {
        //super();
        clients = new ArrayList<INotifyRankingUpdate>();
        clientHashMap = new HashMap<INotifyRankingUpdate, String>();
        this.rankingAdmin = rankingAdmin;
    }

    // fa partire il servizio per il Client di registrazione della callback
    public static INotifyRanking startNotifyService(String classificaSvcName, int port, RankingAdmin rankingAdmin){
        try{
            RankingServiceImpl server = new RankingServiceImpl(rankingAdmin);
            INotifyRanking stub = (INotifyRanking) UnicastRemoteObject.exportObject(server,0);
            LocateRegistry.createRegistry(port);
            Registry registry = LocateRegistry.getRegistry(port);
            registry.bind(classificaSvcName, stub);
            System.out.printf("Il servizio di notifica della Classifica è attivo (porta = %d) \n", port);
            return server;
        }
        catch (Exception e){
            System.out.println("Errore in RankingServiceIMpl.startNotifyRanking" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    //Metodo effettivo della registrazione del Client alla Callback
    public synchronized void registerCallBack(String username, INotifyRankingUpdate interfacciaClient) throws RemoteException{
        if(!clients.contains(interfacciaClient)){
            clients.add(interfacciaClient);
            clientHashMap.put(interfacciaClient, username);
            System.out.println();
            System.out.println("Un nuovo Client si è iscritto alla Callback per la ricezione di aggiornamenti della Classifica.");
            System.out.printf("Client : %s" + "\n", username);
            System.out.println();
            sendRankToEveryone(interfacciaClient, username); //Invia la classifica al Client
        }
        else System.out.printf("Il client %s è già registrato alla Callback", username);
    }

    public synchronized void sendRankToEveryone(INotifyRankingUpdate interfacciaClient, String username){
        var classifica = RankingAdmin.getRanking();
        //INVIARE LA CLASSIFICA IN QUALCHE MODO
        try{
            interfacciaClient.rankingUpdateEvent(classifica);//chiamo INotifyRankingUpdate.rankingUpdateEvent
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }
}
