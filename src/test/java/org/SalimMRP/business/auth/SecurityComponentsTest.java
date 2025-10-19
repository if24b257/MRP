package org.SalimMRP.business.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Prüft die zentralen Sicherheitskomponenten (Hashing und Tokens) in Isolation.
class SecurityComponentsTest {

    private final PasswordHasher hasher = new Sha256PasswordHasher();
    private final TokenService tokenService = new InMemoryTokenService();

    @Test
    @DisplayName("Password hasher is deterministic for same input")
    void hashIsDeterministic() {
        String hash1 = hasher.hash("secret");
        String hash2 = hasher.hash("secret");

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Password hasher produces different hashes for distinct inputs")
    void hashDiffersForDifferentInputs() {
        String hash1 = hasher.hash("secret-a");
        String hash2 = hasher.hash("secret-b");

        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Password verification succeeds for matching password")
    void verifyMatches() {
        String hash = hasher.hash("pa55word");

        assertTrue(hasher.matches("pa55word", hash));
    }

    @Test
    @DisplayName("Password verification fails for different password")
    void verifyFails() {
        String hash = hasher.hash("correct horse battery staple");

        assertFalse(hasher.matches("incorrect", hash));
    }

    @Test
    @DisplayName("Issued token embeds username prefix")
    void tokenContainsUsername() {
        String token = tokenService.issueToken("alice");

        assertTrue(token.startsWith("alice-mrpToken-"));
    }

    @Test
    @DisplayName("Issued tokens are unique")
    void tokensAreUnique() {
        String token1 = tokenService.issueToken("bob");
        String token2 = tokenService.issueToken("bob");

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Token service resolves username for valid token")
    void resolvesUsername() {
        String token = tokenService.issueToken("carol");

        assertEquals("carol", tokenService.resolveUsername(token));
    }

    @Test
    @DisplayName("Token service invalidates tokens")
    void invalidatesTokens() {
        String token = tokenService.issueToken("dave");
        assertTrue(tokenService.isValid(token));

        tokenService.invalidate(token);
        assertFalse(tokenService.isValid(token));
    }
}
