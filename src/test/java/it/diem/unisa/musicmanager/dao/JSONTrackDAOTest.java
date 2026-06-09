package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JSONTrackDAOTest {

    private JSONTrackDAO dao;

    @BeforeEach
    void setUp() {
        File folder = new File("test-data");
        folder.mkdirs();

        File file = new File(folder, "tracks-test.jsonl");
        if (file.exists()) {
            file.delete();
        }

        dao = new JSONTrackDAO("test-data", "tracks-test.jsonl");
    }

    @Test
    void insertShouldSaveTrackOnFile() {
        Track track = new Track("Numb", "Linkin Park", Genre.ROCK,
                "songs/numb.mp3", 185, "2003", null);

        dao.insert(track);

        assertEquals(1, dao.selectAll().size());
        assertEquals("Numb", dao.selectAll().get(0).getTitle());
    }

    @Test
    void searchByIdShouldFindInsertedTrack() {
        Track track = new Track("Believer", "Imagine Dragons", Genre.ROCK,
                "songs/believer.mp3", 204, "2017", null);

        dao.insert(track);

        Optional<Track> result = dao.searchById(track.getId());

        assertTrue(result.isPresent());
        assertEquals("Believer", result.get().getTitle());
    }

    @Test
    void isDuplicatedShouldReturnTrueForSameTitleAndAuthor() {
        Track track1 = new Track("Numb", "Linkin Park", Genre.ROCK,
                "songs/numb.mp3", 185, "2003", null);

        Track track2 = new Track("Numb", "Linkin Park", Genre.ROCK,
                "songs/numb-copy.mp3", 190, "2004", null);

        dao.insert(track1);

        assertTrue(dao.isDuplicated(track2));
    }

    @Test
    void updateShouldModifyExistingTrack() {
        Track track = new Track("Old Title", "Author", Genre.POP,
                "songs/test.mp3", 180, "2020", null);

        dao.insert(track);

        track.setTitle("New Title");
        dao.update(track);

        Optional<Track> result = dao.searchById(track.getId());

        assertTrue(result.isPresent());
        assertEquals("New Title", result.get().getTitle());
    }

    @Test
    void deleteShouldRemoveTrackFromFile() {
        Track track = new Track("Thunder", "Imagine Dragons", Genre.ROCK,
                "songs/thunder.mp3", 187, "2017", null);

        dao.insert(track);
        dao.delete(track.getId());

        assertFalse(dao.searchById(track.getId()).isPresent());
    }
}