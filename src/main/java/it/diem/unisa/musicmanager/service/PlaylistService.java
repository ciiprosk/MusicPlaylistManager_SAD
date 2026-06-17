package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.exception.PlaylistInfoException;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.specification.Specification;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Servizio per la gestione del ciclo di vita e delle operazioni CRUD (Create, Read, Update, Delete)
 * relative alle playlist ({@link Playlist}).
 * Gestisce l'interazione con il database tramite {@link DAO}, l'aggiornamento dello stato globale
 * condiviso ({@link SharedState}), la sincronizzazione degli elementi in coda e la generazione automatica
 * di playlist filtrate tramite specifiche. 
 * Implementa l'interfaccia {@link TrackObserver} per mantenere la coerenza dei dati qualora una traccia
 * venga eliminata definitivamente dall'applicazione.
 */
public class PlaylistService implements TrackObserver {

    /**
     * Componente DAO per la persistenza su file JSON delle playlist.
     */
    private final DAO<Playlist> playlistDAO;

    /**
     * Lo stato condiviso globale dell'applicazione.
     */
    private final SharedState sharedState;

    /**
     * Servizio per la gestione della coda, utilizzato per notificare i cambiamenti (aggiunta/rimozione brani)
     * e mantenere allineata la coda di riproduzione.
     */
    private QueueService queueService;

    /**
     * Costruttore della classe {@code PlaylistService}.
     * 
     * @param playlistDAO L'oggetto DAO per la gestione della persistenza delle playlist.
     * @param sharedState Lo stato condiviso globale dell'applicazione.
     */
    public PlaylistService(DAO<Playlist> playlistDAO, SharedState sharedState) {
        this.playlistDAO = playlistDAO;
        this.sharedState = sharedState;
    }

    /**
     * Imposta il servizio di gestione della coda.
     * 
     * @param queueService Il servizio {@link QueueService} da iniettare.
     */
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Restituisce la lista osservabile di tutte le playlist caricate nel sistema.
     * 
     * @return L'{@link ObservableList} di {@link Playlist} presente nello stato condiviso.
     */
    public ObservableList<Playlist> getPlaylists() {
        return sharedState.getALlPlaylists();
    }

    /**
     * Cerca una playlist specifica nello stato condiviso tramite il suo identificativo univoco.
     * 
     * @param playlistID L'ID (UUID) della playlist da cercare.
     * @return Un {@link Optional} contenente la playlist se trovata, altrimenti un Optional vuoto.
     */
    public Optional<Playlist> getPlaylistById(UUID playlistID) {
        return sharedState.getALlPlaylists().stream()
                .filter(p -> p.getId().equals(playlistID))
                .findFirst();
    }

    /**
     * Restituisce l'elenco delle tracce contenute in una specifica playlist.
     * 
     * @param playlistID L'ID (UUID) della playlist di cui recuperare le tracce.
     * @return Una lista di oggetti {@link Track} contenuti nella playlist.
     * @throws PlaylistInfoException Se la playlist con l'ID fornito non viene trovata nel sistema.
     */
    public List<Track> getTracksFromPlaylist(UUID playlistID) {
        Playlist playlist = sharedState.getALlPlaylists().stream()
                .filter(p -> p.getId().equals(playlistID))
                .findFirst()
                .orElseThrow(() -> new PlaylistInfoException("Playlist not found"));
        return playlist.getTracksList();
    }

    /**
     * Restituisce le 5 playlist più ascoltate memorizzate nello stato condiviso,
     * ordinate in modo decrescente in base al numero di riproduzioni.
     * Se le playlist totali sono meno di 5, vengono restituite tutte quelle disponibili.
     * 
     * @return Una lista di massimo 5 oggetti {@link Playlist} più ascoltati.
     */
    public List<Playlist> getTop5MostPlayedPlaylists() {
        return sharedState.getALlPlaylists()
                .stream()
                .sorted((p1, p2) -> Integer.compare(p2.getPlayCount(), p1.getPlayCount()))
                .limit(5)
                .toList();
    }

    /**
     * Sostituisce una playlist all'interno della lista osservabile dello stato condiviso
     * al fine di notificare l'interfaccia grafica e aggiornare la visualizzazione.
     * 
     * @param playlist La playlist aggiornata.
     */
    private void updateInState(Playlist playlist) {
        ObservableList<Playlist> playlists = sharedState.getALlPlaylists();

        IntStream.range(0, playlists.size())
                .filter(index -> playlists.get(index).getId().equals(playlist.getId()))
                .findFirst().ifPresent(index -> playlists.set(index, playlist));

    }

