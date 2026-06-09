package it.diem.unisa.musicmanager.specification;

import it.diem.unisa.musicmanager.model.Track;

/**
 * Seleziona le tracce di un anno, o di un intervallo di anni [from, to] inclusivo.
 *
 * Track.getYear() e' una String ("1999" oppure "UNKNOWN"): le tracce con anno
 * sconosciuto o in formato non valido non rientrano in nessun intervallo.
 */
public record YearSpecification(int from, int to) implements Specification<Track> {

    /** Canzoni di un singolo anno: new YearSpecification(1999). */
    public YearSpecification(int year) {
        this(year, year);
    }

    @Override
    public boolean isSatisfiedBy(Track track) {
        String value = track.getYear();
        if (value == null || !value.matches("\\d{4}")) {
            return false; // "UNKNOWN" o formato non valido
        }
        int year = Integer.parseInt(value);
        return year >= from && year <= to;
    }
}
