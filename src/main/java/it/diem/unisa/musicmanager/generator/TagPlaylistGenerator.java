package it.diem.unisa.musicmanager.generator;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;

/**
 * Genera una playlist contenente tutte le tracce che hanno un certo tag.
 */

public class TagPlaylistGenerator implements PlaylistGenerator {
    private final Tag tag;

    public TagPlaylistGenerator(Tag tag) {
        this.tag = tag;
    }

   // @Override
    public Playlist generate(SharedState state) {
        Playlist playlist = new Playlist(tag.toString());

        for (Track t : state.getALlTracks()) {
            // una traccia puo' avere piu' tag: la prendo se contiene questo
            //if (t.getTags() != null && t.getTags().contains(tag)) {
              //  playlist.addTrack(t);
            }
        //}
        return playlist;
    }
}
