package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import it.diem.unisa.musicmanager.command.CommandManager;

public class MainController {


    @FXML private Node homePage;
    @FXML private Node tracksPage;
    @FXML private Node playlistsPage;


    @FXML private PlaylistController playlistsPageController;
    @FXML private TracksController tracksPageController;
    @FXML private PlayerController playerController;
    @FXML private HomeController homePageController;

    private it.diem.unisa.musicmanager.service.QueueService queueService;
    private it.diem.unisa.musicmanager.service.PlaylistService playlistService;
    private CommandManager commandManager;


    @FXML
    public void initialize() {
        showPage(homePage);
    }

    @FXML
    private void openHome() {
        if (homePageController != null) {
            homePageController.loadTopTracks();
            homePageController.loadTopPlaylists();
        }

        showPage(homePage);
    }

    @FXML
    private void openTracks() {
        showPage(tracksPage);
    }

    @FXML
    private void openPlaylists() {
        showPage(playlistsPage);
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

    public PlaylistController getPlaylistsPageController() {
        return playlistsPageController;
    }

    public void setQueueService(it.diem.unisa.musicmanager.service.QueueService queueService) {
        this.queueService = queueService;
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
            QueueViewController ctrl = loader.getController();
            ctrl.setPlaylistService(playlistService);
            ctrl.setQueueService(queueService);
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
