package com.apa.clipfarmer.service;

import com.apa.clipfarmer.logic.twitch.TwitchAuthLogic;
import com.apa.clipfarmer.logic.twitch.TwitchClipFetcherLogic;
import com.apa.clipfarmer.model.ClipFarmerArgs;
import com.apa.clipfarmer.model.TwitchStreamerNameEnum;
import com.beust.jcommander.JCommander;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;

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

    /**
     * Execute main batch
     *
     * @param args
     */
    public void execute(String[] args) {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL log4jConfig = classLoader.getResource("log4j2.xml");

        if (log4jConfig != null) {
            System.out.println("Found log4j2.xml: " + log4jConfig);
        } else {
            System.out.println("log4j2.xml NOT found in classpath!");
        }

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
            String clips = TwitchClipFetcherLogic.getTwitchClips(twitchStreamerNameEnum.getName(), oAuthToken);

            System.out.println("Executing ClipFarmerService logic for streamer: " + twitchStreamerNameEnum.getName());

        } catch (Exception e) {
            log.error("Got an error: " + e.getMessage());
        }
    }
}