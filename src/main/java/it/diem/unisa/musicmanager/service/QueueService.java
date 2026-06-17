package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.exception.QueueException;
import it.diem.unisa.musicmanager.model.*;
import it.diem.unisa.musicmanager.playmode.PlayMode;
import it.diem.unisa.musicmanager.playmode.SequentialMode;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Servizio per la gestione e la manipolazione della coda di riproduzione.
 * Gestisce l'aggiunta di brani singoli o intere playlist, gestisce la navigazione della coda
 * in base alla modalità di riproduzione corrente ({@link PlayMode}), e assicura la sincronizzazione
 * automatica della coda quando le playlist in archivio vengono modificate (es. tracce aggiunte, rimosse o riordinate).
 * Implementa l'interfaccia {@link TrackObserver} per rimuovere automaticamente le tracce eliminate dal sistema.
 */
public class QueueService implements TrackObserver {

    /**
     * Lo stato globale condiviso dell'applicazione contenente la coda osservabile.
     */
    private final SharedState sharedState;

    /**
     * Proprietà osservabile che rappresenta l'elemento della coda attualmente in riproduzione.
     */
    private final ObjectProperty<QueueItem> currentItem = new SimpleObjectProperty<>(null);

    /**
     * La modalità di riproduzione attiva (es. sequenziale, shuffle).
     */
    private PlayMode playMode;

    /**
     * Costruisce un nuovo {@code QueueService} associandovi lo stato condiviso.
     * Inizializza la modalità di riproduzione di default in modalità sequenziale ({@link SequentialMode}).
     *
     * @param sharedState Lo stato condiviso globale dell'applicazione.
     */
    public QueueService(SharedState sharedState) {
        this.sharedState = sharedState;
        playMode = new SequentialMode(); //DEFAULT
    }

    /**
     * Restituisce l'elemento della coda attualmente in riproduzione.
     *
     * @return L'oggetto {@link QueueItem} corrente, o {@code null} se nessun elemento è in riproduzione.
     */
    public QueueItem getCurrentItem() {
        return currentItem.get();
    }

    /**
     * Imposta l'elemento corrente della coda.
     *
     * @param queueItem Il nuovo elemento {@link QueueItem} da impostare come corrente.
     */
    public void setCurrentItem(QueueItem queueItem) {
        currentItem.set(queueItem);
    }

    /**
     * Restituisce la lista osservabile degli elementi attualmente in coda.
     *
     * @return La lista osservabile {@link ObservableList} di {@link QueueItem}.
     */
    public ObservableList<QueueItem> getQueueList() {
        return sharedState.getQueue();
    }

