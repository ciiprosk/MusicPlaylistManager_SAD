package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Classe che implementa il modo di riproduzione shuffle.
 */
public class ShuffleMode implements PlayMode {

    private final Random random = new Random();

    /**
     * Il metodo che restituisce il prossimo brano della playlist.
     *
     * @param queue       una lista di elementi della coda.
     * @param currentItem elemento corrente della coda.
     * @return un Optional contenente l'elemento successivo nella coda, se presente.
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
     * Verifica se ci sono elementi nella coda.
     *
     * @param queue       una lista di elementi della coda.
     * @param currentItem elemento corrente della coda.
     * @return true se ci sono elementi nella coda, false altrimenti.
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