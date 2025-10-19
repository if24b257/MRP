package org.SalimMRP.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.SalimMRP.persistence.models.Rating;
import org.SalimMRP.persistence.models.User;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

// Verarbeitet alle Anfragen zu /api/ratings, inklusive Likes und Moderation.
class RatingHandler implements HttpHandler {

    private final RatingController ratingController;

    RatingHandler(RatingController ratingController) {
        this.ratingController = ratingController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = requireUser(exchange);
        if (user == null) {
            return;
        }

        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (parts.length >= 4 && Objects.equals(parts[3], "media")) {
            handleMediaScoped(exchange, method, parts, user);
            return;
        }

        handleRatingScoped(exchange, method, parts, user);
    }

    private void handleMediaScoped(HttpExchange exchange, String method, String[] parts, User user) throws IOException {
        if (parts.length < 5) {
            sendResponse(exchange, 404, "Not found");
            return;
        }
        Integer mediaId = parsePositiveInt(parts[4]);
        if (mediaId == null) {
            sendResponse(exchange, 400, "Invalid media id");
            return;
        }

        switch (method) {
            case "GET" -> handleListRatings(exchange, mediaId, user);
            case "POST" -> handleCreateRating(exchange, mediaId, user);
            default -> sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleRatingScoped(HttpExchange exchange, String method, String[] parts, User user) throws IOException {
        if (parts.length < 4) {
            sendResponse(exchange, 404, "Not found");
            return;
        }

        Integer ratingId = parsePositiveInt(parts[3]);
        if (ratingId == null) {
            sendResponse(exchange, 400, "Invalid rating id");
            return;
        }

        if (parts.length == 4) {
            switch (method) {
                case "PUT" -> handleUpdateRating(exchange, ratingId, user);
                case "DELETE" -> handleDeleteRating(exchange, ratingId, user);
                default -> sendResponse(exchange, 405, "Method not allowed");
            }
            return;
        }

        if (parts.length == 5 && Objects.equals(parts[4], "confirm") && "POST".equals(method)) {
            handleConfirmComment(exchange, ratingId, user);
            return;
        }

        if (parts.length == 5 && Objects.equals(parts[4], "likes")) {
            if ("POST".equals(method)) {
                handleLike(exchange, ratingId, user);
                return;
            }
            if ("DELETE".equals(method)) {
                handleUnlike(exchange, ratingId, user);
                return;
            }
        }

        sendResponse(exchange, 404, "Not found");
    }

    private void handleListRatings(HttpExchange exchange, int mediaId, User user) throws IOException {
        List<Rating> ratings = ratingController.getRatingService().getRatingsForMedia(mediaId);
        List<RatingResponse> response = ratings.stream()
                .map(r -> RatingResponse.from(r, user.getId()))
                .toList();
        sendJsonResponse(exchange, 200, response);
    }

    private void handleCreateRating(HttpExchange exchange, int mediaId, User user) throws IOException {
        RatingRequest request = readRequest(exchange, RatingRequest.class);
        if (request == null || !request.hasValidStar()) {
            sendResponse(exchange, 400, "Invalid rating payload");
            return;
        }

        Rating rating = new Rating();
        rating.setMediaId(mediaId);
        rating.setUserId(user.getId());
        rating.setStarValue(request.starValue);
        rating.setComment(request.comment);
        rating.setCommentConfirmed(false);
        rating.setCreatedAt(Instant.now());

        Rating created = ratingController.getRatingService().createRating(rating);
        if (created == null) {
            sendResponse(exchange, 400, "Unable to create rating");
            return;
        }
        sendJsonResponse(exchange, 201, RatingResponse.from(created, user.getId()));
    }

    private void handleUpdateRating(HttpExchange exchange, int ratingId, User user) throws IOException {
        RatingRequest request = readRequest(exchange, RatingRequest.class);
        if (request == null || !request.hasValidStar()) {
            sendResponse(exchange, 400, "Invalid rating payload");
            return;
        }

        Rating rating = new Rating();
        rating.setId(ratingId);
        rating.setStarValue(request.starValue);
        rating.setComment(request.comment);

        boolean updated = ratingController.getRatingService().updateRating(rating, user.getId());
        if (!updated) {
            sendResponse(exchange, 400, "Unable to update rating");
            return;
        }

        Rating refreshed = ratingController.getRatingService().getRatingById(ratingId);
        sendJsonResponse(exchange, 200, RatingResponse.from(refreshed, user.getId()));
    }

    private void handleDeleteRating(HttpExchange exchange, int ratingId, User user) throws IOException {
        boolean deleted = ratingController.getRatingService().deleteRating(ratingId, user.getId());
        if (!deleted) {
            sendResponse(exchange, 400, "Unable to delete rating");
            return;
        }
        sendResponse(exchange, 204, "");
    }

    private void handleConfirmComment(HttpExchange exchange, int ratingId, User user) throws IOException {
        boolean confirmed = ratingController.getRatingService().confirmComment(ratingId, user.getId());
        if (!confirmed) {
            sendResponse(exchange, 400, "Unable to confirm comment");
            return;
        }
        Rating refreshed = ratingController.getRatingService().getRatingById(ratingId);
        sendJsonResponse(exchange, 200, RatingResponse.from(refreshed, user.getId()));
    }

    private void handleLike(HttpExchange exchange, int ratingId, User user) throws IOException {
        boolean liked = ratingController.getRatingService().likeRating(ratingId, user.getId());
        if (!liked) {
            sendResponse(exchange, 400, "Unable to like rating");
            return;
        }
        Rating refreshed = ratingController.getRatingService().getRatingById(ratingId);
        sendJsonResponse(exchange, 200, RatingResponse.from(refreshed, user.getId()));
    }

    private void handleUnlike(HttpExchange exchange, int ratingId, User user) throws IOException {
        boolean unliked = ratingController.getRatingService().unlikeRating(ratingId, user.getId());
        if (!unliked) {
            sendResponse(exchange, 400, "Unable to remove like");
            return;
        }
        Rating refreshed = ratingController.getRatingService().getRatingById(ratingId);
        sendJsonResponse(exchange, 200, RatingResponse.from(refreshed, user.getId()));
    }

    private <T> T readRequest(HttpExchange exchange, Class<T> type) {
        try {
            return ratingController.getMapper().readValue(exchange.getRequestBody(), type);
        } catch (IOException e) {
            return null;
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        byte[] body = toJson(payload);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private byte[] toJson(Object payload) throws JsonProcessingException {
        String json = ratingController.getMapper().writeValueAsString(payload);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    private User requireUser(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendResponse(exchange, 401, "Missing or invalid Authorization header");
            return null;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (!ratingController.getUserService().isTokenValid(token)) {
            sendResponse(exchange, 401, "Invalid or expired token");
            return null;
        }

        User user = ratingController.getUserService().getUserByToken(token);
        if (user == null) {
            sendResponse(exchange, 401, "Unknown user for token");
        }
        return user;
    }

    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static class RatingRequest {
        public Integer starValue;
        public String comment;

        boolean hasValidStar() {
            return starValue != null && starValue >= 1 && starValue <= 5;
        }
    }

    private static class RatingResponse {
        public int id;
        public int mediaId;
        public int userId;
        public int starValue;
        public String comment;
        public boolean commentConfirmed;
        public Instant createdAt;
        public int likes;
        public boolean likedByCurrentUser;
        public boolean ownedByCurrentUser;

        static RatingResponse from(Rating rating, int currentUserId) {
            RatingResponse response = new RatingResponse();
            response.id = rating.getId();
            response.mediaId = rating.getMediaId();
            response.userId = rating.getUserId();
            response.starValue = rating.getStarValue();
            response.commentConfirmed = rating.isCommentConfirmed();
            response.ownedByCurrentUser = rating.getUserId() == currentUserId;
            response.comment = response.ownedByCurrentUser || rating.isCommentConfirmed() ? rating.getComment() : null;
            response.createdAt = rating.getCreatedAt();
            response.likes = rating.getLikedByUserIds().size();
            response.likedByCurrentUser = rating.getLikedByUserIds().contains(currentUserId);
            return response;
        }
    }
}
