package com.apa.clipfarmer.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Class that represents the TwitchClip model
 *
 * @author alexpages
 */
@Data
public class TwitchClip {

    private int id;
    private String clipId;
    private String title;
    private String creatorName;
    private int viewCount;
    private LocalDateTime createdAt;

}
