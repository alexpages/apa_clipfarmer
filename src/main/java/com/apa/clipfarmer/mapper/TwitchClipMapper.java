package com.apa.clipfarmer.mapper;

import com.apa.clipfarmer.model.TwitchClip;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
