package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Random;

public class ShuffleMode implements PlayMode {

    private final Random random = new Random();

    @Override
    public QueueItem nextItem(List<QueueItem> queue, QueueItem currentItem) {

        int currentIndex = queue.indexOf(currentItem);

        // Rimuovi il brano corrente dalla coda
        queue.remove(currentIndex);

        // Se la coda è vuota dopo la rimozione, la riproduzione è finita
        if (queue.isEmpty())
            return null;

        // Scegli un indice casuale tra i brani rimanenti
        int randomIndex = random.nextInt(queue.size());

        return queue.get(randomIndex);

    }

}