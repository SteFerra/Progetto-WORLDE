package server;

import com.google.gson.Gson;
import condivisi.CodiciRisposta;
import condivisi.Comandi;
import condivisi.Risposta;
import condivisi.interfacce.INotifyRanking;
import condivisi.interfacce.IRegisterService;
import server.admin.RankingAdmin;
import server.admin.UserAdmin;
import server.domini.UserSession;
import server.servizi.RankingServiceImpl;
import server.servizi.RegisterService;

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
import java.util.Iterator;
import java.util.Set;

// rappresenta il server
public class WordleServer {

    private final WordleServerConfig config;

    private final UserAdmin userAdmin;
    private final RankingAdmin rankingAdmin;

    private INotifyRanking rankingNotifyService;

    public WordleServer() throws IOException {
        config = new WordleServerConfig();
        config.LoadConfig("serverConf.cfg");

        userAdmin = new UserAdmin();
        userAdmin.initialize();

        //creazione della classifica
        rankingAdmin = new RankingAdmin();
        rankingAdmin.inizializza();
    }

    public void execute() throws Exception{

        // avvio del servizio per la registrazione dell'utente basato su RMI
        startRegisterService();

        // avvio del servizio per l'aggiornamento classifica che utilizza RMI Callback
        rankingNotifyService = RankingServiceImpl.startNotifyService(config.classificaSvcName, config.classificaSvcPort, rankingAdmin);

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
                    key2.attach(new UserSession());  // associo una sessione con utente anonimo
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
        catch(SocketException e){  // accade quando il client termina in modo anomalo
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

        // gestione della LOGIN
        if (cmd.codice == Comandi.CMD_LOGIN) {
            if (userSession.username != null)
                risposta = new Risposta(CodiciRisposta.ERR_AZIONE_NEGATA, "login già effettuato");
            else if (cmd.parametri.size() != 1)
                risposta = new Risposta(CodiciRisposta.ERR_NUMERO_PARAMETRI_ERRATI, "numero parametri non corretto.");
            else {
                int res = userAdmin.login(cmd.parametri.get(0), cmd.parametri.get(1));
                if (res == 0) { //risposta = SUCCESS
                    userSession.username = cmd.parametri.get(0);   // note: la user session non è più anonima, ma riferita al primo parametro (username)
                }
                risposta = new Risposta(res, "login result.");
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
