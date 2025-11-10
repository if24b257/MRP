package org.SalimMRP.persistence;

import java.util.List;

// Verwaltet Favoriten-Verknüpfungen zwischen Benutzern und Medien.
public interface FavoriteRepository {

    boolean addFavorite(int userId, int mediaId);

    boolean removeFavorite(int userId, int mediaId);

    boolean isFavorite(int userId, int mediaId);

    List<Integer> findMediaIdsByUser(int userId);

    int countFavoritesForMedia(int mediaId);
}
