package com.amit.mymarket.item.service.impl;

import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.common.exception.ServiceException;
import com.amit.mymarket.common.service.MediaStorageService;
import com.amit.mymarket.common.service.util.PathSpecification;
import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.repository.ItemRepository;
import com.amit.mymarket.item.service.ItemManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Service
public class DefaultItemManagementService implements ItemManagementService {

    private final ItemRepository itemRepository;

    private final MediaStorageService mediaStorageService;

    @Autowired
    public DefaultItemManagementService(ItemRepository itemRepository, MediaStorageService mediaStorageService) {
        this.itemRepository = itemRepository;
        this.mediaStorageService = mediaStorageService;
    }

    @Override
    @Transactional
    public Mono<Item> createItemAndOptionallyUploadImage(Item itemToCreate, FilePart file) {
        if (itemToCreate == null) {
            return Mono.error(new ServiceException("Item is null"));
        }

        return this.itemRepository.save(itemToCreate)
                .flatMap(savedItem -> {
                    if (file == null) {
                        return Mono.just(savedItem);
                    }
                    PathSpecification path = buildItemPath(savedItem.getId());
                    return this.mediaStorageService.saveMediaFile(file, path)
                            .flatMap(imagePath -> {
                                savedItem.setImagePath(imagePath);
                                return this.itemRepository.save(savedItem);
                            });
                });
    }

    @Override
    @Transactional
    public Mono<Void> replaceItemImage(long itemId, FilePart file) {
        if (file == null) {
            return Mono.error(new ServiceException("Image file is null"));
        }

        return this.itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Item not found: " + itemId)))
                .flatMap(item -> {
                    String oldImagePath = item.getImagePath();
                    PathSpecification path = buildItemPath(itemId);

                    return this.mediaStorageService.saveMediaFile(file, path)
                            .flatMap(newImagePath -> {
                                item.setImagePath(newImagePath);
                                return this.itemRepository.save(item)
                                        .then(Mono.defer(() -> {
                                            if (StringUtils.hasText(oldImagePath)) {
                                                return this.mediaStorageService.deleteMediaFile(oldImagePath);
                                            }
                                            return Mono.empty();
                                        }));
                            });
                });
    }

    @Override
    @Transactional
    public Mono<Item> updateItemAttributes(long itemId, Item itemToUpdate) {
        return this.itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Item not found: " + itemId)))
                .flatMap(item -> {
                    if (itemToUpdate.getTitle() != null) {
                        item.setTitle(itemToUpdate.getTitle());
                    }
                    if (itemToUpdate.getDescription() != null) {
                        item.setDescription(itemToUpdate.getDescription());
                    }
                    if (itemToUpdate.getPriceMinor() != null) {
                        item.setPriceMinor(itemToUpdate.getPriceMinor());
                    }
                    return this.itemRepository.save(item);
                });
    }

    @Override
    @Transactional
    public Mono<Void> deleteItem(long itemId) {
        return this.itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Item not found: " + itemId)))
                .flatMap(item -> {
                    String imagePath = item.getImagePath();
                    return this.itemRepository.delete(item)
                            .then(Mono.defer(() -> {
                                if (StringUtils.hasText(imagePath)) {
                                    return this.mediaStorageService.deleteMediaFile(imagePath);
                                }
                                return Mono.empty();
                            }));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Item> getItemById(long itemId) {
        return this.itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Item not found: " + itemId)));
    }

    private PathSpecification buildItemPath(long itemId) {
        return PathSpecification.of("items", Long.toString(itemId), "original");
    }

}
