package it.diem.unisa.musicmanager.model;

import it.diem.unisa.musicmanager.exception.PlaylistInfoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Rappresenta una playlist che contiene una raccolta di tracce.
 * Permette di gestire gli attributi di una playlist (nome e tracce) e fornisce funzionalità
 * per modificare le tracce associate e convalidare le regole di business.
 * Questa classe mantiene sia una lista di UUID per la persistenza su file (trackIDs),
 * sia una lista di oggetti Track risolti a runtime (tracksList).
 */
public class Playlist implements Playable {

    /**
     * Il nome della playlist.
     */
    private String name;

    /**
     * L'identificativo univoco della playlist.
     */
    private UUID id;
    
    /**
     * Lista degli UUID delle tracce per il salvataggio nel file JSON.
     */
    private List<UUID> trackIDs;
    
    /**
     * Lista in memoria degli oggetti Track. Non viene serializzata nel JSON.
     */
    private transient List<Track> tracksList;

    /**
     * Il numero di volte in cui la playlist è stata riprodotta.
     */
    private int playCount;

    /**
     * Costruttore della playlist. Inizializza gli identificativi e le liste.
     * 
     * @param name Il nome della playlist.
     * @throws PlaylistInfoException Se il nome non soddisfa le regole di validazione.
     */
    public Playlist(String name) {
        if (checkRulesName(name.trim())) this.name = name.trim();
        this.id = UUID.randomUUID();
        this.trackIDs = new ArrayList<>();
        this.tracksList = new ArrayList<>();
        this.playCount = 0;
    }

    /**
     * Costruttore della playlist utilizzato per il recupero di una playlist dal file JSON (persistenza).
     * 
     * @param id       L'identificatore univoco della playlist.
     * @param name     Il nome della playlist.
     * @param trackIDs Una lista di tracce identificate dai loro UUID.
     * @throws PlaylistInfoException Se il nome non soddisfa le regole di validazione.
     */
    public Playlist(UUID id, String name, List<UUID> trackIDs) {
        if (checkRulesName(name.trim())) this.name = name.trim();
        this.id = id;
        this.trackIDs = trackIDs != null ? new ArrayList<>(trackIDs) : new ArrayList<>();
        this.tracksList = new ArrayList<>();
        this.playCount = 0;
    }

    /**
     * Costruttore della playlist utilizzato per creare temporaneamente una copia
     * per operazioni come l'aggiornamento del nome.
     * 
     * @param playlistID L'identificatore univoco della playlist.
     * @param name       Il nuovo nome della playlist.
     * @throws PlaylistInfoException Se il nome non soddisfa le regole di validazione.
     */
    public Playlist(UUID playlistID, String name) {
        if (checkRulesName(name.trim())) this.name = name.trim();
        this.id = playlistID;
        this.trackIDs = new ArrayList<>();
        this.tracksList = new ArrayList<>();
        this.playCount = 0;
    }

    /**
     * Restituisce la lista di tracce associate a questa playlist per la riproduzione.
     * 
     * @return La lista degli oggetti {@link Track} in memoria.
     */
    @Override
    public List<Track> getTracksToPlay() {
        return tracksList;
    }

    /**
     * Restituisce il tipo di elemento della coda di riproduzione.
     * 
     * @return Il valore {@link QueueItemType#PLAYLIST}.
     */
    @Override
    public QueueItemType getType() {
        return QueueItemType.PLAYLIST;
    }

    /**
     * Getter per l'identificatore univoco della playlist.
     * 
     * @return L'identificatore univoco (UUID) della playlist.
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Restituisce il nome della playlist.
     * 
     * @return Il nome della playlist.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter per il nome della playlist. Valida il nome prima dell'assegnazione.
     * 
     * @param name Il nuovo nome da impostare per la playlist.
     * @throws PlaylistInfoException Se il nome è vuoto o supera i 50 caratteri.
     */
    public void setName(String name) {
        if(checkRulesName(name.trim())) this.name = name.trim();
    }

