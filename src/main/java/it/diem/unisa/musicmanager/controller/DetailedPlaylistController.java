package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
//import it.diem.unisa.musicmanager.service.PlaylistService;
//import it.diem.unisa.musicmanager.service.TrackService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller della schermata di dettaglio playlist (detailedPlaylist.fxml).
 * Mostra in alto il nome della playlist e sotto l'elenco delle tracce
 * (titolo, autore, durata).
 * Per ora gestisce la View: il nome viene mostrato; il riempimento delle tracce
 * a partire dagli UUID richiede il TrackService (verra' collegato dopo).
 */
public class DetailedPlaylistController {

    // --- Campi dell'interfaccia, collegati agli fx:id in detailedPlaylist.fxml ---
    @FXML private Label lblPlaylistName;
    @FXML private Label lblTrackCount;
    @FXML private ListView<Track> tracksList;

    // La playlist mostrata.
    private Playlist playlist;

    // Service (da collegare quando disponibili).
    // private TrackService trackService;
    // private PlaylistService playlistService;

    /**
     * Chiamato automaticamente da JavaFX appena la schermata e' pronta.
     * Definisce come mostrare ogni traccia: titolo, autore e durata.
     */
    @FXML
    private void initialize() {
        tracksList.setCellFactory(lv -> createTrackCell());
    }

    /**
     * Imposta la playlist da visualizzare e ne mostra il nome e il conteggio.
     *
     * @param playlist la playlist di cui mostrare il dettaglio
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        lblPlaylistName.setText(playlist.getName());
        int n = playlist.getTracks().size();
        lblTrackCount.setText(n + (n == 1 ? " traccia" : " tracce"));

        // Quando il TrackService sara' disponibile, qui risolveremo gli UUID
        // della playlist nei rispettivi Track e riempiremo la lista:
        //
        // List<Track> tracce = playlist.getTracks().stream()
        //         .map(id -> trackService.findById(id))   // metodo da aggiungere al service
        //         .filter(Objects::nonNull)
        //         .toList();
        // tracksList.getItems().setAll(tracce);
    }

    /**
     * Crea una cella che mostra "Titolo - Autore" a sinistra e la durata a destra.
     *
     * @return una cella personalizzata per un Track
     */
    private ListCell<Track> createTrackCell() {
        return new ListCell<>() {
            private final Label info = new Label();
            private final Label durata = new Label();
            private final HBox riga = new HBox(10, info, space(), durata);

            {
                info.getStyleClass().add("brano-titolo");
                durata.getStyleClass().add("brano-durata");
            }

            @Override
            protected void updateItem(Track t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setGraphic(null);
                } else {
                    info.setText(t.getTitle() + " - " + t.getAuthor());
                    durata.setText(formatDuration(t.getSongLength()));
                    setGraphic(riga);
                }
            }
        };
    }

    /** Region elastica che spinge la durata a destra. */
    private Region space() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
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
     * Click su "Rinomina": apre la finestra di modifica del nome della playlist.
     */
    @FXML
    private void handleRename() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml"));
            Parent root = loader.load();

            EditPlaylistController controller = loader.getController();
            //controller.setPlaylist(playlist);
            // Quando il PlaylistService sara' attivo:
            // controller.setPlaylistService(playlistService);

            Stage stage = new Stage();
            stage.setTitle("Modifica Playlist");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Dopo la chiusura, aggiorniamo il nome mostrato (potrebbe essere cambiato).
            if (playlist != null) {
                lblPlaylistName.setText(playlist.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}