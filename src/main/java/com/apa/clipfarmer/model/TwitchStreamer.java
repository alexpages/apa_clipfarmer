package com.apa.clipfarmer.model;

import lombok.Data;

import java.util.ArrayList;

/**
 * Class that represents the TwitchStreamer model
 *
 * @author alexpages
 */
@Data
public class TwitchStreamer {

    private int id;
    private String streamerName;
    private String broadcasterId;
    private ArrayList<String> addedClips;

}
