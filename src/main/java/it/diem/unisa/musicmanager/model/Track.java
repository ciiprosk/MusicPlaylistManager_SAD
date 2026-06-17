package it.diem.unisa.musicmanager.model;

import it.diem.unisa.musicmanager.exception.TrackInfoException;
import java.util.EnumSet;
import java.util.Set;

import java.util.List;
import java.util.UUID;

/**
 * Rappresenta una traccia musicale all'interno del sistema di gestione della playlist.
 * Questa classe implementa l'interfaccia {@link Playable} e contiene informazioni dettagliate
 * sul brano, come titolo, autore, genere, durata, percorso del file, anno di pubblicazione,
 * numero di ascolti e tag associati. Include anche logiche di validazione per i suoi campi.
 */
public class Track  implements  Playable{

    /**
     * L'identificativo univoco della traccia.
     */
    private UUID  id;

    /**
     * Il titolo del brano (obbligatorio, massimo 100 caratteri).
     */
    private String title;   //max 100 caratteri, OBBLIGATORIA

    /**
     * L'autore o artista del brano (opzionale, massimo 100 caratteri).
     */
    private String author;   //max 100 caratteri. Opzionale

    /**
     * Il genere musicale del brano.
     */
    private Genre genre; //enum, di default è 'KNOWN'

    /**
     * La durata del brano in secondi (maggiore di 0).
     */
    private final int songLength; //durata in secondi della canzone >0

    /**
     * Il percorso del file audio della traccia.
     */
    private final String songPath;

    /**
     * L'anno di pubblicazione (4 cifre, non nel futuro) o 'UNKNOWN'.
     */
    private String year;    //4 cifre, non nel futuro. di default è 'UNKNOWN'

    /**
     * Il numero di volte in cui la traccia è stata riprodotta.
     */
    private int playCount;  //conteggio ascolto traccia

    /**
     * I tag associati alla traccia.
     */
    private Set<Tag> tags;


    /**
     * Costruisce una nuova traccia musicale con validazione dei dati inseriti.
     * 
     * @param title      Il titolo del brano (max 100 caratteri, obbligatorio).
     * @param author     L'autore/artista del brano (max 100 caratteri, opzionale, default "Unknown").
     * @param genre      Il genere musicale del brano (se null, viene impostato a {@link Genre#UNKNOWN}).
     * @param songPath   Il percorso fisico del file audio della canzone.
     * @param songLength La durata in secondi del brano (deve essere maggiore di 0).
     * @param year       L'anno di pubblicazione a 4 cifre, non nel futuro (default "UNKNOWN").
     * @param tags       L'insieme dei tag associati alla traccia.
     * @throws TrackInfoException Se i dati inseriti violano le regole di validazione (titolo vuoto,
     *                            titolo/autore troppo lunghi, durata <= 0, anno non a 4 cifre o futuro).
     */
    public Track(String title, String author, Genre genre, String songPath, int songLength, String year, Set<Tag> tags) {

        this.id = UUID.randomUUID();

        this.title = validateTitle(title);

        this.author = validateAuthor(author);

        this.genre = genre != null ? genre : Genre.UNKNOWN;

        this.songPath = songPath;

        this.songLength = validateSongLength(songLength);

        this.year = validateYear(year);

        this.playCount = 0;

        this.tags = (tags == null || tags.isEmpty())
                ? EnumSet.noneOf(Tag.class)
                : EnumSet.copyOf(tags);

    }

