package com.apa.clipfarmer.logic;


import com.apa.clipfarmer.model.TwitchConstants;
import com.apa.clipfarmer.utils.HttpUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.Map;

/**
 * Class that gathers all Twitch logic. Auth, fetch, etc.
 *
 * @author alexpages
 */
@Slf4j
@UtilityClass
public class TwitchAuthLogic {

    /**
     * Fetches the OAuth token from Twitch API.
     *
     * @return OAuth token
     * @throws IOException if the request fails
     */
    public static String getOAuthToken() {
        if (TwitchConstants.TWITCH_CLIENT_ID == null || TwitchConstants.TWITCH_CLIENT_SECRET == null) {
            log.error("Environment variables TWITCH_CLIENT_ID or TWITCH_CLIENT_SECRET are not set.");
            throw new IllegalStateException("Missing Twitch credentials.");
        }
        String body = "client_id=" + TwitchConstants.TWITCH_CLIENT_ID + "&client_secret="
                + TwitchConstants.TWITCH_CLIENT_SECRET + "&grant_type=client_credentials";

        HttpPost post = new HttpPost(TwitchConstants.TWITCH_OAUTH_API);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(new StringEntity(body));

        try {
            String responseBody = HttpUtils.executeRequest(post);
            Map<String, Object> responseMap = HttpUtils.parseJsonResponse(responseBody);
            String accessToken = responseMap.get("access_token").toString();
            if (accessToken != null) {
                log.info("Successfully fetched OAuth token.");
                return accessToken;
            } else {
                log.error("Access token not found in the response.");
                throw new IOException("Invalid response from Twitch API: Access token missing.");
            }
        } catch (IOException e) {
            log.error("Unexpected error occurred: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error occurred while fetching OAuth token.", e);
        }
    }
}
