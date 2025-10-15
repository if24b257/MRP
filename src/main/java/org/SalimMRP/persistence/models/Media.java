package org.SalimMRP.persistence.models;

public class Media {
    private int id;
    private String title;
    private String description;
    private String mediaType;
    private int createdByUserId;

    public Media() {
    }

    public Media(String title, String description, String mediaType, int createdByUserId) {
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
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

    public int getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}
