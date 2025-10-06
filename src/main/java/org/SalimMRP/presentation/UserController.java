package org.SalimMRP.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.SalimMRP.business.UserService;
import org.SalimMRP.persistence.models.User;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UserController {
    //nimmt HTTP Anfragen entgegen
    //ruft Businesslogik im Userservice auf
    //gibt passende JSON responses & HTTP codes zurück

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final UserService userService = new UserService();

    public static void registerRoutes(HttpServer server) {
        server.createContext("/api/users/register", new RegisterHandler());
        server.createContext("/api/users/login", new LoginHandler());
    }

    //Handler für Registrierung
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            User user = mapper.readValue(exchange.getRequestBody(), User.class);
            boolean success = userService.register(user);

            if (success) {
                sendResponse(exchange, 201, "User registered successfully.");
            } else {
                sendResponse(exchange, 400, "Username already exists.");
            }
        }
    }

    //Handler für Login
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            Map<String, String> credentials = mapper.readValue(exchange.getRequestBody(), Map.class);
            String username = credentials.get("Username");
            String password = credentials.get("Password");

            String token = userService.login(username, password);

            if (token != null) {
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                sendJsonResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 401, "Invalid username or password.");
            }
        }
    }

    //Hilfsfunktionen
    private static void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.sendResponseHeaders(statusCode, message.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String json = mapper.writeValueAsString(response);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }
}
