package it.diem.unisa.musicmanager.dao;

import com.google.gson.Gson;
import it.diem.unisa.musicmanager.exception.FilePathException;
import it.diem.unisa.musicmanager.model.Playlist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        List<Playlist> playlists= new ArrayList<>();
        if (Files.exists(Paths.get(filePath))) {
        }
        return List.of();
    }

    /**
     * Metodo che inserisce una playlist nel file JSON.
     *
     * Il metodo utilizza la classe OutputStreamWriter per scrivere i dati nel file JSON.
     * 1. Crea un OutputStreamWriter per scrivere i dati nel file JSON.
     * 2. Crea un File per il percorso del file JSON.
     * 3. Scrive i dati nel file JSON utilizzando l'OutputStreamWriter.
     * 4. Chiude l'OutputStreamWriter e il FileOutputStream.
     *
     * @param playlist: la playlist da inserire.
     */
    @Override
    public void insert(Playlist playlist) {
        // voglio suare java.io
        //getsione dello stram dei dati
        FileOutputStream fileOutputStream = null;

        try(OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8")){
            // per l'inserimenro i dati sono di tipo { id, name, list ids' tracks}
            String playlistString =  json.toJson(playlist, Playlist.class); // mi ritorna l'oggetto sottoforma di stringa
            System.out.println(playlistString);
            outputStreamWriter.write(playlistString);
        }
        catch (IOException e) {
            //ancora non so cosa lancaire
        }
    }

    /**
     * @param playlist
     */
    @Override
    public void update(Playlist playlist) {
        //devo fare la mia ricerca di questa playlist e modifico i campi modificati, come nome oppure nuove tracce inserite
        if (!exists()){return;}
            OutputStreamWriter outputStreamWriter = null;
            FileOutputStream fileOutputStream = null;
            File file = new File(filePath);


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

    private boolean checkRulesName(String name) {
        return false;
    }
    private boolean findById(UUID id) {
        return false;
    }

    private boolean exists() {
        File file = new File(filePath);
        return file.exists();
    }
}