# YouTube Video Downloader RSS

This project allows you to download YouTube videos from channels and playlists, and generate RSS feeds for them.

## Prerequisites

- Java 11 or higher
- Node.js and npm
- Python 3
- yt-dlp (YouTube downloader)

## Backend Setup

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/ytdl-rss.git
    cd ytdl-rss
    ```

2. Configure the YouTube API key:
    - Open `src/main/resources/application.properties`
    - Replace the value of `youtube.api.key` with your YouTube API key.

3. Build and run the Spring Boot application:
    ```sh
    mvn clean install
    mvn spring-boot:run
    ```

## Frontend Setup

1. Navigate to the frontend directory:
    ```sh
    cd frontend
    ```

2. Install the dependencies:
    ```sh
    npm install
    ```

3. Start the React development server:
    ```sh
    npm start
    ```

## Usage

- Open your browser and navigate to `http://localhost:3000`
- Use the interface to download videos from YouTube channels and playlists.
- Generate RSS feeds for the downloaded videos.

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

### Get Channel Names and Playlists
- **GET** `/channelNamesAndPlaylists`
- Retrieves channel names and playlist titles from JSON files.

### Update Videos
- **GET** `/updateVideos`
- Updates the videos in the JSON files.
