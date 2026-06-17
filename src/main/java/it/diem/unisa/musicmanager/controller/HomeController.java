package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.QueueService;
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
import javafx.scene.layout.FlowPane;
import it.diem.unisa.musicmanager.command.CommandManager;
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
    private FlowPane topPlaylistsContainer;
    private boolean isListenerAttached = false;
    private TrackService trackService;
    private  PlayerService playerService;
    private  PlaylistService playlistService;
    private QueueService queueService;
    private CommandManager commandManager;

    @FXML
    public void initialize() {
    }

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
        createTrackListener();

        if (this.playerService != null) {
            loadTopTracks();
        }

        if (this.playlistService != null && this.playerService != null) {
            loadTopPlaylists();
        }
    }

    public void setPlayerService(it.diem.unisa.musicmanager.service.PlayerService playerService) {
        this.playerService = playerService;

        if (this.trackService != null) {
            loadTopTracks();
        }

        if (this.playlistService != null && this.trackService != null) {
            loadTopPlaylists();
        }
    }

    public void setPlaylistService(it.diem.unisa.musicmanager.service.PlaylistService playlistService) {
        this.playlistService = playlistService;
        createPlaylistListener();
        loadTopPlaylists();
    }

    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        loadTopTracks();
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

    private boolean isPlaylistListenerAttached = false;

    private void createPlaylistListener() {
        if (!isPlaylistListenerAttached && playlistService != null) {
            playlistService.getPlaylists().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    javafx.application.Platform.runLater(() -> loadTopPlaylists());
                }
            });

            isPlaylistListenerAttached = true;
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
                controller.setQueueService(queueService);
                controller.setPlaylistService(playlistService);
                controller.setCommandManager(commandManager);

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
        if (playlistService == null
                || trackService == null
                || playerService == null
                || commandManager == null
                || topPlaylistsContainer == null) {
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
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "/it/diem/unisa/musicmanager/components/playlistCard.fxml"
                        )
                );

                javafx.scene.Node card = loader.load();

                PlaylistCardController controller = loader.getController();
                controller.setPlaylistService(playlistService);
                controller.setTrackService(trackService);
                controller.setPlayerService(playerService);
                controller.setQueueService(queueService);
                controller.setPlaylist(playlist);
                controller.setCommandManager(commandManager);


                topPlaylistsContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
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
