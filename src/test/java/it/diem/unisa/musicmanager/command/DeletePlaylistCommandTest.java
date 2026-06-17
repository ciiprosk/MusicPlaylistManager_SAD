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

class DeletePlaylistCommandTest {

    private PlaylistService playlistService;
    private DeletePlaylistCommand command;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        DAO<Playlist> fakeDAO = new DAO<Playlist>() {
            private final List<Playlist> list = new ArrayList<>();
            @Override public List<Playlist> selectAll() { return list; }
            @Override public void insert(Playlist p) { list.add(p); }
            @Override public void update(Playlist p) {}
            @Override public void delete(UUID id) { list.removeIf(p -> p.getId().equals(id)); }
            @Override public Optional<Playlist> searchById(UUID id) { return list.stream().filter(p -> p.getId().equals(id)).findFirst(); }
            @Override public boolean isDuplicated(Playlist p) { return false; }
        };

        SharedState state = new SharedState();
        playlistService = new PlaylistService(fakeDAO, state);
        
        playlist = new Playlist("My Rock Playlist");
        playlistService.restorePlaylist(playlist);

        command = new DeletePlaylistCommand(playlistService, playlist.getId());
    }

    @Test
    void testUndoRestoresDeletedPlaylistToArchive() {
        assertEquals(1, playlistService.getPlaylists().size());

        Optional<String> error = command.execute();
        assertTrue(error.isEmpty());
        assertEquals(0, playlistService.getPlaylists().size());

        command.undo();
        
        assertEquals(1, playlistService.getPlaylists().size());
        Playlist restored = playlistService.getPlaylists().get(0);
        assertEquals("My Rock Playlist", restored.getName());
        assertEquals(playlist.getId(), restored.getId());
    }
}
