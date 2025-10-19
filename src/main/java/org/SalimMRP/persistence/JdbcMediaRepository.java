package org.SalimMRP.persistence;

import org.SalimMRP.persistence.models.Media;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// JDBC-Variante des MediaRepository mit den SQL-Statements für CRUD-Operationen.
public class JdbcMediaRepository implements MediaRepository {

    private final ConnectionProvider connectionProvider;

    public JdbcMediaRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public boolean save(Media media) {
        String sql = "INSERT INTO media (title, description, media_type, created_by_user_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getMediaType());
            stmt.setInt(4, media.getCreatedByUserId());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving media: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Media> findAll() {
        String sql = "SELECT * FROM media";
        List<Media> list = new ArrayList<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error fetching media list: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Media findById(int id) {
        String sql = "SELECT * FROM media WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("Error fetching media: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean update(Media media) {
        String sql = "UPDATE media SET title=?, description=?, media_type=? WHERE id=?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getMediaType());
            stmt.setInt(4, media.getId());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating media: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM media WHERE id=?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting media: " + e.getMessage());
            return false;
        }
    }

    private Media mapRow(ResultSet rs) throws SQLException {
        Media media = new Media();
        media.setId(rs.getInt("id"));
        media.setTitle(rs.getString("title"));
        media.setDescription(rs.getString("description"));
        media.setMediaType(rs.getString("media_type"));
        media.setCreatedByUserId(rs.getInt("created_by_user_id"));
        return media;
    }
}
