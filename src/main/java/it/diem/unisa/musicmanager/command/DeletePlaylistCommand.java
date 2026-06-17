package it.diem.unisa.musicmanager.command;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.PlaylistService;

import java.util.Optional;
import java.util.UUID;

/**
 * Classe che implementa l'interfaccia Command per la cancellazione di una playlist.
 * Implementa il Command Pattern per supportare l'undo di operazioni di cancellazione.
 */
public class DeletePlaylistCommand implements Command{
    private final PlaylistService service;
    private final UUID playlistId;
    private Playlist snapshot;   // catturato PRIMA di cancellare

    /**
     * Costruisce un comando per cancellare una playlist.
     * @param service Il servizio che gestisce le playlist (receiver).
     * @param playlistId L'identificativo della playlist da cancellare.
     */
    public DeletePlaylistCommand(PlaylistService service, UUID playlistId) {
        this.service = service;
        this.playlistId = playlistId;
    }

    /**
     * Esegue il comando di cancellazione della playlist.
     * @return Optional#empty() se l'operazione ha successo, altrimenti un Optional contenente un messaggio di errore.
     */
    @Override
    public Optional<String> execute() {
        // catturo lo stato completo prima di cancellare
        snapshot = service.getPlaylistById(playlistId).orElse(null);
        if (snapshot == null) {
            return Optional.of("Playlist not found.");
        }
        service.deletePlaylist(playlistId);
        return Optional.empty();
    }

    /**
     * Operazione di undo per ripristinare lo stato precedente della playlist se l'operazione di cancellazione ha avuto successo.
     *
     */
    @Override
    public void undo() {
        if (snapshot != null) {
            service.restorePlaylist(snapshot);   // ripristina con id + tracce
        }
    }

    /**
     * Restituisce una descrizione del comando.
     * @return una stringa che descrive l'operazione eseguita dal comando
     */
    @Override
    public String getDescription() {
        String nome = (snapshot != null) ? snapshot.getName() : "";
        return "Delete playlist \"" + nome + "\"";
    }
}
