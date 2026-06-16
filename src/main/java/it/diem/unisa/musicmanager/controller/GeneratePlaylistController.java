package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.specification.GenreSpecification;
import it.diem.unisa.musicmanager.specification.Specification;
import it.diem.unisa.musicmanager.specification.TagSpecification;
import it.diem.unisa.musicmanager.specification.YearSpecification;
import it.diem.unisa.musicmanager.util.AlertUtil;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;

import it.diem.unisa.musicmanager.command.Command;
import it.diem.unisa.musicmanager.command.CommandManager;
import it.diem.unisa.musicmanager.command.GeneratePlaylistCommand;

/**
 * Controller della schermata "Genera Playlist".
 * Usa CheckBox al posto di RadioButton: l'utente puo' selezionare piu' criteri.
 * Dentro la stessa colonna i criteri si combinano in OR (anno 1999 O 2000),
 * tra colonne diverse si combinano in AND (anno 1999 E genere ROCK).
 */
public class GeneratePlaylistController {

    @FXML private VBox yearBox;
    @FXML private VBox genreBox;
    @FXML private VBox tagBox;
    @FXML private Button btnGenerate;

    private TrackService trackService;
    private PlaylistService playlistService;

    private CommandManager commandManager;
    /**
     * Inizializza il controller iniettando i servizi necessari e avviando
     * il popolamento dinamico dei componenti grafici della UI.
     *
     * @param trackService    il servizio per la gestione e il recupero delle tracce musicali
     * @param playlistService il servizio per la generazione e il salvataggio delle playlist
     */
    public void init(TrackService trackService, PlaylistService playlistService, CommandManager commandManager) {
        this.trackService = trackService;
        this.playlistService = playlistService;
        this.commandManager = commandManager;
        populateYears();
        populateGenres();
        populateTags();
    }

    // Popola le colonne
    /**
     * Estrae dinamicamente gli anni di pubblicazione da tutte le tracce disponibili,
     * li ordina in modo crescente e popola il contenitore grafico dedicato con le relative CheckBox.
     * Vengono considerati validi solo gli anni non nulli composti da esattamente 4 cifre.
     */
    private void populateYears() {
        trackService.getAllTracks().stream()
                .map(Track::getYear)
                .filter(y -> y != null && y.matches("\\d{4}"))
                .distinct()
                .sorted()
                .forEach(year -> yearBox.getChildren().add(
                        createCheckBox(year, Integer.parseInt(year))));
    }

    /**
     * Estrae dinamicamente i generi musicali da tutte le tracce disponibili,
     * li ordina alfabeticamente e popola il contenitore grafico dedicato con le relative CheckBox.
     * Il genere di fallback {@code Genre.UNKNOWN} viene escluso dal popolamento.
     */
    private void populateGenres() {
        trackService.getAllTracks().stream()
                .map(Track::getGenre)
                .filter(g -> g != Genre.UNKNOWN)
                .distinct()
                .sorted(Comparator.comparing(Enum::name))
                .forEach(genre -> genreBox.getChildren().add(
                        createCheckBox(genre.name(), genre)));
    }

    /**
     * Estrae dinamicamente i tag da tutte le tracce disponibili analizzando le rispettive collezioni,
     * li ordina alfabeticamente ed elimina i duplicati prima di popolare il contenitore grafico dedicato.
     * Questo approccio garantisce che vengano mostrati solo i tag effettivamente associati ad almeno una traccia.
     */
    private void populateTags() {
        trackService.getAllTracks().stream()
                .map(Track::getTags) // Supponendo che restituisca una List<Tag> o Set<Tag>
                .filter(tags -> tags != null)
                .flatMap(Collection::stream) // "Appiattisce" le liste di tag in un unico stream di singoli tag
                .distinct()                  // Elimina i duplicati
                .sorted(Comparator.comparing(Enum::name)) // Ordina alfabeticamente
                .forEach(tag -> tagBox.getChildren().add(
                        createCheckBox(tag.name(), tag)));
    }

    /**
     * Fabbrica un'istanza di {@link CheckBox} configurata con un testo descrittivo,
     * uno stile CSS personalizzato per il testo e un oggetto di business associato come user data.
     *
     * @param label    il testo da mostrare accanto alla CheckBox
     * @param userData l'oggetto di dominio (es. Integer, Genre, Tag) da associare alla CheckBox
     * @return un'istanza di {@link CheckBox} pronta per essere inserita nella UI
     */
    private CheckBox createCheckBox(String label, Object userData) {
        CheckBox cb = new CheckBox(label);
        cb.setUserData(userData);
        cb.setStyle("-fx-text-fill: white;");
        return cb;
    }

    // Costruzione della Specification composta

