package com.apa.clipfarmer.utils;

import com.apa.clipfarmer.mapper.TwitchHighlightMapper;
import com.apa.clipfarmer.mapper.TwitchStreamerMapper;
import com.apa.clipfarmer.model.TwitchStreamer;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
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
     * @param title The title.
     * @param isHighlight Indicates if this video title is for a Highlight video or not.
     * @return A formatted YouTube video title.
     * @throws IllegalStateException If the broadcaster cannot be found or an error occurs.
     */
    public String createVideoTitle(String broadcasterId, String title, Boolean isHighlight) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TwitchStreamerMapper mapper = session.getMapper(TwitchStreamerMapper.class);
            TwitchStreamer twitchStreamer = mapper.selectByTwitchStreamerName(broadcasterId);

            if (twitchStreamer == null) {
                throw new IllegalStateException("TwitchStreamer not found for broadcasterId: " + broadcasterId);
            }

            if (isHighlight) {
                TwitchHighlightMapper twitchHighlightMapper = session.getMapper(TwitchHighlightMapper.class);
                Integer lastId = twitchHighlightMapper.getLastHighlightIdByCreatorName(broadcasterId);
                lastId = (lastId == null) ? 1 : lastId + 1;
                String month = LocalDateTime.now().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
                return String.format("%s HIGHLIGHTS TWITCH %s #%s",
                        twitchStreamer.getTwitchStreamerName().toUpperCase(),
                        month,
                        lastId);
            } else {
                return String.format("Twitch Clip - %s - %s",
                        twitchStreamer.getTwitchStreamerName(),
                        title);
            }
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
            TwitchStreamer twitchStreamer = mapper.selectByTwitchStreamerName(broadcasterId);

            if (twitchStreamer == null) {
                throw new IllegalStateException("TwitchStreamer not found for broadcasterId: " + broadcasterId);
            }

            return String.format("""
                This is a compilation of the most viewed clips from %s from %s.
                
                Follow %s on Twitch:
                ► Twitch: %s%s

                For Video Removal and Copyright Issues:
                Channel Manager/Editor:
                ► %s
                """,
                    twitchStreamer.getTwitchStreamerName(),
                    LocalDateTime.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
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
