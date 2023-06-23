package server;


// Questo è il Main del Server Wordle
// istanza WordleServer e lo fa partire.
public class ServerMain {
    public static void main(String[] args) throws Exception {
        System.out.println("Wordle Server si sta avviando...");
        WordleServer server = new WordleServer();
        server.execute();
    }
}