package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.command.AddTrackToPlaylistCommand;
import it.diem.unisa.musicmanager.command.Command;
import it.diem.unisa.musicmanager.command.CommandManager;
import it.diem.unisa.musicmanager.command.RemoveTrackFromPlaylistCommand;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller per la finestra modale "Aggiungi/Rimuovi Tracce dalla Playlist" (addTrackPlaylist.fxml).
 * Gestisce l'interfaccia grafica che mostra l'elenco di tutte le tracce registrate nel sistema,
 * permettendo all'utente di spuntare o deselezionare quali brani includere nella playlist corrente.
 * All'atto del salvataggio, calcola le differenze (tracce da aggiungere e tracce da rimuovere)
 * e genera ed esegue i rispettivi comandi ({@link AddTrackToPlaylistCommand} e {@link RemoveTrackFromPlaylistCommand})
 * tramite il {@link CommandManager} per supportare la funzionalità di Undo.
 */
public class AddTrackPlaylisController {

    /**
     * La playlist a cui aggiungere o rimuovere le tracce.
     */
    private Playlist playlist;

    /**
     * Servizio per la gestione delle playlist.
     */
    private PlaylistService playlistService;

    /**
     * Servizio per il recupero di tutte le tracce presenti nel sistema.
     */
    private TrackService trackService;

    /**
     * Gestore per l'esecuzione dei comandi (necessario per supportare l'Undo/Redo).
     */
    private CommandManager commandManager;

    /**
     * Callback eseguito al termine dell'operazione di salvataggio (es. per aggiornare la vista chiamante).
     */
    private Runnable onSaveCallback;

    /**
     * Lista dei controller per ciascuna riga di traccia renderizzata.
     */
    private List<RowTrackController> rowControllers = new ArrayList<>();

    /**
     * Contenitore grafico verticale in cui inserire le righe delle tracce.
     */
    @FXML
    private VBox trackList;

    /**
     * Costruttore di default.
     */
    public AddTrackPlaylisController() {
        // Costruttore di default
    }

    /**
     * Imposta il callback da eseguire quando l'utente conferma e salva le modifiche.
     *
     * @param onSaveCallback Il callback {@link Runnable} da eseguire.
     */
    public void setOnSaveCallback(Runnable onSaveCallback) {
        this.onSaveCallback = onSaveCallback;
        // il runnbale è usatpsiisimmo oper le situazioni per cui devo aggironare u'interafccia quando è sttao modifcato quclsoa in un'altra pagina

    }

    /**
     * Imposta la playlist da modificare. Attiva il caricamento delle tracce se gli altri servizi sono pronti.
     * 
     * @param playlist La {@link Playlist} da configurare.
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        checkLoading();
    }

    /**
     * Imposta il servizio playlist. Attiva il caricamento delle tracce se gli altri servizi sono pronti.
     * 
     * @param playlistService Il {@link PlaylistService} da associare.
     */
    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
        checkLoading();
    }

    /**
     * Imposta il servizio tracce. Attiva il caricamento delle tracce se gli altri servizi sono pronti.
     * 
     * @param trackService Il {@link TrackService} da associare.
     */
    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
        checkLoading();
    }

    /**
     * Imposta il gestore per l'esecuzione dei comandi (Undo/Redo).
     * 
     * @param commandManager Il {@link CommandManager} da associare.
     */
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Controlla se tutte le dipendenze necessarie (playlist, playlistService, trackService)
     * sono state correttamente iniettate e, in caso positivo, avvia il caricamento grafico delle tracce.
     */
    private void checkLoading() {
        if (playlist != null && playlistService != null && trackService != null) {
            loadAllTracks();
        }
    }

    /**
     * Svuota la lista grafica, recupera tutte le tracce disponibili nel sistema ed inserisce
     * dinamicamente una riga grafica per ciascuna traccia (configurata in modalità selezione).
     */
    private void loadAllTracks() {
        trackList.getChildren().clear();
        rowControllers.clear();

        for (Track track : trackService.getAllTracks()) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));
                HBox row = loader.load();

                RowTrackController controller = loader.getController();
                controller.setTrack(track);

                boolean inPlaylist = playlist.containsTrack(track.getId());
                controller.setSelectionMode(true, inPlaylist);
                rowControllers.add(controller);
                trackList.getChildren().add(row);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Gestisce l'azione del pulsante "Annulla" (Cancel) chiudendo la finestra modale senza applicare modifiche.
     * 
     * @param event L'evento scatenato dal click del pulsante.
     */
    @FXML
    private void onCancel(javafx.event.ActionEvent event) {
        // Chiude la finestra corrente in modo sicuro e pulisce la mappa in WindowUtil
        it.diem.unisa.musicmanager.util.WindowUtil.close((javafx.scene.Node) event.getSource());
    }

    /**
     * Gestisce il click sul pulsante "Salva" (Save).
     * Analizza lo stato di spunta di ciascuna traccia visualizzata a schermo confrontandolo con lo stato
     * precedente nella playlist, eseguendo i comandi di aggiunta o rimozione traccia per i soli elementi variati.
     * Chiude infine la finestra modale ed esegue il callback di salvataggio registrato.
     *
     * @param actionEvent L'evento scatenato dal pulsante Salva.
     */
    @FXML
    public void onSave(ActionEvent actionEvent) {
        for (RowTrackController controller : rowControllers) {
            Track track = controller.getTrack();

            boolean wasInPlaylist = playlist.containsTrack(track.getId());
            boolean isSelectedNow = controller.isSelected();

            if (isSelectedNow && !wasInPlaylist) {
                Command cmd = new AddTrackToPlaylistCommand(
                        playlistService, playlist.getId(), track.getId(),
                        track.getTitle(), playlist.getName());        // ← due nomi in più
                commandManager.executeCommand(cmd);

            } else if (!isSelectedNow && wasInPlaylist) {
                Command cmd = new RemoveTrackFromPlaylistCommand(
                        playlistService, playlist.getId(), track.getId(),
                        track.getTitle(), playlist.getName());        // ← due nomi in più
                commandManager.executeCommand(cmd);
            }
        }
        WindowUtil.close((Node) actionEvent.getSource());
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
    }
}
