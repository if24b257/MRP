package org.SalimMRP.business.auth;

// Beschreibt, wie Tokens ausgestellt, geprüft, aufgelöst und invalidiert werden können.
public interface TokenService {

    // Erstellt ein neues Token für den angegebenen Benutzer.
    String issueToken(String username);

    // Prüft, ob das Token aktuell bekannt und gültig ist.
    boolean isValid(String token);

    // Liefert den Benutzernamen zu einem Token, falls verfügbar.
    String resolveUsername(String token);

    // Entfernt ein Token, zum Beispiel beim Logout.
    void invalidate(String token);
}
