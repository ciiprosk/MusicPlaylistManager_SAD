package it.diem.unisa.musicmanager.specification;

import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;

import java.util.EnumSet;

/**
 * Seleziona le tracce che contengono i tag richiesti.
 */
public record TagSpecification(EnumSet<Tag> tags) implements Specification<Track> {

    @Override
    public boolean isSatisfiedBy(Track track) {
        if (tags == null || tags.isEmpty()) {
            return true;
        }

        for (Tag t : tags) {
            if (track.getTags().contains(t)) {
                return true;
            }
        }

        return false;
    }
}