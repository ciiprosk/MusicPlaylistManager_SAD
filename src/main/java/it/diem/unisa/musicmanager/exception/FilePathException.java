package it.diem.unisa.musicmanager.exception;

/**
 * Eccezione personalizzata lanciata in caso di errori relativi al percorso dei file
 * (es. percorsi non trovati, formati non validi o problemi di accesso).
 */
public class FilePathException extends RuntimeException {
    /**
     * Costruisce una nuova eccezione con il messaggio di errore specificato.
     * 
     * @param message Il messaggio dettagliato dell'errore.
     */
    public FilePathException(String message) {
        super(message);
    }
}
