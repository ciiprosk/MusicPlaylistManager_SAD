package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.exception.TrackInfoException;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.collections.ObservableList;
import java.util.Optional;
import java.util.UUID;
// bisgna imolemneare a logica crud ei brano e l'agiornmaento di sharedstate
public class TrackService {

    private final DAO<Track> trackDAO;  //serve per operazioni CRUD sulle tracce
    private final SharedState sharedState;  //serve per lo stato condiviso delle tracce

    public TrackService(DAO<Track> trackDAO, SharedState sharedState) {
        this.trackDAO = trackDAO;
        this.sharedState = sharedState;
    }

    public ObservableList<Track> getAllTracks() {
        return sharedState.getALlTracks();  //compito delegato dallo stato condiviso, caricato dal Persistance Service
    }


    public Optional<Track> getTrackById(UUID id){ return null;}

    public Optional<String> addTrack(String title, String author, Genre genre, String songPath, int songLength, String year){
        try{

            Track track = new Track(title, author, genre, songPath, songLength, year);

            if(trackDAO.isDuplicated(track))
                return Optional.of("Error: A track with this title and author already exists!");

            // inserisco traccia nel Json
            trackDAO.insert(track);
            sharedState.getALlTracks().add(track);
            return Optional.empty();

        } catch (TrackInfoException e){     //Se sono qui, ho avuto errore validazione dati track
            return Optional.of("Error: Invalid Track Informations!");
        }
    }

    public Optional<String> updateTrack(Track track){ return null;}
    public Optional<String> deleteTrack(Track track){ return null;}

    private void updateInitState(Track track){}

    public Optional<String> verify(Track track){ return null;}

}
