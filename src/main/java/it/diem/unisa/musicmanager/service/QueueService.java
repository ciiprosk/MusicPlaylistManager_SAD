package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.exception.QueueException;
import it.diem.unisa.musicmanager.model.*;
import it.diem.unisa.musicmanager.playmode.PlayMode;
import it.diem.unisa.musicmanager.playmode.SequentialMode;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.*;

/**
 * Service per la gestione della coda dei brani.
 * Nella coda possono essere inserite sia le singole tracce sia intere playlist.
 *
 */
public class QueueService implements TrackObserver {
    //deve prendere una track e impacchettarli in unqueue item
    private final SharedState sharedState;
    private final ObjectProperty<QueueItem> currentItem =
            new SimpleObjectProperty<>(null);
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
    public void setCurrentItem(QueueItem queueItem) {
        currentItem.set(queueItem);
    }

    public QueueItem getCurrentItem() {
        return currentItem.get();
    }

    public ReadOnlyObjectProperty<QueueItem> currentItemProperty() {
        return currentItem;
    }

    public javafx.collections.ObservableList<QueueItem> getQueueList() {
        return sharedState.getQueue();
    }

    public boolean hasNext() {
        return playMode.hasNext(getQueueList(), getCurrentItem());
    }

    /**
     * Svuota la coda e resetta l'item corrente.
     * Da usare ogni volta che si inizia una nuova sessione di ascolto
     * (es. Play su una playlist nuova, Play su una traccia singola fuori coda).
     */
    public void clearQueue() {
        sharedState.getQueue().clear();
        setCurrentItem(null);
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
    public QueueItem nextItem() {
        Optional<QueueItem> optionalItem =
                playMode.nextItem(
                        sharedState.getQueue(),
                        getCurrentItem()
                );

        if (optionalItem.isEmpty()) {
            throw new QueueException("The Queue is Empty.");
        }

        setCurrentItem(optionalItem.get());

        return getCurrentItem();
    }



    public QueueItem skipCurrentPlaylist() {
        QueueItem current = getCurrentItem();

        if (current == null || current.getBelongsToPlaylist() == null) {
            return nextItem();
        }

        UUID playlistID = current.getBelongsToPlaylist();
        UUID groupID = current.getPlaylistProgressive();

        sharedState.getQueue().removeIf(item ->
                groupID != null
                        && groupID.equals(item.getPlaylistProgressive())
                        && playlistID.equals(item.getBelongsToPlaylist())
        );

        setCurrentItem(null);

        if (sharedState.getQueue().isEmpty()) {
            return null;
        }

        return nextItem();
    }

    //il metodo dev evedere se la tracci asu cui è stato fatto play appartiene a una playlist (sono in detailedPlaylist)
    // se appartiene metto le tracce in coda rimanenti
    public QueueItem queuePlaylistFromTrack(Playable playable, Track trackInPlaylist){
        if(playable == null || trackInPlaylist == null) return null;

        //getQueueList().clear(); //ripulisce la coda
        clearQueue();
        addToQueue(playable); // metto turtta la playlst in coda ma devo togliere le precedenti a wurrlla che tsa andando play

        QueueItem queueItem = nextItem();
        //dobbiamo scartare le alee canzoni fio a quellla su ci abbiamo cliccato play
        while(queueItem != null && !trackInPlaylist.getId().equals(queueItem.getPlayable().getId())){
            queueItem = nextItem();
        }

        //restistuisce l'elemento trovatooo
        return queueItem;

    }

    @Override
    public void onTrackDeleted(UUID trackId) {
        if (trackId == null) {
            return;
        }

        sharedState.getQueue().removeIf(queueItem ->
                queueItem.getPlayable() instanceof Track track
                        && track.getId().equals(trackId)
        );

        QueueItem current = getCurrentItem();

        if (current != null
                && current.getPlayable() instanceof Track track
                && track.getId().equals(trackId)) {
            setCurrentItem(null);
        }
    }

    /** Restituisce il prossimo item SENZA avanzare il cursore. Per la sola visualizzazione. */
    public QueueItem peekNext() {
        return playMode.nextItem(
                getQueueList(),
                getCurrentItem()
        ).orElse(null);
    }

    /**
     * Aggiorna nella coda le occorrenze di una playlist riordinata.
     *
     * Se la playlist è già in riproduzione, il brano corrente resta fermo
     * e vengono riordinati solamente i brani successivi.
     *
     * Le altre occorrenze della playlist vengono riordinate completamente.
     *
     * @param playlist playlist appena riordinata
     */
    public void synchronizePlaylistOrder(Playlist playlist) {

        if (playlist == null || playlist.getId() == null) {
            return;
        }

        javafx.collections.ObservableList<QueueItem> queue =
                sharedState.getQueue();

        if (queue.isEmpty()) {
            return;
        }

        QueueItem activeItem = getCurrentItem();

        UUID activeGroupId = null;
        int activeItemIndex = -1;

        if (activeItem != null
                && playlist.getId().equals(activeItem.getBelongsToPlaylist())) {

            activeGroupId = activeItem.getPlaylistProgressive();
            activeItemIndex = queue.indexOf(activeItem);
        }

        Set<UUID> playlistGroups = new LinkedHashSet<>();

        for (QueueItem item : queue) {
            if (playlist.getId().equals(item.getBelongsToPlaylist())
                    && item.getPlaylistProgressive() != null) {

                playlistGroups.add(item.getPlaylistProgressive());
            }
        }

        for (UUID groupId : playlistGroups) {

            boolean isActiveGroup =
                    activeGroupId != null
                            && activeGroupId.equals(groupId)
                            && activeItemIndex >= 0;

            if (isActiveGroup) {
                reorderFutureItemsOfActiveGroup(
                        queue,
                        playlist,
                        groupId,
                        activeItemIndex
                );
            } else {
                reorderEntirePlaylistGroup(
                        queue,
                        playlist,
                        groupId
                );
            }
        }
    }

    private void reorderFutureItemsOfActiveGroup(
            javafx.collections.ObservableList<QueueItem> queue,
            Playlist playlist,
            UUID groupId,
            int currentIndex
    ) {
        List<Integer> futureIndexes = new ArrayList<>();
        List<QueueItem> futureItems = new ArrayList<>();

        for (int index = currentIndex + 1; index < queue.size(); index++) {
            QueueItem item = queue.get(index);

            if (playlist.getId().equals(item.getBelongsToPlaylist())
                    && groupId.equals(item.getPlaylistProgressive())) {

                futureIndexes.add(index);
                futureItems.add(item);
            }
        }

        sortItemsByPlaylistOrder(futureItems, playlist);

        for (int index = 0; index < futureIndexes.size(); index++) {
            queue.set(
                    futureIndexes.get(index),
                    futureItems.get(index)
            );
        }
    }

    private void reorderEntirePlaylistGroup(
            javafx.collections.ObservableList<QueueItem> queue,
            Playlist playlist,
            UUID groupId
    ) {
        List<Integer> groupIndexes = new ArrayList<>();
        List<QueueItem> groupItems = new ArrayList<>();

        for (int index = 0; index < queue.size(); index++) {
            QueueItem item = queue.get(index);

            if (playlist.getId().equals(item.getBelongsToPlaylist())
                    && groupId.equals(item.getPlaylistProgressive())) {

                groupIndexes.add(index);
                groupItems.add(item);
            }
        }

        sortItemsByPlaylistOrder(groupItems, playlist);

        for (int index = 0; index < groupIndexes.size(); index++) {
            queue.set(
                    groupIndexes.get(index),
                    groupItems.get(index)
            );
        }
    }

    private void sortItemsByPlaylistOrder(
            List<QueueItem> items,
            Playlist playlist
    ) {
        items.sort(Comparator.comparingInt(item -> {
            UUID trackId = item.getPlayable().getId();

            int playlistIndex =
                    playlist.getTracks().indexOf(trackId);

            return playlistIndex >= 0
                    ? playlistIndex
                    : Integer.MAX_VALUE;
        }));
    }

    /**
     * Sposta un elemento della coda in una nuova posizione.
     *
     * Il metodo modifica direttamente la ObservableList contenuta nello SharedState,
     * quindi tutte le viste collegate alla coda vengono notificate automaticamente.
     *
     * @param item elemento della coda da spostare
     * @param newIndex nuova posizione dell'elemento
     */
    public void moveQueueItem(QueueItem item, int newIndex) {

        if (item == null) {
            return;
        }

        javafx.collections.ObservableList<QueueItem> queue =
                sharedState.getQueue();

        int oldIndex = queue.indexOf(item);

        if (oldIndex < 0 || queue.isEmpty()) {
            return;
        }

        /*
         * Il brano attualmente in riproduzione non deve essere spostato.
         * Nella schermata "Next Tracks" normalmente non è visibile,
         * ma questo controllo evita modifiche accidentali.
         */
        if (item == getCurrentItem()) {
            return;
        }

        int safeIndex = Math.max(
                0,
                Math.min(newIndex, queue.size() - 1)
        );

        if (oldIndex == safeIndex) {
            return;
        }

        queue.remove(oldIndex);

        /*
         * Dopo la rimozione la dimensione diminuisce di uno.
         * L'indice massimo valido per l'inserimento coincide con queue.size().
         */
        safeIndex = Math.max(
                0,
                Math.min(safeIndex, queue.size())
        );

        queue.add(safeIndex, item);
    }

    /**
     * Sposta un elemento della coda nella posizione occupata da un altro elemento.
     * Questo metodo è particolarmente utile per il drag and drop della ListView.
     *
     * @param draggedItem elemento trascinato
     * @param targetItem elemento sul quale viene rilasciato
     */
    public void moveQueueItem(QueueItem draggedItem, QueueItem targetItem) {

        if (draggedItem == null
                || targetItem == null
                || draggedItem == targetItem) {
            return;
        }

        int targetIndex =
                sharedState.getQueue().indexOf(targetItem);

        if (targetIndex < 0) {
            return;
        }

        moveQueueItem(draggedItem, targetIndex);
    }

    /**
     * Sincronizza la coda quando una traccia viene aggiunta a una playlist.
     * Cerca ogni gruppo in coda che appartiene a tale playlist e inserisce la traccia
     * subito dopo l'ultimo elemento di quel gruppo.
     *
     * @param playlistId identificatore della playlist modificata
     * @param track traccia aggiunta
     */
    public void synchronizeTrackAdded(UUID playlistId, Track track) {
        if (playlistId == null || track == null) {
            return;
        }

        javafx.collections.ObservableList<QueueItem> queue = sharedState.getQueue();
        if (queue.isEmpty()) {
            return;
        }

        Set<UUID> groupIds = new LinkedHashSet<>();
        for (QueueItem item : queue) {
            if (playlistId.equals(item.getBelongsToPlaylist()) && item.getPlaylistProgressive() != null) {
                groupIds.add(item.getPlaylistProgressive());
            }
        }

        for (UUID groupId : groupIds) {
            int lastIndex = -1;
            for (int i = 0; i < queue.size(); i++) {
                QueueItem item = queue.get(i);
                if (playlistId.equals(item.getBelongsToPlaylist()) && groupId.equals(item.getPlaylistProgressive())) {
                    lastIndex = i;
                }
            }
            if (lastIndex != -1) {
                QueueItem newItem = new QueueItem(track, playlistId, groupId);
                queue.add(lastIndex + 1, newItem);
            }
        }
    }

    /**
     * Sincronizza la coda quando una traccia viene rimossa da una playlist.
     * Rimuove tutte le occorrenze di quella traccia in coda appartenenti a tale playlist.
     * Se la traccia rimossa era quella attualmente in riproduzione, azzera l'item corrente.
     *
     * @param playlistId identificatore della playlist modificata
     * @param trackId identificatore della traccia rimossa
     */
    public void synchronizeTrackRemoved(UUID playlistId, UUID trackId) {
        if (playlistId == null || trackId == null) {
            return;
        }

        javafx.collections.ObservableList<QueueItem> queue = sharedState.getQueue();
        if (queue.isEmpty()) {
            return;
        }

        QueueItem current = getCurrentItem();
        boolean currentRemoved = false;

        List<QueueItem> toRemove = new ArrayList<>();
        for (QueueItem item : queue) {
            if (playlistId.equals(item.getBelongsToPlaylist())
                    && item.getPlayable() instanceof Track track
                    && track.getId().equals(trackId)) {
                toRemove.add(item);
                if (item == current) {
                    currentRemoved = true;
                }
            }
        }

        if (!toRemove.isEmpty()) {
            queue.removeAll(toRemove);
            if (currentRemoved) {
                setCurrentItem(null);
            }
        }
    }
}

