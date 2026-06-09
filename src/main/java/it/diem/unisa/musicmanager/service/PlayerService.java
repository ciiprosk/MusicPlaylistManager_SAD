package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.playmode.PlayMode;
import it.diem.unisa.musicmanager.playmode.SequentialMode;
import javafx.application.Platform;
import javafx.beans.property.*;
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


    private final ObjectProperty<Track> currentTrack = new SimpleObjectProperty<>(null);
    private final BooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);

    private PlayMode currentPlayMode = new SequentialMode();    //di default
    private int currentIndex = 0;   //default


    private MediaPlayer mediaPlayer;

    // Brano attualmente caricato nel player (per capire se "play" e' un resume).
    private Track loadedTrack;

    public ReadOnlyObjectProperty<Track> currentTrackProperty() {
        return currentTrack;
    }

    public ReadOnlyBooleanProperty isPlayingProperty() {
        return isPlaying;
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }

    public void setCurrentPlayMode(PlayMode playMode) {

        this.currentPlayMode = playMode;

    }

    public void play(Track track) {
        if (track == null || track.getSongPath() == null) return;

        if (track.equals(loadedTrack) && mediaPlayer != null) {
            resume();
            return;
        }

        stopCurrent();

        try {
            File file = new File(track.getSongPath());
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            loadedTrack = track;

            // Scriviamo nella nostra Property interna
            currentTrack.set(track);

            // Calcolo dell'avanzamento sulle Property interne
            mediaPlayer.currentTimeProperty().addListener((obs, oldV, newV) -> {
                double total = media.getDuration().toSeconds();
                if (total > 0) {
                    double p = newV.toSeconds() / total;
                    Platform.runLater(() -> progress.set(p)); // Aggiorna progress
                }
            });

            // Gestione fine brano
            mediaPlayer.setOnEndOfMedia(() -> {
                isPlaying.set(false);
                progress.set(0.0);
            });

            mediaPlayer.play();
            isPlaying.set(true); // Aggiorna isPlaying

        } catch (Exception e) {
            e.printStackTrace();
            isPlaying.set(false);
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying.set(false); // Aggiorna isPlaying
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying.set(true); // Aggiorna isPlaying
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

    /**
     * Gestisce l'alternanza di riproduzione e pausa per una determinata traccia.
     * Se la traccia è già in esecuzione, viene messa in pausa.
     * Se era in pausa, riprende. Se è un brano nuovo, lo fa partire.
     */
    public void togglePlay(Track track) {
        if (track == null) return;

        // Se il brano passato è quello corrente e sta già suonando -> Pausa
        if (track.equals(currentTrack.get()) && isPlaying.get()) {
            pause();
        } else {
            // Altrimenti (è un brano nuovo o era in pausa) -> Riproduci
            play(track);
        }
    }

    /*
    //metodo per andare alla prossima traccia, a seconda della Strategy applicata
    public void next() {

        currentPlayMode.nextItem(queue, currentIndex);     //

    }
    */

}
