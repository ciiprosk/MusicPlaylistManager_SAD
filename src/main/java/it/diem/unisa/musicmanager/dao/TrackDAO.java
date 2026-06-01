package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.model.Track;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TrackDAO implements DAO<Track> {


    /**
     * @return
     */
    @Override
    public List<Track> selectAll() {

        return null;

    }

    /**
     * @param track
     */
    @Override
    public void insert(Track track) {

    }

    /**
     * @param track
     */
    @Override
    public void update(Track track) {

    }

    /**
     * @param track
     */
    @Override
    public void delete(Track track) {

    }

    /**
     * @param id
     * @return
     */
    @Override
    public Optional<Track> searchById(UUID id) {
        return Optional.empty();
    }
}
