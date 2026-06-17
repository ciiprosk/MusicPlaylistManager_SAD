package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AddTrackCommandTest {

    private TrackService trackService;
    private SharedState sharedState;

    @BeforeEach
    void setUp() {
        DAO<Track> fakeTrackDAO = new DAO<Track>() {
            private final List<Track> list = new ArrayList<>();
            @Override public List<Track> selectAll() { return list; }
            @Override public void insert(Track t) { list.add(t); }
            @Override public void update(Track t) {}
            @Override public void delete(UUID id) { list.removeIf(t -> t.getId().equals(id)); }
            @Override public Optional<Track> searchById(UUID id) { return list.stream().filter(t -> t.getId().equals(id)).findFirst(); }
            @Override public boolean isDuplicated(Track track) {
                return list.stream().anyMatch(t -> t.isDuplicate(track));
            }
        };

        sharedState = new SharedState();
        trackService = new TrackService(fakeTrackDAO, sharedState);
    }

    @Test
    void testExecuteAddsTrackAndUndoRemovesIt() {
        Set<Tag> tags = EnumSet.of(Tag.FAVOURITE);
        AddTrackCommand command = new AddTrackCommand(
                trackService, "Bohemian Rhapsody", "Queen", "1975", 
                "/path/song.mp3", Genre.ROCK, 354, tags);

        assertTrue(trackService.getAllTracks().isEmpty());

        Optional<String> error = command.execute();
        
        assertTrue(error.isEmpty(), "Execution should not return an error");
        assertEquals(1, trackService.getAllTracks().size());
        
        Track addedTrack = trackService.getAllTracks().get(0);
        assertEquals("Bohemian Rhapsody", addedTrack.getTitle());
        assertEquals("Queen", addedTrack.getAuthor());

        // Now test undo
        command.undo();
        
        assertTrue(trackService.getAllTracks().isEmpty(), "Track should be removed after undo");
    }

    @Test
    void testGetDescription() {
        AddTrackCommand command = new AddTrackCommand(
                trackService, "Imagine", "John Lennon", "1971", 
                "/path/imagine.mp3", Genre.POP, 180, null);

        assertEquals("Add track \"Imagine\"", command.getDescription());
    }
}
