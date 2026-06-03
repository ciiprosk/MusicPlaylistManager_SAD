package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.model.Track;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

/**
 * Service per la riproduzione dei brani.
 * Incapsula il MediaPlayer di JavaFX e aggiorna lo SharedState
 * (brano corrente, stato play/pausa, avanzamento), cosi' la player bar e le
 * altre viste si aggiornano da sole tramite le proprieta' osservabili (Observer).
 */
public class PlayerService {

    private final SharedState sharedState;

    // Player JavaFX corrente; viene ricreato a ogni nuovo brano.
    private MediaPlayer mediaPlayer;

    // Brano attualmente caricato nel player (per capire se "play" e' un resume).
    private Track loadedTrack;

    /**
     * @param sharedState lo stato condiviso (brano corrente, stato, avanzamento)
     */
    public PlayerService(SharedState sharedState) {
        this.sharedState = sharedState;
    }

    /**
     * Avvia la riproduzione di un brano.
     * Se il brano e' gia' quello caricato, riprende da dove era; altrimenti
     * carica e riproduce il nuovo brano.
     *
     * @param track il brano da riprodurre
     */
    public void play(Track track) {
        if (track == null || track.getSongPath() == null) {
            return;
        }

        // Se e' lo stesso brano gia' caricato, basta riprendere.
        if (track.equals(loadedTrack) && mediaPlayer != null) {
            resume();
            return;
        }

        // Nuovo brano: fermiamo il precedente e ne carichiamo uno nuovo.
        stopCurrent();

        try {
            File file = new File(track.getSongPath());
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            loadedTrack = track;

            sharedState.setCurrentTrack(track);

            // Avanzamento (0..1) -> SharedState, sul thread UI.
            //mentre il brano suona, il tempo corrente avanza, e a ogni scatto calcola la percentuale di avanzamento
            // (tempo trascorso ÷ durata totale, un numero tra 0 e 1) e la scrive nello SharedState.
            mediaPlayer.currentTimeProperty().addListener((obs, oldV, newV) -> {
                double total = media.getDuration().toSeconds();
                if (total > 0) {
                    double p = newV.toSeconds() / total;
                    Platform.runLater(() -> sharedState.setProgress(p));
                }
            });

            // Fine brano: azzeriamo stato (non stiamo riproducendo niente) e avanzamento settato a 0.
            mediaPlayer.setOnEndOfMedia(() -> {
                sharedState.setPlaying(false);
                sharedState.setProgress(0.0);
            });

            mediaPlayer.play();
            sharedState.setPlaying(true);

        } catch (Exception e) {
            // Percorso non valido o file non riproducibile: non blocchiamo l'app.
            e.printStackTrace();
            sharedState.setPlaying(false);
        }
    }

    /**
     * Mette in pausa la riproduzione corrente.
     */
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            sharedState.setPlaying(false);
        }
    }

    /**
     * Riprende la riproduzione dopo una pausa.
     */
    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            sharedState.setPlaying(true);
        }
    }

    /**
     * Ferma e libera il player corrente (se presente). Non possiamo riprodurre 2 brani insieme
     */
    private void stopCurrent() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
    // TODO: next() - DA FARE DOPO (richiede una coda di riproduzione)
}
