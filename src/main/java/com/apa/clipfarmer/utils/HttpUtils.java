package com.apa.clipfarmer.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class HttpUtils {

    /**
     * Helper method to parse a JSON response into a map.
     *
     * @param jsonResponse JSON response string
     * @return Map with parsed data
     */
    public static JsonNode parseJsonResponse(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse the JSON string into a JsonNode
            return objectMapper.readTree(jsonResponse);
        } catch (Exception e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
            throw new RuntimeException("Failed to parse JSON response.", e);
        }
    }
}
