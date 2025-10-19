package org.SalimMRP.business.auth;

// Kapselt die Logik zum Passwort-Hashing, damit verschiedene Verfahren austauschbar sind.
public interface PasswordHasher {

    // Berechnet einen Hash-Wert für den übergebenen Klartext.
    String hash(String plainText);

    // Vergleicht, ob der Klartext zum gespeicherten Hash passt.
    boolean matches(String plainText, String hash);
}
