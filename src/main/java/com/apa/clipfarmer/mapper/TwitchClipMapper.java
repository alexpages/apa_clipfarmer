package com.apa.clipfarmer.mapper;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Class that represents the TwitchClip mapper from MyBatis
 *
 * @author alexpages
 */
@Data
public class TwitchClipMapper {

    private int id;
    private String clipId;
    private String title;
    private String creatorName;
    private int viewCount;
    private LocalDateTime createdAt;

}
