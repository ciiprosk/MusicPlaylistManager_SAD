package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Track;
//import it.diem.unisa.musicmanager.service.TrackService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.Year;
import java.util.Arrays;

/**
 * Controller della finestra modale "Crea Brano" (addSong.fxml).
 * Si occupa solo della parte di interfaccia:
 * caricare un file audio dal PC e dedurne la durata;
 * leggere e controllare i campi inseriti dall'utente;
 * costruire un Track e passarlo al TrackService.
 * La logica di salvataggio (regole di dominio, duplicati, persistenza)
 * sta nel service, non qui. Il service viene passato dall'esterno
 * (da TracksController) tramite { setTrackService(TrackService)}.
 */
public class AddSongController {

    // --- Campi dell'interfaccia, collegati agli fx:id presenti in addSong.fxml ---
    @FXML private TextField fieldTitolo;
    @FXML private TextField fieldAutore;
    @FXML private TextField fieldAnno;
    @FXML private TextField fieldDurata;
    @FXML private ComboBox<Genre> comboGenere;
    @FXML private Button btnCarica;
    @FXML private Label lblFilePath;
    @FXML private Label lblError;
    @FXML private Button btnCrea;

    // Service per la gestione dei brani. Viene passato da chi apre il popup (TracksController).
    // private TrackService trackService;

    // Percorso del file audio scelto dall'utente. Salviamo solo il riferimento, non il file.
    private String audioPath;

    // Durata del brano in secondi, dedotta automaticamente dal file audio.
    private int songLengthSeconds;

    /**
     * Imposta il service da usare per salvare i brani.
     * Va chiamato da TracksController subito dopo aver caricato il popup,
     * prima che la finestra venga mostrata.
     *
     * @param trackService il service condiviso per la gestione dei brani

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }
*/
    /**
     * Metodo chiamato automaticamente da JavaFX appena la finestra e' pronta.
     * Lo usiamo per riempire la tendina dei generi.
     */
    @FXML
    private void initialize() {
        // Mettiamo nella tendina tutti i generi tranne UNKNOWN:
        // UNKNOWN e' solo il valore di default se l'utente non sceglie nulla.
        Arrays.stream(Genre.values())
                .filter(g -> g != Genre.UNKNOWN)
                .forEach(comboGenere.getItems()::add);
    }

    /**
     * Apre la finestra di scelta file, salva il percorso del file selezionato
     * e ne ricava la durata.
     *
     * @param e evento generato dal click sul pulsante "Carica traccia Audio"
     */
    @FXML
    private void onLoadAudio(ActionEvent e) {
        // Prepariamo la finestra di scelta file, filtrando solo i formati audio.
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleziona traccia audio");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("File audio", "*.mp3", "*.wav", "*.m4a", "*.aac"));

        // Recuperiamo la finestra corrente, serve per agganciarci il dialog.
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        File file = chooser.showOpenDialog(stage);

        // Se l'utente ha annullato, non facciamo nulla.
        if (file == null) {
            return;
        }

        // Salviamo il percorso del file e mostriamo il nome all'utente.
        audioPath = file.getAbsolutePath();
        lblFilePath.setText(file.getName());
        lblFilePath.setVisible(true);
        lblFilePath.setManaged(true);
        lblError.setText("");

        // Leggiamo la durata del file appena scelto.
        readDuration(file);
    }