    /**
     * Imposta la modalità di riproduzione corrente (es. sequenziale, shuffle).
     *
     * @param playMode La nuova modalità {@link PlayMode} da applicare.
     */
    public void setCurrentPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }

    /**
     * Riordina le tracce future (non ancora riprodotte) all'interno del gruppo playlist attivo.
     *
     * @param queue        La coda degli elementi.
     * @param playlist     La playlist da cui attingere l'ordine aggiornato delle tracce.
     * @param groupId      L'identificativo del gruppo playlist in coda.
     * @param currentIndex L'indice dell'elemento attualmente in riproduzione.
     */
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

    /**
     * Riordina tutti gli elementi appartenenti ad un intero gruppo playlist in coda.
     *
     * @param queue    La coda degli elementi.
     * @param playlist La playlist da cui attingere l'ordine aggiornato delle tracce.
     * @param groupId  L'identificativo del gruppo playlist in coda.
     */
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

    /**
     * Ordina una lista di elementi della coda basandosi sull'ordine reale delle tracce nella playlist specificata.
     *
     * @param items    La lista degli elementi da ordinare.
     * @param playlist La playlist di riferimento.
     */
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
     * Aggiunge un oggetto riproducibile (una singola traccia o una playlist intera) in coda.
     * Se l'oggetto è una playlist, viene spacchettata in singoli elementi `QueueItem` per ciascuna traccia,
     * mantenendo un riferimento all'ID della playlist d'origine e a un ID progressivo di gruppo.
     *
     * @param playable L'oggetto {@link Playable} da inserire in coda.
     * @return La lista di {@link QueueItem} generati e inseriti in coda.
     */
    public List<QueueItem> addToQueue(Playable playable) {

        if (playable == null) return null;

        List<QueueItem> queue = new ArrayList<>();
        UUID belongsToPlaylist = null; //setto a null perché non so ancora se l'oggetto è playlist o traccia
        UUID playlistProgressive = null;
        if (playable.getType() == QueueItemType.PLAYLIST) {
            //la traccia appartiene a una playlist per cui bisogna cambiare il belong to
            belongsToPlaylist = playable.getId();
            List<Track> trackOfPlaylist = playable.getTracksToPlay();
            playlistProgressive = UUID.randomUUID();
            for (Track track : trackOfPlaylist) {
                //per ogni traccia devo creare un queue item
                QueueItem queueItem = new QueueItem(track, belongsToPlaylist, playlistProgressive);
                queue.add(queueItem);
                sharedState.getQueue().add(queueItem);

            }
        } else {

            //convertire l'oggetto in un queueitem
            QueueItem queueItem = new QueueItem(playable, belongsToPlaylist, playlistProgressive);
            queue.add(queueItem);
            sharedState.getQueue().add(queueItem);
        }
        return queue;
    }

    /**
     * Restituisce la proprietà di sola lettura per l'elemento corrente della coda.
     *
     * @return La proprietà {@link ReadOnlyObjectProperty} per l'elemento corrente.
     */
    public ReadOnlyObjectProperty<QueueItem> currentItemProperty() {
        return currentItem;
    }

    /**
     * Verifica se esiste un elemento successivo nella coda in base alla modalità di riproduzione.
     *
     * @return true se è presente un elemento successivo, false altrimenti.
     */
    public boolean hasNext() {
        return playMode.hasNext(getQueueList(), getCurrentItem());
    }

    /**
     * Svuota la coda di riproduzione e resetta l'elemento corrente a null.
     * Da utilizzare all'inizio di una nuova sessione di ascolto.
     */
    public void clearQueue() {
        sharedState.getQueue().clear();
        setCurrentItem(null);
    }

    /**
     * Restituisce e imposta come corrente il prossimo elemento della coda,
     * calcolato in base alla modalità di riproduzione ({@link PlayMode}) attiva.
     *
     * @return Il prossimo {@link QueueItem} impostato come corrente.
     * @throws QueueException Se la coda è vuota.
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

    /**
     * Salta l'intera playlist dell'elemento correntemente in riproduzione.
     * Rimuove tutti gli elementi della stessa playlist associati a quel gruppo progressivo
     * e avanza al prossimo elemento in coda.
     *
     * @return Il prossimo {@link QueueItem} da riprodurre, oppure {@code null} se la coda è vuota.
     */
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

    /**
     * Avvia la riproduzione di una playlist partendo da una specifica traccia al suo interno.
     * Svuota la coda, inserisce la playlist e scorre gli elementi finché non raggiunge la traccia cliccata.
     *
     * @param playable        La playlist da mettere in coda.
     * @param trackInPlaylist La traccia all'interno della playlist da cui far partire la riproduzione.
     * @return L'elemento {@link QueueItem} corrispondente alla traccia da cui avviare la riproduzione.
     */
    public QueueItem queuePlaylistFromTrack(Playable playable, Track trackInPlaylist) {
        if (playable == null || trackInPlaylist == null) return null;

        clearQueue();
        addToQueue(playable);

        QueueItem queueItem = nextItem();
        //dobbiamo scartare le canzoni fino a quella su cui abbiamo cliccato play
        while (queueItem != null && !trackInPlaylist.getId().equals(queueItem.getPlayable().getId())) {
            queueItem = nextItem();
        }

        return queueItem;

    }

    /**
     * Gestisce la cancellazione di una traccia dal sistema.
     * Rimuove tutte le occorrenze della traccia dalla coda e resetta l'elemento corrente
     * se corrispondeva alla traccia eliminata.
     *
     * @param trackId L'ID della traccia eliminata.
     */
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

    /**
     * Restituisce il prossimo elemento in coda senza far avanzare il cursore di riproduzione.
     * Utilizzato esclusivamente per scopi di sola visualizzazione (es. mostrare il brano successivo nella GUI).
     *
     * @return Il prossimo {@link QueueItem} calcolato, o {@code null} se non vi sono altri elementi.
     */
    public QueueItem peekNext() {
        List<QueueItem> dummyQueue = new ArrayList<>(getQueueList());
        return playMode.nextItem(
                dummyQueue,
                getCurrentItem()
        ).orElse(null);
    }

    /**
     * Sincronizza la coda quando una playlist viene riordinata all'interno dell'applicazione.
     * Mantiene fermo il brano corrente in riproduzione e riordina i brani successivi,
     * mentre riordina completamente gli altri gruppi in coda associati alla stessa playlist.
     *
     * @param playlist La playlist che è stata riordinata.
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

    /**
     * Sposta un elemento all'interno della coda di riproduzione in una nuova posizione specificata.
     * Modifica direttamente la lista osservabile in modo da notificare automaticamente le viste della GUI.
     * Il brano correntemente in riproduzione non può essere spostato.
     *
     * @param item     L'elemento {@link QueueItem} da spostare.
     * @param newIndex L'indice di destinazione nella coda.
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
     * Questo metodo è particolarmente utile per il drag and drop della ListView della coda.
     *
     * @param draggedItem L'elemento trascinato.
     * @param targetItem  L'elemento sul quale viene rilasciato.
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
     * Sincronizza la coda quando una traccia viene aggiunta a una playlist nel sistema.
     * Cerca ogni gruppo in coda che appartiene a tale playlist e vi inserisce la traccia
     * subito dopo l'ultimo elemento di quel gruppo.
     *
     * @param playlistId L'identificatore della playlist modificata.
     * @param track      La traccia aggiunta alla playlist.
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
     * Sincronizza la coda quando una traccia viene rimossa da una playlist nel sistema.
     * Rimuove tutte le occorrenze in coda della traccia specificata appartenenti a tale playlist.
     * Se la traccia rimossa era quella attualmente in riproduzione, azzera l'elemento corrente.
     *
     * @param playlistId L'identificatore della playlist modificata.
     * @param trackId    L'identificatore della traccia rimossa dalla playlist.
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
