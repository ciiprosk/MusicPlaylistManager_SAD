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
        if (queue.isEmpty()) {
            return null;
        }

        int currentIndex = queue.indexOf(currentItem);

        if (currentIndex != -1) {
            queue.remove(currentIndex);
            if (currentIndex >= queue.size()) {
                return null; // era l'ultimo
            }
            return queue.get(currentIndex); // dopo il remove, il prossimo è qui
        } else {
            // Il brano attuale non è nella coda, peschiamo il primo!
            QueueItem next = queue.get(0);
            // Rimuoviamo il primo per "consumarlo"
            // Wait, se lo rimuoviamo subito sparisce dalla coda prima di finire?
            // Per ora lasciamolo in coda per vederlo. Lo rimuoveremo quando passerà al successivo!
            return next;
        }
    }

}
