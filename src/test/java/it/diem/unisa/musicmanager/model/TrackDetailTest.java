package it.diem.unisa.musicmanager.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class TrackDetailsTest {

    @Test
    void shouldReturnCorrectTrackDetails() {

        Track track = new Track(
                "Bohemian Rhapsody",
                "Queen",
                Genre.ROCK,
                "songs/bohemian.mp3",
                354,
                "1975",
                EnumSet.of(Tag.FAVOURITE, Tag.EXPLICIT)
        );

        assertEquals("Bohemian Rhapsody", track.getTitle());
        assertEquals("Queen", track.getAuthor());
        assertEquals(Genre.ROCK, track.getGenre());
        assertEquals("songs/bohemian.mp3", track.getSongPath());
        assertEquals(354, track.getSongLength());
        assertEquals("1975", track.getYear());

        assertTrue(track.hasTag(Tag.FAVOURITE));
        assertTrue(track.hasTag(Tag.EXPLICIT));
        assertFalse(track.hasTag(Tag.NEWRELEASE));
    }
}
