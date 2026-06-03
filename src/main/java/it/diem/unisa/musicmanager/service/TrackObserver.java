package it.diem.unisa.musicmanager.service;

import java.util.UUID;

/**
 * Interfaccia per gli observer di TrackService. Per verificare quando una traccia viene eliminata e agire di conseguenza.
 *
 */
public interface TrackObserver {
    /**
     * Metodo chiamato quando una traccia viene eliminata.
     * @param trackId è l'identificatore univoco della traccia eliminata.
     */
    void onTrackDeleted(UUID trackId);

}
