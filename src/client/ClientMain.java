package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import condivisi.CodiciRisposta;
import condivisi.Comandi;
import condivisi.Risposta;
import condivisi.interfacce.IRegisterService;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;


//rappresenta il Main del Client
public class ClientMain {
    public static ClientConfig config;
    private static Map<String,Integer> codiciMap = new HashMap<>() ;
    private static ClassificaLocale classificaLocale;

    public static void main(String[] args) throws IOException {
        //Preparo l'HashMap dei comandi
        preparazioneCodiciMap();

        System.out.println("Avvio del Client in corso");
        // caricamento config
        config = new ClientConfig();
        config.LoadConfig("client.cfg");

        classificaLocale = new ClassificaLocale(config.classificaSvcHost, config.classificaSvcName, config.classificaSvcPort);
        try {
            classificaLocale.inizializza();
        }
        catch (RemoteException e) {
            System.out.println(("Il Server non è attivo, terminazione del Client in corso"));
            System.exit(0);
        }
        catch (NotBoundException e){
            System.out.println("NotBoundException, terminazione del client in corso");
            System.exit(0);
        }

        //thread per unirsi e ricevere i risultati del gioco dal gruppo multicast
        Thread multicastClientThread = new MulticastClientThread(config.multicastAddress, config.multicastPort);
        multicastClientThread.start();

        // apertura del canale socket TCP per inviare i comandi al server
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress(config.host, config.port));
        } catch (java.net.ConnectException e) {
            System.out.println("Server non attivo. Il Client viene terminato.");
            System.exit(0);
        }


        System.out.println("In attesa di comandi");

        Scanner scanner = new Scanner(System.in);
        // ciclo per lettura in input dei comandi e invio al server
        while (true) {
            System.out.print("> ");
            String inputStr = scanner.nextLine();

            //il comando viene analizzato
            var comando = parseComandi(inputStr);
            if (comando != null){

                //Se il comando è REGISTER -> effettua la registrazione dell'utente
                if(comando.codice == Comandi.CMD_REGISTER){
                    if(comando.parametri.size() != 2){
                        System.out.println("\t" + "Errore: Numero di parametri non soddisfatto, inserire username e password");
                    }
                    else{
                        var username = comando.parametri.get(0);
                        var password = comando.parametri.get(1);
                        registrazioneUtente(username, password);
                    }
                }
                //comando ShowMeRanking -> Mostra a schermo la Classifica salvata in locale(non chiama il server)
                else if (comando.codice == Comandi.CMD_SHOWMERANKING){
                    if(comando.parametri.size() != 0 ){
                        System.out.println("\t" + "Errore: Il comando non richiede parametri aggiuntivi");
                    }
                    else {
                        var classifica = classificaLocale.StampaClassifica();
                        if(classifica.isEmpty()) System.out.println("\t La classifica risulta essere vuota ");
                        else System.out.printf("\t Classifica:" + "\n" + "\t" + classifica + "\n");
                    }
                }
                else{
                    Risposta risposta = invioComando(socketChannel, comando);
                    //succede quando viene ricevuta come risposta un codice di errore
                    if(risposta.esito != CodiciRisposta.SUCCESS && risposta.esito != CodiciRisposta.PLAY && risposta.esito != CodiciRisposta.HAI_VINTO
                            && risposta.esito != CodiciRisposta.ERR_HAI_PERSO && risposta.esito != CodiciRisposta.STATISTICHE)
                        System.out.println("\t" + risposta.MessaggioDiRisposta());
                    else
                        gestioneRisposta(comando, risposta, socketChannel);
                }
            }
        }
    }

    private static void preparazioneCodiciMap(){
        codiciMap.put("register", Comandi.CMD_REGISTER);
        codiciMap.put("login", Comandi.CMD_LOGIN);
        codiciMap.put("logout", Comandi.CMD_LOGOUT);
        codiciMap.put("showmeranking", Comandi.CMD_SHOWMERANKING);
        codiciMap.put("playwordle", Comandi.CMD_PLAYWORDLE);
        codiciMap.put("sendword", Comandi.CMD_SENDWORD);
        codiciMap.put("sendmestatistic", Comandi.CMD_SENDMESTATISTIC);
        codiciMap.put("share", Comandi.CMD_SHARE);
        codiciMap.put("showmesharing", Comandi.CMD_SHOWMESHARING);
    }

    //La funzione parseCommand serve a riconoscere il comando inserito a console
    //le parole contenute nell'inputStr vengono inserite in una ArrayList
    //la prima parola viene usata per riconoscere il codice del comando, le restanti parole corrispondono ai parametri username-password
    private static Comandi parseComandi(String inputStr) {
        List<String> matchList = new ArrayList<>();
        String[] parts = inputStr.split(" ");
        int len = 0;
        if (parts.length > 3 ){
            System.out.println("\t" + "Errore: Hai inserito troppi parametri  ");
            return null;
        }
        while (parts.length>len) {
            matchList.add(parts[len]);
            len++;
        }

        //se la lista è = 0 ignoro il comando
        if (matchList.size() == 0)
            return null;

        Comandi cmd = null;
        Integer codice = stringToCode(matchList.get(0));  // controllo se il comando è associato a un codice
        if (codice == null) {
            System.out.println("\t" + "Errore: Comando sconosciuto.");
            return cmd;
        }

        cmd =  new Comandi(codice.intValue(), matchList.stream().skip(1).toList());
        return cmd;
    }



    // presa la parola data in input restituisce il codice del comando associato utilizzando l'HashMap
    private static Integer stringToCode(String s) {
        s = s.trim();
        s = s.toLowerCase();
        Integer codice = codiciMap.get(s);
        return codice;
    }


    //invio i comandi al Server e ricevo in risposta l'esito.
    private static Risposta invioComando(SocketChannel socketChannel, Comandi comando) throws IOException{
        final int bufSize = 1024 * 8;
        var json = new Gson().toJson(comando);
        byte[] message = json.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bufSize);
        buffer.clear();
        buffer.putInt(message.length);
        buffer.put(message);
        buffer.flip();
        try {
            socketChannel.write(buffer);
        }
        catch (Exception e){
            System.out.println("\t Il Server risulta offline");
            System.exit(1);
        }

        buffer.clear();
        try {
            socketChannel.read(buffer);
        }
        catch (Exception e){
            System.out.println("\t Il Server risulta offline");
            System.exit(1);
        }
        buffer.flip();
        int replyLength = buffer.getInt();
        byte[] replyBytes = new byte[replyLength];
        buffer.get(replyBytes);

        var risposta = new Gson().fromJson(new String(replyBytes), Risposta.class);
        return risposta;
    }

    //Dopo aver ricevuto l'esito dal Server, procedo a implementare i vari comandi
    private static void gestioneRisposta(Comandi comando, Risposta risposta, SocketChannel socketChannel) throws IOException {
        int codice = comando.codice;

        //login
        if(codice == 1){
            classificaLocale.registrazioneCallback(comando.parametri.get(0));
            System.out.println("\t" + risposta.MessaggioDiRisposta());
        }

        //logout
        if(codice == 2){
            classificaLocale.deregistrazioneCallback();
            System.out.println("\t" + risposta.MessaggioDiRisposta());
        }

        //playWORDLE
        if(codice == 4){
            System.out.println("\t" + risposta.MessaggioDiRisposta());
        }

        //sendWord
        if(codice == 5){
            System.out.println("\t" + risposta.MessaggioDiRisposta());
            final int bufSize = 1024*8;
            ByteBuffer buffer = ByteBuffer.allocate(bufSize);
            buffer.clear();
            try {
                socketChannel.read(buffer);  // lettura del ste di tentativi
            }
            catch(SocketException e){
                e.printStackTrace();
                return;
            }
            buffer.flip();
            if(!buffer.hasRemaining()) {
                socketChannel.close();
                return;
            }
            byte[] receivedBytes = new byte[buffer.remaining()];
            buffer.get(receivedBytes);
            String messaggio = new String(receivedBytes);
            List<String> set = new LinkedList<>(Arrays.asList(messaggio.split(",")));
            for(String string : set){
                string = string.replace("[", "").replace("]", "").trim();
                System.out.println("\t" + string);
            }
            buffer.clear();
            socketChannel.configureBlocking(false);

            // Imposta un timeout di 1.5 secondi in millisecondi
            long timeout = 1500;
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < timeout) {
                int bytesRead = socketChannel.read(buffer);
                if (bytesRead == -1) {
                    // Il server ha chiuso la connessione
                    socketChannel.close();
                    return;
                } else if (bytesRead > 0) {
                    buffer.flip();
                    byte[] receivedBytes2 = new byte[buffer.remaining()];
                    buffer.get(receivedBytes2);
                    String traduzione = new String(receivedBytes2);
                    if(traduzione.contains("Errore")){
                        System.out.println("\t"+traduzione);
                        break;
                    }
                    else {
                        System.out.println("\tLa traduzione in Italiano della parola segreta è: " + traduzione);
                        break;
                    }
                }
                buffer.clear();
            }
            socketChannel.configureBlocking(true);
        }

        //sendMeStatistic
        if(codice == 6){
            System.out.println("\t" + risposta.MessaggioDiRisposta());
            final int bufSize = 1024*8;
            ByteBuffer buffer = ByteBuffer.allocate(bufSize);
            buffer.clear();
            try {
                socketChannel.read(buffer); //leggo le statistiche
            }
            catch(SocketException e){
                e.printStackTrace();
                return;
            }
            buffer.flip();
            if(!buffer.hasRemaining()) {
                socketChannel.close();
                return;
            }
            byte[] receivedBytes = new byte[buffer.remaining()];
            buffer.get(receivedBytes);
            String json = new String(receivedBytes);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
            HashMap<String,Object> statistiche = gson.fromJson(json, type); //deserializzo da json al tipo corretto
            stampaStatistiche(statistiche);

        }
        //share
        if(codice == 7){
            System.out.println("\t" + risposta.MessaggioDiRisposta());
        }
        //showMeSharing
        if(codice == 8){
            System.out.println("\t" + risposta.MessaggioDiRisposta());
            final int bufSize = 1024*8;
            ByteBuffer buffer = ByteBuffer.allocate(bufSize);
            buffer.clear();
            try {
                socketChannel.read(buffer);  // lettura del ste di tentativi inviati dal server in multicast
            }
            catch(SocketException e){
                e.printStackTrace();
                return;
            }
            buffer.flip();
            if(!buffer.hasRemaining()) {
                socketChannel.close();
                return;
            }
            byte[] receivedBytes = new byte[buffer.remaining()];
            buffer.get(receivedBytes);
            String messaggio = new String(receivedBytes);
            List<String> set = new ArrayList<>(Arrays.asList(messaggio.split(",")));
            for(String string : set){
                string = string.replace("[", "").replace("]", "").trim();
                System.out.println("\t" + string);
            }
            buffer.clear();
        }
    }

    //stampa le statistiche dell'utente aggiornate alla partita più recente.
    private static void stampaStatistiche(HashMap<String, Object> statistiche){
        int partiteGiocate = ((Double) statistiche.get("partite giocate")).intValue();
        float percentualeVittoria = ((Double) statistiche.get("percentuale vittoria")).floatValue();
        int ultimaWinStreak = ((Double) statistiche.get("ultima win streak")).intValue();
        int massimaWinStreak = ((Double) statistiche.get("massima win streak")).intValue();
        ArrayList<Double> guessDistributionList = (ArrayList<Double>) statistiche.get("guess distribution");
        int[] guessDistribution = new int[guessDistributionList.size()];
        for (int i = 0; i < guessDistributionList.size(); i++) {
            guessDistribution[i] = guessDistributionList.get(i).intValue();
        }

        System.out.println("\tPartite giocate: " + partiteGiocate);
        System.out.println("\tPercentuale vittoria: " + percentualeVittoria);
        System.out.println("\tUltima Win Streak: " + ultimaWinStreak);
        System.out.println("\tMassima Win Streak: " + massimaWinStreak);
        System.out.println("\tGuess Distribution: " + Arrays.toString(guessDistribution));
    }


    //Qui avviene la registrazione dell'utente attraverso la RMI
    public static void registrazioneUtente(String username, String password){
        IRegisterService serverObject;
        Remote RemoteObject;
        try{
            Registry registry = LocateRegistry.getRegistry(config.registrazioneHost, config.registrazionePort);
            RemoteObject = registry.lookup(config.registrazioneServiceName);
            serverObject = (IRegisterService) RemoteObject;

            var risultato = serverObject.register(username, password);
            if(risultato == CodiciRisposta.ERR_USERNAME_NON_VALIDO)
                System.out.println("\tL'Username inserito non è valido ");
            else if(risultato == CodiciRisposta.ERR_USERNAME_GIÀ_PRESO)
                System.out.println("\tL'Username inserito è già stato preso");
            else if(risultato == CodiciRisposta.ERR_PASSWORD_TROPPO_CORTA)
                System.out.println("\tPassword inserita è troppo corta");
            else if(risultato == CodiciRisposta.SUCCESS)
                System.out.println("\tRegistrazione effettuata");
            else
                System.out.println("\tErrore sconosciuto");
        }
        catch (Exception e){
            System.out.println("Si è verificato un errore " + e.toString() + e.getMessage());
            e.printStackTrace();
        }
    }

}
