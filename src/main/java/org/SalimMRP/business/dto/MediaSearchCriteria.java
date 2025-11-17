package org.SalimMRP.business.dto;

// Repräsentiert frei kombinierbare Filter- und Sortieroptionen für die Mediensuche.
public class MediaSearchCriteria {
    private String titleQuery;
    private String mediaType;
    private String genre;
    private Integer releaseYear;
    private String ageRestriction;
    private Double minimumRating;
    private SortField sortField = SortField.TITLE;
    private SortDirection sortDirection = SortDirection.ASC;

    public enum SortField {
        TITLE,
        YEAR,
        SCORE
    }

    public enum SortDirection {
        ASC,
        DESC
    }

    public String getTitleQuery() {
        return titleQuery;
    }

    public void setTitleQuery(String titleQuery) {
        this.titleQuery = titleQuery;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getAgeRestriction() {
        return ageRestriction;
    }

    public void setAgeRestriction(String ageRestriction) {
        this.ageRestriction = ageRestriction;
    }

    public Double getMinimumRating() {
        return minimumRating;
    }

    public void setMinimumRating(Double minimumRating) {
        this.minimumRating = minimumRating;
    }

    public SortField getSortField() {
        return sortField;
    }

    public void setSortField(SortField sortField) {
        if (sortField != null) {
            this.sortField = sortField;
        }
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        if (sortDirection != null) {
            this.sortDirection = sortDirection;
        }
    }
}
