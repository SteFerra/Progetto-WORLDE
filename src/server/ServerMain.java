package server;


// main del server
// istanza WordleServer e lancia il servizio.
public class ServerMain {
    public static void main(String[] args) throws Exception {
        System.out.println("Wordle Server si sta avviando...");
        WordleServer server = new WordleServer();
        server.execute();
    }
}