package com.apa.clipfarmer.mapper;

import com.apa.clipfarmer.model.TwitchClip;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis Mapper Interface for TwitchClip database operations.
 *
 * @author alexpages
 */
@Mapper
public interface TwitchClipMapper {

    List<TwitchClip> selectAllClips();

    TwitchClip selectClipById(int id);

    TwitchClip selectClipByClipId(String clipId);

    void insertClip(TwitchClip clip);

}
