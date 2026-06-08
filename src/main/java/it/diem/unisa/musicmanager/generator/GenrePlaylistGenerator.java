package it.diem.unisa.musicmanager.generator;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;

/**
 * Genera una playlist contenente tutte le tracce di un determinato genere.
 */

public class GenrePlaylistGenerator implements PlaylistGenerator{
    // Il genere scelto dall'utente, su cui filtrare.
    private final Genre genre;

    public GenrePlaylistGenerator(Genre genre) {
        this.genre = genre;
    }

    @Override
    public Playlist generate(SharedState state) {
        // Il nome della playlist e' il genere stesso (es. "ROCK").
        Playlist playlist = new Playlist(genre.toString());

        // Aggiungo solo le tracce di quel genere.
        for (Track t : state.getALlTracks()) {
            if (t.getGenre() == genre) {
                playlist.addTrack(t);
            }
        }
        return playlist;
    }
}


