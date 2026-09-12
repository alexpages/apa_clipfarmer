package com.apa.clipfarmer.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.apa.clipfarmer.mapper.TwitchHighlightMapper;
import com.apa.clipfarmer.mapper.TwitchStreamerMapper;
import com.apa.clipfarmer.model.TwitchStreamer;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YoutubeUtilsTest {

    private static final int PREVIOUS_HIGHLIGHT_ID = 3;

    @Mock
    private SqlSessionFactory sqlSessionFactory;

    @Mock
    private SqlSession sqlSession;

    @Mock
    private TwitchStreamerMapper twitchStreamerMapper;

    @Mock
    private TwitchHighlightMapper twitchHighlightMapper;

    private YoutubeUtils youtubeUtils;

    @BeforeEach
    void setUp() {
        youtubeUtils = new YoutubeUtils(sqlSessionFactory);
        when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.getMapper(TwitchStreamerMapper.class)).thenReturn(twitchStreamerMapper);
    }

    @Test
    void createVideoTitleForHighlightIncludesIncrementedHighlightNumber() {
        TwitchStreamer streamer = new TwitchStreamer();
        streamer.setTwitchStreamerName("jasontheween");
        when(twitchStreamerMapper.selectByTwitchStreamerName("jasontheween")).thenReturn(streamer);
        when(sqlSession.getMapper(TwitchHighlightMapper.class)).thenReturn(twitchHighlightMapper);
        when(twitchHighlightMapper.getLastHighlightIdByCreatorName("jasontheween")).thenReturn(PREVIOUS_HIGHLIGHT_ID);

        String title = youtubeUtils.createVideoTitle("jasontheween", "ignored", true);

        assertThat(title).startsWith("JASONTHEWEEN HIGHLIGHTS TWITCH").endsWith("#4");
    }

    @Test
    void createVideoTitleForNonHighlightUsesGivenTitleVerbatim() {
        TwitchStreamer streamer = new TwitchStreamer();
        streamer.setTwitchStreamerName("jasontheween");
        when(twitchStreamerMapper.selectByTwitchStreamerName("jasontheween")).thenReturn(streamer);

        String title = youtubeUtils.createVideoTitle("jasontheween", "Epic Clip", false);

        assertThat(title).isEqualTo("Twitch Clip - jasontheween - Epic Clip");
    }

    @Test
    void createVideoTitleThrowsWhenStreamerNotFound() {
        when(twitchStreamerMapper.selectByTwitchStreamerName("unknown")).thenReturn(null);

        assertThatThrownBy(() -> youtubeUtils.createVideoTitle("unknown", "title", false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createVideoDescriptionIncludesStreamerNameAndContact() {
        TwitchStreamer streamer = new TwitchStreamer();
        streamer.setTwitchStreamerName("jasontheween");
        when(twitchStreamerMapper.selectByTwitchStreamerName("jasontheween")).thenReturn(streamer);

        String description = youtubeUtils.createVideoDescription("jasontheween");

        assertThat(description).contains("jasontheween").contains("twitchclipmediacontact@gmail.com");
    }

    @Test
    void createVideoDescriptionThrowsWhenStreamerNotFound() {
        when(twitchStreamerMapper.selectByTwitchStreamerName("unknown")).thenReturn(null);

        assertThatThrownBy(() -> youtubeUtils.createVideoDescription("unknown"))
                .isInstanceOf(IllegalStateException.class);
    }
}
