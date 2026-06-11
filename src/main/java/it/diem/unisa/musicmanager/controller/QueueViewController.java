package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.QueueService;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class QueueViewController {

    @FXML
    private ListView<QueueItem> queueListView;
    @FXML
    private javafx.scene.control.Label labelCurrentTrack;

    private QueueService queueService;
    private PlaylistService playlistService;
    private it.diem.unisa.musicmanager.service.PlayerService playerService;

    @FXML
    public void initialize() {
        queueListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(QueueItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Track track = (Track) item.getPlayable();
                    String display = track.getTitle() + " - " + track.getAuthor();
                    if (item.getBelongsToPlaylist() != null && playlistService != null) {
                        java.util.Optional<Playlist> opt = playlistService.getPlaylistById(item.getBelongsToPlaylist());
                        if (opt.isPresent()) {
                            display += " (from Playlist: " + opt.get().getName() + ")";
                        }
                    }
                    setText(display);
                    // Applicare stili per renderlo visibile su sfondo scuro
                    setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-background-color: #2b2b2b; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");
                }
            }
        });
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
        if (queueListView != null) {
            queueListView.refresh();
        }
    }

    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
        if (this.queueService != null && this.queueListView != null) {
            this.queueListView.setItems(this.queueService.getQueueList());
        }
    }

    public void setPlayerService(it.diem.unisa.musicmanager.service.PlayerService playerService) {
        this.playerService = playerService;
        if (this.playerService != null && labelCurrentTrack != null) {
            // Aggiorna subito
            updateCurrentTrackLabel(this.playerService.currentTrackProperty().get());
            // Aggiunge il listener per i futuri cambiamenti
            this.playerService.currentTrackProperty().addListener((observable, oldValue, newValue) -> {
                updateCurrentTrackLabel(newValue);
            });
        }
    }

    private void updateCurrentTrackLabel(Track track) {
        if (track == null || !playerService.isPlayingProperty().get()) {
            labelCurrentTrack.setText("No Track Playing");
        } else {
            labelCurrentTrack.setText(track.getTitle() + " - " + track.getAuthor());
        }
    }
}
