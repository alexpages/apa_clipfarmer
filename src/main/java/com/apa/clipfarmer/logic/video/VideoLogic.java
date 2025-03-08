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
    private static final String WATERMARK_TEXT = "/clipFarmer";
    private static final String WATERMARK_IMAGE = "build/resources/watermark.png";

    /**
     * Concatenates multiple video files into a single output file with a watermark.
     *
     * @param videoPaths      List of file paths of the videos to concatenate.
     * @param outputFileName  Name of the output file.
     */
    public void concatenateVideos(List<String> videoPaths, String outputFileName) {
        long startTime = System.currentTimeMillis();

        new File(OUTPUT_FOLDER).mkdirs();
        if (videoPaths == null || videoPaths.isEmpty()) {
            log.error("No video files provided for concatenation.");
            return;
        }

        log.info("Starting video concatenation: {}", outputFileName);

        File outputFile = new File(outputFileName);
        if (outputFile.exists() && !outputFile.delete()) {
            log.error("Failed to delete existing output file: {}", outputFileName);
            return;
        }

        File tempFile = createConcatFile(videoPaths);
        if (tempFile == null) {
            log.error("Failed to create concat input file.");
            return;
        }

        try {
            String tempMergedFile = OUTPUT_FOLDER + "temp_merged.mp4";

            // Step 1: Concatenate videos
            String concatCommand = String.format("ffmpeg -f concat -safe 0 -i %s -c:v libx264 -preset fast -crf 23 -c:a aac -b:a 128k %s", tempFile.getAbsolutePath(), tempMergedFile);
            executeFFmpegCommand(concatCommand);

            // Step 2: Add watermark
            String finalOutputFile = OUTPUT_FOLDER + outputFileName;
            String watermarkCommand = String.format("ffmpeg -i %s -vf \"drawtext=text='%s':fontcolor=white:fontsize=24:x=10:y=h-th-10\" -c:v libx264 -preset fast -crf 23 -c:a aac -b:a 128k %s", tempMergedFile, WATERMARK_TEXT, finalOutputFile);
            executeFFmpegCommand(watermarkCommand);

            log.info("Video concatenation and watermarking completed: {}", finalOutputFile);
        } catch (Exception e) {
            log.error("Error during video processing", e);
        } finally {
            tempFile.delete();
        }
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        log.info("Video processing took {} seconds", elapsedTime);
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
     * Executes an FFmpeg command.
     */
    private void executeFFmpegCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }
}
