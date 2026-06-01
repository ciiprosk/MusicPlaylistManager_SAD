package it.diem.unisa.musicmanager.dao;

import com.google.gson.Gson;
import it.diem.unisa.musicmanager.model.Track;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JSONTrackDAO extends JSONAbstractDAO implements DAO<Track> {

    private final String filePath;

    private final String folderPath;

    private Gson json;

    public JSONTrackDAO(String filePath, String folderPath, String fileName) {

        this.folderPath = folderPath;
        this.filePath = folderPath + File.separator + fileName;
        json = new Gson();
        super.createFileJSON(filePath, folderPath);
    }

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
    public Optional<Track> searchById(UUID id) {
        return Optional.empty();
    }

    /**
     * @param track
     * @return
     */
    @Override
    public boolean isDuplicated(Track track) {
        return false;
    }
}
