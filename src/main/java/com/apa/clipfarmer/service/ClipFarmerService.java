package com.apa.clipfarmer.service;

import com.apa.clipfarmer.logic.twitch.TwitchAuthLogic;
import com.apa.clipfarmer.logic.twitch.TwitchClipFetcherLogic;
import com.apa.clipfarmer.model.ClipFarmerArgs;
import com.apa.clipfarmer.model.TwitchClip;
import com.apa.clipfarmer.model.TwitchStreamerNameEnum;
import com.beust.jcommander.JCommander;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class to trigger the script
 * A twitch streamer will be selected with the key and executed depending on the values passed
 * through the command line
 *
 * @author alexpages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClipFarmerService {

    private final TwitchClipFetcherLogic twitchClipFetcherLogic;

    /**
     * Execute main batch
     *
     * @param args
     */
    public void execute(String[] args) {
        final ClipFarmerArgs clipFarmerArgs;
        try {
            ClipFarmerArgs.Builder clipFarmerArgsBuilder = new ClipFarmerArgs.Builder();
            new JCommander(clipFarmerArgsBuilder).parse(args);
            clipFarmerArgs = clipFarmerArgsBuilder.build();
        } catch (Exception e) {
            log.warn("Invalid arguments: {}", e.getMessage());
            return;
        }

        TwitchStreamerNameEnum twitchStreamerNameEnum = clipFarmerArgs.getTwitchStreamerNameEnum();
        try {
            if (TwitchStreamerNameEnum.INVALID.equals(twitchStreamerNameEnum)) {
                log.warn("Twitch streamer is not present in list or was null");
                return;
            }

            String oAuthToken = TwitchAuthLogic.getOAuthToken();
            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips(twitchStreamerNameEnum.getName(), oAuthToken);

            System.out.println("Executing ClipFarmerService logic for streamer: " + twitchStreamerNameEnum.getName());

        } catch (Exception e) {
            log.error("Got an error: " + e.getMessage());
        }
    }
}