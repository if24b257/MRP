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
        String sql = """
                INSERT INTO media (title, description, media_type, release_year, age_restriction, genres, created_by_user_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getMediaType());
            if (media.getReleaseYear() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, media.getReleaseYear());
            }
            stmt.setString(5, media.getAgeRestriction());

            var genres = media.getGenres();
            java.sql.Array genresArray = null;
            if (!genres.isEmpty()) {
                genresArray = conn.createArrayOf("text", genres.toArray());
            }
            stmt.setArray(6, genresArray);
            stmt.setInt(7, media.getCreatedByUserId());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    media.setId(keys.getInt(1));
                }
            }
            if (genresArray != null) {
                genresArray.free();
            }
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
        String sql = """
                UPDATE media
                   SET title = ?, description = ?, media_type = ?, release_year = ?, age_restriction = ?, genres = ?
                 WHERE id = ?
                """;
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getMediaType());
            if (media.getReleaseYear() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, media.getReleaseYear());
            }
            stmt.setString(5, media.getAgeRestriction());

            var genres = media.getGenres();
            java.sql.Array genresArray = null;
            if (!genres.isEmpty()) {
                genresArray = conn.createArrayOf("text", genres.toArray());
            }
            stmt.setArray(6, genresArray);

            stmt.setInt(7, media.getId());

            stmt.executeUpdate();
            if (genresArray != null) {
                genresArray.free();
            }
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
        int releaseYear = rs.getInt("release_year");
        media.setReleaseYear(rs.wasNull() ? null : releaseYear);
        media.setAgeRestriction(rs.getString("age_restriction"));

        var genresArray = rs.getArray("genres");
        if (genresArray != null) {
            try {
                String[] genres = (String[]) genresArray.getArray();
                media.setGenres(List.of(genres));
            } finally {
                genresArray.free();
            }
        } else {
            media.setGenres(List.of());
        }
        media.setCreatedByUserId(rs.getInt("created_by_user_id"));
        return media;
    }
}
