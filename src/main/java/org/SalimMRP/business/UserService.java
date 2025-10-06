package org.SalimMRP.business;

import org.SalimMRP.persistence.UserRepository;
import org.SalimMRP.persistence.models.User;
import org.SalimMRP.util.AuthUtil;

import java.util.HashMap;
import java.util.Map;

public class UserService {

    private final UserRepository userRepository = new UserRepository();

    //Tokens im Speicher (später evtl. in DB oder Cache)
    private static final Map<String, String> activeTokens = new HashMap<>();

    //Registrierung eines neuen Users
    public boolean register(User user) {
        //Input prüfen
        if (user.getUsername() == null || user.getPassword() == null
                || user.getUsername().isBlank() || user.getPassword().isBlank()) {
            return false;
        }

        //Existiert der Benutzer bereits?
        User existing = userRepository.findByUsername(user.getUsername());
        if (existing != null) {
            return false; //Benutzername schon vergeben
        }

        //Passwort verschlüsseln
        String hashedPassword = AuthUtil.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        //Speichern in DB
        return userRepository.save(user);
    }

    //Login
    public String login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null; //kein Benutzer gefunden
        }

        //Passwort prüfen
        boolean valid = AuthUtil.verifyPassword(password, user.getPassword());
        if (!valid) {
            return null;
        }

        //Token generieren
        String token = AuthUtil.generateToken(username);
        activeTokens.put(token, username);

        return token;
    }

    //Token prüfen
    public boolean isTokenValid(String token) {
        return activeTokens.containsKey(token);
    }

    //Benutzername zu Token
    public String getUsernameFromToken(String token) {
        return activeTokens.get(token);
    }
}
