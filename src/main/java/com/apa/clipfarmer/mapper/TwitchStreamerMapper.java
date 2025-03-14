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

    List<TwitchStreamer> selectAllStreamers();

    TwitchStreamer selectByBroadcasterId(@Param("broadcasterId") String broadcasterId);

    TwitchStreamer selectStreamerById(@Param("id") int id);

    TwitchStreamer selectByTwitchStreamerName(@Param("twitchStreamerName") String twitchStreamerName);


    void insertStreamer(TwitchStreamer twitchStreamer);

    void updateStreamer(TwitchStreamer twitchStreamer);

    void deleteStreamer(@Param("id") int id);
}
