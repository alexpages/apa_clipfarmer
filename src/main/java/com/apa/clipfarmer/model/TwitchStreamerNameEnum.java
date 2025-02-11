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
public enum TwitchStreamerNameEnum {

    JASONTHEWEEN("jasontheween"),
    INVALID("");

    private final String name;

    /**
     * Returns the TwitchStreamerNameEnum from a String value
     *
     * @param streamerName The name of the Streamer
     * @return The TwitchStreamerNameEnum
     */
    public static TwitchStreamerNameEnum fromString(String streamerName) {
        for (TwitchStreamerNameEnum twitchStreamerNameEnum : values()) {
            if (twitchStreamerNameEnum.getName().equals(streamerName)) {
                return twitchStreamerNameEnum;
            }
        }
        return INVALID;
    }
}
