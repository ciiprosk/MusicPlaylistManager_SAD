package it.diem.unisa.musicmanager.state;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.QueueItem;
import it.diem.unisa.musicmanager.model.Track;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
/**
 * La classe rappresenta lo stato condiviso tra le classi del package controller.
 * È un conteitpre di stati osservabili da tutte le classi del package controller.
 * Non contiene alcuna logica di business, niente DAO, niente Media o MediaPlayer.
 */
public class SharedState {
    private final ObservableList<Track> allTracks = FXCollections.observableArrayList();
    private final ObservableList<Playlist> allPlaylists = FXCollections.observableArrayList();
    private final ObservableList<QueueItem> queue = FXCollections.observableArrayList();

    //uso il costruttore di default

    /**
     * Restituisce una lista osservabile di tutte le tracce.
     * @return la lista di tutte le tracce.
     */
    public ObservableList<Track> getALlTracks(){
        return allTracks;
    }

    /**
     * Restituisce una lista osservabile di tutte le playlist.
     * @return una lista di tutte le playlist.
     */
    public ObservableList<Playlist> getALlPlaylists(){
        return allPlaylists;
    }

    /**
     * Restituisce la coda osservabile di riproduzione.
     * @return la coda di riproduzione.
     */
    public ObservableList<QueueItem> getQueue(){
            return queue;
        }

}
