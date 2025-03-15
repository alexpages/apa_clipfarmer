package com.apa.clipfarmer.mapper;

import com.apa.clipfarmer.model.TwitchClip;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis Mapper Interface for TwitchClip database operations.
 *
 * @author alexpages
 */
@Mapper
public interface TwitchClipMapper {

    /**
     * Selects all Twitch clips from the database.
     *
     * @return A list of all TwitchClip objects.
     */
    List<TwitchClip> selectAllClips();

    /**
     * Selects a TwitchClip by its ID.
     *
     * @param id The ID of the TwitchClip.
     * @return The TwitchClip with the specified ID, or null if not found.
     */
    TwitchClip selectClipById(int id);

    /**
     * Selects a TwitchClip by its clip ID.
     *
     * @param clipId The clip ID to search by.
     * @return The TwitchClip with the specified clip ID, or null if not found.
     */
    TwitchClip selectClipByClipId(String clipId);

    /**
     * Inserts a new TwitchClip into the database.
     *
     * @param clip The TwitchClip object to insert.
     */
    void insertClip(TwitchClip clip);
}
