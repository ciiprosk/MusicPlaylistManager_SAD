package it.diem.unisa.musicmanager.controller;

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
        // Quando il TrackService sara' disponibile, qui risolveremo gli UUID
        // della playlist nei rispettivi Track e riempiremo la lista:
        //
        // List<Track> tracce = playlist.getTracks().stream()
        //         .map(id -> trackService.findById(id))   // metodo da aggiungere al service
        //         .filter(Objects::nonNull)
        //         .toList();
        // tracksList.getItems().setAll(tracce);
    }

    public void setQueueService(QueueService queueService){
        this.queueService = queueService;
    }

    public void setPlaylistService(PlaylistService playlistService){
        this.playlistService = playlistService;

    }
    public void setTrackService(TrackService trackService){
        this.trackService = trackService;
        createTrackListener();
        if (this.playlist != null && this.playerService != null) {
            loadTracks();
        }
    }


    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        if (this.playlist != null && this.trackService != null) {
            loadTracks();
        }
    }


    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    private void updateTrackCount() {
        if (playlist == null) return;
        int n = playlist.getTracksList().size();
        lblTrackCount.setText(n + (n == 1 ? " track" : " tracks"));
    }

    private void createTrackListener() {
        if (!isTrackListenerAttached && trackService != null) {
            trackService.getAllTracks().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (playlist != null) {
                        javafx.application.Platform.runLater(() -> loadTracks());
                    }
                }
            });

            isTrackListenerAttached = true;
        }
    }

    private void loadTracks(){
        trackList.getChildren().clear();

        for(Track track : playlist.getTracksList()){
            try{
                Track updatedTrack = trackService.searchTrackById(track.getId())
                        .orElse(track);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));
                HBox row = loader.load();

                RowTrackController controller = loader.getController();
                controller.setTrack(updatedTrack);
                controller.setPlayerService(playerService);
                controller.setTrackService(trackService);
                controller.setQueueService(queueService);
                controller.setPlaylistService(playlistService);
                controller.setParentPlaylist(playlist);

                //se premo il tasto elimina, rimuovo la traccia dalla playlist
                controller.setOnDeleteAction(() -> {
                    if (playlistService != null) {
                        playlistService.removeTrackFromPlaylist(playlist.getId(), track.getId());
                        javafx.application.Platform.runLater(() -> {
                            updateTrackCount();
                            loadTracks();
                        });
                    }
                });

                trackList.getChildren().add(row);

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

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

            // NOTA: Ho rimosso WindowUtil.close(...) da qui!
            // In questo modo la schermata di dettaglio resta aperta sotto
            // mentre modifichi il nome sopra.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPlay(ActionEvent actionEvent) {
        if (playlist == null || playlist.getTracksList().isEmpty()) {
            return;
        }

        if (playlistService != null) {
            playlistService.incrementPlayCount(playlist.getId());
        }

        if (queueService != null && playerService != null) {
            queueService.clearQueue();
            queueService.addToQueue(playlist);
            playerService.next();
        } else if (playerService != null) {
            playerService.play(playlist.getTracksList().get(0), false, true);
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

    public void onDelete(ActionEvent actionEvent) {
        //uso il service per eliminare la playlist
        deletePlaylist();
        //close(actionEvent);
        WindowUtil.close( (Node) actionEvent.getSource());

    }

    public void onSequential(ActionEvent actionEvent) {
        if (playlist == null || playlist.getTracksList().isEmpty()) return;
        if (playlistService != null) playlistService.incrementPlayCount(playlist.getId());
        if (queueService != null && playerService != null) {
            queueService.clearQueue();
            queueService.setCurrentPlayMode(new SequentialMode());
            queueService.addToQueue(playlist);
            playerService.next();
            updateModeButtons(buttonSequential);
        }
    }

    public void onLoop(ActionEvent actionEvent) {
        if (playlist == null || playlist.getTracksList().isEmpty()) return;
        if (playlistService != null) playlistService.incrementPlayCount(playlist.getId());
        if (queueService != null && playerService != null) {
            queueService.clearQueue();
            queueService.setCurrentPlayMode(new LoopMode());
            queueService.addToQueue(playlist);
            playerService.next();
            updateModeButtons(buttonLoop);
        }
    }

    public void onShuffle(ActionEvent actionEvent) {

        if (playlist == null || playlist.getTracksList().isEmpty())
            return;
        if (playlistService != null)
            playlistService.incrementPlayCount(playlist.getId());

        if (queueService != null && playerService != null) {

            queueService.clearQueue();
            queueService.setCurrentPlayMode(new ShuffleMode());
            queueService.addToQueue(playlist);
            playerService.next();
            updateModeButtons(buttonShuffle);

        }
    }

    /*
    private void close(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }

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
                playlistService.deletePlaylist(playlist.getId());
            }
        });
    }

    @FXML
    public void onAddToQueue(ActionEvent actionEvent) {
        if(queueService !=null && playlist != null) {
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