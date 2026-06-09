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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import java.util.Optional;

import java.util.Comparator;

/**
 * Controller della schermata "Genera Playlist".
 * Popola le tre colonne con RadioButton (anni, generi, tag) ricavati dalle
 * tracce effettivamente presenti nel catalogo. Tutte le opzioni condividono
 * un unico ToggleGroup: si seleziona UN SOLO criterio alla volta.
 */
public class GeneratePlaylistController {

    @FXML
    private VBox yearBox;
    @FXML
    private VBox genreBox;
    @FXML
    private VBox tagBox;
    @FXML
    private Button btnGenerate;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private TrackService trackService;
    private PlaylistService playlistService;

    // ───────────────────────────────────────────────────────────────────────
    //  Chiamato dal controller della Home DOPO aver aperto la finestra:
    //
    //  FXMLLoader loader = WindowUtil.openWindow(...);
    //  GeneratePlaylistController ctrl = loader.getController();
    //  ctrl.init(trackService, playlistService);
    // ───────────────────────────────────────────────────────────────────────
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
                .forEach(year -> {
                    RadioButton rb = createRadio(year, Integer.parseInt(year));
                    yearBox.getChildren().add(rb);
                });
    }

    private void populateGenres() {
        trackService.getAllTracks().stream()
                .map(Track::getGenre)
                .filter(g -> g != Genre.UNKNOWN)
                .distinct()
                .sorted(Comparator.comparing(Enum::name))
                .forEach(genre -> {
                    RadioButton rb = createRadio(genre.name(), genre);
                    genreBox.getChildren().add(rb);
                });
    }

    private void populateTags() {
        // Popola da enum. TagSpecification funzionera' solo quando
        // Track avra' un Set<Tag> con getTags().
        for (Tag tag : Tag.values()) {
            RadioButton rb = createRadio(tag.name(), tag);
            tagBox.getChildren().add(rb);
        }
    }

    private RadioButton createRadio(String label, Object userData) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(toggleGroup);
        rb.setUserData(userData);
        rb.setStyle("-fx-text-fill: white;");
        return rb;
    }

    // ── Genera Playlist ───────────────────────────────────────────────────

    @FXML
    private void onGenerate(ActionEvent event) {

        Toggle selected = toggleGroup.getSelectedToggle();
        if (selected == null) {
            AlertUtil.showError("Warning", "Please select a criterion before generating.");
            return;
        }

        Object value = selected.getUserData();
        Specification<Track> criteria;
        String playlistName;

        if (value instanceof Integer) {
            int year = (Integer) value;
            criteria = new YearSpecification(year);
            playlistName = "Playlist " + year;

        } else if (value instanceof Genre) {
            Genre genre = (Genre) value;
            criteria = new GenreSpecification(genre);
            playlistName = "Playlist " + genre.name();

        } else if (value instanceof Tag) {
            // Tag tag = (Tag) value;
            // criteria = new TagSpecification(tag);
            // playlistName = "Playlist " + tag.name();
            AlertUtil.showError("Not Available", "Generation by tag is not yet available.");
            return;

        } else {
            return;
        }

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
}