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

    /**
     * Verifica se l'anno della traccia rientra nell'intervallo {@code [from, to]}.
     * Esclude immediatamente le tracce con anno nullo o impostato a "UNKNOWN".
     *
     * @param track il candidato di tipo Track da valutare
     * @return true se l'anno della traccia è valido e compreso nel range,
     * false altrimenti
     */
    @Override
    public boolean isSatisfiedBy(Track track) {
        String value = track.getYear();

        // Se l'anno è "UNKNOWN" (o nullo), la traccia non soddisfa il criterio numerico
        if (value == null || "UNKNOWN".equalsIgnoreCase(value)) {
            return false;
        }

        int year = Integer.parseInt(value);
        return year >= from && year <= to;
    }
}
