package org.SalimMRP.business;

import org.SalimMRP.business.auth.PasswordHasher;
import org.SalimMRP.business.auth.TokenService;
import org.SalimMRP.persistence.UserRepository;
import org.SalimMRP.persistence.models.User;

import java.util.Objects;

public class DefaultUserService implements UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public DefaultUserService(UserRepository userRepository,
                              PasswordHasher passwordHasher,
                              TokenService tokenService) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
        this.tokenService = Objects.requireNonNull(tokenService, "tokenService must not be null");
    }

    @Override
    public boolean register(User user) {
        if (user == null
                || user.getUsername() == null || user.getUsername().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {
            return false;
        }

        User existing = userRepository.findByUsername(user.getUsername());
        if (existing != null) {
            return false;
        }

        user.setPassword(passwordHasher.hash(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public String login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        if (!passwordHasher.matches(password, user.getPassword())) {
            return null;
        }

        return tokenService.issueToken(username);
    }

    @Override
    public boolean isTokenValid(String token) {
        return tokenService.isValid(token);
    }

    @Override
    public User getUserByToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String username = tokenService.resolveUsername(token);
        if (username == null) {
            return null;
        }
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return userRepository.findByUsername(username);
    }
}
