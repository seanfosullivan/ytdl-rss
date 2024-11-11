package com.seanfos.ytdl_rss.service;

import com.seanfos.ytdl_rss.model.Video;
import org.springframework.stereotype.Service;
import java.util.List;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.io.WireFeedOutput;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;

@Service
public class RssFeedServiceImpl implements RssFeedService {

    @Override
    public String generateRssFeed(String channelJsonPath, String playlistJsonPath) {
        List<Video> videos = new ArrayList<>();
        videos.addAll(parseVideosFromJson(channelJsonPath));
        videos.addAll(parseVideosFromJson(playlistJsonPath));

        Channel channel = new Channel("rss_2.0");
        channel.setTitle("YouTube Video Podcast");
        channel.setDescription("A podcast feed of YouTube videos");
        channel.setLink("localhost:8080/rss");

        for (Video video : videos) {
            Item item = new Item();
            item.setTitle(video.getTitle());
            item.setLink(video.getLink());

            Description description = new Description();
            description.setValue(video.getDescription());
            item.setDescription(description);

            channel.getItems().add(item);
        }

        try {
            WireFeedOutput output = new WireFeedOutput();
            return output.outputString(channel);
        } catch (Exception e) {
            e.printStackTrace();
            return "<error>Unable to generate RSS feed</error>";
        }
    }

    private List<Video> parseVideosFromJson(String filePath) {
        List<Video> videos = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(new JSONTokener(content));
            JSONArray videoArray = jsonObject.getJSONArray("videos");

            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject videoObject = videoArray.getJSONObject(i);
                Video video = new Video();
                video.setTitle(videoObject.getString("title"));
                video.setDescription(videoObject.getString("description"));
                video.setLink(videoObject.getString("link"));
                videos.add(video);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videos;
    }
}
