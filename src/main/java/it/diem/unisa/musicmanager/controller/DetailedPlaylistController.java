package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.command.Command;
import it.diem.unisa.musicmanager.command.DeletePlaylistCommand;
import it.diem.unisa.musicmanager.model.Playlist;
//import it.diem.unisa.musicmanager.service.PlaylistService;
//import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.playmode.LoopMode;
import it.diem.unisa.musicmanager.playmode.PlayMode;
import it.diem.unisa.musicmanager.playmode.SequentialMode;
import it.diem.unisa.musicmanager.playmode.ShuffleMode;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.QueueService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.AlertUtil;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import it.diem.unisa.musicmanager.command.CommandManager;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.IOException;
import java.util.UUID;

/**
 * Controller della schermata di dettaglio playlist (detailedPlaylist.fxml).
 * Mostra in alto il nome della playlist e sotto l'elenco delle tracce
 * (titolo, autore, durata).
 * Per ora gestisce la View: il nome viene mostrato; il riempimento delle tracce
 * a partire dagli UUID richiede il TrackService (verra' collegato dopo).
 */
public class DetailedPlaylistController {

    // Campi dell'interfaccia, collegati agli fx:id in detailedPlaylist.fxml ---
    @FXML private Label labelName;
    @FXML private Label lblTrackCount;
    @FXML private VBox trackList;
    @FXML private Button buttonSequential;
    @FXML private Button buttonLoop;
    @FXML private Button buttonShuffle;


    // La playlist mostrata.
    private Playlist playlist;

    private TrackService trackService;
    private PlaylistService playlistService;
    private PlayerService playerService;
    private QueueService queueService;
    private boolean isTrackListenerAttached = false;
    private CommandManager commandManager;
    private Track draggedTrack;

    /**
     * Chiamato automaticamente da JavaFX appena la schermata e' pronta.
     * Definisce come mostrare ogni traccia: titolo, autore e durata.
     */
    @FXML
    private void initialize() {

    }

    /**
     * Imposta la playlist da visualizzare e ne mostra il nome e il conteggio.
     *
     * @param playlist la playlist di cui mostrare il dettaglio
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        labelName.setText(playlist.getName());
        updateTrackCount();
        if (trackService != null && playerService != null) {
            loadTracks();
        }

    }

    /**
     * Imposta il servizio della coda di riproduzione.
     *
     * @param queueService il {@link QueueService} da associare.
     */
    public void setQueueService(QueueService queueService){
        this.queueService = queueService;
    }

    /**
     * Imposta il servizio per la gestione delle playlist e registra un listener
     * per aggiornare l'interfaccia in caso di modifiche.
     *
     * @param playlistService il {@link PlaylistService} da associare.
     */
    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;

