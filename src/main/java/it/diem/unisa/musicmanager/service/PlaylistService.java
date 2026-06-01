package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.collections.ObservableList;

public class PlaylistService {
    // serve a gestire le operazioni crud sulle playlist
    private final DAO<Playlist> playlistDAO;

    private final SharedState sharedState;

    public PlaylistService(DAO<Playlist> playlistDAO, SharedState sharedState) {
        this.playlistDAO = playlistDAO;
        this.sharedState = sharedState;
    }

    public void getPlaylists() {}
    public void createPlaylist() {}
    public void deletePlaylist() {}
    public void renamePlaylist() {}
    public void addTrackToPlaylist() {}
    public void removeTrackFromPlaylist() {}

    
}
