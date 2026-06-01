package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.exception.FilePathException;
import it.diem.unisa.musicmanager.model.Playlist;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JSONPlaylistDAO  extends JSONAbstractDAO implements DAO<Playlist> {
    private final String filePath;
    private final String folderPath;

    /**
     * @return
     */

    public JSONPlaylistDAO(String folderPath, String fileName) {

        this.folderPath = folderPath;
        this.filePath = folderPath + File.separator + fileName;
        super.createFileJSON(filePath, folderPath);
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
     * @param id
     */
    @Override
    public void delete(UUID id) {

    }

    /**
     * @param id
     * @return
     */
    @Override
    public Optional<Playlist> searchById(UUID id) {
        return Optional.empty();
    }

    /**
     * @param playlist
     * @return
     */
    @Override
    public boolean isDuplicated(Playlist playlist) {
        return false;
    }

}