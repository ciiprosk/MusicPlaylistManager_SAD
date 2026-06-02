package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.PlaylistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class EditPlaylistController {

    private Playlist playlist;
    private PlaylistService playlistService;

    @FXML private TextField fieldName;
    @FXML private Label lblError;

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }
    public void setPlaylistService(PlaylistService playlistService){
        this.playlistService = playlistService;

    }

    public void onCancel(ActionEvent actionEvent) {
        close(actionEvent);
    }

    public void onUpdate(ActionEvent actionEvent) {
        lblError.setText("");

        if(playlistService == null){
            lblError.setText("Playlist Service not available.");
        }

        String name = fieldName.getText() == null ? "" : fieldName.getText().trim();
        if (name.isEmpty()) {
            lblError.setText("You must enter a name.");
        }

        Optional<String> optional = playlistService.renamePlaylist(playlist.getId(), name); //chiamo il service per crare la playlist
        if(optional.isPresent()){
            lblError.setText(optional.get());
        }else{
            close(actionEvent);
        }

    }

    private void close(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }
}
