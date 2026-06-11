package it.diem.unisa.musicmanager.specification;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Track;

/** Seleziona le tracce di un determinato genere. */
public record GenreSpecification(Genre genre) implements Specification<Track> {

    /**
     * {@inheritDoc}
     * Verifica se il genere musicale della traccia corrisponde esattamente
     * al genere racchiuso in questa specifica.
     *
     * @param track il candidato di tipo Track da valutare
     * @return true se il genere della traccia coincide, false altrimenti
     */
    @Override
    public boolean isSatisfiedBy(Track track) {
        return track.getGenre() == genre;
    }
}
