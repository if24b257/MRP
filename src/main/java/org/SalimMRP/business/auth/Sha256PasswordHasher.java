package org.SalimMRP.business.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// Einfache Passwort-Hasher-Implementierung auf Basis von SHA-256 (für echte Systeme besser Algo mit Salt nutzen).
public class Sha256PasswordHasher implements PasswordHasher {

    @Override
    public String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Der Hash wird berechnet und anschließend Base64-kodiert, damit er als Text gespeichert werden kann.
            byte[] hash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not found", e);
        }
    }

    @Override
    public boolean matches(String plainText, String hash) {
        return hash(plainText).equals(hash);
    }
}
