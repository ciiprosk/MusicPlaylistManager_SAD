package it.diem.unisa.musicmanager.testMain;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.dao.JSONPlaylistDAO;
import it.diem.unisa.musicmanager.model.Playlist;

import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.model.Genre;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlaylistSystemTest {
    public static void main(String[] args) {
        // Configurazione: supponiamo di usare la cartella "data" nel progetto
        String folderPath = "data";
        String fileName = "playlists.jsonl";

        DAO<Playlist> dao = new JSONPlaylistDAO(folderPath, fileName);

        try {
            // 1. TEST INSERT
            System.out.println("--- Test Inserimento ---");
            Playlist p1 = new Playlist("La mia Playlist Rock");
            Track dummyTrack = new Track("Titolo Test", "Autore Test", Genre.ROCK, "path", 100, "2020", EnumSet.noneOf(Tag.class));
            p1.addTrack(dummyTrack);
            dao.insert(p1);
            System.out.println("Inserita: " + p1.getName() + " ID: " + p1.getId());

            // 2. TEST SELECT ALL
            System.out.println("\n--- Test Lettura (Select All) ---");
            List<Playlist> list = dao.selectAll();
            list.forEach(p -> System.out.println("Trovata: " + p.getName()));

            // 3. TEST UPDATE
            System.out.println("\n--- Test Update (Cambio Nome) ---");
            p1.setName("Rock Classico 2026");
            dao.update(p1);

            // Verifica update
            Optional<Playlist> updated = dao.searchById(p1.getId());
            updated.ifPresent(p -> System.out.println("Nuovo nome dopo update: " + p.getName()));
/*
            // 4. TEST DELETE
            System.out.println("\n--- Test Delete ---");
            dao.delete(p1.getId());

            Optional<Playlist> deleted = dao.searchById(p1.getId());
            if (deleted.isEmpty()) {
                System.out.println("Playlist eliminata correttamente!");
            }
*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}