package com.amit.mymarket.item.service;

import com.amit.mymarket.item.domain.entity.Item;
import org.springframework.web.multipart.MultipartFile;

public interface ItemManagementService {

    /**
     * Creates an item and optionally uploads a primary image.
     * If image is provided, its storage key is saved into Item.imagePath.
     */
    Item createItemAndOptionallyUploadImage(Item itemToCreate, MultipartFile file);

    /**
     * Replaces primary image of an item:
     * - uploads the new image,
     * - updates Item.imagePath,
     * - optionally removes the previous image (implementation-defined).
     * Returns the new imgPath (storage key or relative path).
     */
    String replacePrimaryItemImage(long itemId, MultipartFile file);

    /**
     * Partial update of item attributes + null values mean “do not change”.
     */
    Item updateItemAttributes(long itemId, Item itemToUpdate);

    /**
     * Deletes the item + implementation may also delete related images from storage.
     */
    void deleteItemCompletely(long itemId);

}
