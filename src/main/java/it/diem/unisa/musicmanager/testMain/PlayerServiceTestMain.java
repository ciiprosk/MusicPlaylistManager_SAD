package it.diem.unisa.musicmanager.testMain;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PlayerServiceTestMain extends Application {

    @Override
    public void start(Stage stage) {
        SharedState sharedState = new SharedState();
        PlayerService playerService = new PlayerService(sharedState);

        /*
         * IMPORTANTE:
         * Sostituisci questo percorso con un file audio reale presente sul tuo PC.
         */
        String audioPath = "/home/rosa/Scaricati/test.wav";

        Track track = new Track(
                "Test Audio",
                "Test Author",
                Genre.ROCK,
                audioPath,
                180,
                "2026"
        );

        System.out.println("Avvio riproduzione...");
        System.out.println("File audio: " + track.getSongPath());

        sharedState.getCurrentTrack().addListener((observable, oldTrack, newTrack) -> {
            if (newTrack != null) {
                System.out.println("Traccia corrente: " + newTrack.getTitle() + " - " + newTrack.getAuthor());
            }
        });

        sharedState.getIsPlaying().addListener((observable, oldValue, newValue) -> {
            System.out.println("Is playing: " + newValue);
        });

        sharedState.getProgress().addListener((observable, oldValue, newValue) -> {
            System.out.println("Progress: " + String.format("%.2f", newValue.doubleValue() * 100) + "%");
        });

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: #1e1e1e;");

        Label titleLabel = new Label("PlayerService Test");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label fileLabel = new Label("File: " + audioPath);
        fileLabel.setStyle("-fx-text-fill: #cccccc;");

        Label infoLabel = new Label("Chiudi questa finestra per fermare il player.");
        infoLabel.setStyle("-fx-text-fill: #aaaaaa;");

        root.getChildren().addAll(titleLabel, fileLabel, infoLabel);

        stage.setTitle("PlayerService Test");
        stage.setScene(new Scene(root, 650, 160));
        stage.setResizable(false);

        stage.setOnShown(event -> playerService.play(track));

        stage.setOnCloseRequest(event -> {
            playerService.stop();
            Platform.exit();
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}