    /**
     * Costruttore per recuperare le informazioni di una traccia già esistente (es. da persistenza)
     * e inizializzare il suo ID specifico.
     * 
     * @param id         L'identificativo univoco (UUID) della traccia.
     * @param title      Il titolo del brano (max 100 caratteri, obbligatorio).
     * @param author     L'autore/artista del brano (max 100 caratteri, opzionale, default "Unknown").
     * @param genre      Il genere musicale del brano (se null, viene impostato a {@link Genre#UNKNOWN}).
     * @param songPath   Il percorso fisico del file audio della canzone.
     * @param songLength La durata in secondi del brano (deve essere maggiore di 0).
     * @param year       L'anno di pubblicazione a 4 cifre, non nel futuro (default "UNKNOWN").
     * @param tags       I tag associati alla traccia.
     * @throws TrackInfoException Se i dati inseriti violano le regole di validazione.
     */
    public Track(UUID id, String title, String author, Genre genre, String songPath, int songLength, String year, EnumSet<Tag> tags) {

        this.id = id;

        this.title = validateTitle(title);

        this.author = validateAuthor(author);

        this.genre = genre != null ? genre : Genre.UNKNOWN;

        this.songPath = songPath;

        this.songLength = validateSongLength(songLength);

        this.year = validateYear(year);

        this.tags = (tags == null || tags.isEmpty())
                ? EnumSet.noneOf(Tag.class)
                : EnumSet.copyOf(tags);

    }

    /**
     * Costruttore con dati dummy, utilizzato principalmente per la rimozione nel DAO.
     * Non applica alcuna validazione sui dati.
     * 
     * @param id L'identificativo univoco (UUID) da associare alla traccia.
     */
    public Track(UUID id) {
        this.id = id;
        this.title = "Unknkwon";
        this.author = "Unknown";
        this.genre = null;
        this.songPath = ""; // Obbligatorio inizializzarlo perché è final
        this.songLength = 1; // Obbligatorio inizializzarlo perché è final, e deve essere >0
        this.year = "UNKNOWN";
        this.playCount = 0;
        this.tags = EnumSet.noneOf(Tag.class);
    }

    /**
     * Restituisce la lista di tracce da riprodurre. Poiché questa classe rappresenta una singola
     * traccia, restituisce una lista contenente solo se stessa.
     * 
     * @return Una lista immutabile contenente questa traccia.
     */
    @Override
    public List<Track> getTracksToPlay() {
        return java.util.List.of(this);
    }

    /**
     * Restituisce il tipo di elemento della coda di riproduzione.
     * 
     * @return Il valore {@link QueueItemType#TRACK}.
     */
    @Override
    public QueueItemType getType() {
        return QueueItemType.TRACK;
    }

    /**
     * Restituisce l'identificativo univoco della traccia.
     * 
     * @return L'UUID della traccia.
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Restituisce la durata del brano in secondi.
     * 
     * @return La durata della canzone.
     */
    public int getSongLength() {
        return songLength;
    }

    /**
     * Restituisce il titolo del brano.
     * 
     * @return Il titolo.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Restituisce il genere musicale del brano.
     * 
     * @return Il {@link Genre} associato.
     */
    public Genre getGenre() {
        return genre;
    }

    /**
     * Restituisce l'anno di pubblicazione del brano.
     * 
     * @return L'anno come stringa o "UNKNOWN".
     */
    public String getYear() {
        return year;
    }

    /**
     * Restituisce l'autore o l'artista del brano.
     * 
     * @return L'autore.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Restituisce il percorso del file audio associato alla traccia.
     * 
     * @return Il percorso del file.
     */
    public String getSongPath() {
        return songPath;
    }

    /**
     * Restituisce una copia dei tag associati a questa traccia.
     * Modifiche al set restituito non influiscono sui tag interni della traccia.
     * 
     * @return Un set contenente i tag associati.
     */
    public Set<Tag> getTags() {
        if (tags == null || tags.isEmpty()) {
            return EnumSet.noneOf(Tag.class);
        }
        return EnumSet.copyOf(tags);
    }

    /**
     * Aggiunge un tag alla traccia.
     * 
     * @param tag Il tag da aggiungere.
     */
    public void addTag(Tag tag) {
        tags.add(tag);
    }

