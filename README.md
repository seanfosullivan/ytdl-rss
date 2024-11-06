# Introduction
## Overview
The basis of this project is to automate the downloading of a YouTube playlist/video/channel and provide it as a video podcast through RSS feed.
## Structure
```
src/main/java/com/example/youtubetorss
│
├── controller
│   └── PlaylistController.java   // REST API to expose RSS feed endpoint
│
├── service
│   └── YouTubeService.java       // Service for fetching data from YouTube API
│   └── YouTubeServiceImpl.java       
│   └── RssFeedService.java       // Service to generate RSS feed
│   └── RssFeedServiceImpl.java       
│
├── model
│   └── Video.java                // Model for video data
│
└── config
    └── AppConfig.java            // Configuration for application properties
```
## Goals
- [x] Retrieve video data from YouTube Playlist ✅ 2024-10-29
- [ ] Retrieve video data from YouTube video
- [ ] Retrieve video data from YouTube Channel
- [ ] Create model for video, playlist and channel
- [ ] Create storage for YouTube sources
- [ ] Automate checks for new videos from playlist and channel sources periodically
- [ ] Provide video as video podcast
- [ ] Automate video download yt-dlp
- [ ] Dockerize project
> [!NOTE] Dockerizing yt-dlp
> yt-dlp may not be dockerizeable might require script to download, install and integrate or may be separate prerequisite if not possible to integrate

# Installation
In resources folder create application.properties file:
```application.properties
spring.application.name=ytdl-rss

youtube.api.key=<Your API key>

youtube.api.baseUrl=https://www.googleapis.com/youtube/v3/
```
