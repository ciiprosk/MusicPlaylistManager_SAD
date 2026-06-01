package it.diem.unisa.musicmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;

public class MainController {

    @FXML private Node homePage;
    @FXML private Node tracksPage;
    @FXML private Node playlistsPage;

    @FXML
    public void initialize() {
        showPage(homePage);
    }

    @FXML
    private void openHome() {
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
}