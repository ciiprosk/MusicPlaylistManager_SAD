package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;

/**
 * Classe che implementa il modo di riproduzione loop.
 */
public class LoopMode implements PlayMode {

    /**
     * Il metodo che restituisce il prossimo brano della playlist.
     * @param queue una lista di elementi della coda.
     * @param currentItem elemento corrente della coda.
     * @return un Optional contenente l'elemento successivo nella coda, se presente.
     */
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

    /**
     * Verifica se ci sono elementi nella coda.
     * @param queue una lista di elementi della coda.
     * @param currentItem elemento corrente della coda.
     * @return true se ci sono elementi nella coda, false altrimenti.
     */
    @Override
    public boolean hasNext(List<QueueItem> queue, QueueItem currentItem) {

        return !queue.isEmpty();

    }

}
