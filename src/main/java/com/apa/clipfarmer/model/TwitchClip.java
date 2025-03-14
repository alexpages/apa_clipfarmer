package com.apa.clipfarmer.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class that represents the TwitchClip model
 *
 * @author alexpages
 */
@Data
@AllArgsConstructor
public class TwitchClip {

    private Long id;
    private String clipId;
    private String title;
    private String creatorName;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private String broadcasterId;
    private String url;
    private int duration; // in seconds
    private String language;
}