    /**
     * Legge la durata del file audio usando JavaFX Media.
     * Attenzione: la durata non e' disponibile subito. Il file viene letto in
     * modo asincrono, quindi il valore arriva nel callback setOnReady.
     *
     * @param file il file audio di cui leggere la durata
     */
    private void readDuration(File file) {
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer player = new MediaPlayer(media);

            // Quando il file e' pronto, leggiamo la durata e la mostriamo.
            player.setOnReady(() -> {
                Duration d = media.getDuration();
                songLengthSeconds = (int) Math.round(d.toSeconds());
                fieldDurata.setText(formatDuration(d));
                player.dispose(); // liberiamo le risorse: ci serviva solo il dato
            });

            // Se il file e' rovinato o non leggibile, avvisiamo l'utente.
            player.setOnError(() -> {
                songLengthSeconds = 0;
                fieldDurata.setText("");
                lblError.setText("Impossibile leggere la durata del file.");
                player.dispose();
            });

        } catch (Exception ex) {
            songLengthSeconds = 0;
            fieldDurata.setText("");
            lblError.setText("File audio non valido.");
        }
    }

    /**
     * Trasforma una durata in una stringa leggibile nel formato minuti:secondi.
     *
     * @param d la durata da formattare
     * @return la durata come testo, es. "3:45"
     */
    private String formatDuration(Duration d) {
        int totSecondi = (int) Math.round(d.toSeconds());
        int minuti = totSecondi / 60;
        int secondi = totSecondi % 60;
        return String.format("%d:%02d", minuti, secondi);
    }

    /**
     * Restituisce il genere scelto dall'utente.
     *
     * @return il genere selezionato, oppure UNKNOWN se non e' stato scelto nulla
     */
    private Genre getSelectedGenre() {
        Genre selezionato = comboGenere.getValue();
        return (selezionato != null) ? selezionato : Genre.UNKNOWN;
    }

    /**
     * Gestisce il click su "Crea": controlla i campi, crea il brano e lo passa
     * al service per il salvataggio, poi chiude la finestra.
     * Se qualcosa non va, mostra un messaggio e non chiude.
     *
     * @param e evento generato dal click sul pulsante "Crea"

    @FXML
    private void onSave(ActionEvent e) {
        // Puliamo eventuali messaggi di errore precedenti.
        lblError.setText("");

        // Leggiamo i campi di testo (gestendo il caso in cui siano vuoti/null).
        String titolo = fieldTitolo.getText() == null ? "" : fieldTitolo.getText().trim();
        String autore = fieldAutore.getText() == null ? "" : fieldAutore.getText().trim();
        String anno   = fieldAnno.getText() == null ? "" : fieldAnno.getText().trim();

        // 1) Il titolo e' obbligatorio.
        if (titolo.isEmpty()) {
            lblError.setText("Il titolo è obbligatorio.");
            return;
        }
        // 2) Serve un file audio valido (da cui abbiamo letto una durata > 0).
        if (audioPath == null || songLengthSeconds <= 0) {
            lblError.setText("Carica un file audio valido.");
            return;
        }
        // 3) L'anno: se vuoto diventa UNKNOWN, altrimenti deve essere 4 cifre e non futuro.
        if (anno.isEmpty()) {
            anno = "UNKNOWN";
        } else {
            if (!anno.matches("\\d{4}")) {
                lblError.setText("L'anno deve essere di 4 cifre.");
                return;
            }
            if (Integer.parseInt(anno) > Year.now().getValue()) {
                lblError.setText("L'anno non può essere nel futuro.");
                return;
            }
        }

        // Tutti i controlli "lato utente" sono passati: creiamo il brano.
        Track nuovo = new Track(titolo, autore, getGenereSelezionato(), audioPath, songLengthSeconds, anno);

        // Il service applica le regole di dominio (validazione + duplicati) e salva.
        // Se qualcosa non va, lancia un'eccezione che mostriamo all'utente.
        try {
            trackService.addTrack(nuovo);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            lblError.setText(ex.getMessage());
            return;
        }

        // Salvataggio riuscito: chiudiamo la finestra.
        // La lista in Tracks si aggiorna da sola (ObservableList dello SharedState).
        chiudi(e);
    }
*/
    /**
     * Gestisce il click su "Annulla": chiude la finestra senza salvare nulla.
     *
     * @param e evento generato dal click sul pulsante "Annulla"
     */
    @FXML
    private void onCancel(ActionEvent e) {
        close(e);
    }

    /**
     * Chiude la finestra modale.
     * Ricava lo Stage a partire dal bottone che ha generato l'evento.
     *
     * @param e evento da cui risalire alla finestra da chiudere
     */
    private void close(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }
}