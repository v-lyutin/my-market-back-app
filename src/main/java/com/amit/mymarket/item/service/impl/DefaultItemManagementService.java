package com.amit.mymarket.item.service.impl;

import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.common.exception.ServiceException;
import com.amit.mymarket.common.service.MediaStorageService;
import com.amit.mymarket.common.service.util.PathSpecification;
import com.amit.mymarket.item.domain.entity.Item;
import com.amit.mymarket.item.repository.ItemRepository;
import com.amit.mymarket.item.service.ItemManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
    public Item createItemAndOptionallyUploadImage(Item itemToCreate, MultipartFile file) {
        if (itemToCreate == null) {
            throw new ServiceException("Item is null");
        }
        Item item = this.itemRepository.saveAndFlush(itemToCreate);
        if (file != null && !file.isEmpty()) {
            String imagePath = this.mediaStorageService.saveMediaFile(file, this.buildItemPath(item.getId()));
            item.setImagePath(imagePath);
            item = this.itemRepository.save(item);
        }
        return item;
    }

    @Override
    @Transactional
    public void replacePrimaryItemImage(long itemId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Image file is null/empty");
        }
        Item item = this.itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        String oldImagePath = item.getImagePath();
        String newImagePath = this.mediaStorageService.saveMediaFile(file, buildItemPath(itemId));
        item.setImagePath(newImagePath);
        this.itemRepository.save(item);
        if (StringUtils.hasText(oldImagePath)) {
            this.mediaStorageService.deleteMediaFile(oldImagePath);
        }
    }

    @Override
    @Transactional
    public Item updateItemAttributes(long itemId, Item itemToUpdate) {
        Item item = this.itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
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
    }

    @Override
    @Transactional
    public void deleteItemCompletely(long itemId) {
        Item item = this.itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        String imagePath = item.getImagePath();
        this.itemRepository.delete(item);
        if (StringUtils.hasText(imagePath)) {
            this.mediaStorageService.deleteMediaFile(imagePath);
        }
    }

    @Override
    public Item fetchItemById(long itemId) {
        return this.itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
    }

    private PathSpecification buildItemPath(long itemId) {
        return PathSpecification.of("items", Long.toString(itemId), "original");
    }

}
