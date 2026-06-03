package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class PlayerBarController {

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

    private PlayerService playerService;
    private SharedState sharedState;

    // True mentre l'utente trascina lo slider: blocca gli aggiornamenti
    // automatici dal player, cosi' il pallino non "scappa" sotto le dita.
    private boolean userIsSeeking = false;

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        this.sharedState = playerService.getSharedState();
        bind();
    }

    /** Collega le Property dello SharedState alla UI della player bar. */
    private void bind() {
        // Brano corrente -> titolo, autore, durata totale
        sharedState.getCurrentTrack().addListener((o, ov, track) -> updateTrackInfo(track));
        updateTrackInfo(sharedState.getCurrentTrack().get());

        // Play/pausa -> icona del bottone
        sharedState.getIsPlaying().addListener((o, ov, playing) -> updatePlayButton(playing));
        updatePlayButton(sharedState.getIsPlaying().get());

        // Avanzamento (player -> slider), solo se l'utente non sta trascinando
        sharedState.getProgress().addListener((o, ov, p) -> {
            if (!userIsSeeking) {
                sliderProgress.setValue(p.doubleValue());
            }
            updateElapsed(p.doubleValue());
        });

        // Slider -> player: salta alla posizione scelta
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

    /** Tempo trascorso = durata totale * avanzamento. */
    private void updateElapsed(double progress) {
        Track track = sharedState.getCurrentTrack().get();
        long elapsed = (track == null) ? 0 : Math.round(track.getSongLength() * progress);
        labelTime.setText(format(elapsed));
    }

    private String format(long totalSeconds) {
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    // --- Bottoni ---

    @FXML
    public void handlePlayPause(ActionEvent e) {
        if (playerService == null) return;
        if (sharedState.getIsPlaying().get()) {
            playerService.pause();
        } else {
            Track current = sharedState.getCurrentTrack().get();
            if (current != null) playerService.play(current);
        }
    }

    @FXML public void handleNext(ActionEvent e) { /* TODO: coda di riproduzione */ }

    @FXML public void handleSkipPlaylist(ActionEvent e) { /* TODO */ }

    @FXML public void handleChangeMode(ActionEvent e) { /* TODO: shuffle/repeat */ }

    @FXML public void handleQueue(ActionEvent e) { /* TODO */ }
}