package com.apa.clipfarmer.service;

import com.apa.clipfarmer.logic.twitch.TwitchAuthLogic;
import com.apa.clipfarmer.logic.twitch.TwitchClipDownloader;
import com.apa.clipfarmer.logic.twitch.TwitchClipFetcherLogic;
import com.apa.clipfarmer.logic.video.VideoLogic;
import com.apa.clipfarmer.mapper.TwitchClipMapper;
import com.apa.clipfarmer.model.ClipFarmerArgs;
import com.apa.clipfarmer.model.TwitchClip;
import com.apa.clipfarmer.model.TwitchStreamerNameEnum;
import com.beust.jcommander.JCommander;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that accepts a Twitch streamer through the command line as a parameter.
 * This service fetches clips, downloads them, and uploads them to a YouTube channel.
 *
 * @author alexpages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClipFarmerService {

    private final TwitchClipFetcherLogic twitchClipFetcherLogic;
    private final TwitchClipDownloader twitchClipDownloader;
    private final SqlSessionFactory sqlSessionFactory;
    private final VideoLogic videoLogic;

    /**
     * The duration of a clip in seconds.
     */
    private static final int CLIP_DURATION = 10;

    /**
     * Execute main batch process.
     *
     * @param args Command-line arguments
     */
    public void execute(String[] args) {
        // Get streamer name
        ClipFarmerArgs clipFarmerArgs = parseArguments(args);
        if (clipFarmerArgs == null) return;
        TwitchStreamerNameEnum twitchStreamer = clipFarmerArgs.getTwitchStreamerNameEnum();
        if (TwitchStreamerNameEnum.INVALID.equals(twitchStreamer)) {
            log.warn("Twitch streamer is not present in list or was null");
            return;
        }

        // Get clips and download them
        String oAuthToken = retrieveOAuthToken();
        if (oAuthToken == null) return;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<TwitchClip> twitchClips = twitchClipFetcherLogic.getTwitchClips(
                    twitchStreamer.getName(), oAuthToken, CLIP_DURATION);
            log.info("Total amount of clips retrieved for broadcasterId [{}] is: [{}]", twitchStreamer.getName(), twitchClips.size());
            TwitchClipMapper mapper = session.getMapper(TwitchClipMapper.class);

            for (TwitchClip clip : twitchClips) {
                processClip(clip, mapper, session);
            }
        } catch (Exception e) {
            log.error("Unexpected error during execution", e);
        }

        // Create summary videos after previous download
        String directoryPath = "build/downloads";
        String outputFileName = "build/output/merged_video.mp4";
        // Get list of video file paths
        List<String> videoPaths = videoLogic.getVideoPaths(directoryPath);
        // Call the method with the retrieved file paths
        videoLogic.concatenateVideos(videoPaths, outputFileName);

    }

    /**
     * Parses command-line arguments into ClipFarmerArgs.
     *
     * @param args Command-line arguments
     * @return Parsed ClipFarmerArgs or null if invalid.
     */
    private ClipFarmerArgs parseArguments(String[] args) {
        try {
            ClipFarmerArgs.Builder clipFarmerArgsBuilder = new ClipFarmerArgs.Builder();
            new JCommander(clipFarmerArgsBuilder).parse(args);
            return clipFarmerArgsBuilder.build();
        } catch (Exception e) {
            log.warn("Invalid arguments: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the Twitch OAuth token.
     *
     * @return OAuth token as a String, or null if retrieval fails.
     */
    private String retrieveOAuthToken() {
        try {
            String token = TwitchAuthLogic.getOAuthToken();
            log.info("OAuth token has been retrieved: [{}]", token);
            return token;
        } catch (Exception e) {
            log.error("Failed to retrieve OAuth token", e);
            return null;
        }
    }

    /**
     * Processes a single Twitch clip by checking its existence, downloading it, and inserting it into the database.
     *
     * @param twitchClip The clip to process.
     * @param mapper     The TwitchClipMapper instance.
     * @param session    The SQL session.
     */
    private void processClip(TwitchClip twitchClip, TwitchClipMapper mapper, SqlSession session) {
        try {
            if (mapper.selectClipByClipId(twitchClip.getClipId()) != null) {
                log.info("Clip {} already exists in DB, skipping.", twitchClip.getClipId());
                return;
            }
            twitchClipDownloader.downloadFile(twitchClip.getUrl(), twitchClip);
            mapper.insertClip(twitchClip);
            session.commit();
            log.info("Inserted new clip {} into the database.", twitchClip.getClipId());
        } catch (Exception e) {
            log.error("Error processing clip {}: {}", twitchClip.getClipId(), e.getMessage(), e);
        }
    }
}
