package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.command.CommandManager;
import it.diem.unisa.musicmanager.command.DeleteTrackCommand;
import it.diem.unisa.musicmanager.command.RemoveTrackFromPlaylistCommand;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.QueueService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.AlertUtil;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;

import java.io.IOException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

/**
 * Controller per la singola riga di un brano.
 * Totalmente disaccoppiato dalla logica di riproduzione.
 */
public class RowTrackController {

    private Track track;
    private TrackService trackService;
    private PlayerService playerService;
    private QueueService queueService;
    private it.diem.unisa.musicmanager.service.PlaylistService playlistService;
    private CommandManager commandManager;
    private it.diem.unisa.musicmanager.model.Playlist parentPlaylist;

    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblPlayCount;
    @FXML private Label lblDuration;

    @FXML private Button buttonMenu;
    @FXML private Button btnModify;
    @FXML private Button btnPlay;

    @FXML private CheckBox checkkSelect;
    @FXML private HBox rootContainer;

    @FXML
    private HBox tagsContainer;

    private boolean isListenerAttached = false;

    /**
     * Alterna la visualizzazione della riga tra modalità standard e modalità selezione.
     * In modalità selezione nasconde i pulsanti di azione (Play, Modifica, Menu)
     * e mostra una CheckBox per la selezione multipla.
     *
     * @param isSelectionMode true per attivare la modalità selezione, false per la standard.
     * @param isAlreadyInPlaylist true se il brano è già presente nella playlist di destinazione (spunta la CheckBox).
     */
    public void setSelectionMode(boolean isSelectionMode, boolean isAlreadyInPlaylist) {
        checkkSelect.setVisible(isSelectionMode);
        checkkSelect.setManaged(isSelectionMode);
        checkkSelect.setSelected(isAlreadyInPlaylist);

        if (btnPlay != null) {
            btnPlay.setVisible(!isSelectionMode);
            btnPlay.setManaged(!isSelectionMode);
        }
        if (btnModify != null) {
            btnModify.setVisible(!isSelectionMode);
            btnModify.setManaged(!isSelectionMode);
        }
        if (buttonMenu != null) {
            buttonMenu.setVisible(!isSelectionMode);
            buttonMenu.setManaged(!isSelectionMode);
        }
    }

    /**
     * Inietta il gestore dei comandi per supportare operazioni annullabili (Undo/Redo).
     * @param commandManager l'istanza del CommandManager.
     */
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Verifica se la riga è attualmente spuntata nella modalità di selezione.
     * @return true se la CheckBox è selezionata, false altrimenti.
     */
    public boolean isSelected() {
        return checkkSelect.isSelected();
    }

    /**
     * Associa un brano a questa riga, popolando tutti gli elementi grafici
     * (titolo, autore, durata, ascolti e tag) con i dati forniti dal Model.
     * @param track l'istanza del brano da visualizzare.
     */
    public void setTrack(Track track) {
        this.track = track;

        lblTitle.setText(track.getTitle());
        lblAuthor.setText(track.getAuthor());

        int minutes = (int) (track.getSongLength() / 60);
        int seconds = (int) (track.getSongLength() % 60);
        lblDuration.setText(String.format("%02d:%02d", minutes, seconds));

        if (lblPlayCount != null) {
            lblPlayCount.setText(track.getPlayCount() + " Plays");
        }

        renderTags();
    }

    /**
     * Inietta il service responsabile della gestione delle tracce.
     * @param trackService l'istanza del service delle tracce.
     */
    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    private final ChangeListener<Track> currentTrackListener = (o, ov, nv) -> {
        updateCurrentTrackStyle();
        updateButtonState();
    };

    private final ChangeListener<Boolean> isPlayingListener = (o, ov, nv) -> {
        updateCurrentTrackStyle();
        updateButtonState();
    };

    /**
     * Inietta il service di riproduzione audio e aggancia dei WeakChangeListener
     * allo stato del player. L'uso di listener deboli garantisce che questa card
     * possa essere rimossa in sicurezza dalla memoria senza causare memory leak
     * quando la lista viene aggiornata.
     * @param playerService l'istanza del service di riproduzione.
     */
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;

        if (!isListenerAttached) {
            playerService.currentTrackProperty().addListener(new WeakChangeListener<>(currentTrackListener));
            playerService.isPlayingProperty().addListener(new WeakChangeListener<>(isPlayingListener));

            isListenerAttached = true;
        }

