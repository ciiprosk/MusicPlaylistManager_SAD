package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;

/**
 * È la classe che implementa il modo di riproduzione sequenziale.
 */
public class SequentialMode implements PlayMode {

    /**
     * Il metodo che restituisce il prossimo brano della playlist.
     *
     * @param queue       è la coda dei brani.
     * @param currentItem è il brano corrente.
     * @return il prossimo brano della playlist.
     */
    @Override
    public Optional<QueueItem> nextItem(List<QueueItem> queue, QueueItem currentItem) {
        if (queue.isEmpty()) {
            return Optional.empty();
        }

        int currentIndex = queue.indexOf(currentItem);

        if (currentIndex != -1) {
            queue.remove(currentIndex);
            if (currentIndex >= queue.size()) {
                return Optional.empty(); // era l'ultimo
            }
            return Optional.of(queue.get(currentIndex)); // dopo il remove, il prossimo è qui
        } else {
            // Il brano attuale non è nella coda, peschiamo il primo!
            QueueItem next = queue.get(0);
            // Rimuoviamo il primo per "consumarlo"
            // Wait, se lo rimuoviamo subito sparisce dalla coda prima di finire?
            // Per ora lasciamolo in coda per vederlo. Lo rimuoveremo quando passerà al successivo!
            return Optional.of(next);
        }
    }

    /**
     * Verifica se ci sono elementi nella coda.
     *
     * @param queue       una lista di elementi della coda.
     * @param currentItem elemento corrente della coda.
     * @return true se ci sono elementi nella coda, false altrimenti.
     */
    @Override
    public boolean hasNext(List<QueueItem> queue, QueueItem currentItem) {
        if (queue.isEmpty()) return false;
        int index = queue.indexOf(currentItem);
        if (index == -1) return true;
        return index < queue.size() - 1;     // ci sono elementi dopo
    }

}
