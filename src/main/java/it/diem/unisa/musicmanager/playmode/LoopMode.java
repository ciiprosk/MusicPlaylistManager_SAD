package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;

/**
 * Strategia di riproduzione in loop (ciclica) della coda.
 * Quando un brano termina, viene rimosso dalla sua posizione corrente e riaccodato alla fine
 * della lista. In questo modo, la coda continua a ruotare indefinitamente finché contiene almeno un elemento.
 */
public class LoopMode implements PlayMode {

    /**
     * Costruttore di default.
     */
    public LoopMode() {
        // Costruttore di default
    }

    /**
     * Restituisce il brano in testa alla coda ed inserisce il brano corrente consumato in fondo ad essa.
     * Consente la rotazione ciclica degli elementi della coda.
     *
     * @param queue       La lista degli elementi correntemente in coda.
     * @param currentItem L'elemento attualmente in riproduzione.
     * @return Un {@link Optional} contenente il prossimo {@link QueueItem} da riprodurre (in testa),
     * oppure {@link Optional#empty()} se la coda è vuota.
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
     * Verifica se sono presenti elementi nella coda per continuare la riproduzione in loop.
     * La riproduzione può continuare indefinitamente purché la coda non sia vuota.
     *
     * @param queue       La lista degli elementi in coda.
     * @param currentItem L'elemento attualmente in riproduzione.
     * @return {@code true} se la coda contiene almeno un elemento, {@code false} altrimenti.
     */
    @Override
    public boolean hasNext(List<QueueItem> queue, QueueItem currentItem) {
        return !queue.isEmpty();
    }

}
