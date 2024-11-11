import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './App.css'; // Ensure the CSS file is imported

function App() {
    const [playlistId, setPlaylistId] = useState('');
    const [channelName, setChannelName] = useState('');
    const [playlistVideos, setPlaylistVideos] = useState([]);
    const [channelVideos, setChannelVideos] = useState([]);
    const [data, setData] = useState({ channelNames: [], playlistTitles: [] });

    useEffect(() => {
        fetchChannelNamesAndPlaylists();
    }, []);

    const fetchChannelNamesAndPlaylists = () => {
        axios.get('/channelNamesAndPlaylists')
            .then(response => {
                setData(response.data);
            })
            .catch(error => {
                console.error("There was an error fetching the data!", error);
            });
    };

    const handleGetPlaylistVideos = async () => {
        try {
            const response = await axios.get(`/playlist?playlistId=${playlistId}`);
            setPlaylistVideos(response.data);
        } catch (error) {
            console.error('Error fetching playlist videos:', error);
        }
    };

    const handleGetChannelVideos = async () => {
        try {
            const response = await axios.get(`/channel?channelName=${channelName}`);
            setChannelVideos(response.data);
        } catch (error) {
            console.error('Error fetching channel videos:', error);
        }
    };

    const updateVideos = () => {
        axios.get('/updateVideos')
            .then(response => {
                console.log(response.data);
                fetchChannelNamesAndPlaylists(); // Refresh the data after update
            })
            .catch(error => {
                console.error("There was an error updating the videos!", error);
            });
    };

    return (
        <div className="App">
            <h1>YouTube Video Downloader</h1>
            <div>
                <h2>Get Playlist Videos</h2>
                <input
                    type="text"
                    value={playlistId}
                    onChange={(e) => setPlaylistId(e.target.value)}
                    placeholder="Enter Playlist ID"
                />
                <button onClick={handleGetPlaylistVideos}>Get Playlist Videos</button>
                <div>
                    <h3>Playlist Videos:</h3>
                    <ul>
                        {playlistVideos.map((video, index) => (
                            <li key={index}>
                                <strong>{video.title}</strong>: {video.description}
                            </li>
                        ))}
                    </ul>
                </div>
            </div>
            <div>
                <h2>Get Channel Videos</h2>
                <input
                    type="text"
                    value={channelName}
                    onChange={(e) => setChannelName(e.target.value)}
                    placeholder="Enter Channel Name"
                />
                <button onClick={handleGetChannelVideos}>Get Channel Videos</button>
                <div>
                    <h3>Channel Videos:</h3>
                    <ul>
                        {channelVideos.map((video, index) => (
                            <li key={index}>
                                <strong>{video.title}</strong>: {video.description}
                            </li>
                        ))}
                    </ul>
                </div>
            </div>
            <div>
                <h1>Channel Names and Playlist Titles</h1>
                <button onClick={updateVideos}>Update Videos</button>
                <h2>Channel Names</h2>
                <ul>
                    {data.channelNames.map((name, index) => (
                        <li key={index}>{name}</li>
                    ))}
                </ul>
                <h2>Playlist Titles</h2>
                <ul>
                    {data.playlistTitles.map((title, index) => (
                        <li key={index}>{title}</li>
                    ))}
                </ul>
            </div>
        </div>
    );
}

export default App;
