-- Select the clipfarmer database
USE clipfarmer;

-- Drop tables if they already exist
DROP TABLE IF EXISTS twitch_clip;
DROP TABLE IF EXISTS twitch_streamer;

-- Create table for TwitchStreamer
CREATE TABLE twitch_streamer (
    id INT NOT NULL AUTO_INCREMENT,
    twitchStreamerName VARCHAR(255) NOT NULL,
    addedClips BLOB, -- Use JSON to store an array of strings (clip IDs)
    PRIMARY KEY (id)
);

-- Create table for TwitchClip
CREATE TABLE twitch_clip (
    id INT NOT NULL AUTO_INCREMENT,
    clipId VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    creatorName VARCHAR(255) NOT NULL,
    viewCount INT NOT NULL,
    createdAt DATETIME NOT NULL,
    streamerId INT, -- Add streamerId to relate clips to streamers
    PRIMARY KEY (id),
    FOREIGN KEY (streamerId) REFERENCES twitch_streamer(id) ON DELETE SET NULL
);
