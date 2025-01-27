package com.apa.clipfarmer;

import com.apa.clipfarmer.logic.ClipFarmerLogic;
import com.apa.clipfarmer.model.ClipFarmerArgs;
import com.apa.clipfarmer.model.StreamerNameEnum;
import com.beust.jcommander.JCommander;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to trigger the script
 * A twitch streamer will be selected with the key and executed depending on the values passes
 * through the command line
 *
 * @author alexpages
 */

@Slf4j
public class ClipFarmer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClipFarmer.class);

    /**
     * Execute main batch
     *
     * @param args
     */
    public static void main(String[] args) {

        final ClipFarmerLogic clipFarmerLogic;
        final ClipFarmerArgs clipFarmerArgs;

        try {
            ClipFarmerArgs.Builder clipFarmerArgsBuilder = new ClipFarmerArgs.Builder();
            new JCommander(clipFarmerArgsBuilder).parse(args);
            clipFarmerArgs = clipFarmerArgsBuilder.build();

        } catch (Exception e) {
            LOGGER.warn("Twitch streamer is not present in list");
            return;
        }

        StreamerNameEnum streamerNameEnum = clipFarmerArgs.getStreamerNameEnum();
        //TODO implement logic
        System.out.println("Hello world!");
    }
}