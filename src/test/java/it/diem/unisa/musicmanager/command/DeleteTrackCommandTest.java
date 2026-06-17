package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DeleteTrackCommandTest {

    private TrackService trackService;
    private PlaylistService playlistService;
    private SharedState sharedState;
    private Track track;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        DAO<Track> fakeTrackDAO = new DAO<Track>() {
            private final List<Track> list = new ArrayList<>();
            @Override public List<Track> selectAll() { return list; }
            @Override public void insert(Track t) { list.add(t); }
            @Override public void update(Track t) {}
            @Override public void delete(UUID id) { list.removeIf(t -> t.getId().equals(id)); }
            @Override public Optional<Track> searchById(UUID id) { return list.stream().filter(t -> t.getId().equals(id)).findFirst(); }
            @Override public boolean isDuplicated(Track track) { return false; }
        };

        DAO<Playlist> fakePlaylistDAO = new DAO<Playlist>() {
            private final List<Playlist> list = new ArrayList<>();
            @Override public List<Playlist> selectAll() { return list; }
            @Override public void insert(Playlist p) { list.add(p); }
            @Override public void update(Playlist p) {}
            @Override public void delete(UUID id) { list.removeIf(p -> p.getId().equals(id)); }
            @Override public Optional<Playlist> searchById(UUID id) { return list.stream().filter(p -> p.getId().equals(id)).findFirst(); }
            @Override public boolean isDuplicated(Playlist p) { return false; }
        };

        sharedState = new SharedState();
        trackService = new TrackService(fakeTrackDAO, sharedState);
        playlistService = new PlaylistService(fakePlaylistDAO, sharedState);

        // Add a track to the service
        track = new Track(UUID.randomUUID(), "Song Title", "Artist", Genre.ROCK, "path/song.mp3", 180, "2020", EnumSet.noneOf(Tag.class));
        trackService.restoreTrack(track);

        // Add a playlist to the service and put the track inside
        playlist = new Playlist("My Playlist");
        playlistService.restorePlaylist(playlist);
        playlistService.addTrackToPlaylist(playlist.getId(), track.getId());
    }

    @Test
    void testExecuteDeletesTrackAndUndoRestoresIt() {
        DeleteTrackCommand command = new DeleteTrackCommand(trackService, playlistService, track.getId());

        assertEquals(1, trackService.getAllTracks().size());
        assertTrue(playlist.containsTrack(track.getId()));

        Optional<String> error = command.execute();
        
        assertTrue(error.isEmpty(), "Execution should not return an error");
        assertEquals(0, trackService.getAllTracks().size());

        // Now test undo
        command.undo();
        
        assertEquals(1, trackService.getAllTracks().size());
        assertTrue(playlist.containsTrack(track.getId()), "Track should be restored to the playlist");
        assertEquals("Song Title", trackService.getAllTracks().get(0).getTitle());
    }

    @Test
    void testGetDescription() {
        DeleteTrackCommand command = new DeleteTrackCommand(trackService, playlistService, track.getId());
        command.execute(); // Need to execute to capture trackCopy
        
        assertEquals("Delete track \"Song Title\"", command.getDescription());
    }
}
