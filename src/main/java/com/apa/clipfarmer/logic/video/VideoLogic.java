package com.apa.clipfarmer.logic.video;

import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final double TRANSITION_DURATION = 0.15; // Transition duration in seconds

    /**
     * Concatenates multiple video files into a single output file with smooth transitions.
     *
     * @param clipDurationsMap   Map of video file paths and their respective durations.
     * @param outputFileName     Name of the output file.
     * @return The path to the output file, or null if concatenation failed.
     */
    public String concatenateVideos(Map<String, Double> clipDurationsMap, String outputFileName) {
        long startTime = System.currentTimeMillis();

        log.info("This is the map of videos and durations to process: {}", clipDurationsMap.toString());

        // Ensure output directory exists
        File outputDir = new File(OUTPUT_FOLDER);
        outputDir.mkdirs();

        if (clipDurationsMap == null || clipDurationsMap.isEmpty()) {
            log.error("No video files provided for concatenation.");
            return null;
        }

        // Verify all input files exist before starting
        for (String path : clipDurationsMap.keySet()) {
            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                log.error("Input file does not exist or is not accessible: {}", path);
                return null;
            }
        }

        log.info("Starting video concatenation with synchronized audio/video transitions: {}", outputFileName);

        // Create absolute path for output file if it's a relative path
        File outputFile = new File(outputFileName);
        if (!outputFile.isAbsolute()) {
            outputFile = new File(System.getProperty("user.dir"), outputFileName);
        }

        if (outputFile.exists() && !outputFile.delete()) {
            log.error("Failed to delete existing output file: {}", outputFile.getAbsolutePath());
            return null;
        }

        try {
            // Create temporary folder for processed clips with absolute path
            File tempDir = new File(System.getProperty("user.dir"), OUTPUT_FOLDER + "temp");
            tempDir.mkdirs();
            log.debug("Created temp directory at: {}", tempDir.getAbsolutePath());

            List<String> processedClips = new ArrayList<>();

            // Process each clip with fade in/out for both video and audio
            int i = 0;
            for (Map.Entry<String, Double> entry : clipDurationsMap.entrySet()) {
                String inputFile = entry.getKey();
                double clipDuration = entry.getValue();

                File processedFile = new File(tempDir, "clip_" + i + ".mp4");
                String processedFilePath = processedFile.getAbsolutePath();
                processedClips.add(processedFilePath);

                log.debug("Processing clip {}: {} -> {}", i, inputFile, processedFilePath);

                List<String> command = new ArrayList<>();
                command.add("ffmpeg");
                command.add("-i");
                command.add(inputFile);

                // Fade in and fade out for all clips, using clip's duration
                command.add("-vf");
                command.add(String.format(Locale.US, "fade=t=in:st=0:d=%f,fade=t=out:st=%f:d=%f",
                        TRANSITION_DURATION,
                        clipDuration - TRANSITION_DURATION,
                        TRANSITION_DURATION));
                command.add("-af");
                command.add(String.format(Locale.US,"afade=t=in:st=0:d=%f,afade=t=out:st=%f:d=%f",
                        TRANSITION_DURATION,
                        clipDuration - TRANSITION_DURATION,
                        TRANSITION_DURATION));
                command.add("-y"); // Overwrite output files without asking
                command.add(processedFilePath);

                executeFFmpegCommand(command.toArray(new String[0]));

                // Verify the processed file was created
                if (!processedFile.exists()) {
                    log.error("Failed to create processed clip: {}", processedFilePath);
                    return null;
                }
                i++;
            }

            // Create a temporary file to store concatenation input
            File tempFile = createConcatFile(processedClips);
            if (tempFile == null) {
                log.error("Failed to create concat input file.");
                return null;
            }
            log.debug("Created concat file at: {} with content for {} files", tempFile.getAbsolutePath(), processedClips.size());
            log.info("Concatenating processed clips...");
            String[] concatCommand = {
                    "ffmpeg",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", tempFile.getAbsolutePath(),
                    "-c", "copy",
                    "-y", // Overwrite output files without asking
                    outputFile.getAbsolutePath()
            };
            executeFFmpegCommand(concatCommand);

            // Clean up
            if (!tempFile.delete()) {
                log.warn("Failed to delete temporary concat file: {}", tempFile.getAbsolutePath());
            }

            for (String clip : processedClips) {
                File clipFile = new File(clip);
                if (!clipFile.delete()) {
                    log.warn("Failed to delete temporary clip file: {}", clip);
                }
            }

            if (!tempDir.delete()) {
                log.warn("Failed to delete temporary directory: {}", tempDir.getAbsolutePath());
            }

            log.info("Video processing completed successfully: {}", outputFile.getAbsolutePath());

            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            log.error("Error during video processing", e);
            return null;
        } finally {
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            log.info("Video processing took {} seconds", elapsedTime);
        }
    }

    /**
     * Executes an FFmpeg command using array of arguments to handle paths with spaces correctly.
     */
    private void executeFFmpegCommand(String[] command) throws IOException, InterruptedException {
        log.debug("Executing FFmpeg command: {}", Arrays.toString(command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(false); // Keep stderr separate for logging
        Process process = processBuilder.start();

        // Handle standard output
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("FFmpeg stdout: {}", line);
                }
            } catch (IOException e) {
                log.error("Error reading FFmpeg stdout", e);
            }
        }).start();

        // Handle error output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("FFmpeg: {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("FFmpeg command failed with exit code: {}", exitCode);
            throw new RuntimeException("FFmpeg command failed with exit code: " + exitCode);
        }
    }

    /**
     * Creates a temporary text file listing the input video paths for FFmpeg.
     */
    private File createConcatFile(List<String> videoPaths) {
        File tempFile = new File(System.getProperty("user.dir"), OUTPUT_FOLDER + "input.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (String videoPath : videoPaths) {
                // Use proper escaping for paths in the concat file
                writer.write("file '" + videoPath.replace("'", "'\\''") + "'\n");
            }
        } catch (IOException e) {
            log.error("Error writing to concat file", e);
            return null;
        }
        return tempFile;
    }
}
