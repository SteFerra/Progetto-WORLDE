package server;

import com.google.gson.Gson;
import condivisi.CodiciRisposta;
import condivisi.Comandi;
import condivisi.Risposta;
import condivisi.interfacce.INotifyRanking;
import condivisi.interfacce.INotifyRankingUpdate;
import condivisi.interfacce.IRegisterService;
import server.admin.RankingAdmin;
import server.admin.UserAdmin;
import server.domini.UserSession;
import server.domini.utentiConnessi;
import server.servizi.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// rappresenta il server
public class WordleServer {

    private final WordleServerConfig config;

    private  UserAdmin userAdmin;
    private final RankingAdmin rankingAdmin;

    private INotifyRanking rankingNotifyService;
    public RankingServiceImpl rankingService;
    private final MulticastService multicastService;

    private utentiConnessi utenticonnessi;

    private static List<String> paroleSegrete;


    public WordleServer() throws IOException {
        config = new WordleServerConfig();
        config.LoadConfig("serverConf.cfg");

        userAdmin = new UserAdmin();
        userAdmin.initialize();

        //creazione della classifica
        rankingAdmin = new RankingAdmin();
        rankingAdmin.inizializza();

        utenticonnessi = new utentiConnessi();

        paroleSegrete=new ArrayList<>();

        multicastService = new MulticastService(config.multicastAddress, config.multicastPort);


    }

    public void execute() throws Exception{

        // avvio del servizio per la registrazione dell'utente basato su RMI
        startRegisterService();

        // avvio del servizio per l'aggiornamento classifica che utilizza RMI Callback
        rankingNotifyService = RankingServiceImpl.startNotifyService(config.classificaSvcName, config.classificaSvcPort, rankingAdmin);
        rankingService = (RankingServiceImpl) rankingNotifyService;

        multicastService.start();

        //Legge il file words.txt e salva tutte le parole in una Lista
        leggiFileParole();

        //ThreadPool per la lettura periodica della parola segreta
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(WordleServer::estraiParola,3,10, TimeUnit.SECONDS);


        //Thread che controlla e rimuove periodicamente i Socket di utenti che risultano ancora loggati
        Thread logoutAutomaticoThread = new Thread(new logoutAutomatico(utenticonnessi.utentiLoggati, config.logoutTimer));
        logoutAutomaticoThread.start();

        //Thread ShutdownHook che salva i vari parametri prima della chiusura del Server
        ServerShutdownHook shutdownHook = new ServerShutdownHook(userAdmin, rankingAdmin, utenticonnessi);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        System.out.println("ServerShutdownHook avviato");

        startReceiverCommand(); //Ricezione Comandi
    }

    private void startReceiverCommand() throws Exception{

        //apertura socket
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(config.host, config.port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server pronto sulla porta: " + config.port);

        while(true){
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                // Controllo se sul canale associato alla chiave
                // c'è la possibilità di accettare una nuova connessione.
                if (key.isAcceptable()) {
                    // Accetto la connessione e registro il canale ottenuto
                    // sul selettore.
                    SocketChannel client = serverSocket.accept();
                    System.out.println("Nuova connessione ricevuta");
                    client.configureBlocking(false);
                    var key2 = client.register(selector, SelectionKey.OP_READ);
                    key2.attach(new UserSession());// associo una sessione con utente anonimo

                }
                // Se il canale associato alla chiave è leggibile,
                // allora procedo con l'invio del messaggio di risposta.
                if (key.isReadable())
                    manageCommand(key);  // gestione del comando ricevuto
                iter.remove();
            }
        }
    }

