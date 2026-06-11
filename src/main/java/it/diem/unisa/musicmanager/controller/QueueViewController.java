package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.QueueService;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.collections.transformation.FilteredList;

public class QueueViewController {

    @FXML
    private ListView<QueueItem> queueListView;
    @FXML
    private javafx.scene.control.Label labelCurrentTrack;

    private QueueService queueService;
    private PlaylistService playlistService;
    private it.diem.unisa.musicmanager.service.PlayerService playerService;
    private FilteredList<QueueItem> filteredQueue;

    @FXML
    public void initialize() {
        queueListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(QueueItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    // sfondo uguale al contenitore, nessun bordo -> la cella vuota sparisce alla vista
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
            // Nasconde sempre la testa (= brano corrente, già mostrato in "Now Playing")
            filteredQueue = new FilteredList<>(this.queueService.getQueueList(), this::shouldDisplay);
            this.queueListView.setItems(filteredQueue);
        }
    }

    public void setPlayerService(it.diem.unisa.musicmanager.service.PlayerService playerService) {
        this.playerService = playerService;
        if (this.playerService != null && labelCurrentTrack != null) {
            updateCurrentTrackLabel(this.playerService.currentTrackProperty().get());

            this.playerService.currentTrackProperty().addListener((observable, oldValue, newValue) -> {
                updateCurrentTrackLabel(newValue);
                Platform.runLater(this::refreshFilter);
            });
        }
        initFilteredList(); // <--- Inizializza anche qui se il queueService era già arrivato
    }

    /**
     * Inizializza centralmente la lista filtrata per evitare liste vuote dovute all'ordine dei setter
     */
    private void initFilteredList() {
        if (this.queueService != null && this.queueListView != null && this.filteredQueue == null) {
            filteredQueue = new FilteredList<>(this.queueService.getQueueList(), this::shouldDisplay);
            this.queueListView.setItems(filteredQueue);
        }
    }

    private boolean shouldDisplay(QueueItem item) {
        if (item == null) return false;
        if (playerService == null) return true;

        Track currentTrack = playerService.currentTrackProperty().get();
        if (currentTrack == null) return true;

        Track itemTrack = (Track) item.getPlayable();
        // NASCONDE dalla lista solo se è lo stesso brano che sta suonando come NOW PLAYING
        return itemTrack == null || !itemTrack.getId().equals(currentTrack.getId());
    }

    private void refreshFilter() {
        if (filteredQueue != null) {
            filteredQueue.setPredicate(null);
            filteredQueue.setPredicate(this::shouldDisplay);
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
