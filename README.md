**This application does the following:**

- Run the application with twitchStreamer name
- Fetches most viewed clips from Twitch in the last 7 days
- Filters clips with time and views
- Download the clips
- Creates a compilation with concatenation
- Uploads the video to Youtube
- Sends an email with confirmation

**Preconditions Software:**

1. xcode-select --install
2. brew install ffmpeg

**Preconditions Software:**

1. Enable smtp with your google account
2. Enable Youtube Data API to get OAuth
3. Create .env with all credentials

`  TWITCH_CLIENT_ID=
   TWITCH_CLIENT_SECRET=
   DATABASE_DRIVER=
   DATABASE_URL=
   DATABASE_USERNAME=
   DATABASE_PASSWORD=
   GMAIL_USER=
   GMAIL_PASSWORD= `


