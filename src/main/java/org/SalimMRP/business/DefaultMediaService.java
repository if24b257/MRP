package org.SalimMRP.business;

import org.SalimMRP.persistence.MediaRepository;
import org.SalimMRP.persistence.models.Media;

import java.util.List;
import java.util.Objects;

// Verwaltet Media-Einträge und kapselt die Regeln für Speichern, Aktualisieren und Löschen.
public class DefaultMediaService implements MediaService {

    private final MediaRepository mediaRepository;

    // Repository wird per Konstruktor übergeben, damit Test-Doubles verwendet werden können.
    public DefaultMediaService(MediaRepository mediaRepository) {
        this.mediaRepository = Objects.requireNonNull(mediaRepository, "mediaRepository must not be null");
    }

    @Override
    public boolean createMedia(Media media) {
        // Nur valide Medien werden gespeichert.
        return isValid(media) && mediaRepository.save(media);
    }

    @Override
    public List<Media> getAllMedia() {
        return mediaRepository.findAll();
    }

    @Override
    public Media getMediaById(int id) {
        return mediaRepository.findById(id);
    }

    @Override
    public boolean updateMedia(Media media) {
        return media != null
                && media.getId() > 0
                && isValid(media)
                && mediaRepository.update(media);
    }

    @Override
    public boolean deleteMedia(int id) {
        return mediaRepository.delete(id);
    }

    // Zentrale Validierung: Titel, Typ und Ersteller müssen vorhanden sein.
    private boolean isValid(Media media) {
        return media != null
                && media.getTitle() != null && !media.getTitle().isBlank()
                && media.getMediaType() != null && !media.getMediaType().isBlank()
                && media.getCreatedByUserId() > 0;
    }
}
