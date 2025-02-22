package com.apa.clipfarmer.service;

import com.apa.clipfarmer.logic.twitch.TwitchAuthLogic;
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
 * Class to trigger the script
 * A twitch streamer will be selected with the key and executed depending on the values passed
 * through the command line.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClipFarmerService {

    private final TwitchClipFetcherLogic twitchClipFetcherLogic;
    private final SqlSessionFactory sqlSessionFactory;

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
            // Get List of clips for that streamer
            List<TwitchClip> lTwitchClip = twitchClipFetcherLogic.getTwitchClips(twitchStreamerNameEnum.getName(), oAuthToken);
            TwitchClipMapper mapper = session.getMapper(TwitchClipMapper.class);
            log.info("TwitchClipMapper has been instantiated");

            for (TwitchClip twitchClip : lTwitchClip) {
                try {
                    // Check if the clip already exists in DB
                    if (mapper.selectClipByClipId(twitchClip.getClipId()) != null) {
                        log.info("Clip {} already exists in DB, skipping.", twitchClip.getClipId());
                        continue;
                    }
                    // Save the clip to the database
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
