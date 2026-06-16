package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import it.diem.unisa.musicmanager.command.CommandManager;

import java.io.IOException;

/**
 * Controller della schermata "Playlists".
 * Mostra le playlist come griglia di card e permette di crearne di nuove
 * aprendo la finestra "Crea Playlist".
 */
public class PlaylistController {

    @FXML private TextField searchBar;
    @FXML private javafx.scene.control.Button btnClearSearch;
    @FXML private FlowPane playlistsGrid;

    private PlaylistService playlistService;
    private PlayerService playerService;
    private TrackService trackService;
    private QueueService queueService;
    private boolean isListenerAttached = false;
    private CommandManager commandManager;

    @FXML
    public void initialize() throws IOException {
        if (searchBar != null) {
            searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                if (btnClearSearch != null) {
                    btnClearSearch.setVisible(!newValue.isEmpty());
                }
                try {
                    loadPlaylists();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        if (btnClearSearch != null) {
            btnClearSearch.setOnAction(e -> searchBar.clear());
        }
    }

    /**
     * Apre la finestra modale "Crea Playlist".
     * Per ora apre solo la finestra (senza service).
     *
     * @param actionEvent evento generato dal click sul pulsante "Add Playlist"
     */
    @FXML
    private void handleAdd(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/addPlaylist.fxml", "Add Playlist", Modality.APPLICATION_MODAL);
            if (loader == null) {
                return;
            }

            // gli passiamo il service qui
            AddPlaylistController controller = loader.getController();
            controller.setPlaylistService(playlistService);
            controller.setCommandManager(commandManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // metodi setere da chiamare per passare i service
    public void setPlaylistService(PlaylistService playlistService) {

        this.playlistService = playlistService;

        //bisgna iniizliazre il listener UNA SOLA VOLTAAA
        if(!isListenerAttached){
            playlistService.getPlaylists().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable obs) {
                    try {
                        loadPlaylists();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            isListenerAttached = true;
        }
        checkReadyAndLoad();
    }
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        checkReadyAndLoad();
    }


    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
        checkReadyAndLoad();
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        checkReadyAndLoad();
    }

    /**
     * Carica le playlist dal service e le mostra come card.
     * @throws IOException
     */
    public void loadPlaylists() throws IOException {
        playlistsGrid.getChildren().clear();

        String keyword = searchBar != null ? searchBar.getText() : "";
        java.util.List<Playlist> playlistsToShow = playlistService.searchPlaylists(keyword);

        for(Playlist p : playlistsToShow){
            playlistsGrid.getChildren().add(createPlaylistCard(p));
        }

        if (playlistsGrid.getChildren().isEmpty()) {
            javafx.scene.control.Label emptyLabel;
            if (keyword != null && !keyword.isBlank()) {
                emptyLabel = new javafx.scene.control.Label("No playlists found for '" + keyword + "'.");
            } else {
                emptyLabel = new javafx.scene.control.Label("Your library is empty. Add a playlist!");
            }
            emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            playlistsGrid.getChildren().add(emptyLabel);
        }
    }

    /**
     * Crea un card per una playlist.
     * @param playlist
     * @return
     * @throws IOException
     */
    private Node createPlaylistCard(Playlist playlist) throws IOException {


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/playlistCard.fxml"));


        Node card = loader.load();
        PlaylistCardController controller = loader.getController();

        controller.setTrackService(trackService);
        controller.setPlaylistService(playlistService); //i serviceeeee
        controller.setPlayerService(playerService);
        controller.setQueueService(queueService);
        controller.setPlaylist(playlist); //gli passo la playlist ddi cui creare la card
        controller.setCommandManager(commandManager);

        return card;
    }

    private void checkReadyAndLoad() {
        if (playlistService != null && trackService != null && playerService != null) {
            try {
                loadPlaylists();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }



}
