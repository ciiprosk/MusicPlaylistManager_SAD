package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Strategia di riproduzione casuale (shuffle) della coda.
 * Consuma il brano corrente rimuovendolo e seleziona in modo casuale il prossimo elemento
 * tra quelli rimanenti nella coda, posizionandolo all'indice 0 (testa) per l'interazione con la GUI.
 */
public class ShuffleMode implements PlayMode {

    /**
     * Generatore di numeri casuali utilizzato per selezionare il prossimo brano.
     */
    private final Random random = new Random();

    /**
     * Costruttore di default.
     */
    public ShuffleMode() {
        // Costruttore di default
    }

    /**
     * Estrae in modo casuale il brano successivo tra quelli rimanenti in coda.
     * Rimuove il brano corrente dalla coda e posiziona la traccia estratta all'indice 0 della lista.
     *
     * @param queue       La lista degli elementi correntemente in coda.
     * @param currentItem L'elemento attualmente in riproduzione.
     * @return Un {@link Optional} contenente il prossimo {@link QueueItem} estratto casualmente,
     * oppure {@link Optional#empty()} se la coda è esaurita.
     */
    @Override
    public Optional<QueueItem> nextItem(List<QueueItem> queue, QueueItem currentItem) {
        if (queue.isEmpty())
            return Optional.empty();

        int currentIndex = queue.indexOf(currentItem);

        // Consuma il brano corrente
        if (currentIndex != -1) {
            queue.remove(currentIndex);
        }

        if (queue.isEmpty())
            return Optional.empty();

        // Pesca a caso tra i rimanenti
        int randomIndex = random.nextInt(queue.size());
        QueueItem next = queue.get(randomIndex);

        // Porta il prossimo in testa: così il corrente è SEMPRE a indice 0
        // ed è questo che fa funzionare il filtro della vista (mostra tutto dopo lo 0)
        queue.remove(randomIndex);
        queue.add(0, next);

        return Optional.of(next);
    }

    /**
     * Verifica se sono presenti altri elementi da riprodurre casualmente oltre a quello corrente.
     *
     * @param queue       La lista degli elementi correntemente in coda.
     * @param currentItem L'elemento attualmente in riproduzione.
     * @return {@code true} se la coda contiene almeno un elemento (se non si è ancora partiti)
     * oppure se contiene almeno un elemento oltre a quello corrente; {@code false} altrimenti.
     */
    @Override
    public boolean hasNext(List<QueueItem> queue, QueueItem currentItem) {

        if (queue.isEmpty())
            return false;

        int index = queue.indexOf(currentItem);

        if (index == -1)
            return !queue.isEmpty();

        return queue.size() > 1;                  // c'è almeno un altro oltre al corrente

    }

}