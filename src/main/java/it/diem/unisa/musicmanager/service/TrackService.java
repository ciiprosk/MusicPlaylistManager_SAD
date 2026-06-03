package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.collections.ObservableList;
import java.util.Optional;
import java.util.UUID;
// bisgna imolemneare a logica crud ei brano e l'agiornmaento di sharedstate
public class TrackService {

    private final DAO<Track> trackDAO;
    private final SharedState sharedState;

    public TrackService(DAO<Track> trackDAO, SharedState sharedState) {
        this.trackDAO = trackDAO;
        this.sharedState = sharedState;
    }

    public ObservableList<Track> getAllTracks() {return null;}
    public Optional<Track> getTrackById(UUID id){ return null;}
    public Optional<String> addTrack(Track track){ return null;}
    public Optional<String> updateTrack(Track track){ return null;}
    public Optional<String> deleteTrack(Track track){ return null;}

    private void updateInitState(Track track){}

    public Optional<String> verify(Track track){ return null;}

}
