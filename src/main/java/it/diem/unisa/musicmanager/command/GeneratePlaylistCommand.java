package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.specification.Specification;

import java.util.*;

/**
 * Classe che implementa l'interfaccia Command per la generazione di una playlist.
 */
public class GeneratePlaylistCommand implements Command{
    private final PlaylistService service;
    private final String name;
    private final Collection<Track> tracks;
    private final Specification<Track> criteria;
    private Playlist created;

    // servono a gestire la sovrscizione dei brnai di una playlist qualora l'utnet volesse
    private boolean isOverwrite;
    private UUID overwrittenPlaylistId;
    private List<Track> oldTracksSnapshot;

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
        Optional<Playlist> existingOpt = service.getPlaylists().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();

        if (existingOpt.isPresent()) {
            Playlist existing = existingOpt.get();
            isOverwrite = true;
            overwrittenPlaylistId = existing.getId();
            // Salva uno snapshot dei vecchi brani per l'Undo
            oldTracksSnapshot = new ArrayList<>(existing.getTracksList());

            // Genera la playlist temporanea con i nuovi brani filtrati
            Playlist temp = service.generate(name, tracks, criteria);

            // Sovrascrive i brani della playlist esistente
            service.updatePlaylistTracks(existing.getId(), temp.getTracksList());
            return Optional.empty();
        } else {
            isOverwrite = false;
            try {
                created = service.generateAndSaveReturning(name, tracks, criteria);
                return Optional.empty();
            } catch (it.diem.unisa.musicmanager.exception.PlaylistInfoException e) {
                return Optional.of(e.getMessage());
            }

        }
    }

    /**
     * Metodo che ripristina lo stato precedente del sistema dopo l'esecuzione del comando.
     * Questo metodo deve rimuovere la playlist appena creata.
     */
    @Override
    public void undo() {
        if (isOverwrite) {
            // Se avevamo sovrascritto, ripristiniamo i brani precedenti
            if (overwrittenPlaylistId != null && oldTracksSnapshot != null) {
                service.updatePlaylistTracks(overwrittenPlaylistId, oldTracksSnapshot);
            }
        } else {
            // Se era una nuova playlist, la eliminiamo
            if (created != null) {
                service.deletePlaylist(created.getId());
            }
        }
    }

    /** Metodo che restituisce una descrizione del comando.
     * @return una stringa che descrive l'operazione eseguita dal comando
     */
    @Override
    public String getDescription() {
        if (isOverwrite) {
            return "Overwrite playlist \"" + name + "\"";
        }
        return "Generate playlist \"" + name + "\"";
    }
}
