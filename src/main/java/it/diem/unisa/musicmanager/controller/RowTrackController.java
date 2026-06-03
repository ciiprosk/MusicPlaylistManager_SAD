package it.diem.unisa.musicmanager.controller;

import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.PlayerService;
import it.diem.unisa.musicmanager.service.TrackService;
import it.diem.unisa.musicmanager.state.SharedState;
import it.diem.unisa.musicmanager.util.AlertUtil;
import it.diem.unisa.musicmanager.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.IOException;

public class RowTrackController {

    // ho sbigno della singola tracca e del service per gestire le cosed ipersistenza
    private Track track;
    private TrackService trackService;
    private PlayerService playerService;
    private SharedState sharedState;
    private Runnable onDeleteAction;    //ci aiuta a capire se stiamo eliminando dall'archivio tracce
    //o dalla playlist


    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblDuration;
    @FXML private javafx.scene.layout.HBox rootContainer;

    @FXML private Button buttonMenu;
    @FXML private Button btnModify;
    @FXML private Button btnPlay;

    @FXML private CheckBox checkkSelect;

    public void setSelectionMode(boolean isSelectionMode, boolean isAlreadyInPlaylist) {
        // Mostra la checkbox
        checkkSelect.setVisible(isSelectionMode);
        checkkSelect.setManaged(isSelectionMode);
        checkkSelect.setSelected(isAlreadyInPlaylist);

        // Nascondi i bottoni play/modifica/elimina se siamo in modalità selezione
        btnPlay.setVisible(!isSelectionMode);
        btnPlay.setManaged(!isSelectionMode);
        btnModify.setVisible(!isSelectionMode);
        btnModify.setManaged(!isSelectionMode);
//        btnDelete.setVisible(!isSelectionMode);
//        btnDelete.setManaged(!isSelectionMode);
    }

    public void setOnDeleteAction(Runnable onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
    }

    public boolean isSelected() {
        return checkkSelect.isSelected();
    }

    public void setTrack(Track track) {
        this.track = track;
        lblTitle.setText(track.getTitle());
        lblAuthor.setText(track.getAuthor());

        int minutes = (int) (track.getSongLength() / 60);
        int seconds = (int) (track.getSongLength() % 60);

        lblDuration.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public void setTrackService(TrackService trackService) {
        this.trackService = trackService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
        this.sharedState = playerService.getSharedState();

        // Quando cambia il brano corrente o lo stato play/pausa,
        // ogni riga ridisegna il proprio bottone.
        sharedState.getCurrentTrack().addListener((o, ov, nv) -> {
            updateButton();
            updateCurrentTrackStyle();  //evidenzia traccia in ascolto
        });
        sharedState.getIsPlaying().addListener((o, ov, nv) -> updateButton());
        updateButton(); // stato iniziale
        updateCurrentTrackStyle();  //evidenzia traccia in ascolto allo stato iniziale
    }

    // true solo se QUESTA riga è il brano corrente E sta suonando
    private boolean isThisPlaying() {
        return track != null
                && track.equals(sharedState.getCurrentTrack().get())
                && sharedState.getIsPlaying().get();
    }

    private void updateButton() {
        btnPlay.setText(isThisPlaying() ? "⏸" : "▶");
    }

    @FXML
    public void handleModify(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/resources//musicmanager/pages/editSong.fxml", "", Modality.WINDOW_MODAL);
        AddSongController controller = loader.getController();
        //creo i set dei service nei controller
    }
    @FXML
    public void handlePlay(ActionEvent actionEvent) {
        if (playerService == null || track == null) return;

        if (isThisPlaying()) {
            playerService.pause();   // sto suonando io → metti in pausa
        } else {
            playerService.play(track); // non sto suonando → parti (o riprendi)
        }
    }

    @FXML
    public void handleDelete(ActionEvent actionEvent) {
        if (trackService != null && track != null) {

            //alert di conferma
            boolean isConfirmed = AlertUtil.showConfirmation("Delete Confirm", "Are you sure you wanna delete this track?");

            //se l'utente ha confermato, allora elimino la traccia (o dalla playlist, o dall'archivio)
            if(isConfirmed && onDeleteAction != null)
                onDeleteAction.run();

        }
    }

    public void handleMenu(ActionEvent actionEvent) throws IOException{

            if (track == null) return;

            ContextMenu menu = new ContextMenu();

            MenuItem detailItem = new MenuItem("Open Detail");

                detailItem.setOnAction(e -> openDetail());

                MenuItem modifyItem = new MenuItem("Modify Name");
                modifyItem.setOnAction(e -> openEditPlaylist());

                MenuItem deleteItem = new MenuItem("Delete Track");
                deleteItem.setOnAction(e -> handleDelete(null));

                menu.getItems().addAll(detailItem, modifyItem, deleteItem);
                menu.show(buttonMenu, Side.BOTTOM, 0, 0);

    }

    private void openDetail()  {
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/detailSong.fxml", track.getTitle(), Modality.WINDOW_MODAL);
            DetailSongController controller = loader.getController();
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    //metodo per evidenziare qual è la traccia in ascolto
    private void updateCurrentTrackStyle() {

        //reset della traccia corrente
        rootContainer.getStyleClass().remove("brano-row-playing");

        if (track != null && track.equals(sharedState.getCurrentTrack().get())) {
            rootContainer.getStyleClass().add("brano-row-playing");
        }
    }

    private void openEditPlaylist(){
        try {
            FXMLLoader loader = WindowUtil.openWindow("/it/diem/unisa/musicmanager/pages/editSong.fxml", track.getTitle(), Modality.WINDOW_MODAL);
            AddSongController controller = loader.getController();
            controller.setTrackService(trackService);
        }catch(IOException e){}
    }

    public Track getTrack() {
        return track;
    }


}
