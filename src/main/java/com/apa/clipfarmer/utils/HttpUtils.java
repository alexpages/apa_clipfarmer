package com.apa.clipfarmer.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@UtilityClass
public class HttpUtils {

    /**
     * Helper method to execute an HTTP request and return the response body as a string.
     *
     * @param request HTTP request to execute
     * @return Response body as a string
     * @throws IOException if the request fails
     */
    public static String executeRequest(HttpUriRequestBase request) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(request)) {
            int statusCode = response.getCode();

            if (statusCode >= 200 && statusCode < 300) {
                return EntityUtils.toString(response.getEntity());
            } else {
                String errorBody = EntityUtils.toString(response.getEntity());
                log.error("HTTP request failed. Status code: {}, Response: {}", statusCode, errorBody);
                throw new IOException("HTTP request failed with status code " + statusCode + ": " + errorBody);
            }

        } catch (ParseException e) {
            log.error("There was an error when parsing out the response: {}", e.getMessage());
            throw new RuntimeException("Error parsing out the response", e);
        }
    }

    /**
     * Helper method to parse a JSON response into a map.
     *
     * @param jsonResponse JSON response string
     * @return Map with parsed data
     */
    public static Map<String, Object> parseJsonResponse(String jsonResponse) {
        Map<String, Object> responseMap = new HashMap<>();

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
