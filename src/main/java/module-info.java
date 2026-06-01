module it.diem.unisa.musicplaylistmanager_sad {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens it.diem.unisa.musicmanager to javafx.fxml;
    exports it.diem.unisa.musicmanager;

    opens it.diem.unisa.musicmanager.controller to javafx.fxml;
}