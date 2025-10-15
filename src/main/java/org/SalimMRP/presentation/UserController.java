package org.SalimMRP.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.SalimMRP.business.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class UserController {
    //nimmt HTTP Anfragen entgegen
    //ruft Businesslogik im Userservice auf
    //gibt passende JSON responses & HTTP codes zurück

    private final ObjectMapper mapper;
    private final UserService userService;

    public UserController(UserService userService, ObjectMapper mapper) {
        this.userService = Objects.requireNonNull(userService, "userService must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public UserService getUserService() {
        return userService;
    }

    public void registerRoutes(HttpServer server) {
        server.createContext("/api/users/register", new RegisterHandler(this));
        server.createContext("/api/users/login", new LoginHandler(this));
    }

    //Hilfsfunktionen
    public void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.sendResponseHeaders(statusCode, message.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String json = mapper.writeValueAsString(response);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }
}
