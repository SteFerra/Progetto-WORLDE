package server;


import com.google.gson.Gson;
import condivisi.CodiciRisposta;
import condivisi.Comandi;
import condivisi.Risposta;
import server.admin.RankingAdmin;
import server.admin.UserAdmin;
import server.domini.UserSession;
import server.servizi.MulticastService;
import server.servizi.RankingServiceImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

//classe che esegue i vari comandi passati al Threadpool
public class TaskThreadPool implements Runnable{
    private final UserSession userSession;
    private final SocketChannel client;
    private final Comandi cmd;
    private final RankingServiceImpl rankingService;
    private final UserAdmin userAdmin;
    private final MulticastService multicastService;
    private RankingAdmin rankingAdmin;

    public TaskThreadPool(UserSession userSession, SocketChannel client, Comandi cmd, RankingServiceImpl rankingService, UserAdmin userAdmin, MulticastService multicastService, RankingAdmin rankingAdmin){
        this.userSession=userSession;
        this.client=client;
        this.cmd=cmd;
        this.rankingService=rankingService;
        this.userAdmin=userAdmin;
        this.multicastService=multicastService;
        this.rankingAdmin=rankingAdmin;
    }




    @Override
    public void run() {
        System.out.printf("Comando %s arrivato al ThreadPool \n", cmd.codice);

        //Se il comando non esiste viene inviata questa risposta predefinita
        Risposta risposta = new Risposta(CodiciRisposta.ERR_AZIONE_NEGATA,"Comando non implementato");
        if(cmd.codice == Comandi.CMD_PLAYWORDLE){

        }

        //invio della risposta
        final int bufSize = 2048;
        ByteBuffer buffer = ByteBuffer.allocate(bufSize);
        var replyStr = new Gson().toJson(risposta);
        byte[] replyBytes = replyStr.getBytes();
        buffer.clear();
        buffer.putInt(replyBytes.length);
        buffer.put(replyBytes);
        buffer.flip();
        try {
            client.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
