package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Controller della schermata "Genera Playlist".
 * Mostra una scelta tra anni, generi e tag disponibili. Si puo' selezionare
 * UN SOLO criterio alla volta (tutte le opzioni condividono un ToggleGroup).
 * Al click su "Genera" crea la playlist per il criterio selezionato.
 */
public class GeneratePlaylistController {

    @FXML private VBox yearBox;
    @FXML private VBox genreBox;
    @FXML private VBox tagBox;

    private PlaylistService playlistService;
    private TrackService trackService;

    // Un solo gruppo per tutte le opzioni: garantisce una sola selezione.
    private final ToggleGroup group = new ToggleGroup();

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
        tryPopulate();
    }

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
        tryPopulate();
    }

    private void tryPopulate() {
        if (trackService == null) return;
        populateYears();
        populateGenres();
        populateTags();
    }

    /** Un radio per ogni anno presente tra le tracce. */
    private void populateYears() {
        yearBox.getChildren().clear();
        Set<String> years = new TreeSet<>();
        for (Track t : trackService.getAllTracks()) {
            years.add(t.getYear());
        }
        for (String year : years) {
            RadioButton rb = new RadioButton(year);
            rb.setToggleGroup(group);
            rb.setUserData(new Criterion(CriterionType.YEAR, year));
            yearBox.getChildren().add(rb);
        }
    }

    /** Un radio per ogni genere presente tra le tracce. */
    private void populateGenres() {
        genreBox.getChildren().clear();
        Set<Genre> genres = new LinkedHashSet<>();
        for (Track t : trackService.getAllTracks()) {
            genres.add(t.getGenre());
        }
        for (Genre g : genres) {
            RadioButton rb = new RadioButton(g.toString());
            rb.setToggleGroup(group);
            rb.setUserData(new Criterion(CriterionType.GENRE, g));
            genreBox.getChildren().add(rb);
        }
    }

    /** Un radio per ogni tag esistente. */
    private void populateTags() {
        tagBox.getChildren().clear();
        for (Tag tag : Tag.values()) {
            RadioButton rb = new RadioButton(tag.toString());
            rb.setToggleGroup(group);
            rb.setUserData(new Criterion(CriterionType.TAG, tag));
            tagBox.getChildren().add(rb);
        }
    }

    /** Genera la playlist per il criterio selezionato. */
    @FXML
    private void handleGenerate(ActionEvent e) {
        if (playlistService == null) return;

        Toggle selected = group.getSelectedToggle();
        if (selected == null) {
            AlertUtil.showError("Nothing selected", "Select one filter.");
            return;
        }

        Criterion criterion = (Criterion) selected.getUserData();

        // In base al tipo, chiamo il generatore giusto.
        switch (criterion.type) {
            case YEAR  -> playlistService.generatePlaylistByYear((String) criterion.value)
                    .ifPresent(msg -> AlertUtil.showError("Error", msg));
            case GENRE -> playlistService.generatePlaylistByGenre((Genre) criterion.value)
                    .ifPresent(msg -> AlertUtil.showError("Error", msg));
            case TAG   -> playlistService.generatePlaylistByTag((Tag) criterion.value)
                    .ifPresent(msg -> AlertUtil.showError("Error", msg));
        }
    }

    // Tipi di criterio possibili.
    private enum CriterionType { YEAR, GENRE, TAG }

    // Piccola struttura per ricordare cosa rappresenta ogni radio:
    // il tipo (anno/genere/tag) e il valore vero (String, Genre o Tag).
    private static class Criterion {
        final CriterionType type;
        final Object value;
        Criterion(CriterionType type, Object value) {
            this.type = type;
            this.value = value;
        }
    }
}