package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.TrackService;

import java.util.*;

/**
 * Classe che implementa l'interfaccia Command per la cancellazione di una traccia.
 * Implementa il Command Pattern per supportare l'undo di operazioni di cancellazione.
 */
public class DeleteTrackCommand implements Command {

    private final TrackService trackService;    //per tracce in archivio
    private final PlaylistService playlistService;  //per ripristinare eventuali tracce eliminate che appartenevano a playlist
    private final UUID trackId; //id della traccia da eliminare

    private Track trackCopy;    //copia della traccia. mi serve per recuperarla dopo che l'ho eliminata dall'archivio
    private final Map<UUID, Integer> playlistPositions = new LinkedHashMap<>(); // mappa: playlistId -> posizione originale della traccia in quella playlist

    /**
     * Costruttore della classe.
     * @param trackService il servizio che gestisce le tracce (receiver)
     * @param playlistService il servizio che gestisce le playlist (receiver)
     * @param trackId l'identificativo della traccia da eliminare
     */
    public DeleteTrackCommand(TrackService trackService, PlaylistService playlistService, UUID trackId) {

        this.trackService = trackService;
        this.playlistService = playlistService;
        this.trackId = trackId;

    }

    /**
     * Esegue il comando di cancellazione della traccia.
     * @return un Optional contenente un messaggio di errore se l'operazione non va a buon fine, altrimenti Optional.empty()
     */
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

    /**
     * Operazione di undo per la cancellazione di una traccia.
     * Ripristina la traccia originale nel sistema, ripristina le relative posizioni originali nelle playlist.
     */
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

    /**
     * Restituisce una descrizione del comando.
     * @return una stringa che descrive l'operazione eseguita dal comando
     */
    @Override
    public String getDescription() {
        String title = (trackCopy != null) ? trackCopy.getTitle() : "";
        return "Delete track \"" + title + "\"";
    }

}