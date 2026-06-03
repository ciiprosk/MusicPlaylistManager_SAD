package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.TrackService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Optional;

/**
 * Controller della finestra modale "Modifica Brano" (editSong.fxml).
 * Mostra i dati del brano in un form, permette di modificarli e salvarli
 * tramite il TrackService. Durata e percorso del file sono in sola lettura
 * perche' nel model sono immutabili.
 */
public class EditSongController {

    @FXML private TextField fieldTitle;
    @FXML private TextField fieldAuthor;
    @FXML private TextField fieldYear;
    @FXML private TextField fieldDuration;
    @FXML private ComboBox<Genre> comboGenre;
    @FXML private Label lblFilePath;
    @FXML private Label lblError;
    @FXML private Button btnSave;

    // Service per validare e salvare le modifiche (passato da chi apre il popup).
    private TrackService trackService;

    // Il brano in fase di modifica.
    private Track track;

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * Imposta il brano da modificare e riempie il form con i valori attuali.
     * Va chiamato prima di mostrare la finestra.
     */
    public void setTrack(Track track) {
        this.track = track;

        fieldTitle.setText(track.getTitle());
        fieldAuthor.setText(track.getAuthor());
        comboGenre.setValue(track.getGenre());

        // L'anno "UNKNOWN" viene mostrato come campo vuoto.
        String year = track.getYear();
        fieldYear.setText("UNKNOWN".equals(year) ? "" : year);

        // Sola lettura: durata e percorso del file.
        fieldDuration.setText(formatDuration(track.getSongLength()));
        lblFilePath.setText(track.getSongPath());
    }

    /** Chiamato da JavaFX all'apertura: riempie la tendina dei generi. */
    @FXML
    private void initialize() {
        // Tutti i generi tranne UNKNOWN (e' il default se non si sceglie).
        Arrays.stream(Genre.values())
                .filter(g -> g != Genre.UNKNOWN)
                .forEach(comboGenre.getItems()::add);
    }

    /** Trasforma una durata in secondi in "m:ss". */
    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /** Genere scelto, oppure UNKNOWN se non e' stato selezionato nulla. */
    private Genre getSelectedGenre() {
        Genre selected = comboGenre.getValue();
        return (selected != null) ? selected : Genre.UNKNOWN;
    }

    /**
     * Click su "Salva": delega validazione e salvataggio al service.
     * Se il service segnala un errore lo mostriamo e teniamo aperta la
     * finestra; altrimenti chiudiamo. La lista brani si aggiorna da sola
     * perche' updateTrack aggiorna lo SharedState.
     */
    @FXML
    private void onSave(ActionEvent e) {
        lblError.setText("");

        String title  = fieldTitle.getText() == null ? "" : fieldTitle.getText().trim();
        String author = fieldAuthor.getText() == null ? "" : fieldAuthor.getText().trim();
        String year   = fieldYear.getText() == null ? "" : fieldYear.getText().trim();

        // Il service valida (titolo, anno, duplicati) e salva.
        // Ritorna Optional vuoto se tutto ok, altrimenti il messaggio d'errore.
        Optional<String> error = trackService.updateTrack(
                track.getId(), title, author, getSelectedGenre(), year);

        if (error.isPresent()) {
            lblError.setText(error.get());
            return;
        }

        close(e);
    }

    /** Click su "Annulla": chiude senza salvare. */
    @FXML
    private void onCancel(ActionEvent e) {
        close(e);
    }

    /** Chiude la finestra modale ricavando lo Stage dal bottone. */
    private void close(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }
}