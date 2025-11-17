package org.SalimMRP.business.dto;

// Beschreibt zusammengefasste Kennzahlen für das Profil eines Benutzers.
public class UserProfile {
    private final String username;
    private final int totalRatings;
    private final double averageRating;
    private final String favoriteGenre;
    private final int favoritesCount;

    public UserProfile(String username,
                       int totalRatings,
                       double averageRating,
                       String favoriteGenre,
                       int favoritesCount) {
        this.username = username;
        this.totalRatings = totalRatings;
        this.averageRating = averageRating;
        this.favoriteGenre = favoriteGenre;
        this.favoritesCount = favoritesCount;
    }

    public String getUsername() {
        return username;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    public int getFavoritesCount() {
        return favoritesCount;
    }
}
