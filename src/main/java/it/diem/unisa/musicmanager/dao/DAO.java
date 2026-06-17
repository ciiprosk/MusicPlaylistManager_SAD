package it.diem.unisa.musicmanager.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interfaccia generica per i DAO (Data Access Object).
 * @param <T> tipo di oggetto da gestire.
 */
public interface DAO<T> {

    /**
     * Metodo da implementare che restituisce tutti gli oggetti presenti nel database.
     * @return gli oggetti presenti nel database.
     */
    List<T> selectAll();

    /**
     * Metodo da implementare per inserire un oggetto nel database.
     * @param t generico, l'oggetto da inserire.
     */
    void insert(T t);

    /**
     * Metodo da implementare per aggiornare un oggetto nel database.
     * @param t generico, l'oggetto da aggiornare.
     */
    void update(T t);

    /**
     * Metodo da implementare per eliminare un oggetto dal database.
     * @param id identificatore univoco dell'oggetto da eliminare.
     */
    void delete(UUID id);

    /**
     * Metodo da implementare per cercare un oggetto nel database tramite l'identificatore univoco.
     * @param id identificatore univoco dell'oggetto da cercare.
     * @return un oggetto opzionale che contiene l'oggetto se trovato, altrimenti Optional.empty().
     */
    Optional<T> searchById(UUID id);

    /**
     * Metodo da implementare per cercare un oggetto nel database.
     * @param t generico, l'oggetto da cercare.
     * @return true se esiste già l'oggetto nel database, false altrimenti.
     */
    boolean isDuplicated(T t);

}
