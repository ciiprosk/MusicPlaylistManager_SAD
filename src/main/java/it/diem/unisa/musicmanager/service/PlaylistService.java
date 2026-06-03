package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.exception.PlaylistInfoException;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.collections.ObservableList;

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
        // la playlist è un observer di tracce: gestisce l'eliminazione delle tracce eliminate nella playlist
        for (Playlist playlist : sharedState.getALlPlaylists()) {
            if (playlist.containsTrack(trackId)) {
                playlist.removeTrack(trackId);
                playlistDAO.update(playlist);
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
        Optional<Playlist> optionalPlaylist = playlistDAO.searchById(playlistID);
        if (optionalPlaylist.isEmpty())
            return Optional.of("Playlist not found");

        Playlist newPlaylist = null;
        // se siamo qui la playlist è stata trovata e posso eseguire l'update
       Playlist playlist = optionalPlaylist.get(); //mi dewrappo l aplaylist ritornata dal dao

        if (playlist.getName().equals(newName)) {   //se ho salvato la playlist con lo stesso nome, ho finito
            return Optional.empty();
        }

       try {
           newPlaylist = new Playlist(playlistID, newName); //in questo modo faccio verifiche su business rules

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
        Playlist playlist = playlistDAO.searchById(playlistID).orElseThrow(()-> new PlaylistInfoException("Playlist not found"));

        if(!playlist.containsTrack(trackID)){
            //se la playlist non contiene la trccia allora la devo inserire con update del dao
            playlist.addTrack(trackID);
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
        Playlist playlist = playlistDAO.searchById(playlistID).orElseThrow(()-> new PlaylistInfoException("Playlist not found"));

        if(playlist.containsTrack(trackID)){ //se la playlist contie la traccia la eliminizmao
            playlist.removeTrack(trackID);
            playlistDAO.update(playlist);
            updateInState(playlist);

        }
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
        return playlistDAO.searchById(playlistID);
    }

    /**
     * Restituisce l'elenco delle tracce contenute in una specifica playlist.
     *
     * @param playlistID l'identificatore UUID della playlist.
     * @return una lista contenente gli identificatori delle tracce presenti nella playlist.
     * @throws PlaylistInfoException se la playlist richiesta non esiste nel sistema.
     */

    public List<UUID> getTracksFromPlaylist(UUID playlistID) {
        Playlist playlist = playlistDAO.searchById(playlistID)
                .orElseThrow(() -> new PlaylistInfoException("Playlist not found"));

        return playlist.getTracks();
    }
}
