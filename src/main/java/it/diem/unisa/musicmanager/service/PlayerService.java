package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.exception.QueueException;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.playmode.PlayMode;
import it.diem.unisa.musicmanager.playmode.SequentialMode;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

/**
 * Service per la riproduzione dei brani.
 * Incapsula il MediaPlayer di JavaFX e aggiorna lo SharedState (brano corrente,
 * stato play/pausa, avanzamento) accedendo alle sue Property. Cosi' la player
 * bar e le altre viste si aggiornano da sole (pattern Observer).
 */
public class PlayerService {

    private TrackService trackService;
    private QueueService queueService;

    private final ObjectProperty<Track> currentTrack = new SimpleObjectProperty<>(null);
    private final BooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);

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

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    /*
    public void setCurrentPlayMode(PlayMode playMode) {

        this.currentPlayMode = playMode;

    }
    */
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    public void play(Track track, boolean clearQueueItem, boolean forceRestart) {
        if (track == null || track.getSongPath() == null) return;

        if (!forceRestart && track.equals(loadedTrack) && mediaPlayer != null) {
            // Se è lo stesso brano ed è in play, non fare nulla (o metti in pausa se preferisci, 
            // ma di solito togglePlay gestisce questo).
            // Se lo chiamo direttamente, faccio solo resume.
            resume();
            return;
        }

        // Segnaliamo alla coda che stiamo ascoltando una canzone "fuori coda"
        if (clearQueueItem && queueService != null) {
            queueService.setCurrentItem(null);
        }

        stopCurrent();
        loadTrack(track);

    }

    private void loadTrack(Track track) {
        try{
            Media media = new Media(new File(track.getSongPath()).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            loadedTrack = track;
            currentTrack.set(track);

            setUpProgressListener(media);
            setupEndOfMediaHandler();

            // Aspetta che il media sia pronto prima di suonare, evita il "salto vuoto"
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
                isPlaying.set(true);
            });

            if(trackService !=null) trackService.incrementPlayCount(track.getId());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpProgressListener(Media media){
        mediaPlayer.currentTimeProperty().addListener((obs, oldV, newV) -> {
            Duration total = mediaPlayer.getTotalDuration();
             if (total != null && !total.isUnknown() && total.toSeconds() > 0) {
                 double p = newV.toSeconds() / total.toSeconds();
                 Platform.runLater(() -> progress.set(p));
             }
        });
    }

    private void setupEndOfMediaHandler() {
        mediaPlayer.setOnEndOfMedia(() -> {
            isPlaying.set(false);
            progress.set(0.0);
            Platform.runLater(this::next);
        });
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying.set(false); // Aggiorna isPlaying
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            // Workaround per un bug noto del motore audio JavaFX (GStreamer): 
            // Forziamo il riallineamento del buffer al tempo esatto prima di fare play 
            // per evitare quel "micro-salto" o ripetizione dell'audio al resume.
            mediaPlayer.seek(mediaPlayer.getCurrentTime());
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
     * Avvia una traccia dall'inizio, anche se era già caricata.
     * Utile quando si preme Play su una playlist e si vuole ripartire dalla prima traccia.
     *
     * @param track traccia da riprodurre dall'inizio.
     */
    public void playFromBeginning(Track track) {
        if (track == null) {
            return;
        }

        stopCurrent();
        loadedTrack = null;

        play(track, false, true);
    }

    /**
     * Gestisce l'alternanza di riproduzione e pausa per una determinata traccia.
     * Se la traccia è già in esecuzione, viene messa in pausa.
     * Se era in pausa, riprende. Se è un brano nuovo, lo fa partire.
     */
    public void togglePlay(Track track) {
        if (track == null) return;

        // Se il brano passato è quello corrente e sta già suonando -> Pausa
        Track current = currentTrack.get();

        if (current != null
                && track.getId().equals(current.getId())
                && isPlaying.get()) {
            pause();
        } else {
            // Nuova traccia fuori coda: svuota la coda (clearQueueItem=true) e riproduci (forceRestart = false)
            if (queueService != null) {
                queueService.clearQueue();
            }
            play(track, true, false);
        }
    }

    public void next() {
        if (queueService == null || !queueService.hasNext()) {
            // reset stato player
            stopCurrent();
            loadedTrack = null;
            currentTrack.set(null);
            return;
        }
        QueueItem next = queueService.nextItem();
    }

    public void skipPlaylist() {
        if (queueService == null) return;

        if (!queueService.hasNext()) {
            stopCurrent();
            loadedTrack = null;
            currentTrack.set(null);
            return;
        }

        try {
            QueueItem next = queueService.skipCurrentPlaylist();
            if (next != null) {
                play(next.getPlayable().getTracksToPlay().get(0), false, true);
            } else {
                stopCurrent(); loadedTrack = null; currentTrack.set(null);
            }
        } catch (QueueException e) {    // La coda è esaurita: resetta lo stato del player.
            stopCurrent(); loadedTrack = null; currentTrack.set(null);
        }
    }

}
