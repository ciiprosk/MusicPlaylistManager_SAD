package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.model.Playable;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.QueueItemType;
import it.diem.unisa.musicmanager.state.SharedState;

import java.util.UUID;

/**
 * Service per la gestione della coda dei brani.
 * Nella coda possono essere inserite sia le singole tracce sia intere playlist.
 *
 */
public class QueueService {
    //deve prendere una track e impacchettarli in unqueue item
    private final SharedState sharedState;

    public QueueService(SharedState sharedState) {
        this.sharedState = sharedState;
    }

    public void addToQueue(Playable playable){
        if(playable == null) return;

        int nextIndex = sharedState.getQueue().size();
        UUID belongsToPlaylist = null; //setto a null perché non so ancora se l'oggetto è playlist o traccia

        if(playable.getType() == QueueItemType.PLAYLIST){
            //la traccia apparteiene a una playlist per cui bisgna cambaire il belong tp
            belongsToPlaylist = playable.getID();
        }

        //convertire l'oggetto in un queueitem
        QueueItem queueItem = new QueueItem(playable, nextIndex, belongsToPlaylist);
        sharedState.getQueue().add(queueItem);
    }

}
