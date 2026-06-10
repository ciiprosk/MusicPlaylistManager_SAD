package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoopModeTest {

    private LoopMode loopMode;

    @BeforeEach
    void setUp() {
        loopMode = new LoopMode();
    }

    // TEST: SE TERMINA UNA SINGOLA TRACCIA IN MODALITÀ LOOP, LA STESSA TRACCIA RIPARTE

    @Test
    void nextItemShouldRestartSameTrackWhenOnlyOneTrackIsInQueue() {

        Track track =
                createTrack("Numb");

        QueueItem item =
                createQueueItem(track, null);

        List<QueueItem> queue =
                new ArrayList<>();

        queue.add(item);

        QueueItem result =
                loopMode.nextItem(
                        queue,
                        item
                );

        assertNotNull(result);

        assertEquals(
                track.getId(),
                result.getPlayable().getId()
        );

        assertEquals(
                1,
                queue.size()
        );
    }

    // TEST: SE TERMINA L'ULTIMA TRACCIA DELLA CODA, IL LOOP RIPARTE DALLA PRIMA

    @Test
    void nextItemShouldRestartFromFirstTrackWhenLastTrackEnds() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        Track thirdTrack =
                createTrack("Third Track");

        QueueItem firstItem =
                createQueueItem(firstTrack, null);

        QueueItem secondItem =
                createQueueItem(secondTrack, null);

        QueueItem thirdItem =
                createQueueItem(thirdTrack, null);

        List<QueueItem> queue =
                new ArrayList<>();

        queue.add(firstItem);
        queue.add(secondItem);
        queue.add(thirdItem);

        QueueItem result =
                loopMode.nextItem(
                        queue,
                        thirdItem
                );

        assertNotNull(result);

        assertEquals(
                firstTrack.getId(),
                result.getPlayable().getId()
        );

        assertEquals(
                3,
                queue.size()
        );
    }

    // TEST: IN MODALITÀ LOOP LA CODA NON VIENE SVUOTATA QUANDO SI ARRIVA ALL'ULTIMO BRANO

    @Test
    void nextItemShouldNotRemoveTracksFromQueueInLoopMode() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        QueueItem firstItem =
                createQueueItem(firstTrack, null);

        QueueItem secondItem =
                createQueueItem(secondTrack, null);

        List<QueueItem> queue =
                new ArrayList<>();

        queue.add(firstItem);
        queue.add(secondItem);

        QueueItem result =
                loopMode.nextItem(
                        queue,
                        secondItem
                );

        assertNotNull(result);

        assertEquals(
                firstTrack.getId(),
                result.getPlayable().getId()
        );

        assertEquals(
                2,
                queue.size()
        );

        assertTrue(
                queue.contains(firstItem)
        );

        assertTrue(
                queue.contains(secondItem)
        );
    }

    // TEST: IL LOOP AVANZA NORMALMENTE AL BRANO SUCCESSIVO SE NON SI È ALL'ULTIMO

    @Test
    void nextItemShouldMoveToNextTrackWhenCurrentTrackIsNotLast() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        Track thirdTrack =
                createTrack("Third Track");

        QueueItem firstItem =
                createQueueItem(firstTrack, null);

        QueueItem secondItem =
                createQueueItem(secondTrack, null);

        QueueItem thirdItem =
                createQueueItem(thirdTrack, null);

        List<QueueItem> queue =
                new ArrayList<>();

        queue.add(firstItem);
        queue.add(secondItem);
        queue.add(thirdItem);

        QueueItem result =
                loopMode.nextItem(
                        queue,
                        firstItem
                );

        assertNotNull(result);

        assertEquals(
                secondTrack.getId(),
                result.getPlayable().getId()
        );

        assertEquals(
                3,
                queue.size()
        );
    }

    // TEST: SE TERMINA L'ULTIMA TRACCIA DI UNA PLAYLIST IN CODA, IL LOOP RIPARTE DALLA PRIMA TRACCIA DELLA PLAYLIST

    @Test
    void nextItemShouldRestartPlaylistFromFirstTrackWhenLastPlaylistTrackEnds() {

        Playlist playlist =
                new Playlist("Rock Playlist");

        Track firstTrack =
                createTrack("Playlist Track 1");

        Track secondTrack =
                createTrack("Playlist Track 2");

        Track thirdTrack =
                createTrack("Playlist Track 3");

        playlist.addTrack(firstTrack);
        playlist.addTrack(secondTrack);
        playlist.addTrack(thirdTrack);

        UUID playlistId =
                playlist.getId();

        QueueItem firstItem =
                createQueueItem(firstTrack, playlistId);

        QueueItem secondItem =
                createQueueItem(secondTrack, playlistId);

        QueueItem thirdItem =
                createQueueItem(thirdTrack, playlistId);

        List<QueueItem> queue =
                new ArrayList<>();

        queue.add(firstItem);
        queue.add(secondItem);
        queue.add(thirdItem);

        QueueItem result =
                loopMode.nextItem(
                        queue,
                        thirdItem
                );

        assertNotNull(result);

        assertEquals(
                firstTrack.getId(),
                result.getPlayable().getId()
        );

        assertEquals(
                playlistId,
                result.getBelongsToPlaylist()
        );

        assertEquals(
                3,
                queue.size()
        );
    }

    // TEST: SE LA CODA È VUOTA, IL LOOP RESTITUISCE NULL

    @Test
    void nextItemShouldReturnNullWhenQueueIsEmpty() {

        List<QueueItem> queue =
                new ArrayList<>();

        QueueItem result =
                loopMode.nextItem(
                        queue,
                        null
                );

        assertNull(result);

        assertTrue(
                queue.isEmpty()
        );
    }

    // METODO DI SUPPORTO PER CREARE QUEUE ITEM VALIDI

    private QueueItem createQueueItem(Track track, UUID belongsToPlaylist) {
        return new QueueItem(
                track,
                belongsToPlaylist,
                UUID.randomUUID()
        );
    }

    // METODO DI SUPPORTO PER CREARE TRACCE VALIDE

    private Track createTrack(String title) {
        return new Track(
                title,
                "Author",
                Genre.ROCK,
                "songs/" + title + ".mp3",
                180,
                "2020",
                EnumSet.noneOf(Tag.class)
        );
    }
}