package org.SalimMRP.persistence.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// POJO für Media-Einträge inklusive Metadaten wie Genres und Altersfreigabe.
// Diese Klasse dient sowohl als Eingabemodell (JSON -> Media) als auch als
// Transferobjekt zwischen Datenbankebene und Geschäftslogik.
public class Media {
    private int id;
    private String title;
    private String description;
    private String mediaType;
    private Integer releaseYear;
    private String ageRestriction;
    private final List<String> genres = new ArrayList<>();
    private int createdByUserId;

    public Media() {
    }

    public Media(String title,
                 String description,
                 String mediaType,
                 Integer releaseYear,
                 String ageRestriction,
                 List<String> genres,
                 int createdByUserId) {
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.releaseYear = releaseYear;
        this.ageRestriction = ageRestriction;
        setGenres(genres);
        this.createdByUserId = createdByUserId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
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

    public List<String> getGenres() {
        return Collections.unmodifiableList(genres);
    }

    public void setGenres(List<String> genres) {
        this.genres.clear();
        if (genres != null) {
            this.genres.addAll(genres.stream()
                    .filter(g -> g != null && !g.isBlank())
                    .map(String::trim)
                    .toList());
        }
    }

    public int getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}
