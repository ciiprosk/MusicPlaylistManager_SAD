package it.diem.unisa.musicmanager.specification;

import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;

import java.util.EnumSet;

/**
 * Seleziona le tracce che contengono i tag richiesti.
 */
public record TagSpecification(Tag tag) implements Specification<Track> {

    /**
     * Verifica se la traccia specificata soddisfa il criterio, controllando
     * che la collezione dei tag della traccia non sia nulla e che contenga
     * il tag racchiuso in questa specifica.
     *
     * @param track il candidato di tipo Track da valutare
     * @return true si se la traccia contiene il tag richiesto,
     * false altrimenti (incluso il caso in cui la traccia non abbia tag)
     */
    @Override
    public boolean isSatisfiedBy(Track track) { return track.getTags() != null && track.getTags().contains(tag); }
}