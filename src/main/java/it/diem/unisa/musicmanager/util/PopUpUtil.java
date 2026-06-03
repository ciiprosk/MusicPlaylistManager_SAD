package it.diem.unisa.musicmanager.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Utility per aprire finestre modali da FXML, evitando di ripetere ovunque lo
 * stesso codice (carica FXML, crea Stage, configura il controller, mostra).
 */
public final class PopUpUtil {

    // Classe di sole utility: niente istanze.
    private PopUpUtil() { }

    /**
     * Carica un FXML, permette di configurare il suo controller (es. iniettare
     * service o dati) e mostra la finestra come modale, attendendo la chiusura.
     *
     * @param fxmlPath  percorso assoluto dell'FXML (es. "/it/diem/.../addSong.fxml")
     * @param title     titolo della finestra
     * @param configure azione che riceve il controller per configurarlo
     *                  (puo' essere null se non serve configurare nulla)
     * @param <C>       tipo del controller dell'FXML
     * @return il controller dell'FXML caricato (utile per leggere dati dopo la
     *         chiusura), oppure null se si verifica un errore
     */
    public static <C> C openModal(String fxmlPath, String title, Consumer<C> configure) {
        try {
            FXMLLoader loader = new FXMLLoader(PopUpUtil.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Recuperiamo il controller e lo configuriamo (es. setTrackService).
            C controller = loader.getController();
            if (configure != null) {
                configure.accept(controller);
            }

            // Mostriamo la finestra come modale e aspettiamo che venga chiusa.
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
