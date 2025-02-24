package com.apa.clipfarmer.service;

import com.apa.clipfarmer.logic.twitch.TwitchAuthLogic;
import com.apa.clipfarmer.logic.twitch.TwitchClipDownloader;
import com.apa.clipfarmer.logic.twitch.TwitchClipFetcherLogic;
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
 * Service that accepts a twitch streamer through the command line as param.
 * This service fetches clips, downloads them and uploads them to a Youtube channel.
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

    /**
     * The duration of a clip in seconds.
     */
    private final int clipDuration = 10;

    /**
     * Execute main batch
     *
     * @param args Command-line arguments
     */
    public void execute(String[] args) {
        ClipFarmerArgs clipFarmerArgs;
        try {
            ClipFarmerArgs.Builder clipFarmerArgsBuilder = new ClipFarmerArgs.Builder();
            new JCommander(clipFarmerArgsBuilder).parse(args);
            clipFarmerArgs = clipFarmerArgsBuilder.build();
        } catch (Exception e) {
            log.warn("Invalid arguments: {}", e.getMessage());
            return;
        }

        TwitchStreamerNameEnum twitchStreamerNameEnum = clipFarmerArgs.getTwitchStreamerNameEnum();
        if (TwitchStreamerNameEnum.INVALID.equals(twitchStreamerNameEnum)) {
            log.warn("Twitch streamer is not present in list or was null");
            return;
        }

        try (SqlSession session = sqlSessionFactory.openSession()){

            // Retrieve Twitch Token
            String oAuthToken = TwitchAuthLogic.getOAuthToken();
            log.info("oAuthToken has been retrieved: [{}]", oAuthToken);

            List<TwitchClip> lTwitchClip = twitchClipFetcherLogic.getTwitchClips(
                    twitchStreamerNameEnum.getName(),
                    oAuthToken,
                    clipDuration);
            TwitchClipMapper mapper = session.getMapper(TwitchClipMapper.class);

            for (TwitchClip twitchClip : lTwitchClip) {
                try {
                    // Check if the clip already exists in DB
                    if (mapper.selectClipByClipId(twitchClip.getClipId()) != null) {
                        log.info("Clip {} already exists in DB, skipping.", twitchClip.getClipId());
                        continue;
                    }

                    twitchClipDownloader.downloadFile(twitchClip.getUrl());
                    mapper.insertClip(twitchClip);
                    session.commit();
                    log.info("Inserted new clip {} into the database.", twitchClip.getClipId());
                    return;
                } catch (Exception e) {
                    log.error("Error processing clip {}: {}", twitchClip.getClipId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error during execution", e);
        }
    }
}
