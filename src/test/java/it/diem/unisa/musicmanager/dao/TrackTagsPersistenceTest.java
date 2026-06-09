package it.diem.unisa.musicmanager.dao;

import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * US5 - Verifica che l'aggiunta di un tag a una traccia venga salvata dal DAO
 * e resti persistente dopo un "riavvio" (un nuovo DAO che rilegge lo stesso file).
 */
class TrackTagsPersistenceTest {

    private static final String FILE_NAME = "tracks.json";

    /**
     * Aggiunge un tag a una traccia, la salva con update e verifica che,
     * ricreando il DAO sullo stesso file (simulazione del riavvio), il tag persista.
     */
    @Test
    void tagAggiuntoPersisteDopoRiavvio(@TempDir Path tempDir) {
        String folder = tempDir.toString();

        // --- prima sessione: creo, salvo, aggiungo tag, aggiorno ---
        JSONTrackDAO dao = new JSONTrackDAO(folder, FILE_NAME);

        Track track = new Track(
                "Test Song",
                "Test Artist",
                null,                       // genere -> default UNKNOWN
                "/music/test.mp3",
                180,
                "2020",
                EnumSet.noneOf(Tag.class)   // nessun tag all'inizio
        );

        dao.insert(track);

        track.addTag(Tag.FAVOURITE);        // l'azione della user story
        dao.update(track);

        // --- riavvio: nuovo DAO che rilegge da zero lo stesso file ---
        JSONTrackDAO daoDopoRiavvio = new JSONTrackDAO(folder, FILE_NAME);
        Optional<Track> ricaricata = daoDopoRiavvio.searchById(track.getId());

        assertTrue(ricaricata.isPresent(), "La traccia deve esistere dopo il riavvio");
        assertTrue(ricaricata.get().hasTag(Tag.FAVOURITE),
                "Il tag aggiunto deve essere stato persistito");
    }

    /**
     * Controprova: senza aggiungere alcun tag, dopo il riavvio la traccia
     * non deve avere quel tag. Serve a dimostrare che il test sopra passa
     * grazie alla persistenza reale, non per un default casuale.
     */
    @Test
    void senzaTagNonRisultaNessunTagDopoRiavvio(@TempDir Path tempDir) {
        String folder = tempDir.toString();

        JSONTrackDAO dao = new JSONTrackDAO(folder, FILE_NAME);

        Track track = new Track(
                "No Tag Song",
                "Test Artist",
                null,
                "/music/notag.mp3",
                200,
                "2019",
                EnumSet.noneOf(Tag.class)
        );

        dao.insert(track);   // nessun addTag

        JSONTrackDAO daoDopoRiavvio = new JSONTrackDAO(folder, FILE_NAME);
        Optional<Track> ricaricata = daoDopoRiavvio.searchById(track.getId());

        assertTrue(ricaricata.isPresent());
        assertFalse(ricaricata.get().hasTag(Tag.FAVOURITE),
                "Una traccia a cui non e' stato aggiunto il tag non deve averlo");
        assertTrue(ricaricata.get().getTags().isEmpty(),
                "L'insieme dei tag deve essere vuoto");
    }
}
