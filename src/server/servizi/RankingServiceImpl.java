package server.servizi;

import condivisi.ClassificaData;
import condivisi.interfacce.INotifyRanking;
import condivisi.interfacce.INotifyRankingUpdate;
import server.admin.RankingAdmin;
import server.domini.utentiConnessi;

import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//classe per gestire il servizio di aggiornamento e notifica della Classifica
//Implementa l'interfaccia INotifyRanking
public class RankingServiceImpl extends RemoteObject implements INotifyRanking {

    //Array dei client registrati alla callback
    public static ArrayList<INotifyRankingUpdate> clients;
    //HashMap contenente callback con relativo username
    private final HashMap<INotifyRankingUpdate, String> clientHashMap;

    private final RankingAdmin rankingAdmin;

    public RankingServiceImpl(RankingAdmin rankingAdmin) throws RemoteException {
        //super();
        clients = new ArrayList<INotifyRankingUpdate>();
        clientHashMap = new HashMap<INotifyRankingUpdate, String>();
        this.rankingAdmin = rankingAdmin;
    }

    public HashMap<INotifyRankingUpdate, String> getClientHashMap(){
        return clientHashMap;
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
    public synchronized void registraCallback(String username, INotifyRankingUpdate interfacciaClient) throws RemoteException{
        if(!clients.contains(interfacciaClient)){
            clients.add(interfacciaClient);
            clientHashMap.put(interfacciaClient, username);
            System.out.println();
            System.out.println("Un nuovo Client si è iscritto alla Callback per la ricezione di aggiornamenti della Classifica.");
            System.out.printf("Client : %s" + "\n", username);
            System.out.println();
            inviaClassifica(interfacciaClient, username); //Invia la classifica al Client
        }
        else System.out.printf("Il client %s è già registrato alla Callback", username);
    }

    public synchronized void cancRegistrazioneCallback(String username, INotifyRankingUpdate interfacciaClient) throws RemoteException{
        if(clients.remove(interfacciaClient)){
            System.out.printf("Il Client [%s] si è cancellato dalla registrazione della Callback \n", username);
            clientHashMap.remove(interfacciaClient);
            System.out.println("Client callback attivi: " + clientHashMap.values());
        }
        else{
            System.out.printf("Impossibile cancellare la registrazione alla Callback del Client [%s] \n", username);
        }
    }

    public INotifyRankingUpdate getStub(String username){
        for(Map.Entry<INotifyRankingUpdate, String> entry : clientHashMap.entrySet()){
            if(entry.getValue().equals(username)){
                INotifyRankingUpdate stub = entry.getKey();
                return stub;
            }
        }
        return null;
    }

    public synchronized void inviaClassifica(INotifyRankingUpdate interfacciaClient, String username){
        var classifica = RankingAdmin.getRanking();
        ClassificaData classificaData = new ClassificaData(classifica);
        try{
            interfacciaClient.rankingUpdateEvent(classificaData);//chiamo INotifyRankingUpdate.rankingUpdateEvent
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }
    public static void aggiornaPosizioni(ArrayList<INotifyRankingUpdate> clients){
        String messaggio = "C'è stato un cambiamento nelle prime tre posizioni della classifica";
        for(INotifyRankingUpdate client : clients){
            try{
                client.aggiornaPosiz(messaggio);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }
    }


}
