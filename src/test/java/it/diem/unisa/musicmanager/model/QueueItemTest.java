package it.diem.unisa.musicmanager.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QueueItemTest {

    @Test
    void testTrackQueueItemCreation() {
        Track track = new Track(UUID.randomUUID());
        QueueItem queueItem = new QueueItem(track, null, null);

        assertEquals(track, queueItem.getPlayable());
        assertNull(queueItem.getBelongsToPlaylist());
        assertNull(queueItem.getPlaylistProgressive());
    }

    @Test
    void testPlaylistQueueItemCreation() {
        Track track = new Track(UUID.randomUUID());
        UUID playlistId = UUID.randomUUID();
        UUID progressiveId = UUID.randomUUID();

        QueueItem queueItem = new QueueItem(track, playlistId, progressiveId);

        assertEquals(track, queueItem.getPlayable());
        assertEquals(playlistId, queueItem.getBelongsToPlaylist());
        assertEquals(progressiveId, queueItem.getPlaylistProgressive());
    }

    @Test
    void testQueueItemTypeIsCorrectlyExposed() {
        Track track = new Track(UUID.randomUUID());
        Playlist playlist = new Playlist("My Playlist");
        
        QueueItem trackItem = new QueueItem(track, null, null);
        assertEquals(QueueItemType.TRACK, trackItem.getPlayable().getType());
        
        QueueItem playlistItem = new QueueItem(playlist, playlist.getId(), java.util.UUID.randomUUID());
        assertEquals(QueueItemType.PLAYLIST, playlistItem.getPlayable().getType());
    }
}
