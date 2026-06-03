package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;


/**
 * Controller della player bar.
 * Tiene un riferimento al PlayerService e delega a esso le azioni dei pulsanti.
 * Resta volutamente "sottile": tutta la logica di riproduzione vive nel service.
 */
public class PlayerController {

    // Service di riproduzione, iniettato da chi carica questo controller.
    private PlayerService playerService;

    private MediaPlayer mediaPlayer;
    private Track loadedTrack;

    @FXML private Button buttonPlay;
    @FXML private Button buttonNext;
    @FXML private Button buttonSkipPlaylist;
    @FXML private Button buttonMode;
    @FXML private Button buttonQueue;

    @FXML private Label labelTrack;
    @FXML private Label labelAuthor;
    @FXML private Label labelTime;
    @FXML private Label labelDuration;
    @FXML private Slider sliderProgress;

    private boolean userIsSeeking = false;
    private boolean isListenerAttached = false;
    /**
     * @param playerService il service di riproduzione da usare
     */
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        // listener una sola volta
        if(!isListenerAttached) {
            bind();
            isListenerAttached = true;
        }else{
            updateTrackInfo(playerService.currentTrackProperty().get());
            updatePlayButton(playerService.isPlayingProperty().get());
        }
    }

    /**
     * Pulsante play/pausa: alterna riproduzione e pausa tramite il service.
     */
    @FXML
    public void handlePlayPause(ActionEvent actionEvent) {
        if (playerService == null) return;
        if (playerService.isPlayingProperty().get()) {
            playerService.pause();
        } else {
            Track current = playerService.currentTrackProperty().get();
            if (current != null) playerService.play(current);
        }

    }
    @FXML
    public void handleNext(ActionEvent actionEvent) {
        //dopo
    }

    public void handleSkipPlaylist(ActionEvent actionEvent) {
        //dopo
    }

    public void handleChangeMode(ActionEvent actionEvent) {
            //dopo
    }

    public void handleQueue(ActionEvent actionEvent) {
        //dopo
    }

    private void bind() {
        // 1. Ascolta il cambio del brano dal Service
        playerService.currentTrackProperty().addListener((o, ov, track) -> updateTrackInfo(track));
        updateTrackInfo(playerService.currentTrackProperty().get());

        // 2. Ascolta lo stato di play/pausa dal Service
        playerService.isPlayingProperty().addListener((o, ov, playing) -> updatePlayButton(playing));
        updatePlayButton(playerService.isPlayingProperty().get());

        // 3. DA SERVICE A SLIDER: Avanzamento automatico del pallino (solo se l'utente non sta trascinando)
        playerService.progressProperty().addListener((o, ov, p) -> {
            if (!userIsSeeking) {
                sliderProgress.setValue(p.doubleValue());
                updateElapsed(p.doubleValue());
            }
        });

        // 4. DA SLIDER A UI (Mentre trascini): I minuti trascorsi seguono il movimento del mouse
        sliderProgress.valueProperty().addListener((o, ov, nv) -> {
            if (userIsSeeking) {
                updateElapsed(nv.doubleValue());
            }
        });

        // 5. Configurazione degli eventi del mouse sullo slider per saltare nel tempo (seek)
        sliderProgress.setMin(0.0);
        sliderProgress.setMax(1.0);
        sliderProgress.setOnMousePressed(e -> userIsSeeking = true);
        sliderProgress.setOnMouseReleased(e -> {
            playerService.seek(sliderProgress.getValue());
            userIsSeeking = false;
        });
    }

    private void updateTrackInfo(Track track) {
        if (track == null) {
            labelTrack.setText("Track");
            labelAuthor.setText("Author");
            labelDuration.setText("00:00");
            labelTime.setText("00:00");
            return;
        }
        labelTrack.setText(track.getTitle());
        labelAuthor.setText(track.getAuthor());
        labelDuration.setText(format((long) track.getSongLength()));
    }
    private void updatePlayButton(boolean playing) {
        buttonPlay.setText(playing ? "⏸" : "▶");
    }

    private void updateElapsed(double progress) {
        Track track = playerService.currentTrackProperty().get();
        long elapsed = (track == null) ? 0 : Math.round(track.getSongLength() * progress);
        labelTime.setText(format(elapsed));
    }

    private String format(long totalSeconds) {
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
