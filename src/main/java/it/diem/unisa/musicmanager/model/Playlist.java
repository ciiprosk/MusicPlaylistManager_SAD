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
public class Playlist implements Playable{

    private String name;
    private UUID id;
    
    /** Lista degli UUID delle tracce per il salvataggio nel file JSON. */
    private List<UUID> trackIDs;
    
    /** Lista in memoria degli oggetti Track. Non viene serializzata nel JSON. */
    private transient List<Track> tracksList;
    private int playCount;

    /**
     * Costruttore della playlist.
     * @param name il nome della playlist.
     */
    public Playlist(String name) {
        if (checkRulesName(name.trim())) this.name = name.trim();
        this.id = UUID.randomUUID();
        this.trackIDs = new ArrayList<>();
        this.tracksList = new ArrayList<>();
        this.playCount = 0;
    }

    /**
     * Costruttore della playlist utilizzato per il recupero di una playlist dal file JSON.
     * @param id l'identificatore univoco della playlist.
     * @param name il nome della playlist.
     * @param trackIDs una lista di tracce identificate dai UUID.
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
     * @param playlistID l'identificatore univoco della playlist.
     * @param name il nuovo nome della playlist.
     */
    public Playlist(UUID playlistID, String name) {
        if (checkRulesName(name.trim())) this.name = name.trim();
        this.id = playlistID;
        this.trackIDs = new ArrayList<>();
        this.tracksList = new ArrayList<>();
        this.playCount = 0;
    }


    /**
     * @return
     */
    @Override
    public List<Track> getTracksToPlay() {
        return tracksList;
    }

    /**
     * @return
     */
    @Override
    public QueueItemType getType() {
        return QueueItemType.PLAYLIST;
    }

    /**
     * Getter per l'identificatore univoco della playlist.
     * @return l'identificatore univoco della playlist.
     */
    @Override
    public UUID getId() {
        return id;
    }
    public String getName(){
        return this.name;
    }
    /**
     * Setter per il nome della playlist.
     * @param name il nuovo nome della playlist.
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
     * @param trackID l'identificatore univoco della traccia da aggiungere.
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
     * @param trackID l'identificatore univoco della traccia da rimuovere.
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
     * Metodo che ritorna una copia della lista degli UUID delle tracce presenti.
     * @return una lista non modificabile di UUID delle tracce.
     */
    public List<UUID> getTracks() {
        return Collections.unmodifiableList(trackIDs);
    }

    /**
     * Verifica se la playlist contiene una determinata traccia tramite il suo ID.
     * @param trackID l'identificatore univoco della traccia da cercare.
     * @return true se la playlist contiene la traccia, false altrimenti.
     */
    public boolean containsTrack(UUID trackID){
        return trackIDs.contains(trackID);
    }

    /**
     * Ritorna il numero di tracce contenute nella playlist.
     * @return il numero di tracce presenti.
     */
    public int numberOfTracks(){
        return trackIDs.size();
    }

    /**
     * Valida le business rules per il nome della playlist.
     * Si assicura che il nome non sia vuoto e che non superi i 50 caratteri.
     *
     * @param name il nome della playlist da verificare
     * @return true se il nome soddisfa le business rules.
     * @throws PlaylistInfoException se il nome non soddisfa le business rules.
     */
    private boolean checkRulesName(String name) throws PlaylistInfoException {
        if(name == null || name.isEmpty()) throw new PlaylistInfoException("The name cannot be empty");
        if(name.length() > 50) throw new PlaylistInfoException("The name cannot be longer than 50 characters");
        return true;
    }

    /**
     * Aggiunge una traccia alla playlist, aggiornando sia la lista in memoria che la lista per il salvataggio JSON.
     * @param track l'oggetto Track da aggiungere.
     */
    public void addTrack(Track track) {
        if (track != null && !trackIDs.contains(track.getId())) {
            trackIDs.add(track.getId());
            tracksList.add(track);
        }
    }

    /**
     * Rimuove una traccia dalla playlist, aggiornando sia la lista in memoria che la lista per il salvataggio JSON.
     * @param track l'oggetto Track da rimuovere.
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
     * @param fromIndex indice iniziale della traccia
     * @param toIndex indice finale della traccia
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
     * @return una lista non modificabile degli oggetti Track presenti nella playlist.
     */
    public List<Track> getTracksList() {
        return Collections.unmodifiableList(tracksList);
    }

    /**
     * Risolve gli UUID caricati dal JSON e popola la lista degli oggetti Track in memoria.
     * Da chiamare all'avvio dopo il caricamento dal PersistenceService.
     * @param allTracks la lista di tutte le tracce presenti nel sistema.
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist other = (Playlist) o;
        return name != null && name.equalsIgnoreCase(other.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void incrementPlayCount() {
        this.playCount++;
    }
}
