package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;

public class LoopMode implements PlayMode {

    @Override
    public QueueItem nextItem(List<QueueItem> queue, QueueItem currentItem) {

        if (queue.isEmpty())
            return null;

        int currentIndex = queue.indexOf(currentItem);

        return queue.get((currentIndex + 1) % queue.size());

    }

}
