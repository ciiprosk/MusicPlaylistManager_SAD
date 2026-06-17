package it.diem.unisa.musicmanager.exception;

/**
 * Eccezione personalizzata lanciata per segnalare errori durante la lettura,
 * la scrittura o il parsing di file in formato JSON.
 */
public class JSONFileException extends RuntimeException {
    /**
     * Costruisce una nuova eccezione con il messaggio di errore specificato.
     * 
     * @param message Il messaggio dettagliato dell'errore.
     */
    public JSONFileException(String message) {
        super(message);
    }
}
