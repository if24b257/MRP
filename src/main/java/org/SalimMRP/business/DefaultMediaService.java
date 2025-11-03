package org.SalimMRP.business;

import org.SalimMRP.business.dto.MediaDetails;
import org.SalimMRP.business.dto.MediaSearchCriteria;
import org.SalimMRP.persistence.FavoriteRepository;
import org.SalimMRP.persistence.MediaRepository;
import org.SalimMRP.persistence.RatingRepository;
import org.SalimMRP.persistence.models.Media;
import org.SalimMRP.persistence.models.Rating;
import org.SalimMRP.persistence.models.RatingSummary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

// Verwaltet Media-Einträge, Favoriten sowie Such- und Empfehlungsläufe.
public class DefaultMediaService implements MediaService {

    private static final int MIN_RELEASE_YEAR = 1900;
    private static final int MAX_RELEASE_YEAR = 2100;

    private final MediaRepository mediaRepository;
    private final RatingRepository ratingRepository;
    private final FavoriteRepository favoriteRepository;

    public DefaultMediaService(MediaRepository mediaRepository,
                               RatingRepository ratingRepository,
                               FavoriteRepository favoriteRepository) {
        this.mediaRepository = Objects.requireNonNull(mediaRepository, "mediaRepository must not be null");
        this.ratingRepository = Objects.requireNonNull(ratingRepository, "ratingRepository must not be null");
        this.favoriteRepository = Objects.requireNonNull(favoriteRepository, "favoriteRepository must not be null");
    }

    @Override
    public boolean createMedia(Media media) {
        return isValid(media) && mediaRepository.save(media);
    }

    @Override
    public List<Media> getAllMedia() {
        return new ArrayList<>(mediaRepository.findAll());
    }

    @Override
    public Media getMediaById(int id) {
        if (id <= 0) {
            return null;
        }
        return mediaRepository.findById(id);
    }

    @Override
    public boolean updateMedia(Media media) {
        return media != null
                && media.getId() > 0
                && isValid(media)
                && mediaRepository.update(media);
    }

    @Override
    public boolean deleteMedia(int id) {
        if (id <= 0) {
            return false;
        }
        return mediaRepository.delete(id);
    }

    @Override
    public List<MediaDetails> searchMedia(MediaSearchCriteria criteria, int requestingUserId) {
        Objects.requireNonNull(criteria, "criteria must not be null");

        List<Media> baseMatches = mediaRepository.findAll().stream()
                .filter(media -> matchesBasicFilters(media, criteria))
                .toList();

        Map<Integer, RatingSummary> summaryById = summariesFor(baseMatches);
        List<MediaDetails> details = new ArrayList<>();

        for (Media media : baseMatches) {
            RatingSummary summary = summaryById.get(media.getId());
            if (!passesRatingThreshold(summary, criteria.getMinimumRating())) {
                continue;
            }
            details.add(buildDetails(media, summary, requestingUserId, false));
        }

        details.sort(comparatorFor(criteria));
        return details;
    }

    @Override
    public MediaDetails getDetailedMedia(int id, int requestingUserId) {
        Media media = getMediaById(id);
        if (media == null) {
            return null;
        }
        Map<Integer, RatingSummary> summary = summariesFor(List.of(media));
        List<Rating> ratings = ratingRepository.findByMediaId(id);
        return buildDetails(media, summary.get(id), requestingUserId, true, ratings);
    }

    @Override
    public boolean addFavorite(int mediaId, int userId) {
        if (userId <= 0 || mediaId <= 0) {
            return false;
        }
        Media media = mediaRepository.findById(mediaId);
        if (media == null) {
            return false;
        }
        return favoriteRepository.addFavorite(userId, mediaId);
    }

    @Override
    public boolean removeFavorite(int mediaId, int userId) {
        if (userId <= 0 || mediaId <= 0) {
            return false;
        }
        return favoriteRepository.removeFavorite(userId, mediaId);
    }

    @Override
    public List<MediaDetails> listFavorites(int userId) {
        if (userId <= 0) {
            return List.of();
        }
        List<Integer> favoriteIds = favoriteRepository.findMediaIdsByUser(userId);
        if (favoriteIds.isEmpty()) {
            return List.of();
        }

        List<Media> favorites = favoriteIds.stream()
                .map(mediaRepository::findById)
                .filter(Objects::nonNull)
                .toList();

        Map<Integer, RatingSummary> summary = summariesFor(favorites);
        List<MediaDetails> details = new ArrayList<>();
        for (Media media : favorites) {
            details.add(buildDetails(media, summary.get(media.getId()), userId, false));
        }
        details.sort(Comparator.comparing(md -> md.getMedia().getTitle(), String.CASE_INSENSITIVE_ORDER));
        return details;
    }

