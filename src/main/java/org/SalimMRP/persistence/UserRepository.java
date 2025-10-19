package org.SalimMRP.persistence;

import org.SalimMRP.persistence.models.User;

// Schnittstelle für das Speichern und Nachschlagen von Benutzern.
public interface UserRepository {

    boolean save(User user);

    User findByUsername(String username);
}
