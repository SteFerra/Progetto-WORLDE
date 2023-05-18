package server.admin;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;

// Classe usata per la creazione/gestione della classifica
// Viene utilizzata una HashMap(String, Integer)
//                        Username -> Punteggio
// La Classifica viene salvata in un file .json per mantenere la persistenza del
// sistema al riavvio del Server
public class RankingAdmin {

    private static HashMap<String, Integer> classifica;

    private static final String classificaFile = "Classifica.json";

    //metodo per creare e ripristinare la classifica al riavvio del server
    public void inizializza() throws FileNotFoundException{
        classifica = new HashMap<>();

        File fileClassifica = new File(classificaFile);
        if(fileClassifica.exists() && !fileClassifica.isDirectory()){
            FileInputStream inputStream = new FileInputStream(classificaFile);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
            Type collectionType = new TypeToken<HashMap<String, Integer>>() {}.getType();
            fileClassifica = new Gson().fromJson(reader, collectionType);
        }
    }

    //Restituisce la Classifica
    public static HashMap<String, Integer> getRanking(){
        return classifica;
    }

    //Salvo la classifica sul file .json
    private void salvaClassifica(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(classificaFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            String json = gson.toJson(classifica);
            outputStreamWriter.write(json);
            outputStreamWriter.flush();
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("Errore nel salvataggio del file: " + e.getMessage());
        }
    }
}
