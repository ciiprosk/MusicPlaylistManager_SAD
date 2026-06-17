package it.diem.unisa.musicmanager.model;

import it.diem.unisa.musicmanager.exception.TrackInfoException;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TrackTest {

    @Test
    void testTrackCreationWithValidData() {
        EnumSet<Tag> tags = EnumSet.of(Tag.FAVOURITE);
        Track track = new Track("Bohemian Rhapsody", "Queen", Genre.ROCK, "/path/to/song.mp3", 354, "1975", tags);

        assertNotNull(track.getId());
        assertEquals("Bohemian Rhapsody", track.getTitle());
        assertEquals("Queen", track.getAuthor());
        assertEquals(Genre.ROCK, track.getGenre());
        assertEquals("/path/to/song.mp3", track.getSongPath());
        assertEquals(354, track.getSongLength());
        assertEquals("1975", track.getYear());
        assertEquals(0, track.getPlayCount());
        assertTrue(track.hasTag(Tag.FAVOURITE));
    }

    @Test
    void testTrackCreationWithDefaultValues() {
        Track track = new Track("Song Title", null, null, "/path", 200, null, null);

        assertEquals("Song Title", track.getTitle());
        assertEquals("Unknown", track.getAuthor());
        assertEquals(Genre.UNKNOWN, track.getGenre());
        assertEquals("UNKNOWN", track.getYear());
        assertTrue(track.getTags().isEmpty());
    }

    @Test
    void testTrackCreationWithInvalidTitle() {
        assertThrows(TrackInfoException.class, () -> new Track(null, "Author", Genre.POP, "/path", 200, "2000", null));
        assertThrows(TrackInfoException.class, () -> new Track("", "Author", Genre.POP, "/path", 200, "2000", null));
        assertThrows(TrackInfoException.class, () -> new Track("   ", "Author", Genre.POP, "/path", 200, "2000", null));
        
        String longTitle = "a".repeat(101);
        assertThrows(TrackInfoException.class, () -> new Track(longTitle, "Author", Genre.POP, "/path", 200, "2000", null));
    }

    @Test
    void testTrackCreationWithInvalidAuthor() {
        String longAuthor = "a".repeat(101);
        assertThrows(TrackInfoException.class, () -> new Track("Title", longAuthor, Genre.POP, "/path", 200, "2000", null));
    }

    @Test
    void testTrackCreationWithInvalidLength() {
        assertThrows(TrackInfoException.class, () -> new Track("Title", "Author", Genre.POP, "/path", 0, "2000", null));
        assertThrows(TrackInfoException.class, () -> new Track("Title", "Author", Genre.POP, "/path", -5, "2000", null));
    }

    @Test
    void testTrackCreationWithInvalidYear() {
        assertThrows(TrackInfoException.class, () -> new Track("Title", "Author", Genre.POP, "/path", 200, "200", null)); // Not 4 digits
        assertThrows(TrackInfoException.class, () -> new Track("Title", "Author", Genre.POP, "/path", 200, "20000", null)); // Not 4 digits
        assertThrows(TrackInfoException.class, () -> new Track("Title", "Author", Genre.POP, "/path", 200, "abcd", null)); // Not digits

        int futureYear = Year.now().getValue() + 1;
        assertThrows(TrackInfoException.class, () -> new Track("Title", "Author", Genre.POP, "/path", 200, String.valueOf(futureYear), null)); // Future year
    }

    @Test
    void testSettersWithValidation() {
        Track track = new Track(UUID.randomUUID());

        track.setTitle("New Title");
        assertEquals("New Title", track.getTitle());
        assertThrows(TrackInfoException.class, () -> track.setTitle(""));

        track.setAuthor("New Author");
        assertEquals("New Author", track.getAuthor());

        track.setYear("2020");
        assertEquals("2020", track.getYear());
        assertThrows(TrackInfoException.class, () -> track.setYear("9999"));
    }

    @Test
    void testTagsManagement() {
        Track track = new Track(UUID.randomUUID());

        assertFalse(track.hasTag(Tag.FAVOURITE));
        
        track.addTag(Tag.FAVOURITE);
        assertTrue(track.hasTag(Tag.FAVOURITE));
        
        track.removeTag(Tag.FAVOURITE);
        assertFalse(track.hasTag(Tag.FAVOURITE));

        track.setTags(EnumSet.of(Tag.FAVOURITE));
        assertTrue(track.hasTag(Tag.FAVOURITE));

        track.setTags(null);
        assertTrue(track.getTags().isEmpty());
    }

    @Test
    void testPlayCount() {
        Track track = new Track(UUID.randomUUID());
        assertEquals(0, track.getPlayCount());

        track.incrementPlayCount();
        assertEquals(1, track.getPlayCount());
    }

    @Test
    void testIsDuplicate() {
        Track track1 = new Track("Title", "Author", Genre.POP, "/path1", 200, "2000", null);
        Track track2 = new Track(" Title ", " Author ", Genre.ROCK, "/path2", 300, "2001", null);
        Track track3 = new Track("Different", "Author", Genre.POP, "/path3", 200, "2000", null);

        assertTrue(track1.isDuplicate(track2));
        assertFalse(track1.isDuplicate(track3));
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        Track track1 = new Track(id, "Title1", "Author1", Genre.POP, "/path1", 200, "2000", null);
        Track track2 = new Track(id, "Title2", "Author2", Genre.ROCK, "/path2", 300, "2001", null);
        Track track3 = new Track(UUID.randomUUID(), "Title1", "Author1", Genre.POP, "/path1", 200, "2000", null);

        assertEquals(track1, track2);
        assertEquals(track1.hashCode(), track2.hashCode());
        assertNotEquals(track1, track3);
        assertNotEquals(track1, null);
        assertNotEquals(track1, new Object());
    }

    @Test
    void testQueueItemAndPlayableMethods() {
        Track track = new Track(UUID.randomUUID());
        
        assertEquals(QueueItemType.TRACK, track.getType());
        assertNotNull(track.getTracksToPlay());
        assertEquals(1, track.getTracksToPlay().size());
        assertEquals(track, track.getTracksToPlay().get(0));
    }
}
