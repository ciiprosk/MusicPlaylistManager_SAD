package it.diem.unisa.musicmanager.controller;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.TrackService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

public class TracksController {
    private final TrackService trackService;

    @FXML private ListView<Track> tracksList;
    @FXML private TextField searchField;

    public TracksController(TrackService trackService) {
        this.trackService = trackService;
    }

    @FXML
    private void initialize() {
        tracksList.setItems(trackService.getAllTracks());
        // opzionale: filtro searchBar con FilteredList
    }

    /**
     * Apre una finestra popup per l'aggiunta di un nuovo brano.
     * Il popup viene caricato da un file FXML e viene mostrato come finestra modale,
     * bloccando la finestra principale fino alla sua chiusura.
     *
     * @param actionEvent evento generato dal click sul pulsante "Add Track"
     */
    public void openAddTrackModal(ActionEvent actionEvent) {
        try {
            // Carica il file FXML della finestra popup
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/addSong.fxml")
            );

            // Crea il contenuto grafico del popup
            Parent root = loader.load();

            // Crea una nuova finestra
            Stage stage = new Stage();

            // Imposta il titolo della finestra
            stage.setTitle("Add Track");

            // Rende il popup modale (blocca la finestra principale finché non viene chiuso)
            stage.initModality(Modality.APPLICATION_MODAL);

            // Inserisce il contenuto nella finestra
            stage.setScene(new Scene(root));

            // Mostra il popup e attende che venga chiuso
            stage.showAndWait();

        } catch (Exception e) {
            // Stampa eventuali errori durante l'apertura del popup
            e.printStackTrace();
        }
    }

}
