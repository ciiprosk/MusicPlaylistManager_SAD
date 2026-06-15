package it.diem.unisa.musicmanager.command;

import java.util.Optional;

/**
 * Rappresenta un comando eseguibile nel sistema.
 * Il Command Pattern incapsula un'azione
 * in un oggetto, permettendo di gestire operazioni come esecuzione, undo
 * e storicizzazione delle azioni.
 * L'implementazione di questa interfaccia consente di uniformare
 * l'esecuzione delle operazioni e supportare funzionalità come undo/redo.
 */
public interface Command {
    /**
     * Esegue il comando.
     * Se l'operazione va a buon fine, viene restituito {@code Optional.empty()}.
     * In caso di errore, viene restituito un {@code Optional<String>}
     * contenente il messaggio di errore.
     * @return un Optional vuoto se l'esecuzione ha successo,
     * oppure un Optional contenente un messaggio di errore
     */
    Optional<String> execute();

    /**
     * Annulla l'effetto dell'ultima esecuzione del comando.
     * Questo metodo deve ripristinare lo stato precedente all'esecuzione
     * del comando.
     */
    void undo();

    String getDescription();
}
