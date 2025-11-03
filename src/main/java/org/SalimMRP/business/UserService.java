package org.SalimMRP.business;

import org.SalimMRP.persistence.models.User;

// Schnittstelle für Benutzer-Funktionen, damit Controller unabhängig von der Implementierung bleiben.
public interface UserService {

    boolean register(User user);

    String login(String username, String password);

    boolean isTokenValid(String token);

    User getUserByToken(String token);

    User findByUsername(String username);

    User findById(int id);
}
