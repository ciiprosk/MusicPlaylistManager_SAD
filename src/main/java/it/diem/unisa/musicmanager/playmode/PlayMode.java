package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;

/**
 * Interfaccia che definisce il contratto per una strategia di riproduzione della coda (Pattern Strategy).
 * Classi diverse che implementano questa interfaccia definiscono algoritmi differenti per determinare
 * qual è il brano successivo da riprodurre e se la riproduzione può proseguire.
 */
public interface PlayMode {

    /**
     * Calcola e restituisce l'elemento successivo nella coda di riproduzione, applicando l'algoritmo
     * specifico della strategia (es. sequenziale, casuale, loop).
     *
     * @param queue       La lista degli elementi correntemente in coda.
     * @param currentItem L'elemento della coda attualmente in riproduzione.
     * @return Un {@link Optional} contenente il prossimo {@link QueueItem} da riprodurre,
     * oppure {@link Optional#empty()} se la coda è finita.
     */
    Optional<QueueItem> nextItem(List<QueueItem> queue, QueueItem currentItem);

    /**
     * Verifica se sono presenti ulteriori elementi da riprodurre in coda in base alla strategia attiva.
     *
     * @param queue       La lista degli elementi correntemente in coda.
     * @param currentItem L'elemento della coda attualmente in riproduzione.
     * @return {@code true} se esiste un elemento successivo da riprodurre, {@code false} altrimenti.
     */
    boolean hasNext(List<QueueItem> queue, QueueItem currentItem);

}
