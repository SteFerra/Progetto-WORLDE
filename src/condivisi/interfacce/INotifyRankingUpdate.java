package condivisi.interfacce;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

// interfaccia usata per comunicare al client attraverso la callback l'aggiornamento della classifica
public interface INotifyRankingUpdate extends Remote {

    public void rankingUpdateEvent(HashMap<String, Integer> classifica) throws RemoteException;
}
