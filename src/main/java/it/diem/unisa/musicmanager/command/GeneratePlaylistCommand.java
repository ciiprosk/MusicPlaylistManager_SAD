package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.specification.Specification;
import java.util.Collection;
import java.util.Optional;

/**
 * Classe che implementa l'interfaccia Command per la generazione di una playlist.
 */
public class GeneratePlaylistCommand implements Command{
    private final PlaylistService service;
    private final String name;
    private final Collection<Track> tracks;
    private final Specification<Track> criteria;
    private Playlist created;

    public GeneratePlaylistCommand(PlaylistService service, String name, Collection<Track> tracks, Specification<Track> criteria) {
        this.service = service;
        this.name = name;
        this.tracks = tracks;
        this.criteria = criteria;
    }


    /**
     * Metodo che esegue il comando di generazione della playlist.
     * @return Optional#empty() se l'operazione ha successo, altrimenti un Optional contenente un messaggio di errore.
     */
    @Override
    public Optional<String> execute() {
        // verifico un secondino se esite già una playlist con questo nome
        Playlist playlist = new Playlist(this.name);

        created = service.generateAndSaveReturning(name, tracks, criteria);

        if (created == null) {
            return Optional.of("A playlist with this name already exists!");
        }
        return Optional.empty();
    }

    /**
     * Metodo che ripristina lo stato precedente del sistema dopo l'esecuzione del comando.
     * Questo metodo deve rimuovere la playlist appena creata.
     */
    @Override
    public void undo() {
        if (created != null) {
            service.deletePlaylist(created.getId());
        }
    }

    /** Metodo che restituisce una descrizione del comando.
     * @return una stringa che descrive l'operazione eseguita dal comando
     */
    @Override
    public String getDescription() {
        return "Generate playlist \"" + name + "\"";
    }
}
