package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;

public class LoopMode implements PlayMode {

    @Override
    public QueueItem nextItem(List<QueueItem> queue, int currentIndex) {

        if (queue.isEmpty())
            return null;

        return queue.get((currentIndex + 1) % queue.size());

    }

}