    @Override
    public List<MediaDetails> recommendMedia(int userId) {
        if (userId <= 0) {
            return List.of();
        }

        List<Rating> userRatings = ratingRepository.findByUserId(userId);
        List<Media> allMedia = mediaRepository.findAll();
        Map<Integer, Media> mediaById = allMedia.stream()
                .collect(Collectors.toMap(Media::getId, media -> media));
        Set<Integer> ratedMediaIds = userRatings.stream()
                .map(Rating::getMediaId)
                .collect(Collectors.toSet());

        Map<String, Integer> genreScores = new HashMap<>();
        Map<String, Integer> typeScores = new HashMap<>();
        Map<String, Integer> ageScores = new HashMap<>();

        for (Rating rating : userRatings) {
            if (rating.getStarValue() < 4) {
                continue;
            }
            Media ratedMedia = mediaById.get(rating.getMediaId());
            if (ratedMedia == null) {
                continue;
            }
            int weight = rating.getStarValue();
            for (String genre : ratedMedia.getGenres()) {
                genreScores.merge(normalize(genre), weight, Integer::sum);
            }
            typeScores.merge(normalize(ratedMedia.getMediaType()), weight, Integer::sum);
            if (ratedMedia.getAgeRestriction() != null) {
                ageScores.merge(normalize(ratedMedia.getAgeRestriction()), weight, Integer::sum);
            }
        }

        Set<String> topGenres = topKeys(genreScores, 3);
        Set<String> topTypes = topKeys(typeScores, 2);
        Set<String> topAges = topKeys(ageScores, 2);

        Map<Integer, RatingSummary> summaries = summariesFor(allMedia);
        List<Candidate> candidates = new ArrayList<>();

        for (Media media : allMedia) {
            if (ratedMediaIds.contains(media.getId())) {
                continue;
            }
            RatingSummary summary = summaries.get(media.getId());
            double average = summary != null ? summary.getAverageScore() : 0.0;
            int ratingCount = summary != null ? summary.getRatingCount() : 0;

            int score = 0;
            if (!topGenres.isEmpty() && intersects(media.getGenres(), topGenres)) {
                score += 6;
            }
            if (!topTypes.isEmpty() && topTypes.contains(normalize(media.getMediaType()))) {
                score += 3;
            }
            if (!topAges.isEmpty()
                    && media.getAgeRestriction() != null
                    && topAges.contains(normalize(media.getAgeRestriction()))) {
                score += 2;
            }
            if (summary != null) {
                score += Math.min(5, (int) Math.round(average));
            }
            if (score == 0 && ratingCount == 0) {
                continue;
            }

            MediaDetails details = buildDetails(media, summary, userId, false);
            candidates.add(new Candidate(details, score, average, ratingCount));
        }

        if (candidates.isEmpty()) {
            MediaSearchCriteria fallback = new MediaSearchCriteria();
            fallback.setSortField(MediaSearchCriteria.SortField.SCORE);
            fallback.setSortDirection(MediaSearchCriteria.SortDirection.DESC);
            fallback.setMinimumRating(3.5);
            return searchMedia(fallback, userId).stream()
                    .limit(10)
                    .toList();
        }

        candidates.sort(Comparator
                .comparingInt(Candidate::score).reversed()
                .thenComparingDouble(Candidate::average).reversed()
                .thenComparingInt(Candidate::ratingCount).reversed()
                .thenComparing(c -> c.details().getMedia().getTitle(), String.CASE_INSENSITIVE_ORDER));

        return candidates.stream()
                .map(Candidate::details)
                .limit(10)
                .toList();
    }

