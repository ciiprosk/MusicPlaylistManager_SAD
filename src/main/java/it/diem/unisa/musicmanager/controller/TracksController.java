package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.command.AddTrackCommand;
import it.diem.unisa.musicmanager.command.CommandManager;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.QueueService;
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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.Optional;

public class TracksController {


    @FXML
    private ListView<Track> tracksList;
    @FXML
    private TextField searchBar;
    @FXML
    private Button btnClearSearch;

    private TrackService trackService;
    private PlayerService playerService;
    private PlaylistService playlistService;
    private QueueService queueService;
    private boolean isListenerAttached = false;
    private CommandManager commandManager;

    @FXML private VBox trackList;

    public void setTrackService(TrackService trackService) {

        this.trackService = trackService;
        createTrackListener();
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }


    public void handleAdd(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/addSong.fxml", "Add Track",Modality.WINDOW_MODAL);
        if (loader == null) {
            return;
        }
        AddSongController controller = loader.getController();
        controller.setTrackService(trackService);
        controller.setCommandManager(commandManager);
    }



    @FXML
    public void initialize() {
        if (searchBar != null) {
            searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                if (btnClearSearch != null) {
                    btnClearSearch.setVisible(!newValue.isEmpty());
                }
                try {
                    loadTracks();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        if (btnClearSearch != null) {
            btnClearSearch.setOnAction(e -> searchBar.clear());
        }
    }

    public void loadTracks() throws IOException {
        trackList.getChildren().clear();

        String keyword = searchBar != null ? searchBar.getText() : "";
        java.util.List<Track> tracksToShow = trackService.searchTracks(keyword);

        for (Track track : tracksToShow) {
            trackList.getChildren().add(createTrackRow(track));
        }

        // Archivio vuoto: invitiamo l'utente ad aggiungere un brano o avvisiamo che la ricerca non ha prodotto risultati.
        if (trackList.getChildren().isEmpty()) {
            Label emptyLabel;
            if (keyword != null && !keyword.isBlank()) {
                emptyLabel = new Label("No tracks found for '" + keyword + "'.");
            } else {
                emptyLabel = new Label("Your library is empty. Add a track!");
            }
            emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");  // bianco e leggermente piu' grande
            trackList.getChildren().add(emptyLabel);
        }
    }
    private Node createTrackRow(Track track) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));


        Node card = loader.load();
        RowTrackController controller = loader.getController();
        controller.setTrack(track); //gli passo la track ddi cui creare la row
        controller.setTrackService(trackService); //i serviceeeee
        controller.setPlayerService(playerService);
        controller.setQueueService(queueService);
        controller.setCommandManager(commandManager);
        controller.setPlaylistService(playlistService);
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

    private void openTrackDetails(Track track) {
        if (track == null) {
            return;
        }

        try {
            FXMLLoader loader = WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/detailSong.fxml",
                    "Track Details",
                    Modality.WINDOW_MODAL
            );
            if (loader == null) {
                return;
            }

            DetailSongController controller = loader.getController();
            controller.setTrackService(trackService);
            controller.setTrack(track);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }
}