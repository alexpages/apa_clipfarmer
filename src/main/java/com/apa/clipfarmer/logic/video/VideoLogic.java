package com.apa.clipfarmer.logic.video;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service that generates different video formats to be uploaded on different platforms.
 *
 * @author alexpages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoLogic {

    private static final String OUTPUT_FOLDER = "build/output/";

    /**
     * Concatenates multiple video files into a single output file with a watermark.
     *
     * @param videoPaths      List of file paths of the videos to concatenate.
     * @param outputFileName  Name of the output file.
     */
    public String concatenateVideos(List<String> videoPaths, String outputFileName) {
        long startTime = System.currentTimeMillis();

        new File(OUTPUT_FOLDER).mkdirs();
        if (videoPaths == null || videoPaths.isEmpty()) {
            log.error("No video files provided for concatenation.");
            return null;
        }

        log.info("Starting video concatenation: {}", outputFileName);

        File outputFile = new File(outputFileName);
        if (outputFile.exists() && !outputFile.delete()) {
            log.error("Failed to delete existing output file: {}", outputFileName);
            return null;
        }

        File tempFile = createConcatFile(videoPaths);
        if (tempFile == null) {
            log.error("Failed to create concat input file.");
            return null;
        }

        try {
            // Step 1: Concatenate videos
            log.info("Initializing video concatenation...");
            String concatCommand = String.format("ffmpeg -f concat -safe 0 -i %s -c copy %s", tempFile.getAbsolutePath(), outputFileName);
            executeFFmpegCommand(concatCommand);
            log.info("Video concatenation completed: {}", outputFileName);

            log.info("Video processing completed successfully: {}", outputFileName);
        } catch (Exception e) {
            log.error("Error during video processing", e);
        } finally {
            tempFile.delete();
        }
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        log.info("Video processing took {} seconds", elapsedTime);
        return outputFileName;
    }

    /**
     * Retrieves the file paths of all video files in the given directory.
     *
     * @param directoryPath The path to the directory containing video files.
     * @return A list of file paths for the video files.
     */
    public static List<String> getVideoPaths(String directoryPath) {
        List<String> videoPaths = new ArrayList<>();
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".avi"));
            if (files != null) {
                for (File file : files) {
                    videoPaths.add(file.getAbsolutePath());
                }
            }
        } else {
            System.err.println("Directory does not exist or is not a directory: " + directoryPath);
        }
        return videoPaths;
    }

    /**
     * Creates a temporary text file listing the input video paths for FFmpeg.
     */
    private File createConcatFile(List<String> videoPaths) {
        File tempFile = new File(OUTPUT_FOLDER + "input.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            int videoCount = 1;
            for (String videoPath : videoPaths) {
                log.info("Concatenation, video {} out of {}", videoCount, videoPaths.size());
                writer.write("file '" + videoPath + "'\n");
                videoCount++;
            }
        } catch (IOException e) {
            log.error("Error writing to concat file", e);
            return null;
        }
        return tempFile;
    }

    /**
     * Executes an FFmpeg command.
     */
    private void executeFFmpegCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.error("FFmpeg: " + line);
            }
        }
        process.waitFor();
    }
}
