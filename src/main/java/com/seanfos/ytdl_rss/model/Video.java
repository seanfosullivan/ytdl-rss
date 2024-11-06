package com.seanfos.ytdl_rss.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Video {
    private String title;
    private String description;
    private String link;
}
