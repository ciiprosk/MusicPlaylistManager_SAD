package it.diem.unisa.musicmanager.model;

import java.util.UUID;

public class Track {

    private UUID  id;

    private String title;   //max 100 caratteri, OBBLIGATORIA

    private String author;   //max 100 caratteri. Opzionale

    private Genre genre; //enum, di default è 'KNOWN'

    private final int songLength; //durata in secondi della canzone >0

    private final String songPath;

    private String year;    //4 cifre, non nel futuro. di default è 'UNKNOWN'


    public Track(String title, String author, Genre genre, String songPath, int songLength, String year) {

        this.id = UUID.randomUUID();

        String trimTitle = title.trim();

        String trimAuthor = author.trim();

        this.title = trimTitle;

        this.author = trimAuthor;

        this.genre = genre;

        this.songPath = songPath;

        this.songLength = songLength;

        this.year = year;

    }

    //costruttore per recuperare le info già esistenti (persistenza)
    public Track(UUID id, String title, String author, Genre genre, String songPath, int songLength, String year) {

        this.id = UUID.randomUUID();

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

        String trimAuthor = track.getAuthor().trim();

        String trimTitle = track.getTitle().trim();

        return this.author.equals(trimAuthor) && this.title.equals(trimTitle);

    }

    /* forse va nel controller?
    public boolean checkValidFields(String title, String author, Genre genre, int songLength, String year) {

        if (!title.isEmpty() && title.length() <= 100) {
            return false;
        }

        if (author.length() <= 100) {
            return false;
        }

        if (genre.name().length() > 50) {
            return false;
        }

        if (songLength > 0) {
            return false;
        }

        if (year != "UNKNOWN" || year.matches(""))

    }

    */

}
