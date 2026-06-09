package it.diem.unisa.musicmanager.util;

import javafx.scene.control.ToggleButton;

import java.util.EnumSet;
import java.util.Set;

import it.diem.unisa.musicmanager.model.Tag;

public class TagUtils {
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
