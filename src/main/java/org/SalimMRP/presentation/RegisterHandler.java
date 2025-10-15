package org.SalimMRP.presentation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.SalimMRP.persistence.models.User;

import java.io.IOException;

//Handler für Registrierung
class RegisterHandler implements HttpHandler {
    private final UserController userController;

    public RegisterHandler(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            userController.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        User user = userController.getMapper().readValue(exchange.getRequestBody(), User.class);
        boolean success = userController.getUserService().register(user);

        if (success) {
            userController.sendResponse(exchange, 201, "User registered successfully.");
            return;
        }
        userController.sendResponse(exchange, 400, "Could not register user.");
    }
}