    /*

     * Metodo legacy per l'aggiunta di una traccia tramite UUID.
     * @param trackID l'identificatore univoco della traccia da aggiungere.

    public void addTrack(UUID trackID){
        if(!trackIDs.contains(trackID)){
            trackIDs.add(trackID);
        }
    }


     * Metodo legacy per la rimozione di una traccia tramite UUID.
     * @param trackID l'identificatore univoco della traccia da rimuovere.

    public void removeTrack(UUID trackID){
        trackIDs.remove(trackID);
    }

    */

    /**
     * Metodo legacy per l'aggiunta di una traccia tramite UUID.
     * Aggiorna la lista degli ID usata per la persistenza su file.
     *
     * @param trackID L'identificatore univoco della traccia da aggiungere.
     */
    public void addTrack(UUID trackID) {
        if (trackID != null && !trackIDs.contains(trackID)) {
            trackIDs.add(trackID);
        }

        if (tracksList == null) {
            tracksList = new ArrayList<>();
        }
    }

    /**
     * Metodo legacy per la rimozione di una traccia tramite UUID.
     * Rimuove la traccia sia dalla lista degli ID sia dalla lista degli oggetti Track in memoria.
     *
     * @param trackID L'identificatore univoco della traccia da rimuovere.
     */
    public void removeTrack(UUID trackID) {
        if (trackID != null) {
            trackIDs.remove(trackID);

            if (tracksList != null) {
                tracksList.removeIf(track -> track.getId().equals(trackID));
            }
        }
    }

    /**
     * Metodo che ritorna una copia non modificabile della lista degli UUID delle tracce presenti.
     * 
     * @return Una lista non modificabile di UUID delle tracce.
     */
    public List<UUID> getTracks() {
        return Collections.unmodifiableList(trackIDs);
    }

    /**
     * Verifica se la playlist contiene una determinata traccia tramite il suo ID.
     * 
     * @param trackID L'identificatore univoco della traccia da cercare.
     * @return true se la playlist contiene la traccia con quell'ID, false altrimenti.
     */
    public boolean containsTrack(UUID trackID) {
        return trackIDs.contains(trackID);
    }

    /**
     * Ritorna il numero di tracce contenute nella playlist.
     * 
     * @return Il numero totale di tracce presenti nella playlist.
     */
    public int numberOfTracks() {
        return trackIDs.size();
    }

    /**
     * Valida le business rules per il nome della playlist.
     * Si assicura che il nome non sia vuoto e che non superi i 50 caratteri.
     *
     * @param name Il nome della playlist da verificare.
     * @return true se il nome soddisfa le business rules.
     * @throws PlaylistInfoException Se il nome è nullo, vuoto o supera i 50 caratteri.
     */
    private boolean checkRulesName(String name) throws PlaylistInfoException {
        if(name == null || name.isEmpty()) throw new PlaylistInfoException("The name cannot be empty");
        if(name.length() > 50) throw new PlaylistInfoException("The name cannot be longer than 50 characters");
        return true;
    }

    /**
     * Aggiunge una traccia alla playlist, aggiornando sia la lista in memoria che la lista per il salvataggio JSON.
     * 
     * @param track L'oggetto {@link Track} da aggiungere.
     */
    public void addTrack(Track track) {
        if (track != null && !trackIDs.contains(track.getId())) {
            trackIDs.add(track.getId());
            tracksList.add(track);
        }
    }

    /**
     * Aggiunge una traccia alla playlist in una determinata posizione, aggiornando sia la lista in memoria
     * che la lista per il salvataggio JSON.
     * 
     * @param track    L'oggetto {@link Track} da aggiungere.
     * @param position La posizione all'interno della playlist in cui inserire la traccia.
     */
    public void addTrackAtPosition(Track track, int position) {

        if (track == null || trackIDs.contains(track.getId()))
            return;

        int idx = Math.min(position, trackIDs.size());

        trackIDs.add(idx, track.getId());

        tracksList.add(idx, track);

    }

