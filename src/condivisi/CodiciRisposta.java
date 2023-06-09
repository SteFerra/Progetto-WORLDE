package condivisi;

// codici di errore per le risposte dal server
public class CodiciRisposta {
    public static final int SUCCESS = 0;
    public static final int ERR_USERNAME_NON_VALIDO = -1;
    public static final int ERR_USERNAME_GIÀ_PRESO = -2;
    public static final int ERR_PASSWORD_TROPPO_CORTA = -3;
    public static final int ERR_USERNAME_NON_PRESENTE = -4;
    public static final int ERR_AZIONE_NEGATA = -5;
    public static final int ERR_NUMERO_PARAMETRI_ERRATI = -6;
    public static final int ERR_PASSWORD_SBAGLIATA = -7;
    public static final int ERR_UTENTE_GIÀ_LOGGATO = -8;
    public static final int ERR_USERNAME_NON_VALIDO_LOGOUT = -9;
    public static final int ERR_AZIONE_NEGATA_LOGIN = -10;
    public static final int PROVA = 99;

}
