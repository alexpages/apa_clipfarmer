package com.apa.clipfarmer.logic.twitch;

import com.apa.clipfarmer.model.TwitchClip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TwitchClipFetcherLogic}.
 *
 * <p>The core logic under test is the JSON parsing, filtering (duration, view count),
 * and sorting behaviour inside {@code convertResponseBodyToTwitchClips}.
 * {@link RestTemplate} is intercepted via {@code mockConstruction} since it is
 * instantiated inline (not injected).
 *
 * @author alexpages
 */
@ExtendWith(MockitoExtension.class)
class TwitchClipFetcherLogicTest {

    @Mock
    private TwitchUserLogic twitchUserLogic;

    @InjectMocks
    private TwitchClipFetcherLogic twitchClipFetcherLogic;

    @BeforeEach
    void setUp() {
        when(twitchUserLogic.getBroadcasterId(anyString(), any())).thenReturn("broadcaster-123");
    }

    @Test
    void getTwitchClips_filtersOutClipsBelowMinDuration() {
        String body = twitchResponse(
                clip("id-short", 500, 5, "2024-01-10T10:00:00Z"),   // duration 5 < min 10 → excluded
                clip("id-long",  500, 15, "2024-01-10T10:00:00Z")   // duration 15 >= 10 → included
        );

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7);

            assertThat(clips).hasSize(1);
            assertThat(clips.get(0).getClipId()).isEqualTo("id-long");
        }
    }

    @Test
    void getTwitchClips_filtersOutClipsBelowMinViews() {
        String body = twitchResponse(
                clip("id-low-views",  99,  30, "2024-01-10T10:00:00Z"),  // 99 < 100 → excluded
                clip("id-high-views", 100, 30, "2024-01-10T10:00:00Z")   // 100 >= 100 → included
        );

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7);

            assertThat(clips).hasSize(1);
            assertThat(clips.get(0).getClipId()).isEqualTo("id-high-views");
        }
    }

    @Test
    void getTwitchClips_sortsClipsByViewCountDescending() {
        String body = twitchResponse(
                clip("id-200", 200, 30, "2024-01-10T10:00:00Z"),
                clip("id-500", 500, 30, "2024-01-10T10:00:00Z"),
                clip("id-350", 350, 30, "2024-01-10T10:00:00Z")
        );

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7);

            assertThat(clips).hasSize(3);
            assertThat(clips.get(0).getViewCount()).isEqualTo(500);
            assertThat(clips.get(1).getViewCount()).isEqualTo(350);
            assertThat(clips.get(2).getViewCount()).isEqualTo(200);
        }
    }

    @Test
    void getTwitchClips_emptyDataArray_returnsEmptyList() {
        String body = "{\"data\":[],\"pagination\":{}}";

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7);

            assertThat(clips).isEmpty();
        }
    }

    @Test
    void getTwitchClips_allClipsPassFilters_returnsAll() {
        String body = twitchResponse(
                clip("id-1", 400, 20, "2024-01-10T10:00:00Z"),
                clip("id-2", 800, 60, "2024-01-10T10:00:00Z")
        );

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7);

            assertThat(clips).hasSize(2);
        }
    }

    @Test
    void getTwitchClips_apiThrowsException_wrapsInRuntimeException() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenThrow(new RuntimeException("Network error")))) {

            assertThatThrownBy(() ->
                    twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to fetch Twitch clips");
        }
    }

    @Test
    void getTwitchClips_durationExactlyAtMinimum_isIncluded() {
        String body = twitchResponse(clip("id-exact", 500, 10, "2024-01-10T10:00:00Z")); // duration == min

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7);

            assertThat(clips).hasSize(1);
        }
    }

    @Test
    void getTwitchClips_viewCountExactlyAtMinimum_isIncluded() {
        String body = twitchResponse(clip("id-exact-views", 100, 30, "2024-01-10T10:00:00Z")); // views == min

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7);

            assertThat(clips).hasSize(1);
        }
    }

    @Test
    void getTwitchClips_responseWithNoPaginationCursor_doesNotFetchNextPage() {
        String body = "{\"data\":[" + clip("id-1", 200, 30, "2024-01-10T10:00:00Z") + "],\"pagination\":{}}";

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7);

            // Only one page fetched — RestTemplate.exchange called exactly once
            assertThat(mocked.constructed()).hasSize(1);
            assertThat(clips).hasSize(1);
        }
    }

    @Test
    void getTwitchClips_responseWithPaginationCursor_fetchesNextPage() {
        String page1 = "{\"data\":[" + clip("id-page1", 200, 30, "2024-01-10T10:00:00Z") + "],"
                + "\"pagination\":{\"cursor\":\"next-cursor\"}}";
        String page2 = "{\"data\":[" + clip("id-page2", 300, 30, "2024-01-10T10:00:00Z") + "],"
                + "\"pagination\":{}}";

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenReturn(new ResponseEntity<>(page1, HttpStatus.OK))
                        .thenReturn(new ResponseEntity<>(page2, HttpStatus.OK)))) {

            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7);

            assertThat(clips).hasSize(2);
            assertThat(clips.stream().map(TwitchClip::getClipId)).containsExactlyInAnyOrder("id-page1", "id-page2");
        }
    }

    @Test
    void getTwitchClips_missingDataField_returnsEmptyList() {
        String body = "{\"pagination\":{}}";

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                        .thenReturn(new ResponseEntity<>(body, HttpStatus.OK)))) {

            List<TwitchClip> clips = twitchClipFetcherLogic.getTwitchClips("xqc", "token", 10, 100, 7);

            assertThat(clips).isEmpty();
        }
    }

    // ----- JSON helpers -----

    private String twitchResponse(String... clips) {
        return "{\"data\":[" + String.join(",", clips) + "],\"pagination\":{}}";
    }

    private String clip(String id, int viewCount, int duration, String createdAt) {
        return String.format("""
                {"id":"%s","title":"Title %s","creator_name":"creator",
                 "view_count":%d,"created_at":"%s","broadcaster_id":"broadcaster-123",
                 "url":"https://twitch.tv/clip/%s","duration":%d,"language":"en"}
                """, id, id, viewCount, createdAt, id, duration);
    }
}
