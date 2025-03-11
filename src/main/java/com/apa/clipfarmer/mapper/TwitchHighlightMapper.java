package com.apa.clipfarmer.mapper;

import com.apa.clipfarmer.model.TwitchHighlight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Interface for TwitchHighlight data access operations.
 *
 * @author alexpages
 */
@Mapper
public interface TwitchHighlightMapper {

    /**
     * Inserts a new TwitchHighlight record.
     *
     * @param highlight the TwitchHighlight object to insert
     * @return number of rows affected
     */
    int insertHighlight(TwitchHighlight highlight);

    /**
     * Retrieves the last highlight ID by creator name.
     *
     * @param creatorName the name of the creator
     * @return the last highlight ID, or null if not found
     */
    Integer getLastHighlightIdByCreatorName(@Param("creatorName") String creatorName);

    /**
     * Retrieves all highlights by creator name.
     *
     * @param creatorName the name of the creator
     * @return a list of TwitchHighlight objects
     */
    List<TwitchHighlight> getHighlightsByCreatorName(@Param("creatorName") String creatorName);
}
