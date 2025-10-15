package org.SalimMRP.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.SalimMRP.business.DefaultMediaService;
import org.SalimMRP.business.DefaultUserService;
import org.SalimMRP.business.MediaService;
import org.SalimMRP.business.UserService;
import org.SalimMRP.business.auth.InMemoryTokenService;
import org.SalimMRP.business.auth.PasswordHasher;
import org.SalimMRP.business.auth.Sha256PasswordHasher;
import org.SalimMRP.business.auth.TokenService;
import org.SalimMRP.persistence.ConnectionProvider;
import org.SalimMRP.persistence.Database;
import org.SalimMRP.persistence.JdbcMediaRepository;
import org.SalimMRP.persistence.JdbcUserRepository;
import org.SalimMRP.persistence.MediaRepository;
import org.SalimMRP.persistence.UserRepository;
import org.SalimMRP.presentation.MediaController;
import org.SalimMRP.presentation.UserController;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        try {
            int port = 8080;
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            System.out.println("Starting Media Ratings Platform server on port " + port + "...");

            ConnectionProvider connectionProvider = Database.fromDefaults();
            UserRepository userRepository = new JdbcUserRepository(connectionProvider);
            MediaRepository mediaRepository = new JdbcMediaRepository(connectionProvider);

            PasswordHasher passwordHasher = new Sha256PasswordHasher();
            TokenService tokenService = new InMemoryTokenService();

            UserService userService = new DefaultUserService(userRepository, passwordHasher, tokenService);
            MediaService mediaService = new DefaultMediaService(mediaRepository);

            ObjectMapper mapper = new ObjectMapper();

            UserController userController = new UserController(userService, mapper);
            userController.registerRoutes(server);

            MediaController mediaController = new MediaController(mediaService, userService, mapper);
            mediaController.registerRoutes(server);

            //Server starten
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
