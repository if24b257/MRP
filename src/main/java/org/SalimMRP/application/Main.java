package org.SalimMRP.application;

import com.sun.net.httpserver.HttpServer;
import org.SalimMRP.presentation.UserController;
import org.SalimMRP.presentation.MediaController;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        try {
            int port = 8080;
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            System.out.println("Starting Media Ratings Platform server on port " + port + "...");

            // Controller registrieren (Routes)
            UserController.registerRoutes(server);
            MediaController.registerRoutes(server);

            // Server starten
            server.setExecutor(null); // Default-Executor
            server.start();

            System.out.println("Server started successfully at http://localhost:" + port);
            System.out.println("Press CTRL + C to stop the server.");

        } catch (IOException e) {
            System.err.println("Error starting HTTP server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
