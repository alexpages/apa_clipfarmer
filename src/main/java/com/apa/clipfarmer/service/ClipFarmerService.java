package com.apa.clipfarmer.service;

import com.apa.clipfarmer.logic.EmailNotificationLogic;
import com.apa.clipfarmer.logic.twitch.TwitchAuthLogic;
import com.apa.clipfarmer.logic.twitch.TwitchClipDownloader;
import com.apa.clipfarmer.logic.twitch.TwitchClipFetcherLogic;
import com.apa.clipfarmer.logic.video.VideoLogic;
import com.apa.clipfarmer.logic.youtube.YoutubeUploaderLogic;
import com.apa.clipfarmer.mapper.TwitchClipMapper;
import com.apa.clipfarmer.model.ClipFarmerArgs;
import com.apa.clipfarmer.model.TwitchClip;
import com.apa.clipfarmer.model.TwitchStreamerNameEnum;
import com.apa.clipfarmer.utils.FileUtils;
import com.apa.clipfarmer.utils.YoutubeUtils;
import com.beust.jcommander.JCommander;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
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
    private final EmailNotificationLogic emailNotificationLogic;
    private final YoutubeUtils youtubeUtils;
    private final YoutubeUploaderLogic youtubeUploaderLogic;

    private static final int CLIP_DURATION = 10;
    private static final int MIN_VIEWS = 400;
    private static final int DAYS_AGO = 5;

    private static final String DOWNLOAD_DIRECTORY = "build/downloads/";
    private static final String OUTPUT_DIRECTORY = "build/output/";
    private static final String MERGED_VIDEO_FILENAME = "_merged_video.mp4";

    /**
     * Execute main batch process.
     *
     * @param args Command-line arguments
     */
    public void execute(String[] args) {
        long startTime = System.currentTimeMillis();

        // Get streamer name
        ClipFarmerArgs clipFarmerArgs = parseArguments(args);
        if (clipFarmerArgs == null) {
            return;
        }
        TwitchStreamerNameEnum twitchStreamer = clipFarmerArgs.getTwitchStreamerNameEnum();
        if (TwitchStreamerNameEnum.INVALID.equals(twitchStreamer)) {
            log.warn("Twitch streamer is not present in list or was null");
            return;
        }

        // Get clips and download them
        String twitchOAuthToken = retrieveTwitchOAuthToken();
        if (twitchOAuthToken == null) {
            return;
        }

        Map<String, Double> clipDurationsMap = new LinkedHashMap<>();
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<TwitchClip> twitchClips = twitchClipFetcherLogic.getTwitchClips(
                    twitchStreamer.getName(), twitchOAuthToken, CLIP_DURATION, MIN_VIEWS, DAYS_AGO);
            log.info("Total amount of clips retrieved for broadcasterId [{}] is: [{}]", twitchStreamer.getName(), twitchClips.size());
            TwitchClipMapper mapper = session.getMapper(TwitchClipMapper.class);

            for (TwitchClip clip : twitchClips) {
                String clipFilePath = DOWNLOAD_DIRECTORY + twitchStreamer.getName() + "/" + clip.getClipId() + ".mp4";
                double duration = processClip(clip, mapper, session, twitchStreamer);
                if (duration != 0.0) {
                    clipDurationsMap.put(clipFilePath, duration);
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error during execution", e);
        }

        // Create summary videos after previous download
        String fileName = MERGED_VIDEO_FILENAME;
        String outputFileName = OUTPUT_DIRECTORY + twitchStreamer.getName() + fileName;

        // Process videos
        String pathVideoCreated = videoLogic.concatenateVideos(clipDurationsMap, outputFileName);
        log.info("pathVideoCreated is: {}", pathVideoCreated);

        // Upload video
        String youtubeDescription = youtubeUtils.createVideoDescription(twitchStreamer.getName());
        String yotubeTitle = youtubeUtils.createVideoTitle(twitchStreamer.getName(), fileName, true);
        youtubeUploaderLogic.uploadHighlightVideo(yotubeTitle, youtubeDescription, pathVideoCreated, twitchStreamer.getName());
        // Send email notification
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        log.info("Batch execution took {} seconds", elapsedTime);
        emailNotificationLogic.sendEmail("Execution finalized", twitchStreamer.getName(), elapsedTime);

        // Clean up
        FileUtils.deleteDirectory(Paths.get("build/output"));
        FileUtils.deleteDirectory(Paths.get("build/downloads"));
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
    private String retrieveTwitchOAuthToken() {
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
     * @param twitchStreamer The twitch streamer enum.
     * @return The duration of the processed clip in seconds.
     */
    private double processClip(TwitchClip twitchClip, TwitchClipMapper mapper, SqlSession session, TwitchStreamerNameEnum twitchStreamer) {
        try {
            if (mapper.selectClipByClipId(twitchClip.getClipId()) != null) {
                log.info("Clip {} already exists in DB, skipping.", twitchClip.getClipId());
                return 0.0;
            }
            twitchClipDownloader.downloadFile(twitchClip.getUrl(), twitchClip, twitchStreamer);
            mapper.insertClip(twitchClip);
            session.commit();
            log.info("Inserted new clip {} into the database.", twitchClip.getClipId());
            return (double) twitchClip.getDuration();
        } catch (Exception e) {
            log.error("Error processing clip {}: {}", twitchClip.getClipId(), e.getMessage(), e);
            return 0;
        }
    }
}
