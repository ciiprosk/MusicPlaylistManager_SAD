package it.diem.unisa.musicmanager.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
 * Classe di utilità per la creazione e la visualizzazione di dialoghi di avviso (Alert) in JavaFX.
 * Fornisce metodi statici pronti all'uso per mostrare messaggi di errore, informazione o richiesta di conferma.
 * Questa classe è finale e non istanziabile.
 */
public final class AlertUtil {

    /**
     * Costruttore privato per prevenire l'istanziazione della classe di utilità.
     */
    private AlertUtil() {
        // Impedisce l'istanziazione
    }

    /**
     * Mostra una finestra di dialogo di errore (Alert di tipo ERROR).
     * Interrompe l'interazione con la finestra chiamante finché l'utente non chiude il dialogo.
     * 
     * @param title   Il titolo da visualizzare nella barra della finestra di avviso.
     * @param message Il testo dettagliato dell'errore da mostrare all'utente.
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra una finestra di dialogo informativa (Alert di tipo INFORMATION).
     * Utilizzato per segnalare all'utente il completamento di un'operazione o fornire comunicazioni.
     * 
     * @param title   Il titolo da visualizzare nella barra della finestra informativa.
     * @param message Il testo informativo da mostrare all'utente.
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra una finestra di dialogo di conferma (Alert di tipo CONFIRMATION).
     * Richiede all'utente di scegliere tra l'opzione "OK" e "Annulla" (o chiusura).
     * 
     * @param title   Il titolo da visualizzare nella barra della finestra di conferma.
     * @param message La domanda o il messaggio di conferma da mostrare all'utente.
     * @return {@code true} se l'utente clicca su "OK", {@code false} altrimenti o in caso di chiusura.
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
