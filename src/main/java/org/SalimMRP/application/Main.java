package org.SalimMRP.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import org.SalimMRP.business.DefaultMediaService;
import org.SalimMRP.business.DefaultRatingService;
import org.SalimMRP.business.DefaultUserService;
import org.SalimMRP.business.MediaService;
import org.SalimMRP.business.RatingService;
import org.SalimMRP.business.UserService;
import org.SalimMRP.business.auth.InMemoryTokenService;
import org.SalimMRP.business.auth.PasswordHasher;
import org.SalimMRP.business.auth.Sha256PasswordHasher;
import org.SalimMRP.business.auth.TokenService;
import org.SalimMRP.persistence.ConnectionProvider;
import org.SalimMRP.persistence.Database;
import org.SalimMRP.persistence.JdbcMediaRepository;
import org.SalimMRP.persistence.JdbcRatingRepository;
import org.SalimMRP.persistence.JdbcUserRepository;
import org.SalimMRP.persistence.MediaRepository;
import org.SalimMRP.persistence.RatingRepository;
import org.SalimMRP.persistence.UserRepository;
import org.SalimMRP.presentation.MediaController;
import org.SalimMRP.presentation.RatingController;
import org.SalimMRP.presentation.UserController;

import java.io.IOException;
import java.net.InetSocketAddress;

// Startpunkt der Anwendung. Stellt alle benötigten Komponenten zusammen,
// richtet die HTTP-Routen ein und startet anschließend den eingebauten HTTP-Server auf Port 8080.
public class Main {

    public static void main(String[] args) {
        try {
            int port = 8080;

            // HttpServer.create erzeugt einen einfachen HTTP-Server, der auf dem angegebenen Port lauscht.
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            System.out.println("Starting Media Ratings Platform server on port " + port + "...");

            // Aufbau der Infrastruktur: Datenbank, Repositories und Services.
            ConnectionProvider connectionProvider = Database.fromDefaults();
            UserRepository userRepository = new JdbcUserRepository(connectionProvider);
            MediaRepository mediaRepository = new JdbcMediaRepository(connectionProvider);
            RatingRepository ratingRepository = new JdbcRatingRepository(connectionProvider);

            PasswordHasher passwordHasher = new Sha256PasswordHasher();
            TokenService tokenService = new InMemoryTokenService();

            UserService userService = new DefaultUserService(userRepository, passwordHasher, tokenService);
            MediaService mediaService = new DefaultMediaService(mediaRepository);
            RatingService ratingService = new DefaultRatingService(ratingRepository, mediaRepository);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Controller registrieren ihre Endpunkte beim Server.
            UserController userController = new UserController(userService, mapper);
            userController.registerRoutes(server);

            MediaController mediaController = new MediaController(mediaService, userService, mapper);
            mediaController.registerRoutes(server);

            RatingController ratingController = new RatingController(ratingService, userService, mapper);
            ratingController.registerRoutes(server);

            // Der Server arbeitet mit dem Standard-Executor und läuft anschließend dauerhaft.
            server.setExecutor(null);
            server.start();

            System.out.println("Server started successfully at http://localhost:" + port);
            System.out.println("Press CTRL + C to stop the server.");

        } catch (IOException e) {
            System.err.println("Error starting HTTP server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
