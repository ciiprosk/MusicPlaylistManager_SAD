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

/**
 * Controller della finestra modale "Crea Playlist" (addPlaylist.fxml).
 * Per ora gestisce SOLO la parte di interfaccia: legge il nome e chiude la
 * finestra. Il salvataggio (service) verra' aggiunto dopo.
 */
public class AddPlaylistController {

    // --- Campi dell'interfaccia, collegati agli fx:id presenti in addPlaylist.fxml ---
    @FXML private TextField fieldName;
    @FXML private Label lblError;
    @FXML private Button btnCreate;

    // Service condiviso per la gestione delle playlist (passato da chi apre il popup).
    private PlaylistService playlistService;

    /**
     * Imposta il service da usare per salvare la playlist.
     *
     * @param playlistService il service condiviso delle playlist

    public void setPlaylistService(PlaylistService playlistService) {
    this.playlistService = playlistService;
    }
     */

    /**
     * Gestisce il click su "Crea".
     * NOTA: per ora e' un placeholder. La logica di salvataggio (commentata sotto)
     * verra' riattivata quando il PlaylistService sara' disponibile.
     *
     * @param e evento generato dal click sul pulsante "Crea"
     */
    @FXML
    private void onSave(ActionEvent e) {
        lblError.setText("Salvataggio non ancora disponibile.");
 
        /*
        // Puliamo eventuali messaggi di errore precedenti.
        lblError.setText("");
 
        // Leggiamo il nome inserito.
        String nome = fieldName.getText() == null ? "" : fieldName.getText().trim();
 
        // Il nome e' obbligatorio.
        if (nome.isEmpty()) {
            lblError.setText("Il nome è obbligatorio.");
            return;
        }
 
        // Il service crea la playlist (come da diagramma: createPlaylist).
        // Se qualcosa non va, lancia un'eccezione che mostriamo all'utente.
        try {
            playlistService.createPlaylist(nome);
        } catch (PlaylistInfoException ex) {
            lblError.setText(ex.getMessage());
            return;
        }
 
        // Fatto: chiudiamo la finestra.
        close(e);
        */
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

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }


}