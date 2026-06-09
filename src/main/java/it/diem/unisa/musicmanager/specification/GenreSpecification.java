package it.diem.unisa.musicmanager.specification;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Track;

/** Seleziona le tracce di un determinato genere. */
public record GenreSpecification(Genre genre) implements Specification<Track> {

    @Override
    public boolean isSatisfiedBy(Track track) {
        return track.getGenre() == genre;
    }
}
