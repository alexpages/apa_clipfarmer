package com.apa.clipfarmer.logic.twitch;

import com.apa.clipfarmer.model.TwitchClip;
import com.apa.clipfarmer.model.TwitchConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@Service
@RequiredArgsConstructor
@Slf4j
public class TwitchClipFetcherLogic {

    private final TwitchUserLogic twitchUserLogic;

    private static final int STARTED_AT = 5;

    /**
     * Fetches the top clips for the given streamer and sorts them by view count.
     *
     * @param streamerName The name of the streamer.
     * @param oAuthToken   The OAuth token for authentication.
     * @return A list of sorted TwitchClip objects.
     */
    public List<TwitchClip> getTwitchClips(String streamerName, String oAuthToken) {
        String broadcasterId = twitchUserLogic.getBroadcasterId(streamerName, oAuthToken);
        String url = UriComponentsBuilder.fromHttpUrl(TwitchConstants.TWITCH_CLIP_API)
                .queryParam("broadcaster_id", broadcasterId)
                .queryParam("started_at", Instant.now().minus(STARTED_AT, ChronoUnit.DAYS)) // Clips from 5 days ago
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

            return convertResponseBodyToTwitchClips(response);
        } catch (Exception e) {
            log.error("Error fetching clips for streamer {}: {}", streamerName, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch Twitch clips.", e);
        }
    }

    /**
     * Converts the response body from the Twitch API into a list of TwitchClip objects and sorts them by view count.
     *
     * @param responseBody The response body from the RestTemplate call.
     * @return A list of sorted TwitchClip objects.
     */
    private static List<TwitchClip> convertResponseBodyToTwitchClips(ResponseEntity<String> responseBody) {
        List<TwitchClip> twitchClips = new ArrayList<>();
        if (!responseBody.hasBody()) {
            return twitchClips;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody.getBody());
            if (!jsonNode.has("data") || !jsonNode.get("data").isArray()) {
                log.warn("No clip data found.");
                return twitchClips; // Return empty list if no "data" is found
            }

            ArrayNode clipsArray = (ArrayNode) jsonNode.get("data");
            clipsArray.forEach(clipNode -> {
                // Parse the created_at string into LocalDateTime
                String createdAtString = clipNode.get("created_at").asText();
                LocalDateTime createdAt = LocalDateTime.parse(createdAtString, DateTimeFormatter.ISO_DATE_TIME);

                TwitchClip twitchClip = new TwitchClip(
                        clipNode.get("id").asInt(),
                        clipNode.get("id").asText(),
                        clipNode.get("title").asText(),
                        clipNode.get("creator_name").asText(),
                        clipNode.get("view_count").asInt(),
                        createdAt
                );
                twitchClips.add(twitchClip);
            });

            // Sort clips by view count in descending order
            twitchClips.sort(Comparator.comparingInt(TwitchClip::getViewCount).reversed());
        } catch (IOException e) {
            log.error("Error parsing response body: {}", e.getMessage(), e);
        }

        return twitchClips;
    }
}
