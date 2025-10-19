package org.SalimMRP.persistence;

import org.SalimMRP.persistence.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// JDBC-gestützte Umsetzung des UserRepository. Führt SQL aus, um Benutzer anzulegen und zu lesen.
public class JdbcUserRepository implements UserRepository {

    private final ConnectionProvider connectionProvider;

    // Verbindungen werden über den injizierten ConnectionProvider bezogen.
    public JdbcUserRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        // try-with-resources sorgt dafür, dass Connection und Statement automatisch geschlossen werden.
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }
}
