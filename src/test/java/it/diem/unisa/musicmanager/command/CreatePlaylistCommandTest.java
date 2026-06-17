package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.state.SharedState;
import it.diem.unisa.musicmanager.dao.DAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class CreatePlaylistCommandTest {

    private PlaylistService playlistService;
    private CreatePlaylistCommand command;
    private final String playlistName = "New Playlist";

    @BeforeEach
    void setUp() {
        DAO<Playlist> fakeDAO = new DAO<Playlist>() {
            private final List<Playlist> list = new ArrayList<>();
            @Override public List<Playlist> selectAll() { return list; }
            @Override public void insert(Playlist p) { list.add(p); }
            @Override public void update(Playlist p) {}
            @Override public void delete(UUID id) { list.removeIf(p -> p.getId().equals(id)); }
            @Override public Optional<Playlist> searchById(UUID id) { return list.stream().filter(p -> p.getId().equals(id)).findFirst(); }
            @Override public boolean isDuplicated(Playlist p) { return list.stream().anyMatch(existing -> existing.getName().equalsIgnoreCase(p.getName())); }
        };

        SharedState state = new SharedState();
        playlistService = new PlaylistService(fakeDAO, state);
        command = new CreatePlaylistCommand(playlistService, playlistName);
    }

    @Test
    void testExecuteCreatesPlaylistSuccessfully() {
        Optional<String> result = command.execute();
        assertTrue(result.isEmpty());
        assertEquals(1, playlistService.getPlaylists().size());
        assertEquals(playlistName, playlistService.getPlaylists().get(0).getName());
    }

    @Test
    void testExecuteFailsWhenPlaylistAlreadyExists() {
        playlistService.createPlaylist(playlistName);

        Optional<String> result = command.execute();

        assertTrue(result.isPresent());
        assertEquals("A playlist with this name already exists!", result.get());
    }

    @Test
    void testUndoDeletesCreatedPlaylist() {
        command.execute(); 
        assertEquals(1, playlistService.getPlaylists().size());

        command.undo();
        assertEquals(0, playlistService.getPlaylists().size());
    }
}
