package client;


import condivisi.ClassificaData;
import condivisi.interfacce.INotifyRanking;
import condivisi.interfacce.INotifyRankingUpdate;
import server.servizi.RankingServiceImpl;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.HashMap;

//implementazione della Callback RMI chiamata dal server quando si ha un aggiornamento della classifica
public class NotificaAggClassificaImpl extends RemoteObject implements INotifyRankingUpdate {
    private final ClassificaLocale classificalocale;

    public NotificaAggClassificaImpl(ClassificaLocale classificaloc){
        super();
        this.classificalocale=classificaloc;
    }


    @Override
    public void rankingUpdateEvent(ClassificaData classificaData) throws RemoteException {
        classificalocale.aggiornaClassificaLocale(classificaData);
    }

    @Override
    public void aggiornaPosiz(String messaggio) throws RemoteException {
        System.out.println(messaggio);
    }
}
