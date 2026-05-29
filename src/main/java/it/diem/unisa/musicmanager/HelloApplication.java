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