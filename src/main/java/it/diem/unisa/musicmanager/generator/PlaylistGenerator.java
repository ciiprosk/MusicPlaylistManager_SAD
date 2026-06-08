package it.diem.unisa.musicmanager.generator;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.state.SharedState;

/**
 * Strategia di generazione automatica di una playlist.
 * Ogni implementazione costruisce una playlist secondo un proprio criterio
 * (per genere, per anno, ...), leggendo le tracce dallo SharedState.
 * Interfaccia (Product)
 */
public interface PlaylistGenerator {
    /**
     * Genera una playlist a partire dalle tracce presenti nello stato.
     * @param state lo stato condiviso da cui leggere le tracce
     * @return la playlist generata
     */
    Playlist generate(SharedState state);
}
