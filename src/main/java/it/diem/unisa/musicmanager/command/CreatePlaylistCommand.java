package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.PlaylistService;

import java.util.Optional;

public class CreatePlaylistCommand implements Command{
    private final PlaylistService service;
    private final String name;
    private Playlist created;   // catturata in execute, serve all'undo

    public CreatePlaylistCommand(PlaylistService service, String name) {
        this.service = service;
        this.name = name;
    }

    @Override
    public Optional<String> execute() {
        created = service.createPlaylistReturning(name);
        if (created == null) {
            return Optional.of("A playlist with this name already exists!");
        }
        return Optional.empty();
    }

    @Override
    public void undo() {
        if (created != null) {
            service.deletePlaylist(created.getId());
        }
    }

    @Override
    public String getDescription() {
        return "Create playlist \"" + name + "\"";
    }
}
