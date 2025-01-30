package com.apa.clipfarmer.logic.twitch;

import com.apa.clipfarmer.model.TwitchConstants;
import com.apa.clipfarmer.utils.HttpUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Fetches and processes the most viewed Twitch clips for a specific streamer.
 * Handles API calls and sorting logic.
 *
 * @author alexpages
 */
@Slf4j
public class TwitchClipFetcherLogic {

    /**
     * Fetches the top clips for the given streamer and sorts them by view count.
     *
     * @param streamerName The name of the streamer.
     * @param oAuthToken   The OAuth token for authentication.
     * @return A JSON string containing the sorted list of clips.
     */
    public static String getTwitchClips(String streamerName, String oAuthToken) {
        String broadcasterId = TwitchUserLogic.getBroadcasterId(streamerName, oAuthToken);
        String url = UriComponentsBuilder.fromHttpUrl(TwitchConstants.TWITCH_CLIP_API)
                .queryParam("broadcaster_id", broadcasterId)
                .queryParam("started_at", Instant.now().minus(5, ChronoUnit.DAYS)) // Clips from 5 days ago
                .toUriString();

        // Set up headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + oAuthToken);
        headers.set("Client-Id", TwitchConstants.TWITCH_CLIENT_ID);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.info("Response from Twitch Clip API: {}", response.getBody());

            JsonNode jsonNodeResponse = HttpUtils.parseJsonResponse(response.getBody());

            if (!jsonNodeResponse.has("data") || !jsonNodeResponse.get("data").isArray()) {
                log.warn("No clip data found for streamer: {}", streamerName);
                return "[]"; // Return empty JSON array
            }
            return sortClipsByViewCount(jsonNodeResponse);
        } catch (Exception e) {
            log.error("Error fetching clips for streamer {}: {}", streamerName, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch Twitch clips.", e);
        }
    }

    /**
     * Sorts Twitch clips by view count in descending order.
     *
     * @param jsonResponse The JSON response containing Twitch clips.
     * @return A JSON string with sorted clips.
     * @throws IOException If parsing fails.
     */
    private static String sortClipsByViewCount(JsonNode jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode clipsArray = (ArrayNode) jsonResponse.get("data");

        List<JsonNode> clipsList = new ArrayList<>();
        clipsArray.forEach(clipsList::add);

        // Sort clips by view_count in descending order
        clipsList.sort(Comparator.comparingInt(o -> -o.get("view_count").asInt()));

        ArrayNode sortedArray = objectMapper.createArrayNode();
        clipsList.forEach(sortedArray::add);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sortedArray);
    }
}
