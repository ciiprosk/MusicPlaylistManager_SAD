package it.diem.unisa.musicmanager.controller;

//import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Controller della finestra modale "Crea Playlist" (addPlaylist.fxml).
 * Per ora gestisce SOLO la parte di interfaccia: legge il nome e chiude la
 * finestra. Il salvataggio (service) verra' aggiunto dopo.
 */
public class AddPlaylistController {

    // --- Campi dell'interfaccia, collegati agli fx:id presenti in addPlaylist.fxml ---
    @FXML private TextField fieldName;
    @FXML private Label lblError;

    // Service condiviso per la gestione delle playlist (passato da chi apre il popup).
    private PlaylistService playlistService;


    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    /**
     * Gestisce il click su "Annulla": chiude la finestra senza salvare.
     *
     * @param e evento generato dal click sul pulsante "Annulla"
     */
    @FXML
    private void onCancel(ActionEvent e) {
        close(e);
    }

    @FXML
    public void onCreate(ActionEvent actionEvent) {
        lblError.setText("");

        if (playlistService == null) {
            lblError.setText("Playlist Service not available.");

        }
        //mi prendo il testo inserito
        String name = fieldName.getText() == null ? "" : fieldName.getText().trim();

        if (name.isEmpty()) {
            lblError.setText("You must enter a name.");
        }

        Optional<String> optional = playlistService.createPlaylist(name); //chiamo il service per crare la playlist
        if(optional.isPresent()){
            lblError.setText(optional.get());
        }else{
            close(actionEvent);
        }
    }

    /**
     * Chiude la finestra modale, ricavando lo Stage dal bottone che ha generato l'evento.
     *
     * @param e evento da cui risalire alla finestra da chiudere
     */
    private void close(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }



}