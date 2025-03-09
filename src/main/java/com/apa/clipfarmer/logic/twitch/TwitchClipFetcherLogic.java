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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    private static final int MIN_VIEWS = 200;

    /**
     * Fetches the top clips for the given streamer and sorts them by view count.
     *
     * @param streamerName The name of the streamer.
     * @param oAuthToken   The OAuth token for authentication.
     * @return A list of sorted TwitchClip objects.
     */
    public List<TwitchClip> getTwitchClips(String streamerName, String oAuthToken, int durationOfClip) {
        String broadcasterId = twitchUserLogic.getBroadcasterId(streamerName, oAuthToken);
        String url = UriComponentsBuilder.fromHttpUrl(TwitchConstants.TWITCH_CLIP_API)
                .queryParam("broadcaster_id", broadcasterId)
                .queryParam("started_at", Instant.now().minus(STARTED_AT, ChronoUnit.DAYS)) // Clips from 5 days ago
                .queryParam("first", 10)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + oAuthToken);
        headers.set("Client-Id", TwitchConstants.TWITCH_CLIENT_ID);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        List<TwitchClip> allClips = new ArrayList<>();
        String afterCursor = null;

        try {
            do {
                String pageUrl = (afterCursor != null) ? url + "&after=" + afterCursor : url;
                ResponseEntity<String> response = restTemplate.exchange(pageUrl, HttpMethod.GET, entity, String.class);
                log.info("Response from Twitch Clip API: {}", response.getBody());
                List<TwitchClip> clips = convertResponseBodyToTwitchClips(response, durationOfClip);
                allClips.addAll(clips);
                log.info("All clips retrieved: {}", allClips);

                afterCursor = null;
//                afterCursor = extractAfterCursor(response); //TODO change back
            } while (afterCursor != null);
        } catch (Exception e) {
            log.error("Error fetching clips for streamer {}: {}", streamerName, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch Twitch clips.", e);
        }

        return allClips;
    }

    /**
     * Extract the cursor for the next page from the response body.
     *
     * @param response The response body from the Twitch API.
     * @return The cursor string for the next page, or null if there is no next page.
     */
    private static String extractAfterCursor(ResponseEntity<String> response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("pagination") && jsonNode.get("pagination").has("cursor")) {
                return jsonNode.get("pagination").get("cursor").asText();
            }
        } catch (IOException e) {
            log.error("Error extracting 'after' cursor: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Converts the response body from the Twitch API into a list of TwitchClip objects and sorts them by view count.
     *
     * @param responseBody The response body from the RestTemplate call.
     * @return A list of sorted TwitchClip objects.
     */
    private static List<TwitchClip> convertResponseBodyToTwitchClips(ResponseEntity<String> responseBody, int durationOfClip) {
        List<TwitchClip> twitchClips = new ArrayList<>();
        if (!responseBody.hasBody()) {
            return twitchClips;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody.getBody());
            if (!jsonNode.has("data") || !jsonNode.get("data").isArray()) {
                log.warn("No clip data found.");
                return twitchClips;
            }
            ArrayNode clipsArray = (ArrayNode) jsonNode.get("data");

            twitchClips = StreamSupport.stream(clipsArray.spliterator(), false)
                    .map(clipNode -> {
                        String createdAtString = clipNode.get("created_at").asText();
                        LocalDateTime createdAt = LocalDateTime.parse(createdAtString, DateTimeFormatter.ISO_DATE_TIME);
                        return new TwitchClip(
                                null,
                                clipNode.get("id").asText(),
                                clipNode.get("title").asText(),
                                clipNode.get("creator_name").asText().toLowerCase(),
                                clipNode.get("view_count").asInt(),
                                createdAt,
                                clipNode.get("broadcaster_id").asText(),
                                clipNode.get("url").asText(),
                                clipNode.get("duration").asInt(),
                                clipNode.get("language").asText()
                        );
                    })
                    .filter(clip -> clip.getDuration() >= durationOfClip)                   // Remove clips with duration < 10
                    .filter(clip -> clip.getViewCount() > MIN_VIEWS)                        // Remove clips with viewCount <= 600
                    .sorted(Comparator.comparingInt(TwitchClip::getViewCount).reversed())   // Sort by viewCount (desc)
                    .collect(Collectors.toList());
            log.info("Collected twitchClips: {}", twitchClips);
        } catch (IOException e) {
            log.error("Error parsing response body: {}", e.getMessage(), e);
        }
        return twitchClips;
    }
}