        updateCurrentTrackStyle();
        updateButtonState();
    }

    /**
     * Inietta il service responsabile della coda di ascolto.
     * @param queueService l'istanza del service della coda.
     */
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Inietta il service responsabile della gestione delle playlist.
     * @param playlistService l'istanza del service delle playlist.
     */
    public void setPlaylistService(it.diem.unisa.musicmanager.service.PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    /**
     * Imposta la playlist genitore se questa riga viene visualizzata all'interno
     * della schermata di dettaglio di una playlist. Questo cambia contestualmente
     * il comportamento del pulsante "Elimina" (rimozione dalla playlist vs rimozione dall'archivio).
     * @param parentPlaylist la playlist contenitrice, o null se in archivio globale.
     */
    public void setParentPlaylist(it.diem.unisa.musicmanager.model.Playlist parentPlaylist) {
        this.parentPlaylist = parentPlaylist;
    }

    /**
     * Restituisce l'istanza del brano associato a questa riga.
     * @return la traccia associata.
     */
    public Track getTrack() {
        return track;
    }

    /**
     * Aggiorna graficamente l'icona del pulsante basandosi esclusivamente sullo stato del Service.
     */
    private void updateButtonState() {
        Track currentTrack = playerService != null
                ? playerService.currentTrackProperty().get()
                : null;

        boolean isCurrentAndPlaying = track != null
                && currentTrack != null
                && track.getId().equals(currentTrack.getId())
                && playerService.isPlayingProperty().get();

        btnPlay.setText(isCurrentAndPlaying ? "⏸" : "▶");
    }

    /**
     * L'azione di riproduzione ora è una semplice delega diretta (sottilissima!)
     */
    @FXML
    public void handlePlay(ActionEvent actionEvent) {
        if (playerService != null && track != null) {
            Track currentTrack = playerService.currentTrackProperty().get();
            boolean wasAlreadyLoaded = currentTrack != null && track.getId().equals(currentTrack.getId());

            if (wasAlreadyLoaded) {
                playerService.togglePlay(track);
            } else if (parentPlaylist != null && queueService != null) {
                it.diem.unisa.musicmanager.model.QueueItem item = queueService.queuePlaylistFromTrack(parentPlaylist, track);
                if (item != null) {
                    playerService.play(track, false, true);
                    if (playlistService != null) {
                        playlistService.incrementPlayCount(parentPlaylist.getId());
                    }
                }
            } else {
                playerService.togglePlay(track);
            }
        }
    }

    /**
     * Apre la schermata modale per la modifica dei metadati della traccia.
     * @param actionEvent l'evento generato dal click.
     */
    @FXML
    public void handleModify(ActionEvent actionEvent) {
        openEditTrack();
    }

    /**
     * Gestisce l'eliminazione della traccia.
     * Riconosce dinamicamente il contesto: se la riga si trova dentro una playlist,
     * il comando rimuoverà il brano solo dalla playlist (RemoveTrackFromPlaylistCommand).
     * Se si trova nell'archivio globale, eliminerà fisicamente la traccia (DeleteTrackCommand).
     * @param actionEvent l'evento generato dal click.
     */
    @FXML
    public void handleDelete(ActionEvent actionEvent) {
        if (track == null) return;
        boolean isConfirmed = AlertUtil.showConfirmation("Confirm Delete", "Are you sure you want to delete this track?");
        if (!isConfirmed) return;
        if (parentPlaylist != null) {
            // contesto playlist --> rimuove dalla playlist
            commandManager.executeCommand(
                    new RemoveTrackFromPlaylistCommand(
                            playlistService, parentPlaylist.getId(), track.getId(),
                            track.getTitle(), parentPlaylist.getName()
                    )
            );
        } else {
            // contesto archivio --> elimina dall'archivio
            commandManager.executeCommand(
                    new DeleteTrackCommand(trackService, playlistService, track.getId())
            );
        }

    }

    /**
     * Apre il menu contestuale della riga (tre puntini), offrendo azioni secondarie
     * come il dettaglio, la modifica, l'eliminazione e l'aggiunta alla coda.
     * @param actionEvent l'evento generato dal click.
     */
    @FXML
    public void handleMenu(ActionEvent actionEvent) {
        if (track == null) return;

        ContextMenu menu = new ContextMenu();
        MenuItem detailItem = new MenuItem("Open Detail");
        detailItem.setOnAction(e -> openDetail());

        MenuItem modifyItem = new MenuItem("Modify Track");
        modifyItem.setOnAction(e -> openEditTrack());

        String deleteText = parentPlaylist != null ? "Remove from Playlist" : "Delete Track";
        MenuItem deleteItem = new MenuItem(deleteText);
        deleteItem.setOnAction(e -> handleDelete(null));

        MenuItem addQueueItem = new MenuItem("Add to Queue");
        addQueueItem.setOnAction(e -> {
            onAddQueue(e);
        });

        menu.getItems().addAll(detailItem, modifyItem, deleteItem, addQueueItem);
        menu.show(buttonMenu, Side.BOTTOM, 0, 0);
    }

    /**
     * Helper per l'apertura modale dei dettagli della traccia.
     */
    private void openDetail() {
        try {
            FXMLLoader loader = WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/detailSong.fxml",
                    track.getTitle(),
                    Modality.WINDOW_MODAL
            );


            if (loader == null) {
                return; // La finestra era già aperta, ci fermiamo senza crashare!
            }

            DetailSongController controller = loader.getController();
            controller.setTrackService(trackService);
            controller.setTrack(track);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper per l'apertura modale della modifica della traccia.
     */
    private void openEditTrack() {
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/editSong.fxml", "Modify Track", Modality.WINDOW_MODAL);
            // Controllo anti-crash: se la finestra di questa specifica traccia è già aperta, ci fermiamo
            if (loader == null) {
                return;
            }
            EditSongController controller = loader.getController();
            controller.setTrackService(trackService);
            controller.setTrack(track);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aggiorna lo stile CSS dell'intero contenitore della riga.
     * Se questo specifico brano è in riproduzione, applica la classe "brano-row-playing"
     * per evidenziarlo visivamente.
     */
    private void updateCurrentTrackStyle() {
        rootContainer.getStyleClass().remove("brano-row-playing");

        Track currentTrack = playerService != null
                ? playerService.currentTrackProperty().get()
                : null;

        boolean isCurrentAndPlaying = track != null
                && currentTrack != null
                && track.getId().equals(currentTrack.getId())
                && playerService.isPlayingProperty().get();

        if (isCurrentAndPlaying) {
            rootContainer.getStyleClass().add("brano-row-playing");
        }
    }

    /**
     * Svuota e renderizza dinamicamente all'interno dell'HBox i badge grafici
     * relativi ai Tag associati al brano (es. EXPLICIT, FAVOURITE, NEWRELEASE).
     */
    private void renderTags() {
        if (tagsContainer == null) return;

        tagsContainer.getChildren().clear();

        for (Tag tag : track.getTags()) {
            tagsContainer.getChildren().add(createTag(tag));
        }
    }

    /**
     * Crea un componente visivo (Label stilizzata) per un singolo tag.
     * @param tag l'enumerativo del tag da graficare.
     * @return il nodo Label formattato con classe CSS appropriata.
     */
    private Label createTag(Tag tag) {
        Label label = new Label();
        label.getStyleClass().add("tag");

        switch (tag) {
            case EXPLICIT -> {
                label.setText("E");
                label.getStyleClass().add("tag-explicit");
            }
            case FAVOURITE -> {
                label.setText("♥");
                label.getStyleClass().add("tag-favourite");
            }
            case NEWRELEASE -> {
                label.setText("NEW");
                label.getStyleClass().add("tag-newrelease");
            }
        }

        return label;
    }

    /**
     * Aggiunge la traccia alla coda di ascolto.
     * Se la coda è attualmente vuota e non vi è alcuna riproduzione attiva, forza
     * l'avvio immediato del brano appena accodato. Altrimenti notifica l'aggiunta.
     * @param actionEvent l'evento generato dal click sull'opzione.
     */
    private void onAddQueue(ActionEvent actionEvent) {
        if(queueService !=null && track != null) {
            //vedio se la coda d ascolto è vuota
            boolean isEmpty = queueService.getQueueList().isEmpty();

            //vedo se c'è un bran oin rispodzione
            boolean isPlayingTrack = playerService.currentTrackProperty().get() != null;

            //aggingo alla coda

            queueService.addToQueue(track);

            if(isEmpty && !isPlayingTrack){// se è vuota e nessuna raccia sta suonando allora osso far partire wuesta
                playerService.next();

            }else{
                AlertUtil.showInfo("Queue Updated", "Track '" + track.getTitle() + "' added to queue!");
            }
        }
    }
}