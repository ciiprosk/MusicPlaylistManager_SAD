package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.playmode.PlayMode;
import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class QueueStrategyInvocationTest {

    private SharedState sharedState;
    private QueueService queueService;

    @BeforeEach
    void setUp() {
        sharedState = new SharedState();
        queueService = new QueueService(sharedState);
    }

    // TEST: LA STRATEGIA VIENE INVOCATA QUANDO SI RICHIEDE IL BRANO SUCCESSIVO

    @Test
    void strategyShouldBeInvokedWhenNextItemIsRequested() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        QueueItem firstItem =
                queueService.addToQueue(firstTrack).get(0);

        QueueItem secondItem =
                queueService.addToQueue(secondTrack).get(0);

        FakePlayMode fakePlayMode =
                new FakePlayMode(secondItem);

        queueService.setCurrentItem(firstItem);
        queueService.setCurrentPlayMode(fakePlayMode);

        QueueItem result =
                queueService.nextItem();

        assertEquals(
                1,
                fakePlayMode.getInvocationCount()
        );

        assertEquals(
                firstItem,
                fakePlayMode.getReceivedCurrentItem()
        );

        assertEquals(
                sharedState.getQueue(),
                fakePlayMode.getReceivedQueue()
        );

        assertEquals(
                secondItem,
                result
        );

        assertEquals(
                secondItem,
                queueService.getCurrentItem()
        );
    }

    // TEST: LA STRATEGIA VIENE INVOCATA ANCHE QUANDO SI SALTA MANUALMENTE LA TRACCIA CORRENTE

    @Test
    void strategyShouldBeInvokedWhenCurrentTrackIsSkipped() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        QueueItem firstItem =
                queueService.addToQueue(firstTrack).get(0);

        QueueItem secondItem =
                queueService.addToQueue(secondTrack).get(0);

        FakePlayMode fakePlayMode =
                new FakePlayMode(secondItem);

        queueService.setCurrentItem(firstItem);
        queueService.setCurrentPlayMode(fakePlayMode);

        QueueItem skippedResult =
                queueService.nextItem();

        assertEquals(
                1,
                fakePlayMode.getInvocationCount()
        );

        assertEquals(
                secondItem,
                skippedResult
        );
    }

    // TEST: LA STRATEGIA VIENE INVOCATA PIÙ VOLTE SE SI AVANZA PIÙ VOLTE NELLA CODA

    @Test
    void strategyShouldBeInvokedEveryTimeNextItemIsRequested() {

        Track firstTrack =
                createTrack("First Track");

        Track secondTrack =
                createTrack("Second Track");

        QueueItem firstItem =
                queueService.addToQueue(firstTrack).get(0);

        QueueItem secondItem =
                queueService.addToQueue(secondTrack).get(0);

        FakePlayMode fakePlayMode =
                new FakePlayMode(secondItem);

        queueService.setCurrentItem(firstItem);
        queueService.setCurrentPlayMode(fakePlayMode);

        queueService.nextItem();
        queueService.nextItem();

        assertEquals(
                2,
                fakePlayMode.getInvocationCount()
        );
    }

    // PLAYMODE FALSA USATA SOLO PER VERIFICARE CHE QUEUESERVICE INVOCHI LA STRATEGIA

    private static class FakePlayMode implements PlayMode {

        private int invocationCount = 0;
        private final QueueItem itemToReturn;
        private List<QueueItem> receivedQueue;
        private QueueItem receivedCurrentItem;

        private FakePlayMode(QueueItem itemToReturn) {
            this.itemToReturn = itemToReturn;
        }

        @Override
        public Optional<QueueItem> nextItem(List<QueueItem> queue, QueueItem currentItem) {

            invocationCount++;
            receivedQueue = queue;
            receivedCurrentItem = currentItem;

            return Optional.of(itemToReturn);
        }

        @Override
        public boolean hasNext(List<QueueItem> queue, QueueItem currentItem) {
            return itemToReturn != null;
        }

        public int getInvocationCount() {
            return invocationCount;
        }

        public List<QueueItem> getReceivedQueue() {
            return receivedQueue;
        }

        public QueueItem getReceivedCurrentItem() {
            return receivedCurrentItem;
        }
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