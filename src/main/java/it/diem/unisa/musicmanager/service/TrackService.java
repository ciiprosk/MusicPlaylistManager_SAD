package it.diem.unisa.musicmanager.service;

import it.diem.unisa.musicmanager.dao.DAO;
import it.diem.unisa.musicmanager.exception.TrackInfoException;
import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Playlist;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.state.SharedState;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
// bisgna imolemneare a logica crud ei brano e l'agiornmaento di sharedstate
public class TrackService {

    private final DAO<Track> trackDAO;  //serve per operazioni CRUD sulle tracce
    private final SharedState sharedState;  //serve per lo stato condiviso delle tracce

    private final List<TrackObserver> observers = new ArrayList<>();

    public TrackService(DAO<Track> trackDAO, SharedState sharedState) {
        this.trackDAO = trackDAO;
        this.sharedState = sharedState;
    }

    public ObservableList<Track> getAllTracks() {
        return sharedState.getALlTracks();  //compito delegato dallo stato condiviso, caricato dal Persistance Service
    }

    public Optional<Track> searchTrackById(UUID trackId) {
        return trackDAO.searchById(trackId);
    }

    public Optional<String> addTrack(String title, String author, Genre genre, String songPath, int songLength, String year){
        try{

            Track track = new Track(title, author, genre, songPath, songLength, year);

            if(trackDAO.isDuplicated(track))
                return Optional.of("Error: A track with this title and author already exists!");

            // inserisco traccia nel Json
            trackDAO.insert(track);
            sharedState.getALlTracks().add(track);
            return Optional.empty();

        } catch (TrackInfoException e){     //Se sono qui, ho avuto errore validazione dati track
            return Optional.of(e.getMessage());
        }
    }

    public Optional<String> updateTrack(UUID trackId, String newTitle, String newAuthor, Genre newGenre, String newYear){
        //come parametri del metodo, tutte le info che possono cambiare di una traccia + Id perché ci serve per cercare nel DAO
        Optional<Track> optionalTrack = trackDAO.searchById(trackId);

        if (optionalTrack.isEmpty()) {
            return Optional.of("Error: Track not found.");
        }

        Track track = optionalTrack.get();  //se non è vuoto, non è più Optional

        try {

            //creo una traccia temporanea
            //così avviene anche validazione dei nuovi campi
            Track tempTrack = new Track(newTitle, newAuthor, newGenre, track.getSongPath(), track.getSongLength(), newYear);

            //verifica duplicato: id diversi, stesso titolo e stesso autore
            boolean isDuplicate = sharedState.getALlTracks().stream()
                    .anyMatch(t -> !t.getId().equals(trackId) && t.isDuplicate(tempTrack));

            if (isDuplicate)
                return Optional.of("Error: A track with this title and author already exists!");

            //aggiornamento campi dell'oggetto track

            track.setTitle(newTitle);
            track.setAuthor(newAuthor);
            track.setGenre(newGenre);
            track.setYear(newYear);

            //update della traccia con DAO
            trackDAO.update(track);

            //avviso interfaccia grafica del cambiamento
            updateInState(track);

            return Optional.empty();    //vuol dire che non c'è nessun errore

        } catch (TrackInfoException e) {    //finisco qui se non va a buon fine la validazione
            return Optional.of(e.getMessage());
        }

    }

    public List<Track> searchTracks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>(sharedState.getALlTracks());
        }
        String lowerKeyword = keyword.toLowerCase();
        return sharedState.getALlTracks().stream()
                .filter(t -> t.getTitle().toLowerCase().contains(lowerKeyword) || 
                             t.getAuthor().toLowerCase().contains(lowerKeyword))
                .toList();
    }


    public void addObserver(TrackObserver observer) {

        this.observers.add(observer);

    }

    public void deleteTrack(UUID trackId){

        trackDAO.delete(trackId);   //eliminazione traccia dall'archivio

        sharedState.getALlTracks().removeIf(t -> t.getId().equals(trackId));    //eliminazione visiva della traccia

        for (TrackObserver observer : observers) {  //tutti gli Observer delle tracce vengono avvisati che la traccia è stata eliminata
            observer.onTrackDeleted(trackId);
        }

    }

    //si occupa dell'update della traccia nell'interfaccia grafica
    private void updateInState(Track track){

        ObservableList<Track> tracks = sharedState.getALlTracks();

        java.util.stream.IntStream.range(0, tracks.size()) //scorre gli indici
                .filter(index -> tracks.get(index).getId().equals(track.getId())) //prende la traccia da aggiornare
                .findFirst()    //prima occorrenza, così da non dover scorrere dopo aver trovato la traccia giusta
                .ifPresent(index -> tracks.set(index, track));  //se c'è, allora si aggiorna

    }

     // Incrementa il numero di ascolti di una traccia e salva la modifica su file.
     // @param trackId identificatore UUID della traccia ascoltata.
     // @return Optional.empty() se l'operazione va a buon fine,
     // Optional con messaggio di errore se la traccia non viene trovata.
    public Optional<String> incrementPlayCount(UUID trackId) {

        Optional<Track> optionalTrack = trackDAO.searchById(trackId);

        if (optionalTrack.isEmpty()) {
            return Optional.of("Error: Track not found.");
        }

        Track track = optionalTrack.get();

        track.incrementPlayCount();

        trackDAO.update(track);

        updateInState(track);

        return Optional.empty();
    }

     // Restituisce le 5 tracce più ascoltate presenti nello stato condiviso.
     // Le tracce vengono ordinate in modo decrescente in base al numero di ascolti.
     // Se le tracce disponibili sono meno di 5, vengono restituite tutte
     // @return lista delle tracce più ascoltate, massimo 5 elementi.

    public List<Track> getTop5MostPlayedTracks() {
        return sharedState.getALlTracks()
                .stream()
                .sorted((t1, t2) -> Integer.compare(t2.getPlayCount(), t1.getPlayCount()))
                .limit(5)
                .toList();
    }

}
