package com.apa.clipfarmer.logic.youtube;

import com.apa.clipfarmer.utils.YoutubeUtils;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Upload a video to the authenticated user's channel. Use OAuth 2.0 to
 * authorize the request.
 *
 * @author alexpages
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeUploaderLogic {
    /**
     * Define a global instance of a Youtube object forYouTube Data API requests.
     */
    private static YouTube youtube;

    /**
     * Define a global variable that specifies the MIME type of the video
     * being uploaded.
     */
    private static final String VIDEO_FILE_FORMAT = "video/*";

    private static final String SAMPLE_VIDEO_FILENAME = "sample-video.mp4";

    /**
     * Upload the user-selected video to the user's YouTube channel. The code
     * looks for the video in the application's project folder and uses OAuth
     * 2.0 to authorize the API request.
     */
    public void uploadHighlightVideo(String youtubeTitle, String youtubeDescription, String pathFileToUpload) {
        log.info("Youtube Title for the next upload: {}", youtubeTitle);
        log.info("Youtube Description for the next upload: {}", youtubeDescription);
        log.info("Path to the file to be uploaded: {}", pathFileToUpload);

        // This OAuth 2.0 access scope allows an application to upload files
        // to the authenticated user's YouTube channel.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.upload");

        try {
            // Authorize the request.
            Credential credential = YoutubeAuth.authorize(scopes, "uploadvideo");
            log.info("OAuth 2.0 authorization successful. Access token: {}", credential.getAccessToken());
            log.info("Access token expiry time: {}", credential.getExpirationTimeMilliseconds());

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(
                    YoutubeAuth.HTTP_TRANSPORT,
                    YoutubeAuth.JSON_FACTORY,
                    credential).setApplicationName("clipfarmer")
                    .build();

            System.out.println("Uploading: " + pathFileToUpload);

            // Add extra information to the video before uploading.
            Video videoObjectDefiningMetadata = getMetadata(youtubeTitle, youtubeDescription);
            File videoFile = new File(pathFileToUpload);
            InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT, new FileInputStream(videoFile));
            mediaContent.setLength(videoFile.length());

            // Insert the video:
            // - First argument is the info that the API request is setting and which info the API should return
            // - Second argument is the metadata
            // - Third argument is the video content.
            YouTube.Videos.Insert videoInsert = youtube.videos()
                    .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);

            // Set the upload type and add an event listener.
            MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

            // "True" uploads in one request
            // "False" uploads even if there is network
            uploader.setDirectUploadEnabled(false);

            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case INITIATION_STARTED:
                            System.out.println("Initiation Started");
                            break;
                        case INITIATION_COMPLETE:
                            System.out.println("Initiation Completed");
                            break;
                        case MEDIA_IN_PROGRESS:
                            System.out.println("Upload in progress");
                            System.out.println("Upload percentage: " + uploader.getProgress());
                            break;
                        case MEDIA_COMPLETE:
                            System.out.println("Upload Completed!");
                            break;
                        case NOT_STARTED:
                            System.out.println("Upload Not Started!");
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);

            // Call the API and upload the video.
            Video returnedVideo = videoInsert.execute();

            // Print data about the newly inserted video from the API response.
            System.out.println("\n================== Returned Video ==================\n");
            System.out.println("  - Id: " + returnedVideo.getId());
            System.out.println("  - Title: " + returnedVideo.getSnippet().getTitle());
            System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
            System.out.println("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
            System.out.println("  - Video Count: " + returnedVideo.getStatistics().getViewCount());

        } catch (GoogleJsonResponseException e) {
            if (e.getDetails() != null) {
                System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                        + e.getDetails().getMessage());
            } else {
                System.err.println("GoogleJsonResponseException occurred, but no details were provided.");
            }
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Video getMetadata(String youtubeTitle, String youtubeDescription) {
        Video videoObjectDefiningMetadata = new Video();

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("public"); // "unlisted" and "private."
        status.setMadeForKids(false);
        videoObjectDefiningMetadata.setStatus(status);

        // Most of the video's metadata is set on the VideoSnippet object.
        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(youtubeTitle);
        snippet.setDescription(youtubeDescription);

        // Set the keyword tags that you want to associate with the video.
        List<String> tags = new ArrayList<>();
        tags.add("Twitch");
        tags.add("Clip");
        tags.add("Highlight");
        snippet.setTags(tags);

        // Add the completed snippet object to the video resource.
        videoObjectDefiningMetadata.setSnippet(snippet);
        return videoObjectDefiningMetadata;
    }
}