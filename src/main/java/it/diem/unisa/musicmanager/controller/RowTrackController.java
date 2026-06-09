package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
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

/**
 * Controller per la singola riga di un brano.
 * Totalmente disaccoppiato dalla logica di riproduzione.
 */
public class RowTrackController {

    private Track track;
    private TrackService trackService;
    private PlayerService playerService;

    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblPlayCount;
    @FXML private Label lblDuration;

    @FXML private Button buttonMenu;
    @FXML private Button btnModify;
    @FXML private Button btnPlay;

    @FXML private CheckBox checkkSelect;
    @FXML private HBox rootContainer;

    private boolean isListenerAttached = false;
    private Runnable onDeleteAction;

    public void setSelectionMode(boolean isSelectionMode, boolean isAlreadyInPlaylist) {
        checkkSelect.setVisible(isSelectionMode);
        checkkSelect.setManaged(isSelectionMode);
        checkkSelect.setSelected(isAlreadyInPlaylist);

        btnPlay.setVisible(!isSelectionMode);
        btnPlay.setManaged(!isSelectionMode);
        btnModify.setVisible(!isSelectionMode);
        btnModify.setManaged(!isSelectionMode);
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
    }

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;

        if (!isListenerAttached) {
            playerService.currentTrackProperty().addListener((o, ov, nv) -> {
                updateCurrentTrackStyle();
                updateButtonState();
            });

            playerService.isPlayingProperty().addListener((o, ov, nv) -> {
                updateCurrentTrackStyle();
                updateButtonState();
            });

            isListenerAttached = true;
        }

        updateCurrentTrackStyle();
        updateButtonState();
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

        menu.getItems().addAll(detailItem, modifyItem, deleteItem);
        menu.show(buttonMenu, Side.BOTTOM, 0, 0);
    }

    private void openDetail() {
        try {
            WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/detailSong.fxml", track.getTitle(), Modality.WINDOW_MODAL);
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

}