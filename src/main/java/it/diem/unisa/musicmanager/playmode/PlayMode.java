package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;

/**
 * Interfaccia che definisce il comportamento di un modo di riproduzione.
 */
public interface PlayMode {

    /**
     * Restituisce l'elemento successivo nella coda.
     *
     * @param queue       una lista di elementi della coda.
     * @param currentItem elemento corrente della coda.
     * @return un Optional contenente l'elemento successivo nella coda, se presente.
     */
    Optional<QueueItem> nextItem(List<QueueItem> queue, QueueItem currentItem);

    /**
     * Restituisce un booleano che indica se la coda contiene elementi successivi.
     *
     * @param queue       una lista di elementi della coda.
     * @param currentItem elemento corrente della coda.
     * @return true se la coda contiene elementi successivi, false altrimenti.
     */
    boolean hasNext(List<QueueItem> queue, QueueItem currentItem);

}
