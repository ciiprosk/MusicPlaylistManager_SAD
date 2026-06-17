package it.diem.unisa.musicmanager;

import javafx.application.Application;

/**
 * Classe principale dell'applicazione JavaFX.
 * Ha il compito di avviare l'applicazione JavaFX.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MusicPlaylistManager.class, args);
    }
}
