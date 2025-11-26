package com.amit.mymarket.item.usecase.impl;

import com.amit.mymarket.cart.service.CartCommandService;
import com.amit.mymarket.item.api.dto.Paging;
import com.amit.mymarket.item.api.dto.CatalogPageDto;
import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.mapper.ItemMapper;
import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.api.type.ItemAction;
import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.service.CatalogQueryService;
import com.amit.mymarket.item.usecase.ItemUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ItemUseCaseFacade implements ItemUseCase {

    private final CatalogQueryService catalogQueryService;

    private final CartCommandService cartCommandService;

    private final ItemMapper itemMapper;

    @Autowired
    public ItemUseCaseFacade(CatalogQueryService catalogQueryService,
                             CartCommandService cartCommandService,
                             ItemMapper itemMapper) {
        this.catalogQueryService = catalogQueryService;
        this.cartCommandService = cartCommandService;
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogPageDto getCatalogPage(String sessionId, String search, SortType sort, int pageNumber, int pageSize) {
        Page<Item> page = this.catalogQueryService.getCatalogPage(search, sort, pageNumber, pageSize);
        List<Item> items = page.getContent();

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, Integer> quantities = this.catalogQueryService.getCartQuantitiesForItems(sessionId, itemIds);

        List<ItemInfoView> itemInfoViews = items.stream()
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
        Item item = this.catalogQueryService.getItemById(itemId);
        int count = this.catalogQueryService.getCartQuantityForItem(sessionId, itemId);
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
