# YouTube Video Downloader RSS

This project allows you to download YouTube videos from channels and playlists, and generate RSS feeds for them.

## Prerequisites

- Java 17 or higher
- Python 3
- yt-dlp (YouTube downloader)

## Setup

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/ytdl-rss.git
    cd ytdl-rss
    ```

2. Configure the YouTube API key (via environment variable):
    - Copy the sample config:
      ```sh
      cp src/main/resources/application-example.properties src/main/resources/application.properties
      ```
    - Set your API key in the environment before starting the app:
      ```sh
      export YOUTUBE_API_KEY=your_key_here
      ```

3. Build and run the Spring Boot application:
    ```sh
    mvn clean install
    mvn spring-boot:run
    ```

### Run with Docker

Build and run using Docker:

```sh
# Build image
docker build -t ytdl-rss:local .

# Run container (mount host output dir and pass API key)
docker run --rm -p 8080:8080 \
  -e YOUTUBE_API_KEY="$YOUTUBE_API_KEY" \
  -e DOWNLOADER_OUTPUT_DIR=/data/videos \
  -e DOWNLOADER_YTDLP_PATH=/usr/local/bin/yt-dlp \
  -v $(pwd)/data/videos:/data/videos \
  ytdl-rss:local
```

Or with docker-compose:

```sh
export YOUTUBE_API_KEY=your_key_here
docker compose up --build
```

## Configuration

These properties can be set in `src/main/resources/application.properties` or via environment variables.

- YOUTUBE_API_KEY (env var) — required
- downloader.ytdlp.path — path to yt-dlp binary (default `/usr/local/bin/yt-dlp`)
- downloader.allowed.hosts — allowed video hostnames (default `youtube.com,youtu.be`)
- downloader.output.dir — download output root (default `${user.home}/videos`)
- downloader.concurrency.max — max concurrent downloads (default `2`)
- downloader.timeout.seconds — per-download timeout (default `900`)

## Usage

Base URL: `http://localhost:8080`

Examples:

```sh
# Download a single video by ID
curl "http://localhost:8080/video?videoId=VIDEO_ID"

# Download all videos from a playlist
curl "http://localhost:8080/playlist?playlistId=PLAYLIST_ID"

# Download recent videos from a channel by name
curl "http://localhost:8080/channel?channelName=CHANNEL_NAME"

# Generate RSS feed from JSON data files
curl "http://localhost:8080/rssfeed"
```

## API Endpoints

### Download Video
- **GET** `/video?videoId={videoId}`
- Downloads a single video by its ID.

### Download Playlist Videos
- **GET** `/playlist?playlistId={playlistId}`
- Downloads videos from a playlist by its ID.

### Download Channel Videos
- **GET** `/channel?channelName={channelName}`
- Downloads videos from a channel by its name.

### Generate RSS Feed
- **GET** `/rssfeed`
- Generates an RSS feed for the downloaded videos.

### Download Videos from Channel JSON
- **GET** `/downloadFromChannelJson`
- Downloads videos listed in the channel JSON file.

### Download Videos from Playlist JSON
- **GET** `/downloadFromPlaylistJson`
- Downloads videos listed in the playlist JSON file.

### Update Videos
- **GET** `/updateVideos`
- Updates the videos in the JSON files.
