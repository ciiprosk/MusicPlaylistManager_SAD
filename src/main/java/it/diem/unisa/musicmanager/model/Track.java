package it.diem.unisa.musicmanager.model;

import java.util.UUID;

public class Track {

    private static final UUID  id = UUID.randomUUID();

    private String title;

    private String author;

    private Genre genre; //creare enum Genre

    private int songLength;

    private final String songPath;

    public Track(String title, String author, Genre genre, String songPath) {

        this.title = title;

        this.author = author;

        this.genre = genre;

        this.songPath = songPath;

    }

    //FARE COSTRUTTORE CON LA DURATA

    public UUID getId() {
        return id;
    }

    public int getSongLength() {
        return songLength;
    }

    public String getTitle() {
        return title;
    }

    public Genre getGenre() {
        return genre;
    }

    public String getAuthor() {
        return author;
    }

    public String getSongPath() {
        return songPath;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
