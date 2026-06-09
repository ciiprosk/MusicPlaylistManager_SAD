package it.diem.unisa.musicmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;

public class MainController {


    @FXML private Node homePage;
    @FXML private Node tracksPage;
    @FXML private Node playlistsPage;


    @FXML private PlaylistController playlistsPageController;
    @FXML private TracksController tracksPageController;
    @FXML private PlayerController playerController;
    @FXML private HomeController homePageController;


    @FXML
    public void initialize() {
        showPage(homePage);
    }

    @FXML
    private void openHome() {
        if (homePageController != null) {
            homePageController.loadTopTracks();
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

    public TracksController getTracksPageController()
    {
        return tracksPageController;
    }




    public PlayerController getPlayerController() { return playerController;}

    public HomeController getHomePageController() {
        return homePageController;
    }
}