package org.SalimMRP.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class AuthUtil {

    //Token erzeugen -> z. B. "salim-mrpToken-550e8400-e29b-41d4-a716-446655440000"
    public static String generateToken(String username) {
        // UUID v4 (zufällig, funktioniert in allen Java-Versionen)
        UUID uuid = UUID.randomUUID();
        return username + "-mrpToken-" + uuid.toString();
    }

    //Passwort-Hashing mit SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }

    //Passwort-Überprüfung (erneut hashen und vergleichen)
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        String hashOfInput = hashPassword(plainPassword);
        return hashOfInput.equals(hashedPassword);
    }
}
