package com.seanfos.ytdl_rss;

import com.seanfos.ytdl_rss.controller.YoutubeRssController;
import com.seanfos.ytdl_rss.model.Video;
import com.seanfos.ytdl_rss.service.YouTubeServiceImpl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(YoutubeRssController.class)
class YoutubeRssControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private YouTubeServiceImpl youTubeService;

    @Test
    void testGetRssFeed_Success() throws Exception {
        // Arrange
        String playlistId = "somePlaylistId";
        List<Video> videos = Arrays.asList(
                new Video("Video 1", "v1 description", "www.v1.com"),
                new Video("Video 2", "v2 description", "www.v2.com"),
                new Video("Video 3", "v3 description", "wwww.v3.com")
        );

        when(youTubeService.getPlaylistVideos(anyString())).thenReturn(videos);

        // Act & Assert
        mockMvc.perform(get("/rss").param("playlistId", playlistId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Video 1 Video 2 Video 3 "));
    }

    @Test
    void testGetRssFeed_Error() throws Exception {
        // Arrange
        when(youTubeService.getPlaylistVideos(anyString())).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/rss").param("playlistId", "somePlaylistId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("<error>Unable to generate RSS feed</error>"));
    }
}
