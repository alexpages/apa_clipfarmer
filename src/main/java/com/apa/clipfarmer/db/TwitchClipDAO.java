package com.apa.clipfarmer.db;

import com.apa.clipfarmer.config.DatabaseClient;
import com.apa.clipfarmer.model.TwitchClip;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for managing {@link TwitchClip} entities in the database.
 */
@RequiredArgsConstructor
public class TwitchClipDAO {

    private final DatabaseClient databaseClient;

    /**
     * Saves a new Twitch clip.
     */
    public void save(TwitchClip clip) {
        String sql = "INSERT INTO twitch_clips (clip_id, title, creator_name, view_count, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, clip.getClipId());
            statement.setString(2, clip.getTitle());
            statement.setString(3, clip.getCreatorName());
            statement.setInt(4, clip.getViewCount());
            statement.setObject(5, clip.getCreatedAt());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving Twitch clip", e);
        }
    }

    /**
     * Finds a Twitch clip by its clip ID.
     *
     * @param clipId the unique identifier of the Twitch clip
     * @return the {@link TwitchClip} object, or {@code null} if not found
     */
    public TwitchClip findByClipId(String clipId) {
        String sql = "SELECT * FROM twitch_clips WHERE clip_id = ?";
        try (Connection connection = databaseClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, clipId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Twitch clip by clipId", e);
        }
        return null;
    }

    /**
     * Retrieves all Twitch clips.
     *
     * @return a list of all {@link TwitchClip} objects
     */
    public List<TwitchClip> findAll() {
        String sql = "SELECT * FROM twitch_clips";
        List<TwitchClip> clips = new ArrayList<>();
        try (Connection connection = databaseClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                clips.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all Twitch clips", e);
        }
        return clips;
    }

    /**
     * Updates the view count of a Twitch clip.
     */
    public void updateViewCount(String clipId, int newViewCount) {
        String sql = "UPDATE twitch_clips SET view_count = ? WHERE clip_id = ?";
        try (Connection connection = databaseClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newViewCount);
            statement.setString(2, clipId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating view count for Twitch clip", e);
        }
    }

    /**
     * Deletes a Twitch clip by its clip ID.
     */
    public void deleteByClipId(String clipId) {
        String sql = "DELETE FROM twitch_clips WHERE clip_id = ?";
        try (Connection connection = databaseClient.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, clipId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Twitch clip by clipId", e);
        }
    }

    /**
     * Maps a result set row to a {@link TwitchClip}.
     *
     * @param rs the result set containing the data
     * @return a {@link TwitchClip} object
     * @throws SQLException if an error occurs while accessing the result set
     */
    private TwitchClip mapRow(ResultSet rs) throws SQLException {
        TwitchClip clip = new TwitchClip();
        clip.setId(rs.getInt("id"));
        clip.setClipId(rs.getString("clip_id"));
        clip.setTitle(rs.getString("title"));
        clip.setCreatorName(rs.getString("creator_name"));
        clip.setViewCount(rs.getInt("view_count"));
        clip.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return clip;
    }

    /**
     * RowMapper for {@link TwitchClip}.
     */
    class TwitchClipRowMapper implements RowMapper<TwitchClip> {
        @Override
        public TwitchClip mapRow(ResultSet rs, int rowNum) throws SQLException {
            TwitchClip clip = new TwitchClip();
            clip.setId(rs.getInt("id"));
            clip.setClipId(rs.getString("clip_id"));
            clip.setTitle(rs.getString("title"));
            clip.setCreatorName(rs.getString("creator_name"));
            clip.setViewCount(rs.getInt("view_count"));
            clip.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return clip;
        }
    }
}