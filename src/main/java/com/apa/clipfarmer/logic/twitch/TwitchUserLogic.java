package com.apa.clipfarmer.logic.twitch;

import com.apa.clipfarmer.db.MyBatisConfig;
import com.apa.clipfarmer.model.TwitchConstants;
import com.apa.clipfarmer.model.TwitchStreamer;
import com.apa.clipfarmer.utils.HttpUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Twitch-related logic for retrieving user information.
 *
 * @author alexpages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TwitchUserLogic {

    private final SqlSessionFactory sqlSessionFactory;

    /**
     * Retrieves the broadcaster ID for a given streamer name.
     *
     * @param streamerName the name of the streamer.
     * @param oAuthToken   the OAuth token for authentication.
     * @return the broadcaster ID.
     */
    public String getBroadcasterId(String streamerName, String oAuthToken) {
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

            insertStreamerInDatabase(streamerName, broadcasterId);

            return broadcasterId;
        } catch (Exception e) {
            log.error("Unexpected error while fetching broadcaster ID: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error occurred while fetching broadcaster ID.", e);
        }
    }

    /**
     * Inserts a TwitchStreamer into the database.
     *
     * @param streamerName  the name of the streamer.
     * @param broadcasterId the ID of the broadcaster.
     */
    private void insertStreamerInDatabase(String streamerName, String broadcasterId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TwitchStreamer existingStreamer = session.selectOne("com.apa.clipfarmer.mapper.TwitchStreamerMapper.selectByBroadcasterId", broadcasterId);

            if (existingStreamer != null) {
                log.info("Streamer with broadcaster ID {} already exists. Skipping insertion.", broadcasterId);
                return;
            }

            TwitchStreamer twitchStreamer = new TwitchStreamer();
            twitchStreamer.setStreamerName(streamerName);
            twitchStreamer.setBroadcasterId(broadcasterId);

            session.insert("com.apa.clipfarmer.mapper.TwitchStreamerMapper.insertStreamer", twitchStreamer);
            session.commit();
            log.info("Streamer {} with broadcaster ID {} inserted into the database.", streamerName, broadcasterId);
        } catch (Exception e) {
            // Log the error and throw a RuntimeException if insertion fails
            log.error("Error inserting streamer {} into the database: {}", streamerName, e.getMessage(), e);
            throw new RuntimeException("Error inserting streamer into the database.", e);
        }
    }

}
