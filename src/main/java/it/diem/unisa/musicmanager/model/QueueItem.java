package it.diem.unisa.musicmanager.model;

import java.util.UUID;

/**
 * Rappresenta un singolo elemento all'interno della coda di riproduzione.
 * Può racchiudere un oggetto {@link Playable} (una traccia o una playlist)
 * e tiene traccia dell'eventuale playlist di appartenenza e del suo identificativo progressivo.
 */
public class QueueItem {

    /**
     * L'oggetto riproducibile associato a questo elemento della coda (può essere una Track o Playlist).
     */
    private final Playable playable;

    /**
     * L'identificatore univoco (UUID) della playlist a cui appartiene l'elemento (se applicabile).
     */
    private final UUID belongsToPlaylist;

    /**
     * L'identificatore univoco progressivo dell'elemento all'interno della coda/playlist.
     */
    private UUID playlistProgressive;

    /**
     * Costruttore della classe QueueItem.
     * 
     * @param playable            La traccia o playlist associata.
     * @param belongsToPlaylist   L'identificativo della playlist a cui appartiene.
     * @param playlistProgressive L'identificativo progressivo dell'elemento.
     */
    public QueueItem(Playable playable, UUID belongsToPlaylist, UUID playlistProgressive) {
        this.playable = playable;
        this.belongsToPlaylist = belongsToPlaylist;
        this.playlistProgressive = playlistProgressive;
    }

    /**
     * Restituisce l'oggetto {@link Playable} (traccia o playlist) racchiuso in questo elemento.
     * 
     * @return L'oggetto Playable.
     */
    public Playable getPlayable() {
        return playable;
    }

    /**
     * Restituisce l'identificatore della playlist a cui appartiene l'elemento.
     * 
     * @return L'UUID della playlist di appartenenza.
     */
    public UUID getBelongsToPlaylist() {
        return belongsToPlaylist;
    }

    /**
     * Restituisce l'identificatore progressivo dell'elemento.
     * 
     * @return L'UUID del progressivo.
     */
    public UUID getPlaylistProgressive() { 
        return playlistProgressive;
    }

    /**
     * Verifica se l'elemento racchiuso corrisponde ad una playlist.
     * 
     * @return true se l'elemento è di tipo PLAYLIST, false altrimenti.
     */
    public boolean isPlaylist(){
        if(playable.getType() != QueueItemType.PLAYLIST) return false;
        return true;
    }

    /**
     * Verifica se l'elemento racchiuso corrisponde ad una singola traccia.
     * 
     * @return true se l'elemento è di tipo TRACK, false altrimenti.
     */
    public boolean isTrack(){
        if(playable.getType() != QueueItemType.TRACK) return false;
        return true;
    }
}
