package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.state.SharedState;
import it.diem.unisa.musicmanager.specification.Specification;
import it.diem.unisa.musicmanager.specification.GenreSpecification;
import it.diem.unisa.musicmanager.dao.DAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.EnumSet;
import static org.junit.jupiter.api.Assertions.*;

class GeneratePlaylistCommandTest {

    private PlaylistService playlistService;
    private GeneratePlaylistCommand command;
    private List<Track> tracksCatalog;
    private Specification<Track> criteria;
    private final String playlistName = "Generated Rock Playlist";

    @BeforeEach
    void setUp() {
        DAO<Playlist> fakePlaylistDAO = new DAO<Playlist>() {
            private final List<Playlist> list = new ArrayList<>();
            @Override public List<Playlist> selectAll() { return list; }
            @Override public void insert(Playlist p) { list.add(p); }
            @Override public void update(Playlist p) {}
            @Override public void delete(UUID id) { list.removeIf(p -> p.getId().equals(id)); }
            @Override public Optional<Playlist> searchById(UUID id) { return list.stream().filter(p -> p.getId().equals(id)).findFirst(); }
            @Override public boolean isDuplicated(Playlist p) { return false; }
        };

        SharedState state = new SharedState();
        playlistService = new PlaylistService(fakePlaylistDAO, state);

        tracksCatalog = new ArrayList<>();
        Track rockTrack = new Track(UUID.randomUUID(), "Rock Song", "Artist", Genre.ROCK, "path1.mp3", 180, "2020", EnumSet.noneOf(Tag.class));
        Track popTrack = new Track(UUID.randomUUID(), "Pop Song", "Artist", Genre.POP, "path2.mp3", 180, "2020", EnumSet.noneOf(Tag.class));
        tracksCatalog.add(rockTrack);
        tracksCatalog.add(popTrack);

        criteria = new GenreSpecification(Genre.ROCK);

        command = new GeneratePlaylistCommand(playlistService, playlistName, tracksCatalog, criteria);
    }

    @Test
    void testUndoRemovesGeneratedPlaylistFromArchive() {
        assertEquals(0, playlistService.getPlaylists().size());

        Optional<String> error = command.execute();
        assertTrue(error.isEmpty());
        assertEquals(1, playlistService.getPlaylists().size());
        
        Playlist generated = playlistService.getPlaylists().get(0);
        assertEquals(playlistName, generated.getName());
        assertEquals(1, generated.numberOfTracks());

        command.undo();

        assertEquals(0, playlistService.getPlaylists().size());
    }
}
