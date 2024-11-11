package com.seanfos.ytdl_rss.service;

import com.seanfos.ytdl_rss.model.Video;
import com.seanfos.ytdl_rss.model.Channel;
import com.seanfos.ytdl_rss.model.Playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;
import org.springframework.web.client.HttpClientErrorException;

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
        JSONObject jsonResponse = null;
        if (response != null) {
            jsonResponse = new JSONObject(response);
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
        savePlaylistToFile(new Playlist(playlistId, jsonResponse.getString("title"), videos), Paths.get("src", "main", "resources", "data", "playlist.json").toString());
        return videos;
    }

    @Override
    public List<Video> getChannelVideos(String channelName) {
        String channelId = getChannelId(channelName);
        if (channelId != null){
            List<Video> videos = getChannelVideosById(channelId);
            saveChannelToFile(new Channel(channelName, videos), Paths.get("src", "main", "resources", "data", "channel.json").toString());
            return videos;
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
        try {
            System.out.println("Request URL: " + url); // Log the request URL
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("Response: " + response); // Log the response
            List<Video> videos = new ArrayList<>();
        
            if (response != null) {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray items = jsonResponse.getJSONArray("items");
        
                for (int i = 0; i < items.length() && videos.size() < 10; i++) {
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
        } catch (HttpClientErrorException e) {
            System.err.println("Error fetching channel videos: " + e.getMessage());
            return List.of();
        }
    }

    private void savePlaylistToFile(Playlist playlist, String filePath) {
        try {
            JSONObject jsonObject;
            if (Files.exists(Paths.get(filePath))) {
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                jsonObject = new JSONObject(content);
            } else {
                jsonObject = new JSONObject();
                jsonObject.put("id", playlist.getId());
                jsonObject.put("name", playlist.getName());
                jsonObject.put("videos", new JSONArray());
            }

            JSONArray jsonArray = jsonObject.getJSONArray("videos");
            for (Video video : playlist.getVideos()) {
                JSONObject videoObject = new JSONObject();
                videoObject.put("title", video.getTitle());
                videoObject.put("description", video.getDescription());
                videoObject.put("link", video.getLink());
                jsonArray.put(videoObject);
            }

            Files.write(Paths.get(filePath), jsonObject.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveChannelToFile(Channel channel, String filePath) {
        try {
            JSONObject jsonObject;
            if (Files.exists(Paths.get(filePath))) {
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                if (content.isEmpty()) {
                    jsonObject = new JSONObject();
                } else {
                    jsonObject = new JSONObject(content);
                }
            } else {
                jsonObject = new JSONObject();
                jsonObject.put("name", channel.getName());
                jsonObject.put("videos", new JSONArray());
            }

            JSONArray jsonArray = jsonObject.getJSONArray("videos");
            for (Video video : channel.getVideos()) {
                JSONObject videoObject = new JSONObject();
                videoObject.put("title", video.getTitle());
                videoObject.put("description", video.getDescription());
                videoObject.put("link", video.getLink());
                jsonArray.put(videoObject);
            }

            Files.write(Paths.get(filePath), jsonObject.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    public void downloadVideosFromJson(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(new JSONTokener(content));
            JSONArray videos = jsonObject.getJSONArray("videos");

            for (int i = 0; i < videos.length(); i++) {
                JSONObject videoObject = videos.getJSONObject(i);
                Video video = new Video();
                video.setTitle(videoObject.getString("title"));
                video.setDescription(videoObject.getString("description"));
                video.setLink(videoObject.getString("link"));
                ytdlpDownloader(video);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //@Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void updatePlaylistAndChannelFiles() {
        // Get playlist IDs from JSON file
        String playlistFilePath = Paths.get("src", "main", "resources", "data", "playlist.json").toString();
        List<String> playlistIds = getIdsFromJsonFile(playlistFilePath, "id");

        // Get channel IDs from JSON file
        String channelFilePath = Paths.get("src", "main", "resources", "data", "channel.json").toString();
        List<String> channelIds = getIdsFromJsonFile(channelFilePath, "id");

        // Update playlist JSON files
        for (String playlistId : playlistIds) {
            getPlaylistVideos(playlistId);
        }

        // Update channel JSON files
        for (String channelId : channelIds) {
            getChannelVideosById(channelId);
        }
    }

    private List<String> getIdsFromJsonFile(String filePath, String key) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(content);
            if (jsonObject.has(key) && jsonObject.get(key) instanceof JSONArray) {
                JSONArray jsonArray = jsonObject.getJSONArray(key);
                return jsonArray.toList().stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
            } else if (jsonObject.has(key)) {
                return List.of(jsonObject.getString(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of();
    }

}
