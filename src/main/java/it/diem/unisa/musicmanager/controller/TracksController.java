package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Track;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller della schermata "Tracks".
 * Per ora gestisce SOLO la navigazione: apre la finestra "Crea Brano".
 * La logica (service, lista dei brani, salvataggio) verra' aggiunta dopo.
 */
public class TracksController {

    // --- Campi dell'interfaccia, collegati agli fx:id in tracks.fxml ---
    @FXML private ListView<Track> tracksList;
    @FXML private TextField searchBar;

    /**
     * Apre la finestra modale "Crea Brano".
     * Per ora apre solo la finestra, senza passare alcun service.
     *
     * @param actionEvent evento generato dal click sul pulsante "Add Track"
     */
    @FXML
    private void handleAggiungi(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/addSong.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Track");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}