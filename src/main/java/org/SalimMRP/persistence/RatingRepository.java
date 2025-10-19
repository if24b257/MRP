package org.SalimMRP.persistence;

import org.SalimMRP.persistence.models.Rating;

import java.util.List;
import java.util.Set;

// Schnittstelle zur Verwaltung von Ratings inklusive Moderation und Like-Informationen.
public interface RatingRepository {

    Rating save(Rating rating);

    boolean update(Rating rating);

    boolean delete(int id);

    Rating findById(int id);

    Rating findByMediaIdAndUserId(int mediaId, int userId);

    List<Rating> findByMediaId(int mediaId);

    boolean confirmComment(int ratingId);

    boolean addLike(int ratingId, int userId);

    boolean removeLike(int ratingId, int userId);

    Set<Integer> findLikes(int ratingId);
}
