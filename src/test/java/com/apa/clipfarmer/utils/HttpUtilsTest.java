package com.apa.clipfarmer.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link HttpUtils}.
 *
 * @author alexpages
 */
class HttpUtilsTest {

    @Test
    void parseJsonResponse_validJson_returnsJsonNode() {
        JsonNode result = HttpUtils.parseJsonResponse("{\"access_token\":\"abc123\"}");

        assertThat(result.get("access_token").asText()).isEqualTo("abc123");
    }

    @Test
    void parseJsonResponse_nestedJson_traversesCorrectly() {
        JsonNode result = HttpUtils.parseJsonResponse("{\"pagination\":{\"cursor\":\"xyz\"}}");

        assertThat(result.get("pagination").get("cursor").asText()).isEqualTo("xyz");
    }

    @Test
    void parseJsonResponse_emptyObject_returnsEmptyNode() {
        JsonNode result = HttpUtils.parseJsonResponse("{}");

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void parseJsonResponse_invalidJson_throwsRuntimeException() {
        assertThatThrownBy(() -> HttpUtils.parseJsonResponse("not-json"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse JSON response");
    }

    @Test
    void parseJsonResponse_nullValue_throwsException() {
        assertThatThrownBy(() -> HttpUtils.parseJsonResponse(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void parseJsonResponse_jsonArray_parsesCorrectly() {
        JsonNode result = HttpUtils.parseJsonResponse("[{\"id\":\"1\"},{\"id\":\"2\"}]");

        assertThat(result.isArray()).isTrue();
        assertThat(result).hasSize(2);
    }
}
