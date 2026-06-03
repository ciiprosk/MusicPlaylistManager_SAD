package it.diem.unisa.musicmanager.dao;

import com.google.gson.Gson;
import it.diem.unisa.musicmanager.exception.FilePathException;
import it.diem.unisa.musicmanager.exception.JSONFileException;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JSONTrackDAO extends JSONAbstractDAO implements DAO<Track> {

    private final String filePath;

    private Gson json;

    public JSONTrackDAO(String folderPath, String fileName) {

        this.filePath = folderPath + File.separator + fileName;
        json = new Gson();
        super.createFileJSON(this.filePath, folderPath);
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
            //
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
        updateFile(track, false);
    }

    /**
     * @param id
     */
    @Override
    public void delete(UUID id) {

        Track tempTrack = new Track(id); //costruttore realizzato appositamente
        //per la delete, ci serve per eliminare la traccia dal Json tramite id

        updateFile(tempTrack,true);
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

    private void updateFile(Track track, boolean delete){
        if (!fileExists()){
            return;
        }

        File tempFile = new File(filePath + ".temp"); //qui c'è il file vecchio

        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));){

            String line;
            while((line = bufferedReader.readLine())!=null){
                if(line.trim().isEmpty()){ continue;}

                Track oldTrack = json.fromJson(line, Track.class);

                // è stata trovata la traccia
                if(oldTrack.getId().equals(track.getId())){
                    if(!delete){
                        String trackString = json.toJson(track, Track.class);
                        bufferedWriter.write(trackString + "\n");
                    }
                }else{
                    bufferedWriter.write(line + "\n");
                }
            }


        } catch (FileNotFoundException e) {
            throw new JSONFileException("File not found: " + filePath );
        } catch (UnsupportedEncodingException e) {
            throw new JSONFileException("Unsupported encoding: " + filePath );
        } catch (IOException e) {
            throw new JSONFileException("Error reading or writing file: " + filePath );
        }
        try{
            Files.move(tempFile.toPath(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            throw new FilePathException("Error: File Path not created!");
        }
        return;
    }
}
