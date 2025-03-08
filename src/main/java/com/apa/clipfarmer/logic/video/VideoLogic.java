package com.apa.clipfarmer.logic.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * Concatenates multiple video files into a single output file using FFmpeg's concat demuxer.
     *
     * @param videoPaths      List of file paths of the videos to concatenate.
     * @param outputFileName  Name of the output file.
     */
    public void concatenateVideos(List<String> videoPaths, String outputFileName) {
        long startTime = System.currentTimeMillis();  // Start time

        new File(OUTPUT_FOLDER).mkdirs();
        if (videoPaths == null || videoPaths.isEmpty()) {
            log.error("No video files provided for concatenation.");
            return;
        }

        log.info("Starting video concatenation: {}", outputFileName);

        // Delete existing output file if it exists
        File outputFile = new File(outputFileName);
        if (outputFile.exists() && !outputFile.delete()) {
            log.error("Failed to delete existing output file: {}", outputFileName);
            return;
        }

        // Create a temporary text file listing the input videos
        File tempFile = createConcatFile(videoPaths);
        if (tempFile == null) {
            log.error("Failed to create concat input file.");
            return;
        }

        // Run the FFmpeg command with the concat demuxer
        try {
            String command = String.format("ffmpeg -f concat -safe 0 -i %s -c copy %s", tempFile.getAbsolutePath(), outputFileName);
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            log.info("Video concatenation completed: {}", outputFileName);
        } catch (Exception e) {
            log.error("Error during video concatenation using concat demuxer", e);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        log.info("Video concatenation took {} milliseconds", elapsedTime);
    }


    /**
     * Creates a temporary text file listing the paths of the input videos for FFmpeg's concat demuxer.
     *
     * @param videoPaths List of video file paths.
     * @return The created temporary text file.
     */
    private File createConcatFile(List<String> videoPaths) {
        File tempFile = new File(OUTPUT_FOLDER + "input.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (String videoPath : videoPaths) {
                writer.write("file '" + videoPath + "'\n");
            }
        } catch (IOException e) {
            log.error("Error writing to concat file", e);
            return null;
        }
        return tempFile;
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
}