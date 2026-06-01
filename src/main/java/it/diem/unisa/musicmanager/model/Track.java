package it.diem.unisa.musicmanager.model;

import java.util.UUID;

public class Track {

    private static final UUID  id = UUID.randomUUID();

    private String title;   //max 100 caratteri

    private String author;   //max 100 caratteri

    private Genre genre; //enum

    private final int songLength; //durata in secondi della canzone

    private final String songPath;

    private String year;


    public Track(String title, String author, Genre genre, String songPath, int songLength, String year) {

        String trimTitle = title.trim();

        String trimAuthor = author.trim();

        this.title = trimTitle;

        this.author = trimAuthor;

        this.genre = genre;

        this.songPath = songPath;

        this.songLength = songLength;

        this.year = year;

    }

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

    public String getYear() {
        return year;
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

    public void setYear(String year) {
        this.year = year;
    }

    public boolean isDuplicate(Track track) {
        //tracce duplicate se hanno stesso nome e stesso autore

        return this.author.equals(track.getAuthor()) && this.title.equals(track.getTitle());

    }

    public boolean isValid(Track track) {

        return true;

    }

}
