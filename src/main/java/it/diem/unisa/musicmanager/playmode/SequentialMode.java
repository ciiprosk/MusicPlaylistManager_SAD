package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Queue;

/**
 * È la classe che implementa il modo di riproduzione sequenziale.
 */
public class SequentialMode implements PlayMode {

    /**
     * Il metodo che restituisce il prossimo brano della playlist.
     * @param queue è la coda dei brani.
     * @param currentItem è il brano corrente.
     * @return il prossimo brano della playlist.
     */
    @Override
    public QueueItem nextItem(List<QueueItem> queue, QueueItem currentItem) {

        int currentIndex = queue.indexOf(currentItem);

        queue.remove(currentIndex);

        if (currentIndex >= queue.size())
            return null; // era l'ultimo

        return queue.get(currentIndex); // dopo il remove, il prossimo è qui
    }

}
