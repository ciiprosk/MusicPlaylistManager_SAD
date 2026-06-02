package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Track;
//import it.diem.unisa.musicmanager.exception.TrackInfoException;
//import it.diem.unisa.musicmanager.service.TrackService;
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
 * Controller della finestra modale "Modifica Brano" (editSong.fxml).
 * Per ora gestisce SOLO la parte di interfaccia: mostra i dati del brano e
 * permette di chiudere la finestra. Il salvataggio (service) verra' aggiunto dopo.
 * Durata e percorso del file sono in sola lettura perche' nel model sono immutabili.
 */
public class EditSongController {

    // --- Campi dell'interfaccia, collegati agli fx:id presenti in editSong.fxml ---
    @FXML private TextField fieldTitle;
    @FXML private TextField fieldAuthor;
    @FXML private TextField fieldYear;
    @FXML private TextField fieldDuration;
    @FXML private ComboBox<Genre> comboGenre;
    @FXML private Label lblFilePath;
    @FXML private Label lblError;
    @FXML private Button btnSave;

    // Service condiviso per la gestione dei brani (passato da chi apre il popup).
    // private TrackService trackService;

    // Il brano in fase di modifica (passato prima di mostrare la finestra).
    private Track track;

    /**
     * Imposta il service da usare per salvare le modifiche.
     *
     * @param trackService il service condiviso dei brani

    public void setTrackService(TrackService trackService) {
    this.trackService = trackService;
    }
     */

    /**
     * Imposta il brano da modificare e riempie il form con i suoi valori attuali.
     * Va chiamato prima di mostrare la finestra.
     *
     * @param track il brano da modificare
     */
    public void setTrack(Track track) {
        this.track = track;

        // Pre-compiliamo i campi modificabili.
        fieldTitle.setText(track.getTitle());
        fieldAuthor.setText(track.getAuthor());
        comboGenre.setValue(track.getGenre());

        // L'anno "UNKNOWN" viene mostrato come campo vuoto.
        String year = track.getYear();
        fieldYear.setText("UNKNOWN".equals(year) ? "" : year);

        // Info in sola lettura: durata e percorso del file.
        fieldDuration.setText(formatDuration(track.getSongLength()));
        lblFilePath.setText(track.getSongPath());
    }

    /**
     * Chiamato automaticamente da JavaFX appena la finestra e' pronta.
     * Riempie la tendina dei generi.
     */
    @FXML
    private void initialize() {
        // Tutti i generi tranne UNKNOWN (e' il default se l'utente non sceglie).
        Arrays.stream(Genre.values())
                .filter(g -> g != Genre.UNKNOWN)
                .forEach(comboGenre.getItems()::add);
    }

    /**
     * Trasforma una durata in secondi in una stringa minuti:secondi.
     *
     * @param totalSeconds durata in secondi
     * @return la durata come testo, es. "3:45"
     */
    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Restituisce il genere scelto dall'utente.
     *
     * @return il genere selezionato, oppure UNKNOWN se non e' stato scelto nulla
     */
    private Genre getSelectedGenre() {
        Genre selected = comboGenre.getValue();
        return (selected != null) ? selected : Genre.UNKNOWN;
    }

    /**
     * Gestisce il click su "Salva".
     * NOTA: per ora e' un sssss. La logica di salvataggio (commentata sotto)
     * verra' riattivata quando il TrackService sara' disponibile.
     *
     * @param e evento generato dal click sul pulsante "Salva"
     */
    @FXML
    private void onSave(ActionEvent e) {
        lblError.setText("Salvataggio non ancora disponibile.");

        /*
        // Puliamo eventuali messaggi di errore precedenti.
        lblError.setText("");

        // Leggiamo i campi modificabili.
        String title  = fieldTitle.getText() == null ? "" : fieldTitle.getText().trim();
        String author = fieldAuthor.getText() == null ? "" : fieldAuthor.getText().trim();
        String year   = fieldYear.getText() == null ? "" : fieldYear.getText().trim();

        // 1) Il titolo e' obbligatorio.
        if (title.isEmpty()) {
            lblError.setText("Il titolo è obbligatorio.");
            return;
        }
        // 2) L'anno: se vuoto diventa UNKNOWN, altrimenti deve essere 4 cifre e non futuro.
        if (year.isEmpty()) {
            year = "UNKNOWN";
        } else {
            if (!year.matches("\\d{4}")) {
                lblError.setText("L'anno deve essere di 4 cifre.");
                return;
            }
            if (Integer.parseInt(year) > Year.now().getValue()) {
                lblError.setText("L'anno non può essere nel futuro.");
                return;
            }
        }

        // Applichiamo le modifiche al brano esistente (solo i campi modificabili).
        track.setTitle(title);
        track.setAuthor(author);
        track.setGenre(getSelectedGenre());
        track.setYear(year);

        // Il service valida e salva la modifica.
        try {
            trackService.modifyTrack(track);
        } catch (TrackInfoException ex) {
            lblError.setText(ex.getMessage());
            return;
        }

        // Fatto: chiudiamo la finestra. La lista in Tracks riflette la modifica.
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