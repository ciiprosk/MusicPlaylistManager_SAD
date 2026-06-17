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

    /**
     * Inietta il service responsabile della gestione logica e persistenza delle playlist.
     * @param playlistService l'istanza del service delle playlist.
     */
    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    /**
     * Inietta il service responsabile della gestione delle tracce (necessario per le viste di dettaglio).
     * @param trackService l'istanza del service delle tracce.
     */
    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * Inietta il service responsabile della riproduzione audio.
     * Configura inoltre dei listener deboli (WeakChangeListener) sulle proprietà del player
     * per aggiornare lo stile grafico della card quando la playlist inizia o smette di suonare,
     * prevenendo memory leak nel caso in cui la card venga distrutta.
     *
     * @param playerService l'istanza del service di riproduzione.
     */
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        if (!isListenerAttached && playerService != null) {
            playerService.currentTrackProperty().addListener(new WeakChangeListener<>(playbackListener));
            playerService.isPlayingProperty().addListener(new WeakChangeListener<>(playbackListener));
            isListenerAttached = true;
        }
        updatePlayingStyle();
    }

    /**
     * Inietta il service responsabile della coda di ascolto.
     * @param queueService l'istanza del service della coda.
     */
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
        updatePlayingStyle();
    }

    /**
     * Inietta il gestore dei comandi per supportare le operazioni annullabili (Undo/Redo),
     * come l'eliminazione della playlist.
     * @param commandManager l'istanza del gestore dei comandi.
     */
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
        if (playlist == null || playerService == null) return;

        if (playlist.getTracksList().isEmpty()) {
            it.diem.unisa.musicmanager.util.AlertUtil.showInfo("Empty Playlist", "To play this playlist, you must first add a track.");
            return;
        }
        if (playlistService != null) {
            playlistService.incrementPlayCount(playlist.getId());
        }
        //l PlayerService fchiamatoooooo
        playerService.togglePlay(playlist);

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
                if (playlist.getTracksList().isEmpty()) {
                    it.diem.unisa.musicmanager.util.AlertUtil.showInfo(
                            "Empty Playlist", 
                            "To play this playlist, you must first add a track."
                    );
                    return;
                }
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

    /**
     * Apre la finestra per la modifica del nome della playlist.
     */
    private void openEditPlaylist() {
        if (playlistService == null) return;
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml", playlist.getName(), Modality.APPLICATION_MODAL);
            if (loader == null) {
                return;
            }
            EditPlaylistController ctrl = loader.getController();

            ctrl.setPlaylist(playlist);
            ctrl.setPlaylistService(playlistService);
                        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Apre la vista dettagliata della playlist, iniettando tutte le dipendenze necessarie
     * nel relativo controller per consentire la gestione completa (aggiunta/rimozione tracce).
     */
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

    /**
     * Gestisce la logica di eliminazione della playlist.
     * Chiede conferma all'utente tramite una finestra di dialogo (Alert) e, in caso positivo,
     * esegue l'operazione incapsulandola in un {@link DeletePlaylistCommand} per permettere l'annullamento.
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
     * Aggiorna dinamicamente lo stile CSS della card e l'icona del pulsante Play.
     * Verifica se il brano attualmente in esecuzione nel player appartiene alla playlist
     * associata a questa card. In caso affermativo, applica una classe CSS di evidenziazione
     * ("playlist-card-playing") e modifica l'icona del pulsante in "Pausa".
     */
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