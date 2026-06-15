package it.diem.unisa.musicmanager.command;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.PlaylistService;

import java.util.Optional;
import java.util.UUID;

public class DeletePlaylistCommand implements Command{
    private final PlaylistService service;
    private final UUID playlistId;
    private Playlist snapshot;   // catturato PRIMA di cancellare

    public DeletePlaylistCommand(PlaylistService service, UUID playlistId) {
        this.service = service;
        this.playlistId = playlistId;
    }

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

    @Override
    public void undo() {
        if (snapshot != null) {
            service.restorePlaylist(snapshot);   // ripristina con id + tracce
        }
    }

    @Override
    public String getDescription() {
        String nome = (snapshot != null) ? snapshot.getName() : "";
        return "Delete playlist \"" + nome + "\"";
    }
}
