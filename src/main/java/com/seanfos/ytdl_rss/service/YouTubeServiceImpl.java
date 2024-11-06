package com.seanfos.ytdl_rss.service;

import com.seanfos.ytdl_rss.model.Video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeServiceImpl implements YouTubeService{
    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.baseUrl}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    @Autowired
    public YouTubeServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Video getVideoData(String videoId) {
        String url = String.format("%svideos?part=snippet&videoId=%s&key=%s", apiUrl, videoId, apiKey);
        String response = restTemplate.getForObject(url, String.class);
        JSONObject item = new JSONObject(response).getJSONObject("snippet");

        Video video = new Video();
        video.setTitle(item.getString("title"));
        video.setDescription(item.getString("description"));
        video.setLink("https://www.youtube.com/watch?v=" + item.getJSONObject("resourceId").getString("videoId"));

        return video;
    }

    @Override
    public List<Video> getPlaylistVideos(String playlistId) {
        String url = String.format("%splaylistItems?part=snippet&playlistId=%s&maxResults=20&key=%s", apiUrl, playlistId, apiKey);
        String response = restTemplate.getForObject(url, String.class);

        List<Video> videos = new ArrayList<>();
        if (response != null) {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray items = jsonResponse.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i).getJSONObject("snippet");
                
                Video video = new Video();
                video.setTitle(item.getString("title"));
                video.setDescription(item.getString("description"));
                video.setLink("https://www.youtube.com/watch?v=" + item.getJSONObject("resourceId").getString("videoId"));

                videos.add(video);
            }
        }
        return videos;
    }

    @Override
    public List<Video> getChannelVideos(String channelName) {
        String channelId = getChannelId(channelName);
        if (channelId != null){
            return getChannelVideosById(channelId);
        }
        return List.of();
    }

    @Override
    public String getChannelId(String channelName) {
        String url = String.format("%ssearch?part=id&q=%s&key=%s&type=channel&maxResults=1", apiUrl, channelName, apiKey);
        String response = restTemplate.getForObject(url, String.class);
        JSONObject jsonResponse = new JSONObject(response);
        System.out.println(jsonResponse); //this is probably not returning just the channelId
        return response;
    }

    @Override
    public List<Video> getChannelVideosById(String channelId) {
        String url = String.format("%ssearch?part=snippet&channelId=%s&maxResults=20&key=%s&order=date", apiUrl, channelId, apiKey);
        String response = restTemplate.getForObject(url, String.class);
        List<Video> videos = new ArrayList<>();
        if (response != null) {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray items = jsonResponse.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i).getJSONObject("snippet");
                
                Video video = new Video();
                video.setTitle(item.getString("title"));
                video.setDescription(item.getString("description"));
                video.setLink("https://www.youtube.com/watch?v=" + item.getJSONObject("resourceId").getString("videoId"));

                videos.add(video);
            }
        }
        return videos;
    }

}
