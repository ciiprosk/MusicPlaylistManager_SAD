package it.diem.unisa.musicmanager.model;

import java.util.List;
import java.util.UUID;

/**
 * Interfaccia comune per gli elementi riproducibili all'interno dell'applicazione (es. Tracce e Playlist).
 * Fornisce metodi uniformi per ottenere gli ID, il tipo di elemento e l'elenco di tracce associate.
 */
public interface Playable {

    /**
     * Restituisce la lista di tracce da riprodurre per questo elemento.
     *
     * @return Una lista di oggetti {@link Track}.
     */
    List<Track> getTracksToPlay();

    /**
     * Restituisce il tipo specifico di elemento della coda (TRACK o PLAYLIST).
     *
     * @return Il {@link QueueItemType} corrispondente.
     */
    QueueItemType getType();

    /**
     * Restituisce l'identificativo univoco dell'elemento.
     *
     * @return L'UUID associato.
     */
    UUID getId();
}
