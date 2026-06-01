package it.diem.unisa.musicmanager.dao;

import com.google.gson.Gson;
import it.diem.unisa.musicmanager.exception.FilePathException;
import it.diem.unisa.musicmanager.exception.JSONFileException;
import it.diem.unisa.musicmanager.model.Playlist;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
        if (!exists()) {return playlists;}

        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))){
            String line;
            while((line = bufferedReader.readLine())!=null){
                if(line.trim().isEmpty()){ continue;}
                Playlist p = json.fromJson(line, Playlist.class);
                playlists.add(p);
            }

        } catch (
                FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return playlists;
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

        if (!exists()) {return;}
        //in questo caso posso suare outputwtiter peerché la scrittura è di un solo oggetto semplicino
        try(OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8")){
            // per l'inserimenro i dati sono di tipo { id, name, list ids' tracks}
            String playlistString =  json.toJson(playlist, Playlist.class); // mi ritorna l'oggetto sottoforma di stringa
            System.out.println(playlistString);
            outputStreamWriter.write(playlistString + "\n");
        }
        catch (IOException e) {
            throw new JSONFileException("Error: File Path not created!");
        }
    }

    /**
     * @param playlist
     */
    @Override
    public void update(Playlist playlist) {
        //per fare la modifca mi ritrovo in uno dei due casi
        // 1. la playlist esiste già ed è sttao modiifcato il nome
        // 2. la playlist esiste ed è stata aggiunta una traccia

        // per i file json lines nel nostro caso e con tutti i file non strutturati, ò'update e la delete non può essere fatta sulla singola riga
        // quindi queelo che si dev efare i uqetso caso è
        // 1. creareun file tempoòraneo con i vecchi dati
        // 2. leggere e scrivere gli oggetti del file nel file di playlist fino a quando non trovo il file moidificato, lo insersico e finisco di scrivere

        updateFile(playlist, false);
    }

    /**
     * Metodo che cancella una playlist dal file JSON.
     * @param id è l'identificatore univoco della playlist da cancellare.
     */
    @Override
    public void delete(UUID id) {
    Playlist playlist = new Playlist(id, "temp", new ArrayList<>()); //creo una playlist vuota con l'id che voglio cancellare
    updateFile(playlist, true);
    }

    /**
     * Metodo che cerca una playlist nel file JSON.
     * @param id è l'identificatore univoco della playlist da cercare.
     * @return un Optional che contiene la playlist se trovata, altrimenti Optional.empty().
     */
    @Override
    public Optional<Playlist> searchById(UUID id) {
        Optional<Playlist> playlist = Optional.empty();
        if (!exists()) {return playlist;}
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))){
            String line;
            while((line = bufferedReader.readLine())!=null){
                if(line.trim().isEmpty()){ continue;}
                Playlist p = json.fromJson(line, Playlist.class);
                if(p.getId().equals(id)){
                    playlist = Optional.of(p);
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            throw new JSONFileException("File not found: " + filePath );
        } catch (IOException e) {
            throw new JSONFileException("Error reading or writing file: " + filePath );
        }

        return playlist;

    }

    /**
     * Il metodo controlla se il nome di una playlst è già presente nel file JSON.
     * @param playlist è la playlist da controllare, di cui si controlla il nome.
     * @return true se il nome è giè presente, false altrimenti.
     */
    @Override
    public boolean isDuplicated(Playlist playlist) {
        if (!exists()) {return false;}
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))){
            String line;
            while((line = bufferedReader.readLine())!=null){
                if(line.trim().isEmpty()){ continue;}
                Playlist p = json.fromJson(line, Playlist.class); // p è la playlist corrente del file da veificare che sia uguale al nome della playlist da verificare
                if(p.getName().equals(playlist.getName())){
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            throw new JSONFileException("File not found: " + filePath);
        } catch (IOException e) {
            throw new JSONFileException("Error reading or writing file: " + filePath);
        }
        return false;
    }

    /**
     * Metodo privato di utilità che aggiorna il file jsonlines
     * @param playlist è la playlist da aggiornare (modificare o cancellare)
     * @param delete è un booleano che indica se la playlist deve essere cancellata
     * @return false se l'operazione non è andata a buon fine, true altrimenti
     */
    private boolean updateFile(Playlist playlist, boolean delete){
        if (!exists()){return false;}
        File tempFile = new File(filePath + ".temp"); //qui c'è il file vecchio

        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));){

            // il motivo per cui scriamo su temp e leggiamo su priginale è un fattp di sicurezza nel caso in cui ci sia un blackout posso recuperare i ifle dal file temporaneo
            String line;
            while((line = bufferedReader.readLine())!=null){
                if(line.trim().isEmpty()){ continue;}

                Playlist p = json.fromJson(line, Playlist.class);

                // è stata trpvata la playlist
                if(p.getId().equals(playlist.getId())){
                    if(!delete){
                        String playlistString = json.toJson(playlist, Playlist.class);
                        bufferedWriter.write(playlistString + "\n");
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
        return true;
    }

    /**
     * Metodo privato che controlla se il file esiste
     * @return true se esiste, false altrimenti
     */
    private boolean exists() {
//        File file = new File(filePath); //il file non viene creato da zero, ma viene associato un puntatore al filepath
//        return file.exists();
        return Files.exists(Paths.get(filePath)); //gestisce meglio tutto
    }
}