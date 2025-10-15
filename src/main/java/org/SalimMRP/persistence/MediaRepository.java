package org.SalimMRP.persistence;

import org.SalimMRP.persistence.models.Media;

import java.util.List;

public interface MediaRepository {

    boolean save(Media media);

    List<Media> findAll();

    Media findById(int id);

    boolean update(Media media);

    boolean delete(int id);
}
