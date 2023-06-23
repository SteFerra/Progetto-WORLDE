package condivisi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Classe usata per la lettura del file di configurazione
public class ConfigReader {

    private final String filename;
    private final Map<String, String> map;

    public ConfigReader(String filename){
        this.filename = filename;
        map = new HashMap<String, String>();
    }

    public void LoadConfiguration() throws IOException {
        map.clear();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String linea;
        while((linea = reader.readLine()) != null){
            readLine(linea.trim());
        }
    }

    //Legge la linea di configurazione trascurando i commenti rappresentati da #
    private void readLine(String line){
        if (line.startsWith("#"))  // è un commento
            return;
        if (line.length() == 0)
            return;

        String[] words = line.split("=",2);
        if (words.length != 2)
            System.err.println("Linea di configurazione non corretta sintatticamente.");
        var key = words[0].trim().toLowerCase();
        var value = words[1].trim();
        map.putIfAbsent(key,value);
    }

    public String getStringValue(String key, String defaultValue){
        return map.getOrDefault(key,defaultValue);
    }

    public int getIntValue(String key, int defaultValue){
        if(!map.containsKey(key))
            return defaultValue;
        return Integer.parseInt(map.get(key));
    }

    public Long getLongValue(String key, long defaultValue){
        if(!map.containsKey(key))
            return defaultValue;
        return Long.parseLong(map.get(key));
    }
}