    /**
     * Rimuove un tag dalla traccia.
     * 
     * @param tag Il tag da rimuovere.
     */
    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    /**
     * Verifica se la traccia possiede un determinato tag.
     * 
     * @param tag Il tag da verificare.
     * @return true se il tag è presente, false altrimenti.
     */
    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    /**
     * Imposta i tag associati a questa traccia.
     * 
     * @param tags Il nuovo set di tag da associare.
     */
    public void setTags(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            this.tags = EnumSet.noneOf(Tag.class);
        } else {
            this.tags = EnumSet.copyOf(tags);
        }
    }

    /**
     * Imposta il titolo del brano previa validazione.
     * 
     * @param title Il nuovo titolo da impostare.
     * @throws TrackInfoException Se il titolo non è valido.
     */
    public void setTitle(String title) {
        this.title = validateTitle(title);
    }

    /**
     * Imposta l'autore del brano previa validazione.
     * 
     * @param author Il nuovo autore da impostare.
     * @throws TrackInfoException Se l'autore non è valido.
     */
    public void setAuthor(String author) {
        this.author = validateAuthor(author);
    }

    /**
     * Imposta il genere musicale del brano.
     * 
     * @param genre Il nuovo genere da impostare.
     */
    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    /**
     * Imposta l'anno di pubblicazione previa validazione.
     * 
     * @param year Il nuovo anno da impostare.
     * @throws TrackInfoException Se l'anno non è valido.
     */
    public void setYear(String year) {
        this.year = validateYear(year);
    }

    /**
     * Verifica se una traccia è un duplicato di questa.
     * Due tracce sono considerate duplicate se hanno lo stesso titolo e lo stesso autore
     * (senza spazi iniziali o finali).
     * 
     * @param track La traccia da confrontare.
     * @return true se le tracce sono duplicate, false altrimenti.
     */
    public boolean isDuplicate(Track track) {
        //tracce duplicate se hanno stesso nome e stesso autore

        String trimAuthor = track.getAuthor().trim();

        String trimTitle = track.getTitle().trim();

        return this.author.equals(trimAuthor) && this.title.equals(trimTitle);

    }

    /**
     * Valida il titolo secondo le regole di business (non vuoto, max 100 caratteri).
     * 
     * @param title Il titolo da validare.
     * @return Il titolo validato e ripulito dagli spazi.
     * @throws TrackInfoException Se il titolo è nullo, vuoto o supera i 100 caratteri.
     */
    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new TrackInfoException("Title cannot be empty.");
        }
        if (title.trim().length() > 100) {
            throw new TrackInfoException("Title cannot exceed 100 characters.");
        }
        return title.trim();
    }

    /**
     * Valida l'autore secondo le regole di business (max 100 caratteri, default "Unknown").
     * 
     * @param author L'autore da validare.
     * @return L'autore validato e ripulito dagli spazi.
     * @throws TrackInfoException Se supera i 100 caratteri.
     */
    private String validateAuthor(String author) {
        if (author != null && author.trim().length() > 100) {
            throw new TrackInfoException("Author cannot exceed 100 characters.");
        }
        return (author != null && !author.trim().isEmpty()) ? author.trim() : "Unknown";
    }

    /**
     * Valida la lunghezza del brano (deve essere maggiore di 0).
     * 
     * @param songLength La durata in secondi.
     * @return La durata validata.
     * @throws TrackInfoException Se la durata è minore o uguale a 0.
     */
    private int validateSongLength(int songLength) {
        if (songLength <= 0) {
            throw new TrackInfoException("Song length must be greater than 0 seconds.");
        }
        return songLength;
    }

    /**
     * Valida l'anno di pubblicazione (4 cifre, non nel futuro, default "UNKNOWN").
     * 
     * @param year L'anno da validare.
     * @return L'anno validato.
     * @throws TrackInfoException Se l'anno non è composto da 4 cifre o è nel futuro.
     */
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

    /**
     * Restituisce il conteggio degli ascolti del brano.
     * 
     * @return Il numero di volte in cui la traccia è stata ascoltata.
     */
    public int getPlayCount() {
        return playCount;
    }

    /**
     * Incrementa di uno il conteggio degli ascolti del brano.
     */
    public void incrementPlayCount() {
        this.playCount++;
    }

    /**
     * Verifica l'uguaglianza tra questo oggetto ed un altro.
     * Due tracce sono considerate uguali se hanno lo stesso ID (UUID).
     * 
     * @param o L'oggetto da confrontare con questa traccia.
     * @return true se gli oggetti sono uguali, false altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Track other = (Track) o;

        return id != null && id.equals(other.id);
    }

    /**
     * Restituisce l'hashcode basato sull'ID della traccia.
     * 
     * @return L'hashcode calcolato.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
