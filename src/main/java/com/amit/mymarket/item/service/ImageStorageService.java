package com.amit.mymarket.item.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    /**
     * Stores the image for a specific item and returns a storage key (to be saved as imgPath).
     * Recommended key pattern: items/{itemId}/{uuid}/{variant}.ext
     */
    String storeItemImage(long itemId, MultipartFile multipartFile);

    /**
     * Deletes an object by its key. Implementations may ignore missing objects.
     */
    void deleteObjectByKey(String key);

    /**
     * Builds a public URL for the key if direct linking is required.
     * Implementations may simply return the key when a CDN/static prefix is added elsewhere.
     */
    default String buildPublicUrl(String key) {
        return key;
    }

}
