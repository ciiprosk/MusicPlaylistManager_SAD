package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.exception.TrackInfoException;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Servizio per la gestione del ciclo di vita e delle operazioni CRUD (Create, Read, Update, Delete)
 * relative alle tracce musicali ({@link Track}).
 * Coordina l'accesso ai dati persistiti tramite il {@link DAO}, l'aggiornamento dello stato globale
 * condiviso ({@link SharedState}) per riflettere le modifiche nella GUI e la notifica ad altri
 * componenti (tramite il pattern Observer) in caso di eliminazione o modifica delle tracce.
 */
public class TrackService {

    /**
     * Componente DAO utilizzato per le operazioni di persistenza delle tracce su file JSON.
     */
    private final DAO<Track> trackDAO;

    /**
     * Stato globale condiviso dell'applicazione che contiene la lista osservabile delle tracce caricate.
     */
    private final SharedState sharedState;

    /**
     * Lista degli osservatori registrati per ricevere notifiche sugli eventi relativi alle tracce (es. eliminazione).
     */
    private final List<TrackObserver> observers = new ArrayList<>();

    /**
     * Costruisce un nuovo {@code TrackService} con i riferimenti al DAO e allo stato condiviso.
     *
     * @param trackDAO    Il DAO per la gestione della persistenza delle tracce.
     * @param sharedState Lo stato condiviso globale dell'applicazione.
     */
    public TrackService(DAO<Track> trackDAO, SharedState sharedState) {
        this.trackDAO = trackDAO;
        this.sharedState = sharedState;
    }

    /**
     * Restituisce la lista osservabile di tutte le tracce caricate nell'applicazione.
     *
     * @return L'{@link ObservableList} di {@link Track} presente nello stato condiviso.
     */
    public ObservableList<Track> getAllTracks() {
        return sharedState.getALlTracks();
    }

    /**
     * Restituisce la lista delle 5 tracce più ascoltate caricate nello stato condiviso,
     * ordinate in modo decrescente in base al numero di riproduzioni.
     * Se le tracce totali sono meno di 5, vengono restituite tutte quelle disponibili.
     *
     * @return Una lista di massimo 5 oggetti {@link Track} più riprodotti.
     */
    public List<Track> getTop5MostPlayedTracks() {
        return sharedState.getALlTracks()
                .stream()
                .sorted((t1, t2) -> Integer.compare(t2.getPlayCount(), t1.getPlayCount()))
                .limit(5)
                .toList();
    }

    /**
     * Sostituisce una traccia all'interno della lista osservabile dello stato condiviso
     * per forzare l'aggiornamento grafico dei componenti legati ad essa.
     *
     * @param track La traccia aggiornata da reinserire nello stato condiviso.
     */
    private void updateInState(Track track) {

        ObservableList<Track> tracks = sharedState.getALlTracks();

        java.util.stream.IntStream.range(0, tracks.size()) //scorre gli indici
                .filter(index -> tracks.get(index).getId().equals(track.getId())) //prende la traccia da aggiornare
                .findFirst()    //prima occorrenza, così da non dover scorrere dopo aver trovato la traccia giusta
                .ifPresent(index -> tracks.set(index, track));  //se c'è, allora si aggiorna

    }

    /**
     * Cerca una traccia nel sistema di persistenza tramite il suo ID univoco.
     *
     * @param trackId L'ID (UUID) della traccia da cercare.
     * @return Un {@link Optional} contenente la traccia se trovata, altrimenti un Optional vuoto.
     */
    public Optional<Track> searchTrackById(UUID trackId) {
        return trackDAO.searchById(trackId);
    }

    /**
     * Crea, valida e aggiunge una nuova traccia musicale sia nella persistenza che nello stato condiviso.
     * Verifica preventivamente che non esista già una traccia con lo stesso titolo e autore.
     *
     * @param title      Il titolo della traccia.
     * @param author     L'autore o l'artista.
     * @param genre      Il genere musicale.
     * @param songPath   Il percorso del file audio.
     * @param songLength La durata in secondi.
     * @param year       L'anno di pubblicazione.
     * @param tags       L'insieme dei tag da associare.
     * @return Un {@link Optional} vuoto se l'aggiunta ha successo; un Optional contenente
     * il messaggio d'errore se la traccia è duplicata o se fallisce la validazione dei dati.
     */
    public Optional<String> addTrack(String title, String author, Genre genre, String songPath, int songLength, String year,
                                     Set<Tag> tags) {
        try {

            EnumSet<Tag> safeTags =
                    (tags == null || tags.isEmpty())
                            ? EnumSet.noneOf(Tag.class)
                            : EnumSet.copyOf(tags);

            Track track = new Track(
                    title,
                    author,
                    genre,
                    songPath,
                    songLength,
                    year,
                    safeTags
            );

            if (trackDAO.isDuplicated(track)) {
                return Optional.of("Error: A track with this title and author already exists!");
            }

            trackDAO.insert(track);
            sharedState.getALlTracks().add(track);

            return Optional.empty();

        } catch (TrackInfoException e) {
            return Optional.of(e.getMessage());
        }
    }

