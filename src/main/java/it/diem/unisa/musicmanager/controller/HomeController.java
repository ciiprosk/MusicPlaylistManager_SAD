package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.TrackService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
/**
 * Controller responsible for managing the behavior of the Home view.
 *
 * This class is associated with the Home section of the application, as defined in the home.fxml file.
 * It interacts with the MainController to control the visibility and functionality of the Home view.
 *
 * The view includes components such as a search bar for user input, a section for displaying recent tracks,
 * and a section for displaying playlists. Behavior and functionality concerning these components
 * are defined within this controller.
 *
 * This class serves as the bridge between the user interface (UI) and the underlying logic associated with
 * the Home section, enabling dynamic updates and handling user interactions.
 */
public class HomeController {
    @FXML
    private VBox topTracksContainer;

    private TrackService trackService;

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
        loadTopTracks();
    }

    public void loadTopTracks() {
        if (trackService == null || topTracksContainer == null) {
            return;
        }

        topTracksContainer.getChildren().clear();

        List<Track> topTracks = trackService.getTop5MostPlayedTracks();

        if (topTracks.isEmpty()) {
            Label emptyLabel = new Label("Nessuna traccia ascoltata.");
            emptyLabel.setStyle("-fx-text-fill: #cccccc;");
            topTracksContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Track track : topTracks) {
            Label label = new Label(
                    track.getTitle()
                            + " - "
                            + track.getAuthor()
                            + " | Ascolti: "
                            + track.getPlayCount()
            );

            label.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            topTracksContainer.getChildren().add(label);
        }
    }
    // è semplicissimo caricare sia le card che la row dei brani, metto il codice di esempio:
    /*
    // Dentro TracksController.java, dove avete una VBox o una griglia per ospitare le tracce
@FXML
private VBox tracksContainer;


public void loadTracks(ObservableList<Track> allTracks) {
    tracksContainer.getChildren().clear(); // Pulisce la lista visiva

    for (Track track : allTracks) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));
            Node row = loader.load();

            // Prendiamo il controller specifico di QUESTA singola riga appena creata
            RowTrackController controller = loader.getController();
            // Gli passiamo i dati del brano e lo SharedState
            controller.setTrack(track);

            // Aggiungiamo la riga grafica al contenitore della pagina
            tracksContainer.getChildren().add(row);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
     */
}
