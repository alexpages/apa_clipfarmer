package com.apa.clipfarmer.logic.youtube;

import com.apa.clipfarmer.mapper.TwitchStreamerMapper;
import com.apa.clipfarmer.model.TwitchStreamer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import static com.apa.clipfarmer.model.ClipFarmerConstants.CLIP_FARMER_CONTACT;
import static com.apa.clipfarmer.model.TwitchConstants.TWITCH_URL;

/**
 * Class that provide utils for Youtube related matters.
 *
 * @author alexpages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeUtils {

    private final SqlSessionFactory sqlSessionFactory;

    public String createVideoDescription(String broadcasterId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TwitchStreamerMapper mapper = session.getMapper(TwitchStreamerMapper.class);
            TwitchStreamer twitchStreamer = mapper.selectByBroadcasterId(broadcasterId);

            if (twitchStreamer == null) {
                throw new RuntimeException("TwitchStreamer was not found in the Database");
            }

            return String.format("""
                Follow %s's Twitch:
                ► Twitch:   / %s

                For Video Removal and Copyright Issues.
                Channel Manager/Editor:
                ► %s
                """,
                twitchStreamer.getTwitchStreamerName(),
                TWITCH_URL + twitchStreamer.getTwitchStreamerName(),
                CLIP_FARMER_CONTACT);
        } catch (Exception e) {
            log.warn("There was an error while creating the Youtube video description: {}", e);
            throw new RuntimeException("Cancelling the creation of video description due to the following error: {}", e);
        }
    }
}
