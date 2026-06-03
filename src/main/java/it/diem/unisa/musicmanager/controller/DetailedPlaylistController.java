package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
//import it.diem.unisa.musicmanager.service.PlaylistService;
//import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Controller della schermata di dettaglio playlist (detailedPlaylist.fxml).
 * Mostra in alto il nome della playlist e sotto l'elenco delle tracce
 * (titolo, autore, durata).
 * Per ora gestisce la View: il nome viene mostrato; il riempimento delle tracce
 * a partire dagli UUID richiede il TrackService (verra' collegato dopo).
 */
public class DetailedPlaylistController {

    // --- Campi dell'interfaccia, collegati agli fx:id in detailedPlaylist.fxml ---
    @FXML private Label labelName;
    @FXML private Label lblTrackCount;
    @FXML private ListView<Track> tracksList;

    // La playlist mostrata.
    private Playlist playlist;

    // Service (da collegare quando disponibili).
    // private TrackService trackService;
    private PlaylistService playlistService;

    /**
     * Chiamato automaticamente da JavaFX appena la schermata e' pronta.
     * Definisce come mostrare ogni traccia: titolo, autore e durata.
     */
    @FXML
    private void initialize() {
      //  tracksList.setCellFactory(lv -> createTrackCell());
    }

    /**
     * Imposta la playlist da visualizzare e ne mostra il nome e il conteggio.
     *
     * @param playlist la playlist di cui mostrare il dettaglio
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        labelName.setText(playlist.getName());
        int n = playlist.getTracks().size();
        lblTrackCount.setText(n + (n == 1 ? " track" : " tracks"));

        // Quando il TrackService sara' disponibile, qui risolveremo gli UUID
        // della playlist nei rispettivi Track e riempiremo la lista:
        //
        // List<Track> tracce = playlist.getTracks().stream()
        //         .map(id -> trackService.findById(id))   // metodo da aggiungere al service
        //         .filter(Objects::nonNull)
        //         .toList();
        // tracksList.getItems().setAll(tracce);
    }

    public void setPlaylistService(PlaylistService playlistService){
        this.playlistService = playlistService;
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

    public void onModify(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml", playlist.getName(),Modality.APPLICATION_MODAL);

            EditPlaylistController controller = loader.getController();
            controller.setPlaylist(playlist);
            controller.setPlaylistService(playlistService);

            /*
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml"));
            Parent root = loader.load();

            EditPlaylistController controller = loader.getController();
            controller.setPlaylist(playlist);

            controller.setPlaylistService(playlistService);
            //close(actionEvent);

            Stage stage = new Stage();
            stage.setTitle("Modifica Playlist");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            */
            WindowUtil.close( (Node) actionEvent.getSource());


            // Dopo la chiusura, aggiorniamo il nome mostrato (potrebbe essere cambiato).
            if (playlist != null) {
                labelName.setText(playlist.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onAddTrack(ActionEvent actionEvent) {
    }

    public void onDelete(ActionEvent actionEvent) {
        //uso il service per eliminare la playlist
        deletePlaylist();
        //close(actionEvent);
        WindowUtil.close( (Node) actionEvent.getSource());

    }

    /*
    private void close(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }

     */

    private void deletePlaylist() {
        if (playlistService == null || playlist == null) return;

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete the Playlist \"" + playlist.getName() + "\"?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                playlistService.deletePlaylist(playlist.getId());
            }
        });
    }

}