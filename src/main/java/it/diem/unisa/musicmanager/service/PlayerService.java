package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.model.Track;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import it.diem.unisa.musicmanager.state.SharedState;
import java.io.File;

/**
 * Service per la riproduzione dei brani.
 * Incapsula il MediaPlayer di JavaFX e aggiorna lo SharedState (brano corrente,
 * stato play/pausa, avanzamento) accedendo alle sue Property. Cosi' la player
 * bar e le altre viste si aggiornano da sole (pattern Observer).
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


    public SharedState getSharedState() {
        return sharedState;
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

        // Stesso brano gia' caricato: basta riprendere.
        if (track.equals(loadedTrack) && mediaPlayer != null) {
            resume();
            return;
        }

        // Nuovo brano: fermiamo il precedente e carichiamo questo.
        stopCurrent();

        try {
            File file = new File(track.getSongPath());
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            loadedTrack = track;

            // Brano corrente -> SharedState (la player bar si aggiorna da sola).
            sharedState.getCurrentTrack().set(track);

            // Avanzamento (0..1) -> SharedState, sul thread UI.
            mediaPlayer.currentTimeProperty().addListener((obs, oldV, newV) -> {
                double total = media.getDuration().toSeconds();
                if (total > 0) {
                    double p = newV.toSeconds() / total;
                    Platform.runLater(() -> sharedState.getProgress().set(p));
                }
            });

            // Fine brano: azzeriamo stato e avanzamento.
            mediaPlayer.setOnEndOfMedia(() -> {
                sharedState.getIsPlaying().set(false);
                sharedState.getProgress().set(0.0);
            });

            mediaPlayer.play();
            sharedState.getIsPlaying().set(true);

        } catch (Exception e) {
            // File non valido o non riproducibile: non blocchiamo l'app.
            e.printStackTrace();
            sharedState.getIsPlaying().set(false);
        }
    }

    /**
     * Mette in pausa la riproduzione corrente.
     */
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            sharedState.getIsPlaying().set(false);
        }
    }

    /**
     * Riprende la riproduzione dopo una pausa.
     */
    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            sharedState.getIsPlaying().set(true);
        }
    }

    /**
     * Ferma e libera il player corrente (se presente).
     */
    private void stopCurrent() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    /**
     * Salta a una posizione del brano corrente.
     * Riceve una frazione 0..1 (la posizione dello slider) e la traduce
     * nel tempo corrispondente del brano, poi sposta lì la riproduzione.
     *
     * @param fraction posizione 0..1 (0 = inizio, 1 = fine del brano)
     */
    public void seek(double fraction) {
        // Nessun brano caricato: niente da fare.
        if (mediaPlayer != null) {
            // Durata totale del brano (potrebbe non essere ancora nota).
            javafx.util.Duration total = mediaPlayer.getTotalDuration();

            // Salta solo se la durata e' davvero disponibile:
            // se il media non e' ancora pronto, total e' UNKNOWN e il calcolo
            // darebbe un tempo senza senso.
            if (total != null && !total.isUnknown()) {
                // Posizione = durata totale * frazione.
                // Es. fraction = 0.5 su un brano di 3:00 -> salta a 1:30.
                mediaPlayer.seek(total.multiply(fraction));
            }
        }
    }

    // TODO: next() - DA FARE DOPO (richiede una coda di riproduzione)
}
