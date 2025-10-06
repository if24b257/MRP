package org.SalimMRP.persistence;

import org.SalimMRP.persistence.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    //Neuen Benutzer speichern
    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password, favorite_genre, total_ratings, average_score) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFavoriteGenre());
            stmt.setInt(4, user.getTotalRatings());
            stmt.setDouble(5, user.getAverageScore());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
            return false;
        }
    }

    //Benutzer anhand von Username abrufen
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("favorite_genre"),
                        rs.getInt("total_ratings"),
                        rs.getDouble("average_score")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }
}
