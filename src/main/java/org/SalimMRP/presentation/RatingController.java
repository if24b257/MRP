package org.SalimMRP.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.SalimMRP.business.RatingService;
import org.SalimMRP.business.UserService;

import java.util.Objects;

// Einstiegspunkt für Rating-bezogene Endpunkte.
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;
    private final ObjectMapper mapper;

    public RatingController(RatingService ratingService, UserService userService, ObjectMapper mapper) {
        this.ratingService = Objects.requireNonNull(ratingService, "ratingService must not be null");
        this.userService = Objects.requireNonNull(userService, "userService must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    public RatingService getRatingService() {
        return ratingService;
    }

    public UserService getUserService() {
        return userService;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void registerRoutes(HttpServer server) {
        server.createContext("/api/ratings", new RatingHandler(this));
    }
}
