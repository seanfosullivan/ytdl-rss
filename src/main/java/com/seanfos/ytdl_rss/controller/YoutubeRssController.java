package com.seanfos.ytdl_rss.controller;

import com.seanfos.ytdl_rss.service.YouTubeServiceImpl;
import com.seanfos.ytdl_rss.service.RssFeedService;
import com.seanfos.ytdl_rss.model.Video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.json.JSONObject;
import org.json.JSONArray;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import java.util.List;

@RestController
public class YoutubeRssController {

    private final YouTubeServiceImpl youTubeService;
    private final RssFeedService rssFeedService;

    @Autowired
    public YoutubeRssController(YouTubeServiceImpl youTubeService, RssFeedService rssFeedService) {
        this.youTubeService = youTubeService;
        this.rssFeedService = rssFeedService;
    }

    @GetMapping(value = "/video")
    public String downloadVideo(@RequestParam String videoId) {
        try {
            Video video = youTubeService.getVideoData(videoId);
            youTubeService.ytdlpDownloader(video);
            return video.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Unable to download video</error>";
        }
    }

    @GetMapping(value = "/playlist")
    public String downloadPlaylistVideos(@RequestParam String playlistId) {
        try {
            List<Video> videos = youTubeService.getPlaylistVideos(playlistId);
            StringBuilder titles = new StringBuilder();
            for (Video v : videos){
                titles.append("[");
                titles.append(v.getLink());
                titles.append("],");
                youTubeService.ytdlpDownloader(v);
            }
            return titles.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Unable to download videos</error>";
        }
    }

    @GetMapping(value = "/channel")
    public String downloadChannelVideos(@RequestParam String channelName) {
        try {
            List<Video> videos = youTubeService.getChannelVideos(channelName);
            StringBuilder titles = new StringBuilder();
            
            // Limit to 10 videos
            int maxVideos = 10;
            int videoCount = 0;
    
            for (Video v : videos) {
                // Stop if we've processed 10 videos
                if (videoCount >= maxVideos) {
                    break;
                }
    
                titles.append("[");
                titles.append(v.getLink());
                titles.append("],");
    
                // Call the downloader method
                youTubeService.ytdlpDownloader(v);
                
                // Increment video counter
                videoCount++;
            }
    
            return titles.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Unable to download videos</error>";
        }
    }

    @GetMapping(value = "/rssfeed")
    public String generateRssFeed() {
        try {
            String channelJsonPath = "/home/seanfos/workdir/ytdl-rss/src/main/resources/data/channel.json";
            String playlistJsonPath = "/home/seanfos/workdir/ytdl-rss/src/main/resources/data/playlist.json";
            return rssFeedService.generateRssFeed(channelJsonPath, playlistJsonPath);
        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Unable to generate RSS feed</error>";
        }
    }

    @GetMapping(value = "/downloadFromChannelJson")
    public String downloadFromChannelJson() {
        try {
            String filePath = "/home/seanfos/workdir/ytdl-rss/src/main/resources/data/channel.json";
            youTubeService.downloadVideosFromJson(filePath);
            return "Videos downloaded from channel JSON file.";
        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Unable to download videos from channel JSON file</error>";
        }
    }

    @GetMapping(value = "/downloadFromPlaylistJson")
    public String downloadFromPlaylistJson() {
        try {
            String filePath = "/home/seanfos/workdir/ytdl-rss/src/main/resources/data/playlist.json";
            youTubeService.downloadVideosFromJson(filePath);
            return "Videos downloaded from playlist JSON file.";
        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Unable to download videos from playlist JSON file</error>";
        }
    }

    @GetMapping(value = "/channelNamesAndPlaylists")
    public String getChannelNamesAndPlaylists() {
        try {
            String channelFilePath = "/home/seanfos/workdir/ytdl-rss/src/main/resources/data/channel.json";
            String playlistFilePath = "/home/seanfos/workdir/ytdl-rss/src/main/resources/data/playlist.json";

            JSONObject channelJson = new JSONObject(new String(Files.readAllBytes(Paths.get(channelFilePath))));
            JSONObject playlistJson = new JSONObject(new String(Files.readAllBytes(Paths.get(playlistFilePath))));

            JSONArray channelNames = new JSONArray();
            channelNames.put(channelJson.getString("name"));

            JSONArray playlistTitles = new JSONArray();
            JSONArray videos = channelJson.getJSONArray("videos");
            for (int i = 0; i < videos.length(); i++) {
                playlistTitles.put(videos.getJSONObject(i).getString("title"));
            }

            JSONObject result = new JSONObject();
            result.put("channelNames", channelNames);
            result.put("playlistTitles", playlistTitles);

            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "<error>Unable to fetch channel names and playlists</error>";
        }
    }

    @GetMapping(value = "/updateVideos")
    public String updateVideos() {
        try {
            youTubeService.updatePlaylistAndChannelFiles();
            return "Videos updated successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Unable to update videos</error>";
        }
    }
}