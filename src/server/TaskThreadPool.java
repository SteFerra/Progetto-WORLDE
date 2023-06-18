package server;


import com.google.gson.Gson;
import condivisi.CodiciRisposta;
import condivisi.Comandi;
import condivisi.Risposta;
import server.admin.RankingAdmin;
import server.admin.UserAdmin;
import server.domini.User;
import server.domini.UserSession;
import server.servizi.MulticastService;
import server.servizi.RankingServiceImpl;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

//classe che esegue i vari comandi passati al Threadpool
public class TaskThreadPool implements Runnable{
    private final UserSession userSession;
    private final SocketChannel client;
    private final Comandi cmd;
    private final RankingServiceImpl rankingService;
    private final UserAdmin userAdmin;
    private final MulticastService multicastService;
    private RankingAdmin rankingAdmin;
    private Gioco gioco;
    private String parola;
    private Risposta risposta;
    private List<String> paroleSegrete;
    private static final Map<SocketChannel, Gioco> map = new ConcurrentHashMap<>();

    public TaskThreadPool(UserSession userSession, SocketChannel client, Comandi cmd, RankingServiceImpl rankingService, UserAdmin userAdmin, MulticastService multicastService, RankingAdmin rankingAdmin, List<String> paroleSegrete){
        this.userSession=userSession;
        this.client=client;
        this.cmd=cmd;
        this.rankingService=rankingService;
        this.userAdmin=userAdmin;
        this.multicastService=multicastService;
        this.rankingAdmin=rankingAdmin;
        this.paroleSegrete = paroleSegrete;
        //this.gioco = gioco;
    }




    @Override
    public void run() {
        //Se il comando non esiste viene inviata questa risposta predefinita
        risposta = new Risposta(CodiciRisposta.ERR_COMANDO_NON_IMPLEMENTATO,"Comando non implementato");

        //comando per far iniziare la partita, salva la parola segreta selezionata randomicamente
        //e controlla che l'utente non abbia già giocato per quella parola
        if(cmd.codice == Comandi.CMD_PLAYWORDLE){
            String username = userSession.getUsername();
            //Se l'utente ha già giocato per quella parola impedisco di farlo giocare ancora
            if(userAdmin.haGiocato(username) == true) {
                System.out.printf("Il giocatore [%s] ha già giocato \n", username);
                risposta = new Risposta(CodiciRisposta.ERR_PARTITA_GIÀ_GIOCATA, "Hai già giocato");
                inviaRisposta(risposta);
                return;
            }
            Gioco gioco = new Gioco(userAdmin,rankingService,userSession,client);
            map.put(client, gioco);
            for (Map.Entry<SocketChannel, Gioco> entry : map.entrySet()) {
                System.out.println(entry.getKey() + " => " + entry.getValue());
            }
            String parolaSegreta = WordleServer.parolaSegreta;
            gioco.iniziaPartita(parolaSegreta);
            userAdmin.setStaGiocando(username,true);
            System.out.printf("L'utente [%s] sta giocando con la parola: %s \n" ,username, parolaSegreta);
            risposta = new Risposta(CodiciRisposta.PLAY, "Puoi iniziare a giocare a Wordle");
            inviaRisposta(risposta);

        }

        if(cmd.codice == Comandi.CMD_SENDWORD){
            if(cmd.parametri.size() != 1){
                risposta = new Risposta(CodiciRisposta.ERR_NUMERO_PARAMETRI_ERRATI,"Numero parametri errati");
                inviaRisposta(risposta);
                return;
            }
            //Se non sta giocando invio un errore
            else if(userAdmin.staGiocando(userSession.getUsername()) == false){
                risposta = new Risposta(CodiciRisposta.ERR_DEVI_PRIMA_INIZIARE_A_GIOCARE,"Devi prima iniziare a giocare");
                inviaRisposta(risposta);
                return;
            }
            //se ha già giocato invio un errore
            else if(userAdmin.haGiocato(userSession.getUsername()) == true){
                risposta = new Risposta(CodiciRisposta.ERR_PARTITA_GIÀ_GIOCATA,"Hai già giocato");
                inviaRisposta(risposta);
                return;
            }

            String parolaIndovinata = cmd.parametri.get(0);
            //se la parola inviata non è lunga 10 caratteri
            if(parolaIndovinata.length() != 10){
                risposta = new Risposta(CodiciRisposta.ERR_PAROLA_TROPPO_CORTA,"parola troppo corta");
                inviaRisposta(risposta);
                return;
            }
            //se la parola inviata non è presente nel dizionario
            if(!paroleSegrete.contains(parolaIndovinata)){
                risposta = new Risposta(CodiciRisposta.ERR_PAROLA_NON_PRESENTE, "La parola che hai inserito non è presente nel dizionario");
                inviaRisposta(risposta);
                return;
            }
            System.out.println("funziona");
            Gioco gioco = map.get(client);
            System.out.println("gioco: " + gioco);
            if (gioco != null) {
                Set<String> risultato = null;
                try {
                    risultato = gioco.indovinaParola(parolaIndovinata);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(risultato != null) {
                risposta = new Risposta(CodiciRisposta.SUCCESS, "SUCCESS");
                inviaRisposta(risposta);
                System.out.println("Risposta correttamente inviata");
                inviaRisultato(risultato.toString());
                System.out.println("Inviato!");
            }
            }
        }

    }

    private void inviaRisultato(String risultato){
        ByteBuffer buffer = ByteBuffer.wrap(risultato.getBytes());
        while(buffer.hasRemaining()){
            try {
                client.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void inviaRisposta(Risposta risposta){
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
