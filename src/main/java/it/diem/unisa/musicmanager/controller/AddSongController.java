package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Genre;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;

/**
 * Controller della finestra modale "Crea Brano" (addSong.fxml).
 * Gestisce i campi del form e la chiusura della finestra.
 */
public class AddSongController {

    @FXML private TextField fieldTitolo;
    @FXML private TextField fieldAutore;
    @FXML private TextField fieldAnno;
    @FXML private TextField fieldDurata;
    @FXML private ComboBox<Genre> comboGenere;
    @FXML private Label lblFilePath;
    @FXML private Label lblError;
    @FXML private Button btnCrea;

    /**
     * Chiamato automaticamente da JavaFX una volta, subito dopo che l'FXML
     * e' stato caricato e i campi @FXML sono stati iniettati.
     */
    @FXML
    private void initialize() {
        // riempie la tendina con tutti i generi TRANNE UNKNOWN
        // (UNKNOWN e' solo il valore di default quando l'utente non sceglie nulla)
        Arrays.stream(Genre.values())
                .filter(g -> g != Genre.UNKNOWN)
                .forEach(comboGenere.getItems()::add);
    }

    /**
     * Genere scelto dall'utente, oppure UNKNOWN se non ha selezionato nulla.
     */
    private Genre getGenereSelezionato() {
        Genre selezionato = comboGenere.getValue();
        return (selezionato != null) ? selezionato : Genre.UNKNOWN;
    }

    /**
     * Chiude la finestra modale senza salvare.
     * Ricava lo Stage dal bottone che ha generato l'evento.
     */
    @FXML
    private void onAnnulla(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }
}