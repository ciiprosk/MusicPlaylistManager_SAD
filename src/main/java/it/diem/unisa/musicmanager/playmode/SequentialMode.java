package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Queue;

public class SequentialMode implements PlayMode {

    @Override
    public QueueItem nextItem(List<QueueItem> queue, int currentIndex) {

        queue.remove(currentIndex);

        if (currentIndex >= queue.size())
            return null; // era l'ultimo

        return queue.get(currentIndex); // dopo il remove, il prossimo è qui
    }

}
