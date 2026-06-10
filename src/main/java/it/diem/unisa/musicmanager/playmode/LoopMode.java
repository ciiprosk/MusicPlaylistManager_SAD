package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;

public class LoopMode implements PlayMode {

    @Override
    public Optional<QueueItem> nextItem(List<QueueItem> queue, QueueItem currentItem) {

        if (queue.isEmpty())
            return Optional.empty();

        int currentIndex = queue.indexOf(currentItem);

        if (currentIndex == -1) {
            return Optional.of(queue.get(0));
        }

        return Optional.of(queue.get((currentIndex + 1) % queue.size()));

    }

    @Override
    public boolean hasNext(List<QueueItem> queue, QueueItem currentItem) {

        return !queue.isEmpty();

    }

}
