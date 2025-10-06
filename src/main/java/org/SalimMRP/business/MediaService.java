package org.SalimMRP.business;

import org.SalimMRP.persistence.MediaRepository;
import org.SalimMRP.persistence.models.Media;
import java.util.List;

public class MediaService {

    private final MediaRepository repo = new MediaRepository();

    public boolean createMedia(Media media) {
        if (media.getTitle() == null || media.getMediaType() == null) return false;
        return repo.save(media);
    }

    public List<Media> getAllMedia() {
        return repo.findAll();
    }

    public Media getMediaById(int id) {
        return repo.findById(id);
    }

    public boolean updateMedia(Media media) {
        return repo.update(media);
    }

    public boolean deleteMedia(int id) {
        return repo.delete(id);
    }
}
