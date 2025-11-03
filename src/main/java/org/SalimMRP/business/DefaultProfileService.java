package org.SalimMRP.business;

import org.SalimMRP.business.dto.LeaderboardEntry;
import org.SalimMRP.business.dto.MediaDetails;
import org.SalimMRP.business.dto.UserProfile;
import org.SalimMRP.persistence.FavoriteRepository;
import org.SalimMRP.persistence.RatingRepository;
import org.SalimMRP.persistence.UserRepository;
import org.SalimMRP.persistence.models.Media;
import org.SalimMRP.persistence.models.Rating;
import org.SalimMRP.persistence.models.User;
import org.SalimMRP.persistence.models.UserRatingCount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

// Kombiniert Daten aus mehreren Repositories, um Profilinformationen aufzubereiten.
public class DefaultProfileService implements ProfileService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final FavoriteRepository favoriteRepository;
    private final MediaService mediaService;

    public DefaultProfileService(UserRepository userRepository,
                                 RatingRepository ratingRepository,
                                 FavoriteRepository favoriteRepository,
                                 MediaService mediaService) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.ratingRepository = Objects.requireNonNull(ratingRepository, "ratingRepository must not be null");
        this.favoriteRepository = Objects.requireNonNull(favoriteRepository, "favoriteRepository must not be null");
        this.mediaService = Objects.requireNonNull(mediaService, "mediaService must not be null");
    }

    @Override
    public UserProfile buildProfile(int userId) {
        if (userId <= 0) {
            return null;
        }
        User user = userRepository.findById(userId);
        if (user == null) {
            return null;
        }

        List<Rating> history = ratingRepository.findByUserId(userId);
        double averageRating = history.stream()
                .mapToInt(Rating::getStarValue)
                .average()
                .orElse(0.0);

        String favoriteGenre = determineFavoriteGenre(history);
        int favoritesCount = favoriteRepository.findMediaIdsByUser(userId).size();

        return new UserProfile(
                user.getUsername(),
                history.size(),
                averageRating,
                favoriteGenre,
                favoritesCount
        );
    }

    @Override
    public List<Rating> ratingHistory(int userId) {
        if (userId <= 0) {
            return List.of();
        }
        return ratingRepository.findByUserId(userId);
    }

    @Override
    public List<MediaDetails> favoriteMedia(int userId) {
        return mediaService.listFavorites(userId);
    }

    @Override
    public List<LeaderboardEntry> leaderboard(int limit) {
        int effectiveLimit = limit > 0 ? limit : 10;
        List<UserRatingCount> ratingCounts = ratingRepository.findRatingCountsPerUser(effectiveLimit);
        List<LeaderboardEntry> result = new ArrayList<>();

        for (UserRatingCount count : ratingCounts) {
            User user = userRepository.findById(count.getUserId());
            if (user == null) {
                continue;
            }
            result.add(new LeaderboardEntry(user.getUsername(), count.getRatingCount()));
        }
        return result;
    }

    private String determineFavoriteGenre(List<Rating> ratings) {
        if (ratings.isEmpty()) {
            return null;
        }

        Map<String, Integer> scores = new HashMap<>();
        Map<String, String> displayNames = new HashMap<>();
        Map<Integer, Media> mediaCache = new HashMap<>();

        for (Rating rating : ratings) {
            if (rating.getStarValue() < 3) {
                continue;
            }
            Media media = mediaCache.computeIfAbsent(rating.getMediaId(), mediaService::getMediaById);
            if (media == null) {
                continue;
            }
            int weight = rating.getStarValue();
            for (String genre : media.getGenres()) {
                String normalized = normalize(genre);
                scores.merge(normalized, weight, Integer::sum);
                displayNames.putIfAbsent(normalized, genre.trim());
            }
        }

        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .map(displayNames::get)
                .map(this::formatGenreLabel)
                .orElse(null);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String formatGenreLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.length() == 1) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }
}
