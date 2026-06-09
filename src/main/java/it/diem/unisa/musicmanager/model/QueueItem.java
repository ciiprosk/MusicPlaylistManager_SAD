package it.diem.unisa.musicmanager.model;

import java.util.UUID;

/**
 * La classe QueueItem rappresenta un elemento della coda di riproduzione.
 * Ogni traccia può essere o associata a una playlist oppure non appartenerne a nessuna.
 *
 */
public class QueueItem {
    private final Playable playable;
    private int queueIndex;
    private final UUID belongsToPlaylist;


    public QueueItem(Playable playable, int queueIndex, UUID belongsToPlaylist) {
        this.playable = playable;
        this.queueIndex = queueIndex;
        this.belongsToPlaylist = belongsToPlaylist;
    }

    public Playable getPlayable() {
        return playable;
    }
    public int getQueueIndex() {
        return queueIndex;
    }
    public UUID getBelongsToPlaylist() {
        return belongsToPlaylist;
    }

    public void setQueueIndex(int queueIndex) {
        this.queueIndex = queueIndex;
    }

    public boolean isPlaylist(){
        if(playable.getType() != QueueItemType.PLAYLIST) return false;
        return true;
    }

    public boolean isTrack(){
        if(playable.getType() != QueueItemType.TRACK) return false;
        return true;
    }
}
