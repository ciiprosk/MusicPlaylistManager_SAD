package it.diem.unisa.musicmanager.model;

import java.util.UUID;

public class Track {

    private static final UUID  id = UUID.randomUUID();

    private String title;

    private String author;

    private Genre genre; //creare enum Genre

    private final int songLength;

    private final String songPath;

    private int year;


    public Track(String title, String author, Genre genre, String songPath, int songLength, int year) {

        this.title = title.trim();

        this.author = author.trim();

        this.genre = genre;

        this.songPath = songPath;

        this.songLength = songLength;

        this.year = year;

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

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public void setYear(int year) {
        
    }

    public boolean isDuplicate(Track track) {
        //tracce duplicate se hanno stesso nome e stesso autore

        return this.author.equals(track.getAuthor()) && this.title.equals(track.getTitle());

    }

}
