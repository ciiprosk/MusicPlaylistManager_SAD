package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.stage.Modality;

import java.io.IOException;

public class RowTrackController {

    // ho sbigno della singola tracca e del service per gestire le cosed ipersistenza
    private Track track;
    private TrackService trackService;
    private PlayerService playerService;


    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblDuration;

    public void setTrack(Track track) {
        this.track = track;
        lblTitle.setText(track.getTitle());
        lblAuthor.setText(track.getAuthor());

        int minutes = (int) (track.getSongLength() / 60);
        int seconds = (int) (track.getSongLength() % 60);

        lblDuration.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @FXML
    public void handleModify(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/resources//musicmanager/pages/editSong.fxml", "", Modality.WINDOW_MODAL);
        AddSongController controller = loader.getController();
        //creo i set dei servicenei controller
    }
    @FXML
    public void handlePlay(ActionEvent actionEvent) {
    }

    @FXML
    public void handleDelete(ActionEvent actionEvent) {
        if (trackService != null && track != null) {
            trackService.deleteTrack(track.getId());
        }
    }

    public void handleMenu(ActionEvent actionEvent) {

    }
}
