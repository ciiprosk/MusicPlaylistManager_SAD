package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.command.AddTrackCommand;
import it.diem.unisa.musicmanager.command.CommandManager;
import it.diem.unisa.musicmanager.exception.TrackInfoException;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.TagUtils;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

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
    @FXML
    private TextField fieldTitolo;
    @FXML
    private TextField fieldAutore;
    @FXML
    private TextField fieldAnno;
    @FXML
    private TextField fieldDurata;
    @FXML
    private ComboBox<Genre> comboGenere;
    @FXML
    private Button btnCarica;
    @FXML
    private Label lblFilePath;
    @FXML
    private Label lblError;
    @FXML
    private Button btnCrea;
    @FXML
    private ToggleButton btnExplicit;
    @FXML
    private ToggleButton btnFavorite;
    @FXML
    private ToggleButton btnNewRelease;

    // Service per la gestione dei brani. Viene passato da chi apre il popup (TracksController).
    private TrackService trackService;

    // Percorso del file audio scelto dall'utente. Salviamo solo il riferimento, non il file.
    private String audioPath;

    // Durata del brano in secondi, dedotta automaticamente dal file audio.
    private int songLengthSeconds;

    //per fare Command (add, delete e i rispettivi Undo)
    private CommandManager commandManager;

    /**
     * Imposta il service da usare per salvare i brani.
     * Va chiamato da TracksController subito dopo aver caricato il popup,
     * prima che la finestra venga mostrata.
     *
     * @param trackService il service condiviso per la gestione dei brani
     *
     */

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * Metodo chiamato automaticamente da JavaFX appena la finestra e' pronta.
     * Lo usiamo per riempire la tendina dei generi.
     */

    public void setCommandManager(CommandManager commandManager) {

        this.commandManager = commandManager;

    }

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
     * Gestisce il click su "Crea": controlla i campi, crea il brano e lo passa
     * al service per il salvataggio, poi chiude la finestra.
     * Se qualcosa non va, mostra un messaggio e non chiude.
     *
     * @param e evento generato dal click sul pulsante "Crea"
     */
    @FXML
    private void onSave(ActionEvent e) {

        lblError.setText("");

        String titolo = fieldTitolo.getText() == null ? "" : fieldTitolo.getText().trim();
        String autore = fieldAutore.getText() == null ? "" : fieldAutore.getText().trim();
        String anno = fieldAnno.getText() == null ? "" : fieldAnno.getText().trim();

        // UI check minimali
        if (audioPath == null || songLengthSeconds <= 0) {
            lblError.setText("Upload a valid audio file.");
            return;
        }

        Genre genre = comboGenere.getValue();
        if (genre == null) {
            genre = Genre.UNKNOWN;
        }

        try {

            // Validazione lasciata al costruttore
            Optional<String> result = commandManager.executeCommand(  // ← passa per il CommandManager
                    new AddTrackCommand(
                            trackService, titolo, autore, anno, audioPath,
                            genre, songLengthSeconds,
                            TagUtils.fromToggles(btnExplicit, btnFavorite, btnNewRelease)
                    )
            );
            if (result.isPresent()) {
                lblError.setText(result.get());
                return;
            }

            close(e);

        } catch (TrackInfoException ex) {
            // errori del MODEL (Track)
            lblError.setText(ex.getMessage());
        }
    }

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
        WindowUtil.close((Node) e.getSource());
    }


}