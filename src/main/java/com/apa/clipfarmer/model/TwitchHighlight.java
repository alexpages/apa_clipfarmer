package com.apa.clipfarmer.model;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * Class that represents the TwitchClip model
 *
 * @author alexpages
 */
@Data
public class TwitchHighlight {

    private Long id;
    private Integer highlightId;
    private String title;
    private String creatorName;
    private String youtubeUrl;
    private LocalDateTime createdAt;

}
