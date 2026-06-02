package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;

public class PersistenceService {
    //tutti i service hanno questa struttura iniziale
    private final DAO<Track> trackDAO;
    private final DAO<Playlist> playlistDAO;

    private final SharedState sharedState;

    /**
     * Costruttore della classe PersistenceService. Riceve i DAO e lo stato condiviso.
     * @param trackDAO
     * @param playlistDAO
     * @param sharedState
     */
    public PersistenceService(DAO<Track> trackDAO, DAO<Playlist> playlistDAO, SharedState sharedState) {
        this.trackDAO = trackDAO;
        this.playlistDAO = playlistDAO;
        this.sharedState = sharedState;
    }

    /**
     * Metodo che carica tutti i dati dal file JSON. Sia le tracce che le playlist.
      */
    public void load(){
        sharedState.getALlTracks().setAll(trackDAO.selectAll()); // aggiunge tutte le tracce al set di tutte le tracce
        sharedState.getALlPlaylists().setAll(playlistDAO.selectAll()); // aggiunge tutte le playlist al set di tutte le playlist
    }


}
