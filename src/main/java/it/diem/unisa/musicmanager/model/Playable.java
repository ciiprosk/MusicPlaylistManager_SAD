package it.diem.unisa.musicmanager.model;

import java.util.List;
import java.util.UUID;

/**
 * È l'interfaccia comune di Track e Playlist.
 * Permette di trattare le due classi citate allo stesso modo.
 */
public interface Playable {
    List<Track> getTracksToPlay();
    QueueItemType getType();
    UUID getId();
}
