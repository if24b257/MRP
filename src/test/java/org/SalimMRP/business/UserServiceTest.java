package org.SalimMRP.business;

import org.SalimMRP.business.auth.InMemoryTokenService;
import org.SalimMRP.business.auth.PasswordHasher;
import org.SalimMRP.business.auth.Sha256PasswordHasher;
import org.SalimMRP.business.auth.TokenService;
import org.SalimMRP.persistence.UserRepository;
import org.SalimMRP.persistence.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private InMemoryUserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        PasswordHasher passwordHasher = new Sha256PasswordHasher();
        TokenService tokenService = new InMemoryTokenService();
        userService = new DefaultUserService(userRepository, passwordHasher, tokenService);
    }

    @Test
    @DisplayName("register stores hashed password and assigns id")
    void registerStoresHashedPassword() {
        User user = new User("alice", "plain");

        assertTrue(userService.register(user));

        User stored = userRepository.getStoredUser("alice");
        assertNotNull(stored);
        assertNotEquals("plain", stored.getPassword(), "Password should be hashed");
        assertTrue(stored.getId() > 0, "User should receive generated id");
    }

    @Test
    @DisplayName("register rejects duplicate usernames")
    void registerRejectsDuplicates() {
        assertTrue(userService.register(new User("bob", "pw1")));
        assertFalse(userService.register(new User("bob", "pw2")));
    }

    @Test
    @DisplayName("register rejects blank username or password")
    void registerRejectsBlankInput() {
        assertFalse(userService.register(new User("", "pw")));
        assertFalse(userService.register(new User("charlie", "")));
    }

    @Test
    @DisplayName("login returns token for valid credentials")
    void loginSucceedsWithCorrectCredentials() {
        userService.register(new User("diana", "pass123"));

        String token = userService.login("diana", "pass123");

        assertNotNull(token);
        assertTrue(userService.isTokenValid(token));
    }

    @Test
    @DisplayName("login fails for non-existent user")
    void loginFailsForUnknownUser() {
        assertNull(userService.login("ghost", "pw"));
    }

    @Test
    @DisplayName("login fails when password does not match")
    void loginFailsForWrongPassword() {
        userService.register(new User("edgar", "secret"));

        assertNull(userService.login("edgar", "wrong"));
    }

    @Test
    @DisplayName("Token validation fails for unknown token")
    void tokenValidationFailsForUnknownToken() {
        assertFalse(userService.isTokenValid("invalid-token"));
    }

    @Test
    @DisplayName("getUserByToken looks up user from token store")
    void getUserByTokenReturnsUser() {
        userService.register(new User("irene", "pw"));
        String token = userService.login("irene", "pw");

        User user = userService.getUserByToken(token);

        assertNotNull(user);
        assertEquals("irene", user.getUsername());
    }

    @Test
    @DisplayName("findByUsername returns user copy")
    void findByUsernameReturnsCopy() {
        userService.register(new User("frank", "pw"));

        User stored = userRepository.getStoredUser("frank");
        User found = userService.findByUsername("frank");

        assertNotSame(stored, found, "Service should not expose internal storage");
        assertEquals(stored.getUsername(), found.getUsername());
    }

    private static class InMemoryUserRepository implements UserRepository {
        private final Map<String, User> storage = new HashMap<>();
        private int nextId = 1;

        @Override
        public boolean save(User user) {
            if (user == null || storage.containsKey(user.getUsername())) {
                return false;
            }
            User stored = cloneUser(user);
            stored.setId(nextId++);
            storage.put(stored.getUsername(), stored);
            return true;
        }

        @Override
        public User findByUsername(String username) {
            User existing = storage.get(username);
            if (existing == null) {
                return null;
            }
            return cloneUser(existing);
        }

        User getStoredUser(String username) {
            return storage.get(username);
        }

        private User cloneUser(User source) {
            return new User(
                    source.getId(),
                    source.getUsername(),
                    source.getPassword()
            );
        }
    }
}
