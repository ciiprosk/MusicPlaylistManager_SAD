package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.playmode.LoopMode;
import it.diem.unisa.musicmanager.playmode.PlayMode;
import it.diem.unisa.musicmanager.playmode.SequentialMode;
import it.diem.unisa.musicmanager.playmode.ShuffleMode;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.QueueService;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;

import java.util.List;
import java.util.UUID;


/**
 * Controller della player bar.
 * Tiene un riferimento al PlayerService e delega a esso le azioni dei pulsanti.
 * Resta volutamente "sottile": tutta la logica di riproduzione vive nel service.
 */
public class PlayerController {

    // Service di riproduzione, iniettato da chi carica questo controller.
    private PlayerService playerService;
    private QueueService queueService;
    private it.diem.unisa.musicmanager.service.PlaylistService playlistService;

    private final PlayMode[] playModes = {new SequentialMode(), new ShuffleMode(), new LoopMode()};
    private int currentPlayModeIndex = 0;

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
     * @param queueService il service di coda di riproduzione
     */
    public void setQueueService(QueueService queueService) {

        this.queueService = queueService;

        queueService.getQueueList().addListener((ListChangeListener<QueueItem>) c -> {
            updateNextButton();
            updateSkipPlaylistButton();
        });

        updateNextButton();
        updateSkipPlaylistButton();
    }

    /**
     * @param playlistService il service delle playlist di tracce
     */
    public void setPlaylistService(it.diem.unisa.musicmanager.service.PlaylistService playlistService) {
        this.playlistService = playlistService;
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
            if (current != null) playerService.play(current, false, true);
        }

    }

    /**
     * Pulsante Next: salta la traccia in riproduzione, passa alla successiva
     */
    @FXML
    public void handleNext(ActionEvent actionEvent) {
        //chiamo il service
        playerService.next();
    }

    /**
     * Pulsante Skip Playlist: salta la playlist in riproduzione, passa al prossimo elemento in coda di ascolto
     */
    @FXML
    public void handleSkipPlaylist(ActionEvent actionEvent) {
        playerService.skipPlaylist();
    }

    /**
     *PlayMode: cambia modalità di ascolto della coda (shuffle, loop e sequenziale)
     */
    @FXML
    public void handleChangeMode(ActionEvent actionEvent) {
        //cicla tra le modalità e le setta nella queue service
        currentPlayModeIndex = (currentPlayModeIndex + 1) % playModes.length;
        queueService.setCurrentPlayMode(playModes[currentPlayModeIndex]);
        updateModeButton();

    }

    /**
     * Pulsante PlayMode: aggiorna visivamente il tasto in base alla modalità di ascolto
     */
    private void updateModeButton(){
        switch (currentPlayModeIndex){
            case 0 -> {
                buttonMode.setText("⇢");
                buttonMode.setTooltip(new Tooltip("Sequential"));   //testo in sovraimpressione sul tasto
            } //U+21E2
            case 1 -> {
                buttonMode.setText("⇄"); //\u21C4
                buttonMode.setTooltip(new Tooltip("Shuffle"));   //testo in sovraimpressione sul tasto
            }
            case 2 -> {
                buttonMode.setText("↻"); //\u21BB
                buttonMode.setTooltip(new Tooltip("Loop"));   //testo in sovraimpressione sul tasto
            }
        }
    }

    /**
     * Gestisce l'apertura della finestra per la visualizzazione della coda di riproduzione.
     * Carica il file FXML associato, inietta i service nel nuovo controller e previene
     * l'apertura di finestre duplicate.
     * * @param actionEvent l'evento generato dal click sul bottone della coda.
     */
    public void handleQueue(ActionEvent actionEvent) {
        try {
            javafx.fxml.FXMLLoader loader = it.diem.unisa.musicmanager.util.WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/queueView.fxml",
                    "Listening Queue",
                    javafx.stage.Modality.NONE
            );
            // --- AGGIUNGI QUESTA PROTEZIONE QUI ---
            if (loader == null) {
                return; // Ferma il codice se la finestra della coda è già aperta!
            }
            QueueViewController ctrl = loader.getController();
            ctrl.setPlaylistService(playlistService);
            ctrl.setQueueService(queueService);
            ctrl.setPlayerService(playerService);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Collega le proprietà osservabili dei Service ai componenti grafici della View.
     * Imposta i listener per l'aggiornamento automatico dei metadati del brano,
     * dello stato del pulsante play/pausa e della posizione dello slider in base al tempo.
     */
    private void bind() {
        // 1. Ascolta il cambio del brano dal Service e aggiorna info + pulsante skip playlist
        playerService.currentTrackProperty().addListener((o, ov, track) -> {
            updateTrackInfo(track);
            updateSkipPlaylistButton();
        });
        updateTrackInfo(playerService.currentTrackProperty().get());
        updateSkipPlaylistButton();
        updateModeButton(); // Fix text visibility at startup

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

        // 6. Quando cambia currentTrackProperty(), chiamiamo updateNextButton()
        playerService.currentTrackProperty().addListener((o, ov, nv) -> updateNextButton());

    }

    /**
     * Aggiorna lo stato di abilitazione del pulsante "Salta Playlist".
     * Il pulsante è attivo solo se la traccia correntemente in esecuzione
     * appartiene a una playlist.
     */
    private void updateSkipPlaylistButton() {
        if (queueService == null) return;
        QueueItem current = queueService.getCurrentItem();
        boolean belongsToPlaylist = current != null && current.getBelongsToPlaylist() != null;
        buttonSkipPlaylist.setDisable(!belongsToPlaylist);
    }

    /**
     * Aggiorna le etichette testuali (titolo, autore, durata) con le informazioni
     * del brano correntemente in esecuzione. Ripristina i valori di default se la traccia è null.
     * * @param track la traccia di cui visualizzare i dettagli.
     */
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

    /**
     * Aggiorna l'icona testuale del pulsante Play/Pausa in base allo stato fornito.
     * * @param playing true se è in corso la riproduzione, false altrimenti.
     */
    private void updatePlayButton(boolean playing) {
        buttonPlay.setText(playing ? "⏸" : "▶");
    }

    /**
     * Calcola e aggiorna l'etichetta del tempo trascorso in base alla percentuale
     * di avanzamento dello slider e alla durata totale del brano corrente.
     * * @param progress valore compreso tra 0.0 e 1.0 che rappresenta l'avanzamento.
     */
    private void updateElapsed(double progress) {
        Track track = playerService.currentTrackProperty().get();
        long elapsed = (track == null) ? 0 : Math.round(track.getSongLength() * progress);
        labelTime.setText(format(elapsed));
    }

    /**
     * Formatta una quantità di tempo in secondi nel formato MM:SS.
     * * @param totalSeconds il totale dei secondi da formattare.
     * @return una stringa formattata (es. "03:45").
     */
    private String format(long totalSeconds) {
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    /**
     * Aggiorna lo stato di abilitazione del pulsante Next.
     * Il pulsante viene disabilitato se non ci sono tracce caricate nel player.
     */
    private void updateNextButton() {
        // Disattiva il pulsante Next solo se il player è completamente fermo (nessuna traccia caricata)
        buttonNext.setDisable(playerService.currentTrackProperty().get() == null);
    }

}