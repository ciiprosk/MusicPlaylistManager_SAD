package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
/**
 * Controller di una card playlist (playlistCard.fxml).
 * Mostra nome e numero di tracce, e offre le azioni Play / Modifica / Menu.
 * La playlist viene passata tramite {@link #setPlaylist(Playlist)} prima
 * di mostrare la card.
 */
public class PlaylistCardController {

    private PlaylistService playlistService; //lo ricevo dal qyeelo che mi chiama
    private PlayerService playerService;
    private TrackService trackService;


    @FXML private Label labelName;
    @FXML private Label labelTracks;
    @FXML private Button btnPlay;
    @FXML private Button btnModify;
    @FXML private Button btnMenu;

    // La playlist mostrata da questa card.
    private Playlist playlist;

    /**
     * Imposta la playlist della card e riempie le etichette.
     *
     * @param playlist la playlist da mostrare
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        labelName.setText(playlist.getName());
        int n = playlist.getTracksList().size();
        labelTracks.setText(n + (n == 1 ? " Track" : " Tracks"));
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }
    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;

    }


    /**
     * Click su Play: avvia la riproduzione della playlist.
     * (Da collegare al PlayerService quando disponibile.)
     */
    @FXML
    private void handlePlay() {
        if (playlist == null || playlist.getTracksList().isEmpty()) {
            return;
        }

        Track firstTrack = playlist.getTracksList().get(0);

        Track currentTrack = playerService != null
                ? playerService.currentTrackProperty().get()
                : null;

        boolean isThisPlaylistAlreadyPlaying =
                playerService != null
                        && currentTrack != null
                        && currentTrack.getId().equals(firstTrack.getId())
                        && playerService.isPlayingProperty().get();

        if (isThisPlaylistAlreadyPlaying) {
            return;
        }

        if (playlistService != null) {
            playlistService.incrementPlayCount(playlist.getId());
        }

        if (playerService != null) {
            playerService.play(firstTrack);
        }
    }

    /**
     * Click su Modifica: apre la finestra di modifica della playlist (editPlaylist).
     */
    @FXML
    private void handleModify() {
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml", "Modifica Playlist", Modality.APPLICATION_MODAL);

            // Passiamo la playlist al controller della modifica.
            EditPlaylistController controller = loader.getController();
            controller.setPlaylist(playlist);
            controller.setPlaylistService(playlistService);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Click su Menu: apre le altre azioni della playlist.
     */
    @FXML
    private void handleMenu() {
        if (playlist == null) return;

        ContextMenu menu = new ContextMenu();

        MenuItem detailItem = new MenuItem("Open Detail");
        detailItem.setOnAction(e -> openDetail());

        MenuItem modifyItem = new MenuItem("Modify Name");
        modifyItem.setOnAction(e -> openEditPlaylist());

        MenuItem deleteItem = new MenuItem("Delete playlist");
        deleteItem.setOnAction(e -> deletePlaylist());

        menu.getItems().addAll(detailItem, modifyItem, deleteItem);
        menu.show(btnMenu, Side.BOTTOM, 0, 0);
    }

    private void openEditPlaylist() {
        if (playlistService == null) return;
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml", playlist.getName(),Modality.APPLICATION_MODAL);
            EditPlaylistController ctrl = loader.getController();

            //
            //ctrl.setTrackService(trackService);
            ctrl.setPlaylist(playlist);
            ctrl.setPlaylistService(playlistService);
            //ctrl.setPlayerService(playerService); non serve

            /*
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml"));
            Parent root = loader.load();

            EditPlaylistController ctrl = loader.getController();
            ctrl.setPlaylist(playlist);
            ctrl.setPlaylistService(playlistService);

            Stage stage = new Stage();
            stage.setTitle("Modify Playlist");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDetail() {
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/detailedPlaylist.fxml", playlist.getName(),Modality.NONE);
            DetailedPlaylistController ctrl = loader.getController();

            ctrl.setPlaylistService(playlistService);
            ctrl.setTrackService(trackService);
            ctrl.setPlayerService(playerService);
            ctrl.setPlaylist(playlist);
            /*
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/detailedPlaylist.fxml"));
            Parent root = loader.load();



            Stage stage = new Stage();
            stage.setTitle(playlist.getName());
            stage.setResizable(false);
            stage.initModality(Modality.NONE);
            stage.setScene(new Scene(root, 900, 650));
            stage.show();
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deletePlaylist() {
        if (playlistService == null || playlist == null) return;

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete the Playlist \"" + playlist.getName() + "\"?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                playlistService.deletePlaylist(playlist.getId());
            }
        });
    }
    private void openAddTrackToPlaylist() {
        //DA FAREE
    }
}