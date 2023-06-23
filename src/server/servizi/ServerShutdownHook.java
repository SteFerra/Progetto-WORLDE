package server.servizi;

import server.admin.RankingAdmin;
import server.admin.UserAdmin;
import server.domini.utentiConnessi;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//Classe che implementa lo ShutdownHook lato Server per garantire la corretta persistenza in caso di chiusura accidentale del Server
public class ServerShutdownHook extends Thread{

    private final UserAdmin userAdmin;
    private final RankingAdmin rankingAdmin;
    private final utentiConnessi utentiConnessi;
    private final ThreadPoolExecutor threadPool;

    public ServerShutdownHook(UserAdmin userAdmin, RankingAdmin rankingAdmin, utentiConnessi utentiConnessi, ThreadPoolExecutor threadPool) {
        this.userAdmin = userAdmin;
        this.rankingAdmin = rankingAdmin;
        this.utentiConnessi = utentiConnessi;
        this.threadPool = threadPool;
    }

    @Override
    public void run(){
        rankingAdmin.salvaClassifica();

        threadPool.shutdown();
        try{
            if(!threadPool.awaitTermination(3, TimeUnit.SECONDS));{
                threadPool.shutdownNow();
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        try {
            userAdmin.saveUserListHook();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            utentiConnessi.chiudiSocket();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