    /**
     * Cerca una traccia nello stato condiviso tramite il suo identificativo univoco.
     * 
     * @param trackId L'ID (UUID) della traccia da cercare.
     * @return La {@link Track} corrispondente.
     * @throws PlaylistInfoException Se la traccia con l'ID specificato non esiste nel sistema.
     */
    private Track searchTrackById(UUID trackId) {
        return sharedState.getALlTracks().stream().filter(t -> t.getId().equals(trackId)).findFirst().orElseThrow(() -> new PlaylistInfoException("Track not found"));
    }

    /**
     * Rimuove una traccia da tutte le playlist caricate in caso di eliminazione definitiva.
     * Metodo di callback attivato dal pattern Observer quando una traccia viene eliminata da `TrackService`.
     * 
     * @param trackId L'ID (UUID) della traccia che è stata eliminata.
     */
    @Override
    public void onTrackDeleted(UUID trackId) {

        if (trackId == null) {
            return;
        }

        for (Playlist playlist : sharedState.getALlPlaylists()) {
            if (playlist.containsTrack(trackId)) {
                playlist.removeTrack(trackId);
                playlistDAO.update(playlist);
                updateInState(playlist);
            }
        }
    }

    /**
     * Crea e memorizza una nuova playlist vuota.
     * Verifica preventivamente che non esista già una playlist con lo stesso nome.
     * 
     * @param name Il nome da assegnare alla nuova playlist.
     * @return Un {@link Optional} vuoto in caso di successo, oppure contenente il messaggio
     *         di errore se il nome non è valido o se la playlist risulta già esistente.
     */
    public Optional<String> createPlaylist(String name) {
        Playlist playlist = null;
        try {
            playlist = new Playlist(name);

            if (playlistDAO.isDuplicated(playlist))
                return Optional.of("Playlist name already exists");

            // ora posso inserire la playlist nel file dao
            playlistDAO.insert(playlist);
            sharedState.getALlPlaylists().add(playlist);

        } catch (PlaylistInfoException e) {
            return Optional.of(e.getMessage()); // in questo modo posso gestire i vari tipi di messaggi di errore nel controller
        }
        return Optional.empty();

    }

    /**
     * Rimuove in modo permanente una playlist dal sistema sia dal file JSON che dallo stato condiviso.
     * 
     * @param playlistID L'ID della playlist da eliminare.
     */
    public void deletePlaylist(UUID playlistID) {
        playlistDAO.delete(playlistID);
        sharedState.getALlPlaylists().removeIf(p -> p.getId().equals(playlistID));
    }

