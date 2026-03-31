package com.apa.clipfarmer.model;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ClipFarmerArgs} argument parsing.
 *
 * @author alexpages
 */
class ClipFarmerArgsTest {

    @Test
    void build_validStreamerName_returnsCorrectEnum() {
        ClipFarmerArgs.Builder builder = new ClipFarmerArgs.Builder();
        new JCommander(builder).parse("streamerName=xqc");

        ClipFarmerArgs args = builder.build();

        assertThat(args.getTwitchStreamerNameEnum()).isEqualTo(TwitchStreamerNameEnum.XQC);
    }

    @Test
    void build_unknownStreamerName_returnsInvalidEnum() {
        ClipFarmerArgs.Builder builder = new ClipFarmerArgs.Builder();
        new JCommander(builder).parse("streamerName=nobody");

        ClipFarmerArgs args = builder.build();

        assertThat(args.getTwitchStreamerNameEnum()).isEqualTo(TwitchStreamerNameEnum.INVALID);
    }

    @Test
    void parse_missingRequiredParam_throwsParameterException() {
        ClipFarmerArgs.Builder builder = new ClipFarmerArgs.Builder();

        assertThatThrownBy(() -> new JCommander(builder).parse())
                .isInstanceOf(ParameterException.class);
    }

    @Test
    void build_allKnownStreamers_resolveCorrectly() {
        for (TwitchStreamerNameEnum expected : TwitchStreamerNameEnum.values()) {
            if (expected == TwitchStreamerNameEnum.INVALID) continue;

            ClipFarmerArgs.Builder builder = new ClipFarmerArgs.Builder();
            new JCommander(builder).parse("streamerName=" + expected.getName());

            assertThat(builder.build().getTwitchStreamerNameEnum()).isEqualTo(expected);
        }
    }
}
