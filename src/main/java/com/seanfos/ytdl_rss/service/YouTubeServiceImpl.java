package com.seanfos.ytdl_rss.service;

import com.seanfos.ytdl_rss.model.Video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        // Construct the API URL
        String url = String.format("%ssearch?part=id&q=%s&key=%s&type=channel&maxResults=1", apiUrl, channelName, apiKey);
        
        // Send the request to the YouTube API
        String response = restTemplate.getForObject(url, String.class);
        
        // Parse the response into a JSONObject
        JSONObject jsonResponse = new JSONObject(response);
        
        // Check if the response contains any items
        if (jsonResponse.has("items") && jsonResponse.getJSONArray("items").length() > 0) {
            // Extract the channelId from the first item
            return jsonResponse.getJSONArray("items")
                                           .getJSONObject(0)
                                           .getJSONObject("id")
                                           .getString("channelId");
        } else {
            // If no items or no channelId, return a meaningful message
            return "Channel not found";
        }
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
                
                // Ensure that the item is a video and contains the 'resourceId' field
                if (items.getJSONObject(i).getJSONObject("id").has("videoId")) {
                    Video video = new Video();
                    video.setTitle(item.getString("title"));
                    video.setDescription(item.getString("description"));
    
                    // Get the video link
                    String videoId = items.getJSONObject(i).getJSONObject("id").getString("videoId");
                    video.setLink("https://www.youtube.com/watch?v=" + videoId);
    
                    videos.add(video);
                }
            }
        }
        return videos;
    }

    public void ytdlpDownloader(Video video) {
        try {
            // Get the home directory path for the user
            String userHome = System.getProperty("user.home");
            
            // Define the output directory for the downloaded video
            String outputPath = userHome + "/videos/%(channel)s/%(title)s.%(ext)s";
            
            // Get the video URL
            String videoLink = video.getLink();

            // Construct the command with proper arguments
            ProcessBuilder pb = new ProcessBuilder("python3", userHome + "/Downloads/yt-dlp", "-o", outputPath, videoLink);

            // Start the process
            Process process = pb.start();

            // Read the output from the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to complete and check exit status
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Python script executed successfully.");
            } else {
                System.out.println("Error: Python script execution failed.");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
