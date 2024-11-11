package com.seanfos.ytdl_rss.service;

import com.seanfos.ytdl_rss.model.Video;
import java.util.List;

public interface RssFeedService {
    String generateRssFeed(String channelJsonPath, String playlistJsonPath);
}
