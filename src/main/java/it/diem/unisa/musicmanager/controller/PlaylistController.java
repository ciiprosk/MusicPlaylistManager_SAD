package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller della schermata "Playlists".
 * Mostra le playlist come griglia di card e permette di crearne di nuove
 * aprendo la finestra "Crea Playlist".
 */
public class PlaylistController {

    // --- Campi dell'interfaccia, collegati agli fx:id in playlist.fxml ---
    @FXML private TextField searchBar;
    @FXML private FlowPane playlistsGrid;

    /**
     * Apre la finestra modale "Crea Playlist".
     * Per ora apre solo la finestra (senza service).
     *
     * @param actionEvent evento generato dal click sul pulsante "Add Playlist"
     */
    @FXML
    private void handleAdd(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/addPlaylist.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Playlist");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}