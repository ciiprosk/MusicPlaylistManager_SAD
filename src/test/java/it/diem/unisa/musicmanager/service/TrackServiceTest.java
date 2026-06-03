package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.dao.JSONPlaylistDAO;
import it.diem.unisa.musicmanager.dao.JSONTrackDAO;
import it.diem.unisa.musicmanager.exception.TrackInfoException;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrackServiceTest {

    private TrackService service;
    private SharedState sharedState;

    @BeforeEach
    void setUp() {
        File folder = new File("test-data");
        folder.mkdirs();

        new File(folder, "tracks-service-test.jsonl").delete();
        new File(folder, "playlists-service-test.jsonl").delete();

        DAO<Track> trackDAO = new JSONTrackDAO("test-data", "tracks-service-test.jsonl");
        DAO<Playlist> playlistDAO = new JSONPlaylistDAO("test-data", "playlists-service-test.jsonl");

        sharedState = new SharedState();
        service = new TrackService(trackDAO, sharedState);
    }

    //TEST AGGIUNTA TRACCIA NEGATA CON CAMPO TITOLO VUOTO

    @Test
    void addTrackShouldFailWhenTitleIsEmpty() {
        Optional<String> result = service.addTrack(
                "",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIUNTA TRACCIA NEGATA CON CAMPO TITOLO COMPOSTO DI SOLI SPAZI

    @Test
    void addTrackShouldFailWhenTitleIsOnlySpaces() {
        Optional<String> result = service.addTrack(
                "   ",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIUNTA TRACCIA NEGATA CON CAMPO TITOLO MAGGIORE DI 100 CARATTERI

    @Test
    void addTrackShouldFailWhenTitleIsLongerThan100Characters() {
        String longTitle = "A".repeat(101);

        Optional<String> result = service.addTrack(
                longTitle,
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIUNTA TRACCIA NEGATA CON CAMPO AUTORE MAGGIORE DI 100 CARATTERI

    @Test
    void addTrackShouldFailWhenAuthorIsLongerThan100Characters() {
        String longAuthor = "A".repeat(101);

        Optional<String> result = service.addTrack(
                "Valid Title",
                longAuthor,
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIUNTA TRACCIA NEGATA CON DURATA TRACCIA 0

    @Test
    void addTrackShouldFailWhenSongLengthIsZero() {
        Optional<String> result = service.addTrack(
                "Valid Title",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                0,
                "2020"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIUNTA TRACCIA NEGATA CON DURATA TRACCIA NEGATIVA

    @Test
    void addTrackShouldFailWhenSongLengthIsNegative() {
        Optional<String> result = service.addTrack(
                "Valid Title",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                -10,
                "2020"
        );

        assertTrue(result.isPresent());
    }

    //AGGIUNTA TRACCIA NEGATA CON ANNO AVENTE MENO DI 4 CIFRE

    @Test
    void addTrackShouldFailWhenYearHasNotFourDigits() {
        Optional<String> result = service.addTrack(
                "Valid Title",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "123"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIUNTA TRACCIA NEGATA QUANDO ANNO è FUTURO

    @Test
    void addTrackShouldFailWhenYearIsFuture() {
        String futureYear = String.valueOf(java.time.Year.now().getValue() + 1);

        Optional<String> result = service.addTrack(
                "Valid Title",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                futureYear
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIUNTA TRACCIA NEGATA QUANDO TRACCIA HA TUTTI I CAMPI DUPLICATI

    @Test
    void addTrackShouldFailWhenTrackIsDuplicated() {
        service.addTrack(
                "Numb",
                "Linkin Park",
                Genre.ROCK,
                "songs/numb.mp3",
                185,
                "2003"
        );

        Optional<String> result = service.addTrack(
                "Numb",
                "Linkin Park",
                Genre.ROCK,
                "songs/numb-copy.mp3",
                190,
                "2003"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIUNTA TRACCIA AVVENUTA CON SUCCESSO QUANDO I DATI SONO VALIDI

    @Test
    void addTrackShouldSucceedWhenDataIsValid() {
        Optional<String> result = service.addTrack(
                "Numb",
                "Linkin Park",
                Genre.ROCK,
                "songs/numb.mp3",
                185,
                "2003"
        );

        assertTrue(result.isEmpty());
        assertEquals(1, sharedState.getALlTracks().size());
    }

    //TEST AGGIUNTA TRACCIA USA AUTORE DEFAULT CON CAMPO AUTORE VUOTO

    @Test
    void addTrackShouldUseDefaultAuthorWhenAuthorIsEmpty() {
        Optional<String> result = service.addTrack(
                "Song Without Author",
                "",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        assertTrue(result.isEmpty());
        assertEquals("Unknown", sharedState.getALlTracks().get(0).getAuthor());
    }

    //TEST AGGIUNTA TRACCIA USA GENERE DEFAULT CON CAMPO GENERE VUOTO

    @Test
    void addTrackShouldUseDefaultGenreWhenGenreIsNull() {
        Optional<String> result = service.addTrack(
                "Song Without Genre",
                "Author",
                null,
                "songs/test.mp3",
                180,
                "2020"
        );

        assertTrue(result.isEmpty());
        assertEquals(Genre.UNKNOWN, sharedState.getALlTracks().get(0).getGenre());
    }

    //TEST AGGIUNTA TRACCIA USA ANNO DI DEFAULT CON CAMPO ANNO VUOTO

    @Test
    void addTrackShouldUseDefaultYearWhenYearIsEmpty() {
        Optional<String> result = service.addTrack(
                "Song Without Year",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                ""
        );

        assertTrue(result.isEmpty());
        assertEquals("UNKNOWN", sharedState.getALlTracks().get(0).getYear());
    }

    //TEST AGGIORNAMENTO NEGATO CON TITOLO VUOTO

    @Test
    void updateTrackShouldFailWhenTitleIsEmpty() {
        service.addTrack(
                "Valid Title",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        Track track = sharedState.getALlTracks().get(0);

        Optional<String> result = service.updateTrack(
                track.getId(),
                "",
                "Author",
                Genre.ROCK,
                "2020"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIORNAMENTO NEGATO CON TITOLO PIù GRANDE DI 100 CARATTERI

    @Test
    void updateTrackShouldFailWhenTitleIsLongerThan100Characters() {
        service.addTrack(
                "Valid Title",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        Track track = sharedState.getALlTracks().get(0);
        String longTitle = "A".repeat(101);

        Optional<String> result = service.updateTrack(
                track.getId(),
                longTitle,
                "Author",
                Genre.ROCK,
                "2020"
        );

        assertTrue(result.isPresent());
    }

    //AGGIORNAMNETO NEGATO CON NOME AUTORE PIù GRANDE DI 100 CARATTERI

    @Test
    void updateTrackShouldFailWhenAuthorIsLongerThan100Characters() {
        service.addTrack(
                "Valid Title",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        Track track = sharedState.getALlTracks().get(0);
        String longAuthor = "A".repeat(101);

        Optional<String> result = service.updateTrack(
                track.getId(),
                "Valid Title",
                longAuthor,
                Genre.ROCK,
                "2020"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIORNAMENTO NEGATO CON ANNO AVENTE MENO DI 4 CIFRE

    @Test
    void updateTrackShouldFailWhenYearHasNotFourDigits() {
        service.addTrack(
                "Valid Title",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        Track track = sharedState.getALlTracks().get(0);

        Optional<String> result = service.updateTrack(
                track.getId(),
                "Valid Title",
                "Author",
                Genre.ROCK,
                "12"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIORNAMENTO NEGATO CON ANNO FUTURO

    @Test
    void updateTrackShouldFailWhenYearIsFuture() {
        service.addTrack(
                "Valid Title",
                "Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        Track track = sharedState.getALlTracks().get(0);
        String futureYear = String.valueOf(java.time.Year.now().getValue() + 1);

        Optional<String> result = service.updateTrack(
                track.getId(),
                "Valid Title",
                "Author",
                Genre.ROCK,
                futureYear
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIORNAMNETO NEGATO QUANDO TRACCIA DIVENTA UN DUPLICATO

    @Test
    void updateTrackShouldFailWhenTrackBecomesDuplicated() {
        service.addTrack(
                "Numb",
                "Linkin Park",
                Genre.ROCK,
                "songs/numb.mp3",
                185,
                "2003"
        );

        service.addTrack(
                "Faint",
                "Linkin Park",
                Genre.ROCK,
                "songs/faint.mp3",
                160,
                "2003"
        );

        Track faint = sharedState.getALlTracks().get(1);

        Optional<String> result = service.updateTrack(
                faint.getId(),
                "Numb",
                "Linkin Park",
                Genre.ROCK,
                "2003"
        );

        assertTrue(result.isPresent());
    }

    //TEST AGGIORNAMENTO AVVENUTO CON SUCCESSO CON DATI VALIDI

    @Test
    void updateTrackShouldSucceedWhenDataIsValid() {
        service.addTrack(
                "Old Title",
                "Old Author",
                Genre.ROCK,
                "songs/test.mp3",
                180,
                "2020"
        );

        Track track = sharedState.getALlTracks().get(0);

        Optional<String> result = service.updateTrack(
                track.getId(),
                "New Title",
                "New Author",
                Genre.POP,
                "2021"
        );

        assertTrue(result.isEmpty());
        assertEquals("New Title", sharedState.getALlTracks().get(0).getTitle());
        assertEquals("New Author", sharedState.getALlTracks().get(0).getAuthor());
        assertEquals(Genre.POP, sharedState.getALlTracks().get(0).getGenre());
        assertEquals("2021", sharedState.getALlTracks().get(0).getYear());
    }

    //TEST RIMOZIONE RIFERIMENTI DI UNA TRACCIA DALLE PLAYLIST IN CUI ERA PRESENTE DOPO LA RIMOZIONE

    @Test
    void deleteTrackShouldRemoveTrackReferenceFromAllPlaylists() {
        service.addTrack(
                "Numb",
                "Linkin Park",
                Genre.ROCK,
                "songs/numb.mp3",
                185,
                "2003"
        );

        Track track = sharedState.getALlTracks().get(0);

        Playlist playlist1 = new Playlist("Playlist 1");
        Playlist playlist2 = new Playlist("Playlist 2");

        playlist1.addTrack(track.getId());
        playlist2.addTrack(track.getId());

        sharedState.getALlPlaylists().add(playlist1);
        sharedState.getALlPlaylists().add(playlist2);

        // --- FIX OBSERVER: Simuliamo che qualcuno stia ascoltando la notifica ---
        service.addObserver(deletedId -> {
            for (Playlist p : sharedState.getALlPlaylists()) {
                p.removeTrack(deletedId);
            }
        });
        // -----------------------------------------------------------------------

        service.deleteTrack(track.getId());

        assertFalse(playlist1.containsTrack(track.getId()));
        assertFalse(playlist2.containsTrack(track.getId()));
        assertFalse(sharedState.getALlTracks().contains(track));
    }

    //TEST RIMOZIONE TRACCIA DA PLAYLIST NON DEVE RIMUOVERE TRACCIA DALL'ARCHIVIO

    @Test
    void removeTrackFromPlaylistShouldNotDeleteTrackFromGeneralArchive() {

        Optional<String> result =
                service.addTrack(
                        "Numb",
                        "Linkin Park",
                        Genre.ROCK,
                        "songs/numb.mp3",
                        185,
                        "2003"
                );

        assertTrue(result.isEmpty());

        Track track =
                sharedState.getALlTracks().get(0);

        Playlist playlist =
                new Playlist("Rock");

        playlist.addTrack(track.getId());

        sharedState.getALlPlaylists().add(playlist);

        playlist.removeTrack(track.getId());

        assertFalse(playlist.containsTrack(track.getId()));

        assertTrue(
                sharedState.getALlTracks()
                        .stream()
                        .anyMatch(t -> t.getId().equals(track.getId()))
        );
    }
}