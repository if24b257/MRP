package org.SalimMRP.business;

import org.SalimMRP.business.dto.MediaDetails;
import org.SalimMRP.business.dto.MediaSearchCriteria;
import org.SalimMRP.persistence.models.Media;

import java.util.List;

// Beschreibt die Fähigkeiten, die ein Media-Service bereitstellen muss (CRUD über Media-Objekte).
public interface MediaService {

    boolean createMedia(Media media);

    List<Media> getAllMedia();

    Media getMediaById(int id);

    boolean updateMedia(Media media);

    boolean deleteMedia(int id);

    List<MediaDetails> searchMedia(MediaSearchCriteria criteria, int requestingUserId);

    MediaDetails getDetailedMedia(int id, int requestingUserId);

    boolean addFavorite(int mediaId, int userId);

    boolean removeFavorite(int mediaId, int userId);

    List<MediaDetails> listFavorites(int userId);

    List<MediaDetails> recommendMedia(int userId);
}
