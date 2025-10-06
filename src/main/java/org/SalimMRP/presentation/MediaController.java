package org.SalimMRP.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.SalimMRP.business.MediaService;
import org.SalimMRP.persistence.models.Media;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MediaController {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MediaService mediaService = new MediaService();

    public static void registerRoutes(HttpServer server) {
        server.createContext("/api/media", new MediaHandler());
    }

    static class MediaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");

            switch (method) {
                case "GET" -> handleGet(exchange, parts);
                case "POST" -> handlePost(exchange);
                case "PUT" -> handlePut(exchange);
                case "DELETE" -> handleDelete(exchange, parts);
                default -> sendResponse(exchange, 405, "Method not allowed");
            }
        }

        private void handleGet(HttpExchange exchange, String[] parts) throws IOException {
            if (parts.length == 3) {
                List<Media> list = mediaService.getAllMedia();
                sendJsonResponse(exchange, 200, list);
            } else if (parts.length == 4) {
                int id = Integer.parseInt(parts[3]);
                Media media = mediaService.getMediaById(id);
                if (media != null) sendJsonResponse(exchange, 200, media);
                else sendResponse(exchange, 404, "Media not found");
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            Media media = mapper.readValue(exchange.getRequestBody(), Media.class);
            boolean success = mediaService.createMedia(media);
            if (success) sendResponse(exchange, 201, "Media created");
            else sendResponse(exchange, 400, "Invalid data");
        }

        private void handlePut(HttpExchange exchange) throws IOException {
            Media media = mapper.readValue(exchange.getRequestBody(), Media.class);
            boolean success = mediaService.updateMedia(media);
            if (success) sendResponse(exchange, 200, "Media updated");
            else sendResponse(exchange, 400, "Update failed");
        }

        private void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
            if (parts.length == 4) {
                int id = Integer.parseInt(parts[3]);
                boolean success = mediaService.deleteMedia(id);
                if (success) sendResponse(exchange, 200, "Media deleted");
                else sendResponse(exchange, 404, "Not found");
            } else sendResponse(exchange, 400, "Invalid request");
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            exchange.sendResponseHeaders(statusCode, message.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(message.getBytes(StandardCharsets.UTF_8));
            }
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
            String json = mapper.writeValueAsString(response);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, json.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
