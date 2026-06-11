package it.diem.unisa.musicmanager.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Questa classe implementa il patter Facade, con i metodi utilizzati dai controller per aprire le varie finestre.
 *
 */
public class WindowUtil {

    // Mappa globale per tracciare le finestre aperte usando il titolo o il percorso FXML come chiave univoca.
    // Usiamo il titolo (es. il nome della playlist) così ogni playlist ha la sua finestra unica!
    private static final Map<String, Stage> openStages = new HashMap<>();

    public static FXMLLoader openWindow(String fxmlPath, String title, Modality modality) throws IOException {

        // 1. Se una finestra con questo titolo è già aperta, portala in primo piano e non fare nulla
        if (openStages.containsKey(title)) {
            Stage existingStage = openStages.get(title);
            existingStage.setIconified(false); // La ripristina se era minimizzata
            existingStage.requestFocus();      // Le dà il focus

            // Per non rompere la firma del metodo, ricarichiamo il loader (o restituiamo null se gestito,
            // ma l'ideale è riprendere quello esistente. Visto che serve solo il controller, creiamo un loader finto
            // o ritorniamo il loader della finestra che però non viene rimostrata).
            FXMLLoader dummyLoader = new FXMLLoader(WindowUtil.class.getResource(fxmlPath));
            return null;
        }

        // 2. Se non è aperta, la creiamo normalmente
        FXMLLoader loader = new FXMLLoader(WindowUtil.class.getResource(fxmlPath));

        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle(title);
        //stage.setResizable(false);
        stage.initModality(modality);
        stage.setScene(new Scene(root));
        stage.show();

        // 3. Registriamo la finestra nella mappa
        openStages.put(title, stage);

        // 4. Quando la finestra viene chiusa dall'utente, la rimuoviamo dalla mappa
        stage.setOnCloseRequest(event -> openStages.remove(title));

        // 5. Questo garantisce la rimozione dalla mappa in QUALSIASI tipo di chiusura nascosta (hiding)
        stage.setOnHiding(event -> openStages.remove(title));

        return loader;

    }

    public static void close(Node source) {
        ((Stage) source.getScene().getWindow()).close();
    }
}
