package it.diem.unisa.musicmanager.exception;

/**
 * Eccezione personalizzata lanciata in caso di violazione delle regole di business
 * o delle informazioni relative ad una playlist (es. nome non valido o troppo lungo).
 */
public class PlaylistInfoException extends RuntimeException {
    /**
     * Costruisce una nuova eccezione con il messaggio di errore specificato.
     * 
     * @param message Il messaggio dettagliato dell'errore.
     */
    public PlaylistInfoException(String message) {
        super(message);
    }
}
