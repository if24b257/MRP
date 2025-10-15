package org.SalimMRP.business;

import org.SalimMRP.persistence.models.Media;

import java.util.List;

public interface MediaService {

    boolean createMedia(Media media);

    List<Media> getAllMedia();

    Media getMediaById(int id);

    boolean updateMedia(Media media);

    boolean deleteMedia(int id);
}
