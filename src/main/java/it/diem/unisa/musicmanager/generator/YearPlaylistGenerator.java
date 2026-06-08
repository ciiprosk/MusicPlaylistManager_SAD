package it.diem.unisa.musicmanager.generator;

import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;

/**
 * Genera una playlist contenente tutte le tracce di un determinato anno.
 * ConcreteProduct
 */
public class YearPlaylistGenerator implements PlaylistGenerator{

    // L'anno scelto dall'utente, su cui filtrare (es. "2020").
    private final String year;

    public YearPlaylistGenerator(String year) {
        this.year = year;
    }

    @Override
    public Playlist generate(SharedState state) {
        Playlist playlist = new Playlist("Best of " + year);

        for (Track t : state.getALlTracks()) {
            if (t.getYear().equals(year)) {
                playlist.addTrack(t);
            }
        }
        return playlist;
    }

}
