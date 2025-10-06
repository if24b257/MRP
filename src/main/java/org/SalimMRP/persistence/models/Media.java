package org.SalimMRP.persistence.models;

public class Media {
    private int id;
    private String title;
    private String description;
    private String mediaType; // movie, series, game
    private int releaseYear;
    private String genre;
    private int ageRestriction;
    private double averageScore;
    private int createdByUserId;

    public Media() {}

    public Media(String title, String description, String mediaType, int releaseYear, String genre, int ageRestriction, int createdByUserId) {
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.ageRestriction = ageRestriction;
        this.createdByUserId = createdByUserId;
    }

    //Getter und Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(int ageRestriction) { this.ageRestriction = ageRestriction; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public int getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(int createdByUserId) { this.createdByUserId = createdByUserId; }
}
