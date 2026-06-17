package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;

public class LoopMode implements PlayMode {

    @Override
    public Optional<QueueItem> nextItem(List<QueueItem> queue, QueueItem currentItem) {

        if (queue.isEmpty()) return Optional.empty();

        int currentIndex = queue.indexOf(currentItem);
        if (currentIndex != -1) {
            QueueItem consumed = queue.remove(currentIndex);
            queue.add(consumed);   // lo rimetto in fondo: il loop continua a girare
        }
        return Optional.of(queue.getFirst());   // il prossimo è sempre in testa
    }

    @Override
    public boolean hasNext(List<QueueItem> queue, QueueItem currentItem) {

        return !queue.isEmpty();

    }

}
