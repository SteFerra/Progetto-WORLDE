package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import condivisi.CodiciRisposta;
import condivisi.Risposta;
import server.admin.UserAdmin;
import server.domini.User;
import server.domini.UserSession;
import server.servizi.MulticastService;
import server.servizi.RankingServiceImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

//Classe che rappresenta la partita
public class Gioco {
    private UserAdmin userAdmin;
    private RankingServiceImpl rankingService;
    private UserSession userSession;
    private SocketChannel client;
    private MulticastService multicastService;

    public final int lunghezzaParola = 10;
    public final int maxTentativi = 12;
    public int tentativi = 0;

    public Integer IDpartita;
    public String parolaIndovinata;
    public String parolaSegreta;
    public Set<String> setTentativi;
    public Set<String> setTentativiFinale;
    public int esitoPartita;



    public Gioco(UserAdmin userAdmin, RankingServiceImpl rankingService, UserSession userSession, SocketChannel client, MulticastService multicastService){
        this.userAdmin = userAdmin;
        this.rankingService = rankingService;
        this.userSession = userSession;
        this.client = client;
        this.multicastService = multicastService;
        setTentativi = new LinkedHashSet<>();
    }

    public void iniziaPartita(String parolaSegreta, Integer IDpartita){
        this.parolaSegreta = parolaSegreta;
        this.IDpartita = IDpartita;
        this.esitoPartita = 0;
        this.tentativi = 0;
    }
    public Set<String> indovinaParola(String parolaIndovinata) throws IOException {
        if(tentativi < maxTentativi) {
            StringBuilder risultato = new StringBuilder();
            for (int i = 0; i < lunghezzaParola; i++) {
                char cIndovina = parolaIndovinata.charAt(i);
                char cSegreta = parolaSegreta.charAt(i);

                if (cIndovina == cSegreta) {
                    risultato.append('+');  //il carattere si trova nella giusta posizione
                } else if (parolaSegreta.indexOf(cIndovina) != -1 && cIndovina!=cSegreta) {
                    risultato.append('?');  //il carattere è giusto ma non in quella posizione
                } else if (parolaSegreta.indexOf(cIndovina) != -1 && cIndovina==cSegreta){
                    risultato.append('+');   //questo succede per le doppie
                }else{
                    risultato.append('X');  //il carattere non è presenta nella parola segreta
                }
            }
            tentativi++;
            String esito = risultato.toString();
            System.out.println(esito);

            if(tentativi == maxTentativi){
                if(esito.equals("++++++++++")){ //se ha vinto all'ultimo tentativo
                    System.out.printf("L'Utente [%s] ha indovinato la parola: %s \n", userSession.username, parolaSegreta);
                    esitoPartita = 1;
                    setTentativi.add(esito);
                    setTentativiFinale = new LinkedHashSet<>();
                    setTentativiFinale.addAll(setTentativi);
                    String stringa = String.format("La parola segreta è: %s. IdPartita: %s. Numero tentativi impiegati: %s  ", parolaSegreta, IDpartita,tentativi );
                    setTentativiFinale.add(stringa);
                    setTentativi.clear();

                    //Setto le due variabili haGiocato e staGiocando
                    userAdmin.setHaGiocato(userSession.username, true);
                    userAdmin.setStaGiocando(userSession.username, false);

                    //Invio la risposta con i tentativi e la traduzione
                    Risposta risposta = new Risposta(CodiciRisposta.HAI_VINTO,"Hai vinto");
                    inviaRisposta(risposta);
                    inviaRisultato(setTentativiFinale.toString());
                    inviaTraduzione();

                    //Aggiorno le statistiche dell'utente e la classifica
                    userAdmin.aggiornaPartiteVinte(userSession.username, tentativi);
                    userAdmin.aggiornaPunteggio(userSession.username);
                    return null;

                }else{ //ha perso (non è riuscito a indovinare la parola all'ultimo tentativo)
                    System.out.printf("L'Utente [%s] ha perso \n", userSession.username);
                    esitoPartita = 2;
                    setTentativi.add(esito);
                    setTentativiFinale = new LinkedHashSet<>();
                    setTentativiFinale.addAll(setTentativi);
                    String stringa = String.format("La parola segreta è: %s. IdPartita: %s. Numero tentativi impiegati: %s  ", parolaSegreta, IDpartita,tentativi );
                    setTentativiFinale.add(stringa);
                    setTentativi.clear();

                    //Setto le due variabili haGiocato e staGiocando
                    userAdmin.setHaGiocato(userSession.username, true);
                    userAdmin.setStaGiocando(userSession.username, false);

                    //Invio la risposta con i tentativi e la traduzione
                    Risposta risposta = new Risposta(CodiciRisposta.ERR_HAI_PERSO,"Hai perso");
                    inviaRisposta(risposta);
                    inviaRisultato(setTentativiFinale.toString());
                    inviaTraduzione();

                    //Aggiorno le statistiche dell'utente e la classifica
                    userAdmin.aggiornaPartitePerse(userSession.username, tentativi);
                    userAdmin.aggiornaPunteggio(userSession.username);
                    return null;
                }
            }else if(esito.equals("++++++++++")){ //se ha vinto
                    System.out.printf("L'Utente [%s] ha indovinato la parola: %s \n", userSession.username, parolaSegreta);
                    esitoPartita = 1;
                    setTentativi.add(esito);
                    setTentativiFinale = new LinkedHashSet<>();
                    setTentativiFinale.addAll(setTentativi);
                    String stringa = String.format("La parola segreta è: %s. IdPartita: %s. Numero tentativi impiegati: %s  ", parolaSegreta, IDpartita,tentativi );
                    setTentativiFinale.add(stringa);
                    setTentativi.clear();

                    //Setto le due variabili haGiocato e staGiocando
                    userAdmin.setHaGiocato(userSession.username, true);
                    userAdmin.setStaGiocando(userSession.username, false);

                    //Invio la risposta con i tentativi e la traduzione
                    Risposta risposta = new Risposta(CodiciRisposta.HAI_VINTO,"Hai vinto");
                    inviaRisposta(risposta);
                    inviaRisultato(setTentativiFinale.toString());
                    inviaTraduzione();

                    //Aggiorno le statistiche dell'utente e la classifica
                    userAdmin.aggiornaPartiteVinte(userSession.username, tentativi);
                    userAdmin.aggiornaPunteggio(userSession.username);
                    //tentativi=0;
                    return null;
            }else { //se sbaglia parola ma ha ancora dei tentativi
                setTentativi.add(esito);
                System.out.println("Inserito correttamente");
                return setTentativi;
            }
        }
        return null;
    }

    private void inviaTraduzione(){
        try {
            String url = "https://mymemory.translated.net/api/get?q=" + URLEncoder.encode(parolaSegreta, "UTF-8") + "&langpair=en|it";
            //creo una connessione HTTP GET
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            //leggo la risposta dal sito
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Analizza la risposta JSON per ottenere la traduzione
            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
            String traduzione = jsonObject.get("responseData").getAsJsonObject().get("translatedText").getAsString();
            connection.disconnect();
            System.out.println("Traduzione italiana: " + traduzione);
            inviaRisultato(traduzione);
        }catch (IOException e){
            System.out.println("Non è stato possibile contattare il server mymemory per la traduzione della parola segreta");
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
