package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.QueueService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.AlertUtil;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;

import java.io.IOException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

/**
 * Controller per la singola riga di un brano.
 * Totalmente disaccoppiato dalla logica di riproduzione.
 */
public class RowTrackController {

    private Track track;
    private TrackService trackService;
    private PlayerService playerService;
    private QueueService queueService;

    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblPlayCount;
    @FXML private Label lblDuration;

    @FXML private Button buttonMenu;
    @FXML private Button btnModify;
    @FXML private Button btnPlay;

    @FXML private CheckBox checkkSelect;
    @FXML private HBox rootContainer;

    @FXML
    private HBox tagsContainer;

    private boolean isListenerAttached = false;
    private Runnable onDeleteAction;

    public void setSelectionMode(boolean isSelectionMode, boolean isAlreadyInPlaylist) {
        checkkSelect.setVisible(isSelectionMode);
        checkkSelect.setManaged(isSelectionMode);
        checkkSelect.setSelected(isAlreadyInPlaylist);

        if (btnPlay != null) {
            btnPlay.setVisible(!isSelectionMode);
            btnPlay.setManaged(!isSelectionMode);
        }
        if (btnModify != null) {
            btnModify.setVisible(!isSelectionMode);
            btnModify.setManaged(!isSelectionMode);
        }
        if (buttonMenu != null) {
            buttonMenu.setVisible(!isSelectionMode);
            buttonMenu.setManaged(!isSelectionMode);
        }
    }

    public boolean isSelected() {
        return checkkSelect.isSelected();
    }

    public void setTrack(Track track) {
        this.track = track;

        lblTitle.setText(track.getTitle());
        lblAuthor.setText(track.getAuthor());

        int minutes = (int) (track.getSongLength() / 60);
        int seconds = (int) (track.getSongLength() % 60);
        lblDuration.setText(String.format("%02d:%02d", minutes, seconds));

        if (lblPlayCount != null) {
            lblPlayCount.setText(track.getPlayCount() + " Plays");
        }

        renderTags();
    }



    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    private final ChangeListener<Track> currentTrackListener = (o, ov, nv) -> {
        updateCurrentTrackStyle();
        updateButtonState();
    };

    private final ChangeListener<Boolean> isPlayingListener = (o, ov, nv) -> {
        updateCurrentTrackStyle();
        updateButtonState();
    };

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;

        if (!isListenerAttached) {
            playerService.currentTrackProperty().addListener(new WeakChangeListener<>(currentTrackListener));
            playerService.isPlayingProperty().addListener(new WeakChangeListener<>(isPlayingListener));

            isListenerAttached = true;
        }

        updateCurrentTrackStyle();
        updateButtonState();
    }

    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Aggiorna graficamente l'icona del pulsante basandosi esclusivamente sullo stato del Service.
     */
    private void updateButtonState() {
        Track currentTrack = playerService != null
                ? playerService.currentTrackProperty().get()
                : null;

        boolean isCurrentAndPlaying = track != null
                && currentTrack != null
                && track.getId().equals(currentTrack.getId())
                && playerService.isPlayingProperty().get();

        btnPlay.setText(isCurrentAndPlaying ? "⏸" : "▶");
    }

    /**
     * L'azione di riproduzione ora è una semplice delega diretta (sottilissima!)
     */
    @FXML
    public void handlePlay(ActionEvent actionEvent) {
        if (playerService != null && track != null) {
            playerService.togglePlay(track);//lo fa il seerivce
        }
    }

    @FXML
    public void handleModify(ActionEvent actionEvent) {
        openEditTrack();
    }

    @FXML
    public void handleDelete(ActionEvent actionEvent) {
        if (trackService != null && track != null) {

            boolean isConfirmed = AlertUtil.showConfirmation("Confirm Delete", "Are you sure you want to delete this track?");

            if (isConfirmed && onDeleteAction != null) {
                onDeleteAction.run();
            }
        }

    }

    @FXML
    public void handleMenu(ActionEvent actionEvent) {
        if (track == null) return;

        ContextMenu menu = new ContextMenu();
        MenuItem detailItem = new MenuItem("Open Detail");
        detailItem.setOnAction(e -> openDetail());

        MenuItem modifyItem = new MenuItem("Modify Track");
        modifyItem.setOnAction(e -> openEditTrack());

        MenuItem deleteItem = new MenuItem("Delete Track");
        deleteItem.setOnAction(e -> handleDelete(null));

        MenuItem addQueueItem = new MenuItem("Add to Queue");
        addQueueItem.setOnAction(e -> {
            onAddQueue(e);
            });

        menu.getItems().addAll(detailItem, modifyItem, deleteItem, addQueueItem);
        menu.show(buttonMenu, Side.BOTTOM, 0, 0);
    }

    private void openDetail() {
        try {
            FXMLLoader loader = WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/detailSong.fxml",
                    track.getTitle(),
                    Modality.WINDOW_MODAL
            );

            DetailSongController controller = loader.getController();

            controller.setTrackService(trackService);
            controller.setTrack(track);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEditTrack() {
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/editSong.fxml", "Modify Track", Modality.WINDOW_MODAL);
            EditSongController controller = loader.getController();
            controller.setTrackService(trackService);
            controller.setTrack(track);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteTrackAction() {
        if (trackService != null && track != null) {
            trackService.deleteTrack(track.getId());
        }
    }

    public Track getTrack() {
        return track;
    }

    public void setOnDeleteAction(Runnable onDeleteAction) {
        //AlertUtil.showConfirmation("Confirm Delete", "Are you sure you want to delete this track?");
        this.onDeleteAction = onDeleteAction;
    }

    private void updateCurrentTrackStyle() {
        rootContainer.getStyleClass().remove("brano-row-playing");

        Track currentTrack = playerService != null
                ? playerService.currentTrackProperty().get()
                : null;

        boolean isCurrentAndPlaying = track != null
                && currentTrack != null
                && track.getId().equals(currentTrack.getId())
                && playerService.isPlayingProperty().get();

        if (isCurrentAndPlaying) {
            rootContainer.getStyleClass().add("brano-row-playing");
        }
    }


    private void renderTags() {
        if (tagsContainer == null) return;

        tagsContainer.getChildren().clear();

        for (Tag tag : track.getTags()) {
            tagsContainer.getChildren().add(createTag(tag));
        }
    }

    //Creazione dinamica delle label associate ai tag
    private Label createTag(Tag tag) {
        Label label = new Label();
        label.getStyleClass().add("tag");

        switch (tag) {
            case EXPLICIT -> {
                label.setText("E");
                label.getStyleClass().add("tag-explicit");
            }
            case FAVOURITE -> {
                label.setText("♥");
                label.getStyleClass().add("tag-favourite");
            }
            case NEWRELEASE -> {
                label.setText("NEW");
                label.getStyleClass().add("tag-newrelease");
            }
        }

        return label;
    }
    private void onAddQueue(ActionEvent actionEvent) {
        if(queueService !=null && track != null) {
            //vedio se la coda d ascolto è vuota
            boolean isEmpty = queueService.getQueueList().isEmpty();

            //vedo se c'è un bran oin rispodzione
            boolean isPlayingTrack = playerService.currentTrackProperty().get() != null;

            //aggingo alla coda

            queueService.addToQueue(track);

            if(isEmpty && !isPlayingTrack){// se è vuota e nessuna raccia sta suonando allora osso far partire wuesta
                playerService.next();

            }else{
                AlertUtil.showInfo("Queue Updated", "Track '" + track.getTitle() + "' added to queue!");
            }
        }
    }
}