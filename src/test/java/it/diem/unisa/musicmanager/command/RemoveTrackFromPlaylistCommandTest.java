package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.state.SharedState;
import it.diem.unisa.musicmanager.dao.DAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.EnumSet;
import static org.junit.jupiter.api.Assertions.*;

class RemoveTrackFromPlaylistCommandTest {

    private PlaylistService playlistService;
    private RemoveTrackFromPlaylistCommand command;
    private Playlist playlist;
    private Track track;

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

        playlist = new Playlist("My Playlist");
        playlistService.restorePlaylist(playlist);

        track = new Track(UUID.randomUUID(), "Song Title", "Artist", Genre.ROCK, "path/song.mp3", 180, "2020", EnumSet.noneOf(Tag.class));
        state.getALlTracks().add(track);
        playlist.addTrack(track); // La traccia è inizialmente presente nella playlist

        command = new RemoveTrackFromPlaylistCommand(playlistService, playlist.getId(), track.getId(), track.getTitle(), playlist.getName());
    }

    @Test
    void testUndoRestoresRemovedTrackToPlaylistArchive() {
        assertTrue(playlist.containsTrack(track.getId()));

        Optional<String> error = command.execute();
        assertTrue(error.isEmpty());
        assertFalse(playlist.containsTrack(track.getId()));

        command.undo();

        assertTrue(playlist.containsTrack(track.getId()));
    }
}
