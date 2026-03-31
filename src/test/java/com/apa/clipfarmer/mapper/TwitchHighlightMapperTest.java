package com.apa.clipfarmer.mapper;

import com.apa.clipfarmer.AbstractIntegrationTest;
import com.apa.clipfarmer.model.TwitchHighlight;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link TwitchHighlightMapper}.
 * Each test runs inside a transaction that is rolled back after completion.
 *
 * @author alexpages
 */
@Transactional
class TwitchHighlightMapperTest extends AbstractIntegrationTest {

    @Autowired
    private TwitchHighlightMapper mapper;

    @Test
    void insertHighlight_thenGetLastId_returnsInsertedId() {
        TwitchHighlight highlight = highlight("testcreator", "https://youtube.com/watch?v=abc", 1);
        mapper.insertHighlight(highlight);

        int lastId = mapper.getLastHighlightIdByCreatorName("testcreator");

        assertThat(lastId).isEqualTo(1);
    }

    @Test
    void getLastHighlightIdByCreatorName_multipleInserts_returnsLatest() {
        LocalDateTime earlier = LocalDateTime.now().minusHours(2);
        LocalDateTime later = LocalDateTime.now();

        TwitchHighlight first = highlight("streamer", "https://youtube.com/watch?v=first", 1);
        first.setCreatedAt(earlier);

        TwitchHighlight second = highlight("streamer", "https://youtube.com/watch?v=second", 2);
        second.setCreatedAt(later);

        mapper.insertHighlight(first);
        mapper.insertHighlight(second);

        int lastId = mapper.getLastHighlightIdByCreatorName("streamer");

        assertThat(lastId).isEqualTo(2);
    }

    @Test
    void getLastHighlightIdByCreatorName_differentCreators_returnsOnlyOwnLatest() {
        mapper.insertHighlight(highlight("creator-a", "https://youtube.com/watch?v=a", 10));
        mapper.insertHighlight(highlight("creator-b", "https://youtube.com/watch?v=b", 99));

        int lastIdA = mapper.getLastHighlightIdByCreatorName("creator-a");

        assertThat(lastIdA).isEqualTo(10);
    }

    private TwitchHighlight highlight(String creatorName, String youtubeUrl, int highlightId) {
        TwitchHighlight h = new TwitchHighlight();
        h.setHighlightId(highlightId);
        h.setTitle("Highlight #" + highlightId);
        h.setCreatorName(creatorName);
        h.setYoutubeUrl(youtubeUrl);
        h.setCreatedAt(LocalDateTime.now());
        return h;
    }
}
