package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class QueueServiceSyncTest {

    private SharedState sharedState;
    private QueueService queueService;
    private PlaylistService playlistService;
    private TrackService trackService;

    // Stub per DAO Playlist
    private final DAO<Playlist> playlistDAO = new DAO<>() {
        private final Map<UUID, Playlist> db = new HashMap<>();
        @Override public List<Playlist> selectAll() { return new ArrayList<>(db.values()); }
        @Override public void insert(Playlist item) { db.put(item.getId(), item); }
        @Override public void update(Playlist item) { db.put(item.getId(), item); }
        @Override public void delete(UUID id) { db.remove(id); }
        @Override public Optional<Playlist> searchById(UUID id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean isDuplicated(Playlist item) { return false; }
    };

    // Stub per DAO Track
    private final DAO<Track> trackDAO = new DAO<>() {
        private final Map<UUID, Track> db = new HashMap<>();
        @Override public List<Track> selectAll() { return new ArrayList<>(db.values()); }
        @Override public void insert(Track item) { db.put(item.getId(), item); }
        @Override public void update(Track item) { db.put(item.getId(), item); }
        @Override public void delete(UUID id) { db.remove(id); }
        @Override public Optional<Track> searchById(UUID id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean isDuplicated(Track item) { return false; }
    };

    private Track track1;
    private Track track2;
    private Track newTrack;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        sharedState = new SharedState();
        queueService = new QueueService(sharedState);
        playlistService = new PlaylistService(playlistDAO, sharedState);
        playlistService.setQueueService(queueService);
        trackService = new TrackService(trackDAO, sharedState);

        // Creazione dati di test
        track1 = new Track(UUID.randomUUID(), "Track 1", "Author", Genre.ROCK, "path1.mp3", 180, "2020", EnumSet.noneOf(Tag.class));
        track2 = new Track(UUID.randomUUID(), "Track 2", "Author", Genre.ROCK, "path2.mp3", 200, "2020", EnumSet.noneOf(Tag.class));
        newTrack = new Track(UUID.randomUUID(), "New Track", "Author", Genre.POP, "path3.mp3", 150, "2021", EnumSet.noneOf(Tag.class));

        // Registriamo nel database e nello sharedState
        trackDAO.insert(track1);
        trackDAO.insert(track2);
        trackDAO.insert(newTrack);
        sharedState.getALlTracks().add(track1);
        sharedState.getALlTracks().add(track2);
        sharedState.getALlTracks().add(newTrack);

        playlist = new Playlist("My Playlist");
        playlist.addTrack(track1);
        playlist.addTrack(track2);

        playlistDAO.insert(playlist);
        sharedState.getALlPlaylists().add(playlist);
    }

    @Test
    void addingTrackToPlaylistShouldSyncQueue() {
        // Aggiungiamo la playlist alla coda di riproduzione
        queueService.addToQueue(playlist);

        // La coda deve contenere le 2 tracce iniziali
        assertEquals(2, sharedState.getQueue().size());
        assertEquals(track1.getId(), sharedState.getQueue().get(0).getPlayable().getId());
        assertEquals(track2.getId(), sharedState.getQueue().get(1).getPlayable().getId());

        // Aggiungiamo una nuova traccia alla playlist tramite il service
        playlistService.addTrackToPlaylist(playlist.getId(), newTrack.getId());

        // La coda deve essersi aggiornata automaticamente a 3 tracce, mantenendo l'ordine
        assertEquals(3, sharedState.getQueue().size());
        assertEquals(newTrack.getId(), sharedState.getQueue().get(2).getPlayable().getId());
        assertEquals(playlist.getId(), sharedState.getQueue().get(2).getBelongsToPlaylist());
    }

    @Test
    void removingTrackFromPlaylistShouldSyncQueue() {
        // Aggiungiamo la playlist alla coda
        queueService.addToQueue(playlist);

        // La coda contiene le 2 tracce iniziali
        assertEquals(2, sharedState.getQueue().size());

        // Rimuoviamo la prima traccia dalla playlist
        playlistService.removeTrackFromPlaylist(playlist.getId(), track1.getId());

        // La coda deve essersi aggiornata rimuovendo la traccia
        assertEquals(1, sharedState.getQueue().size());
        assertEquals(track2.getId(), sharedState.getQueue().get(0).getPlayable().getId());
    }

    @Test
    void removingCurrentTrackFromPlaylistShouldResetCurrentItem() {
        queueService.addToQueue(playlist);
        QueueItem firstItem = sharedState.getQueue().get(0);
        queueService.setCurrentItem(firstItem);

        assertEquals(firstItem, queueService.getCurrentItem());

        // Rimuoviamo il brano attualmente in riproduzione
        playlistService.removeTrackFromPlaylist(playlist.getId(), track1.getId());

        // Il brano rimosso deve essere rimosso dalla coda e il currentItem azzerato
        assertNull(queueService.getCurrentItem());
        assertEquals(1, sharedState.getQueue().size());
        assertEquals(track2.getId(), sharedState.getQueue().get(0).getPlayable().getId());
    }
}
