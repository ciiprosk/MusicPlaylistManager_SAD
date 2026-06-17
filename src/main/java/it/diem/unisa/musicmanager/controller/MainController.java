package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import it.diem.unisa.musicmanager.command.CommandManager;
import javafx.scene.control.Button;

public class MainController {


    @FXML private Node homePage;
    @FXML private Node tracksPage;
    @FXML private Node playlistsPage;
    @FXML private Button btnHome;
    @FXML private Button btnTracks;
    @FXML private Button btnPlaylists;


    @FXML private PlaylistController playlistsPageController;
    @FXML private TracksController tracksPageController;
    @FXML private PlayerController playerController;
    @FXML private HomeController homePageController;

    private it.diem.unisa.musicmanager.service.QueueService queueService;
    private it.diem.unisa.musicmanager.service.PlaylistService playlistService;
    private it.diem.unisa.musicmanager.service.PlayerService playerService;
    private CommandManager commandManager;


    @FXML
    public void initialize() {
        showPage(homePage);
        setActiveMenu(btnHome);
    }

    @FXML
    private void openHome() {
        if (homePageController != null) {
            homePageController.loadTopTracks();
            homePageController.loadTopPlaylists();
        }

        showPage(homePage);
        setActiveMenu(btnHome);
    }

    @FXML
    private void openTracks() {
        showPage(tracksPage);
        setActiveMenu(btnTracks);
    }

    @FXML
    private void openPlaylists() {
        showPage(playlistsPage);
        setActiveMenu(btnPlaylists);
    }

    private void showPage(Node pageToShow) {
        if (homePage != null) homePage.setVisible(false);
        if (tracksPage != null) tracksPage.setVisible(false);
        if (playlistsPage != null) playlistsPage.setVisible(false);

        if (pageToShow != null) {
            pageToShow.setVisible(true);
            pageToShow.toFront();
        }
    }

    private void setActiveMenu(Button active) {
        btnHome.getStyleClass().remove("btn-menu-active");
        btnTracks.getStyleClass().remove("btn-menu-active");
        btnPlaylists.getStyleClass().remove("btn-menu-active");
        if (!active.getStyleClass().contains("btn-menu-active")) {
            active.getStyleClass().add("btn-menu-active");
        }
    }

    public PlaylistController getPlaylistsPageController() {
        return playlistsPageController;
    }

    public void setQueueService(it.diem.unisa.musicmanager.service.QueueService queueService) {
        this.queueService = queueService;
    }

    public void setPlayerService(it.diem.unisa.musicmanager.service.PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setPlaylistService(it.diem.unisa.musicmanager.service.PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @FXML
    private void openQueue() {
        try {
            javafx.fxml.FXMLLoader loader = it.diem.unisa.musicmanager.util.WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/queueView.fxml",
                    "Coda di Riproduzione",
                    javafx.stage.Modality.NONE
            );
            if (loader == null) {
                return;
            }
            QueueViewController ctrl = loader.getController();
            ctrl.setPlaylistService(playlistService);
            ctrl.setQueueService(queueService);
            ctrl.setPlayerService(playerService);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
     }


    public TracksController getTracksPageController()
    {
        return tracksPageController;
    }




    public PlayerController getPlayerController() { return playerController;}

    public HomeController getHomePageController() {
        return homePageController;
    }


        @FXML
        private void onUndo() {
            if (commandManager == null || !commandManager.canUndo()) {
                AlertUtil.showInfo("Nothing to undo", "There are no actions to undo.");
                return;
            }

            String descrizione = commandManager.peekUndoDescription().orElse("l'ultima azione");

            boolean conferma = AlertUtil.showConfirmation(
                    "Undo",
                    "Do you want to undo: " + descrizione + "?"
            );

            if (conferma) {
                commandManager.undo();
            }
        }
    }
