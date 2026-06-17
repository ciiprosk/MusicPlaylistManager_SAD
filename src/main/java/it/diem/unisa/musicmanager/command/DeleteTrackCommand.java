package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.TrackService;

import java.util.*;

public class DeleteTrackCommand implements Command {

    private final TrackService trackService;    //per tracce in archivio
    private final PlaylistService playlistService;  //per ripristinare eventuali tracce eliminate che appartenevano a playlist
    private final UUID trackId; //id della traccia da eliminare

    private Track trackCopy;    //copia della traccia. mi serve per recuperarla dopo che l'ho eliminata dall'archivio
    private final Map<UUID, Integer> playlistPositions = new LinkedHashMap<>(); // mappa: playlistId -> posizione originale della traccia in quella playlist

    public DeleteTrackCommand(TrackService trackService, PlaylistService playlistService, UUID trackId) {

        this.trackService = trackService;
        this.playlistService = playlistService;
        this.trackId = trackId;

    }

    @Override
    public Optional<String> execute() {

        //stiamo per eliminare la traccia originale: ci salviamo la copia, nel caso dovessimo fare Undo
        trackCopy = trackService.searchTrackById(trackId).orElse(null);

        if (trackCopy == null)
            return Optional.of("Track not found.");


        // salvo le posizioni originali nelle playlist
        if (playlistService != null) {

            for (Playlist p : playlistService.getPlaylists()) {

                List<UUID> trackIds = p.getTracks();
                int pos = trackIds.indexOf(trackId);

                if (pos >= 0) {
                    playlistPositions.put(p.getId(), pos);
                }

            }
        }

        // cancello gli observer rimuovono la traccia dalle playlist automaticamente
        trackService.deleteTrack(trackId);
        return Optional.empty();

    }

    @Override
    public void undo() {

        if (trackCopy == null)
            return;

        // ripristina la traccia nell'archivio
        trackService.restoreTrack(trackCopy);

        // reiserisce nelle playlist originali, nella posizione originale
        for (Map.Entry<UUID, Integer> entry : playlistPositions.entrySet()) {
            playlistService.addTrackToPlaylistAtPosition(entry.getKey(), trackCopy, entry.getValue());
        }

    }

    @Override
    public String getDescription() {
        String title = (trackCopy != null) ? trackCopy.getTitle() : "";
        return "Delete track \"" + title + "\"";
    }

}