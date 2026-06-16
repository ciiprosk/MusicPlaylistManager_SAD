package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.QueueService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import it.diem.unisa.musicmanager.command.CommandManager;
import it.diem.unisa.musicmanager.command.Command;
import it.diem.unisa.musicmanager.command.DeletePlaylistCommand;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import it.diem.unisa.musicmanager.model.QueueItem;



/**
 * Controller di una card playlist (playlistCard.fxml).
 * Mostra nome e numero di tracce, e offre le azioni Play / Modifica / Menu.
 * La playlist viene passata tramite {@link #setPlaylist(Playlist)} prima
 * di mostrare la card.
 */
public class PlaylistCardController {

    private PlaylistService playlistService; //lo ricevo dal qyeelo che mi chiama
    private PlayerService playerService;
    private TrackService trackService;
    private QueueService queueService;


    @FXML
    private Label labelName;
    @FXML
    private Label labelTracks;
    @FXML
    private Label labelPlayCount;
    @FXML
    private Button btnPlay;
    @FXML
    private Button btnModify;
    @FXML
    private Button btnMenu;
    @FXML
    private VBox rootCard;

    // La playlist mostrata da questa card.
    private Playlist playlist;
    private CommandManager commandManager;
    private boolean isListenerAttached = false;

    private final ChangeListener<Object> playbackListener = (o, ov, nv) -> updatePlayingStyle();

    /**
     * Imposta la playlist della card e riempie le etichette.
     *
     * @param playlist la playlist da mostrare
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        labelName.setText(playlist.getName());
        int n = playlist.getTracksList().size();
        labelTracks.setText(n + (n == 1 ? " Track" : " Tracks"));
        if (labelPlayCount != null) {
            labelPlayCount.setText(playlist.getPlayCount() + (playlist.getPlayCount() == 1 ? " Play" : " Plays"));
        }
        updatePlayingStyle();
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        if (!isListenerAttached && playerService != null) {
            playerService.currentTrackProperty().addListener(new WeakChangeListener<>(playbackListener));
            playerService.isPlayingProperty().addListener(new WeakChangeListener<>(playbackListener));
            isListenerAttached = true;
        }
        updatePlayingStyle();
    }

    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
        updatePlayingStyle();
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }


    /**
     * Click su Play: avvia la riproduzione della playlist.
     * (Da collegare al PlayerService quando disponibile.)
     */
    /**
     * Click su Play: avvia immediatamente la riproduzione della playlist corrente,
     * interrompendo qualsiasi altra playlist o brano in esecuzione.
     */
    @FXML
    private void handlePlay() {
        // Controllo di sicurezza per evitare crash se i dati non sono pronti
        if (playlist == null || playlist.getTracksList().isEmpty() || playerService == null) {
            return;
        }

        // Se sto già suonando QUESTA playlist (stesso belongsToPlaylist nel currentItem),
        // non riparto da capo: faccio solo toggle pausa/play sulla traccia corrente.
        QueueItem current = queueService != null ? queueService.getCurrentItem() : null;
        boolean thisPlaylistIsCurrent =
                current != null && playlist.getId().equals(current.getBelongsToPlaylist());

        if (thisPlaylistIsCurrent) {
            // Sto già suonando questa playlist: pausa o resume SENZA toccare la coda
            if (playerService.isPlayingProperty().get()) {
                playerService.pause();
            } else {
                playerService.resume();
            }
            return;
        }

        // Recuperiamo la prima traccia della playlist che vogliamo ascoltare
        Track firstTrack = playlist.getTracksList().get(0);

        // Incrementiamo il contatore dei "Plays" della playlist tramite il service
        if (playlistService != null) {
            playlistService.incrementPlayCount(playlist.getId());
        }

        if (queueService != null) {
            // 1. Svuotiamo completamente la vecchia coda d'ascolto attiva
            queueService.clearQueue();

            // 2. Aggiungiamo l'intera nuova playlist alla coda
            queueService.addToQueue(playlist);

            // 3. AGGIORNAMENTO CRUCIALE: Forziamo il player a suonare DIRETTAMENTE la prima traccia.
            // Passando true come terzo parametro (forceRestart), indichiamo al PlayerService di
            // stoppare immediatamente la vecchia canzone e avviare la nuova senza esitazioni.
            //playerService.play(firstTrack, false, true);

            // 4. Allineiamo la coda indicando qual è il brano attualmente in riproduzione
            //queueService.setCurrentItem(queueService.getQueueList().isEmpty() ? null : queueService.getQueueList().get(0));
            playerService.next();
        } else {
            // Fallback di sicurezza nel caso in cui il QueueService non fosse iniettato
            //playerService.play(firstTrack, false, true);
            playerService.play(playlist.getTracksList().get(0), false, true);
        }
    }

