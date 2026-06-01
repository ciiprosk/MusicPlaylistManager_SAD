package it.diem.unisa.musicmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("MusicPlaylistManagerGUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);


        stage.setMinWidth(900);
        stage.setMinHeight(650);

        stage.setTitle("APP");
        stage.setScene(scene);
        stage.show();
    }

}
// per evitare il piu possibile il pattern singleton usiamo quetsa inizializzazione tipo:
/*
// App.java — unico punto di creazione
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // creati UNA VOLTA SOLA — ma non sono Singleton statici
        TrackDAO    trackDAO    = new JsonTrackDAO();
        DAO playlistDAO = new JsonPlaylistDAO();
        AppState    appState    = new AppState();

        TrackService    trackService    = new TrackService(trackDAO, appState);
        PlaylistService playlistService = new PlaylistService(playlistDAO, appState);
        PlayerService   playerService   = new PlayerService(appState);

        // inietti nei controller tramite ControllerFactory
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
        loader.setControllerFactory(controllerClass -> {
            if (controllerClass == MainController.class)
                return new MainController(trackService, playlistService, playerService);
            if (controllerClass == PlayerController.class)
                return new PlayerController(playerService);
            // ... altri controller
            return null;
        });

        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}

i singoli controller le ricveono dal costruttore
// ✅ Con DI — dipendenze esplicite
public class BraniController {
    private final TrackService trackService;

    public BraniController(TrackService trackService) {
        this.trackService = trackService; // dichiarata, non pescata
    }

    @FXML
    public void initialize() {
        trackService.getAll(); // stesso risultato, ma dipendenza visibile
    }
}
 */