package org.SalimMRP.persistence;

import org.SalimMRP.persistence.models.Media;

import java.util.List;

// Abstraktion für alle Datenbankzugriffe rund um Medien.
public interface MediaRepository {

    boolean save(Media media);

    List<Media> findAll();

    Media findById(int id);

    boolean update(Media media);

    boolean delete(int id);
}
