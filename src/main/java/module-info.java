module it.diem.unisa.musicplaylistmanager_sad {
    requires javafx.controls;
    requires javafx.fxml;


    opens it.diem.unisa.musicplaylistmanager_sad to javafx.fxml;
    exports it.diem.unisa.musicplaylistmanager_sad;
}