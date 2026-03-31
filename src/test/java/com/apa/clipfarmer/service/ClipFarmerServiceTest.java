package com.apa.clipfarmer.service;

import com.apa.clipfarmer.logic.EmailNotificationLogic;
import com.apa.clipfarmer.logic.twitch.TwitchClipDownloader;
import com.apa.clipfarmer.logic.twitch.TwitchClipFetcherLogic;
import com.apa.clipfarmer.logic.video.VideoLogic;
import com.apa.clipfarmer.logic.youtube.YoutubeUploaderLogic;
import com.apa.clipfarmer.mapper.TwitchClipMapper;
import com.apa.clipfarmer.model.TwitchClip;
import com.apa.clipfarmer.utils.YoutubeUtils;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ClipFarmerService}.
 *
 * <p>Note: {@code TwitchAuthLogic.getOAuthToken()} is a static method that cannot be mocked
 * with plain Mockito. In tests it throws (no Twitch credentials), so the OAuth token is null.
 * Downstream stubs use {@code any()} to accommodate the null value.
 * TODO (refactor): convert TwitchAuthLogic into an injectable Spring bean.
 *
 * @author alexpages
 */
@ExtendWith(MockitoExtension.class)
class ClipFarmerServiceTest {

    @Mock private TwitchClipFetcherLogic twitchClipFetcherLogic;
    @Mock private TwitchClipDownloader twitchClipDownloader;
    @Mock private SqlSessionFactory sqlSessionFactory;
    @Mock private SqlSession sqlSession;
    @Mock private TwitchClipMapper twitchClipMapper;
    @Mock private VideoLogic videoLogic;
    @Mock private EmailNotificationLogic emailNotificationLogic;
    @Mock private YoutubeUtils youtubeUtils;
    @Mock private YoutubeUploaderLogic youtubeUploaderLogic;

    @InjectMocks
    private ClipFarmerService clipFarmerService;

    // ----- early-exit tests (no DB interaction needed) -----

    @Test
    void execute_invalidArgs_returnsEarlyWithoutProcessing() {
        clipFarmerService.execute(new String[]{});

        verifyNoInteractions(twitchClipFetcherLogic, videoLogic, emailNotificationLogic);
    }

    @Test
    void execute_invalidStreamerName_returnsEarlyWithoutProcessing() {
        clipFarmerService.execute(new String[]{"streamerName=unknownstreamer"});

        verifyNoInteractions(twitchClipFetcherLogic, videoLogic, emailNotificationLogic);
    }

    // ----- tests that reach the DB / downstream layer -----

