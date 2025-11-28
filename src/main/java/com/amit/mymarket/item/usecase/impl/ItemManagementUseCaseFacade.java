package com.amit.mymarket.item.usecase.impl;

import com.amit.mymarket.item.api.dto.CreateItemForm;
import com.amit.mymarket.item.api.dto.ItemView;
import com.amit.mymarket.item.api.dto.UpdateItemForm;
import com.amit.mymarket.item.api.mapper.ItemMapper;
import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.service.ItemManagementService;
import com.amit.mymarket.item.usecase.ItemManagementUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

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
    @Transactional
    public Mono<ItemView> createItemAndOptionallyUploadImage(CreateItemForm createItemForm, FilePart file) {
        Item item = this.itemMapper.toItem(createItemForm);
        return this.itemManagementService.createItemAndOptionallyUploadImage(item, file)
                .map(this.itemMapper::toItemView);
    }

    @Override
    @Transactional
    public Mono<Void> replaceItemImage(long itemId, FilePart file) {
        return this.itemManagementService.replaceItemImage(itemId, file);
    }

    @Override
    @Transactional
    public Mono<Void> updateItemAttributes(long itemId, UpdateItemForm updateItemForm) {
        Item item = this.itemMapper.toItem(updateItemForm);
        return this.itemManagementService.updateItemAttributes(itemId, item).then();
    }

    @Override
    @Transactional
    public Mono<Void> deleteItem(long itemId) {
        return this.itemManagementService.deleteItem(itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ItemView> getItemById(long itemId) {
        return this.itemManagementService.getItemById(itemId).map(this.itemMapper::toItemView);
    }

}
