package server.servizi;

import server.admin.RankingAdmin;
import server.admin.UserAdmin;
import server.domini.utentiConnessi;

import java.io.IOException;
import java.nio.channels.SocketChannel;

//Classe che implementa lo ShutdownHook lato Server
public class ServerShutdownHook extends Thread{

    private final UserAdmin userAdmin;
    private final RankingAdmin rankingAdmin;
    private final utentiConnessi utentiConnessi;

    public ServerShutdownHook(UserAdmin userAdmin, RankingAdmin rankingAdmin,utentiConnessi utentiConnessi) {
        this.userAdmin = userAdmin;
        this.rankingAdmin = rankingAdmin;
        this.utentiConnessi = utentiConnessi;
    }

    @Override
    public void run(){
        rankingAdmin.salvaClassifica();

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
