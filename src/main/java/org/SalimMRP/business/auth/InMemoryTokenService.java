package org.SalimMRP.business.auth;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

// Einfache Token-Verwaltung: Tokens werden im Speicher abgelegt und lassen sich direkt nachschlagen.
// Für Lernzwecke ausreichend, für den Produktivbetrieb wäre ein dauerhaftes Backend sinnvoll.
public class InMemoryTokenService implements TokenService {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    @Override
    public String issueToken(String username) {
        Objects.requireNonNull(username, "username must not be null");

        // Das Token wird aus dem Benutzernamen plus einer zeitlich sortierten UUIDv7 gebildet.
        String token = username + "-mrpToken-" + UuidCreator.getTimeOrdered();
        tokens.put(token, username);
        return token;
    }

    @Override
    public boolean isValid(String token) {
        return token != null && tokens.containsKey(token);
    }

    @Override
    public String resolveUsername(String token) {
        return token == null ? null : tokens.get(token);
    }

    @Override
    public void invalidate(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
