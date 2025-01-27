package com.apa.clipfarmer.logic;

import com.apa.clipfarmer.model.TwitchConstants;
import com.apa.clipfarmer.utils.HttpUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;

import java.io.IOException;
import java.util.Map;

/**
 * Twitch related logic to users
 *
 * @author alexpages
 */
@UtilityClass
@Slf4j
public class TwitchUserLogic {

    /**
     * Method to obtain the broadcaster_id from streamerName or login id
     * @param streamerName
     * @return the broadcaster_id
     * @throws IOException
     */
    public static String getBroadcasterId(String streamerName) throws IOException {
        String oAuthToken = TwitchAuthLogic.getOAuthToken();
        String url = TwitchConstants.TWITCH_USERS_API + "?login=" + streamerName;
        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", "Bearer " + oAuthToken);
        get.setHeader("Client-Id", TwitchConstants.TWITCH_CLIENT_ID);

        try {
            String responseBody = HttpUtils.executeRequest(get);
            Map<String, Object> responseMap = HttpUtils.parseJsonResponse(responseBody);
            return responseMap.get("id").toString();
        } catch (IOException e) {
            log.error("Unexpected error occurred: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error occurred while fetching Twitch clips.", e);
        }
    }
}
