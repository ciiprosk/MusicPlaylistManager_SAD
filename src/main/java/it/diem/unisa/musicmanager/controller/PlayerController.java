package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.service.PlayerService;
import javafx.event.ActionEvent;

/**
 * Controller della player bar.
 * Tiene un riferimento al PlayerService e delega a esso le azioni dei pulsanti.
 * Resta volutamente "sottile": tutta la logica di riproduzione vive nel service.
 */
public class PlayerController {

    // Service di riproduzione, iniettato da chi carica questo controller.
    private PlayerService playerService;

    /**
     * @param playerService il service di riproduzione da usare
     */
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Pulsante play/pausa: alterna riproduzione e pausa tramite il service.
     */
    public void handlePlayPause(ActionEvent actionEvent) {
    }

    public void handleNext(ActionEvent actionEvent) {
    }

    public void handleSkipPlaylist(ActionEvent actionEvent) {
    }

    public void handleChangeMode(ActionEvent actionEvent) {
    }

    public void handleQueue(ActionEvent actionEvent) {
    }
}
