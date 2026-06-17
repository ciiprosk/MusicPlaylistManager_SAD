package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.QueueService;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class QueueViewController {

    /**
     * Controller per la schermata della Coda di Riproduzione (queueView.fxml).
     * Gestisce la visualizzazione del brano attualmente in esecuzione e della lista dei brani successivi.
     * Implementa una logica avanzata tramite {@link FilteredList} per mostrare all'utente solo
     * gli elementi futuri (nascondendo quelli già riprodotti).
     * Fornisce inoltra la funzionalità di Drag and Drop per permettere all'utente di riordinare
     * manualmente i brani in coda.
     */

    @FXML
    private ListView<QueueItem> queueListView;
    @FXML
    private javafx.scene.control.Label labelCurrentTrack;

    private QueueService queueService;
    private PlaylistService playlistService;
    private it.diem.unisa.musicmanager.service.PlayerService playerService;
    private FilteredList<QueueItem> filteredQueue;
    private QueueItem draggedQueueItem;



    /**
     * Metodo di inizializzazione chiamato automaticamente da JavaFX.
     * Configura la CellFactory per la ListView, personalizzando la visualizzazione testuale
     * degli elementi in coda (Titolo, Autore, eventuale Playlist di provenienza).
     * Implementa inoltre tutti i gestori di eventi per il Drag & Drop nativo,
     * consentendo il riordino grafico e logico degli elementi.
     */
    @FXML
    public void initialize() {

        queueListView.setCellFactory(param -> {
            ListCell<QueueItem> cell = new ListCell<>() {

                @Override
                protected void updateItem(
                        QueueItem item,
                        boolean empty
                ) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    if (!(item.getPlayable() instanceof Track track)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    String display =
                            track.getTitle()
                                    + " - "
                                    + track.getAuthor();

                    if (item.getBelongsToPlaylist() != null
                            && playlistService != null) {

                        java.util.Optional<Playlist> opt =
                                playlistService.getPlaylistById(
                                        item.getBelongsToPlaylist()
                                );

                        if (opt.isPresent()) {
                            display +=
                                    " (from Playlist: "
                                            + opt.get().getName()
                                            + ")";
                        }
                    }

                    setText(display);
                }
            };

            cell.setOnDragDetected(event -> {
                if (cell.isEmpty() || cell.getItem() == null) {
                    return;
                }

                draggedQueueItem = cell.getItem();

                Dragboard dragboard =
                        cell.startDragAndDrop(TransferMode.MOVE);

                ClipboardContent content =
                        new ClipboardContent();

                content.putString(
                        draggedQueueItem
                                .getPlayable()
                                .getId()
                                .toString()
                );

                dragboard.setContent(content);
                event.consume();
            });

            cell.setOnDragOver(event -> {
                if (draggedQueueItem != null
                        && !cell.isEmpty()
                        && cell.getItem() != null
                        && cell.getItem() != draggedQueueItem) {

                    event.acceptTransferModes(
                            TransferMode.MOVE
                    );
                }

                event.consume();
            });

            cell.setOnDragDropped(event -> {
                boolean completed = false;

                QueueItem targetItem =
                        cell.getItem();

                if (draggedQueueItem != null
                        && targetItem != null
                        && queueService != null
                        && draggedQueueItem != targetItem) {

                    queueService.moveQueueItem(
                            draggedQueueItem,
                            targetItem
                    );

                    refreshQueueFilter();
                    completed = true;
                }

                event.setDropCompleted(completed);
                event.consume();
            });

            cell.setOnDragDone(event -> {
                draggedQueueItem = null;
                event.consume();
            });

            return cell;
        });
    }

    /**
     * Inietta il service responsabile delle playlist.
     * Necessario per recuperare i nomi delle playlist originarie dei brani in coda.
     * Al momento dell'iniezione, forza un refresh grafico della lista per aggiornare le etichette.
     * @param playlistService l'istanza del service delle playlist.
     */
    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
        if (queueListView != null) {
            queueListView.refresh();
        }
    }

    /**
     * Inietta il service responsabile della gestione logica della coda.
     * Inizializza la {@link FilteredList} agganciata alla lista originale del Service e
     * predispone i listener per intercettare aggiunte, rimozioni e cambi di brano corrente,
     * assicurando che la visualizzazione rimanga coerente.
     * @param queueService l'istanza del service della coda.
     */
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;

        if (this.queueService != null && this.queueListView != null) {
            this.filteredQueue = new FilteredList<>(
                    this.queueService.getQueueList(),
                    item -> true
            );

            this.queueListView.setItems(filteredQueue);

            this.queueService.getQueueList().addListener(
                    (ListChangeListener<QueueItem>) change ->
                            refreshQueueFilter()
            );

            /*
             * Fondamentale quando la traccia successiva è uguale a quella corrente:
             * currentTrackProperty potrebbe non cambiare, mentre currentItem cambia.
             */
            this.queueService.currentItemProperty().addListener(
                    (observable, oldItem, newItem) ->
                            refreshQueueFilter()
            );

            refreshQueueFilter();
        }
    }

    /**
     * Inietta il service responsabile della riproduzione audio.
     * Imposta un listener sul brano in esecuzione per aggiornare l'etichetta superiore
     * ("Now Playing") e richiedere un aggiornamento del filtro della coda.
     * @param playerService l'istanza del service di riproduzione.
     */
    public void setPlayerService(it.diem.unisa.musicmanager.service.PlayerService playerService) {
        this.playerService = playerService;
        if (this.playerService != null && labelCurrentTrack != null) {
            // Aggiorna subito
            updateCurrentTrackLabel(this.playerService.currentTrackProperty().get());
            // Aggiunge il listener per i futuri cambiamenti
            this.playerService.currentTrackProperty().addListener((observable, oldValue, newValue) -> {
                updateCurrentTrackLabel(newValue);
                refreshQueueFilter();
            });
        }
    }

    /**
     * Aggiorna il testo dell'etichetta che mostra il brano attualmente in esecuzione.
     * @param track la traccia corrente, oppure null se la riproduzione è ferma.
     */
    private void updateCurrentTrackLabel(Track track) {
        if (track == null) {
            labelCurrentTrack.setText("No Track Playing");
        } else {
            labelCurrentTrack.setText(track.getTitle() + " - " + track.getAuthor());
        }
    }

    /**
     * Ricalcola il predicato (condizione logica) della FilteredList.
     * Filtra la lista originale mostrata all'utente in modo da includere ESCLUSIVAMENTE
     * i brani che si trovano dopo l'indice dell'elemento attualmente in riproduzione.
     * I brani passati vengono mantenuti in memoria dal QueueService, ma nascosti dalla View.
     */
    private void refreshQueueFilter() {
        if (filteredQueue == null) return;
        javafx.collections.ObservableList<QueueItem> source = queueService.getQueueList();
        filteredQueue.setPredicate(item -> {
            QueueItem current = queueService.getCurrentItem();
            int currentIndex = (current == null) ? -1 : source.indexOf(current);
            return source.indexOf(item) > currentIndex;   // solo le prossime
        });
        javafx.application.Platform.runLater(() -> queueListView.refresh());
    }
}