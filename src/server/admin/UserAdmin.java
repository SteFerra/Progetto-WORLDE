package server.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import condivisi.CodiciRisposta;
import server.Hasher;
import server.domini.User;

import java.io.*;
import java.util.*;

//classe per creare e gestire la lista di utenti registrati con la relativa classe User
public class UserAdmin {

    static final String listaUtentiFile = "UserList.json";
    public List<User> usersList;

    // inizializzazione che consiste nella creazione della lista degli utenti, o il ripristino da json.
    public void initialize() throws IOException{
        File f = new File(listaUtentiFile);

        if(f.exists() && !f.isDirectory()) {
            FileInputStream inputStream = new FileInputStream(listaUtentiFile);
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
            var collectionType = new TypeToken<ArrayList<User>>(){}.getType();
            usersList = new Gson().fromJson(reader,collectionType) ;
            inputStream.close();
        }
        else
            usersList = new ArrayList<>();
    }

    //Registrazione di un utente
    public synchronized int register(String username, String password) throws Exception{
        if(username.length()==0)
            return CodiciRisposta.ERR_USERNAME_NON_VALIDO;

        if(getUser(username)!=null)
            return CodiciRisposta.ERR_USERNAME_GIÀ_PRESO;
        if(password.length()<=4)
            return CodiciRisposta.ERR_PASSWORD_TROPPO_CORTA;

        createUser(username,password);
        return CodiciRisposta.SUCCESS;

    }

    private User createUser(String username, String password) throws Exception{
        Hasher hasher = new Hasher();
        String hashedPwd = hasher.Hash(password);
        int idUser = getNewId();
        User newUser = new User(idUser, username, hashedPwd);
        usersList.add(newUser);
        saveUserList(usersList);
        return newUser;
    }

    // ritorna un nuovo id da assegnare a un nuovo utente.
    private int getNewId() {
        var maxId = usersList.stream().mapToInt(x -> x.getId()).max().orElse(0);
        return maxId+1;
    }

    // ritorna l'utente con lo username specificato.
    public synchronized User getUser (String username){
        return usersList.stream().filter(user -> user.getUsername().equals(username)).findAny().orElse(null);
    }

    //cerca se l'username è presente nel file degli utenti registrati
    public synchronized int login(String username, String password) throws Exception{
        var utente = getUser(username);
        if (utente==null)
            return CodiciRisposta.ERR_USERNAME_NON_PRESENTE;
        Hasher hasher = new Hasher();
        if (!hasher.isValidHashPassword(password,utente.getHashedPwd()))
            return CodiciRisposta.ERR_PASSWORD_SBAGLIATA;
        return CodiciRisposta.SUCCESS;
    }

    public synchronized int logout(String username){
        var utente = getUser(username);
        if(utente == null)
            return CodiciRisposta.ERR_USERNAME_NON_PRESENTE;
        return CodiciRisposta.SUCCESS;
    }

    public Boolean haGiocato(String username){
        for(User user : usersList){
            if(user.getUsername().equals(username)){
                return user.getHaGiocato();
            }
        }
        return false;
    }

    public Boolean staGiocando(String username){
        for(User user : usersList){
            if(user.getUsername().equals(username)){
                return user.getStaGiocando();
            }
        }
        return false;
    }

    //mette a true/false la variabile haGiocato del giocatore
    public void setHaGiocato(String username, Boolean bool){
        for(User user : usersList){
            if(user.getUsername().equals(username)){
                user.setHaGiocato(bool);
            }
        }
    }

    //setta la variabile staGiocando
    public void setStaGiocando(String username, Boolean bool){
        for(User user : usersList){
            if(user.getUsername().equals(username)){
                user.setStaGiocando(bool);
            }
        }
    }

    //in caso di vittoria aggiorno le statistiche
    public void aggiornaPartiteVinte(String username, int tentativi){
        for(User user : usersList){
            if(user.getUsername().equals(username)){
                user.numPartiteGiocate++;
                user.numPartiteVinte++;
                user.guessDistribution[tentativi]++;
                user.percentVittoria = ((float) user.numPartiteVinte/ user.numPartiteGiocate)*100;
                user.ultimaWinStreak++;
                if(user.ultimaWinStreak > user.maxWinStreak) user.maxWinStreak=user.ultimaWinStreak;
            }
        }
    }

    //in caso di partita persa aggiorno le statistiche
    public void aggiornaPartitePerse(String username, int tentativi){
        for(User user : usersList){
            if(user.getUsername().equals(username)){
                user.numPartiteGiocate++;
                user.numPartitePerse++;
                user.percentVittoria = ((float) user.numPartiteVinte/ user.numPartiteGiocate)*100;
                user.ultimaWinStreak = 0;
            }
        }
    }

    public void aggiornaPunteggio(String username){
        for(User user: usersList){
            if(user.getUsername().equals(username)){
                user.aggiornaPunteggio();
            }
        }
    }

    //viene chiamata ogni volta viene estratta una nuova parola segreta
    //dando così modo agli utenti di poter rigiocare
    public void resettaPartita(){
        for(User user : usersList){
            user.setHaGiocato(false);
        }
    }

    public HashMap<String, Object> getStatistiche(String username){
        for(User user: usersList){
            if(user.getUsername().equals(username)){
                HashMap<String, Object> statistiche = new HashMap<>();
                statistiche.put("partite giocate", user.numPartiteGiocate);
                statistiche.put("percentuale vittoria", user.percentVittoria);
                statistiche.put("ultima win streak", user.ultimaWinStreak);
                statistiche.put("massima win streak", user.maxWinStreak);
                statistiche.put("guess distribution", user.guessDistribution);
                return statistiche;
            }
        }
        return null;
    }


    // salvataggio della lista utenti su file json.
    public void saveUserList(List<User> usersList) throws IOException{
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileOutputStream fos = new FileOutputStream(listaUtentiFile);
        OutputStreamWriter ow = new OutputStreamWriter(fos);
        String userJson = gson.toJson(usersList);
        ow.write(userJson);
        ow.flush();
        ow.close();
    }

    public void saveUserListHook() throws IOException {
        saveUserList(usersList);
    }
}
