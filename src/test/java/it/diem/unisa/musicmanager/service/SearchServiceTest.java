package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SearchServiceTest {

    private SharedState sharedState;
    private TrackService trackService;
    private PlaylistService playlistService;

    @BeforeEach
    void setUp() {

        sharedState =
                new SharedState();

        trackService =
                new TrackService(
                        new FakeTrackDAO(),
                        sharedState
                );

        playlistService =
                new PlaylistService(
                        new FakePlaylistDAO(),
                        sharedState
                );
    }

    // TEST: LA RICERCA TRACCE PER TITOLO È CASE-INSENSITIVE

    @Test
    void searchTracksShouldFindTrackByTitleIgnoringCase() {

        Track numb =
                createTrack(
                        "Numb",
                        "Linkin Park"
                );

        Track yellow =
                createTrack(
                        "Yellow",
                        "Coldplay"
                );

        sharedState.getALlTracks().add(numb);
        sharedState.getALlTracks().add(yellow);

        List<Track> lowerCaseResult =
                trackService.searchTracks("numb");

        List<Track> upperCaseResult =
                trackService.searchTracks("NUMB");

        assertEquals(
                1,
                lowerCaseResult.size()
        );

        assertEquals(
                1,
                upperCaseResult.size()
        );

        assertEquals(
                numb.getId(),
                lowerCaseResult.get(0).getId()
        );

        assertEquals(
                numb.getId(),
                upperCaseResult.get(0).getId()
        );
    }

    // TEST: LA RICERCA TRACCE PER AUTORE È CASE-INSENSITIVE

    @Test
    void searchTracksShouldFindTrackByAuthorIgnoringCase() {

        Track numb =
                createTrack(
                        "Numb",
                        "Linkin Park"
                );

        Track yellow =
                createTrack(
                        "Yellow",
                        "Coldplay"
                );

        sharedState.getALlTracks().add(numb);
        sharedState.getALlTracks().add(yellow);

        List<Track> result =
                trackService.searchTracks("linkin");

        List<Track> upperCaseResult =
                trackService.searchTracks("LINKIN");

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                1,
                upperCaseResult.size()
        );

        assertEquals(
                numb.getId(),
                result.get(0).getId()
        );

        assertEquals(
                numb.getId(),
                upperCaseResult.get(0).getId()
        );
    }

    // TEST: LA RICERCA TRACCE RESTITUISCE SOLO LE TRACCE ATTESE

    @Test
    void searchTracksShouldReturnOnlyExpectedTracks() {

        Track numb =
                createTrack(
                        "Numb",
                        "Linkin Park"
                );

        Track inTheEnd =
                createTrack(
                        "In The End",
                        "Linkin Park"
                );

        Track yellow =
                createTrack(
                        "Yellow",
                        "Coldplay"
                );

        sharedState.getALlTracks().add(numb);
        sharedState.getALlTracks().add(inTheEnd);
        sharedState.getALlTracks().add(yellow);

        List<Track> result =
                trackService.searchTracks("linkin");

        assertEquals(
                2,
                result.size()
        );

        assertTrue(
                result.stream().anyMatch(track -> track.getId().equals(numb.getId()))
        );

        assertTrue(
                result.stream().anyMatch(track -> track.getId().equals(inTheEnd.getId()))
        );

        assertFalse(
                result.stream().anyMatch(track -> track.getId().equals(yellow.getId()))
        );
    }

    // TEST: SE LA RICERCA TRACCE È VUOTA, RESTITUISCE TUTTE LE TRACCE

    @Test
    void searchTracksShouldReturnAllTracksWhenKeywordIsBlank() {

        Track firstTrack =
                createTrack(
                        "Numb",
                        "Linkin Park"
                );

        Track secondTrack =
                createTrack(
                        "Yellow",
                        "Coldplay"
                );

        sharedState.getALlTracks().add(firstTrack);
        sharedState.getALlTracks().add(secondTrack);

        List<Track> result =
                trackService.searchTracks("");

        assertEquals(
                2,
                result.size()
        );
    }

    // TEST: SE NESSUNA TRACCIA CORRISPONDE, RESTITUISCE LISTA VUOTA

    @Test
    void searchTracksShouldReturnEmptyListWhenNoTrackMatches() {

        Track track =
                createTrack(
                        "Numb",
                        "Linkin Park"
                );

        sharedState.getALlTracks().add(track);

        List<Track> result =
                trackService.searchTracks("metallica");

        assertTrue(
                result.isEmpty()
        );
    }

    // TEST: LA RICERCA PLAYLIST È CASE-INSENSITIVE

    @Test
    void searchPlaylistsShouldFindPlaylistIgnoringCase() {

        Playlist rockPlaylist =
                new Playlist("Rock Classics");

        Playlist popPlaylist =
                new Playlist("Pop Hits");

        sharedState.getALlPlaylists().add(rockPlaylist);
        sharedState.getALlPlaylists().add(popPlaylist);

        List<Playlist> lowerCaseResult =
                playlistService.searchPlaylists("rock");

        List<Playlist> upperCaseResult =
                playlistService.searchPlaylists("ROCK");

        assertEquals(
                1,
                lowerCaseResult.size()
        );

        assertEquals(
                1,
                upperCaseResult.size()
        );

        assertEquals(
                rockPlaylist.getId(),
                lowerCaseResult.get(0).getId()
        );

        assertEquals(
                rockPlaylist.getId(),
                upperCaseResult.get(0).getId()
        );
    }

    // TEST: LA RICERCA PLAYLIST RESTITUISCE SOLO LE PLAYLIST ATTESE

    @Test
    void searchPlaylistsShouldReturnOnlyExpectedPlaylists() {

        Playlist rockPlaylist =
                new Playlist("Rock Classics");

        Playlist workoutPlaylist =
                new Playlist("Workout Rock");

        Playlist popPlaylist =
                new Playlist("Pop Hits");

        sharedState.getALlPlaylists().add(rockPlaylist);
        sharedState.getALlPlaylists().add(workoutPlaylist);
        sharedState.getALlPlaylists().add(popPlaylist);

        List<Playlist> result =
                playlistService.searchPlaylists("rock");

        assertEquals(
                2,
                result.size()
        );

        assertTrue(
                result.stream().anyMatch(playlist -> playlist.getId().equals(rockPlaylist.getId()))
        );

        assertTrue(
                result.stream().anyMatch(playlist -> playlist.getId().equals(workoutPlaylist.getId()))
        );

        assertFalse(
                result.stream().anyMatch(playlist -> playlist.getId().equals(popPlaylist.getId()))
        );
    }

    // TEST: SE LA RICERCA PLAYLIST È VUOTA, RESTITUISCE TUTTE LE PLAYLIST

    @Test
    void searchPlaylistsShouldReturnAllPlaylistsWhenKeywordIsBlank() {

        Playlist firstPlaylist =
                new Playlist("Rock Classics");

        Playlist secondPlaylist =
                new Playlist("Pop Hits");

        sharedState.getALlPlaylists().add(firstPlaylist);
        sharedState.getALlPlaylists().add(secondPlaylist);

        List<Playlist> result =
                playlistService.searchPlaylists("");

        assertEquals(
                2,
                result.size()
        );
    }

    // TEST: SE NESSUNA PLAYLIST CORRISPONDE, RESTITUISCE LISTA VUOTA

    @Test
    void searchPlaylistsShouldReturnEmptyListWhenNoPlaylistMatches() {

        Playlist playlist =
                new Playlist("Rock Classics");

        sharedState.getALlPlaylists().add(playlist);

        List<Playlist> result =
                playlistService.searchPlaylists("jazz");

        assertTrue(
                result.isEmpty()
        );
    }

    // METODO DI SUPPORTO PER CREARE TRACCE VALIDE

    private Track createTrack(String title, String author) {

        return new Track(
                title,
                author,
                Genre.ROCK,
                "songs/" + title + ".mp3",
                180,
                "2020",
                EnumSet.noneOf(Tag.class)
        );
    }

    // DAO FALSO PER I TEST DI TRACKSERVICE

    private static class FakeTrackDAO implements DAO<Track> {

        @Override
        public List<Track> selectAll() {
            return new ArrayList<>();
        }

        @Override
        public void insert(Track track) {
        }

        @Override
        public void update(Track track) {
        }

        @Override
        public void delete(UUID id) {
        }

        @Override
        public Optional<Track> searchById(UUID id) {
            return Optional.empty();
        }

        @Override
        public boolean isDuplicated(Track track) {
            return false;
        }
    }

    // DAO FALSO PER I TEST DI PLAYLISTSERVICE

    private static class FakePlaylistDAO implements DAO<Playlist> {

        @Override
        public List<Playlist> selectAll() {
            return new ArrayList<>();
        }

        @Override
        public void insert(Playlist playlist) {
        }

        @Override
        public void update(Playlist playlist) {
        }

        @Override
        public void delete(UUID id) {
        }

        @Override
        public Optional<Playlist> searchById(UUID id) {
            return Optional.empty();
        }

        @Override
        public boolean isDuplicated(Playlist playlist) {
            return false;
        }
    }
}