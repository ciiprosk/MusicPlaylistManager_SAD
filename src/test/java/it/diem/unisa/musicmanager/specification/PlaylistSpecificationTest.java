package it.diem.unisa.musicmanager.specification;

import it.diem.unisa.musicmanager.model.*;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.specification.GenreSpecification;
import it.diem.unisa.musicmanager.specification.Specification;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class PlaylistSpecificationTest {

//US11
    @Test
    void genreSpecificationShouldFilterCorrectly() {

        Track rock = new Track(
                "Rock Song", "Author", Genre.ROCK,
                "path", 200, "2020",
                EnumSet.noneOf(Tag.class)
        );

        Track pop = new Track(
                "Pop Song", "Author", Genre.POP,
                "path", 200, "2020",
                EnumSet.noneOf(Tag.class)
        );

        Specification<Track> spec = new GenreSpecification(Genre.ROCK);

        assertTrue(spec.isSatisfiedBy(rock));
        assertFalse(spec.isSatisfiedBy(pop));
    }

    @Test
    void playlistShouldContainOnlyTracksWithSelectedGenre() {

        Track rock1 = new Track(
                "Rock1", "A", Genre.ROCK,
                "p", 200, "2020",
                EnumSet.noneOf(Tag.class)
        );

        Track rock2 = new Track(
                "Rock2", "A", Genre.ROCK,
                "p", 200, "2020",
                EnumSet.noneOf(Tag.class)
        );

        Track pop = new Track(
                "Pop1", "A", Genre.POP,
                "p", 200, "2020",
                EnumSet.noneOf(Tag.class)
        );

        List<Track> tracks = List.of(rock1, rock2, pop);

        Specification<Track> spec = new GenreSpecification(Genre.ROCK);

        PlaylistService service = new PlaylistService(null, null);

        Playlist playlist = service.generate("Test Playlist", tracks, spec);

        assertEquals(2, playlist.getTracksList().size());
        assertTrue(playlist.getTracksList().contains(rock1));
        assertTrue(playlist.getTracksList().contains(rock2));
        assertFalse(playlist.getTracksList().contains(pop));
    }
    @Test
    void shouldFilterTracksByYearCorrectly() {

        Track t1 = new Track("Song1", "Author1", Genre.ROCK, "p1", 200, "1999", null);
        Track t2 = new Track("Song2", "Author2", Genre.POP, "p2", 200, "2000", null);
        Track t3 = new Track("Song3", "Author3", Genre.JAZZ, "p3", 200, "1999", null);

        List<Track> tracks = List.of(t1, t2, t3);

        Specification<Track> spec = new YearSpecification(1999);

        List<Track> result = tracks.stream()
                .filter(spec::isSatisfiedBy)
                .collect(Collectors.toList());

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t3));
        assertFalse(result.contains(t2));
    }

    @Test
    void shouldFilterTracksByTag() {

        Track t1 = new Track("Song1", "Author1", Genre.ROCK, "p1", 200, "1999",
                EnumSet.of(Tag.EXPLICIT, Tag.FAVOURITE));

        Track t2 = new Track("Song2", "Author2", Genre.POP, "p2", 200, "2000",
                EnumSet.of(Tag.NEWRELEASE));

        Track t3 = new Track("Song3", "Author3", Genre.JAZZ, "p3", 200, "1999",
                EnumSet.of(Tag.EXPLICIT));

        List<Track> tracks = List.of(t1, t2, t3);

        Specification<Track> tagSpec = new TagSpecification(Tag.EXPLICIT);

        List<Track> result = tracks.stream()
                .filter(tagSpec::isSatisfiedBy)
                .collect(Collectors.toList());

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t3));
        assertFalse(result.contains(t2));
    }

}
