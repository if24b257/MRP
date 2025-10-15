package org.SalimMRP.business.auth;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTokenService implements TokenService {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    @Override
    public String issueToken(String username) {
        Objects.requireNonNull(username, "username must not be null");
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
