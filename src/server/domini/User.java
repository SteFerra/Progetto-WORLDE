package server.domini;

//Classe che rappresenta un utente
public class User {

    private int id;
    private String username;
    private String hashedpwd;

    //informazioni dell'utente relative al gioco come punteggio, numero partite...
    private int punteggio;
    private int numPartiteGiocate;
    private int numPartiteVinte;
    private int numPartitePerse;
    private float percentVittoria;
    private int ultimaWinStreak;
    private int maxWinStreak;
    private int[] guessDistribution;


    public User(int id, String username, String hashedpwd){
        this.id=id;
        this.username=username;
        this.hashedpwd=hashedpwd;

        this.punteggio=0;
        this.numPartiteGiocate=0;
        this.numPartiteVinte=0;
        this.numPartitePerse=0;
        this.percentVittoria=0;
        this.ultimaWinStreak=0;
        this.maxWinStreak=0;
        this.guessDistribution=null;
    }

    public String getUsername(){

        return this.username;
    }

    public String getHashedPwd(){

        return hashedpwd;
    }

    public int getIdByUsername(String username){ return id;}

    public int getId() {
        return id;
    }
}