    /**
     * Aggiorna le informazioni di una traccia esistente identificata dal suo ID.
     * Convalida i nuovi valori tramite un'istanza temporanea e verifica che non si creino duplicati.
     *
     * @param trackId   L'identificativo univoco della traccia da aggiornare.
     * @param newTitle  Il nuovo titolo.
     * @param newAuthor Il nuovo autore.
     * @param newGenre  Il nuovo genere musicale.
     * @param newYear   Il nuovo anno di pubblicazione.
     * @param newTags   Il nuovo set di tag da associare.
     * @return Un {@link Optional} vuoto in caso di successo; un Optional contenente
     * il messaggio d'errore se la traccia non è trovata, se è duplicata o se fallisce la validazione.
     */
    public Optional<String> updateTrack(UUID trackId, String newTitle, String newAuthor, Genre newGenre, String newYear, Set<Tag> newTags) {
        //come parametri del metodo, tutte le info che possono cambiare di una traccia + Id perché ci serve per cercare nel DAO
        Optional<Track> optionalTrack = trackDAO.searchById(trackId);

        if (optionalTrack.isEmpty()) {
            return Optional.of("Error: Track not found.");
        }

        Track track = optionalTrack.get();  //se non è vuoto, non è più Optional

        try {

            //creo una traccia temporanea
            //così avviene anche validazione dei nuovi campi
            Track tempTrack = new Track(newTitle, newAuthor, newGenre, track.getSongPath(), track.getSongLength(), newYear,
                    newTags != null ? EnumSet.copyOf(newTags) : EnumSet.noneOf(Tag.class));

            //verifica duplicato: id diversi, stesso titolo e stesso autore
            boolean isDuplicate = sharedState.getALlTracks().stream()
                    .anyMatch(t -> !t.getId().equals(trackId) && t.isDuplicate(tempTrack));

            if (isDuplicate)
                return Optional.of("Error: A track with this title and author already exists!");

            //aggiornamento campi dell'oggetto track

            track.setTitle(newTitle);
            track.setAuthor(newAuthor);
            track.setGenre(newGenre);
            track.setYear(newYear);
            track.setTags(newTags);

            //update della traccia con DAO
            trackDAO.update(track);

            //avviso interfaccia grafica del cambiamento
            updateInState(track);

            return Optional.empty();    //vuol dire che non c'è nessun errore

        } catch (TrackInfoException e) {    //finisco qui se non va a buon fine la validazione
            return Optional.of(e.getMessage());
        }

    }

    /**
     * Cerca le tracce i cui titoli o autori contengono la parola chiave specificata (case-insensitive).
     * Se la parola chiave è nulla o vuota, restituisce una copia dell'intera lista di tracce.
     *
     * @param keyword La parola chiave da cercare.
     * @return La lista delle tracce che corrispondono ai criteri di ricerca.
     */
    public List<Track> searchTracks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>(sharedState.getALlTracks());
        }
        String lowerKeyword = keyword.toLowerCase();
        return sharedState.getALlTracks().stream()
                .filter(t -> t.getTitle().toLowerCase().contains(lowerKeyword) ||
                        t.getAuthor().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    /**
     * Registra un osservatore per ricevere gli eventi relativi al ciclo di vita delle tracce.
     *
     * @param observer L'osservatore {@link TrackObserver} da registrare.
     */
    public void addObserver(TrackObserver observer) {

        this.observers.add(observer);

    }

    /**
     * Elimina in modo permanente una traccia dal sistema tramite il suo ID.
     * Notifica preventivamente tutti gli osservatori registrati e rimuove la traccia dal DAO
     * e dallo stato condiviso.
     *
     * @param trackId L'ID della traccia da eliminare.
     */
    public void deleteTrack(UUID trackId) {

        for (TrackObserver observer : observers) {
            observer.onTrackDeleted(trackId);
        }

        trackDAO.delete(trackId);

        sharedState.getALlTracks().removeIf(t -> t.getId().equals(trackId));
    }

    /**
     * Ripristina una traccia musicale precedentemente rimossa (utilizzato per la funzionalità di Undo).
     * Reinserisce l'oggetto sia nella persistenza che nello stato condiviso.
     *
     * @param track La traccia da ripristinare.
     */
    public void restoreTrack(Track track) {
        if (track == null) return;
        trackDAO.insert(track);
        sharedState.getALlTracks().add(track);
    }

    /**
     * Incrementa il numero di ascolti di una determinata traccia, aggiornandola nella persistenza
     * e nello stato condiviso per aggiornare la GUI.
     *
     * @param trackId L'identificatore univoco (UUID) della traccia da incrementare.
     * @return Un {@link Optional} vuoto se l'operazione ha successo, o contenente un messaggio d'errore
     * se la traccia non viene trovata.
     */
    public Optional<String> incrementPlayCount(UUID trackId) {

        Optional<Track> optionalTrack = trackDAO.searchById(trackId);

        if (optionalTrack.isEmpty()) {
            return Optional.of("Error: Track not found.");
        }

        Track track = optionalTrack.get();

        track.incrementPlayCount();

        trackDAO.update(track);

        updateInState(track);

        return Optional.empty();
    }

}
