module it.diem.unisa.musicplaylistmanager_sad {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.google.gson;

    opens it.diem.unisa.musicmanager to javafx.fxml;
    exports it.diem.unisa.musicmanager;

    opens it.diem.unisa.musicmanager.controller to javafx.fxml;
    opens it.diem.unisa.musicmanager.model to com.google.gson;

    exports it.diem.unisa.musicmanager.testMain to javafx.graphics;
}