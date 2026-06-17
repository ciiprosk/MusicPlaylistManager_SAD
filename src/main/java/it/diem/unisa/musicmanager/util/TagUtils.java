package it.diem.unisa.musicmanager.util;

import javafx.scene.control.ToggleButton;

import java.util.EnumSet;
import java.util.Set;

import it.diem.unisa.musicmanager.model.Tag;

/**
 * Classe di utilità per la manipolazione e la conversione dei tag della traccia.
 * Offre metodi per mappare gli elementi dell'interfaccia grafica (come i ToggleButton) 
 * verso gli elementi di business (gli enum {@link Tag}).
 */
public class TagUtils {

    /**
     * Costruttore di default.
     */
    public TagUtils() {
        // Costruttore di default
    }

    /**
     * Converte lo stato di selezione di tre {@link ToggleButton} della GUI in un insieme di tag ({@link Tag}).
     * Restituisce un set contenente solo i tag corrispondenti ai bottoni spuntati (selezionati).
     * 
     * @param explicit   Il bottone grafico per il tag EXPLICIT.
     * @param favorite   Il bottone grafico per il tag FAVOURITE.
     * @param newRelease Il bottone grafico per il tag NEWRELEASE.
     * @return Un insieme {@link Set} di {@link Tag} derivato dallo stato dei bottoni grafici.
     */
    public static Set<Tag> fromToggles(ToggleButton explicit,
                                       ToggleButton favorite,
                                       ToggleButton newRelease) {

        EnumSet<Tag> tags = EnumSet.noneOf(Tag.class);

        if (explicit.isSelected()) tags.add(Tag.EXPLICIT);
        if (favorite.isSelected()) tags.add(Tag.FAVOURITE);
        if (newRelease.isSelected()) tags.add(Tag.NEWRELEASE);

        return tags;
    }
}
