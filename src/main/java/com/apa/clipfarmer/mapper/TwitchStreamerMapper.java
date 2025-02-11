package com.apa.clipfarmer.mapper;

import lombok.Data;

import java.util.ArrayList;

/**
 * Class that represents the TwitchStreamer model
 *
 * @author alexpages
 */
@Data
public class TwitchStreamerMapper {

    private int id;
    private String twitchStreamerName;
    private String broadcasterId;
    private ArrayList<String> addedClips;

}
