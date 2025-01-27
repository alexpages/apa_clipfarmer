package com.apa.clipfarmer.logic;

import com.apa.clipfarmer.model.TwitchConstants;
import com.apa.clipfarmer.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fetches the most viewed Twitch clips for a specific streamer.
 * This class handles the logic for fetching clips using Twitch APIs.
 *
 * @author alexpages
 */
@Slf4j
public class TwitchClipFetcherLogic {

    /**
     * Fetches the top 5 clips for the given streamer.
     *
     * @param streamerName the name of the streamer
     * @return a list of URLs of the clips
     */
    public List<String> getTwitchClips(String streamerName) {
        String oAuthToken = TwitchAuthLogic.getOAuthToken();
        String broadcasterId = TwitchUserLogic.getBroadcasterId(streamerName);

        if (broadcasterId == null || broadcasterId.isEmpty()) {
            log.error("Failed to fetch broadcaster ID for streamer: {}", streamerName);
            throw new IllegalArgumentException("Invalid broadcaster ID for streamer: " + streamerName);
        }

        String url = TwitchConstants.TWITCH_CLIP_API + "?broadcaster_id=" + broadcasterId + "&first=5";
        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", "Bearer " + oAuthToken);
        get.setHeader("Client-Id", TwitchConstants.TWITCH_CLIENT_ID);

        try {
            String responseBody = HttpUtils.executeRequest(get);
            Map<String, Object> responseMap = HttpUtils.parseJsonResponse(responseBody);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> clips = (List<Map<String, Object>>) responseMap.get("data");
            if (clips == null) {
                log.error("No clips found in the response for streamer: {}", streamerName);
                return new ArrayList<>();
            }

            List<String> clipUrls = new ArrayList<>();
            for (Map<String, Object> clip : clips) {
                String urlValue = (String) clip.get("url");
                if (urlValue != null) {
                    clipUrls.add(urlValue);
                }
            }

            log.info("Successfully fetched {} clips for streamer: {}", clipUrls.size(), streamerName);
            return clipUrls;
        } catch (IOException e) {
            log.error("Unexpected error occurred while fetching clips for streamer {}: {}", streamerName, e.getMessage(), e);
            throw new RuntimeException("Unexpected error occurred while fetching Twitch clips.", e);
        }
    }
}