    /**
     * Click su Modifica: apre la finestra di modifica della playlist (editPlaylist).
     */
    @FXML
    private void handleModify() {
        try {
            // Usiamo un titolo specifico per la playlist corrente
            FXMLLoader loader = WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/editPlaylist.fxml",
                    "Modify: " + playlist.getName(),
                    Modality.APPLICATION_MODAL
            );

            // Controllo anti-crash se la finestra è già aperta
            if (loader == null) return;

            EditPlaylistController controller = loader.getController();
            controller.setPlaylist(playlist);
            controller.setPlaylistService(playlistService);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Click su Menu: apre le altre azioni della playlist.
     */
    @FXML
    private void handleMenu() {
        if (playlist == null) return;

        ContextMenu menu = new ContextMenu();

        MenuItem detailItem = new MenuItem("Open Detail");
        detailItem.setOnAction(e -> openDetail());

        MenuItem modifyItem = new MenuItem("Modify Name");
        modifyItem.setOnAction(e -> openEditPlaylist());

        MenuItem deleteItem = new MenuItem("Delete playlist");
        deleteItem.setOnAction(e -> deletePlaylist());

        MenuItem addQueueItem = new MenuItem("Add to Queue");
        addQueueItem.setOnAction(e -> {
            if (queueService != null && playlist != null) {
                // 1. Controlla lo stato della coda e del player PRIMA di aggiungere
                boolean isEmpty = queueService.getQueueList().isEmpty();
                boolean isPlayingTrack = playerService != null && playerService.currentTrackProperty().get() != null;
                // 2. Aggiunge la playlist alla coda
                queueService.addToQueue(playlist);
                // 3. Se la coda era vuota e non c'era riproduzione attiva, avvia automaticamente
                if (isEmpty && !isPlayingTrack && playerService != null) {
                    playerService.next();
                } else {
                    it.diem.unisa.musicmanager.util.AlertUtil.showInfo(
                            "Queue Updated",
                            "Playlist '" + playlist.getName() + "' has been added to the playback queue!"
                    );
                }
            }
        });

        menu.getItems().addAll(detailItem, modifyItem, deleteItem, addQueueItem);  // ← addQueueItem ora c'è

        menu.show(btnMenu, Side.BOTTOM, 0, 0);


        // Aggancia lo stylesheet al popup DOPO lo show (prima getScene() è null).
        // Il guard evita il crash se il path non viene trovato: in quel caso il menu
        // si apre comunque, solo senza stile.
        var cssUrl = getClass().getResource("/it.diem.unisa/css/style.css");
        if (cssUrl != null) {
            menu.getScene().getStylesheets().add(cssUrl.toExternalForm());
        }

    }

    private void openEditPlaylist() {
        if (playlistService == null) return;
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml", playlist.getName(), Modality.APPLICATION_MODAL);
            if (loader == null) {
                return;
            }
            EditPlaylistController ctrl = loader.getController();

            //
            //ctrl.setTrackService(trackService);
            ctrl.setPlaylist(playlist);
            ctrl.setPlaylistService(playlistService);
            //ctrl.setPlayerService(playerService); non serve


            /*
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml"));
            Parent root = loader.load();

            EditPlaylistController ctrl = loader.getController();
            ctrl.setPlaylist(playlist);
            ctrl.setPlaylistService(playlistService);

            Stage stage = new Stage();
            stage.setTitle("Modify Playlist");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDetail() {
        try {
            FXMLLoader loader = WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/detailedPlaylist.fxml",
                    playlist.getName(),
                    Modality.NONE
            );

            // Controllo anti-crash se la finestra di dettaglio di questa playlist è già aperta
            if (loader == null) return;

            DetailedPlaylistController ctrl = loader.getController();
            ctrl.setPlaylistService(playlistService);
            ctrl.setTrackService(trackService);
            ctrl.setPlayerService(playerService);
            ctrl.setQueueService(queueService);
            ctrl.setPlaylist(playlist);
            ctrl.setCommandManager(commandManager);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                Command cmd = new DeletePlaylistCommand(playlistService, playlist.getId());
                commandManager.executeCommand(cmd);

            }
        });
    }

    private void openAddTrackToPlaylist() {
        //DA FAREE
    }

    private void updatePlayingStyle() {
        if (rootCard == null) return;
        rootCard.getStyleClass().remove("playlist-card-playing");

        boolean thisPlaylistIsPlaying = false;

        if (playlist != null && queueService != null && playerService != null) {
            QueueItem current = queueService.getCurrentItem();

            thisPlaylistIsPlaying =
                    current != null
                            && playlist.getId().equals(current.getBelongsToPlaylist())   // ← QUESTA playlist, non un'altra
                            && playerService.isPlayingProperty().get();
        }

        if (thisPlaylistIsPlaying) {
            rootCard.getStyleClass().add("playlist-card-playing");
        }

        if (btnPlay != null) {
            btnPlay.setText(thisPlaylistIsPlaying ? "⏸" : "▶");
        }
    }
}