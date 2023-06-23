package server.domini;

// Identifica la sessione dell'utente loggato. Contiene solo l'username.
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
