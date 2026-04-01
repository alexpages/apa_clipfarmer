package com.apa.clipfarmer.model;

import java.util.Arrays;
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

    JASONTHEWEEN("jasontheween", "en"),
    STABLERONALDO("stableronaldo", "en"),
    LACY("lacy", "en"),
    XQC("xqc", "en"),
    EDWARDKSO("edwardkso", "en"),
    VALKYRAE("valkyrae", "en"),
    XCHOCOBARS("xchocobars", "en"),
    MIZKIF("mizkif", "en"),
    INVALID("", "");

    private final String name;
    private final String language;

    /**
     * Returns the TwitchStreamerNameEnum from a String value
     *
     * @param streamerName The name of the Streamer
     * @return The TwitchStreamerNameEnum
     */
    public static TwitchStreamerNameEnum fromString(String streamerName) {
        return Arrays.stream(values())
                .filter(e -> e.getName().equals(streamerName))
                .findFirst()
                .orElse(INVALID);
    }
}
