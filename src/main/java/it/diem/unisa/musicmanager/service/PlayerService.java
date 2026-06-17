package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.exception.QueueException;
import it.diem.unisa.musicmanager.model.Playable;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.QueueItemType;
import it.diem.unisa.musicmanager.model.Track;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Servizio per la gestione e la riproduzione dei brani musicali.
 * Incapsula il {@link MediaPlayer} di JavaFX e aggiorna le proprietà osservabili
 * (brano corrente, stato di riproduzione/pausa, avanzamento temporale) in modo
 * che la barra di riproduzione e le altre viste dell'interfaccia si aggiornino
 * automaticamente (pattern Observer/Binding).
 * Implementa l'interfaccia {@link TrackObserver} per reagire alla rimozione di tracce dal sistema.
 */
public class PlayerService implements TrackObserver {

    /**
     * Proprietà osservabile contenente la traccia attualmente in riproduzione (o pronta ad esserlo).
     */
    private final ObjectProperty<Track> currentTrack = new SimpleObjectProperty<>(null);
    /**
     * Proprietà osservabile che indica se il player sta attualmente riproducendo un brano (true) o è in pausa/fermo (false).
     */
    private final BooleanProperty isPlaying = new SimpleBooleanProperty(false);
    /**
     * Proprietà osservabile che rappresenta la percentuale di avanzamento della riproduzione corrente (valore compreso tra 0.0 e 1.0).
     */
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    /**
     * Riferimento al servizio per la gestione delle tracce.
     */
    private TrackService trackService;
    /**
     * Riferimento al servizio per la gestione della coda di riproduzione.
     */
    private QueueService queueService;
    /**
     * Il componente nativo di JavaFX per il controllo della riproduzione del file multimediale.
     */
    private MediaPlayer mediaPlayer;

    /**
     * Il brano attualmente caricato nel player (utilizzato per determinare se un'azione di play corrisponde ad una ripresa).
     */
    private Track loadedTrack;

    /**
     * Restituisce la proprietà di sola lettura per lo stato di riproduzione.
     *
     * @return La proprietà {@link ReadOnlyBooleanProperty} indicante se la riproduzione è attiva.
     */
    public ReadOnlyBooleanProperty isPlayingProperty() {
        return isPlaying;
    }

    /**
     * Imposta il servizio di gestione delle tracce.
     *
     * @param trackService Il servizio {@link TrackService} da iniettare.
     */
    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * Imposta il servizio di gestione della coda.
     *
     * @param queueService Il servizio {@link QueueService} da iniettare.
     */
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Carica ed avvia internamente la traccia mediante il {@link MediaPlayer}.
     * Configura i listener di avanzamento, gestisce la fine del brano e incrementa il contatore di riproduzioni.
     *
     * @param track La traccia da caricare.
     */
    private void loadTrack(Track track) {
        try {
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

            if (trackService != null) trackService.incrementPlayCount(track.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Imposta il listener sul tempo corrente del player per aggiornare la proprietà di progresso.
     *
     * @param media Il file multimediale associato.
     */
    private void setUpProgressListener(Media media) {
        mediaPlayer.currentTimeProperty().addListener((obs, oldV, newV) -> {
            if (mediaPlayer == null) {
                return; // Se il player è stato azzerato dal cambio playlist, esce senza crashare
            }

            Duration total = mediaPlayer.getTotalDuration();
            if (total != null && !total.isUnknown() && total.toSeconds() > 0) {
                double p = newV.toSeconds() / total.toSeconds();
                Platform.runLater(() -> progress.set(p));
            }
        });
    }

    /**
     * Configura l'azione da intraprendere quando la riproduzione di un brano giunge al termine.
     * L'azione predefinita è il passaggio al brano successivo.
     */
    private void setupEndOfMediaHandler() {
        mediaPlayer.setOnEndOfMedia(() -> {
            Platform.runLater(this::next);
        });
    }

    /**
     * Ferma la riproduzione corrente e rilascia le risorse allocate per il {@link MediaPlayer}.
     */
    private void stopCurrent() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    /**
     * Restituisce la proprietà di sola lettura per il brano corrente.
     *
     * @return La proprietà {@link ReadOnlyObjectProperty} contenente la traccia corrente.
     */
    public ReadOnlyObjectProperty<Track> currentTrackProperty() {
        return currentTrack;
    }

    /**
     * Restituisce la proprietà di sola lettura per la percentuale di avanzamento del brano.
     *
     * @return La proprietà {@link ReadOnlyDoubleProperty} con il progresso (0.0 a 1.0).
     */
    public ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }

    /**
     * Avvia la riproduzione di una traccia specificata.
     * Se la traccia è già caricata ed è in pausa, ne riprende la riproduzione (a meno che non venga forzato il riavvio).
     *
     * @param track          La traccia da riprodurre.
     * @param clearQueueItem Se impostato a true, deseleziona l'elemento corrente nella coda di riproduzione.
     * @param forceRestart   Se impostato a true, forza il riavvio della traccia dall'inizio anche se era già caricata.
     */
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

    /**
     * Sospende temporaneamente la riproduzione del brano corrente e aggiorna lo stato.
     */
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying.set(false); // Aggiorna isPlaying
        }
    }

