package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Playlist;
//import it.diem.unisa.musicmanager.service.PlaylistService;
//import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.PlaylistService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.io.IOException;
import java.util.UUID;

/**
 * Controller della schermata di dettaglio playlist (detailedPlaylist.fxml).
 * Mostra in alto il nome della playlist e sotto l'elenco delle tracce
 * (titolo, autore, durata).
 * Per ora gestisce la View: il nome viene mostrato; il riempimento delle tracce
 * a partire dagli UUID richiede il TrackService (verra' collegato dopo).
 */
public class DetailedPlaylistController {

    // Campi dell'interfaccia, collegati agli fx:id in detailedPlaylist.fxml ---
    @FXML private Label labelName;
    @FXML private Label lblTrackCount;
    @FXML private VBox trackList;


    // La playlist mostrata.
    private Playlist playlist;

    private TrackService trackService;
    private PlaylistService playlistService;
    private PlayerService playerService;
    private boolean isTrackListenerAttached = false;

    /**
     * Chiamato automaticamente da JavaFX appena la schermata e' pronta.
     * Definisce come mostrare ogni traccia: titolo, autore e durata.
     */
    @FXML
    private void initialize() {

    }

    /**
     * Imposta la playlist da visualizzare e ne mostra il nome e il conteggio.
     *
     * @param playlist la playlist di cui mostrare il dettaglio
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        labelName.setText(playlist.getName());
        updateTrackCount();
        if (trackService != null && playerService != null) {
            loadTracks();
        }


        // Quando il TrackService sara' disponibile, qui risolveremo gli UUID
        // della playlist nei rispettivi Track e riempiremo la lista:
        //
        // List<Track> tracce = playlist.getTracks().stream()
        //         .map(id -> trackService.findById(id))   // metodo da aggiungere al service
        //         .filter(Objects::nonNull)
        //         .toList();
        // tracksList.getItems().setAll(tracce);
    }

    public void setPlaylistService(PlaylistService playlistService){
        this.playlistService = playlistService;

    }
    public void setTrackService(TrackService trackService){
        this.trackService = trackService;
        createTrackListener();
        if (this.playlist != null && this.playerService != null) {
            loadTracks();
        }
    }


    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        if (this.playlist != null && this.trackService != null) {
            loadTracks();
        }
    }




    private void updateTrackCount() {
        if (playlist == null) return;
        int n = playlist.getTracksList().size();
        lblTrackCount.setText(n + (n == 1 ? " track" : " tracks"));
    }

    private void createTrackListener() {
        if (!isTrackListenerAttached && trackService != null) {
            trackService.getAllTracks().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (playlist != null) {
                        javafx.application.Platform.runLater(() -> loadTracks());
                    }
                }
            });

            isTrackListenerAttached = true;
        }
    }

    private void loadTracks(){
        trackList.getChildren().clear();

        for(Track track : playlist.getTracksList()){
            try{
                Track updatedTrack = trackService.searchTrackById(track.getId())
                        .orElse(track);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/diem/unisa/musicmanager/components/trackRow.fxml"));
                HBox row = loader.load();

                RowTrackController controller = loader.getController();
                controller.setTrack(updatedTrack);
                controller.setPlayerService(playerService);
                controller.setTrackService(trackService);

                //se premo il tasto elimina, rimuovo la traccia dalla playlist
                controller.setOnDeleteAction(() -> {
                    if (playlistService != null) {
                        playlistService.removeTrackFromPlaylist(playlist.getId(), track.getId());
                        javafx.application.Platform.runLater(() -> {
                            updateTrackCount();
                            loadTracks();
                        });
                    }
                });

                trackList.getChildren().add(row);

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void onModify(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml", playlist.getName(),Modality.APPLICATION_MODAL);

            EditPlaylistController controller = loader.getController();

            controller.setPlaylistService(playlistService);
            controller.setPlaylist(playlist);

            /*
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/diem/unisa/musicmanager/pages/editPlaylist.fxml"));
            Parent root = loader.load();

            EditPlaylistController controller = loader.getController();
            controller.setPlaylist(playlist);

            controller.setPlaylistService(playlistService);
            //close(actionEvent);

            Stage stage = new Stage();
            stage.setTitle("Modifica Playlist");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            */
            WindowUtil.close( (Node) actionEvent.getSource());


            // Dopo la chiusura, aggiorniamo il nome mostrato (potrebbe essere cambiato).
            if (playlist != null) {
                labelName.setText(playlist.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onAddTrack(ActionEvent actionEvent) {
        //con trackseevice aggiungo una traccia alla playlist ma manca una view
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/addTrackPlaylist.fxml", playlist.getName(), Modality.APPLICATION_MODAL);
            AddTrackPlaylisController controller= loader.getController();

            //gli passo la playlist corrente e il service

            controller.setPlaylistService(playlistService);
            controller.setTrackService(trackService);
            controller.setPlaylist(playlist);
            controller.setOnSaveCallback(() -> {
                javafx.application.Platform.runLater(() -> {
                    updateTrackCount();
                    loadTracks();
                });
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void onDelete(ActionEvent actionEvent) {
        //uso il service per eliminare la playlist
        deletePlaylist();
        //close(actionEvent);
        WindowUtil.close( (Node) actionEvent.getSource());

    }

    /*
    private void close(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }

     */

    private void deletePlaylist() {
        if (playlistService == null || playlist == null) return;

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete the Playlist \"" + playlist.getName() + "\"?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                playlistService.deletePlaylist(playlist.getId());
            }
        });
    }


}