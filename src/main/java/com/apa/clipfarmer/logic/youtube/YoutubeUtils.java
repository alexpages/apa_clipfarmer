package com.apa.clipfarmer.logic.youtube;

import com.apa.clipfarmer.mapper.TwitchStreamerMapper;
import com.apa.clipfarmer.model.TwitchClip;
import com.apa.clipfarmer.model.TwitchStreamer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import static com.apa.clipfarmer.model.ClipFarmerConstants.CLIP_FARMER_CONTACT;
import static com.apa.clipfarmer.model.TwitchConstants.TWITCH_URL;

/**
 * Utility class for handling YouTube-related operations, such as generating video titles and descriptions.
 *
 * @author alexpages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeUtils {

    private final SqlSessionFactory sqlSessionFactory;

    /**
     * Creates a YouTube video title based on the broadcaster's ID and Twitch clip details.
     *
     * @param broadcasterId The ID of the Twitch broadcaster.
     * @param twitchClip    The Twitch clip details.
     * @return A formatted YouTube video title.
     * @throws IllegalStateException If the broadcaster cannot be found or an error occurs.
     */
    public String createVideoTitle(String broadcasterId, TwitchClip twitchClip) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TwitchStreamerMapper mapper = session.getMapper(TwitchStreamerMapper.class);
            TwitchStreamer twitchStreamer = mapper.selectByBroadcasterId(broadcasterId);

            if (twitchStreamer == null) {
                throw new IllegalStateException("TwitchStreamer not found for broadcasterId: " + broadcasterId);
            }

            return String.format("Twitch Clip - %s - %s",
                    twitchStreamer.getTwitchStreamerName(),
                    twitchClip.getTitle());
        } catch (Exception e) {
            log.error("Error creating YouTube video title for broadcasterId {}: {}", broadcasterId, e.getMessage(), e);
            throw new IllegalStateException("Failed to create video title due to an internal error.", e);
        }
    }

    /**
     * Creates a YouTube video description with the Twitch streamer’s information.
     *
     * @param broadcasterId The ID of the Twitch broadcaster.
     * @return A formatted YouTube video description.
     * @throws IllegalStateException If the broadcaster cannot be found or an error occurs.
     */
    public String createVideoDescription(String broadcasterId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TwitchStreamerMapper mapper = session.getMapper(TwitchStreamerMapper.class);
            TwitchStreamer twitchStreamer = mapper.selectByBroadcasterId(broadcasterId);

            if (twitchStreamer == null) {
                throw new IllegalStateException("TwitchStreamer not found for broadcasterId: " + broadcasterId);
            }

            return String.format("""
                Follow %s on Twitch:
                ► Twitch: %s%s

                For Video Removal and Copyright Issues:
                Channel Manager/Editor:
                ► %s
                """,
                    twitchStreamer.getTwitchStreamerName(),
                    TWITCH_URL,
                    twitchStreamer.getTwitchStreamerName(),
                    CLIP_FARMER_CONTACT);
        } catch (Exception e) {
            log.error("Error creating YouTube video description for broadcasterId {}: {}", broadcasterId, e.getMessage(), e);
            throw new IllegalStateException("Failed to create video description due to an internal error.", e);
        }
    }
}
