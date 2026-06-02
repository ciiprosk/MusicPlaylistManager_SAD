package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.exception.PlaylistInfoException;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.collections.ObservableList;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;


// nei service ogni operazione che modifica i dati deve
//1. leggere o modificare l'oggetto da state o dao
// 2. fare operazioni con dao
//3. aggiornare lo stato globale
public class PlaylistService {
    // serve a gestire le operazioni crud sulle playlist
    private final DAO<Playlist> playlistDAO;

    private final SharedState sharedState;

    public PlaylistService(DAO<Playlist> playlistDAO, SharedState sharedState) {
        this.playlistDAO = playlistDAO;
        this.sharedState = sharedState;
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
        try{
            Playlist playlist = new Playlist(name);

            if(playlistDAO.isDuplicated(playlist))
                return Optional.of("Playlist name already exists");

            // ora posso inserrie la playlist nel file dao
            playlistDAO.insert(playlist);
            sharedState.getALlPlaylists().add(playlist);
            return Optional.empty();

        } catch (PlaylistInfoException e){
            return Optional.of("Playlist name already exists");
        }
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
       try {
           newPlaylist = new Playlist(newName);
        }catch (PlaylistInfoException e){
           return Optional.of("Playlist name already exists");
       }
       //controllo che il nuovo nome sia univoco
        if(playlistDAO.isDuplicated(newPlaylist))
            return Optional.of("Playlist name already exists");

        playlist.setName(newName);
        playlistDAO.update(playlist);
        updateInState(playlist);
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
        //ho ricevuuto tute le playlists ma le devo modificare con i nuovi aggiornameni rispetto alla playlist ricevuto come parametr
        //1. devo cercare playlist nella lista
        IntStream.range(0, playlists.size())
                .filter(index -> playlists.get(index).equals(playlist.getId()))
                .findFirst().ifPresent(index -> playlists.set(index, playlist));

    }
}
