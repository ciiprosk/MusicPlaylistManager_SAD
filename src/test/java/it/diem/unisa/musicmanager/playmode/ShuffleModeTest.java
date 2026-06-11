package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playable;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShuffleModeTest {

    private ShuffleMode shuffleMode;

    @BeforeEach
    void setUp() {
        shuffleMode = new ShuffleMode();
    }

    // TEST: LA STRATEGIA SHUFFLE RESTITUISCE TUTTI I BRANI ESATTAMENTE UNA VOLTA

    @Test
    void shuffleModeShouldReturnAllTracksExactlyOnceWithoutDuplicates() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        Track thirdTrack =
                createTrack("Third Track");

        Track fourthTrack =
                createTrack("Fourth Track");

        List<QueueItem> queue =
                new ArrayList<>();

        queue.add(createQueueItem(firstTrack));
        queue.add(createQueueItem(secondTrack));
        queue.add(createQueueItem(thirdTrack));
        queue.add(createQueueItem(fourthTrack));

        Set<UUID> expectedTrackIds =
                Set.of(
                        firstTrack.getId(),
                        secondTrack.getId(),
                        thirdTrack.getId(),
                        fourthTrack.getId()
                );

        Set<UUID> returnedTrackIds =
                new HashSet<>();

        QueueItem currentItem =
                null;

        while (true) {

            Optional<QueueItem> nextItem =
                    shuffleMode.nextItem(
                            queue,
                            currentItem
                    );

            if (nextItem.isEmpty()) {
                break;
            }

            QueueItem item =
                    nextItem.get();

            UUID returnedId =
                    item.getPlayable().getId();

            assertFalse(
                    returnedTrackIds.contains(returnedId),
                    "La modalità shuffle ha restituito un brano duplicato"
            );

            returnedTrackIds.add(returnedId);

            currentItem =
                    item;
        }

        assertEquals(
                expectedTrackIds,
                returnedTrackIds
        );

        assertTrue(
                queue.isEmpty()
        );
    }

    // TEST: LA STRATEGIA SHUFFLE NON RESTITUISCE DUPLICATI

    @Test
    void shuffleModeShouldNeverReturnDuplicateTracks() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        Track thirdTrack =
                createTrack("Third Track");

        List<QueueItem> queue =
                new ArrayList<>();

        queue.add(createQueueItem(firstTrack));
        queue.add(createQueueItem(secondTrack));
        queue.add(createQueueItem(thirdTrack));

        Set<UUID> returnedIds =
                new HashSet<>();

        QueueItem currentItem =
                null;

        while (true) {

            Optional<QueueItem> nextItem =
                    shuffleMode.nextItem(
                            queue,
                            currentItem
                    );

            if (nextItem.isEmpty()) {
                break;
            }

            QueueItem item =
                    nextItem.get();

            UUID id =
                    item.getPlayable().getId();

            assertTrue(
                    returnedIds.add(id),
                    "Brano duplicato restituito dalla modalità shuffle"
            );

            currentItem =
                    item;
        }

        assertEquals(
                3,
                returnedIds.size()
        );

        assertTrue(
                queue.isEmpty()
        );
    }

    // TEST: SE LA CODA È VUOTA, SHUFFLE RESTITUISCE OPTIONAL VUOTO

    @Test
    void shuffleModeShouldReturnEmptyWhenQueueIsEmpty() {

        List<QueueItem> queue =
                new ArrayList<>();

        Optional<QueueItem> result =
                shuffleMode.nextItem(
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