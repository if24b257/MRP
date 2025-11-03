package org.SalimMRP.business;

import org.SalimMRP.business.dto.MediaDetails;
import org.SalimMRP.business.dto.MediaSearchCriteria;
import org.SalimMRP.persistence.FavoriteRepository;
import org.SalimMRP.persistence.MediaRepository;
import org.SalimMRP.persistence.RatingRepository;
import org.SalimMRP.persistence.models.Media;
import org.SalimMRP.persistence.models.Rating;
import org.SalimMRP.persistence.models.RatingSummary;
import org.SalimMRP.persistence.models.UserRatingCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Verifiziert die Validierungs-, Such- und Favoritenlogik des DefaultMediaService.
class MediaServiceTest {

    private InMemoryMediaRepository mediaRepository;
    private InMemoryRatingRepository ratingRepository;
    private InMemoryFavoriteRepository favoriteRepository;
    private DefaultMediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaRepository = new InMemoryMediaRepository();
        ratingRepository = new InMemoryRatingRepository();
        favoriteRepository = new InMemoryFavoriteRepository();
        mediaService = new DefaultMediaService(mediaRepository, ratingRepository, favoriteRepository);
    }

    @Test
    @DisplayName("createMedia persists valid media entry with metadata")
    void createMediaPersistsEntity() {
        Media media = buildMedia("Matrix", "Movie", 1);

        assertTrue(mediaService.createMedia(media));
        assertEquals(1, mediaRepository.size());
        assertTrue(media.getId() > 0);
    }

    @Test
    @DisplayName("createMedia rejects missing release year")
    void createMediaRejectsMissingYear() {
        Media media = buildMedia("Title", "Game", 1);
        media.setReleaseYear(null);

        assertFalse(mediaService.createMedia(media));
    }

    @Test
    @DisplayName("searchMedia filters by title and genre")
    void searchMediaFiltersByTitleAndGenre() {
        Media mediaA = buildMedia("Star Wars", "Movie", 1);
        mediaA.setGenres(List.of("Sci-Fi", "Adventure"));
        mediaRepository.save(mediaA);

        Media mediaB = buildMedia("Stardew Valley", "Game", 2);
        mediaB.setGenres(List.of("Simulation"));
        mediaRepository.save(mediaB);

        MediaSearchCriteria criteria = new MediaSearchCriteria();
        criteria.setTitleQuery("star");
        criteria.setGenre("sci-fi");

        List<MediaDetails> result = mediaService.searchMedia(criteria, 99);
        assertEquals(1, result.size());
        assertEquals("Star Wars", result.get(0).getMedia().getTitle());
    }

    @Test
    @DisplayName("searchMedia applies minimum rating filter and sorting by score")
    void searchMediaAppliesMinimumRating() {
        Media mediaA = buildMedia("Movie A", "Movie", 1);
        mediaRepository.save(mediaA);
        ratingRepository.save(ratingFor(mediaA.getId(), 1, 5));
        ratingRepository.save(ratingFor(mediaA.getId(), 2, 4));

        Media mediaB = buildMedia("Movie B", "Movie", 1);
        mediaRepository.save(mediaB);
        ratingRepository.save(ratingFor(mediaB.getId(), 1, 2));

        MediaSearchCriteria criteria = new MediaSearchCriteria();
        criteria.setMinimumRating(4.0);
        criteria.setSortField(MediaSearchCriteria.SortField.SCORE);
        criteria.setSortDirection(MediaSearchCriteria.SortDirection.DESC);

        List<MediaDetails> result = mediaService.searchMedia(criteria, 99);
        assertEquals(1, result.size());
        assertEquals("Movie A", result.get(0).getMedia().getTitle());
        assertEquals(4.5, result.get(0).getAverageRating(), 0.001);
    }

    @Test
    @DisplayName("getDetailedMedia returns ratings and favorite counters")
    void getDetailedMediaReturnsRatings() {
        Media media = buildMedia("Portal", "Game", 1);
        mediaRepository.save(media);

        Rating rating = ratingFor(media.getId(), 2, 5);
        rating.setComment("Great puzzle game");
        ratingRepository.save(rating);

        favoriteRepository.addFavorite(2, media.getId());

        MediaDetails details = mediaService.getDetailedMedia(media.getId(), 2);
        assertNotNull(details);
        assertEquals(1, details.getRatingCount());
        assertEquals(5.0, details.getAverageRating(), 0.001);
        assertEquals(1, details.getRatings().size());
        assertEquals("Great puzzle game", details.getRatings().get(0).getComment());
        assertTrue(details.isFavoriteForUser());
        assertEquals(1, details.getFavoritesCount());
    }

    @Test
    @DisplayName("addFavorite marks entry for user")
    void addFavoriteMarksEntry() {
        Media media = buildMedia("Favorite", "Movie", 1);
        mediaRepository.save(media);

        assertTrue(mediaService.addFavorite(media.getId(), 7));
        assertTrue(mediaService.listFavorites(7).stream()
                .anyMatch(details -> details.getMedia().getId() == media.getId()));
    }

    @Test
    @DisplayName("removeFavorite removes previously stored favorite")
    void removeFavoriteRemovesEntry() {
        Media media = buildMedia("Favorite", "Movie", 1);
        mediaRepository.save(media);
        mediaService.addFavorite(media.getId(), 7);

        assertTrue(mediaService.removeFavorite(media.getId(), 7));
        assertTrue(mediaService.listFavorites(7).isEmpty());
    }

    @Test
    @DisplayName("recommendMedia prefers items with matching genres")
    void recommendMediaPrefersGenres() {
        Media liked = buildMedia("Liked", "Movie", 2);
        liked.setGenres(List.of("Sci-Fi"));
        mediaRepository.save(liked);

        Media candidate = buildMedia("Recommended", "Movie", 5);
        candidate.setGenres(List.of("Sci-Fi", "Adventure"));
        mediaRepository.save(candidate);
        ratingRepository.save(ratingFor(candidate.getId(), 4, 4));

        Media unrelated = buildMedia("Unrelated", "Movie", 6);
        unrelated.setGenres(List.of("Drama"));
        mediaRepository.save(unrelated);
        ratingRepository.save(ratingFor(unrelated.getId(), 4, 5));

        // User highly rates the liked media to establish preferences.
        Rating userRating = ratingFor(liked.getId(), 10, 5);
        ratingRepository.save(userRating);

        List<MediaDetails> recommendation = mediaService.recommendMedia(10);
        assertFalse(recommendation.isEmpty());
        assertEquals("Recommended", recommendation.get(0).getMedia().getTitle());
        assertTrue(recommendation.stream().noneMatch(r -> r.getMedia().getId() == liked.getId()),
                "Own rated media should not be recommended");
    }

    @Test
    @DisplayName("recommendMedia falls back to popular items when no preferences exist")
    void recommendMediaFallsBackToPopular() {
        Media first = buildMedia("Popular One", "Game", 1);
        mediaRepository.save(first);
        ratingRepository.save(ratingFor(first.getId(), 1, 5));

        Media second = buildMedia("Popular Two", "Movie", 2);
        mediaRepository.save(second);
        ratingRepository.save(ratingFor(second.getId(), 1, 4));

        List<MediaDetails> recommendations = mediaService.recommendMedia(42);
        assertEquals(2, recommendations.size());
    }

    private Media buildMedia(String title, String type, int creatorId) {
        Media media = new Media();
        media.setTitle(title);
        media.setDescription("Description for " + title);
        media.setMediaType(type);
        media.setReleaseYear(2010);
        media.setAgeRestriction("PG-13");
        media.setGenres(List.of("Adventure"));
        media.setCreatedByUserId(creatorId);
        return media;
    }

    private Rating ratingFor(int mediaId, int userId, int stars) {
        Rating rating = new Rating();
        rating.setMediaId(mediaId);
        rating.setUserId(userId);
        rating.setStarValue(stars);
        rating.setCreatedAt(Instant.now());
        rating.setCommentConfirmed(true);
        return rating;
    }

    // ---------- Test Doubles ----------

    private static class InMemoryMediaRepository implements MediaRepository {
        private final Map<Integer, Media> storage = new HashMap<>();
        private int nextId = 1;

        @Override
        public boolean save(Media media) {
            if (media == null) {
                return false;
            }
            Media stored = cloneMedia(media);
            stored.setId(nextId++);
            storage.put(stored.getId(), stored);
            media.setId(stored.getId());
            return true;
        }

        @Override
        public List<Media> findAll() {
            return storage.values().stream().map(this::cloneMedia).toList();
        }

        @Override
        public Media findById(int id) {
            Media media = storage.get(id);
            return media == null ? null : cloneMedia(media);
        }

        @Override
        public boolean update(Media media) {
            if (media == null || !storage.containsKey(media.getId())) {
                return false;
            }
            storage.put(media.getId(), cloneMedia(media));
            return true;
        }

        @Override
        public boolean delete(int id) {
            return storage.remove(id) != null;
        }

        int size() {
            return storage.size();
        }

        private Media cloneMedia(Media source) {
            Media clone = new Media();
            clone.setId(source.getId());
            clone.setTitle(source.getTitle());
            clone.setDescription(source.getDescription());
            clone.setMediaType(source.getMediaType());
            clone.setReleaseYear(source.getReleaseYear());
            clone.setAgeRestriction(source.getAgeRestriction());
            clone.setGenres(source.getGenres());
            clone.setCreatedByUserId(source.getCreatedByUserId());
            return clone;
        }
    }

    private static class InMemoryRatingRepository implements RatingRepository {
        private final Map<Integer, Rating> storage = new HashMap<>();
        private int nextId = 1;

        @Override
        public Rating save(Rating rating) {
            Rating stored = cloneRating(rating);
            stored.setId(nextId++);
            storage.put(stored.getId(), stored);
            rating.setId(stored.getId());
            return cloneRating(stored);
        }

        @Override
        public boolean update(Rating rating) {
            if (!storage.containsKey(rating.getId())) {
                return false;
            }
            storage.put(rating.getId(), cloneRating(rating));
            return true;
        }

        @Override
        public boolean delete(int id) {
            return storage.remove(id) != null;
        }

        @Override
        public Rating findById(int id) {
            Rating rating = storage.get(id);
            return rating == null ? null : cloneRating(rating);
        }

        @Override
        public Rating findByMediaIdAndUserId(int mediaId, int userId) {
            return storage.values().stream()
                    .filter(r -> r.getMediaId() == mediaId && r.getUserId() == userId)
                    .findFirst()
                    .map(this::cloneRating)
                    .orElse(null);
        }

        @Override
        public List<Rating> findByMediaId(int mediaId) {
            return storage.values().stream()
                    .filter(r -> r.getMediaId() == mediaId)
                    .map(this::cloneRating)
                    .toList();
        }

        @Override
        public List<Rating> findByUserId(int userId) {
            return storage.values().stream()
                    .filter(r -> r.getUserId() == userId)
                    .map(this::cloneRating)
                    .toList();
        }

        @Override
        public List<RatingSummary> summarizeByMediaIds(List<Integer> mediaIds) {
            List<RatingSummary> summaries = new ArrayList<>();
            for (Integer id : mediaIds) {
                List<Rating> ratings = findByMediaId(id);
                if (ratings.isEmpty()) {
                    continue;
                }
                double average = ratings.stream().mapToInt(Rating::getStarValue).average().orElse(0.0);
                summaries.add(new RatingSummary(id, average, ratings.size()));
            }
            return summaries;
        }

        @Override
        public List<UserRatingCount> findRatingCountsPerUser(int limit) {
            Map<Integer, Long> counts = new HashMap<>();
            for (Rating rating : storage.values()) {
                counts.merge(rating.getUserId(), 1L, Long::sum);
            }
            return counts.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .limit(limit)
                    .map(e -> new UserRatingCount(e.getKey(), e.getValue()))
                    .toList();
        }

        @Override
        public boolean confirmComment(int ratingId) {
            Rating rating = storage.get(ratingId);
            if (rating == null) {
                return false;
            }
            rating.setCommentConfirmed(true);
            return true;
        }

        @Override
        public boolean addLike(int ratingId, int userId) {
            Rating rating = storage.get(ratingId);
            if (rating == null) {
                return false;
            }
            return rating.likeByUser(userId);
        }

        @Override
        public boolean removeLike(int ratingId, int userId) {
            Rating rating = storage.get(ratingId);
            if (rating == null) {
                return false;
            }
            return rating.unlikeByUser(userId);
        }

        @Override
        public Set<Integer> findLikes(int ratingId) {
            Rating rating = storage.get(ratingId);
            return rating == null ? Set.of() : new HashSet<>(rating.getLikedByUserIds());
        }

        private Rating cloneRating(Rating source) {
            Rating clone = new Rating();
            clone.setId(source.getId());
            clone.setMediaId(source.getMediaId());
            clone.setUserId(source.getUserId());
            clone.setStarValue(source.getStarValue());
            clone.setComment(source.getComment());
            clone.setCommentConfirmed(source.isCommentConfirmed());
            clone.setCreatedAt(source.getCreatedAt());
            clone.setLikedByUserIds(new HashSet<>(source.getLikedByUserIds()));
            return clone;
        }
    }

    private static class InMemoryFavoriteRepository implements FavoriteRepository {
        private final Map<Integer, Set<Integer>> favoritesByUser = new HashMap<>();

        @Override
        public boolean addFavorite(int userId, int mediaId) {
            return favoritesByUser
                    .computeIfAbsent(userId, key -> new HashSet<>())
                    .add(mediaId);
        }

        @Override
        public boolean removeFavorite(int userId, int mediaId) {
            Set<Integer> favorites = favoritesByUser.get(userId);
            return favorites != null && favorites.remove(mediaId);
        }

        @Override
        public boolean isFavorite(int userId, int mediaId) {
            return favoritesByUser.getOrDefault(userId, Set.of()).contains(mediaId);
        }

        @Override
        public List<Integer> findMediaIdsByUser(int userId) {
            return new ArrayList<>(favoritesByUser.getOrDefault(userId, Set.of()));
        }

        @Override
        public int countFavoritesForMedia(int mediaId) {
            int count = 0;
            for (Set<Integer> favorites : favoritesByUser.values()) {
                if (favorites.contains(mediaId)) {
                    count++;
                }
            }
            return count;
        }
    }
}
