package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;
import java.util.Optional;

public interface PlayMode {

    Optional<QueueItem> nextItem(List<QueueItem> queue, QueueItem currentItem);

}
