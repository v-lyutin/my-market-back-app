package com.amit.mymarket.item.service.impl;

import com.amit.mymarket.cart.repository.CartItemRepository;
import com.amit.mymarket.cart.repository.projection.CartItemRow;
import com.amit.mymarket.common.exception.ResourceNotFoundException;
import com.amit.mymarket.item.entity.Item;
import com.amit.mymarket.item.service.type.SortType;
import com.amit.mymarket.item.repository.ItemRepository;
import com.amit.mymarket.item.repository.projection.ItemWithQuantity;
import com.amit.mymarket.item.service.CatalogQueryService;
import com.amit.mymarket.item.service.util.SearchNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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
    public Page<Item> fetchCatalogPage(String search, SortType sortType, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(pageNumber - 1, 0), Math.max(pageSize, 1));
        Page<ItemWithQuantity> itemWithCountRowPage = this.itemRepository.findCatalogWithCounts(
                null,
                SearchNormalizer.normalizeSearch(search),
                sortType.name(),
                pageable
        );
        List<Long> itemIds = itemWithCountRowPage.getContent().stream()
                .map(ItemWithQuantity::getId)
                .toList();
        if (itemIds.isEmpty()) {
            return Page.empty(pageable);
        }
        Map<Long, Item> itemsById = this.itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        List<Item> items = itemIds.stream()
                .map(itemsById::get)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(items, pageable, itemWithCountRowPage.getTotalElements());
    }

    @Override
    public int fetchCartQuantityForItem(String sessionId, long itemId) {
        ItemWithQuantity itemWithCountRow = this.itemRepository.findItemWithCount(itemId, sessionId);
        if (itemWithCountRow == null) {
            return 0;
        }
        Integer cartQuantity = itemWithCountRow.getCount();
        return cartQuantity == null ? 0 : cartQuantity;
    }

    @Override
    public Map<Long, Integer> fetchCartQuantitiesForItems(String sessionId, List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> requestedItemIds = new HashSet<>(itemIds);
        List<CartItemRow> cartItemRows = this.cartItemRepository.findCartItems(sessionId);
        Map<Long, Integer> itemQuantities = cartItemRows.stream()
                .filter(cartItemRow -> requestedItemIds.contains(cartItemRow.getId()))
                .collect(Collectors.toMap(
                        CartItemRow::getId,
                        row -> Optional.ofNullable(row.getCount()).orElse(0)
                ));
        itemIds.stream()
                .filter(Objects::nonNull)
                .forEach(id -> itemQuantities.putIfAbsent(id, 0));
        return itemQuantities;
    }

    @Override
    public Item fetchItemOrThrow(long itemId) {
        return this.itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: id=" + itemId));
    }

}
