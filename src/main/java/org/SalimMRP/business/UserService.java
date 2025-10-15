package org.SalimMRP.business;

import org.SalimMRP.persistence.models.User;

public interface UserService {

    boolean register(User user);

    String login(String username, String password);

    boolean isTokenValid(String token);

    User getUserByToken(String token);

    User findByUsername(String username);
}
