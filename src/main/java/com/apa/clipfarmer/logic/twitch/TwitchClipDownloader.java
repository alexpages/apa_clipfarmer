package com.apa.clipfarmer.logic.twitch;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;

@Service
public class TwitchClipDownloader {

    private static final String OUTPUT_FOLDER = "build/downloads/";

    private static String getClipVideoUrl(String clipUrl) throws IOException {
        URL url = new URL(clipUrl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        // Set a User-Agent to mimic a browser
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Regex for the MP4 URL
        Pattern pattern = Pattern.compile("\"sourceURL\":\"(https:[^\"]+\\.mp4)\"");
        Matcher matcher = pattern.matcher(response.toString());

        if (matcher.find()) {
            return matcher.group(1).replace("\\/", "/"); // Unescape URL
        }
        return null;
    }

    private static void downloadFile(String fileURL) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        // Create directories if they don't exist
        File outputFile = new File(OUTPUT_FOLDER);
        outputFile.getParentFile().mkdirs();

        InputStream inputStream = conn.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();
    }
}
