package com.apa.clipfarmer.logic.twitch;

import com.apa.clipfarmer.model.TwitchClip;
import com.apa.clipfarmer.model.TwitchStreamerNameEnum;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import static com.apa.clipfarmer.model.TwitchConstants.TWITCH_GQL_URL;
import static com.apa.clipfarmer.model.TwitchConstants.TWITCH_GRAPHQL_CLIENT_ID;

/**
 * Class to download clips from url fetched previously
 *
 * @author alexpages
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TwitchClipDownloader {

    private static final String OUTPUT_FOLDER = "build/downloads/";

    /**
     * Downloads a Twitch clip using the provided URL and OAuth token.
     *
     * @param clipUrl the URL of the Twitch clip
     */
    public void downloadFile(String clipUrl, TwitchClip twitchClip, TwitchStreamerNameEnum twitchStreamer) {
        log.info("Starting download for clip: {}", clipUrl);

        String clipSlug = extractClipSlug(clipUrl);
        if (clipSlug.isEmpty()) {
            log.error("Invalid clip URL: {}", clipUrl);
            return;
        }
        log.info("Clip Slug has been extracted successfully: {}", clipSlug);

        Optional<String> oVideoUrl = getClipVideoUrl(clipSlug);
        if (oVideoUrl.isEmpty()) {
            log.error("Could not retrieve video URL for clip: {}", clipUrl);
            return;
        }
        log.info("Clip video URL has been extracted successfully: {}", oVideoUrl.get());
        String folder = OUTPUT_FOLDER + twitchStreamer.getName();
        String outputFileName = String.format(
                "%s/clip_%s_%s_%s.mp4",
                folder,
                twitchClip.getLanguage(),
                twitchClip.getBroadcasterId(),
                twitchClip.getClipId()
        );
        downloadVideo(oVideoUrl.get(), outputFileName, folder);
    }

    /**
     * Extracts the clip slug from the given clip URL.
     *
     * @param clipUrl the URL of the clip
     * @return the extracted clip slug
     */
    private String extractClipSlug(String clipUrl) {
        int lastSlashIndex = clipUrl.lastIndexOf("/");
        return (lastSlashIndex != -1) ? clipUrl.substring(lastSlashIndex + 1) : "";
    }

    /**
     * Retrieves the video URL for the given clip slug.
     *
     * @param clipSlug the slug of the clip
     * @return an Optional containing the video URL if found, or empty if not
     */
    private Optional<String> getClipVideoUrl(String clipSlug) {
        try {
            String jsonQuery = createJsonQuery(clipSlug);

            HttpURLConnection conn = createPostConnection();
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonQuery.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);
            log.info("Twitch API Response ({}): {}", responseCode, response);

            if (responseCode != 200) {
                log.error("Failed to fetch clip: HTTP {}", responseCode);
                return Optional.empty();
            }

            return extractVideoUrl(response);
        } catch (IOException e) {
            log.error("Error fetching Twitch clip: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Creates a JSON query for fetching clip data.
     *
     * @param clipSlug the slug of the clip
     * @return the JSON query string
     */
    private String createJsonQuery(String clipSlug) {
        return String.format("""
        {
            "operationName": "VideoAccessToken_Clip",
            "variables": { "slug": "%s" },
            "query": "query ($slug: ID!) { clip(slug: $slug) { videoQualities { sourceURL } playbackAccessToken(params: { platform: \\"web\\" }) { signature value } } } }",
            "extensions": {
                "persistedQuery": {
                    "version": 1,
                    "sha256Hash": "36b89d2507fce29e5ca551df756d27c1cfe079e2609642b4390aa4c35796eb11"
                }
            }
        }
        """, clipSlug);
    }

    /**
     * Creates an HTTP POST connection to the Twitch API.
     *
     * @return the HttpURLConnection
     * @throws IOException if an I/O error occurs
     */
    private HttpURLConnection createPostConnection() throws IOException {
        URL url = new URL(TWITCH_GQL_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Client-ID", TWITCH_GRAPHQL_CLIENT_ID);
        conn.setRequestProperty("Authorization", "Bearer " + TwitchAuthLogic.getOAuthToken());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        return conn;
    }

    /**
     * Reads the response from the given HttpURLConnection.
     *
     * @param conn the HttpURLConnection
     * @return the response as a string
     * @throws IOException if an I/O error occurs
     */
    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    /**
     * Extracts the video URL from the API response.
     *
     * @param response the API response as a string
     * @return an Optional containing the video URL if found, or empty if not
     */
    private Optional<String> extractVideoUrl(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            return parseClipData(jsonResponse);
        } catch (Exception e) {
            log.error("Error parsing API response: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Parses the clip data from the JSON response.
     *
     * @param response the JSON response
     * @return an Optional containing the video URL if found, or empty if not
     */
    private Optional<String> parseClipData(JSONObject response) {
        try {
            log.info("Parsing the API response: {}", response.toString());

            if (response.has("data")) {
                JSONObject videoAccessTokenData = response.getJSONObject("data").getJSONObject("clip");
                String signature = videoAccessTokenData.getJSONObject("playbackAccessToken").getString("signature");
                String token = videoAccessTokenData.getJSONObject("playbackAccessToken").getString("value");

                // Get the video URL (assuming you want the first available quality)
                String videoURL = videoAccessTokenData.getJSONArray("videoQualities").getJSONObject(0).getString("sourceURL");
                String clipDownloadUrl = String.format("%s?sig=%s&token=%s", videoURL, signature, token);
                return Optional.of(clipDownloadUrl);
            } else {
                log.error("No valid data found in the response.");
            }
        } catch (Exception e) {
            log.error("Error parsing clip data: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Downloads the video from the given URL to the specified output file.
     *
     * @param videoUrl      the URL of the video to download
     * @param outputFileName the name of the output file
     */
    private void downloadVideo(String videoUrl, String outputFileName, String folder) {
        try {
            log.info("Downloading video from: {}", videoUrl);
            new File(folder).mkdirs();

            HttpURLConnection conn = (HttpURLConnection) new URL(videoUrl).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (InputStream inputStream = conn.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(outputFileName)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            log.info("Download completed: {}", outputFileName);
        } catch (IOException e) {
            log.error("Error downloading video: {}", e.getMessage());
        }
    }
}
