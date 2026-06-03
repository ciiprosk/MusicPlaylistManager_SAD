package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.model.Playlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.UUID;

class JSONPlaylistDAOTest {

    private DAO<Playlist> playlistDAO;

    @BeforeEach
    void setUp() {

        File folder = new File("test-data");
        folder.mkdirs();

        new File(folder, "playlists-test.jsonl").delete();

        playlistDAO = new JSONPlaylistDAO(
                "test-data",
                "playlists-test.jsonl"
        );
    }

    //TEST PERSISTENZA FISICA REALE

    @Test
    void insertShouldSaveEmptyPlaylistOnFile() {

        Playlist playlist = new Playlist("Rock");

        playlistDAO.insert(playlist);

        DAO<Playlist> newPlaylistDAO =
                new JSONPlaylistDAO(
                        "test-data",
                        "playlists-test.jsonl"
                );

        Optional<Playlist> result =
                newPlaylistDAO.searchById(playlist.getId());

        assertTrue(result.isPresent());
        assertEquals("Rock", result.get().getName());
        assertTrue(result.get().getTracks().isEmpty());
    }

    //TEST ALL'AGGIUNTA DI UN BRANO NELLA PLAYLIST SI DEVE AGGIORNARE IL FILE

    @Test
    void updateShouldSavePlaylistWithNewTracksOnFile() {

        Playlist playlist = new Playlist("Pop");

        playlistDAO.insert(playlist);

        UUID trackId = UUID.randomUUID();

        playlist.addTrack(trackId);

        playlistDAO.update(playlist);

        DAO<Playlist> newPlaylistDAO =
                new JSONPlaylistDAO(
                        "test-data",
                        "playlists-test.jsonl"
                );

        Optional<Playlist> result =
                newPlaylistDAO.searchById(playlist.getId());

        assertTrue(result.isPresent());
        assertEquals("Pop", result.get().getName());
        assertEquals(1, result.get().getTracks().size());
        assertTrue(result.get().containsTrack(trackId));
    }
}