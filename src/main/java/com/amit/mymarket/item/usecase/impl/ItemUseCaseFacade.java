package com.amit.mymarket.item.usecase.impl;

import com.amit.mymarket.cart.service.CartCommandService;
import com.amit.mymarket.common.service.MediaStorageService;
import com.amit.mymarket.common.util.Paging;
import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.dto.CatalogPageDto;
import com.amit.mymarket.item.api.mapper.ItemMapper;
import com.amit.mymarket.item.domain.entity.Item;
import com.amit.mymarket.item.domain.type.ItemAction;
import com.amit.mymarket.item.domain.type.SortType;
import com.amit.mymarket.item.service.CatalogQueryService;
import com.amit.mymarket.item.usecase.ItemUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ItemUseCaseFacade implements ItemUseCase {

    private final CatalogQueryService catalogQueryService;

    private final CartCommandService cartCommandService;

    private final MediaStorageService mediaStorageService;

    private final ItemMapper itemMapper;

    @Autowired
    public ItemUseCaseFacade(CatalogQueryService catalogQueryService,
                             CartCommandService cartCommandService,
                             MediaStorageService mediaStorageService,
                             ItemMapper itemMapper) {
        this.catalogQueryService = catalogQueryService;
        this.cartCommandService = cartCommandService;
        this.mediaStorageService = mediaStorageService;
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogPageDto getCatalogPage(String sessionId, String search, SortType sort, int pageNumber, int pageSize) {
        Page<Item> page = this.catalogQueryService.fetchCatalogPage(search, sort, pageNumber, pageSize);
        List<Item> items = page.getContent();

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, Integer> quantities = this.catalogQueryService.fetchCartQuantitiesForItems(sessionId, itemIds);

        List<ItemInfoView> itemInfoViews = items.stream()
                .peek(item -> {
                    if (StringUtils.hasText(item.getImagePath())) {
                        item.setImagePath(this.mediaStorageService.buildPublicUrl(item.getImagePath()));
                    }
                })
                .map(item -> this.itemMapper.toItemInfoView(item, quantities.getOrDefault(item.getId(), 0)))
                .toList();

        List<List<ItemInfoView>> chunkedItemInfoViews = chunk(itemInfoViews, 3);

        Paging paging = new Paging(
                pageSize,
                pageNumber,
                page.hasPrevious(),
                page.hasNext()
        );

        return new CatalogPageDto(chunkedItemInfoViews, paging, search, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemInfoView getItem(String sessionId, long itemId) {
        Item item = this.catalogQueryService.fetchItemOrThrow(itemId);
        if (StringUtils.hasText(item.getImagePath())) {
            item.setImagePath(this.mediaStorageService.buildPublicUrl(item.getImagePath()));
        }
        int count = this.catalogQueryService.fetchCartQuantityForItem(sessionId, itemId);
        return this.itemMapper.toItemInfoView(item, count);
    }

    @Override
    @Transactional
    public void mutateItem(String sessionId, long itemId, ItemAction itemAction) {
        switch (itemAction) {
            case PLUS -> this.cartCommandService.incrementCartItemQuantity(sessionId, itemId);
            case MINUS -> this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);
        }
    }

    private static <T> List<List<T>> chunk(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return chunks;
    }

}
