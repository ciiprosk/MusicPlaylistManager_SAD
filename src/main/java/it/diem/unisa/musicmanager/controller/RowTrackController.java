package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.*;
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
    @FXML private Label lblDuration;

    @FXML private Button buttonMenu;
    @FXML private Button btnModify;
    @FXML private Button btnPlay;

    @FXML private CheckBox checkkSelect;

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
    }

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;

        if (!isListenerAttached) {
            // Reagisce ai cambiamenti del servizio per aggiornare l'icona
            playerService.currentTrackProperty().addListener((o, ov, nv) -> updateButtonState());
            playerService.isPlayingProperty().addListener((o, ov, nv) -> updateButtonState());
            isListenerAttached = true;
        }
        updateButtonState();
    }

    /**
     * Aggiorna graficamente l'icona del pulsante basandosi esclusivamente sullo stato del Service.
     */
    private void updateButtonState() {
        boolean isCurrentAndPlaying = track != null
                && playerService != null
                && track.equals(playerService.currentTrackProperty().get())
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
        deleteTrackAction();
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
        deleteItem.setOnAction(e -> deleteTrackAction());

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
        this.onDeleteAction = onDeleteAction;
    }
}