package condivisi;

// classe utilizzata per scegliere la risposta del server (inviata in formato json)
public class Risposta {
    public int resultCode;  // codici definiti in condivisi.CodiciRisposta
    public String payload;  // contenuto che dipende dal comando e da resultCode.

    public Risposta(int resultCode, String payload){
        this.resultCode = resultCode;
        this.payload = payload;
    }

    // trasforma il codice di response in messaggio.
    public String getResponseMessage(){
        switch (resultCode){
            //in caso di risultato corretto
            case CodiciRisposta.SUCCESS: return "Operazione eseguita con successo.";

            //in caso di errori
            case CodiciRisposta.ERR_INVALID_USERNAME: return "Nome utente non valido";
            case CodiciRisposta.ERR_USERNAME_NOT_EXISTING: return "Nome utente non esistente";

            default: return "Codice di risposta sconosciuto ("+resultCode+").";
        }
    }
}
