package com.apa.clipfarmer;

import com.apa.clipfarmer.logic.twitch.TwitchAuthLogic;
import com.apa.clipfarmer.logic.twitch.TwitchClipFetcherLogic;
import com.apa.clipfarmer.model.ClipFarmerArgs;
import com.apa.clipfarmer.model.StreamerNameEnum;
import com.beust.jcommander.JCommander;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to trigger the script
 * A twitch streamer will be selected with the key and executed depending on the values passed
 * through the command line
 *
 * @author alexpages
 */

@RequiredArgsConstructor
@Slf4j
public class ClipFarmer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClipFarmer.class);

    /**
     * Execute main batch
     *
     * @param args
     */
    public static void main(String[] args) {
        args = new String[]{"streamerName=jasontheween"};

        final ClipFarmerArgs clipFarmerArgs;
        try {
            ClipFarmerArgs.Builder clipFarmerArgsBuilder = new ClipFarmerArgs.Builder();
            new JCommander(clipFarmerArgsBuilder).parse(args);
            clipFarmerArgs = clipFarmerArgsBuilder.build();
        } catch (Exception e) {
            LOGGER.warn("Invalid arguments: {}", e.getMessage());
            return;
        }

        StreamerNameEnum streamerNameEnum = clipFarmerArgs.getStreamerNameEnum();
        try {
            if (StreamerNameEnum.INVALID.equals(streamerNameEnum)) {
                LOGGER.warn("Twitch streamer is not present in list or was null");
                return;
            }

            String oAuthToken = TwitchAuthLogic.getOAuthToken();
            String clips = TwitchClipFetcherLogic.getTwitchClips(streamerNameEnum.getName(), oAuthToken);
            System.out.println("Executing ClipFarmer logic for streamer: " + streamerNameEnum.getName());

        } catch (Exception e) {

        }
    }
}