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

    @GetMapping(value = "/rss")
    public String getRssFeed(@RequestParam String playlistId) {
        try {
            List<Video> videos = youTubeService.getPlaylistVideos(playlistId);
            StringBuilder titles = new StringBuilder();
            for (Video v : videos){
                titles.append(v.getTitle());
                titles.append(" ");
            }
            return titles.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Unable to generate RSS feed</error>";
        }
    }
}