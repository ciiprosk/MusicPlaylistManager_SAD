package it.diem.unisa.musicmanager.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Playlist {
    private String name;
    private final UUID id = UUID.randomUUID();
    private List<UUID> tracks;

    public Playlist(String name) {
        this.name = name;
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

}
