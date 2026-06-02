package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Track;
//import it.diem.unisa.musicmanager.service.PlayerService;
//import it.diem.unisa.musicmanager.service.TrackService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller della schermata "Tracks": mostra l'elenco dei brani e
 * permette di aggiungerne di nuovi tramite la finestra "Crea Brano".
 * I service vengono ricevuti dal costruttore (Dependency Injection):
 * le dipendenze sono dichiarate, non create internamente.
 */
public class TracksController {

    // --- Service
    // private final TrackService trackService;
    // private final PlayerService playerService;

    // --- Campi dell'interfaccia, collegati agli fx:id in tracks.fxml ---
    @FXML private ListView<Track> tracksList;
    @FXML private TextField searchBar;

    /**
     * @param trackService  service condiviso per la gestione dei brani
     * @param playerService service condiviso per la riproduzione

    public TracksController(TrackService trackService, PlayerService playerService) {
        this.trackService = trackService;
        this.playerService = playerService;
    }
     */
    /**
     * Chiamato automaticamente da JavaFX appena la schermata e' pronta.
     * Definisce come mostrare i brani e collega la lista a quella condivisa.

    @FXML
    private void initialize() {
        // Per ogni riga mostriamo "Titolo - Autore".
        tracksList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Track t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : t.getTitle() + " - " + t.getAuthor());
            }
        });

        // Colleghiamo la ListView alla lista condivisa dei brani:
        // quando il service aggiunge/rimuove un brano, la lista si aggiorna da sola.
        tracksList.setItems(trackService.getAllTracks());
    }
*/
    /**
     * Apre la finestra modale "Crea Brano".
     * Passa il service al popup; al salvataggio la lista si aggiorna da sola
     * perche' e' legata alla stessa lista condivisa.
     *
     * @param actionEvent evento generato dal click sul pulsante "Add Track"

    @FXML
    private void handleAggiungi(ActionEvent actionEvent) {
        try {
            // Carichiamo l'FXML del popup.
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/addSong.fxml"));
            Parent root = loader.load();

            // Passiamo lo stesso service al controller del popup.
            // (Questo loader e' separato e non usa la controllerFactory,
            //  quindi l'iniezione qui la facciamo a mano.)
            AddSongController controller = loader.getController();
            controller.setTrackService(trackService);

            // Mostriamo la finestra come modale (blocca la principale finche' aperta).
            Stage stage = new Stage();
            stage.setTitle("Add Track");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            // Nessun refresh manuale: la lista e' legata allo SharedState.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     */
}