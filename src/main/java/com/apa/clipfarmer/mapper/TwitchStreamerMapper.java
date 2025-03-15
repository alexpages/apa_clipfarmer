package com.apa.clipfarmer.mapper;

import com.apa.clipfarmer.model.TwitchStreamer;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Interface for TwitchStreamer data access operations.
 *
 * @author alexpages
 */
@Mapper
public interface TwitchStreamerMapper {

    /**
     * Selects all Twitch streamers from the database.
     *
     * @return A list of all TwitchStreamer objects.
     */
    List<TwitchStreamer> selectAllStreamers();

    /**
     * Selects a TwitchStreamer by broadcaster ID.
     *
     * @param broadcasterId The broadcaster's ID to search by.
     * @return The TwitchStreamer with the specified broadcaster ID, or null if not found.
     */
    TwitchStreamer selectByBroadcasterId(@Param("broadcasterId") String broadcasterId);

    /**
     * Selects a TwitchStreamer by ID.
     *
     * @param id The ID of the TwitchStreamer.
     * @return The TwitchStreamer with the specified ID, or null if not found.
     */
    TwitchStreamer selectStreamerById(@Param("id") int id);

    /**
     * Selects a TwitchStreamer by Twitch streamer name.
     *
     * @param twitchStreamerName The name of the Twitch streamer.
     * @return The TwitchStreamer with the specified name, or null if not found.
     */
    TwitchStreamer selectByTwitchStreamerName(@Param("twitchStreamerName") String twitchStreamerName);

    /**
     * Inserts a new TwitchStreamer into the database.
     *
     * @param twitchStreamer The TwitchStreamer object to insert.
     */
    void insertStreamer(TwitchStreamer twitchStreamer);

    /**
     * Updates an existing TwitchStreamer in the database.
     *
     * @param twitchStreamer The TwitchStreamer object to update.
     */
    void updateStreamer(TwitchStreamer twitchStreamer);

    /**
     * Deletes a TwitchStreamer by ID.
     *
     * @param id The ID of the TwitchStreamer to delete.
     */
    void deleteStreamer(@Param("id") int id);
}
