package org.SalimMRP.persistence;

import org.SalimMRP.persistence.models.Rating;
import org.SalimMRP.persistence.models.RatingSummary;
import org.SalimMRP.persistence.models.UserRatingCount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// JDBC-gestützte Umsetzung des RatingRepository inklusive Like- und Moderationsoperationen.
public class JdbcRatingRepository implements RatingRepository {

    private final ConnectionProvider connectionProvider;

    public JdbcRatingRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Rating save(Rating rating) {
        String sql = """
                INSERT INTO ratings (media_id, user_id, star_value, comment, comment_confirmed, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, rating.getMediaId());
            stmt.setInt(2, rating.getUserId());
            stmt.setInt(3, rating.getStarValue());
            stmt.setString(4, rating.getComment());
            stmt.setBoolean(5, rating.isCommentConfirmed());
            if (rating.getCreatedAt() == null) {
                stmt.setTimestamp(6, null);
            } else {
                stmt.setTimestamp(6, Timestamp.from(rating.getCreatedAt()));
            }
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    rating.setId(keys.getInt(1));
                }
            }
            return rating;

        } catch (SQLException e) {
            System.err.println("Error saving rating: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(Rating rating) {
        String sql = """
                UPDATE ratings
                   SET star_value = ?, comment = ?, comment_confirmed = ?, created_at = ?
                 WHERE id = ?
                """;
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rating.getStarValue());
            stmt.setString(2, rating.getComment());
            stmt.setBoolean(3, rating.isCommentConfirmed());
            if (rating.getCreatedAt() == null) {
                stmt.setTimestamp(4, null);
            } else {
                stmt.setTimestamp(4, Timestamp.from(rating.getCreatedAt()));
            }
            stmt.setInt(5, rating.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating rating: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM ratings WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting rating: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Rating findById(int id) {
        String sql = """
                SELECT id, media_id, user_id, star_value, comment, comment_confirmed, created_at
                  FROM ratings
                 WHERE id = ?
                """;
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Rating rating = mapRow(rs);
                    rating.setLikedByUserIds(findLikes(id));
                    return rating;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching rating: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Rating findByMediaIdAndUserId(int mediaId, int userId) {
        String sql = """
                SELECT id, media_id, user_id, star_value, comment, comment_confirmed, created_at
                  FROM ratings
                 WHERE media_id = ? AND user_id = ?
                """;
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mediaId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Rating rating = mapRow(rs);
                    rating.setLikedByUserIds(findLikes(rating.getId()));
                    return rating;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching rating by media and user: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Rating> findByMediaId(int mediaId) {
        String sql = """
                SELECT id, media_id, user_id, star_value, comment, comment_confirmed, created_at
                  FROM ratings
                 WHERE media_id = ?
                 ORDER BY created_at DESC
                """;
        List<Rating> ratings = new ArrayList<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mediaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Rating rating = mapRow(rs);
                    rating.setLikedByUserIds(findLikes(rating.getId()));
                    ratings.add(rating);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching ratings: " + e.getMessage());
        }
        return ratings;
    }

    @Override
    public List<Rating> findByUserId(int userId) {
        String sql = """
                SELECT id, media_id, user_id, star_value, comment, comment_confirmed, created_at
                  FROM ratings
                 WHERE user_id = ?
                 ORDER BY created_at DESC
                """;
        List<Rating> ratings = new ArrayList<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Rating rating = mapRow(rs);
                    rating.setLikedByUserIds(findLikes(rating.getId()));
                    ratings.add(rating);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching ratings by user: " + e.getMessage());
        }
        return ratings;
    }

    @Override
    public List<RatingSummary> summarizeByMediaIds(List<Integer> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT media_id, AVG(star_value) AS avg_score, COUNT(*) AS rating_count
                  FROM ratings
                 WHERE media_id = ANY(?)
                 GROUP BY media_id
                """;

        List<RatingSummary> summaries = new ArrayList<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            var idsArray = conn.createArrayOf("int4", mediaIds.toArray(Integer[]::new));
            stmt.setArray(1, idsArray);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    summaries.add(new RatingSummary(
                            rs.getInt("media_id"),
                            rs.getDouble("avg_score"),
                            rs.getInt("rating_count")
                    ));
                }
            }
            idsArray.free();

        } catch (SQLException e) {
            System.err.println("Error summarizing ratings: " + e.getMessage());
        }
        return summaries;
    }

    @Override
    public List<UserRatingCount> findRatingCountsPerUser(int limit) {
        String sql = """
                SELECT user_id, COUNT(*) AS rating_count
                  FROM ratings
                 GROUP BY user_id
                 ORDER BY rating_count DESC, user_id ASC
                 LIMIT ?
                """;

        List<UserRatingCount> result = new ArrayList<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int effectiveLimit = limit > 0 ? limit : Integer.MAX_VALUE;
            stmt.setInt(1, effectiveLimit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new UserRatingCount(
                            rs.getInt("user_id"),
                            rs.getLong("rating_count")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching rating leaderboard: " + e.getMessage());
        }
        return result;
    }

    @Override
    public boolean confirmComment(int ratingId) {
        String sql = "UPDATE ratings SET comment_confirmed = TRUE WHERE id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ratingId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error confirming rating comment: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addLike(int ratingId, int userId) {
        String sql = "INSERT INTO rating_likes (rating_id, user_id) VALUES (?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ratingId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                return false;
            }
            System.err.println("Error adding like: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeLike(int ratingId, int userId) {
        String sql = "DELETE FROM rating_likes WHERE rating_id = ? AND user_id = ?";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ratingId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error removing like: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Set<Integer> findLikes(int ratingId) {
        String sql = "SELECT user_id FROM rating_likes WHERE rating_id = ?";
        Set<Integer> likes = new HashSet<>();

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ratingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    likes.add(rs.getInt("user_id"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching likes: " + e.getMessage());
        }
        return likes;
    }

    private Rating mapRow(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getInt("id"));
        rating.setMediaId(rs.getInt("media_id"));
        rating.setUserId(rs.getInt("user_id"));
        rating.setStarValue(rs.getInt("star_value"));
        rating.setComment(rs.getString("comment"));
        rating.setCommentConfirmed(rs.getBoolean("comment_confirmed"));
        rating.setCreatedAt(toInstant(rs, "created_at"));
        return rating;
    }

    private Instant toInstant(ResultSet rs, String column) throws SQLException {
        var timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private boolean isUniqueViolation(SQLException e) {
        // SQLState 23505 entspricht Unique-Violation in PostgreSQL.
        return "23505".equals(e.getSQLState());
    }
}
