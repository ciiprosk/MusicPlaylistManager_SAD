package it.diem.unisa.musicmanager.model;

import it.diem.unisa.musicmanager.PlaylistException;
import it.diem.unisa.musicmanager.exception.PlaylistInfoException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Rappresenta una playlist che contiene una raccolta di tracce identificate dai UUID.
 * Permette di gestire gli attributi di una playlist (nome e tracce) e fornisce funzionalità per modificare le tracce associate e convalidare le regole di business.
 *
 */
public class Playlist {
    /**
     * Gli attributi di una playlist sono:
     * Il nome della playlist;
     * Un identificatore univoco (UUID) per la playlist;
     * Una raccolta di tracce identificate dai UUID.
     */
    private String name;
    private final UUID id = UUID.randomUUID();
    private List<UUID> tracks;

    /**
     * Costruttore della playlist.
     * @param name: il nome della playlist.
     * Viene inizializzata la raccolta di tracce vuota.
     */
    public Playlist(String name) {
        if (checkRulesName(name.trim()))  this.name = name.trim();
        this.tracks = new ArrayList<>();
    }

    /**
     * Getter per il nome della playlist.
     * @return il nome della playlist.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter per l'identificatore univoco della playlist.
     * @return l'identificatore univoco della playlist.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Setter per il nome della playlist.
     * @param name il nuovo nome della playlist.
     */

    public void setName(String name) {
        if(checkRulesName(name.trim())) this.name = name.trim();
    }

    /**
     * Metodo che aggiunge le tracce all'interno della playlist.
     * @param trackID: l'identificatore univoco della traccia da aggiungere.
     */
    public void addTrack(UUID trackID){
        tracks.add(trackID);
    }

    /**
     * Metodo che rimuove le tracce dalla playlist.
     * @param trackID: l'identificatore univoco della traccia da rimuovere.
     */
    public void removeTrack(UUID trackID){
        tracks.remove(trackID);
    }

    /**
     * Metodo che ritorna le tracce presenti nella playlist.
     * @return una lista di tracce presenti nella playlist.
     */
    public List<UUID> getTracks() {
        return tracks;
    }
     public boolean containsTrack(UUID trackID){
        return tracks.contains(trackID);
     }

    /**
     * Valida le business rules per il nome della playlist.
     * Si assicura che il nome non sia vuoto e che non superi i 50 caratteri.
     *
     * @param name il nome della playlist da verificare
     * @return true se il nome soddisfa le business rules, un'eccezione altrimenti.
     * @throws PlaylistInfoException se il nome non soddisfa le business rules.
     */
     // metodo per la verifica delle business rules
    private boolean checkRulesName(String name) throws PlaylistInfoException {
        boolean check = false;
        if(name.isEmpty()) throw new PlaylistInfoException("The name cannot be empty");
        if(name.length() > 50) throw new PlaylistInfoException("The name cannot be longer than 50 characters");

        check = true;
        return check;
    }

}
