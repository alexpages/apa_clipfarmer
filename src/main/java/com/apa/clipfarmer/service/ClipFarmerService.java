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
import io.micrometer.common.util.StringUtils;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final TwitchAuthLogic twitchAuthLogic;
    private final TwitchClipFetcherLogic twitchClipFetcherLogic;
    private final TwitchClipDownloader twitchClipDownloader;
    private final TwitchClipMapper twitchClipMapper;
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

        ClipFarmerArgs clipFarmerArgs = parseArguments(args);
        if (clipFarmerArgs == null) {
            return;
        }
        TwitchStreamerNameEnum twitchStreamer = clipFarmerArgs.getTwitchStreamerNameEnum();
        if (TwitchStreamerNameEnum.INVALID.equals(twitchStreamer)) {
            log.warn("Twitch streamer is not present in list or was null");
            return;
        }

        String twitchOAuthToken = retrieveTwitchOAuthToken();

        List<TwitchClip> twitchClips = twitchClipFetcherLogic.getTwitchClips(
                twitchStreamer.getName(), twitchOAuthToken, CLIP_DURATION, MIN_VIEWS, DAYS_AGO);
        log.info("Total amount of clips retrieved for streamer [{}]: [{}]", twitchStreamer.getName(), twitchClips.size());

        List<String> clipPaths = twitchClips.stream()
                .map(clip -> processClip(clip, twitchStreamer))
                .flatMap(Optional::stream)
                .toList();

        String outputFileName = OUTPUT_DIRECTORY + twitchStreamer.getName() + MERGED_VIDEO_FILENAME;
        String pathVideoCreated = videoLogic.concatenateVideos(clipPaths, outputFileName);
        log.info("pathVideoCreated is: {}", pathVideoCreated);

        String youtubeDescription = youtubeUtils.createVideoDescription(twitchStreamer.getName());
        String youtubeTitle = youtubeUtils.createVideoTitle(twitchStreamer.getName(), MERGED_VIDEO_FILENAME, true);
        youtubeUploaderLogic.uploadHighlightVideo(youtubeTitle, youtubeDescription, pathVideoCreated, twitchStreamer.getName());

        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        log.info("Batch execution took {} seconds", elapsedTime);
        emailNotificationLogic.sendEmail("Execution finalized", twitchStreamer.getName(), elapsedTime);

        cleanUpTemporaryFiles();
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
            String token = twitchAuthLogic.getOAuthToken();
            if (StringUtils.isEmpty(token)) {
                throw new RuntimeException("Twitch token retrieved was null or empty.");
            }
            log.info("OAuth token retrieved successfully.");
            return token;
        } catch (Exception e) {
            log.error("Failed to retrieve OAuth token", e);
            return null;
        }
    }

    /**
     * Processes a single Twitch clip: checks DB, downloads, and inserts if new.
     *
     * @param twitchClip    The clip to process.
     * @param twitchStreamer The twitch streamer enum.
     * @return The file path of the downloaded clip, or empty if the clip was skipped or failed.
     */
    private Optional<String> processClip(TwitchClip twitchClip, TwitchStreamerNameEnum twitchStreamer) {
        try {
            if (twitchClipMapper.selectClipByClipId(twitchClip.getClipId()) != null) {
                log.info("Clip {} already exists in DB, skipping.", twitchClip.getClipId());
                return Optional.empty();
            }
            twitchClipDownloader.downloadFile(twitchClip.getUrl(), twitchClip, twitchStreamer);
            twitchClipMapper.insertClip(twitchClip);
            log.info("Inserted new clip {} into the database.", twitchClip.getClipId());
            String clipFilePath = DOWNLOAD_DIRECTORY + twitchStreamer.getName() + "/" + twitchClip.getClipId() + ".mp4";
            return Optional.of(clipFilePath);
        } catch (Exception e) {
            log.error("Error processing clip {}: {}", twitchClip.getClipId(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Deletes all temporary folders including the files within.
     */
    private void cleanUpTemporaryFiles() {
        FileUtils.deleteDirectory(Paths.get("build/output"));
        FileUtils.deleteDirectory(Paths.get("build/downloads"));
    }
}
