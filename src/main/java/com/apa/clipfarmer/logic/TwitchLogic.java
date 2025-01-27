package com.apa.clipfarmer.logic;


import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that gathers all Twitch logic. Auth, fetch, etc.
 *
 * @author alexpages
 */
@Slf4j
@UtilityClass
public class TwitchLogic {

    private static final String TWITCH_CLIENT_SECRET = System.getenv("TWITCH_CLIENT_SECRET");
    private static final String TWITCH_CLIENT_ID = System.getenv("TWITCH_CLIENT_ID");
    private static final String TWITCH_URL = "https://id.twitch.tv/oauth2/token";

    /**
     * Fetches the OAuth token from Twitch API.
     *
     * @return OAuth token
     * @throws IOException if the request fails
     */
    public static String getOAuthToken() {
        if (TWITCH_CLIENT_ID == null || TWITCH_CLIENT_SECRET == null) {
            log.error("Environment variables TWITCH_CLIENT_ID or TWITCH_CLIENT_SECRET are not set.");
            throw new IllegalStateException("Missing Twitch credentials.");
        }
        String body = "client_id=" + TWITCH_CLIENT_ID + "&client_secret=" + TWITCH_CLIENT_SECRET + "&grant_type=client_credentials";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(TWITCH_URL);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setEntity(new StringEntity(body));

            try (CloseableHttpResponse response = client.execute(post)) {
                int statusCode = response.getCode();
                log.info("Received response with status code: {}", statusCode);
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    log.debug("Response body: {}", responseBody);
                    Map<String, String> responseMap = parseJsonResponse(responseBody);
                    String accessToken = responseMap.get("access_token");
                    if (accessToken != null) {
                        log.info("Successfully fetched OAuth token.");
                        return accessToken;
                    } else {
                        log.error("Access token not found in the response.");
                        throw new IOException("Invalid response from Twitch API: Access token missing.");
                    }
                } else {
                    String errorBody = EntityUtils.toString(response.getEntity());
                    log.error("Failed to fetch OAuth token. Status code: {}, Response: {}", statusCode, errorBody);
                    throw new IOException("Failed to fetch OAuth token: " + errorBody);
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error occurred while fetching OAuth token.", e);
        }
    }

    /**
     * Helper method to parse the JSON response from Twitch API.
     *
     * @param jsonResponse JSON response string
     * @return Map with parsed data
     */
    private Map<String, String> parseJsonResponse(String jsonResponse) {
        Map<String, String> responseMap = new HashMap<>();

        try {
            String[] pairs = jsonResponse.replaceAll("[{}\"]", "").split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                responseMap.put(keyValue[0].trim(), keyValue[1].trim());
            }
        } catch (Exception e) {
            log.error("Error parsing JSON response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse JSON response.", e);
        }
        return responseMap;
    }
}
