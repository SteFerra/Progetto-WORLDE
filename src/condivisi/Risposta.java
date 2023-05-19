package condivisi;

// classe utilizzata per scegliere la risposta del server (inviata in formato json)
public class Risposta {
    public int esito;  // codici definiti in condivisi.CodiciRisposta
    public String payload;  // contenuto che dipende dal comando e da esito.

    public Risposta(int esito, String payload){
        this.esito = esito;
        this.payload = payload;
    }

    // trasforma il codice di response in messaggio.
    public String MessaggioDiRisposta(){
        switch (esito){
            //in caso di risultato corretto
            case CodiciRisposta.SUCCESS: return "Operazione eseguita con successo.";

            //in caso di errori
            case CodiciRisposta.ERR_USERNAME_NON_VALIDO: return "Nome utente non valido";
            case CodiciRisposta.ERR_USERNAME_NON_PRESENTE: return "Nome utente non esistente";

            default: return "Codice di risposta sconosciuto ("+esito+").";
        }
    }
}
