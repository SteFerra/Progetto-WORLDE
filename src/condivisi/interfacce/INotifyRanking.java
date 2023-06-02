package condivisi.interfacce;

import java.nio.channels.SocketChannel;
import java.rmi.Remote;
import java.rmi.RemoteException;

//Interfaccia usata per la registrazione/deregistrazione della callback del client
public interface INotifyRanking extends Remote{

    public void registraCallback(String username, INotifyRankingUpdate clientInterfaccia) throws RemoteException;

    public void cancRegistrazioneCallback(String username, INotifyRankingUpdate clientInterfaccia) throws RemoteException;

    public INotifyRankingUpdate getStub(String username) throws RemoteException;
}
