package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class TracksController {


    @FXML
    private ListView<Track> tracksList;
    @FXML
    private TextField searchBar;

    private TrackService trackService;
    private PlayerService playerService;
    private boolean isListenerAttached = false;

    @FXML private VBox trackList;


    public void setTrackService(TrackService trackService) {

        this.trackService = trackService;
        createTrackListener();
    }
/*
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

 */


    public void handleAdd(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/addSong.fxml", "Add Track",Modality.WINDOW_MODAL);
        AddSongController controller = loader.getController();
        controller.setTrackService(trackService);
    }


    public void loadTracks() throws IOException {
        trackList.getChildren().clear();

        for(Track track : trackService.getAllTracks()){


            trackList.getChildren().add(createTrackRow(track));
        }

    }

    private Node createTrackRow(Track track) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));


        Node card = loader.load();
        RowTrackController controller = loader.getController();
        controller.setTrack(track); //gli passo la track ddi cui creare la row
        controller.setTrackService(trackService); //i serviceeeee
        controller.setPlayerService(playerService);
        return card;
    }


    private void createTrackListener() {
        if (!isListenerAttached) {
            trackService.getAllTracks().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable obs) {
                    try {
                        loadTracks();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            isListenerAttached = true;
        }
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }
}