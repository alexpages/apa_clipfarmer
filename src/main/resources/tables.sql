-- Select the clipfarmer database
USE clipfarmer;

-- Drop tables if they already exist
DROP TABLE IF EXISTS twitch_clip;
DROP TABLE IF EXISTS twitch_streamer;

-- Create table for TwitchStreamer
CREATE TABLE twitch_streamer (
    id INT NOT NULL AUTO_INCREMENT,
    twitch_streamer_name VARCHAR(255) NOT NULL,
    broadcaster_id VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

-- Create table for TwitchClip
CREATE TABLE twitch_clip (
    id INT NOT NULL AUTO_INCREMENT,
    clip_id VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    creator_name VARCHAR(255) NOT NULL,
    broadcaster_id VARCHAR(255),
    view_count INT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (broadcaster_id) REFERENCES twitch_streamer(broadcaster_id) ON DELETE SET NULL
);
