package com.apa.clipfarmer.model;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.Getter;
import lombok.Setter;

/**
 * Class representing JVM args passed through command line
 *
 * @author alexpages
 */
@Getter
public final class ClipFarmerArgs {

    private StreamerNameEnum streamerNameEnum;

    /**
     * Class to build ClipFarmerArgs
     *
     * @author alexpages
     */
    @Setter
    @Parameters(separators = "=")
    public static class Builder {

        @Parameter(names = "streamerName", required = true)
        private String streamerName;

        public ClipFarmerArgs build(){
            ClipFarmerArgs clipFarmerArgs = new ClipFarmerArgs();
            clipFarmerArgs.streamerNameEnum = StreamerNameEnum.fromString(this.streamerName);
            return clipFarmerArgs;
        }
    }
}
