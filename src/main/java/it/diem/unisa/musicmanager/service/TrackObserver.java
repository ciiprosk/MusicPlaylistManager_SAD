package it.diem.unisa.musicmanager.service;

import java.util.UUID;

public interface TrackObserver {

    void onTrackDeleted(UUID trackId);

}
