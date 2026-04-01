package com.apa.clipfarmer.mapper;

import com.apa.clipfarmer.AbstractIntegrationTest;
import com.apa.clipfarmer.model.TwitchStreamer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link TwitchStreamerMapper}.
 * Each test runs inside a transaction that is rolled back after completion,
 * leaving the database clean for the next test.
 *
 * @author alexpages
 */
@Transactional
class TwitchStreamerMapperTest extends AbstractIntegrationTest {

    @Autowired
    private TwitchStreamerMapper mapper;

    @Test
    void insertStreamer_thenSelectById_returnsStreamer() {
        TwitchStreamer streamer = streamer("teststreamer", "broadcaster-001");
        mapper.insertStreamer(streamer);

        TwitchStreamer found = mapper.selectStreamerById(streamer.getId());

        assertThat(found).isNotNull();
        assertThat(found.getTwitchStreamerName()).isEqualTo("teststreamer");
    }

    @Test
    void selectByBroadcasterId_returnsCorrectStreamer() {
        mapper.insertStreamer(streamer("astreamer", "broadcaster-002"));

        TwitchStreamer found = mapper.selectByBroadcasterId("broadcaster-002");

        assertThat(found).isNotNull();
        assertThat(found.getTwitchStreamerName()).isEqualTo("astreamer");
    }

    @Test
    void selectByTwitchStreamerName_returnsCorrectStreamer() {
        mapper.insertStreamer(streamer("namedstreamer", "broadcaster-003"));

        TwitchStreamer found = mapper.selectByTwitchStreamerName("namedstreamer");

        assertThat(found).isNotNull();
        assertThat(found.getBroadcasterId()).isEqualTo("broadcaster-003");
    }

    @Test
    void selectAllStreamers_returnsAllInserted() {
        mapper.insertStreamer(streamer("streamer-a", "bcast-a"));
        mapper.insertStreamer(streamer("streamer-b", "bcast-b"));

        List<TwitchStreamer> all = mapper.selectAllStreamers();

        assertThat(all).hasSize(2);
    }

    @Test
    void deleteStreamer_removesItFromDb() {
        TwitchStreamer streamer = streamer("todelete", "broadcaster-del");
        mapper.insertStreamer(streamer);

        mapper.deleteStreamer(streamer.getId());

        assertThat(mapper.selectStreamerById(streamer.getId())).isNull();
    }

    @Test
    void updateStreamer_changesName() {
        TwitchStreamer streamer = streamer("original", "broadcaster-upd");
        mapper.insertStreamer(streamer);

        streamer.setTwitchStreamerName("updated");
        mapper.updateStreamer(streamer);

        TwitchStreamer found = mapper.selectStreamerById(streamer.getId());
        assertThat(found.getTwitchStreamerName()).isEqualTo("updated");
    }

    private TwitchStreamer streamer(String name, String broadcasterId) {
        TwitchStreamer s = new TwitchStreamer();
        s.setTwitchStreamerName(name);
        s.setBroadcasterId(broadcasterId);
        return s;
    }
}
