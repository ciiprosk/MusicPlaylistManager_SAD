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
                // --- STRATEGIA DI SHIFT (SLITTAMENTO) ---
                // Invece di usare l'item che ci passa JavaFX, prendiamo l'indice della cella corrente
                int index = getIndex();

                // Se c'è una traccia in riproduzione, facciamo "slittare" la lista in avanti di 1.
                // La cella 0 mostrerà l'elemento 1, la cella 1 l'elemento 2, e così via.
                Track currentPlayingTrack = (playerService != null) ? playerService.currentTrackProperty().get() : null;

                if (currentPlayingTrack != null && queueService != null && !queueService.getQueueList().isEmpty()) {
                    // Aggiorniamo l'item prendendo quello successivo nella coda reale
                    if (index + 1 < queueService.getQueueList().size()) {
                        item = queueService.getQueueList().get(index + 1);
                    } else {
                        item = null; // Siamo arrivati alla fine della coda effettiva
                    }
                }

                super.updateItem(item, empty || item == null);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0;");
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
                    setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-background-color: #2b2b2b; -fx-border-color: #333333; -fx-border-width: 0 0 1 0; -fx-padding: 8px;");
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
            updateCurrentTrackLabel(this.playerService.currentTrackProperty().get());

            if (queueListView != null) {
                queueListView.refresh();
            }

            this.playerService.currentTrackProperty().addListener((observable, oldValue, newValue) -> {
                updateCurrentTrackLabel(newValue);

                if (queueListView != null) {
                    queueListView.refresh();
                }
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