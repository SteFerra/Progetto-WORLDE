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

public class UserAdmin {

    static final String listaUtentiFile = "UserList.json";
    private List<User> usersList;

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
        var user = getUser(username);
        if (user==null)
            return CodiciRisposta.ERR_USERNAME_NON_PRESENTE;
        Hasher hasher = new Hasher();
        if (!hasher.isValidHashPassword(password,user.getHashedPwd()))
            return CodiciRisposta.ERR_PASSWORD_SBAGLIATA;
        return CodiciRisposta.SUCCESS;
    }


    // salvataggio della lista utenti su file json.
    private void saveUserList(List<User> usersList) throws IOException{
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileOutputStream fos = new FileOutputStream(listaUtentiFile);
        OutputStreamWriter ow = new OutputStreamWriter(fos);
        String userJson = gson.toJson(usersList);
        ow.write(userJson);
        ow.flush();
        ow.close();
    }
}
