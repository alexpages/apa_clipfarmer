package com.apa.clipfarmer.model;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to store all Twitch constants
 *
 * @author alexpages
 */
@UtilityClass
@Slf4j
public final class TwitchConstants {

    public static final String TWITCH_CLIENT_SECRET = System.getenv("TWITCH_CLIENT_SECRET");
    public static final String TWITCH_CLIENT_ID = System.getenv("TWITCH_CLIENT_ID");
    public static final String TWITCH_OAUTH_API = "https://id.twitch.tv/oauth2/token";
    public static final String TWITCH_CLIP_API = "https://api.twitch.tv/helix/clips";
    public static final String TWITCH_USERS_API = "https://api.twitch.tv/helix/users";
    public static final String TWITCH_GRAPHQL_CLIENT_ID = "kimne78kx3ncx6brgo4mv6wki5h1ko"; // Public Client ID
    public static final String TWITCH_GQL_URL = "https://gql.twitch.tv/gql";


}
