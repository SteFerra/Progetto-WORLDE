package condivisi;

import java.util.ArrayList;
import java.util.List;

// classe che rappresenta il comando inviato al server (in formato json)
public class Comandi {
    public static final int CMD_REGISTER = 0;
    public static final int CMD_LOGIN = 1;
    public static final int CMD_LOGOUT = 2;
    public static final int CMD_SHOWMERANKING = 3;
    public static final int CMD_PLAYWORDLE = 4;
    public static final int CMD_SENDWORD = 5;
    public static final int CMD_SENDMESTATISTIC = 6;
    public static final int CMD_SHARE = 7;
    public static final int CMD_SHOWMESHARING = 8;

    public int codice;  // codice del comando
    public ArrayList<String> parametri; // parametri del comando

    public Comandi(int codice, List<String> param){
        this.codice = codice;
        parametri = new ArrayList<>();
        parametri.addAll(param);
    }
}
