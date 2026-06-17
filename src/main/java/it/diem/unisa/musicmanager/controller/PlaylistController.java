package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import it.diem.unisa.musicmanager.command.CommandManager;

import java.io.IOException;

/**
 * Controller della schermata "Playlists".
 * Mostra le playlist come griglia di card e permette di crearne di nuove
 * aprendo la finestra "Crea Playlist".
 */
public class PlaylistController {

    @FXML private TextField searchBar;
    @FXML private javafx.scene.control.Button btnClearSearch;
    @FXML private FlowPane playlistsGrid;

    private PlaylistService playlistService;
    private PlayerService playerService;
    private TrackService trackService;
    private QueueService queueService;
    private boolean isListenerAttached = false;
    private CommandManager commandManager;

    /**
     * Metodo di inizializzazione chiamato automaticamente da JavaFX al termine del caricamento dell'FXML.
     * Configura il listener reattivo sulla barra di ricerca: ogni volta che l'utente digita un carattere,
     * l'elenco delle playlist viene filtrato in tempo reale. Gestisce inoltre la visibilità
     * del pulsante per svuotare rapidamente il campo di ricerca.
     * * @throws IOException se si verifica un errore durante il primo caricamento delle card.
     */
    @FXML
    public void initialize() throws IOException {
        if (searchBar != null) {
            searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                if (btnClearSearch != null) {
                    btnClearSearch.setVisible(!newValue.isEmpty());
                }
                try {
                    loadPlaylists();
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
     * Apre la finestra modale "Crea Playlist".
     * Per ora apre solo la finestra (senza service).
     *
     * @param actionEvent evento generato dal click sul pulsante "Add Playlist"
     */
    @FXML
    private void handleAdd(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/addPlaylist.fxml", "Add Playlist", Modality.APPLICATION_MODAL);
            if (loader == null) {
                return;
            }

            // gli passiamo il service qui
            AddPlaylistController controller = loader.getController();
            controller.setPlaylistService(playlistService);
            controller.setCommandManager(commandManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Inietta il service responsabile della gestione logica e persistenza delle playlist.
     * Al momento dell'iniezione, aggancia un `InvalidationListener` alla lista osservabile del Model
     * per garantire che la griglia venga ridisegnata automaticamente in caso di modifiche esterne.
     * L'uso del flag `isListenerAttached` previene memory leak dovuti a registrazioni multiple.
     * * @param playlistService l'istanza del service delle playlist.
     */
    public void setPlaylistService(PlaylistService playlistService) {

        this.playlistService = playlistService;

        //bisgna iniizliazre il listener UNA SOLA VOLTAAA
        if(!isListenerAttached){
            playlistService.getPlaylists().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable obs) {
                    try {
                        loadPlaylists();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            isListenerAttached = true;
        }
        checkReadyAndLoad();
    }

    /**
     * Inietta il service responsabile della riproduzione audio.
     * @param playerService l'istanza del service di riproduzione.
     */
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        checkReadyAndLoad();
    }

    /**
     * Inietta il service responsabile della gestione delle tracce.
     * @param trackService l'istanza del service delle tracce.
     */
    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
        checkReadyAndLoad();
    }

    /**
     * Inietta il service responsabile della coda di riproduzione.
     * @param queueService l'istanza del service della coda.
     */
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }


    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
        checkReadyAndLoad();
    }

    /**
     * Carica le playlist dal service e le mostra come card.
     * @throws IOException
     */
    public void loadPlaylists() throws IOException {
        playlistsGrid.getChildren().clear();

        String keyword = searchBar != null ? searchBar.getText() : "";
        java.util.List<Playlist> playlistsToShow = playlistService.searchPlaylists(keyword);

        for(Playlist p : playlistsToShow){
            playlistsGrid.getChildren().add(createPlaylistCard(p));
        }

        if (playlistsGrid.getChildren().isEmpty()) {
            javafx.scene.control.Label emptyLabel;
            if (keyword != null && !keyword.isBlank()) {
                emptyLabel = new javafx.scene.control.Label("No playlists found for '" + keyword + "'.");
            } else {
                emptyLabel = new javafx.scene.control.Label("Your library is empty. Add a playlist!");
            }
            emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            playlistsGrid.getChildren().add(emptyLabel);
        }
    }

    /**
     * Crea un card per una playlist.
     * @param playlist
     * @return
     * @throws IOException
     */
    private Node createPlaylistCard(Playlist playlist) throws IOException {


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/playlistCard.fxml"));


        Node card = loader.load();
        PlaylistCardController controller = loader.getController();

        controller.setTrackService(trackService);
        controller.setPlaylistService(playlistService); //i serviceeeee
        controller.setPlayerService(playerService);
        controller.setQueueService(queueService);
        controller.setPlaylist(playlist); //gli passo la playlist ddi cui creare la card
        controller.setCommandManager(commandManager);

        return card;
    }

    /**
     * Metodo di utilità per garantire che la griglia venga renderizzata solo quando
     * tutte le dipendenze essenziali sono state correttamente iniettate dall'applicazione principale.
     * Evita fastidiosi NullPointerException durante la fase di caricamento.
     */
    private void checkReadyAndLoad() {
        if (playlistService != null && trackService != null && playerService != null) {
            try {
                loadPlaylists();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}
