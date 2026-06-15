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
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class QueueViewController {

    @FXML
    private ListView<QueueItem> queueListView;
    @FXML
    private javafx.scene.control.Label labelCurrentTrack;

    private QueueService queueService;
    private PlaylistService playlistService;
    private it.diem.unisa.musicmanager.service.PlayerService playerService;
    private FilteredList<QueueItem> filteredQueue;
    private QueueItem draggedQueueItem;

    @FXML
    public void initialize() {

        queueListView.setCellFactory(param -> {
            ListCell<QueueItem> cell = new ListCell<>() {

                @Override
                protected void updateItem(
                        QueueItem item,
                        boolean empty
                ) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    if (!(item.getPlayable() instanceof Track track)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    String display =
                            track.getTitle()
                                    + " - "
                                    + track.getAuthor();

                    if (item.getBelongsToPlaylist() != null
                            && playlistService != null) {

                        java.util.Optional<Playlist> opt =
                                playlistService.getPlaylistById(
                                        item.getBelongsToPlaylist()
                                );

                        if (opt.isPresent()) {
                            display +=
                                    " (from Playlist: "
                                            + opt.get().getName()
                                            + ")";
                        }
                    }

                    setText(display);
                }
            };

            cell.setOnDragDetected(event -> {
                if (cell.isEmpty() || cell.getItem() == null) {
                    return;
                }

                draggedQueueItem = cell.getItem();

                Dragboard dragboard =
                        cell.startDragAndDrop(TransferMode.MOVE);

                ClipboardContent content =
                        new ClipboardContent();

                content.putString(
                        draggedQueueItem
                                .getPlayable()
                                .getId()
                                .toString()
                );

                dragboard.setContent(content);
                event.consume();
            });

            cell.setOnDragOver(event -> {
                if (draggedQueueItem != null
                        && !cell.isEmpty()
                        && cell.getItem() != null
                        && cell.getItem() != draggedQueueItem) {

                    event.acceptTransferModes(
                            TransferMode.MOVE
                    );
                }

                event.consume();
            });

            cell.setOnDragDropped(event -> {
                boolean completed = false;

                QueueItem targetItem =
                        cell.getItem();

                if (draggedQueueItem != null
                        && targetItem != null
                        && queueService != null
                        && draggedQueueItem != targetItem) {

                    queueService.moveQueueItem(
                            draggedQueueItem,
                            targetItem
                    );

                    refreshQueueFilter();
                    completed = true;
                }

                event.setDropCompleted(completed);
                event.consume();
            });

            cell.setOnDragDone(event -> {
                draggedQueueItem = null;
                event.consume();
            });

            return cell;
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
            this.filteredQueue = new FilteredList<>(
                    this.queueService.getQueueList(),
                    item -> true
            );

            this.queueListView.setItems(filteredQueue);

            this.queueService.getQueueList().addListener(
                    (ListChangeListener<QueueItem>) change ->
                            refreshQueueFilter()
            );

            /*
             * Fondamentale quando la traccia successiva è uguale a quella corrente:
             * currentTrackProperty potrebbe non cambiare, mentre currentItem cambia.
             */
            this.queueService.currentItemProperty().addListener(
                    (observable, oldItem, newItem) ->
                            refreshQueueFilter()
            );

            refreshQueueFilter();
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
                refreshQueueFilter();
            });
        }
    }

    private void updateCurrentTrackLabel(Track track) {
        if (track == null) {
            labelCurrentTrack.setText("No Track Playing");
        } else {
            labelCurrentTrack.setText(track.getTitle() + " - " + track.getAuthor());
        }
    }

    private void refreshQueueFilter() {
        if (filteredQueue == null) return;
        javafx.collections.ObservableList<QueueItem> source = queueService.getQueueList();
        filteredQueue.setPredicate(item -> {
            QueueItem current = queueService.getCurrentItem();
            int currentIndex = (current == null) ? -1 : source.indexOf(current);
            return source.indexOf(item) > currentIndex;   // solo le prossime
        });
        javafx.application.Platform.runLater(() -> queueListView.refresh());
    }
}