package com.apa.clipfarmer.logic.twitch;

import com.apa.clipfarmer.model.TwitchConstants;
import com.apa.clipfarmer.utils.HttpUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Class that gathers all Twitch logic: authentication, data fetching, etc.
 *
 * @author alexpages
 */
@Slf4j
@UtilityClass
public class TwitchAuthLogic {

    private static final String GRANT_TYPE = "client_credentials";

    /**
     * Fetches the OAuth token from the Twitch API.
     *
     * @return OAuth token
     * @throws IllegalStateException if credentials are missing
     * @throws RuntimeException if the API request fails
     */
    public static String getOAuthToken() {
        validateTwitchCredentials();

        String url = UriComponentsBuilder.fromHttpUrl(TwitchConstants.TWITCH_OAUTH_API)
                .queryParam("client_id", TwitchConstants.TWITCH_CLIENT_ID)
                .queryParam("client_secret", TwitchConstants.TWITCH_CLIENT_SECRET)
                .queryParam("grant_type", GRANT_TYPE)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to fetch OAuth Token. HTTP Status: {}", response.getStatusCode());
                throw new RuntimeException("Twitch API request failed with status: " + response.getStatusCode());
            }

            JsonNode jsonResponse = HttpUtils.parseJsonResponse(response.getBody());
            return Optional.ofNullable(jsonResponse.get("access_token"))
                    .map(JsonNode::asText)
                    .orElseThrow(() -> {
                        log.error("Access token not found in Twitch API response.");
                        return new RuntimeException("Invalid response: Access token missing.");
                    });

        } catch (RestClientException e) {
            log.error("Error fetching OAuth token: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error occurred while fetching OAuth token.", e);
        }
    }

    /**
     * Validates that Twitch credentials are set in the environment.
     *
     * @throws IllegalStateException if credentials are missing
     */
    private static void validateTwitchCredentials() {
        if (TwitchConstants.TWITCH_CLIENT_ID == null || TwitchConstants.TWITCH_CLIENT_SECRET == null) {
            log.error("Environment variables TWITCH_CLIENT_ID or TWITCH_CLIENT_SECRET are not set.");
            throw new IllegalStateException("Missing Twitch credentials.");
        }
    }
}