    private Comparator<MediaDetails> comparatorFor(MediaSearchCriteria criteria) {
        Comparator<MediaDetails> comparator;
        if (criteria.getSortField() == MediaSearchCriteria.SortField.YEAR) {
            comparator = Comparator.comparingInt(details ->
                    details.getMedia().getReleaseYear() != null ? details.getMedia().getReleaseYear() : Integer.MIN_VALUE);
        } else if (criteria.getSortField() == MediaSearchCriteria.SortField.SCORE) {
            comparator = Comparator.comparingDouble(MediaDetails::getAverageRating);
        } else {
            comparator = Comparator.comparing(details -> details.getMedia().getTitle(), String.CASE_INSENSITIVE_ORDER);
        }

        if (criteria.getSortDirection() == MediaSearchCriteria.SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return comparator.thenComparing(details -> details.getMedia().getTitle(), String.CASE_INSENSITIVE_ORDER);
    }

    private boolean matchesBasicFilters(Media media, MediaSearchCriteria criteria) {
        if (criteria.getTitleQuery() != null) {
            String query = criteria.getTitleQuery().toLowerCase();
            if (media.getTitle() == null || !media.getTitle().toLowerCase().contains(query)) {
                return false;
            }
        }

        if (criteria.getMediaType() != null) {
            if (media.getMediaType() == null
                    || !normalize(media.getMediaType()).equals(normalize(criteria.getMediaType()))) {
                return false;
            }
        }

        if (criteria.getGenre() != null) {
            String wanted = normalize(criteria.getGenre());
            boolean found = media.getGenres().stream()
                    .map(this::normalize)
                    .anyMatch(wanted::equals);
            if (!found) {
                return false;
            }
        }

        if (criteria.getReleaseYear() != null) {
            if (!Objects.equals(media.getReleaseYear(), criteria.getReleaseYear())) {
                return false;
            }
        }

        if (criteria.getAgeRestriction() != null) {
            if (media.getAgeRestriction() == null
                    || !normalize(media.getAgeRestriction()).equals(normalize(criteria.getAgeRestriction()))) {
                return false;
            }
        }

        return true;
    }

    private boolean passesRatingThreshold(RatingSummary summary, Double minimumRating) {
        if (minimumRating == null) {
            return true;
        }
        return summary != null && summary.getAverageScore() >= minimumRating;
    }

    private Map<Integer, RatingSummary> summariesFor(List<Media> mediaList) {
        if (mediaList.isEmpty()) {
            return Map.of();
        }
        List<Integer> ids = mediaList.stream()
                .map(Media::getId)
                .collect(Collectors.toList());
        return ratingRepository.summarizeByMediaIds(ids).stream()
                .collect(Collectors.toMap(RatingSummary::getMediaId, summary -> summary));
    }

    private MediaDetails buildDetails(Media media,
                                      RatingSummary summary,
                                      int userId,
                                      boolean includeRatings) {
        return buildDetails(media, summary, userId, includeRatings, includeRatings ? null : List.of());
    }

    private MediaDetails buildDetails(Media media,
                                      RatingSummary summary,
                                      int userId,
                                      boolean includeRatings,
                                      List<Rating> ratingsOverride) {
        double average = summary != null ? summary.getAverageScore() : 0.0;
        int ratingCount = summary != null ? summary.getRatingCount() : 0;
        int favoritesCount = favoriteRepository.countFavoritesForMedia(media.getId());
        boolean favorite = userId > 0 && favoriteRepository.isFavorite(userId, media.getId());
        List<Rating> ratings = includeRatings
                ? (ratingsOverride != null ? ratingsOverride : ratingRepository.findByMediaId(media.getId()))
                : List.of();
        return MediaDetails.of(media, average, ratingCount, favoritesCount, favorite, ratings);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean intersects(List<String> genres, Set<String> preferred) {
        for (String genre : genres) {
            if (preferred.contains(normalize(genre))) {
                return true;
            }
        }
        return false;
    }

    private Set<String> topKeys(Map<String, Integer> scores, int limit) {
        if (scores.isEmpty() || limit <= 0) {
            return Set.of();
        }
        return scores.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean isValid(Media media) {
        if (media == null) {
            return false;
        }
        if (media.getTitle() == null || media.getTitle().isBlank()) {
            return false;
        }
        if (media.getMediaType() == null || media.getMediaType().isBlank()) {
            return false;
        }
        if (media.getCreatedByUserId() <= 0) {
            return false;
        }
        if (media.getReleaseYear() == null
                || media.getReleaseYear() < MIN_RELEASE_YEAR
                || media.getReleaseYear() > MAX_RELEASE_YEAR) {
            return false;
        }
        if (media.getAgeRestriction() == null || media.getAgeRestriction().isBlank()) {
            return false;
        }
        if (media.getGenres().isEmpty()) {
            return false;
        }
        return true;
    }

    private record Candidate(MediaDetails details, int score, double average, int ratingCount) {
    }
}
