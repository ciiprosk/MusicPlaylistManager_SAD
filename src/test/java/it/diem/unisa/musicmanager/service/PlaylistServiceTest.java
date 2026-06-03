package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.dao.JSONPlaylistDAO;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
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

        UUID trackId = UUID.randomUUID();

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                trackId
        );

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertTrue(updatedPlaylist.containsTrack(trackId));

        assertEquals(
                1,
                updatedPlaylist.getTracks().size()
        );
    }

    //TEST AGGIUNTA TRACCIA A PLAYLIST NEGATO SE LA TRACCIA è UN DUPLICATO

    @Test
    void addTrackToPlaylistShouldNotInsertDuplicateTrack() {

        playlistService.createPlaylist("Rock");

        Playlist playlist =
                sharedState.getALlPlaylists().get(0);

        UUID trackId = UUID.randomUUID();

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                trackId
        );

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                trackId
        );

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertEquals(
                1,
                updatedPlaylist.getTracks().size()
        );
    }

    //TEST CREAZIONE PLAYLIST NEGATA CON NOME PLAYLIST DUPLICATO

    @Test
    void createPlaylistShouldFailWhenNameAlreadyExists() {

        playlistService.createPlaylist("Rock");

        Optional<String> result =
                playlistService.createPlaylist("Rock");

        assertTrue(result.isPresent());
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

        UUID trackId = UUID.randomUUID();

        playlistService.addTrackToPlaylist(
                playlist.getId(),
                trackId
        );

        playlistService.removeTrackFromPlaylist(
                playlist.getId(),
                trackId
        );

        Playlist updatedPlaylist =
                sharedState.getALlPlaylists().get(0);

        assertFalse(
                updatedPlaylist.containsTrack(trackId)
        );

        assertEquals(
                0,
                updatedPlaylist.getTracks().size()
        );
    }

    //TEST CREAZIONE PLAYLIST NEGATO CON CAMPO NOME PLAYLIST VUOTO

    @Test
    void createPlaylistShouldFailWhenNameIsEmpty() {

        Optional<String> result =
                playlistService.createPlaylist("");

        assertTrue(result.isPresent());
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
}