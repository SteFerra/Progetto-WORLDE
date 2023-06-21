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
        return switch (esito) {
            //in caso di risultato corretto
            case CodiciRisposta.SUCCESS -> "Operazione eseguita con successo";
            case CodiciRisposta.PLAY -> "Puoi iniziare ad indovinare la parola segreta";
            case CodiciRisposta.HAI_VINTO -> "Complimenti hai indovinato la parola segreta!";
            case CodiciRisposta.STATISTICHE -> "Ecco le tue statistiche";

            //in caso di errori
            case CodiciRisposta.ERR_COMANDO_NON_IMPLEMENTATO -> "Comando non implementato";
            case CodiciRisposta.ERR_USERNAME_NON_VALIDO -> "Nome utente non valido";
            case CodiciRisposta.ERR_NUMERO_PARAMETRI_ERRATI -> "Il numero di parametri inseriti non è corretto";
            case CodiciRisposta.ERR_DEVI_PRIMA_INIZIARE_A_GIOCARE -> "Devi prima inizare a giocare con il comando playWordle";

            //errori nella login
            case CodiciRisposta.ERR_AZIONE_NEGATA_LOGIN -> "Hai già effettuato il login";
            case CodiciRisposta.ERR_USERNAME_NON_PRESENTE -> "Nome utente non esistente";
            case CodiciRisposta.ERR_UTENTE_GIÀ_LOGGATO -> "L'utente risulta già loggato";
            case CodiciRisposta.ERR_PASSWORD_SBAGLIATA -> "La password non è corretta";

            //logout
            case CodiciRisposta.ERR_AZIONE_NEGATA -> "Devi prima effettuare il login";
            case CodiciRisposta.ERR_USERNAME_NON_VALIDO_LOGOUT -> "Lo username non corrisponde a quello inserito durante il login";

            //playwordle
            case CodiciRisposta.ERR_PARTITA_GIÀ_GIOCATA -> "Hai già giocato, aspetta l'estrazione della prossima parola";

            //sendWord
            case CodiciRisposta.ERR_PAROLA_TROPPO_CORTA -> "Parola troppo corta, Inserisci una parola di 10 lettere";
            case CodiciRisposta.ERR_PAROLA_NON_PRESENTE -> "La parola che hai inserito non è presente nel dizionario";
            case CodiciRisposta.ERR_HAI_PERSO -> "Hai perso, non sei riuscito ad indovinare la parola segreta";

            default -> "Codice di risposta sconosciuto (" + esito + ").";
        };
    }
}
