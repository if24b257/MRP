package org.SalimMRP.business;

import org.SalimMRP.persistence.MediaRepository;
import org.SalimMRP.persistence.RatingRepository;
import org.SalimMRP.persistence.models.Media;
import org.SalimMRP.persistence.models.Rating;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

// Enthält die Geschäftslogik für Ratings: Erstellung, Bearbeitung, Moderation und Likes.
public class DefaultRatingService implements RatingService {

    private final RatingRepository ratingRepository;
    private final MediaRepository mediaRepository;

    public DefaultRatingService(RatingRepository ratingRepository, MediaRepository mediaRepository) {
        this.ratingRepository = Objects.requireNonNull(ratingRepository, "ratingRepository must not be null");
        this.mediaRepository = Objects.requireNonNull(mediaRepository, "mediaRepository must not be null");
    }

    @Override
    public Rating createRating(Rating rating) {
        if (!isCreatable(rating)) {
            return null;
        }

        Media targetMedia = mediaRepository.findById(rating.getMediaId());
        if (targetMedia == null) {
            return null;
        }

        Rating existing = ratingRepository.findByMediaIdAndUserId(rating.getMediaId(), rating.getUserId());
        if (existing != null) {
            return null;
        }

        rating.setId(0);
        rating.setCommentConfirmed(false);
        rating.setCreatedAt(Instant.now());
        rating.setLikedByUserIds(null);
        return ratingRepository.save(rating);
    }

    @Override
    public List<Rating> getRatingsForMedia(int mediaId) {
        if (mediaId <= 0) {
            return List.of();
        }
        return ratingRepository.findByMediaId(mediaId);
    }

    @Override
    public Rating getRatingById(int id) {
        if (id <= 0) {
            return null;
        }
        return ratingRepository.findById(id);
    }

    @Override
    public Rating getUserRatingForMedia(int mediaId, int userId) {
        if (mediaId <= 0 || userId <= 0) {
            return null;
        }
        return ratingRepository.findByMediaIdAndUserId(mediaId, userId);
    }

    @Override
    public boolean updateRating(Rating rating, int userId) {
        if (rating == null || rating.getId() <= 0 || userId <= 0) {
            return false;
        }

        Rating existing = ratingRepository.findById(rating.getId());
        if (existing == null || existing.getUserId() != userId) {
            return false;
        }

        if (!isStarValueValid(rating.getStarValue())) {
            return false;
        }

        boolean commentChanged = !Objects.equals(existing.getComment(), rating.getComment());
        existing.setStarValue(rating.getStarValue());
        existing.setComment(rating.getComment());
        if (commentChanged) {
            existing.setCommentConfirmed(false);
        }

        return ratingRepository.update(existing);
    }

    @Override
    public boolean deleteRating(int ratingId, int userId) {
        if (ratingId <= 0 || userId <= 0) {
            return false;
        }

        Rating existing = ratingRepository.findById(ratingId);
        if (existing == null || existing.getUserId() != userId) {
            return false;
        }
        return ratingRepository.delete(ratingId);
    }

    @Override
    public boolean confirmComment(int ratingId, int userId) {
        if (ratingId <= 0 || userId <= 0) {
            return false;
        }

        Rating existing = ratingRepository.findById(ratingId);
        if (existing == null || existing.getUserId() != userId) {
            return false;
        }

        if (existing.getComment() == null || existing.getComment().isBlank()) {
            return false;
        }

        if (existing.isCommentConfirmed()) {
            return true;
        }

        return ratingRepository.confirmComment(ratingId);
    }

    @Override
    public boolean likeRating(int ratingId, int userId) {
        if (ratingId <= 0 || userId <= 0) {
            return false;
        }

        Rating rating = ratingRepository.findById(ratingId);
        if (rating == null || rating.getUserId() == userId) {
            return false;
        }

        return ratingRepository.addLike(ratingId, userId);
    }

    @Override
    public boolean unlikeRating(int ratingId, int userId) {
        if (ratingId <= 0 || userId <= 0) {
            return false;
        }

        Rating rating = ratingRepository.findById(ratingId);
        if (rating == null || rating.getUserId() == userId) {
            return false;
        }

        return ratingRepository.removeLike(ratingId, userId);
    }

    private boolean isCreatable(Rating rating) {
        return rating != null
                && rating.getMediaId() > 0
                && rating.getUserId() > 0
                && isStarValueValid(rating.getStarValue());
    }

    private boolean isStarValueValid(int starValue) {
        return starValue >= 1 && starValue <= 5;
    }
}
