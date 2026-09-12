package com.apa.clipfarmer.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TwitchStreamerNameEnumTest {

    @Test
    void fromStringReturnsMatchingEnumForKnownStreamer() {
        assertThat(TwitchStreamerNameEnum.fromString("jasontheween")).isEqualTo(TwitchStreamerNameEnum.JASONTHEWEEN);
        assertThat(TwitchStreamerNameEnum.fromString("xqc")).isEqualTo(TwitchStreamerNameEnum.XQC);
    }

    @Test
    void fromStringReturnsInvalidForUnknownStreamer() {
        assertThat(TwitchStreamerNameEnum.fromString("not_a_real_streamer")).isEqualTo(TwitchStreamerNameEnum.INVALID);
    }

    @Test
    void fromStringReturnsInvalidForNullInput() {
        assertThat(TwitchStreamerNameEnum.fromString(null)).isEqualTo(TwitchStreamerNameEnum.INVALID);
    }
}
