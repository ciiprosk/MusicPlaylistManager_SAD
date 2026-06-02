package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller di una card playlist (playlistCard.fxml).
 * Mostra nome e numero di tracce, e offre le azioni Play / Modifica / Menu.
 * La playlist viene passata tramite {@link #setPlaylist(Playlist)} prima
 * di mostrare la card.
 */
public class PlaylistCardController {

    // --- Campi dell'interfaccia, collegati agli fx:id in playlistCard.fxml ---
    @FXML private Label lblPlaylistIcon;
    @FXML private Label lblNome;
    @FXML private Label lblTracce;
    @FXML private Button btnPlay;
    @FXML private Button btnModify;
    @FXML private Button btnMenu;

    // La playlist mostrata da questa card.
    private Playlist playlist;

    /**
     * Imposta la playlist della card e riempie le etichette.
     *
     * @param playlist la playlist da mostrare
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        lblNome.setText(playlist.getName());
        int n = playlist.getTracks().size();
        lblTracce.setText(n + (n == 1 ? " traccia" : " tracce"));
    }

    /**
     * Click su Play: avvia la riproduzione della playlist.
     * (Da collegare al PlayerService quando disponibile.)
     */
    @FXML
    private void handlePlay() {
        // TODO: riproduzione della playlist
    }

    /**
     * Click su Modifica: apre la finestra di modifica della playlist (editPlaylist).
     */
    @FXML
    private void handleModify() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml"));
            Parent root = loader.load();

            // Passiamo la playlist al controller della modifica.
            EditPlaylistController controller = loader.getController();
            //controller.setPlaylist(playlist);
            // Quando il PlaylistService sara' attivo, qui passeremo anche il service:
            // controller.setPlaylistService(playlistService);

            Stage stage = new Stage();
            stage.setTitle("Modifica Playlist");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Click su Menu: apre le altre azioni della playlist.
     */
    @FXML
    private void handleMenu() {
        // TODO: menu con altre azioni (es. elimina, dettagli)
    }
}