package com.seanfos.ytdl_rss.service;

import java.util.List;

import com.seanfos.ytdl_rss.model.Video;

public interface YouTubeService {
    
    /**
     * Retrieve video data from videoID
     * @param videoId
     * @return Video data for provided videoID
     */
    public Video getVideoData(String videoId); 

    /**
     * Retrieve a list of video data from playlistId
     * @param playlistId
     * @return A list of videos from playlist
     */
    public List<Video> getPlaylistVideos(String playlistId);

    /**
     * Retrieve a list of videos from channelName
     * @param channelName
     * @return A list of videos from channel
     */
    public List<Video> getChannelVideos(String channelName); 

    /**
     * Retrieve channelId from channelName
     * @param channelName
     * @return channelId 
     */
    public String getChannelId(String channelName);

    /**
     * Retrieve a lisf of videos from channelId
     * @param channelId
     * @return List of videos
     */
    public List<Video> getChannelVideosById(String channelId); 

    /**
     * Download video using youtube-dl
     * @param video
     */
    public void ytdlpDownloader(Video video);

    /**
     * Download videos from JSON file
     * @param filePath
     */
    void downloadVideosFromJson(String filePath);
}