package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.PlaylistService;

import java.util.Optional;

/**
 * Classe cje implementa l'interfaccia Command per la creazione di una playlist.
 * Implementa il Command Pattern per supportare l'undo di operazioni di creazione.
 */
public class CreatePlaylistCommand implements Command{
    private final PlaylistService service;
    private final String name;
    private Playlist created;   // catturata in execute, serve all'undo

    /**
     * Costruisce un comando per creare una playlist.
     * @param service Il servizio che gestisce le playlist (receiver).
     * @param name Il nome della nuova playlist.
     */
    public CreatePlaylistCommand(PlaylistService service, String name) {
        this.service = service;
        this.name = name;
    }

    /**
     * Esegue il comando di creazione della playlist.
     * @return Optional#empty() se l'operazione ha successo, altrimenti un Optional contenente un messaggio di errore.
     */
    @Override
    public Optional<String> execute() {
        try {
            created = service.createPlaylistReturning(name);
            return Optional.empty();
        } catch (it.diem.unisa.musicmanager.exception.PlaylistInfoException e) {
            return Optional.of(e.getMessage());
        }
    }

    /**
     * Operazione di undo per rimuovere la playlist creata se l'operazione di creazione ha avuto successo.
     */
    @Override
    public void undo() {
        if (created != null) {
            service.deletePlaylist(created.getId());
        }
    }

    /**
     * Restituisce una descrizione del comando.
     * @return una stringa che descrive l'operazione eseguita dal comando
     */
    @Override
    public String getDescription() {
        return "Create playlist \"" + name + "\"";
    }
}
