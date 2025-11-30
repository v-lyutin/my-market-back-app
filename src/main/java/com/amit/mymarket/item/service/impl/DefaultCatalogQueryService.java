package com.amit.mymarket.item.service.impl;

import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.repository.ItemRepository;
import com.amit.mymarket.item.repository.projection.ItemWithQuantity;
import com.amit.mymarket.item.service.CatalogQueryService;
import com.amit.mymarket.item.service.util.CatalogPageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultCatalogQueryService implements CatalogQueryService {

    private final ItemRepository itemRepository;

    private final CartItemRepository cartItemRepository;

    @Autowired
    public DefaultCatalogQueryService(ItemRepository itemRepository, CartItemRepository cartItemRepository) {
        this.itemRepository = itemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public Mono<Page<Item>> getCatalogPage(String searchQuery, SortType sortType, int pageNumber, int pageSize) {
        CatalogPageRequest catalogPageRequest = CatalogPageRequest.of(searchQuery, sortType, pageNumber, pageSize);

        Mono<List<ItemWithQuantity>> itemsWithQuantity = this.itemRepository.searchItemsWithQuantity(
                        null,
                        catalogPageRequest.searchQuery(),
                        catalogPageRequest.sort(),
                        catalogPageRequest.limit(),
                        catalogPageRequest.offset()
                )
                .collectList();

        Mono<Long> totalItemsCount = this.itemRepository.countItemsBySearchQuery(catalogPageRequest.searchQuery());

        return Mono.zip(itemsWithQuantity, totalItemsCount)
                .flatMap(tuple -> {
                    List<ItemWithQuantity> rows = tuple.getT1();
                    long totalCount = tuple.getT2();

                    if (rows.isEmpty()) {
                        return Mono.just(Page.empty(catalogPageRequest.pageable()));
                    }

                    List<Long> itemIds = rows.stream().map(ItemWithQuantity::id).toList();

                    return this.itemRepository.findAllById(itemIds)
                            .collectMap(Item::getId)
                            .map(itemsById -> {
                                List<Item> items = itemIds.stream()
                                        .map(itemsById::get)
                                        .filter(Objects::nonNull)
                                        .toList();

                                return new PageImpl<>(items, catalogPageRequest.pageable(), totalCount);
                            });
                });
    }

    @Override
    public Mono<Integer> getCartQuantityForItem(String sessionId, long itemId) {
        return this.itemRepository.findItemWithQuantity(itemId, sessionId)
                .map(itemWithQuantity -> Optional.ofNullable(itemWithQuantity.quantity()).orElse(0))
                .defaultIfEmpty(0);
    }

    @Override
    public Mono<Map<Long, Integer>> getCartQuantitiesForItems(String sessionId, List<Long> itemIds) {
        if (CollectionUtils.isEmpty(itemIds)) {
            return Mono.just(Collections.emptyMap());
        }

        Set<Long> requestedItemIds = new HashSet<>(itemIds);

        return this.cartItemRepository.findCartItems(sessionId)
                .collectList()
                .map(cartItemRows -> {
                    Map<Long, Integer> itemQuantities = cartItemRows.stream()
                            .filter(cartItems -> requestedItemIds.contains(cartItems.id()))
                            .collect(Collectors.toMap(
                                    CartItemRow::id,
                                    cartItem -> Optional.ofNullable(cartItem.quantity()).orElse(0)
                            ));
                    itemIds.stream()
                            .filter(Objects::nonNull)
                            .forEach(id -> itemQuantities.putIfAbsent(id, 0));
                    return itemQuantities;
                });
    }

    @Override
    public Mono<Item> getItemById(long itemId) {
        return this.itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Item not found: id=" + itemId)));
    }

}