    /**
     * Rinomina una playlist esistente effettuando le opportune validazioni di business
     * e controllando l'unicità del nome sul DAO.
     * 
     * @param playlistID L'ID della playlist da rinominare.
     * @param newName    Il nuovo nome da assegnare alla playlist.
     * @return Un {@link Optional} vuoto in caso di successo, o un Optional contenente il messaggio
     *         di errore se la playlist non viene trovata, se il nome è già presente o se non supera la validazione.
     */
    public Optional<String> renamePlaylist(UUID playlistID, String newName) {
        Playlist playlist = sharedState.getALlPlaylists().stream()
                .filter(p -> p.getId().equals(playlistID))
                .findFirst().orElse(null);

        if (playlist == null)
            return Optional.of("Playlist not found");

        if (playlist.getName().equals(newName)) {   //se ho salvato la playlist con lo stesso nome, ho finito
            return Optional.empty();
        }

        try {
            Playlist newPlaylist = new Playlist(playlistID, newName); //in questo modo faccio verifiche su business rules

            //controllo che il nuovo nome sia univoco
            if (playlistDAO.isDuplicated(newPlaylist))
                return Optional.of("Playlist name already exists");

            playlist.setName(newName);
            playlistDAO.update(playlist);
            updateInState(playlist);

        } catch (PlaylistInfoException e) {
            return Optional.of(e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Aggiunge una traccia a una playlist e sincronizza la modifica sia sul file persistito
     * che nello stato condiviso. Se necessario, notifica anche la coda di riproduzione.
     * 
     * @param playlistID L'identificatore della playlist a cui aggiungere la traccia.
     * @param trackID    L'identificatore della traccia da aggiungere.
     * @throws PlaylistInfoException Se la playlist o la traccia non vengono trovate.
     */
    public void addTrackToPlaylist(UUID playlistID, UUID trackID) {
        Playlist playlist = sharedState.getALlPlaylists().stream()
                .filter(p -> p.getId().equals(playlistID))
                .findFirst()
                .orElseThrow(() -> new PlaylistInfoException("Playlist not found"));


        if (!playlist.containsTrack(trackID)) {
            //se la playlist non contiene la traccia allora la devo inserire con update del dao
            Track track = searchTrackById(trackID);
            playlist.addTrack(track);
            playlistDAO.update(playlist);
            updateInState(playlist);

            if (queueService != null) {
                queueService.synchronizeTrackAdded(playlistID, track);
            }
        }
    }

    /**
     * Inserisce una traccia all'interno di una playlist in una posizione specifica.
     * 
     * @param playlistId L'identificatore della playlist.
     * @param track      La traccia da aggiungere.
     * @param position   La posizione (indice) in cui inserire il brano.
     */
    public void addTrackToPlaylistAtPosition(UUID playlistId, Track track, int position) {
        Playlist playlist = sharedState.getALlPlaylists().stream()
                .filter(p -> p.getId().equals(playlistId))
                .findFirst()
                .orElse(null);
        if (playlist == null || playlist.containsTrack(track.getId())) return;
        playlist.addTrackAtPosition(track, position);
        playlistDAO.update(playlist);
        updateInState(playlist);
    }

    /**
     * Rimuove una traccia da una playlist specifica, aggiornando la persistenza, lo stato condiviso
     * ed allineando la coda di riproduzione qualora la playlist fosse in coda.
     * 
     * @param playlistID L'identificatore della playlist da cui rimuovere la traccia.
     * @param trackID    L'identificatore della traccia da rimuovere.
     * @throws PlaylistInfoException Se la playlist o la traccia non vengono trovate.
     */
    public void removeTrackFromPlaylist(UUID playlistID, UUID trackID) {
        Playlist playlist = sharedState.getALlPlaylists().stream()
                .filter(p -> p.getId().equals(playlistID))
                .findFirst()
                .orElseThrow(() -> new PlaylistInfoException("Playlist not found"));

        if (playlist.containsTrack(trackID)) { //se la playlist contiene la traccia la eliminiamo
            Track track = searchTrackById(trackID);
            playlist.removeTrack(track);
            playlistDAO.update(playlist);
            updateInState(playlist);

            if (queueService != null) {
                queueService.synchronizeTrackRemoved(playlistID, trackID);
            }
        }
    }

    /**
     * Sposta una traccia da un indice a un altro all'interno di una playlist.
     * Il nuovo ordine delle tracce viene salvato nel file JSON e aggiornato nello SharedState.
     * 
     * @param playlistID L'identificatore della playlist da modificare.
     * @param fromIndex  L'indice originale della traccia da spostare.
     * @param toIndex    L'indice di destinazione in cui posizionare la traccia.
     * @throws PlaylistInfoException Se la playlist richiesta non esiste.
     */
    public void moveTrackInPlaylist(
            UUID playlistID,
            int fromIndex,
            int toIndex
    ) {
        Playlist playlist = sharedState.getALlPlaylists()
                .stream()
                .filter(p -> p.getId().equals(playlistID))
                .findFirst()
                .orElseThrow(() ->
                        new PlaylistInfoException("Playlist not found")
                );

        int numberOfTracks =
                playlist.getTracksList().size();

        if (fromIndex < 0
                || fromIndex >= numberOfTracks
                || toIndex < 0
                || toIndex >= numberOfTracks
                || fromIndex == toIndex) {
            return;
        }

        playlist.moveTrack(fromIndex, toIndex);

        playlistDAO.update(playlist);
        updateInState(playlist);
    }

    /**
     * Cerca le playlist il cui nome contiene la parola chiave fornita (case-insensitive).
     * Se la parola chiave è nulla o vuota, viene restituita una copia di tutte le playlist.
     * 
     * @param keyword La parola chiave da cercare nel nome delle playlist.
     * @return Una lista di playlist corrispondenti.
     */
    public List<Playlist> searchPlaylists(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new java.util.ArrayList<>(sharedState.getALlPlaylists());
        }
        String lowerKeyword = keyword.toLowerCase();
        return sharedState.getALlPlaylists().stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    /**
     * Genera un'istanza di Playlist a partire da una collezione di tracce,
     * filtrando solo quelle che soddisfano i criteri della specifica specificata.
     * Si tratta di un metodo puro che non persiste le modifiche nel database.
     * 
     * @param name     Il nome da assegnare alla playlist generata.
     * @param tracks   La collezione di tracce da filtrare.
     * @param criteria La specifica ({@link Specification}) contenente i criteri di filtraggio.
     * @return La playlist generata con le sole tracce che soddisfano la specifica.
     */
    public Playlist generate(String name, Collection<Track> tracks, Specification<Track> criteria) {
        Playlist playlist = new Playlist(name);
        tracks.stream()
                .filter(criteria::isSatisfiedBy)
                .forEach(playlist::addTrack);
        return playlist;
    }

    /**
     * Genera una nuova playlist filtrando le tracce in base alla specifica e la salva.
     * Verifica preventivamente che non esista già una playlist con lo stesso nome.
     * 
     * @param name     Il nome da assegnare alla playlist.
     * @param tracks   La collezione di tracce disponibili da filtrare.
     * @param criteria La specifica contenente i criteri di filtraggio logici.
     * @return Un {@link Optional} vuoto se l'operazione ha successo, o contenente un messaggio
     *         di errore se il nome della playlist è già presente o non valido.
     */
    public Optional<String> generateAndSave(String name, Collection<Track> tracks, Specification<Track> criteria) {

        try {

            if (playlistDAO.isDuplicated(new Playlist(name))) {
                return Optional.of("Error: A playlist with this name already exists!");
            }

            Playlist playlist = generate(name, tracks, criteria);
            playlistDAO.insert(playlist);
            sharedState.getALlPlaylists().add(playlist);
            return Optional.empty();

        } catch (PlaylistInfoException e) {
            return Optional.of(e.getMessage());
        }

    }

    /**
     * Incrementa il numero di riproduzioni di una playlist, salvando l'aggiornamento
     * su file ed aggiornando lo stato in memoria.
     * 
     * @param playlistId L'ID della playlist da incrementare.
     * @return Un {@link Optional} vuoto in caso di successo, o un Optional contenente il messaggio
     *         di errore se la playlist non viene trovata.
     */
    public Optional<String> incrementPlayCount(UUID playlistId) {

        Playlist playlist = sharedState.getALlPlaylists()
                .stream()
                .filter(p -> p.getId().equals(playlistId))
                .findFirst()
                .orElse(null);

        if (playlist == null) {
            return Optional.of("Playlist not found");
        }

        playlist.incrementPlayCount();

        playlistDAO.update(playlist);

        updateInState(playlist);

        return Optional.empty();
    }

    /**
     * Crea e persiste una playlist nel sistema, restituendone l'istanza creata.
     * Utilizzato in combinazione con il pattern Command per permettere il tracciamento e l'Undo dell'operazione.
     * 
     * @param name Il nome della nuova playlist.
     * @return La {@link Playlist} creata e persistita.
     * @throws PlaylistInfoException Se esiste già una playlist con questo nome o se le regole di validazione falliscono.
     */
    public Playlist createPlaylistReturning(String name) {
        Playlist playlist = new Playlist(name);
        if (playlistDAO.isDuplicated(playlist)) {
            throw new PlaylistInfoException("A playlist with this name already exists!");
        }
        playlistDAO.insert(playlist);
        sharedState.getALlPlaylists().add(playlist);
        return playlist;
    }

    /**
     * Ripristina nel sistema una playlist pre-esistente con tutte le sue relazioni ed ID intatti.
     * Utilizzato per annullare l'eliminazione di una playlist (Undo).
     * 
     * @param playlist La playlist da ripristinare.
     */
    public void restorePlaylist(Playlist playlist) {
        if (playlist == null) return;
        playlistDAO.insert(playlist);
        sharedState.getALlPlaylists().add(playlist);
    }

    /**
     * Genera e salva una playlist in base a criteri di filtraggio, restituendo l'oggetto generato.
     * Utilizzato in combinazione con il pattern Command per permettere il tracciamento e l'Undo dell'operazione.
     * 
     * @param name     Il nome della nuova playlist.
     * @param tracks   La collezione di tracce da filtrare.
     * @param criteria La specifica contenente i criteri di filtraggio.
     * @return La {@link Playlist} creata e persistita.
     * @throws PlaylistInfoException Se esiste già una playlist con questo nome o se la validazione del nome fallisce.
     */
    public Playlist generateAndSaveReturning(String name, Collection<Track> tracks, Specification<Track> criteria) {
        Playlist testPlaylist = new Playlist(name);
        if (playlistDAO.isDuplicated(testPlaylist)) {
            throw new PlaylistInfoException("A playlist with this name already exists!");
        }
        Playlist playlist = generate(name, tracks, criteria);
        playlistDAO.insert(playlist);
        sharedState.getALlPlaylists().add(playlist);
        return playlist;
    }

    /**
     * Sostituisce l'intera lista di brani di una playlist esistente, aggiornando il DAO e lo stato condiviso.
     * 
     * @param playlistId L'ID della playlist da aggiornare.
     * @param newTracks  La nuova lista di brani da inserire nella playlist.
     * @throws PlaylistInfoException Se la playlist richiesta non esiste.
     */
    public void updatePlaylistTracks(UUID playlistId, List<Track> newTracks) {
        Playlist playlist = getPlaylistById(playlistId)
                .orElseThrow(() -> new PlaylistInfoException("Playlist not found"));

        playlist.replaceTracks(newTracks);
        playlistDAO.update(playlist);
        updateInState(playlist);
    }
}
