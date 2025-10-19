package org.SalimMRP.business;

import org.SalimMRP.persistence.models.Rating;

import java.util.List;

// Service-Schnittstelle für die Bewertung von Medien inklusive Moderation und Likes.
public interface RatingService {

    Rating createRating(Rating rating);

    List<Rating> getRatingsForMedia(int mediaId);

    Rating getRatingById(int id);

    Rating getUserRatingForMedia(int mediaId, int userId);

    boolean updateRating(Rating rating, int userId);

    boolean deleteRating(int ratingId, int userId);

    boolean confirmComment(int ratingId, int userId);

    boolean likeRating(int ratingId, int userId);

    boolean unlikeRating(int ratingId, int userId);
}
