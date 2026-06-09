package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.TrackService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.stage.Modality;
import javafx.fxml.FXML;
import java.io.IOException;import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import java.util.List;
/**
 * Controller responsible for managing the behavior of the Home view.
 *
 * This class is associated with the Home section of the application, as defined in the home.fxml file.
 * It interacts with the MainController to control the visibility and functionality of the Home view.
 *
 * The view includes components such as a search bar for user input, a section for displaying recent tracks,
 * and a section for displaying playlists. Behavior and functionality concerning these components
 * are defined within this controller.
 *
 * This class serves as the bridge between the user interface (UI) and the underlying logic associated with
 * the Home section, enabling dynamic updates and handling user interactions.
 */
public class HomeController {
    @FXML
    private VBox topTracksContainer;
    @FXML
    private VBox topPlaylistsContainer;
    private boolean isListenerAttached = false;
    private TrackService trackService;
    private it.diem.unisa.musicmanager.service.PlayerService playerService;
    private it.diem.unisa.musicmanager.service.PlaylistService playlistService;

    @FXML
    public void initialize() {
    }

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
        createTrackListener();
        if (this.playerService != null) {
            loadTopTracks();
        }
    }

    public void setPlayerService(it.diem.unisa.musicmanager.service.PlayerService playerService) {
        this.playerService = playerService;
        if (this.trackService != null) {
            loadTopTracks();
        }
    }

    public void setPlaylistService(it.diem.unisa.musicmanager.service.PlaylistService playlistService) {
        this.playlistService = playlistService;
        loadTopPlaylists();
    }

    private void createTrackListener() {
        if (!isListenerAttached && trackService != null) {
            trackService.getAllTracks().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    loadTopTracks();
                }
            });

            isListenerAttached = true;
        }
    }

    public void loadTopTracks() {
        if (trackService == null || playerService == null || topTracksContainer == null) {
            return;
        }

        topTracksContainer.getChildren().clear();

        List<Track> topTracks = trackService.getTop5MostPlayedTracks();

        if (topTracks.isEmpty()) {
            javafx.scene.control.Label emptyLabel = new javafx.scene.control.Label("No tracks played yet.");
            emptyLabel.setStyle("-fx-text-fill: #cccccc;");
            topTracksContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Track track : topTracks) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));
                javafx.scene.Node row = loader.load();

                RowTrackController controller = loader.getController();
                controller.setTrack(track);
                controller.setTrackService(trackService);
                controller.setPlayerService(playerService);
                controller.setOnDeleteAction(() -> {
                    trackService.deleteTrack(track.getId());
                    loadTopTracks();
                });

                topTracksContainer.getChildren().add(row);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void goToGeneratePlaylist() {
        try {
            FXMLLoader loader = WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/generateplaylist.fxml",
                    "Genera playlist",
                    Modality.APPLICATION_MODAL);

            GeneratePlaylistController ctrl = loader.getController();
            ctrl.init(trackService, playlistService);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTopPlaylists() {
        if (playlistService == null || topPlaylistsContainer == null) {
            return;
        }

        topPlaylistsContainer.getChildren().clear();

        List<Playlist> topPlaylists =
                playlistService.getTop5MostPlayedPlaylists();

        if (topPlaylists.isEmpty()) {
            Label emptyLabel = new Label("No playlists played yet.");
            emptyLabel.setStyle("-fx-text-fill: #cccccc;");
            topPlaylistsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Playlist playlist : topPlaylists) {
            Label label = new Label(
                    playlist.getName()
                            + " | Plays: "
                            + playlist.getPlayCount()
            );

            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            topPlaylistsContainer.getChildren().add(label);
        }
    }

    // è semplicissimo caricare sia le card che la row dei brani, metto il codice di esempio:
    /*
    // Dentro TracksController.java, dove avete una VBox o una griglia per ospitare le tracce
@FXML
private VBox tracksContainer;


public void loadTracks(ObservableList<Track> allTracks) {
    tracksContainer.getChildren().clear(); // Pulisce la lista visiva

    for (Track track : allTracks) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));
            Node row = loader.load();

            // Prendiamo il controller specifico di QUESTA singola riga appena creata
            RowTrackController controller = loader.getController();
            // Gli passiamo i dati del brano e lo SharedState
            controller.setTrack(track);

            // Aggiungiamo la riga grafica al contenitore della pagina
            tracksContainer.getChildren().add(row);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
     */
}
