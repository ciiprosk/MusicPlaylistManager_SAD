package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.state.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerServiceTest {

    private PlayerService playerService;
    private SharedState sharedState;

    @BeforeEach
    void setUp() {
        sharedState = new SharedState();
        playerService = new PlayerService(sharedState);
    }

    //TEST STATO CORRENTE DEL PLAYER

    @Test
    void shouldProvideCurrentPlaybackState() {
        sharedState.getIsPlaying().set(true);

        assertTrue(
                playerService.getSharedState().getIsPlaying().get()
        );

        sharedState.getIsPlaying().set(false);

        assertFalse(
                playerService.getSharedState().getIsPlaying().get()
        );
    }
}