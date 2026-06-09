package it.diem.unisa.musicmanager.playmode;

import it.diem.unisa.musicmanager.model.QueueItem;

import java.util.List;

public interface PlayMode {

    QueueItem nextItem(List<QueueItem> queue, int currentIndex);

}
