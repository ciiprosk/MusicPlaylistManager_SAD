package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.model.Playlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}