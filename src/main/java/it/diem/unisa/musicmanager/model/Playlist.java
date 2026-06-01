package it.diem.unisa.musicmanager.model;

import it.diem.unisa.musicmanager.PlaylistException;
import it.diem.unisa.musicmanager.exception.PlaylistInfoException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Playlist {
    private String name;
    private final UUID id = UUID.randomUUID();
    private List<UUID> tracks;

    public Playlist(String name) {
        if (checkRulesName(name.trim()))  this.name = name.trim();
        this.tracks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public UUID getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addTrack(UUID trackID){
        tracks.add(trackID);
    }

    public void removeTrack(UUID trackID){
        tracks.remove(trackID);
    }

    public List<UUID> getTracks() {
        return tracks;
    }
     public boolean containsTrack(UUID trackID){
        return tracks.contains(trackID);
     }

     // metodo per la verifica delle business rules
    private boolean checkRulesName(String name) throws PlaylistInfoException {
        if(name.isEmpty()) throw new PlaylistInfoException("The name cannot be empty");
        if(name.length() > 50) throw new PlaylistInfoException("The name cannot be longer than 50 characters");

        return true;
    }

}
