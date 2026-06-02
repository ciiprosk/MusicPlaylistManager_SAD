package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller della schermata "Playlists".
 * Mostra le playlist come griglia di card e permette di crearne di nuove
 * aprendo la finestra "Crea Playlist".
 */
public class PlaylistController {

    @FXML private TextField searchBar;
    @FXML private FlowPane playlistsGrid;

    private PlaylistService playlistService;
    private PlayerService playerService;
    private boolean isListenerAttached = false;

    @FXML
    public void initialize() throws IOException {
        // RICHIO NULLPOINTEECPTION  loadPlaylists();
    }

    /**
     * Apre la finestra modale "Crea Playlist".
     * Per ora apre solo la finestra (senza service).
     *
     * @param actionEvent evento generato dal click sul pulsante "Add Playlist"
     */
    @FXML
    private void handleAdd(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/addPlaylist.fxml"));
            Parent root = loader.load();

            // gli passiamo il service qui
            AddPlaylistController controller = loader.getController();
            controller.setPlaylistService(playlistService);

            Stage stage = new Stage();
            stage.setTitle("Add Playlist");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // metodi setere da chiamare per passare i service
    public void setPlaylistService(PlaylistService playlistService) {

        this.playlistService = playlistService;

        //bisgna iniizliazre il listener UNA SOLA VOLTAAA
        if(!isListenerAttached){
            playlistService.getPlaylists().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable obs) {
                    try {
                        loadPlaylists();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            isListenerAttached = true;
        }

    }
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Carica le playlist dal service e le mostra come card.
     * @throws IOException
     */
    public void loadPlaylists() throws IOException {
        playlistsGrid.getChildren().clear();

        for(Playlist p : playlistService.getPlaylists()){
            //per fare la ricerca

            if(searchBar != null && !searchBar.getText().isBlank()){
                if(!p.getName().toLowerCase().contains(searchBar.getText().toLowerCase())){
                    continue;
                }
            }
            playlistsGrid.getChildren().add(createPlaylistCard(p));
        }
    }

    /**
     * Crea un card per una playlist.
     * @param playlist
     * @return
     * @throws IOException
     */
    private Node createPlaylistCard(Playlist playlist) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/it/diem/unisa/musicmanager/components/playlistCard.fxml")
        );
        Node card = loader.load();
        PlaylistCardController controller = loader.getController();
        controller.setPlaylist(playlist); //gli passo la playlist ddi cui creare la card
        controller.setPlaylistService(playlistService); //i serviceeeee
        controller.setPlayerService(playerService);
        return card;
    }

}
