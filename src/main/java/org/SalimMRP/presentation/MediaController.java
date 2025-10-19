package org.SalimMRP.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.SalimMRP.business.MediaService;
import org.SalimMRP.business.UserService;

import java.util.Objects;

// Einstiegspunkt für alle Media-Endpunkte mit Referenzen auf Services und JSON-Mapper.
public class MediaController {

    private final ObjectMapper mapper;
    private final MediaService mediaService;
    private final UserService userService;

    public MediaController(MediaService mediaService, UserService userService, ObjectMapper mapper) {
        this.mediaService = Objects.requireNonNull(mediaService, "mediaService must not be null");
        this.userService = Objects.requireNonNull(userService, "userService must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public MediaService getMediaService() {
        return mediaService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void registerRoutes(HttpServer server) {
        server.createContext("/api/media", new MediaHandler(this));
    }

}
