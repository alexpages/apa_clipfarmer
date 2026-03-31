package com.apa.clipfarmer.utils;

import com.apa.clipfarmer.mapper.TwitchHighlightMapper;
import com.apa.clipfarmer.mapper.TwitchStreamerMapper;
import com.apa.clipfarmer.model.TwitchStreamer;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link YoutubeUtils}.
 *
 * @author alexpages
 */
@ExtendWith(MockitoExtension.class)
class YoutubeUtilsTest {

    @Mock private SqlSessionFactory sqlSessionFactory;
    @Mock private SqlSession sqlSession;
    @Mock private TwitchStreamerMapper streamerMapper;
    @Mock private TwitchHighlightMapper highlightMapper;

    @InjectMocks
    private YoutubeUtils youtubeUtils;

    private TwitchStreamer streamer;

    @BeforeEach
    void setUp() {
        streamer = new TwitchStreamer();
        streamer.setTwitchStreamerName("xqc");
        streamer.setBroadcasterId("broadcaster-xqc");

        lenient().when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
        lenient().when(sqlSession.getMapper(TwitchStreamerMapper.class)).thenReturn(streamerMapper);
        // lenient: not all tests use the highlight mapper
        lenient().when(sqlSession.getMapper(TwitchHighlightMapper.class)).thenReturn(highlightMapper);
    }

    @Test
    void createVideoTitle_highlight_firstVideo_containsNumberOne() {
        when(streamerMapper.selectByTwitchStreamerName("xqc")).thenReturn(streamer);
        when(highlightMapper.getLastHighlightIdByCreatorName("xqc")).thenReturn(null);

        String title = youtubeUtils.createVideoTitle("xqc", "someFile.mp4", true);

        assertThat(title).contains("XQC").contains("HIGHLIGHTS").contains("#1");
    }

    @Test
    void createVideoTitle_highlight_existingVideos_incrementsId() {
        when(streamerMapper.selectByTwitchStreamerName("xqc")).thenReturn(streamer);
        when(highlightMapper.getLastHighlightIdByCreatorName("xqc")).thenReturn(5);

        String title = youtubeUtils.createVideoTitle("xqc", "someFile.mp4", true);

        assertThat(title).contains("XQC").contains("#6");
    }

    @Test
    void createVideoTitle_notHighlight_containsStreamerAndTitle() {
        when(streamerMapper.selectByTwitchStreamerName("xqc")).thenReturn(streamer);

        String title = youtubeUtils.createVideoTitle("xqc", "MyClip", false);

        assertThat(title).contains("xqc").contains("MyClip");
    }

    @Test
    void createVideoTitle_streamerNotFound_throwsIllegalStateException() {
        when(streamerMapper.selectByTwitchStreamerName("unknown")).thenReturn(null);

        assertThatThrownBy(() -> youtubeUtils.createVideoTitle("unknown", "title", true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createVideoDescription_validStreamer_containsStreamerNameAndTwitchUrl() {
        when(streamerMapper.selectByTwitchStreamerName("xqc")).thenReturn(streamer);

        String description = youtubeUtils.createVideoDescription("xqc");

        assertThat(description).contains("xqc").contains("twitch.tv");
    }

    @Test
    void createVideoDescription_streamerNotFound_throwsIllegalStateException() {
        when(streamerMapper.selectByTwitchStreamerName("ghost")).thenReturn(null);

        assertThatThrownBy(() -> youtubeUtils.createVideoDescription("ghost"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createVideoDescription_containsContactEmail() {
        when(streamerMapper.selectByTwitchStreamerName("xqc")).thenReturn(streamer);

        String description = youtubeUtils.createVideoDescription("xqc");

        assertThat(description).contains("twitchclipmediacontact@gmail.com");
    }

    @Test
    void createVideoTitle_highlight_containsCurrentMonth() {
        when(streamerMapper.selectByTwitchStreamerName("xqc")).thenReturn(streamer);
        when(highlightMapper.getLastHighlightIdByCreatorName("xqc")).thenReturn(null);

        String title = youtubeUtils.createVideoTitle("xqc", "file.mp4", true);

        // Title must include the current month in uppercase (e.g. "APRIL", "JANUARY")
        String currentMonth = java.time.LocalDateTime.now()
                .getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                .toUpperCase();
        assertThat(title).contains(currentMonth);
    }
}
