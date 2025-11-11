package com.amit.mymarket.item.usecase.impl;

import com.amit.mymarket.item.api.dto.CreateItemForm;
import com.amit.mymarket.item.api.dto.ItemView;
import com.amit.mymarket.item.api.dto.UpdateItemForm;
import com.amit.mymarket.item.api.mapper.ItemMapper;
import com.amit.mymarket.item.domain.entity.Item;
import com.amit.mymarket.item.service.ItemManagementService;
import com.amit.mymarket.item.usecase.ItemManagementUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ItemManagementUseCaseFacade implements ItemManagementUseCase {

    private final ItemManagementService itemManagementService;

    private final ItemMapper itemMapper;

    @Autowired
    public ItemManagementUseCaseFacade(ItemManagementService itemManagementService, ItemMapper itemMapper) {
        this.itemManagementService = itemManagementService;
        this.itemMapper = itemMapper;
    }

    @Override
    public ItemView createItemAndOptionallyUploadImage(CreateItemForm createItemForm, MultipartFile file) {
        Item item = this.itemMapper.toItem(createItemForm);
        return this.itemMapper.toItemView(this.itemManagementService.createItemAndOptionallyUploadImage(item, file));
    }

    @Override
    public void replacePrimaryItemImage(long itemId, MultipartFile file) {
        this.itemManagementService.replacePrimaryItemImage(itemId, file);
    }

    @Override
    public void updateItemAttributes(long itemId, UpdateItemForm updateItemForm) {
        Item item = this.itemMapper.toItem(updateItemForm);
        this.itemMapper.toItemView(this.itemManagementService.updateItemAttributes(itemId, item));
    }

    @Override
    public void deleteItemCompletely(long itemId) {
        this.itemManagementService.deleteItemCompletely(itemId);
    }

    @Override
    public ItemView fetchItemById(long itemId) {
        return this.itemMapper.toItemView(this.itemManagementService.fetchItemById(itemId));
    }

}
