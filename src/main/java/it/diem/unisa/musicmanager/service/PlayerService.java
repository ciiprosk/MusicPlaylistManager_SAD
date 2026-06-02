package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.scene.media.MediaPlayer;

public class PlayerService {
    private final SharedState sharedState;
    private MediaPlayer mediaPlayer;

    public PlayerService(SharedState sharedState) {
        this.sharedState = sharedState;
    }

    public void play(Track track) {}
    public void pause() {}
    public void resume() {}
    public void stop() {}
}
