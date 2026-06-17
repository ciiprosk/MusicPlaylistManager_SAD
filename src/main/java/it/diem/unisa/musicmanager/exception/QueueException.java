package it.diem.unisa.musicmanager.exception;

/**
 * Eccezione personalizzata lanciata in caso di errori o operazioni non consentite
 * all'interno della coda di riproduzione.
 */
public class QueueException extends RuntimeException {
    /**
     * Costruisce una nuova eccezione con il messaggio di errore specificato.
     * 
     * @param message Il messaggio dettagliato dell'errore.
     */
    public QueueException(String message) {
        super(message);
    }
}
