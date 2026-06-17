package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.dao.JSONPlaylistDAO;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlaylistServiceTest {

    private PlaylistService playlistService;
    private SharedState sharedState;

    @BeforeEach
    void setUp() {

        File folder = new File("test-data");
        folder.mkdirs();

        new File(folder, "playlists-service-test.jsonl").delete();

        DAO<Playlist> playlistDAO =
                new JSONPlaylistDAO(
                        "test-data",
                        "playlists-service-test.jsonl"
                );

        sharedState = new SharedState();

        playlistService =
                new PlaylistService(
                        playlistDAO,
                        sharedState
                );
    }

    //TEST CAMBIA NOME PLAYLIST NEGATO QUANDO ESISTE UNA PLAYLIST CON IL NOME INSERITO

    @Test
    void renamePlaylistShouldFailWhenNameAlreadyExists() {

        playlistService.createPlaylist("Rock");
        playlistService.createPlaylist("Pop");

        Playlist secondPlaylist =
                sharedState.getALlPlaylists().get(1);

        Optional<String> result =
                playlistService.renamePlaylist(
                        secondPlaylist.getId(),
                        "Rock"
                );

        assertTrue(result.isPresent());
    }

    //TEST CAMBIA NOME PLAYLIST NEGATO QUANDO PLAYLIST NON ESISTE

    @Test
    void renamePlaylistShouldFailWhenPlaylistDoesNotExist() {

        Optional<String> result =
                playlistService.renamePlaylist(
                        UUID.randomUUID(),
                        "Nuovo Nome"
                );

        assertTrue(result.isPresent());
    }

    //TEST CREA PLAYLIST DEVE POTER CREARE E SALVARE PLAYLIST VUOTE

    @Test
    void createPlaylistShouldCreateAndSaveEmptyPlaylist() {

        Optional<String> result =
                playlistService.createPlaylist("La Mia Playlist");

        assertTrue(result.isEmpty());

        assertEquals(1, sharedState.getALlPlaylists().size());

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        assertEquals("La Mia Playlist", playlist.getName());

        assertTrue(playlist.getTracks().isEmpty());
    }

    //TEST CAMBIA NOME PLAYLIST AGGIORNA NOME CORRETTAMENTE

    @Test
    void renamePlaylistShouldUpdateNameCorrectly() {

        Optional<String> creationResult =
                playlistService.createPlaylist("Vecchio Nome");

        assertTrue(creationResult.isEmpty());

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Optional<String> renameResult =
                playlistService.renamePlaylist(
                        playlist.getId(),
                        "Nuovo Nome"
                );

        assertTrue(renameResult.isEmpty());

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertEquals(
                "Nuovo Nome",
                updatedPlaylist.getName()
        );

        assertNotEquals(
                "Vecchio Nome",
                updatedPlaylist.getName()
        );
    }

    //TEST AGGIUNTA TRACCIA A PLAYLIST DEVE AGGIUNGERE UNA TRACCIA CORRETTAMENTE

    @Test
    void addTrackToPlaylistShouldAddTrackCorrectly() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Track track = new Track(
                "Numb",
                "Linkin Park",
                Genre.ROCK,
                "songs/numb.mp3",
                185,
                "2003",
                null
        );

        sharedState.getALlTracks().add(track);

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track.getId()
        );

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertTrue(updatedPlaylist.containsTrack(track.getId()));

        assertEquals(
                1,
                updatedPlaylist.getTracks().size()
        );

        assertEquals(
                1,
                updatedPlaylist.getTracksList().size()
        );
    }

    //TEST CREAZIONE PLAYLIST NEGATA CON NOME PLAYLIST DUPLICATO

    @Test
    void createPlaylistShouldFailWhenNameAlreadyExists() {

        playlistService.createPlaylist("Rock");

        Optional<String> result =
                playlistService.createPlaylist("Rock");

        assertTrue(result.isPresent());
        assertEquals(
                "Playlist name already exists",
                result.get()
        );
    }

    //TEST CANCELLA PLAYLIST RIMUOVE PLAYLIST DA SHARED STATE

    @Test
    void deletePlaylistShouldRemovePlaylistFromSharedState() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        playlistService.deletePlaylist(
                playlist.getId()
        );

        assertTrue(
                sharedState.getALlPlaylists().isEmpty()
        );
    }

    //TEST RIMOZIONE TRACCIA DA PLAYLIST RIMUOVE LA TRACCIA CORRETTA DALLA PLAYLIST CORRENTE

    @Test
    void removeTrackFromPlaylistShouldRemoveTrackCorrectly() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Track track = new Track(
                "Numb",
                "Linkin Park",
                Genre.ROCK,
                "songs/numb.mp3",
                185,
                "2003",
                null
        );

        sharedState.getALlTracks().add(track);

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track.getId()
        );

        playlistService.removeTrackFromPlaylist(
                playlist.getId(),
                track.getId()
        );

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertFalse(
                updatedPlaylist.containsTrack(track.getId())
        );

        assertEquals(
                0,
                updatedPlaylist.getTracks().size()
        );

        assertEquals(
                0,
                updatedPlaylist.getTracksList().size()
        );
    }

    //TEST CREAZIONE PLAYLIST NEGATO CON CAMPO NOME PLAYLIST VUOTO

    @Test
    void createPlaylistShouldFailWhenNameIsEmpty() {

        Optional<String> result =
                playlistService.createPlaylist("");

        assertTrue(result.isPresent());
        assertEquals(
                "The name cannot be empty",
                result.get()
        );
    }

    //TEST CREA PLAYLIST AGGIUNGE LA PLAYLIST ALLO SHARED STATE

    @Test
    void createPlaylistShouldAddPlaylistToSharedState() {
        playlistService.createPlaylist("Rock");

        assertEquals(1, sharedState.getALlPlaylists().size());
        assertEquals("Rock", sharedState.getALlPlaylists().get(0).getName());
    }

    //TEST CREA PLAYLIST NOTIFICA OBSERVABLE LIST QUANDO CREATA

    @Test
    void createPlaylistShouldNotifyObservableListWhenPlaylistIsCreated() {
        final boolean[] notified = {false};

        sharedState.getALlPlaylists().addListener(
                (javafx.collections.ListChangeListener<Playlist>) change -> notified[0] = true
        );

        playlistService.createPlaylist("Rock");

        assertTrue(notified[0]);
    }

    //TEST CREA PLAYLIST RESTITUISCE MESSAGGIO DI CONFERMA CREAZIONE O ERRORE

    @Test
    void createPlaylistShouldReturnEmptyOptionalWhenPlaylistIsCreated() {
        Optional<String> result =
                playlistService.createPlaylist("Rock");

        assertTrue(result.isEmpty());
    }

    //TEST CREA PLAYLIST NEAGTO CON CAMPO NOME CONTENENTE SOLO SPAZI

    @Test
    void createPlaylistShouldFailWhenNameContainsOnlySpaces() {

        Optional<String> result =
                playlistService.createPlaylist("   ");

        assertTrue(result.isPresent());
        assertEquals(
                "The name cannot be empty",
                result.get()
        );
    }

    //TEST CREAZIONE PLAYLIST NEGATO CON CAMPO NOME MAGGIORE DI 50 CARATTERI

    @Test
    void createPlaylistShouldFailWhenNameIsLongerThan50Characters() {

        String longName = "A".repeat(51);

        Optional<String> result =
                playlistService.createPlaylist(longName);

        assertTrue(result.isPresent());
        assertEquals(
                "The name cannot be longer than 50 characters",
                result.get()
        );
    }

    //TEST CREAZIONE PLAYLIST GENERA UN ID PER LA PLAYLIST

    @Test
    void createPlaylistShouldGenerateUUID() {
        playlistService.createPlaylist("Rock");

        Playlist playlist = sharedState.getALlPlaylists().get(0);

        assertNotNull(playlist.getId());
    }

    //TEST CREAZIONE PLAYLIST RIMUOVE SPAZI DA INIZIO E FINE DEL CAMPO NOME PLAYLIST

    @Test
    void createPlaylistShouldTrimName() {
        playlistService.createPlaylist("   Rock   ");

        Playlist playlist = sharedState.getALlPlaylists().get(0);

        assertEquals("Rock", playlist.getName());
    }

    //TEST OBSERVER NOTIFICA SU CAMBIO NOME PLAYLIST

    @Test
    void observableListShouldNotifyWhenPlaylistIsRenamed() {
        playlistService.createPlaylist("Rock");

        final boolean[] notified = {false};

        sharedState.getALlPlaylists().addListener(
                (javafx.collections.ListChangeListener<Playlist>) change -> notified[0] = true
        );

        Playlist playlist = sharedState.getALlPlaylists().get(0);

        playlistService.renamePlaylist(playlist.getId(), "New Rock");

        assertTrue(notified[0]);
    }

    //TEST OBSERVER NOTIFICA SU CANCELLAZIONE PLAYLIST

    @Test
    void observableListShouldNotifyWhenPlaylistIsDeleted() {
        playlistService.createPlaylist("Rock");

        final boolean[] notified = {false};

        sharedState.getALlPlaylists().addListener(
                (javafx.collections.ListChangeListener<Playlist>) change -> notified[0] = true
        );

        Playlist playlist = sharedState.getALlPlaylists().get(0);

        playlistService.deletePlaylist(playlist.getId());

        assertTrue(notified[0]);
    }

    //TEST RINOMINA PLAYLIST NEGATO SE CAMPO NOME NUOVO è VUOTO

    @Test
    void renamePlaylistShouldFailWhenNewNameIsEmpty() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Optional<String> result =
                playlistService.renamePlaylist(
                        playlist.getId(),
                        ""
                );

        assertTrue(result.isPresent());
        assertEquals(
                "The name cannot be empty",
                result.get()
        );
    }

    //TEST RINOMINA PLAYLIST NEGATO SE CAMPO NOME NUOVO è MAGGIORE DI 50 CARATTERI

    @Test
    void renamePlaylistShouldFailWhenNewNameIsLongerThan50Characters() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        String longName = "A".repeat(51);

        Optional<String> result =
                playlistService.renamePlaylist(
                        playlist.getId(),
                        longName
                );

        assertTrue(result.isPresent());
        assertEquals(
                "The name cannot be longer than 50 characters",
                result.get()
        );
    }

    //TEST RINOMINA PLAYLIST RIMUOVE SPAZI DA INIZIO E FINE CAMPO NOME PLAYLIST

    @Test
    void renamePlaylistShouldTrimNewName() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Optional<String> result =
                playlistService.renamePlaylist(
                        playlist.getId(),
                        "   Pop   "
                );

        assertTrue(result.isEmpty());

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertEquals(
                "Pop",
                updatedPlaylist.getName()
        );
    }

    //TEST RINOMINA PLAYLIST CONSETITO CON NOME PLAYLIST UGUALE AL PRECEDENTE

    @Test
    void renamePlaylistWithSameNameShouldSucceed() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Optional<String> result =
                playlistService.renamePlaylist(
                        playlist.getId(),
                        "Rock"
                );

        assertTrue(result.isEmpty());

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertEquals(
                "Rock",
                updatedPlaylist.getName()
        );
    }

    //TEST AGGIUNTA BRANO A PLAYLIST AGGIUNGE BRANO ALLA FINE DELLA PLAYLIST

    @Test
    void addTrackToPlaylistShouldAddSelectedTrackAtTheEnd() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Track track1 = createTrack("Track1");
        Track track2 = createTrack("Track2");

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track1.getId()
        );

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track2.getId()
        );

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertEquals(2, updatedPlaylist.getTracks().size());

        assertEquals(
                track1.getId(),
                updatedPlaylist.getTracks().get(0)
        );

        assertEquals(
                track2.getId(),
                updatedPlaylist.getTracks().get(1)
        );

        assertEquals(2, updatedPlaylist.getTracksList().size());

        assertEquals(
                track1.getId(),
                updatedPlaylist.getTracksList().get(0).getId()
        );

        assertEquals(
                track2.getId(),
                updatedPlaylist.getTracksList().get(1).getId()
        );
    }

    //TEST AGGIUNGERE UN BRANO ALLA PLAYLIST NOTIFICA OBSERVER

    @Test
    void addTrackToPlaylistShouldNotifyObservableList() {

        playlistService.createPlaylist("Rock");

        final boolean[] notified = {false};

        sharedState.getALlPlaylists().addListener(
                (javafx.collections.ListChangeListener<Playlist>) change -> notified[0] = true
        );

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Track track = createTrack("Numb");

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track.getId()
        );

        assertTrue(notified[0]);
    }

    //TEST AGGIUNTA BRANO IN UNA PLAYLIST DEVE ESSERE POSSIBILE ANCHE DURANTE LA RIPRODUZIONE DELLA PLAYLIST

    @Test
    void addTrackToPlaylistShouldWorkWhilePlaying() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Track currentTrack = createTrack("CurrentTrack");
        Track newTrack = createTrack("NewTrack");



        playlistService.addTrackToPlaylist(
                playlist.getId(),
                currentTrack.getId()
        );

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                newTrack.getId()
        );

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertTrue(
                updatedPlaylist.containsTrack(newTrack.getId())
        );

        assertEquals(
                newTrack.getId(),
                updatedPlaylist.getTracks().get(1)
        );

        assertEquals(
                newTrack.getId(),
                updatedPlaylist.getTracksList().get(1).getId()
        );
    }

    //TEST AGGIUNTA BRANO A PLAYLIST NON DEVE PERMETTERE DI AGGIUNGERE BRANI DUPLICATI

    @Test
    void addTrackToPlaylistShouldNotAddDuplicateTrack() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Track track = createTrack("Numb");

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track.getId()
        );

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track.getId()
        );

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertEquals(
                1,
                updatedPlaylist.getTracks().size()
        );

        assertEquals(
                1,
                updatedPlaylist.getTracksList().size()
        );
    }

    //TEST PERSISTENZA TRAMITE UPDATE DAO SU AGGIUNTA TRACCIA A PLAYLIST

    @Test
    void addTrackToPlaylistShouldPersistUpdatedPlaylistOnFile() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Track track = createTrack("Numb");

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track.getId()
        );

        DAO<Playlist> newPlaylistDAO =
                new JSONPlaylistDAO(
                        "test-data",
                        "playlists-service-test.jsonl"
                );

        Optional<Playlist> result =
                newPlaylistDAO.searchById(playlist.getId());

        assertTrue(result.isPresent());

        assertTrue(
                result.get().containsTrack(track.getId())
        );

        assertEquals(
                1,
                result.get().getTracks().size()
        );
    }

    //TEST VIETATO AGGIUNGERE UN BRANO AD UNA PLAYLIST INESISTENTE

    @Test
    void addTrackToPlaylistShouldFailWhenPlaylistDoesNotExist() {

        UUID fakePlaylistId = UUID.randomUUID();
        UUID trackId = UUID.randomUUID();

        assertThrows(
                RuntimeException.class,
                () -> playlistService.addTrackToPlaylist(fakePlaylistId, trackId)
        );
    }

    //TEST OBSERVER NOTIFICA CAMBIAMENTO INTERNO QUANDO VIENE AGGIUNTO UN BRANO

    @Test
    void addTrackToPlaylistShouldNotifyViewOfInternalChange() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Track track = createTrack("Numb");

        final boolean[] notified = {false};

        sharedState.getALlPlaylists().addListener(
                (javafx.collections.ListChangeListener<Playlist>) change -> {
                    while (change.next()) {
                        if (change.wasUpdated() || change.wasReplaced()) {
                            notified[0] = true;
                        }
                    }
                }
        );

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track.getId()
        );

        assertTrue(
                notified[0],
                "La View non è stata notificata della modifica interna alla playlist"
        );
    }

    //TEST RIMOZIONE DI UNA TRACCIA CONSENTITA ANCHE DURANTE LA RIPRODUZIONE DELLA PLAYLIST CHE LO CONTIENE

    @Test
    void removeTrackFromPlaylistShouldWorkWhilePlaying() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Track track = createTrack("Numb");

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track.getId()
        );

        playlistService.removeTrackFromPlaylist(
                playlist.getId(),
                track.getId()
        );

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertFalse(
                updatedPlaylist.containsTrack(track.getId())
        );

        assertTrue(
                updatedPlaylist.getTracks().isEmpty()
        );

        assertTrue(
                updatedPlaylist.getTracksList().isEmpty()
        );
    }

    //TEST RESTITUISCI PLAYLIST PER ID SELEZIONATO

    @Test
    void getPlaylistByIdShouldReturnSpecificPlaylist() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Optional<Playlist> result =
                playlistService.getPlaylistById(playlist.getId());

        assertTrue(result.isPresent());
        assertEquals("Rock", result.get().getName());
    }

    //TEST RECUPERA PLAYLIST INESISTENTE RESTITUISCE OPTIONAL VUOTO

    @Test
    void getPlaylistByIdShouldReturnEmptyWhenPlaylistDoesNotExist() {

        Optional<Playlist> result =
                playlistService.getPlaylistById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    //TEST RESTITUISCI TRACCE DA UNA PLAYLIST

    @Test
    void getTracksFromPlaylistShouldReturnTrackList() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Track track1 = createTrack("Track1");
        Track track2 = createTrack("Track2");

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track1.getId()
        );

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                track2.getId()
        );

        List<Track> tracks =
                playlistService.getTracksFromPlaylist(playlist.getId());

        assertEquals(2, tracks.size());

        assertEquals(
                track1.getId(),
                tracks.get(0).getId()
        );

        assertEquals(
                track2.getId(),
                tracks.get(1).getId()
        );
    }

    //SERVE SOLO PER I TEST

    private Track createTrack(String title) {
        Track track = new Track(
                title,
                "Author",
                Genre.ROCK,
                "songs/" + title + ".mp3",
                180,
                "2020",
                null
        );

        sharedState.getALlTracks().add(track);

        return track;
    }

    //TEST MAX 5 PLAYLIST TRA LE PIù ASCOLTATE

    @Test
    void getTop5MostPlayedPlaylistsShouldReturnMaximumFivePlaylists() {

        for (int i = 1; i <= 10; i++) {
            playlistService.createPlaylist("Playlist " + i);

            Playlist playlist =
                    sharedState.getALlPlaylists().get(i - 1);

            for (int j = 0; j < i; j++) {
                playlist.incrementPlayCount();
            }
        }

        List<Playlist> result =
                playlistService.getTop5MostPlayedPlaylists();

        assertEquals(5, result.size());
    }

    //TEST INCREMENTO CORRETTO DEL PLAYLIST PLAY COUNT

    @Test
    void incrementPlaylistPlayCountShouldIncreasePlayCount() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        Optional<String> result =
                playlistService.incrementPlayCount(playlist.getId());

        assertTrue(result.isEmpty());

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertEquals(1, updatedPlaylist.getPlayCount());
    }

    //TEST SPOSTAMENTO BRANO ALL'INTERNO DI UNA PLAYLIST (US14)
    @Test
    void moveTrackInPlaylistShouldChangeTracksOrderAndPersist() {
        playlistService.createPlaylist("Rock");
        Playlist playlist = sharedState.getALlPlaylists().get(0);

        Track track1 = createTrack("Track1");
        Track track2 = createTrack("Track2");
        Track track3 = createTrack("Track3");

        playlistService.addTrackToPlaylist(playlist.getId(), track1.getId());
        playlistService.addTrackToPlaylist(playlist.getId(), track2.getId());
        playlistService.addTrackToPlaylist(playlist.getId(), track3.getId());

        // Ordine iniziale: Track1, Track2, Track3
        assertEquals(track1.getId(), playlist.getTracks().get(0));
        assertEquals(track2.getId(), playlist.getTracks().get(1));
        assertEquals(track3.getId(), playlist.getTracks().get(2));

        // Spostiamo Track1 (indice 0) in posizione 2
        playlistService.moveTrackInPlaylist(playlist.getId(), 0, 2);

        // Nuovo ordine atteso nello sharedState: Track2, Track3, Track1
        Playlist updatedPlaylist = sharedState.getALlPlaylists().get(0);
        assertEquals(track2.getId(), updatedPlaylist.getTracks().get(0));
        assertEquals(track3.getId(), updatedPlaylist.getTracks().get(1));
        assertEquals(track1.getId(), updatedPlaylist.getTracks().get(2));

        // Verifica che la modifica sia stata persistita sul file tramite il DAO
        DAO<Playlist> newPlaylistDAO = new JSONPlaylistDAO("test-data", "playlists-service-test.jsonl");
        Optional<Playlist> persisted = newPlaylistDAO.searchById(playlist.getId());
        assertTrue(persisted.isPresent());
        assertEquals(track2.getId(), persisted.get().getTracks().get(0));
        assertEquals(track3.getId(), persisted.get().getTracks().get(1));
        assertEquals(track1.getId(), persisted.get().getTracks().get(2));
    }
}