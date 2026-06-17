package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;

/**
 * Strategia di riproduzione sequenziale della coda (standard).
 * I brani vengono riprodotti nell'ordine in cui compaiono nella lista. Una volta riprodotto,
 * il brano viene rimosso (consumato) dalla coda e la riproduzione termina dopo l'ultimo elemento.
 */
public class SequentialMode implements PlayMode {

    /**
     * Costruttore di default.
     */
    public SequentialMode() {
        // Costruttore di default
    }

    /**
     * Restituisce il brano successivo nella coda seguendo l'ordine sequenziale.
     * Consuma (rimuove) il brano corrente se presente nella coda e restituisce la nuova testa.
     * Se il brano corrente non è in coda, restituisce il primo elemento disponibile senza rimuoverlo.
     *
     * @param queue       La lista degli elementi correntemente in coda.
     * @param currentItem L'elemento attualmente in riproduzione.
     * @return Un {@link Optional} contenente il prossimo {@link QueueItem} da riprodurre,
     * oppure {@link Optional#empty()} se la coda è esaurita.
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
            return Optional.of(next);
        }
    }

    /**
     * Verifica se sono presenti altri elementi da riprodurre in coda dopo quello corrente.
     *
     * @param queue       La lista degli elementi correntemente in coda.
     * @param currentItem L'elemento attualmente in riproduzione.
     * @return {@code true} se la coda non è vuota e vi sono elementi successivi a quello corrente;
     * {@code false} se la coda è vuota o se l'elemento corrente è l'ultimo.
     */
    @Override
    public boolean hasNext(List<QueueItem> queue, QueueItem currentItem) {
        if (queue.isEmpty()) return false;
        int index = queue.indexOf(currentItem);
        if (index == -1) return true;
        return index < queue.size() - 1;     // ci sono elementi dopo
    }

}
