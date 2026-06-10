package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.exception.QueueException;
import it.diem.unisa.musicmanager.model.*;
import it.diem.unisa.musicmanager.playmode.PlayMode;
import it.diem.unisa.musicmanager.playmode.SequentialMode;
import it.diem.unisa.musicmanager.state.SharedState;

import java.util.*;

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
        UUID playlistProgressive = null;
        if(playable.getType() == QueueItemType.PLAYLIST){
            //la traccia apparteiene a una playlist per cui bisgna cambaire il belong tp
            belongsToPlaylist = playable.getId();
            List<Track> trackOfPlaylist = playable.getTracksToPlay();
            playlistProgressive = UUID.randomUUID();
            for (Track track : trackOfPlaylist) {
                //per ogni traccia devo creare un queue item
                QueueItem queueItem = new QueueItem(track, belongsToPlaylist, playlistProgressive);
                queue.add(queueItem);
                sharedState.getQueue().add(queueItem);

            }
        }else {

            //convertire l'oggetto in un queueitem
            QueueItem queueItem = new QueueItem(playable, belongsToPlaylist, playlistProgressive);
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

    public javafx.collections.ObservableList<QueueItem> getQueueList() {
        return sharedState.getQueue();
    }

    public boolean hasNext() {
        List<QueueItem> queue = sharedState.getQueue();
        if (queue.isEmpty()) return false;
        int index = queue.indexOf(currentItem);
        if (index == -1) return true;       // perché indexOf ritorna -1 in caso currentItem non sia presente
        return index < queue.size() - 1;    // ci sono elementi dopo il corrente
    }

    /**
     * Svuota la coda e resetta l'item corrente.
     * Da usare ogni volta che si inizia una nuova sessione di ascolto
     * (es. Play su una playlist nuova, Play su una traccia singola fuori coda).
     */
    public void clearQueue() {
        sharedState.getQueue().clear();
        currentItem = null;
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

        Optional<QueueItem> optionalItem= playMode.nextItem(sharedState.getQueue(),currentItem );

        if (!optionalItem.isPresent())
            throw new QueueException("The Queue is Empty.");

        this.currentItem = optionalItem.get();

        return currentItem;
    }

    

    public QueueItem skipCurrentPlaylist(){
        //se l'oggetto corrente è null o non appartiene a una playlist allora prosegue con il prossimo item
        if (currentItem == null || currentItem.getBelongsToPlaylist() == null) return nextItem();
        //se sono qui vuol dire che l'oggetto corrente appartiene a una playlist

        UUID playlistID = currentItem.getBelongsToPlaylist();
        UUID groupPlaylistID = currentItem.getPlaylistProgressive();

        QueueItem next = nextItem();
        while(next != null && groupPlaylistID.equals(next.getPlaylistProgressive()) && groupPlaylistID !=null && playlistID.equals(next.getBelongsToPlaylist())){
            next = nextItem();
        }

        return next;
    }

    //il metodo dev evedere se la tracci asu cui è stato fatto play appartiene a una playlist (sono in detailedPlaylist)
    // se appartiene metto le tracce in coda rimanenti
    public QueueItem queuePlaylistFromTrack(Playable playable, Track trackInPlaylist){
        if(playable == null || trackInPlaylist == null) return null;

        getQueueList().clear(); //ripulisce la coda
        addToQueue(playable); // metto turtta la playlst in coda ma devo togliere le precedenti a wurrlla che tsa andando play

        QueueItem queueItem = nextItem();
        //dobbiamo scartare le alee canzoni fio a quellla su ci abbiamo cliccato play
        while(queueItem != null && !trackInPlaylist.getId().equals(queueItem.getPlayable().getId())){
            queueItem = nextItem();
        }

        //restistuisce l'elemento trovatooo
        return queueItem;

    }




}
