package com.amit.mymarket.common.service.strategy;

import com.amit.mymarket.common.service.util.PathSpecification;

import java.util.Map;
import java.util.UUID;

public enum KeyNamingStrategyEnum implements KeyNamingStrategy {

    /**
     * Default structure:
     * namespace/{ownerId}/{uuid}/{variant}.ext
     * Example: items/42/550e8400-e29b-41d4-a716-446655440000/original.jpg
     */
    DEFAULT_STRATEGY {
        @Override
        public String buildKey(PathSpecification pathSpecification, String mimeType) {
            String extension = extensionByMime(mimeType);
            String uuid = UUID.randomUUID().toString();
            if (pathSpecification.ownerId() == null || pathSpecification.ownerId().isBlank()) {
                return "%s/%s/%s.%s".formatted(pathSpecification.namespace(), uuid, pathSpecification.variant(), extension);
            }
            return "%s/%s/%s/%s.%s".formatted(
                    pathSpecification.namespace(),
                    pathSpecification.ownerId(),
                    uuid,
                    pathSpecification.variant(),
                    extension
            );
        }
    },

    /**
     * Flat key structure (no namespace or owner):
     * {uuid}-{variant}.ext
     * Example: 550e8400-e29b-41d4-a716-446655440000-original.png
     */
    FLAT_STRATEGY {
        @Override
        public String buildKey(PathSpecification pathSpecification, String mimeType) {
            String extension = extensionByMime(mimeType);
            return "%s-%s.%s".formatted(UUID.randomUUID(), pathSpecification.variant(), extension);
        }
    };

    private static final Map<String, String> EXTENSION_BY_MIME = Map.of(
            "image/jpeg", "jpg",
            "image/png",  "png",
            "image/webp", "webp"
    );

    protected static String extensionByMime(String mimeType) {
        return EXTENSION_BY_MIME.getOrDefault(mimeType, "bin");
    }

}
