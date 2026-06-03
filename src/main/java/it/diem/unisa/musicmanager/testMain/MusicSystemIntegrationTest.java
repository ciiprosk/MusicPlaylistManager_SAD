package it.diem.unisa.musicmanager.testMain;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.dao.JSONPlaylistDAO;
import it.diem.unisa.musicmanager.dao.JSONTrackDAO;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;

import java.util.Optional;

public class MusicSystemIntegrationTest {
    public static void main(String[] args) {
        String folderPath = "data";

        // Inizializzazione DAO
        DAO<Track> trackDAO = new JSONTrackDAO(folderPath, "tracks.jsonl");
        DAO<Playlist> playlistDAO = new JSONPlaylistDAO(folderPath, "playlists.jsonl");

        try {
            System.out.println("--- 1. Creazione e Salvataggio Tracce ---");
            Track t1 = new Track("Bohemian Rhapsody", "Queen", Genre.ROCK, "/path/song1.mp3", 354, "1975");
            Track t2 = new Track("Hotel California", "Eagles", Genre.ROCK, "/path/song2.mp3", 400, "1976");

            trackDAO.insert(t1);
            trackDAO.insert(t2);
            System.out.println("Tracce salvate: " + t1.getTitle() + " e " + t2.getTitle());

            System.out.println("\n--- 2. Creazione Playlist con riferimenti alle tracce ---");
            Playlist p1 = new Playlist("I Miei Classici");

            // Aggiungiamo i riferimenti (UUID) delle tracce alla playlist
            p1.addTrack(t1.getId());
            p1.addTrack(t2.getId());

            playlistDAO.insert(p1);
            System.out.println("Playlist '" + p1.getName() + "' creata con " + p1.numberOfTrakcs() + " tracce.");

            System.out.println("\n--- 3. Verifica Integrazione (Recupero Playlist e traccia) ---");
            Optional<Playlist> savedPlaylist = playlistDAO.searchById(p1.getId());

            if (savedPlaylist.isPresent()) {
                Playlist p = savedPlaylist.get();
                System.out.println("Playlist recuperata: " + p.getName());

                // Per ogni UUID nella playlist, cerchiamo la traccia corrispondente nel TrackDAO
                for (var trackId : p.getTracks()) {
                    Optional<Track> trackFound = trackDAO.searchById(trackId);
                    trackFound.ifPresent(t ->
                            System.out.println(" - Contiene traccia: " + t.getTitle() + " di " + t.getAuthor())
                    );
                }
            }

            System.out.println("\n--- 4. Test Rimozione Traccia da Playlist ---");
            p1.removeTrack(t1.getId());
            playlistDAO.update(p1);
            System.out.println("Traccia rimossa. Nuova dimensione: " + playlistDAO.searchById(p1.getId()).get().numberOfTrakcs());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}