package it.diem.unisa.musicmanager.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe di utilità che implementa il pattern Facade per la gestione delle finestre di dialogo dell'applicazione.
 * Fornisce metodi standardizzati per l'apertura e la chiusura delle finestre basate su file FXML.
 * Tiene traccia delle finestre attualmente aperte per evitarne l'apertura di copie duplicate.
 */
public class WindowUtil {

    /**
     * Mappa globale per tracciare le finestre aperte, utilizzando il titolo (o percorso) della finestra come chiave univoca.
     * Consente di ripristinare e portare in primo piano una finestra già aperta invece di aprirne una nuova copia.
     */
    private static final Map<String, Stage> openStages = new HashMap<>();

    /**
     * Costruttore di default.
     */
    public WindowUtil() {
        // Costruttore di default
    }

    /**
     * Carica un file FXML e apre una nuova finestra (Stage) con il titolo e la modalità specificati.
     * Se una finestra con lo stesso titolo è già presente nella mappa delle finestre aperte,
     * non ne crea una nuova ma si limita a ripristinarla e portarla in primo piano.
     * Per evitare che su sistemi operativi Linux le finestre vengano aperte con dimensioni
     * microscopiche, imposta delle dimensioni di fallback nel caso in cui non siano dichiarate
     * esplicitamente all'interno del file FXML, vincolando inoltre le dimensioni minime dello Stage.
     * 
     * @param fxmlPath Il percorso relativo della risorsa FXML da caricare (es. "MyView.fxml").
     * @param title    Il titolo da assegnare alla finestra (utilizzato anche come chiave univoca).
     * @param modality La modalità della finestra (es. {@link Modality#APPLICATION_MODAL} o {@link Modality#NONE}).
     * @return Il caricatore {@link FXMLLoader} utilizzato per caricare il file FXML se la finestra è stata aperta ex novo;
     *         restituisce {@code null} se la finestra era già aperta ed è stata solo portata in primo piano.
     * @throws IOException Se si verifica un errore nel caricamento o nella lettura del file FXML.
     */
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

        // Impostiamo delle dimensioni di default o leggiamo quelle preferite dall'FXML
        double width = 550;
        double height = 450;

        if (root instanceof Region) {
            Region region = (Region) root;
            double prefWidth = region.getPrefWidth();
            double prefHeight = region.getPrefHeight();

            // Region.USE_COMPUTED_SIZE è -1.0. Se le dimensioni preferite sono esplicitamente definite, usiamo quelle.
            if (prefWidth > 0) {
                width = prefWidth;
            }
            if (prefHeight > 0) {
                height = prefHeight;
            }
        }

        stage.setScene(new Scene(root, width, height));
        
        // Imposta le dimensioni minime dello Stage per evitare il collasso della finestra su Linux
        stage.setMinWidth(width);
        stage.setMinHeight(height);
        
        stage.show();

        // 3. Registriamo la finestra nella mappa
        openStages.put(title, stage);

        // 4. Quando la finestra viene chiusa dall'utente, la rimuoviamo dalla mappa
        stage.setOnCloseRequest(event -> openStages.remove(title));

        // 5. Questo garantisce la rimozione dalla mappa in QUALSIASI tipo di chiusura nascosta (hiding)
        stage.setOnHiding(event -> openStages.remove(title));

        return loader;

    }

    /**
     * Chiude la finestra (Stage) associata a un nodo specifico dell'interfaccia grafica.
     * Consente di chiudere la finestra corrente al click di un pulsante o all'interno di un evento.
     * 
     * @param source Il nodo grafico (es. un bottone) appartenente alla finestra che si desidera chiudere.
     */
    public static void close(Node source) {
        ((Stage) source.getScene().getWindow()).close();
    }
}