    @Test
    void execute_validStreamer_noClips_callsVideoLogicWithEmptyList() throws Exception {
        setupSession();
        when(twitchClipFetcherLogic.getTwitchClips(anyString(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(videoLogic.concatenateVideos(any(), anyString())).thenReturn(null);
        when(youtubeUtils.createVideoDescription(anyString())).thenReturn("desc");
        when(youtubeUtils.createVideoTitle(anyString(), anyString(), any())).thenReturn("title");

        clipFarmerService.execute(new String[]{"streamerName=xqc"});

        verify(twitchClipFetcherLogic).getTwitchClips(anyString(), any(), anyInt(), anyInt(), anyInt());
        verify(videoLogic).concatenateVideos(List.of(), "build/output/xqc_merged_video.mp4");
    }

    @Test
    void execute_validStreamer_clipAlreadyInDb_isSkipped() throws Exception {
        TwitchClip existingClip = clip("clip-001", "xqc");
        setupSession();
        when(twitchClipFetcherLogic.getTwitchClips(anyString(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(existingClip));
        when(twitchClipMapper.selectClipByClipId("clip-001")).thenReturn(existingClip);
        when(videoLogic.concatenateVideos(any(), anyString())).thenReturn("/output/video.mp4");
        when(youtubeUtils.createVideoDescription(anyString())).thenReturn("desc");
        when(youtubeUtils.createVideoTitle(anyString(), anyString(), any())).thenReturn("title");

        clipFarmerService.execute(new String[]{"streamerName=xqc"});

        verify(twitchClipMapper, never()).insertClip(any());
        verify(twitchClipDownloader, never()).downloadFile(anyString(), any(), any());
    }

    @Test
    void execute_validStreamer_newClip_downloadsAndInsertsIt() throws Exception {
        TwitchClip newClip = clip("clip-new", "xqc");
        setupSession();
        when(twitchClipFetcherLogic.getTwitchClips(anyString(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(newClip));
        when(twitchClipMapper.selectClipByClipId("clip-new")).thenReturn(null);
        when(videoLogic.concatenateVideos(any(), anyString())).thenReturn("/output/video.mp4");
        when(youtubeUtils.createVideoDescription(anyString())).thenReturn("desc");
        when(youtubeUtils.createVideoTitle(anyString(), anyString(), any())).thenReturn("title");

        clipFarmerService.execute(new String[]{"streamerName=xqc"});

        verify(twitchClipDownloader).downloadFile(anyString(), any(), any());
        verify(twitchClipMapper).insertClip(newClip);
        verify(sqlSession).commit();
    }

    @Test
    void execute_videoCreated_uploadsToYoutubeAndSendsEmail() throws Exception {
        setupSession();
        when(twitchClipFetcherLogic.getTwitchClips(anyString(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(videoLogic.concatenateVideos(any(), anyString())).thenReturn("/output/video.mp4");
        when(youtubeUtils.createVideoDescription(anyString())).thenReturn("desc");
        when(youtubeUtils.createVideoTitle(anyString(), anyString(), any())).thenReturn("title");

        clipFarmerService.execute(new String[]{"streamerName=xqc"});

        verify(youtubeUploaderLogic).uploadHighlightVideo("title", "desc", "/output/video.mp4", "xqc");
        verify(emailNotificationLogic).sendEmail(anyString(), anyString(), anyLong());
    }

    @Test
    void execute_processClipThrows_isSkippedAndExecutionContinues() throws Exception {
        TwitchClip badClip = clip("clip-bad", "xqc");
        setupSession();
        when(twitchClipFetcherLogic.getTwitchClips(anyString(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(badClip));
        when(twitchClipMapper.selectClipByClipId("clip-bad")).thenReturn(null);
        doThrow(new RuntimeException("Download failed"))
                .when(twitchClipDownloader).downloadFile(anyString(), any(), any());
        when(videoLogic.concatenateVideos(any(), anyString())).thenReturn(null);
        when(youtubeUtils.createVideoDescription(anyString())).thenReturn("desc");
        when(youtubeUtils.createVideoTitle(anyString(), anyString(), any())).thenReturn("title");

        // Should NOT throw — processClip catches the exception and skips the clip
        clipFarmerService.execute(new String[]{"streamerName=xqc"});

        verify(twitchClipMapper, never()).insertClip(any());
        // Execution continues: video and upload steps are still reached
        verify(videoLogic).concatenateVideos(any(), anyString());
    }

    @Test
    void execute_mixedClips_insertsNewAndSkipsExisting() throws Exception {
        TwitchClip existing = clip("clip-exists", "xqc");
        TwitchClip newClip  = clip("clip-new",    "xqc");
        setupSession();
        when(twitchClipFetcherLogic.getTwitchClips(anyString(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(existing, newClip));
        when(twitchClipMapper.selectClipByClipId("clip-exists")).thenReturn(existing);
        when(twitchClipMapper.selectClipByClipId("clip-new")).thenReturn(null);
        when(videoLogic.concatenateVideos(any(), anyString())).thenReturn("/output/video.mp4");
        when(youtubeUtils.createVideoDescription(anyString())).thenReturn("desc");
        when(youtubeUtils.createVideoTitle(anyString(), anyString(), any())).thenReturn("title");

        clipFarmerService.execute(new String[]{"streamerName=xqc"});

        verify(twitchClipMapper, never()).insertClip(existing);
        verify(twitchClipMapper).insertClip(newClip);
    }

    // ----- helpers -----

    private void setupSession() {
        lenient().when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
        lenient().when(sqlSession.getMapper(TwitchClipMapper.class)).thenReturn(twitchClipMapper);
    }

    private TwitchClip clip(String clipId, String broadcasterId) {
        return new TwitchClip(null, clipId, "Title", "creator", 500,
                LocalDateTime.now(), broadcasterId, "https://twitch.tv/clip/" + clipId, 30, "en");
    }
}
