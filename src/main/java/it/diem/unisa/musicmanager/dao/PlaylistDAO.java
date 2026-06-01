package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.exception.FilePathException;
import it.diem.unisa.musicmanager.model.Playlist;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlaylistDAO implements DAO<Playlist>{
    private final String filePath;
    private final String folderPath;

    /**
     * @return
     */

    public PlaylistDAO( String folderPath, String fileName) {

        this.folderPath = folderPath;
        this.filePath = folderPath + File.separator + fileName;
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
       File folder = new File(folderPath);
       File file = new File(filePath);

       try{
           //controllo che la directory esista
           if(!folder.exists()){
               folder.mkdir(); //crea la directory

           }
           if(!file.exists()){
               file.createNewFile();
               Files.write(Paths.get(filePath), "[]".getBytes()); // si usa files perché è una lobreria nuova e rende più semplici questi metodi
                //getbytes si usa peer converire
           }
       } catch (Exception e) {
           throw new FilePathException("Error: File Path not created!");
       }

    }
}
