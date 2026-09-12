package com.apa.clipfarmer.logic.twitch;

import static org.assertj.core.api.Assertions.assertThat;

import com.apa.clipfarmer.model.TwitchClip;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class TwitchClipFetcherLogicTest {

    private static final int CLIP_DURATION = 10;
    private static final int MIN_VIEWS = 400;

    @Test
    void convertResponseBodyToTwitchClipsFiltersByDurationAndViewsThenSortsByViewCountDescending() {
        String responseBody = """
                {
                  "data": [
                    {"id":"a1","title":"Clip A","creator_name":"Streamer","view_count":1000,
                     "created_at":"2024-01-01T12:00:00Z","broadcaster_id":"123",
                     "url":"http://twitch.tv/clip/a1","duration":15,"language":"en"},
                    {"id":"b1","title":"Clip B (too short)","creator_name":"Streamer","view_count":2000,
                     "created_at":"2024-01-01T12:00:00Z","broadcaster_id":"123",
                     "url":"http://twitch.tv/clip/b1","duration":5,"language":"en"},
                    {"id":"c1","title":"Clip C (too few views)","creator_name":"Streamer","view_count":300,
                     "created_at":"2024-01-01T12:00:00Z","broadcaster_id":"123",
                     "url":"http://twitch.tv/clip/c1","duration":20,"language":"en"},
                    {"id":"d1","title":"Clip D","creator_name":"Streamer","view_count":1500,
                     "created_at":"2024-01-01T12:00:00Z","broadcaster_id":"123",
                     "url":"http://twitch.tv/clip/d1","duration":12,"language":"en"}
                  ]
                }
                """;

        List<TwitchClip> clips = TwitchClipFetcherLogic.convertResponseBodyToTwitchClips(
                ResponseEntity.ok(responseBody), CLIP_DURATION, MIN_VIEWS);

        assertThat(clips).extracting(TwitchClip::getClipId).containsExactly("d1", "a1");
    }

    @Test
    void convertResponseBodyToTwitchClipsReturnsEmptyListWhenNoBody() {
        List<TwitchClip> clips = TwitchClipFetcherLogic.convertResponseBodyToTwitchClips(
                ResponseEntity.ok().build(), CLIP_DURATION, MIN_VIEWS);

        assertThat(clips).isEmpty();
    }

    @Test
    void convertResponseBodyToTwitchClipsReturnsEmptyListWhenNoDataField() {
        List<TwitchClip> clips = TwitchClipFetcherLogic.convertResponseBodyToTwitchClips(
                ResponseEntity.ok("{}"), CLIP_DURATION, MIN_VIEWS);

        assertThat(clips).isEmpty();
    }
}
