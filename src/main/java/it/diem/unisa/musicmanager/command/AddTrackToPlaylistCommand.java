package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.exception.PlaylistInfoException;
import it.diem.unisa.musicmanager.service.PlaylistService;

import java.util.Optional;
import java.util.UUID;
/**
 * Comando che aggiunge una traccia a una playlist.
 * Implementa il Command Pattern incapsulando l'operazione di aggiunta
 * di un brano a una playlist e permettendo di annullarla tramite undo.
 * Il comando utilizza PlaylistService come receiver,
 * che contiene la logica di business per la gestione delle playlist.
 */
public class AddTrackToPlaylistCommand implements Command{

    //Servizio che esegue le operazioni reali sulle playlist (receiver)
    private final PlaylistService service;

    // Identificativo della playlist su cui operare.
    private final UUID playlistId;

    //Identificativo della traccia da aggiungere.
    private final UUID trackId;

    // Serve per garantire che l'undo venga eseguito solo se
    //  l'operazione di execute ha realmente aggiunto la traccia.
    private boolean applied = false;

    private final String trackTitle;
    private final String playlistName;

    /**
     * Costruisce un comando per aggiungere una traccia a una playlist.
     * @param service     servizio che gestisce le playlist (receiver)
     * @param playlistId   identificativo della playlist
     * @param trackId      identificativo della traccia da aggiungere
     */
    public AddTrackToPlaylistCommand(PlaylistService service, UUID playlistId, UUID trackId, String trackTitle, String playlistName) {
        this.service = service;
        this.playlistId = playlistId;
        this.trackId = trackId;
        this.trackTitle = trackTitle;
        this.playlistName = playlistName;
    }

    /**
     * Esegue il comando aggiungendo la traccia alla playlist.
     * Se la playlist non esiste viene lanciata un'eccezione che viene
     * catturata e convertita in un messaggio di errore.
     * Se la traccia è già presente, non viene effettuata alcuna modifica.
     * @return Optional#empty() se l'operazione ha successo,
     * oppure un Optional contenente il messaggio di errore
     */
    @Override
    public Optional<String> execute() {
        try {
            // recupera playlist e controlla se contiene la traccia
            boolean alreadyThere = service.getPlaylistById(playlistId)
                    .map(p -> p.containsTrack(trackId))//true se presente
                    .orElseThrow(() -> new PlaylistInfoException("Playlist not found")); //errore altrimenti

            // il tuo service aggiunge solo se assente
            if (!alreadyThere) {
                service.addTrackToPlaylist(playlistId, trackId); //add reale
                applied = true; // segna che ho modificato lo stato
            }
            // successo → nessun errore
            return Optional.empty();
        } catch (PlaylistInfoException e) {

            // errore → ritorna messaggio
            return Optional.of(e.getMessage());
        }
    }

    /**
     * Annulla l'effetto del comando, rimuovendo la traccia dalla playlist
     * se era stata effettivamente aggiunta durante execute().
     */
    @Override
    public void undo() {
        // annullo SOLO se avevo davvero aggiunto
        if (applied) {
            service.removeTrackFromPlaylist(playlistId, trackId);
        }
    }

    @Override
    public String getDescription() {
        return "Add \"" + trackTitle + "\" to \"" + playlistName + "\"";
    }
}
