package server.admin;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import condivisi.interfacce.INotifyRanking;
import condivisi.interfacce.INotifyRankingUpdate;
import server.WordleServer;
import server.servizi.RankingServiceImpl;

import java.io.*;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

// Classe usata per la creazione/gestione della classifica
// Viene utilizzata una HashMap(String username, Integer punteggio)
// La Classifica viene salvata in un file .json per mantenere la persistenza del
// sistema al riavvio del Server
public class RankingAdmin {

    private static HashMap<String, Double> classifica;

    private final String classificaFile = "Classifica.json";

    //metodo per creare e ripristinare la classifica al riavvio del server
    public void inizializza() throws FileNotFoundException{
        classifica = new LinkedHashMap<>();

        File fileClassifica = new File(classificaFile);
        if(fileClassifica.exists() && !fileClassifica.isDirectory()){
            FileInputStream inputStream = new FileInputStream(classificaFile);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
            Type collectionType = new TypeToken<LinkedHashMap<String, Double>>() {}.getType();
            classifica = new Gson().fromJson(reader, collectionType);
            System.out.println("CLASSIFICA: " + classifica);
        }
    }

    //Restituisce la Classifica
    public synchronized static HashMap<String, Double> getRanking(){
        return classifica;
    }

    //Salvo la classifica sul file .json
    public void salvaClassifica(){
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

    public static synchronized void aggiornaClassifica(String username, double punteggio){
        // Salvo la classifica prima dell'aggiornamento
        HashMap<String, Double> classificaPrecedente = new LinkedHashMap<>(classifica);

        // Aggiorno la classifica
        classifica.put(username, punteggio);
        ordinaClassifica();
        System.out.println("Classifica aggiornata");

        // Controllo se le prime tre posizioni sono cambiate
        boolean primeTrePosizioniCambiate = false;
        List<String> classificaPrecedenteKeys = new ArrayList<>(classificaPrecedente.keySet());
        List<String> classificaKeys = new ArrayList<>(classifica.keySet());

        for (int i = 0; i < 3; i++) {
            String usernamePrecedente = classificaPrecedenteKeys.get(i);
            String usernameAttuale = classificaKeys.get(i);

            if (!usernamePrecedente.equals(usernameAttuale)) {
                primeTrePosizioniCambiate = true;
                break;
            }
        }
        if(primeTrePosizioniCambiate){
            //avviso i client che sono cambiati le prime tre posizioni in classifica
            RankingServiceImpl.aggiornaPosizioni(RankingServiceImpl.clients);
        }
    }


    //aggiorno la classifica in ordine crescente (punteggio più basso = giocatore più bravo)
    //utilizzo lambda function.
    private static void ordinaClassifica(){
        classifica = classifica.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        System.out.println("Classifica ordinata");
    }
}
