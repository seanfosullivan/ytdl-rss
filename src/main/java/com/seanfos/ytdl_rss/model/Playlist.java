package com.seanfos.ytdl_rss.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {
    private String id;
    private List<Video> videos;
}
