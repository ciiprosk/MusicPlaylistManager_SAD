package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceServiceTest {

    private PersistenceService persistenceService;
    private SharedState sharedState;

    private static class DummyDAO<T> implements DAO<T> {
        private final List<T> data;
        public DummyDAO(List<T> data) { this.data = data; }
        @Override public List<T> selectAll() { return data; }
        @Override public void insert(T item) {}
        @Override public void update(T item) {}
        @Override public void delete(java.util.UUID id) {}
        @Override public java.util.Optional<T> searchById(java.util.UUID id) { return java.util.Optional.empty(); }
        @Override public boolean isDuplicated(T item) { return false; }
    }

    @BeforeEach
    void setUp() {
        sharedState = new SharedState();
        
        Track track1 = new Track("T1", "A1", null, "p1", 100, "2000", null);
        Track track2 = new Track("T2", "A2", null, "p2", 200, "2001", null);
        
        Playlist playlist1 = new Playlist("P1");
        playlist1.addTrack(track1.getId());
        
        DAO<Track> trackDAO = new DummyDAO<>(List.of(track1, track2));
        DAO<Playlist> playlistDAO = new DummyDAO<>(List.of(playlist1));
        
        persistenceService = new PersistenceService(trackDAO, playlistDAO, sharedState);
    }

    @Test
    void testLoadPopulatesSharedStateAndResolvesTracks() {
        persistenceService.load();
        
        assertEquals(2, sharedState.getALlTracks().size());
        assertEquals(1, sharedState.getALlPlaylists().size());
        
        Playlist loadedPlaylist = sharedState.getALlPlaylists().get(0);
        assertEquals(1, loadedPlaylist.getTracksList().size());
        assertEquals("T1", loadedPlaylist.getTracksList().get(0).getTitle());
    }
}
