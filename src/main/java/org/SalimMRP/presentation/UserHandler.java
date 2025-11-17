package org.SalimMRP.presentation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.SalimMRP.business.dto.LeaderboardEntry;
import org.SalimMRP.business.dto.MediaDetails;
import org.SalimMRP.business.dto.UserProfile;
import org.SalimMRP.persistence.models.Media;
import org.SalimMRP.persistence.models.Rating;
import org.SalimMRP.persistence.models.User;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Behandelt geschützte /api/users-Anfragen (Profile, Favoriten, Leaderboard).
class UserHandler implements HttpHandler {

    private final UserController userController;

    UserHandler(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User authUser = userController.authenticate(exchange);
        if (authUser == null) {
            return;
        }

        List<String> segments = Arrays.stream(exchange.getRequestURI().getPath().split("/"))
                .filter(segment -> !segment.isBlank())
                .toList();

        if (segments.size() < 2 || !"api".equalsIgnoreCase(segments.get(0)) || !"users".equalsIgnoreCase(segments.get(1))) {
            userController.sendResponse(exchange, 404, "Not found");
            return;
        }

        if (segments.size() == 2) {
            userController.sendResponse(exchange, 404, "Not found");
            return;
        }

        if ("leaderboard".equalsIgnoreCase(segments.get(2))) {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                userController.sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }
            handleLeaderboard(exchange);
            return;
        }

        String requestedUser = decode(segments.get(2));
        if (!requestedUser.equalsIgnoreCase(authUser.getUsername())) {
            userController.sendResponse(exchange, 403, "You can only access your own profile");
            return;
        }

        if (segments.size() == 3) {
            userController.sendResponse(exchange, 404, "Not found");
            return;
        }

        String resource = segments.get(3).toLowerCase();
        switch (resource) {
            case "profile" -> handleProfile(exchange, authUser);
            case "ratings" -> handleRatings(exchange, authUser);
            case "favorites" -> handleFavorites(exchange, authUser);
            default -> userController.sendResponse(exchange, 404, "Not found");
        }
    }

    private void handleProfile(HttpExchange exchange, User authUser) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            userController.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        UserProfile profile = userController.getProfileService().buildProfile(authUser.getId());
        if (profile == null) {
            userController.sendResponse(exchange, 404, "Profile not found");
            return;
        }
        userController.sendJsonResponse(exchange, 200, profile);
    }

    private void handleRatings(HttpExchange exchange, User authUser) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            userController.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        List<Rating> ratings = userController.getProfileService().ratingHistory(authUser.getId());
        Map<Integer, Media> mediaCache = new HashMap<>();
        List<RatingHistoryResponse> response = new ArrayList<>();

        for (Rating rating : ratings) {
            Media media = mediaCache.computeIfAbsent(rating.getMediaId(), userController.getMediaService()::getMediaById);
            String title = media != null ? media.getTitle() : null;
            response.add(RatingHistoryResponse.from(rating, title));
        }
        userController.sendJsonResponse(exchange, 200, response);
    }

    private void handleFavorites(HttpExchange exchange, User authUser) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            userController.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        List<MediaDetails> favorites = userController.getProfileService().favoriteMedia(authUser.getId());
        userController.sendJsonResponse(exchange, 200, favorites);
    }

    private void handleLeaderboard(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        int limit = 10;
        if (params.containsKey("limit")) {
            try {
                limit = Integer.parseInt(params.get("limit"));
            } catch (NumberFormatException ignored) {
            }
        }
        List<LeaderboardEntry> entries = userController.getProfileService().leaderboard(limit);
        userController.sendJsonResponse(exchange, 200, entries);
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = decode(pair.substring(0, idx));
                String value = decode(pair.substring(idx + 1));
                params.put(key.toLowerCase(), value);
            } else {
                params.put(decode(pair).toLowerCase(), "");
            }
        }
        return params;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private record RatingHistoryResponse(int id,
                                         int mediaId,
                                         String mediaTitle,
                                         int starValue,
                                         String comment,
                                         boolean commentConfirmed,
                                         Instant createdAt,
                                         int likes) {

        static RatingHistoryResponse from(Rating rating, String mediaTitle) {
            return new RatingHistoryResponse(
                    rating.getId(),
                    rating.getMediaId(),
                    mediaTitle,
                    rating.getStarValue(),
                    rating.getComment(),
                    rating.isCommentConfirmed(),
                    rating.getCreatedAt(),
                    rating.getLikedByUserIds().size()
            );
        }
    }
}
