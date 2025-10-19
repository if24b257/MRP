package org.SalimMRP.persistence.models;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// POJO für Bewertungen eines Medien-Eintrags inklusive Moderations- und Like-Informationen.
public class Rating {
    private int id;
    private int mediaId;
    private int userId;
    private int starValue;
    private String comment;
    private Instant createdAt;
    private boolean commentConfirmed;
    private final Set<Integer> likedByUserIds = new HashSet<>();

    public Rating() {
    }

    public Rating(int mediaId, int userId, int starValue, String comment, Instant createdAt) {
        this.mediaId = mediaId;
        this.userId = userId;
        setStarValue(starValue);
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Rating(int id,
                  int mediaId,
                  int userId,
                  int starValue,
                  String comment,
                  Instant createdAt,
                  boolean commentConfirmed,
                  Set<Integer> likedByUserIds) {
        this.id = id;
        this.mediaId = mediaId;
        this.userId = userId;
        setStarValue(starValue);
        this.comment = comment;
        this.createdAt = createdAt;
        this.commentConfirmed = commentConfirmed;
        if (likedByUserIds != null) {
            this.likedByUserIds.addAll(likedByUserIds);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStarValue() {
        return starValue;
    }

    public void setStarValue(int starValue) {
        if (starValue < 1 || starValue > 5) {
            throw new IllegalArgumentException("starValue must be between 1 and 5");
        }
        this.starValue = starValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCommentConfirmed() {
        return commentConfirmed;
    }

    public void setCommentConfirmed(boolean commentConfirmed) {
        this.commentConfirmed = commentConfirmed;
    }

    public Set<Integer> getLikedByUserIds() {
        return likedByUserIds;
    }

    public void setLikedByUserIds(Set<Integer> likedByUserIds) {
        this.likedByUserIds.clear();
        if (likedByUserIds != null) {
            this.likedByUserIds.addAll(likedByUserIds);
        }
    }

    public boolean likeByUser(int userId) {
        return likedByUserIds.add(userId);
    }

    public boolean unlikeByUser(int userId) {
        return likedByUserIds.remove(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating rating)) return false;
        return id == rating.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
