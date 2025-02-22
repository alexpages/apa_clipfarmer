package com.apa.clipfarmer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

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
}
