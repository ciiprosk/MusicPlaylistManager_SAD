package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.model.Playable;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.QueueItemType;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.playmode.PlayMode;
import it.diem.unisa.musicmanager.playmode.SequentialMode;
import it.diem.unisa.musicmanager.state.SharedState;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Service per la gestione della coda dei brani.
 * Nella coda possono essere inserite sia le singole tracce sia intere playlist.
 *
 */
public class QueueService {
    //deve prendere una track e impacchettarli in unqueue item
    private final SharedState sharedState;
    private QueueItem currentItem;
    private PlayMode playMode;

    public QueueService(SharedState sharedState) {
        this.sharedState = sharedState;
        playMode = new SequentialMode(); //DEFAULT
    }

    public List<QueueItem> addToQueue(Playable playable){

        if(playable == null) return null;

        List<QueueItem> queue = new ArrayList<>();
        UUID belongsToPlaylist = null; //setto a null perché non so ancora se l'oggetto è playlist o traccia

        if(playable.getType() == QueueItemType.PLAYLIST){
            //la traccia apparteiene a una playlist per cui bisgna cambaire il belong tp
            belongsToPlaylist = playable.getId();
            List<Track> trackOfPlaylist = playable.getTracksToPlay();
            for (Track track : trackOfPlaylist) {
                //per ogni traccia devo creare un queue item
                QueueItem queueItem = new QueueItem(track, belongsToPlaylist);
                queue.add(queueItem);
                sharedState.getQueue().add(queueItem);

            }
        }else {

            //convertire l'oggetto in un queueitem
            QueueItem queueItem = new QueueItem(playable, belongsToPlaylist);
            queue.add(queueItem);
            sharedState.getQueue().add(queueItem);
        }
        return queue;
    }

    //la classe deve imple,enatre la logica del singolo item della coda
    public void setCurrentItem(QueueItem queueItem){
        this.currentItem = queueItem;
    }

    public QueueItem getCurrentItem(){
        return currentItem;
    }

    public void setCurrentPlayMode(PlayMode playMode){
        this.playMode = playMode;
    }
    /**
     * Meotodo di utilità privata che restituisce il prossimo item della coda.
     * Usa l'interfaccia playmode per calcalare recupaerare il prossimo item calcolato
     * dall'interfaccia secodndo il metodo di riporduzione.
     * @return
     */
    public QueueItem nextItem(){
        //il metodo chiede allo strategy quale deve essere il porssimo item
        //perprima cosa gli passo allo startegy la mia coda attuale

        currentItem= playMode.nextItem(sharedState.getQueue(),currentItem );
        return currentItem;
    }





}
