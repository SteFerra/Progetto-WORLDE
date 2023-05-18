package server.domini;

//Classe che rappresenta un utente
public class User {

    private int id;
    private String username;
    private String hashedpwd;

    //informazioni dell'utente relative al gioco come punteggio, numero partite...
    private int score;
    private int numPlay;
    private int numPlayWin;
    private int numPlayLose;
    private float percentWin;
    private int lastWinStreak;
    private int maxWinStreak;
    private int guessDistribution;


    public User(int id, String username, String hashedpwd){
        this.id=id;
        this.username=username;
        this.hashedpwd=hashedpwd;

        this.score=0;
        this.numPlay=0;
        this.numPlayWin=0;
        this.numPlayLose=0;
        this.percentWin=0;
        this.lastWinStreak=0;
        this.maxWinStreak=0;
        this.guessDistribution=0;
    }

    public String getUsername(){

        return this.username;
    }

    public String getHashedPwd(){

        return hashedpwd;
    }

    public int getId() {
        return id;
    }
}
