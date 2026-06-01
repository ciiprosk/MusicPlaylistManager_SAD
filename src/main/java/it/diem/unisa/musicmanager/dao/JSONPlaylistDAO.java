package it.diem.unisa.musicmanager.dao;

import com.google.gson.Gson;
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
    private Gson json;

    /**
     * Costruttore della classe JSONPlaylistDAO.
     * @return un oggetto JSONPlaylistDAO
     */

    public JSONPlaylistDAO(String folderPath, String fileName) {

        this.folderPath = folderPath;
        this.filePath = folderPath + File.separator + fileName;
        json = new Gson();
        super.createFileJSON(filePath, folderPath);
    }

    /**
     * Metodo che restituisce tutte le playlist presenti nel file JSON.
     * @return La lista di playlist.
     */

    @Override
    public List<Playlist> selectAll() {
        if (Files.exists(Paths.get(filePath))) {

        }
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