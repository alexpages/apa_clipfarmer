package com.apa.clipfarmer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

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
