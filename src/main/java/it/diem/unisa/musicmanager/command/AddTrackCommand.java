package it.diem.unisa.musicmanager.command;

import it.diem.unisa.musicmanager.model.Genre;
import it.diem.unisa.musicmanager.model.Tag;
import it.diem.unisa.musicmanager.model.Track;
import it.diem.unisa.musicmanager.service.TrackService;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AddTrackCommand implements Command {

    //ci servono come attributi il service di Track (per le operazioni CRUD)
    //e gli attributi di Track
    private final TrackService service;
    private final String title;
    private final String author;
    private final String year;
    private final String songPath;
    private final Genre genre;
    private final int songLength;
    private final Set<Tag> tags;
    private UUID createdId;   // catturato dopo execute(), serve all'undo

    public AddTrackCommand(TrackService service, String title, String author, String year, String songPath, Genre genre, int songLength, Set<Tag> tags) {

        this.service = service;
        this.title = title;
        this.author = author;
        this.year = year;
        this.songPath = songPath;
        this.genre = genre;
        this.songLength = songLength;
        this.tags = tags;

    }

    @Override
    public Optional<String> execute() {
        Optional<String> error = service.addTrack(title, author, genre, songPath, songLength, year, tags);
        if (error.isEmpty()) {

            //l'autore va gestito
            String normalizedAuthor = (author == null || author.trim().isEmpty())
                    ? "Unknown" : author.trim();

            // cerca la traccia appena creata per recuperarne l'id
            createdId = service.getAllTracks().stream()
                    .filter(t -> t.getTitle().equals(title.trim()) && t.getAuthor().equals(normalizedAuthor))
                    .findFirst()
                    .map(Track::getId)
                    .orElse(null);
        }
        return error;
    }

    @Override
    public void undo() {
        if (createdId != null) {
            service.deleteTrack(createdId);
        }
    }

    @Override
    public String getDescription() {
        return "Add track \"" + title + "\"";
    }
}
