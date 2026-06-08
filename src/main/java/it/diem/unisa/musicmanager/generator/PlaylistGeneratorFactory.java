package it.diem.unisa.musicmanager.generator;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Tag;

/**
 * Factory che crea il PlaylistGenerator adatto al criterio scelto.
 * Il chiamante (PlaylistService) non conosce le classi concrete: chiede
 * un generatore per un certo criterio e riceve l'implementazione giusta.
 */

public class PlaylistGeneratorFactory {

    /** Crea un generatore che filtra le tracce per genere. */
    public static PlaylistGenerator byGenre(Genre genre) {
        return new GenrePlaylistGenerator(genre);
    }

    /** Crea un generatore che filtra le tracce per anno. */
    public static PlaylistGenerator byYear(String year) {
        return new YearPlaylistGenerator(year);
    }

    /** Crea un generatore che filtra le tracce per tag. */
    public static PlaylistGenerator byTag(Tag tag) {
        return new TagPlaylistGenerator(tag);
    }
}
