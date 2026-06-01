module it.diem.unisa.musicplaylistmanager_sad {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires javafx.media;

    opens it.diem.unisa.musicmanager to javafx.fxml;
    exports it.diem.unisa.musicmanager;

    opens it.diem.unisa.musicmanager.controller to javafx.fxml;
    opens it.diem.unisa.musicmanager.model to com.google.gson;
}