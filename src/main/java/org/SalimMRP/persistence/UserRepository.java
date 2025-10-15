package org.SalimMRP.persistence;

import org.SalimMRP.persistence.models.User;

public interface UserRepository {

    boolean save(User user);

    User findByUsername(String username);
}
