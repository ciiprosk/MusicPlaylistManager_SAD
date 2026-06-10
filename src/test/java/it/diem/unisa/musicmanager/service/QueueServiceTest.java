package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueueServiceTest {

    private QueueService queueService;
    private SharedState sharedState;

    @BeforeEach
    void setUp() {
        sharedState = new SharedState();
        queueService = new QueueService(sharedState);
    }

    // TEST AGGIUNTA DI UNA SINGOLA TRACCIA ALLA CODA DI ASCOLTO

    @Test
    void addToQueueShouldAddSingleTrackCorrectly() {

        Track track = createTrack("Numb");

        List<QueueItem> result =
                queueService.addToQueue(track);

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                1,
                sharedState.getQueue().size()
        );

        QueueItem queueItem =
                sharedState.getQueue().get(0);

        assertTrue(
                queueItem.isTrack()
        );

        assertFalse(
                queueItem.isPlaylist()
        );

        assertEquals(
                track.getId(),
                queueItem.getPlayable().getId()
        );

        assertNull(
                queueItem.getBelongsToPlaylist()
        );
    }

    // TEST AGGIUNTA DI PIÙ TRACCE SINGOLE ALLA CODA MANTIENE L'ORDINE DI INSERIMENTO

    @Test
    void addToQueueShouldKeepSingleTracksInsertionOrder() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        queueService.addToQueue(firstTrack);
        queueService.addToQueue(secondTrack);

        assertEquals(
                2,
                sharedState.getQueue().size()
        );

        assertEquals(
                firstTrack.getId(),
                sharedState.getQueue().get(0).getPlayable().getId()
        );

        assertEquals(
                secondTrack.getId(),
                sharedState.getQueue().get(1).getPlayable().getId()
        );

        assertNull(
                sharedState.getQueue().get(0).getBelongsToPlaylist()
        );

        assertNull(
                sharedState.getQueue().get(1).getBelongsToPlaylist()
        );
    }

    // TEST AGGIUNTA DI UNA PLAYLIST ALLA CODA DI ASCOLTO

    @Test
    void addToQueueShouldAddPlaylistTracksCorrectly() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        Playlist playlist =
                new Playlist("Rock Playlist");

        playlist.addTrack(firstTrack);
        playlist.addTrack(secondTrack);

        List<QueueItem> result =
                queueService.addToQueue(playlist);

        assertNotNull(result);

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                2,
                sharedState.getQueue().size()
        );

        QueueItem firstItem =
                sharedState.getQueue().get(0);

        QueueItem secondItem =
                sharedState.getQueue().get(1);

        assertTrue(
                firstItem.isTrack()
        );

        assertTrue(
                secondItem.isTrack()
        );

        assertFalse(
                firstItem.isPlaylist()
        );

        assertFalse(
                secondItem.isPlaylist()
        );

        assertEquals(
                firstTrack.getId(),
                firstItem.getPlayable().getId()
        );

        assertEquals(
                secondTrack.getId(),
                secondItem.getPlayable().getId()
        );

        assertEquals(
                playlist.getId(),
                firstItem.getBelongsToPlaylist()
        );

        assertEquals(
                playlist.getId(),
                secondItem.getBelongsToPlaylist()
        );
    }

    // TEST AGGIUNTA PLAYLIST MANTIENE L'ORDINE DELLE TRACCE DELLA PLAYLIST

    @Test
    void addToQueueShouldKeepPlaylistTrackOrder() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        Track thirdTrack =
                createTrack("Third Track");

        Playlist playlist =
                new Playlist("Ordered Playlist");

        playlist.addTrack(firstTrack);
        playlist.addTrack(secondTrack);
        playlist.addTrack(thirdTrack);

        queueService.addToQueue(playlist);

        assertEquals(
                3,
                sharedState.getQueue().size()
        );

        assertEquals(
                firstTrack.getId(),
                sharedState.getQueue().get(0).getPlayable().getId()
        );

        assertEquals(
                secondTrack.getId(),
                sharedState.getQueue().get(1).getPlayable().getId()
        );

        assertEquals(
                thirdTrack.getId(),
                sharedState.getQueue().get(2).getPlayable().getId()
        );
    }

    // TEST AGGIUNTA NULL NON MODIFICA LA CODA

    @Test
    void addToQueueShouldReturnNullAndNotModifyQueueWhenPlayableIsNull() {

        List<QueueItem> result =
                queueService.addToQueue(null);

        assertNull(result);

        assertTrue(
                sharedState.getQueue().isEmpty()
        );
    }

    // TEST LA LISTA RESTITUITA CONTIENE GLI STESSI ELEMENTI AGGIUNTI ALLO SHARED STATE

    @Test
    void addToQueueShouldReturnAddedQueueItems() {

        Track track =
                createTrack("Numb");

        List<QueueItem> result =
                queueService.addToQueue(track);

        assertNotNull(result);

        assertEquals(
                result.get(0),
                sharedState.getQueue().get(0)
        );
    }

    // METODO DI SUPPORTO PER CREARE TRACCE VALIDE NEI TEST

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

    // TEST AVANZAMENTO AL BRANO SUCCESSIVO IN MODALITÀ SEQUENZIALE

    @Test
    void nextItemShouldMoveToNextTrackInSequentialMode() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        Track thirdTrack =
                createTrack("Third Track");

        queueService.addToQueue(firstTrack);
        queueService.addToQueue(secondTrack);
        queueService.addToQueue(thirdTrack);

        QueueItem firstItem =
                sharedState.getQueue().get(0);

        queueService.setCurrentItem(firstItem);

        QueueItem nextItem =
                queueService.nextItem();

        assertNotNull(
                nextItem
        );

        assertEquals(
                secondTrack.getId(),
                nextItem.getPlayable().getId()
        );

        assertEquals(
                secondTrack.getId(),
                queueService.getCurrentItem().getPlayable().getId()
        );
    }

    // TEST INTERRUZIONE RIPRODUZIONE QUANDO SI SALTA L'ULTIMO BRANO DELLA CODA

    @Test
    void nextItemShouldReturnNullWhenSkippingLastTrackInSequentialMode() {

        Track onlyTrack =
                createTrack("Only Track");

        queueService.addToQueue(onlyTrack);

        QueueItem onlyItem =
                sharedState.getQueue().get(0);

        queueService.setCurrentItem(onlyItem);

        QueueItem nextItem =
                queueService.nextItem();

        assertNull(
                nextItem
        );

        assertNull(
                queueService.getCurrentItem()
        );

        assertTrue(
                sharedState.getQueue().isEmpty()
        );
    }

    // TEST AVANZAMENTO SEQUENZIALE FINO ALLA FINE DELLA CODA

    @Test
    void nextItemShouldAdvanceUntilQueueEnds() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        Track thirdTrack =
                createTrack("Third Track");

        queueService.addToQueue(firstTrack);
        queueService.addToQueue(secondTrack);
        queueService.addToQueue(thirdTrack);

        QueueItem firstItem =
                sharedState.getQueue().get(0);

        queueService.setCurrentItem(firstItem);

        QueueItem secondItem =
                queueService.nextItem();

        assertNotNull(secondItem);
        assertEquals(
                secondTrack.getId(),
                secondItem.getPlayable().getId()
        );

        QueueItem thirdItem =
                queueService.nextItem();

        assertNotNull(thirdItem);
        assertEquals(
                thirdTrack.getId(),
                thirdItem.getPlayable().getId()
        );

        QueueItem endItem =
                queueService.nextItem();

        assertNull(
                endItem
        );

        assertNull(
                queueService.getCurrentItem()
        );

        assertTrue(
                sharedState.getQueue().isEmpty()
        );
    }
}