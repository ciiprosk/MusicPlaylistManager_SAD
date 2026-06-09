package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.playmode.LoopMode;
import it.diem.unisa.musicmanager.playmode.PlayMode;
import it.diem.unisa.musicmanager.playmode.SequentialMode;
import it.diem.unisa.musicmanager.playmode.ShuffleMode;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.QueueService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

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

    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
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
        //chiamo il service
        playerService.next();
    }

    @FXML
    public void handleSkipPlaylist(ActionEvent actionEvent) {
        //la logica è che deve saltare tutti i brani presenti nella coda e saltareli tutti alla porssima traccia
        if (queueService == null) return;

        QueueItem current = queueService.getCurrentItem();
        if(current == null) return;

        UUID currentPlaylist = current.getBelongsToPlaylist();

        //se è una traccia sola l'id n di associatìzione è null
        QueueItem next = queueService.nextItem();

        while(next != null && currentPlaylist.equals(next.getBelongsToPlaylist())){
            next = queueService.nextItem();
        }

        if (next != null) {
            List<Track> tracks = next.getPlayable().getTracksToPlay();
            if(!tracks.isEmpty()){
                playerService.play(tracks.get(0));
            }
        }
    }

    @FXML
    public void handleChangeMode(ActionEvent actionEvent) {
            //cicla tra le modalità e le setta nella queue service
        currentPlayModeIndex = (currentPlayModeIndex + 1) % playModes.length;
        queueService.setCurrentPlayMode(playModes[currentPlayModeIndex]);
        updateModeButton();

    }

    private void updateModeButton(){
        switch (currentPlayModeIndex){
            case 0 -> buttonMode.setText("⇢"); //U+21E2
            case 1 -> buttonMode.setText("⇄"); //\u21C4
            case 2 -> buttonMode.setText("↻"); //\u21BB
        }
    }

    public void handleQueue(ActionEvent actionEvent) {
        //dopo
    }

    private void bind() {
        // 1. Ascolta il cambio del brano dal Service e aggiorna info + pulsante skip playlist
        playerService.currentTrackProperty().addListener((o, ov, track) -> {
            updateTrackInfo(track);
            updateSkipPlaylistButton();
        });
        updateTrackInfo(playerService.currentTrackProperty().get());
        updateSkipPlaylistButton();

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

    private void updateSkipPlaylistButton() {
        if (queueService == null) return;
        QueueItem current = queueService.getCurrentItem();
        boolean belongsToPlaylist = current != null && current.getBelongsToPlaylist() != null;
        buttonSkipPlaylist.setVisible(belongsToPlaylist);
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