    /**
     * Riprende la riproduzione del brano corrente dal punto in cui era stato sospeso.
     * Applica un workaround per allineare il buffer audio e ridurre il rischio di micro-salti all'avvio.
     */
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
     * Sposta la riproduzione in un punto specifico del brano basato su una percentuale di avanzamento.
     *
     * @param fraction Valore compreso tra 0.0 (inizio brano) e 1.0 (fine brano).
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
     * Utile quando si preme Play su una playlist e si vuole ripartire dal primo brano.
     *
     * @param track La traccia da riprodurre dall'inizio.
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
     * Passa al brano successivo nella coda di riproduzione.
     * Se non ci sono ulteriori brani disponibili nella coda, arresta il player e pulisce la coda.
     */
    public void next() {
        if (queueService == null) {
            stopCurrent();
            loadedTrack = null;
            currentTrack.set(null);
            isPlaying.set(false);
            progress.set(0.0);
            return;
        }

        if (!queueService.hasNext()) {
            stopCurrent();
            loadedTrack = null;
            queueService.clearQueue();
            currentTrack.set(null);
            isPlaying.set(false);
            progress.set(0.0);

            return;
        }

        QueueItem next = queueService.nextItem();

        if (next != null) {
            List<Track> tracks = next.getPlayable().getTracksToPlay();

            if (!tracks.isEmpty()) {
                play(tracks.get(0), false, true);
            } else {
                stopCurrent();
                loadedTrack = null;
                queueService.clearQueue();
                currentTrack.set(null);
                isPlaying.set(false);
                progress.set(0.0);
            }
        } else {
            stopCurrent();
            loadedTrack = null;
            queueService.clearQueue();
            currentTrack.set(null);
            isPlaying.set(false);
            progress.set(0.0);
        }
    }

    /**
     * Salta l'intera playlist attualmente in riproduzione e passa all'elemento successivo della coda.
     * Se non ci sono elementi successivi o si verifica un errore, arresta il player e svuota la coda.
     */
    public void skipPlaylist() {
        if (queueService == null) {
            stopCurrent();
            loadedTrack = null;
            currentTrack.set(null);
            isPlaying.set(false);
            progress.set(0.0);
            return;
        }

        if (!queueService.hasNext()) {
            stopCurrent();
            loadedTrack = null;
            queueService.clearQueue();
            currentTrack.set(null);
            isPlaying.set(false);
            progress.set(0.0);
            return;
        }

        try {
            QueueItem next = queueService.skipCurrentPlaylist();

            if (next != null) {
                List<Track> tracks = next.getPlayable().getTracksToPlay();

                if (!tracks.isEmpty()) {
                    play(tracks.get(0), false, true);
                } else {
                    stopCurrent();
                    loadedTrack = null;
                    queueService.clearQueue();
                    currentTrack.set(null);
                    isPlaying.set(false);
                    progress.set(0.0);
                }
            } else {
                stopCurrent();
                loadedTrack = null;
                queueService.clearQueue();
                currentTrack.set(null);
                isPlaying.set(false);
                progress.set(0.0);
            }

        } catch (QueueException e) {
            stopCurrent();
            loadedTrack = null;
            queueService.clearQueue();
            currentTrack.set(null);
            isPlaying.set(false);
            progress.set(0.0);
        }
    }

