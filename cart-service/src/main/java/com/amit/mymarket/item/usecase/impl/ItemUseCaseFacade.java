package com.amit.mymarket.item.usecase.impl;

import com.amit.mymarket.cart.service.CartCommandService;
import com.amit.mymarket.item.api.dto.CatalogPageDto;
import com.amit.mymarket.item.api.dto.ItemInfoView;
import com.amit.mymarket.item.api.dto.Paging;
import com.amit.mymarket.item.api.mapper.ItemMapper;
import com.amit.mymarket.item.api.type.ItemAction;
import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.service.CatalogQueryService;
import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.usecase.ItemUseCase;
import com.amit.mymarket.item.usecase.util.PagingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Collections;
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
    public Mono<CatalogPageDto> getCatalogPage(String sessionId, String search, SortType sort, int pageNumber, int pageSize) {
        return this.catalogQueryService.getCatalogPage(search, sort, pageNumber, pageSize)
                .flatMap(page -> this.buildCatalogPageDto(sessionId, search, sort, pageNumber, pageSize, page));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ItemInfoView> getItem(String sessionId, long itemId) {
        Mono<Item> item = this.catalogQueryService.getItemById(itemId);
        Mono<Integer> quantity = this.catalogQueryService.getCartQuantityForItem(sessionId, itemId);

        return Mono.zip(item, quantity)
                .map(tuple -> this.itemMapper.toItemInfoView(tuple.getT1(), tuple.getT2()));
    }

    @Override
    @Transactional
    public Mono<Void> mutateItem(String sessionId, long itemId, ItemAction itemAction) {
        return switch (itemAction) {
            case PLUS -> this.cartCommandService.incrementCartItemQuantity(sessionId, itemId);
            case MINUS -> this.cartCommandService.decrementCartItemQuantityOrDelete(sessionId, itemId);
        };
    }

    private Mono<CatalogPageDto> buildCatalogPageDto(String sessionId,
                                                     String search,
                                                     SortType sort,
                                                     int pageNumber,
                                                     int pageSize,
                                                     Page<Item> page) {
        List<Item> items = page.getContent();
        Paging paging = new Paging(pageSize, pageNumber, page.hasPrevious(), page.hasNext());
        if (items.isEmpty()) {
            return Mono.just(
                    new CatalogPageDto(
                            Collections.emptyList(),
                            paging,
                            search,
                            sort
                    )
            );
        }
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        return this.catalogQueryService.getCartQuantitiesForItems(sessionId, itemIds)
                .map(quantities -> this.toCatalogPageDto(items, quantities, paging, search, sort));
    }

    private CatalogPageDto toCatalogPageDto(List<Item> items,
                                            Map<Long, Integer> quantities,
                                            Paging paging,
                                            String search,
                                            SortType sort) {
        List<ItemInfoView> itemInfoViews = items.stream()
                .map(item -> {
                    int quantity = quantities.getOrDefault(item.getId(), 0);
                    return this.itemMapper.toItemInfoView(item, quantity);
                })
                .toList();
        List<List<ItemInfoView>> chunkedItemInfoViews = PagingUtils.chunk(itemInfoViews, 3);
        return new CatalogPageDto(chunkedItemInfoViews, paging, search, sort);
    }

}
