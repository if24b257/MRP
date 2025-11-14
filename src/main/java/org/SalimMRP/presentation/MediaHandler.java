package org.SalimMRP.presentation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.SalimMRP.business.dto.MediaDetails;
import org.SalimMRP.business.dto.MediaSearchCriteria;
import org.SalimMRP.persistence.models.Media;
import org.SalimMRP.persistence.models.Rating;
import org.SalimMRP.persistence.models.User;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Zentrale Handler-Klasse für alle /api/media-Anfragen inklusive Suche, Favoriten und Empfehlungen.
class MediaHandler implements HttpHandler {
    private final MediaController mediaController;

    MediaHandler(MediaController mediaController) {
        this.mediaController = mediaController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = requireUser(exchange);
        if (user == null) {
            return;
        }

        List<String> segments = Arrays.stream(exchange.getRequestURI().getPath().split("/"))
                .filter(segment -> !segment.isBlank())
                .toList();

        if (segments.size() < 2
                || !"api".equalsIgnoreCase(segments.get(0))
                || !"media".equalsIgnoreCase(segments.get(1))) {
            sendResponse(exchange, 404, "Not found");
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();

        if (segments.size() == 2) {
            handleRoot(exchange, method, user);
            return;
        }

        String third = segments.get(2);
        if ("recommendations".equalsIgnoreCase(third)) {
            handleRecommendations(exchange, method, user);
            return;
        }

        Integer mediaId = parseId(third);
        if (mediaId == null) {
            sendResponse(exchange, 400, "Invalid media id");
            return;
        }

        if (segments.size() == 3) {
            handleSingleMedia(exchange, method, user, mediaId);
            return;
        }

        String action = segments.get(3).toLowerCase();
        if ("favorites".equals(action)) {
            handleFavorites(exchange, method, user, mediaId);
            return;
        }

        sendResponse(exchange, 404, "Not found");
    }

    private void handleRoot(HttpExchange exchange, String method, User user) throws IOException {
        switch (method) {
            case "GET" -> {
                MediaSearchCriteria criteria = buildCriteria(exchange.getRequestURI().getQuery());
                List<MediaDetails> details = mediaController.getMediaService().searchMedia(criteria, user.getId());
                List<MediaResponse> response = details.stream()
                        .map(detail -> MediaResponse.from(detail, user.getId(), false))
                        .toList();
                sendJsonResponse(exchange, 200, response);
            }
            case "POST" -> handleCreate(exchange, user);
            default -> sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleRecommendations(HttpExchange exchange, String method, User user) throws IOException {
        if (!"GET".equals(method)) {
            sendResponse(exchange, 405, "Method not allowed");
            return;
        }
        List<MediaDetails> recommendations = mediaController.getMediaService().recommendMedia(user.getId());
        List<MediaResponse> response = recommendations.stream()
                .map(detail -> MediaResponse.from(detail, user.getId(), false))
                .toList();
        sendJsonResponse(exchange, 200, response);
    }

    private void handleCreate(HttpExchange exchange, User user) throws IOException {
        Media media = mediaController.getMapper().readValue(exchange.getRequestBody(), Media.class);
        media.setCreatedByUserId(user.getId());

        if (!mediaController.getMediaService().createMedia(media)) {
            sendResponse(exchange, 400, "Invalid data");
            return;
        }

        MediaDetails details = mediaController.getMediaService().getDetailedMedia(media.getId(), user.getId());
        sendJsonResponse(exchange, 201, MediaResponse.from(details, user.getId(), true));
    }

    private void handleSingleMedia(HttpExchange exchange, String method, User user, int mediaId) throws IOException {
        switch (method) {
            case "GET" -> {
                MediaDetails details = mediaController.getMediaService().getDetailedMedia(mediaId, user.getId());
                if (details == null) {
                    sendResponse(exchange, 404, "Media not found");
                    return;
                }
                sendJsonResponse(exchange, 200, MediaResponse.from(details, user.getId(), true));
            }
            case "PUT" -> handleUpdate(exchange, user, mediaId);
            case "DELETE" -> handleDelete(exchange, user, mediaId);
            default -> sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleUpdate(HttpExchange exchange, User user, int mediaId) throws IOException {
        Media payload = mediaController.getMapper().readValue(exchange.getRequestBody(), Media.class);
        payload.setId(mediaId);

        Media existing = mediaController.getMediaService().getMediaById(mediaId);
        if (existing == null) {
            sendResponse(exchange, 404, "Media not found");
            return;
        }
        if (existing.getCreatedByUserId() != user.getId()) {
            sendResponse(exchange, 403, "You can only modify your own media entries");
            return;
        }
        payload.setCreatedByUserId(existing.getCreatedByUserId());

        if (!mediaController.getMediaService().updateMedia(payload)) {
            sendResponse(exchange, 400, "Update failed");
            return;
        }

        MediaDetails updated = mediaController.getMediaService().getDetailedMedia(mediaId, user.getId());
        sendJsonResponse(exchange, 200, MediaResponse.from(updated, user.getId(), true));
    }

    private void handleDelete(HttpExchange exchange, User user, int mediaId) throws IOException {
        Media existing = mediaController.getMediaService().getMediaById(mediaId);
        if (existing == null) {
            sendResponse(exchange, 404, "Media not found");
            return;
        }
        if (existing.getCreatedByUserId() != user.getId()) {
            sendResponse(exchange, 403, "You can only delete your own media entries");
            return;
        }

        if (!mediaController.getMediaService().deleteMedia(mediaId)) {
            sendResponse(exchange, 500, "Failed to delete media");
            return;
        }
        sendResponse(exchange, 200, "Media deleted");
    }

    private void handleFavorites(HttpExchange exchange, String method, User user, int mediaId) throws IOException {
        boolean success;
        switch (method) {
            case "POST" -> success = mediaController.getMediaService().addFavorite(mediaId, user.getId());
            case "DELETE" -> success = mediaController.getMediaService().removeFavorite(mediaId, user.getId());
            default -> {
                sendResponse(exchange, 405, "Method not allowed");
                return;
            }
        }

        if (!success) {
            sendResponse(exchange, 400, "Unable to update favorites");
            return;
        }
        MediaDetails details = mediaController.getMediaService().getDetailedMedia(mediaId, user.getId());
        sendJsonResponse(exchange, 200, MediaResponse.from(details, user.getId(), false));
    }

    private MediaSearchCriteria buildCriteria(String query) {
        Map<String, String> params = parseQuery(query);
        MediaSearchCriteria criteria = new MediaSearchCriteria();

        if (params.containsKey("title")) {
            criteria.setTitleQuery(params.get("title"));
        }
        if (params.containsKey("mediatype")) {
            criteria.setMediaType(params.get("mediatype"));
        }
        if (params.containsKey("genre")) {
            criteria.setGenre(params.get("genre"));
        }
        if (params.containsKey("releaseyear")) {
            try {
                criteria.setReleaseYear(Integer.parseInt(params.get("releaseyear")));
            } catch (NumberFormatException ignored) {
            }
        }
        if (params.containsKey("agerestriction")) {
            criteria.setAgeRestriction(params.get("agerestriction"));
        }
        if (params.containsKey("minrating")) {
            try {
                criteria.setMinimumRating(Double.parseDouble(params.get("minrating")));
            } catch (NumberFormatException ignored) {
            }
        }
        if (params.containsKey("sort")) {
            try {
                criteria.setSortField(MediaSearchCriteria.SortField.valueOf(params.get("sort").toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (params.containsKey("direction")) {
            try {
                criteria.setSortDirection(MediaSearchCriteria.SortDirection.valueOf(params.get("direction").toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return criteria;
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }
        for (String part : query.split("&")) {
            int idx = part.indexOf('=');
            if (idx > 0) {
                String key = decode(part.substring(0, idx));
                String value = decode(part.substring(idx + 1));
                params.put(key.toLowerCase(), value);
            } else {
                params.put(decode(part).toLowerCase(), "");
            }
        }
        return params;
    }

    private String decode(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private User requireUser(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendResponse(exchange, 401, "Missing or invalid Authorization header");
            return null;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (!mediaController.getUserService().isTokenValid(token)) {
            sendResponse(exchange, 401, "Invalid or expired token");
            return null;
        }

        User user = mediaController.getUserService().getUserByToken(token);
        if (user == null) {
            sendResponse(exchange, 401, "Unknown user for token");
        }
        return user;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String json = mediaController.getMapper().writeValueAsString(response);
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private Integer parseId(String rawId) {
        try {
            return Integer.parseInt(rawId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static class MediaResponse {
        public int id;
        public String title;
        public String description;
        public String mediaType;
        public Integer releaseYear;
        public String ageRestriction;
        public List<String> genres;
        public int createdByUserId;
        public double averageRating;
        public int ratingCount;
        public int favoritesCount;
        public boolean favoriteForUser;
        public List<RatingView> ratings;

        static MediaResponse from(MediaDetails details, int currentUserId, boolean includeRatings) {
            Media media = details.getMedia();
            MediaResponse response = new MediaResponse();
            response.id = media.getId();
            response.title = media.getTitle();
            response.description = media.getDescription();
            response.mediaType = media.getMediaType();
            response.releaseYear = media.getReleaseYear();
            response.ageRestriction = media.getAgeRestriction();
            response.genres = new ArrayList<>(media.getGenres());
            response.createdByUserId = media.getCreatedByUserId();
            response.averageRating = details.getAverageRating();
            response.ratingCount = details.getRatingCount();
            response.favoritesCount = details.getFavoritesCount();
            response.favoriteForUser = details.isFavoriteForUser();

            if (includeRatings) {
                response.ratings = details.getRatings().stream()
                        .map(rating -> RatingView.from(rating, currentUserId))
                        .toList();
            } else {
                response.ratings = List.of();
            }
            return response;
        }
    }

    private static class RatingView {
        public int id;
        public int mediaId;
        public int userId;
        public int starValue;
        public String comment;
        public boolean commentConfirmed;
        public Instant createdAt;
        public int likes;
        public boolean likedByCurrentUser;

        static RatingView from(Rating rating, int currentUserId) {
            RatingView view = new RatingView();
            view.id = rating.getId();
            view.mediaId = rating.getMediaId();
            view.userId = rating.getUserId();
            view.starValue = rating.getStarValue();
            view.commentConfirmed = rating.isCommentConfirmed();
            view.comment = (rating.getUserId() == currentUserId || rating.isCommentConfirmed())
                    ? rating.getComment()
                    : null;
            view.createdAt = rating.getCreatedAt();
            view.likes = rating.getLikedByUserIds().size();
            view.likedByCurrentUser = rating.getLikedByUserIds().contains(currentUserId);
            return view;
        }
    }
}
