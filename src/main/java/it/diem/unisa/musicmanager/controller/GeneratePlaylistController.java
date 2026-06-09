package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.specification.GenreSpecification;
import it.diem.unisa.musicmanager.specification.Specification;
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

import java.util.Comparator;
import java.util.Optional;

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

    public void init(TrackService trackService, PlaylistService playlistService) {
        this.trackService = trackService;
        this.playlistService = playlistService;
        populateYears();
        populateGenres();
        populateTags();
    }

    // Popola le colonne

    private void populateYears() {
        trackService.getAllTracks().stream()
                .map(Track::getYear)
                .filter(y -> y != null && y.matches("\\d{4}"))
                .distinct()
                .sorted()
                .forEach(year -> yearBox.getChildren().add(
                        createCheckBox(year, Integer.parseInt(year))));
    }

    private void populateGenres() {
        trackService.getAllTracks().stream()
                .map(Track::getGenre)
                .filter(g -> g != Genre.UNKNOWN)
                .distinct()
                .sorted(Comparator.comparing(Enum::name))
                .forEach(genre -> genreBox.getChildren().add(
                        createCheckBox(genre.name(), genre)));
    }

    private void populateTags() {
        for (Tag tag : Tag.values()) {
            tagBox.getChildren().add(createCheckBox(tag.name(), tag));
        }
    }

    private CheckBox createCheckBox(String label, Object userData) {
        CheckBox cb = new CheckBox(label);
        cb.setUserData(userData);
        cb.setStyle("-fx-text-fill: white;");
        return cb;
    }

    // Costruzione della Specification composta

    /**
     * Combina in OR tutte le CheckBox selezionate di una colonna (VBox).
     * Ritorna Optional.empty() se nessuna e' selezionata in quella colonna.
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
     * Converte il valore userData di una CheckBox nella Specification corrispondente.
     */
    private Specification<Track> toSpecification(Object value) {
        if (value instanceof Integer) {
            return new YearSpecification((Integer) value);
        } else if (value instanceof Genre) {
            return new GenreSpecification((Genre) value);
        } else if (value instanceof Tag) {
            // return new TagSpecification((Tag) value);
            return null; // non disponibile finche' Track non ha getTags()
        }
        return null;
    }

    // Genera Playlist

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

        Optional<String> error = playlistService.generateAndSave(
                playlistName,
                trackService.getAllTracks(),
                criteria);

        if (error.isPresent()) {
            AlertUtil.showError("Error", error.get());
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Playlist Generated");
            alert.setHeaderText(null);
            alert.setContentText("Playlist \"" + playlistName + "\" created!");
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

    private void appendSelected(StringBuilder sb, VBox column) {
        for (Node node : column.getChildren()) {
            if (node instanceof CheckBox && ((CheckBox) node).isSelected()) {
                sb.append(" ").append(((CheckBox) node).getText());
            }
        }
    }
}