    private void manageCommand(SelectionKey key) throws Exception {
        final int bufSize = 1024*8;
        ByteBuffer buffer = ByteBuffer.allocate(bufSize);
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();
        try {
            client.read(buffer);  // lettura del comando
        }
        catch(SocketException e){// accade quando il client termina in modo anomalo
            aggiornaCallback(client);
            client.close();
            return;
        }
        buffer.flip();
        if(!buffer.hasRemaining()) {
            client.close();
            return;
        }
        int receivedLength = buffer.getInt();
        byte[] receivedBytes = new byte[receivedLength];
        buffer.get(receivedBytes);
        String receivedStr = new String(receivedBytes);

        Comandi cmd = new Gson().fromJson(receivedStr,Comandi.class);  // il comando viene creato dal json ricevuto

        Risposta risposta = null;
        var userSession = (UserSession)key.attachment();   // acquisizione della sessione

        //comando LOGIN
        if (cmd.codice == Comandi.CMD_LOGIN) {
            if (userSession.username != null)
                risposta = new Risposta(CodiciRisposta.ERR_AZIONE_NEGATA_LOGIN, "login già effettuato");
            else if (cmd.parametri.size() != 2)
                risposta = new Risposta(CodiciRisposta.ERR_NUMERO_PARAMETRI_ERRATI, "numero parametri non corretto.");
            else {
                if(!utenticonnessi.èConnesso(cmd.parametri.get(0))) {
                    int res = userAdmin.login(cmd.parametri.get(0), cmd.parametri.get(1));
                    if (res == 0) { //risposta = SUCCESS
                        userSession.username = cmd.parametri.get(0);
                        utenticonnessi.utentiLoggati.put(cmd.parametri.get(0), client);
                    }
                    risposta = new Risposta(res, "risultato login");
                }
                else{
                    risposta = new Risposta(CodiciRisposta.ERR_UTENTE_GIÀ_LOGGATO,"utente già loggato");
                }
            }
        }
        //comando LOGOUT
        else if(cmd.codice == Comandi.CMD_LOGOUT){
            if(userSession.username == null)
                risposta = new Risposta(CodiciRisposta.ERR_AZIONE_NEGATA, "devi prima fare il login");
            else if(cmd.parametri.size() != 1)
                risposta = new Risposta(CodiciRisposta.ERR_NUMERO_PARAMETRI_ERRATI, "numero dei parametri errato, inserisci solo l'username");
            else if(!userSession.username.equals(cmd.parametri.get(0)))
                risposta = new Risposta(CodiciRisposta.ERR_USERNAME_NON_VALIDO_LOGOUT, "lo username non corrisponde a quello inserito durante la login ");
            else{
                userSession.clear();
                int res = userAdmin.logout(cmd.parametri.get(0));
                risposta = new Risposta(res, "risultato logout");
                if(res == 0){//logout andato a buon fine e rimuovo lo username dalla lista degli utenti loggati
                    utenticonnessi.utentiLoggati.remove(cmd.parametri.get(0));
                }

            }
        }
        // invio la risposta al client
        if (risposta != null) {
            var replyStr =  new Gson().toJson(risposta);  // response inviato in json
            byte[] replyBytes = replyStr.getBytes();
            buffer.clear();
            buffer.putInt(replyBytes.length);
            buffer.put(replyBytes);
            buffer.flip();
            client.write(buffer);
        }
    }

    //legge il file contenente le parole e le salva in una Lista<String>
    private void leggiFileParole(){
        try (BufferedReader reader = new BufferedReader(new FileReader("words.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 10) {
                    paroleSegrete.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Estrae una parola casuale dalla lista delle parole segrete.
    private static void estraiParola(){
        if(paroleSegrete.isEmpty()){
            System.out.println("Il file contenente le parole segrete risulta vuoto!");
            return;
        }

        Random random = new Random();
        int indice = random.nextInt(paroleSegrete.size());
        String parolaSegreta = paroleSegrete.get(indice);
        System.out.printf("La nuova parola segreta è: %s \n", parolaSegreta);
    }

    public void aggiornaCallback(SocketChannel clientSocket) throws RemoteException {
        String username = utenticonnessi.getUsername(clientSocket);
        INotifyRankingUpdate stub = rankingNotifyService.getStub(username);
        if(stub!=null){
            rankingService.cancRegistrazioneCallback(username, stub);
        }
    }


    // registrazione del RMI per il comando "REGISTER"
    private void startRegisterService() throws RemoteException {
        IRegisterService rService = new RegisterService(userAdmin);
        var stub = (IRegisterService) UnicastRemoteObject.exportObject(rService,config.registrazionePort);
        LocateRegistry.createRegistry(config.registrazionePort);
        Registry r = LocateRegistry.getRegistry(config.registrazionePort);
        r.rebind(config.registrazioneServiceName,stub);
        System.out.println("Servizio di registrazione pronto (porta = "+config.registrazionePort+")");
    }
}
