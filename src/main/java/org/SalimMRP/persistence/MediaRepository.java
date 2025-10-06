package org.SalimMRP.persistence;

import org.SalimMRP.persistence.models.Media;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaRepository {

    public boolean save(Media media) {
        String sql = "INSERT INTO media (title, description, media_type, release_year, genre, age_restriction, average_score, created_by_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getMediaType());
            stmt.setInt(4, media.getReleaseYear());
            stmt.setString(5, media.getGenre());
            stmt.setInt(6, media.getAgeRestriction());
            stmt.setDouble(7, media.getAverageScore());
            stmt.setInt(8, media.getCreatedByUserId());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving media: " + e.getMessage());
            return false;
        }
    }

    public List<Media> findAll() {
        String sql = "SELECT * FROM media";
        List<Media> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
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

    public Media findById(int id) {
        String sql = "SELECT * FROM media WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("Error fetching media: " + e.getMessage());
        }
        return null;
    }

    public boolean update(Media media) {
        String sql = "UPDATE media SET title=?, description=?, media_type=?, release_year=?, genre=?, age_restriction=?, average_score=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getMediaType());
            stmt.setInt(4, media.getReleaseYear());
            stmt.setString(5, media.getGenre());
            stmt.setInt(6, media.getAgeRestriction());
            stmt.setDouble(7, media.getAverageScore());
            stmt.setInt(8, media.getId());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating media: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM media WHERE id=?";
        try (Connection conn = Database.getConnection();
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
        media.setReleaseYear(rs.getInt("release_year"));
        media.setGenre(rs.getString("genre"));
        media.setAgeRestriction(rs.getInt("age_restriction"));
        media.setAverageScore(rs.getDouble("average_score"));
        media.setCreatedByUserId(rs.getInt("created_by_user_id"));
        return media;
    }
}
