package com.apa.clipfarmer.mapper;

import com.apa.clipfarmer.AbstractIntegrationTest;
import com.apa.clipfarmer.model.TwitchClip;
import com.apa.clipfarmer.model.TwitchStreamer;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link TwitchClipMapper}.
 * Each test runs inside a transaction that is rolled back after completion.
 * A streamer is inserted in {@code @BeforeEach} since clips have a FK on broadcaster_id.
 *
 * @author alexpages
 */
@Transactional
class TwitchClipMapperTest extends AbstractIntegrationTest {

    private static final String BROADCASTER_ID = "test-broadcaster";

    @Autowired
    private TwitchClipMapper clipMapper;

    @Autowired
    private TwitchStreamerMapper streamerMapper;

    @BeforeEach
    void insertStreamer() {
        TwitchStreamer streamer = new TwitchStreamer();
        streamer.setTwitchStreamerName("teststreamer");
        streamer.setBroadcasterId(BROADCASTER_ID);
        streamerMapper.insertStreamer(streamer);
    }

    @Test
    void insertClip_thenSelectByClipId_returnsClip() {
        TwitchClip clip = clip("clip-001", "My Clip");
        clipMapper.insertClip(clip);

        TwitchClip found = clipMapper.selectClipByClipId("clip-001");

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("My Clip");
    }

    @Test
    void insertClip_thenSelectById_returnsClip() {
        TwitchClip clip = clip("clip-002", "Another Clip");
        clipMapper.insertClip(clip);

        TwitchClip found = clipMapper.selectClipById(clip.getId().intValue());

        assertThat(found).isNotNull();
        assertThat(found.getClipId()).isEqualTo("clip-002");
    }

    @Test
    void selectAllClips_returnsAllInserted() {
        clipMapper.insertClip(clip("clip-003", "Clip A"));
        clipMapper.insertClip(clip("clip-004", "Clip B"));

        List<TwitchClip> all = clipMapper.selectAllClips();

        assertThat(all).hasSize(2);
    }

    @Test
    void selectClipByClipId_notFound_returnsNull() {
        TwitchClip found = clipMapper.selectClipByClipId("non-existent-id");

        assertThat(found).isNull();
    }

    @Test
    void insertClip_generatesId() {
        TwitchClip clip = clip("clip-005", "Has ID");
        clipMapper.insertClip(clip);

        assertThat(clip.getId()).isNotNull().isPositive();
    }

    private TwitchClip clip(String clipId, String title) {
        return new TwitchClip(
                null,
                clipId,
                title,
                "creator",
                500,
                LocalDateTime.now(),
                BROADCASTER_ID,
                "https://twitch.tv/clip/" + clipId,
                30,
                "en"
        );
    }
}
