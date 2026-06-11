package it.diem.unisa.musicmanager;

import it.diem.unisa.musicmanager.controller.MainController;
import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.dao.JSONPlaylistDAO;
import it.diem.unisa.musicmanager.dao.JSONTrackDAO;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.service.*;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import it.diem.unisa.musicmanager.model.Track;
import java.io.IOException;

public class MusicPlaylistManager extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //chiamo i dao e shared
        SharedState sharedState = new SharedState();
        DAO<Track> trackDAO = new JSONTrackDAO("data", "tracks.jsonl");
        DAO<Playlist> playlistDAO = new JSONPlaylistDAO("data", "playlists.jsonl");

        // creo i service e carico le tracce
        PersistenceService persistenceService = new PersistenceService(trackDAO, playlistDAO, sharedState);
        persistenceService.load();

        PlaylistService playlistService = new PlaylistService(playlistDAO, sharedState);
        TrackService trackService = new TrackService(trackDAO, sharedState);
        PlayerService playerService = new PlayerService();
        QueueService queueService = new QueueService(sharedState);

        playerService.setTrackService(trackService);
        playerService.setQueueService(queueService);

        trackService.addObserver(playlistService);
        trackService.addObserver(queueService);
        trackService.addObserver(playerService);

        //trackService.addObserver(deletedTrackId -> {
           // for (Playlist playlist : sharedState.getALlPlaylists()) {
              //  if (playlist.containsTrack(deletedTrackId)) {
                  //  playlistService.removeTrackFromPlaylist(
                   //         playlist.getId(),
                    //        deletedTrackId
                //    );
              //  }
          //  }
      //  });


        FXMLLoader fxmlLoader = new FXMLLoader(MusicPlaylistManager.class.getResource("MusicPlaylistManagerGUI.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);


        MainController controller = fxmlLoader.getController();
        controller.setPlaylistService(playlistService);
        controller.setQueueService(queueService);

        // passo service a home
        controller.getHomePageController().setQueueService(queueService);
        controller.getHomePageController().setTrackService(trackService);
        controller.getHomePageController().setPlayerService(playerService);
        controller.getHomePageController().setPlaylistService(playlistService);

        // paasso service a playlist
        controller.getPlaylistsPageController().setQueueService(queueService);
        controller.getPlaylistsPageController().setTrackService(trackService);
        controller.getPlaylistsPageController().setPlayerService(playerService);
        controller.getPlaylistsPageController().setPlaylistService(playlistService);
        controller.getPlaylistsPageController().loadPlaylists();

        // passo service a tracks
        controller.getTracksPageController().setQueueService(queueService);
        controller.getTracksPageController().setTrackService(trackService);
        controller.getTracksPageController().setPlayerService(playerService);
        controller.getTracksPageController().loadTracks();

        //passo service al player
        controller.getPlayerController().setPlayerService(playerService);
        controller.getPlayerController().setQueueService(queueService);
        controller.getPlayerController().setPlaylistService(playlistService);

        stage.setMinWidth(900);
        stage.setMinHeight(650);

        stage.setTitle("APP");
        stage.setScene(scene);
        stage.show();
    }


}
