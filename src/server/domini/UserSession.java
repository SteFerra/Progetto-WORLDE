package server.domini;

// Rappresenta la sessione utente. Contiene il solo username.
public class UserSession {
    public  String username = null;

    // rende la sessione anonima (senza login dell'utente)
    public void clear() {
        username = null;
    }

    public String getUsername(){
        return username;
    }
}
