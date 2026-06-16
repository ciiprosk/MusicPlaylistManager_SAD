package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.exception.PlaylistInfoException;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.collections.ObservableList;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.specification.Specification;


import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;


/**
 * La classe PlaylistService gestisce le operazioni CRUD sulle playlist. In particolare si occupa di:
 * - Leggere e scrivere playlist dal file JSON;
 * - Gestire le modifiche nello stato globale;
 * - Effettuare operazioni di aggiornamento nel file JSON.
 * - Effettuare operazioni di aggiornamento nello stato globale (sharedState).
 */
// nei service ogni operazione che modifica i dati deve
//1. leggere o modificare l'oggetto da state o dao
// 2. fare operazioni con dao
//3. aggiornare lo stato globale
public class PlaylistService implements TrackObserver{
    // serve a gestire le operazioni crud sulle playlist
    private final DAO<Playlist> playlistDAO;

    private final SharedState sharedState;



    /**
     * Costruttore della classe PlaylistService.
     * @param playlistDAO: è l'oggetto DAO che gestisce le operazioni CRUD sulle playlist.
     * @param sharedState: è l'oggetto SharedState che gestisce lo stato globale delle applicazioni.
     */
    public PlaylistService(DAO<Playlist> playlistDAO, SharedState sharedState) {
        this.playlistDAO = playlistDAO;
        this.sharedState = sharedState;

    }

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
     * Metodo che restituisce tutte le playlist presenti nel file JSON. Usa una lista observable per gestire le modifiche.
     * @return una lista di playlist.
     */
    public ObservableList<Playlist> getPlaylists() {
        return sharedState.getALlPlaylists();
    }

    /**
     * Metodo che crea una playlist:
     * 1. eriifca i dati ed eventuali doppioni
     * 2. inseirisce peersistenza
     * 3. cambia lo stato di shared state
     * @param name
     * @return
     */
    public Optional<String> createPlaylist(String name) {
            Playlist playlist = null;
            try {
                playlist = new Playlist(name);

                if(playlistDAO.isDuplicated(playlist))
                    return Optional.of("Playlist name already exists");

                // ora posso inserrie la playlist nel file dao
                playlistDAO.insert(playlist);
                sharedState.getALlPlaylists().add(playlist);


            }catch(PlaylistInfoException e){
                return Optional.of(e.getMessage()); // in questo modo posso gestire i vari tipi di messaggi di errore nel controller
            }
        return Optional.empty();

    }

    /**
     * Metodo che elimina una playlist dal file JSON.
     * @param playlistID è l'identificatore univoco della playlist da cancellare.
     */
    public void deletePlaylist(UUID playlistID) {
        playlistDAO.delete(playlistID);
        sharedState.getALlPlaylists().removeIf(p -> p.getId().equals(playlistID));
    }

    /**
     * Metodo che ritorna un Optional<String> contenente un messaggio di errore se la playlist non esiste, altrimenti ritorna Optional.empty().
     * 1. Cerca la playlist nel file JSON utilizzando l'identificatore univoco.
     * 2. Se la playlist non esiste, restituisce Optional.of("Playlist not found").
     * 3. Se la playlist esiste, restituisce Optional.empty() dopo aver aggiornato il nome della playlist nel file JSON.
     * @param playlistID è l'identificatore univoco della playlist da aggiornare.
     * @param newName è il nuovo nome della playlist.
     * @return un Optional<String> contenente un messaggio di errore se la playlist non esiste, altrimenti ritorna Optional.empty().
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
           if(playlistDAO.isDuplicated(newPlaylist))
               return Optional.of("Playlist name already exists");

           playlist.setName(newName);
           playlistDAO.update(playlist);
           updateInState(playlist);

        }catch (PlaylistInfoException e){
           return Optional.of(e.getMessage());
       }

        return Optional.empty();
    }

    /**
     * Metodo che aggiunge una traccia a una playlist.
     * @param playlistID è l'identificatore univoco della playlist a cui aggiungere la traccia.
     * @param trackID è l'identificatore univoco della traccia da aggiungere.
     */
    public void addTrackToPlaylist(UUID playlistID, UUID trackID) {
        Playlist playlist = sharedState.getALlPlaylists().stream()
                .filter(p -> p.getId().equals(playlistID))
                .findFirst()
                .orElseThrow(()-> new PlaylistInfoException("Playlist not found"));


        if(!playlist.containsTrack(trackID)){
            //se la playlist non contiene la trccia allora la devo inserire con update del dao
            Track track = searchTrackById(trackID);
            playlist.addTrack(track);
            playlistDAO.update(playlist);
            updateInState(playlist);
        }
    }

