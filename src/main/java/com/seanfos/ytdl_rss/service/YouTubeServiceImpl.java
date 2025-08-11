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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import java.util.stream.Collectors;
import org.springframework.web.client.HttpClientErrorException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import jakarta.annotation.PostConstruct;

@Service
public class YouTubeServiceImpl implements YouTubeService{
    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.baseUrl}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    // Downloader hardening configuration
    @Value("${downloader.ytdlp.path:/usr/local/bin/yt-dlp}")
    private String ytdlpPath;

    @Value("${downloader.ytdlp.sha256:}")
    private String ytdlpSha256; // optional; if set, verify before running

    @Value("${downloader.allowed.hosts:youtube.com,youtu.be}")
    private String allowedHostsCsv;

    @Value("${downloader.output.dir:${user.home}/videos}")
    private String outputRootDir;

    @Value("${downloader.concurrency.max:2}")
    private int maxConcurrentDownloads;

    @Value("${downloader.timeout.seconds:900}")
    private long downloadTimeoutSeconds;

    private Semaphore downloadSemaphore;

    @Autowired
    public YouTubeServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initializeSemaphore() {
        this.downloadSemaphore = new Semaphore(Math.max(1, maxConcurrentDownloads));
    }

    @Override
    public Video getVideoData(String videoId) {
        String url = String.format("%svideos?part=snippet&id=%s&key=%s", apiUrl, videoId, apiKey);
        String response = restTemplate.getForObject(url, String.class);

        JSONObject json = new JSONObject(response);
        JSONArray items = json.optJSONArray("items");
        if (items == null || items.length() == 0) {
            throw new IllegalArgumentException("Video not found for id=" + videoId);
        }

        JSONObject item = items.getJSONObject(0);
        JSONObject snippet = item.getJSONObject("snippet");

        Video video = new Video();
        video.setTitle(snippet.optString("title", videoId));
        video.setDescription(snippet.optString("description", ""));
        video.setLink("https://www.youtube.com/watch?v=" + item.getString("id"));

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
        // Validate link host
        String videoLink = video.getLink();
        if (!isAllowedHost(videoLink)) {
            System.err.println("Blocked download: disallowed host in URL " + videoLink);
            return;
        }

        // Ensure output directory exists
        String outputPathTemplate = ensureOutputDir(outputRootDir) + "/%(channel)s/%(title)s.%(ext)s";

        // Verify yt-dlp path and checksum if provided
        Path ytdlpBinary = Paths.get(ytdlpPath);
        if (!Files.isRegularFile(ytdlpBinary) || !Files.isExecutable(ytdlpBinary)) {
            System.err.println("yt-dlp binary not found or not executable at: " + ytdlpPath);
            return;
        }
        if (ytdlpSha256 != null && !ytdlpSha256.isBlank()) {
            try {
                String actual = sha256Hex(ytdlpBinary);
                if (!ytdlpSha256.equalsIgnoreCase(actual)) {
                    System.err.println("yt-dlp checksum mismatch; refusing to execute. Expected " + ytdlpSha256 + " got " + actual);
                    return;
                }
            } catch (IOException | NoSuchAlgorithmException ex) {
                System.err.println("Failed to compute checksum for yt-dlp: " + ex.getMessage());
                return;
            }
        }

        // Concurrency limit
        boolean acquired = false;
        try {
            acquired = downloadSemaphore.tryAcquire(1, TimeUnit.SECONDS);
            if (!acquired) {
                System.err.println("Download concurrency limit reached; skipping for now: " + videoLink);
                return;
            }

            ProcessBuilder pb = new ProcessBuilder(ytdlpPath, "-o", outputPathTemplate, videoLink);
            pb.redirectErrorStream(false);
            Process process = pb.start();

            // Capture stdout and stderr concurrently
            StreamCollector stdout = new StreamCollector(process.getInputStream());
            StreamCollector stderr = new StreamCollector(process.getErrorStream());
            Thread tOut = new Thread(stdout);
            Thread tErr = new Thread(stderr);
            tOut.start();
            tErr.start();

            boolean finished = process.waitFor(downloadTimeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.err.println("yt-dlp timed out after " + downloadTimeoutSeconds + "s for URL: " + videoLink);
                return;
            }

            int exit = process.exitValue();
            // Ensure collectors finished
            tOut.join(TimeUnit.SECONDS.toMillis(5));
            tErr.join(TimeUnit.SECONDS.toMillis(5));

            if (exit != 0) {
                System.err.println("yt-dlp failed (exit=" + exit + ") for URL: " + videoLink + "\nSTDERR:\n" + stderr.getContent());
            } else {
                System.out.println("yt-dlp succeeded for URL: " + videoLink + "\nSTDOUT:\n" + stdout.getContent());
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.err.println("Download interrupted: " + ie.getMessage());
        } catch (IOException ioe) {
            System.err.println("Failed to start yt-dlp: " + ioe.getMessage());
        } finally {
            if (acquired) {
                downloadSemaphore.release();
            }
        }
    }

    private boolean isAllowedHost(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            List<String> allowed = Arrays.stream(allowedHostsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            for (String allowedHost : allowed) {
                if (host.equalsIgnoreCase(allowedHost) || host.toLowerCase().endsWith("." + allowedHost.toLowerCase())) {
                    return true;
                }
            }
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private String ensureOutputDir(String rootDir) {
        try {
            Path root = Paths.get(rootDir);
            Files.createDirectories(root);
            return root.toString();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create output directory: " + rootDir, e);
        }
    }

    private static class StreamCollector implements Runnable {
        private final InputStream inputStream;
        private final StringBuilder buffer = new StringBuilder();

        StreamCollector(InputStream inputStream) { this.inputStream = inputStream; }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    buffer.append(line).append(System.lineSeparator());
                }
            } catch (IOException ignored) {
            }
        }

        public String getContent() { return buffer.toString(); }
    }

    private static String sha256Hex(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(path)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) {
                digest.update(buf, 0, r);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
