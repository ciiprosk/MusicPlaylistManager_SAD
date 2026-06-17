package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.exception.PlaylistInfoException;
import it.diem.unisa.musicmanager.service.PlaylistService;

import java.util.Optional;
import java.util.UUID;


/**
 * Comando che rimuove una traccia da una playlist.
 * Implementa il Command Pattern incapsulando l'operazione di rimozione
 * di un brano da una playlist e consentendo il ripristino tramite undo.
 * Utilizza PlaylistService come receiver per eseguire
 * le operazioni di business sulla playlist.
 */
public class RemoveTrackFromPlaylistCommand implements Command{

    //Servizio che gestisce la logica delle playlist.
    private final PlaylistService service;

    // Identificativo della playlist su cui operare.
    private final UUID playlistId;

    //Identificativo della traccia da rimuovere.
    private final UUID trackId;


    /**
     * Indica se la rimozione è stata effettivamente eseguita.
     * Serve per garantire che l'undo venga eseguito solo se
     * la traccia era realmente presente e rimossa.
     */
    private boolean applied = false;

    private final String trackTitle;
    private final String playlistName;


    /**
     * Costruisce un comando per rimuovere una traccia da una playlist.
     * @param service     servizio che gestisce le playlist (receiver)
     * @param playlistId   identificativo della playlist
     * @param trackId      identificativo della traccia da rimuovere
     */
    public RemoveTrackFromPlaylistCommand(PlaylistService service, UUID playlistId, UUID trackId, String trackTitle, String playlistName) {
        this.service = service;
        this.playlistId = playlistId;
        this.trackId = trackId;
        this.trackTitle = trackTitle;
        this.playlistName = playlistName;
    }

    /**
     * Esegue il comando rimuovendo la traccia dalla playlist.
     * Se la playlist non esiste viene sollevata un'eccezione.
     * La rimozione avviene solo se la traccia è effettivamente presente.
     * @return Optional#empty() se l'operazione ha successo,
     * oppure un Optional contenente il messaggio di errore
     */
    @Override
    public Optional<String> execute() {
        try {
            // recupera playlist e verifica se contiene la traccia
            boolean present = service.getPlaylistById(playlistId)
                    .map(p -> p.containsTrack(trackId)) //true se presente
                    .orElseThrow(() -> new PlaylistInfoException("Playlist not found")); //errore altrimenti
            //se la traccia è presente la rimuovo
            if (present) {
                service.removeTrackFromPlaylist(playlistId, trackId);
                applied = true; //segno che l'azione è stata eseguita
            }
            return Optional.empty(); //successo
        } catch (PlaylistInfoException e) {
            //errore → ritorna messaggio
            return Optional.of(e.getMessage());
        }
    }

    /**
     * Ripristina la traccia nella playlist se era stata
     * effettivamente rimossa durante execute().
     */
    @Override
    public void undo() {
        // undo solo se la remove è avvenuta davvero
        if (applied) {
            service.addTrackToPlaylist(playlistId, trackId); // ripristino
        }
    }

    @Override
    public String getDescription() {
        return "Remove \"" + trackTitle + "\" from \"" + playlistName + "\"";
    }
}
