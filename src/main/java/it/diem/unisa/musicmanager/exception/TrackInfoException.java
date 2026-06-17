package it.diem.unisa.musicmanager.exception;

/**
 * Eccezione personalizzata lanciata in caso di violazione delle regole di business
 * o delle informazioni relative ad una traccia musicale (es. titolo vuoto, autore troppo lungo,
 * anno futuro, ecc.).
 */
public class TrackInfoException extends RuntimeException {
    /**
     * Costruisce una nuova eccezione con il messaggio di errore specificato.
     * 
     * @param message Il messaggio dettagliato dell'errore.
     */
    public TrackInfoException(String message) {
        super(message);
    }
}