    /**
     * Rimuove una traccia dalla playlist, aggiornando sia la lista in memoria che la lista per il salvataggio JSON.
     * 
     * @param track L'oggetto {@link Track} da rimuovere.
     */
    public void removeTrack(Track track) {
        if (track != null) {
            trackIDs.remove(track.getId());
            tracksList.remove(track);
        }
    }

    /**
     * Sposta una traccia da una posizione a un'altra all'interno della playlist.
     * Mantiene sincronizzate sia la lista degli UUID usata per la persistenza,
     * sia la lista degli oggetti Track usata a runtime.
     *
     * @param fromIndex Indice iniziale (di origine) della traccia.
     * @param toIndex   Indice finale (di destinazione) della traccia.
     */
    public void moveTrack(int fromIndex, int toIndex) {

        if (trackIDs == null || tracksList == null) {
            return;
        }

        if (fromIndex < 0
                || fromIndex >= trackIDs.size()
                || toIndex < 0
                || toIndex >= trackIDs.size()
                || fromIndex == toIndex) {
            return;
        }

        UUID movedTrackId = trackIDs.remove(fromIndex);
        trackIDs.add(toIndex, movedTrackId);

        Track movedTrack = tracksList.remove(fromIndex);
        tracksList.add(toIndex, movedTrack);
    }

    /**
     * Ritorna la lista degli oggetti Track reali contenuti nella playlist.
     * Questa lista è popolata in fase di caricamento dal PersistenceService.
     * 
     * @return Una lista non modificabile degli oggetti {@link Track} presenti nella playlist.
     */
    public List<Track> getTracksList() {
        return Collections.unmodifiableList(tracksList);
    }

    /**
     * Risolve gli UUID caricati dal JSON e popola la lista degli oggetti Track in memoria.
     * Da chiamare all'avvio dopo il caricamento dal PersistenceService.
     * 
     * @param allTracks La lista di tutte le tracce presenti nel sistema da cui attingere per la risoluzione.
     */
    public void resolveTracks(List<Track> allTracks) {
        this.tracksList = new ArrayList<>();
        if (this.trackIDs != null && allTracks != null) {
            for (UUID id : this.trackIDs) {
                allTracks.stream()
                        .filter(t -> t.getId().equals(id))
                        .findFirst()
                        .ifPresent(this.tracksList::add);
            }
        }
    }

    /**
     * Sostituisce i brani attuali della playlist con una nuova lista.
     * Utilizzato per la sovrascrittura ed il ripristino con l'Undo.
     * 
     * @param newTracks La nuova lista di oggetti {@link Track} da impostare.
     */
    public void replaceTracks(List<Track> newTracks) {
        this.trackIDs.clear();
        if (this.tracksList == null) {
            this.tracksList = new ArrayList<>();
        } else {
            this.tracksList.clear();
        }
        if (newTracks != null) {
            for (Track t : newTracks) {
                this.addTrack(t);
            }
        }
    }

    /**
     * Verifica l'uguaglianza tra questa playlist ed un altro oggetto.
     * Due playlist sono considerate uguali se hanno lo stesso nome (case-insensitive).
     * 
     * @param o L'oggetto da confrontare.
     * @return true se gli oggetti sono uguali, false altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist other = (Playlist) o;
        return name != null && name.equalsIgnoreCase(other.name);
    }

    /**
     * Restituisce l'hashcode basato sul nome della playlist.
     * 
     * @return L'hashcode calcolato.
     */
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * Restituisce il conteggio delle riproduzioni della playlist.
     * 
     * @return Il numero di volte in cui la playlist è stata riprodotta.
     */
    public int getPlayCount() {
        return playCount;
    }

    /**
     * Incrementa di uno il conteggio delle riproduzioni della playlist.
     */
    public void incrementPlayCount() {
        this.playCount++;
    }
}