    /**
     * Metodo che rimuove una traccia da una playlist.
     * @param playlistID è l'identificatore univoco della playlist da cui rimuovere la traccia.
     * @param trackID è l'identificatore univoco della traccia da rimuovere.
     */

    public void removeTrackFromPlaylist(UUID playlistID, UUID trackID) {
        Playlist playlist = sharedState.getALlPlaylists().stream()
                .filter(p -> p.getId().equals(playlistID))
                .findFirst()
                .orElseThrow(()-> new PlaylistInfoException("Playlist not found"));

        if(playlist.containsTrack(trackID)){ //se la playlist contie la traccia la eliminizmao
            Track track = searchTrackById(trackID);
            playlist.removeTrack(track);
            playlistDAO.update(playlist);
            updateInState(playlist);

        }
    }

    /**
     * Sposta una traccia da una posizione a un'altra all'interno di una playlist.
     * Il nuovo ordine viene salvato nel file JSON e aggiornato nello SharedState.
     *
     * @param playlistID identificatore della playlist da modificare
     * @param fromIndex posizione iniziale della traccia
     * @param toIndex nuova posizione della traccia
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
     * Cerca le playlist il cui nome contiene la parola chiave specificata.
     * La ricerca non è sensibile alle lettere maiuscole o minuscole (case-insensitive).
     * Se la parola chiave è nulla, vuota o composta solo da spazi, viene restituita
     * una copia modificabile di tutte le playlist disponibili.
     *
     * @param keyword la stringa da cercare nel nome della playlist
     * @return una {@link List} contenente le playlist che corrispondono ai criteri di ricerca
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
     * Metodo che aggiorna l'elenco di playlist nello stato globale.
     * @param playlist è la playlist da aggiornare nello stato globale.
     */
    private void updateInState(Playlist playlist) {
        ObservableList<Playlist> playlists = sharedState.getALlPlaylists();

        //ho ricevuuto tute le playlists ma le devo modificare con i nuovi aggiornameni rispetto alla playlist ricevuto come paramet
        IntStream.range(0, playlists.size())
                .filter(index -> playlists.get(index).getId().equals(playlist.getId()))
                .findFirst().ifPresent(index -> playlists.set(index, playlist));

    }

    /**
     * Recupera una playlist specifica dal sistema tramite il suo identificatore univoco.
     *
     * @param playlistID l'identificatore UUID della playlist da cercare.
     * @return un Optional contenente la playlist se presente nel sistema,
     *         oppure Optional.empty() se non esiste alcuna playlist con l'ID specificato.
     */

    public Optional<Playlist> getPlaylistById(UUID playlistID) {
        return sharedState.getALlPlaylists().stream()
                .filter(p -> p.getId().equals(playlistID))
                .findFirst();
    }

    /**
     * Restituisce l'elenco delle tracce contenute in una specifica playlist.
     *
     * @param playlistID l'identificatore UUID della playlist.
     * @return una lista contenente gli identificatori delle tracce presenti nella playlist.
     * @throws PlaylistInfoException se la playlist richiesta non esiste nel sistema.
     */

    public List<Track> getTracksFromPlaylist(UUID playlistID) {
        Playlist playlist = sharedState.getALlPlaylists().stream()
                .filter(p -> p.getId().equals(playlistID))
                .findFirst()
                .orElseThrow(() -> new PlaylistInfoException("Playlist not found"));
        return playlist.getTracksList();
    }

    /**
     * Crea un oggetto Playlist filtrando una collezione di tracce in base a una specifica.

     * Metodo puro che non applica persistenza: istanzia la nuova playlist e vi aggiunge
     * tutte le tracce che soddisfano il criterio {@link Specification#isSatisfiedBy(Object)}.
     *
     * @param name     il nome da assegnare alla playlist
     * @param tracks   la collezione di tracce da filtrare
     * @param criteria la specifica contenente i criteri di filtraggio
     * @return la {@link Playlist} generata contenente solo le tracce filtrate
     */
    public Playlist generate(String name, Collection<Track> tracks, Specification<Track> criteria) {
        Playlist playlist = new Playlist(name);
        tracks.stream()
                .filter(criteria::isSatisfiedBy)
                .forEach(playlist::addTrack);
        return playlist;
    }

