package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.io.IOException;

public class PlaylistController {

    //chiede al service le playlist e la creazione delle playlists
    private PlaylistService playlistService;
    private PlayerService playerService;

    @FXML private FlowPane playlistsGrid;
    @FXML private TextField searchBar;


    @FXML
    public void initialize() throws IOException {
        loadPlaylists();
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

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

    private Node createPlaylistCard(Playlist playlist) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/it/diem/unisa/musicmanager/components/playlistCard.fxml")
        );
        Node card = loader.load();
        PlaylistCardController controller = loader.getController();
        controller.setPlaylist(playlist);
        controller.setPlaylistService(playlistService);
        controller.setPlayerService(playerService);
        //controller.setOnOpenDetails(this::openDetailsModal); // callback opzionale
        return card;
    }

    @FXML
    public void openCreatePlaylist(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/pages/createPlaylist.fxml"));
        Parent root = loader.load();


    }
}