    /**
     * Analizza le CheckBox selezionate in una {@link VBox} e ne combina i criteri in OR
     * Scorre i nodi della colonna: per ogni CheckBox spuntata, converte il suo {@code userData}
     * in una specifica tramite {@link #toSpecification(Object)} e la concatena alle altre
     * usando il metodo {@link Specification#or(Specification)}.
     *
     * @param column il contenitore grafico della UI da analizzare
     * @return un {@link Optional} con la specifica combinata in OR, oppure {@link Optional#empty()}
     * se nessuna CheckBox è selezionata
     */
    private Optional<Specification<Track>> buildColumnSpec(VBox column) {
        Specification<Track> combined = null;

        for (Node node : column.getChildren()) {
            if (node instanceof CheckBox && ((CheckBox) node).isSelected()) {
                Specification<Track> spec = toSpecification(((CheckBox) node).getUserData());
                if (spec == null) continue;
                combined = (combined == null) ? spec : combined.or(spec);
            }
        }

        return Optional.ofNullable(combined);
    }

    /**
     * Converte l'oggetto {@code userData} di una CheckBox nella relativa {@link Specification}
     * Riconosce il tipo di dato e istanzia la specifica concreta corrispondente
     * (anno, genere o tag). Restituisce {@code null} se il tipo non è gestito.
     * @param value l'oggetto estratto dalla CheckBox (Integer, Genre o Tag)
     * @return la specifica concreta adatta al tipo, oppure {@code null}
     */
    private Specification<Track> toSpecification(Object value) {
        if (value instanceof Integer) {
            return new YearSpecification((Integer) value);
        } else if (value instanceof Genre) {
            return new GenreSpecification((Genre) value);
        } else if (value instanceof Tag) {
            return new TagSpecification((Tag) value);
        }
        return null;
    }

    // Genera Playlist
    /**
     * Gestisce l'evento di generazione della playlist al click sul pulsante.
     * Recupera le specifiche dalle tre colonne (anni, generi, tag) tramite {@link #buildColumnSpec},
     * le combina in AND logico e delega al servizio il filtraggio e il salvataggio.
     * Mostra un alert di errore se non ci sono selezioni o se il salvataggio fallisce.
     * @param event l'evento di azione scatenato dalla UI
     */
    @FXML
    private void onGenerate(ActionEvent event) {

        // Combina le colonne in AND tra loro
        Optional<Specification<Track>> yearSpec  = buildColumnSpec(yearBox);
        Optional<Specification<Track>> genreSpec = buildColumnSpec(genreBox);
        Optional<Specification<Track>> tagSpec   = buildColumnSpec(tagBox);

        // Unisci tutte le colonne selezionate con AND
        Specification<Track> criteria = null;
        if (yearSpec.isPresent())  criteria = yearSpec.get();
        if (genreSpec.isPresent()) criteria = (criteria == null) ? genreSpec.get() : criteria.and(genreSpec.get());
        if (tagSpec.isPresent())   criteria = (criteria == null) ? tagSpec.get()   : criteria.and(tagSpec.get());

        if (criteria == null) {
            AlertUtil.showError("Warning", "Please select at least one criterion before generating.");
            return;
        }

        // Costruisci il nome automaticamente dai criteri selezionati
        String playlistName = buildPlaylistName();

        // Verifica se la playlist esiste già
        boolean exists = playlistService.getPlaylists().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(playlistName));
        if (exists) {
            boolean confirm = AlertUtil.showConfirmation(
                    "Playlist Exists",
                    "A playlist named \"" + playlistName + "\" already exists. Do you want to overwrite it with the new tracks?"
            );
            if (!confirm) {
                return; // Interrompe se l'utente seleziona "No" / "Annulla"
            }
        }

        // creo l'oggetto command per la generazione delle playlist
        Command command = new GeneratePlaylistCommand(playlistService, playlistName, trackService.getAllTracks(), criteria);
//        Optional<String> error = playlistService.generateAndSave(
//                playlistName,
//                trackService.getAllTracks(),
//                criteria);
        // eseguo il comando e repureo il messaggio di ritorno
        Optional<String> error = commandManager.executeCommand(command);
        if (error.isPresent()) {
            AlertUtil.showError("Error", error.get());
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Playlist Generated");
            alert.setHeaderText(null);
            if (exists) {
                alert.setContentText("Playlist \"" + playlistName + "\" updated!");
            } else {
                alert.setContentText("Playlist \"" + playlistName + "\" created!");
            }
            alert.showAndWait();
            WindowUtil.close(btnGenerate);
        }
    }

    /**
     * Costruisce il nome della playlist dalle selezioni attive.
     * Esempio: "Playlist ROCK - 1999, 2000"
     */
    private String buildPlaylistName() {
        StringBuilder sb = new StringBuilder("Playlist");

        appendSelected(sb, genreBox);
        appendSelected(sb, yearBox);
        appendSelected(sb, tagBox);

        return sb.toString().trim();
    }
    /**
     * Scorre gli elementi di una colonna visiva e aggiunge al costruttore di stringhe
     * il testo di ogni CheckBox selezionata.
     * Viene utilizzato per raccogliere i nomi dei filtri attivi e generare
     * automaticamente il titolo della playlist.
     * @param sb     il costruttore di stringhe su cui accumulare i testi
     * @param column il contenitore grafico della UI da cui leggere le selezioni
     */
    private void appendSelected(StringBuilder sb, VBox column) {
        for (Node node : column.getChildren()) {
            if (node instanceof CheckBox && ((CheckBox) node).isSelected()) {
                sb.append(" ").append(((CheckBox) node).getText());
            }
        }
    }
}