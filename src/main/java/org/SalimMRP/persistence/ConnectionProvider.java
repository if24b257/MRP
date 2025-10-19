package org.SalimMRP.persistence;

import java.sql.Connection;
import java.sql.SQLException;

// Abstraktion für die Beschaffung einer JDBC-Verbindung, damit Tests eigene Provider nutzen können.
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
