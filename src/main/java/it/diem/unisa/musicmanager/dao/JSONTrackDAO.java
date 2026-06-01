package it.diem.unisa.musicmanager.dao;

import com.google.gson.Gson;
import it.diem.unisa.musicmanager.exception.FilePathException;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JSONTrackDAO extends JSONAbstractDAO implements DAO<Track> {

    private final String filePath;

    private final String folderPath;

    private Gson json;

    public JSONTrackDAO(String folderPath, String fileName) {

        this.folderPath = folderPath;
        this.filePath = folderPath + File.separator + fileName;
        json = new Gson();
        super.createFileJSON(this.filePath, this.folderPath);
    }

    /**
     * @return
     */
    @Override
    public List<Track> selectAll() {

        List<Track> tracks = new ArrayList<>();

        if (!fileExists()) {   //se il file non esiste...
            return tracks;  //ritorno la lista vuota
        }

        // Leggo il file riga per riga
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Salto le righe vuote
                if (!line.trim().isEmpty()) {

                    //da Json, ottengo un oggetto Track
                    Track track = json.fromJson(line, Track.class);

                    //aggiungo traccia alla lista
                    tracks.add(track);
                }

            }

        } catch (IOException e) {
            // Catturo l'eccezione checked e lancio la tua RuntimeException custom
            throw new FilePathException("Error: Selection of tracks failed!");
        }

        return tracks;


    }

    /**
     * @param track
     */
    @Override
    public void insert(Track track) {

        if (!fileExists())  //se il file non esiste, non posso fare la Insert
            return;

        try(OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8")){
            // per l'inserimenro i dati sono di tipo {id, title, author, genre, songPath, songLength, year}
            String trackString =  json.toJson(track, Track.class); // mi ritorna l'oggetto sottoforma di stringa
            outputStreamWriter.write(trackString + "\n");
        } catch (IOException e) {
            throw new FilePathException("Error during the Insert!");
        }

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

        // Cerca la prima traccia con l'ID corrispondente e la restituisce
        //Sfruttiamo la selectAll già creata e la possibilità di fare filtro con lo stream
        //sull'id. Ritorniamo la prima occorrenza, dovrebbe essercene comunque sempre solo una
        return selectAll().stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    /**
     * @param track
     * @return
     */
    @Override
    public boolean isDuplicated(Track track) {
        // Cerca se esiste già una traccia con lo stesso titolo e autore
        return selectAll().stream()
                .anyMatch(t -> t.getTitle().equals(track.getTitle())
                        && t.getAuthor().equals(track.getAuthor()));
    }

    private boolean fileExists() {
        return Files.exists(Paths.get(filePath));
    }
}
