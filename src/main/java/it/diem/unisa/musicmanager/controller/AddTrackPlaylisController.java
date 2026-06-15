package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.command.AddTrackToPlaylistCommand;
import it.diem.unisa.musicmanager.command.Command;
import it.diem.unisa.musicmanager.command.RemoveTrackFromPlaylistCommand;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import it.diem.unisa.musicmanager.command.CommandManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddTrackPlaylisController {

    private Playlist playlist;
    private PlaylistService playlistService;
    private TrackService trackService;
    private CommandManager commandManager;

    private List<RowTrackController> rowControllers = new ArrayList<>();
    @FXML private VBox trackList;

    @FXML
    public void onSave(ActionEvent actionEvent) {
        for (RowTrackController controller : rowControllers) {
            Track track = controller.getTrack();

            boolean wasInPlaylist = playlist.containsTrack(track.getId());
            boolean isSelectedNow = controller.isSelected();

            if (isSelectedNow && !wasInPlaylist) {
                Command cmd = new AddTrackToPlaylistCommand(
                        playlistService, playlist.getId(), track.getId(),
                        track.getTitle(), playlist.getName());        // ← due nomi in più
                commandManager.executeCommand(cmd);

            } else if (!isSelectedNow && wasInPlaylist) {
                Command cmd = new RemoveTrackFromPlaylistCommand(
                        playlistService, playlist.getId(), track.getId(),
                        track.getTitle(), playlist.getName());        // ← due nomi in più
                commandManager.executeCommand(cmd);
            }
        }
        WindowUtil.close((Node) actionEvent.getSource());
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
    }

    private Runnable onSaveCallback;

    public void setOnSaveCallback(Runnable onSaveCallback) {
        this.onSaveCallback = onSaveCallback;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        checkLoading();
    }

    public void setPlaylistService(PlaylistService playlistService) {
        this.playlistService = playlistService;
        checkLoading();

    }

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
        checkLoading();
    }
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    private void checkLoading(){
        if (playlist != null && playlistService != null && trackService != null) {
            loadAllTracks();
        }
    }

    private void loadAllTracks() {
        trackList.getChildren().clear();
        rowControllers.clear();

        for (Track track : trackService.getAllTracks()) {

            try{
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));
                HBox row = loader.load();

                RowTrackController controller = loader.getController();
                controller.setTrack(track);

                boolean inPlaylist = playlist.containsTrack(track.getId());
                controller.setSelectionMode(true, inPlaylist);
                rowControllers.add(controller);
                trackList.getChildren().add(row);

            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }
    @FXML
    private void onCancel(javafx.event.ActionEvent event) {
        // Chiude la finestra corrente in modo sicuro e pulisce la mappa in WindowUtil
        it.diem.unisa.musicmanager.util.WindowUtil.close((javafx.scene.Node) event.getSource());
    }
}
