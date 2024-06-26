package condivisi.interfacce;

import condivisi.ClassificaData;

import java.rmi.Remote;
import java.rmi.RemoteException;


// interfaccia usata per comunicare al client attraverso la callback l'aggiornamento della classifica
public interface INotifyRankingUpdate extends Remote {

    //invia al Client registrato alla Callback la classifica attraverso la classe ClassificaData la quale contiene la classifica Serializzata
    public void rankingUpdateEvent(ClassificaData classifica) throws RemoteException;

    //invia ai Client registrati alla Callback il messaggio che c'è stato un aggiornamento nelle prime tre posizioni della classifica
    public void aggiornaPosiz(String messaggio) throws RemoteException;
}
