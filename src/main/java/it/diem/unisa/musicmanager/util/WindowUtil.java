package it.diem.unisa.musicmanager.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Questa classe implementa il patter Facade, con i metodi utilizzati dai controller per aprire le varie finestre.
 *
 */
public class WindowUtil {

    public static FXMLLoader openWindow(String fxmlPath, String title, Modality modality) throws IOException {
        FXMLLoader loader = new FXMLLoader(WindowUtil.class.getResource(fxmlPath));

        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle(title);
        //stage.setResizable(false);
        stage.initModality(modality);
        stage.setScene(new Scene(root));
        stage.show();

        return loader;

    }

    public static void close(Node source) {
        ((Stage) source.getScene().getWindow()).close();
    }
}
