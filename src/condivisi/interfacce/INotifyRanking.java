package condivisi.interfacce;

import java.rmi.Remote;
import java.rmi.RemoteException;

//Interfaccia usata per la registrazione/deregistrazione della callback del client
public interface INotifyRanking extends Remote{

    public void registerCallBack(String username, INotifyRankingUpdate clientInterfaccia) throws RemoteException;
}
