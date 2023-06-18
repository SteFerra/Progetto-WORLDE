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
    public static final int ERR_PARTITA_GIÀ_GIOCATA = -11;
    public static final int ERR_DEVI_PRIMA_INIZIARE_A_GIOCARE = -12;
    public static final int ERR_COMANDO_NON_IMPLEMENTATO = -13;
    public static final int ERR_PAROLA_TROPPO_CORTA = -14;
    public static final int ERR_PAROLA_NON_PRESENTE = -15;
    public static final int ERR_HAI_PERSO = -16;
    public static final int HAI_VINTO = -17;
    public static final int PLAY = 1;

}
