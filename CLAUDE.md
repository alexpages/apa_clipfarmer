# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this application does

A Spring Boot batch application that, for a given Twitch streamer:
1. Fetches the most-viewed Twitch clips from the last N days (via Twitch Helix API)
2. Filters clips by duration and view count
3. Downloads the clip video files (via Twitch's internal GQL API, not the public API)
4. Concatenates them into a single video with fade transitions (via `ffmpeg`/`ffprobe`)
5. Uploads the resulting video to YouTube (via YouTube Data API v3, OAuth 2.0)
6. Sends an email notification (Gmail SMTP) summarizing the run

It runs once per process invocation via `CommandLineRunner` and exits — it is a batch job, not a long-running server.

## Setup / preconditions

- `xcode-select --install` and `brew install ffmpeg` (ffmpeg/ffprobe must be on `PATH`, invoked as subprocesses by `VideoLogic`)
- A `.env` file at the repo root with:
  ```
  TWITCH_CLIENT_ID=
  TWITCH_CLIENT_SECRET=
  DATABASE_DRIVER=
  DATABASE_URL=
  DATABASE_USERNAME=
  DATABASE_PASSWORD=
  GMAIL_USER=
  GMAIL_PASSWORD=
  ```
- A MySQL database matching `src/main/resources/tables.sql` (tables: `twitch_streamer`, `twitch_clip`, `twitch_highlight`)
- `src/main/resources/client_secrets.json` populated with a real Google OAuth Client ID/Secret (YouTube Data API must be enabled) — YouTube upload auth runs an interactive local-server OAuth flow (port 8888) on first use and caches the token under `~/.oauth-credentials/`
- Gmail account with SMTP/app-password access enabled for `EmailNotificationLogic`

Twitch/DB env vars are read directly via `System.getenv(...)` into static constants (`TwitchConstants`), not via Spring `@Value` — they must be present in the process environment when the JVM starts (loaded from `.env`, not `application.properties`).

## Commands

```bash
./gradlew build                # compile + checkstyle + test
./gradlew test                 # run all tests
./gradlew test --tests "com.apa.clipfarmer.db.MyBatisConfigTest"   # run a single test
./gradlew checkstyleMain checkstyleTest   # lint only (config/checkstyle/checkstyle.xml)
./gradlew bootRun              # run the app
```

The streamer to process is currently **hardcoded** in `ClipFarmerApplication.main` (`streamerName=jasontheween`) rather than taken from real process args — see the `//TODO change for args` comment there. Valid streamer names must exist in `TwitchStreamerNameEnum`; anything else resolves to `INVALID` and the run is aborted in `ClipFarmerService.execute`.

## Architecture

Layering, top to bottom:
- `ClipFarmerApplication` (Spring Boot entry point) → `ClipFarmerService` (orchestrates the whole batch pipeline in `execute()`) → `logic/*` packages (one package per external integration: `twitch`, `video`, `youtube`, plus top-level `EmailNotificationLogic`) → `mapper/*` (MyBatis mapper interfaces + matching XML in `resources/mybatis/mapper/`) → MySQL.

Key flow in `ClipFarmerService.execute()`:
1. Parse args with JCommander into `ClipFarmerArgs` (backed by `TwitchStreamerNameEnum`)
2. `TwitchAuthLogic.getOAuthToken()` — app-level Twitch OAuth (client credentials grant)
3. `TwitchClipFetcherLogic.getTwitchClips(...)` — calls `TwitchUserLogic` to resolve broadcaster ID (and persist the streamer row), then queries Helix `/clips`, filters/sorts in memory
4. For each clip not already in `twitch_clip` (checked via `TwitchClipMapper`): `TwitchClipDownloader.downloadFile(...)` downloads the actual mp4 (this uses Twitch's *undocumented* GQL endpoint with a hardcoded public `TWITCH_GRAPHQL_CLIENT_ID`, separate from the Helix API used for metadata), then insert + commit the DB row
5. `VideoLogic.concatenateVideos(...)` shells out to `ffmpeg`/`ffprobe` per clip (fade in/out, matched by clip duration) then concatenates via ffmpeg's concat demuxer; downloaded/output files live under `build/downloads/` and `build/output/`
6. `YoutubeUploaderLogic.uploadHighlightVideo(...)` runs OAuth via `YoutubeAuth` and uploads via `YouTube.Videos.Insert`, then records a `twitch_highlight` row
7. `EmailNotificationLogic.sendEmail(...)` sends an HTML summary
8. `FileUtils.deleteDirectory(...)` cleans up `build/output` and `build/downloads`

Persistence uses **MyBatis** (not JPA/Hibernate, despite `spring-boot-starter-data-jpa` being a dependency) — `MyBatisConfig` wires the `SqlSessionFactory` and scans `com.apa.clipfarmer.mapper`. Mapper XML files live in `src/main/resources/mybatis/mapper/`, config in `src/main/resources/mybatis/mybatis-config.xml`. Some code paths get a mapper via `session.getMapper(TwitchClipMapper.class)` while others call `session.selectOne(...)`/`session.insert(...)` with a fully-qualified statement ID string (see `TwitchUserLogic.insertStreamerInDatabase`) — both styles are in use.

Two distinct Twitch integrations are involved and easy to conflate:
- **Helix API** (`api.twitch.tv/helix/...`) — official, requires app OAuth token + `TWITCH_CLIENT_ID`, used for user lookup and clip metadata (`TwitchUserLogic`, `TwitchClipFetcherLogic`)
- **GQL API** (`gql.twitch.tv/gql`) — unofficial, uses a hardcoded public client ID, used only to resolve the actual downloadable video URL for a clip (`TwitchClipDownloader`)

Configuration classes: `EmailConfig` (JavaMailSender), `MyBatisConfig` (SqlSessionFactory). `TwitchConstants` and `ClipFarmerConstants` are `@UtilityClass` holders for constants/env lookups rather than Spring `@ConfigurationProperties`.