    /**
     * Gestisce la cancellazione di una traccia dal sistema.
     * Se la traccia eliminata corrisponde a quella attualmente in riproduzione, arresta il player
     * e deseleziona l'elemento corrente.
     *
     * @param trackId L'ID della traccia che è stata eliminata.
     */
    @Override
    public void onTrackDeleted(UUID trackId) {

        if (trackId == null) {
            return;
        }

        Track current =
                currentTrack.get();

        if (current != null && current.getId().equals(trackId)) {
            stopCurrent();
            loadedTrack = null;
            currentTrack.set(null);
            isPlaying.set(false);
            progress.set(0.0);

            if (queueService != null) {
                queueService.setCurrentItem(null);
            }
        }
    }

    /**
     * Riproduce l'elemento {@link Playable} specificato (una singola traccia o un'intera playlist).
     * Svuota la coda corrente e aggiunge il nuovo elemento all'inizio per avviare subito la riproduzione.
     *
     * @param playable L'elemento da riprodurre.
     */
    public void playPlayable(Playable playable) {
        if (playable == null) return;

        stopCurrent();
        loadedTrack = null;

        if (queueService != null) {
            queueService.clearQueue();
            List<QueueItem> items = queueService.addToQueue(playable);
            if (items != null && !items.isEmpty()) {
                queueService.setCurrentItem(items.get(0));
            }
        }

        List<Track> tracksToPlay = playable.getTracksToPlay();
        if (!tracksToPlay.isEmpty()) {
            play(tracksToPlay.get(0), false, true);
        }
    }

    /**
     * Attiva o disattiva la riproduzione (Pausa/Play) per l'elemento {@link Playable} fornito.
     * Se il playable fornito è già in esecuzione, il metodo mette in pausa la riproduzione.
     * Se il playable fornito è diverso da quello in corso (o non vi è alcuna traccia attiva),
     * svuota la coda, imposta il nuovo elemento e ne avvia la riproduzione.
     *
     * @param playable L'elemento riproducibile su cui eseguire il toggle.
     */
    public void togglePlay(Playable playable) {
        if (playable == null) return;

        Track current = currentTrack.get();
        boolean isCurrentlyPlayingThisPlayable = false;

        if (current != null && isPlaying.get()) {
            if (playable.getType() == QueueItemType.TRACK && playable.getId().equals(current.getId())) {
                // Se ho passato una Track ed è quella in esecuzione
                isCurrentlyPlayingThisPlayable = true;
            } else if (playable.getType() == QueueItemType.PLAYLIST && queueService != null && queueService.getCurrentItem() != null) {
                // Se ho passato una Playlist e la traccia attuale fa parte di QUELLA playlist in coda
                if (playable.getId().equals(queueService.getCurrentItem().getBelongsToPlaylist())) {
                    isCurrentlyPlayingThisPlayable = true;
                }
            }
        }

        if (isCurrentlyPlayingThisPlayable) {
            pause();
        } else {
            // Logica per far partire la riproduzione (Track o Playlist)
            if (queueService != null) {
                queueService.clearQueue();
                List<QueueItem> items = queueService.addToQueue(playable);
                if (items != null && !items.isEmpty()) {
                    queueService.setCurrentItem(items.get(0));
                }
            }

            List<Track> tracksToPlay = playable.getTracksToPlay();
            if (!tracksToPlay.isEmpty()) {
                play(tracksToPlay.get(0), false, false);
            }
        }
    }

}
