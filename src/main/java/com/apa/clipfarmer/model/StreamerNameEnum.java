package com.apa.clipfarmer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum for twitch streamers
 *
 * @author alexpages
 */
@AllArgsConstructor
@Getter
public enum StreamerNameEnum {

    JASONTHEWEEN("jasontheween"),
    INVALID("");

    private final String name;


    /**
     * Returns the StreamerNameEnum from a String value
     *
     * @param streamerName The name of the Streamer
     * @return The StreamerNameEnum
     */
    public static StreamerNameEnum fromString(String streamerName) {
        for (StreamerNameEnum streamerNameEnum : values()) {
            if (streamerNameEnum.getName().equals(streamerName)) {
                return streamerNameEnum;
            }
        }
        return INVALID;
    }
}
