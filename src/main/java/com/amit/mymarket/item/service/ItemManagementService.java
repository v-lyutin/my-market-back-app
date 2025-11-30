package com.amit.mymarket.item.service;

import com.amit.mymarket.item.entity.Item;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface ItemManagementService {

    /**
     * Creates an item and optionally uploads a primary image.
     * If image is provided, its storage key is saved into Item.imagePath.
     */
    Mono<Item> createItemAndOptionallyUploadImage(Item itemToCreate, FilePart file);

    /**
     * Replaces primary image of an item:
     * - uploads the new image,
     * - updates Item.imagePath,
     * - optionally removes the previous image (implementation-defined).
     * Returns the new imgPath (storage key or relative path).
     */
    Mono<Void> replaceItemImage(long itemId, FilePart file);

    /**
     * Partial update of item attributes + null values mean “do not change”.
     */
    Mono<Item> updateItemAttributes(long itemId, Item itemToUpdate);

    /**
     * Deletes the item + implementation may also delete related images from storage.
     */
    Mono<Void> deleteItem(long itemId);

    Mono<Item> getItemById(long itemId);

}
