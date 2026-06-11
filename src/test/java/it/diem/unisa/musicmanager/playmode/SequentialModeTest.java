package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SequentialModeTest {

    private SequentialMode sequentialMode;

    @BeforeEach
    void setUp() {
        sequentialMode = new SequentialMode();
    }

    // TEST: LA STRATEGIA SEQUENZIALE RESTITUISCE IL BRANO SUCCESSIVO CORRETTO

    @Test
    void nextItemShouldReturnNextTrackInQueue() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        Track thirdTrack =
                createTrack("Third Track");

        List<QueueItem> queue =
                new ArrayList<>();

        QueueItem firstItem =
                createQueueItem(firstTrack);

        QueueItem secondItem =
                createQueueItem(secondTrack);

        QueueItem thirdItem =
                createQueueItem(thirdTrack);

        queue.add(firstItem);
        queue.add(secondItem);
        queue.add(thirdItem);

        Optional<QueueItem> result =
                sequentialMode.nextItem(
                        queue,
                        firstItem
                );

        assertTrue(
                result.isPresent()
        );

        assertTrue(
                result.get().isTrack()
        );

        assertEquals(
                secondTrack.getId(),
                result.get().getPlayable().getId()
        );
    }

    // TEST: LA STRATEGIA SEQUENZIALE AVANZA ALL'ELEMENTO CON INDICE SUCCESSIVO

    @Test
    void nextItemShouldReturnItemAtNextIndex() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        Track thirdTrack =
                createTrack("Third Track");

        List<QueueItem> queue =
                new ArrayList<>();

        QueueItem firstItem =
                createQueueItem(firstTrack);

        QueueItem secondItem =
                createQueueItem(secondTrack);

        QueueItem thirdItem =
                createQueueItem(thirdTrack);

        queue.add(firstItem);
        queue.add(secondItem);
        queue.add(thirdItem);

        int currentIndex =
                queue.indexOf(firstItem);

        Optional<QueueItem> result =
                sequentialMode.nextItem(
                        queue,
                        firstItem
                );

        assertTrue(
                result.isPresent()
        );

        int resultIndex =
                queue.indexOf(result.get());

        assertEquals(
                0,
                currentIndex
        );

        /*
         * Se SequentialMode rimuove l'elemento corrente dalla coda,
         * il brano successivo diventa l'elemento in posizione 0.
         */
        assertEquals(
                0,
                resultIndex
        );

        assertEquals(
                secondTrack.getId(),
                result.get().getPlayable().getId()
        );
    }

    // TEST: QUANDO SI SALTA L'ULTIMO BRANO, LA STRATEGIA SI ARRESTA

    @Test
    void nextItemShouldReturnEmptyWhenCurrentItemIsLastTrack() {

        Track onlyTrack =
                createTrack("Only Track");

        List<QueueItem> queue =
                new ArrayList<>();

        QueueItem onlyItem =
                createQueueItem(onlyTrack);

        queue.add(onlyItem);

        Optional<QueueItem> result =
                sequentialMode.nextItem(
                        queue,
                        onlyItem
                );

        assertTrue(
                result.isEmpty()
        );

        assertTrue(
                queue.isEmpty()
        );
    }

    // TEST: AVANZANDO DA UNA CODA DI DUE BRANI, DOPO IL SECONDO LA STRATEGIA SI FERMA

    @Test
    void nextItemShouldStopAfterLastTrack() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        List<QueueItem> queue =
                new ArrayList<>();

        QueueItem firstItem =
                createQueueItem(firstTrack);

        QueueItem secondItem =
                createQueueItem(secondTrack);

        queue.add(firstItem);
        queue.add(secondItem);

        Optional<QueueItem> nextItem =
                sequentialMode.nextItem(
                        queue,
                        firstItem
                );

        assertTrue(
                nextItem.isPresent()
        );

        assertEquals(
                secondTrack.getId(),
                nextItem.get().getPlayable().getId()
        );

        Optional<QueueItem> endItem =
                sequentialMode.nextItem(
                        queue,
                        nextItem.get()
                );

        assertTrue(
                endItem.isEmpty()
        );

        assertTrue(
                queue.isEmpty()
        );
    }

    // TEST: SE LA CODA È VUOTA, LA STRATEGIA RESTITUISCE OPTIONAL VUOTO

    @Test
    void nextItemShouldReturnEmptyWhenQueueIsEmpty() {

        List<QueueItem> queue =
                new ArrayList<>();

        Optional<QueueItem> result =
                sequentialMode.nextItem(
                        queue,
                        null
                );

        assertTrue(
                result.isEmpty()
        );

        assertTrue(
                queue.isEmpty()
        );
    }

    // TEST: LA STRATEGIA SEQUENZIALE AVANZA CORRETTAMENTE ANCHE TRA PLAYLIST IN CODA

    @Test
    void nextItemShouldMoveToNextPlaylistInQueue() {

        Playlist firstPlaylist =
                new Playlist("First Playlist");

        Playlist secondPlaylist =
                new Playlist("Second Playlist");

        Playlist thirdPlaylist =
                new Playlist("Third Playlist");

        QueueItem firstItem =
                createQueueItem(firstPlaylist);

        QueueItem secondItem =
                createQueueItem(secondPlaylist);

        QueueItem thirdItem =
                createQueueItem(thirdPlaylist);

        List<QueueItem> queue =
                new ArrayList<>();

        queue.add(firstItem);
        queue.add(secondItem);
        queue.add(thirdItem);

        Optional<QueueItem> result =
                sequentialMode.nextItem(
                        queue,
                        firstItem
                );

        assertTrue(
                result.isPresent()
        );

        assertTrue(
                result.get().isPlaylist()
        );

        assertEquals(
                secondPlaylist.getId(),
                result.get().getPlayable().getId()
        );
    }

    // TEST: LA STRATEGIA SEQUENZIALE SI FERMA QUANDO SI SALTA L'ULTIMA PLAYLIST

    @Test
    void nextItemShouldReturnEmptyWhenCurrentItemIsLastPlaylist() {

        Playlist playlist =
                new Playlist("Only Playlist");

        QueueItem onlyItem =
                createQueueItem(playlist);

        List<QueueItem> queue =
                new ArrayList<>();

        queue.add(onlyItem);

        Optional<QueueItem> result =
                sequentialMode.nextItem(
                        queue,
                        onlyItem
                );

        assertTrue(
                result.isEmpty()
        );

        assertTrue(
                queue.isEmpty()
        );
    }

    // METODO DI SUPPORTO PER CREARE QUEUE ITEM VALIDI

    private QueueItem createQueueItem(Playable playable) {
        return new QueueItem(
                playable,
                null,
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