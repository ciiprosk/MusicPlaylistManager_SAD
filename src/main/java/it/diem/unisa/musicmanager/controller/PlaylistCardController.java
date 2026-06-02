package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * La classe controller PlaylistCardController gestisce l'interazione con l'interfaccia utente per la visualizzazione di una singola playlist.
 * È un componente UI riusabile.
 * 1. dvee tenere lo stato della card playlist
 * 2. deve aggiornare i testi deklla card quando vinee settata la playlist
 * 3. gestire i due bottoni, handlePLay per la riroduzione e un handleMenu per aprire un menu
 */
public class PlaylistCardController {
    private Playlist playlist;
    private PlaylistService playlistService;

    private Runnable onOpenDetails;

    @FXML private Label labelName;
    @FXML private Label labelTracks;

    public void setPlaylist(Playlist playlist){
        this.playlist = playlist;
        refresh();
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public void handlePlay(ActionEvent actionEvent) {
    //da fare successivamente
    }

    public void handleMenu(ActionEvent actionEvent) {
    }

    private void refresh() {
        if (playlist == null) return;
        if (labelName != null) labelName.setText(playlist.getName());
        int n = playlist.getTracks() == null ? 0 : playlist.numberOfTrakcs();
        if (labelTracks != null) labelTracks.setText(n + " tracks");
    }

    private void renamePlaylist(){
        if(playlist == null) return;

    }
    private void deletePlaylist(){
        if(playlist == null) return;

    }
    
}
