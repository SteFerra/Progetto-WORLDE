package condivisi;

import java.io.Serializable;
import java.util.HashMap;

//Classe che rappresenta la classifica utilizzata nell'invio per la notifica callback
//Implementa Serializable
public class ClassificaData implements Serializable {
    private HashMap<String, Double> classificaData;

    public ClassificaData(HashMap<String, Double> classifica){
        classificaData = classifica;
    }

    public HashMap<String, Double> getClassifica() {
        return classificaData;
    }
}
