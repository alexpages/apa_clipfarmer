package com.apa.clipfarmer.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TwitchStreamerNameEnum}.
 *
 * @author alexpages
 */
class TwitchStreamerNameEnumTest {

    @Test
    void fromString_knownStreamer_returnsCorrectEnum() {
        assertThat(TwitchStreamerNameEnum.fromString("jasontheween")).isEqualTo(TwitchStreamerNameEnum.JASONTHEWEEN);
        assertThat(TwitchStreamerNameEnum.fromString("xqc")).isEqualTo(TwitchStreamerNameEnum.XQC);
        assertThat(TwitchStreamerNameEnum.fromString("valkyrae")).isEqualTo(TwitchStreamerNameEnum.VALKYRAE);
    }

    @Test
    void fromString_nullOrEmpty_returnsInvalid() {
        assertThat(TwitchStreamerNameEnum.fromString(null)).isEqualTo(TwitchStreamerNameEnum.INVALID);
        assertThat(TwitchStreamerNameEnum.fromString("")).isEqualTo(TwitchStreamerNameEnum.INVALID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"JASONTHEWEEN", "JasonTheWeen", "unknown_streamer", "  xqc  "})
    void fromString_unknownOrWrongCase_returnsInvalid(String input) {
        assertThat(TwitchStreamerNameEnum.fromString(input)).isEqualTo(TwitchStreamerNameEnum.INVALID);
    }

    @Test
    void invalid_hasEmptyNameAndLanguage() {
        assertThat(TwitchStreamerNameEnum.INVALID.getName()).isEmpty();
        assertThat(TwitchStreamerNameEnum.INVALID.getLanguage()).isEmpty();
    }

    @Test
    void allKnownStreamers_haveNonEmptyFields() {
        for (TwitchStreamerNameEnum streamer : TwitchStreamerNameEnum.values()) {
            if (streamer == TwitchStreamerNameEnum.INVALID) continue;
            assertThat(streamer.getName()).isNotEmpty();
            assertThat(streamer.getLanguage()).isNotEmpty();
        }
    }
}
