package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.PlaylistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import it.diem.unisa.musicmanager.util.WindowUtil;


import java.util.Optional;

/**
 * Controller per la finestra modale di modifica della playlist (editPlaylist.fxml).
 * Gestisce esclusivamente la ridenominazione (cambio del nome) di una playlist esistente.
 */
public class EditPlaylistController {

    /**
     * La playlist in corso di modifica.
     */
    private Playlist playlist;

    /**
     * Servizio per la gestione e l'aggiornamento della playlist nel sistema.
     */
    private PlaylistService playlistService;

    /**
     * Campo di testo per l'inserimento del nuovo nome della playlist.
     */
    @FXML private TextField fieldName;

    /**
     * Etichetta di testo per mostrare all'utente eventuali messaggi d'errore o avvisi.
     */
    @FXML private Label lblError;

    /**
     * Imposta la playlist che l'utente desidera modificare.
     *
     * @param playlist La {@link Playlist} da associare.
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    /**
     * Imposta il servizio responsabile delle operazioni di modifica sulle playlist.
     *
     * @param playlistService Il {@link PlaylistService} da associare.
     */
    public void setPlaylistService(PlaylistService playlistService){
        this.playlistService = playlistService;
    }

    /**
     * Gestisce l'azione del pulsante "Annulla", chiudendo la finestra senza salvare le modifiche.
     *
     * @param actionEvent L'evento generato dal click sul pulsante di annullamento.
     */
    public void onCancel(ActionEvent actionEvent) {
        close(actionEvent);
    }

    /**
     * Gestisce l'azione del pulsante "Aggiorna".
     * Esegue i controlli di validazione sull'input (es. nome non vuoto) e,
     * in caso di successo, richiede al {@link PlaylistService} di applicare
     * il nuovo nome alla playlist, per poi chiudere la finestra.
     * Mostra un messaggio di errore nell'interfaccia se l'operazione fallisce o i dati non sono validi.
     *
     * @param actionEvent L'evento generato dal click sul pulsante di aggiornamento.
     */
    public void onUpdate(ActionEvent actionEvent) {
        lblError.setText("");

        if(playlistService == null){
            lblError.setText("Playlist Service not available.");
            return;
        }

        String name = fieldName.getText() == null ? "" : fieldName.getText().trim();
        if (name.isEmpty()) {
            lblError.setText("You must enter a name.");
            return;
        }

        Optional<String> optional = playlistService.renamePlaylist(playlist.getId(), name); //chiamo il service per rinominare la playlist
        if(optional.isPresent()){
            lblError.setText(optional.get());
        }else{
            close(actionEvent);
        }

    }

    /**
     * Metodo di supporto per chiudere la finestra modale corrente.
     *
     * @param e L'evento azione da cui ricavare il nodo radice (la finestra).
     */
    private void close(ActionEvent e) {
        WindowUtil.close((Node) e.getSource());
    }

}
