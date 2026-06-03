package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerServiceTest {

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        // Lo SharedState è stato eliminato: inizializziamo solo il motore puro
        playerService = new PlayerService();
    }

    /**
     * Verifica che lo stato iniziale del lettore musicale sia
     * resettato correttamente al momento della creazione del servizio.
     */
    @Test
    void shouldHaveDefaultInitialState() {
        assertNull(playerService.currentTrackProperty().get(),
                "All'avvio non deve esserci alcun brano caricato.");

        assertFalse(playerService.isPlayingProperty().get(),
                "All'avvio il lettore non deve essere in stato di riproduzione.");

        assertEquals(0.0, playerService.progressProperty().get(), 0.001,
                "All'avvio l'avanzamento dello slider deve essere a 0.0.");
    }

    /**
     * Verifica che invocando il comando di pausa su un player vuoto
     * (senza file multimediali caricati nell'hardware), l'applicazione
     * gestisca la situazione in sicurezza rimanendo a 'false' senza crashare.
     */
    @Test
    void shouldKeepStateFalseIfPauseCalledWithoutMedia() {
        playerService.pause();

        assertFalse(playerService.isPlayingProperty().get(),
                "Lo stato deve rimanere false se non c'è musica in esecuzione.");
    }

    /**
     * Verifica che invocando il comando di resume su un player vuoto
     * lo stato rimanga correttamente disattivato.
     */
    @Test
    void shouldKeepStateFalseIfResumeCalledWithoutMedia() {
        playerService.resume();

        assertFalse(playerService.isPlayingProperty().get(),
                "Lo stato non deve passare a true se manca il file audio da riprendere.");
    }
}