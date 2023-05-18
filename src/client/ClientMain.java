package client;

import condivisi.CodiciRisposta;
import condivisi.Comandi;
import condivisi.interfacce.IRegisterService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        // ciclo per input comandi ed invio
        while (true) {
            System.out.print("> ");
            String inputStr = scanner.nextLine();

            // comando EXIT
            if (inputStr.equalsIgnoreCase("exit")) {
                //Aggiungere la chiusura della callback dell'aggiornamento classifica --->
                socketChannel.close();
                break;
            }


            // altrimenti il comando viene analizzato
            var comando = parseComandi(inputStr);
            if (comando != null){

                //Se il comando è REGISTER -> effettua la registrazione dell'utente
                if(comando.codice == Comandi.CMD_REGISTER){
                    if(comando.parametri.size() < 2){
                        System.out.println("< Numero di parametri non soddisfatto, inserire username e password");
                    }
                    else{
                        var username = comando.parametri.get(0);
                        var password = comando.parametri.get(1);
                        registrazioneUtente(username, password);
                    }
                }
                //comando ShowMeRanking -> Mostra a schermo la Classifica salvata in locale
                else if (comando.codice == Comandi.CMD_SHOWMERANKING){
                    var classifica = classificaLocale.StampaClassifica();
                    System.out.printf("Classifica:" + "\n", classifica );
                }
            }
        }
    }

    private static void preparazioneCodiciMap(){
        codiciMap.put("register", Comandi.CMD_REGISTER);
        codiciMap.put("login", Comandi.CMD_LOGIN);
        codiciMap.put("logout", Comandi.CMD_LOGOUT);
        codiciMap.put("showmeranking", Comandi.CMD_SHOWMERANKING);
    }

    //La funzione parseCommand serve a riconoscere il comando inserito a console
    //Tramite l'utilizzo della regular expression le parole contenuto nell'inputStr vengono
    //inserite in una ArrayList, la prima parola viene usata per riconoscere il codice del comando
    private static Comandi parseComandi(String inputStr) {
        List<String> matchList = new ArrayList<String>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(inputStr);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                matchList.add(regexMatcher.group(2));
            } else {
                matchList.add(regexMatcher.group());
            }
        }
        //se la lista è = 0 ignoro il comando
        if (matchList.size() == 0)
            return null;

        Comandi cmd = null;
        Integer code = stringToCode(matchList.get(0));  // controllo se il comando è associato a un codice
        if (code == null) {
            System.out.println("< "+"Comando sconosciuto.");
            return cmd;
        }

        cmd =  new Comandi(code.intValue(), matchList.stream().skip(1).toList());
        return cmd;

    }

    // la parola data in input viene convertita nel codice del comando utilizzando l'HashMap
    private static Integer stringToCode(String s) {
        s = s.trim();
        s = s.toLowerCase();
        Integer code = codiciMap.get(s);
        return code;
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
            if(risultato == CodiciRisposta.ERR_INVALID_USERNAME)
                System.out.println("< L'Username inserito non è valido ");
            else if(risultato == CodiciRisposta.ERR_USERNAME_USED)
                System.out.println("< L'Username inserito è già stato preso");
            else if(risultato == CodiciRisposta.ERR_PASSWORD_TOOSHORT)
                System.out.println("< Password inserita è troppo corta");
            else if(risultato == CodiciRisposta.SUCCESS)
                System.out.println("< Registrazione effettuata");
            else
                System.out.println("< Errore sconosciuto");
        }
        catch (Exception e){
            System.out.println("Si è verificato un errore " + e.toString() + e.getMessage());
            e.printStackTrace();
        }
    }

}
