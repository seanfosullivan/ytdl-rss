package com.seanfos.ytdl_rss.service;


public interface RssFeedService {
    String generateRssFeed(String channelJsonPath, String playlistJsonPath);
}
