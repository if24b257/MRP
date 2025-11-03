package org.SalimMRP.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// JDBC-Implementierung für Favoritenabfragen und -aktionen.
public class JdbcFavoriteRepository implements FavoriteRepository {

    private final ConnectionProvider connectionProvider;

    public JdbcFavoriteRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public boolean addFavorite(int userId, int mediaId) {
        String sql = "INSERT INTO favorites (user_id, media_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, mediaId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding favorite: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeFavorite(int userId, int mediaId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND media_id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, mediaId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error removing favorite: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isFavorite(int userId, int mediaId) {
        String sql = "SELECT 1 FROM favorites WHERE user_id = ? AND media_id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, mediaId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error checking favorite: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Integer> findMediaIdsByUser(int userId) {
        String sql = "SELECT media_id FROM favorites WHERE user_id = ? ORDER BY marked_at DESC";
        List<Integer> mediaIds = new ArrayList<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mediaIds.add(rs.getInt("media_id"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching favorites: " + e.getMessage());
        }
        return mediaIds;
    }

    @Override
    public int countFavoritesForMedia(int mediaId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE media_id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mediaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error counting favorites: " + e.getMessage());
        }
        return 0;
    }
}
