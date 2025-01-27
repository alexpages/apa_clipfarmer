package com.apa.clipfarmer;

import com.apa.clipfarmer.logic.ClipFarmerLogic;
import com.apa.clipfarmer.logic.TwitchLogic;
import com.apa.clipfarmer.model.ClipFarmerArgs;
import com.apa.clipfarmer.model.StreamerNameEnum;
import com.beust.jcommander.JCommander;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private final ClipFarmerLogic clipFarmerLogic;

    /**
     * Execute main batch
     *
     * @param args
     */
    public static void main(String[] args) {
        final ClipFarmerArgs clipFarmerArgs;

        try {
            System.out.println(TwitchLogic.getOAuthToken());

            ClipFarmerArgs.Builder clipFarmerArgsBuilder = new ClipFarmerArgs.Builder();
            new JCommander(clipFarmerArgsBuilder).parse(args);
            clipFarmerArgs = clipFarmerArgsBuilder.build();
            if (StringUtils.isEmpty(clipFarmerArgs.getStreamerNameEnum().getName())) {
                throw new Exception("Streamer name is invalid");
            }

        } catch (Exception e) {
            LOGGER.warn("Twitch streamer is not present in list or was null. Error: {}", e.getMessage());
            return;
        }

        StreamerNameEnum streamerNameEnum = clipFarmerArgs.getStreamerNameEnum();
        //TODO implement logic
        System.out.println("Hello world!");
    }
}