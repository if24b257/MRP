package org.SalimMRP.business;

import org.SalimMRP.persistence.MediaRepository;
import org.SalimMRP.persistence.models.Media;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MediaServiceTest {

    private InMemoryMediaRepository mediaRepository;
    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaRepository = new InMemoryMediaRepository();
        mediaService = new DefaultMediaService(mediaRepository);
    }

    @Test
    @DisplayName("createMedia persists valid media entry")
    void createMediaPersistsEntity() {
        Media media = buildMedia("Matrix", "movie", 1);

        assertTrue(mediaService.createMedia(media));
        assertEquals(1, mediaRepository.size());
    }

    @Test
    @DisplayName("createMedia rejects missing title")
    void createMediaRejectsMissingTitle() {
        Media media = buildMedia(null, "movie", 1);

        assertFalse(mediaService.createMedia(media));
    }

    @Test
    @DisplayName("createMedia rejects missing media type")
    void createMediaRejectsMissingMediaType() {
        Media media = buildMedia("Title", null, 1);

        assertFalse(mediaService.createMedia(media));
    }

    @Test
    @DisplayName("createMedia rejects missing creator")
    void createMediaRejectsMissingCreator() {
        Media media = buildMedia("Title", "movie", 0);

        assertFalse(mediaService.createMedia(media));
    }

    @Test
    @DisplayName("updateMedia rejects media without id")
    void updateMediaRejectsMissingId() {
        Media media = buildMedia("Title", "movie", 1);

        assertFalse(mediaService.updateMedia(media));
    }

    @Test
    @DisplayName("updateMedia delegates update to repository")
    void updateMediaUpdatesStoredEntity() {
        Media stored = buildMedia("Old", "movie", 1);
        mediaRepository.save(stored);

        Media update = buildMedia("New", "movie", 1);
        update.setId(stored.getId());

        assertTrue(mediaService.updateMedia(update));
        assertEquals("New", mediaRepository.getStoredById(stored.getId()).getTitle());
    }

    @Test
    @DisplayName("deleteMedia removes entity")
    void deleteMediaRemovesEntity() {
        Media stored = buildMedia("Delete me", "game", 1);
        mediaRepository.save(stored);

        assertTrue(mediaService.deleteMedia(stored.getId()));
        assertEquals(0, mediaRepository.size());
    }

    @Test
    @DisplayName("getAllMedia returns list copy")
    void getAllMediaReturnsListCopy() {
        mediaRepository.save(buildMedia("One", "movie", 1));
        mediaRepository.save(buildMedia("Two", "series", 2));

        List<Media> list = mediaService.getAllMedia();

        assertEquals(2, list.size());
        list.clear();
        assertEquals(2, mediaRepository.size(), "List modifications should not affect repository");
    }

    @Test
    @DisplayName("getMediaById returns stored entity")
    void getMediaByIdReturnsStoredEntity() {
        Media stored = buildMedia("Lookup", "movie", 1);
        mediaRepository.save(stored);

        Media found = mediaService.getMediaById(stored.getId());

        assertNotNull(found);
        assertEquals(stored.getTitle(), found.getTitle());
    }

    private Media buildMedia(String title, String type, int creatorId) {
        Media media = new Media();
        media.setTitle(title);
        media.setMediaType(type);
        media.setCreatedByUserId(creatorId);
        media.setDescription("");
        return media;
    }

    private static class InMemoryMediaRepository implements MediaRepository {
        private final Map<Integer, Media> storage = new HashMap<>();
        private int nextId = 1;

        @Override
        public boolean save(Media media) {
            if (media == null) {
                return false;
            }
            Media stored = cloneMedia(media);
            stored.setId(nextId++);
            storage.put(stored.getId(), stored);
            media.setId(stored.getId());
            return true;
        }

        @Override
        public List<Media> findAll() {
            List<Media> list = new ArrayList<>();
            for (Media media : storage.values()) {
                list.add(cloneMedia(media));
            }
            return list;
        }

        @Override
        public Media findById(int id) {
            Media media = storage.get(id);
            return media == null ? null : cloneMedia(media);
        }

        @Override
        public boolean update(Media media) {
            if (media == null || !storage.containsKey(media.getId())) {
                return false;
            }
            storage.put(media.getId(), cloneMedia(media));
            return true;
        }

        @Override
        public boolean delete(int id) {
            return storage.remove(id) != null;
        }

        int size() {
            return storage.size();
        }

        Media getStoredById(int id) {
            return storage.get(id);
        }

        private Media cloneMedia(Media source) {
            Media clone = new Media();
            clone.setId(source.getId());
            clone.setTitle(source.getTitle());
            clone.setDescription(source.getDescription());
            clone.setMediaType(source.getMediaType());
            clone.setCreatedByUserId(source.getCreatedByUserId());
            return clone;
        }
    }
}
