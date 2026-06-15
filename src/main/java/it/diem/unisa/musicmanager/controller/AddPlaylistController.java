package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.service.PlaylistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import it.diem.unisa.musicmanager.command.CommandManager;
import it.diem.unisa.musicmanager.command.Command;
import it.diem.unisa.musicmanager.command.CreatePlaylistCommand;

import java.util.Optional;

/**
 * Controller della finestra modale "Crea Playlist" (addPlaylist.fxml).
 * Legge il nome inserito e chiede al PlaylistService di creare la playlist.
 * Il service comunica gli errori tramite Optional<String>: vuoto = successo,
 * presente = messaggio di errore da mostrare.
 */
public class AddPlaylistController {

    // --- Campi dell'interfaccia, collegati agli fx:id in addPlaylist.fxml ---
    @FXML private TextField fieldName;
    @FXML private Label lblError;


    // Service per la gestione delle playlist (passato da chi apre il popup).
    private PlaylistService playlistService;
    private CommandManager commandManager;

    /**
     * Imposta il service da usare per creare la playlist.
     *
     * @param playlistService il service condiviso delle playlist
     */
    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Gestisce il click su "Crea": valida il nome, chiede al service di creare
     * la playlist e chiude la finestra se l'operazione va a buon fine.
     *
     * @param e evento generato dal click sul pulsante "Crea"
     */
    @FXML
    private void onCreate(ActionEvent e) {
        lblError.setText("");

        // Se il service non e' stato iniettato, non possiamo procedere.
        if (playlistService == null) {
            lblError.setText("Playlist Service not available.");
            return;
        }

        // Leggiamo il nome inserito.
        String name = fieldName.getText() == null ? "" : fieldName.getText().trim();
        if (name.isEmpty()) {
            lblError.setText("You must enter a name.");
            return;
        }

        // Creazione tramite Command, così è annullabile.
        Command cmd = new CreatePlaylistCommand(playlistService, name);
        Optional<String> error = commandManager.executeCommand(cmd);
        if (error.isPresent()) {
            lblError.setText(error.get());
        } else {
            close(e);
        }
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
