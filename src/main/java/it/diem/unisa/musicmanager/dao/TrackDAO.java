package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.exception.FilePathException;
import it.diem.unisa.musicmanager.model.Track;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TrackDAO implements DAO<Track> {

    private final String filePath;

    public TrackDAO(String filePath) {

        this.filePath = filePath;

        createFilePath();

    }

    private void createFilePath() {

        File file = new File(filePath);

        if (!file.exists()) { //se il file non esiste ancora, lo creo

            try {
                file.createNewFile();   //provo a creare il file

                Files.write(Paths.get(filePath), "[]".getBytes());

            } catch (IOException e) {
                throw new FilePathException("Error: File Path not created!");
            }

        }

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
