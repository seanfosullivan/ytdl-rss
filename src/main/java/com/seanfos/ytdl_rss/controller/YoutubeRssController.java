package com.seanfos.ytdl_rss.controller;

import com.seanfos.ytdl_rss.service.YouTubeServiceImpl;
import com.seanfos.ytdl_rss.model.Video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class YoutubeRssController {

    private final YouTubeServiceImpl youTubeService;

    @Autowired
    public YoutubeRssController(YouTubeServiceImpl youTubeService) {
        this.youTubeService = youTubeService;
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
    

}