    /**
     * Genera una nuova playlist filtrando le tracce fornite tramite la specifica e la salva.
     *
     * Verifica preventivamente che non esista già una playlist con lo stesso nome. In caso
     * positivo, persiste la playlist su file tramite DAO e aggiorna lo stato in memoria.
     *
     * @param name     il nome da assegnare alla playlist
     * @param tracks   la collezione di tutte le tracce disponibili da filtrare
     * @param criteria la specifica contenente i criteri di filtraggio logici
     * @return un Optional vuoto se l'operazione ha successo, oppure un Optional
     * contenente il messaggio di errore se il nome della playlist è duplicato
     */
    public Optional<String> generateAndSave(String name, Collection<Track> tracks, Specification<Track> criteria) {

        // 1. Controllo preventivo sul nome (ottimizzazione)
        if (playlistDAO.isDuplicated(new Playlist(name))) {
            return Optional.of("Error: A playlist with this name already exists!");
        }

        // 2. Delega della logica algoritmica al metodo puro
        Playlist playlist = generate(name, tracks, criteria);

        // 3. Persistenza ed effetti collaterali
        playlistDAO.insert(playlist);
        sharedState.getALlPlaylists().add(playlist);

        return Optional.empty();
    }



    /**
     * Cerca una traccia nello stato condiviso tramite il suo identificativo univoco.
     *
     * @param trackId l'UUID della traccia da cercare
     * @return la Track trovata
     * @throws PlaylistInfoException se nessuna traccia corrisponde all'ID fornito
     */
    private Track searchTrackById(UUID trackId) {
        return sharedState.getALlTracks().stream().filter(t -> t.getId().equals(trackId)).findFirst().orElseThrow(()-> new PlaylistInfoException("Track not found"));
    }

    /**
     * Incrementa il contatore delle riproduzioni di una playlist e aggiorna i dati.
     *
     * Cerca la playlist per ID: se esiste, incrementa il suo playCount,
     * persiste la modifica nel database tramite DAO e aggiorna lo stato in memoria.
     *
     * @param playlistId l'UUID della playlist da aggiornare
     * @return un Optional con il messaggio di errore se la playlist non esiste,
     * oppure Optional#empty() se l'operazione va a buon fine
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
     * Restituisce le prime 5 playlist con il maggior numero di riproduzioni.
     * Recupera tutte le playlist dallo stato condiviso, le ordina in ordine
     * decrescente in base al contatore delle riproduzioni e ne seleziona al massimo 5.
     *
     * @return una List contenente le 5 playlist più ascoltate, ordinate dal numero
     * maggiore al minore di riproduzioni
     */
    public List<Playlist> getTop5MostPlayedPlaylists() {
        return sharedState.getALlPlaylists()
                .stream()
                .sorted((p1, p2) -> Integer.compare(p2.getPlayCount(), p1.getPlayCount()))
                .limit(5)
                .toList();
    }

    /**
     * Come createPlaylist, ma restituisce la playlist creata (per il pattern Command).
     * @return la Playlist creata, oppure null se il nome è duplicato o invalido.
     */
    public Playlist createPlaylistReturning(String name) {
        try {
            Playlist playlist = new Playlist(name);
            if (playlistDAO.isDuplicated(playlist)) {
                return null;
            }
            playlistDAO.insert(playlist);
            sharedState.getALlPlaylists().add(playlist);
            return playlist;
        } catch (PlaylistInfoException e) {
            return null;
        }
    }
    /**
     * Reinserisce una playlist già costruita (con il suo id e le sue tracce),
     * usato per annullare una cancellazione.
     */
    public void restorePlaylist(Playlist playlist) {
        if (playlist == null) return;
        playlistDAO.insert(playlist);
        sharedState.getALlPlaylists().add(playlist);
    }

    /**
     * Come generateAndSave, ma restituisce la playlist generata (per il pattern Command).
     * @param name nome della nuova playlist
     * @param tracks tracce da inserire nella playlist
     * @param criteria criterio di filtraggio
     * @return
     */
    public Playlist generateAndSaveReturning(String name, Collection<Track> tracks, Specification<Track> criteria) {
        if (playlistDAO.isDuplicated(new Playlist(name))) {
            return null;
        }
        Playlist playlist = generate(name, tracks, criteria);
        playlistDAO.insert(playlist);
        sharedState.getALlPlaylists().add(playlist);
        return playlist;
    }

    /**
     * Aggiorna i brani di una playlist esistente nel DAO e nello stato condiviso.
     */
    public void updatePlaylistTracks(UUID playlistId, List<Track> newTracks) {
        Playlist playlist = getPlaylistById(playlistId)
                .orElseThrow(() -> new PlaylistInfoException("Playlist not found"));

        playlist.replaceTracks(newTracks);
        playlistDAO.update(playlist);
        updateInState(playlist);
    }
}
