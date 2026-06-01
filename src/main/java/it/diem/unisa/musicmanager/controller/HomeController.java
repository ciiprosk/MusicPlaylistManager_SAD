package it.diem.unisa.musicmanager.controller;

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
