package org.SalimMRP.persistence.models;

public class User {
    private int id;
    private String username;
    private String password;
    private String favoriteGenre;
    private int totalRatings;
    private double averageScore;

    //Leerer Konstruktor (wichtig für Jackson & JDBC)
    public User() {}

    //Konstruktor für einfache Initialisierung
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    //Konstruktor für vollständige User-Objekte (z. B. aus DB)
    public User(int id, String username, String password, String favoriteGenre, int totalRatings, double averageScore) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.favoriteGenre = favoriteGenre;
        this.totalRatings = totalRatings;
        this.averageScore = averageScore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    public void setFavoriteGenre(String favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }
}
