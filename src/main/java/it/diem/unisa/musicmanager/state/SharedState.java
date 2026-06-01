package it.diem.unisa.musicmanager.state;
import it.diem.unisa.musicmanager.model.Playlist;
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

    private final ObjectProperty<Track> currentTrack = new SimpleObjectProperty<>();
    private final BooleanProperty isPlaying = new SimpleBooleanProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty();

    //uso il costruttore di default

    public ObservableList<Track> getALlTracks(){
        return allTracks;
    }
    public ObservableList<Playlist> getALlPlaylists(){
        return allPlaylists;
    }
    public ObjectProperty<Track> getCurrentTrack(){
        return currentTrack;
    }
    public BooleanProperty getIsPlaying(){
        return isPlaying;
    }
    public DoubleProperty getProgress(){
        return progress;
    }

}
