package it.diem.unisa.musicmanager.model;

import java.util.UUID;

/**
 * La classe QueueItem rappresenta un elemento della coda di riproduzione.
 * Ogni traccia può essere o associata a una playlist oppure non appartenerne a nessuna.
 *
 */
public class QueueItem {
    private final Playable playable;
    private final UUID belongsToPlaylist;
    private UUID playlistProgressive;


    public QueueItem(Playable playable, UUID belongsToPlaylist, UUID playlistProgressive) {
        this.playable = playable;

        this.belongsToPlaylist = belongsToPlaylist;
        this.playlistProgressive = playlistProgressive;
    }

    public Playable getPlayable() {
        return playable;
    }
    public UUID getBelongsToPlaylist() {
        return belongsToPlaylist;
    }
    public UUID getPlaylistProgressive() { return playlistProgressive;}
    public boolean isPlaylist(){
        if(playable.getType() != QueueItemType.PLAYLIST) return false;
        return true;
    }

    public boolean isTrack(){
        if(playable.getType() != QueueItemType.TRACK) return false;
        return true;
    }
}
