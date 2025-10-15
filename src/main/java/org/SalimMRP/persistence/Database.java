package org.SalimMRP.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class Database implements ConnectionProvider {

    private final String url;
    private final String user;
    private final String password;

    public Database(String url, String user, String password) {
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.password = Objects.requireNonNull(password, "password must not be null");
        loadDriver();
    }

    private void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC Driver not found. Include it in your classpath!", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static Database fromDefaults() {
        return new Database("jdbc:postgresql://localhost:5433/mrp_db", "postgres", "postgres");
    }
}
