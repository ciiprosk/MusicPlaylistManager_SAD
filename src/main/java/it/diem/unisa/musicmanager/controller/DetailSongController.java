package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.TrackObserver;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.AlertUtil;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.UUID;

/**
 * Controller della finestra modale "Dettaglio Brano".
 * Mostra i dati di un Track in sola lettura e permette di modificarlo,
 * eliminarlo o chiudere la finestra. Riceve brano e service dall'esterno.
 */
public class DetailSongController {

    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblGenre;
    @FXML private Label lblYear;
    @FXML private Label lblDuration;
    @FXML private Label lblFilePath;
    @FXML private Label lblTags;

    // Il brano mostrato e il service per modificarlo/eliminarlo.
    private Track track;
    private TrackService trackService;

    /** Riceve il brano da mostrare e riempie le label. */
    public void setTrack(Track track) {
        if (track == null) return;
        this.track = track;

        lblTitle.setText(track.getTitle());
        lblAuthor.setText(track.getAuthor());
        lblGenre.setText(track.getGenre().toString());
        lblYear.setText(track.getYear());
        lblDuration.setText(formatDuration(track.getSongLength()));
        lblFilePath.setText(track.getSongPath());
        lblTags.setText(formatTags(track));
    }

    /** Riceve il service usato per modifica ed eliminazione. */
    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;

    }

    /** Formatta i secondi in mm:ss (es. 95 -> "01:35"). */
    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Modifica: apre la finestra di editing passandole brano e service,
     * esattamente come fa il menu della riga.
     */
    @FXML
    private void handleEdit(ActionEvent e) {
        if (track == null) return;
        try {
            // MODIFICATO IL TITOLO: aggiungiamo "Modify: " davanti,
            // così la chiave nella mappa di WindowUtil sarà unica ("Modify: 66")
            FXMLLoader loader = WindowUtil.openWindow(
                    "/it/diem/unisa/musicmanager/pages/editSong.fxml",
                    "Modify: " + track.getTitle(), Modality.APPLICATION_MODAL);

            if (loader == null) {
                return;
            }

            EditSongController controller = loader.getController();
            controller.setTrackService(trackService);
            controller.setTrack(track);
            // a salvataggio riuscito, chiudi il dettaglio
            controller.setOnSaved(() -> close(e));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Elimina: chiede conferma e, se confermato, elimina il brano dall'archivio.
     * La chiusura della finestra avviene tramite la notifica onTrackDeleted,
     * così c'è un solo punto di chiusura per l'eliminazione.
     */
    @FXML
    private void handleDelete(ActionEvent e) {
        if (trackService == null || track == null) return;

        boolean isConfirmed = AlertUtil.showConfirmation(
                "Delete Confirm", "Are you sure you wanna delete this track?");

        if (isConfirmed) {
            trackService.deleteTrack(track.getId());  // scatena onTrackDeleted -> chiude
        }
    }



    /** Chiudi: chiude la finestra senza fare altro. */
    @FXML
    private void onClose(ActionEvent e) {
        close(e);
    }

    /** Chiude la finestra modale ricavando lo Stage dal bottone. */
    private void close(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }


    private String formatTags(Track track) {
        return track.getTags()
                .stream()
                .map(Enum::name)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No tags");
    }



}