package com.apa.clipfarmer.logic.twitch;

import com.apa.clipfarmer.model.TwitchConstants;
import com.apa.clipfarmer.utils.HttpUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Twitch-related logic for retrieving user information.
 *
 * @author alexpages
 */
@UtilityClass
@Slf4j
public class TwitchUserLogic {

    /**
     * Retrieves the broadcaster ID for a given streamer name.
     *
     * @param streamerName the name of the streamer.
     * @param oAuthToken   the OAuth token for authentication.
     * @return the broadcaster ID.
     */
    public static String getBroadcasterId(String streamerName, String oAuthToken) {
        String url = UriComponentsBuilder.fromHttpUrl(TwitchConstants.TWITCH_USERS_API)
                .queryParam("login", streamerName)
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
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to fetch broadcaster ID. HTTP Status: {}", response.getStatusCode());
                throw new RuntimeException("Twitch API request failed with status: " + response.getStatusCode());
            }

            log.info("Response from Twitch API: {}", response.getBody());
            JsonNode jsonNodeResponse = HttpUtils.parseJsonResponse(response.getBody());

            // Validate response structure
            JsonNode dataNode = jsonNodeResponse.get("data");
            if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                log.error("Invalid response: Missing 'data' field or empty array.");
                throw new RuntimeException("Invalid response: No broadcaster data found.");
            }

            // Extract broadcaster ID
            String broadcasterId = Optional.ofNullable(dataNode.get(0))
                    .map(node -> node.get("id"))
                    .map(JsonNode::asText)
                    .orElse(null);

            if (broadcasterId == null || broadcasterId.isEmpty()) {
                log.error("Failed to fetch broadcaster ID for streamer: {}", streamerName);
                throw new IllegalArgumentException("Invalid broadcaster ID for streamer: " + streamerName);
            }
            return broadcasterId;
        } catch (Exception e) {
            log.error("Unexpected error while fetching broadcaster ID: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error occurred while fetching broadcaster ID.", e);
        }
    }
}
