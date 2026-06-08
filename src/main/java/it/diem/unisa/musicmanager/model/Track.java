package it.diem.unisa.musicmanager.model;

import it.diem.unisa.musicmanager.exception.TrackInfoException;

import java.util.UUID;

public class Track {

    private UUID  id;

    private String title;   //max 100 caratteri, OBBLIGATORIA

    private String author;   //max 100 caratteri. Opzionale

    private Genre genre; //enum, di default è 'KNOWN'

    private final int songLength; //durata in secondi della canzone >0

    private final String songPath;

    private String year;    //4 cifre, non nel futuro. di default è 'UNKNOWN'

    private int playCount;  //conteggio ascolto traccia


    public Track(String title, String author, Genre genre, String songPath, int songLength, String year) {

        this.id = UUID.randomUUID();

        this.title = validateTitle(title);

        this.author = validateAuthor(author);

        this.genre = genre != null ? genre : Genre.UNKNOWN;

        this.songPath = songPath;

        this.songLength = validateSongLength(songLength);

        this.year = validateYear(year);

        this.playCount = 0;

    }

    //costruttore per recuperare le info già esistenti (persistenza)
    public Track(UUID id, String title, String author, Genre genre, String songPath, int songLength, String year) {

        this.id = id;

        this.title = validateTitle(title);

        this.author = validateAuthor(author);

        this.genre = genre != null ? genre : Genre.UNKNOWN;

        this.songPath = songPath;

        this.songLength = validateSongLength(songLength);

        this.year = validateYear(year);

    }

    //costruttore con solo id, ci serve per la delete nel DAO
    //è un costruttore con dati Dummy, per cui non applico validazione dei dati
    public Track(UUID id) {
        this.id = id;
        this.title = "Unknkwon";
        this.author = "Unknown";
        this.genre = null;
        this.songPath = ""; // Obbligatorio inizializzarlo perché è final
        this.songLength = 1; // Obbligatorio inizializzarlo perché è final, e deve essere >0
        this.year = "UNKNOWN";
        this.playCount = 0;
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
        this.title = validateTitle(title);
    }

    public void setAuthor(String author) {
        this.author = validateAuthor(author);
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public void setYear(String year) {
        this.year = validateYear(year);
    }

    public boolean isDuplicate(Track track) {
        //tracce duplicate se hanno stesso nome e stesso autore

        String trimAuthor = track.getAuthor().trim();

        String trimTitle = track.getTitle().trim();

        return this.author.equals(trimAuthor) && this.title.equals(trimTitle);

    }

//metodi privati di validazione, chiamati dai costruttori e dai setter
// per rispettare le business rules

private String validateTitle(String title) {
    if (title == null || title.trim().isEmpty()) {
        throw new TrackInfoException("Title cannot be empty.");
    }
    if (title.trim().length() > 100) {
        throw new TrackInfoException("Title cannot exceed 100 characters.");
    }
    return title.trim();
}

    private String validateAuthor(String author) {
        if (author != null && author.trim().length() > 100) {
            throw new TrackInfoException("Author cannot exceed 100 characters.");
        }
        return (author != null && !author.trim().isEmpty()) ? author.trim() : "Unknown";
    }

    private int validateSongLength(int songLength) {
        if (songLength <= 0) {
            throw new TrackInfoException("Song length must be greater than 0 seconds.");
        }
        return songLength;
    }

    private String validateYear(String year) {

        //caso di campo vuoto --> si mette il default
        String finalYear = (year != null && !year.trim().isEmpty()) ? year.trim() : "UNKNOWN";

        //caso non vuoto (quindi non default)
        if (!finalYear.equals("UNKNOWN")) {

            // controllo formato --> esattamente 4 cifre
            if (!finalYear.matches("\\d{4}")) {
                throw new TrackInfoException("Year must be exactly 4 digits or 'UNKNOWN'.");
            }

            // controllo anno --> non può essere un anno futuro
            int currentYear = java.time.Year.now().getValue();
            int parsedYear = Integer.parseInt(finalYear);   //devo farlo per poter confrontare gli anni

            if (parsedYear > currentYear) {
                throw new TrackInfoException("Year cannot be in the future (max " + currentYear + ").");
            }
        }

        return finalYear;
    }
    public int getPlayCount() {
        return playCount;
    }

    public void incrementPlayCount() {
        this.playCount++;
    }

}
