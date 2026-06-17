package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.QueueService;
import it.diem.unisa.musicmanager.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import it.diem.unisa.musicmanager.command.CommandManager;
import javafx.scene.control.Button;

/**
 * Controller principale della GUI dell'applicazione MusicPlaylistManager.
 * Funge da orchestratore centrale dell'interfaccia grafica: gestisce la barra laterale di navigazione
 * (sidebar) per scambiare le pagine interne (Home, Tracce, Playlist), ospita i riferimenti ai
 * sotto-controller caricati e si occupa ddi iniettare i servizi applicativi
 * necessari nei rispettivi sotto-moduli. Gestisce inoltre l'apertura di finestre esterne come la coda,
 * e la richiesta globale di Undo delle azioni utente.
 */
public class MainController {

    /**
     * Nodo dell'interfaccia grafica corrispondente alla pagina Home.
     */
    @FXML private Node homePage;

    /**
     * Nodo dell'interfaccia grafica corrispondente alla pagina di gestione delle tracce.
     */
    @FXML private Node tracksPage;

    /**
     * Nodo dell'interfaccia grafica corrispondente alla pagina di gestione delle playlist.
     */
    @FXML private Node playlistsPage;

    /**
     * Bottone del menu laterale per accedere alla pagina Home.
     */
    @FXML private Button btnHome;

    /**
     * Bottone del menu laterale per accedere alla pagina delle tracce.
     */
    @FXML private Button btnTracks;

    /**
     * Bottone del menu laterale per accedere alla pagina delle playlist.
     */
    @FXML private Button btnPlaylists;

    /**
     * Sotto-controller che gestisce la pagina delle playlist.
     */
    @FXML private PlaylistController playlistsPageController;

    /**
     * Sotto-controller che gestisce la pagina delle tracce.
     */
    @FXML private TracksController tracksPageController;

    /**
     * Sotto-controller che gestisce il lettore musicale (player bar in basso).
     */
    @FXML private PlayerController playerController;

    /**
     * Sotto-controller che gestisce la pagina iniziale Home (statistiche e top tracks/playlists).
     */
    @FXML private HomeController homePageController;

    /**
     * Riferimento al servizio per la gestione della coda di riproduzione.
     */
    private QueueService queueService;

    /**
     * Riferimento al servizio per la gestione delle playlist.
     */
    private PlaylistService playlistService;

    /**
     * Riferimento al servizio per il controllo della riproduzione audio.
     */
    private PlayerService playerService;

    /**
     * Riferimento al gestore dei comandi per il supporto a Undo/Redo.
     */
    private CommandManager commandManager;

    /**
     * Metodo di inizializzazione richiamato automaticamente da JavaFX dopo il caricamento del file FXML.
     * Imposta la pagina iniziale dell'applicazione sulla pagina Home e attiva il rispettivo pulsante.
     */
    @FXML
    public void initialize() {
        showPage(homePage);
        setActiveMenu(btnHome);
    }

    /**
     * Gestisce l'azione di apertura della pagina Home.
     * Ricarica le classifiche delle tracce e playlist più ascoltate e mostra la vista corrispondente.
     */
    @FXML
    private void openHome() {
        if (homePageController != null) {
            homePageController.loadTopTracks();
            homePageController.loadTopPlaylists();
        }

        showPage(homePage);
        setActiveMenu(btnHome);
    }

    /**
     * Gestisce l'azione di apertura della pagina delle Tracce.
     * Mostra la vista corrispondente aggiornando la selezione del menu.
     */
    @FXML
    private void openTracks() {
        showPage(tracksPage);
        setActiveMenu(btnTracks);
    }

    /**
     * Gestisce l'azione di apertura della pagina delle Playlist.
     * Mostra la vista corrispondente aggiornando la selezione del menu.
     */
    @FXML
    private void openPlaylists() {
        showPage(playlistsPage);
        setActiveMenu(btnPlaylists);
    }

    /**
     * Nasconde tutti i pannelli e rende visibile in primo piano solo la pagina specificata.
     * 
     * @param pageToShow Il nodo grafico ({@link Node}) corrispondente alla pagina da visualizzare.
     */
    private void showPage(Node pageToShow) {
        if (homePage != null) homePage.setVisible(false);
        if (tracksPage != null) tracksPage.setVisible(false);
        if (playlistsPage != null) playlistsPage.setVisible(false);

        if (pageToShow != null) {
            pageToShow.setVisible(true);
            pageToShow.toFront();
        }
    }

    /**
     * Evidenzia il pulsante del menu laterale attualmente attivo e rimuove lo stile grafico dai restanti.
     * 
     * @param active Il pulsante ({@link Button}) del menu che deve essere evidenziato.
     */
    private void setActiveMenu(Button active) {
        btnHome.getStyleClass().remove("btn-menu-active");
        btnTracks.getStyleClass().remove("btn-menu-active");
        btnPlaylists.getStyleClass().remove("btn-menu-active");
        if (!active.getStyleClass().contains("btn-menu-active")) {
            active.getStyleClass().add("btn-menu-active");
        }
    }

    /**
     * Restituisce il controller secondario associato alla pagina delle playlist.
     * 
     * @return Il {@link PlaylistController} associato.
     */
    public PlaylistController getPlaylistsPageController() {
        return playlistsPageController;
    }

    /**
     * Imposta il servizio di gestione della coda.
     * 
     * @param queueService Il servizio {@link QueueService} da associare.
     */
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Imposta il servizio di riproduzione dei brani.
     * 
     * @param playerService Il servizio {@link PlayerService} da associare.
     */
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Imposta il servizio di gestione delle playlist.
     * 
     * @param playlistService Il servizio {@link PlaylistService} da associare.
     */
    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    /**
     * Imposta il gestore per l'esecuzione e l'annullamento dei comandi.
     * 
     * @param commandManager Il gestore {@link CommandManager} da associare.
     */
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Restituisce il controller secondario associato alla pagina delle tracce.
     * 
     * @return Il {@link TracksController} associato.
     */
    public TracksController getTracksPageController() {
        return tracksPageController;
    }

    /**
     * Restituisce il controller secondario associato al lettore musicale (player bar).
     * 
     * @return Il {@link PlayerController} associato.
     */
    public PlayerController getPlayerController() { 
        return playerController;
    }

    /**
     * Restituisce il controller secondario associato alla pagina iniziale Home.
     * 
     * @return L'{@link HomeController} associato.
     */
    public HomeController getHomePageController() {
        return homePageController;
    }

    /**
     * Gestisce l'azione globale di annullamento (Undo) richiamata dall'interfaccia grafica.
     * Se ci sono azioni da annullare nel {@link CommandManager}, richiede la conferma all'utente
     * tramite una finestra di dialogo ed esegue l'operazione di ripristino dello stato precedente.
     */
    @FXML
    private void onUndo() {
        if (commandManager == null || !commandManager.canUndo()) {
            AlertUtil.showInfo("Nothing to undo", "There are no actions to undo.");
            return;
        }

        String descrizione = commandManager.peekUndoDescription().orElse("the last action");

        boolean conferma = AlertUtil.showConfirmation(
                "Undo",
                "Do you want to undo: " + descrizione + "?"
        );

        if (conferma) {
            commandManager.undo();
        }
    }
}
