package server;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


//classe che crea i task che verranno passati al Threadpool per l'esecuzione.
public class TaskThreadPool implements Runnable{
    private final UserSession userSession;
    private final SocketChannel client;
    private final Comandi cmd;
    private final RankingServiceImpl rankingService;
    private final UserAdmin userAdmin;
    private final MulticastService multicastService;
    private RankingAdmin rankingAdmin;
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
    }




    @Override
    public void run() {

        //comando per far iniziare la partita, salva la parola segreta selezionata randomicamente
        //e controlla che l'utente non abbia già giocato per quella parola
        if(cmd.codice == Comandi.CMD_PLAYWORDLE){
            String username = userSession.getUsername();

            //Numero parametri sbagliato
            if(cmd.parametri.size() != 0){
                risposta = new Risposta(CodiciRisposta.ERR_NUMERO_PARAMETRI_ERRATI,"Numero parametri errato");
                inviaRisposta(risposta);
                return;
            }

            //Se l'utente ha già giocato per quella parola impedisco di farlo giocare ancora
            if(userAdmin.haGiocato(username) == true) {
                System.out.printf("Il Giocatore [%s] ha già giocato \n", username);
                risposta = new Risposta(CodiciRisposta.ERR_PARTITA_GIÀ_GIOCATA, "Hai già giocato");
                inviaRisposta(risposta);
                return;
            }

            //Se l'utente sta gia giocando
            if(userAdmin.staGiocando(username) == true){
                System.out.printf("Il Giocatore [%s] sta già giocando \n", username);
                risposta = new Risposta(CodiciRisposta.ERR_DEVI_PRIMA_FINIRE_LA_PARTITA,"Stai gia giocando");
                inviaRisposta(risposta);
                return;
            }
            Gioco gioco = new Gioco(userAdmin,rankingService,userSession,client, multicastService);
            map.put(client, gioco); //Salvo all'interno della Map il socket dell'utente e la partita.
            String parolaSegreta = WordleServer.parolaSegreta;
            Integer IDpartita = WordleServer.IDpartita;
            gioco.iniziaPartita(parolaSegreta, IDpartita);  //inizio la partita salvando all'interno del Gioco la parola segreta- IDpartita estratte in quel momento.
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
            Gioco gioco = map.get(client);  //prendo dalla Map il gioco associato al client
            if (gioco != null) {
                List<String> risultato = null;
                try {
                    risultato = gioco.indovinaParola(parolaIndovinata); //ottengo la lista di stringhe che rappresentano i tentativi d'indovinare la parola segreta
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(risultato != null) {
                risposta = new Risposta(CodiciRisposta.SUCCESS, "SUCCESS");
                inviaRisposta(risposta);
                inviaRisultato(risultato.toString());
                }
            }
        }

        if(cmd.codice == Comandi.CMD_SENDMESTATISTIC){
            if(cmd.parametri.size() != 0){
                risposta = new Risposta(CodiciRisposta.ERR_NUMERO_PARAMETRI_ERRATI,"Numero parametri errati");
                inviaRisposta(risposta);
                return;
            }
            //se sta giocando invio un errore
            else if(userAdmin.staGiocando(userSession.getUsername()) == true){
                risposta = new Risposta(CodiciRisposta.ERR_DEVI_PRIMA_FINIRE_LA_PARTITA,"Devi prima finire la partita");
                inviaRisposta(risposta);
                return;
            }
            else{
                //Procedo a inviare tramite json le statistiche dell'utente
                HashMap<String, Object> statistiche = userAdmin.getStatistiche(userSession.username);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(statistiche);
                Risposta risposta = new Risposta(CodiciRisposta.STATISTICHE,"Statistiche");
                inviaRisposta(risposta);
                inviaRisultato(json);
            }
        }

        if(cmd.codice == Comandi.CMD_SHARE){
            //se sta già giocando
            if(userAdmin.staGiocando(userSession.getUsername()) == true){
                risposta = new Risposta(CodiciRisposta.ERR_DEVI_PRIMA_FINIRE_LA_PARTITA,"Devi prima finire la partita");
                inviaRisposta(risposta);
                return;
            }

            if(userAdmin.haGiocato(userSession.getUsername()) == false){
                risposta = new Risposta(CodiciRisposta.ERR_DEVI_PRIMA_INIZIARE_A_GIOCARE,"Devi prima iniziare e finire la partita");
                inviaRisposta(risposta);
                return;
            }

            if(cmd.parametri.size() != 0){
                risposta = new Risposta(CodiciRisposta.ERR_NUMERO_PARAMETRI_ERRATI,"Numero parametri errato");
                inviaRisposta(risposta);
                return;
            }

            //prendo l'esito con i parametri della partita
            Gioco gioco = map.get(client);
            if (gioco != null) {
                int IDPARTITA = gioco.IDpartita;
                int TENTATIVI = gioco.tentativi;
                String username = userSession.getUsername();
                String messaggio = "Utente " + username + " Wordle " + IDPARTITA + ": " + TENTATIVI + "/12";
                var set = gioco.setTentativiFinale;
                List<String> stringList = new ArrayList<>(set);
                int lastIndex = stringList.size() - 1; //rimuovo l'ultimo elemento dalla lista. (rappresenta la stringa con la parola segreta/id).
                stringList.remove(lastIndex);
                List<String> setAggiornato = new LinkedList<>();
                setAggiornato.add(messaggio);   //Inserisco il messaggio all'interno e successivamente i tentativi
                setAggiornato.addAll(stringList);
                risposta = new Risposta(CodiciRisposta.SUCCESS, "SUCCESS");
                inviaRisposta(risposta);
                multicastService.inviaMessaggioMulticast(setAggiornato);
            }
        }

        if(cmd.codice == Comandi.CMD_SHOWMESHARING){
            if(cmd.parametri.size() != 0){
                risposta = new Risposta(CodiciRisposta.ERR_NUMERO_PARAMETRI_ERRATI,"Numero parametri errato");
                inviaRisposta(risposta);
                return;
            }

            var arrayList = multicastService.arrayList;
            risposta = new Risposta(CodiciRisposta.SUCCESS,"SUCCESS");
            inviaRisposta(risposta);
            inviaRisultato(arrayList.toString());
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
