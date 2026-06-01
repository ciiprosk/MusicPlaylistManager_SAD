module it.diem.unisa.musicplaylistmanager_sad {
    requires javafx.controls;
    requires javafx.fxml;

    opens it.diem.unisa.musicmanager to javafx.fxml;
    exports it.diem.unisa.musicmanager;

    // AGGIUNGI QUESTA RIGA:
    // Permette a JavaFX di accedere tramite reflection alle classi dei controller
    opens it.diem.unisa.musicmanager.controller to javafx.fxml;
}