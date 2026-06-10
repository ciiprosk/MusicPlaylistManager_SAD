package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ShuffleMode implements PlayMode {

    private final Random random = new Random();

    @Override
    public Optional<QueueItem> nextItem(List<QueueItem> queue, QueueItem currentItem) {

        if (queue.isEmpty())
            return Optional.empty();

        int currentIndex = queue.indexOf(currentItem);

        if (currentIndex != -1) {
            // Rimuovi il brano corrente dalla coda
            queue.remove(currentIndex);
        }

        // Se la coda è vuota dopo la rimozione, la riproduzione è finita
        if (queue.isEmpty())
            return Optional.empty();

        // Scegli un indice casuale tra i brani rimanenti
        int randomIndex = random.nextInt(queue.size());

        return Optional.of(queue.get(randomIndex));


    }

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