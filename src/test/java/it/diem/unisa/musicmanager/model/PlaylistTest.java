package it.diem.unisa.musicmanager.model;

import it.diem.unisa.musicmanager.exception.PlaylistInfoException;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PlaylistTest {

    @Test
    void testPlaylistCreationWithValidName() {
        Playlist playlist = new Playlist("My Favorite Songs");
        assertEquals("My Favorite Songs", playlist.getName());
        assertNotNull(playlist.getId());
        assertEquals(0, playlist.numberOfTracks());
    }

    @Test
    void testPlaylistCreationWithEmptyNameShouldThrowException() {
        assertThrows(PlaylistInfoException.class, () -> new Playlist(""));
        assertThrows(PlaylistInfoException.class, () -> new Playlist("   "));
    }

    @Test
    void testPlaylistCreationWithTooLongNameShouldThrowException() {
        String longName = "a".repeat(51);
        assertThrows(PlaylistInfoException.class, () -> new Playlist(longName));
    }

    @Test
    void testAddAndRemoveTrack() {
        Playlist playlist = new Playlist("Rock");
        UUID trackId = UUID.randomUUID();

        playlist.addTrack(trackId);
        assertTrue(playlist.containsTrack(trackId));
        assertEquals(1, playlist.numberOfTracks());

        playlist.removeTrack(trackId);
        assertFalse(playlist.containsTrack(trackId));
        assertEquals(0, playlist.numberOfTracks());
    }

    @Test
    void testMoveTrack() {
        Playlist playlist = new Playlist("Jazz");
        Track t1 = new Track(UUID.randomUUID());
        Track t2 = new Track(UUID.randomUUID());

        playlist.addTrack(t1);
        playlist.addTrack(t2);

        assertEquals(t1.getId(), playlist.getTracks().get(0));
        assertEquals(t2.getId(), playlist.getTracks().get(1));

        playlist.moveTrack(0, 1);

        assertEquals(t2.getId(), playlist.getTracks().get(0));
        assertEquals(t1.getId(), playlist.getTracks().get(1));
    }
}