package org.SalimMRP.presentation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Bearbeitet Login-Anfragen, prüft die Methode, liest den JSON-Body und liefert bei Erfolg ein Token.
class LoginHandler implements HttpHandler {
    private final UserController userController;

    public LoginHandler(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            userController.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // Username und Passwort werden aus dem JSON-Body gelesen.
        LoginRequest request = userController.getMapper().readValue(exchange.getRequestBody(), LoginRequest.class);
        if (request.username == null || request.password == null) {
            userController.sendResponse(exchange, 400, "Provide username and password.");
            return;
        }

        String token = userController.getUserService().login(request.username, request.password);

        if (token != null) {
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            userController.sendJsonResponse(exchange, 200, response);
        } else {
            userController.sendResponse(exchange, 401, "Invalid username or password.");
        }
    }

    // Minimale Klasse für das Einlesen der JSON-Daten.
    private static class LoginRequest {
        public String username;
        public String password;
    }
}
