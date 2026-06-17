package it.diem.unisa.musicmanager.controller;


import it.diem.unisa.musicmanager.command.CommandManager;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.QueueService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.scene.control.Label;
import java.io.IOException;

/**
 * Controller per la schermata principale dell'archivio tracce.
 * Gestisce la visualizzazione dinamica dell'elenco dei brani musicali,
 * fornendo funzionalità di ricerca in tempo reale (filtro testuale) e l'accesso
 * alle interfacce per l'aggiunta di nuovi brani o la visualizzazione dei dettagli.
 * Sfrutta il pattern Observer per ricaricare automaticamente la UI ogni volta
 * che la lista delle tracce nel Model subisce una modifica.
 */
public class TracksController {


    @FXML
    private ListView<Track> tracksList;
    @FXML
    private TextField searchBar;
    @FXML
    private Button btnClearSearch;

    private TrackService trackService;
    private PlayerService playerService;
    private PlaylistService playlistService;
    private QueueService queueService;
    private boolean isListenerAttached = false;
    private CommandManager commandManager;

    @FXML private VBox trackList;

    /**
     * Inietta il service responsabile della gestione delle tracce (Model).
     * All'iniezione, aggancia automaticamente un listener reattivo alla lista
     * delle tracce per mantenere la View costantemente sincronizzata.
     * * @param trackService l'istanza del service delle tracce.
     */
    public void setTrackService(TrackService trackService) {

        this.trackService = trackService;
        createTrackListener();
    }

    /**
     * Inietta il service responsabile della gestione delle tracce (Model).
     * All'iniezione, aggancia automaticamente un listener reattivo alla lista
     * delle tracce per mantenere la View costantemente sincronizzata.
     * * @param trackService l'istanza del service delle tracce.
     */
    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    /**
     * Inietta il service responsabile della gestione della coda di ascolto.
     * * @param queueService l'istanza del service della coda.
     */
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Inietta il service responsabile della riproduzione audio.
     * * @param playerService l'istanza del service di riproduzione.
     */
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Inietta il gestore dei comandi (Command Pattern) per supportare operazioni
     * annullabili/ripetibili (Undo/Redo).
     * * @param commandManager l'istanza del gestore dei comandi.
     */
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Gestisce l'evento di click sul pulsante per l'aggiunta di una nuova traccia.
     * Apre la finestra modale dedicata all'inserimento dei dati del nuovo brano.
     * * @param actionEvent l'evento generato dal click sul bottone.
     * @throws IOException se il caricamento del file FXML fallisce.
     */
    public void handleAdd(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/addSong.fxml", "Add Track",Modality.WINDOW_MODAL);
        if (loader == null) {
            return;
        }
        AddSongController controller = loader.getController();
        controller.setTrackService(trackService);
        controller.setCommandManager(commandManager);
    }


    /**
     * Inizializzazione standard di JavaFX, chiamata automaticamente dopo il caricamento dell'FXML.
     * Configura il listener sulla barra di ricerca per eseguire il filtraggio testuale in tempo reale
     * e gestisce la visibilità del pulsante per svuotare la ricerca.
     */
    @FXML
    public void initialize() {
        if (searchBar != null) {
            searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                if (btnClearSearch != null) {
                    btnClearSearch.setVisible(!newValue.isEmpty());
                }
                try {
                    loadTracks();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        if (btnClearSearch != null) {
            btnClearSearch.setOnAction(e -> searchBar.clear());
        }
    }

    /**
     * Carica e renderizza la lista delle tracce all'interno del contenitore VBox (trackList).
     * Applica eventuali filtri testuali provenienti dalla barra di ricerca.
     * Gestisce la User Experience in caso di lista vuota, mostrando messaggi contestuali
     * (archivio completamente vuoto vs. nessun risultato trovato per la ricerca corrente).
     * * @throws IOException se il caricamento dei componenti grafici delle singole righe fallisce.
     */
    public void loadTracks() throws IOException {
        trackList.getChildren().clear();

        String keyword = searchBar != null ? searchBar.getText() : "";
        java.util.List<Track> tracksToShow = trackService.searchTracks(keyword);

        for (Track track : tracksToShow) {
            trackList.getChildren().add(createTrackRow(track));
        }

        // Archivio vuoto: invitiamo l'utente ad aggiungere un brano o avvisiamo che la ricerca non ha prodotto risultati.
        if (trackList.getChildren().isEmpty()) {
            Label emptyLabel;
            if (keyword != null && !keyword.isBlank()) {
                emptyLabel = new Label("No tracks found for '" + keyword + "'.");
            } else {
                emptyLabel = new Label("Your library is empty. Add a track!");
            }
            emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");  // bianco e leggermente piu' grande
            trackList.getChildren().add(emptyLabel);
        }
    }

    /**
     * Genera dinamicamente il componente grafico (Node) per una singola riga della lista tracce.
     * Inietta tutte le dipendenze necessarie nel controller della singola riga per permettere
     * l'interazione (Play, aggiunta alla coda, eliminazione, ecc.).
     * * @param track la traccia da visualizzare nella riga.
     * @return il nodo grafico JavaFX configurato.
     * @throws IOException se il file FXML `trackRow.fxml` non viene trovato o è corrotto.
     */
    private Node createTrackRow(Track track) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));


        Node card = loader.load();
        RowTrackController controller = loader.getController();
        controller.setTrack(track); //gli passo la track ddi cui creare la row
        controller.setTrackService(trackService); //i serviceeeee
        controller.setPlayerService(playerService);
        controller.setQueueService(queueService);
        controller.setCommandManager(commandManager);
        controller.setPlaylistService(playlistService);
        return card;
    }

    /**
     * Registra un InvalidationListener sulla lista osservabile del TrackService.
     * Questo garantisce che ogni volta che una traccia viene aggiunta, rimossa o modificata
     * altrove nell'applicazione, la UI si aggiorni automaticamente ricaricando i componenti grafici.
     */
    private void createTrackListener() {
        if (!isListenerAttached) {
            trackService.getAllTracks().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable obs) {
                    try {
                        loadTracks();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            isListenerAttached = true;
        }
    }




}