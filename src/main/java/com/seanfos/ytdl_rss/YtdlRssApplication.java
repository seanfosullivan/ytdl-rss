package com.seanfos.ytdl_rss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class YtdlRssApplication {

    public static void main(String[] args) {
        SpringApplication.run(YtdlRssApplication.class, args);
    }

}
