package org.SalimMRP.presentation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.SalimMRP.persistence.models.Media;
import org.SalimMRP.persistence.models.User;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Zentrale Handler-Klasse für alle /api/media-Anfragen mit Authentifizierung und Routing.
class MediaHandler implements HttpHandler {
    private final MediaController mediaController;

    public MediaHandler(MediaController mediaController) {
        this.mediaController = mediaController;
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

        // Die Entscheidung, welche Logik greift, basiert auf HTTP-Methode und Pfadlänge.
        switch (method) {
            case "GET" -> handleGet(exchange, parts);
            case "POST" -> handlePost(exchange, user);
            case "PUT" -> handlePut(exchange, user);
            case "DELETE" -> handleDelete(exchange, parts, user);
            default -> sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 3 || (parts.length == 4 && parts[3].isBlank())) {
            List<Media> list = mediaController.getMediaService().getAllMedia();
            sendJsonResponse(exchange, 200, list);
            return;
        }

        // Zugriff auf eine einzelne Ressource /api/media/{id}
        if (parts.length == 4) {
            Integer id = parseId(parts[3]);
            if (id == null) {
                sendResponse(exchange, 400, "Invalid media id");
                return;
            }
            Media media = mediaController.getMediaService().getMediaById(id);
            if (media == null) {
                sendResponse(exchange, 404, "Media not found");
                return;
            }
            sendJsonResponse(exchange, 200, media);
            return;
        }

        sendResponse(exchange, 404, "Not found");
    }

    private void handlePost(HttpExchange exchange, User user) throws IOException {
        Media media = mediaController.getMapper().readValue(exchange.getRequestBody(), Media.class);
        media.setCreatedByUserId(user.getId());

        // Bei gültigen Daten gibt der Service true zurück, sonst 400.
        if (mediaController.getMediaService().createMedia(media)) {
            sendResponse(exchange, 201, "Media created");
            return;
        }
        sendResponse(exchange, 400, "Invalid data");
    }

    private void handlePut(HttpExchange exchange, User user) throws IOException {
        Media media = mediaController.getMapper().readValue(exchange.getRequestBody(), Media.class);
        if (media.getId() <= 0) {
            sendResponse(exchange, 400, "Media id is required for updates");
            return;
        }

        Media existing = mediaController.getMediaService().getMediaById(media.getId());
        if (existing == null) {
            sendResponse(exchange, 404, "Media not found");
            return;
        }

        if (existing.getCreatedByUserId() != user.getId()) {
            sendResponse(exchange, 403, "You can only modify your own media entries");
            return;
        }

        // Die Informationen des ursprünglichen Eintrags bleiben bestehen.
        media.setCreatedByUserId(existing.getCreatedByUserId());

        if (mediaController.getMediaService().updateMedia(media)) {
            sendResponse(exchange, 200, "Media updated");
            return;
        }
        sendResponse(exchange, 400, "Update failed");
    }

    private void handleDelete(HttpExchange exchange, String[] parts, User user) throws IOException {
        if (parts.length != 4) {
            sendResponse(exchange, 400, "Invalid request");
            return;
        }

        Integer id = parseId(parts[3]);
        if (id == null) {
            sendResponse(exchange, 400, "Invalid media id");
            return;
        }

        Media existing = mediaController.getMediaService().getMediaById(id);
        if (existing == null) {
            sendResponse(exchange, 404, "Media not found");
            return;
        }

        if (existing.getCreatedByUserId() != user.getId()) {
            sendResponse(exchange, 403, "You can only delete your own media entries");
            return;
        }

        if (mediaController.getMediaService().deleteMedia(id)) {
            sendResponse(exchange, 200, "Media deleted");
            return;
        }
        sendResponse(exchange, 500, "Failed to delete media");
    }

    private User requireUser(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendResponse(exchange, 401, "Missing or invalid Authorization header");
            return null;
        }

        // Token wird validiert und anschließend dem Benutzer zugeordnet.
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

    // Hilfsmethode, um eine ID aus dem Pfad sicher zu parsen.
    private Integer parseId(String rawId) {
        try {
            return Integer.parseInt(rawId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
