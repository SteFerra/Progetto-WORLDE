package server.domini;

import server.admin.RankingAdmin;

//Classe che rappresenta un utente
public class User {

    private int id;
    private String username;
    private String hashedpwd;

    //informazioni dell'utente relative al gioco come punteggio, numero partite...
    public double punteggio;
    public int numPartiteGiocate;
    public int numPartiteVinte;
    public int numPartitePerse;
    public float percentVittoria;
    public int ultimaWinStreak;
    public int maxWinStreak;
    public int[] guessDistribution;
    public Boolean haGiocato;
    public Boolean staGiocando;


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
        this.guessDistribution= new int[12];
        this.haGiocato = false;
        this.staGiocando = false;
    }

    public String getUsername(){

        return this.username;
    }

    public String getHashedPwd(){

        return hashedpwd;
    }

    public void aggiornaPunteggio(){
        int sum = 0, numGuessed = 0, maxAttempts=12;
        for (int i = 0; i < guessDistribution.length; i++) {
            sum += (i + 1) * guessDistribution[i];
            numGuessed += guessDistribution[i];
        }
        // Per le parole non indovinate (partite perse),
        // considero un numero di tentativi pari a 7 (ovvero maxAttempts + 1).
        sum += (maxAttempts + 1) * (numPartitePerse);
        this.punteggio = ((double) sum / (double) numPartiteGiocate);
        RankingAdmin.aggiornaClassifica(username, punteggio);
        System.out.println("Risultato aggiornato: " + punteggio);
    }

    public Boolean getHaGiocato(){ return haGiocato; }
    public Boolean getStaGiocando(){return staGiocando;}

    public void setHaGiocato(boolean bool){ this.haGiocato = bool;}
    public void setStaGiocando(boolean bool){this.staGiocando = bool;}

    public int getIdByUsername(String username){ return id;}

    public int getId() {
        return id;
    }
}
