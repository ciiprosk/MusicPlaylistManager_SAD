package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.model.Playlist;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlaylistDAO implements DAO<Playlist>{
    private final String filePath;
    /**
     * @return
     */

    public PlaylistDAO(String filePath) {
        this.filePath = filePath;
        createFile();
    }


    @Override
    public List<Playlist> selectAll() {
        return List.of();
    }

    /**
     * @param playlist
     */
    @Override
    public void insert(Playlist playlist) {

    }

    /**
     * @param playlist
     */
    @Override
    public void update(Playlist playlist) {

    }

    /**
     * @param playlist
     */
    @Override
    public void delete(Playlist playlist) {

    }

    /**
     * @param id
     * @return
     */
    @Override
    public Optional<Playlist> searchById(UUID id) {
        return Optional.empty();
    }

    private void createFile(){
       File file = new File(filePath);
       if(!file.exists()) file.mkdir();
    }
}
