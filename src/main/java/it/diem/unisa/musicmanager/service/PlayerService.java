package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

/**
 * Questa classe gestisce il punto in cui una traccia viene riprodotta.
 */
public class PlayerService {

    private final SharedState sharedState;
    private MediaPlayer mediaPlayer;

    public PlayerService(SharedState sharedState) {
        this.sharedState = sharedState;
    }

    public void play(Track track) {
        if (track == null || track.getSongPath() == null || track.getSongPath().isBlank()) {
            return;
        }

        stop();

        File audioFile = new File(track.getSongPath());
        if (!audioFile.exists()) {
            System.err.println("File audio non trovato: " + track.getSongPath());
            return;
        }

        try {
            Media media = new Media(audioFile.toURI().toString());

            media.setOnError(() -> {
                System.err.println("Errore Media: " + media.getError());
                resetAfterError();
            });

            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> {
                sharedState.getCurrentTrack().set(track);
                sharedState.getProgress().set(0);
                sharedState.getIsPlaying().set(true);
                mediaPlayer.play();
                System.out.println("Riproduzione avviata.");
            });

            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> updateProgress());

            mediaPlayer.setOnEndOfMedia(() -> {
                sharedState.getIsPlaying().set(false);
                sharedState.getProgress().set(1);
                System.out.println("Riproduzione terminata.");
            });

            mediaPlayer.setOnError(() -> {
                System.err.println("Errore MediaPlayer: " + mediaPlayer.getError());
                resetAfterError();
            });

        } catch (MediaException e) {
            System.err.println("Impossibile creare il MediaPlayer.");
            System.err.println("File: " + track.getSongPath());
            System.err.println("Motivo: " + e.getMessage());
            resetAfterError();
        } catch (Exception e) {
            System.err.println("Errore inatteso durante la riproduzione.");
            System.err.println("Motivo: " + e.getMessage());
            resetAfterError();
        }
    }

    public void pause() {
        if (mediaPlayer == null) {
            return;
        }
    //semplcieee esiste il metodo dall'oggetttooooo
        mediaPlayer.pause();
        sharedState.getIsPlaying().set(false);
    }

    public void resume() {
        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.play();
        sharedState.getIsPlaying().set(true);
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose(); //libera utte le risorse
            mediaPlayer = null;
        }

        sharedState.getIsPlaying().set(false);
        sharedState.getProgress().set(0);
    }

    public boolean isPlaying() {
        return sharedState.getIsPlaying().get();
    }

    private void updateProgress() {
        if (mediaPlayer == null) {
            return;
        }

        Duration current = mediaPlayer.getCurrentTime();
        Duration totalDuration = mediaPlayer.getTotalDuration();

        if (totalDuration == null || totalDuration.isUnknown() || totalDuration.toMillis() <= 0) {
            return;
        }

        double progress = current.toMillis() / totalDuration.toMillis();
        sharedState.getProgress().set(progress);
    }

    private void resetAfterError() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        sharedState.getIsPlaying().set(false);
        sharedState.getProgress().set(0);
    }
}