        // listener si aggiorna quando una playlist viene modificata
        playlistService.getPlaylists().addListener((InvalidationListener) obs -> {
            if (playlist != null) {
                javafx.application.Platform.runLater(() -> {
                    updateTrackCount();
                    loadTracks();
                });
            }
        });
    }

    /**
     * Imposta il servizio delle tracce e avvia l'ascolto per le modifiche alle tracce.
     * Se i servizi dipendenti sono pronti, carica le tracce della playlist.
     *
     * @param trackService il {@link TrackService} da associare.
     */
    public void setTrackService(TrackService trackService){
        this.trackService = trackService;
        createTrackListener();
        if (this.playlist != null && this.playerService != null) {
            loadTracks();
        }
    }


    /**
     * Imposta il servizio di riproduzione.
     * Se i servizi dipendenti sono pronti, carica le tracce della playlist.
     *
     * @param playerService il {@link PlayerService} da associare.
     */
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        if (this.playlist != null && this.trackService != null) {
            loadTracks();
        }
    }


    /**
     * Imposta il gestore dei comandi (Undo/Redo).
     * Se i servizi dipendenti sono pronti, carica le tracce della playlist.
     *
     * @param commandManager il {@link CommandManager} da associare.
     */
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        if (playlist != null && trackService != null && playerService != null) {
            loadTracks();
        }
    }
    /**
     * Aggiorna l'etichetta dell'interfaccia che mostra il numero di tracce presenti nella playlist.
     */
    private void updateTrackCount() {
        if (playlist == null) return;
        int n = playlist.getTracksList().size();
        lblTrackCount.setText(n + (n == 1 ? " track" : " tracks"));
    }

    /**
     * Crea e registra un listener per il {@link TrackService} in modo da ricaricare la lista
     * delle tracce qualora avvengano modifiche (es. aggiunta/rimozione) nel catalogo globale.
     */
    private void createTrackListener() {
        if (!isTrackListenerAttached && trackService != null) {
            trackService.getAllTracks().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (playlist != null) {
                       Platform.runLater(() -> loadTracks());
                    }
                }
            });

            isTrackListenerAttached = true;
        }
    }

    /**
     * Carica e mostra graficamente le tracce della playlist.
     * Associa ad ogni riga la logica di drag & drop per il riordinamento delle tracce.
     */
    private void loadTracks(){
        trackList.getChildren().clear();

        for(Track track : playlist.getTracksList()){
            try{
                Track updatedTrack = trackService.searchTrackById(track.getId())
                        .orElse(track);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));
                HBox row = loader.load();

                row.setOnDragDetected(event -> {
                    draggedTrack = track;

                    Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(track.getId().toString());
                    dragboard.setContent(content);

                    event.consume();
                });

                row.setOnDragOver(event -> {
                    if (draggedTrack != null
                            && !draggedTrack.getId().equals(track.getId())) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }

                    event.consume();
                });

                row.setOnDragDropped(event -> {
                    boolean completed = false;

                    if (draggedTrack != null
                            && playlist != null
                            && playlistService != null) {

                        int fromIndex =
                                playlist.getTracksList().indexOf(draggedTrack);

                        int toIndex =
                                playlist.getTracksList().indexOf(track);

                        if (fromIndex >= 0
                                && toIndex >= 0
                                && fromIndex != toIndex) {

                            playlistService.moveTrackInPlaylist(
                                    playlist.getId(),
                                    fromIndex,
                                    toIndex
                            );

                            if (queueService != null) {
                                queueService.synchronizePlaylistOrder(playlist);
                            }

                            loadTracks();
                            completed = true;
                        }
                    }

                    event.setDropCompleted(completed);
                    event.consume();
                });

                row.setOnDragDone(event -> {
                    draggedTrack = null;
                    event.consume();
                });

                RowTrackController controller = loader.getController();
                controller.setTrack(updatedTrack);
                controller.setPlayerService(playerService);
                controller.setTrackService(trackService);
                controller.setQueueService(queueService);
                controller.setPlaylistService(playlistService);
                controller.setParentPlaylist(playlist);
                controller.setCommandManager(commandManager);


                trackList.getChildren().add(row);

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Apre la finestra modale per modificare i dettagli (es. nome) della playlist corrente.
     *
     * @param actionEvent L'evento scatenato dal click sul pulsante "Modifica".
     */
    public void onModify(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/editPlaylist.fxml",
                    "Modify: " + playlist.getName(),
                    Modality.APPLICATION_MODAL
            );

            // Protezione anti-crash anche qui
            if (loader == null) {
                return;
            }

            EditPlaylistController controller = loader.getController();
            controller.setPlaylistService(playlistService);
            controller.setPlaylist(playlist);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Avvia la riproduzione della playlist.
     *
     * @param actionEvent L'evento scatenato dal click sul pulsante di riproduzione.
     */
    public void onPlay(ActionEvent actionEvent) {
        if (playlist == null) return;
        if (playlist.getTracksList().isEmpty()) {
            AlertUtil.showInfo("Empty Playlist", "To play this playlist, you must first add a track.");
            return;
        }

        if (playlistService != null) {
            playlistService.incrementPlayCount(playlist.getId());
        }

        if (playerService != null) {
            playerService.playPlayable(playlist);
        }
    }

    @FXML
    public void onAddTrack(ActionEvent actionEvent) {
        try {
            // Apriamo la finestra usando il titolo della playlist corrente
            FXMLLoader loader = WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/addTrackPlaylist.fxml",
                    "Add Track to: " + playlist.getName(), // Titolo dinamico specifico
                    Modality.APPLICATION_MODAL
            );

            // 1. PROTEZIONE ANTI-CRASH: se la finestra è già aperta, ci fermiamo qui
            if (loader == null) {
                return;
            }

            AddTrackPlaylisController controller = loader.getController();

            // Passiamo i dati al controller della nuova finestra
            controller.setPlaylistService(playlistService);
            controller.setTrackService(trackService);
            controller.setPlaylist(playlist);
            controller.setCommandManager(commandManager);

            // Quando la finestra di aggiunta salva e si chiude, rinfreschiamo la lista
            controller.setOnSaveCallback(() -> {
                javafx.application.Platform.runLater(() -> {
                    updateTrackCount();
                    loadTracks();
                });
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Avvia la procedura di eliminazione della playlist corrente, previa conferma dell'utente.
     *
     * @param actionEvent L'evento scatenato dal click sul pulsante "Elimina".
     */
    public void onDelete(ActionEvent actionEvent) {
        //uso il service per eliminare la playlist
        deletePlaylist();
        //close(actionEvent);
        WindowUtil.close( (Node) actionEvent.getSource());

    }

    /**
     * Avvia la riproduzione della playlist in modalità sequenziale.
     *
     * @param actionEvent L'evento scatenato dal click sul pulsante "Sequential".
     */
    public void onSequential(ActionEvent actionEvent) {
        if (playlist == null) return;
        if (playlist.getTracksList().isEmpty()) {
            AlertUtil.showInfo("Empty Playlist", "To play this playlist, you must first add a track.");
            return;
        }
        if (playlistService != null) playlistService.incrementPlayCount(playlist.getId());
        if (queueService != null && playerService != null) {
            queueService.setCurrentPlayMode(new SequentialMode()); // o LoopMode/SequentialMode
            playerService.playPlayable(playlist);
            updateModeButtons(buttonShuffle);
        }
    }

    /**
     * Avvia la riproduzione della playlist in modalità a ciclo continuo (loop).
     *
     * @param actionEvent L'evento scatenato dal click sul pulsante "Loop".
     */
    public void onLoop(ActionEvent actionEvent) {
        if (playlist == null) return;
        if (playlist.getTracksList().isEmpty()) {
            AlertUtil.showInfo("Empty Playlist", "To play this playlist, you must first add a track.");
            return;
        }
        if (playlistService != null) playlistService.incrementPlayCount(playlist.getId());
        if (queueService != null && playerService != null) {
            queueService.setCurrentPlayMode(new LoopMode()); // o LoopMode/SequentialMode
            playerService.playPlayable(playlist);
            updateModeButtons(buttonShuffle);
        }
    }

    /**
     * Avvia la riproduzione della playlist in modalità casuale (shuffle).
     *
     * @param actionEvent L'evento scatenato dal click sul pulsante "Shuffle".
     */
    public void onShuffle(ActionEvent actionEvent) {

        if (playlist == null) return;
        if (playlist.getTracksList().isEmpty()) {
            AlertUtil.showInfo("Empty Playlist", "To play this playlist, you must first add a track.");
            return;
        }
        if (playlistService != null)
            playlistService.incrementPlayCount(playlist.getId());

        if (queueService != null && playerService != null) {
            queueService.setCurrentPlayMode(new ShuffleMode()); // o LoopMode/SequentialMode
            playerService.playPlayable(playlist);
            updateModeButtons(buttonShuffle);
        }
    }


    /**
     * Aggiorna lo stile dei pulsanti delle modalità di riproduzione,
     * evidenziando quello attualmente attivo.
     *
     * @param active Il bottone corrispondente alla modalità di riproduzione attivata.
     */
    private void updateModeButtons(Button active) { //metodo per CSS + gestione delle modalità ascolto playlist
        // Reset tutti
        buttonSequential.setDisable(false);
        buttonLoop.setDisable(false);
        buttonShuffle.setDisable(false);
        buttonSequential.getStyleClass().remove("btn-mode-active");
        buttonLoop.getStyleClass().remove("btn-mode-active");
        buttonShuffle.getStyleClass().remove("btn-mode-active");
        // Attiva solo quello selezionato
        active.setDisable(true);
        active.getStyleClass().add("btn-mode-active");
    }

    /**
     * Mostra un alert di conferma e, in caso positivo, esegue il comando di
     * eliminazione della playlist corrente tramite il {@link CommandManager}.
     */
    private void deletePlaylist() {
        if (playlistService == null || playlist == null) return;

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete the Playlist \"" + playlist.getName() + "\"?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                Command cmd = new DeletePlaylistCommand(playlistService, playlist.getId());
                commandManager.executeCommand(cmd);
            }
        });
    }

    /**
     * Aggiunge tutte le tracce della playlist in coda di riproduzione.
     * Se la coda era vuota e nessuna traccia è in esecuzione, avvia automaticamente
     * il player.
     *
     * @param actionEvent L'evento scatenato dal click sul pulsante "Aggiungi alla coda".
     */
    @FXML
    public void onAddToQueue(ActionEvent actionEvent) {
        if(queueService !=null && playlist != null) {
            if (playlist.getTracksList().isEmpty()) {
                AlertUtil.showInfo("Empty Playlist", "To play this playlist, you must first add a track.");
                return;
            }
            //vedio se la coda d ascolto è vuota
            boolean isEmpty = queueService.getQueueList().isEmpty();

            //vedo se c'è un bran oin rispodzione
            boolean isPlayingTrack = playerService.currentTrackProperty().get() != null;

            //aggingo alla coda

            queueService.addToQueue(playlist);

            if(isEmpty && !isPlayingTrack){// se è vuota e nessuna raccia sta suonando allora osso far partire wuesta
                playerService.next();

            }else{
                AlertUtil.showInfo("Queue Updated", "All Tracks' Playlist '" + playlist.getName() + "' added to queue!");
            }
        }
    }
}